package de.se.data;

import de.se.SAP.SAPService;

import java.util.Date;

/**
 * This class contains all data for a service request in the SAP C4C system.
 */
public class ServiceRequest {

    public enum PROCESSING_TYPE_CODE {SRRQ, ZCLN;} //SRRQ for maintenance and ZCLN for cleaning orders

    private int id;
    private String sapObjectID; //the object ID that SAP assigns to this object -> we need this for deleting stuff from the SAP database

    private String name; //the "heading" of the service request
    private int customerID; //Uni Augsburg03: "CustomerID" -> 4000562
    private int servicePriorityCode;
    private int serviceRequestLifeCycleStatusCode;
    private Date requestedEnd; //planned completion date
    private String serviceRequestDescription; //the actual content of the service request
    private PROCESSING_TYPE_CODE processingTypeCode; //SRRQ for maintenance and ZCLN for cleaning orders

    private String referenceID; //references the stop, vehicle or line this service request links to
    private String feedbackReferenceID; //references the feedbacks this service request links to (optional): a list of comma separated feedback IDs
    private Date creationDate; //this field comes from us and was not requested by MHP
    private String referenceType;

    public ServiceRequest() {
        this.id = -1;
        this.name = null;
        this.customerID = -1;
        this.servicePriorityCode = -1;
        this.serviceRequestLifeCycleStatusCode = -1;
        this.requestedEnd = null;
        this.serviceRequestDescription = "";
        this.processingTypeCode = null;
        this.referenceID = "";
        this.feedbackReferenceID = "";
        this.creationDate = null;
    }

    public ServiceRequest(int id, int servicePriorityCode, Date requestedEnd, Date completedOnDate,
                          String serviceRequestDescription,
                          RequestItem requestItem, PROCESSING_TYPE_CODE processingTypeCode, String feedbackReferenceID,
                          String referenceType, String serviceRequestName) { //referenceType = "Vehicle" or referenceType = "Stop"
        this.id = id;
        this.name = serviceRequestName;
        this.customerID = SAPService.OUR_CUSTOMER_ID;
        this.servicePriorityCode = servicePriorityCode;
        this.serviceRequestLifeCycleStatusCode = 1; //it is always 1 in the beginning
        this.requestedEnd = requestedEnd;
        this.serviceRequestDescription = serviceRequestDescription;
        this.processingTypeCode = processingTypeCode;
        this.referenceType = referenceType;
        this.referenceID = requestItem.getRequestItemID();
        this.feedbackReferenceID = feedbackReferenceID;
        this.creationDate = new Date();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getServicePriorityCode() {
        return servicePriorityCode;
    }

    public void setServicePriorityCode(int servicePriorityCode) {
        this.servicePriorityCode = servicePriorityCode;
    }

    public int getServiceRequestLifeCycleStatusCode() {
        return serviceRequestLifeCycleStatusCode;
    }

    public void setServiceRequestLifeCycleStatusCode(int serviceRequestLifeCycleStatusCode) {
        this.serviceRequestLifeCycleStatusCode = serviceRequestLifeCycleStatusCode;
    }

    public Date getRequestedEnd() {
        return requestedEnd;
    }

    public void setRequestedEnd(Date requestedEnd) {
        this.requestedEnd = requestedEnd;
    }

    public String getServiceRequestDescription() {
        return serviceRequestDescription;
    }

    public void setServiceRequestDescription(String serviceRequestDescription) {
        this.serviceRequestDescription = serviceRequestDescription;
    }

    public PROCESSING_TYPE_CODE getProcessingTypeCode() {
        return processingTypeCode;
    }

    public void setProcessingTypeCode(PROCESSING_TYPE_CODE processingTypeCode) {
        this.processingTypeCode = processingTypeCode;
    }

    public String getReferenceID() {
        return referenceID;
    }

    public void setReferenceID(String referenceID) {
        this.referenceID = referenceID;
    }

    public String getFeedbackReferenceID() {
        return feedbackReferenceID;
    }

    public void setFeedbackReferenceID(String feedbackReferenceID) {
        this.feedbackReferenceID = feedbackReferenceID;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = creationDate;
    }

    public String getSapObjectID() {
        return sapObjectID;
    }

    public void setSapObjectID(String sapObjectID) {
        this.sapObjectID = sapObjectID;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public String getLifeCycleCodeString(){
        String state="";
        switch(serviceRequestLifeCycleStatusCode){
            case 1: state="Open";
                break;
            case 2: state="In Process";
                break;
            case 3: state= "Customer Action";
                break;
            case 4: state= "Completed";
                break;
            case 5: state = "Closed";
                break;
            default:state ="Unknown";

        }

        return state;
    }
}
