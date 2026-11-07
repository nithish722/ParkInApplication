package com.example.parkin.dao;

import com.example.parkin.database.MockDatabaseStore;
import com.example.parkin.models.ParkingArea;

import java.util.ArrayList;
import java.util.List;

public class ParkingAreaDAO {

    public List<ParkingArea> findAll() {
        return new ArrayList<>(MockDatabaseStore.getInstance().getParkingAreas().values());
    }

    public ParkingArea findById(int areaId) {
        return MockDatabaseStore.getInstance().getParkingAreas().get(areaId);
    }

    public ParkingArea findByCode(String code) {
        if (code == null) return null;
        for (ParkingArea pa : MockDatabaseStore.getInstance().getParkingAreas().values()) {
            // Checks getCode() or fallback
            String areaCode = pa.getCode();
            if (areaCode != null && areaCode.equalsIgnoreCase(code)) {
                return pa;
            }
        }
        return null;
    }

    public boolean insert(ParkingArea area) {
        if (area == null) return false;
        int newId = MockDatabaseStore.getInstance().nextAreaId();
        area.setAreaId(newId);
        MockDatabaseStore.getInstance().getParkingAreas().put(newId, area);
        return true;
    }

    public boolean update(ParkingArea area) {
        if (area == null || !MockDatabaseStore.getInstance().getParkingAreas().containsKey(area.getAreaId())) {
            return false;
        }
        MockDatabaseStore.getInstance().getParkingAreas().put(area.getAreaId(), area);
        return true;
    }

    public boolean updateAvailableCapacity(int areaId, int delta) {
        ParkingArea area = findById(areaId);
        if (area != null) {
            int currentAvailable = area.getAvailableCapacity();
            int total = area.getTotalCapacity();
            int newCap = Math.max(0, Math.min(total, currentAvailable + delta));
            area.setAvailableCapacity(newCap);
            return update(area);
        }
        return false;
    }

    public boolean delete(int areaId) {
        return MockDatabaseStore.getInstance().getParkingAreas().remove(areaId) != null;
    }
}