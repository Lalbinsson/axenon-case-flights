package com.example;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Comparator;
import java.util.Collections;

import com.google.gson.Gson;
import com.mysql.cj.jdbc.result.UpdatableResultSet;

import java.sql.*;
//import FlightResponse;

public class Main {

    public static void main(String[] args) {
        // validera input?
        String airport = "ARN"; // args[0]; //fixa inputen

        // Get and format date
        LocalDate date = LocalDate.now(ZoneOffset.UTC); // '240223'
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String formattedDate = date.format(formatter);

        // Get and format DateTime
        ZonedDateTime UTC = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter UTCformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedUTC = UTC.format(UTCformatter);

        String QueryString = "";

        String subscriptionKey = "8823aefacb4340868bf25406bac9ddee";

        String db = "jdbc:mysql://localhost:3306/flights";
        String username = "root";
        String password = "password";

        String r = "";

        try {

            // Ska dessa vara i en egen try catch?

            // Encode parameters
            String encodedAirport = URLEncoder.encode(airport, "UTF-8");
            String encodedScheduledDate = URLEncoder.encode(formattedDate, "UTF-8");
            String encodedEstimatedDateTime = URLEncoder.encode(formattedUTC, "UTF-8");

            // Build the query string
            QueryString = String.format(
                    "https://api.swedavia.se/flightinfo/v2/query?filter=airport%%20eq%%20'%s'%%20and%%20scheduled%%20eq%%20'%s'%%20and%%20estimated%%20le%%20'%s'&count=20",
                    encodedAirport, encodedScheduledDate, encodedEstimatedDateTime);

        } catch (UnsupportedEncodingException e) {
            System.err.println("Error encoding parameters: " + e.getMessage());
        }

        // öppna connection och hämta arrivals
        try {

            URL url = new URL(QueryString);

            // Open connection
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            // Set request method to GET
            connection.setRequestMethod("GET");

            // Set up header feilds and authentication
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            // skriva ut response, men också meddelande om det är ett error.

            // Read the response data
            BufferedReader reader;
            if (responseCode == HttpURLConnection.HTTP_OK) {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
            }

            // Read response body

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = reader.readLine()) != null) {
                response.append(inputLine);
            }
            reader.close();

            r = response.toString();

            // System.out.println(r);

