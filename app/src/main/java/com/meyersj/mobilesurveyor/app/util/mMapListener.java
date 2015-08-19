package com.meyersj.mobilesurveyor.app.util;

import android.content.Context;

import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.RotateEvent;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by jeff on 8/25/14.
 */
public class mMapListener implements MapListener {

    private Context context;
    private MapView mapview;

    public mMapListener(Context context, MapView mapView) {
        this.context = context;
        this.mapview = mapView;
    }

    @Override
    public void onScroll(ScrollEvent scrollEvent) {

    }

    @Override
    public void onZoom(ZoomEvent zoomEvent) {

    }

    @Override
    public void onRotate(RotateEvent rotateEvent) {

    }
}
