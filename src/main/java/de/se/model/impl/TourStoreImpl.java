package de.se.model.impl;


import de.se.DB.TimeBetweenStopsPersistence;
import de.se.DB.TourPersistence;
import de.se.DB.hibernate_models.Timebetweenstopsdb;
import de.se.DB.impl.TimeBetweenStopsPersistenceImpl;
import de.se.DB.hibernate_models.Tourdb;
import de.se.DB.impl.TourPersistenceImpl;
import de.se.TransportAPI.TransportAPIHandler;
import de.se.data.*;
import de.se.data.Position;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.TourStore;
import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

public class TourStoreImpl implements TourStore {

    /**
     * The number of days in the past which should be filled with the "normal" tour routine
     */
    public int NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST = 4;

    /**
     * The number of days which should be fieeld which tours starting in the past from:
     * "NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST"
     * E.g.:
     * NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST = 4
     * NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL = 14
     * -> 4 Days in the past, the current day and 9 in the future
     */
    public int NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL = 14;

    TourPersistence tourPersistence;
    TimeBetweenStopsPersistence betweenPersistence;
    List<Tour> tours;
    List<Timebetweenstopsdb> timebetweenstopsdbs;
    private int uniqueNumberOfToursId = 0;
    private Logger logger = Logger.getLogger(TourStoreImpl.class);

    public TourStoreImpl() {
        tourPersistence = new TourPersistenceImpl();
        betweenPersistence = new TimeBetweenStopsPersistenceImpl();
    }

    public void setPersister(TourPersistence tourPers, TimeBetweenStopsPersistence tbsPers) {
        betweenPersistence = tbsPers;
        tourPersistence = tourPers;
    }

    @Override
    public void initialize() {
        initDBs();

        //initialization of the stopTime attribute from tours
        for (Tour tour : tours) {
            initializeTourTime(tour);
        }
    }

    private void initializeTourTime(Tour tour) {
        Network network = ServiceRegistry.getService(Network.class);

        List<Stop> stops = network.getLine(tour.getLine()).getRoute().getStopSequence();
        if (stops.get(0).getId().equals(tour.getEndStop())) { //reverse list if it is the wrong way round
            Collections.reverse(stops);
        }
        Map<Stop, Date> stopTime = new HashMap<Stop, Date>();
        Date timeOnStop = tour.getStartTime();
        String lastStop = stops.get(0).getId();
        stopTime.put(stops.get(0), timeOnStop);
        for (int i = 1; i < stops.size(); i++) { //skip first stop
            String currentStop = stops.get(i).getId();
            try {
                long timeBetweenStops = getTimeBetweenTwoConnectedStops(lastStop, currentStop);
                timeOnStop = new Date(timeOnStop.getTime() + timeBetweenStops * 60 * 1000);
                stopTime.put(stops.get(i), timeOnStop);
                lastStop = stops.get(i).getId();
            } catch (Exception e) {
                logger.error("Could not initialize the tour times for tour: " + tour.getId());
                e.printStackTrace();
            }
        }
        tour.setStopTime(stopTime);
    }

    private long getTimeBetweenTwoConnectedStops(String startStop, String nextStop) throws Exception {
        for (Timebetweenstopsdb time : timebetweenstopsdbs) {
            if ((time.getStartstop().equals(startStop) && time.getNextstop().equals(nextStop)) || (time.getStartstop().equals(nextStop) && time.getNextstop().equals(startStop))) {
                return time.getTimeinminutes();
            }
        }

        throw new Exception("There is no connection between this stops!!");
    }

    /**
     * Fills the tables when it is not already filled
     */
    public void initDBs() {
        timebetweenstopsdbs = betweenPersistence.fetchTimeBetweenStops();

        if (timebetweenstopsdbs.size() == 0) {
            saveTimeBetweenStops(getAllBetweensFromAPI());
            timebetweenstopsdbs = betweenPersistence.fetchTimeBetweenStops();
        }

        tours = tourPersistence.fetchTours();
        uniqueNumberOfToursId = (tourPersistence.getMaxID() + 1) % (Integer.MAX_VALUE / 2);

        if (tours.size() == 0) {
            //saveTours(getAllToursFromAPIForOneDay()); // real tours for tubes
            try {
                saveToursWithoutChecking(getFabricatedTours());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("Could not save initial tours in database: " + e.getMessage());
            }
        }

        keepToursUpToDate();
        tours = tourPersistence.fetchTours();
    }

