package com.meyersj.mobilesurveyor.app.locations;

import android.util.Log;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;


public class mMapViewListener implements MapViewListener {
    private final String TAG = "MyMapViewListener";
    private ItemizedIconOverlay locOverlay;

    public mMapViewListener(ItemizedIconOverlay locOverlay) {
        this.locOverlay = locOverlay;
    }

    @Override
    public void onShowMarker(final MapView mapView, final Marker marker) {

    }

    @Override
    public void onHidemarker(final MapView pMapView, final Marker pMarker) {

    }


    @Override
    public void onTapMarker(final MapView mapView, final Marker marker) {
        Log.d(TAG, "marker was tapped");
    }

    @Override
    public void onLongPressMarker(final MapView mapView, final Marker marker) {

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
