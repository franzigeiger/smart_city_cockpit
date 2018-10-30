package de.se.DB.mocks;

import de.se.DB.TimeBetweenStopsPersistence;
import de.se.DB.hibernate_models.Timebetweenstopsdb;

import java.util.ArrayList;
import java.util.List;

public class TimeBetweenStopsPersistenceMock implements TimeBetweenStopsPersistence {
    @Override
    public List<Timebetweenstopsdb> fetchTimeBetweenStops() {
        List<Timebetweenstopsdb> stops = new ArrayList<Timebetweenstopsdb>();

       Timebetweenstopsdb wa= new Timebetweenstopsdb();
       wa.setStartstop("one");
       wa.setNextstop("two");
       wa.setTimeinminutes(2);


        Timebetweenstopsdb wa2= new Timebetweenstopsdb();
        wa2.setStartstop("two");
        wa2.setNextstop("three");
        wa2.setTimeinminutes(2);

        Timebetweenstopsdb wa3= new Timebetweenstopsdb();
        wa3.setStartstop("three");
        wa3.setNextstop("four");
        wa3.setTimeinminutes(2);

        stops.add(wa);
        stops.add(wa2);
        stops.add(wa3);

        // connect all 100 next stops which are in the NetworkMock
        Timebetweenstopsdb wa4= new Timebetweenstopsdb();
        wa4.setStartstop("four");
        wa4.setNextstop("ID 0");
        wa4.setTimeinminutes(2);
        stops.add(wa4);

        Timebetweenstopsdb waX;
        for (int i = 1; i < 100; i++) {
            waX= new Timebetweenstopsdb();
            waX.setStartstop("ID " + (i - 1));
            waX.setNextstop("ID " + i);
            waX.setTimeinminutes(2);
            stops.add(waX);
        }

        return stops;
    }

    @Override
    public int getTimeBetweenTwoConnectedStops(String firstStopID, String secondStopID) {
        return 2;
    }

    @Override
    public void saveTimeBetweenStops(Timebetweenstopsdb timebetweenstopsdb) {

    }

    @Override
    public void deleteTimeBetweenStops(Timebetweenstopsdb timebetweenstopsdb) {

    }
}
