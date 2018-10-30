package de.se.DB.impl;

import de.se.DB.GeneralPersistence;
import de.se.DB.StopPersistence;
import de.se.DB.hibernate_models.Stopdb;
import de.se.data.Stop;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class StopPersistenceImpl extends GeneralPersistence implements StopPersistence {

    @Override
    public List<Stop> fetchStops() {
        final Session session = getNewSession();
        List<Stopdb> stopsDBs = session.createQuery("FROM Stopdb").list();

        List<Stop> stops = new ArrayList<>();
        for (Stopdb stopDB : stopsDBs) {
            stops.add(new Stop(stopDB));
        }

        session.close();
        return stops;
    }

    public void saveStop(Stop stop) {
        saveObjectToDatabase(stop.getStopDB());
    }
}



