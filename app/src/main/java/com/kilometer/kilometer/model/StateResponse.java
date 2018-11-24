package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class StateResponse implements Serializable {

    @Expose
    private String status;
    @SerializedName("ontrip")
    private boolean onTrip;
    @Expose
    private Trip trip;

    public StateResponse() {
    }

    public StateResponse(String status, boolean onTrip, Trip trip) {
        this.status = status;
        this.onTrip = onTrip;
        this.trip = trip;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOnTrip() {
        return onTrip;
    }

    public void setOnTrip(boolean onTrip) {
        this.onTrip = onTrip;
    }

    public Trip getTrip() {
        return trip;
    }

    public void setTrip(Trip trip) {
        this.trip = trip;
    }

    @Override
    public String toString() {
        return "StateResponse{" +
                "status='" + status + '\'' +
                ", onTrip=" + onTrip +
                ", trip=" + trip +
                '}';
    }
}
