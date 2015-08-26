package com.meyersj.mobilesurveyor.app.survey.OnOff;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;

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
import com.meyersj.mobilesurveyor.app.survey.MapFragment;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;

public class StopFragment extends MapFragment {

    private final String TAG = getClass().getCanonicalName();


    @Bind(R.id.mapview) MapView mv;
    @Bind(R.id.seq_list) View seqView;
    @Bind(R.id.input_stop) AutoCompleteTextView stopName;
    @Bind(R.id.clear_input_stop) ImageButton clear;
    @Bind(R.id.on_stops_seq) ListView onSeqListView;
    @Bind(R.id.off_stops_seq) ListView offSeqListView;
    @Bind(R.id.stop_seq_btn) Button stopSeqBtn;


    //private Button toggleOnBtn;
    //private Button toggleOffBtn;
    //private TextView osmText;

    private SelectedStops selectedStops;
    private StopSequenceAdapter onSeqListAdapter;
    private StopSequenceAdapter offSeqListAdapter;

    private ArrayList<Marker> locList = new ArrayList<Marker>();
    private ArrayList<Marker> boardStopsList = new ArrayList<Marker>();
    private ArrayList<Marker> alightStopsList = new ArrayList<Marker>();


    private ItemizedIconOverlay boardOverlay;
    private ItemizedIconOverlay alightOverlay;
    private ItemizedIconOverlay selOverlay;

    private ArrayList<Marker> selList = new ArrayList<Marker>();

    private HashMap<String, Marker> stopsMap;


    //Boolean isOnReversed = false;
    //Boolean isOffReversed = false;
    //private ArrayList<Marker> locListOpposite = new ArrayList<Marker>();


    private BoundingBox bbox;
    protected SurveyManager manager;
    protected Bundle extras;
    protected String line;
    protected String dir;


