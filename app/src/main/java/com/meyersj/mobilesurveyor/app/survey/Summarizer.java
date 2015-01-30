package com.meyersj.mobilesurveyor.app.survey;

import android.util.Log;

import com.mapbox.mapboxsdk.overlay.Marker;

/**
 * Created by jeff on 1/28/15.
 */
public class Summarizer {

    public class Location {
        public Marker loc;
        public String purpose;
        public String purposeOther;
        public String mode;
        public String modeOther;
        public String blocks;
        public String parking;

        public Location(String a) {
            purpose = "1";
            purposeOther = "";
            mode = "1";
            modeOther = "";
            blocks = "4";
            parking = "";
        }

        public Location(Integer a) {
            purpose = "2";
            purposeOther = "";
            mode = "3";
            modeOther = "";
            blocks = "";
            parking = "downtown";
        }
    }

    public String purposeLookup(String val) {
        String retVal = "";
        switch(Integer.valueOf(val)) {
            case 1: retVal = "home"; break;
            case 2: retVal = "work"; break;
        }
        return retVal;
    }

    public String accessLookup(String mode, String alt) {
        String retVal = "";
        switch(Integer.valueOf(mode)) {
            case 1: retVal = "walked " + alt + " blocks"; break;
            case 2: retVal = "drove from your parked location " + alt; break;
            case 3: retVal = "bicycled"; break;
        }
        return retVal;
    }

    public String egressLookup(String mode, String alt) {
        String retVal = "";
        switch(Integer.valueOf(mode)) {
            case 1: retVal = "walk " + alt + " blocks"; break;
            case 2: retVal = "drive from your parking location at " + alt; break;
            case 3: retVal = "bicycled"; break;
        }
        return retVal;
    }

    protected final String TAG = "Summarizer";
    protected SurveyManager manager;

    public Summarizer(SurveyManager manager) {
        this.manager = manager;

        Location orig = new Location("a");
        Location dest = new Location(1);

        String onStop = "NW 5th and Davis";
        String offStop = "SE 39th and Powell";





        summarize();

    }

    public void summarize() {
        Log.d(TAG, "summarizer");

    }


}
