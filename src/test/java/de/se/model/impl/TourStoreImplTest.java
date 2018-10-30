package de.se.model.impl;

import de.se.DB.hibernate_models.Linedb;
import de.se.DB.hibernate_models.Vehicledb;
import de.se.DB.mocks.TimeBetweenStopsPersistenceMock;
import de.se.DB.mocks.TourPersistenceMock;
import de.se.data.*;
import de.se.model.DummyTestObjectGenerator;
import de.se.model.ServiceRegistry;
import de.se.model.interfaces.Network;
import de.se.model.interfaces.TourStore;
import org.junit.*;

import java.sql.Timestamp;
import java.util.*;

import static org.junit.Assert.*;

public class TourStoreImplTest {
    static TourStore tourStore = new TourStoreImpl();
    Tour lastInsertedTour = null;

    @BeforeClass
    public static void setupClass() {
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile="test";
        TourStoreImpl tourStoreCasted = (TourStoreImpl) tourStore;
        tourStoreCasted.setPersister(new TourPersistenceMock(), new TimeBetweenStopsPersistenceMock());
        tourStore.initialize();
    }

    /**
     * After each test delete inserted Element if it wasn't (e.g. due to an error)
     */
    @After
    public void cleanDB() {
        if (lastInsertedTour != null && tourStore.getAllExistingTours().contains(lastInsertedTour))
            tourStore.removeTour(lastInsertedTour);
    }

    /**
     * cleans the database when there is still a dummy object from failed test
     */
    @Before
    public void cleanDBBefore() {
        tourStore.removeTour(new Tour(DummyTestObjectGenerator.getDummyTourDBForMock()));
    }

    @Test
    public void initialize() {
        assertNotNull(tourStore.getAllExistingTours());
        assertTrue(tourStore.getAllExistingTours().size() > 0);
        assertNotNull(tourStore.getTimebetweenstops());
        assertTrue(tourStore.getTimebetweenstops().size() > 0);
    }

    @Test
    public void testInitialize(){

    }

    @Test
    public void initializeTourTimeTest() {
        List<Tour> tours = tourStore.getAllExistingTours();
        for (Tour tour : tours) {
            if (tour.getId() == 1) { //only the tour with id 1 can have correct values
                Network network = ServiceRegistry.getService(Network.class);
                List<Stop> stops = network.getLine(tour.getLine()).getRoute().getStopSequence();
                if (stops.get(0).getId().equals(tour.getEndStop())) { //reverse list if it is the wrong way round
                    Collections.reverse(stops);
                }// order should be one, two, three, four
                
                Map<Stop, Date> stopDateMap = tour.getStopTime();
                Date startTime = tour.getStartTime();

                assertEquals(startTime.getTime(), stopDateMap.get(stops.get(0)).getTime());
                assertEquals(startTime.getTime() + 1 * 2 * 60 * 1000, stopDateMap.get(stops.get(1)).getTime());
                assertEquals(startTime.getTime() + 2 * 2 * 60 * 1000, stopDateMap.get(stops.get(2)).getTime());
                assertEquals(startTime.getTime() + 3 * 2 * 60 * 1000, stopDateMap.get(stops.get(3)).getTime());
                assertEquals(tour.getEndTime().getTime(), stopDateMap.get(stops.get(3)).getTime());
            }
        }
    }

    /**
     * Test 4 methods: saveTours, saveTour, removeTours, removeTour
     * the saveTours and removeTours differ not much and therefore do not create much more risk to fail
     */
    @Test
    public void saveAndDeleteTours() throws Exception {
        Tour newTour = new Tour(DummyTestObjectGenerator.getDummyTourDBForMock());
        List<Tour> tours = new ArrayList<>();
        tours.add(newTour);
        tourStore.saveTours(tours);
        lastInsertedTour = newTour;
        assertTrue(tourStore.getAllExistingTours().contains(newTour));

        // search it to simulate real conditions
        Tour tourToDelete = tourStore.getTourById(newTour.getId());
        tours = new ArrayList<>();
        tours.add(tourToDelete);
        tourStore.removeTours(tours);
        lastInsertedTour = null;
        assertFalse(tourStore.getAllExistingTours().contains(newTour));
    }

