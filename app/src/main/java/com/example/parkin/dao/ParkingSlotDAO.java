package com.example.parkin.dao;

import com.example.parkin.database.MockDatabaseStore;
import com.example.parkin.models.ParkingSlot;

import java.util.ArrayList;
import java.util.List;

public class ParkingSlotDAO {

    public List<ParkingSlot> findAll() {
        return new ArrayList<>(MockDatabaseStore.getInstance().getParkingSlots().values());
    }

    public List<ParkingSlot> findByArea(int areaId) {
        List<ParkingSlot> list = new ArrayList<>();
        for (ParkingSlot s : MockDatabaseStore.getInstance().getParkingSlots().values()) {
            if (s.getAreaId() == areaId) {
                list.add(s);
            }
        }
        return list;
    }

    public List<ParkingSlot> findByAreaAndType(int areaId, String vehicleType) {
        List<ParkingSlot> list = new ArrayList<>();
        for (ParkingSlot s : MockDatabaseStore.getInstance().getParkingSlots().values()) {
            if (s.getAreaId() == areaId && s.getSlotType() != null && s.getSlotType().equalsIgnoreCase(vehicleType)) {
                list.add(s);
            }
        }
        return list;
    }

    public ParkingSlot findById(int slotId) {
        return MockDatabaseStore.getInstance().getParkingSlots().get(slotId);
    }

    public boolean insert(ParkingSlot slot) {
        if (slot == null) return false;
        int newId = MockDatabaseStore.getInstance().nextSlotId();
        slot.setSlotId(newId);
        MockDatabaseStore.getInstance().getParkingSlots().put(newId, slot);
        return true;
    }

    public boolean update(ParkingSlot slot) {
        if (slot == null || !MockDatabaseStore.getInstance().getParkingSlots().containsKey(slot.getSlotId())) {
            return false;
        }
        MockDatabaseStore.getInstance().getParkingSlots().put(slot.getSlotId(), slot);
        return true;
    }

    public boolean updateStatus(int slotId, String newStatus) {
        ParkingSlot s = MockDatabaseStore.getInstance().getParkingSlots().get(slotId);
        if (s != null) {
            s.setStatus(newStatus);
            return true;
        }
        return false;
    }

    public boolean delete(int slotId) {
        return MockDatabaseStore.getInstance().getParkingSlots().remove(slotId) != null;
    }
}