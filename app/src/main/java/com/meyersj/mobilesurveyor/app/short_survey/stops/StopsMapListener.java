/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.mobilesurveyor.app.short_survey.stops;

import android.util.Log;

import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

import java.util.ArrayList;


public class StopsMapListener implements com.mapbox.mapboxsdk.events.MapListener {

    private static final String TAG = "OnOffMapListener";

    MapView mv;
    ArrayList<Marker> markers;
    ItemizedIconOverlay locOverlay;


    public StopsMapListener(MapView mv, ArrayList<Marker> markers, ItemizedIconOverlay locOverlay) {
        this.mv = mv;
        this.markers = markers;
        this.locOverlay = locOverlay;
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
    }

    //TODO only show tooltips inside current view?

    @Override
    public void onZoom(ZoomEvent zoomEvent) {

        Float zoomLevel = zoomEvent.getZoomLevel();
        Log.d(TAG, "zoom level: " + String.valueOf(zoomLevel));

        if (markers != null) {

            if (zoomLevel >= 15.5) {
                for(Marker m: markers) {
                    InfoWindow toolTip = m.getToolTip(mv);
                    m.showBubble(toolTip, mv, false);
                }
            }
            else {
                for(Marker m: markers) {
                    m.getToolTip(mv).close();
                }
            }
        }

        if (locOverlay != null) {
            if (zoomLevel < 14.0) {
                mv.removeOverlay(locOverlay);
            }
            else {
                mv.addOverlay(locOverlay);
            }
        }
    }

}
