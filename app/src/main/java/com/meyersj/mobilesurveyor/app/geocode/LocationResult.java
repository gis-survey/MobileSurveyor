package com.meyersj.mobilesurveyor.app.geocode;

import com.mapbox.mapboxsdk.geometry.LatLng;


public class LocationResult {

    public final String TAG = getClass().getCanonicalName();

    protected String text;
    protected LatLng latLng;

    public LocationResult(String text, String lat, String lon) {
        this.text = text;
        if (!lat.isEmpty() && !lon.isEmpty()) {
            this.latLng = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
        }
    }

    @Override
    public String toString() {
        return text;
    }

    public LatLng getLatLng() {
        return latLng;
    }


}
