/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


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


public class PathUtils {

    private final static String PATHOVERLAY = "com.mapbox.mapboxsdk.overlay.PathOverlay";

    public static ArrayList<PathOverlay> getPathFromAssets(Context context, String assetsFile) {

        ArrayList<PathOverlay> paths = new ArrayList<PathOverlay>();

        Paint mPaint = new Paint();
        mPaint.setColor(context.getResources().getColor(R.color.darker_grey));
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
