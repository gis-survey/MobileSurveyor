package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class TransfersMapFragment extends MapFragment {

    //protected final int MAX_TRANSFERS = 4;
    protected SurveyManager manager;
    protected View routesLayout;
    protected ListView listView;
    protected Button transfersBtn;
    protected Button submit;
    protected static ArrayAdapter routesAdapter;
    protected HashMap<String, String> routeLookup;
    protected ArrayList<String> selectedRoutes;
    protected String[] routes;
    protected ViewPager pager;
    protected int transfersCount = 0;

    public TransfersMapFragment(SurveyManager manager, ViewPager pager) {
        this.manager = manager;
        this.pager = pager;
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
        transfersBtn = (Button) view.findViewById(R.id.transfers_btn);
        listView = (ListView) view.findViewById(R.id.routes_list_view);
        routesLayout = (View) view.findViewById(R.id.routes_list_layout);
        submit = (Button) view.findViewById(R.id.submit_btn);
        buildRouteLookup();
        routes = activity.getResources().getStringArray(R.array.lines);
        final ArrayList<String> routesList = new ArrayList<String>();
        // don't add current route to transfers list
        for(String route: routes) {
            if(!routeLookup.get(route).equals(line)) {
                routesList.add(route);
            }
        }
        routesAdapter = new ArrayAdapter<String>(view.getContext(),
                android.R.layout.simple_list_item_multiple_choice, routesList);
        listView.setAdapter(routesAdapter);
        changeListVisibility(routesLayout.getVisibility());

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String routeID = routeLookup.get(routesList.get(i));
                CheckedTextView item = (CheckedTextView) view;
                if(transfersCount == Cons.MAX_TRANSFERS && item.isChecked()) {
                    String msg = "Maximum of " + String.valueOf(Cons.MAX_TRANSFERS) + " allowed";
                    Utils.shortToastCenter(context, msg);
                    listView.setItemChecked(i, false);
                }
                else if(!item.isChecked()) {
                    clearRoute(routeID, dir);
                    manager.removeTransfer(routeID);
                    transfersCount -= 1;
                }
                else {
                    addRoute(context, routeID, dir, true);
                    transfersCount += 1;
                    manager.updateTransfer(routeID);
                }
            }
        });

        transfersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeListVisibility(routesLayout.getVisibility());
            }
        });

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitWithSurveyBundle(true);
            }
        });
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

    protected void exitWithSurveyBundle(Boolean valid) {
        Boolean[] validate = manager.validate();
        Log.d(TAG, validate.toString());
        for(int i = 0; i < validate.length; i++) {
            Log.d(TAG, validate[i].toString());
            if(!validate[i]) {
                String msg = "";
                switch(i) {
                    case 0:
                        msg = "missing information about origin location";
                        break;
                    case 1:
                        msg = "missing information about destination location";
                        break;
                    case 2:
                        msg = "on and off locations are incomplete";
                        break;
                }
                Utils.shortToastCenter(context, msg);
                pager.setCurrentItem(i);
                //TODO change previous and next buttons being enabled/disabled
                return;
            }
        }

        int result;
        Intent intent = new Intent();
        if (valid) {
            intent = manager.addExtras(intent);
            result = activity.RESULT_OK;
        }
        else {
            result = activity.RESULT_CANCELED;
        }
        activity.setResult(result, intent);
        activity.finish();
    }

    //TODO remove this hardcoded building a hashtable to using an input file
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
        routeLookup.put("99-McLoughlin Express", "99");
        routeLookup.put("152-Milwaukie", "152");
        routeLookup.put("MAX Yellow Line", "190");
        routeLookup.put("MAX Green Line", "200");
        routeLookup.put("Portland Streetcar - NS Line", "193");
        routeLookup.put("Portland Streetcar - CL Line", "194");
        routeLookup.put("4", "4-Division/Fessenden");
        routeLookup.put("9", "9-Powell Blvd");
        routeLookup.put("17", "17-Holgate/Broadway");
        routeLookup.put("19", "19-Woodstock/Glisan");
        routeLookup.put("28", "28-Linwood");
        routeLookup.put("29", "29-Lake/Webster Rd");
        routeLookup.put("30", "30-Estacada");
        routeLookup.put("31", "31-King Rd");
        routeLookup.put("32", "32-Oatfield");
        routeLookup.put("33", "33-McLoughlin");
        routeLookup.put("34", "34-River Rd");
        routeLookup.put("35", "35-Macadam/Greeley");
        routeLookup.put("70", "70-12th/NE 33rd Ave");
        routeLookup.put("75", "75-Cesar Chavez/Lombard");
        routeLookup.put("99", "99-McLoughlin Express");
        routeLookup.put("152", "152-Milwaukie");
        routeLookup.put("190", "MAX Yellow Line");
        routeLookup.put("200", "MAX Green Line");
        routeLookup.put("193", "Portland Streetcar - NS Line");
        routeLookup.put("194", "Portland Streetcar - CL Line");
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