    @Override
    public List<Tour> getToursForLine(Line line) {
        List<Tour> returnTours = new ArrayList<>();
        for (Tour tour : tours) {
            if (tour != null && tour.getLine() != null && tour.getLine().equals(line.getId()))
                returnTours.add(tour);
        }
        return returnTours;
    }

    @Override
    public List<Tour> getToursForVehicle(String vehicleID) {
        List<Tour> returnTours = new ArrayList<>();
        Iterator<Tour> toursIterator = tours.iterator(); // use iterator to avoid ConcurrentModificationException
        while (toursIterator.hasNext()) {
            Tour tour = toursIterator.next();
            if (tour.getVehicle() != null && tour.getVehicle().equals(vehicleID))
                returnTours.add(tour);
        }
        return returnTours;
    }

    @Override
    public Tour getCurrentTourForVehicle(String vehicleID) {
        for (Tour tour : getToursForVehicle(vehicleID)) {
            Date startTime = tour.getStartTime();
            // vehicles should be shown on last stop 30-40 seconds after they arrived
            Date newEndTime = new Date(tour.getEndTime().getTime() + 40 * 1000);

            Date now = new Date();
            if ((startTime.getTime() == now.getTime() || startTime.before(now)) &&
                    (now.getTime() == newEndTime.getTime() || now.before(newEndTime))) {
                return tour; //there should only be one current tour
            }
        }

        //this.logger.debug("Could not find a current tour for vehicle with ID " + vehicleID);
        return null;
    }

    @Override
    public List<Tour> getCurrentTours(Line line) {
        List<Tour> currentLineTours = new ArrayList<Tour>();
        Date now = new Date();
        for (Tour tour : tours) {
            if (tour.getLine().equals(line.getId())) {
                if (tour.getStartTime().before(now) && tour.getEndTime().after(now)) {
                    currentLineTours.add(tour);
                }
            }
        }
        return currentLineTours;
    }

    @Override
    public List<Tour> getCurrentToursWithDelays(Line line, HashMap<String, Delay> vehicleDelays) {
        List<Tour> currentLineTours = new ArrayList<Tour>();
        Date now = new Date();
        Iterator<Tour> toursIterator = tours.iterator(); // use iterator to avoid ConcurrentModificationException
        while (toursIterator.hasNext()) {
            Tour tour = toursIterator.next();
            if (tour.getLine().equals(line.getId())) {
                String vehicleIdOfTour = tour.getVehicle();
                Delay delay = vehicleDelays.get(vehicleIdOfTour);
                int delayInSeconds = 0;
                if (delay != null) {
                    delayInSeconds = delay.getDelay();
                }

                Date newStartTime = new Date(tour.getStartTime().getTime() - (delayInSeconds * 1000));
                Date newEndTime = new Date(tour.getEndTime().getTime() - (delayInSeconds * 1000));

                if (newStartTime.before(now) && newEndTime.after(now)) {
                    currentLineTours.add(tour);
                }
            }
        }
        return currentLineTours;
    }

    @Override
    public List<Timebetweenstopsdb> getTimebetweenstops() {
        return timebetweenstopsdbs;
    }

    @Override
    public List<Tour> getAllExistingTours() {
        return tours;
    }

    @Override
    public void saveTours(List<Tour> toursToAdd) throws Exception {
        List<Tourdb> tourdbs = new ArrayList<>();
        int exceptions = 0;
        for (Tour tour : toursToAdd) {
            if (tour.getEndTime() == null) {
                long time = getEndtimeOfTour(tour);
                tour.getTourDB().setEndTime(new Timestamp(time));
            }

            if (!vehicleIsAvailable(tour, tour.getVehicle())) {
                exceptions++;
                continue; // do not add this tour
            }


            // if the id is -1 it will get a unique id from the persistence class
            this.initializeTourTime(tour);

            tourdbs.add(tour.getTourDB());
            this.tours.add(tour);
        }
        if (tourdbs.size() != 0) {
            tourPersistence.saveTourdbs(tourdbs);
        }
        if (exceptions > 0) {
            throw new Exception("Some tours could not be added because the vehicles are not available: " + exceptions + " of " +
                    toursToAdd.size() + " tours failed");
        }
    }

