package com.meyersj.mobilesurveyor.app.survey;

import android.content.Context;
import android.util.Log;

import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.stops.Stop;
import com.meyersj.mobilesurveyor.app.util.Cons;

import java.util.ArrayList;
import java.util.List;


public class SurveyManager {

    protected Context context;
    protected String line;
    protected String dir;
    protected Marker orig;
    protected Marker dest;
    protected Marker onStop;
    protected Marker offStop;

    public SurveyManager(Context context, String line, String dir) {
        this.context = context;
        this.line = line;
        this.dir = dir;
    }

    public void setLocation(Marker marker, String mode) {
        if(mode.equals("origin")) {
            this.orig = marker;
            Log.d("SHOWLOC", "orig set");
        }
        else if(mode.equals("destination")) {
            this.dest = marker;
            Log.d("SHOWLOC", "dest set");
        }
    }

    public Marker getOrig(){
        return this.orig;
    }

    public Marker getDest(){
        return this.dest;
    }

    public void setStop(Marker marker, String passage) {
        if(passage.equals(Cons.BOARD)) {
            this.onStop = marker;
            Log.d("SHOWLOC", "on stop set");
        }
        else if(passage.equals(Cons.ALIGHT)) {
            this.offStop = marker;
            Log.d("SHOWLOC", "off stop set");
        }
    }

    public Marker getOnStop(){
        return this.onStop;
    }

    public Marker getOffStop(){
        return this.offStop;
    }

    //public void showLocations(MapView mv) {
        //Log.d("SHOWLOC", "showlocations");
        //locations.removeAllItems();
        //if(orig != null) {
        //    Log.d("SHOWLOC", "orig not null");
        //    orig.addTo(mv);
        //SLO    locations.addItem(orig);
       // }
        //if(dest != null)
         //   Log.d("SHOWLOC", "dest not null");
         //   dest.addTo(mv);
         //   locations.addItem(dest);
        //}
        //mv.addItemizedOverlay(locations);
    //}

    //public void hideLocations(MapView mv) {
    //    locations.removeAllItems();
    //    mv.removeOverlay(locations);
    //}


}
