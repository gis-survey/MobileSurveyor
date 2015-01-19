package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.Overlay;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.PathUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public abstract class MapFragment extends Fragment {

    protected final String TAG = "MapFragment";
    protected final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    protected final String TILESNAME = "OSMTriMet.mbtiles";

    protected Activity activity;
    protected Context context;
    protected View view;
    protected MapView mv;
    protected ItemizedIconOverlay surveyOverlay;
    protected ArrayList<Marker> surveyList = new ArrayList<Marker>();
    public MapFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_default_map, container, false);
        //Log.d(TAG, "create view map fragment");
        activity = getActivity();
        context = activity.getApplicationContext();
        //mv = (MapView) view.findViewById(R.id.mapview);
        //setTiles(mv);
        surveyOverlay = new ItemizedIconOverlay(context, surveyList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    protected void setTiles(MapView mv) {
        ILatLng startingPoint = new LatLng(45.52186, -122.679005);
        ITileLayer mbTilesSource;
        ITileLayer osmSource = new WebSourceTileLayer("openstreetmap",
                "http://tile.openstreetmap.org/{z}/{x}/{y}.png").setName("OpenStreetMap")
                .setAttribution("© OpenStreetMap Contributors");
        try {
            File tiles = new File(TILESPATH, TILESNAME);
            mbTilesSource = new MBTilesLayer(tiles);
            mv.setTileSource(mbTilesSource);
        }
        catch(Exception e) {
            Log.e(TAG, "unable to open local mbtiles");
            if(mv == null)
                Log.d(TAG, "map view is null");
            mv.setTileSource(osmSource);
        }

        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(startingPoint);
        mv.setZoom(14);
    }

    protected void addRoute(Context context, String line, String dir) {
        String geoJSONName = line + "_" + dir + "_routes.geojson";
        Paint pathPaint = new Paint();
        pathPaint.setColor(getResources().getColor(R.color.black_light_light));
        pathPaint.setAntiAlias(true);
        pathPaint.setStrokeWidth(6.0f);
        pathPaint.setStyle(Paint.Style.STROKE);
        ArrayList<PathOverlay> paths = PathUtils.getPathFromAssets(context, "geojson/" + geoJSONName);
        if (paths != null) {
            for(PathOverlay mPath: paths) {
                mPath.setPaint(pathPaint);
                mv.addOverlay(mPath);
            }
        }
    }

    public void updateView(SurveyManager manager) {
        mv.removeOverlay(surveyOverlay);
        surveyOverlay.removeAllItems();

        Marker orig = manager.getOrig();
        Marker dest = manager.getDest();
        Marker onStop = manager.getOnStop();
        Marker offStop = manager.getOffStop();

        if(this instanceof PickLocationFragment) {
            PickLocationFragment fragment = (PickLocationFragment) this;
            if(fragment.getMode().equals("origin") && orig != null) {
                surveyOverlay.addItem(orig);
            }
            else if(fragment.getMode().equals("destination") && dest != null) {
                surveyOverlay.addItem(dest);
            }
        }
        else {
            if(orig != null) {
                surveyOverlay.addItem(orig);
            }
            if(dest != null) {
                surveyOverlay.addItem(dest);
            }
            if(onStop != null) {
                surveyOverlay.addItem(onStop);
            }
            if(offStop != null) {
                surveyOverlay.addItem(offStop);
            }
        }
        mv.addItemizedOverlay(surveyOverlay);
    }


}
