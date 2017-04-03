package com.asmarainnovations.taxidriver;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import com.asmarainnovations.taxidriver.MapActivity.AddressResultReceiver;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
public class FetchAddressIntentService extends IntentService {
	
	private static final String TAG = "Mapper";
	static final int numberOptions = 10;
	String[] optionArray = new String[numberOptions];
	double lat, lon;
	GoogleMap map;
	LatLng convertedcoordinates;
	public AddressResultReceiver mRReceiver;
	
	/*public FetchAddressIntentService() {
		super("FetchAddressIntentService");
		// TODO Auto-generated constructor stub
	}*/
	
	public FetchAddressIntentService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	//This to bypass the dead thread exception
	Handler mMainThreadHandler = null;
    public FetchAddressIntentService() {
        super(FetchAddressIntentService.class.getName());

        mMainThreadHandler = new Handler();
    }
	
	@Override
	public void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
        
		Geocoder geocoder = new Geocoder(this, Locale.getDefault());
		String errorMessage = "";

	    // Get the location passed to this service through an extra.
	    Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);
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
	    if (addresses == null || addresses.size()  == 0) {
	        if (errorMessage.isEmpty()) {
	            errorMessage = getString(R.string.no_address_found);
	            Log.e(TAG, errorMessage);
	        }
	        deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
	    } else if (!"".equals(addresses) || null != addresses || addresses.size() > 0) {
	        Address address = addresses.get(0);
	        final ArrayList<String> addressFragments = new ArrayList<String>();

	        // Fetch the address lines using getAddressLine,
	        // join them, and send them to the thread.
	        for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
	            addressFragments.add(address.getAddressLine(i));
	        }
	        //Log.i(TAG, getString(R.string.no_address_found));
	        
	      //This is to bypass the dead thread exception
	  		System.out.printf("Network service intent handling: %s\n", intent.toString());
	          mMainThreadHandler.post(new Runnable() {
	              @Override
	              public void run() {
	            	  deliverResultToReceiver(Constants.SUCCESS_RESULT, TextUtils.join(System.getProperty("line.separator"), addressFragments));
	              }
	          });
	    }
	}

	//This method is to deliver the error codes
    private void deliverResultToReceiver(int resultCode, String message) {
    	MapActivity mac = new MapActivity();
    	MapActivity.AddressResultReceiver mRReceiver = mac.new AddressResultReceiver(new Handler());
    	//mRReceiver = new MainActivity.AddressResultReceiver(new Handler());
    	//mRReceiver.setReceiver(this.mRReceiver);
    	Bundle bundle = new Bundle();
    	try {
			if (message != null || !"".equals(message))
        bundle.putString(Constants.RESULT_DATA_KEY, message);
				mRReceiver.send(resultCode, bundle);
			} catch (NullPointerException npe) {
				// TODO Auto-generated catch block
				npe.printStackTrace();
			}
    }


	// Method to geocode location passed as string (e.g., "Pentagon"), which
	// places the corresponding latitude and longitude in the variables lat and
	// lon. This method will convert address to lat/lon

	public LatLng geocodeLocation(String placeName) {

		// Following adapted from Conder and Darcey, pp.321 ff.
		Geocoder gcoder = new Geocoder(this);

		// Note that the Geocoder uses synchronous network access, so in a
		// serious application
		// it would be best to put it on a background thread to prevent blocking
		// the main UI if network
		// access is slow. Here we are just giving an example of how to use it
		// so, for simplicity, we
		// don't put it on a separate thread. See the class RouteMapper in this
		// package for an example
		// of making a network access on a background thread. Geocoding is
		// implemented by a backend
		// that is not part of the core Android framework, so we use the static
		// method
		// Geocoder.isPresent() to test for presence of the required backend on
		// the given platform.

		try {
			List<Address> results = null;
			if (Geocoder.isPresent()) {
				results = gcoder.getFromLocationName(placeName, numberOptions);
			} else {
				Log.i(TAG, "No geocoder accessible on this platform");
				//return;
			}
			Iterator<Address> locations = results.iterator();
			String raw = "\nRaw String:\n";
			String country;
			int opCount = 0;
			while (locations.hasNext()) {
				Address location = locations.next();
				if (opCount == 0 && location != null) {
					lat = location.getLatitude();
					lon = location.getLongitude();
				}
				country = location.getCountryName();
				if (country == null) {
					country = "";
				} else {
					country = ", " + country;
				}
				raw += location + "\n";
				optionArray[opCount] = location.getAddressLine(0) + ", "
						+ location.getAddressLine(1) + country + "\n";
				opCount++;
			}
			Log.i(TAG, raw);
			Log.i(TAG, "\nOptions:\n");
			for (int i = 0; i < opCount; i++) {
				Log.i(TAG, "(" + (i + 1) + ") " + optionArray[i]);
			}
			Log.i(TAG, "lat=" + lat + " lon=" + lon);

		} catch (IOException e) {
			Log.e(TAG, "I/O Failure; is network available?", e);
		}
		
		return convertedcoordinates = new LatLng(lat, lon);
	}
	
	
	//A class that contains the constants to check the results of the geocoder class
	public final class Constants {
	    public static final int SUCCESS_RESULT = 0;
	    public static final int FAILURE_RESULT = 1;
	    public static final String PACKAGE_NAME = "com.asmarainnovations.taxidriver";
	    public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
	    public static final String RESULT_DATA_KEY = PACKAGE_NAME + ".RESULT_DATA_KEY";
	    public static final String LOCATION_DATA_EXTRA = PACKAGE_NAME + ".LOCATION_DATA_EXTRA";
	}
}

