package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.ArrayList;

public class EstimationResponse implements Serializable {

    @Expose
    private String status;
    @Expose
    private ArrayList<String> from;
    @Expose
    private ArrayList<String> to;
    @Expose
    private String distance;
    @Expose
    private String duration;
    @Expose
    private ArrayList<Estimation> estimations;

    public EstimationResponse() {
    }

    public EstimationResponse(String status, ArrayList<String> from, ArrayList<String> to, String distance,
                              String duration, ArrayList<Estimation> estimations) {
        this.status = status;
        this.from = from;
        this.to = to;
        this.distance = distance;
        this.duration = duration;
        this.estimations = estimations;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ArrayList<String> getFrom() {
        return from;
    }

    public void setFrom(ArrayList<String> from) {
        this.from = from;
    }

    public ArrayList<String> getTo() {
        return to;
    }

    public void setTo(ArrayList<String> to) {
        this.to = to;
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

    public ArrayList<Estimation> getEstimations() {
        return estimations;
    }

    public void setEstimations(ArrayList<Estimation> estimations) {
        this.estimations = estimations;
    }

    @Override
    public String toString() {
        return "EstimationResponse{" +
                "status='" + status + '\'' +
                ", from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", distance='" + distance + '\'' +
                ", duration='" + duration + '\'' +
                ", estimations=" + estimations +
                '}';
    }
}
