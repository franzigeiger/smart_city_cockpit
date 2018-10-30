package de.se.model.impl;

import com.google.gson.*;
import de.se.SAP.SAPService;
import de.se.data.*;
import de.se.data.enums.ServiceEnum;
import de.se.data.enums.StopProblem;
import de.se.data.enums.VehicleProblem;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FeedbackService;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.ServiceRequestService;
import okhttp3.Request;
import okhttp3.RequestBody;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceRequestServiceImpl extends SAPService implements ServiceRequestService {

    private ArrayList<ServiceRequest> serviceRequests = new ArrayList<>();

    private final static String SERVICE_COLLECTION_URL = AUTH_URL + "ServiceRequestCollection";
    private final static String EXPANDED_SERVICE_COLLECTION_URL =
            SERVICE_COLLECTION_URL +
                    //"?$orderby=CreationDate" +
                    //"?$search='Uni Augsburg Gruppe 03'" + //filter only for our service requests
                    "?$expand=ServiceRequestDescription"; //we need this expanded so we can access all the nested attributes
                    //+ "$skiptoken=101" //in case we want to skip stuff

    /*We need this to update our list of service requests in case a new service request has been added
      because we only get the SAP object ID by pulling from the SAP database again.
      Without a SAP object ID we cannot delete a service request.
      We also pull the data from SAP again after deleting a service request.*/
    private boolean savedOrDeletedAServiceRequestAfterPullingFromSAP = false;


    private String fetchServiceRequestsStringFromSAPC4C() throws IOException {
        return fetchStringFromSAPC4C(EXPANDED_SERVICE_COLLECTION_URL);
    }

    @Override
    public ArrayList<ServiceRequest> getAllServiceRequests() {
        if (this.serviceRequests.isEmpty() || this.savedOrDeletedAServiceRequestAfterPullingFromSAP) {
            //else we have already pulled the latest version from SAP and return the cached service requests

            if (token == null) {
                requestAuthToken();
            }
            //Fetch service requests
            String serviceRequestJsonString = null;
            try {
                serviceRequestJsonString = fetchServiceRequestsStringFromSAPC4C();
                logger.info ("Fetched service requests from SAP C4C");
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Could not fetch service requests from SAP C4C");
            }

            if (serviceRequestJsonString != null && !serviceRequestJsonString.isEmpty()) {
                //first parse the string that we got from SAP to a JSON and then parse it into our service request entities
                JsonObject wrappedServiceRequests = new JsonParser().parse(serviceRequestJsonString).getAsJsonObject();
                this.serviceRequests = parseServiceRequests(wrappedServiceRequests);
            }

            this.savedOrDeletedAServiceRequestAfterPullingFromSAP = false; //enable caching again
        }

        return this.serviceRequests;
    }

    /**
     * Parses the service requests we got from SAP into our data structure: a list of ServiceRequests
     * @param wrappedServiceRequestsJson a JSON object containing the service request data from SAP
     * @return a list of service requests constructed from the input data
     */
    private ArrayList<ServiceRequest> parseServiceRequests(JsonObject wrappedServiceRequestsJson) {
        int skippedServiceRequests = 0; //keep track of how many service requests we already skipped
        ArrayList<ServiceRequest> serviceRequests = new ArrayList<>();

            //this is how the JSON is formatted:
            JsonArray serviceRequestsJsonArray = wrappedServiceRequestsJson.get("d").getAsJsonObject().get("results").getAsJsonArray();

            while (serviceRequestsJsonArray != null) { //SAP only sends 100 service requests at once
                for (JsonElement serviceRequestJsonElement : serviceRequestsJsonArray) { //iterate over all service requests
                    JsonObject singleServiceRequestJson = serviceRequestJsonElement.getAsJsonObject();

                    try {
                        if (singleServiceRequestJson.get("CustomerID").getAsInt() != OUR_CUSTOMER_ID) {
                            continue; //the service request is not from us -> do not add it to the result
                        }
                    } catch (NumberFormatException e) { //this is no problem, some customer IDs are no integers (but ours is)
                        continue;
                    }

                    if (singleServiceRequestJson.get("ServiceRequestClassificationCode").getAsString().equals("3")) {
                        continue; // the service request has been set to 'not relevant' -> do not add it to the result (this is equal to a deleted service request)
                    }

                    ServiceRequest serviceRequest = new ServiceRequest();
                    //fill the service request entity with the data we got from our request to the SAP C4C system
                    serviceRequest.setSapObjectID(singleServiceRequestJson.get("ObjectID").getAsString());
                    serviceRequest.setId(singleServiceRequestJson.get("ID").getAsInt());
                    serviceRequest.setName(getSafeStringFromJsonElement(
                            singleServiceRequestJson.get("Name").getAsJsonObject().get("content")));
                    serviceRequest.setCustomerID(singleServiceRequestJson.get("CustomerID").getAsInt());
                    serviceRequest.setServicePriorityCode(singleServiceRequestJson.get("ServicePriorityCode").getAsInt());
                    serviceRequest.setServiceRequestLifeCycleStatusCode(
                            singleServiceRequestJson.get("ServiceRequestLifeCycleStatusCode").getAsInt());

                    serviceRequest.setRequestedEnd(convertSAPDateToJavaDate(
                            getSafeStringFromJsonElement(singleServiceRequestJson.get("RequestedEnd"))));

                     /*serviceRequest.setCompletedOnDate(convertSAPDateToJavaDate(
                        getSafeStringFromJsonElement(singleServiceRequestJson.get("CompletedOnDate"))));*/ //this field has been dismissed

                    JsonArray descriptions = singleServiceRequestJson.get("ServiceRequestDescription").getAsJsonArray(); //this JSON array is nested
                    if (descriptions.size() == 0) {
                        serviceRequest.setServiceRequestDescription("");
                    } else {
                        serviceRequest.setServiceRequestDescription(
                                getSafeStringFromJsonElement(descriptions.get(0).getAsJsonObject().get("Text"))); //get the actual description
                    }

                    serviceRequest.setProcessingTypeCode(ServiceRequest.PROCESSING_TYPE_CODE.valueOf(
                            getSafeStringFromJsonElement(singleServiceRequestJson.get("ProcessingTypeCode"))));
                    serviceRequest.setReferenceID(singleServiceRequestJson.get("ReferenceID").getAsString());

                    if (serviceRequest.getReferenceID().startsWith("T_") || serviceRequest.getReferenceID().startsWith("B_") || //all vehicle names start with "T_" or "B_"
                            serviceRequest.getReferenceID().startsWith("Vehicle")) { //historical reasons: old vehicles actually start with "Vehicle_"
                        serviceRequest.setReferenceType("Vehicle");
                    } else { //otherwise it has to be a stop
                        serviceRequest.setReferenceType("Stop");
                    }

                    String feedbackRefIDString = singleServiceRequestJson.get("FeedbackReference").getAsString();
                    if (feedbackRefIDString.endsWith("...")) {
                        //the string has been truncated when saving this service request before because SAP only allows 36 characters
                        logger.warn("Found truncated feedback reference ID string: \"" + feedbackRefIDString + "\". " +
                                "Removing the last entry from it ...");
                        feedbackRefIDString = feedbackRefIDString.substring(0, feedbackRefIDString.lastIndexOf(",")); //so we remove the last entry
                    }
                    serviceRequest.setFeedbackReferenceID(feedbackRefIDString);

                    serviceRequest.setCreationDate(convertSAPCreationDateToJavaDate(
                            getSafeStringFromJsonElement(singleServiceRequestJson.get("CreationDate"))));
                    serviceRequest.setName(getSafeStringFromJsonElement(
                            singleServiceRequestJson.get("Name").getAsJsonObject().get("content")));

                    serviceRequests.add(serviceRequest);
                }

                JsonElement nextJson = wrappedServiceRequestsJson.get("d").getAsJsonObject().get("__next"); //SAP only puts 100 service requests into one response

                if (nextJson != null) { //new request for next 100 service requests
                    String serviceRequestJsonString = null;
                    try {
                        serviceRequestJsonString = fetchStringFromSAPC4C(EXPANDED_SERVICE_COLLECTION_URL +
                                "&$skiptoken=" + (101 + skippedServiceRequests));
                        skippedServiceRequests += 100; //increment for next request
                        logger.info ("Fetched service requests part II from SAP C4C");
                    } catch (IOException e) {
                        e.printStackTrace();
                        logger.error("Could not fetch service requests part II from SAP C4C");
                    }

                    if (serviceRequestJsonString != null && serviceRequestJsonString.isEmpty() == false) {
                        wrappedServiceRequestsJson = new JsonParser().parse(serviceRequestJsonString).getAsJsonObject();
                    }

                    serviceRequestsJsonArray = wrappedServiceRequestsJson.get("d").getAsJsonObject().get("results").getAsJsonArray();
                } else {
                    break; //we got all service requests
                }
            }

        return serviceRequests;
    }


    @Override
    public int saveServiceRequest(ServiceRequest serviceRequest) {
        requestAuthToken(); //just to be sure we request another auth token
        JsonObject name = new JsonObject();
        name.addProperty("content", serviceRequest.getName());

        JsonObject serviceRequestText = new JsonObject();
        serviceRequestText.addProperty("Text", serviceRequest.getServiceRequestDescription());
        JsonArray serviceRequestDescription = new JsonArray();
        serviceRequestDescription.add(serviceRequestText);

        //here we build a JSON object to send to the SAP system. We fill it with the data from the passed service request
        JsonObject SAPServiceRequest = new JsonObject();
        SAPServiceRequest.add("Name", name);
        SAPServiceRequest.addProperty("CustomerID", OUR_CUSTOMER_ID + ""); //our customer ID
        SAPServiceRequest.addProperty("ServicePriorityCode", serviceRequest.getServicePriorityCode() + "");
        SAPServiceRequest.addProperty("ServiceRequestLifeCycleStatusCode", serviceRequest.getServiceRequestLifeCycleStatusCode() + "");

        SAPServiceRequest.addProperty("RequestedEnd", convertJavaDateToSAPDate(serviceRequest.getRequestedEnd()) + "");
        //SAPServiceRequest.addProperty("CompletedOnDate", convertJavaDateToSAPDate(serviceRequest.getCompletedOnDate()) + ""); //this field has been dismissed

        SAPServiceRequest.add("ServiceRequestDescription", serviceRequestDescription);
        SAPServiceRequest.addProperty("ProcessingTypeCode", serviceRequest.getProcessingTypeCode().toString());
        SAPServiceRequest.addProperty("ReferenceID", serviceRequest.getReferenceID());

        String feedbackRefIDString = serviceRequest.getFeedbackReferenceID();
        if (feedbackRefIDString.length() > 36) { //maximum size of this field in the SAP system is 36
            logger.warn("Truncating the following feedback reference ID string because the SAP system only allows 36 characters: \""
                    + feedbackRefIDString + "\"");
            feedbackRefIDString = feedbackRefIDString.substring(0, 32) + "...";
        }
        SAPServiceRequest.addProperty("FeedbackReference", feedbackRefIDString);

        setFeedbackToCompleted(serviceRequest); //after a service request has been created we set the corresponding feedback to completed
        deleteProblemsAfterServiceRequestGeneration(serviceRequest); //after a service request has been created we delete corresponding problems that make sense

        //"CreationDate" gets set automatically by SAP

        Gson gson = new Gson();
        String bodyStringOfServiceRequest = gson.toJson(SAPServiceRequest);
        logger.info("Body of SAP service request to save: " + bodyStringOfServiceRequest);
        RequestBody body = RequestBody.create(JSON, bodyStringOfServiceRequest);

        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("x-csrf-token", token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", AUTH_HEADER)
                .url(SERVICE_COLLECTION_URL)
                .post(body) //POST the request to the SAP C4C system
                .build();

        int responseCode = executeHTTPRequest(request, "saving a service request");
        this.savedOrDeletedAServiceRequestAfterPullingFromSAP = true; //this indicates that we have to pull from SAP when getting the service requests next time
        return responseCode;
    }

    /**
     * Sets a feedback to completed. We do this here because a service request for this feedback was created.
     * @param serviceRequest the service request containing the vehicle or stop id
     */
    private void setFeedbackToCompleted(ServiceRequest serviceRequest) {
        if (serviceRequest.getFeedbackReferenceID().isEmpty()) {
            //the service request has no feedback reference -> we cannot set a feedback to completed
            return;
        }

        FeedbackService feedbackService = ServiceRegistry.getService(FeedbackService.class);
        String [] feedbackIDs = serviceRequest.getFeedbackReferenceID().split(","); //this is how the reference IDs are formatted
        for (String feedbackID : feedbackIDs) {
            try {
                int feedbackIDInt = Integer.parseInt(feedbackID);
                feedbackService.setFeedbackFinished(feedbackIDInt);
            } catch (NumberFormatException e) {
                logger.warn("The feedback id is not an integer so we cannot set the feedback to completed: \""
                        + feedbackID + "\"");
            } catch (Exception e) {
                logger.warn("Could not set the feedback with ID \"" + feedbackID + "\" to completed.");
            }
        }
    }

    /**
     * Deletes problems for the vehicle or stop because a service request was created
     * @param serviceRequest the service request containing the vehicle or stop id
     */
    private void deleteProblemsAfterServiceRequestGeneration(ServiceRequest serviceRequest) {
        if (serviceRequest.getReferenceID().isEmpty()) {
            return;
        }

        Network network = ServiceRegistry.getService(Network.class);
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);

        if (serviceRequest.getReferenceType().equals("Vehicle")) {
            Vehicle vehicle = fleetService.getVehicleForId(serviceRequest.getReferenceID()); //find the referenced vehicle
            //depending on the service request type we delete meaningful problems from the vehicle
            if (serviceRequest.getProcessingTypeCode().equals(ServiceRequest.PROCESSING_TYPE_CODE.SRRQ)) {
                List<TimedVehicleProblem> newListOfVehicleProblems = getVehicleProblemsToKeep(ServiceEnum.VehicleMaintenance, vehicle);
                vehicle.setProblems(newListOfVehicleProblems);
            } else if (serviceRequest.getProcessingTypeCode().equals(ServiceRequest.PROCESSING_TYPE_CODE.ZCLN)) {
                List<TimedVehicleProblem> newListOfVehicleProblems = getVehicleProblemsToKeep(ServiceEnum.VehicleCleaning, vehicle);
                vehicle.setProblems(newListOfVehicleProblems);
            }
        } else if (serviceRequest.getReferenceType().equals("Stop")) {
            Stop stop = network.getStopPerID(serviceRequest.getReferenceID()); //find the referenced stop
            //depending on the service request type we delete meaningful problems from the stop
            if (serviceRequest.getProcessingTypeCode().equals(ServiceRequest.PROCESSING_TYPE_CODE.SRRQ)) {
                List<TimedStopProblem> newListOfStopProblems = getStopProblemsToKeep(ServiceEnum.StopMaintenance, stop);
                stop.setProblems(newListOfStopProblems);
            } else if (serviceRequest.getProcessingTypeCode().equals(ServiceRequest.PROCESSING_TYPE_CODE.ZCLN)) {
                List<TimedStopProblem> newListOfStopProblems = getStopProblemsToKeep(ServiceEnum.StopCleaning, stop);
                stop.setProblems(newListOfStopProblems);
            }
        }
    }

    /**
     * Implicitly deletes vehicle problems as a follow-up to creating a service request
     * by building a list containing all problems except the ones that we want to delete.
     * The problems to delete are determined by heuristics, i.e. a VehicleCleaning service request removes all "Dirty"
     * vehicle problems and a VehicleMaintenance service request removes all Defect... vehicle problems
     * @param serviceEnum the ServiceEnum describing if the created service request was VehicleCleaning or VehicleMaintenance
     * @param vehicle the vehicle from which we want to delete problems
     * @return a list of the TimedVehicleProblems that we want to keep after the service request was created
     */
    private List<TimedVehicleProblem> getVehicleProblemsToKeep(ServiceEnum serviceEnum, Vehicle vehicle) {
        List<TimedVehicleProblem> newListOfVehicleProblems = new ArrayList<>(); //fill a new list from scratch

        for (TimedVehicleProblem timedVehicleProblem : vehicle.getProblems()) {
            if (serviceEnum.equals(ServiceEnum.VehicleCleaning) &&
                    !timedVehicleProblem.getVehicleProblem().equals(VehicleProblem.Dirty)) {
                //this implicitly removes all Dirty problems if a VehicleCleaning service request has been issued
                newListOfVehicleProblems.add(timedVehicleProblem);
            }

            if (serviceEnum.equals(ServiceEnum.VehicleMaintenance) &&
                    (!timedVehicleProblem.getVehicleProblem().equals(VehicleProblem.Defect_Engine) ||
                            !timedVehicleProblem.getVehicleProblem().equals(VehicleProblem.Defect_Air_Conditioner) ||
                            !timedVehicleProblem.getVehicleProblem().equals(VehicleProblem.Defect_Door) ||
                            !timedVehicleProblem.getVehicleProblem().equals(VehicleProblem.Defect_Wheel))) {
                //this implicitly removes all Defect... problems if a VehicleMaintenance service request has been issued
                newListOfVehicleProblems.add(timedVehicleProblem);
            }
        }
        return newListOfVehicleProblems;
    }

    /**
     * Implicitly deletes stop problems as a follow-up to creating a service request
     * by building a list containing all problems except the ones that we want to delete.
     * The problems to delete are determined by heuristics, i.e. a StopCleaning service request removes all "Dirty"
     * stop problems and a StopMaintenance service request removes all Broken stop problems
     * @param serviceEnum the ServiceEnum describing if the created service request was StopCleaning or StopMaintenance
     * @param stop the stop from which we want to delete problems
     * @return a list of the TimedStopProblems that we want to keep after the service request was created
     */
    private List<TimedStopProblem> getStopProblemsToKeep(ServiceEnum serviceEnum, Stop stop) {
        List<TimedStopProblem> newListOfStopProblems = new ArrayList<>(); //fill a new list from scratch

        for (TimedStopProblem timedStopProblem : stop.getProblems()) {
            if (serviceEnum.equals(ServiceEnum.StopCleaning) &&
                    !timedStopProblem.getStopProblem().equals(StopProblem.Dirty)) {
                //this implicitly removes all Dirty problems if a StopCleaning service request has been issued
                newListOfStopProblems.add(timedStopProblem);
            }

            if (serviceEnum.equals(ServiceEnum.StopMaintenance) &&
                    !timedStopProblem.getStopProblem().equals(StopProblem.Broken)) {
                //this implicitly removes all Broken problems if a StopCleaning service request has been issued
                newListOfStopProblems.add(timedStopProblem);
            }
        }
        return newListOfStopProblems;
    }

    @Override
    public int removeServiceRequest(ServiceRequest serviceRequest) {
        requestAuthToken(); //request an auth token just to be sure
        JsonObject SAPServiceRequest = new JsonObject();

        SAPServiceRequest.addProperty("ObjectID", serviceRequest.getSapObjectID()); //this uniquely identifies a service request in the SAP system

        if (serviceRequest.getServiceRequestLifeCycleStatusCode() != 1) {
            setStatusToOpen(serviceRequest, SAPServiceRequest); //so we can delete the service request
        }

        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("x-csrf-token", token)
                .addHeader("Authorization", AUTH_HEADER)
                .url(SERVICE_COLLECTION_URL + "('" + serviceRequest.getSapObjectID() + "')")
                .delete()
                .build();

        int responseCode = executeHTTPRequest(request, "deleting a service request");
        this.savedOrDeletedAServiceRequestAfterPullingFromSAP = true;
        return responseCode;
    }

    /**
     * The SAP system does not allow the deletion of service requests with arbitrary life cycle codes.
     * Thus, we set the life cycle code to 1 ("OPEN") to be sure we can delete it.
     * @param serviceRequest the service request containing the SAP object ID needed to identify the service request
     * @param SAPServiceRequest the JSON service request for which we want to set the status to 1 ("OPEN")
     */
    private void setStatusToOpen(ServiceRequest serviceRequest, JsonObject SAPServiceRequest) {
        requestAuthToken();
        SAPServiceRequest.addProperty("ServiceRequestLifeCycleStatusCode", "1"); //"1" is OPEN

        Gson gson = new Gson();
        String bodyStringOfServiceRequest = gson.toJson(SAPServiceRequest);
        logger.info("Body of SAP service request to mark as open: " + bodyStringOfServiceRequest);
        RequestBody body = RequestBody.create(JSON, bodyStringOfServiceRequest);

        Request request = new Request.Builder()
                .addHeader("Accept", "application/json")
                .addHeader("x-csrf-token", token)
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", AUTH_HEADER)
                .url(SERVICE_COLLECTION_URL + "('" + serviceRequest.getSapObjectID() + "')")
                .patch(body)
                .build();

        executeHTTPRequest(request, "marking a service request as open");
    }

    @Override
    public List<ServiceRequest> getServicesForRefID(String refId) {
        List<ServiceRequest> serviceRequestsForRefID = new ArrayList<>();
        List<ServiceRequest> allServiceRequests = getAllServiceRequests(); //we determine all service requests
        for (ServiceRequest serviceRequest : allServiceRequests) {
            if (refId.equals(serviceRequest.getReferenceID())) { //and see if we can find one with the passed reference id
                serviceRequestsForRefID.add(serviceRequest);
            }
        }

        return serviceRequestsForRefID;
    }

    @Override
    public String getName() {
        return ServiceRequestServiceImpl.class.toString();
    }
}
