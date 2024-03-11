package com.example;

import java.sql.*;
import java.util.List;

public class DatabaseHandler {
    private Connection connection;
    private static final String URL = "jdbc:mysql://localhost:3306/flights";
    private static final String USER = "root";
    private static final String PASSWORD = "password";

    public Connection getConnection() {
        return connection;
    }

    public void connect() throws SQLException {
        connection = DriverManager.getConnection(URL, USER, PASSWORD);
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

    public void flushDepartures() {
        String sql = "DELETE FROM departures";
        try (
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.executeUpdate();
            System.out.println("Departures table flushed successfully.");
        } catch (SQLException e) {
            System.err.println("Error flushing departures: " + e.getMessage());
        }
    }

    public void addDepartures(List<FlightDetail> departures) {
        String sql = "INSERT INTO departures (flight_id, airline, airport, time) " +
        "VALUES (?, ?, ?, ?)";

        try (
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(sql)) {
            for (FlightDetail departure : departures) {
                statement.setString(1, departure.getFlightID());
                statement.setString(2, departure.getAirlineOperator().getName());
                statement.setString(3, departure.getSecondaryAirportEnglish());
                statement.setString(4, departure.getTime().getFormattedTime());
                statement.executeUpdate();
            }
            System.out.println("Departures added successfully.");
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public void showDepartures() {
        String sql = "SELECT * FROM departures ORDER BY time DESC";
        try (
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                String id = resultSet.getString("flight_id");
                String airline = resultSet.getString("airline");
                String airport = resultSet.getString("airport");
                String time = resultSet.getString("time");

                System.out.println("Flight ID: " + id + ", airline: " + airline + ", airport: " + airport +", Time: " + time);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}