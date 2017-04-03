package com.asmarainnovations.taxidriver;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Million on 8/3/2016.
 */
public class VerificationCode extends Activity {
    EditText codeedittext;
    Button verificationSender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.verificationtextmessage);
        codeedittext = (EditText) findViewById(R.id.etVerificationCode);
        verificationSender = (Button) findViewById(R.id.bsend_verification_code);
        verificationSender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = codeedittext.getText().toString().trim();
                Login loginActivity = new Login();
                if (code != null && loginActivity.codeString != null && code == loginActivity.codeString){
                    //save credentials in shared pref
                    SharedPreferences sharedPref = getSharedPreferences("taxi", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    //this stores a string to tell that the user is already signed up
                    editor.putString("register","true");
                    editor.commit();
                    //the phone verification passed
                    Intent passed = new Intent(VerificationCode.this, Login.class);
                    startActivity(passed);
                }else{
                    codeedittext.setError("Oops invalid code, please enter valid code");
                }
            }
        });
    }
}
