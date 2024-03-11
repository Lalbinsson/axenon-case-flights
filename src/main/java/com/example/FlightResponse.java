package com.example;

import java.util.List;

public class FlightResponse {
    private List<Flight> flights;

    public List<Flight> getFlights() {
        return flights;
    }
}

class Flight {
    private FlightDetail arrival;
    private FlightDetail departure;

    public FlightDetail getArrival() {
        return arrival;
    }

    public FlightDetail getDeparture() {
        return departure;
    }
}

