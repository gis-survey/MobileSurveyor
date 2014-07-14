package com.meyersj.locationsurvey.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.mapbox.mapboxsdk.api.ILatLng;
import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.overlay.ItemizedIconOverlay;
import com.mapbox.mapboxsdk.overlay.Marker;
import com.mapbox.mapboxsdk.overlay.PathOverlay;
import com.mapbox.mapboxsdk.tileprovider.tilesource.ITileLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.MBTilesLayer;
import com.mapbox.mapboxsdk.tileprovider.tilesource.WebSourceTileLayer;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.locationsurvey.app.util.LocationResult;
import com.meyersj.locationsurvey.app.util.PathUtils;
import com.meyersj.locationsurvey.app.util.SolrAdapter;
import com.meyersj.locationsurvey.app.util.SolrServer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


//ActionBarActivity
public class PickLocationActivity extends ActionBarActivity {

    private final String TAG = "PickLocationActivity";
    private final String LINE = "rte";
    private final String DIR = "dir";
    private final String ODK_LAT = "lat";
    private final String ODK_LNG = "lng";
    private final String RLIS_TOKEN = "rlis_token";

    private final File TILESPATH = new File(Environment.getExternalStorageDirectory(), "maps/mbtiles");
    private final File GEOJSONPATH = new File(Environment.getExternalStorageDirectory(), "maps/geojson/trimet/");
    private final String TILESNAME = "OSMTriMet.mbtiles";
    //private EditText address;
    //private Button search;
    private ImageButton clear;
    private Button submit;

    private ItemizedIconOverlay locOverlay;
    private ArrayList<Marker> locList = new ArrayList<Marker>();
    private MapView mv;
    private String line = null;
    Properties prop;

