package com.asmarainnovations.taxidriver;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/*Service class that will hold WakeLock, listen for one location update, unregisters location updates, sends location to server, 
schedules AlarmManager, releases WakeLock and calls stopSelf(); Consider a timeout if location updates are not coming. If timeout reached, 
unregister location updates, register next Alarm, release WakeLock and call stopSelf.*/
//this doesn't seem to be called as the app is in the background.
public final class LocationUpdaterServic extends IntentService {

	String TAG = "";
	LocationManager locationManager;
	Context context;
	LocationListener listener;
	Location l = null;
	final String local = Config.local;
	final String remote = Config.remote;

    public LocationUpdaterServic() { super("LocationUpdaterServic"); }
	public LocationUpdaterServic(String name) {
		super(name);
	}


	@Override
	public void onCreate() {
		super.onCreate();
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		try {
			String lat = intent.getStringExtra("dlat");
			String lon = intent.getStringExtra("dlon");
			String pastoken = intent.getStringExtra("pid");
			Log.i("new l", lat + lon);
			if (lat != null && lon != null) {
				sendToServer(Double.parseDouble(lat), Double.parseDouble(lon), pastoken);
			}else{
				Log.i("ATTENTION", lat + " is null!!!");
			}
			}catch (Exception exception){
			exception.printStackTrace();
			Log.i("received exception", exception.toString());
		}
		}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}



	//Receive location updates
	private void startReceivingLocationUpdates() {

	}

	private void sendToServer(double la, double lo, String usertoken) {
		// send to server in background thread. you might want to start AsyncTask here
		try {
			try {
				URL updateurl;
				HttpURLConnection urlConn;
                updateurl = new URL (remote+"initial_available_cab.php");
				urlConn = (HttpURLConnection)updateurl.openConnection();
				urlConn.setDoInput (true);
				urlConn.setDoOutput (true);
				urlConn.setUseCaches (false);
				urlConn.setRequestProperty("Content-Type","application/json");
				urlConn.setRequestProperty("Accept", "application/json");
				//urlConn.setChunkedStreamingMode(0);
				urlConn.setRequestMethod("POST");
				urlConn.connect();

				//Create JSONObject here
				JSONObject json = new JSONObject();
				json.put("drlat", String.valueOf(la));
				json.put("drlon", String.valueOf(lo));
                json.put("passenger", usertoken);

				String postData=json.toString();

				// Send POST output.
				OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
				os.write(postData);
				Log.i("NOTIFICATION", "Cab location Sent");
				os.close();

				BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
				String msg="";
				String line = "";
				while ((line = reader.readLine()) != null) {
					msg += line; }
				Log.i("msg=",""+msg);
			} catch (MalformedURLException muex) {
				// TODO Auto-generated catch block
				muex.printStackTrace();
			} catch (IOException ioex){
				ioex.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("ERROR", "There is error in this code " + String.valueOf(lo));

		}
	}
}
