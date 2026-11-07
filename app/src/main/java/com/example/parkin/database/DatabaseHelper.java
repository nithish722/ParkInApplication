package com.example.parkin.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.parkin.models.ParkingArea;
import com.example.parkin.models.ParkingSlot;
import com.example.parkin.models.Reservation;
import com.example.parkin.models.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "crisscross_local.db";
    private static final int DATABASE_VERSION = 1;
    private static DatabaseHelper instance;

    // Table Names
    public static final String TABLE_USERS = "users";
    public static final String TABLE_PARKING_AREAS = "parking_areas";
    public static final String TABLE_PARKING_SLOTS = "parking_slots";
    public static final String TABLE_RESERVATIONS = "reservations";

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // 1. Create Users Table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "user_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "full_name TEXT, " +
                "email TEXT UNIQUE, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "phone TEXT, " +
                "vehicle_plate TEXT, " +
                "role TEXT, " +
                "status TEXT)");

        // 2. Create Parking Areas Table
        db.execSQL("CREATE TABLE " + TABLE_PARKING_AREAS + " (" +
                "area_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "area_name TEXT, " +
                "area_code TEXT, " +
                "vehicle_type TEXT, " +
                "total_capacity INTEGER, " +
                "available_spots INTEGER, " +
                "proximity_landmark TEXT, " +
                "status TEXT)");

        // 3. Create Parking Slots Table
        db.execSQL("CREATE TABLE " + TABLE_PARKING_SLOTS + " (" +
                "slot_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "area_id INTEGER, " +
                "area_name TEXT, " +
                "slot_number TEXT, " +
                "vehicle_type TEXT, " +
                "floor_level TEXT, " +
                "status TEXT)");

        // 4. Create Reservations Table
        db.execSQL("CREATE TABLE " + TABLE_RESERVATIONS + " (" +
                "reservation_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "booking_code TEXT, " +
                "user_id INTEGER, " +
                "user_name TEXT, " +
                "event_title TEXT, " +
                "area_name TEXT, " +
                "slot_number TEXT, " +
                "vehicle_plate TEXT, " +
                "status TEXT)");

        // Seed initial essential data
        seedInitialRecords(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_AREAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARKING_SLOTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RESERVATIONS);
        onCreate(db);
    }

    private void seedInitialRecords(SQLiteDatabase db) {
        // Pre-populate Default Admin
        ContentValues admin = new ContentValues();
        admin.put("full_name", "System Administrator");
        admin.put("email", "admin@crisscross.com");
        admin.put("username", "admin");
        admin.put("password", "admin123");
        admin.put("phone", "+91 9876543210");
        admin.put("vehicle_plate", "TN-01-AD-9999");
        admin.put("role", "ADMIN");
        admin.put("status", "ACTIVE");
        db.insert(TABLE_USERS, null, admin);

        // Pre-populate Default Customer Alex
        ContentValues customer = new ContentValues();
        customer.put("full_name", "Alex Reynolds");
        customer.put("email", "alex@campus.edu");
        customer.put("username", "alex");
        customer.put("password", "alex123");
        customer.put("phone", "+91 9845012345");
        customer.put("vehicle_plate", "TN-22-CR-1024");
        customer.put("role", "CUSTOMER");
        customer.put("status", "ACTIVE");
        db.insert(TABLE_USERS, null, customer);

        // Seed Areas
        insertArea(db, "SJT PARKING", "SJT", 30, 24, "SJT Auditorium (1 min walk)");
        insertArea(db, "CDMM PARKING", "CDMM", 30, 26, "CDMM Hall (2 min walk)");
        insertArea(db, "WOODY'S PARKING", "WOODYS", 30, 27, "Woody's Grounds (1 min walk)");

        // Seed SJT Slots
        for (int i = 1; i <= 15; i++) {
            String num = String.format("SJT-C%02d", i);
            String status = (i == 3) ? "OCCUPIED" : (i == 6) ? "RESERVED" : "AVAILABLE";
            insertSlot(db, 1, "SJT PARKING", num, "CAR", "Ground Level", status);
        }
        for (int i = 1; i <= 15; i++) {
            String num = String.format("SJT-B%02d", i);
            String status = (i == 4) ? "OCCUPIED" : "AVAILABLE";
            insertSlot(db, 1, "SJT PARKING", num, "BIKE", "North Bay", status);
        }
    }

    private void insertArea(SQLiteDatabase db, String name, String code, int cap, int avail, String landmark) {
        ContentValues cv = new ContentValues();
        cv.put("area_name", name);
        cv.put("area_code", code);
        cv.put("vehicle_type", "BOTH");
        cv.put("total_capacity", cap);
        cv.put("available_spots", avail);
        cv.put("proximity_landmark", landmark);
        cv.put("status", "OPEN");
        db.insert(TABLE_PARKING_AREAS, null, cv);
    }

    private void insertSlot(SQLiteDatabase db, int areaId, String areaName, String slotNum, String type, String level, String status) {
        ContentValues cv = new ContentValues();
        cv.put("area_id", areaId);
        cv.put("area_name", areaName);
        cv.put("slot_number", slotNum);
        cv.put("vehicle_type", type);
        cv.put("floor_level", level);
        cv.put("status", status);
        db.insert(TABLE_PARKING_SLOTS, null, cv);
    }

    // ==========================================
    // USER CRUD METHODS (Persistent Storage)
    // ==========================================

    /**
     * Inserts a newly registered user into the local database
     */
    public boolean registerUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if username or email already exists
        Cursor cursor = db.rawQuery("SELECT user_id FROM " + TABLE_USERS + " WHERE username = ? OR email = ?",
                new String[]{user.getUsername(), user.getEmail()});
        if (cursor.getCount() > 0) {
            cursor.close();
            return false; // User already exists
        }
        cursor.close();

        ContentValues cv = new ContentValues();
        cv.put("full_name", user.getFullName());
        cv.put("email", user.getEmail());
        cv.put("username", user.getUsername());
        cv.put("password", user.getPassword());
        cv.put("phone", user.getPhone());
        cv.put("vehicle_plate", user.getVehiclePlate());
        cv.put("role", user.getRole() != null ? user.getRole() : "CUSTOMER");
        cv.put("status", "ACTIVE");

        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    /**
     * Validates credentials and returns the persistent User object
     */
    public User authenticateUser(String usernameOrEmail, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE (username = ? OR email = ?) AND password = ?",
                new String[]{usernameOrEmail, usernameOrEmail, password});

        User user = null;
        if (cursor.moveToFirst()) {
            user = new User(
                    cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                    cursor.getString(cursor.getColumnIndexOrThrow("email")),
                    cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    cursor.getString(cursor.getColumnIndexOrThrow("password")),
                    cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                    cursor.getString(cursor.getColumnIndexOrThrow("vehicle_plate")),
                    cursor.getString(cursor.getColumnIndexOrThrow("role")),
                    cursor.getString(cursor.getColumnIndexOrThrow("status"))
            );
        }
        cursor.close();
        return user;
    }

    /**
     * Retrieves all persistent users (for the Admin Users panel)
     */
    public List<User> getAllUsers() {
        List<User> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " ORDER BY user_id DESC", null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("full_name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("email")),
                        cursor.getString(cursor.getColumnIndexOrThrow("username")),
                        cursor.getString(cursor.getColumnIndexOrThrow("password")),
                        cursor.getString(cursor.getColumnIndexOrThrow("phone")),
                        cursor.getString(cursor.getColumnIndexOrThrow("vehicle_plate")),
                        cursor.getString(cursor.getColumnIndexOrThrow("role")),
                        cursor.getString(cursor.getColumnIndexOrThrow("status"))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /**
     * Updates an existing user's profile details
     */
    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("full_name", user.getFullName());
        cv.put("email", user.getEmail());
        cv.put("phone", user.getPhone());
        cv.put("vehicle_plate", user.getVehiclePlate());
        cv.put("status", user.getStatus());

        int rows = db.update(TABLE_USERS, cv, "user_id = ?", new String[]{String.valueOf(user.getUserId())});
        return rows > 0;
    }
}