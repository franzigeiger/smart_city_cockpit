package de.se.DB.impl;

import de.se.DB.GeneralPersistence;
import de.se.DB.TourPersistence;
import de.se.DB.hibernate_models.Tourdb;
import de.se.data.Tour;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TourPersistenceImpl extends GeneralPersistence implements TourPersistence {
    @Override
    public List<Tour> fetchTours() {
        final Session session = getNewSession();
        List<Tourdb> toursDBs = session.createQuery("FROM Tourdb").list();

        List<Tour> tours = new ArrayList<>();
        for (Tourdb tourDB : toursDBs) {
            tours.add(new Tour(tourDB));
        }

        session.close();
        return tours;
    }

    @Override
    public void saveTour(Tour tour) {
        if (tour.getId() == -1) {
            tour.getTourDB().setId(getMaxID() + 1);
        }
        saveObjectToDatabase(tour.getTourDB());
    }

    @Override
    public void saveTourdbs(List<Tourdb> tourdbs) {
        final Session session = getNewSession();
        session.beginTransaction();
        try {
            for (Tourdb tourdb : tourdbs) {
                session.save(tourdb);
            }
        } catch (Exception exception) {
            this.logger.warn("The following exception occurred while saving multiple tourdb objects to the database: " +
                    exception.getMessage());
        } finally {
            session.flush();
            session.close();
        }
    }

    @Override
    public void deleteTour(Tour tour) {
        deleteObjectFromDatabase(tour.getTourDB());
    }

    @Override
    public void updateTour(Tour tour) {
        final Session session = getNewSession();

        Transaction tx = session.beginTransaction();
        session.update(tour.getTourDB());
        tx.commit();

        session.close();
    }

    @Override
    public Tourdb getTourById(int id) {
        final Session session = getNewSession();
        List<Tourdb> toursDBs = session.createQuery("FROM Tourdb where id =" + id).list();
        session.close();

        Tourdb returnTourdb = null;
        if (toursDBs.size() != 0) {
            returnTourdb = toursDBs.get(0);
        }

        return returnTourdb;
    }

    @Override
    public int getMaxID() {
        final Session session = getNewSession();
        List<Tourdb> toursDBs = session.createQuery("from Tourdb where id=(select max(id) from Tourdb)").list();
        session.close();
        if (toursDBs.size() == 0)
            return 0;
        return toursDBs.get(0).getId();
    }

    @Override
    public int getNumberOfToursOnDay(Timestamp day) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(day.getTime());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 00);
        Timestamp beginningOfTheDay = new Timestamp(cal.getTimeInMillis());
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        Timestamp endOfTheDay = new Timestamp(cal.getTimeInMillis());

        final Session session = getNewSession();
        Query hql = session.createQuery("select count(*) from Tourdb " +
                "where startTime > :beginningOfTheDay and endTime < :endOfTheDay");
        hql.setParameter("beginningOfTheDay", beginningOfTheDay);
        hql.setParameter("endOfTheDay", endOfTheDay);
        int numberOfTours = ((Long) hql.uniqueResult()).intValue();
        session.close();

        return numberOfTours;
    }

    private List<Tourdb> getTourdbsOlderThan(int days) {
        final Session session = getNewSession();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -days);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 00);
        Timestamp giventime = new Timestamp(cal.getTimeInMillis());
        String hql = "from Tourdb where startTime <  :giventime";
        Query hibernateQuery = session.createQuery(hql);
        hibernateQuery.setParameter("giventime", giventime);
        List<Tourdb> toursDBs = hibernateQuery.list();

        List<Tourdb> tourdbs = new ArrayList<>();
        for (Tourdb tourDB : toursDBs) {
            tourdbs.add(tourDB);
        }

        session.close();
        return tourdbs;
    }

    @Override
    public int deleteOlderToursThan(int days) {
        List<Tourdb> tourdbs = getTourdbsOlderThan(days);
        if (tourdbs.size() == 0) {
            return 0;
        }
        final Session session = getNewSession();
        session.beginTransaction();
        try {
            for (Tourdb tourdb : tourdbs) {
                session.delete(tourdb);
            }
        } catch (Exception exception) {
            this.logger.warn("The following exception occurred while deleting multiple tourdb objects to the database: " +
                    exception.getMessage());
        } finally {
            session.flush();
            session.close();
        }
        return tourdbs.size();
    }
}
