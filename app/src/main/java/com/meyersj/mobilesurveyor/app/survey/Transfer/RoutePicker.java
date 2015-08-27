package com.meyersj.mobilesurveyor.app.survey.Transfer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.survey.MapFragment;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.DataLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;


public class RoutePicker {

    private final String TAG = "RoutePicker";
    private final String ADDROUTE = "Add another route";

    protected SurveyManager manager;
    protected MapFragment frag;
    protected Context context;
    protected LinearLayout routeLayout;
    protected ArrayList<String> routes;
    protected LinearLayout view;
    protected Spinner spinner;
    protected ImageButton remove;
    protected Integer number;
    protected RoutePicker previous;
    protected RoutePicker next;
    protected String[] selectedRoutes;
    protected String[] selectedDirections;
    protected String[] rte;
    protected String dir;
    protected String curRte;
    protected Boolean noSelection = true;
    protected HashMap<String, String> routeLookup;

    //TODO make RoutePickers nodes in a double linked list
    public RoutePicker(MapFragment frag, Context context, LayoutInflater inflater,
                       ViewGroup parent, LinearLayout routeLayout, ArrayList<String> routes,
                       String line, Boolean first, Integer number,
                       String[] selectedRoutes, String[] selectedDirections,
                       String[] rte, SurveyManager manager) {
        this.manager = manager;
        this.frag = frag;
        this.context = context;
        this.number = number;
        this.rte = rte;
        this.selectedRoutes = selectedRoutes;
        this.selectedDirections = selectedDirections;
        this.routeLayout = routeLayout;
        this.view = (LinearLayout) inflater.inflate(R.layout.route_spinner_layout, parent, false);
        this.routeLayout.addView(view);
        this.spinner = (Spinner) this.view.findViewById(R.id.route_spinner);
        this.remove = (ImageButton) this.view.findViewById(R.id.remove_route);
        this.routes = routes;
        attachAdapter();
        buildRouteLookup();
        setSpinner(line);
        setSpinnerListener(line);
        setRemoveListener(line);
        if(line != null) {
            selectedRoutes[number - 1] = line;
        }
        TextView routeText = (TextView) view.findViewById(R.id.route_number);
        routeText.setText("Route #" + String.valueOf(number));
        if(!first) {
            setRemoveVisibility(View.VISIBLE);
            setViewVisibility(View.INVISIBLE);
        }
    }

    public void setSpinnerListener(final String line) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String routeID = routeLookup.get(routes.get(i));
                if(!noSelection) { //ignore when view is first constructed
                    if (i > 0) { //ignore if route picked is first item in list (empty string)
                        String selRte = selectedRoutes[number - 1];
                        if(selRte != null && !selRte.equals(rte[0])) {
                            frag.clearRoute(selRte, rte[1]);
                        }
                        selectedRoutes[number - 1] = routeID;
                        //if(!routeID.equals(rte[0])) {
                        manager.inputTransferDirection((Activity) context, routeID, number - 1);
                        frag.addTransferRoute(context, routeID, rte[1]);
                        //}
                    }
                    if (next != null) {
                        next.setViewVisibility(View.VISIBLE);
                    }
                }
                else {
                    noSelection = false;
                    if(number - 1 == 0)
                        selectedRoutes[number - 1] = routeID;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

    }

    public View getView() {
        return view;
    }

    public void setNext(RoutePicker next) {
        this.next = next;
    }

    public void setPrevious(RoutePicker previous) {
        this.previous = previous;
    }

    public Spinner getSpinner() {
        return spinner;
    }

    public void setRemoveVisibility(int value) {
        remove.setVisibility(value);
    }

    public void setViewVisibility(int value) {
        view.setVisibility(value);
        view.setVisibility(View.VISIBLE);
    }


    protected void setSpinner(String line) {
        if (line != null && !line.isEmpty()){
            frag.addTransferRoute(context, line, rte[1]);
            String lineDesc = routeLookup.get(line);
            for (int i = 0; i < routes.size(); i++) {
                if (routes.get(i).equals(lineDesc)) {
                    this.spinner.setSelection(i);
                    break;
                }
            }
        }
    }

    protected void setRemoveListener(final String line) {
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                frag.clearRoute(selectedRoutes[number - 1], rte[1]);
                selectedRoutes[number - 1] = null;
                selectedDirections[number - 1] = null;
                spinner.setSelection(0);
            }
        });
    }

    protected void attachAdapter() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
                android.R.layout.simple_spinner_dropdown_item, routes);
        spinner.setAdapter(adapter);
    }

    protected void buildRouteLookup() {
        routeLookup = DataLoader.getRoutesLookup(context);
        routeLookup.put(ADDROUTE, null);
        routeLookup.put("", "");
    }



}