    @Test
    public void testUpdateTour() throws Exception {
        int size=tourStore.getAllExistingTours().size();
        tourStore.saveTour(new Tour(-1, new Timestamp(0), null, "one", "four" , "V1", "l1"));
        assertEquals(size + 1 , tourStore.getAllExistingTours().size());
    }

    @Test
    public void testSaveAndDeleteTour() throws Exception {
        Tour workTour=tourStore.getTourById(1);

        tourStore.updateTour(workTour, "V3");

        assertEquals("V3", tourStore.getTourById(1).getVehicle());

        tourStore.updateTour(workTour, "");
        assertEquals("", tourStore.getTourById(1).getVehicle());

        tourStore.updateTour(workTour, "V2");
        assertEquals("V2", tourStore.getTourById(1).getVehicle());

        try{
            tourStore.updateTour(workTour, "V4");
            Assert.fail();
        }catch(Exception e){
            assertEquals("V2", tourStore.getTourById(1).getVehicle());
        }


        tourStore.updateTour(workTour, "V3");
        assertEquals("V3", tourStore.getTourById(1).getVehicle());


    }

    @Test
    public void getToursForLine() {
        Linedb linedb = new Linedb();
        linedb.setId("l1");
        tourStore.getToursForLine(new Line(linedb));
        // initialize() inserts 784 bakerloo tours
        assertTrue(tourStore.getToursForLine(new Line(linedb)).size() >= 4);
    }

    /**
     * This Test might also Fail due to not working save or remove of Tours
     * Check saveAndDeleteTours() test
     */
    @Test
    public void getToursForVehicle() throws Exception {
        Tour newTour = new Tour(DummyTestObjectGenerator.getDummyTourDBForMock2());
        tourStore.saveTour(newTour);

        Vehicledb vehicledb = new Vehicledb();
        vehicledb.setId("V5");
        assertTrue(tourStore.getToursForVehicle(new Vehicle(vehicledb).getId()).size() >= 1);

        tourStore.removeTour(newTour);
    }

    @Test
    public void getPositionOfTourOnStop() {
        Tour tourOnStop = DummyTestObjectGenerator.getCurrentlyRunningTourOnStop();
        Position pos = tourStore.getPositionFromTour(tourOnStop);
        assertEquals("three", pos.getPrevStop());
        assertEquals("four", pos.getNextStop());
        assertTrue(pos.isOnPrev());

        pos = tourStore.getPositionFromDelayedTour(tourOnStop, 2* 60);
        assertEquals("two", pos.getPrevStop());
        assertEquals("three", pos.getNextStop());
        assertTrue(pos.isOnPrev());
    }

    @Test
    public void getPositionOfTourBewteenStop() {
        Tour tourBetweenStop = DummyTestObjectGenerator.getCurrentlyRunningTourBetweenStops();
        Position pos = tourStore.getPositionFromTour(tourBetweenStop);
        assertEquals("three", pos.getPrevStop());
        assertEquals("four", pos.getNextStop());
        assertFalse(pos.isOnPrev());

        pos = tourStore.getPositionFromDelayedTour(tourBetweenStop, 2 * 60);
        assertEquals("two", pos.getPrevStop());
        assertEquals("three", pos.getNextStop());
        assertFalse(pos.isOnPrev());
    }

    @Test
    public void getPositionOfTourOnLastStop() {
        Tour tourOnStop = DummyTestObjectGenerator.getCurrentlyRunningTourOnLastStop();
        Position pos = tourStore.getPositionFromTour(tourOnStop);
        assertEquals("ID 99", pos.getPrevStop());
        assertNull(pos.getNextStop()); // the last stop should not have a next stop
        assertTrue(pos.isOnPrev());
    }

    // Cases were there is no position
    @Test
    public void getPositionNull() {
        Tour oldTour = DummyTestObjectGenerator.getOldTour();
        Tour upcomingTour = DummyTestObjectGenerator.getUpcomingTour();
        Tour currentTourAddDelay = DummyTestObjectGenerator.getCurrentlyRunningTourOnStop();

        assertNull(tourStore.getPositionFromTour(oldTour));
        assertNull(tourStore.getPositionFromTour(upcomingTour));
        assertNull(tourStore.getPositionFromDelayedTour(currentTourAddDelay, 30 * 60));
    }

