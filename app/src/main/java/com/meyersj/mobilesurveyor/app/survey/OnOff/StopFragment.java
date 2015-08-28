package com.meyersj.mobilesurveyor.app.survey.OnOff;

import android.app.Activity;
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
    @Bind(R.id.stops_seq_list) ListView seqListView;
    //@Bind(R.id.on_stops_seq) ListView seqListView;
    //@Bind(R.id.off_stops_seq) ListView offSeqListView;
    @Bind(R.id.stop_seq_btn) Button stopSeqBtn;


    //private Button toggleOnBtn;
    //private Button toggleOffBtn;
    //private TextView osmText;

    private OnOffMapListener mapListener;
    private SelectedStops selectedStops;
    private StopSequenceAdapter stopSequenceAdapter;
    //private StopSequenceAdapter offSeqListAdapter;

    //private ArrayList<Marker> locList = new ArrayList<Marker>();
    private ArrayList<Marker> stopsList = new ArrayList<Marker>();
    //private ArrayList<Marker> alightStopsList = new ArrayList<Marker>();


    private ItemizedIconOverlay stopsOverlay;
    //private ItemizedIconOverlay alightOverlay;
    private ItemizedIconOverlay selOverlay;

    private ArrayList<Marker> selList = new ArrayList<Marker>();

    private HashMap<String, Marker> stopsMap;



    private BoundingBox bbox;
    protected SurveyManager manager;
    protected Bundle extras;
    protected String line;
    protected String dir;
    protected String mode;
    protected String stopID;


    public void initialize(SurveyManager manager, Bundle extras, String mode, SelectedStops selectedStops) {
        this.manager = manager;
        this.extras = extras;
        this.selectedStops = selectedStops;
        line = extras != null ? extras.getString(Cons.LINE, Cons.DEFAULT_RTE) : Cons.DEFAULT_RTE;
        dir = extras != null ? extras.getString(Cons.DIR, Cons.DEFAULT_DIR) : Cons.DEFAULT_DIR;
        this.mode = mode;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_stop_map, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        ButterKnife.bind(this, view);
        mapListener = new OnOffMapListener(mv);
        setTiles(mv);
        setupStops();
        restoreState();
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

    private void setupStops() {
        clearRoutes();
        //if (mapListener != null) mv.removeListener(mapListener);
        //if (stopsOverlay != null) mv.removeOverlay(stopsOverlay);
        if (selOverlay != null) {
            mv.removeOverlay(selOverlay);
        }

        String[] route;
        if(mode.equals(Cons.BOARD))
            route = manager.getFirstRoute();
        else
            route = manager.getLastRoute();
        addTransferRoute(context, route[0], route[1]);
        zoomToRoute(mv, route[0], route[1]);
        stopsList = getStops(route[0], route[1], false);
        setItemizedOverlay();
        mapListener.setMarkers(stopsList);
        mapListener.setOverlay(stopsOverlay);
        mv.addListener(mapListener);
        setupStopSequenceList();
        setupStopSearch();
        selectedStops.setAdapter(stopSequenceAdapter, mode);
        selectedStops.setOverlay(selOverlay, mode);
    }


    protected void setItemizedOverlay() {
        stopsOverlay = new ItemizedIconOverlay(mv.getContext(), stopsList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        selectStop(item.getDescription(), mode);
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
        mv.addItemizedOverlay(selOverlay);
    }

    private void setupStopSequenceList() {
        //osmText = (TextView) view.findViewById(R.id.osm_text);
        ArrayList<Stop> stops = stopsSequenceSort(stopsList);
        stopSequenceAdapter = new StopSequenceAdapter(activity, stops);
        stopSequenceAdapterSetup(seqListView, stopSequenceAdapter);
        stopSeqBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeSeqListVisibility(seqView.getVisibility());
            }
        });
    }



    private void setupStopSearch() {
        String[] stopNames = buildStopsArray(stopsList);
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
                selectStop(stopsMap.get(stopsList.get(position)).getDescription(), mode);
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
                selectStop(stop.getDescription(), mode);
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

    protected void restoreState() {
        if(extras == null) return;

        if(mode.equals(Cons.BOARD))
            this.stopID = extras.getString(Cons.BOARD_ID_ODK, null);
        else
            this.stopID = extras.getString(Cons.ALIGHT_ID_ODK, null);
        if(this.stopID != null)
            selectStop(this.stopID, mode);
    }

    protected void selectStop(String stopID, String mode) {
        if(stopID == null || stopID.isEmpty()) return;
        Marker marker = null;
        Integer index = null;

        ArrayList<Stop> sortedLocList = stopsSequenceSort(stopsList);
        for(int i = 0; i < sortedLocList.size(); i++) {
            Stop stop = sortedLocList.get(i);

            if(stop.getDescription().equals(stopID)) {
                marker = stop;
                index = i;
            }
        }

        if(marker != null) {
            stopSequenceAdapter.setSelectedIndex(index);
            selectedStops.saveSequenceMarker(mode, marker);
            manager.setStop(marker, mode);
        }
    }

    public void updateView(SurveyManager manager) {
        setupStops();
        mv.removeOverlay(surveyOverlay);
        surveyOverlay.removeAllItems();
        Marker location = null;
        if(mode.equals(Cons.BOARD)) {
            location = manager.getOrig();
        }
        else {
            location = manager.getDest();
        }
        if(location != null) surveyOverlay.addItem(location);
        mv.addItemizedOverlay(surveyOverlay);
        selectStop(manager.getStopID(mode), mode);
    }


}

