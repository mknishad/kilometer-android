package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Estimation implements Serializable {

    @Expose
    private String kind;
    @Expose
    private String fare;

    public Estimation() {
    }

    public Estimation(String kind, String fare) {
        this.kind = kind;
        this.fare = fare;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getFare() {
        return fare;
    }

    public void setFare(String fare) {
        this.fare = fare;
    }

    @Override
    public String toString() {
        return "Estimation{" +
                "kind='" + kind + '\'' +
                ", fare='" + fare + '\'' +
                '}';
    }
}
