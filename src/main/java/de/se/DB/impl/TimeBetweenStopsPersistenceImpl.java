package de.se.DB.impl;

import de.se.DB.TimeBetweenStopsPersistence;
import de.se.DB.hibernate_models.Timebetweenstopsdb;
import de.se.DB.GeneralPersistence;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.List;

public class TimeBetweenStopsPersistenceImpl extends GeneralPersistence implements TimeBetweenStopsPersistence {
    private Logger logger = Logger.getLogger(TimeBetweenStopsPersistenceImpl.class);

    @Override
    public List<Timebetweenstopsdb> fetchTimeBetweenStops() {
        final Session session = getNewSession();
        List<Timebetweenstopsdb> Timebetweenstopsdbs = session.createQuery("FROM Timebetweenstopsdb").list();

        session.close();
        return Timebetweenstopsdbs;
    }

    public int getTimeBetweenTwoConnectedStops(String firstStopID, String secondStopID) {
        final Session session = getNewSession();
        Query query = session.createQuery("FROM Timebetweenstopsdb WHERE startStop = '" + firstStopID +
                "' and nextStop = '" + secondStopID + "' or startStop = '" + secondStopID + "' and nextstop = '" +
                firstStopID + "'");
        int time = -1;
        try {
            Timebetweenstopsdb timeBetweenStops = (Timebetweenstopsdb) query.getSingleResult();
            time = timeBetweenStops.getTimeinminutes();
        }
        catch (NoResultException e) {
            logger.warn("getTimeBetweenTwoConnectedStops did not find a the connection and returned -1");
        }
        session.close();
        return time;
    }

    @Override
    public void saveTimeBetweenStops(Timebetweenstopsdb timebetweenstopsdb) {
        saveObjectToDatabase(timebetweenstopsdb);
    }

    @Override
    public void deleteTimeBetweenStops(Timebetweenstopsdb timebetweenstopsdb) {
        deleteObjectFromDatabase(timebetweenstopsdb);
    }

    public String toString() {
        return "TimeBetweenStopsPersistenceImpl";
    }
}
