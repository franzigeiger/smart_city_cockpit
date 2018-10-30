package de.se.DB;

import de.se.data.Notification;

import java.util.List;

/**
 * Fetches and deletes notifications from as well as saves notifications to database
 */
public interface NotificationPersistence {
    /**
     * Fetches Notifications from the database
     * @return a list of all Notification objects from the database
     */
    List<Notification> fetchNotifications();

    /**
     * Saves a Notification object to the database.
     * A mapping from Notification to NotificationDB has to be performed in this method
     * because the database only works with NotificationDB objects and not Notification objects.
     * @param note the Notification object to be saved in the database
     */
    void saveNotification(Notification note);

    /**
     * Deletes a Notification object from the database.
     * @param note the notification object to delete from the database
     */
    void deleteNotification(Notification note);
}