    private AutoCompleteTextView solrSearch;
    private SolrAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_location);

        //address = (EditText) findViewById(R.id.input_address);
        //search = (Button) findViewById(R.id.search_address);
        clear = (ImageButton) findViewById(R.id.clear_text);
        submit = (Button) findViewById(R.id.submit);

        mv = (MapView) findViewById(R.id.mapview);
        mv.setMapViewListener(new mMapViewListener());



        setTiles(mv);
        setItemizedOverlay(mv);

        //bundle to get extras if started via ODK collect
        Bundle extras = getExtras();
        prop = getProperties();

        //solrSearch = (AutoCompleteTextView) findViewById(R.id.solr_input);

        adapter = new SolrAdapter(this,android.R.layout.simple_list_item_1);
        solrSearch = (AutoCompleteTextView) findViewById(R.id.solr_input);
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

                //Log.d(TAG, id)

                String address = parent.getItemAtPosition(position).toString();
                LocationResult locationResult = adapter.getLocationResultItem(address);

                if (locationResult != null) {
                    Log.d(TAG, locationResult.getAddress());
                    Log.d(TAG, locationResult.getLatLng().toString());
                    addMarker(locationResult.getLatLng());
                }
                else {
                    Log.e(TAG, "did not find record: " + address);
                }

                //close keypad
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                //getLocation(position);
                //Log.d(TAG, adapter.getItem(position));

                //selectLocType(stopsMap.get(stopsList.get(position)));
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


    /*
    private void getLocation(int position) {
        //adapter.getItem(position);
        //adapter.getItem(adapter.getItemId(position));
        //Log.d(TAG, adapter.getItem(position));
    }
    */


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
        ITileLayer mbTilesSource = null;
        ITileLayer osmSource = null;
        BoundingBox box = new BoundingBox(46.0 ,-122 ,45.0, -123.5);


        Log.d(TAG, "unable to open local mbtiles");
        osmSource = new WebSourceTileLayer("openstreetmap",
                "http://tile.openstreetmap.org/{z}/{x}/{y}.png").setName("OpenStreetMap")
                .setAttribution("© OpenStreetMap Contributors");


        try {
            File tiles = new File(TILESPATH, TILESNAME);
            mbTilesSource = new MBTilesLayer(tiles);


            mv.setTileSource(mbTilesSource);
            Log.d(TAG, String.valueOf(mbTilesSource.getMaximumZoomLevel()));
            Log.d(TAG, String.valueOf(mbTilesSource.getMinimumZoomLevel()));

            //box = source.getBoundingBox();
            //Log.d(TAG, box.toString());
            //TilesListener tilesListener = new TilesListener(mv, mbTilesSource, osmSource);
            //mv.addListener(tilesListener);
        }
        catch(Exception e) {
            Log.d(TAG, "unable to open local mbtiles");
            mv.setTileSource(osmSource);
        }

        //mv.setTileSource(osmSource);

        //mv.setScrollableAreaLimit(box);
        //mv.setMinZoomLevel(mv.getTileProvider().getMinimumZoomLevel());
        //mv.setMaxZoomLevel(mv.getTileProvider().getMaximumZoomLevel());
        mv.setCenter(startingPoint);
        mv.setZoom(14);
    }

    private void setItemizedOverlay(MapView mv) {
        final MapView mapView = mv;
        locOverlay = new ItemizedIconOverlay(mv.getContext(), locList,
                new ItemizedIconOverlay.OnItemGestureListener<Marker>() {
                    public boolean onItemSingleTapUp(final int index, final Marker item) {
                        mapView.selectMarker(item);
                        return true;
                    }

                    public boolean onItemLongPress(final int index, final Marker item) {
                        //if (mMapViewListener != null) {
                        //    mMapViewListener.onLongPressMarker(MapView.this, item);
                        //}
                        return true;
                    }
                }
        );
        mv.addItemizedOverlay(locOverlay);
    }

    private boolean exitWithLocation() {
        Intent intent = new Intent();
        Map<String, Double> coordinates = getCoordinates();

        //TODO should I return 0-0 or not put and extras?
        //Double lat = 0.0;
        //Double lon = 0.0;

        if(coordinates != null) {
            intent.putExtra(ODK_LAT, coordinates.get("lat"));
            intent.putExtra(ODK_LNG, coordinates.get("lon"));
            setResult(RESULT_OK, intent);
        }
        else
            setResult(RESULT_CANCELED, intent);

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
                //exitWithLocation();
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

    //
    private Bundle getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            Log.d(TAG, "extras not null");
            if(extras.containsKey("uuid")) {
                Log.d(TAG, extras.getString("uuid"));
            }

            if(extras.containsKey(LINE) && extras.containsKey(DIR)) {
                String line = extras.getString(LINE);
                String dir = extras.getString(DIR);
                String routes = line + "_" + dir + "_" + "routes.geojson";
                //loadGeoJSONPaths(routes);
            }
        }

        return extras;
    }


    private void loadGeoJSONPaths(String geoJSONName) {

        ArrayList<PathOverlay> paths = PathUtils.getPathFromAssets(this, "geojson/" + geoJSONName);

        if (paths != null) {
            for(PathOverlay path: paths)
                mv.addOverlay(path);
        }

    }

    private class GeocodeTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                Log.d(TAG, statusLine.toString());
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    Log.d(TAG, responseString);
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }

            } catch (ClientProtocolException e) {
                Log.d(TAG, "ClientProtocolException");
                Log.d(TAG, e.toString());//TODO Handle problems..
            } catch (IOException e) {
                Log.d(TAG, "IOException");
                Log.d(TAG, e.toString());//TODO Handle problems..
            }

            return responseString;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (result != null) {
                addMarker(getLatLng(result));
            }
            else {
                Log.d(TAG, "geocode query not successful");
            }

            //Do anything with response..
        }
    }

    /*
    private String buildURL() {
        String address = this.address.getText().toString();
        String addressEncode = null;

        String token = prop.getProperty(RLIS_TOKEN);
        Log.d(TAG, token);
        String baseUrl = "http://gis.oregonmetro.gov/rlisapi/?token=" + token + "&mode=locate&form=json&";
        String url = null;

        try {
            addressEncode = "input=" + URLEncoder.encode(address, "UTF-8");
            url = baseUrl + addressEncode;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return url;
    }
    */


    private LatLng getLatLng(String jsonInput) {
        JSONParser parser=new JSONParser();
        LatLng loc = null;
        Double lat;
        Double lon;

        try{
            Object obj = parser.parse(jsonInput);
            JSONObject results = (JSONObject) obj;
            Log.d(TAG, results.toString());
            String error = results.get("error").toString();

            if(results.get("error").toString().equals("false")) {
                JSONArray data = (JSONArray)results.get("data");
                JSONObject dataJson = (JSONObject)data.get(0);
                lat = Double.parseDouble(dataJson.get("lat").toString());
                lon = Double.parseDouble(dataJson.get("lng").toString());
                loc = new LatLng(lat, lon);
            }
            else {
                Log.d(TAG, "failed to geocode: " + error);
                Toast.makeText(this, (String) "Invalid Address: " + error,
                    Toast.LENGTH_LONG).show();
            }

        }catch(ParseException pe){
            Log.e(TAG, pe.toString());
        }
        return loc;
    }

    private void addMarker(LatLng latLng) {

        if(latLng != null) {
            clearMarkers();
            Marker m = new Marker(null, null, latLng);
            m.addTo(mv);
            locOverlay.addItem(m);
            mv.invalidate();
            mv.setCenter(latLng);
            mv.setZoom(16);
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }


    /*
    private void addGeoJSON(String geoJSON) throws IOException {
        InputStream is;
        String jsonText;


        is = new URL(geoJSON).openStream();
        BufferedReader rd =
                new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
        jsonText = readAll(rd);
        Log.d(TAG, "JSON TEST");
        Log.d(TAG, jsonText);

    }

    private String openGeoJSON(String path) {
        String entireFileText = null;
        try {
            entireFileText = new Scanner(new File(path))
                    .useDelimiter("\\A").next();
        } catch (FileNotFoundException e) {
            Log.e(TAG, "unable to open " + path);
            e.printStackTrace();
        }
        return entireFileText;
    }
    */

    protected Properties getProperties() {
        Properties properties = null;

        try {
            InputStream inputStream = this.getResources().getAssets().open("config.properties");
            properties = new Properties();
            properties.load(inputStream);
            Log.d(TAG, "properties are now loaded");
        } catch (IOException e) {
            Log.e(TAG, "properties failed to load, " + e);
        }
        return properties;
    }


}
