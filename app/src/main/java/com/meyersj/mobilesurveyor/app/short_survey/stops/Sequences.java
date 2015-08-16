/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.mobilesurveyor.app.short_survey.stops;

import android.app.Activity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.mapbox.mapboxsdk.overlay.Marker;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.short_survey.stops.helpers.SequenceAdapter;
import com.meyersj.mobilesurveyor.app.util.Cons;

import java.util.ArrayList;
import java.util.Collections;


public class Sequences {

    private Activity activity;
    private Button showSequences;
    private Boolean isDisplayed = false;
    private LinearLayout sequencesLayout;
    private ArrayList<Marker> stopsList = new ArrayList<Marker>();
    private ListView onList;
    private ListView offList;
    private Manager manager;
    private SequenceAdapter onSeqListAdapter;
    private SequenceAdapter offSeqListAdapter;

    public Sequences(Activity activity, ArrayList<Marker> stopsList, Manager manager) {
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

        onSeqListAdapter = new SequenceAdapter(activity, stops);
        offSeqListAdapter = new SequenceAdapter(activity, stops);

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

    protected void stopSequenceAdapterSetup(final ListView listView, final SequenceAdapter adapter) {

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

    public SequenceAdapter getOnAdapter() {
        return onSeqListAdapter;
    }

    public SequenceAdapter getOffAdapter() {
        return offSeqListAdapter;
    }

}
