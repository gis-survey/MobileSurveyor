package com.meyersj.locationsurvey.app.stops;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.locationsurvey.app.R;

import java.util.Comparator;

/**
 * Created by meyersj on 7/2/2014.
 */
public class Stop extends Marker implements Comparable<Stop> {

    private String desc;
    private String stopID;
    private Integer stopSeq;


    public Stop(MapView mv, String desc, String stopID, Integer stopSeq, LatLng aLatLng) {
        super(mv, desc, stopID, aLatLng);
        this.desc = desc;
        this.stopID = stopID;
        this.stopSeq = stopSeq;
    }

    public Stop(String desc, String stopID, Integer stopSeq, LatLng aLatLng) {
        super(desc, stopID, aLatLng);
        this.desc = desc;
        this.stopID = stopID;
        this.stopSeq = stopSeq;
    }



    @Override
    protected InfoWindow createTooltip(MapView mv) {
        return new InfoWindow(R.layout.title_tooltip, mv);
    }


    public String getDesc(){
        return desc;
    }

    public String getLabel(){
        return stopSeq.toString() + " - " + desc;
    }

    public String getStopID() {
        return stopID;
    }

    public Integer getStopSeq() {
        return stopSeq;
    }
    @Override
    public String toString() {
        return this.getLabel();
    }

    @Override
    public int compareTo(Stop compare) {
        Integer stopSeq =((Stop)compare).getStopSeq();
        /* For Ascending order*/
        return this.stopSeq - stopSeq;
    }

}
