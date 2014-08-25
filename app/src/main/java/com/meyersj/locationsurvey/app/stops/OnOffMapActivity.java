package com.meyersj.locationsurvey.app.stops;

import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.meyersj.locationsurvey.app.util.PostService;
import com.meyersj.locationsurvey.app.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
//import android.support.v7.internal.widget.AdapterViewICS;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.locationsurvey.app.util.PathUtils;
import com.meyersj.locationsurvey.app.util.Utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;


public class OnOffMapActivity extends ActionBarActivity {
    private final String TAG = "OnOffMapActivity";
    private final String ODK_ACTION = "com.meyersj.locationsurvey.app.ODK_ONOFFMAP";
    private final String ONOFF_ACTION = "com.meyersj.locationsurvey.app.ONOFFMAP";
    private final String URL = "url";
    private final String LINE = "rte";
    private final String DIR = "dir";
    private final String DATE = "date";
    private final String ON_STOP = "on_stop";
    private final String OFF_STOP = "off_stop";
    private final String TYPE = "type";
    private final String USER_ID = "user_id";
    private final String ODK_BOARD = "board_id";
    private final String ODK_ALIGHT = "alight_id";
    private final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    private final String TILES = "OSMTriMet.mbtiles";
    private final String BOARD = "On";
    private final String ALIGHT = "Off";

    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private Context context;

    private Integer submitCount;

    // parameters for HTTP POST
    private String line;
    private String dir;
    private String url;
    private String user_id;

    // Variables for stop selection
    //private Marker board;
    //private Marker alight;
    //private Marker current;
    //private String locType;

    // Stop Icons
    private Drawable onIcon;
    private Drawable offIcon;
    private Drawable stopIcon;

    // Views
    private MapView mv;
    private AutoCompleteTextView stopName;
    private ImageButton clear;
    private ListView onSeqListView;
    private ListView offSeqListView;
    private Button stopSeqBtn;
    private Button submit;
    private TextView osmText;
    private Spinner countSpinner;

    private SelectedStops selectedStops;


    private ArrayAdapter<Integer> countAdapter;
    private StopSequenceAdapter onSeqListAdapter;
    private StopSequenceAdapter offSeqListAdapter;

    private ItemizedIconOverlay locOverlay;
    private ItemizedIconOverlay selOverlay;

    private ArrayList<Marker> locList = new ArrayList<Marker>();
    private ArrayList<Marker> selList = new ArrayList<Marker>();

