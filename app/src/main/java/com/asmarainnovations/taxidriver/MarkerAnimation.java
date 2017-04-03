package com.asmarainnovations.taxidriver;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Property;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */

/**
 * Created by Million on 2/11/2016.
 */
public class MarkerAnimation {

	public void animateMarker(final Location location, final Marker myMarkerLOC) {
		if (myMarkerLOC != null) {
			final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
			ValueAnimator valueAnimator = new ValueAnimator();
			final LatLng startPosition = myMarkerLOC.getPosition();
			final float startRotation = myMarkerLOC.getRotation();
			final float angle = 180 - Math.abs(Math.abs(startRotation - location.getBearing()) - 180);
			final float right = WhichWayToTurn(startRotation, location.getBearing());
			valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
				@Override
				public void onAnimationUpdate(ValueAnimator animation)
				{
					try {
						if (myMarkerLOC == null) // oops... destroying map during animation...
						{
							return;
						}
						float v = animation.getAnimatedFraction();
						LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, PositionUtil(location));
						float rotation = startRotation + right * v * angle;
						myMarkerLOC.setRotation((float) rotation);
						myMarkerLOC.setPosition(newPosition);
					} catch (Exception ex) {
						// I don't care atm..
					}
				}

				private LatLng PositionUtil(Location location) {
					LatLng a = new LatLng(location.getLatitude(), location.getLongitude());
					return a;
				}
			});
			valueAnimator.setFloatValues(0, 1);
			valueAnimator.setDuration(300);
			valueAnimator.start();
		}
	}
	private float WhichWayToTurn(float currentDirection, float targetDirection)
	{
		float diff = targetDirection - currentDirection;
		if (Math.abs(diff) == 0)
		{
			return 0;
		}
		if(diff > 180)
		{
			return -1;
		}
		else
		{
			return 1;
		}
	}

	//I changed the type of finalPosition from LatLng to List<LatLng>
	static void animateMarkerToGB(final Marker marker, final List<LatLng> finalPosition, final LatLngInterpolator latLngInterpolator) {
		final LatLng startPosition = marker.getPosition();
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		final Interpolator interpolator = new AccelerateDecelerateInterpolator();
		final float durationInMs = 3000;

		handler.post(new Runnable() {
			long elapsed;
			float t;
			float v;

			@Override
			public void run() {
				// Calculate progress using interpolator
				elapsed = SystemClock.uptimeMillis() - start;
				t = elapsed / durationInMs;
				v = interpolator.getInterpolation(t);
				for (int indx = 0; indx < finalPosition.size(); indx++) {
					marker.setPosition(latLngInterpolator.interpolate(v, startPosition, finalPosition.get(indx)));
				}
				// Repeat till progress is complete.
				if (t < 1) {
					// Post again 16ms later.
					handler.postDelayed(this, 16);
				}
			}
		});
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	static void animateMarkerToHC(final Marker marker, final LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
		final LatLng startPosition = marker.getPosition();

		ValueAnimator valueAnimator = new ValueAnimator();
		valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				float v = animation.getAnimatedFraction();
				LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, finalPosition);
				marker.setPosition(newPosition);
			}
		});
		valueAnimator.setFloatValues(0, 1); // Ignored.
		valueAnimator.setDuration(3000);
		valueAnimator.start();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	static void animateMarkerToICS(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
		TypeEvaluator<LatLng> typeEvaluator = new TypeEvaluator<LatLng>() {
			@Override
			public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
				return latLngInterpolator.interpolate(fraction, startValue, endValue);
			}
		};
		Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
		ObjectAnimator animator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
		animator.setDuration(3000);
		animator.start();
	}
}
