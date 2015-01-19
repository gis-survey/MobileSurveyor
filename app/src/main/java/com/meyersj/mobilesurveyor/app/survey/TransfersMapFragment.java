package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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


public class TransfersMapFragment extends MapFragment {

    protected SurveyManager manager;
    protected String line;
    protected String dir;

    protected View routesLayout;
    protected ListView listView;
    protected Button transfersBtn;

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
        addRoute(context, line, dir);

        transfersBtn = (Button) view.findViewById(R.id.transfers_btn);
        listView = (ListView) view.findViewById(R.id.routes_list_view);
        routesLayout = (View) view.findViewById(R.id.routes_list_layout);

        String[] routes = activity.getResources().getStringArray(R.array.lines);
        ArrayList<String> routesList = new ArrayList<String>();
        for(String route: routes) {
            routesList.add(route);
        }
        RoutesAdapter routesAdapter = new RoutesAdapter(context, routesList);
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(view.getContext(),
        //        R.layout.textview_route_transfers, routes);
        listView.setAdapter(routesAdapter);
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
