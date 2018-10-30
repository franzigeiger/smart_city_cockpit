package de.se.model.impl;

import com.google.gson.*;
import de.se.SAP.SAPService;
import de.se.data.AppointmentInvolvedParties;
import de.se.data.Event;
import de.se.model.interfaces.EventService;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class EventServiceImpl extends SAPService implements EventService {

    private ArrayList<Event> events = new ArrayList<>();

    private final static String APPOINTMENT_COLLECTION_URL = AUTH_URL + "AppointmentCollection";
    private final static String EXPANDED_APPOINTMENT_COLLECTION_URL =
            APPOINTMENT_COLLECTION_URL +
                    "?$orderby=CreatedOn" +
                    "&$expand=AppointmentNotes,AppointmentInvolvedParties"; //we need this expanded so we can access all the nested attributes

    private Logger logger = Logger.getLogger(EventServiceImpl.class);

    /*We need this to update our list of events in case a new event has been added
      because we only get the SAP object ID by pulling from the SAP database again.
      Without a SAP object ID we cannot delete an event.
      We also pull the data from SAP again after deleting an event.*/
    private boolean savedOrDeletedAnEventAfterPullingFromSAP = false;

    private String fetchAppointmentsStringFromSAPC4C() throws IOException {
        return fetchStringFromSAPC4C(EXPANDED_APPOINTMENT_COLLECTION_URL);
    }

    @Override
    public List<Event> getAllEvents() {
        if (this.events.isEmpty() || this.savedOrDeletedAnEventAfterPullingFromSAP) {
            //else we have already pulled the latest version from SAP and return the cached events
            if (token == null) {
                requestAuthToken();
            }

            String appointmentsJsonString = null;
            try {
                appointmentsJsonString = fetchAppointmentsStringFromSAPC4C();
                logger.info("Fetched appointments from SAP C4C");
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Could not fetch appointments from SAP C4C");
            }

            if (appointmentsJsonString != null && !appointmentsJsonString.isEmpty()) {
                //first parse the string that we got from SAP to a JSON and then parse it into our event entities
                JsonObject wrappedAppointments = new JsonParser().parse(appointmentsJsonString).getAsJsonObject();
                this.events = parseAppointments(wrappedAppointments);
            }

            this.savedOrDeletedAnEventAfterPullingFromSAP = false; //enable caching again
        }

        return this.events;
    }

    /**
     * Parses the appointments we got from SAP into our data structure: a list of Events
     * @param wrappedAppointmentsJson a Json object containing the appointment data from SAP
     * @return a list of events constructed from the input data
     */
    private ArrayList<Event> parseAppointments(JsonObject wrappedAppointmentsJson) {
        ArrayList<Event> events = new ArrayList<>();
        JsonArray eventsJsonArray = wrappedAppointmentsJson.get("d").getAsJsonObject().get("results").getAsJsonArray();

        for (JsonElement eventJsonElement : eventsJsonArray) {
            JsonObject singleEventJson = eventJsonElement.getAsJsonObject();

            if (singleEventJson.get("AccountID").getAsInt() != OUR_CUSTOMER_ID) {
                continue; //the service request is not from us -> do not add it to the result
            }

            Event event = new Event();
            //fill the event with the data we got from our request to the SAP C4C system
            event.setSapObjectID(singleEventJson.get("ObjectID").getAsString());
            event.setId(singleEventJson.get("ID").getAsInt());
            event.setTitle(singleEventJson.get("Subject").getAsString());
            event.setStartTimestamp(new Timestamp(convertSAPCreationDateToJavaDate(getSafeStringFromJsonElement(
                    singleEventJson.get("StartDateTime").getAsJsonObject().get("content"))).getTime()));
            event.setEndTimestamp(new Timestamp(convertSAPCreationDateToJavaDate(getSafeStringFromJsonElement(
                    singleEventJson.get("EndDateTime").getAsJsonObject().get("content"))).getTime()));
            event.setLocation(singleEventJson.get("LocationName").getAsString());

            fillNestedObjectsInEvent(singleEventJson, event);

            events.add(event);
        }

        return events;
    }

    private void fillNestedObjectsInEvent(JsonObject singleEventJson, Event event) {
        //AppointmentNotes and AppointmentInvolvedParties are nested fields. The values are extracted here.

        try {
            JsonArray descriptions = singleEventJson.get("AppointmentNotes").getAsJsonArray();
            if (descriptions.size() == 0) {
                event.setDescription("");
            } else {
                event.setDescription(
                        getSafeStringFromJsonElement(descriptions.get(0).getAsJsonObject().get("Text"))); //get the actual description
            }
        } catch (IllegalStateException e) { //this means there are no appointment notes for this appointment - everything is fine
            event.setDescription("");
        }

        try {
            JsonArray involvedParties = singleEventJson.get("AppointmentInvolvedParties").getAsJsonArray();
            //the involved parties are not empty - SAP sets some involved parties by default which we ignore here
            event.setInvolvedParty(""); //we initialize it to empty string
            String test = involvedParties.get(involvedParties.size() - 1).getAsJsonObject().get("PartyID").getAsString();
            int testInt = Integer.parseInt(test);
            for (JsonElement partyJson : involvedParties) { //we are looking for the right involved party that we set
                String involvedPartyName = "";
                try {
                    involvedPartyName = AppointmentInvolvedParties.getInvolvedPartyName(Integer.parseInt(
                            partyJson.getAsJsonObject().get("PartyID").getAsString())); //get the actual party ID
                } catch (NumberFormatException e) {
                    continue;
                }
                if (!involvedPartyName.isEmpty()) { //we found the correct involved party
                    event.setInvolvedParty(involvedPartyName);
                    break;
                }
            }
        } catch (Exception e) { //this means there are no appointment involved parties for this appointment - everything is fine
            event.setInvolvedParty("");
        }
    }

    @Override
    public int saveEvent(Event event) {
        requestAuthToken();
        //now we build a JSON event from the passed event so we can send it to the SAP system
        JsonObject SAPAppointment = new JsonObject();
        SAPAppointment.addProperty("Subject", event.getTitle());
        SAPAppointment.addProperty("AccountID", OUR_CUSTOMER_ID + "");

        JsonObject startDateTime = buildDateTimeJsonObjectFromMillis(event.getStartTimestamp().getTime());
        SAPAppointment.add("StartDateTime", startDateTime);
        JsonObject endDateTime = buildDateTimeJsonObjectFromMillis(event.getEndTimestamp().getTime());
        SAPAppointment.add("EndDateTime", endDateTime);

        SAPAppointment.addProperty("LocationName", event.getLocation());
        SAPAppointment.add("AppointmentNotes",
                buildAppointmentNotesJsonArrayFromDescriptionString(event.getDescription()));
        SAPAppointment.add("AppointmentInvolvedParties",
                buildAppointmentInvolvedPartiesJsonArrayFromInvolvedPartyString(event.getInvolvedParty()));

        Gson gson = new Gson();
        String bodyStringOfAppointment = gson.toJson(SAPAppointment);
        logger.info("Body of SAP appointment to save: " + bodyStringOfAppointment);
        RequestBody body = RequestBody.create(JSON, bodyStringOfAppointment);

        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("x-csrf-token", token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", AUTH_HEADER)
                .url(APPOINTMENT_COLLECTION_URL)
                .post(body)
                .build();

        int sapResponseCode = executeHTTPRequest(request, "saving an appointment");
        this.savedOrDeletedAnEventAfterPullingFromSAP = true;
        return sapResponseCode;
    }

    @Override
    public String getName() {
        return EventServiceImpl.class.toString();
    }

    @Override
    public int removeEvent(Event event) {
        requestAuthToken();
        JsonObject SAPAppointment = new JsonObject();

        SAPAppointment.addProperty("ObjectID", event.getSapObjectID()); //this uniquely identifies an event in the SAP system

        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("x-csrf-token", token)
                .addHeader("Authorization", AUTH_HEADER)
                .url(APPOINTMENT_COLLECTION_URL + "('" + event.getSapObjectID() + "')")
                .delete()
                .build();

        int sapResponseCode = executeHTTPRequest(request, "deleting an appointment");
        this.savedOrDeletedAnEventAfterPullingFromSAP = true;
        return sapResponseCode;
    }

    /**
     * Constructs a Json object from milliseconds since epoch
     * This is needed for setting the start and end datetime when saving appointments to SAP
     * @param millis the milliseconds since epoch
     * @return a Json object with additional metadata that SAP needs
     */
    private JsonObject buildDateTimeJsonObjectFromMillis(long millis) {
        JsonObject dateTime = new JsonObject();
        JsonObject metadata = new JsonObject();
        metadata.addProperty("type", "c4codata.LOCALNORMALISED_DateTime"); //this is the format that SAP wants
        dateTime.add("__metadata", metadata);

        dateTime.addProperty("timeZoneCode", "CET");
        dateTime.addProperty("content", "/Date(" + millis + ")/"); //comply with the SAP date format
        return dateTime;
    }

    /**
     * Constructs a Json array from an appointment description
     * This is needed for setting the appointment notes reference when saving appointments to SAP
     * @param description a string containing the description of an appointment
     * @return a Json array containing a single appointment note with the passed appointment description
     */
    private JsonArray buildAppointmentNotesJsonArrayFromDescriptionString(String description) {
        JsonObject singleAppointmentNote = new JsonObject();
        singleAppointmentNote.addProperty("Text", description);
        singleAppointmentNote.addProperty("TypeCode", "10002"); //this is just a default but mandatory value

        JsonArray appointmentNotes = new JsonArray();
        appointmentNotes.add(singleAppointmentNote);

        return appointmentNotes;
    }

    /**
     * Constructs a Json array from an appointment involved party
     * This is needed for setting the appointment involved parties when saving appointments to SAP
     * @param involvedParty a string describing the involved party
     * @return a Json array containing a single involved party with the passed appointment involved party
     */
    private JsonElement buildAppointmentInvolvedPartiesJsonArrayFromInvolvedPartyString(String involvedParty) {
        JsonObject singleInvolvedPartyJson = new JsonObject();
        singleInvolvedPartyJson.addProperty("PartyID", AppointmentInvolvedParties.getPartyID(involvedParty) + "");
        singleInvolvedPartyJson.addProperty("RoleCode", "34"); //"34" for customer
        singleInvolvedPartyJson.addProperty("PartyTypeCode", "167"); //"167" for appointment

        JsonArray appointmentInvolvedParty = new JsonArray();
        appointmentInvolvedParty.add(singleInvolvedPartyJson);

        return appointmentInvolvedParty;
    }
}
