package com.example.locationfinderapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database name and version
    private static final String DB_NAME = "locations.db";
    private static final int DB_VERSION = 1;

    // Constructor that calls SQLiteOpenHelper's constructor
    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Called when the database is created; creates the "location" table with required fields
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS location (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "address TEXT UNIQUE, " +
                "latitude REAL, " +
                "longitude REAL);");
    }

    // Called when database version is incremented; drops and recreates the "location" table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS location");
        onCreate(db);
    }

    /**
     * Checks if the database is empty.
     *
     * @return true if no entries exist in the "location" table, false otherwise.
     */
    public boolean isDatabaseEmpty() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            // Query to count the rows in the location table
            cursor = db.rawQuery("SELECT COUNT(*) FROM location", null);
            cursor.moveToFirst(); // Move to the first result
            return cursor.getInt(0) == 0; // Return true if count is 0
        } finally {
            if (cursor != null) {
                cursor.close(); // Ensure cursor is closed to avoid memory leaks
            }
        }
    }

    /**
     * Populates the database with JSON data from an API.
     *
     * @param jsonData JSONArray containing location information.
     */
    public void populateDatabaseWithApiData(JSONArray jsonData) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction(); // Begin database transaction for batch processing
        try {
            for (int i = 0; i < jsonData.length(); i++) {
                JSONObject obj = jsonData.getJSONObject(i);
                String address = obj.getString("display_name");
                double latitude = obj.getDouble("lat");
                double longitude = obj.getDouble("lon");

                // Prepares data to be inserted
                ContentValues values = new ContentValues();
                values.put("address", address);
                values.put("latitude", latitude);
                values.put("longitude", longitude);

                db.insert("location", null, values); // Inserts row into location table
            }
            db.setTransactionSuccessful(); // Marks transaction as successful if all inserts complete
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction(); // Ends transaction, committing if successful
        }
    }

    /**
     * Queries the database for cities that start with the specified name.
     *
     * @param cityName The partial or full name of the city to search.
     * @return Cursor with the query results.
     */
    public Cursor getCityByName(String cityName) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Perform case-insensitive search by converting city name to lowercase and using LIKE query
        return db.rawQuery("SELECT * FROM location WHERE LOWER(address) LIKE ?",
                new String[]{cityName.toLowerCase() + "%"});
    }

    /**
     * Adds a new location to the database.
     *
     * @param address The address of the location.
     * @param latitude The latitude coordinate.
     * @param longitude The longitude coordinate.
     * @return Row ID of the newly inserted location, or -1 if insertion failed.
     */
    public long addLocation(String address, double latitude, double longitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("address", address);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        return db.insert("location", null, values); // Insert data into location table
    }

    /**
     * Deletes locations that start with a specified address.
     *
     * @param address The partial or full name of the address to delete.
     * @return Number of rows deleted.
     */
    public int deleteLocation(String address) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Deletes all rows with addresses that start with the specified string
        return db.delete("location", "address LIKE ?", new String[]{address + "%"});
    }

    /**
     * Updates the latitude and longitude for a specified address.
     *
     * @param address The partial or full name of the address to update.
     * @param newLatitude The new latitude value.
     * @param newLongitude The new longitude value.
     * @return Number of rows updated.
     */
    public int updateLocation(String address, double newLatitude, double newLongitude) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("latitude", newLatitude);
        values.put("longitude", newLongitude);

        // Updates the first matching row found with the specified address
        return db.update("location", values, "address LIKE ?", new String[]{address + "%"});
    }
}
