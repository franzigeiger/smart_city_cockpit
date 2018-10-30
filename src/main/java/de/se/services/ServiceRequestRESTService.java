package de.se.services;

import de.se.data.RequestItem;
import de.se.data.ServiceRequest;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.ServiceRequestService;
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
public class ServiceRequestRESTService {

    Logger logger = Logger.getLogger(ServiceRequestRESTService.class);

    @GET
    @Path("/services")
    @Produces(MediaType.APPLICATION_JSON)
    public String getServiceRequests() throws IOException {
        try{
            ServiceRequestService service = ServiceRegistry.getService(ServiceRequestService.class);
            JSONArray serviceRequests = new JSONArray();
            for(ServiceRequest request : service.getAllServiceRequests()){
                JSONObject requestJson = new JSONObject();
                requestJson.put("id",request.getId());
                requestJson.put("creation", request.getCreationDate().getTime());
                requestJson.put("name", request.getName());
                requestJson.put("dueDate", request.getRequestedEnd().getTime());
                requestJson.put("description", request.getServiceRequestDescription());
                requestJson.put("objectType", request.getReferenceType());
                requestJson.put("priority", request.getServicePriorityCode());
                requestJson.put("objectId", request.getReferenceID());
                requestJson.put("feedback", request.getFeedbackReferenceID());
                String serviceType = "";
                switch(request.getProcessingTypeCode()){
                    case SRRQ: serviceType = "Maintenance";
                        break;
                    case ZCLN: serviceType = "Cleaning";
                        break;
                }

                requestJson.put("state", request.getLifeCycleCodeString());
                requestJson.put("serviceType", serviceType);

                serviceRequests.add(requestJson);
            }

            return serviceRequests.toJSONString();
        } catch(Exception e){
            logger.error(e.getMessage() );
            JSONObject error = new JSONObject();
            error.put("error","An error occured: " + e.getMessage());
            return error.toJSONString();
        }
    }


    @POST
    @Path("/service/new")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response saveServiceRequest(String json) throws ParseException {
        try {
            JSONParser parser = new JSONParser();
            JSONObject serviceToAdd = (JSONObject) parser.parse(json);
            String reference = (String) serviceToAdd.get("referenceId");
            String type = (String) serviceToAdd.get("type");
            String name = (String) serviceToAdd.get("name");
            long dueDate = (long) serviceToAdd.get("dueDate");
            String description = (String) serviceToAdd.get("description");
            String serviceType = (String) serviceToAdd.get("serviceType");
            JSONArray feedbacks = (JSONArray) serviceToAdd.get("feedback");
            String feedbackReferenceIDsString = "";
            if(feedbacks != null) {
                for (int i = 0; i < feedbacks.size(); i++) {
                    Long feedback = (Long) feedbacks.get(i);
                    feedbackReferenceIDsString += feedback.toString();
                    if (i < feedbacks.size() - 1) {
                        feedbackReferenceIDsString += ",";
                    }
                }
            }
            int priority =  Integer.parseInt(serviceToAdd.get("priority").toString());

            RequestItem item = null;
            switch (type.toLowerCase()) {
                case "stop":
                    item = ServiceRegistry.getService(Network.class).getStopPerID(reference);
                    break;
                case "vehicle":
                    item = ServiceRegistry.getService(FleetService.class).getVehicleForId(reference);
                    break;
                default:
                    throw new Exception("The reference type cannot be solved!");
            }

            if(item == null){
                throw new Exception("There was no item found for given reference id!");
            }

            ServiceRequest.PROCESSING_TYPE_CODE code= null;
            switch(serviceType){
                case "Maintenance": code = ServiceRequest.PROCESSING_TYPE_CODE.SRRQ ;
                    break;
                case "Cleaning": code = ServiceRequest.PROCESSING_TYPE_CODE.ZCLN;
                    break;

                default:
                    code = ServiceRequest.PROCESSING_TYPE_CODE.ZCLN;
            }

            //Must be set in backend: id, serviceRequestLifecycleCode, completionOnDate
            //ServiceEnum is not set too, because its difficult to calculate and not necessary
            //this should be changed.
            //name is delivered but cannot be set. We have to think about that.
            ServiceRequest newRequest = new ServiceRequest(-1, priority, new Date(dueDate), new Date(),
                    description, item , code, feedbackReferenceIDsString, type, name);

            ServiceRequestService service = ServiceRegistry.getService(ServiceRequestService.class);
            int sapResponseCode = service.saveServiceRequest(newRequest);

            return Response.status(sapResponseCode).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.CONFLICT.getStatusCode(), e.getMessage()).build();
        }

    }

}
