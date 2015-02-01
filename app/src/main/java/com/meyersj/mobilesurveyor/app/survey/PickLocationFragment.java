package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.locations.LocationResult;
import com.meyersj.mobilesurveyor.app.locations.SolrAdapter;
import com.meyersj.mobilesurveyor.app.locations.mMapViewListener;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class PickLocationFragment extends MapFragment {

    private String mode; // origin or destination
    private ImageButton clear;
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

    public PickLocationFragment(SurveyManager manager, String mode, Bundle extras) {
        this.manager = manager;
        this.mode = mode;
        this.extras = extras;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "on create view pick");
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_pick_location, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        circleIcon = context.getResources().getDrawable(R.drawable.marker_stroked_24);
        squareIcon = context.getResources().getDrawable(R.drawable.marker_24);

        clear = (ImageButton) view.findViewById(R.id.clear_text);
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
        mv.setMapViewListener(new mMapViewListener(this, locOverlay, this.manager, mode, circleIcon, squareIcon));
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


        modeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if(modeCount > 0) {
                    String selected = modeSpinner.getSelectedItem().toString().toLowerCase();
                    if (!selected.isEmpty()) {
                        manager.updateMode(mode, String.valueOf(position));
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
                    if (selected.contains("other (specify)")) {
                        manager.inputPurposeOther(activity, mode); // specify other location type
                    }
                }
                locCount++;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        refresh();
        return view;
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

    public String getMode() {
        return this.mode;
    }

    public void refresh() {
        //isRestored = false;
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

        String[] locTemp = getResources().getStringArray(R.array.location_type_array);
        List<String> locTypes = Arrays.asList(locTemp);
        String[] modeTemp;
        List<String> modeTypes;


        if(mode.equals("origin")) {
            purposeKey = "orig_purpose";
            purposeOtherKey = "orig_purpose_other";
            modeKey = "orig_access";
            modeOtherKey = "orig_access_other";
            blocksKey = "orig_blocks";
            parkingKey = "orig_parking";
            latKey = "orig_lat";
            lngKey = "orig_lng";
            modeTemp = getResources().getStringArray(R.array.access_mode_array);
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
            modeTemp = getResources().getStringArray(R.array.egress_mode_array);
        }
        modeTypes = Arrays.asList(modeTemp);

        if(hasExtra(purposeKey)) {
            Integer index = extras.getInt(purposeKey, -1);
            Log.d(TAG,"purpose index: " + String.valueOf(index));
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
                //int i = modeTypes.indexOf(mode);
                modeSpinner.setSelection(index);
                manager.updateMode(mode, String.valueOf(index));
                if(hasExtra(modeOtherKey)) {
                    //String modeOther = extras.getString(modeOtherKey);
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
        /*
        if(hasExtra(latKey) && hasExtra(lngKey)) {
            addMarker(new LatLng(extras.getDouble(latKey), extras.getDouble(lngKey)));
        }
        */

        //isRestored = true;
    }

    protected Boolean hasExtra(String key) {
        if(extras.containsKey(key) && (extras.get(key) != null)) {
            return true;
        }
        return false;
    }

}
