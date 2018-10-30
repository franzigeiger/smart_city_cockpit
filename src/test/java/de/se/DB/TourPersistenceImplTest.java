package de.se.DB;

import de.se.DB.hibernate_models.Tourdb;
import de.se.DB.impl.TourPersistenceImpl;
import de.se.data.Tour;
import de.se.model.DummyTestObjectGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;

public class TourPersistenceImplTest {
    private TourPersistence tourPersistence = new TourPersistenceImpl();
    private Tour lastInsertedTour = null;

    /**
     * After each test delete inserted Element if it wasn't (e.g. due to an error)
     */
    @After
    public void cleanDBAfter() {
        if (lastInsertedTour != null && tourPersistence.fetchTours().contains(lastInsertedTour))
            tourPersistence.deleteTour(lastInsertedTour);
        cleanDB();
    }

    /**
     * cleans the database when there is still a dummy object from failed test
     */
    @Before
    public void cleanDBBefore() {
        cleanDB();
    }

    private void cleanDB() {
        tourPersistence.deleteTour(new Tour(DummyTestObjectGenerator.getDummyTourDB()));
        //as well of test objects of the test "testDeleteOlderToursThan"
        tourPersistence.deleteTour(new Tour(DummyTestObjectGenerator.get100DaysOldTour()));
        Tourdb tourdb2 = DummyTestObjectGenerator.get100DaysOldTour();
        tourdb2.setId(10000000);
        tourPersistence.deleteTour(new Tour(tourdb2));
        Tourdb tourdb3 = DummyTestObjectGenerator.get100DaysOldTour();
        tourdb3.setId(10000001);
        tourPersistence.deleteTour(new Tour(tourdb3));
    }

    @Test
    public void testFetchTours() {
        List<Tour> tours = tourPersistence.fetchTours();
        assertNotNull(tours);
        assertTrue(tours.size() > 10);
    }

    @Test
    public void testSaveGetAndDeleteTour() {
        List<Tour> tours = tourPersistence.fetchTours();
        Tour newTour = new Tour(DummyTestObjectGenerator.getDummyTourDB());
        tourPersistence.saveTour(newTour);
        lastInsertedTour = newTour;

        Tourdb askedFourTourdb = tourPersistence.getTourById(newTour.getId());
        assertTrue(askedFourTourdb.equals(newTour.getTourDB()));
        assertTrue(toursContainsTour(tourPersistence.fetchTours(), newTour));

        tourPersistence.deleteTour(newTour);
        assertFalse(toursContainsTour(tourPersistence.fetchTours(), newTour));
        assertEquals(tourPersistence.fetchTours().size(), tours.size());
    }

    @Test
    public void testSaveMultipleTourdbs() {
        // create list with two dummy tourdbs for saving
        Tourdb tourdb1 = DummyTestObjectGenerator.getDummyTourDB();
        Tourdb tourdb2 = DummyTestObjectGenerator.getDummyTourDB();
        tourdb2.setId(tourdb1.getId() + 1);
        List<Tourdb> tourdbs = new ArrayList<>();
        tourdbs.add(tourdb1);
        tourdbs.add(tourdb2);

        // multiple tourdbs are saved
        tourPersistence.saveTourdbs(tourdbs);

        //check if first element in list was added
        Tourdb askedFourTourdb1 = tourPersistence.getTourById(tourdb1.getId());
        assertTrue(askedFourTourdb1.equals(tourdb1));
        //check if second element in list was added
        Tourdb askedFourTourdb2 = tourPersistence.getTourById(tourdb2.getId());
        assertTrue(askedFourTourdb2.equals(tourdb2));

        // delete test tourdbs again
        tourPersistence.deleteTour(new Tour(askedFourTourdb1));
        tourPersistence.deleteTour(new Tour(askedFourTourdb2));

        // check if test tourdbs
        assertNull(tourPersistence.getTourById(tourdb1.getId()));
        assertNull(tourPersistence.getTourById(tourdb2.getId()));
    }

