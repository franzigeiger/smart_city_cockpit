package de.se.TransportAPI;

import com.google.gson.*;
import de.se.DB.hibernate_models.*;
import de.se.SAP.NonPersistentCookieJar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;

/**
 * Handles connection to the tfl API
 */
public class TransportAPIHandler {
    private int uniqueId = 0; // for unique primary key

    private static TransportAPIHandler instance;

    private final static Logger logger = Logger.getLogger(TransportAPIHandler.class);

    private final static String APP_ID = "4073b991";
    private final static String APP_KEY = "5357b65d7545819bb75b24ae73f69129";
    private final static String urlTubeStationsCentral = "http://transportapi.com/v3/uk/tube/central.json" +
            "?app_id=" + APP_ID + "&app_key=" + APP_KEY;

    // API tfl.gov.uk
    // Username: SCC
    // Password: SCC_ftw1
    private final static String TFL_APP_ID = "03c9eafd";
    private final static String TFL_APP_KEY = "d7f6486edae85b0d36cd2792c559487e";
    private final static String TFL_URL_AUTH = "?app_id=" + TFL_APP_ID + "&app_key=" + TFL_APP_KEY;


    private String stopSequencesInboundPart1 = "https://api.tfl.gov.uk/line/";
    private String stopSequencesInboundPart2 = "/route/sequence/inbound"; //in between we need the line

    private String tourSequensPart1 = "https://api.tfl.gov.uk/Line/";
    private String tourSequensPart2 = "/Timetable/"; // in between we need the line and at the end the stop

    private String[] lines = {"bakerloo", "hammersmith-city", "jubilee", "victoria", "waterloo-city"};
    private String[] colors = {"c28d5b", "d799af", "cccdce", "00a0e2", "63c9b4"};

    private String[] busLines = {"23", "14", "188"};
    private String[] busColors = {"F287B6", "808285", "FBBD96"};

    private String lineTemplate = "https://api.tfl.gov.uk/Line/";

    private String vehiclesForTubes = "https://api.tfl.gov.uk/Mode/tube/Arrivals";

    private OkHttpClient client;

    public TransportAPIHandler() {
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        client = new OkHttpClient.Builder()
                .cookieJar(new NonPersistentCookieJar())
                .build();
        logger.info("Started tfl Connection HTTP Client.");

        List<Linedb> linedbs = fetchLineDBs();

        logger.info("Parsed line");
    }

    /**
     * Fetch all LineDBs from tfl api
     *
     * @return list with all LineDBs from "lines" array
     */
    public List<Linedb> fetchLineDBs() {
        List<Linedb> linedbs = new ArrayList<>();
        for (int i = 0; i < lines.length + busLines.length; i++) {
            int validIndex = i < lines.length ? i : i - lines.length;
            String correctCurrentLine = i < lines.length ? lines[validIndex] : busLines[validIndex];
            String lineJsonString = null;
            try {
                lineJsonString = fetchFromAPI(lineTemplate + correctCurrentLine + "/");
                logger.info("Fetched tube/bus station from tfl API");
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Could not fetch tube/bus station from tfl API");
                return new ArrayList<>();
            }
            try {
                JsonArray lineJsonArray = new JsonParser().parse(lineJsonString).getAsJsonArray();
                JsonElement element = lineJsonArray.get(0);
                JsonObject object = element.getAsJsonObject();

                JsonElement idJson = object.get("id");
                String id = idJson.getAsString();

                JsonElement nameJson = object.get("name");
                String name = nameJson.getAsString();

                String color = i < colors.length ? colors[validIndex] : busColors[validIndex];

                JsonElement typeJson = object.get("modeName");
                String type = typeJson.getAsString();

                Linedb linedb = new Linedb();
                linedb.setId(id);
                linedb.setName(name);
                linedb.setColor(color);
                linedb.setType(type);

                linedbs.add(linedb);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
                logger.error("Failed to parse this Json: " + lineJsonString);
                // If it fails here: "Request Limit exceeded"
            }

        }

        return linedbs;
    }

