package com.asmarainnovations.taxidriver;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 10/13/2015.
 */
public class MyLocationListener implements LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private static final String TAG = "MyLocationListener";

    private static final long INTERVAL = 1000 * 30;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    private static final long ONE_MIN = 1000 * 60;
    private static final long REFRESH_TIME = ONE_MIN * 5;
    private static final float MINIMUM_ACCURACY = 50.0f;
    Activity locationActivity;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location location;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    FusedLocationReceiver locationReceiver = null;

    public MyLocationListener(Activity locationActivity, FusedLocationReceiver locationReceiver) {
        this.locationReceiver = locationReceiver;
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        this.locationActivity = locationActivity;

        googleApiClient = new GoogleApiClient.Builder(locationActivity)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "I'm connected now");
        Location currentLocation = fusedLocationProviderApi.getLastLocation(googleApiClient);
        if (currentLocation != null && currentLocation.getTime() > REFRESH_TIME) {
            location = currentLocation;
        } else {
            fusedLocationProviderApi.requestLocationUpdates(googleApiClient, locationRequest, this);
            // Schedule a Thread to unregister location listeners
            Executors.newScheduledThreadPool(1).schedule(new Runnable() {
                @Override
                public void run() {
                    fusedLocationProviderApi.removeLocationUpdates(googleApiClient, MyLocationListener.this);
                }
            }, ONE_MIN, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location is changed!");
        //if the existing location is empty or
        //the current location accuracy is greater than existing accuracy
        //then store the current location
        if (null == this.location || location.getAccuracy() < this.location.getAccuracy()) {
            this.location = location;
        // let's inform my client class through the receiver
            locationReceiver.onLocationChanged();
            //if the accuracy is not better, remove all location updates for this listener
            if (this.location.getAccuracy() < MINIMUM_ACCURACY) {
                fusedLocationProviderApi.removeLocationUpdates(googleApiClient, this);
            }
        }
    }


    public Location getLocation() {
        return this.location;
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
