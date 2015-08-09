package com.meyersj.mobilesurveyor.app.stops;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.mapbox.mapboxsdk.overlay.Marker;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by jeff on 8/8/15.
 */
public class StopsSearch {

    private Activity activity;
    private StopsManager manager;
    private AutoCompleteTextView inputStopName;
    private HashMap<String, Marker> stopsMap;
    ArrayList<String> stopNames;


    public StopsSearch (final Activity activity, ArrayList<Marker> stopsList, StopsManager manager) {
        this.activity = activity;
        this.manager = manager;
        inputStopName = (AutoCompleteTextView) activity.findViewById(R.id.search_stop);
        stopsMap = buildStopsMap(stopsList);
        stopNames = buildStopNames(stopsMap);
        setupListeners();

    }

    protected HashMap<String, Marker> buildStopsMap(ArrayList<Marker> locList) {

        HashMap<String, Marker> stopsMap = new HashMap<String, Marker>();
        for(Marker m: locList) {
            stopsMap.put(m.getTitle(), m);
            stopsMap.put(m.getDescription(), m);
        }
        return stopsMap;
    }


    protected ArrayList<String> buildStopNames(HashMap<String, Marker> map) {
        String[] stopNames = new String[map.size()];
        Integer i = 0;
        for (String key : map.keySet()) {
            stopNames[i] = key;
            i += 1;
        }

        ArrayList<String> stopsNamesList = new ArrayList<String>();
        Collections.addAll(stopsNamesList, stopNames);

        return stopsNamesList;
    }


    private void setupListeners() {
        StopSearchAdapter adapter = new StopSearchAdapter(activity, android.R.layout.simple_list_item_1, stopNames);
        inputStopName.setAdapter(adapter);
        inputStopName.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                inputStopName.setText("");
                closeKeypad();
                selectLocType(stopsMap.get(stopNames.get(position)));
            }
        });
    }

    public void closeKeypad() {
        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }



    protected void selectLocType(final Marker selectedMarker) {
        String message = selectedMarker.getTitle();

        final CharSequence[] items = {Cons.BOARD, Cons.ALIGHT};

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(message)
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        manager.setCurrentMarker(selectedMarker, items[i].toString());
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

}
