package com.asmarainnovations.taxidriver;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 12/9/2015.
 */

public class ReverseGeocoderDestinationIntentService extends IntentService {
    public static final String SEND_ACTION = "com.asmarainnovations.taxidriver.ReverseGeocoderDestinationIntentService";
    String TAG = "";
    LocalBroadcastManager addressBroadcaster;

    public ReverseGeocoderDestinationIntentService() {
        super("");
    }
    public ReverseGeocoderDestinationIntentService(String name) {
        super(name);
    }

    @Override
    public void onCreate() {
        addressBroadcaster = LocalBroadcastManager.getInstance(this);
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        String errorMessage = "";
        if (intent != null && !"".equals(intent.getStringExtra("ltlng"))) {
            // Get the location passed to this service through an extra.
            String locstring = intent.getStringExtra("ltlng");
            String[] locstrarr = locstring.split(",");
            double firstd = Double.parseDouble(locstrarr[0]);
            double secondd = Double.parseDouble(locstrarr[1]);
            Location location = new Location("");
            location.setLatitude(firstd);
            location.setLongitude(secondd);
            Log.e("location received", location.toString());
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // In this sample, get just one addresse.
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = getString(R.string.service_not_available);
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = getString(R.string.invalid_lat_lon);
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " +
                        location.getLongitude(), illegalArgumentException);
            }

            // Handle case where no address was found.
            if (addresses == null || addresses.size() == 0) {
                if (errorMessage.isEmpty()) {
                    errorMessage = getString(R.string.no_address_found);
                    Log.e(TAG, errorMessage);
                }
                //deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            } else if (!"".equals(addresses) || null != addresses || addresses.size() > 0) {
                Address address = addresses.get(0);
                final ArrayList<String> addressFragments = new ArrayList<String>();

                // Fetch the address lines using getAddressLine,
                // join them, and send them to the thread.
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Intent sendAddress = new Intent();
                sendAddress.putExtra("addr", addressFragments);
                sendAddress.setAction(SEND_ACTION);
                addressBroadcaster.sendBroadcast(sendAddress);
                //Log.i(TAG, getString(R.string.no_address_found));

            }
        }else{
            Log.e("ERROR", "Empty location ReverseGeocoder line 99");
        }
    }
}
