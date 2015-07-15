/*
 * Copyright © 2015 Jeffrey Meyers
 * This program is released under the "MIT License".
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

package com.meyersj.mobilesurveyor.app.survey.Transfer;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.survey.MapFragment;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;
import com.meyersj.mobilesurveyor.app.survey.Transfer.RoutePicker;
import com.meyersj.mobilesurveyor.app.util.Cons;

import java.util.ArrayList;


public class TransfersMapFragment extends MapFragment {

    protected SurveyManager manager;
    protected LinearLayout routesLayout;
    protected Button transfersBtn;
    private ImageButton scope;
    protected String[] routes;
    protected ViewPager pager;
    protected Bundle extras;
    protected String[] selectedRoutes;


    public void initialize(SurveyManager manager, ViewPager pager, Bundle extras) {
        this.manager = manager;
        this.pager = pager;
        this.extras = extras;
        this.selectedRoutes = new String[Cons.MAX_TRANSFERS];

        if(extras != null) {
            if (extras.containsKey(Cons.LINE) && extras.containsKey(Cons.DIR)) {
                line = extras.getString(Cons.LINE);
                dir = extras.getString(Cons.DIR);
            }
        }
        else {
            line = "9";
            dir = "1";
        }
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
        scope = (ImageButton) view.findViewById(R.id.scope);
        routes = activity.getResources().getStringArray(R.array.transfer_routes);
        final ArrayList<String> routesList = new ArrayList<String>();
        for(String route: routes) {
            routesList.add(route);
        }

        addDefaultRoute(context, line, dir);
        String[] rte = new String[] {line, dir};
        ArrayList<String> defaultRoutesList = (ArrayList<String>) routesList.clone();
        defaultRoutesList.add(0, "");

        String[] savedRoutes = {line, null, null, null, null};

        if(extras != null) {
            String key;
            for(int i = 0; i < 5; i++) {
                key = "route" + Integer.toString(i+1);
                int route = extras.getInt(key, -1);
                if(route != -1) {
                    savedRoutes[i] = Integer.toString(route);
                }
            }
        }

        manager.setTransfers(selectedRoutes);

        //TODO create a factory to make RoutePicker objects and set Next/Previous
        final RoutePicker rp1 = new RoutePicker(this, context, inflater, container,
                routesLayout, routesList, savedRoutes[0], true, 1, selectedRoutes, rte);
        final RoutePicker rp2 = new RoutePicker(this, context, inflater, container,
                routesLayout, defaultRoutesList, savedRoutes[1], false, 2, selectedRoutes, rte);
        final RoutePicker rp3 = new RoutePicker(this, context, inflater, container,
                routesLayout, defaultRoutesList, savedRoutes[2], false, 3, selectedRoutes, rte);
        final RoutePicker rp4 = new RoutePicker(this, context, inflater, container,
                routesLayout, defaultRoutesList, savedRoutes[3], false, 4, selectedRoutes, rte);
        final RoutePicker rp5 = new RoutePicker(this, context, inflater, container,
                routesLayout, defaultRoutesList, savedRoutes[4], false, 5, selectedRoutes, rte);

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

        scope.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ILatLng startingPoint = new LatLng(45.52186, -122.679005);
                mv.setCenter(startingPoint);
                mv.setZoom(12);
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
            scope.setVisibility(View.INVISIBLE);
            routesLayout.setVisibility(View.VISIBLE);
            transfersBtn.setBackground(
                    context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_top));
        }
        else {
            scope.setVisibility(View.VISIBLE);
            routesLayout.setVisibility(View.INVISIBLE);
            transfersBtn.setBackground(
                    context.getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_all));
        }
    }

}