    public void initialize(SurveyManager manager, Bundle extras) {
        this.manager = manager;
        this.extras = extras;
        line = extras != null ? extras.getString(Cons.LINE, Cons.DEFAULT_RTE) : Cons.DEFAULT_RTE;
        dir = extras != null ? extras.getString(Cons.DIR, Cons.DEFAULT_DIR) : Cons.DEFAULT_DIR;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_on_off_map, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        ButterKnife.bind(this, view);
        setTiles(mv);

        // get first and last route from manager
        // if view was first created it would be default route/direction

        // board stops
        // alight stops

        //if (line != null && dir != null) {

        setupStops();


        //locList = getStops(line, dir, true);

        //selList = new ArrayList<Marker>();



        /*
            // set listener for when marker tooltip is selected
            for (Marker marker: locList) {
                setToolTipListener(marker);
            }
            if(bbox != null) {
                mv.zoomToBoundingBox(bbox, true, false, true, true);
            }
            setItemizedOverlay(mv, locList, selList);
            mv.addListener(new OnOffMapListener(mv, locList, locOverlay));

            addDefaultRoute(context, line, dir);
            setupStopSequenceList();
            setupStopSearch();
            selectedStops = new SelectedStops(
                    context, onSeqListAdapter, offSeqListAdapter, selOverlay);
            //if line is a streetcar
            //enable on or off to be reversed because streetcar runs in a loop
            //for (String streetcar: Cons.STREETCARS) {
            //    if (streetcar.equals(line)) {
            //        setupReverseStops();
            //        break;
            //    }
            //}
            zoomToRoute(mv);
        }
        */

        //restoreState();
        return view;
    }

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
        Log.d(TAG, geoJSONName);
        BuildStops stops = new BuildStops(context, mv, "geojson/" + geoJSONName, dir);
        if(zoom) {
            Log.d(TAG, "getting bounding box");
            bbox = stops.getBoundingBox();
            if(bbox != null)
                mv.zoomToBoundingBox(bbox, true, false, true, true);
        }
        return stops.getStops();
    }

    //modify mView for each toolTip in each marker to prevent closing it when touched
    protected void setToolTipListener(final Marker marker, final String mode) {
        View mView = marker.getToolTip(mv).getView();
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
                selectLocType(marker);
                selectedStops.setCurrentMarker(marker, mode);
                return true;
            }
        });
    }

    private void setupStops() {
        addDefaultRoute(context, line, dir);
        String[] first = manager.getFirstRoute();
        String[] last = manager.getLastRoute();
        boardStopsList = getStops(first[0], first[1], false);
        alightStopsList = getStops(last[0], last[1], false);
        for (Marker marker: boardStopsList) {
            setToolTipListener(marker, Cons.BOARD);
        }
        for (Marker marker: alightStopsList) {
            setToolTipListener(marker, Cons.ALIGHT);
        }
        setItemizedOverlay();
        mv.addListener(new OnOffMapListener(mv, boardStopsList, boardOverlay));
        mv.addListener(new OnOffMapListener(mv, alightStopsList, alightOverlay));

        setupStopSequenceList();
        //setupStopSearch();
        selectedStops = new SelectedStops(
                context, onSeqListAdapter, offSeqListAdapter, selOverlay);
    }


    protected void setItemizedOverlay() {
        boardOverlay = new ItemizedIconOverlay(mv.getContext(), boardStopsList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        selectLocType(item);
                        //manager.setStop(item, Cons.BOARD);
                        //selectedStops.saveCurrentMarker(item);
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );
        alightOverlay = new ItemizedIconOverlay(mv.getContext(), alightStopsList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        selectLocType(item);
                        //manager.setStop(item, Cons.ALIGHT);
                        //selectedStops.saveCurrentMarker(item);
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
                return false;
            }

            @Override
            public boolean onItemLongPress(int i, Marker marker) {
                return false;
            }
        });
        //mv.addItemizedOverlay(boardOverlay);
        //mv.addItemizedOverlay(alightOverlay);
        mv.addItemizedOverlay(selOverlay);
    }

    private void setupStopSequenceList() {
        //seqView = view.findViewById(R.id.seq_list);
        //stopSeqBtn = (Button) view.findViewById(R.id.stop_seq_btn);
        //onSeqListView = (ListView) view.findViewById(R.id.on_stops_seq);
        //offSeqListView = (ListView) view.findViewById(R.id.off_stops_seq);
        //osmText = (TextView) view.findViewById(R.id.osm_text);
        /* if streetcar we need opposite direction stops in case
        /* if streetcar we need opposite direction stops in case
        user toggles that on or off was before start of line */
        ArrayList<Stop> boardStops = stopsSequenceSort(boardStopsList);
        ArrayList<Stop> alightStops = stopsSequenceSort(alightStopsList);
        onSeqListAdapter = new StopSequenceAdapter(activity, boardStops);
        offSeqListAdapter = new StopSequenceAdapter(activity, alightStops);
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
                Utils.closeKeypad(activity);
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
                        selectedStops.setCurrentMarker(selectedMarker, choice);
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String choice = selectedStops.getCurrentType();
                        Log.d(TAG, choice);
                        manager.setStop(selectedMarker, choice);
                        selectedStops.saveCurrentMarker(selectedMarker);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedStops.clearCurrentMarker();
                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }

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
                } else {
                    selectedStops.saveSequenceMarker(Cons.ALIGHT, stop);
                    manager.setStop(stop, Cons.ALIGHT);
                }
            }
        });
    }

    //toggle visibility of sequence list depending on current visibility
    private void changeSeqListVisibility(int currentVisibility) {
        //osmText.setVisibility(currentVisibility);
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

    /*
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
    */

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


    protected void restoreState() {
        if(extras == null)
            return;
        Integer boardID = extras.getInt("board_id", 0);
        Integer alightID = extras.getInt("alight_id", 0);
        selectStops(boardID, alightID);
    }

    protected void selectStops(Integer boardID, Integer alightID) {
        Marker[] marker = new Marker[2];
        Integer[] index = new Integer[2];
        ArrayList<Stop> sortedLocList = stopsSequenceSort(locList);

        for(int i = 0; i < sortedLocList.size(); i++) {
            Stop stop = sortedLocList.get(i);

            if(stop.getDescription().equals(String.valueOf(boardID))) {
                marker[0] = stop;
                index[0] = i;
            }
            if(stop.getDescription().equals(String.valueOf(alightID))) {
                marker[1] = stop;
                index[1] = i;
            }
        }
        if(index[0] != null && index[1] != null) {
            onSeqListAdapter.setSelectedIndex(index[0]);
            offSeqListAdapter.setSelectedIndex(index[1]);
            selectedStops.saveSequenceMarker(Cons.BOARD, marker[0]);
            selectedStops.saveSequenceMarker(Cons.ALIGHT, marker[1]);
            manager.setStop(marker[0], Cons.BOARD);
            manager.setStop(marker[1], Cons.ALIGHT);
        }
    }

    public void updateView(SurveyManager manager) {
        Log.d(TAG, manager.toString());
        setupStops();
        /*
        mv.removeOverlay(surveyOverlay);
        surveyOverlay.removeAllItems();
        Marker orig = manager.getOrig();
        Marker dest = manager.getDest();
        Marker onStop = manager.getOnStop();
        Marker offStop = manager.getOffStop();
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
        mv.addItemizedOverlay(surveyOverlay);
        */
    }



}

