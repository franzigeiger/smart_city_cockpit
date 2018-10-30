package de.se.model.mocks;

import de.se.data.ServiceRequest;
import de.se.data.Stop;
import de.se.data.Vehicle;
import de.se.model.DummyTestObjectGenerator;
import de.se.model.interfaces.ServiceRequestService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServiceRequestServiceMock implements ServiceRequestService {

    private ArrayList<ServiceRequest> services;

    @Override
    public ArrayList<ServiceRequest> getAllServiceRequests() {
        return this.services;
    }

    @Override
    public int saveServiceRequest(ServiceRequest service) {
        return 200;
    }

    @Override
    public int removeServiceRequest(ServiceRequest service) {
        return 200;
    }

    @Override
    public List<ServiceRequest> getServicesForRefID(String refId) {
        return services;
    }

    @Override
    public String getName() {
        return ServiceRequestServiceMock.class.toString();
    }

    @Override
    public void initialize() {
        this.services = new ArrayList<>();

        Date newDate = new Date();
        newDate.setTime(newDate.getTime() + 10000);

        this.services.add(DummyTestObjectGenerator.getServiceRequest());
        this.services.add(new ServiceRequest(2, 2, new Date(),
                new Date(), "Fancy description",
                new Stop("1"), ServiceRequest.PROCESSING_TYPE_CODE.ZCLN, "1", "Vehicle", "Fancy SR name"));
        this.services.add(new ServiceRequest(3, 3, new Date(),
                new Date(), "Weird description",
                new Vehicle(DummyTestObjectGenerator.getDummyVehicledb()),
                ServiceRequest.PROCESSING_TYPE_CODE.ZCLN, "2", "Vehicle", "Weird SR name"));
    }
}
