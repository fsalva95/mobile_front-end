package com.example.mobileapplication;

public class Car {
    private int id;
    private String longitude;
    private String latitude;
    private String busy;
    private String address;
    private String state;

    public Car() {
    }

    public Car(int id, String longitude, String latitude, String busy, String address, String state) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.busy = busy;
        this.address = address;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getBusy() {
        return busy;
    }

    public void setBusy(String busy) {
        this.busy = busy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
