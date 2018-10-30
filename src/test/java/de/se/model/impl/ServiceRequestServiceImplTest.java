package de.se.model.impl;

import de.se.data.ServiceRequest;
import de.se.model.DummyTestObjectGenerator;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.FeedbackService;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static org.junit.Assert.*;

public class ServiceRequestServiceImplTest {

    private static ServiceRequestServiceImpl serviceRequestService;
    private Logger logger = Logger.getLogger(ServiceRequestServiceImplTest.class);

    @BeforeClass
    public static void setUp() {
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        serviceRequestService = new ServiceRequestServiceImpl();
        serviceRequestService.initialize();
    }

    @Test
    public void testGetAllServiceRequests() throws Exception {
        ArrayList<ServiceRequest> serviceRequests = serviceRequestService.getAllServiceRequests();
        logger.info("Service requests size: " + serviceRequests.size());
        assertNotNull(serviceRequests);

        for (ServiceRequest serviceRequest : serviceRequests) {
            assertNotNull(serviceRequest.getName());
            assertTrue(serviceRequest.getCustomerID() != -1);
            assertTrue(serviceRequest.getServicePriorityCode() != -1);
            assertTrue(serviceRequest.getServiceRequestLifeCycleStatusCode() != -1);
            assertNotNull(serviceRequest.getRequestedEnd());
            //assertNotNull(serviceRequest.getCompletedOnDate()); //this field has been dismissed
            assertNotNull(serviceRequest.getServiceRequestDescription());
            assertNotNull(serviceRequest.getProcessingTypeCode());
            assertTrue(serviceRequest.getProcessingTypeCode().equals(ServiceRequest.PROCESSING_TYPE_CODE.SRRQ) ||
                    serviceRequest.getProcessingTypeCode().equals(ServiceRequest.PROCESSING_TYPE_CODE.ZCLN));
            assertNotNull(serviceRequest.getReferenceID());
            assertNotNull(serviceRequest.getFeedbackReferenceID());
            assertNotNull(serviceRequest.getCreationDate());
        }

        logger.info("Service requests size - end of test method: " + serviceRequests.size());
    }

    @Test
    public void testSaveAndRemoveServiceRequest() throws Exception {
        int serviceRequestsSizeBeforeSaving = serviceRequestService.getAllServiceRequests().size();

        ServiceRequest dummyServiceRequest = DummyTestObjectGenerator.getServiceRequest();
        int savingServiceRequestResponseCode = serviceRequestService.saveServiceRequest(dummyServiceRequest);

        List<ServiceRequest> serviceRequestsForRefID = serviceRequestService.getServicesForRefID("Stop1");
        ArrayList<ServiceRequest> serviceRequests = serviceRequestService.getAllServiceRequests();

        int serviceRequestsSizeAfterSaving = serviceRequests.size();

        ServiceRequest justInsertedServiceRequest = serviceRequests.get(serviceRequests.size() - 1);
        //now we delete the service request so it is deleted before an assert can fail
        logger.info("Size of service requests before deleting: " + serviceRequestsSizeAfterSaving);
        int deletingServiceRequestResponseCode = serviceRequestService.removeServiceRequest(justInsertedServiceRequest);

        assertTrue(serviceRequestsForRefID.size() > 0); //it has to contain at least the newly added service request

        FeedbackService feedbackService = ServiceRegistry.getService(FeedbackService.class);
        assertTrue(feedbackService.getAllFeedbacks().get(0).getFinished()); //check if we successfully set the feedback to true


        serviceRequests = serviceRequestService.getAllServiceRequests();
        int serviceRequestsSizeAfterDeleting = serviceRequests.size();
        logger.info("Size of service requests after deleting: " + serviceRequestsSizeAfterDeleting);

        //and now we assert
        assertEquals(serviceRequestsSizeBeforeSaving + 1, serviceRequestsSizeAfterSaving);


        assertEquals(dummyServiceRequest.getName(), justInsertedServiceRequest.getName());
        assertEquals(dummyServiceRequest.getCustomerID(), justInsertedServiceRequest.getCustomerID());
        assertEquals(dummyServiceRequest.getServicePriorityCode(), justInsertedServiceRequest.getServicePriorityCode());
        assertEquals(dummyServiceRequest.getServiceRequestLifeCycleStatusCode(), justInsertedServiceRequest.getServiceRequestLifeCycleStatusCode());

        assertEquals(dummyServiceRequest.getRequestedEnd().getTime() / 1000, //accuracy in SAP DB is up to one second
                justInsertedServiceRequest.getRequestedEnd().getTime() / 1000);
        /*assertEquals(dummyServiceRequest.getCompletedOnDate().getTime() / 1000,
                justInsertedServiceRequest.getCompletedOnDate().getTime() / 1000);*/ //this field has been dismissed

        assertEquals(dummyServiceRequest.getServiceRequestDescription(), justInsertedServiceRequest.getServiceRequestDescription());
        assertEquals(dummyServiceRequest.getProcessingTypeCode(), justInsertedServiceRequest.getProcessingTypeCode());
        assertEquals(dummyServiceRequest.getReferenceID(), justInsertedServiceRequest.getReferenceID());
        assertEquals(dummyServiceRequest.getReferenceType(), justInsertedServiceRequest.getReferenceType());
        assertEquals(dummyServiceRequest.getFeedbackReferenceID(), justInsertedServiceRequest.getFeedbackReferenceID());

        assertNotNull(justInsertedServiceRequest.getCreationDate());
        Calendar dummyCalendar = new GregorianCalendar();
        dummyCalendar.setTime(dummyServiceRequest.getCreationDate()); //granularity of CreationDate is only up to the day, not up to milliseconds
        Calendar justInsertedCalendar = new GregorianCalendar();
        justInsertedCalendar.setTime(justInsertedServiceRequest.getCreationDate());
        assertEquals(dummyCalendar.get(Calendar.DAY_OF_YEAR), justInsertedCalendar.get(Calendar.DAY_OF_YEAR));

        assertEquals(serviceRequestsSizeAfterSaving - 1, serviceRequestsSizeAfterDeleting);
        assertEquals(201, savingServiceRequestResponseCode);
        assertEquals(204, deletingServiceRequestResponseCode);

        /*for (ServiceRequest sr : serviceRequests) {
            //this piece of code tries to delete all SRs with life cycle code = 1 (should usually be commented out)
            if (sr.getServiceRequestLifeCycleStatusCode() == 1) {
                serviceRequestService.removeServiceRequest(sr);
            }
        }*/
    }

}