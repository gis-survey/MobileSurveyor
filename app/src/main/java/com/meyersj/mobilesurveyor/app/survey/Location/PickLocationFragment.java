package com.meyersj.mobilesurveyor.app.survey.Location;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.ODKApplication;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.geocode.LocationResult;
import com.meyersj.mobilesurveyor.app.geocode.GeocodeAdapter;
import com.meyersj.mobilesurveyor.app.stops.BuildStops;
import com.meyersj.mobilesurveyor.app.stops.OnOffMapListener;
import com.meyersj.mobilesurveyor.app.survey.MapFragment;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.ArrayList;
import java.util.Properties;


public class PickLocationFragment extends MapFragment {

    private String mode; // "origin" or "destination"
    private ImageButton clear;
    private CheckBox region;
    private ItemizedIconOverlay stopsOverlay;
    private ItemizedIconOverlay locOverlay;
    private ArrayList<Marker> locList = new ArrayList<Marker>();
    protected Properties prop;
    private AutoCompleteTextView solrSearch;
    private GeocodeAdapter adapter;
    private SurveyManager manager;
    protected Drawable originIcon;
    protected Drawable destIcon;
    protected Spinner modeSpinner;
    protected Spinner locationSpinner;
    protected Bundle extras;
    protected Integer locCount = 0;
    protected Integer modeCount = 0;
    protected OnOffMapListener mapListener;


    public void initialize(SurveyManager manager, String mode, Bundle extras) {
        this.manager = manager;
        this.mode = mode;
        this.extras = extras;
        line = extras != null ? extras.getString(Cons.LINE, Cons.DEFAULT_RTE) : Cons.DEFAULT_RTE;
        dir = extras != null ? extras.getString(Cons.DIR, Cons.DEFAULT_DIR) : Cons.DEFAULT_DIR;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_pick_location, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        originIcon = context.getResources().getDrawable(R.drawable.start);
        destIcon = context.getResources().getDrawable(R.drawable.end);

        TextView desc = (TextView) view.findViewById(R.id.mode_desc);
        if(mode.equals(Cons.ORIG)) desc.setText("Starting Location");
        else if(mode.equals(Cons.DEST)) desc.setText("Ending Location");
        else desc.setText("Location");

        clear = (ImageButton) view.findViewById(R.id.clear_text);
        region = (CheckBox) view.findViewById(R.id.region);

        TextView modeSpinnerText = (TextView) view.findViewById(R.id.mode_spinner_text);
        locationSpinner = (Spinner) view.findViewById(R.id.location_type_spinner);
        modeSpinner = (Spinner) view.findViewById(R.id.mode_spinner);

        ArrayAdapter<CharSequence> locTypeAdapter = ArrayAdapter.createFromResource(
                view.getContext(), R.array.location_type_array, android.R.layout.simple_spinner_item);
        locTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locTypeAdapter);

        mv = (MapView) view.findViewById(R.id.mapview);
        setTiles(mv);
        locOverlay = newItemizedOverlay(locList);
        mv.addOverlay(locOverlay);

        prop = Utils.getProperties(context, Cons.PROPERTIES);
        if (mode.equals(Cons.ORIG)) {
            ArrayAdapter<CharSequence> accessAdapter = ArrayAdapter.createFromResource(
                    view.getContext(), R.array.access_mode_array, android.R.layout.simple_spinner_item);
            accessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modeSpinner.setAdapter(accessAdapter);
            modeSpinnerText.setText("Mode of access: ");
        } else if (mode.equals(Cons.DEST)) {
            ArrayAdapter<CharSequence> egressAdapter = ArrayAdapter.createFromResource(
                    view.getContext(), R.array.egress_mode_array, android.R.layout.simple_spinner_item);
            egressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modeSpinner.setAdapter(egressAdapter);
            modeSpinnerText.setText("Mode of departure: ");
        }

        if (!Utils.isNetworkAvailable(context)) {
            Utils.shortToastCenter(context,
                    "No network connection, pick location from map");
        }

        solrSearch = (AutoCompleteTextView) view.findViewById(R.id.geocode_input);
        adapter = new GeocodeAdapter(context, android.R.layout.simple_list_item_1,
                (ODKApplication) getActivity().getApplication(), manager, mode);
        solrSearch.setAdapter(adapter);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mv.setMapViewListener(new PickLocationMapViewListener(this, locOverlay, this.manager, mode, originIcon, destIcon, region, solrSearch));
        mapListener = new OnOffMapListener(mv);
        mv.addListener(mapListener);