    /**
     * Helpermethod:
     * Saves all given Tours without the "normal" checking like in method "saveTour" or "saveTours"
     * Not checked: if the endtime is set and if the vehicle is available at this time
     * Usage: To initialise all tours in the beginning/initialization in method "initDBs()"
     *
     * @param toursToAdd: list of Tour objects which should be add to the database
     */
    private void saveToursWithoutChecking(List<Tour> toursToAdd) {
        List<Tourdb> tourdbs = new ArrayList<>();
        for (Tour tour : toursToAdd) {
            tourdbs.add(tour.getTourDB());
        }
        tourPersistence.saveTourdbs(tourdbs);
    }

    private void saveTimeBetweenStops(List<Timebetweenstopsdb> betweens) {
        for (Timebetweenstopsdb between : betweens) {
            betweenPersistence.saveTimeBetweenStops(between);
            timebetweenstopsdbs.add(between);
        }
    }

    /**
     * This method calculates all vehicles which are available to drive on a new tour.
     * Therefore we have to calculate the end of the given tour, based on beginning and line to drive.
     * Then we loop through all vehicles to check if they are occupied in a tour.
     * If not we add the vehicles id to the return list.
     *
     * @param startTime the start time of the requested tour
     * @param lineId    the requested line
     * @return a list of vehicle ids which are avaiable at the requested time.
     */
    @Override
    public List<String> checkForAvailableVehicles(Date startTime, String lineId) {
        Tour workTour = new Tour(-1, new Timestamp(startTime.getTime()), null, "", "", "", lineId);

        Timestamp endStamp = new Timestamp(getEndtimeOfTour(workTour));
        workTour.setEndTime(endStamp);

        return checkForAvailabelVehicles(workTour);
    }

    private List<String> checkForAvailabelVehicles(Tour workTour) {
        String type = ServiceRegistry.getService(Network.class).getLine(workTour.getLine()).getVehicleType();
        List<String> availableVehicles = new ArrayList<String>();
        FleetService fleet = ServiceRegistry.getService(FleetService.class);
        Collection<Vehicle> vehicles = fleet.getNotLiveVehicles();
        for (Vehicle vehicle : vehicles) {
            if (vehicle.getType().equalsIgnoreCase(type)) {
                if (vehicleIsAvailable(workTour, vehicle.getId())) {
                    availableVehicles.add(vehicle.getId());
                }
            }
        }
        return availableVehicles;
    }


    @Override
    public void saveTour(Tour tour) throws Exception {
        if (tour.getEndTime() == null) {
            long time = getEndtimeOfTour(tour);
            tour.getTourDB().setEndTime(new Timestamp(time));
        }

        if (!vehicleIsAvailable(tour, tour.getVehicle())) {
            throw new Exception("Vehicle not available");
        }

        // if the id is -1 it will get a unique id from the persistence class
        this.initializeTourTime(tour);
        tourPersistence.saveTour(tour);
        if (!tours.contains(tour)) {
            tours.add(tour);
        }
    }

    public long getEndtimeOfTour(Tour tour) {
        Network network = ServiceRegistry.getService(Network.class);
        List<Stop> stops = network.getLine(tour.getLine()).getRoute().getStopSequence();
        if (stops.get(0).getId().equals(tour.getEndStop())) { //reverse list if it is the wrong way round
            Collections.reverse(stops);
        }
        long timeOnStop = tour.getStartTime().getTime();
        String lastStop = stops.get(0).getId();
        for (int i = 1; i < stops.size(); ++i) {
            String currentStop = stops.get(i).getId();
            try {
                long timeBetweenStops = getTimeBetweenTwoConnectedStops(lastStop, currentStop);
                timeOnStop += timeBetweenStops * 60 * 1000;
                lastStop = stops.get(i).getId();
            } catch (Exception e) {
                logger.error("Could not calculate endTime of Tour: " + tour.getId());
                e.printStackTrace();
            }
        }
        return timeOnStop;
    }

