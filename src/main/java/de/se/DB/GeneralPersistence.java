package de.se.DB;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import javax.transaction.Transactional;

/**
 * Contains general stuff all persistence classes have to do
 */
public class GeneralPersistence {

    protected Logger logger = Logger.getLogger(GeneralPersistence.class);

    public Session getNewSession() {
        return DBConnector.getInstance().openSession();
    }

    @Transactional
    protected void saveObjectToDatabase(Object object) {
        final Session session = DBConnector.getInstance().openSession();
        session.beginTransaction();
        try {
            session.saveOrUpdate(object);
        } catch (Exception exception) {
            this.logger.warn("The following exception occurred while saving an object to the database: " +
                    exception.getMessage());
        } finally {
            session.flush();
            session.close();
        }
    }

    @Transactional
    protected void deleteObjectFromDatabase(Object object) {
        final Session session = DBConnector.getInstance().openSession();
        session.beginTransaction();
        try {
            session.delete(object);
        } finally {
            session.flush();
            session.close();
        }
    }
}
