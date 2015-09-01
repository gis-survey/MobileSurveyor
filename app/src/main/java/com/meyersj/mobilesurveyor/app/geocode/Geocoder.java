package com.meyersj.mobilesurveyor.app.geocode;


import android.text.TextUtils;
import android.util.Log;

import com.loopj.android.http.SyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;
import com.meyersj.mobilesurveyor.app.util.Cons;

import org.apache.http.Header;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Geocoder {

    private final String TAG = getClass().getCanonicalName();

    private String solrUrl = null;
    private String peliasUrl = null;
    private SyncHttpClient client;
    private SurveyManager manager;
    private String mode;
    private final String solrParams = "wt=json&rows=4&qt=dismax";

    private HashMap<String, LocationResult> resultsHash = new HashMap<String, LocationResult>();
    private ArrayList<String> resultsInOrder = new ArrayList<String>();

    public Geocoder(String solrUrl, String peliasUrl) {
        this.solrUrl = solrUrl + "?" + solrParams;
        this.peliasUrl = peliasUrl;
        this.client = new SyncHttpClient();
    }


    public Geocoder(String solrUrl, String peliasUrl, SurveyManager manager, String mode) {
        this.solrUrl = solrUrl + "?" + solrParams;
        this.peliasUrl = peliasUrl;
        this.client = new SyncHttpClient();
        this.manager = manager;
        this.mode = mode;
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

    private String buildParams(String input, int count) {
        String lat = String.valueOf(Cons.CENTROID.getLatitude());
        String lon = String.valueOf(Cons.CENTROID.getLongitude());

        String params = addParam("input", input);
        params += addParam("lat", lat);
        params += addParam("lon", lon);
        params += addParam("size", String.valueOf(count));
        params += addParam("layers", "poi");
        params += addParam("details", "false");
        return params;
    }

    protected void lookup(String input) {
        if(manager != null) manager.setSeachString(input, mode);
        clearResults();
        Pattern intersectionPattern = Pattern.compile("(.*)\\s(and|&)\\s(.*)");
        Matcher intersection = intersectionPattern.matcher(input);
        if (intersection.matches()) {
            String street1  = intersection.group(1);
            String street2  = intersection.group(3);
            lookupSolr(street1, street2);
            lookupPelias(input, 4);
        }
        else {
            lookupPelias(input, 8);
        }
    }

    protected void lookupPelias(String input, int count) {
        String urlParams = peliasUrl + "?" + buildParams(input, count);
        this.client = new SyncHttpClient();
        client.get(urlParams, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, String.valueOf(statusCode));
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                Log.d(TAG, responseString);
                //clearResults();
                parsePeliasResponse(responseString);
            }
        });
    }


    protected String solrTerm(String field, String value) {
        if(value.length() > 5) value = value + "~2";
        return field + ":" + value;
    }

    protected String solrTerms(String field, String street) {
        String[] split = street.split("\\s+");
        for(int i = 0; i < split.length; i++) {
            split[i] = solrTerm(field, split[i]);
        }
        return TextUtils.join(" AND ", split);
    }

    protected void lookupSolr(String street1, String street2) {
        String q = solrTerms("street_1", street1) + " AND " + solrTerms("street_2", street2);
        String urlParams = solrUrl + addParam("q", q);
        this.client = new SyncHttpClient();
        client.get(urlParams, new TextHttpResponseHandler() {

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "FAILURE: " + responseString);
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
        resultsInOrder.add(record.toString());
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
            Log.d(TAG, responseString);
            if(responseJSON.containsKey("features")) {
                JSONArray features = (JSONArray) responseJSON.get("features");
                for (Object obj : features) {
                    addRecord(parsePeliasRecord((JSONObject) obj));
                }
            }
            else {
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
        String street1 = record.get("street_1").toString();
        String street2 = record.get("street_2").toString();
        String text = street1 + " & " + street2;
        if(record.containsKey("city")) text += appendString(record.get("city").toString());
        if(record.containsKey("zip")) text += appendString(record.get("zip").toString());
        if(record.containsKey("county")) text += appendString(record.get("county").toString());
        String lat = record.get("lat").toString();
        String lon = record.get("lon").toString();
        return new LocationResult(text, lat, lon);
    }

    protected String appendString(String input) {
        return !input.isEmpty() ? ", " + input : "";
    }


}
