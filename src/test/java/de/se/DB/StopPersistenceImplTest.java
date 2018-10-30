package de.se.DB;

import de.se.DB.impl.StopPersistenceImpl;
import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class StopPersistenceImplTest {
    private StopPersistence stopPersistence = new StopPersistenceImpl();

    @Test
    public void testFetchStops() throws Exception {
        assertNotNull(stopPersistence.fetchStops());
        assertTrue(stopPersistence.fetchStops().size() > 0); //all stops for 5 lines minus 8 duplicates
    }

}