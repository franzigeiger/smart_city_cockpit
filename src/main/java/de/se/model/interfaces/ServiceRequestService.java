package de.se.model.interfaces;

import de.se.data.ServiceRequest;
import de.se.model.ParentService;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class to get, save and remove service requests in the SAP database
 */
public interface ServiceRequestService extends ParentService {
    /**
     * Gets ALL services for our customer ID from the SAP database
     * @return a list with all services that currently exist in the SAP database
     */
    ArrayList<ServiceRequest> getAllServiceRequests();

    /**
     * Saves the given service in the SAP database
     * @param service the service to be saved in the SAP database
     * @return the HTTP response code for saving the service request
     */
    int saveServiceRequest(ServiceRequest service);

    /**
     * Marks a service as closed in the SAP database (we are not allowed to delete from there)
     * @param service the service to be marked as closed in the SAP database
     * @return the HTTP response code for removing the service request
     */
    int removeServiceRequest(ServiceRequest service);

    /**
     * This method returns a list of services for the given reference id, either vehicle or stop
     * @param refId the reference ID, referencing a vehicle (e.g. T_001) or a stop
     */
    List<ServiceRequest> getServicesForRefID(String refId);
}
