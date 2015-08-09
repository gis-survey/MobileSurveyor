package com.meyersj.mobilesurveyor.app.stops;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mapbox.mapboxsdk.overlay.Marker;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;

import java.util.ArrayList;
import java.util.Collections;


public class StopsSequences {

    private Activity activity;
    private Button showSequences;
    private Boolean isDisplayed = false;
    private LinearLayout sequencesLayout;
    private ArrayList<Marker> stopsList = new ArrayList<Marker>();
    private ListView onList;
    private ListView offList;
    private StopsManager manager;
    private StopSequenceAdapter onSeqListAdapter;
    private StopSequenceAdapter offSeqListAdapter;

    public StopsSequences(Activity activity, ArrayList<Marker> stopsList, StopsManager manager) {
        this.activity = activity;
        this.manager = manager;
        showSequences = (Button) activity.findViewById(R.id.show_sequences);
        sequencesLayout = (LinearLayout) activity.findViewById(R.id.sequences_layout);
        onList = (ListView) activity.findViewById(R.id.on_stops_list);
        offList = (ListView) activity.findViewById(R.id.off_stops_list);
        this.stopsList = stopsList;

        showSequences.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        setupLists();
    }

    public void toggle() {
        if(isDisplayed) hide();
        else show();
    }

    public void show() {
        sequencesLayout.setVisibility(View.VISIBLE);
        isDisplayed = true;
    }

    public void hide() {
        sequencesLayout.setVisibility(View.GONE);
        isDisplayed = false;
    }


    private void setupLists() {

        ArrayList<Stop> stops = stopsSequenceSort(stopsList);

        onSeqListAdapter = new StopSequenceAdapter(activity, stops);
        offSeqListAdapter = new StopSequenceAdapter(activity, stops);

        stopSequenceAdapterSetup(onList, onSeqListAdapter);
        stopSequenceAdapterSetup(offList, offSeqListAdapter);

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

                if (listView == onList) {
                    manager.saveSequenceMarker(Cons.BOARD, stop);
                }
                else {
                    manager.saveSequenceMarker(Cons.ALIGHT, stop);
                }
            }
        });
    }

    public StopSequenceAdapter getOnAdapter() {
        return onSeqListAdapter;
    }

    public StopSequenceAdapter getOffAdapter() {
        return offSeqListAdapter;
    }

}
