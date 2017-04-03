package com.asmarainnovations.taxidriver;

import android.content.Context;
import android.os.CountDownTimer;
import android.widget.Toast;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 12/11/2015.
 */

/**
 * This is a custom class for any repittive methods, variables with a global scope and stuff like that.
 */

public class UtilityClass {
    Context mContext;

    public UtilityClass(Context c){
        this.mContext = c;
    }

    //a method to show toast message for as long as needed
    public void showToast(int duration, String customMessage) {
        final Toast toast = Toast.makeText(mContext, customMessage, Toast.LENGTH_SHORT);
        toast.show();
        new CountDownTimer(duration, 500) {
            public void onTick(long millisUntilFinished) {
                toast.show();
            }
            public void onFinish() {
                toast.cancel();
            }

        }.start();
    }
}
