package com.meyersj.mobilesurveyor.app.geocode;


import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.TextHttpResponseHandler;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.squareup.okhttp.OkHttpClient;
//import com.squareup.okhttp.Request;
//import com.squareup.okhttp.Response;

import org.apache.http.Header;
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
    private final String solrParams = "wt=json&rows=5&qt=dismax";

    private HashMap<String, LocationResult> resultsHash = new HashMap<String, LocationResult>();
    private ArrayList<String> resultsInOrder = new ArrayList<String>();

    public Geocoder(String url) {
        this.url = url + "?" + solrParams;
        this.httpClient = new OkHttpClient();
    }

    public HashMap<String, LocationResult> getResultsHash() {
        return resultsHash;
    }

    public ArrayList<String> getResultsInOrder() {
        return resultsInOrder;
    }



    private String addParam(String key, String value) {
        try {
            Log.d(TAG, value + " " + URLEncoder.encode(value, "UTF-8"));
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
        params += addParam("size", "8");
        params += addParam("layers", "poi");
        params += addParam("details", "false");
        return params;
    }

    protected void lookup(String input) {
        lookupSolr(input);
    }

    protected void lookupPelias(String input) {
        Log.d(TAG, "lookup: " + input);
        String urlParams = url + "?" + buildParams(input);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(urlParams, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, responseString);
                //clearResults();
                //parseResponse(responseString);
            }
        });
    }

    protected void lookupSolr(String input) {
        Log.d(TAG, "lookup: " + input);
        String urlParams = url + addParam("q", input.replace(" & ", " and "));
        AsyncHttpClient client = new AsyncHttpClient();
        client.get(urlParams, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, responseString);
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, responseString);
                parseSolrResponse(responseString);
            }
        });
    }

    public void clearResults() {
        resultsHash.clear();
        resultsInOrder.clear();
    }

    protected void addRecord(LocationResult record) {
        //int size = resultsInOrder.size();
        //if(size > 5) {
        //    resultsInOrder.remove(size - 1);
        //}
        resultsInOrder.add(0, record.toString());
        resultsHash.put(record.toString(), record);
    }

    protected void parseSolrResponse(String responseString) {

        JSONParser parser = new JSONParser();
        try {
            JSONObject responseJSON = (JSONObject) parser.parse(responseString);
            JSONObject responseData = (JSONObject) responseJSON.get("response");
            JSONArray responseDocs = (JSONArray) responseData.get("docs");
            clearResults();
            for(Object j: responseDocs) {
                JSONObject record = (JSONObject) j;
                addRecord(parseSolrRecord(record));
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    protected void parsePeliasResponse(String responseString) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject responseJSON = (JSONObject) parser.parse(responseString);

            if(responseJSON.containsKey("features")) {
                JSONArray features = (JSONArray) responseJSON.get("features");
                for (Object obj : features) {
                    JSONObject feature = (JSONObject) obj;
                    Log.d(TAG, "loop: " + feature.toString());
                    addRecord(parsePeliasRecord(feature));
                }
            }
            else {
                Log.d(TAG, "loop: " + responseJSON.toString());
                addRecord(parsePeliasRecord(responseJSON));
            }
        } catch (ParseException e) {
            Log.d(TAG, e.toString());
        }
    }

    protected LocationResult parsePeliasRecord(JSONObject record) {
        Log.d(TAG, record.toString());
        JSONObject geometry = (JSONObject) record.get("geometry");
        JSONObject properties = (JSONObject) record.get("properties");
        JSONArray coordinates = (JSONArray) geometry.get("coordinates");
        String text = properties.get("text").toString();
        String lon = coordinates.get(0).toString();
        String lat = coordinates.get(1).toString();
        return new LocationResult(text, lat, lon);
    }

    protected LocationResult parseSolrRecord(JSONObject record) {
        Log.d(TAG, record.toString());
        String street = record.get("street").toString();
        String city = record.get("city").toString();
        String zip = record.get("zip").toString();
        String county = record.get("county").toString();
        String text = street + appendString(city) + appendString(zip) + appendString(county);
        String lat = record.get("lat").toString();
        String lon = record.get("lon").toString();
        return new LocationResult(text, lat, lon);
    }

    protected String appendString(String input) {
        return !input.isEmpty() ? ", " + input : "";
    }


}
