package com.example;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.sql.*;

// Compile:
//(in java folder): javac -cp /home/lucy/.m2/repository/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar:/home/lucy/.m2/repository/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar com/example/*.java

// Run
//(in java folder): java -cp /home/lucy/.m2/repository/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar:/home/lucy/.m2/repository/com/mysql/mysql-connector-j/8.0.33/mysql-connector-j-8.0.33.jar:. com/example/Main ARN


public class Main {
        public static void main(String[] args) {
        String airport = "";
        if (args.length > 0) {
            airport = args[0];
        }
        if (airport == null || airport.isEmpty() || !(airport instanceof String)) {
            System.err.println("Invalid input");
        }

        List<FlightDetail> departures = new ArrayList<>();
        List<FlightDetail> arrivals = new ArrayList<>();
 
        try {
         String query = buildQueryString(airport);
         String response = fetchData(query);
         parseResponse(response, departures, arrivals);
        } catch (Exception e) {
            System.err.println("Error encoding parameters: " + e.getMessage());
        }

        // Sort the list on most recent time
        Collections.sort(arrivals, new FlightDetail.FlightComparator());
        Collections.sort(departures, new FlightDetail.FlightComparator());

        //Print arrivals and departures
        System.out.println("ARRIVALS");
        for (FlightDetail a : arrivals) {
            System.out.println(a.printDetails());
        }

        System.out.println("DEPARTURES");
        for (FlightDetail d : departures) {
            System.out.println(d.printDetails());
        }    

        addDeparturesToDB(departures);
    }

    private static String buildQueryString(String airport) throws UnsupportedEncodingException {
        LocalDate date = LocalDate.now(ZoneOffset.UTC);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
        String formattedDate = date.format(formatter);

        ZonedDateTime UTC = ZonedDateTime.now(ZoneOffset.UTC);
        DateTimeFormatter UTCFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String formattedUTC = UTC.format(UTCFormatter);

        String encodedAirport = URLEncoder.encode(airport, "UTF-8");
        String encodedScheduledDate = URLEncoder.encode(formattedDate, "UTF-8");
        String encodedEstimatedDateTime = URLEncoder.encode(formattedUTC, "UTF-8");

        return String.format("https://api.swedavia.se/flightinfo/v2/query?filter=airport%%20eq%%20'%s'%%20and%%20scheduled%%20eq%%20'%s'%%20and%%20estimated%%20le%%20'%s'",
                encodedAirport, encodedScheduledDate, encodedEstimatedDateTime);
    }

    private static String fetchData(String queryString) throws IOException {
        URL url = new URL(queryString);
        String subscriptionKey = "8823aefacb4340868bf25406bac9ddee";

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Ocp-Apim-Subscription-Key", subscriptionKey);

        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        BufferedReader reader;
        StringBuilder response = new StringBuilder();

        if (responseCode == HttpURLConnection.HTTP_OK) {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        }

        String inputLine;
        while ((inputLine = reader.readLine()) != null) {
            response.append(inputLine);
        }
        reader.close();
        connection.disconnect();
        return response.toString();
    }

    private static void parseResponse(String response, List<FlightDetail> arrivals, List<FlightDetail> departures) {
        Gson gson = new Gson();
        FlightResponse flightResponse = gson.fromJson(response, FlightResponse.class);
        List<Flight> flights = flightResponse.getFlights();

        for (Flight flight : flights) {
            if (flight.getArrival() != null) {
                arrivals.add(flight.getArrival());
            } else if (flight.getDeparture() != null) {
                departures.add(flight.getDeparture());
            }
        }
    }

    private static void addDeparturesToDB(List<FlightDetail> departures) {
        DatabaseHandler db = new DatabaseHandler();

        //If more than 10 flihts, get the first 10.
        int n = Math.min(10, departures.size());
        List<FlightDetail> limitedDepartures = departures.subList(0, n);

        try {
            db.connect();
            db.flushDepartures();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                db.disconnect();
            } catch (SQLException e) {
                System.err.println("Error disconnecting from the database: " + e.getMessage());
            }
        }

        try {
            db.connect();
            db.addDepartures(limitedDepartures);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                db.disconnect();
            } catch (SQLException e) {
                System.err.println("Error disconnecting from the database: " + e.getMessage());
            }
        }

        try {
            db.connect();
            db.showDepartures();
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        } finally {
            try {
                db.disconnect();
            } catch (SQLException e) {
                System.err.println("Error disconnecting from the database: " + e.getMessage());
            }
        }
    }
}