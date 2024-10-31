# LocationFinderApp

Location Finder application can be used to search and manage geographic data from locations in the Greater Toronto Area. The application allows the user to add, update, delete, and query any specific location by its city name. This report describes how the Location Finder application uses databases in the storage and management of location data.

# Schema Design of the Database
Currently, the database has only one table called 'locations', which holds data on the following columns:
id: A unique identifier auto-incrementing for each record.
address: The city name and detailed address of the location.
latitude: The latitude coordinate of the location.
longitude: The longitude coordinate of the location.
The above form of structure allows the application to store and retrieve information that is crucial to each location in an organized manner.

# Overview of Database Handling
In my application, fetching, storing, and using location data from an external API to a local SQLite database are handled by three Java classes i.e., LocationFetcher class, DatabaseHelper class, and the MainActivity.

Firstly, LocationFetcher classfetches location data from an outside API. In this case, it's specifically for the cities of the GTA. It uses OkHttpCliente Library to make the HTTP requests, pulls data as JSON responses, and consolidates the results. DatabaseHelper class manages the SQLite database in the creation, updating, or maintaining of the database. It initializes a new database in databases, populates, and performs CRUD. The MainActivity class coordinates components to ensure that data flows properly from the API to the database and, consequently, to the user. It checks, in respect of this, whether the database is empty. If so, it calls for the method populateDatabaseWithApiData(). This division of classes makes the app maintainable and extendable with respect to adding new features.
