package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class StartTripRequest implements Serializable {
    @Expose
    private String deviceId;
    @Expose
    private String from;
    @Expose
    private String to;
    @Expose
    private String vehicle;
    @Expose
    private Passenger passenger;
    @Expose
    private Location location;

    public StartTripRequest() {
    }

    public StartTripRequest(String deviceId, String from, String to, String vehicle, Passenger passenger, Location location) {
        this.deviceId = deviceId;
        this.from = from;
        this.to = to;
        this.vehicle = vehicle;
        this.passenger = passenger;
        this.location = location;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
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

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "StartTripRequest{" +
                "deviceId='" + deviceId + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", vehicle='" + vehicle + '\'' +
                ", passenger=" + passenger +
                ", location=" + location +
                '}';
    }
}