    @Override
    public List<Tour> getAssignableToursInTimeRange(String vehicleId, long start, long end) {
        FleetService fleet = ServiceRegistry.getService(FleetService.class);
        Network network = ServiceRegistry.getService(Network.class);

        Vehicle targetVehicle = fleet.getVehicleForId(vehicleId);
        String type = targetVehicle.getType();

        List<Tour> vehicleTours = new ArrayList<>();

        //fetch all tours in the given timespan, and add them either to the vehicles tour or to the available tours
        List<Tour> toursInRange = new ArrayList<>();
        for (Tour tour : tours) {
            String lineType = network.getLine(tour.getLine()).getVehicleType();
            //this is separated because we need all tours which cut the time span
            if (tour.getVehicle() != null && tour.getVehicle().equals(vehicleId)) {
                if (tour.getStartTime().getTime() <= end && tour.getEndTime().getTime() >= start) {
                    vehicleTours.add(tour);
                }
                //for toursInRange we need all tours, which really are in the timespan
            } else if (lineType.equalsIgnoreCase(type) && tour.getStartTime().getTime() >= start && tour.getEndTime().getTime() <= end && !tour.getVehicle().equals(vehicleId)) {
                toursInRange.add(tour);
            }
        }

        //I tried streams. Looks a bit confusing but makes fun! :)
        List<Tour> filtered = toursInRange.stream()
                //filter the tours which are in the initial time span
                .filter(f -> {
                    //we have to filter the toursInRange if they cut somehow the vehicle tours.
                    return vehicleTours.stream()
                            .filter(v -> {
                                //we filter also tours in vehicle, which overlap the current filter tour.
                                if (v.getStartTime().getTime() <= f.getEndTime().getTime() && v.getEndTime().getTime() >= f.getStartTime().getTime()) {
                                    //if the tours overlap we return true to the tour v will stay in list
                                    return true;
                                }
                                //if they don't overlap we return false so v is removed from list
                                return false;
                            })
                            //now we have a list with all overlapping tours, and if this list is > 0 the f tour can't be assigned to the vehicle
                            //because the vehicle has already a tour at the specific time. So we return false to remove the item from filtered list
                            .collect(Collectors.toList()).size() > 0 ? false : true;
                })
                .collect(Collectors.toList());

        return filtered;

    }

    /**
     * Had another mechanism before but this is way to slow.
     *
     * @param old
     * @return
     */
    @Override
    public List<String> getPossibleReplacementVehicles(String old) {
        FleetService fleet = ServiceRegistry.getService(FleetService.class);
        Vehicle oldVehicle = fleet.getVehicleForId(old);
        String type = oldVehicle.getType();

        List<Tour> vehicleTours = this.getToursForVehicle(old);

        if (vehicleTours.size() != 0) {
            List<String> availableVehicles = checkForAvailabelVehicles(vehicleTours.get(0));

            for (int i = 1; i < vehicleTours.size(); i++) {
                Iterator<String> iter = availableVehicles.iterator();
                while (iter.hasNext()) {
                    if (!vehicleIsAvailable(vehicleTours.get(i), iter.next())) {
                        iter.remove();
                    }
                }
            }

            return availableVehicles;
        } else {
            return fleet.getNotLiveVehicles()
                    .stream()
                    .filter(v -> v.getType().equals(type))
                    .map(v -> v.getId()).collect(Collectors.toList());
        }
    }

    @Override
    public void setReplacementVehicle(String old, String newVehicle) throws Exception {
        List<Tour> tours = this.getToursForVehicle(old);
        for (Tour tour : tours) {
            tour.setVehicle(newVehicle);
            this.saveTour(tour);
        }
    }

    @Override
    public List<Tour> getToursWithoutVehicles() {
        List<Tour> vehicleLessTours = new ArrayList<Tour>();
        for (Tour tour : tours) {
            if (tour.getVehicle() == null || tour.getVehicle().equals("")) {
                vehicleLessTours.add(tour);
            }
        }

        return vehicleLessTours;
    }


