package de.se.TransportAPI;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class TransportAPIHandlerTest {
    private TransportAPIHandler handler = new TransportAPIHandler();

    @Test
    public void testFetchLineDBs() throws Exception {
        Assert.assertNotNull(handler.fetchLineDBs());
        Assert.assertTrue(handler.fetchLineDBs().size() > 0);
    }

    @Test
    public void testFetchStopDBs() throws Exception {
        Assert.assertNotNull(handler.fetchStopDBs());
        Assert.assertTrue(handler.fetchStopDBs().size() > 0);
    }

    //This test is obsolete. We do not use any live vehicles from the tfl API
    @Test
    public void testFetchVehicleDBs() throws Exception {
        Assert.assertNotNull(handler.fetchVehicleDBsForTubes());
    }

    //This test is obsolete. We do not use any live tours from the tfl API
    @Test
    public void testFetchTourDBs() {
        Assert.assertNotNull(handler.fetchTourDBs());
    }

    @Ignore
    public void testFetchTimeBetweenStopsDBs() {
        Assert.assertNotNull(handler.fetchTimeBetweenStopsDBs());
    }
}