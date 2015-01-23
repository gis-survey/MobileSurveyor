package com.meyersj.mobilesurveyor.app.survey;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.meyersj.mobilesurveyor.app.util.Cons;


public class SurveyManager {

    protected static final String TAG = "SurveyManager";
    protected Context context;
    protected String line;
    protected String dir;
    protected Location orig;
    protected Location dest;
    protected Marker onStop;
    protected Marker offStop;

    public class Location {
        public Marker loc;
        public String purpose;
        public String mode;
        public String blocks;
        public String parking;

        public Location() {
            loc = null;
            purpose = "";
            mode = "";
            blocks = "";
            parking = "";
        }

        public Boolean isComplete() {
            if( loc == null || purpose == null || mode == null ||
                    blocks == null || parking == null)
                return false;
            return true;
        }
    }

    public SurveyManager(Context context, String line, String dir) {
        this.context = context;
        this.line = line;
        this.dir = dir;
        this.orig = new Location();
        this.dest = new Location();
    }

    public void setLocation(Marker marker, String mode) {
        if(mode.equals("origin")) {
            this.orig.loc = marker;
        }
        else if(mode.equals("destination")) {
            this.dest.loc = marker;
        }
    }

    public Marker getOrig(){
        return this.orig.loc;
    }

    public Marker getDest(){
        return this.dest.loc;
    }

    public void setStop(Marker marker, String passage) {
        if(passage.equals(Cons.BOARD)) {
            this.onStop = marker;
        }
        else if(passage.equals(Cons.ALIGHT)) {
            this.offStop = marker;
        }
    }

    public void updateMode(String passage, String modeValue) {
        if(passage.equals("origin")) {
            this.orig.mode = modeValue;
        }
        else if(passage.equals("destination")) {
            this.dest.mode = modeValue;
        }
    }

    public void updatePurpose(String passage, String purposeValue) {
        if(passage.equals("origin")) {
            this.orig.purpose = purposeValue;
        }
        else if(passage.equals("destination")) {
            this.dest.purpose = purposeValue;
        }
    }

    public void updateBlocks(String passage, String blocksValue) {
        if(passage.equals("origin")) {
            this.orig.blocks = blocksValue;
        }
        else if(passage.equals("destination")) {
            this.dest.blocks = blocksValue;
        }
    }

    public void updateParking(String passage, String parkingValue) {
        if(passage.equals("origin")) {
            this.orig.parking = parkingValue;
        }
        else if(passage.equals("destination")) {
            this.dest.parking = parkingValue;
        }
    }

    public Marker getOnStop(){
        return this.onStop;
    }

    public Marker getOffStop(){
        return this.offStop;
    }


    public Intent constructExitIntent(Intent intent) {
        Log.d("SurveyManager", "exit: " + this.orig.mode);

        intent.putExtra(Cons.ODK_ORIG_PURPOSE, this.orig.purpose);
        intent.putExtra(Cons.ODK_ORIG_ACCESS, this.orig.mode);
        intent.putExtra(Cons.ODK_ORIG_BLOCKS, this.orig.blocks);
        intent.putExtra(Cons.ODK_ORIG_PARKING, this.orig.parking);
        //intent.putExtra(Cons.ODK_ORIG_REGION, this.orig.region);
        if(this.orig.loc != null) {
            LatLng latLng = this.orig.loc.getPoint();
            intent.putExtra(Cons.ODK_ORIG_LAT, latLng.getLatitude());
            intent.putExtra(Cons.ODK_ORIG_LNG, latLng.getLongitude());
        }
        intent.putExtra(Cons.ODK_DEST_PURPOSE, this.dest.purpose);
        intent.putExtra(Cons.ODK_DEST_EGRESS, this.dest.mode);
        intent.putExtra(Cons.ODK_DEST_BLOCKS, this.dest.blocks);
        intent.putExtra(Cons.ODK_DEST_PARKING, this.dest.parking);
        //intent.putExtra(Cons.ODK_DEST_REGION, this.dest.region);
        if(this.dest.loc != null) {
            LatLng latLng = this.dest.loc.getPoint();
            intent.putExtra(Cons.ODK_DEST_LAT, latLng.getLatitude());
            intent.putExtra(Cons.ODK_DEST_LNG, latLng.getLongitude());
        }

        if(this.onStop != null) {
            intent.putExtra(Cons.ODK_BOARD_ID, this.onStop.getDescription());
        }
        if(this.offStop != null) {
            intent.putExtra(Cons.ODK_ALIGHT_ID, this.offStop.getDescription());
        }

        return intent;
    }


}