    /**
     * Fetch all StopDBs from tfl api but only save the last 5 chars of the id
     *
     * @return list with all StopDBs from "lines" and "busLines" array
     */
    public List<Stopdb> fetchStopDBs() {
        List<Stopdb> stopdbs = new ArrayList<>();
        List<String> alreadyUsedIds = new ArrayList<>();
        for (int i = 0; i < lines.length + busLines.length; i++) {
            int validIndex = i < lines.length ? i : i - lines.length;
            String correctCurrentLine = i < lines.length ? lines[validIndex] : busLines[validIndex];
            String stopsJsonString = getStopSequencesInbound(correctCurrentLine);

            JsonObject stopsJsonObject = new JsonParser().parse(stopsJsonString).getAsJsonObject();
            JsonArray JsonArray = stopsJsonObject.getAsJsonArray("stopPointSequences");
            JsonElement jsonElement = JsonArray.get(0);
            JsonArray stopsJsonArray = jsonElement.getAsJsonObject().get("stopPoint").getAsJsonArray();

            for (int j = 0; j < stopsJsonArray.size(); j++) { //loop over all stops
                JsonElement singleStopElement = stopsJsonArray.get(j);
                JsonObject singleStopbject = singleStopElement.getAsJsonObject();

                JsonElement idJson = singleStopbject.get("id");
                String id = idJson.getAsString();
                id = changeStopStringToShorterVersion(id);
                if (alreadyUsedIds.contains(id)) { //avoid duplicates
                    continue;
                }
                alreadyUsedIds.add(id);

                JsonElement nameJson = singleStopbject.get("name");
                String name = nameJson.getAsString();

                JsonElement latitudeJson = singleStopbject.get("lat");
                String latitude = latitudeJson.getAsString();

                JsonElement longitudeJson = singleStopbject.get("lon");
                String longitude = longitudeJson.getAsString();

                Stopdb stopdb = new Stopdb();
                stopdb.setId(id);
                stopdb.setName(name);
                stopdb.setLatitude(latitude);
                stopdb.setLongitude(longitude);
                stopdbs.add(stopdb);
            }
        }

        return stopdbs;
    }

    /**
     * change stop id for more legibility.
     * This methode is short but should guarantee that all line id are changed the same way
     *
     * @param id original id of stop
     * @return new short id of stop
     */
    private String changeStopStringToShorterVersion(String id) {
        if (id == null) {
            return null;
        }
        try {
            return id.substring(id.length() - 5);
        }
        catch (IndexOutOfBoundsException e) {
            logger.warn("ID was to short ! No using only: " + id);
            return id;
        }
    }

    /**
     * Fetch all Stops on each Line combined in Stopsinlinedb objects fromt the api
     *
     * @return a list of all stops with the attribute line combined the Stopsinlinedb objects
     */
    public List<Stopsinlinedb> fetchStopsInLineDBs() {
        List<Stopsinlinedb> stopsinlinedbs = new ArrayList<>();
        for (int i = 0; i < lines.length + busLines.length; i++) {
            int validIndex = i < lines.length ? i : i - lines.length;
            String correctCurrentLine = i < lines.length ? lines[validIndex] : busLines[validIndex];
            String lineJsonString = getStopSequencesInbound(correctCurrentLine);

            JsonObject stopsJsonObject = new JsonParser().parse(lineJsonString).getAsJsonObject();

            JsonElement lineIdJson = stopsJsonObject.get("lineId");
            String lineId = lineIdJson.getAsString();

            JsonArray jsonArray = stopsJsonObject.getAsJsonArray("orderedLineRoutes");
            JsonElement jsonElement = jsonArray.get(0);
            JsonArray stopsJsonArray = jsonElement.getAsJsonObject().get("naptanIds").getAsJsonArray();

            for (int j = 0; j < stopsJsonArray.size(); j++) { //loop over all stops
                JsonElement stopIdJson = stopsJsonArray.get(j);
                String stopId = stopIdJson.getAsString();
                stopId = changeStopStringToShorterVersion(stopId);

                Stopsinlinedb stopInLineDb = new Stopsinlinedb();
                stopInLineDb.setStop(stopId);
                stopInLineDb.setLine(lineId);
                stopInLineDb.setPositionstoponline(j);
                stopsinlinedbs.add(stopInLineDb);
            }
        }

        return stopsinlinedbs;
    }