    @Test
    public void setEndtimeOfTour() {
        Tour wihtouEndTimeTour = DummyTestObjectGenerator.getTourWithoutEndTime();
        long endTime = tourStore.getEndtimeOfTour(wihtouEndTimeTour);
        wihtouEndTimeTour.setEndTime(new Timestamp(endTime));
        assertEquals(wihtouEndTimeTour.getStartTime().getTime() + 3 * 2 * 60 * 1000 ,wihtouEndTimeTour.getEndTime().getTime());
    }

    @Test
    public void testcheckForAvailableVehicles(){
        Timestamp now = new Timestamp((new Date()).getTime());
        Timestamp start= new Timestamp(now.getTime() + 3 * 2 * 60 * 10000);
        List<String> vehicles= tourStore.checkForAvailableVehicles(new Timestamp(1009999999), "l4");
        assertEquals(3, vehicles.size());
         vehicles= tourStore.checkForAvailableVehicles(now, "l2");
        assertEquals(2 ,vehicles.size());
    }

    //Ignore this test before commit
    @Ignore
    public void testIfToursAreMovingCorrectly() throws InterruptedException {
        Tour tourOnStop = DummyTestObjectGenerator.getCurrentlyRunningTourOnStop();
        Position pos = tourStore.getPositionFromDelayedTour(tourOnStop, 2* 60);
        assertEquals("two", pos.getPrevStop());
        assertEquals("three", pos.getNextStop());
        assertTrue(pos.isOnPrev());

        Thread.sleep(31 * 1000);

        //should now be between stops
        pos = tourStore.getPositionFromDelayedTour(tourOnStop, 2* 60);
        assertEquals("two", pos.getPrevStop());
        assertEquals("three", pos.getNextStop());
        assertFalse(pos.isOnPrev());

        Thread.sleep(89 * 1000);

        //should now be on next stops
        pos = tourStore.getPositionFromDelayedTour(tourOnStop, 2* 60);
        assertEquals("three", pos.getPrevStop());
        assertEquals("four", pos.getNextStop());
        assertTrue(pos.isOnPrev());
    }

    /**
     * To run this test you have to comment out all lines in the "setupClass" method
     * and instead write this line:
     *      tourStore = ServiceRegistry.getService(TourStore.class);
     * But only for this test!
     */
    // ALWAYS IGNORE THIS THIS BEFORE COMMITTING !
    //@Test
    @Ignore
    public void testKeepToursUpToDate() {
        boolean testPart1 = false; // trick to have two test in one (not two test because of the annoying setup

        TourStoreImpl tourStoreImpl = (TourStoreImpl) tourStore;
        if (testPart1) {

            // tests when the method was not called for more than "normal" NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL
            tourStoreImpl.NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST = 20;
            tourStoreImpl.NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL = 4;

            tourStoreImpl.keepToursUpToDate();

            int deleteTours = tourStoreImpl.tourPersistence.deleteOlderToursThan(10);
            assertEquals(4 * 8 * 112, deleteTours);

            // test the main part of the method which keeps the the tours updated
            tourStoreImpl.keepToursUpToDate(); // create the tours
        }
        else {
            // by setting NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST one day less, we simulate that a day pasted
            tourStoreImpl.NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_THE_PAST = 19;
            tourStoreImpl.NUMBER_OF_FILLED_DAYS_WITH_TOURS_IN_TOAL = 4;
            tourStoreImpl.keepToursUpToDate(); // update the tours

            // the tours of 20 days in the past should be deleted
            int deletedTours = tourStoreImpl.tourPersistence.deleteOlderToursThan(19);
            assertEquals(0, deletedTours);


            // but there should be new tours for 4 days (3 existed already and 1 new day)
            deletedTours = tourStoreImpl.tourPersistence.deleteOlderToursThan(15);
            assertEquals(4 * 8 * 112, deletedTours);
        }
    }


