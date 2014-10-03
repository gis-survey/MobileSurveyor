package com.meyersj.locationsurvey.app.scans;

import android.util.Log;

import com.meyersj.locationsurvey.app.util.Utils;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.text.ParseException;
import java.util.Date;


public class CurrentLocation {

    private final String TAG = "CurrentLocation";
    String lat = null;
    String lon = null;
    Float accuracy = null;
    Date date = null;


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
