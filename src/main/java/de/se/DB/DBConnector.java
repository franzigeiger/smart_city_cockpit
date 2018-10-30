package de.se.DB;

import org.apache.log4j.Logger;
import org.hibernate.*;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import javax.persistence.metamodel.EntityType;

/**
 * Singleton that manages the connection to our de.se.DB.
 */
public class DBConnector {
    private static DBConnector instance;
    private final static Logger logger = Logger.getLogger(DBConnector.class);

    private static final SessionFactory ourSessionFactory;

    /**
     * Singleton DBConnector instance
     *
     * @return DBconnector instance
     */
    public static DBConnector getInstance() {
        if (DBConnector.instance == null) {
            DBConnector.instance = new DBConnector();
        }
        return DBConnector.instance;
    }

    private DBConnector() {
        // @Marcel: Maybe not useful to load all data on each startup
        //setupHibernate();
    }

    /**
     * Create hibernate configuration
     */
    static {
        try {
            logger.info("Open Database Connection...");
            Configuration configuration = new Configuration();
            configuration.configure();

            configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
            // 14.12.2017 @ Marcel: Set db-url from Heroku to empty string --> force Marcel-DB/Amazon on Heroku
            String dbUrl_heroku = ""; //System.getenv("JDBC_DATABASE_URL");
            if (dbUrl_heroku != null && !dbUrl_heroku.isEmpty()) {
                configuration.setProperty("hibernate.connection.url", dbUrl_heroku);
            } else {
                // AMAZON AWS CREDENTIALS
                configuration.setProperty("hibernate.connection.url", "url");
                configuration.setProperty("hibernate.connection.username", "sth");
                configuration.setProperty("hibernate.connection.password", "empty");
            }
            ourSessionFactory = configuration.buildSessionFactory();
            logger.info("Database Connection established.");
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public Session openSession() throws HibernateException {
        return ourSessionFactory.openSession();
    }

    /**
     * Setup ORM + log all parsed data (for debug)
     */
    private void setupHibernate() {
        final Session session = openSession();
        try {
            logger.info("querying all the managed entities...");
            final Metamodel metamodel = session.getSessionFactory().getMetamodel();
            for (EntityType<?> entityType : metamodel.getEntities()) {
                final String entityName = entityType.getName();
                final Query query = session.createQuery("from " + entityName);
                logger.info("executing: " + query.getQueryString());
                for (Object o : query.list()) {
                    logger.info("Query Result: " + o);
                }
            }
        } finally {
            session.close();
        }
    }
}
