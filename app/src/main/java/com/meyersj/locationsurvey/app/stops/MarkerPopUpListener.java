package com.meyersj.locationsurvey.app.stops;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

import java.util.ArrayList;

/**
 * Created by meyersj on 6/27/2014.
 */
public class MarkerPopUpListener implements MapListener {

    private static final String TAG = "MarkerPopUpMapListener";

    MapView mv;
    ArrayList<Marker> markers;

    public MarkerPopUpListener(MapView mapView) {
        mv = mapView;
    }

    public MarkerPopUpListener(MapView mapView, ArrayList<Marker> aMarkers) {
        mv = mapView;
        markers = aMarkers;
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        //Log.d(TAG, scrollEvent.toString());
    }


    //TODO only show tooltips inside current view?

    @Override
    public void onZoom(ZoomEvent zoomEvent) {
        Float zoomLevel = zoomEvent.getZoomLevel();
        Log.d(TAG, "zoom level: " + String.valueOf(zoomLevel));

        //Log.d(TAG, mv.getBoundingBox().toString());

        if (markers != null) {

            if (zoomLevel >= 16.7) {
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
    }

}