    /**
     *  This method test if vehicle tours, which are outside of timespan are not recognized.
     */
    @Test
    public void testGetAvailableToursForVehicleFreeTours2() throws Exception {

        List<Tour> tours = new ArrayList<>();
        long start = 200000000;
        long end = start + 100000;
        //tour of vehicle we check
        tours.add( new Tour(1, new Timestamp(start - 40000) , new Timestamp(start - 30000) , "one" , "four" , "V1" , "l1"));
        tours.add( new Tour(2, new Timestamp(end + 40000) , new Timestamp(end + 50000) , "one" , "four" , "V1" , "l1"));


        //tours from other vehicles which can be switched
        tours.add( new Tour(3, new Timestamp(start + 1000) , new Timestamp(start + 11000) , "one" , "four" , "V2" , "l1"));
        tours.add( new Tour(4, new Timestamp(start + 89000) , new Timestamp(start + 99000) , "one" , "four" , "V2" , "l1"));

        tourStore.saveTours(tours);

        int size = tourStore.getAssignableToursInTimeRange("V1",start , end ).size();

        tourStore.removeTours(tours);

        assertEquals(2, size);

    }

    //This method test if vehicle tours, which are in timespan are mentioned to filter other tours.
    @Test
    public void testGetAvailableToursForVehicleVehicleToursInTimespan3() throws Exception {

        List<Tour> tours = new ArrayList<>();
        long start = 2000000;
        long end = start + 100000;
        //tour of vehicle we check
        tours.add( new Tour(1, new Timestamp(start + 40000) , new Timestamp(start + 50000) , "one" , "four" , "V1" , "l1"));
        tours.add( new Tour(2, new Timestamp(start + 60000) , new Timestamp(start + 70000) , "one" , "four" , "V1" , "l1"));

        //tours from other vehicles which can be switched
        tours.add( new Tour(3, new Timestamp(start + 45000) , new Timestamp(start + 55000) , "one" , "four" , "V2" , "l1"));
        tours.add( new Tour(4, new Timestamp(start + 56000) , new Timestamp(start + 66000) , "one" , "four" , "V2" , "l1"));

        tourStore.saveTours(tours);


        int size = tourStore.getAssignableToursInTimeRange("V1",start , end ).size();

        tourStore.removeTours(tours);

        assertEquals(0, size);

    }



    /**
     * This method tests, if tours, which overlap the vehicles tours will not be returned.
     * @throws Exception
     */
    @Test
    public void testGetAvailableToursForVehicleOverlappingVehicleTours4() throws Exception {
        List<Tour> tours = new ArrayList<>();
        long start = 200000000;
        long end = start + 100000;
        //tour of vehicle we check
        tours.add( new Tour(1, new Timestamp(start + 40000) , new Timestamp(start + 50000) , "one" , "four" , "V1" , "l1"));

        tours.add( new Tour(2, new Timestamp(start + 35000) , new Timestamp(start + 45000) , "one" , "four" , "V2" , "l1"));

        tours.add( new Tour(3, new Timestamp(start + 46000) , new Timestamp(start + 56000) , "one" , "four" , "V2" , "l1"));

        tourStore.saveTours(tours);

        int size = tourStore.getAssignableToursInTimeRange("V1",start , end ).size();

        tourStore.removeTours(tours);

        assertEquals( 0, size);
    }

    /**
     * This method tests, if tours of the input vehicle, which overlap the timespan will be added to check.
     * @throws Exception
     */
    @Test
    public void testGetAvailableToursForVehicleVTOurOverlapsTimespan5() throws Exception {
        List<Tour> tours = new ArrayList<>();
        long start = 200000000;
        long end = start + 100000;
        //tour of vehicle we check
        tours.add( new Tour(1, new Timestamp(start - 5000) , new Timestamp(start + 5000) , "one" , "four" , "V1" , "l1"));

        tours.add( new Tour(2, new Timestamp(start + 4000) , new Timestamp(start + 14000) , "one" , "four" , "V2" , "l1"));

        tours.add( new Tour(3, new Timestamp(start + 3000) , new Timestamp(start + 13000) , "one" , "four" , "V3" , "l1"));

        tours.add( new Tour(4, new Timestamp(start + 3000) , new Timestamp(start + 13000) , "one" , "four" , "V4" , "l3"));
        tours.add( new Tour(5, new Timestamp(start + 3000) , new Timestamp(start + 13000) , "one" , "four" , "V5" , "l3"));

        tourStore.saveTours(tours);
        int size = tourStore.getAssignableToursInTimeRange("V1",start , end ).size();

        tourStore.removeTours(tours);

        assertEquals( 0, size);
    }

