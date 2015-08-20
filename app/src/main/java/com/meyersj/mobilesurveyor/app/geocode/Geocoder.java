package com.meyersj.mobilesurveyor.app.geocode;


import android.util.Log;

import com.meyersj.mobilesurveyor.app.util.Cons;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;


public class Geocoder {

    private final String TAG = getClass().getCanonicalName();

    private String url = null;
    private OkHttpClient httpClient;

    private HashMap<String, LocationResult> resultsHash = new HashMap<String, LocationResult>();
    private ArrayList<String> resultsInOrder = new ArrayList<String>();

    public Geocoder(String url) {
        this.url = url;
        this.httpClient = new OkHttpClient();
    }

    public HashMap<String, LocationResult> getResultsHash() {
        return resultsHash;
    }

    public ArrayList<String> getResultsInOrder() {
        return resultsInOrder;
    }

    public void clearResults() {
        resultsHash.clear();
        resultsInOrder.clear();
    }

    private String addParam(String key, String value) {
        try {
            return "&" + key + "=" + URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.d(TAG, e.toString());
            return "";
        }
    }

    private String buildParams(String input) {
        String lat = String.valueOf(Cons.CENTROID.getLatitude());
        String lon = String.valueOf(Cons.CENTROID.getLongitude());

        String params = addParam("input", input);
        params += addParam("lat", lat);
        params += addParam("lon", lon);
        params += addParam("size", "5");
        params += addParam("layers", "address,poi");
        params += addParam("details", "false");
        return params;
    }

    protected void lookup(String input) {
        Request request = new Request.Builder()
                .url(url + "?" + buildParams(input))
                .build();

        try {
            Response response = httpClient.newCall(request).execute();
            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
            clearResults();
            parseResponse(response.body().string());
        }
        catch (IOException e) {
            Log.d(TAG, e.toString());
        }

    }

    protected void parseResponse(String responseString) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject responseJSON = (JSONObject) parser.parse(responseString);
            JSONArray features = (JSONArray) responseJSON.get("features");
            for (Object obj : features) {
                JSONObject feature = (JSONObject) obj;
                LocationResult record = parseRecord(feature);
                resultsHash.put(record.toString(), record);
                resultsInOrder.add(0, record.toString());
            }

        } catch (ParseException e) {
            Log.d(TAG, e.toString());
        }

    }

    protected LocationResult parseRecord(JSONObject record) {
        Log.d(TAG, record.toString());
        JSONObject geometry = (JSONObject) record.get("geometry");
        JSONObject properties = (JSONObject) record.get("properties");
        JSONArray coordinates = (JSONArray) geometry.get("coordinates");
        String text = properties.get("text").toString();
        String lon = coordinates.get(0).toString();
        String lat = coordinates.get(1).toString();
        return new LocationResult(text, lat, lon);
    }


}