        // set up listeners for view objects
        solrSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String address = parent.getItemAtPosition(position).toString();
                LocationResult locationResult = adapter.getLocationResultItem(address);
                if (locationResult != null) {
                    adapter.clearResults();
                    addMarker(locationResult.getLatLng());
                }
                Utils.closeKeypad(activity);
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solrSearch.clearListSelection();
                solrSearch.setText("");
                manager.setSeachString("", mode);
            }
        });

        region.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    manager.setRegion(true, mode);
                    if(locOverlay.size() > 0)
                        verifyClearLocation(getActivity());
                }
                else {
                    manager.setRegion(false, mode);
                }
            }
        });

        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(modeCount > 0) {
                    String selected = modeSpinner.getSelectedItem().toString().toLowerCase();
                    if (!selected.isEmpty()) {
                        manager.updateMode(mode, String.valueOf(position));
                        Log.d(TAG, "selected not empty");
                    }
                    else {
                        manager.updateMode(mode, "");
                        Log.d(TAG, "selected IS empty");
                    }
                    if (selected.startsWith("other")) {
                        manager.inputModeOther(activity, mode);
                    }
                }
                modeCount++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(locCount > 0) {
                    String selected = locationSpinner.getSelectedItem().toString().toLowerCase();
                    if (!selected.isEmpty()) {
                        manager.updatePurpose(mode, String.valueOf(position));
                    }
                    else {
                        manager.updatePurpose(mode, "");
                    }
                    if (selected.startsWith("other")) {
                        manager.inputPurposeOther(activity, mode); // specify other location type
                    }
                }
                locCount++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        restoreState();
        return view;
    }

    public void verifyClearLocation(final Activity activity) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage("Do you also want to remove the location?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        clearMarkers();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                });

        AlertDialog select = builder.create();
        select.show();
    }

    protected ArrayList<Marker> getStops(String line, String dir, Boolean zoom) {
        String geoJSONName = line + "_" + dir + "_stops.geojson";
        Log.d(TAG, geoJSONName);
        BuildStops stops = new BuildStops(context, mv, "geojson/" + geoJSONName, dir);
        if (stops != null) return stops.getStops();
        return new ArrayList<Marker>();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private ItemizedIconOverlay newItemizedOverlay(ArrayList<Marker> markers) {
        ItemizedIconOverlay overlay = new ItemizedIconOverlay(mv.getContext(), markers,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );
        return overlay;
    }

    private void addMarker(LatLng latLng) {
        if(latLng != null) {
            clearMarkers();
            Marker m = new Marker(null, null, latLng);
            if(mode.equals(Cons.ORIG))
                m.setMarker(originIcon);
            if(mode.equals(Cons.DEST)) {
                m.setMarker(destIcon);
            }
            m.addTo(mv);
            locOverlay.addItem(m);
            mv.invalidate();
            mv.setCenter(latLng);
            mv.setZoom(17);
            manager.setLocation(m, mode);
            manager.setRegion(false, mode);
            if(region.isChecked()) region.setChecked(false);
        }
    }

    private void clearMarkers() {
        locOverlay.setFocus(null);
        locOverlay.removeAllItems();
        manager.setLocation(null, mode);
        mv.invalidate();
    }

    public void restoreState() {
        if(extras == null)
            return;

        String addressKey;
        String purposeKey;
        String modeKey;
        String latKey;
        String lngKey;
        String regionKey;

        // TODO make this better
        if(mode.equals(Cons.ORIG)) {
            addressKey = "orig_address";
            purposeKey = "orig_purpose";
            modeKey = "orig_access";
            latKey = "orig_lat";
            lngKey = "orig_lng";
            regionKey = "orig_outside_region";
        }
        else {
            addressKey = "dest_address";
            purposeKey = "dest_purpose";
            modeKey = "dest_egress";
            latKey = "dest_lat";
            lngKey = "dest_lng";
            regionKey = "dest_outside_region";
        }
        if(hasExtra(purposeKey)) {
            Integer index = extras.getInt(purposeKey, -1);
            if(index > 0) {
                locationSpinner.setSelection(index);
            }
        }
        if(hasExtra(modeKey)) {
            Integer index = extras.getInt(modeKey, -1);
            if(index > 0) {
                modeSpinner.setSelection(index);
            }
        }
        if(hasExtra(latKey) && hasExtra(lngKey)) {
            Double lat = extras.getDouble(latKey);
            Double lng = extras.getDouble(lngKey);
            addMarker(new LatLng(lat, lng));
        }
        if(hasExtra(regionKey)) {
            Boolean outsideRegion = Boolean.valueOf(extras.getString(regionKey, "false"));
            manager.setRegion(outsideRegion, mode);
            region.setChecked(outsideRegion);
        }
        solrSearch.setText(extras.getString(addressKey, ""));
        manager.setSeachString(extras.getString(addressKey, ""), mode);

    }

    protected Boolean hasExtra(String key) {
        if(extras.containsKey(key) && (extras.get(key) != null)) {
            return true;
        }
        return false;
    }

    // gets called everytime a tab is switched
    // this restores the state of the view showing the correct data
    public void updateView(SurveyManager manager) {
        mv.removeOverlay(surveyOverlay);
        surveyOverlay.removeAllItems();
        Marker location = null;
        Marker stop = null;


        String[] route;
        if(mode.equals("origin")) {
            route = manager.getFirstRoute();
            stop = manager.getOnStop();
            location = manager.getOrig();
        }
        else { // destination
            route = manager.getLastRoute();
            stop = manager.getOffStop();
            location = manager.getDest();
        }
        clearRoutes();
        addTransferRoute(context, route[0], route[1]);

        ArrayList<Marker> stopsList = getStops(route[0], route[1], true);
        stopsOverlay = newItemizedOverlay(stopsList);
        mapListener.setMarkers(stopsList);
        mapListener.setOverlay(stopsOverlay);

        if(stop != null) surveyOverlay.addItem(stop);
        if(location != null) locOverlay.addItem(location);
        mv.addItemizedOverlay(surveyOverlay);
        mv.addItemizedOverlay(locOverlay);
    }

}