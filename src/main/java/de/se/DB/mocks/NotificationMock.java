package de.se.DB.mocks;

import de.se.DB.NotificationPersistence;
import de.se.data.Line;
import de.se.data.Notification;
import de.se.data.Route;
import de.se.data.Stop;
import de.se.data.enums.VehicleEnum;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.Network;

import java.util.ArrayList;
import java.util.List;

public class NotificationMock implements NotificationPersistence {
    @Override
    public List<Notification> fetchNotifications() {
        List<Stop> stops =ServiceRegistry.getService(Network.class).getStops();
        List<Notification> notes = new ArrayList<Notification>();
        Notification n1 = new Notification(1, "Le line has some unexplainable problems", stops.get(0), null);
        //n1.lineID = "l1";
        Notification n2 = new Notification(2, "Le line has some unexplainable problems", stops.get(0), null);
        //n1.lineID = "l1";
        Notification n3 = new Notification(3,"Le stop has some unexplainable problems", null, new Line("l1", "Line 1" , VehicleEnum.Bus.toString(), new Route(new ArrayList<Stop>()), "red" ));
        //n1.lineID = "three";
        Notification n4 = new Notification(4,"Le stop has some unexplainable problems", null, new Line("l1", "Line 1" , VehicleEnum.Bus.toString(), new Route(new ArrayList<Stop>()), "red" ));
        //n1.lineID = "four";
        notes.add(n1);
        notes.add(n2);
        notes.add(n3);
        notes.add(n4);

        return notes;
    }

    @Override
    public void saveNotification(Notification note) {

    }

    @Override
    public void deleteNotification(Notification note) {

    }
}
