package de.se.model.interfaces;

import de.se.DB.hibernate_models.Timebetweenstopsdb;
import de.se.data.Delay;
import de.se.data.Line;
import de.se.data.Position;
import de.se.data.Tour;
import de.se.model.ParentService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

public interface TourStore extends ParentService {

    /**
     * This method return ALL existing and not past tours for a line
     *
     * @param line
     * @return
     */
    public List<Tour> getToursForLine(Line line);

    /**
     * This method return ALL existing and not past tours for a vehicle
     *
     * @param vehicleID the ID of the vehicle for which we want to determine the current tours
     * @return
     */
    List<Tour> getToursForVehicle(String vehicleID);

    /**
     * Determines the current tour for a vehicle
     *
     * @param vehicleID the vehicle for which we want to determine the current tour
     * @return the current tour of the vehicle
     */
    Tour getCurrentTourForVehicle(String vehicleID);


    /**
     * This method returns all saved timebetweenstops objects
     *
     * @return Timebetweenstopsdb objects saved in this class from the database
     */
    List<Timebetweenstopsdb> getTimebetweenstops();

    /**
     * This method returns all current tours of one line
     *
     * @param line for which we want the current tours
     * @return all tours running on that line at the moment
     */
    List<Tour> getCurrentTours(Line line);

    /**
     * This method calculates the current running tours and takes into account that there are delays
     *
     * @param line the line for which we want the current tours
     * @param vehicleDelays hashmap which maps vehicles (and therefore as well tours) to delays
     * @return a list of tours which are currently running
     */
    List<Tour> getCurrentToursWithDelays(Line line, HashMap<String, Delay> vehicleDelays);

    /**
     * This method saves a list of tours either new tours or changed tours
     *
     * @param tours
     */
    public void saveTours(List<Tour> tours) throws Exception;


    /**
     * This method checks for all vehicles which are available on this line on the given time
     * THerefore we have to calculate the endtime of requested tour time at that line and then search for vehicles,
     * which do not have a tour assigned at the requested time.
     *
     * @param startTime the start time of the requested tour
     * @param lineId    the requested line
     * @return a list of available vehicles at that time
     */
    List<String> checkForAvailableVehicles(Date startTime, String lineId);

    /**
     * This method saves on tour
     *
     * @param tour
     */
    public void saveTour(Tour tour) throws Exception;

    /**
     * This method changes an existing tour object in database and backend code.
     * Scince the method contains a check if the update is valid, it is important to deliver the unchanged tour
     * object and the new vehicle as string to check if the change can be done or in case of an exceptioon, must be undone.
     *
     * @param tour    the original tour object
     * @param vehicle the new vehicle to assigne
     * @throws Exception
     */
    void updateTour(Tour tour, String vehicle) throws Exception;

    /**
     * This method removes
     *
     * @param tour
     */
    public void removeTour(Tour tour);

    /**
     * Thie method removes all given tours in the list
     *
     * @param tours which should be removed
     */
    void removeTours(List<Tour> tours);

    /**
     * This method returns all existing tours of the Database
     *
     * @return all existing tours
     */
    List<Tour> getAllExistingTours();

    /**
     * This methode deletes Tours which are older than 4 days and creates Tours for the next 10 days
     * (9 for the future + current day)
     */
    void keepToursUpToDate();

    /**
     * Builds a position object from a tour
     *
     * @param tour the tour for which we want to calculate the position
     * @return a position object containing the previous and next stop as well as a flag
     * whether the vehicle is supposed to be on the previous stop (otherwise it is between both stops)
     */
    Position getPositionFromTour(Tour tour);

    /**
     * Builds a position object from a tour, but also taking the passed delay into consideration
     *
     * @param tour           the tour for which we want to calculate the position
     * @param delayInSeconds the delay the vehicle currently has (which influences the position) in seconds
     * @return a position object containing the previous and next stop as well as a flag
     * whether the vehicle is supposed to be on the previous stop (otherwise it is between both stops)
     */
    Position getPositionFromDelayedTour(Tour tour, int delayInSeconds);


    /**
     * This method is needed to delete tours
     *
     * @param tourId
     * @return returns the tour object in the tourStore with the given Id when it exists
     */
    Tour getTourById(int tourId);

    /**
     * This method is needed to change tours (e.g. change vehicle or delete vehicle from tour)
     *
     * @param tourId
     * @return returns a copy of the tour object in the tourStore with the given Id when it exists
     */
    Tour getCopiedTourById(int tourId);

    /**
     * Helpermethod for getting the Endtime:
     * Assumtion: Start and end stop are start and end stop of the whole line
     *
     * @param tour containing the starttime and lineid as minimum
     */
    long getEndtimeOfTour(Tour tour);

    /**
     * This method searches for all tours in given time range(between start and end),
     * which don't conflict with tours from vehicle with given id.
     *
     * @param vehicleId the id of vehicle to filter
     * @param start     the start time in milliseconds
     * @param end       the end time im milliseconds
     * @return
     */
    List<Tour> getAssignableToursInTimeRange(String vehicleId, long start, long end);

    /**
     * This method looks for all vehicles, which could take all tours of given vehicle.
     *
     * @return
     */
    List<String> getPossibleReplacementVehicles(String old);

    /**
     * THis method changes all tours of vehicle old to vehicle newVehicle
     *
     * @param old        the old vehicle, whoose tours should be switched
     * @param newVehicle the vehicle which should get new tours
     */
    void setReplacementVehicle(String old, String newVehicle) throws Exception;

    /**
     * This method returns all tours, which do not have a vehicle assigned AND which start in the future.
     * It won't make sense to deliver old tours which don't have vehicles I think.
     *
     * @return
     */
    List<Tour> getToursWithoutVehicles();
}
