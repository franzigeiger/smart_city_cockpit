package de.se.model.impl;

import de.se.data.Event;
import de.se.model.DummyTestObjectGenerator;
import de.se.model.ServiceRegistry;
import org.apache.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class EventServiceImplTest {

    private static EventServiceImpl eventService;
    private static Logger logger = Logger.getLogger(EventServiceImplTest.class);

    @BeforeClass
    public static void setUp() {
        ServiceRegistry.clearRegistry();
        ServiceRegistry.profile = "test";
        eventService = new EventServiceImpl();
        eventService.initialize();
    }

    @Test
    public void testGetAllEvents() throws Exception {
        List<Event> events = eventService.getAllEvents();
        logger.info("Size of events: " + events.size());
        assertNotNull(events);

        for (Event event : events) {
            assertTrue(event.getId() != -1);
            assertNotNull(event.getTitle());
            assertNotNull(event.getStartTimestamp());
            assertNotNull(event.getEndTimestamp());
            assertNotNull(event.getLocation());
            assertNotNull(event.getDescription());
            assertNotNull(event.getInvolvedParty());
        }
    }

    @Ignore
    public void testSaveAndRemoveEvent() throws Exception {
        List<Event> events = eventService.getAllEvents();
        int eventsSizeBeforeSaving = events.size();

        logger.info("Size of events: " + eventsSizeBeforeSaving);

        Event dummyEvent = DummyTestObjectGenerator.getDummyEvent();
        int savingEventResponseCode = eventService.saveEvent(dummyEvent);

        events = eventService.getAllEvents();
        int eventsSizeAfterSaving = events.size();
        assertEquals(eventsSizeBeforeSaving + 1, eventsSizeAfterSaving);

        Event justInsertedEvent = events.get(events.size() - 1);

        assertEquals(dummyEvent.getTitle(), justInsertedEvent.getTitle());
        assertEquals(dummyEvent.getLocation(), justInsertedEvent.getLocation());
        assertEquals(dummyEvent.getStartTimestamp().getTime(), justInsertedEvent.getStartTimestamp().getTime());
        assertEquals(dummyEvent.getEndTimestamp().getTime(), justInsertedEvent.getEndTimestamp().getTime());
        assertEquals(dummyEvent.getDescription(), justInsertedEvent.getDescription());
        assertEquals(dummyEvent.getInvolvedParty(), justInsertedEvent.getInvolvedParty());

        int deletingEventResponseCode = eventService.removeEvent(justInsertedEvent);

        events = eventService.getAllEvents();
        int eventsSizeAfterDeleting = events.size();

        assertEquals(eventsSizeAfterSaving - 1, eventsSizeAfterDeleting);

        logger.info("Size of events - end of test method: " + eventsSizeAfterDeleting);

        assertEquals(201, savingEventResponseCode);
        assertEquals(204, deletingEventResponseCode);
    }

}