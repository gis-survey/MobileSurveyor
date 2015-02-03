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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;

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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;


public class TransfersMapFragment extends MapFragment {

    protected final int MAX_TRANSFERS = 5;
    protected SurveyManager manager;
    protected LinearLayout routesLayout;
    //protected ListView listView;
    protected Button transfersBtn;
    //protected LinearLayout routesLayout;
    protected ArrayList<Spinner> routeSpinners;
    protected ArrayList<String> routeList;
    protected HashMap<String, String> routeLookup;
    //protected ArrayList<String> selectedRoutes;
    protected String[] routes;
    protected ViewPager pager;
    protected Bundle extras;
    protected String[] selectedRoutes;
    //protected int transfersCount = 0;

    public TransfersMapFragment(SurveyManager manager, ViewPager pager, Bundle extras) {
        this.manager = manager;
        this.pager = pager;
        this.extras = extras;
        this.selectedRoutes = new String[MAX_TRANSFERS];
    }



    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_transfers_map, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        routesLayout = (LinearLayout) view.findViewById(R.id.routes_list_layout);
        mv = (MapView) view.findViewById(R.id.mapview);
        setTiles(mv);

        transfersBtn = (Button) view.findViewById(R.id.transfers_btn);
        routes = activity.getResources().getStringArray(R.array.transfer_routes);
        final ArrayList<String> routesList = new ArrayList<String>();
        for(String route: routes) {
            routesList.add(route);
        }

        if(extras != null) {
            Log.d(TAG, "extras not null");
            if (extras.containsKey(Cons.LINE) && extras.containsKey(Cons.DIR)) {
                Log.d(TAG, "has line info");
                line = extras.getString(Cons.LINE);
                dir = extras.getString(Cons.DIR);
                Log.d(TAG, "rte: " + line + "-" + dir);
            }
        }
        else {
            line = "9";
            dir = "1";
        }
        addDefaultRoute(context, line, dir);
        String[] rte = new String[] {line, dir};

        manager.setTransfers(selectedRoutes);
        final RoutePicker rp1 = new RoutePicker(this, context, inflater, container,
                routesLayout, routesList, line, true, 1, selectedRoutes, rte);
        final RoutePicker rp2 = new RoutePicker(this, context, inflater, container,
                routesLayout, routesList, line, false, 2, selectedRoutes, rte);
        final RoutePicker rp3 = new RoutePicker(this, context, inflater, container,
                routesLayout, routesList, line, false, 3, selectedRoutes, rte);
        final RoutePicker rp4 = new RoutePicker(this, context, inflater, container,
                routesLayout, routesList, line, false, 4, selectedRoutes, rte);
        final RoutePicker rp5 = new RoutePicker(this, context, inflater, container,
                routesLayout, routesList, line, false, 5, selectedRoutes, rte);

        rp1.setNext(rp2);
        rp2.setNext(rp3);
        rp3.setNext(rp4);
        rp4.setNext(rp5);
        rp5.setNext(null);

        rp1.setPrevious(null);
        rp2.setPrevious(rp1);
        rp3.setPrevious(rp2);
        rp4.setPrevious(rp3);
        rp5.setPrevious(rp4);


        changeListVisibility(routesLayout.getVisibility());

        transfersBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeListVisibility(routesLayout.getVisibility());
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
