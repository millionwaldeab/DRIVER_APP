package com.asmarainnovations.taxidriver;

import android.location.Location;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Million on 5/5/2016.
 */
public class Passenger {
    String token;
    Marker cMarker;
    String phone;

    public Passenger(){
        super();
    }

    public Passenger(String tokn){
        this.token = tokn;
    }

    public Passenger(Marker mkr, String mytok){
        this.cMarker = mkr;
        this.token = mytok;
    }

    public Passenger(Marker mkr, String mytok, String telephone){
        this.cMarker = mkr;
        this.token = mytok;
        this.phone = telephone;
    }
}