    /**
     * This method tests if tours between the vehicle tours will be returned
     * @throws Exception
     */
    @Test
    public void testGetAvailableToursForVehicleOverlappingFreeThoughVehicleTour6() throws Exception {
        List<Tour> tours = new ArrayList<>();
        long start = 200000000;
        long end = start + 100000;
        //tour of vehicle we check
        tours.add( new Tour(1, new Timestamp(start + 40000) , new Timestamp(start + 50000) , "one" , "four" , "V1" , "l1"));
        tours.add( new Tour(2, new Timestamp(start + 65000) , new Timestamp(start + 75000) , "one" , "four" , "V1" , "l1"));

        tours.add( new Tour(3, new Timestamp(start + 52000) , new Timestamp(start + 62000) , "one" , "four" , "V2" , "l1"));

        tours.add( new Tour(4, new Timestamp(start + 76000) , new Timestamp(start + 86000) , "one" , "four" , "V2" , "l1"));

        tourStore.saveTours(tours);

        int size = tourStore.getAssignableToursInTimeRange("V1",start , end ).size();

        tourStore.removeTours(tours);

        assertEquals(2, size);
    }


    /**
     * This method tests if tours, which only overlap the timespan will not be returned.
     * @throws Exception
     */
    @Test
    public void testGetAvailableToursForVehicleOverlappingTimespan7() throws Exception {
        List<Tour> tours = new ArrayList<>();
        long start = 200000000;
        long end = start + 100000;
        //tour of vehicle we check
        tours.add( new Tour(1, new Timestamp(start + 40000) , new Timestamp(start + 50000) , "one" , "four" , "V1" , "l1"));

        tours.add( new Tour(2, new Timestamp(start - 5000) , new Timestamp(start + 5000) , "one" , "four" , "V2" , "l1"));

        tours.add( new Tour(3, new Timestamp(end - 5000) , new Timestamp(end + 5000) , "one" , "four" , "V2" , "l1"));

        tourStore.saveTours(tours);

        int size = tourStore.getAssignableToursInTimeRange("V1",start , end ).size();

        tourStore.removeTours(tours);

        assertEquals(0, size);
    }

    /**
     * This method tests if tours which are in the timespan and does not overlap vehicle tours will be returned
     * @throws Exception
     */
    @Test
    public void testGetAvailableToursForVehicleInTimespan8() throws Exception {
        List<Tour> tours = new ArrayList<>();
        long start = 200000000;
        long end = start + 100000;

        //only add tours of other vehicles
        tours.add( new Tour(1, new Timestamp(start + 5000) , new Timestamp(start + 15000) , "one" , "four" , "V2" , "l1"));

        tours.add( new Tour(2, new Timestamp(end + 85000) , new Timestamp(start + 95000) , "one" , "four" , "V2" , "l1"));

        tourStore.saveTours(tours);

        int size = tourStore.getAssignableToursInTimeRange("V1",start , end ).size();

        tourStore.removeTours(tours);

        assertEquals( 2, size);
    }

    /**
     * This method tests if tours which are not in the timespan will not be returned
     * @throws Exception
     */
    @Test
    public void testGetAvailableToursForVehicleOutsideTimespan9() throws Exception {
        List<Tour> tours = new ArrayList<>();
        long start = 200000000;
        long end = start + 100000;
        //tour of vehicle we check
        tours.add( new Tour(1, new Timestamp(start + 40000) , new Timestamp(start + 50000) , "one" , "four" , "V1" , "l1"));

        tours.add( new Tour(2, new Timestamp(start - 55000) , new Timestamp(start - 45000) , "one" , "four" , "V2" , "l1"));

        tours.add( new Tour(3, new Timestamp(end + 5000) , new Timestamp(end + 15000) , "one" , "four" , "V2" , "l1"));

        tourStore.saveTours(tours);

        int size = tourStore.getAssignableToursInTimeRange("V1",start , end ).size();

        tourStore.removeTours(tours);

        assertEquals( 0, size);
    }

