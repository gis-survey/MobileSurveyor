package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.BuildStops;
import com.meyersj.mobilesurveyor.app.stops.OnOffMapListener;
import com.meyersj.mobilesurveyor.app.stops.SelectedStops;
import com.meyersj.mobilesurveyor.app.stops.Stop;
import com.meyersj.mobilesurveyor.app.stops.StopSearchAdapter;
import com.meyersj.mobilesurveyor.app.stops.StopSequenceAdapter;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class OnOffFragment extends MapFragment {

    private final String TAG = "OnOffMapActivity";

    //private final String ODK_ACTION = "com.meyersj.mobilesurveyor.app.ODK_ONOFFMAP";
    //private final String ONOFF_ACTION = "com.meyersj.mobilesurveyor.app.ONOFFMAP";

    // parameters for HTTP POST
    private String line;
    private String dir;

    // Views
    private AutoCompleteTextView stopName;
    private ImageButton clear;
    private View seqView;
    private ListView onSeqListView;
    private ListView offSeqListView;
    private Button stopSeqBtn;
    private Button toggleOnBtn;
    private Button toggleOffBtn;
    private TextView osmText;

    private SelectedStops selectedStops;
    private ArrayAdapter<Integer> countAdapter;
    private StopSequenceAdapter onSeqListAdapter;
    private StopSequenceAdapter offSeqListAdapter;
    private HttpClient client;

    //list of markers generated from geojson for current stop and direction
    private ArrayList<Marker> locList = new ArrayList<Marker>();
    //overlay on map that displays each stop
    private ItemizedIconOverlay locOverlay;
    //used to lookup marker when the stop name is selected from the search bar
    private HashMap<String, Marker> stopsMap;

    Boolean isOnReversed = false;
    Boolean isOffReversed = false;
    private ArrayList<Marker> locListOpposite = new ArrayList<Marker>();

    private ItemizedIconOverlay selOverlay;
    private ArrayList<Marker> selList = new ArrayList<Marker>();
    private BoundingBox bbox;
    protected SurveyManager manager;

    public OnOffFragment(SurveyManager manager, String line, String dir) {
        this.manager = manager;
        this.line = line;
        this.dir = dir;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d("ONOFFLIFE", "on create on off");
        view = inflater.inflate(R.layout.fragment_on_off_map, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        mv = (MapView) view.findViewById(R.id.mapview);
        setTiles(mv);

        clear = (ImageButton) view.findViewById(R.id.clear_input_stop);
        stopName = (AutoCompleteTextView) view.findViewById(R.id.input_stop);

        //TODO move to seperate class for all other posts
        client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();

        //10 second timeout
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);

        if (line != null && dir != null) {
            locList = getStops(line, dir, true);
            selList = new ArrayList<Marker>();

            // set listener for when marker tooltip is selected
            for (Marker marker: locList) {
                setToolTipListener(marker);
            }

            if(bbox != null) {
                mv.zoomToBoundingBox(bbox, true, false, true, true);
            }

            setItemizedOverlay(mv, locList, selList);
            mv.addListener(new OnOffMapListener(mv, locList, locOverlay));

            addRoute(context, line, dir, false);

            setupStopSequenceList();
            setupStopSearch();
            selectedStops = new SelectedStops(
                    context, onSeqListAdapter, offSeqListAdapter, selOverlay);

            //if line is a streetcar
            //enable on or off to be reversed because streetcar runs in a loop
            for (String streetcar: Cons.STREETCARS) {
                if (streetcar.equals(line)) {
                    Log.d(TAG, "we have a streetcar");
                    setupReverseStops();
                    break;
                }
            }
        }

        //setupODKSubmitAction();

        if (!Utils.isNetworkAvailable(context)) {
            Utils.longToastCenter(context,
                    "Please enable network connections.");
        }
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {}

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    // open stops geojson for current route
    // parse into ArrayList of markers
    // each marker contains stop description, stop id, stop sequence and LatLng
    protected ArrayList<Marker> getStops(String line, String dir, Boolean zoom) {
        String geoJSONName = line + "_" + dir + "_stops.geojson";
        BuildStops stops = new BuildStops(context, mv, "geojson/" + geoJSONName, dir);

        if(zoom) {
            bbox = stops.getBoundingBox();
            mv.zoomToBoundingBox(bbox, true, false, true, true);
        }

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


    /*
    protected void addRoute(String line, String dir) {
        String geoJSONName = line + "_" + dir + "_routes.geojson";

        Paint pathPaint = new Paint();
        pathPaint.setColor(getResources().getColor(R.color.black_light_light));
        pathPaint.setAntiAlias(true);
        pathPaint.setStrokeWidth(6.0f);
        pathPaint.setStyle(Paint.Style.STROKE);

        ArrayList<PathOverlay> paths = PathUtils.getPathFromAssets(activity, "geojson/" + geoJSONName);

        if (paths != null) {
            for(PathOverlay mPath: paths) {
                mPath.setPaint(pathPaint);
                mv.addOverlay(mPath);
            }
        }
    }
    */

    private void setupStopSequenceList() {
        seqView = view.findViewById(R.id.seq_list);
        stopSeqBtn = (Button) view.findViewById(R.id.stop_seq_btn);
        onSeqListView = (ListView) view.findViewById(R.id.on_stops_seq);
        offSeqListView = (ListView) view.findViewById(R.id.off_stops_seq);
        osmText = (TextView) view.findViewById(R.id.osm_text);

        /* if streetcar we need opposite direction stops in case
        user toggles that on or off was before start of line */

        ArrayList<Stop> stops = stopsSequenceSort(locList);

        onSeqListAdapter = new StopSequenceAdapter(activity, stops);
        offSeqListAdapter = new StopSequenceAdapter(activity, stops);

        stopSequenceAdapterSetup(onSeqListView, onSeqListAdapter);
        stopSequenceAdapterSetup(offSeqListView, offSeqListAdapter);

        stopSeqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSeqListVisibility(seqView.getVisibility());
            }
        });
    }


    private void setupStopSearch() {
        String[] stopNames = buildStopsArray(locList);
        //String[] stopNames =
        //      {"N Lombard TC MAX Station", "SW 6th & Madison St MAX Station","13123", "11512", ... };
        final ArrayList<String> stopsList = new ArrayList<String>();
        Collections.addAll(stopsList, stopNames);

        StopSearchAdapter adapter = new StopSearchAdapter
                (activity,android.R.layout.simple_list_item_1,stopsList);
        stopName.setAdapter(adapter);

        stopName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                stopName.setText("");
                closeKeypad();
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

    protected void selectLocType(final Marker selectedMarker) {
        Log.d(TAG, "select loc type");
        String message = selectedMarker.getTitle();

        final CharSequence[] items = {Cons.BOARD, Cons.ALIGHT};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
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
                        //check if choice (on-off) is being displayed in reverse
                        //if so switch to regular direction
                        //and highlight selected
                        String choice = selectedStops.getCurrentType();
                        Log.d(TAG, choice);

                        if(choice.equals(Cons.BOARD) && isOnReversed) {
                            reverseDirection(Cons.ON, isOnReversed);
                        }
                        if(choice.equals(Cons.ALIGHT) && isOffReversed) {
                            reverseDirection(Cons.OFF, isOffReversed);
                        }

                        manager.setStop(selectedMarker, choice);
                        selectedStops.saveCurrentMarker(selectedMarker);
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



    protected void setupReverseStops() {
        String dirOpposite = dir.equals("0") ? "1" : "0";
        locListOpposite = getStops(line, dirOpposite, false);

        toggleOnBtn = (Button) view.findViewById(R.id.on_btn);
        toggleOffBtn = (Button) view.findViewById(R.id.off_btn);

        Drawable onDrawable = context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_none);
        Drawable offDrawable = context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_none);

        toggleOnBtn.setBackground(onDrawable);
        toggleOffBtn.setBackground(offDrawable);

        toggleOnBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(isOffReversed) {
                    Utils.shortToastCenter(context, "Both on and off cannot have direction reversed");
                }
                else {
                    reverseDirection(Cons.ON, isOnReversed);
                }
                return true;
            }
        });
        toggleOffBtn.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if(isOnReversed) {
                    Utils.shortToastCenter(context, "Both on and off cannot have direction reversed");
                }
                else {
                    reverseDirection(Cons.OFF, isOffReversed);
                }
                return true;
            }
        });
    }

    /*
    private void setupODKSubmitAction() {
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (selectedStops.validateSelection()) {
                    String onStop = selectedStops.getBoard().getDescription();
                    String offStop = selectedStops.getAlight().getDescription();

                    if (selectedStops.validateStopSequence() || (isOnReversed || isOffReversed) ) {
                        exitWithStopIDs(onStop, offStop);
                    }
                    else {
                        Utils.longToastCenter(context,
                                "Invalid stop sequence based on route direction");
                    }
                }
                else {
                    Utils.longToastCenter(context,
                            "Both boarding and alighting locations must be set");
                }
            }
        });
    }
    */

    protected ArrayList<Stop> stopsSequenceSort(final ArrayList<Marker> locList) {
        ArrayList<Stop> stops = new ArrayList<Stop>();

        for(Marker marker: locList) {
            stops.add((Stop) marker);
        }
        Collections.sort(stops);

        return stops;
    }

    protected void stopSequenceAdapterSetup(final ListView listView, final StopSequenceAdapter adapter) {
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                adapter.setSelectedIndex(position);

                Stop stop = (Stop) adapterView.getAdapter().getItem(position);

                if (listView == onSeqListView) {
                    selectedStops.saveSequenceMarker(Cons.BOARD, stop);
                    manager.setStop(stop, Cons.BOARD);
                }
                else {
                    selectedStops.saveSequenceMarker(Cons.ALIGHT, stop);
                    manager.setStop(stop, Cons.ALIGHT);
                }
            }
        });
    }

    //toggle visibility of sequence list depending on current visibility
    private void changeSeqListVisibility(int currentVisibility) {
        osmText.setVisibility(currentVisibility);

        if (currentVisibility == View.INVISIBLE) {
            stopSeqBtn.setText("Hide stop sequences");
            seqView.setVisibility(View.VISIBLE);
            stopSeqBtn.setBackground(
                    context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_top));
        }
        else {
            seqView.setVisibility(View.INVISIBLE);
            stopSeqBtn.setText("Show stop sequences");
            stopSeqBtn.setBackground(
                    context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_all));
        }
    }


    protected void reverseDirection(String mode, Boolean isReversed) {
        if(mode.equals(Cons.ON)) {
            if(isReversed == false) {
                changeAdapter(onSeqListView, onSeqListAdapter, locListOpposite);
                isOnReversed = true;
                toggleOnBtn.setText("On Stop (Opposite Direction)");
            }
            else {
                changeAdapter(onSeqListView, onSeqListAdapter, locList);
                isOnReversed = false;
                toggleOnBtn.setText("On Stop");
            }
        }
        else {
            if(isReversed == false) {
                changeAdapter(offSeqListView, offSeqListAdapter, locListOpposite);
                isOffReversed = true;
                toggleOffBtn.setText("Off Stop (Opposite Direction)");
            }
            else {
                changeAdapter(offSeqListView, offSeqListAdapter, locList);
                isOffReversed = false;
                toggleOffBtn.setText("Off Stop");
            }
        }
    }

    private void changeAdapter(ListView listView, StopSequenceAdapter adapter, ArrayList<Marker> locList)  {
        ArrayList<Stop> stops = stopsSequenceSort(locList);
        if (adapter == onSeqListAdapter) {
            onSeqListAdapter = new StopSequenceAdapter(activity, stops);
            selectedStops.setOnAdapter(onSeqListAdapter);
            selectedStops.clearSequenceMarker(Cons.BOARD);
            stopSequenceAdapterSetup(listView, onSeqListAdapter);
        }
        else {
            offSeqListAdapter = new StopSequenceAdapter(activity, stops);
            selectedStops.setOffAdapter(offSeqListAdapter);
            selectedStops.clearSequenceMarker(Cons.ALIGHT);
            stopSequenceAdapterSetup(listView, offSeqListAdapter);
        }
    }
    public void closeKeypad() {
        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /*
    private boolean exitWithStopIDs(String onStop, String offStop) {
        Log.d(TAG, "exit: " + onStop + " " + offStop);
        //Intent intent = new Intent();
        //intent.putExtra(Cons.ODK_BOARD, onStop);
        //intent.putExtra(Cons.ODK_ALIGHT, offStop);
        //activity.setResult(activity.RESULT_OK, intent);
        //activity.finish();
        return true;
    }

    protected void postResults(String onStop, String offStop, Boolean isOnReversed, Boolean isOffReversed) {
        Log.d(TAG, "posting results");

        Date date = new Date();

        Bundle extras = new Bundle();
        //extras.putString(Cons.URL, url);
        extras.putString(Cons.LINE, line);
        extras.putString(Cons.DIR, dir);
        extras.putString(Cons.DATE, Utils.dateFormat.format(date));
        extras.putString(Cons.ON_STOP, onStop);
        extras.putString(Cons.OFF_STOP, offStop);

        extras.putString(Cons.TYPE, Cons.PAIR);
        extras.putString(Cons.ON_REVERSED, String.valueOf(isOnReversed));
        extras.putString(Cons.OFF_REVERSED, String.valueOf(isOffReversed));

        //Utils.appendCSV("stops", buildPairRow(extras));
        //String[] params = getPairParams(extras);
        //PostTask task = new PostTask();
        //task.execute(params);
    }


    protected String buildPairRow(Bundle bundle) {
        String row = "";
        row += bundle.getString(Cons.DATE) + ",";
        row += bundle.getString(Cons.USER_ID) + ",";
        row += bundle.getString(Cons.LINE) + ",";
        row += bundle.getString(Cons.DIR) + ",";
        row += bundle.getString(Cons.ON_STOP) + ",";
        row += bundle.getString(Cons.OFF_STOP) + ",";
        row += bundle.getString(Cons.ON_REVERSED) + ",";
        row += bundle.getString(Cons.OFF_REVERSED);
        return row;
    }

    protected String[] getPairParams(Bundle bundle) {
        String[] params = new String[2];
        JSONObject json = new JSONObject();
        json.put(Cons.USER_ID, bundle.getString(Cons.USER_ID));
        json.put(Cons.DATE, bundle.getString(Cons.DATE));
        json.put(Cons.LINE, bundle.getString(Cons.LINE));
        json.put(Cons.DIR, bundle.getString(Cons.DIR));
        json.put(Cons.ON_STOP, bundle.getString(Cons.ON_STOP));
        json.put(Cons.OFF_STOP, bundle.getString(Cons.OFF_STOP));
        json.put(Cons.ON_REVERSED, bundle.getString(Cons.ON_REVERSED));
        json.put(Cons.OFF_REVERSED, bundle.getString(Cons.OFF_REVERSED));
        params[0] = Utils.getUrlApi(context) + "/insertPair";
        Log.d(TAG, Utils.getUrlApi(context));
        params[1] = json.toJSONString();
        return params;
    }
    class PostTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            Log.d(TAG, "url:" + params[0]);
            Log.d(TAG, "data:" + params[1]);
            return post(params);
        }
        @Override
        protected void onPostExecute(String response) {
            Log.d(TAG, "onPostExecute(): " + response);
        }
    }


    protected String post(String[] params) {

        String retVal = null;
        HttpPost post = new HttpPost(params[0]);

        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Cons.DATA, params[1]));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);
            HttpEntity entityR = response.getEntity();
            Log.d(TAG, EntityUtils.toString(entityR));
            retVal = response.toString();

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException" + e.toString());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
        }
        return retVal;
    }
    */

}