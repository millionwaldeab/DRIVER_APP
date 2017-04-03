package com.asmarainnovations.taxidriver;

import com.google.android.gms.maps.model.LatLng;
import static java.lang.Math.asin;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.pow;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;
/* Copyright 2013 Google Inc.
   Licensed under Apache 2.0: http://www.apache.org/licenses/LICENSE-2.0.html */
public interface LatLngInterpolator {
	public LatLng interpolate(float fraction, LatLng a, LatLng b);

	public class Linear implements LatLngInterpolator
	{
		@Override
		public LatLng interpolate(float fraction, LatLng a, LatLng b)
		{
			double lat = (b.latitude - a.latitude) * fraction + a.latitude;
			double lng = (b.longitude - a.longitude) * fraction + a.longitude;
			return new LatLng(lat, lng);
		}
	}
	public class LinearFixed implements LatLngInterpolator
	{
		@Override
		public LatLng interpolate(float fraction, LatLng a, LatLng b) {
			double lat = (b.latitude - a.latitude) * fraction + a.latitude;
			double lngDelta = b.longitude - a.longitude;
			// Take the shortest path across the 180th meridian.
			if (Math.abs(lngDelta) > 180) {
				lngDelta -= Math.signum(lngDelta) * 360;
			}
			double lng = lngDelta * fraction + a.longitude;
			return new LatLng(lat, lng);
		}
	}
}

