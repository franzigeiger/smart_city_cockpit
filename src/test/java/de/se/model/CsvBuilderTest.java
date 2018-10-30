package de.se.model;

import de.se.data.Line;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.Network;
import junit.framework.TestCase;

public class CsvBuilderTest extends TestCase {

    public void testBuildCSV() throws Exception {
        ServiceRegistry.profile = "test";
        CsvBuilder csvBuilder = new CsvBuilder();
        String csv = csvBuilder.buildCSV();

        FleetService fleetService = ServiceRegistry.getService(FleetService.class);
        Network network = ServiceRegistry.getService(Network.class);
        int vehicles = fleetService.getAllVehicles().size();
        int notifications = 0;
        for (Line line : network.getLines()) {
            notifications += network.getNotifications(line.getId()).size();
        }

        assertEquals(vehicles + notifications + 4 + 3, csv.split("\n").length); //+ 4 because of 2 lines header (twice) and + 3 because of three "\n" between vehicles and notifications
    }

}