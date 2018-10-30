package de.se.DB.impl;

import de.se.DB.GeneralPersistence;
import de.se.DB.StopsInLinePersistence;
import de.se.DB.hibernate_models.Stopsinlinedb;
import org.hibernate.Session;

import java.util.List;

public class StopsInLinePersistenceImpl extends GeneralPersistence implements StopsInLinePersistence {

    public List<Stopsinlinedb> fetchStopsInLines() {
        final Session session = getNewSession();
        List<Stopsinlinedb> stopsinline = session.createQuery("FROM Stopsinlinedb").list();
        session.close();
        return stopsinline;
    }

    public List<Stopsinlinedb> getSortedStopsonLine(String lineId) {
        final Session session = getNewSession();
        List<Stopsinlinedb> stopsonline = session.createQuery("FROM Stopsinlinedb WHERE line ='" + lineId + "' ORDER BY positionStopOnLine DESC").list();
        session.close();
        return stopsonline;
    }

    @Override
    public void saveStopsInLine(Stopsinlinedb stopsInLine) {
        saveObjectToDatabase(stopsInLine);
    }
}
