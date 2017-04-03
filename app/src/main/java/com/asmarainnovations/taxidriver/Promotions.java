package com.asmarainnovations.taxidriver;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
/**
 * Created by Million on 9/29/2015.
 */
public class Promotions extends Activity {
    TextView no_promo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.promotions);
        no_promo = (TextView) findViewById(R.id.tvnoPromo);
        no_promo.setText(R.string.no_promotions);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}
