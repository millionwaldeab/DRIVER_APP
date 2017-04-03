package com.asmarainnovations.taxidriver;

import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;

/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 12/1/2015.
 */
public class Location_Updater_While_Driving implements Callable<String> {

    final String local = Config.local;
    final String remote = Config.remote;

    String token, la, lo, passen, trigger;
    public Location_Updater_While_Driving(){super();}
    public Location_Updater_While_Driving(String a, String b, String c, String d, String e){
        this.token = a;
        this.la = b;
        this.lo = c;
        this.passen = d;
        this.trigger = e;
    }
    public String call() {
        sendInitialUpdates(token, la, lo, passen, trigger);
        return "Task Done";
    }

    public void sendInitialUpdates(String a, String b, String c, String d, String e){
        HttpURLConnection urlConn = null;
        try {
            try {
                URL url;
                //HttpURLConnection urlConn;
                url = new URL(remote + "driver.php");
                urlConn = (HttpURLConnection) url.openConnection();
                System.setProperty("http.keepAlive", "true");
                //urlConn.setDoInput(true); //this is for get request
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                //urlConn.setChunkedStreamingMode(0);  //this causes filenotfoundexception
                urlConn.setRequestMethod("POST");
                urlConn.connect();
                try {
                    //Create JSONObject here
                    JSONObject json = new JSONObject();
                    json.put("drtoken", a);
                    json.put("drlat", b);
                    json.put("drlon", c);
                    json.put("passenger", d);
                    json.put("isConnected", e);
                    String postData = json.toString();

                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    Log.i("NOTIFICATION", "Data Sent");

                    if (d != "connect") {
                        os.flush();
                        os.close();
                    }
                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String msg = "";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        msg += line;
                    }
                    Log.i("msg=", "" + msg);

                } catch (JSONException jsonex) {
                    jsonex.printStackTrace();
                    Log.e("jsnExce", jsonex.toString());
                }
            } catch (MalformedURLException muex) {
                // TODO Auto-generated catch block
                muex.printStackTrace();
            } catch (IOException ioex) {
                ioex.printStackTrace();
                try { //if there is IOException clean the connection and clear it for reuse(works if the stream is not too long)
                    int respCode = urlConn.getResponseCode();
                    InputStream es = urlConn.getErrorStream();
                    byte[] buffer =  null;
                    int ret = 0;
                    // read the response body
                    while ((ret = es.read(buffer)) > 0) {
                        Log.e("streamingError", String.valueOf(respCode) +  String.valueOf(ret));
                    }
                    // close the errorstream
                    es.close();
                } catch(IOException ex) {
                    // deal with the exception
                    ex.printStackTrace();
                }
            }
        } catch (Exception generalException) {
            generalException.printStackTrace();
            Log.e("ERROR", "There is error in this code " + String.valueOf(e));
        }
    }
}