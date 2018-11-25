package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class EstimationRequest implements Serializable {

    @Expose
    private String from;
    @Expose
    private String to;

    public EstimationRequest() {
    }

    public EstimationRequest(String from, String to) {
        this.from = from;
        this.to = to;
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

    @Override
    public String toString() {
        return "EstimationRequest{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                '}';
    }
}
