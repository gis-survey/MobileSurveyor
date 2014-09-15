package com.meyersj.locationsurvey.app.scans;

import android.util.Log;

import com.meyersj.locationsurvey.app.util.Utils;

import org.joda.time.DateTime;
import org.joda.time.Seconds;

import java.text.ParseException;
import java.util.Date;

/**
 * Created by jeff on 9/15/14.
 */
public class CurrentLocation {

    private final String TAG = "CurrentLocation";
    Boolean isValid = false;
    String lat = null;
    String lon = null;
    Float accuracy = null;
    Date date = null;


    public CurrentLocation() {
        isValid = false;
    }

    protected Date parseDate(String dateString) {
        Date date = null;
        try {
            date = Utils.dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }


    public void setLocation(String lat, String lon, Float accuracy, String dateString) {
        this.date = parseDate(dateString);
        this.lat = lat;
        this.lon = lon;
        this.accuracy = accuracy;
        this.isValid = true;
    }

    public void clear() {
        this.date = null;
        this.lat = null;
        this.lon = null;
        this.accuracy = null;
        this.isValid = false;
    }

    public String getLat() {
        return lat;
    }

    public String getLon() {
        return lon;
    }

    public Float timeDifference(Date compareDate) {
        DateTime current = new DateTime(date);
        DateTime compare = new DateTime(compareDate);

        float diff = compare.getMillis() - current.getMillis();
        return (Float) diff / 1000;
    }






}