    /**
     * Fetch all tubes on lines from "lines" array from tfl api
     *
     * @return list with all tubes (VehicleDBs) on lines from "lines" array
     */
    public List<Vehicledb> fetchVehicleDBsForTubes() {
        List<Vehicledb> vehicledbs = new ArrayList<>();
        HashSet<String> alreadyUsedIds = new HashSet<>();
        String vehiclesJsonString = null;
        try {
            vehiclesJsonString = fetchFromAPI(vehiclesForTubes);
            logger.info("Fetched vehicles for tubes from tfl API");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not fetch vehicles for tubes from tfl API");
            return new ArrayList<>();
        }

        JsonArray vehiclesJsonArray = new JsonParser().parse(vehiclesJsonString).getAsJsonArray();

        for (int i = 0; i < vehiclesJsonArray.size(); i++) { //traverse the json array
            JsonElement element = vehiclesJsonArray.get(i);
            JsonObject innerObject = element.getAsJsonObject();

            JsonElement vehicleId = innerObject.get("vehicleId");
            String vehicleIdString = vehicleId.getAsString();

            JsonElement lineId = innerObject.get("lineId");
            String lineIdString = lineId.getAsString();

            JsonElement modeName = innerObject.get("modeName");
            String modeNameString = modeName.getAsString();

            if (!alreadyUsedIds.contains(vehicleIdString)) { //avoid duplicates
                for (String line : lines) { //check if we want to store vehicles for this line
                    if (line.equals(lineIdString)) {
                        Vehicledb vehicledb = new Vehicledb();
                        vehicledb.setId(vehicleIdString);
                        vehicledb.setType(modeNameString);
                        vehicledbs.add(vehicledb);
                        alreadyUsedIds.add(vehicleIdString);
                    }
                }
            }
        }

        return vehicledbs;
    }

    /**
     * Fetch all existing maximal length tours for one day of all lines
     *
     * @return a list of Tourdb objects each representing a tour on a line
     */
    public List<Tourdb> fetchTourDBs() {
        List<Tourdb> tourdbs = new ArrayList<>();
        for (int i = 0; i < lines.length; i++) {
            String lineJsonString = getStopSequencesInbound(lines[i]);

            JsonObject stopsJsonObject = new JsonParser().parse(lineJsonString).getAsJsonObject();

            JsonElement lineIdJson = stopsJsonObject.get("lineId");
            String lineId = lineIdJson.getAsString();

            JsonArray jsonArray = stopsJsonObject.getAsJsonArray("orderedLineRoutes");
            JsonElement jsonElement = jsonArray.get(0);
            JsonArray stopsJsonArray = jsonElement.getAsJsonObject().get("naptanIds").getAsJsonArray();

            JsonElement stopIdJson = stopsJsonArray.get(0);
            String first_stop = stopIdJson.getAsString();
            first_stop = first_stop;

            stopIdJson = stopsJsonArray.get(stopsJsonArray.size() - 1);
            String last_stop = stopIdJson.getAsString();
            last_stop = last_stop;

            addToursToList(tourdbs, getTourSequens(lines[i], first_stop), lineId, first_stop, last_stop); //inbound
            addToursToList(tourdbs, getTourSequens(lines[i], last_stop), lineId, last_stop, first_stop); //outbound
        }

        return tourdbs;
    }

