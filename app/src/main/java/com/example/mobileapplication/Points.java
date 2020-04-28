package com.example.mobileapplication;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.List;

public class Points implements Serializable {

    private List<LatLng> point;

    public Points(List<LatLng> point) {
        this.point = point;
    }

    public List<LatLng> getPoint() {
        return point;
    }

    public void setPoint(List<LatLng> point) {
        this.point = point;
    }
}
