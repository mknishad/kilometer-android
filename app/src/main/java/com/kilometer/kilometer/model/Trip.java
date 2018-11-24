package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Trip implements Serializable {

    @Expose
    private String tripId;
    @Expose
    private String from;
    @Expose
    private String to;
    @Expose
    private String vehicle;
    @Expose
    private Passenger passenger;

    public Trip() {
    }

    public Trip(String tripId, String from, String to, String vehicle, Passenger passenger) {
        this.tripId = tripId;
        this.from = from;
        this.to = to;
        this.vehicle = vehicle;
        this.passenger = passenger;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public Passenger getPassenger() {
        return passenger;
    }

    public void setPassenger(Passenger passenger) {
        this.passenger = passenger;
    }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", vehicle='" + vehicle + '\'' +
                ", passenger=" + passenger +
                '}';
    }
}
