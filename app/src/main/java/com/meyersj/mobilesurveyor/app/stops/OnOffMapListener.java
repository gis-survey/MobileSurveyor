package com.meyersj.mobilesurveyor.app.stops;

import android.util.Log;

import com.mapbox.mapboxsdk.events.MapListener;
//import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

import java.util.ArrayList;


public class OnOffMapListener implements MapListener {

    private static final String TAG = "OnOffMapListener";

    MapView mv;
    ArrayList<Marker> markers;
    ItemizedIconOverlay locOverlay;

    public OnOffMapListener(MapView mv) {
        this.mv = mv;
    }

    public OnOffMapListener(MapView mv, ArrayList<Marker> markers, ItemizedIconOverlay locOverlay) {
        this.mv = mv;
        this.markers = markers;
        this.locOverlay = locOverlay;
    }

    public void setMarkers(ArrayList<Marker> markers) {
        if(this.markers != null) {
            for (Marker m : this.markers) {
                m.getToolTip(mv).close();
            }
        }
        this.markers = markers;
    }

    public void setOverlay(ItemizedIconOverlay locOverlay) {
        if(this.locOverlay != null) {
            mv.removeOverlay(this.locOverlay);
        }
        this.locOverlay = locOverlay;
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {}


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

    //@Override
    //public void onRotate(RotateEvent rotateEvent) {}

}