    //test can as well due to save, get or delete method. Check "saveGetAndDeleteTour" test
    @Test
    public void testUpdateTour() {
        List<Tour> tours = tourPersistence.fetchTours();
        Tour newTour = new Tour(DummyTestObjectGenerator.getDummyTourDB());
        tourPersistence.saveTour(newTour);
        lastInsertedTour = newTour;

        Timestamp newTimestamp = new Timestamp((new Date()).getTime());
        newTour.getTourDB().setLine("jubilee");
        newTour.getTourDB().setStartTime(newTimestamp);
        newTour.getTourDB().setEndTime(newTimestamp);
        tourPersistence.updateTour(newTour);
        Tourdb askedFourTourdb = tourPersistence.getTourById(newTour.getId());
        assertNotNull(askedFourTourdb);
        assertEquals(newTour.getLine(), askedFourTourdb.getLine());
        assertEquals(newTour.getStartTime(), askedFourTourdb.getStartTime());
        assertEquals(newTour.getEndTime(), askedFourTourdb.getEndTime());

        tourPersistence.deleteTour(newTour);
    }

    /**
     * Attention: This test can fail due to external changes
     * Reason: The test is based on counting the numbers of tours.
     *         When this numbers changes during the test, it will fail.
     * Handling: Restart the test. This should not happen often
     */
    @Test
    public void testDeleteOlderToursThan() {
        // create and save three old Tour
        List<Tourdb> tourdbs = new ArrayList<>();
        Tourdb tourdb1 = DummyTestObjectGenerator.get100DaysOldTour();
        Tourdb tourdb2 = DummyTestObjectGenerator.get100DaysOldTour();
        tourdb2.setId(10000000);
        Tourdb tourdb3 = DummyTestObjectGenerator.get100DaysOldTour();
        tourdb3.setId(10000001);
        tourdbs.add(tourdb1);
        tourdbs.add(tourdb2);
        tourdbs.add(tourdb3);

        int oldSize = tourPersistence.fetchTours().size();
        tourPersistence.saveTourdbs(tourdbs);

        //old tours should be saved
        assertEquals(oldSize + 3, tourPersistence.fetchTours().size());

        int deletedTourdbs =tourPersistence.deleteOlderToursThan(99);
        assertEquals(3, deletedTourdbs);
        assertEquals(oldSize, tourPersistence.fetchTours().size());
    }

    @Test
    public void testGetNumberOfToursOnDay() {
        Tour tour1 = new Tour(DummyTestObjectGenerator.get100DaysOldTour());
        Tour tour2 = new Tour(DummyTestObjectGenerator.get100DaysOldTour());
        tour1.getTourDB().setId(10000000);

        int oldSize = tourPersistence.fetchTours().size();
        tourPersistence.saveTour(tour1);
        tourPersistence.saveTour(tour2);

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, -100);
        Timestamp timestamp100DaysOld = new Timestamp(cal.getTimeInMillis());
        int numberOfTours = tourPersistence.getNumberOfToursOnDay(timestamp100DaysOld);
        assertEquals(2, numberOfTours);

        tourPersistence.deleteTour(tour1);
        tourPersistence.deleteTour(tour2);
        int newSize = tourPersistence.fetchTours().size();
        assertEquals(oldSize, newSize); //just to be sure the have been deleted
    }

    @Test
    public void testGetMaxID() {
        Tour tour = new Tour(DummyTestObjectGenerator.get100DaysOldTour());
        tourPersistence.saveTour(tour);

        int maxID = tourPersistence.getMaxID();
        assertEquals(9999999, maxID);

        tourPersistence.deleteTour(tour);

    }

    private boolean toursContainsTour(List<Tour> tours, Tour searchTour) {
        for (Tour tour : tours) {
            if (tour.getId() == searchTour.getId())
                return true;
        }
        return false;
    }
}
