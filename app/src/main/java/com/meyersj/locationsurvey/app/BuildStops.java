package com.meyersj.locationsurvey.app;

import android.util.Log;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.GeoJSON;
import com.cocoahero.android.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;


public class BuildStops {
    private String TAG = "BuildStops";

    private MapView mv;
    private String geoJSON;
    private ArrayList<Marker> markers;

    protected BuildStops(MapView inMv, File inRoute) {
        mv = inMv;
        markers = new ArrayList<Marker>();
        String geoJSONString = openGeoJSON(inRoute);
        parseGeoJSON(geoJSONString);
    }

    private void parseGeoJSON(String geoJSON) {
        try {
            FeatureCollection parsed = (FeatureCollection) GeoJSON.parse(geoJSON);

            for (Feature f : parsed.getFeatures()) {

                if (f.getGeometry() instanceof Point) {
                    JSONArray coordinates = null;
                    coordinates = (JSONArray) f.getGeometry().toJSON().get("coordinates");
                    double lon = (Double) coordinates.get(0);
                    double lat = (Double) coordinates.get(1);

                    JSONObject properties = f.getProperties();
                    String stopID = properties.get("stop_id").toString();
                    String stopName = properties.get("stop_name").toString();

                    Log.d(TAG, stopID);
                    Log.d(TAG, stopName);

                    Marker newMarker = new Marker(mv, stopName, stopID, new LatLng(lat, lon));
                    markers.add(newMarker);
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String openGeoJSON(File file) {
        String entireFileText = null;
        try {
            entireFileText = new Scanner(file)
                    .useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "unable to open " + file.toString());
            e.printStackTrace();
        }
        return entireFileText;
    }


    public ArrayList<Marker> getMarkers() {
        return markers;
    }

}
