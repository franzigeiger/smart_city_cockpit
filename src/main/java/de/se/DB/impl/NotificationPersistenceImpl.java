package de.se.DB.impl;

import de.se.DB.NotificationPersistence;
import de.se.DB.hibernate_models.Notificationdb;
import de.se.DB.GeneralPersistence;
import de.se.data.Notification;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.List;

public class NotificationPersistenceImpl extends GeneralPersistence implements NotificationPersistence {

    @Override
    public List<Notification> fetchNotifications() {
        final Session session = getNewSession();
        List<Notificationdb> notificationsDBs = session.createQuery("FROM Notificationdb").list();

        List<Notification> notifications = new ArrayList<>();
        for (Notificationdb notificationDB : notificationsDBs) {
            notifications.add(new Notification(notificationDB));
        }

        session.close();
        return notifications;
    }

    @Override
    public void saveNotification(Notification note) {
        saveObjectToDatabase(note.getDb());
    }

    @Override
    public void deleteNotification(Notification note) {
        deleteObjectFromDatabase(note.getDb());
    }
}