    private BoundingBox bbox;
    private HashMap<String, Marker> stopsMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_off_map);

        context = getApplicationContext();

        submit = (Button) findViewById(R.id.submit);
        mv = (MapView) findViewById(R.id.mapview);
        clear = (ImageButton) findViewById(R.id.clear_input_stop);
        stopName = (AutoCompleteTextView) findViewById(R.id.input_stop);

        onIcon = getResources().getDrawable(R.drawable.transit_green_40);
        //onIcon = getResources().getDrawable(R.drawable.circle_red2);
        offIcon = getResources().getDrawable(R.drawable.transit_red_40);
        stopIcon = getResources().getDrawable(R.drawable.circle_filled_black_30);


        setupCounter(5);
        getExtras();
        setTiles(mv);

        if (line != null && dir != null) {
            locList = getStops(line, dir);
            selList = new ArrayList<Marker>();

            // set listener for when marker tooltip is selected
            for (Marker marker: locList)
                setToolTipListener(marker);

            if(bbox != null) {
                mv.zoomToBoundingBox(bbox, true, false, true, true);
            }

            setItemizedOverlay(mv, locList, selList);
            OnOffMapListener listener = new OnOffMapListener(mv, locList, locOverlay);
            mv.addListener(listener);

            addRoute(line, dir);
            setupStopSequenceList();
            setupStopSearch();
            selectedStops = new SelectedStops(
                    getApplicationContext(), onSeqListAdapter, offSeqListAdapter, selOverlay);

        }



        Intent i = this.getIntent();
        String action = i.getAction();


        if (action.equals(ONOFF_ACTION)) {
            setupOnOffSubmitAction();
        }
        else if (action.equals(ODK_ACTION)) {
            setupODKSubmitAction();
            countSpinner.setVisibility(View.GONE);
            //submit.setLayoutWight

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT, 10.0f);
            submit.setLayoutParams(params);

        }

        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            Utils.longToastCenter(getApplicationContext(),
                    "Please enable network connections.");
        }
    }


    protected void stopSequenceAdapterSetup(final ListView listView, final StopSequenceAdapter adapter, final Button button) {
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                adapter.setSelectedIndex(position);

                Stop stop = (Stop) adapterView.getAdapter().getItem(position);

                if (listView == onSeqListView) {
                    selectedStops.saveSequenceMarker(BOARD, stop);
                }
                else {
                    selectedStops.saveSequenceMarker(ALIGHT, stop);
                }
            }
        });
    }

    private void setupStopSearch() {
        String[] stopNames = buildStopsArray(locList);
        //String[] stopNames = {"N Lombard TC MAX Station", "SW 6th & Madison St MAX Station","13123", "11512" };
        final ArrayList<String> stopsList = new ArrayList<String>();
        Collections.addAll(stopsList, stopNames);

        StopSearchAdapter adapter = new StopSearchAdapter
                (this,android.R.layout.simple_list_item_1,stopsList);
        stopName.setAdapter(adapter);

        stopName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                stopName.setText("");

                //close keypad
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                selectLocType(stopsMap.get(stopsList.get(position)));
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopName.clearListSelection();
                stopName.setText("");
            }
        });
    }


    private void setupCounter(int size) {
        countSpinner = (Spinner) findViewById(R.id.count_spinner);

        ArrayList<Integer> countList = new ArrayList<Integer>();
        for (int i = 1; i < size + 1; i++) {
            countList.add(i);
        }

        countAdapter = new ArrayAdapter<Integer>(
                getApplicationContext(), R.layout.spinner_item_center, countList);
        countAdapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        countSpinner.setAdapter(countAdapter);

        countSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                submitCount = (Integer) adapterView.getAdapter().getItem(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}

        });


    }

    private void setupStopSequenceList() {
        stopSeqBtn = (Button) findViewById(R.id.stop_seq_btn);
        onSeqListView = (ListView) findViewById(R.id.on_stops_seq);
        offSeqListView = (ListView) findViewById(R.id.off_stops_seq);
        osmText = (TextView) findViewById(R.id.osm_text);

        ArrayList<Stop> stops = stopsSequenceSort(locList);

        onSeqListAdapter = new StopSequenceAdapter(this, stops);
        offSeqListAdapter = new StopSequenceAdapter(this, stops);

        stopSequenceAdapterSetup(onSeqListView, onSeqListAdapter, stopSeqBtn);
        stopSequenceAdapterSetup(offSeqListView, offSeqListAdapter, stopSeqBtn);

        stopSeqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (onSeqListView.getVisibility() == View.INVISIBLE) {
                    osmText.setVisibility(View.INVISIBLE);

                    onSeqListView.setVisibility(View.VISIBLE);
                    offSeqListView.setVisibility(View.VISIBLE);
                    stopSeqBtn.setBackground(context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_top));


                } else {
                    osmText.setVisibility(View.VISIBLE);

                    onSeqListView.setVisibility(View.INVISIBLE);
                    offSeqListView.setVisibility(View.INVISIBLE);
                    stopSeqBtn.setBackground(context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_all));

                }
            }
        });

    }


    private void setupOnOffSubmitAction() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedStops.validateSelection()) {
                    //verify correct locations
                    if (selectedStops.validateStopSequence()) {
                        verifyAndSubmitLocationsPOST(submitCount);
                    }
                    else {
                        Utils.longToast(getApplicationContext(),
                                "Invalid stop sequence based on route direction");
                    }
                }
                else {
                    Utils.longToast(getApplicationContext(),
                            "Both boarding and alighting locations must be set");
                }
            }
        });
    }

    private void setupODKSubmitAction() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedStops.validateSelection()) {
                    //verifyAndSubmitLocationsODK();
                    String onStop = selectedStops.getBoard().getDescription();
                    String offStop = selectedStops.getAlight().getDescription();

                    if (selectedStops.validateStopSequence()) {
                        exitWithStopIDs(onStop, offStop);
                    }
                    else {
                        Utils.longToast(getApplicationContext(),
                                "Invalid stop sequence based on route direction");
                    }
                }
                else {
                    Utils.longToast(getApplicationContext(),
                            "Both boarding and alighting locations must be set");
                }
            }
        });
    }


    protected ArrayList<Stop> stopsSequenceSort(final ArrayList<Marker> locList) {
        ArrayList<Stop> stops = new ArrayList<Stop>();

        for(Marker marker: locList) {
            stops.add((Stop) marker);
        }
        Collections.sort(stops);

        return stops;
    }


    protected String[] buildStopsArray(ArrayList<Marker> locList) {

        stopsMap = new HashMap<String, Marker>();

        for(Marker m: locList) {
            stopsMap.put(m.getTitle(), m);
            stopsMap.put(m.getDescription(), m);
        }

        String[] stopNames = new String[stopsMap.size()];

        Integer i = 0;
        for (String key : stopsMap.keySet()) {
            stopNames[i] = key;
            i += 1;
        }

        return stopNames;
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
        extras.putString(USER_ID, user_id);
        extras.putString(TYPE, "pair");

        Intent post = new Intent(getApplicationContext(), PostService.class);
        post.putExtras(extras);
        getApplicationContext().startService(post);
    }

    protected void resetMap() {
        selectedStops.clearCurrentMarker();
        mv.zoomToBoundingBox(bbox, true, false, true, true);
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
        //mv.setZoom(14);
    }


    protected void setItemizedOverlay(
            final MapView mapView, ArrayList<Marker> locList, ArrayList<Marker> selList) {

        locOverlay = new ItemizedIconOverlay(mv.getContext(), locList,

                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        selectLocType(item);
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );

        selOverlay = new ItemizedIconOverlay(mv.getContext(), selList, new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
            @Override
            public boolean onItemSingleTapUp(int i, Marker marker) {
                Log.d(TAG, "selOverlay single tap");
                return false;
            }

            @Override
            public boolean onItemLongPress(int i, Marker marker) {
                Log.d(TAG, "selOverlay long press");
                return false;
            }
        });

        mv.addItemizedOverlay(locOverlay);
        mv.addItemizedOverlay(selOverlay);
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
                        selectedStops.setCurrentMarker(selectedMarker, choice);

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Clicked OK");
                        selectedStops.saveCurrentMarker(selectedMarker);
                        //mv.zoomToBoundingBox(bbox, true, false, true, true);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.d(TAG, "Clicked Cancel");
                        selectedStops.clearCurrentMarker();
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }


    // set spinner to first item
    private void resetCount() {
        submitCount = 1;
        countSpinner.setSelection(0);

    }
    protected void verifyAndSubmitLocationsPOST(final int count) {

        final Marker board = selectedStops.getBoard();
        final Marker alight = selectedStops.getAlight();

        if (Utils.isNetworkAvailable(getApplicationContext())) {
            String boardLoc = board.getTitle();
            String alightLoc = alight.getTitle();
            String message = BOARD + ": " + boardLoc + "\n\n" + ALIGHT + ": " + alightLoc;
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

                            for(int i_count = 0; i_count < count; i_count++ ) {
                                postResults(onStop, offStop);
                            }

                            resetCount();
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
        else {
            Log.d(TAG, "No network connection");
            Utils.shortToastCenter(getApplicationContext(), "Please enable network connections.");
        }
    }

    private boolean exitWithStopIDs(String onStop, String offStop) {
        Intent intent = new Intent();
        intent.putExtra(ODK_BOARD, onStop);
        intent.putExtra(ODK_ALIGHT, offStop);
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
            if(extras.containsKey(USER_ID)) {
                Log.d(TAG, extras.getString(USER_ID));
                user_id = extras.getString(USER_ID);
            }
        }
    }

    // open stops geojson for current route
    // parse into ArrayList of markers
    // each marker contains stop description, stop id, stop sequence and LatLng
    protected ArrayList<Marker> getStops(String line, String dir) {
        String geoJSONName = line + "_" + dir + "_stops.geojson";
        BuildStops stops = new BuildStops(context, mv, "geojson/" + geoJSONName);
        bbox = stops.getBoundingBox();
        mv.zoomToBoundingBox(bbox, true, false, true, true);
        return stops.getStops();

    }


    //modify mView for each toolTip in each marker to prevent closing it when touched
    protected void setToolTipListener(final Marker marker) {

        View mView = marker.getToolTip(mv).getView();

        //from InfoWindow Constructor but commented out close
        mView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    //do nothing (don't close)
                }
                return false;
            }
        });

        mView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Log.d(TAG, "locOverlay toolTip LongPress");
                Log.d(TAG, marker.getTitle());
                selectLocType(marker);
                return true;
            }
        });

    }

    protected void addRoute(String line, String dir) {
        String geoJSONName = line + "_" + dir + "_routes.geojson";

        ArrayList<PathOverlay> paths = PathUtils.getPathFromAssets(this, "geojson/" + geoJSONName);

        Paint pathPaint = new Paint();
        pathPaint.setColor(getResources().getColor(R.color.black_light_light));
        pathPaint.setAntiAlias(true);
        pathPaint.setStrokeWidth(6.0f);
        pathPaint.setStyle(Paint.Style.STROKE);

        if (paths != null) {
            for(PathOverlay path: paths) {
                path.setPaint(pathPaint);
                mv.addOverlay(path);

            }

        }
    }

}