    /**
     * Helper method for fetchTourDBs
     *
     * @param tourdbs:     list in which all generated tours should be added
     * @param tourSequens: String from the api request which will be past
     * @param lineId:      id of the line which the tours should be generated for
     * @param first_stop:  First stop of the line which the tour starts
     * @param last_stop:   Last station of the tour on the line
     */
    private void addToursToList(List<Tourdb> tourdbs, String tourSequens, String lineId, String first_stop, String last_stop) {
        String tourJsonString = tourSequens;
        JsonObject tourJsonObject = new JsonParser().parse(tourJsonString).getAsJsonObject();
        JsonElement timetableJson = tourJsonObject.get("timetable");
        JsonElement routesJson = timetableJson.getAsJsonObject().get("routes").getAsJsonArray().get(0);

        // find interval which drives whole line
        JsonArray intervalJsonArray = routesJson.getAsJsonObject().get("stationIntervals").getAsJsonArray();
        JsonArray longestIntervalArrayJson = intervalJsonArray.get(0).getAsJsonObject().get("intervals").getAsJsonArray();
        for (int intervalNumber = 1; intervalNumber < intervalJsonArray.size(); ++intervalNumber) {
            if (longestIntervalArrayJson.size() < intervalJsonArray.get(intervalNumber).getAsJsonObject().get("intervals").getAsJsonArray().size()) {
                longestIntervalArrayJson = intervalJsonArray.get(intervalNumber).getAsJsonObject().get("intervals").getAsJsonArray();
            }
        }

        JsonElement timeToEndStopJson = longestIntervalArrayJson.get(longestIntervalArrayJson.size() - 1).getAsJsonObject().get("timeToArrival");
        int minutesToEndStop = timeToEndStopJson.getAsInt();

        JsonArray differentSchedulesJson = routesJson.getAsJsonObject().get("schedules").getAsJsonArray();
        // for testing we only take the first (e.g. Monday)
        JsonArray toursOfOneDayJson = differentSchedulesJson.get(0).getAsJsonObject().get("knownJourneys").getAsJsonArray();
        for (int j = 0; j < toursOfOneDayJson.size(); ++j) { //loop over all tours and create them
            JsonElement tourStartTimeJson = toursOfOneDayJson.get(j);
            int intervalId = tourStartTimeJson.getAsJsonObject().get("intervalId").getAsInt();
            if (intervalId != 0) //only tours with the interval 0 run the whole line
                continue;
            String hourString = tourStartTimeJson.getAsJsonObject().get("hour").getAsString();
            String minuteString = tourStartTimeJson.getAsJsonObject().get("minute").getAsString();
            int hour = Integer.parseInt(hourString);
            int minute = Integer.parseInt(minuteString);

            Tourdb tourdb = new Tourdb();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            Timestamp timestamp = new Timestamp(cal.getTimeInMillis());
            tourdb.setStartTime(timestamp);
            cal.add(Calendar.MINUTE, minutesToEndStop);
            timestamp = new Timestamp(cal.getTimeInMillis());
            tourdb.setEndTime(timestamp);
            tourdb.setId(uniqueId);
            uniqueId++;
            tourdb.setStartStop(first_stop);
            tourdb.setEndStop(last_stop);
            tourdb.setLine(lineId);
            tourdb.setVehicle(null);

            tourdbs.add(tourdb);
        }
    }


    /**
     * Helpermethod for getting the stops sequence of a line
     *
     * @param line for which we want the stopSequence
     * @return Json in string format from the api
     */
    private String getStopSequencesInbound(String line) {
        String lineJsonString = null;
        try {
            lineJsonString = fetchFromAPI(stopSequencesInboundPart1 + line + stopSequencesInboundPart2);
            logger.info("Fetched stops from tfl API");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not fetch stops from tfl API");
        }
        return lineJsonString;
    }


    /**
     * Fetches all time between Stops
     *
     * @return all times between stops as in a Timebetweenstopsdb
     */
    public List<Timebetweenstopsdb> fetchTimeBetweenStopsDBs() {
        List<Timebetweenstopsdb> timebetweenstopsdbs = new ArrayList<>();
        addTimeBetweenStopsDBsFor(timebetweenstopsdbs, lines);
        addTimeBetweenStopsDBsFor(timebetweenstopsdbs, busLines);

        return timebetweenstopsdbs;
    }

