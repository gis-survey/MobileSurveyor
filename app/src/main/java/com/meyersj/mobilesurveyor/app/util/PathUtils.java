package com.meyersj.mobilesurveyor.app.util;

import android.content.Context;
import android.graphics.Paint;

import com.cocoahero.android.geojson.FeatureCollection;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.util.DataLoadingUtils;
import com.meyersj.mobilesurveyor.app.R;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by meyersj on 6/26/2014.
 */
public class PathUtils {

    private final static String PATHOVERLAY = "com.mapbox.mapboxsdk.overlay.PathOverlay";


    //public static TransitRoute routeFactory(Context context, String line, String dir, ) {
    //    TransitRoute transitRoute = new TransitRoute(context,line, dir);
        //ArrayList<PathOverlay> paths = new ArrayList<PathOverlay>();

    //    return transitRoute;
    //}

    public static ArrayList<PathOverlay> getPathFromAssets(Context context, String assetsFile) {

        ArrayList<PathOverlay> paths = new ArrayList<PathOverlay>();

        Paint mPaint = new Paint();
        mPaint.setColor(context.getResources().getColor(R.color.black_light));
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(10.0f);
        mPaint.setStyle(Paint.Style.STROKE);

        try {
            FeatureCollection route = DataLoadingUtils.loadGeoJSONFromAssets(context, assetsFile);
            ArrayList<Object> data = DataLoadingUtils.createUIObjectsFromGeoJSONObjects(route, null);

            for(Object x: data) {
                if (x.getClass().getCanonicalName().equals(PATHOVERLAY)) {
                    PathOverlay path = (PathOverlay) x;
                    path.setPaint(mPaint);
                    paths.add(path);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (paths.size() == 0)
            return null;
        else
            return paths;
    }


}
