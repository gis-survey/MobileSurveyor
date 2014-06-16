package com.meyersj.locationsurvey.app.util;

import android.util.Log;

import com.mapbox.mapboxsdk.geometry.BoundingBox;

/**
 * Created by meyersj on 6/16/2014.
 */
public class BuildBoundingBox {

    private static final String TAG = "BoundingBox";
    private static final Double BUFFER = 0.001;

    private Double north = 0.0;
    private Double east = 0.0;
    private Double south = 0.0;
    private Double west = 0.0;

    public BuildBoundingBox() {
        Log.d(TAG, "BuildBoundingBox");
    }

    //Brute force update bounds based on input points
    //TODO use smarter algorithm
    //convex hull -> bounding box
    public void checkPoint(Double x, Double y) {

        if (east == 0.0) {
            east = x;
            west = x;
        }
        if (north == 0.0) {
            north = y;
            south = y;
        }

        //check x bounds
        if (x < west) {
            west = x;
        }
        else if (x > east) {
            east = x;
        }

        //check y bounds
        if (y > north) {
            north = y;
        }
        else if (y < south) {
            south = y;
        }
    }

    public BoundingBox getBoundingBox() {
        return new BoundingBox(north + BUFFER, east + BUFFER, south - BUFFER, west - BUFFER);
    }

}
