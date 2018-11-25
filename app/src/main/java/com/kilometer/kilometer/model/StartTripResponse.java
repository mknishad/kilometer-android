package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class StartTripResponse implements Serializable {

    @Expose
    private String status;
    @Expose
    private Trip trip;
    @Expose
    private String error;

    public StartTripResponse() {
    }

    public StartTripResponse(String status, Trip trip) {
        this.status = status;
        this.trip = trip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "StartTripResponse{" +
                "status='" + status + '\'' +
                ", trip=" + trip +
                ", error='" + error + '\'' +
                '}';
    }
}