            // Close connection
            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }

        // // Parse JSON response using Gson
        // Gson gson = new Gson();
        // FlightResponse flightResponse = gson.fromJson(r, FlightResponse.class);

        // // Get list of flights from the response
        // List<Flight> flights = flightResponse.getFlights();

        // Parse JSON response into FlightResponse object
        Gson gson = new Gson();
        FlightResponse flightResponse = gson.fromJson(r, FlightResponse.class);

        // Access the list of flights
        List<Flight> flights = flightResponse.getFlights();

        // Iterate through each flight and access its details
        for (Flight flight : flights) {
            System.out.println("Flight ID: " + flight.getFlightId());
            System.out.println("Scheduled Time: " + flight.getScheduledTime());
        }

        System.out.println("before sort");

        // Print the sorted list of flights
        for (Flight flight : flights) {
            String scheduledTime = flight.getScheduledTime();
            if (scheduledTime != null) {
                System.out.println("Flight ID: " + flight.getFlightId() + ", Scheduled Time: " + scheduledTime);
            }
        }

        // Create a custom comparator for Flight objects
        Comparator<Flight> flightComparator = new Comparator<Flight>() {
            @Override
            public int compare(Flight flight1, Flight flight2) {
                // Get the scheduled time strings of each flight
                String scheduledTime1 = flight1.getScheduledTime();
                String scheduledTime2 = flight2.getScheduledTime();

                // Handle null values
                if (scheduledTime1 == null && scheduledTime2 == null) {
                    return 0;
                } else if (scheduledTime1 == null) {
                    return 1;
                } else if (scheduledTime2 == null) {
                    return -1;
                }

                // Compare the scheduled times
                return scheduledTime1.compareTo(scheduledTime2);
            }
        };

        // Sort the list of flights based on scheduled time
        Collections.sort(flights, flightComparator);

        System.out.println("after sort");

        // Print the sorted list of flights
        for (Flight flight : flights) {
            String scheduledTime = flight.getScheduledTime();
            if (scheduledTime != null) {
                System.out.println("Flight ID: " + flight.getFlightId() + ", Scheduled Time: " + scheduledTime);
            }
        }

        // // Create a combined list of arrival and departure details
        // List<String> combinedList = new ArrayList<>();
        // for (Flight flight : flights) {
        // String scheduledTime = flight.getScheduledTime();
        // if (scheduledTime != null) {
        // combinedList.add(scheduledTime);
        // }
        // }

        // // Print flight details (unsorted)
        // for (FlightTime time : combinedList) {
        // System.out.println("Scheduled Time: " + time.getScheduledUtc());
        // }

        // // Sort the combined list based on scheduledUtc values
        // Collections.sort(combinedList, new Comparator<FlightTime>() {
        // @Override
        // public int compare(FlightTime time1, FlightTime time2) {
        // String scheduledTime1 = time1 != null ? time1.getScheduledUtc() : null;
        // String scheduledTime2 = time2 != null ? time2.getScheduledUtc() : null;

        // if (scheduledTime1 == null && scheduledTime2 == null) {
        // return 0;
        // } else if (scheduledTime1 == null) {
        // return 1;
        // } else if (scheduledTime2 == null) {
        // return -1;
        // }

        // return scheduledTime1.compareTo(scheduledTime2);
        // }
        // });

        // System.out.println("Sorted:");

        // // Print sorted flight details
        // for (FlightTime time : combinedList) {
        // System.out.println("Scheduled Time: " + time.getScheduledUtc());
        // }

        // Old with flightDetail:

        // // Parse JSON data into a Java object
        // Gson gson = new Gson();
        // FlightResponse flightResponse = gson.fromJson(r,
        // FlightResponse.class);

        // List<Flight> f = flightResponse.getFlights();

        // List<FlightDetail> combinedList = new ArrayList<>();
        // for (Flight flight : f) {
        // if (flight.getArrival() != null && flight.getArrival().getArrivalTime() !=
        // null) {
        // combinedList.add(flight.getArrival());
        // }
        // if (flight.getDeparture() != null && flight.getDeparture().getDepartureTime()
        // != null) {
        // combinedList.add(flight.getDeparture());
        // }
        // }

        // for (int i = 0; i < 20; i++) {
        // FlightDetail flight = combinedList.get(i);

        // if (flight.getArrivalTime() != null) {
        // System.out.println(combinedList.get(i).getFlightID() + " : "
        // + combinedList.get(i).getArrivalTime().getScheduledUtc());
        // }

        // if (flight.getDepartureTime() != null) {
        // System.out.println(combinedList.get(i).getFlightID() + " : "
        // + combinedList.get(i).getDepartureTime().getScheduledUtc());
        // }

        // }

        // Collections.sort(combinedList, new Comparator<FlightDetail>() {
        // @Override
        // public int compare(FlightDetail flight1, FlightDetail flight2) {
        // // Compare based on scheduledUtc of arrivalTime
        // String arrivalTime1 = flight1.getArrivalTime() != null ?
        // flight1.getArrivalTime().getScheduledUtc()
        // : null;
        // String arrivalTime2 = flight2.getArrivalTime() != null ?
        // flight2.getArrivalTime().getScheduledUtc()
        // : null;

        // // Compare based on scheduledUtc of departureTime
        // String departureTime1 = flight1.getDepartureTime() != null
        // ? flight1.getDepartureTime().getScheduledUtc()
        // : null;
        // String departureTime2 = flight2.getDepartureTime() != null
        // ? flight2.getDepartureTime().getScheduledUtc()
        // : null;

        // // Handle null values
        // if (arrivalTime1 == null && arrivalTime2 == null) {
        // return 0;
        // } else if (arrivalTime1 == null) {
        // return 1;
        // } else if (arrivalTime2 == null) {
        // return -1;
        // }

        // // Compare the scheduledUtc values
        // return arrivalTime1.compareTo(arrivalTime2); // Or
        // departureTime1.compareTo(departureTime2) for
        // // departureTime
        // }
        // });

        // System.out.println("sorted");

        // for (int i = 0; i < 20; i++) {
        // FlightDetail flight = combinedList.get(i);

        // if (flight.getArrivalTime() != null) {
        // System.out.println(combinedList.get(i).getFlightID() + " : "
        // + combinedList.get(i).getArrivalTime().getScheduledUtc());
        // }

        // if (flight.getDepartureTime() != null) {
        // System.out.println(combinedList.get(i).getFlightID() + " : "
        // + combinedList.get(i).getDepartureTime().getScheduledUtc());
        // }

        // }

        // // Parse JSON data into a Java object
        // Gson gson = new Gson();
        // FlightResponse flightResponse = gson.fromJson(arrivalsResponse,
        // FlightResponse.class);

        // List<Flight> f = flightResponse.getFlights();

        // // Access fields of the Java object
        // // System.out.println("Name: " + f.size());

        // DatabaseHandler databaseHandler = new DatabaseHandler();

        // try {
        // databaseHandler.connect(db, username, password);
        // System.out.println("Connected to the database.");

        // // Skapa en table för departures.
        // // Lägga till tio senaste departures med timestamps.

        // // För Departures:
        // // Alla i response bodyn.

        // // Kolla om ID finns i databasen, annars lägg till alla med nytt flightID i
        // // table.

        // // Hämta tio senaste

        // ResultSet resultSet = databaseHandler.executeQuery("SELECT * FROM
        // departures");

        // while (resultSet.next()) {
        // String u = resultSet.getString("flightID");
        // System.out.println(u);
        // // System.out.println("flightID in db: ", u);
        // // Process the retrieved data
        // }

        // databaseHandler.disconnect();

        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        // Connection successful

        // if (responseCode == HttpURLConnection.HTTP_OK) {

        // }

    }

}

