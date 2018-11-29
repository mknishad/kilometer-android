package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class EndTripResponse implements Serializable {

    @Expose
    private String status;
    @Expose
    private String distance;
    @Expose
    private String duration;
    @Expose
    private String fare;
    @Expose
    private String vehicle;
    @Expose
    private String error;

    public EndTripResponse() {
    }

    public EndTripResponse(String status, String distance, String duration, String fare,
                           String vehicle, String error) {
        this.status = status;
        this.distance = distance;
        this.duration = duration;
        this.fare = fare;
        this.vehicle = vehicle;
        this.error = error;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "EndTripResponse{" +
                "status='" + status + '\'' +
                ", distance='" + distance + '\'' +
                ", duration='" + duration + '\'' +
                ", fare='" + fare + '\'' +
                ", vehicle='" + vehicle + '\'' +
                ", error='" + error + '\'' +
                '}';
    }
}
