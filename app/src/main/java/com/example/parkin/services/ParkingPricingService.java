package com.example.parkin.services;

public class ParkingPricingService {

    /**
     * Checks if the user's email belongs to VIT domain (*.vit.ac.in or vitstudent.ac.in).
     */
    public static boolean isVitEmail(String email) {
        if (email == null) return false;
        String e = email.trim().toLowerCase();
        return e.endsWith("vit.ac.in") || e.endsWith("vitstudent.ac.in") || e.contains("vit.ac.in");
    }

    /**
     * Calculates the parking fee for a user based on email domain and duration.
     * VIT Emails (*.vit.ac.in): 100% FREE (INR 0.00)
     * Non-VIT Emails: INR 50.00 base for up to 4 hours + 12% extra per hour beyond 4 hours (INR 6.00/hr)
     */
    public static double calculateParkingFee(String email, int durationHours) {
        if (isVitEmail(email)) {
            return 0.00;
        }
        double baseFee = 50.00;
        if (durationHours <= 4) {
            return baseFee;
        }
        int extraHours = durationHours - 4;
        double overtimeFeePerHour = baseFee * 0.12; // 12% = 6.00
        return baseFee + (extraHours * overtimeFeePerHour);
    }

    public static String getTariffDescription(String email) {
        if (isVitEmail(email)) {
            return "🎉 VIT Campus Member Waiver: 100% FREE Parking Pass";
        }
        return "💳 Standard Visitor Tariff: INR 50.00 (4 Hours Base Pass) + 12% Extra (INR 6.00/hr over 4 hrs)";
    }
}