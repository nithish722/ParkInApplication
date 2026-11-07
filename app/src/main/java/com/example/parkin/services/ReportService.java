package com.example.parkin.services;

import com.example.parkin.dao.EventDAO;
import com.example.parkin.dao.ParkingAreaDAO;
import com.example.parkin.dao.ParkingSlotDAO;
import com.example.parkin.dao.ReservationDAO;
import com.example.parkin.dao.UserDAO;
import com.example.parkin.models.Event;
import com.example.parkin.models.ParkingArea;
import com.example.parkin.models.Reservation;
import com.example.parkin.models.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    private static ReportService instance;
    private final ReservationDAO reservationDAO;
    private final ParkingAreaDAO areaDAO;
    private final ParkingSlotDAO slotDAO;
    private final EventDAO eventDAO;
    private final UserDAO userDAO;

    public static class SystemStats {
        public int totalEvents;
        public int activeEvents;
        public int totalReservations;
        public int confirmedReservations;
        public int completedReservations;
        public int cancelledReservations;
        public int totalCapacity;
        public int totalAvailable;
        public int totalOccupied;
        public double overallOccupancyRate;
        public int totalUsers;
        public int totalCustomers;
        public int carReservations;
        public int bikeReservations;
        public Map<String, Integer> areaOccupancyMap = new HashMap<>();
        public Map<String, Integer> areaTotalMap = new HashMap<>();
    }

    private ReportService() {
        this.reservationDAO = new ReservationDAO();
        this.areaDAO = new ParkingAreaDAO();
        this.slotDAO = new ParkingSlotDAO();
        this.eventDAO = new EventDAO();
        this.userDAO = new UserDAO();
    }

    public static synchronized ReportService getInstance() {
        if (instance == null) {
            instance = new ReportService();
        }
        return instance;
    }

    public SystemStats getSystemStats() {
        SystemStats s = new SystemStats();

        // Events
        List<Event> events = eventDAO.findAll();
        s.totalEvents = events.size();
        s.activeEvents = (int) events.stream().filter(e -> !"CANCELLED".equalsIgnoreCase(e.getStatus())).count();

        // Areas & Capacity
        List<ParkingArea> areas = areaDAO.findAll();
        for (ParkingArea a : areas) {
            s.totalCapacity += a.getTotalCapacity();
            s.totalAvailable += a.getAvailableCapacity();
            int occupied = a.getOccupiedCount();
            s.totalOccupied += occupied;
            s.areaOccupancyMap.put(a.getAreaName(), occupied);
            s.areaTotalMap.put(a.getAreaName(), a.getTotalCapacity());
        }
        if (s.totalCapacity > 0) {
            s.overallOccupancyRate = ((double) s.totalOccupied / s.totalCapacity) * 100.0;
        }

        // Reservations
        List<Reservation> reservations = reservationDAO.findAll();
        s.totalReservations = reservations.size();
        for (Reservation r : reservations) {
            if ("CONFIRMED".equalsIgnoreCase(r.getStatus()) || "ACTIVE".equalsIgnoreCase(r.getStatus())) {
                s.confirmedReservations++;
            } else if ("COMPLETED".equalsIgnoreCase(r.getStatus())) {
                s.completedReservations++;
            } else if ("CANCELLED".equalsIgnoreCase(r.getStatus())) {
                s.cancelledReservations++;
            }

            if ("CAR".equalsIgnoreCase(r.getVehicleType())) {
                s.carReservations++;
            } else if ("BIKE".equalsIgnoreCase(r.getVehicleType())) {
                s.bikeReservations++;
            }
        }

        // Users
        List<User> users = userDAO.findAll();
        s.totalUsers = users.size();
        s.totalCustomers = (int) users.stream().filter(u -> !"ADMIN".equalsIgnoreCase(u.getRole())).count();

        return s;
    }
}