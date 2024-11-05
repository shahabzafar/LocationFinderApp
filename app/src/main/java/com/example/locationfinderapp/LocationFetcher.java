package com.example.locationfinderapp;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;

public class LocationFetcher {

    // OkHttp client for handling HTTP requests
    private OkHttpClient client = new OkHttpClient();

    /**
     * Fetches location data for 100 cities in the GTA
     *
     * @param cities Array of city names to fetch location data for.
     * @return JSONArray containing location data for each city.
     */
    public JSONArray fetchGtaLocations(String[] cities) {
        JSONArray allLocations = new JSONArray();

        // Loops through each city in the provided array
        for (String city : cities) {
            // Builds the request URL, including city name and format parameters
            String url = "https://nominatim.openstreetmap.org/search?city=" + city + "&country=Canada&format=json";
            Request request = new Request.Builder().url(url).build();

            // Executes the request and handle the response
            try (Response response = client.newCall(request).execute()) {
                // Checks if response was successful and has a body
                if (response.isSuccessful() && response.body() != null) {
                    // Parses the response body to JSON array
                    JSONArray cityLocations = new JSONArray(response.body().string());

                    // Add each location in the city's array to the overall array
                    for (int i = 0; i < cityLocations.length(); i++) {
                        allLocations.put(cityLocations.getJSONObject(i));
                    }
                }
            } catch (Exception e) {
                // Logs any exceptions during the API call
                e.printStackTrace();
            }
        }
        return allLocations; // Returns compiled list of all GTA city locations
    }
}
