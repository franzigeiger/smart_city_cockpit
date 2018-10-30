package de.se.main;

import de.se.model.ServiceRegistry;
import de.se.model.interfaces.LiveEngine;
import de.se.model.interfaces.Network;
import org.apache.log4j.Logger;

public class AppInitialization {

    static Logger logger = Logger.getLogger(AppInitialization.class);

    public static void start() {
        long currentTIme = System.currentTimeMillis();

        ServiceRegistry.getService(Network.class).getLines();
        logger.info("Initalized Networkclass: " + ServiceRegistry.getService(Network.class).getClass().getName() );

        long duration = System.currentTimeMillis() - currentTIme;

        logger.info("Startup of app took:" + duration + " Milliseconds");

        logger.info("Start Live Engine...");
        ServiceRegistry.getService(LiveEngine.class).start();

    }


}
