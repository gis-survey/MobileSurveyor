package com.meyersj.mobilesurveyor.app.util;

import android.content.Context;
import android.graphics.Paint;
import android.util.Log;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.LineString;
import com.cocoahero.android.geojson.MultiLineString;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;

import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;


public class TransitRoute {

    protected final String TAG = "TransitRoute";
    protected Context context;
    protected String key;
    protected Boolean valid = true;
    protected Paint paint;
    protected ArrayList<PathOverlay> paths = new ArrayList<PathOverlay>();
    protected BoundingBox bbox;
    protected ArrayList<LatLng> allPoints = new ArrayList<LatLng>();

    public TransitRoute(Context context, String line, String dir, Paint paint) {
        this.key = line + "_" + dir;
        this.context = context;
        this.paint = paint;

        String assetsFile = "geojson/" + this.key + "_routes.geojson";
        try {
            FeatureCollection features = DataLoadingUtils.loadGeoJSONFromAssets(context, assetsFile);
            for(Feature feature: features.getFeatures()) {
                if (feature.getGeometry() instanceof LineString) {
                    JSONArray points = (JSONArray) feature.getGeometry().toJSON().get("coordinates");
                    construct(points);
                } else if (feature.getGeometry() instanceof MultiLineString) {
                    JSONArray lines = (JSONArray) feature.getGeometry().toJSON().get("coordinates");
                    for (int k = 0; k < lines.length(); k++) {
                        JSONArray points = (JSONArray) lines.get(k);
                        construct(points);
                    }
                }
            }
            bbox =  buildBBox();
        } catch (IOException e) {
            Log.d(TAG, e.toString());
            valid = false;
            Log.d(TAG, e.toString());
            e.printStackTrace();
        } catch (JSONException e) {
            valid = false;
            Log.d(TAG, e.toString());
            e.printStackTrace();
        }
    }

    protected void construct(JSONArray points) throws JSONException {
        PathOverlay path = new PathOverlay();
        JSONArray coordinates;
        for (int i = 0; i < points.length(); i++) {
            coordinates = (JSONArray) points.get(i);
            double lon = (Double) coordinates.get(0);
            double lat = (Double) coordinates.get(1);
            LatLng latLng = new LatLng(lat, lon);
            allPoints.add(latLng);
            path.addPoint(latLng);
        }
        path.setPaint(paint);
        paths.add(path);
    }

    public void addRoute(MapView mapView, Boolean zoomTo) {
        for(PathOverlay path: paths) {
            mapView.addOverlay(path);
        }
        if(zoomTo) zoomTo(mapView);
    }

    public void clearRoute(MapView mapView) {
        for(PathOverlay path: paths) {
            mapView.removeOverlay(path);
        }
    }

    public void zoomTo(MapView mapView) {
        if(bbox != null) {
            mapView.zoomToBoundingBox(bbox, true, true);
        }
    }

    public Boolean isValid() {
        return valid;
    }

    public BoundingBox buildBBox() {
        BoundingBox extent = BoundingBox.fromLatLngs(allPoints);
        Double latPad = extent.getLatitudeSpan() * 0.1;
        Double lonPad = extent.getLongitudeSpan() * 0.1;
        return new BoundingBox(
                extent.getLatNorth() + (latPad) * 2.5,
                extent.getLonEast() + lonPad,
                extent.getLatSouth() - latPad,
                extent.getLonWest() - lonPad);
    }

}
