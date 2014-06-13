package com.meyersj.locationsurvey.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.zxing.Result;
import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;


public class OnOffMapActivity extends ActionBarActivity {
    private final String TAG = "MapActivity";
    private final String ODK_ACTION = "com.meyersj.locationsurvey.app.ODK_ONOFFMAP";
    private final String ONOFF_ACTION = "com.meyersj.locationsurvey.app.ONOFFMAP";
    private final String URL = "url";
    private final String LINE = "rte";
    private final String DIR = "dir";
    private final String DATE = "date";
    private final String ON_STOP = "on_stop";
    private final String OFF_STOP = "off_stop";
    private final String TYPE = "type";
    private final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    private final File GEOJSONPATH = new File(Environment.getExternalStorageDirectory(), "maps/geojson/trimet");
    private final String TILES = "OSMTriMet.mbtiles";
    private final String BOARD = "Boarding";
    private final String ALIGHT = "Alighting";

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    Context context;
    private Button submit;
    private MapView mv;
    private String line;
    private String dir;
    private String url;
    private Marker board;
    private Marker alight;
    private Marker current;
    private String locType;
    private Drawable onIcon;
    private Drawable offIcon;
    private Drawable stopIcon;


    private ItemizedIconOverlay locOverlay;
    private ArrayList<Marker> locList = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_off_map);

        context = getApplicationContext();
        onIcon =getResources().getDrawable(R.drawable.transit_green_40);
        offIcon = getResources().getDrawable(R.drawable.transit_red_40);
        stopIcon = getResources().getDrawable(R.drawable.circle_filled_dark_tan_30);

        //clear = (Button) findViewById(R.id.clear);
        submit = (Button) findViewById(R.id.submit);
        mv = (MapView) findViewById(R.id.mapview);
        //mv.setMapViewListener(new MyMapViewListener());

        getExtras();
        setTiles(mv);



        if (line != null && dir != null) {
            locList = getStops(line, dir);
            addRoute(line, dir);
        }

        setItemizedOverlay(mv, locList);


        Intent i = this.getIntent();
        String action = i.getAction();


        if (action.equals(ONOFF_ACTION)) {
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(board == null || alight == null) {
                        Toast.makeText(getApplicationContext(),"Both boarding and alighting locations must be set",
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        //verify correct locations
                        verifyAndSubmitLocationsPOST();
                    }

                }
            });
        }
        else if (action.equals(ODK_ACTION)) {
            submit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(board == null || alight == null) {
                        Toast.makeText(getApplicationContext(),"Both boarding and alighting locations must be set",
                                Toast.LENGTH_LONG).show();
                    }
                    else {
                        //verify correct locations
                        verifyAndSubmitLocationsODK();
                    }

                }
            });
        }
    }

    protected void postResults(String onStop, String offStop) {
        Date date = new Date();

        Bundle extras = new Bundle();
        extras.putString(URL, url);
        extras.putString(LINE, line);
        extras.putString(DIR, dir);
        extras.putString(DATE, dateFormat.format(date));
        extras.putString(ON_STOP, onStop);
        extras.putString(OFF_STOP, offStop);
        extras.putString(TYPE, "pair");

        Intent post = new Intent(getApplicationContext(), PostService.class);
        post.putExtras(extras);
        getApplicationContext().startService(post);
    }

    protected void resetMap() {
        clearCurrentMarker();
        if (alight != null) {
            alight.setMarker(stopIcon);
            alight = null;
        }
        if (board != null) {
            board.setMarker(stopIcon);
            board = null;
        }
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
        mv.setZoom(14);
    }


    protected void setItemizedOverlay(MapView mv, ArrayList<Marker> locList) {
        final MapView mapView = mv;
        locOverlay = new ItemizedIconOverlay(mv.getContext(), locList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        Log.d(TAG, "locOverlay item tapped");
                        Log.d(TAG, item.getTitle());
                        //mapView.selectMarker(item);
                        selectLocType(item);
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final Marker item) {
                        //item.getDescription();
                        Log.d(TAG, "LongPress: " + item.getDescription());
                        return true;

                    }
                }
        );
        mv.addItemizedOverlay(locOverlay);
    }

    //Used to set current marker and type chosen in AlertDialog for boarding and alighting location
    protected void setCurrentMarker(Marker currentMarker, String type) {
        current = currentMarker;
        locType = type;
    }

    //clears selected marker if user selects 'Cancel' in AlertDialog for boarding and alighting location
    protected void clearCurrentMarker() {
        current = null;
        locType = null;
    }

    //saves selected marker and type if user selects 'OK' in AlertDialog for boarding and alighting location
    protected void saveCurrentMarker() {
        if (locType != null) {

            //if board or alight marker is already set switch it back to default icon
            if (alight != null && alight == current) {
                alight.setMarker(stopIcon);
                alight = null;
            }
            if (board != null && board == current) {
                board.setMarker(stopIcon);
                board = null;
            }

            if (locType.equals(BOARD)) {
                if(board != null) {
                    board.setMarker(stopIcon);
                }
                board = current;
                board.setMarker(onIcon);
                Log.d(TAG, BOARD + ": " + board.getTitle());
            }
            else {
                if (alight != null) {
                    alight.setMarker(stopIcon);
                }
                alight = current;
                alight.setMarker(offIcon);
                Log.d(TAG, ALIGHT + ": " + alight.getTitle());
            }
            current = null;
            locType = null;
        }
    }

    protected void selectLocType(final Marker selectedMarker) {
        String message = selectedMarker.getTitle();

        final CharSequence[] items = {BOARD, ALIGHT};
        AlertDialog.Builder builder = new AlertDialog.Builder(OnOffMapActivity.this);
        builder.setTitle(message)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String choice = items[i].toString();
                        Log.d(TAG, "Choice: " + choice);
                        setCurrentMarker(selectedMarker, choice);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Clicked OK");
                        saveCurrentMarker();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Clicked Cancel");
                        clearCurrentMarker();
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }

    protected void verifyAndSubmitLocationsPOST() {
        String boardLoc = board.getTitle();
        String alightLoc = alight.getTitle();
        String message = "Boarding: " + boardLoc + "\n\nAlighting: " + alightLoc;
        AlertDialog.Builder builder = new AlertDialog.Builder(OnOffMapActivity.this);
        builder.setTitle("Are you sure you want to submit these locations?")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //get stop ids
                        String onStop = board.getDescription();
                        String offStop = alight.getDescription();
                        //call function to post on off pair
                        postResults(onStop, offStop);
                        resetMap();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }

    protected void verifyAndSubmitLocationsODK() {
        String boardLoc = board.getTitle();
        String alightLoc = alight.getTitle();
        String message = "Boarding: " + boardLoc + "\n\nAlighting: " + alightLoc;
        AlertDialog.Builder builder = new AlertDialog.Builder(OnOffMapActivity.this);
        builder.setTitle("Are you sure you want to submit these locations?")
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //get stop ids
                        String onStop = board.getDescription();
                        String offStop = alight.getDescription();
                        exitWithStopIDs(onStop, offStop);

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //do nothing
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }

    private boolean exitWithStopIDs(String onStop, String offStop) {
        Intent intent = new Intent();
        intent.putExtra("boarding", onStop);
        intent.putExtra("alighting", offStop);
        setResult(RESULT_OK, intent);
        finish();
        return true;
    }


    protected void getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if(extras.containsKey(LINE)) {
                Log.d(TAG, extras.getString(LINE));
                line = extras.getString(LINE);
            }
            if(extras.containsKey(DIR)) {
                Log.d(TAG, extras.getString(DIR));
                dir = extras.getString(DIR);
            }
            if(extras.containsKey(URL)) {
                Log.d(TAG, extras.getString(URL));
                url = extras.getString(URL);
            }
        }
    }

    protected ArrayList<Marker> getStops(String line, String dir) {
        String geoJSONName = line + "_" + dir + "_stops.geojson";
        File stopsFile = new File(GEOJSONPATH, geoJSONName);
        BuildStops stops = new BuildStops(context, mv, stopsFile);
        return stops.getMarkers();
    }

    protected void addRoute(String line, String dir) {
        String geoJSONName = line + "_" + dir + "_routes.geojson";
        File routesFile = new File(GEOJSONPATH, geoJSONName);
        mv.loadFromGeoJSONURL("file://" + routesFile.toString());
    }

}