    @Test
    public void testVehicleIsAVailable() throws Exception {
        int all=tourStore.getAllExistingTours().size();
        Tour initial=  new Tour(-1, new Timestamp( 4000000) , new Timestamp( 5000000) , "one" , "four" , "V1" , "l1");
        tourStore.saveTour(initial);


        Tour duplicate=  new Tour(-1, new Timestamp(4000000) , new Timestamp(5000000) , "one" , "four" , "V1" , "l1");
       try {
           tourStore.saveTour(duplicate);
           Assert.fail();
       }catch (Exception e){

       }

        Tour before=  new Tour(-1, new Timestamp(2000000) , new Timestamp(3900000) , "one" , "four" , "V1" , "l1");
        Tour after=  new Tour(-1, new Timestamp(5100000) , new Timestamp(6100000) , "one" , "four" , "V1" , "l1");

        tourStore.saveTour(before);
        tourStore.saveTour(after);

        Tour newTour=  new Tour(-1, new Timestamp(5100000) , new Timestamp(6100000) , "one" , "four" , "V2" , "l1");

        tourStore.saveTour(newTour);
        int newSize= tourStore.getAllExistingTours().size();

        tourStore.removeTour(initial);
        tourStore.removeTour(duplicate);
        tourStore.removeTour(before);
        tourStore.removeTour(after);
        tourStore.removeTour(newTour);

        assertEquals(all + 4 , newSize);

    }


    @Test
    public void testGetPossibleReplacementVehicles() throws Exception {
        Timestamp now = new Timestamp((new Date()).getTime());
        Tour initial=  new Tour(66,new Timestamp(1009999999) , new Timestamp( 1099999999) , "one" , "four" , "V2" , "l1");
        Tour initial2=  new Tour(67,now , new Timestamp( now.getTime()+ 10000) , "one" , "four" , "V2" , "l1");
        tourStore.saveTour(initial);
        tourStore.saveTour(initial2);


        List<String> vehicles =tourStore.getPossibleReplacementVehicles("V2");

        tourStore.removeTour(initial);
        tourStore.removeTour(initial2);

        assertEquals(2, vehicles.size());
    }

    @Test
    public void setReplacementVehicle() throws Exception {
        Timestamp now = new Timestamp((new Date()).getTime());
        Tour initial=  new Tour(66,new Timestamp(1009999999) , new Timestamp( 1099999999) , "one" , "four" , "V2" , "l1");
        Tour initial2=  new Tour(67,now , new Timestamp( now.getTime()+ 10000) , "one" , "four" , "V2" , "l1");

        tourStore.saveTour(initial);
        tourStore.saveTour(initial2);
        int oldToursOfV1 = tourStore.getToursForVehicle("V1").size();
        int oldToursOfV2 = tourStore.getToursForVehicle("V2").size();

        tourStore.setReplacementVehicle("V2","V1");

        int newToursOfV1 = tourStore.getToursForVehicle("V1").size();
        int newToursOfV2 = tourStore.getToursForVehicle("V2").size();

        tourStore.removeTour(initial);
        tourStore.removeTour(initial2);


        assertEquals(oldToursOfV1 + 2 , newToursOfV1);
        assertEquals(oldToursOfV2 - 2, newToursOfV2);
    }

    @Test
    public void testGetToursWithoutVehicle() throws Exception {
        String oldVehicle = tourStore.getTourById(1).getVehicle();
        tourStore.updateTour( tourStore.getTourById(1), "");

        List<Tour> tours = tourStore.getToursWithoutVehicles();
        assertEquals(1, tours.size());
        assertEquals(tours.get(0).getVehicle(), "");

        tourStore.updateTour(tourStore.getTourById(1), oldVehicle);


    }

}
