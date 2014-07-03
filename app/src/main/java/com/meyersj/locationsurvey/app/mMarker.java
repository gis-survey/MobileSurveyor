package com.meyersj.locationsurvey.app;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.InfoWindow;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by meyersj on 7/2/2014.
 */
public class mMarker extends Marker {

    public mMarker(MapView mv, String aTitle, String aDescription, LatLng aLatLng) {
        super(mv, aTitle, aDescription, aLatLng);
    }

    @Override
    protected InfoWindow createTooltip(MapView mv) {
        return new InfoWindow(R.layout.title_tooltip, mv);
    }

}
