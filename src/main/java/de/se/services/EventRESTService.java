package de.se.services;

import de.se.data.Event;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.EventService;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Date;


@Path("")
public class EventRESTService {

    Logger logger = Logger.getLogger(EventRESTService.class);

    @GET
    @Path("/events")
    @Produces(MediaType.APPLICATION_JSON)
    public String getEvents() throws IOException {
        try{
        EventService service =ServiceRegistry.getService(EventService.class);
        JSONArray events = new JSONArray();
        for(Event event : service.getAllEvents()){
            JSONObject eventJson = new JSONObject();
            eventJson.put("id", event.getId());
            eventJson.put("start", event.getStartTimestamp().getTime());
            eventJson.put("end", event.getEndTimestamp().getTime());
            eventJson.put("location", event.getLocation());
            eventJson.put("title", event.getTitle());
            eventJson.put("description", event.getDescription());
            eventJson.put("involved", event.getInvolvedParty());

            events.add(eventJson);
        }

        return events.toJSONString();
        }catch(Exception e){
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }


    @POST
    @Path("/event/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveEvent(String json) throws ParseException {
        JSONParser parser = new JSONParser();
        JSONObject eventTOAdd =(JSONObject) parser.parse(json);
        long start  = (long)eventTOAdd.get("start");
        long end  = (long)eventTOAdd.get("end");
        String title  = (String)eventTOAdd.get("title");
        String description  = (String)eventTOAdd.get("description");
        String location  = (String)eventTOAdd.get("location");
        String involved = (String)eventTOAdd.get("involved");


        //id must be changed in service.
         Event newEvent = new Event(0, title, new Date(start) , new Date(end), location, description, involved);

         EventService service = ServiceRegistry.getService(EventService.class);
         int sapResponseCode = service.saveEvent(newEvent);

         return Response.status(sapResponseCode).build();
    }

}
