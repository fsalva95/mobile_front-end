package com.example.mobileapplication;

import com.example.mobileapplication.Car;

import java.util.Date;

public class Booking {



    private Car car;
    private String email;
    private String start_date;
    private String end_date;
    //private Boolean ended;


    public Booking(Car car, String email, String start_date, String end_date) {
        this.car = car;
        this.email = email;
        this.start_date = start_date;
        this.end_date = end_date;
    }

    public Booking() {
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }
}
