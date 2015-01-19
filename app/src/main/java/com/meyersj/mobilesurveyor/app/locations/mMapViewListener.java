package com.meyersj.mobilesurveyor.app.locations;

import android.util.Log;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;


public class mMapViewListener implements MapViewListener {
    private final String TAG = "MyMapViewListener";
    private ItemizedIconOverlay locOverlay;
    private SurveyManager manager;
    private String mode;

    public mMapViewListener(ItemizedIconOverlay locOverlay) {
        this.locOverlay = locOverlay;
    }

    public mMapViewListener(ItemizedIconOverlay locOverlay, SurveyManager manager, String mode) {
        this.locOverlay = locOverlay;
        this.manager = manager;
        this.mode = mode;
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
        Log.d("LISTENER", "onlongpress");
        LatLng latLng = new LatLng(iLatLng.getLatitude(), iLatLng.getLongitude());
        Marker m = new Marker(mapView, "", null, latLng);
        m.addTo(mapView);
        locOverlay.removeAllItems();
        locOverlay.addItem(m);
        mapView.invalidate();
        if (manager != null) {
            manager.setLocation(m, mode);
        }
    }

}
        //Log.d("SurveyManager", "location was set: " + mode);