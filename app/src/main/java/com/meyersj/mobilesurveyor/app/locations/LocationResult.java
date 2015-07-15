/*
 * Copyright © 2015 Jeffrey Meyers
 * This program is released under the "MIT License".
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

package com.meyersj.mobilesurveyor.app.locations;

import com.mapbox.mapboxsdk.geometry.LatLng;


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

    public Double getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = Double.valueOf(score);
    }

    public boolean isValid() {
        if (address != null && latLng != null && score != null) {
            return true;
        } else {
            return false;
        }
    }




}