// FlightResponse.java

class FlightResponse {
    private List<Flight> flights;

    public List<Flight> getFlights() {
        return flights;
    }
}

// Flight.java
class Flight {
    private String flightId;
    private String scheduledTime;

    public String getFlightId() {
        return flightId;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }
}

// Arrival.java
class Arrival extends Flight {
    // Additional properties or methods specific to arrival

    @Override
    public String getScheduledTime() {
        return super.getScheduledTime(); // You may return a specific scheduled time for arrival
    }
}

// Departure.java
class Departure extends Flight {
    // Additional properties or methods specific to departure

    @Override
    public String getScheduledTime() {
        return super.getScheduledTime(); // You may return a specific scheduled time for departure
    }
}

// // FlightTime.java
// class FlightTime {
// private String scheduledUtc;

// public String getScheduledUtc() {
// return scheduledUtc;
// }
// }

// // public class ... i egen fil.
// class FlightResponse {
// private List<Flight> flights;

// public List<Flight> getFlights() {
// return flights;
// }

// // public void sortFlightsByArrivalTime() {
// // flights.sort(Comparator.comparing(Flight::getArrivalTime).reversed());
// // }

// }

// // Kolla på om detta kan göras med subklasser på något sätt. Att Flight är
// // superklass och Arrival och Departure ärver från den.

// class Flight {
// // private String flightId;
// // private String departureAirportEnglish;
// private FlightDetail arrival;
// private FlightDetail departure;
// // private Airline airlineOperator;

// public FlightDetail getArrival() {
// return arrival;
// }

// public FlightDetail getDeparture() {
// return departure;
// }

// // public String getFlightId() {
// // return flightId;
// // }

// // // Getter and setter for departureAirportEnglish
// // public String getDepartureAirportEnglish() {
// // return departureAirportEnglish;
// // }

// // public String getArrivalTime() {
// // return arrivalTime.getScheduledUtc();
// // }

// // // public String getAirlineOperator() {
// // return airlineOperator.getName();
// // }

// }

// class FlightDetail {

// private String flightId;
// private FlightTime arrivalTime;
// private FlightTime departureTime;

// public String getFlightID() {
// return flightId;
// }

// public FlightTime getArrivalTime() {
// return arrivalTime;
// }

// public FlightTime getDepartureTime() {
// return departureTime;
// }

// // Getters and setters
// }

// class FlightTime {
// private String scheduledUtc;

