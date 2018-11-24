package com.kilometer.kilometer.model;

import com.google.gson.annotations.Expose;

import java.io.Serializable;

public class Passenger implements Serializable {

    @Expose
    private String name;
    @Expose
    private String phone;

    public Passenger() {
    }

    public Passenger(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "Passenger{" +
                "name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                '}';
    }
}
