package com.example;

import java.util.Comparator;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.Instant;

public class FlightDetail {
    private String flightId;
    private String departureAirportEnglish;
    private String arrivalAirportEnglish;
    private Airline airlineOperator;
    private FlightTime arrivalTime;
    private FlightTime departureTime;

    public String getFlightID() {
        return flightId;
    }

    public String getSecondaryAirportEnglish(){
        if (arrivalAirportEnglish != null) {
            return arrivalAirportEnglish;
        } else {
            return departureAirportEnglish;
        }
    }

    public Airline getAirlineOperator() {
        return airlineOperator;
    }

    public FlightTime getTime() {
        if (arrivalTime != null) {
            return arrivalTime;
        } else {
            return departureTime;
        }
    }

    public String printDetails() {
        StringBuilder sb = new StringBuilder();

        sb.append(getFlightID()+" "+getAirlineOperator().getName());
        sb.append(": "+getSecondaryAirportEnglish());
        sb.append(" "+" "+getTime().getFormattedTime());
        return sb.toString();
    }

    // Comparator for arrival times
    public static class FlightComparator implements Comparator<FlightDetail> {
        @Override
        public int compare(FlightDetail flight1, FlightDetail flight2) {
            String time1 = flight1.getTime().getScheduledUtc();
            String time2 = flight2.getTime().getScheduledUtc();
            return time2.compareTo(time1);
        }
    }
}

class FlightTime {
    private String scheduledUtc;

    public String getScheduledUtc() {
        return scheduledUtc;
    }

    public String getFormattedTime(){
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.parse(getScheduledUtc()), ZoneId.systemDefault());
        DateTimeFormatter Stringformatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = localDateTime.format(Stringformatter);
        return formattedDateTime;
    }
}

 class Airline {
    private String name;

    public String getName() {
        return name;
    }
}