package com.meyersj.locationsurvey.app.locations;

import android.util.Log;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;

import java.util.ArrayList;


public class mMapViewListener implements MapViewListener {
    private final String TAG = "MyMapViewListener";
    private ItemizedIconOverlay locOverlay;

    public mMapViewListener(ItemizedIconOverlay locOverlay) {
        this.locOverlay = locOverlay;
    }

    @Override
    public void onShowMarker(MapView mapView, Marker marker) {

    }

    @Override
    public void onHidemarker(MapView mapView, Marker marker) {

    }

    @Override
    public void onTapMarker(MapView mapView, Marker marker) {
        Log.d(TAG, "marker was tapped");
    }

    @Override
    public void onLongPressMarker(MapView mapView, Marker marker) {

    }

    @Override
    public void onTapMap(MapView mapView, ILatLng iLatLng) {

    }

    @Override
    public void onLongPressMap(MapView mapView, ILatLng iLatLng) {
        LatLng latLng = new LatLng(iLatLng.getLatitude(), iLatLng.getLongitude());
        locOverlay.removeAllItems();
        Marker m = new Marker("", null, latLng);
        m.addTo(mapView);
        locOverlay.addItem(m);
        mapView.invalidate();
    }


}