    @Override
    public void updateTour(Tour tour, String vehicle) throws Exception {
        if (vehicle != null && !vehicle.equals("")) {
            if (!vehicleIsAvailable(tour, vehicle)) {
                throw new Exception("Vehicle not available");
            }
        }

        tour.setVehicle(vehicle);

        for (Tour loopTour : tours) {
            if (loopTour.getId() == tour.getId()) {
                // first case is important if the current tour does not have vehicle
                if (loopTour.getVehicle() != tour.getVehicle() || !loopTour.getVehicle().equals(tour.getVehicle())) {
                    tours.remove(loopTour);
                    tours.add(tour);
                    tourPersistence.updateTour(tour);
                }
                return;
            }
        }
    }

    /**
     * This method checks, if the delivered vehicle from is available for the actual tours time.
     * Therefore we loop through the complete tours to search for occupations.
     * If the is no conflict with other already existing tours, true is returned.
     *
     * @param checkTour the tour to check for
     * @return
     */
    public boolean vehicleIsAvailable(Tour checkTour, String vehicle) {
        for (Tour tour : tours) {
            if (tour.getVehicle() != null && tour.getVehicle().equals(vehicle) && tour.getId() != checkTour.getId()) {
                if (!(tour.getEndTime().before(checkTour.getStartTime()) || tour.getStartTime().after(checkTour.getEndTime()))) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void removeTour(Tour tour) {
        tourPersistence.deleteTour(tour);
        tours.remove(tour);
    }

    @Override
    public void removeTours(List<Tour> tours) {
        for (Tour tour : tours)
            removeTour(tour);
    }

    @Override
    public String getName() {
        return "TourStore";
    }

    /**
     * Assumption:
     * All tubes start driving at 7:00 and the last starts at 21:00
     * Every 15 minutes goes a tube
     *
     * @return a list of Tour Object of all tours created with the assumption above
     * for 7 Days from the day of calling this method (including that day
     */
    private List<Tour> getFabricatedTours() {
        return getFabricatedToursFor(-NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST, NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL);
    }

    /**
     * Assumption:
     * All buses start driving at 7:00 and the last starts at 21:00
     * Every 15 minutes goes a bus
     *
     * @return a list of Tour Object of all tours created with the assumption above
     * for 7 Days from the day of calling this method (including that day

    private List<Tour> getFabricatedToursForBusess() {
    return getFabricatedToursFor(linesInfoForBus, 3, true,
    -NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST, NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL);
    }*/

    /**
     * Helpermethod for creating the tours for lines
     *
     * @param startDay             number of days relative to now were tours start to exist (e.g. -4 creates tours starting 4 days ago)
     * @param numberOfDaysToCreate number of days which are created starting form the startDay
     * @return returns all created tours (they are not saved in the DB!)
     * <p>
     * e.g.: startDay = -4 and numberOfDaysToCreate = 14
     * -> creates tours for 4 days in the past, for the current day and 9 for the future
     */
    private List<Tour> getFabricatedToursFor(int startDay, int numberOfDaysToCreate) {
        if (numberOfDaysToCreate <= 0) {
            return null;
        }
        List<Tour> tours = new ArrayList<>();
        Tourdb tourdb;

        Network network = ServiceRegistry.getService(Network.class);
        Map<String, Long> lineDuration = new HashMap<>();
        int busCounter = 0;
        int tubeCounter = 0;
        for (Line line : network.getLines()) {
            for (int tourOnThatDay = 0; tourOnThatDay < 56; ++tourOnThatDay) { // Between 7:00 and 21:00 every 15min -> 56
                for (int day = 0; day < numberOfDaysToCreate; ++day) {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.DAY_OF_MONTH, day + startDay);
                    cal.set(Calendar.HOUR_OF_DAY, 7);
                    cal.set(Calendar.MINUTE, 00);
                    cal.add(Calendar.MINUTE, 15 * tourOnThatDay);
                    Timestamp timestampStart = new Timestamp(cal.getTimeInMillis());
                    String typString = "B_";
                    int vehicleIdPrefix = 0;
                    if (line.getVehicleType().equalsIgnoreCase("bus")) {
                        vehicleIdPrefix = busCounter * 20; // all busses start wie 10 (e.g. 1099)
                    } else {
                        vehicleIdPrefix = tubeCounter * 20;
                        typString = "T_";
                    }

                    List<Stop> stopSequence = line.getRoute().getStopSequence();
                    if (stopSequence.size() == 0) {
                        continue; // this should usually not happen (only during testing it is ok due to mock data)
                    }
                    if (lineDuration.get(line.getId()) == null) {
                        tourdb = createTourdbObject(uniqueNumberOfToursId, line.getId(), stopSequence.get(0).getId(),
                                stopSequence.get(stopSequence.size() - 1).getId(), timestampStart, 0,
                                typString + String.format("%03d", (vehicleIdPrefix + tourOnThatDay % 10)));
                        lineDuration.put(line.getId(), tourdb.getEndTime().getTime() - tourdb.getStartTime().getTime());
                    } else {
                        tourdb = createTourdbObject(uniqueNumberOfToursId, line.getId(), stopSequence.get(0).getId(),
                                stopSequence.get(stopSequence.size() - 1).getId(), timestampStart, lineDuration.get(line.getId()),
                                typString + String.format("%03d", (vehicleIdPrefix + tourOnThatDay % 10)));
                    }

                    tours.add(new Tour(tourdb));
                    uniqueNumberOfToursId++;
                    // other way round
                    tourdb = createTourdbObject(uniqueNumberOfToursId, line.getId(), stopSequence.get(stopSequence.size() - 1).getId(),
                            stopSequence.get(0).getId(), timestampStart, lineDuration.get(line.getId()),
                            typString + String.format("%03d", (vehicleIdPrefix + 10 + tourOnThatDay % 10)));
                    tours.add(new Tour(tourdb));
                    uniqueNumberOfToursId++;
                }
            }

            if (line.getVehicleType().equalsIgnoreCase("bus")) {
                busCounter++;
            } else {
                tubeCounter++;
            }
        }
        return tours;
    }

    private Tourdb createTourdbObject(int id, String line, String startStop, String endStop,
                                      Timestamp beginning, long durationMilliSeconds, String vehicle) {
        Tourdb tourdb = new Tourdb();
        tourdb.setId(id);
        tourdb.setVehicle(vehicle); //must reference a real VehicleDB (e.g. Vehicle 0) or be null
        tourdb.setLine(line); //must reference a real LineDB
        tourdb.setStartStop(startStop); //must reference a real StopDB
        tourdb.setEndStop(endStop); //must reference a real StopDB
        tourdb.setStartTime(beginning);

        if (durationMilliSeconds == 0) {
            Tour tour = new Tour(tourdb);
            long time = this.getEndtimeOfTour(tour);
            tourdb.setEndTime(new Timestamp(time));
        } else {
            tourdb.setEndTime(new Timestamp(beginning.getTime() + durationMilliSeconds));
        }

        return tourdb;
    }

    // when this function is called minimum one time per day
    // it deletes the tours of one day and creates new for one day
    @Override
    public void keepToursUpToDate() {
        int numberOfDeletedTours = tourPersistence.deleteOlderToursThan(NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST);
        logger.info("TourStore, keepToursUpToDate: deleted:" + numberOfDeletedTours);
        Calendar cal = Calendar.getInstance();
        int startDay = NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL - NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST;
        cal.add(Calendar.DAY_OF_MONTH, startDay); //startDay is one to much because of the -1 in the for loop)
        for (int i = 0; i < NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL; ++i) {
            cal.add(Calendar.DAY_OF_MONTH, -1);
            Timestamp timestampOfTheDay = new Timestamp(cal.getTimeInMillis());
            int numberOfToursOnDay = tourPersistence.getNumberOfToursOnDay(timestampOfTheDay);
            // every day should have around 896 tours (8 lines, 112 tours per line)
            // Assumtion: nobody would create or delete 448 tours on a day  (currently there is no multi-delete)
            // So the only reason why there are not enough tours on the day is because they were not yet created
            if (numberOfToursOnDay > 448) { // found the day were the "normale" tours start existing again
                // something is wrong when a lot less or more tours were deleted then should be created
                /*if (Math.abs(numberOfDeletedTours - i * 896) > 448) {
                    return;
                }*/
                if (i == 0) { //all days are up to date
                    return;
                }
                try {
                    saveTours(getFabricatedToursFor(
                            startDay - i, i));

                    logger.info("TourStore, keepToursUpToDate: created new tours");
                } catch (Exception e) {
                    logger.error("Could not save new updated tours in database: The following exception occurred: " + e.getMessage());
                }
                return;
            }
        }
        // this case only occurs when this method was not called for more than NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL days
        try {
            saveTours(getFabricatedTours());

            logger.info("TourStore, keepToursUpToDate: created new tours");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Could not save new updated tours in database: " + e.getMessage());
        }
    }

    // currently not used since there are to many each day
    private List<Tour> getAllToursFromAPIForOneDay() {
        TransportAPIHandler apiHandler = new TransportAPIHandler();
        List<Tourdb> tourDBs = apiHandler.fetchTourDBs();

        List<Tour> tours = new ArrayList<>();
        for (Tourdb tourdb : tourDBs) {
            Tour tour = new Tour(tourdb);
            tours.add(tour);
        }

        return tours;
    }

    private List<Timebetweenstopsdb> getAllBetweensFromAPI() {
        TransportAPIHandler apiHandler = new TransportAPIHandler();
        return apiHandler.fetchTimeBetweenStopsDBs();
    }

    @Override
    public Position getPositionFromTour(Tour tour) {
        return getPositionFromDelayedTour(tour, 0);
    }

    @Override
    public Position getPositionFromDelayedTour(Tour tour, int delayInSeconds) {
        //check if tour is currently running
        Date now = new Date();
        // vehicles should be shown on last stop 30-40 seconds after they arrived
        Date newEndTime = new Date(tour.getEndTime().getTime() + 40 * 1000);
        if (now.before(tour.getStartTime()) || now.after(newEndTime)) {
            return null;
        }

        long passedTimeSinceStartInSeconds = (now.getTime() - tour.getStartTime().getTime()) / 1000; // precision not needed
        passedTimeSinceStartInSeconds -= delayInSeconds;
        if (passedTimeSinceStartInSeconds < 0) {
            return null;
        }

        Network network = ServiceRegistry.getService(Network.class);
        List<Stop> stopsOnLine = network.getStopsPerLine(tour.getLine());

        if (!tour.getStartStop().equals(stopsOnLine.get(0).getId())) { // when it runs the wrong direction start from the end
            Collections.reverse(stopsOnLine);
        }
        Position pos = null;
        String prevStop = tour.getStartStop();
        for (int i = 1; i < stopsOnLine.size(); ++i) {
            String nextStop = stopsOnLine.get(i).getId();
            long timeBetweenInMinutes = 0;
            try {
                timeBetweenInMinutes = getTimeBetweenTwoConnectedStops(prevStop, nextStop);
            } catch (Exception e) {
                logger.error("Could not calculate current position of Tour: " + tour.getId());
                e.printStackTrace();
            }
            if (timeBetweenInMinutes * 60 <= passedTimeSinceStartInSeconds) {
                passedTimeSinceStartInSeconds -= timeBetweenInMinutes * 60;
                prevStop = nextStop;
            } else {
                // the vehicle should stay for 30 seconds on the last stop of the tour for not disappearing when arriving on the last stop
                pos = new Position(prevStop, nextStop, passedTimeSinceStartInSeconds <= 30);
                break;
            }
        }
        // when the vehicle is not on a previous stop and not between two stops it must be on the last stop
        if (pos == null) {
            pos = new Position(prevStop, null, true);
        }

        return pos;
    }

    @Override
    public Tour getTourById(int tourId) {
        for (Tour tour : tours) {
            if (tour.getId() == tourId) {
                return tour;
            }
        }

        return null;
    }

    @Override
    public Tour getCopiedTourById(int tourId) {
        for (Tour tour : tours) {
            if (tour.getId() == tourId) {
                Tourdb db = tour.getTourDB();

                Tour copy = new Tour(db.getId(), db.getStartTime(), db.getEndTime(), db.getStartStop(), db.getEndStop(), tour.getVehicle(), tour.getLine());
                copy.setStopTime(tour.getStopTime());

                return copy;
            }
        }
        return null;
    }
}
