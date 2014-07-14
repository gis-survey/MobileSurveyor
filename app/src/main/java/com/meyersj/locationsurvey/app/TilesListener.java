package com.meyersj.locationsurvey.app;

import android.util.Log;

import com.mapbox.mapboxsdk.events.MapListener;
import com.mapbox.mapboxsdk.events.ScrollEvent;
import com.mapbox.mapboxsdk.events.ZoomEvent;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.tileprovider.MapTileLayerBase;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.views.MapView;

/**
 * Created by jeff on 13/07/14.
 */
public class TilesListener implements MapListener {

    private static final String TAG = "TilesListener";
    MapView mapView;
    ITileLayer mbTiles;
    ITileLayer osmTiles;
    BoundingBox mbTilesBox;

    public TilesListener(MapView mapView, ITileLayer mbTiles, ITileLayer osmTiles) {
        this.mapView = mapView;
        this.mbTiles = mbTiles;
        this.osmTiles = osmTiles;
        this.mbTilesBox = this.mbTiles.getBoundingBox();
    }


    @Override
    public void onScroll(ScrollEvent scrollEvent) {
        //Log.d(TAG, scrollEvent.getX());





    }

    @Override
    public void onZoom(ZoomEvent zoomEvent) {
        Float zoomLevel = zoomEvent.getZoomLevel();
        Log.d(TAG, "zoom level: " + String.valueOf(zoomLevel));

        MapTileLayerBase base = mapView.getTileProvider();
        Log.d(TAG, base.toString());

        /*
        if (zoomLevel < 11) {

            if(mapView.getTileProvider() != osmTiles) {
                mapView.setTileSource(osmTiles);
            }
        }
        if (zoomLevel >= 11) {

            if(mapView.getTileProvider() != mbTiles) {
                mapView.setTileSource(mbTiles);
            }
        }
        */



    }
}
