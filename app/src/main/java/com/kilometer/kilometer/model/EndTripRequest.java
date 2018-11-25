package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class EndTripRequest implements Serializable {

    @Expose
    private String deviceId;
    @Expose
    private Location location;

    public EndTripRequest() {
    }

    public EndTripRequest(String deviceId, Location location) {
        this.deviceId = deviceId;
        this.location = location;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "EndTripRequest{" +
                "deviceId='" + deviceId + '\'' +
                ", location=" + location +
                '}';
    }
}
