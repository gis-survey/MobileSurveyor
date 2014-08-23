package com.meyersj.locationsurvey.app;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.locationsurvey.app.locations.LocationResult;
import com.meyersj.locationsurvey.app.locations.SolrAdapter;
import com.meyersj.locationsurvey.app.util.Utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


//ActionBarActivity
public class PickLocationActivity extends ActionBarActivity {

    private final String TAG = "PickLocationActivity";
    private final String PROPERTIES = "config.properties";
    private final String ODK_LAT = "lat";
    private final String ODK_LNG = "lng";
    private final String SOLR_URL = "solr_url";
    private final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    private final String TILESNAME = "OSMTriMet.mbtiles";

    private ImageButton clear;
    private Button submit;

    private ItemizedIconOverlay locOverlay;
    private ArrayList<Marker> locList = new ArrayList<Marker>();
    private MapView mv;
    Properties prop;

    private AutoCompleteTextView solrSearch;
    private SolrAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        clear = (ImageButton) findViewById(R.id.clear_text);
        submit = (Button) findViewById(R.id.submit);

        mv = (MapView) findViewById(R.id.mapview);
        mv.setMapViewListener(new mMapViewListener());

        setTiles(mv);
        setItemizedOverlay(mv);
        prop = Utils.getProperties(getApplicationContext(), PROPERTIES);

        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            Toast toast = Toast.makeText(this, "No network connection, pick location from map", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER, 0, 0);
            toast.show();
        }


        solrSearch = (AutoCompleteTextView) findViewById(R.id.solr_input);
        adapter = new SolrAdapter(this,android.R.layout.simple_list_item_1, prop.getProperty(SOLR_URL));
        solrSearch.setAdapter(adapter);

        solrSearch.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                solrSearch.selectAll();
                return false;
            }
        });

        solrSearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                String address = parent.getItemAtPosition(position).toString();
                LocationResult locationResult = adapter.getLocationResultItem(address);

                if (locationResult != null) {
                    Log.d(TAG, locationResult.getAddress());
                    Log.d(TAG, locationResult.getLatLng().toString());
                    addMarker(locationResult.getLatLng());
                }
                else {
                    Log.e(TAG, "Did not find record: " + address);
                }

                //close keypad
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);
            }
        });

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                solrSearch.clearListSelection();
                solrSearch.setText("");
            }
        });


        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exitWithLocation();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_controllor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setTiles(MapView mv) {
        ILatLng startingPoint = new LatLng(45.52186, -122.679005);
        ITileLayer mbTilesSource;
        ITileLayer osmSource = new WebSourceTileLayer("openstreetmap",
                "http://tile.openstreetmap.org/{z}/{x}/{y}.png").setName("OpenStreetMap")
                .setAttribution("© OpenStreetMap Contributors");

        try {
            File tiles = new File(TILESPATH, TILESNAME);
            mbTilesSource = new MBTilesLayer(tiles);
            mv.setTileSource(mbTilesSource);
            Log.d(TAG, String.valueOf(mbTilesSource.getMaximumZoomLevel()));
            Log.d(TAG, String.valueOf(mbTilesSource.getMinimumZoomLevel()));
        }
        catch(Exception e) {
            Log.e(TAG, "unable to open local mbtiles");
            mv.setTileSource(osmSource);
        }

        mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(startingPoint);
        mv.setZoom(14);
    }

    private void setItemizedOverlay(MapView mv) {
        final MapView mapView = mv;
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

    private boolean exitWithLocation() {
        Intent intent = new Intent();
        Map<String, Double> coordinates = getCoordinates();

        if(coordinates != null) {
            Log.d(TAG, "coordinates not null");
            intent.putExtra(ODK_LAT, coordinates.get("lat"));
            intent.putExtra(ODK_LNG, coordinates.get("lon"));
            setResult(RESULT_OK, intent);
        }
        else {
            Log.d(TAG, "coordinates null");
            setResult(RESULT_CANCELED, intent);
        }
        finish();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
        }
        return super.onKeyDown(keyCode, event);
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



    private void addMarker(LatLng latLng) {

        if(latLng != null) {
            clearMarkers();
            Marker m = new Marker(null, null, latLng);
            m.addTo(mv);
            locOverlay.addItem(m);
            mv.invalidate();
            mv.setCenter(latLng);
            mv.setZoom(17);
        }
    }


    private void clearMarkers() {
        ArrayList<ItemizedIconOverlay> overlays = mv.getItemizedOverlays();
        ItemizedIconOverlay locOverlay;
        if (!overlays.isEmpty()) {

            locOverlay = overlays.get(0);
            int length = locOverlay.size();

            for (int i = 0; i < length; ++i) {
                Marker m = locList.get(i);
                m.closeToolTip();
            }

            locOverlay.setFocus(null);
            locOverlay.removeAllItems();
            mv.invalidate();
        }
    }

}