// public String getScheduledUtc() {
// return scheduledUtc;
// }

// // Getters and setters
// }

// class Arrival {
// private String scheduledUtc;

// public String getScheduledUtc() {
// return scheduledUtc;
// }

// }

// class Airline {
// private String name;

// public String getName() {
// return name;
// }
// }

// public class ... i egen fil.
class DatabaseHandler {
    private Connection connection;

    public void connect(String url, String username, String password) throws SQLException {
        connection = DriverManager.getConnection(url, username, password);
    }

    public void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        Statement statement = connection.createStatement();
        return statement.executeQuery(sql);
    }
}



// class Flight {
//     private String flightId; // This property is used to determine the subclass type

//     // Other common properties and methods
// }

// class Departure extends Flight {
//     // Departure specific properties and methods
// }

// class Arrival extends Flight {
//     // Arrival specific properties and methods
// }


// class FlightDeserializer extends StdDeserializer<Flight> {
//     public FlightDeserializer() {
//         this(null);
//     }

//     public FlightDeserializer(Class<?> vc) {
//         super(vc);
//     }

//     @Override
//     public Flight deserialize(JsonParser jp, DeserializationContext ctxt)
//             throws IOException, JsonProcessingException {
//         JsonNode node = jp.getCodec().readTree(jp);
//         if (node.has("departure")) {
//             return jp.getCodec().treeToValue(node.get("departure"), Departure.class);
//         } else if (node.has("arrival")) {
//             return jp.getCodec().treeToValue(node.get("arrival"), Arrival.class);
//         }
//         throw new IllegalArgumentException("Unknown flight type");
//     }
// }













///////////////////////////////////////////


/// Hämta dep och arr i var sin endpoint, ej query, blir alla och inte filtrerat på le 


// String r = "";
// String r2 = "";

// // // Fetch departures        
// // try {

// //     // Ska dessa vara i en egen try catch?

// //     // Encode parameters
// //     String encodedAirport = URLEncoder.encode(airport, "UTF-8");
// //     String encodedScheduledDate = URLEncoder.encode(formattedDate, "UTF-8");
// //     String encodedEstimatedDateTime = URLEncoder.encode(formattedUTC, "UTF-8");


// // // Build the query string
// // QueryString = String.format("https://api.swedavia.se/flightinfo/v2/query?filter=airport%%20eq%%20'%s'%%20and%%20scheduled%%20eq%%20'%s'%%20and%%20estimated%%20le%%20'%s'&count=20",
// //         encodedAirport, encodedScheduledDate, encodedEstimatedDateTime);

// // } catch (UnsupportedEncodingException e) {
// //     System.err.println("Error encoding parameters: " + e.getMessage());
// // }

// // // öppna connection och hämta arrivals
// // try {

// //     URL url = new URL(QueryString);

// //     // Open connection
// //     HttpURLConnection connection = (HttpURLConnection) url.openConnection();

// //     // Set request method to GET
// //     connection.setRequestMethod("GET");

// //     // Set up header feilds and authentication
// //     connection.setRequestProperty("Accept", "application/json");
// //     connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

// //     int responseCode = connection.getResponseCode();
// //     System.out.println("Response Code: " + responseCode);
// //     // skriva ut response, men också meddelande om det är ett error.

// //     // Read the response data
// //     BufferedReader reader;
// //     if (responseCode == HttpURLConnection.HTTP_OK) {
// //         reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
// //     } else {
// //         reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
// //     }

// //     // Read response body

// //     String inputLine;
// //     StringBuilder response = new StringBuilder();

// //     while ((inputLine = reader.readLine()) != null) {
// //         response.append(inputLine);
// //     }
// //     reader.close();

// //     r = response.toString();

// //     //System.out.println(r);

// //     // Close connection
// //     connection.disconnect();

// // } catch (Exception e) {
// //     e.printStackTrace();
// // }

// // // // Parse JSON response into FlightResponse object
// // Gson gson = new Gson();
// // FlightResponse flightResponse = gson.fromJson(r, FlightResponse.class);

// // // Access the list of flights
// // List<Flight> flights = flightResponse.getFlights();

// // System.out.println(flights);


