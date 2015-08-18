/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.onoffsurveyor.app.short_survey.stops;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.onoffsurveyor.app.R;
import com.meyersj.onoffsurveyor.app.short_survey.stops.helpers.BuildStops;
import com.meyersj.onoffsurveyor.app.util.Cons;
import com.meyersj.onoffsurveyor.app.util.PathUtils;

import java.io.File;
import java.util.ArrayList;


public class MapActivity extends ActionBarActivity {

    private final String TAG = "OnOffMapActivity";
    private final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    private final String TILES = "OSMTriMet.mbtiles";

    private Context context;
    private MapView mv;
    private Sequences sequences;
    private String line;
    private String dir;
    private BoundingBox bbox;


    private Manager manager;
    private ArrayList<Marker> stopsList = new ArrayList<Marker>();
    private ArrayList<Marker> selectedList = new ArrayList<Marker>();
    private ItemizedIconOverlay stopsOverlay;
    private ItemizedIconOverlay selectedOverlay;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_off_map);

        context = getApplicationContext();
        mv = (MapView) findViewById(R.id.mapview);

        setTiles(mv);
        getExtras();

        if (line != null && dir != null) {
            addRoute(line, dir);
            stopsList = getStopMarkers(line, dir, true);
            for (Marker stop: stopsList) {
                setToolTipListener(stop);
            }


            if(bbox != null) {
                mv.zoomToBoundingBox(bbox, true, false, true, true);
            }

            setItemizedOverlay(mv, stopsList, selectedList);
            mv.addListener(new StopsMapListener(mv, stopsList, stopsOverlay));

            manager = new Manager(context, selectedOverlay);
            sequences = new Sequences(this, stopsList, manager);
            manager.setOnAdapter(sequences.getOnAdapter());
            manager.setOffAdapter(sequences.getOffAdapter());

            Save save = new Save(this, manager, mv, bbox, getIntent().getExtras());
            Search search = new Search(this, stopsList, manager);
        }
    }


    protected void getExtras() {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if(extras.containsKey(Cons.LINE)) {
                Log.d(TAG, extras.getString(Cons.LINE));
                line = extras.getString(Cons.LINE);
            }
            if(extras.containsKey(Cons.DIR)) {
                Log.d(TAG, extras.getString(Cons.DIR));
                dir = extras.getString(Cons.DIR);
            }
        }
    }

    protected ArrayList<Marker> getStopMarkers(String line, String dir, Boolean zoom) {
        String geoJSONName = line + "_" + dir + "_stops.geojson";
        BuildStops stops = new BuildStops(context, mv, "geojson/" + geoJSONName, dir);

        if(zoom) {
            bbox = stops.getBoundingBox();
            mv.zoomToBoundingBox(bbox, true, false, true, true);
        }
        return stops.getStops(); //  stopsSequenceSort(stops.getStops());
    }



    protected void setTiles(MapView mv) {
        ILatLng startingPoint = new LatLng(45.52186, -122.679005);
        ITileLayer source;
        BoundingBox box = new BoundingBox(46.0 ,-122 ,45.0, -123.5);

        try {
            File mbTiles = new File(TILESPATH, TILES);
            source = new MBTilesLayer(mbTiles);
            mv.setTileSource(source);
            box = source.getBoundingBox();
        }
        catch(Exception e) {
            Log.d(TAG, "unable to open local mbtiles");
            source = new WebSourceTileLayer("openstreetmap",
                    "http://tile.openstreetmap.org/{z}/{x}/{y}.png").setName("OpenStreetMap")
                    .setAttribution("© OpenStreetMap Contributors")
                    .setMinimumZoomLevel(11)
                    .setMaximumZoomLevel(17);
            mv.setTileSource(source);
        }

        mv.setScrollableAreaLimit(box);
        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(startingPoint);
    }


    //modify mView for each toolTip in each marker to prevent closing it when touched
    protected void setToolTipListener(final Marker stop) {

        View mView = stop.getToolTip(mv).getView();

        //from InfoWindow Constructor but commented out close
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                }
                return false;
            }
        });
        mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                selectLocTypeDialog(stop);
                return true;
            }
        });
    }


    protected void selectLocTypeDialog(final Marker selectedMarker) {
        String message = selectedMarker.getTitle();

        final CharSequence[] items = {Cons.BOARD, Cons.ALIGHT};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(message)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String choice = items[i].toString();
                        manager.setCurrentMarker(selectedMarker, choice);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        manager.saveCurrentMarker(selectedMarker);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        manager.clearCurrentMarker();
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }



    protected void setItemizedOverlay(
            final MapView mv, ArrayList<Marker> locList, ArrayList<Marker> selList) {

        stopsOverlay = new ItemizedIconOverlay(mv.getContext(), locList,

                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        selectLocTypeDialog(item);
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );

        selectedOverlay = new ItemizedIconOverlay(mv.getContext(), selList,

                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    @Override
                    public boolean onItemSingleTapUp(int i, Marker marker) {
                        return false;
                    }

                    @Override
                    public boolean onItemLongPress(int i, Marker marker) {
                        return false;
                    }
                });


        mv.addItemizedOverlay(stopsOverlay);
        mv.addItemizedOverlay(selectedOverlay);
    }


    protected void addRoute(String line, String dir) {
        String geoJSONName = line + "_" + dir + "_routes.geojson";

        Paint pathPaint = new Paint();
        pathPaint.setColor(getResources().getColor(R.color.grey));
        pathPaint.setAntiAlias(true);
        pathPaint.setStrokeWidth(6.0f);
        pathPaint.setStyle(Paint.Style.STROKE);

        ArrayList<PathOverlay> paths = PathUtils.getPathFromAssets(this, "geojson/" + geoJSONName);

        if (paths != null) {
            for(PathOverlay mPath: paths) {
                mPath.setPaint(pathPaint);
                mv.addOverlay(mPath);
            }
        }
    }




}

