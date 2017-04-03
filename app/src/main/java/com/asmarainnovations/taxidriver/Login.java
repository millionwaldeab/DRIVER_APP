/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
package com.asmarainnovations.taxidriver;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.google.android.gms.iid.InstanceIDListenerService;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * Created by Million on 9/8/2015.
 */
public class Login extends Activity implements View.OnClickListener {
    EditText name, tele, company, cab_number, lice_expiry, fare;
    Spinner cab_type;
    Button signup, buttonChoose, buttonUpload;
    String namestring, telestring, compstring, cabNostring, expirystring, typestring, fileName, farestring, codeString;
    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;
    int number;
    ProgressDialog prgDialog;
    Bitmap bitmap;
    private int PICK_IMAGE_REQUEST = 1;
    final String local = Config.local;
    final String remote = Config.remote;
    private String UPLOAD_URL =remote+"business_permit_upload.php";
    private ImageView imageView;
    TextView terms;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";
    RegistrationResponseReceiver receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        name = (EditText) findViewById(R.id.etname);
        tele = (EditText) findViewById(R.id.ettel);
        company = (EditText) findViewById(R.id.etCompany);
        cab_number = (EditText) findViewById(R.id.etTaxiNumber);
        lice_expiry = (EditText) findViewById(R.id.etLicenseExpiry);
        fare = (EditText) findViewById(R.id.etRates);
        signup = (Button) findViewById(R.id.bsignup);
        terms = (TextView) findViewById(R.id.tvterms);
        buttonChoose = (Button) findViewById(R.id.buttonLoadPicture);
        buttonUpload = (Button) findViewById(R.id.bUploadLicense);
        cab_type = (Spinner) findViewById(R.id.spType);
        imageView = (ImageView) findViewById(R.id.imgView);
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        terms.setOnClickListener(this);
        receiver = new RegistrationResponseReceiver();
        prgDialog = new ProgressDialog(this);
        sharedPref = getSharedPreferences("taxidriver", MODE_PRIVATE);
        editor = sharedPref.edit();
        // Set Cancelable as False
        prgDialog.setCancelable(false);
        //get registration id
        Intent intent = new Intent(this, RegistrationIntentService.class);
        startService(intent);

