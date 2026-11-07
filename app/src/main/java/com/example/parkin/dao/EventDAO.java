package com.example.parkin.dao;

import com.example.parkin.database.MockDatabaseStore;
import com.example.parkin.models.Event;

import java.util.ArrayList;
import java.util.List;

public class EventDAO {

    public List<Event> findAll() {
        return new ArrayList<>(MockDatabaseStore.getInstance().getEvents().values());
    }

    public List<Event> findUpcoming() {
        List<Event> list = new ArrayList<>();
        for (Event e : MockDatabaseStore.getInstance().getEvents().values()) {
            if (!"CANCELLED".equalsIgnoreCase(e.getStatus())) {
                list.add(e);
            }
        }
        return list;
    }

    public Event findById(int eventId) {
        return MockDatabaseStore.getInstance().getEvents().get(eventId);
    }

    public boolean insert(Event event) {
        if (event == null) return false;
        int newId = MockDatabaseStore.getInstance().nextEventId();
        event.setEventId(newId);
        MockDatabaseStore.getInstance().getEvents().put(newId, event);
        return true;
    }

    public boolean update(Event event) {
        if (event == null || !MockDatabaseStore.getInstance().getEvents().containsKey(event.getEventId())) {
            return false;
        }
        MockDatabaseStore.getInstance().getEvents().put(event.getEventId(), event);
        return true;
    }

    public boolean delete(int eventId) {
        return MockDatabaseStore.getInstance().getEvents().remove(eventId) != null;
    }
}