package de.se.DB.mocks;

import de.se.DB.StopPersistence;
import de.se.data.Stop;

import java.util.ArrayList;
import java.util.List;

public class StopMock implements StopPersistence{
    @Override
    public List<Stop> fetchStops() {
        List<Stop> stops = new ArrayList<Stop>();
       stops.add( new Stop("one", "kö" , "123.12341236", "123.12341236" ,  null));
        stops.add( new Stop("two", "uni" , "124.12341236", "124.12341236" ,  null));
        stops.add( new Stop("three", "rathaus" , "125.12341236", "125.12341236" ,  null));
        stops.add( new Stop("four", "hbf" , "126.12341236", "126.12341236" ,  null));

        return stops;
    }

    @Override
    public void saveStop(Stop stop) {

    }
}
