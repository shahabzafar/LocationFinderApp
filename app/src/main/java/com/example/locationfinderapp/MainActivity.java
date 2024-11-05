package com.example.locationfinderapp;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    LocationFetcher fetcher;

    // Array of city names within the GTA (Greater Toronto Area)
    String[] gtaCities = {
            "Toronto", "Mississauga", "Brampton", "Markham", "Vaughan",
            "Oakville", "Richmond Hill", "Burlington", "Milton", "Oshawa",
            "Whitby", "Ajax", "Pickering", "Aurora", "Newmarket",
            "Caledon", "Georgina", "East Gwillimbury", "Halton Hills", "King City",
            "Bolton", "Woodbridge", "Maple", "Thornhill", "Unionville",
            "Scarborough", "Etobicoke", "North York", "Concord", "Nobleton",
            "Stouffville", "Clarington", "Uxbridge", "Port Perry", "Brooklin",
            "Bradford", "Keswick", "Mount Albert", "Queensville", "Innisfil",
            "Beeton", "Tottenham", "Schomberg", "Shelburne", "Orangeville",
            "Alliston", "Alton", "Nobleton", "Palgrave", "Caledon East",
            "Georgetown", "Acton", "Campbellville", "Rockwood", "Fergus",
            "Elora", "Erin", "Belfountain", "Mimico", "Long Branch",
            "Leaside", "Lawrence Park", "Rexdale", "Don Mills", "Agincourt",
            "Malvern", "Birch Cliff", "Guildwood", "High Park", "Roncesvalles",
            "The Beaches", "Leslieville", "Riverdale", "Cabbagetown", "The Annex",
            "Yorkville", "Kensington Market", "Liberty Village", "Chinatown", "Danforth",
            "Little Italy", "Little Portugal", "Bloordale Village", "Dovercourt Village", "Islington Village",
            "Morningside", "West Hill", "Rouge", "Port Union", "Cliffcrest",
            "Clairlea", "Golden Mile", "Weston", "Mount Dennis", "The Junction",
            "Swansea", "Parkdale", "Regent Park", "St. James Town", "Riverside"
    };

    // UI elements(edit text boxes that the user types in)
    private EditText addressInput;
    private EditText latitudeInput;
    private EditText longitudeInput;
    private TextView resultTextView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initializing the database helper and data fetcher
        dbHelper = new DatabaseHelper(this);
        fetcher = new LocationFetcher();

        // Finding UI components by their IDs
        addressInput = findViewById(R.id.city);
        latitudeInput = findViewById(R.id.Latitude);
        longitudeInput = findViewById(R.id.Longitude);
        resultTextView = findViewById(R.id.Result);

        // Defining the button components and setting up the click listeners for performing CRUD operations
        Button queryButton = findViewById(R.id.Query);
        Button addButton = findViewById(R.id.Add);
        Button deleteButton = findViewById(R.id.Delete);
        Button updateButton = findViewById(R.id.Update);
        Button clearButton = findViewById(R.id.ClearAll);

        queryButton.setOnClickListener(v -> queryCity(addressInput.getText().toString()));
        addButton.setOnClickListener(v -> addLocation());
        deleteButton.setOnClickListener(v -> deleteLocation());
        updateButton.setOnClickListener(v -> updateLocation());
        clearButton.setOnClickListener(v -> clearAllFields());

        // Populating the database with all the cities if the table is empty
        if (dbHelper.isDatabaseEmpty()) {
            new Thread(() -> {
                try {
                    // Fetches locations for GTA cities using API call and populates database
                    JSONArray allGtaLocations = fetcher.fetchGtaLocations(gtaCities);
                    dbHelper.populateDatabaseWithApiData(allGtaLocations);

                    // Show a success message on the main UI thread
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Database populated!", Toast.LENGTH_SHORT).show());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    /**
     * Queries the database for a city and displays its latitude and longitude.
     * @param city The name of the city to query.
     */
    private void queryCity(String city) {
        if (city.isEmpty()) {
            Toast.makeText(this, "Please enter a city name", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            Cursor cursor = dbHelper.getCityByName(city.toLowerCase());
            runOnUiThread(() -> {
                if (cursor != null && cursor.moveToFirst()) {
                    int latitudeIndex = cursor.getColumnIndex("latitude");
                    int longitudeIndex = cursor.getColumnIndex("longitude");

                    if (latitudeIndex == -1 || longitudeIndex == -1) {
                        Toast.makeText(MainActivity.this, "Columns not found!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String latitude = cursor.getString(latitudeIndex);
                    String longitude = cursor.getString(longitudeIndex);
                    resultTextView.setText("Latitude: " + latitude + ", Longitude: " + longitude);
                } else {
                    Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                }
                if (cursor != null) {
                    cursor.close();
                }
            });
        }).start();
    }

    /**
     * Adds a location to the database with inputted address, latitude, and longitude.
     */
    private void addLocation() {
        String address = addressInput.getText().toString();
        String latitudeStr = latitudeInput.getText().toString();
        String longitudeStr = longitudeInput.getText().toString();

        // Validating that none of the fields are left empty by the user
        if (address.isEmpty() || latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parses the latitude and longitude
        double latitude = Double.parseDouble(latitudeStr);
        double longitude = Double.parseDouble(longitudeStr);

        new Thread(() -> {
            long result = dbHelper.addLocation(address, latitude, longitude);
            runOnUiThread(() -> {
                if (result != -1) {
                    Toast.makeText(MainActivity.this, "Location added!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Error adding location", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Deletes a location from the database based on the address/city_name typed by the user
     */
    private void deleteLocation() {
        String address = addressInput.getText().toString();

        // Ensure the address is entered before attempting deletion
        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter an address to delete", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            int result = dbHelper.deleteLocation(address);
            runOnUiThread(() -> {
                if (result > 0) {
                    Toast.makeText(MainActivity.this, "Location deleted!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Updates the latitude and longitude of a specific address in the database.
     */
    private void updateLocation() {
        String address = addressInput.getText().toString();
        String latitudeStr = latitudeInput.getText().toString();
        String longitudeStr = longitudeInput.getText().toString();

        // Ensure all fields are filled out before updating
        if (address.isEmpty() || latitudeStr.isEmpty() || longitudeStr.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Parse the latitude and longitude inputs
        double newLatitude = Double.parseDouble(latitudeStr);
        double newLongitude = Double.parseDouble(longitudeStr);

        new Thread(() -> {
            int result = dbHelper.updateLocation(address, newLatitude, newLongitude);
            runOnUiThread(() -> {
                if (result > 0) {
                    Toast.makeText(MainActivity.this, "Location updated!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "City not found", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    /**
     * Clears all input fields and resets the result display.
     */
    private void clearAllFields() {
        addressInput.setText("");
        latitudeInput.setText("");
        longitudeInput.setText("");
        resultTextView.setText("Results will appear here");
    }
}
