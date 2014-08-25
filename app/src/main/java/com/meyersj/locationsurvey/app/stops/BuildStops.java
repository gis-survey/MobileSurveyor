package com.meyersj.locationsurvey.app.stops;

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
import com.meyersj.locationsurvey.app.R;
import com.meyersj.locationsurvey.app.stops.Stop;
import com.meyersj.locationsurvey.app.util.BuildBoundingBox;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class BuildStops {
    private String TAG = "BuildStops";

    private Context context;
    private MapView mv;
    private String geoJSON;
    private ArrayList<Marker> stops;
    private BuildBoundingBox bboxBuilder;

    public BuildStops(Context aContext, MapView inMv, String inRoute) {
        context = aContext;
        mv = inMv;
        stops = new ArrayList<Marker>();
        FeatureCollection geoJSON = openGeoJSON(inRoute);
        bboxBuilder = new BuildBoundingBox();
        parseGeoJSON(geoJSON);


    }


    private void parseGeoJSON(FeatureCollection parsed) {
        Drawable circleIcon = context.getResources().getDrawable(R.drawable.circle_filled_black_30);

        try {

            for (Feature f : parsed.getFeatures()) {

                if (f.getGeometry() instanceof Point) {
                    JSONArray coordinates = null;
                    coordinates = (JSONArray) f.getGeometry().toJSON().get("coordinates");
                    double lon = (Double) coordinates.get(0);
                    double lat = (Double) coordinates.get(1);

                    Log.d(TAG, String.valueOf(lon));
                    Log.d(TAG, String.valueOf(lat));
                    bboxBuilder.checkPoint(lon, lat);

                    JSONObject properties = f.getProperties();
                    String stopID = properties.get("stop_id").toString();
                    String stopName = properties.get("stop_name").toString();
                    String stopSeq = properties.get("stop_seq").toString();

                    Log.d(TAG, stopID);
                    Log.d(TAG, stopName);
                    Log.d(TAG, stopSeq);



                    Stop stop = new Stop(
                            mv, stopName, stopID, Integer.parseInt(stopSeq), new LatLng(lat, lon));
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
        return bboxBuilder.getBoundingBox();
    }

}
