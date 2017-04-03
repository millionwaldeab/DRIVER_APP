/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
package com.asmarainnovations.taxidriver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.asmarainnovations.taxidriver.FetchAddressIntentService.Constants;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.gcm.GcmListenerService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Filter;

public class MapActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener,
        OnMyLocationButtonClickListener, OnClickListener {

    protected static Marker customMarker;
    public static Location myloc, mylocation, changedL;
    LocationManager locationManager;
    LocationRequest mLocationRequest;
    LocationListener listener;
    static AutoCompleteTextView msg, destinationText;
    String  TAG, ipassenger = "test";
    static String tok = null;
    private static Button getdirection, cancelrequest, todestination, signout, signin;
    GoogleMap map;
    GoogleApiClient mGoogleApiClient;
    GoogleCloudMessaging gcm;
    AtomicInteger msgId;
    public AddressResultReceiver mAddressResultReceiver;
    Context context;
    Connection conn;
    boolean mAddressRequested, loaded = false;
    private boolean mMediaPlaying = false;
    boolean mRequestingLocationUpdates = false, isDriverAvailable = false, isSendingUpdates = false, isUpdatingDB = false;
    private boolean isDialogShown = false;

    DrawerLayout mdrawerLayout;
    ListView mMenuList;
    ImageView appImage;
    TextView TitleText;
    String[] MenuTitles = new String[]{"Promotions", "Legal", "Contact Us", "Misc"};
    Toolbar toolbar;
    private ActionBarDrawerToggle drlistener;
    private final int TWO_MINUTES = 1000 * 60 * 2;
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Server Url absolute url where php files are placed.
    static final String PHP_SERVER_URL = Config.PHP_SERVER_URL; //local php server url for development and testing
    String SENDER_ID = Config.SENDER_ID; //this is the project number
    String PASSENGER_SENDER_ID = Config.PASSENGER_SENDER_ID; //your project number
    static final String GCM_TAG = "GCMDemo"; //log messages
    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    final String local = Config.local;
    final String remote = Config.remote;//your production server url
    private static final String API_KEY = Config.ANDROID_API_KEY; //this is android api key 2
    final static String server_Key = Config.server_Key;//add your own server key
    private MarkerAnimation cabAnimation;
    //BroadcastReceiver downstreamReceiver;
    //Connection c;
    //private AlertDialog.Builder builder;
    //private AlertDialog dia;
    private static SoundPool spRequest, spCancelled, spDestination;
    private static int spSoundId;
    MyLocationListener loclistener;
    private FusedLocationProviderApi fusedLocationProviderApi = LocationServices.FusedLocationApi;

    private static View mView;
    //private Dialog mDialog;
    private LayoutInflater mInflater;

    Passenger passenger;
    List<Passenger> passengerList;

    Location_Updater_While_Driving drivingUpdater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_activity);
        msg = (AutoCompleteTextView) findViewById(R.id.actvgetdirection);
        destinationText = (AutoCompleteTextView) findViewById(R.id.actvDestination);
        getdirection = (Button) findViewById(R.id.bgetdirection);
        cancelrequest = (Button) findViewById(R.id.brejectrequest);
        todestination = (Button) findViewById(R.id.bto_destination);
        signout = (Button) findViewById(R.id.bsignout);
        signin = (Button) findViewById(R.id.bsignin);

        gcm = GoogleCloudMessaging.getInstance(this);
        msgId = new AtomicInteger();
        tok = getRegistrationToken();
        signout.setOnClickListener(this);
        signin.setOnClickListener(this);
        //navigation drawer specific UI design
        mdrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mMenuList = (ListView) findViewById(R.id.MenuList);
        appImage = (ImageView) findViewById(android.R.id.home);

        TitleText = (TextView) findViewById(android.R.id.title);
        mAddressResultReceiver = new AddressResultReceiver(new Handler());

        getdirection.setVisibility(View.INVISIBLE);
        cancelrequest.setVisibility(View.INVISIBLE);
        todestination.setVisibility(View.INVISIBLE);
        context = getApplicationContext();
        cabAnimation = new MarkerAnimation();
        passengerList = new ArrayList<>();

        //builder = new AlertDialog.Builder(MapActivity.this);

        // Check device for Play Services APK. If this check succeeds, proceed with normal processing.
        // Otherwise, prompt user to get valid Play Services APK.

        setSoundPoolVariables(); //Instantiate Sound pool variables

        //If there is no internet connection notify the user and close
        conn = new Connection(context);
        if (!conn.isNetworkAvailable()) {
            UtilityClass customtoast = new UtilityClass(context);
            customtoast.showToast(30000, "No internet connection, please try again later");
            finish();
        }
        if (checkPlayServices()) {

            // This prevents my Nexus 7 running Android 4.4.2 from opening
            // the soft keyboard when the app is launched rather than when
            // an input field is selected.
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            //startReceivingLocationUpdates();
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            // Do a null check to confirm that we have not already instantiated the
            // map.
            if (map == null) {
                // Try to obtain the map from the SupportMapFragment.
                map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
                // Check if we were successful in obtaining the map.
                if (map != null) {
                    map.setMyLocationEnabled(true);
                    map.getUiSettings().isCompassEnabled();
                    map.getUiSettings().setMyLocationButtonEnabled(true);
                    map.getUiSettings().isZoomControlsEnabled();
                    map.getUiSettings().isMyLocationButtonEnabled();
                    //map.setPadding(0, 300, 0, 0);
                    //make the current location at the center

                    startLocationUpdates();

                    map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                        @Override
                        public void onMyLocationChange(Location location) {
                            CameraUpdate center = CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude()));
                            CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);
                            map.moveCamera(center);
                            map.animateCamera(zoom);
                            draw_current_cab_location(location);
                            mylocation = location;
                            isDriverAvailable = true;
                            isUpdatingDB = true;
                            updateDB(tok, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));

                            //updates = new ContinuosDBUpdates(tok, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));

                            //ft = new FutureTask<String>(updates);
                            //boolean b = ft.cancel(false);
                            //ft.run();

                            //PostData sendUpdates = new PostData();
                            //sendUpdates.execute(tok, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), "connect");

                        }
                    });
                }
            }
            //Keep the screen awake while the app is on
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            //get location hard coded way so onlacationchanged is called
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            boolean enabledGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean enabledWiFi = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Check if enabled and if not send user to the GSP settings
            // Better solution would be to display a dialog and suggesting to
            // go to the settings
            if (!enabledGPS) {
                Toast.makeText(this, "GPS signal not found", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }

           /* try {
                mylocation = getCurrentLocation();

                // Initialize the location fields
                if (mylocation != null && tok != null) {
                    //draw_current_cab_location(mylocation);
                    //PostData sendUpdates = new PostData();
                    //sendUpdates.execute(tok, String.valueOf(mylocation.getLatitude()), String.valueOf(mylocation.getLongitude()));
                } else {
                    try {
                        mylocation = loclistener.getLocation();
                        //draw_current_cab_location(mylocation);
                        //PostData sendUpdates = new PostData();
                        //sendUpdates.execute(tok, String.valueOf(mylocation.getLatitude()), String.valueOf(mylocation.getLongitude()));
                    } catch (NullPointerException nullexception) {
                        nullexception.printStackTrace();
                        Log.e("exception ma 210", nullexception.toString());
                    }
                }
            }catch(Exception exc){
                exc.printStackTrace();
                Log.e("Exception line ma 211", exc.toString());
            }*/

            // Get the address from the goecoder class and display it on the
            // autocomplatetextview
            //fetchAddressButtonHandler(msg);

            //get the gcm token
            Intent i = new Intent(this, RegistrationIntentService.class);
            startService(i);

            /*if (isDriverAvailable) {
                draw_current_cab_location(map.getMyLocation());
                //customMarker.setVisible(true);
            } else {
                customMarker.setVisible(false);
            }*/
        }

        msg.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.search_results_list_item, R.id.tvSearchResultItem));
        msg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String selectedPlace = (String) parent.getItemAtPosition(position);
                Intent myintent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + selectedPlace));
                if (myintent.resolveActivity(getPackageManager()) != null) { //to make sure there is an app to open the map
                    startActivity(myintent);
                }
                //Place selectedPlace = Places.GeoDataApi.getPlaceById(mGoogleApiClient, );
                //Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
            }

            private LatLng getSelectedLatLng(Place placeStringe) {
                LatLng placeltlg = placeStringe.getLatLng();
                return placeltlg;
            }
        });

        destinationText.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.search_results_list_item, R.id.tvSearchResultItem));
        destinationText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
        //PostData sendUpdates = new PostData();
        //sendUpdates.execute(tok, String.valueOf(customMarker.getPosition().latitude), String.valueOf(customMarker.getPosition().longitude));

        //the custom layout is to be able to change the sliding panel background and text color...
        mMenuList.setAdapter(new ArrayAdapter(this, R.layout.custom_layout_for_listview, MenuTitles));

        drlistener = new ActionBarDrawerToggle(this, mdrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getActionBar().setTitle(getTitle());
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getActionBar().setTitle(getTitle());
            }
        };

        // Set the drawer toggle as the DrawerListener
        mdrawerLayout.setDrawerListener(drlistener);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);

        mMenuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String itemPosition = parent.getItemAtPosition(position).toString();
                switch (itemPosition) {
                    case "Promotions":
                        Intent promotion = new Intent(MapActivity.this, Promotions.class);
                        startActivity(promotion);
                        break;
                    case "Legal":
                        Intent legal = new Intent(MapActivity.this, Legal.class);
                        startActivity(legal);
                        break;
                    case "Contact Us":
                        Intent contact = new Intent(MapActivity.this, ContactUs.class);
                        startActivity(contact);
                        break;
                    case "Misc":
                        Intent miscel = new Intent(MapActivity.this, Miscellaneous.class);
                        startActivity(miscel);
                        break;
                }
            }
        });
    }

    private void setSoundPoolVariables() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            spRequest = createNewSoundPool(spRequest, 7, R.raw.pickup_request);
            spCancelled = createNewSoundPool(spCancelled, 2, R.raw.notifyme);
            spDestination = createNewSoundPool(spDestination, 2, R.raw.notifyme);
        } else {
            spRequest = createOldSoundPool(spRequest, 7, R.raw.pickup_request);
            spCancelled = createOldSoundPool(spCancelled, 2, R.raw.notifyme);
            spDestination = createOldSoundPool(spDestination, 2, R.raw.notifyme);
        }
    }

    //this is to send initial location updates to display, when passenger starts the app
    private BroadcastReceiver initial_BroadcastRcvr = new BroadcastReceiver() {
        String  ipalat = "test", ipalon = "test";
        @Override
        public void onReceive(Context context, Intent intent) {
            final String messagestr = intent.getStringExtra(MyGcmListenerService.INITIAL_UPDATES);
            if (messagestr != null && messagestr.length() != 0) {
                String[] separated = messagestr.split(",");
                ipassenger = separated[0].trim();
                ipalat = separated[1].trim();
                ipalon = separated[2].trim();
                //create a passenger object
                passenger = new Passenger(ipassenger);
                passengerList.add(passenger);

                //changedL = getChangedLocation();
                isSendingUpdates = true;
                map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location newLocation) {
                        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins="+String.valueOf(newLocation.getLatitude())
                                +","+String.valueOf(newLocation.getLongitude())+"&destinations="+ipalat+","+ipalon+"&departure_time="
                                +String.valueOf(newLocation.getTime())+"&traffic_model=best_guess&key="+server_Key;
                        String dstnFile = remote + "initial_available_cab.php";
                        getDrivingDuration(dstnFile, "connected", url);
                    }
                });
            } else {
                Log.e("ma456 rcvr", "Receiver received null intent");
            }
        }
    };

    //this is passenger request that will receive passenger location in latitude and longitude and token
    private BroadcastReceiver downstreamReceiver = new BroadcastReceiver() {
        Location l = new Location(LocationManager.GPS_PROVIDER);
        String latst = "test", lonst = "test", strid = "test";

        @Override
        public void onReceive(Context context, final Intent intent) {
            //here sending locations to other passengers should stop
            //LayoutInflater inflater = getLayoutInflater();
            //View dialoglayout = inflater.inflate(, null);
            //builder.setView(dialoglayout);
            //builder.setTitle("REQUEST");
            //builder.setMessage("Would you like to accept this trip?");
            //builder.show();
            //dia = builder.create();
            //dia.show();

            String messagestr = intent.getStringExtra(MyGcmListenerService.DOWNSTREAM_MESSAGE);
            if (messagestr != null && !messagestr.isEmpty()) {
                getdirection.setVisibility(View.VISIBLE);
                cancelrequest.setVisibility(View.VISIBLE);
                //Receive the location latlng as a string and convert to location object
                String[] separated = messagestr.split(",");
                latst = separated[0].trim();
                lonst = separated[1].trim();
                strid = separated[2].trim(); //this is the passenger id making the request
                try {
                    double lati = Double.parseDouble(latst);
                    double longi = Double.parseDouble(lonst);
                    l.setLatitude(lati);
                    l.setLongitude(longi);
                    //open alert dialog and driver accpets or rejects request
                    alertPassenger(strid, lati, longi);
                    //convert the coordinates received from passenger to an address
                    startIntentService(l);

                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            } else {
                Toast.makeText(getApplication(), "No valid address provided", Toast.LENGTH_SHORT).show();
            }

            Intent dialogstarter = new Intent(context, MapActivity.class);
            dialogstarter.setAction("mydialog");
            dialogstarter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            dialogstarter.putExtra("id", strid);
            context.startActivity(dialogstarter);

            getdirection.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //do something like this to launch google map navigation
                    Intent myintent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=" + latst + "," + lonst));
                    if (myintent.resolveActivity(getPackageManager()) != null) { //to make sure there is an app to open the map
                        startActivity(myintent);
                    }

                    //after the driver accepts request call this to send location updates to passenger. But this should done
                    //from a service because the driver is now on a google navigation app and this app is in background
                    //sendUpdatesToPassenger(strid);
                }
            });
            cancelrequest.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //cancel passenger's request
                    msg.setText("");
                    getdirection.setVisibility(View.GONE);
                    cancelrequest.setVisibility(View.GONE);
                    CancelRequest cancelrequest = new CancelRequest();
                    String pid = strid;
                    String cancelcode = "cancel";
                    String did = tok;
                    isSendingUpdates = false;
                    isUpdatingDB = false;
                    cancelrequest.execute(pid, cancelcode, did);
                }
            });
        }
    };


    //this will receive passenger destination location in latitude and longitude and token
    private BroadcastReceiver destinationReceiver = new BroadcastReceiver() {
        Location l = new Location("");
        String latst = "test", lonst = "test", strid = "test";

        @Override
        public void onReceive(Context context, final Intent intent) {
            spDestination.play(spSoundId, 1, 1, 1, 2, 1);

            String messagestr = intent.getStringExtra(MyGcmListenerService.DESTINATION_MESSAGE);
            if (messagestr != null && !messagestr.isEmpty()) {
                //getdirection.setVisibility(View.VISIBLE);
                //cancelrequest.setVisibility(View.VISIBLE);
                //Receive the location latlng as a string and convert to location object
                String[] separated = messagestr.split(",");
                latst = separated[0].trim();
                lonst = separated[1].trim();
                strid = separated[2].trim(); //this is the passenger id making the request
                try {
                    double lati = Double.parseDouble(latst);
                    double longi = Double.parseDouble(lonst);
                    l.setLatitude(lati);
                    l.setLongitude(longi);

                    //convert the coordinates received from passenger to an address
                    Intent sendlocation = new Intent(MapActivity.this, ReverseGeocoderDestinationIntentService.class);
                    sendlocation.putExtra("ltlng", latst + ", " + lonst);
                    startService(sendlocation);

                    //get driving route towards destination
                    todestination.setVisibility(View.VISIBLE);
                    todestination.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //minimize the app so it will stay in background
                            Intent main = new Intent(Intent.ACTION_MAIN);
                            main.addCategory(Intent.CATEGORY_HOME);
                            main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(main);
                            //do something like this to launch google map navigation
                            Intent myintent = new Intent(android.content.Intent.ACTION_VIEW,
                                    Uri.parse("geo:0,0?q=" + latst + "," + lonst));
                            startActivity(myintent);
                            //after the driver accepts request call this to send location updates to passenger
                            //this is probably redundant as the rider is already getting updates when driver comes to pick them up
                            //sendUpdatesToPassenger(strid);
                        }
                    });
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
            } else {
                Toast.makeText(getApplication(), "No valid address provided", Toast.LENGTH_SHORT).show();
            }

            getdirection.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //minimize the app so it will stay in background
                    Intent main = new Intent(Intent.ACTION_MAIN);
                    main.addCategory(Intent.CATEGORY_HOME);
                    main.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(main);
                    //do something like this to launch google map navigation
                    Intent myintent = new Intent(android.content.Intent.ACTION_VIEW,
                            Uri.parse("geo:0,0?q=" + latst + "," + lonst));
                    startActivity(myintent);
                    //After drop off the driver should be online and available again
                }
            });
            /*cancelrequest.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    //cancel passenger's request
                    CancelRequest cancelrequest = new CancelRequest();
                    String cancelcode = "cancel";
                    String pid =  strid;
                    cancelrequest.execute(cancelcode, pid);
                }
            });*/
        }
    };

    //this will receive the address converted from the location object from reverse geocoder class
    private BroadcastReceiver destinationAddressReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            ArrayList<String> AddressAL = intent.getStringArrayListExtra("addr");
            String convertedAddress = TextUtils.join(" ", AddressAL);
            destinationText.setText(convertedAddress);
        }
    };


    private void alertPassenger(final String requester, final double platitude, final double plongitude) {
        final Dialog d = new Dialog(MapActivity.this, R.style.dialog_box_style);
        d.setContentView(R.layout.dialog_layout);
        if (!d.isShowing()) {
            isDialogShown = true;
            d.show();
        }
        spRequest.play(spSoundId, 1, 1, 1, 12, 1);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //dia.dismiss();
                d.dismiss();
                isDialogShown = false;
                spRequest.release();
            }
        }, 7000);    //the alert will play for 7 seconds and stop


        Button accept = (Button) d.findViewById(R.id.bAcceptRequest);
        accept.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //mDialog.cancel();
                //dia.dismiss();
                d.dismiss();
                isDialogShown = false;
                if (!d.isShowing()) {
                    Log.e("Dialog", "not shwing");
                } else {
                    Log.e("Dialog", "shwing");
                }
                spRequest.release();
                //when driver accepts the request the location updates to other passengers should stop.
                //stop the location updates to other passengers now this driver should be unavailable
                //cancel the initial updates to all riders and then send regular updates to the one that requested ride
                for (int i =0; i < passengerList.size(); i++) {
                    if (passengerList != null && !passengerList.get(i).token.equals(requester)){
                        //make driver unavailable from other passengers
                        isDriverAvailable = false;
                        DriverUnavailable done = new DriverUnavailable();
                        done.execute(passengerList.get(i).token, tok, "unavailable");
                        passengerList.remove(passengerList.get(i));
                    }
                }

                //start service that will send updates while driving
                /*Intent driving_Location_update = new Intent(MapActivity.this, Location_Updater_While_Driving.class);
                driving_Location_update.setAction("com.asmarainnovations.taxidriver.Location_Updater_While_Driving");
                driving_Location_update.putExtra("passtoken", rider);
                driving_Location_update.putExtra("drvrtoken", tok);

                startService(driving_Location_update);*/
                /*map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                    @Override
                    public void onMyLocationChange(Location location) {
                        drivingUpdater = new Location_Updater_While_Driving(tok, String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()), rider, "connected");
                        drivingupdtrft = new FutureTask<String>(drivingUpdater);
                        boolean b = drivingupdtrft.cancel(false);
                        drivingupdtrft.run();
                    }
                });*/
            }
        });

        Button reject = (Button) d.findViewById(R.id.bDeclineRequest);
        reject.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //when driver declines request, notify passenger and make his screen back to the original look
                //mDialog.cancel();
                //dia.dismiss();
                d.dismiss();
                isDialogShown = false;
                spRequest.release();
                msg.setText("");
                getdirection.setVisibility(View.GONE);
                cancelrequest.setVisibility(View.GONE);
                //decline passenger's request
                CancelRequest cancelrequest = new CancelRequest();
                String pid = requester;
                String cancelcode = "cancel";
                String did = tok;
                cancelrequest.execute(pid, cancelcode, did);
            }
        });
    }

    public Location getChangedLocation(){
        map.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                mylocation = location;
            }
        });
        return mylocation;
    }

    public Location getCurrentLocation() {
        Location mGPSLocation = null;
        Location mNetworkLocation = null;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10);
        mLocationRequest.setFastestInterval(10);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER);
        //mLocationRequest.setSmallestDisplacement(0.1F);

        //LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, (android.location.LocationListener) listener);
        startReceivingLocationUpdates();
        conn.checkLocationAccessPermission(); //Check if location access has been granted by the user
        mNetworkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        mGPSLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (isBetterLocation(mGPSLocation, mNetworkLocation)) ;
        return mGPSLocation;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    public boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        } else if (currentBestLocation == null && location == null) {
            Toast.makeText(context, "Sorry, no valid network or GPS location found. " +
                    "Please change location and try again.", Toast.LENGTH_SHORT).show();


            // Check whether the new location fix is newer or older
            long timeDelta = location.getTime() - currentBestLocation.getTime();
            boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
            boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
            boolean isNewer = timeDelta > 0;

            // If it's been more than two minutes since the current location, use the new location
            // because the user has likely moved
            if (isSignificantlyNewer) {
                return true;
                // If the new location is more than two minutes older, it must be worse
            } else if (isSignificantlyOlder) {
                return false;
            }

            // Check whether the new location fix is more or less accurate
            int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
            boolean isLessAccurate = accuracyDelta > 0;
            boolean isMoreAccurate = accuracyDelta < 0;
            boolean isSignificantlyLessAccurate = accuracyDelta > 200;

            // Check if the old and new location are from the same provider
            boolean isFromSameProvider = isSameProvider(location.getProvider(),
                    currentBestLocation.getProvider());

            // Determine location quality using a combination of timeliness and accuracy
            if (isMoreAccurate) {
                return true;
            } else if (isNewer && !isLessAccurate) {
                return true;
            } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
                return true;
            }
        }
        return false;
    }

    /** Checks whether two providers are the same */
    public boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    /*void sendUpdatesToPassenger(String passengertoken) {
        //Update passenger with driver location
		*//*mLocationRequest = new LocationRequest();
		mLocationRequest.setInterval(10);
		mLocationRequest.setFastestInterval(10);
		mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);*//*
        try {
            startLocationUpdates();
            Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            if (mLastLocation != null) {
                //mLastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                double driverlatitude = mLastLocation.getLatitude();
                double driverlongitude = mLastLocation.getLongitude();

                String la = String.valueOf(driverlatitude);
                String lo = String.valueOf(driverlongitude);
                Intent serviceIntent = new Intent(this, LocationUpdaterServic.class);
                serviceIntent.putExtra("dlat", la);
                serviceIntent.putExtra("dlon", lo);
                serviceIntent.putExtra("pid", passengertoken);
                startService(serviceIntent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    private boolean checkPlayServices() {
        final int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                Dialog dialog = GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST);
                if (dialog != null) {
                    dialog.show();
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            if (ConnectionResult.SERVICE_INVALID == resultCode) finish();
                        }
                    });
                    return false;
                }
            }
            Toast.makeText(context, getString(R.string.update_googleplay), Toast.LENGTH_LONG).show();
            finish();
            return false;
        }
        return true;
    }


    public String getRegistrationToken() {
        //retrieve token from sharedpreferences
        SharedPreferences prefs = getSharedPreferences(QuickstartPreferences.MY_PREFS_NAME, MODE_PRIVATE);
        String retrievedtoken = prefs.getString("savedtoken", "No name defined");//"No name defined" is the default value.
        return retrievedtoken;
    }


    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }


    //play sound play for passenger request and destination for devices running newer than api 21
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected SoundPool createNewSoundPool(SoundPool soup, int length, int resource) {
        AudioAttributes attributes = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        soup = new SoundPool.Builder()
                .setMaxStreams(length)
                .setAudioAttributes(attributes)
                .build();
        spSoundId = soup.load(this, resource, 1);
        return soup;
    }

    //play sound play for passenger request and destination for devices running older than api 21
    @SuppressWarnings("deprecation")
    protected SoundPool createOldSoundPool(SoundPool sp, int length, int resource) {
        sp = new SoundPool(length, AudioManager.STREAM_MUSIC, 0);
        spSoundId = sp.load(this, resource, 1);
        return sp;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

       /* int id = item.getItemId(); //this is the original code before I added the switch case
        if (id == R.id.action_settings) {
            return true;
        }*/

        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (drlistener.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        super.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(downstreamReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(destinationReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(destinationAddressReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(initial_BroadcastRcvr);
        stopLocationUpdates();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
        //downstream receiver register here
        LocalBroadcastManager.getInstance(this).registerReceiver((downstreamReceiver),
                new IntentFilter(MyGcmListenerService.DOWNSTREAM_MESSAGE));

        LocalBroadcastManager.getInstance(this).registerReceiver((destinationReceiver),
                new IntentFilter(MyGcmListenerService.DESTINATION_MESSAGE));

        LocalBroadcastManager.getInstance(this).registerReceiver((destinationAddressReceiver),
                new IntentFilter(ReverseGeocoderDestinationIntentService.SEND_ACTION));

        LocalBroadcastManager.getInstance(this).registerReceiver((initial_BroadcastRcvr),
                new IntentFilter(MyGcmListenerService.INITIAL_UPDATES));

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && !mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        //downstream receiver register here
        LocalBroadcastManager.getInstance(this).registerReceiver((downstreamReceiver),
                new IntentFilter(MyGcmListenerService.DOWNSTREAM_MESSAGE));
        super.onStart();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            //mGoogleApiClient.stopAutoManage(this);
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    protected void startLocationUpdates() {
        try {
            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);  //throws npe
                mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                mLocationRequest.setNumUpdates(5);
            } else {
                mGoogleApiClient.connect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        } else {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onBackPressed() {
        if (this.mdrawerLayout.isDrawerOpen(GravityCompat.START)) {
            this.mdrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            //this will close and exit the app
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//***Change Here***
            startActivity(intent);
            finish();
            System.exit(0);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arcr) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onConnected(Bundle bun) {
        // TODO Auto-generated method stub
        startReceivingLocationUpdates();
        //startLocationUpdates();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        drlistener.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drlistener.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    //Receive location updates
    public void startReceivingLocationUpdates() {
        if (locationManager == null) {
            locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }

        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(android.location.LocationManager.NETWORK_PROVIDER, 1000, 0F,
                        (android.location.LocationListener) listener);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }

            try {
                locationManager.requestLocationUpdates(android.location.LocationManager.GPS_PROVIDER, 1000, 0F,
                        (android.location.LocationListener) listener);
                //if (listener != null) listener.showGpsOnScreenIndicator(false);
            } catch (SecurityException ex) {
                Log.i(TAG, "fail to request location update, ignore", ex);
            } catch (IllegalArgumentException ex) {
                Log.d(TAG, "provider does not exist " + ex.getMessage());
            }

            Log.d(TAG, "startReceivingLocationUpdates");
        }
    }

    @Override
    public boolean onMyLocationButtonClick() {
        //centeredMap(map.getCameraPosition().target);
        /*Location centerlocation = new Location("");
        centerlocation.setLatitude(map.getCameraPosition().target.latitude);
        centerlocation.setLongitude(map.getCameraPosition().target.longitude);
        draw_current_cab_location(centerlocation);*/
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        draw_current_cab_location(location);
    }

    //the whole content to draw the cab moved here from onlocationchanged
    private void draw_current_cab_location(Location currentlocation) {
        LatLng current = new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude());
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(current, 15);
        map.animateCamera(cameraUpdate);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }

        if(locationManager.getProvider(LocationManager.GPS_PROVIDER).supportsBearing()) {
            Location calculatedLoc = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location older = getCurrentLocation();
            if(older != null && calculatedLoc != null) {
                //this list is to get the earliest possible location to animate bearing of the cab
                ArrayList<Location> updts = new ArrayList<>();
                updts.add(older);
                updts.add(calculatedLoc);

                Collections.sort(updts, new Comparator<Location>() {
                    @Override
                    public int compare(Location lhs, Location rhs) {
                        return lhs.getClass().getName().compareTo(rhs.getClass().getName());
                    }
                });

                float bearing = updts.get(0).bearingTo(currentlocation);
                centeredMap(currentlocation, bearing);
            }
        }
    }

    void centeredMap(final Location locat, float bearng){
        View marker = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.custom_marker_layout, null);
        if (customMarker != null) customMarker.remove();
        customMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(locat.getLatitude(), locat.getLongitude()))
                .title("Title")
                .snippet("Description")
                .anchor(0.5f, 0.5f)
                .flat(true)
                .rotation(bearng)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.movingcab)));
        /*customMarker.setPosition(new LatLng(locat.getLatitude(), locat.getLongitude()));
        customMarker.setTitle("Title");
        customMarker.setSnippet("Description");
        customMarker.setAnchor(0.5f, 0.5f);
        customMarker.setFlat(true);
        customMarker.setRotation(bearng);*/
        //cabAnimation.animateMarker(locat, customMarker);
        List<LatLng> ltlngList = new ArrayList<>();
        LatLng intpltrltlng = new LatLng(locat.getLatitude(), locat.getLongitude());
        ltlngList.add(intpltrltlng);
        if (ltlngList.size() == 5) {
            LatLngInterpolator lngitpltr = new LatLngInterpolator.LinearFixed();
            cabAnimation.animateMarker(locat, customMarker);
            //cabAnimation.animateMarkerToGB(customMarker, ltlngList, lngitpltr);
        }
                //.icon(BitmapDescriptorFactory.fromBitmap(createDrawableFromView(this, marker))));
    }

    // Convert a view to bitmap
    public static Bitmap createDrawableFromView(Context context, View view) {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }


	//Convert passenger latlng to valid address
    public void fetchAddressButtonHandler(View view, Location location) {
		// Only start the service to fetch the address if GoogleApiClient is
		// connected.
		if (mGoogleApiClient != null && mGoogleApiClient.isConnected() && location != null) {
			myloc = LocationServices.FusedLocationApi
					.getLastLocation(mGoogleApiClient);
            startIntentService(location);
		}
		// If GoogleApiClient isn't connected, process the user's request by
		// setting mAddressRequested to true. Later, when GoogleApiClient
		// connects,
		// launch the service to fetch the address. As far as the user is
		// concerned, pressing the Fetch Address button
		// immediately kicks off the process of getting the address.
		mAddressRequested = true;
		updateUIWidgets();
	}

	private void updateUIWidgets() {
		// TODO Auto-generated method stub

	}

    
    protected void startIntentService(Location lo) {
		Intent myintent = new Intent(this, FetchAddressIntentService.class);
		myintent.putExtra(Constants.RECEIVER, mAddressResultReceiver);
		myintent.putExtra(Constants.LOCATION_DATA_EXTRA, lo);
		startService(myintent);
	}

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + server_Key);
            sb.append("&components=country:"+Locale.getDefault().getCountry()); //replaced us by Locale.getDefault() so it pulls local addresses of user
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException mue) {
            //Error processing Places API URL
            Log.e("TAG", mue.toString());
            mue.printStackTrace();
            return resultList;
        } catch (IOException ioe) {
            //Error cannecting to Places API
            Log.e("TAG", ioe.toString());
            ioe.printStackTrace();
            return resultList;
        } catch (Exception ex){
            ex.printStackTrace();
            Log.e("TAG", ex.toString());
        }finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException je) {
            //cannot process JSON results
            Log.e("TAG", je.toString());
            je.printStackTrace();
        } catch (Exception exe){
            Log.e("TAG", exe.toString());
            exe.printStackTrace();
        }
        return resultList;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bsignout:
                Toast.makeText(getApplicationContext(), "signed out", Toast.LENGTH_LONG).show();
                for (int i = 0; i < passengerList.size(); i++) {
                    if (!passengerList.get(i).token.equals(null)) {
                        //make driver unavailable for other passengers
                        isDriverAvailable = false;
                        isUpdatingDB = false;
                        isSendingUpdates = false;
                        DriverUnavailable done = new DriverUnavailable();
                        done.execute(passengerList.get(i).token, tok, "unavailable");
                    }
                }
                signout.setVisibility(View.GONE);
                signin.setVisibility(View.VISIBLE);
                break;
            case R.id.bsignin:
                Toast.makeText(getApplicationContext(), "you are now signed in", Toast.LENGTH_LONG).show();
                for (int x = 0; x < passengerList.size(); x++) {
                    if (!passengerList.get(x).token.equals(null)) {
                        //make driver available
                        isDriverAvailable = true;
                        isUpdatingDB = true;
                        isSendingUpdates = true;
                        Location lction = getChangedLocation();
                        DriverAvailable available = new DriverAvailable();
                        available.execute(passengerList.get(x).token, tok, String.valueOf(lction.getLatitude()), String.valueOf(lction.getLongitude()));

                        i_am_back_online(passengerList.get(x).token, tok);
                    }
                }
                signin.setVisibility(View.GONE);
                signout.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void i_am_back_online(String passenger, String driver) {
        new AsyncTask() {
            @Override
            protected String doInBackground(Object[] params) {
                String msg = "";
                try {
                    Bundle data = new Bundle();
                    data.putString("my_message", "Hello World");
                    data.putString("my_action","SAY_HELLO");
                    String id = Integer.toString(msgId.incrementAndGet());
                    gcm.send(PASSENGER_SENDER_ID + "@gcm.googleapis.com", id, data);
                    msg = "Sent message";
                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }
        }.execute(null, null, null);
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {

        private ArrayList resultList;
        //private ArrayList<HashMap<String, Place>> results = new ArrayList<>();

        public GooglePlacesAutocompleteAdapter(Context context, int list, int textViewResourceId) {

            super(context, list, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return resultList.get(index).toString();
        }

        //@Override
        //public HashMap<String, Place>  getItem(int index) {return results.get(index);}

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());
                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

    //this receiver is registered in the manifest so it can receive messages even when the activity/app is in background
    public static class cancellationreceiver extends BroadcastReceiver{

        public cancellationreceiver(){};
        @Override
        public void onReceive(Context context, Intent intent) {
            String text = intent.getStringExtra(MyGcmListenerService.CANCELLED);
            String[] separated = text.split(",");
            String code = separated[0].trim();
            String passenger = separated[1].trim();
            if (code != null && !code.isEmpty()) {
                //user has cancelled so update UI
                spCancelled.play(spSoundId, 1, 1.5f, 1, 2, 1);
                msg.setText("");
                Toast.makeText(context, "Sorry, the passenger has cancelled your request.", Toast.LENGTH_SHORT).show();
                getdirection.setVisibility(View.GONE);
                cancelrequest.setVisibility(View.GONE);
                todestination.setVisibility(View.GONE);
                destinationText.setVisibility(View.INVISIBLE);


                Intent bringmyapptofront = new Intent(context, MapActivity.class);
                bringmyapptofront.addCategory(Intent.CATEGORY_LAUNCHER);
                bringmyapptofront.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(bringmyapptofront);

            }
        }
    }

    @SuppressLint("ParcelCreator") //should delete this line and solve the CREATOR field crap before production
    public class AddressResultReceiver extends ResultReceiver {

		ResultReceiver mReceiver;

		public AddressResultReceiver(Handler handler) {
			super(handler);
		}

		public void setReceiver(ResultReceiver receiver) {
			mReceiver = receiver;
		}

		@Override
		protected void onReceiveResult(int resultCode, Bundle resultData) {
			// mReceiver.onReceiveResult(resultCode, resultData);
			if (resultCode == Constants.FAILURE_RESULT) {
				// Toast.makeText(getApplicationContext(),
				// "ADDRESS LOOKUP UNSUCCESSFUL", Toast.LENGTH_SHORT).show();
			} else if (resultCode == Constants.SUCCESS_RESULT) {
				// Display the address string
				// or an error message sent from the intent service
				final String mAddressOutput = resultData
						.getString(Constants.RESULT_DATA_KEY);
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						if (mAddressOutput != null)
							MapActivity.showaddress(mAddressOutput);
					}
				});
				// Show a toast message if an address was found.
				if (resultCode == Constants.SUCCESS_RESULT) {
					// Toast.makeText(getApplicationContext(),
					// "SUCCESS",Toast.LENGTH_SHORT).show();
				}
			}
		}
	}
    protected static void showaddress(String mAddressOutput) {
		// TODO Auto-generated method stub
		msg.setText(String.valueOf(mAddressOutput));
	}

    /*public class sendUpdates extends AsyncTask<String, Void, Void>{

        @Override
        public Void doInBackground(String... params) {
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
                    json.put("driver", params[0]);
                    json.put("drlat", params[1]);
                    json.put("drlon", params[2]);
                    json.put("passenger", params[3]);
                    json.put("isConnected", params[4]);

                    String postData = json.toString();
                    Log.e("the whole shit %$#", postData);
                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    Log.e("NOTIFICATION", "Cab initial ma.senUpdates line 1373 location Sent");
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
                Log.e("ERROR", "There is error in this code ");
            }
            return null;
        }
    }*/

    //This is updating database continuously like it is supposed to
    public void updateDB(final String Dtoken, final String latitude, final String longitude){
        JSONObject obj = new JSONObject();
        try {
            obj.put("drtoken", Dtoken);
            obj.put("drlat", latitude);
            obj.put("drlon", longitude);
            Log.e("JSONOBJECT", String.valueOf(obj));
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSONERR ma1521", String.valueOf(e));
        }

        RequestQueue queue = Volley.newRequestQueue(this);

        String url = remote + "driver.php";

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("JSONRes ma1531", String.valueOf(response));
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSONERR ma1537", String.valueOf(error));
            }
        });
        if(isUpdatingDB == true) {
            queue.add(jsObjRequest);
        }else{
            queue.cancelAll(this);
        }
    }

    //send location updates to the requesting passenger
    public void initialContupdts(String drvr, String ltd, String lng, String psgr, String dstnFl, String connected, final String arrival){
        JSONObject obj = new JSONObject();
        String requestTAG = "rTAG"; //a tag to handle this request and cancel it as needed
        try {
            obj.put("drtoken", drvr);
            obj.put("drlat", ltd);
            obj.put("drlon", lng);
            obj.put("passenger", psgr);
            obj.put("isConnected", connected);
            obj.put("eta", arrival);
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("JSONERR ma1559", arrival);
        }

        RequestQueue queue = Volley.newRequestQueue(this);
        if(connected.equals("disconnected") || !isSendingUpdates){ //cancel the request
            queue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, dstnFl, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("JSONRes ma1575", arrival + String.valueOf(response));
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSONERR ma1581", String.valueOf(error));
            }
        });
        jsObjRequest.setTag(requestTAG); //set tag to this particular request
        if(isSendingUpdates) {
            queue.add(jsObjRequest);
        }else{
            queue.cancelAll(requestTAG);
        }
    }

    //send location updates to the requesting passenger
    public void getDrivingDuration(final String dstnfile, final String connectOrNot, String url){
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsObjRequest = new JsonObjectRequest(Request.Method.POST, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String  j = response.getString("rows");
                    JSONArray rowa = new JSONArray(j);
                    JSONObject rowobj = rowa.getJSONObject(0);
                    JSONArray elementsa = rowobj.getJSONArray("elements");
                    for (int i = 0; i < elementsa.length(); i++) {
                        JSONObject currentObject = elementsa.getJSONObject(i);
                        String duration = currentObject.getString("duration_in_traffic");
                        JSONObject timeObject = new JSONObject(duration);
                        String eta = timeObject.getString("text");
                        sendContinousUpdates(dstnfile, connectOrNot, eta);
                        Log.e("JSONRes ma1611", eta);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("JSONERR ma1621", String.valueOf(error));
            }
        });
        queue.add(jsObjRequest);
    }

    private void sendContinousUpdates(String destinationFile, String isconnected, String timearrival){
        //send driver location updates until user requests different driver or driver gets request
        changedL = getChangedLocation();
        if(changedL != null){
        initialContupdts(tok, String.valueOf(changedL.getLatitude()), String.valueOf(changedL.getLongitude()), ipassenger, destinationFile, isconnected, timearrival);
        }
    }


  //async class to send the registration id.
  	/*class PostData extends AsyncTask<String, Void, Void> {
      HttpURLConnection urlConn;
  		@Override
  		protected Void doInBackground(String... args) {
  			try {
                while (!isCancelled()) {
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
                            json.put("drtoken", args[0]);
                            json.put("drlat", args[1]);
                            json.put("drlon", args[2]);
                            String postData = json.toString();

                            // Send POST output.
                            OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                            os.write(postData);
                            Log.i("NOTIFICATION", "Data Sent");

                            if (args[3] != "connect") {
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
                }
  	        } catch (Exception e) {
  	            e.printStackTrace();
  	            Log.e("ERROR", "There is error in this code " + String.valueOf(e));
            }
  			return null;
  		}
  	}*/

    //async class to send the cancel code.
    class CancelRequest extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                try {
                    URL updateurl;
                    HttpURLConnection urlConn;
                    updateurl = new URL (remote+"cancelbydriver.php");
                    urlConn = (HttpURLConnection)updateurl.openConnection();
                    urlConn.setDoInput(true);
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches (false);
                    urlConn.setRequestProperty("Content-Type","application/json");
                    urlConn.setRequestProperty("Accept", "application/json");
                    //urlConn.setChunkedStreamingMode(0);
                    urlConn.setRequestMethod("POST");
                    urlConn.connect();

                    //Create JSONObject here
                    JSONObject json = new JSONObject();
                    json.put("passengtok", args[0]);
                    json.put("cancelReq", args[1]);
                    json.put("dritok", args[2]);

                    String postData=json.toString();

                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String msg="";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        msg += line; }
                    Log.e("msg=",""+msg);
                } catch (MalformedURLException muex) {
                    // TODO Auto-generated catch block
                    muex.printStackTrace();
                } catch (IOException ioex){
                    ioex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR", "Problem sending cancel cancelreq line 1417");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }
    }

    //to cancel availability of driver after driver accepts a request from different passenger or sign out of the app.
    class DriverUnavailable extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                try {
                    URL unavailableUrl;
                    HttpURLConnection urlConn;
                    unavailableUrl = new URL (remote+"driver_unavailable.php");
                    urlConn = (HttpURLConnection)unavailableUrl.openConnection();
                    //urlConn.setDoInput(true);
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches (false);
                    urlConn.setRequestProperty("Content-Type","application/json");
                    urlConn.setRequestProperty("Accept", "application/json");
                    urlConn.setRequestMethod("POST");
                    urlConn.connect();

                    //Create JSONObject here
                    JSONObject json = new JSONObject();
                    json.put("passengtok", args[0]);
                    json.put("dritok", args[1]);
                    json.put("availability", args[2]);

                    String postData=json.toString();

                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String msg="";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        msg += line; }
                    Log.e("msg=",""+msg);
                } catch (MalformedURLException muex) {
                    // TODO Auto-generated catch block
                    muex.printStackTrace();
                } catch (IOException ioex){
                    ioex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR ma1821", "Problem sending unavailable");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }

    class DriverAvailable extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... args) {
            try {
                try {
                    URL unavailableUrl;
                    HttpURLConnection urlConn;
                    unavailableUrl = new URL (remote+"sendupdates.php");
                    urlConn = (HttpURLConnection)unavailableUrl.openConnection();
                    //urlConn.setDoInput(true);
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches (false);
                    urlConn.setRequestProperty("Content-Type","application/json");
                    urlConn.setRequestProperty("Accept", "application/json");
                    urlConn.setRequestMethod("POST");
                    urlConn.connect();

                    //Create JSONObject here
                    JSONObject json = new JSONObject();
                    json.put("driver", args[0]);
                    json.put("drlat", args[1]);
                    json.put("drlon", args[2]);
                    json.put("passenger", args[3]);

                    String postData=json.toString();

                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    os.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    String msg="";
                    String line = "";
                    while ((line = reader.readLine()) != null) {
                        msg += line; }
                    Log.e("msg=",""+msg);
                } catch (MalformedURLException muex) {
                    // TODO Auto-generated catch block
                    muex.printStackTrace();
                } catch (IOException ioex){
                    ioex.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ERROR ma1821", "Problem sending unavailable");
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

        }
    }
	/*public class MyInstanceIDListenerService extends InstanceIDListenerService {
		*//*public MyInstanceIDListenerService(){
            super();
        }*//*
		public String getToken() {
			InstanceID instanceID = InstanceID.getInstance(this);
			String token = null;
			try {
				token = instanceID.getToken(getString(R.string.gcm_defaultSenderId),
						GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
			return token;
		}
		@Override
		public void onTokenRefresh() {
			// Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
			Intent intent = new Intent(this, RegistrationIntentService.class);
			startService(intent);

		}
	}*/

	public static class MyGcmListenerService extends GcmListenerService {

		public static final String DOWNSTREAM_MESSAGE = "com.asmarainnovations.taxidriver.dtMessage";
		public static final String CANCEL = "com.asmarainnovations.taxidriver.cancelMessage";
		public static final String CANCELLED = "";
		public static final String DESTINATION_MESSAGE = "com.asmarainnovations.taxidriver.destinationMessage";
        public static final String INITIAL_UPDATES = "com.asmarainnovations.taxidriver.initial_updates";
		LocalBroadcastManager broadcaster,cancelNotifier, destinationBroadcaster, initial_Updater;

		public MyGcmListenerService() {
			super();
		}

		@Override
		public void onCreate() {
			//this will broadcast downstream messages to update the UI
			broadcaster = LocalBroadcastManager.getInstance(this);
			cancelNotifier = LocalBroadcastManager.getInstance(this);
			destinationBroadcaster = LocalBroadcastManager.getInstance(this);
            initial_Updater = LocalBroadcastManager.getInstance(this);
			super.onCreate();
		}

		@Override
		public void onMessageSent(String msgId) {
			super.onMessageSent(msgId);
            Log.e("sentNotification", msgId);
		}

		@Override
		public void onDeletedMessages() {
			super.onDeletedMessages();
		}

		@Override
		public void onSendError(String msgId, String error) {
			super.onSendError(msgId, error);
            Log.e("smgsendingErr", msgId +  error);
		}

		@Override
		public void onMessageReceived(String from, Bundle data) {

				if (data != null && !data.isEmpty()) {//this is for the initial updates before any requests by the user
                    if ( data.containsKey("ipaid") && data.containsKey("ipala") && data.containsKey("ipalo")){
                        String passenger = data.getString("ipaid") + "," + data.getString("ipala") + "," + data.getString("ipalo");

                        Intent initial_updatr = new Intent();
                        initial_updatr.putExtra(INITIAL_UPDATES, passenger);
                        initial_updatr.setAction(INITIAL_UPDATES);
                        initial_Updater.sendBroadcast(initial_updatr);
                    }
					else if(data.containsKey("lati") && data.containsKey("longi") && data.containsKey("paid")) {
						String message = data.getString("lati") + "," + data.getString("longi") + "," + data.getString("paid");
                        Intent mIntent = new Intent();
						mIntent.putExtra(DOWNSTREAM_MESSAGE, message);
						mIntent.setAction(DOWNSTREAM_MESSAGE); //should match the receiver intent filter at the registering
						broadcaster.sendBroadcast(mIntent);
					}else if (data.containsKey("cancelByPassenger") && data.containsKey("passen")){
						String cancelCoandPassen = data.getString("cancelByPassenger") + ", " + data.getString("passen");
                        Intent cancelInt = new Intent();
						cancelInt.putExtra(CANCELLED, cancelCoandPassen);
						cancelInt.setAction("com.asmarainnovations.MapActivity.MyGcmListenerService.CANCEL");
						sendBroadcast(cancelInt);
					}else if(data.containsKey("dstlat") && data.containsKey("dstlon") && data.containsKey("paid") && data.containsKey("dest")){
						String destring = data.getString("dstlat") + ", " + data.getString("dstlon") + ", " + data.getString("paid")+ ", " + data.getString("dest");
                        Intent destIntent = new Intent();
						destIntent.putExtra(DESTINATION_MESSAGE, destring);
						destIntent.setAction(DESTINATION_MESSAGE); //should match the receiver intent filter at the registering
						destinationBroadcaster.sendBroadcast(destIntent);
					}
				} else {
					Log.e("Received", "empty message");
				}
		}
	}
}
