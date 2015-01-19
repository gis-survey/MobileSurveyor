package com.meyersj.mobilesurveyor.app.survey;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


public class PickLocationFragment extends MapFragment {

    private String mode; // origin or destination
    private ImageButton clear;
    private ItemizedIconOverlay locOverlay;
    private ArrayList<Marker> locList = new ArrayList<Marker>();
    Properties prop;
    private AutoCompleteTextView solrSearch;
    private SolrAdapter adapter;
    private SurveyManager manager;

    public PickLocationFragment(SurveyManager manager, String mode) {
        this.manager = manager;
        this.mode = mode;
    }

    // newInstance constructor for creating fragment with arguments
    public static PickLocationFragment newInstance(int page, String title) {
        Log.d("SEQLOC", "pick location new instance");
        PickLocationFragment fragment = new PickLocationFragment(null, null);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.fragment_pick_location, container, false);
        activity = getActivity();
        context = activity.getApplicationContext();
        clear = (ImageButton) view.findViewById(R.id.clear_text);
        TextView modeSpinnerText = (TextView) view.findViewById(R.id.mode_spinner_text);
        Spinner locationSpinner = (Spinner) view.findViewById(R.id.location_type_spinner);
        Spinner modeSpinner = (Spinner) view.findViewById(R.id.mode_spinner);
        ArrayAdapter<CharSequence> locTypeAdapter = ArrayAdapter.createFromResource(
                view.getContext(),R.array.location_type_array, android.R.layout.simple_spinner_item);
        locTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(locTypeAdapter);
        mv = (MapView) view.findViewById(R.id.mapview);
        setTiles(mv);
        setItemizedOverlay(mv);
        mv.setMapViewListener(new mMapViewListener(locOverlay, this.manager, mode));
        prop = Utils.getProperties(context, Cons.PROPERTIES);

        if (mode.equals("origin")) {
            ArrayAdapter<CharSequence> accessAdapter = ArrayAdapter.createFromResource(
                    view.getContext(),R.array.access_mode_array, android.R.layout.simple_spinner_item);
            accessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modeSpinner.setAdapter(accessAdapter);
            modeSpinnerText.setText("Mode of access: ");
        }
        else if (mode.equals("destination")) {
            ArrayAdapter<CharSequence> egressAdapter = ArrayAdapter.createFromResource(
                    view.getContext(),R.array.egress_mode_array, android.R.layout.simple_spinner_item);
            egressAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            modeSpinner.setAdapter(egressAdapter);
            modeSpinnerText.setText("Mode of departure: ");
        }

        if (!Utils.isNetworkAvailable(context)) {
            Utils.shortToastCenter(context,
                    "No network connection, pick location from map");
        }

        solrSearch = (AutoCompleteTextView) view.findViewById(R.id.solr_input);
        adapter = new SolrAdapter(context ,android.R.layout.simple_list_item_1, Utils.getUrlSolr(context));
        solrSearch.setAdapter(adapter);

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
                closeKeypad();
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solrSearch.clearListSelection();
                solrSearch.setText("");
            }
        });

        return view;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d("SEQLOC", "attach");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d("SEQLOC", "detach");
    }


    private void setItemizedOverlay(final MapView mv) {
        Log.d("SEQLOC", "new pick location overlay");
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
            m.addTo(mv);
            locOverlay.addItem(m);
            Log.d("SEQLOC", locOverlay.toString());
            mv.invalidate();
            mv.setCenter(latLng);
            mv.setZoom(17);
            manager.setLocation(m, mode);
        }
    }

    public void closeKeypad() {
        InputMethodManager inputManager = (InputMethodManager)
                activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
    }


    /*
    private boolean exitWithLocation(Boolean valid) {
        //int result;
        Intent intent = new Intent();

        if (valid) {
            Map<String, Double> coordinates = getCoordinates();
            if(coordinates != null) {
                Log.d(TAG, "valid coordinates");
                intent.putExtra(Cons.ODK_LAT, coordinates.get("lat"));
                intent.putExtra(Cons.ODK_LNG, coordinates.get("lon"));
                //result = activity.RESULT_OK;
            }
            else {
                Log.d(TAG, "cancelled: no coordinates");
                //result = activity.RESULT_CANCELED;
            }
        }
        else {
            Log.d(TAG, "cancelled: invalid");
            //result = activity.RESULT_CANCELED;
        }

        Utils.shortToastCenter(context, "location saved");
        //activity.setResult(result, intent);
        //activity.finish();
        return true;
    }
    */


    private void clearMarkers() {
        Log.d(TAG, "clear all markers");
        locOverlay.setFocus(null);
        locOverlay.removeAllItems();
        mv.invalidate();
    }

    private Map<String, Double> getCoordinates() {
        Map<String, Double> coordinates = null;
        new HashMap();
        int length = locList.size();
        if (length > 0) {
            Marker m = locList.get(0);
            LatLng point = m.getPoint();
            coordinates = new HashMap();
            coordinates.put("lat", point.getLatitude());
            coordinates.put("lon", point.getLongitude());
        }
        return coordinates;
    }

    public String getMode() {
        return this.mode;
    }

}
