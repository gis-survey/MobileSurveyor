/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.mobilesurveyor.app.short_survey.scans;

import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.Date;


public class CurrentLocation {

    private final String TAG = "CurrentLocation";
    private String lat = null;
    private String lon = null;
    private Float accuracy = null;
    private Date date = null;


    public CurrentLocation() {

    }

    public void setLocation(String lat, String lon, Float accuracy, String dateString) {
        this.date = Utils.parseDate(dateString);
        this.lat = lat;
        this.lon = lon;
        this.accuracy = accuracy;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public Date getDate() {
        return this.date;
    }

    public String getAccuracy() {
        return String.valueOf(accuracy);
    }



}
