package de.se.model;

import de.se.model.impl.*;
import de.se.model.interfaces.*;
import de.se.model.mocks.*;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

public class ServiceRegistry {

    Logger logger = Logger.getLogger(ServiceRegistry.class);

    private static ServiceRegistry instance;

    //to test methods, which use the service registry, set in before class profile test
    public static String profile = "run";

    Map<Class, ParentService> registry;

    public void initialize(){
        registry = new LinkedHashMap<Class, ParentService>();

        if(profile.equals("run")){
            logger.info("Initialize ServiceRegistry with profile run!");

            registry.put(Network.class,  new NetworkImpl());
            registry.put(FleetService.class, new FleetServiceImpl());
            registry.put(LiveEngine.class, new LiveEngineImpl());
            registry.put(TourStore.class , new TourStoreImpl());
            registry.put(FeedbackService.class, new FeedbackServiceImpl());
            registry.put(EventService.class, new EventServiceImpl());
            registry.put(ServiceRequestService.class, new ServiceRequestServiceImpl());
        }


        //this is for test issues!
        if (profile.equals("test")){
            logger.info("Initialize ServiceRegistry with profile test!");

            registry.put(Network.class,  new NetworkMock());
            registry.put(FleetService.class, new FleetMock());
            registry.put(TourStore.class , new TourStoreMock());
            registry.put(LiveEngine.class, new LiveEngineMock());
            registry.put(FeedbackService.class, new FeedbackServiceMock());
            registry.put(EventService.class, new EventServiceMock());
            registry.put(ServiceRequestService.class, new ServiceRequestServiceMock());
        }

        for(ParentService service : registry.values()){

            service.initialize();
            logger.info("Service " + service.getName() + " is initialized!");
        }

        logger.info("ServiceRegistry is ready!");
    }

    private void reinitializeLiveEngine() {
        if (profile.equals("run")) {
            registry.put(LiveEngine.class, new LiveEngineImpl());
        } else if (profile.equals("test")) {
            registry.put(LiveEngine.class, new LiveEngineMock());
        }

        registry.get(LiveEngine.class).initialize();

        logger.info("Successfully reinitialized live engine in ServiceRegistry");
    }

    public static void restartLiveEngine() {
        if (instance == null) {
            instance = new ServiceRegistry();
            instance.initialize();
        }
        instance.reinitializeLiveEngine();
    }


    public static <T extends ParentService> T getService(Class<T> classToGet ){

        if(instance == null ){
            instance = new ServiceRegistry();
            instance.initialize();
        }

        return (T) instance.getRegistry().get(classToGet);
    }

    public Map<Class, ParentService> getRegistry() {
        return registry;
    }

    public static void clearRegistry(){
        instance = null;
    }
}
