/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.mobilesurveyor.app.stops;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;


public class Stop extends Marker implements Comparable<Stop> {

    private String desc;
    private String stopID;
    private Integer stopSeq;
    private String dir;

    public Stop(MapView mv, String desc, String stopID, Integer stopSeq, LatLng aLatLng, String dir) {
        super(mv, desc, stopID, aLatLng);
        this.desc = desc;
        this.stopID = stopID;
        this.stopSeq = stopSeq;
        this.dir = dir;
    }

    @Override
    protected InfoWindow createTooltip(MapView mv) {
        return new InfoWindow(R.layout.map_tooltip_stop, mv);
    }


    public String getDesc(){
        return desc;
    }

    public String getDir() {
        return dir;
    }

    public String getStopID() {
        return stopID;
    }

    // returns negative if 'this' stop is less Stop passed as argument
    public Integer compareSeq(Stop stop) {
        return this.stopSeq - stop.getStopSeq();

    }

    public Integer getStopSeq() {
        return stopSeq;
    }

    @Override
    public String toString() {
        return this.desc;
    }

    @Override
    public int compareTo(Stop compare) {
        Integer stopSeq =((Stop)compare).getStopSeq();
        /* For Ascending order*/
        return this.stopSeq - stopSeq;
    }

}
