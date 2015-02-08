package com.meyersj.mobilesurveyor.app.survey.Location;

import android.app.Activity;
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

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.locations.LocationResult;
import com.meyersj.mobilesurveyor.app.locations.SolrAdapter;
import com.meyersj.mobilesurveyor.app.locations.mMapViewListener;
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
    private ImageButton scope;
    private CheckBox region;
    private ItemizedIconOverlay locOverlay;
    private ArrayList<Marker> locList = new ArrayList<Marker>();
    protected Properties prop;
    private AutoCompleteTextView solrSearch;
    private SolrAdapter adapter;
    private SurveyManager manager;
    protected Drawable circleIcon;
    protected Drawable squareIcon;
    protected Spinner modeSpinner;
    protected Spinner locationSpinner;
    protected Bundle extras;
    protected Integer locCount = 0;
    protected Integer modeCount = 0;

    /*
    public PickLocationFragment(SurveyManager manager, String mode, Bundle extras) {
        this.manager = manager;
        this.mode = mode;
        this.extras = extras;

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
    */

    public void initialize(SurveyManager manager, String mode, Bundle extras) {
        this.manager = manager;
        this.mode = mode;
        this.extras = extras;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_pick_location, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        circleIcon = context.getResources().getDrawable(R.drawable.start);
        squareIcon = context.getResources().getDrawable(R.drawable.end);

        TextView desc = (TextView) view.findViewById(R.id.mode_desc);
        if(mode.equals("origin")) desc.setText("Starting Location");
        else if(mode.equals("destination")) desc.setText("Ending Location");
        else desc.setText("Location");

        clear = (ImageButton) view.findViewById(R.id.clear_text);
        scope = (ImageButton) view.findViewById(R.id.scope);
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
        setItemizedOverlay(mv);
        mv.setMapViewListener(new PickLocationMapViewListener(this, locOverlay, this.manager, mode, circleIcon, squareIcon));

        prop = Utils.getProperties(context, Cons.PROPERTIES);
        if (mode.equals("origin")) {
            ArrayAdapter<CharSequence> accessAdapter = ArrayAdapter.createFromResource(
                    view.getContext(), R.array.access_mode_array, android.R.layout.simple_spinner_item);
            accessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modeSpinner.setAdapter(accessAdapter);
            modeSpinnerText.setText("Mode of access: ");
        } else if (mode.equals("destination")) {
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

        solrSearch = (AutoCompleteTextView) view.findViewById(R.id.solr_input);
        adapter = new SolrAdapter(context, android.R.layout.simple_list_item_1, Utils.getUrlSolr(context));
        solrSearch.setAdapter(adapter);
        activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        addDefaultRoute(context, line, dir);
        ArrayList<Marker> locList = getStops(line, dir, true);
        setItemizedOverlay(locList);
        mv.addListener(new OnOffMapListener(mv, locList, locOverlay));
        updateView(manager);

        // set up listeners for view objects
        solrSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String address = parent.getItemAtPosition(position).toString();
                LocationResult locationResult = adapter.getLocationResultItem(address);

                if (locationResult != null) {
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

        region.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {
                    manager.setRegion("1", mode);
                }
                else {
                    manager.setRegion("2", mode);
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
                    if (selected.contains("walk")) {
                        manager.inputBlocks(activity, mode);
                    } else if (selected.contains("parked") || selected.contains("drive") ||
                            selected.contains("carpool")) {
                        manager.inputParking(activity, mode);
                    } else if (selected.contains("other")) {
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
                    if (selected.contains("other (specify)")) {
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

    protected ArrayList<Marker> getStops(String line, String dir, Boolean zoom) {
        String geoJSONName = line + "_" + dir + "_stops.geojson";
        Log.d(TAG, geoJSONName);
        BuildStops stops = new BuildStops(context, mv, "geojson/" + geoJSONName, dir);
        //if(zoom) {
            //Log.d(TAG, "getting bounding box");
            //bbox = stops.getBoundingBox();
            //mv.zoomToBoundingBox(bbox, true, false, true, true);
        //}
        return stops.getStops();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
    private void setItemizedOverlay(final MapView mv) {
        locOverlay = new ItemizedIconOverlay(mv.getContext(), locList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );
        mv.addItemizedOverlay(locOverlay);
    }

    private void addMarker(LatLng latLng) {
        if(latLng != null) {
            clearMarkers();
            Marker m = new Marker(null, null, latLng);
            if(mode.equals("origin"))
                m.setMarker(circleIcon);
            if(mode.equals("destination"))
                m.setMarker(squareIcon);
            m.addTo(mv);
            locOverlay.addItem(m);
            mv.invalidate();
            mv.setCenter(latLng);
            mv.setZoom(17);
            manager.setLocation(m, mode);
        }
    }

    private void clearMarkers() {
        locOverlay.setFocus(null);
        locOverlay.removeAllItems();
        mv.invalidate();
    }

    public void restoreState() {
        if(extras == null)
            return;

        String purposeKey;
        String purposeOtherKey;
        String modeKey;
        String modeOtherKey;
        String blocksKey;
        String parkingKey;
        String latKey;
        String lngKey;
        String regionKey;

        if(mode.equals("origin")) {
            purposeKey = "orig_purpose";
            purposeOtherKey = "orig_purpose_other";
            modeKey = "orig_access";
            modeOtherKey = "orig_access_other";
            blocksKey = "orig_blocks";
            parkingKey = "orig_parking";
            latKey = "orig_lat";
            lngKey = "orig_lng";
            regionKey = "orig_region";
        }
        else {
            purposeKey = "dest_purpose";
            purposeOtherKey = "dest_purpose_other";
            modeKey = "dest_egress";
            modeOtherKey = "dest_access_other";
            blocksKey = "dest_blocks";
            parkingKey = "dest_parking";
            latKey = "dest_lat";
            lngKey = "dest_lng";
            regionKey = "dest_region";
        }
        if(hasExtra(purposeKey)) {
            Integer index = extras.getInt(purposeKey, -1);
            if(index > 0) {
                locationSpinner.setSelection(index);
                manager.updatePurpose(mode, String.valueOf(index));
                if (hasExtra(purposeOtherKey)) {
                    String purposeOther = extras.getString(purposeOtherKey);
                    manager.updatePurposeOther(mode, purposeOther);
                }
            }
        }
        if(hasExtra(modeKey)) {
            Integer index = extras.getInt(modeKey, -1);
            if(index > 0) {
                modeSpinner.setSelection(index);
                manager.updateMode(mode, String.valueOf(index));
                if(hasExtra(modeOtherKey)) {
                    manager.updateModeOther(mode, extras.getString(modeOtherKey));
                }
                if(hasExtra(blocksKey)) {
                    manager.updateBlocks(mode, String.valueOf(extras.getInt(blocksKey)));
                }
                if(hasExtra(parkingKey)) {
                    manager.updateParking(mode, extras.getString(parkingKey));
                }
            }
        }
        if(hasExtra(latKey) && hasExtra(lngKey)) {
            Double lat = extras.getDouble(latKey);
            Double lng = extras.getDouble(lngKey);
            if(lat > 44 && lng < -122) {
                addMarker(new LatLng(lat, lng));
            }
        }
        if(hasExtra(regionKey)) {
            Log.d(TAG, "has region key");
            if(extras.getString(regionKey).equals("1")) {
                region.setChecked(true);
            }
        }

    }

    protected Boolean hasExtra(String key) {
        if(extras.containsKey(key) && (extras.get(key) != null)) {
            return true;
        }
        return false;
    }

    protected void setItemizedOverlay(ArrayList<Marker> locList) {
        locOverlay = new ItemizedIconOverlay(mv.getContext(), locList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final Marker item) {
                        return true;
                    }
                }
        );
    }

}