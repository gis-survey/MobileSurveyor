package com.meyersj.mobilesurveyor.app.stops;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.cocoahero.android.geojson.Feature;
import com.cocoahero.android.geojson.FeatureCollection;
import com.cocoahero.android.geojson.Point;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.BuildBoundingBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class BuildStops {
    private String TAG = "BuildStops";

    private Context context;
    private MapView mv;
    private ArrayList<Marker> stops;
    private BuildBoundingBox bboxBuilder;
    private String dir;

    public BuildStops(Context context, MapView mv, String inRoute, String dir) {
        this.context = context;
        this.mv = mv;
        this.dir = dir;
        stops = new ArrayList<Marker>();
        FeatureCollection geoJSON = openGeoJSON(inRoute);
        if (geoJSON != null) {
            Log.d(TAG, "geojson not null");
            bboxBuilder = new BuildBoundingBox();
            parseGeoJSON(geoJSON);
        }
        else {
            Log.d(TAG, "geojson IS null, no bbox");
        }
    }

    private void parseGeoJSON(FeatureCollection parsed) {
        Drawable circleIcon = context.getResources().getDrawable(R.drawable.circle_24);
        try {
            for (Feature f : parsed.getFeatures()) {
                if (f.getGeometry() instanceof Point) {
                    JSONArray coordinates = null;
                    coordinates = (JSONArray) f.getGeometry().toJSON().get("coordinates");
                    double lon = (Double) coordinates.get(0);
                    double lat = (Double) coordinates.get(1);
                    bboxBuilder.checkPoint(lon, lat);
                    JSONObject properties = f.getProperties();
                    String stopID = properties.get("stop_id").toString();
                    String stopName = properties.get("stop_name").toString();
                    String stopSeq = properties.get("stop_seq").toString();
                    Stop stop = new Stop(
                            mv, stopName, stopID, Integer.parseInt(stopSeq), new LatLng(lat, lon), dir);
                    stop.setMarker(circleIcon);
                    stops.add(stop);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private FeatureCollection openGeoJSON(String assetsFile) {
        FeatureCollection parsed = null;
        try {
            parsed = DataLoadingUtils.loadGeoJSONFromAssets(context, assetsFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parsed;
    }

    public ArrayList<Marker> getStops() {
        return stops;
    }

    public BoundingBox getBoundingBox() {
        if(bboxBuilder != null) return bboxBuilder.getBoundingBox();
        return null;
    }

}
