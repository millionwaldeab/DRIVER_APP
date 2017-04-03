package com.asmarainnovations.taxidriver;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.io.IOException;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 7/21/2015.
 */
public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};
    public static final String PARAM_OUT = "";
    Context context;

    public RegistrationIntentService(){
        super ("RegistrationIntentService");
    }
    @Override
    protected void onHandleIntent(Intent intent) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        try {
            // In the (unlikely) event that multiple refresh operations occur simultaneously,
            // ensure that they are processed sequentially.
            synchronized (TAG) {
                // [START register_for_gcm]
                // Initially this call goes out to the network to retrieve the token, subsequent calls
                // are local.
                // [START get_token]
                try {
                    InstanceID instanceID = InstanceID.getInstance(this);
                    String token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
                            GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                    // [END get_token]

                    Intent i = new Intent();
                    i.setAction(Login.RegistrationResponseReceiver.ACTION_RESP);
                    i.addCategory(Intent.CATEGORY_DEFAULT);
                    i.putExtra(RegistrationIntentService.PARAM_OUT, token);
                    i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);  //added this to try to see if the receiver can get the intent
                    LocalBroadcastManager.getInstance(this).sendBroadcast(i);
                    // TODO: Implement this method to send any registration to your app's servers.
                    /**
                     * Persist registration to third-party servers.
                     *
                     * Modify this method to associate the user's GCM registration token with any server-side account
                     * maintained by your application.
                     *
                     * @param token The new token.
                     */
                //save the token in sharedpreferences
                saveToken(token);

                }catch (Exception exce){
                    exce.printStackTrace();
                    Log.e("ERROR", exce.toString());
                }

                // Subscribe to topic channels
                //subscribeTopics(token);

                // You should store a boolean that indicates whether the generated token has been
                // sent to your server. If the boolean is false, send the token to your server,
                // otherwise your server should have already received the token.
                sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, true).apply();
                // [END register_for_gcm]
            }
        } catch (Exception e) {
            Log.d(TAG, "Failed to complete token refresh", e);
            // If an exception happens while fetching the new token or updating our registration data
            // on a third-party server, this ensures that we'll attempt the update at a later time.
            sharedPreferences.edit().putBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false).apply();
        }
        // Notify UI that registration has completed, so the progress indicator can be hidden.
        Intent registrationComplete = new Intent(QuickstartPreferences.REGISTRATION_COMPLETE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(registrationComplete);
    }

    /**
     * Subscribe to any GCM topics of interest, as defined by the TOPICS constant.
     *
     * @param token GCM token
     * @throws IOException if unable to reach the GCM PubSub service
     */
    // [START subscribe_topics]
    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
    // [END subscribe_topics]


    public void saveToken(String stringToken){
        //save the token in a sharedprefs
        SharedPreferences.Editor editor = getSharedPreferences(QuickstartPreferences.MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("savedtoken", stringToken);
        editor.commit();
    }
}
