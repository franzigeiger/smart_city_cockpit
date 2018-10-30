package de.se.model;

import de.se.data.Line;
import de.se.data.Notification;
import de.se.data.Vehicle;
import de.se.model.interfaces.FleetService;
import de.se.model.interfaces.Network;
import org.apache.log4j.Logger;

import java.util.List;

/**
 * This class is responsible for building a CSV string containing a list of all vehicles and notifications in the database
 */
public class CsvBuilder {

    private static Logger logger = Logger.getLogger(CsvBuilder.class);

    /**
     * Builds a CSV string from all vehicles and notifications in the database
     * @return a comma separated string containing a list of all vehicles and notifications in the database
     */
    public String buildCSV() { //vehicles and notifications
        StringBuilder buffer = new StringBuilder();
        buffer = addVehicles(buffer);
        buffer.append("\n\n\n"); //make some space between vehicles and notifications
        buffer = addNotifications(buffer);

        logger.info("Successfully built CSV file containing vehicles and notifications ...");
        return buffer.toString();
    }

    /**
     * Adds the list of all vehicles in the database to the given StringBuilder
     * @param buffer the StringBuilder to which we want to add the list of vehicles
     * @return the original StringBuilder, enriched with a list of vehicles
     */
    private StringBuilder addVehicles(StringBuilder buffer) {
        FleetService fleetService = ServiceRegistry.getService(FleetService.class);

        buffer.append("Vehicles:\n").append("id,type,deleted\n");

        List<Vehicle> vehicles = fleetService.getAllVehicles();
        for (Vehicle vehicle : vehicles) { //we build a CSV string here
            buffer.append(vehicle.getId()).append(",");
            buffer.append(vehicle.getType()).append(",");
            buffer.append(vehicle.isDeleted()).append("\n");
        }

        return buffer;
    }

    /**
     * Adds the list of all notifications in the database to the given StringBuilder
     * @param buffer the StringBuilder to which we want to add the list of notifications
     * @return the original StringBuilder, enriched with a list of notifications
     */
    private StringBuilder addNotifications(StringBuilder buffer) {
        Network network = ServiceRegistry.getService(Network.class);

        buffer.append("Notifications:\n").append("id,line,stop,description\n");

        for (Line line : network.getLines()) {
            List<Notification> notifications = network.getNotifications(line.getId());
            for (Notification notification : notifications) {
                buffer.append(notification.getID()).append(",");
                buffer.append(notification.getTargetLineID() == null ? "" : notification.getTargetLineID()).append(",");
                buffer.append(notification.getTargetStopID() == null ? "" : notification.getTargetStopID()).append(",");
                buffer.append(notification.getDescription()).append("\n");
            }
        }

        return buffer;
    }
}