        String getStatus = sharedPref.getString("register", "nil");
        //they have already registered
        if(getStatus.equals("true")) {
            //Open this Home activity
            Intent passed = new Intent(Login.this, MainActivity.class);
            startActivity(passed);
        }else{
            sendVerificationCode();
            Intent enterCode = new Intent(Login.this, VerificationCode.class);
            startActivity(enterCode);
        }
    }

    //convert image to string to stream it
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    //display the image and load from gallery
    private void showFileChooser() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            /*Intent intent = new Intent();
            intent.setType("image*//*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);*/
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("TAG", e.toString());
        }
    }


    protected void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        if(null != s) {
                            Log.e("main155", s);
                        }else{
                            Log.e("main 157", s +  "is null");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        if(null != volleyError.toString()) {
                            volleyError.printStackTrace();
                            //Log.e("main164", volleyError.getMessage().toString());
                        }else{
                            Log.e("main166", "volleyerror is null");
                        }
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Getting Image Name
                String name = getString(R.string.driver_business_permit);

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name+".jpg");

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }


    // When Image is selected from Gallery
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /*if (requestCode == RESULT_LOAD_IMG && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            String picturePath = cursor.getString(columnIndex);
            cursor.close();

            String fileNameSegments[] = picturePath.split("/");
            fileName = fileNameSegments[fileNameSegments.length - 1];

            Bitmap myImg = BitmapFactory.decodeFile(picturePath);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            // Must compress the Image to reduce image size to make upload easy
            myImg.compress(Bitmap.CompressFormat.PNG, 50, stream);
            byte[] byte_arr = stream.toByteArray();
            // Encode Image to String
            encodedString = Base64.encodeToString(byte_arr, 0);
        uploadImage();
        }*/
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                //Setting the Bitmap to ImageView
                imageView.setImageBitmap(bitmap);

                fileName = filePath.getLastPathSegment();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.buttonLoadPicture:
                //showFileChooser();
               showFileChooser();
                break;

            case R.id.bUploadLicense:
                uploadImage();
                break;

            case R.id.tvterms:
                Intent termsint = new Intent(Login.this, Legal.class);
                startActivity(termsint);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(RegistrationResponseReceiver.ACTION_RESP);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(null != prgDialog){
            prgDialog.dismiss();
        }
        super.onDestroy();
    }

    //this will send a secret 5 digit code to the phone number provided
    private void sendVerificationCode() {
        int min = 00000, max = 99999;
        SecureRandom random = new SecureRandom();
        int generatedNumber = random.nextInt(max - min + 1) + min;
        codeString = String.valueOf(generatedNumber);
        String message = "This is your secret code, please enter it in the app now" +  codeString;
        try {
            SmsManager sms = SmsManager.getDefault();
            sms.sendTextMessage(tele.getText().toString().trim(), null, message, null, null);
        } catch (IllegalArgumentException illexc) {
            illexc.printStackTrace();
            Log.e("Exception la314", illexc.toString());
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    public class RegistrationResponseReceiver extends BroadcastReceiver {
        static final String ACTION_RESP = "com.asmarainnovations.taxidriver.intent.action.MESSAGE_PROCESSED";
        double latitude, longitude;
        public RegistrationResponseReceiver(){
            //super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            final String receivedtoid = intent.getStringExtra(RegistrationIntentService.PARAM_OUT);
            final GlobalValidator validator = new GlobalValidator();

            signup.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ((receivedtoid != null) && validator.isPersonName(name, true) && validator.isPhoneNumber(tele, true)
                            && validator.isAlphaNumeric(company, true) && validator.isAlphaNumeric(cab_number, true)
                            && validator.isDate(lice_expiry, true) && validator.isAlphaNumeric(fare, true)) {
                        namestring = name.getText().toString();
                        telestring = tele.getText().toString();
                        compstring = company.getText().toString();
                        cabNostring = cab_number.getText().toString();
                        expirystring = lice_expiry.getText().toString();
                        typestring = cab_type.getSelectedItem().toString();
                        farestring = fare.getText().toString();

                        //Convert android datetime to mysql datetime format
                        // Parse the input date
                        SimpleDateFormat fmt = new SimpleDateFormat("MM-dd-yyyy");
                        Date inputDate = null;
                        try {
                            inputDate = fmt.parse(expirystring);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }

                        //check if the date entered has already expired
                        if(is_Permit_Expired(inputDate)){
                            lice_expiry.setError("Your permit has expired!!!");
                            return; //exit because the permit expires today or has already expired
                        }else {

                            // Create the MySQL datetime string
                            fmt = new SimpleDateFormat("yyyy-MM-dd");
                            String mysql_date = fmt.format(inputDate);


                            SendLoginCredentials sendcreds = new SendLoginCredentials();
                            sendcreds.execute(receivedtoid, namestring, telestring, compstring, cabNostring, mysql_date, typestring, farestring);
                        }

                        editor.putString("register","true");
                        editor.commit();
                            //Open this Home activity
                            Intent intent = new Intent(Login.this, MainActivity.class);
                            startActivity(intent);


                    } else {
                        Log.i("credentials!!!", "caution: empty credentials");
                        Toast.makeText(getApplicationContext(), "complete all boxes!!!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            });

        }

        private boolean is_Permit_Expired(Date permitExpiry){
            Calendar c = Calendar.getInstance();

        // set the calendar to start of today
            c.set(Calendar.HOUR_OF_DAY, 0);
            c.set(Calendar.MINUTE, 0);
            c.set(Calendar.SECOND, 0);
            c.set(Calendar.MILLISECOND, 0);

        // and get that as a Date
            long todayInMillis = c.getTimeInMillis();

        // and get that as a Date
            long dateSpecified = permitExpiry.getTime();
            if(dateSpecified > todayInMillis){
                return false;
            }
            return true;
        }

    }


    public class MyInstanceIDListenerService extends InstanceIDListenerService {
        public MyInstanceIDListenerService(){ super();}
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
    }


    //async class to send credentials.
    class SendLoginCredentials extends AsyncTask<String, Void, String> {

        public SendLoginCredentials(){
            super();
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                try {
                    URL url;
                    HttpURLConnection urlConn;
                    url = new URL (remote+"driver_register.php");
                    urlConn = (HttpURLConnection)url.openConnection();
                    //urlConn.setDoInput (true); //this is for get request
                    urlConn.setDoOutput (true);
                    urlConn.setUseCaches (false);
                    urlConn.setRequestProperty("Content-Type","application/json");
                    urlConn.setRequestProperty("Accept", "application/json");
                    //urlConn.setChunkedStreamingMode(0);
                    urlConn.setRequestMethod("POST");
                    urlConn.connect();
                    //get google account
                    AccountManager am = AccountManager.get(getBaseContext()); // "this" references the current Context
                    Account[] accounts = am.getAccountsByType("com.google");

                    //Create JSONObject here
                    JSONObject json = new JSONObject();
                    json.put("drtoken", String.valueOf(args[0]));
                    json.put("name", String.valueOf(args[1]));
                    json.put("tele", String.valueOf(args[2]));
                    json.put("Google_account", accounts[0].name);
                    json.put("company", String.valueOf(args[3]));
                    json.put("cabno", String.valueOf(args[4]));
                    json.put("expiration", String.valueOf(args[5]));
                    json.put("type", String.valueOf(args[6]));
                    json.put("fare", String.valueOf(args[7]));

                    String postData=json.toString();

                    // Send POST output.
                    OutputStreamWriter os = new OutputStreamWriter(urlConn.getOutputStream(), "UTF-8");
                    os.write(postData);
                    Log.i("NOTIFICATION", "Data Sent");
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
                Log.e("ERROR", "There is error in this code loginline 160");
            }
            return null;
        }
    }
}
