package com.meyersj.mobilesurveyor.app.survey.Location;

import android.graphics.drawable.Drawable;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.mapbox.mapboxsdk.views.MapViewListener;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;
import com.meyersj.mobilesurveyor.app.util.Cons;


public class PickLocationMapViewListener implements MapViewListener {
    private final String TAG = getClass().getCanonicalName();
    private ItemizedIconOverlay locOverlay;
    private String mode;
    private Drawable start;
    private Drawable end;
    private PickLocationFragment fragment;
    private SurveyManager manager;
    private CheckBox outsideRegion;
    private AutoCompleteTextView stopSearch;


    public PickLocationMapViewListener(PickLocationFragment fragment, ItemizedIconOverlay
            locOverlay, SurveyManager manager, String mode, Drawable start, Drawable end, CheckBox outsideRegion, AutoCompleteTextView stopSearch) {
        this.locOverlay = locOverlay;
        this.manager = manager;
        this.outsideRegion = outsideRegion;
        this.mode = mode;
        this.start = start;
        this.end = end;
        this.fragment = fragment;
        this.stopSearch = stopSearch;
    }


    @Override
    public void onShowMarker(final MapView mapView, final Marker marker) {}

    @Override
    public void onHidemarker(MapView mapView, Marker marker) {

    }

    @Override
    public void onTapMarker(final MapView mapView, final Marker marker) {}

    @Override
    public void onLongPressMarker(final MapView mapView, final Marker marker) {

    }

    @Override
    public void onTapMap(MapView mapView, ILatLng iLatLng) {}

    @Override
    public void onLongPressMap(MapView mapView, ILatLng iLatLng) {
        LatLng latLng = new LatLng(iLatLng.getLatitude(), iLatLng.getLongitude());
        Marker m = new Marker(mapView, "", null, latLng);
        if(manager != null) {
            if(mode.equals(Cons.ORIG)) {
                if (manager.getOrig() != null) fragment.removeLocation(manager.getOrig());
                m.setMarker(start);
            }
            else if(mode.equals(Cons.DEST)) {
                if (manager.getDest() != null) fragment.removeLocation(manager.getDest());
                m.setMarker(end);
            }
            manager.setLocation(m, mode);
        }
        locOverlay.removeAllItems();
        locOverlay.addItem(m);
        mapView.invalidate();
        m.addTo(mapView);
        manager.setRegion(false, mode);
        manager.setSeachString("", mode);
        if(stopSearch != null)
            stopSearch.setText("");
        if(outsideRegion.isChecked()) outsideRegion.setChecked(false);
    }
}




