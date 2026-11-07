package com.example.parkin.models;

public class ParkingArea {
    private int areaId;
    private String name;
    private String code;
    private String vehicleType;
    private int totalCapacity;
    private int availableCapacity;
    private String locationDesc;
    private double hourlyRate;
    private String nearestVenue;
    private int walkingTimeMins;
    private String status;

    public ParkingArea() {
        this.status = "OPEN";
    }

    public ParkingArea(int areaId, String name, String code, String vehicleType,
                       int totalCapacity, int availableCapacity, String locationDesc,
                       double hourlyRate, String nearestVenue, int walkingTimeMins, String status) {
        this.areaId = areaId;
        this.name = name;
        this.code = code;
        this.vehicleType = vehicleType;
        this.totalCapacity = totalCapacity;
        this.availableCapacity = availableCapacity;
        this.locationDesc = locationDesc;
        this.hourlyRate = hourlyRate;
        this.nearestVenue = nearestVenue;
        this.walkingTimeMins = walkingTimeMins;
        this.status = status;
    }

    public int getAreaId() { return areaId; }
    public void setAreaId(int areaId) { this.areaId = areaId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    // Alias for getAreaName()
    public String getAreaName() { return name != null ? name : ""; }
    public void setAreaName(String areaName) { this.name = areaName; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    // Alias for getAreaCode()
    public String getAreaCode() { return code != null ? code : ""; }
    public void setAreaCode(String areaCode) { this.code = areaCode; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public int getTotalCapacity() { return totalCapacity; }
    public void setTotalCapacity(int totalCapacity) { this.totalCapacity = totalCapacity; }

    public int getAvailableCapacity() { return availableCapacity; }
    public void setAvailableCapacity(int availableCapacity) { this.availableCapacity = availableCapacity; }

    // Alias for getAvailableSpots()
    public int getAvailableSpots() { return availableCapacity; }

    public int getOccupiedCount() { return Math.max(0, this.totalCapacity - this.availableCapacity);}
    public void setAvailableSpots(int availableSpots) { this.availableCapacity = availableSpots; }

    public String getLocationDesc() { return locationDesc; }
    public void setLocationDesc(String locationDesc) { this.locationDesc = locationDesc; }

    public double getHourlyRate() { return hourlyRate; }
    public void setHourlyRate(double hourlyRate) { this.hourlyRate = hourlyRate; }

    public String getNearestVenue() { return nearestVenue; }
    public void setNearestVenue(String nearestVenue) { this.nearestVenue = nearestVenue; }

    public int getWalkingTimeMins() { return walkingTimeMins; }
    public void setWalkingTimeMins(int walkingTimeMins) { this.walkingTimeMins = walkingTimeMins; }

    // Alias for getProximityLandmark()
    public String getProximityLandmark() {
        if (nearestVenue != null && !nearestVenue.isEmpty()) {
            return nearestVenue + " (" + walkingTimeMins + " min walk)";
        }
        return locationDesc != null ? locationDesc : "Campus Proximity";
    }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}