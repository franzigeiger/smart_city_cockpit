package de.se.SAP;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import de.se.model.impl.EventServiceImpl;
import de.se.model.impl.ServiceRequestServiceImpl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * Superclass for managing service requests and events
 */
public class SAPService {

    protected final static Logger logger = Logger.getLogger(SAPService.class);
    protected OkHttpClient client;
    protected static String token;

    //Config for our de.se.SAP C4C access using OData with basic auth
    protected final static String AUTH_URL = "url";

    protected final static String USER = "uniaugsburg";
    protected final static String PW = "pw1234566";
    public final static int OUR_CUSTOMER_ID = 4000562;

    protected final static String UNENCODED_AUTH_HEADER = USER + ":" + PW;
    protected final static String AUTH_HEADER = "Basic " + new String(Base64.getEncoder().encode(UNENCODED_AUTH_HEADER.getBytes()));

    protected static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    /**
     * Initializes the SAP service so requests to the SAP C4C system can be executed afterwards
     */
    public void initialize() {
        //Create Cookie Jar to store relevant session cookies
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

        //Create HTTP client for SAP
        client = new OkHttpClient.Builder()
                .cookieJar(new NonPersistentCookieJar())
                .connectTimeout(10, TimeUnit.SECONDS) //to prevent SocketTimeoutException
                .writeTimeout(10, TimeUnit.SECONDS) //to prevent SocketTimeoutException
                .readTimeout(30, TimeUnit.SECONDS) //to prevent SocketTimeoutException
                .build();
        logger.info("Started SAP Connection HTTP Client.");

        requestAuthToken();

        //initialize member variables
        if (this instanceof ServiceRequestServiceImpl) {
            ((ServiceRequestServiceImpl) this).getAllServiceRequests();
        } else if (this instanceof EventServiceImpl) {
            ((EventServiceImpl) this).getAllEvents();
        } else {
            logger.warn("Unknown service - expected service request service or event service");
        }
    }

    /**
     * Requests an auth token which is needed for communication with the SAP C4C system
     */
    protected void requestAuthToken() {
        try {
            token = obtainToken();
            logger.info ("Obtained auth token for SAP C4C: " + token);
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("Could not obtain auth token for SAP C4C");
        }
    }

    /**
     * Obtains a csrf-token that needs to be used with modifying HTTP requests (e.g. PUT, POST, ...)
     * @return the csrf-token as a String
     * @throws IOException in case of an error while obtaining the csrf-token
     */
    protected String obtainToken() throws IOException {
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", AUTH_HEADER)
                .addHeader("x-csrf-token", "fetch")
                .url(AUTH_URL)
                .build();

        Response response = client.newCall(request).execute();
        return response.header("x-csrf-token");
    }

    /**
     * Fetches a data string from the SAP C4C system
     * This method is called in the subclasses for appointments or service requests respectively
     * @param url the url where the data should be fetched from
     * @return a String containing the response from the SAP C4C system
     * @throws IOException in case something goes wrong while fetching the response from the SAP C4C system
     */
    protected String fetchStringFromSAPC4C(String url) throws IOException {
        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("Authorization", AUTH_HEADER)
                .url(url)
                .build();

        Response response = client.newCall(request).execute();
        String bodyString = "";
        if (response != null && response.body() != null) {
            bodyString = response.body().string();
            response.body().close();
        }

        return bodyString;
    }

    /**
     * Executes the given HTTP request and prints a message
     * @param request the HTTP request to execute
     * @param message a message indicating the success or failure of the HTTP request execution
     * @return The HTTP code of the response
     */
    protected int executeHTTPRequest(Request request, String message) {
        Response response = null;
        try {
            response = client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
            logger.error("The following exception occurred while " + message + " in the SAP C4C system: " +
                    e.getMessage());
        }

        if (response != null) {
            logger.info("SAP response code to " + message + ": " + response.code());
            return response.code();
        } else {
            return 555; //error in communication with SAP
        }
    }

    protected String getSafeStringFromJsonElement(JsonElement jsonElement) { //to avoid NPEs
        if (jsonElement == null || jsonElement == JsonNull.INSTANCE) {
            return "";
        } else {
            return jsonElement.getAsString();
        }
    }

    /**
     * Converts a date from SAP format to an equivalent date in Java format
     * If the parsing was unsuccessful, a date two weeks from now is returned
     * @param sapDate a SAP date, formatted as a string
     * @return a Java date object representing the input SAP date
     */
    protected Date convertSAPDateToJavaDate(String sapDate) {
        if (sapDate.isEmpty()) {
            //logger.warn("Empty date in SAP service - Returning current time + 2 weeks as date"); //this would be too much spamming
            return getDateInTwoWeeks();
        }
        try {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(sapDate); //match the SAP date format
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Could not extract date from SAP string: \"" + sapDate + "\".  - Returning current time + 2 weeks as date");
            return getDateInTwoWeeks();
        }
    }

    /**
     * Computes a date two weeks from now in the future. Used as a fallback in case of date parsing errors with SAP
     * @return a Java date object with the time set to two weeks from now
     */
    private Date getDateInTwoWeeks() {
        Date date = new Date();
        date.setTime(date.getTime() + 2 * 7 * 24 * 60 * 60 * 1000); //we add two weeks
        return date;
    }

    /**
     * Converts a date in Java format to an equivalent date in SAP format
     * @param date a Java date object
     * @return a String representing the input date in SAP format
     */
    protected String convertJavaDateToSAPDate(Date date) {
        String dateString = "";
        try {
            dateString = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date); //match the SAP date format
        } catch (Exception e) {
            logger.warn("The following exception occurred while parsing date: " + e.getMessage());
        }

        return dateString;
    }

    /**
     * Creation dates from SAP have another format and have to be passed differently.
     * @param sapDate the SAP creation date as a String
     * @return a Java date corresponding to the passed SAP date
     */
    protected Date convertSAPCreationDateToJavaDate(String sapDate) {
        if (sapDate.isEmpty()) {
            logger.warn("Empty date in SAP service - Returning current time + 2 weeks as date");
            return getDateInTwoWeeks();
        }
        try {
            sapDate = sapDate.substring(6, sapDate.length() - 2); //cut off "/Date(" and trailing ")/" -> this is how the SAP creation date is formatted
            long millis = Long.parseLong(sapDate);
            return new Date(millis);
        } catch (Exception e) {
            e.printStackTrace();
            logger.warn("Could not extract date from SAP string: \"" + sapDate + "\".  - Returning current time + 2 weeks as date");
            return getDateInTwoWeeks();
        }
    }

}
