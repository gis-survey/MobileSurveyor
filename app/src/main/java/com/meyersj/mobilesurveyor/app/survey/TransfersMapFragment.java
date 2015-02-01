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
    //protected int transfersCount = 0;

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
        routesLayout = (LinearLayout) view.findViewById(R.id.routes_list_layout);
        mv = (MapView) view.findViewById(R.id.mapview);
        setTiles(mv);
        addDefaultRoute(context, line, dir);
        transfersBtn = (Button) view.findViewById(R.id.transfers_btn);
        routes = activity.getResources().getStringArray(R.array.lines);
        final ArrayList<String> routesList = new ArrayList<String>();
        for(String route: routes) {
            routesList.add(route);
        }
        Stack<RoutePicker> routePickersStack = new Stack<RoutePicker>();

        //ArrayList<String> defaultRoutesList = (ArrayList<String>) routesList.clone();
        //defaultRoutesList.add(0, "Add route");
        //RoutePicker rp1 = new RoutePicker(context, inflater, container,
        //        routesLayout, routesList, line);

        routePickersStack.push(new RoutePicker(context, inflater, container,
                routesLayout, routesList, line));

        //RoutePicker rp2 = new RoutePicker(context, inflater, container,
        //        routesLayout, routesList, null);
        //RoutePicker rp3 = new RoutePicker(context, inflater, container,
        //        routesLayout, routesList, null);
        //RoutePicker rp4 = new RoutePicker(context, inflater, container,
        //        routesLayout, routesList, null);
        //RoutePicker rp5 = new RoutePicker(context, inflater, container,
        //        routesLayout, routesList, null);


        /*

        1)  Add one route and set it to the route currently being surveyed

        2)  If a user opens the first spinner and selects a different route

            inflate a new routePicker view and setup its spinner to None state

        3)  If a user taps CROSS then that transfer is removed.

        4) If a user selects a route from the route that was selected as default

            remove the default message from the list and return to step 2

        5) When user taps "next"|<changes tabs>|<closes routes list> validate that the default
            is still selected if not add error message

         */

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


    /*
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
    */

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
