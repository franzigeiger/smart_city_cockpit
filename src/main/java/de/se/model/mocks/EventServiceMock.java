package de.se.model.mocks;

import de.se.data.Event;
import de.se.model.interfaces.EventService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventServiceMock implements EventService {

    private List<Event> events;

    @Override
    public List<Event> getAllEvents() {
        return this.events;
    }

    @Override
    public int saveEvent(Event event) {
        return 200;
    }

    @Override
    public int removeEvent(Event event) {
        return 200;
    }

    @Override
    public String getName() {
        return EventServiceMock.class.toString();
    }

    @Override
    public void initialize() {
        events = new ArrayList<>();

        Date oldDate = new Date();
        oldDate.setTime(oldDate.getTime() - 10000);

        Date newDate = new Date();
        newDate.setTime(oldDate.getTime() + 10000);

        events.add(new Event(1, "Random event", oldDate, new Date(), "Random location",
                "Random description", "Arsenal Football Club")); //involvedParty has to be a real party from AppointmentInvolvedParties class
        events.add(new Event(2, "Fancy event", new Date(), newDate, "Fancy location",
                "Fancy description", "Arsenal Football Club")); //involvedParty has to be a real party from AppointmentInvolvedParties class
        events.add(new Event(3, "Weird event", oldDate, new Date(), "Weird location",
                "Weird description", "Arsenal Football Club")); //involvedParty has to be a real party from AppointmentInvolvedParties class
    }
}
