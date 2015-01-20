package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class TransfersMapFragment extends MapFragment {

    protected SurveyManager manager;
    protected String line;
    protected String dir;

    protected View routesLayout;
    protected ListView listView;
    protected Button transfersBtn;
    protected Button submit;

    protected static ArrayAdapter<String> routesAdapter;
    protected HashMap<String, String> routeLookup;
    protected HashMap<String, String> routeLookupReverse;
    protected ArrayList<String> selectedRoutes;
    protected String[] routes;

    public TransfersMapFragment(SurveyManager manager, String line, String dir) {
        this.manager = manager;
        this.line = line;
        this.dir = dir;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_transfers_map, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        mv = (MapView) view.findViewById(R.id.mapview);
        setTiles(mv);
        addRoute(context, line, dir, false);

        // set 'line' item to checked and disabled

        transfersBtn = (Button) view.findViewById(R.id.transfers_btn);
        listView = (ListView) view.findViewById(R.id.routes_list_view);
        routesLayout = (View) view.findViewById(R.id.routes_list_layout);
        submit = (Button) view.findViewById(R.id.submit_btn);

        buildRouteLookup();

        routes = activity.getResources().getStringArray(R.array.lines);
        ArrayList<String> routesList = new ArrayList<String>();
        for(String route: routes) {
            routesList.add(route);
            if(routeLookup.get(route) == line) {
                for(int i = 0; i < routes.length; i++) {
                    if(routes[i].equals(route)) {
                        //same route as default, save index to check its box then disable it
                    }
                }
            }
        }
        //RoutesAdapter routesAdapter = new RoutesAdapter(context, routesList);
        routesAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_list_item_multiple_choice, routes);
        listView.setAdapter(routesAdapter);
        changeListVisibility(routesLayout.getVisibility());

        //listView.setSelection();

        transfersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeListVisibility(routesLayout.getVisibility());

                if(routesLayout.getVisibility() == View.INVISIBLE) {
                    SparseBooleanArray checked = listView.getCheckedItemPositions();

                    clearRoutes();
                    selectedRoutes = new ArrayList<String>();
                    for (int i = 0; i < checked.size(); i++) {
                        int position = checked.keyAt(i);
                        if (checked.valueAt(i)) {
                            selectedRoutes.add(routesAdapter.getItem(position));
                        }
                    }
                    addRoutes();


                }
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check that all the data has been entered

                //return data back to odk ad exit?



            }
        });

        //listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        //listView.setItemsCanFocus(false);
        //listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
        //    public void onItemClick(AdapterView<?> myAdapter, View myView, int myItemInt, long mylng) {
        //        String selectedFromList = (String) listView.getItemAtPosition(myItemInt);
        //        Log.d(TAG, selectedFromList);
        //    }
        //});

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

    protected void addRoutes() {
        for(String s: selectedRoutes) {
            String routeID = routeLookup.get(s);
            addRoute(context, routeID, dir, true);
        }
    }

    protected void buildRouteLookup() {
        routeLookup = new HashMap<String, String>();
        routeLookup.put("4-Division/Fessenden", "4");
        routeLookup.put("9-Powell Blvd", "9");
        routeLookup.put("17-Holgate/Broadway", "17");
        routeLookup.put("19-Woodstock/Glisan", "19");
        routeLookup.put("28-Linwood", "28");
        routeLookup.put("29-Lake/Webster Rd", "29");
        routeLookup.put("30-Estacada", "30");
        routeLookup.put("31-King Rd", "31");
        routeLookup.put("32-Oatfield", "32");
        routeLookup.put("33-McLoughlin", "33");
        routeLookup.put("34-River Rd", "34");
        routeLookup.put("35-Macadam/Greeley", "35");
        routeLookup.put("70-12th/NE 33rd Ave", "70");
        routeLookup.put("75-Cesar Chavez/Lombard", "75");
        routeLookup.put("152-Milwaukie", "152");
        routeLookup.put("MAX Yellow Line", "190");
        routeLookup.put("MAX Green Line", "200");
        routeLookup.put("Portland Streetcar - NS Line", "193");
        routeLookup.put("Portland Streetcar - CL Line", "194");

        routeLookupReverse = new HashMap<String, String>();
        routeLookupReverse.put("4", "4-Division/Fessenden");
        routeLookupReverse.put("9", "9-Powell Blvd");
        routeLookupReverse.put("17", "17-Holgate/Broadway");
        routeLookupReverse.put("19", "19-Woodstock/Glisan");
        routeLookupReverse.put("28", "28-Linwood");
        routeLookupReverse.put("29", "29-Lake/Webster Rd");
        routeLookupReverse.put("30", "30-Estacada");
        routeLookupReverse.put("31", "31-King Rd");
        routeLookupReverse.put("32", "32-Oatfield");
        routeLookupReverse.put("33", "33-McLoughlin");
        routeLookupReverse.put("34", "34-River Rd");
        routeLookupReverse.put("35", "35-Macadam/Greeley");
        routeLookupReverse.put("70", "70-12th/NE 33rd Ave");
        routeLookupReverse.put("75", "75-Cesar Chavez/Lombard");
        routeLookupReverse.put("152", "152-Milwaukie");
        routeLookupReverse.put("190", "MAX Yellow Line");
        routeLookupReverse.put("200", "MAX Green Line");
        routeLookupReverse.put("193", "Portland Streetcar - NS Line");
        routeLookupReverse.put("194", "Portland Streetcar - CL Line");
    }

    //toggle visibility of list depending on current visibility
    private void changeListVisibility(int currentVisibility) {
        if (currentVisibility == View.INVISIBLE) {
            routesLayout.setVisibility(View.VISIBLE);
            transfersBtn.setBackground(
                    context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_top));
        }
        else {
            routesLayout.setVisibility(View.INVISIBLE);
            transfersBtn.setBackground(
                    context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_all));
        }
    }
}
