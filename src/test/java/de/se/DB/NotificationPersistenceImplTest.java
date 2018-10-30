package de.se.DB;

import de.se.DB.hibernate_models.Notificationdb;
import de.se.DB.impl.NotificationPersistenceImpl;
import de.se.data.Notification;
import junit.framework.TestCase;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class NotificationPersistenceImplTest{
    NotificationPersistence persistence = new NotificationPersistenceImpl();

    @Test
    public void testFetchNotifications() throws Exception {
        assertNotNull(persistence.fetchNotifications());
    }

    @Test
    public void testSaveNotification() throws Exception {
        Notificationdb db = new Notificationdb();
        db.setDescription("Minor delay");
        db.setId(12345);
        db.setLine("bakerloo");
        db.setStop("LUHAW");
        Notification notification = new Notification(db);

        // try to delete it when exists (can exists when saving crashed before deleting
        // Otherwise saving will crash
        persistence.deleteNotification(notification);

        persistence.saveNotification(notification);
        // cleanDB
        persistence.deleteNotification(notification);
    }

}