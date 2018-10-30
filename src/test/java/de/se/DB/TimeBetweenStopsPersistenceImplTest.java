package de.se.DB;

import de.se.DB.hibernate_models.Timebetweenstopsdb;
import de.se.DB.impl.TimeBetweenStopsPersistenceImpl;
import de.se.model.DummyTestObjectGenerator;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNull;

public class TimeBetweenStopsPersistenceImplTest {
    private TimeBetweenStopsPersistence timePersistence = new TimeBetweenStopsPersistenceImpl();

    @Test
    public void fetchTours() {
        List<Timebetweenstopsdb> timebetweenstopsdbList = timePersistence.fetchTimeBetweenStops();
        assertNotNull(timebetweenstopsdbList);
        assertTrue(timebetweenstopsdbList.size() >= 94); // 94 are times between tube stations
    }

    @Test
    public void saveAndDeleteTimeBetweenStops() {
        Timebetweenstopsdb timebetweenstopsdb = DummyTestObjectGenerator.getDummyTimebetweenstopsdb();

        int sizeBeforeInsert = timePersistence.fetchTimeBetweenStops().size();
        timePersistence.saveTimeBetweenStops(timebetweenstopsdb);

        int sizeAfterInsert = timePersistence.fetchTimeBetweenStops().size();
        assertEquals(sizeAfterInsert - 1, sizeBeforeInsert);

        timePersistence.deleteTimeBetweenStops(timebetweenstopsdb);
        int sizeAfterDeleting = timePersistence.fetchTimeBetweenStops().size();

        assertEquals( sizeAfterInsert -1, sizeAfterDeleting);
    }

    @Test
    public void getSpecificTime () {
        int timeInMinutes = timePersistence.getTimeBetweenTwoConnectedStops("LUHAW", "LUKEN");
        assertEquals(timeInMinutes, 2);

        assertEquals(timePersistence.getTimeBetweenTwoConnectedStops("noStop", "noStop"), -1);
    }
}