// // Ny stringbuilder för varje departure.

// // Fetch arrivals       
// try {

// // Ska dessa vara i en egen try catch?

// // Encode parameters
// String encodedAirport = URLEncoder.encode(airport, "UTF-8");
// String encodedScheduledDate = URLEncoder.encode(formattedDate, "UTF-8");
// String encodedEstimatedDateTime = URLEncoder.encode(formattedUTC, "UTF-8");

// // Build the query string

// //https://api.swedavia.se/flightinfo/v2/'%s'/arrivals/'%s'
// QueryString = "https://api.swedavia.se/flightinfo/v2/ARN/arrivals/2024-03-10";

// } catch (UnsupportedEncodingException e) {
// System.err.println("Error encoding parameters: " + e.getMessage());
// }

// // öppna connection och hämta arrivals
// try {

// URL url = new URL(QueryString);

// // Open connection
// HttpURLConnection connection = (HttpURLConnection) url.openConnection();

// // Set request method to GET
// connection.setRequestMethod("GET");

// // Set up header feilds and authentication
// connection.setRequestProperty("Accept", "application/json");
// connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

// int responseCode = connection.getResponseCode();
// System.out.println("Response Code: " + responseCode);
// // skriva ut response, men också meddelande om det är ett error.

// // Read the response data
// BufferedReader reader;
// if (responseCode == HttpURLConnection.HTTP_OK) {
//     reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
// } else {
//     reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
// }

// // Read response body

// String inputLine;
// StringBuilder response = new StringBuilder();

// while ((inputLine = reader.readLine()) != null) {
//     response.append(inputLine);
// }
// reader.close();

// r2 = response.toString();

// System.out.println(r2);

// // Close connection
// connection.disconnect();

// } catch (Exception e) {
// e.printStackTrace();
// }

// // // Parse JSON response into FlightResponse object
// Gson gson2 = new Gson();
// FlightResponse flightResponse2 = gson2.fromJson(r2, FlightResponse.class);

// // Access the list of flights
// List<Flight> flights2 = flightResponse2.getFlights();

// System.out.println(flights2);

// // Ny stringbuilder för varje departure.

// // Fetch dep     
// try {

// // Ska dessa vara i en egen try catch?

// // Encode parameters
// String encodedAirport = URLEncoder.encode(airport, "UTF-8");
// String encodedScheduledDate = URLEncoder.encode(formattedDate, "UTF-8");
// String encodedEstimatedDateTime = URLEncoder.encode(formattedUTC, "UTF-8");

// // Build the query string

// //https://api.swedavia.se/flightinfo/v2/'%s'/arrivals/'%s'
// QueryString = "https://api.swedavia.se/flightinfo/v2/ARN/departures/2024-03-10";

// } catch (UnsupportedEncodingException e) {
// System.err.println("Error encoding parameters: " + e.getMessage());
// }

// // öppna connection och hämta arrivals
// try {

// URL url = new URL(QueryString);

// // Open connection
// HttpURLConnection connection = (HttpURLConnection) url.openConnection();

// // Set request method to GET
// connection.setRequestMethod("GET");

// // Set up header feilds and authentication
// connection.setRequestProperty("Accept", "application/json");
// connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

// int responseCode = connection.getResponseCode();
// System.out.println("Response Code: " + responseCode);
// // skriva ut response, men också meddelande om det är ett error.

// // Read the response data
// BufferedReader reader;
// if (responseCode == HttpURLConnection.HTTP_OK) {
// reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
// } else {
// reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
// }

// // Read response body

// String inputLine;
// StringBuilder response = new StringBuilder();

// while ((inputLine = reader.readLine()) != null) {
// response.append(inputLine);
// }
// reader.close();

// r = response.toString();

// System.out.println(r);

// // Close connection
// connection.disconnect();

// } catch (Exception e) {
// e.printStackTrace();
// }
// // // Parse JSON response into FlightResponse object
// Gson gson = new Gson();
// FlightResponse flightResponse = gson2.fromJson(r, FlightResponse.class);

// // Access the list of flights
// List<Flight> flights = flightResponse.getFlights();

// System.out.println(flights);

// // Ny stringbuilder för varje departure.