    private void addTimeBetweenStopsDBsFor(List<Timebetweenstopsdb> timebetweenstopsdbs, String[] linesForStops) {
        for (int i = 0; i < linesForStops.length; i++) {
            String lineJsonString = getStopSequencesInbound(linesForStops[i]);

            JsonObject stopsJsonObject = new JsonParser().parse(lineJsonString).getAsJsonObject();

            JsonArray jsonArray = stopsJsonObject.getAsJsonArray("orderedLineRoutes");
            JsonElement jsonElement = jsonArray.get(0);
            JsonArray stopsJsonArray = jsonElement.getAsJsonObject().get("naptanIds").getAsJsonArray();

            JsonElement stopIdJson = stopsJsonArray.get(0);
            String firstStop = stopIdJson.getAsString();

            String tourJsonString = getTourSequens(linesForStops[i], firstStop);
            JsonObject tourJsonObject = new JsonParser().parse(tourJsonString).getAsJsonObject();
            JsonElement timetableJson = tourJsonObject.get("timetable");
            JsonElement routesJson = timetableJson.getAsJsonObject().get("routes").getAsJsonArray().get(0);

            // find interval which drives whole line
            JsonArray intervalJsonArray = routesJson.getAsJsonObject().get("stationIntervals").getAsJsonArray();
            JsonArray longestIntervalArrayJson = intervalJsonArray.get(0).getAsJsonObject().get("intervals").getAsJsonArray();
            for (int intervalNumber = 1; intervalNumber < intervalJsonArray.size(); ++intervalNumber) {
                if (longestIntervalArrayJson.size() < intervalJsonArray.get(intervalNumber).getAsJsonObject().get("intervals").getAsJsonArray().size()) {
                    longestIntervalArrayJson = intervalJsonArray.get(intervalNumber).getAsJsonObject().get("intervals").getAsJsonArray();
                }
            }

            firstStop = changeStopStringToShorterVersion(firstStop);
            addTimeBetweenStopsDBForEachInterval(timebetweenstopsdbs, longestIntervalArrayJson, firstStop);

        }
    }

    /**
     * Helpermethod for fetchTimeBetweenStopsDBs
     * Adds all time between stops to the given list
     *
     * @param timebetweenstopsdbs      list which should be filled with all existing times between stops in given longestIntervalArrayJson
     * @param longestIntervalArrayJson json which contains all times between stops on the line starting at first_stop
     * @param first_stop               stopId on which the json start listing the times
     */
    private void addTimeBetweenStopsDBForEachInterval(List<Timebetweenstopsdb> timebetweenstopsdbs, JsonArray longestIntervalArrayJson, String first_stop) {
        String preStop = first_stop;
        int timeBetweenStartAndLastStation = 0;
        for (int i = 0; i < longestIntervalArrayJson.size(); ++i) {
            JsonObject intervalObjekt = longestIntervalArrayJson.get(i).getAsJsonObject();
            String nextStop = intervalObjekt.get("stopId").getAsString();
            int timeBetweenStartAndThisStation = intervalObjekt.get("timeToArrival").getAsInt();
            int timeBetweenStops = timeBetweenStartAndThisStation - timeBetweenStartAndLastStation;
            timeBetweenStartAndLastStation = timeBetweenStartAndThisStation;

            preStop = changeStopStringToShorterVersion(preStop);
            nextStop = changeStopStringToShorterVersion(nextStop);
            Timebetweenstopsdb timebetweenstopsdb = new Timebetweenstopsdb();
            timebetweenstopsdb.setStartstop(preStop);
            timebetweenstopsdb.setNextstop(nextStop);
            timebetweenstopsdb.setTimeinminutes(timeBetweenStops);
            timebetweenstopsdbs.add(timebetweenstopsdb);
            preStop = nextStop;
        }
    }


    /**
     * Helpermethod
     *
     * @param lineID which we want tours for
     * @param stopID were the tour should start (normale start or end stop of a line)
     * @return a json containing all tours starting form stopId on line with lineID
     */
    private String getTourSequens(String lineID, String stopID) {
        String tourJsonString = null;
        try {
            tourJsonString = fetchFromAPI(tourSequensPart1 + lineID + tourSequensPart2 + stopID);
            logger.info("Fetched tours from tfl API");
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not fetch tours from tfl API");
        }
        return tourJsonString;
    }

    private String fetchTubeStationsDeprecated() throws IOException {
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .url(urlTubeStationsCentral)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    private String fetchFromAPI(String url) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .url(url + TFL_URL_AUTH)
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }

    public static TransportAPIHandler getInstance() {
        if (TransportAPIHandler.instance == null) {
            TransportAPIHandler.instance = new TransportAPIHandler();
        }
        return TransportAPIHandler.instance;
    }

}
