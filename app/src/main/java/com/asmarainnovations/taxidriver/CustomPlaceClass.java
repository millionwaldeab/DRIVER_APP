package com.asmarainnovations.taxidriver;

import android.content.Context;
import android.net.Uri;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.List;
import java.util.Locale;

/**
 * Created by Million on 2/25/2016.
 */
public class CustomPlaceClass implements Place {

    Context con;
    String place;

    public CustomPlaceClass(Context context, String string){
        this.con = context;
        this.place = string;
    }

    @Override
    public String getId() {
        return place;
    }

    @Override
    public List<Integer> getPlaceTypes() {
        return null;
    }

    @Override
    public CharSequence getAddress() {
        return null;
    }

    @Override
    public Locale getLocale() {
        return Locale.getDefault();
    }

    @Override
    public CharSequence getName() {
        return place;
    }

    @Override
    public LatLng getLatLng() {
        return null;
    }

    @Override
    public LatLngBounds getViewport() {
        return null;
    }

    @Override
    public Uri getWebsiteUri() {
        return null;
    }

    @Override
    public CharSequence getPhoneNumber() {
        return null;
    }

    @Override
    public float getRating() {
        return 0;
    }

    @Override
    public int getPriceLevel() {
        return 0;
    }

    @Override
    public Place freeze() {
        return null;
    }

    @Override
    public boolean isDataValid() {
        return false;
    }
}
