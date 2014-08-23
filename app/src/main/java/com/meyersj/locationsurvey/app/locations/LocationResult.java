package com.meyersj.locationsurvey.app.locations;

import com.mapbox.mapboxsdk.geometry.LatLng;

/**
 * Created by meyersj on 7/11/2014.
 */
public class LocationResult {

    protected String address;
    protected LatLng latLng;
    protected Double score;

    public LocationResult() {
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(String lat, String lon) {
        this.latLng = new LatLng(Double.valueOf(lat), Double.valueOf(lon));
    }

    public void setLatLng(Double lat, Double lon) {
        this.latLng = new LatLng(lat, lon);
    }

    public Double getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = Double.valueOf(score);
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public boolean isValid() {
        if (address != null && latLng != null && score != null) {
            return true;
        } else {
            return false;
        }
    }




}
