package com.example.parkin.dao;

import com.example.parkin.database.MockDatabaseStore;
import com.example.parkin.models.Reservation;

import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    public List<Reservation> findAll() {
        List<Reservation> list = new ArrayList<>(MockDatabaseStore.getInstance().getReservations().values());
        list.sort((a, b) -> {
            if (a.getBookingTime() == null || b.getBookingTime() == null) return 0;
            return b.getBookingTime().compareTo(a.getBookingTime());
        });
        return list;
    }

    public List<Reservation> findByUserId(int userId) {
        List<Reservation> list = new ArrayList<>();
        for (Reservation r : MockDatabaseStore.getInstance().getReservations().values()) {
            if (r.getUserId() == userId) {
                list.add(r);
            }
        }
        list.sort((a, b) -> {
            if (a.getBookingTime() == null || b.getBookingTime() == null) return 0;
            return b.getBookingTime().compareTo(a.getBookingTime());
        });
        return list;
    }

    public Reservation findByBookingCode(String code) {
        if (code == null) return null;
        for (Reservation r : MockDatabaseStore.getInstance().getReservations().values()) {
            if (r.getBookingCode() != null && r.getBookingCode().equalsIgnoreCase(code)) {
                return r;
            }
        }
        return null;
    }

    public boolean insert(Reservation r) {
        if (r == null) return false;
        int newId = MockDatabaseStore.getInstance().nextReservationId();
        r.setReservationId(newId);
        MockDatabaseStore.getInstance().getReservations().put(newId, r);
        return true;
    }

    public boolean updateStatus(int reservationId, String status) {
        Reservation r = MockDatabaseStore.getInstance().getReservations().get(reservationId);
        if (r != null) {
            r.setStatus(status);
            return true;
        }
        return false;
    }

    public boolean delete(int reservationId) {
        return MockDatabaseStore.getInstance().getReservations().remove(reservationId) != null;
    }
}