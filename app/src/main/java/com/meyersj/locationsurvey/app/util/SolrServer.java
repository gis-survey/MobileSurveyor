package com.meyersj.locationsurvey.app.util;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.mapbox.mapboxsdk.geometry.LatLng;

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
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by meyersj on 7/11/2014.
 */
public class SolrServer {

    private final String TAG = "SolrServer";

    //private List<String> solrResults = new ArrayList<String>();
    //private List<LocationResult> solrResults2 = new ArrayList<LocationResult>();

    //private String responseString;

    private HashMap<String, LocationResult> solrResults = new HashMap<String, LocationResult>();


    public SolrServer() {

    }


    /*
    public List<String> searchResults(String input) {
        //List<String> list = new ArrayList<String>();

        String url = "http://maps10.trimet.org/solr/select?start=0&wt=json&qt=dismax&rows=10&q=" + input.replace(" ", "%20");

        new SolrSearch().execute(url);

        //will not return results first time because solrResults is getting update asynchronously
        return solrResults;
    }


    public HashMap<String, LocationResult> searchResults2(String input) {
        //List<String> list = new ArrayList<String>();

        String url = "http://maps10.trimet.org/solr/select?start=0&wt=json&qt=dismax&rows=1&q=" + input.replace(" ", "%20");

        new SolrSearch().execute(url);

        //will not return results first time because solrResults is getting update asynchronously
        return solrResults2;
    }
    */


    public HashMap<String, LocationResult> getSolrResults() {
        return solrResults;
    }

    protected void solrLookup(String input) {

        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        //String responseString = null;

        String url = "http://maps10.trimet.org/solr/select?start=0&wt=json&qt=dismax&rows=5&q=" + input.replace(" ", "%20");


        try {
            response = httpclient.execute(new HttpGet(url));
            StatusLine statusLine = response.getStatusLine();
            //Log.d(TAG, statusLine.toString());
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                parseResponse(out.toString());
                //this.responseString = out.toString();
                //Log.d(TAG, responseString);
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

        //Log.d(TAG, responseString);


    }


    /*
    private class SolrSearch extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;

            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                //Log.d(TAG, statusLine.toString());
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    //Log.d(TAG, responseString);
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
                //Log.d(TAG, result);
                //addMarker(getLatLng(result));
                parseResponse(result);


            }
            else {
                Log.d(TAG, "geocode query not successful");
            }

            //Do anything with response..
        }
    }
    */


    protected void parseResponse(String responseString) {

        //List<String> results = null;


        JSONParser parser = new JSONParser();
        try {
            //Object obj = parser.parse(response);
            JSONObject responseJSON = (JSONObject) parser.parse(responseString);

            String responseHeader = responseJSON.get("responseHeader").toString();
            //Log.d(TAG, responseHeader);

            //String responseData = responseJSON.get("response").toString();
            //Log.d(TAG, responseData);

            JSONObject responseData = (JSONObject) responseJSON.get("response");
            JSONArray responseDocs = (JSONArray) responseData.get("docs");
            //Log.d(TAG, responseDocs.toString());

            //solrResults.clear();
            solrResults.clear();

            //Log.d(TAG, "*************************************");

            //results = new ArrayList<String>();

            Log.d(TAG, "Start results from solr");
            for(Object j: responseDocs) {
                //Log.d(TAG, j.toString());
                JSONObject record = (JSONObject) j;
                //Log.d(TAG, record.toString());

                //solrResults.add(record.get("name").toString());
                LocationResult result = parseRecord(record);

                if (result != null) {
                    Log.d(TAG, result.getAddress());
                    solrResults.put(result.getAddress(), result);
                }
                else {
                    Log.e(TAG, "parseRecord not successful");
                }



            }
            Log.d(TAG, "End results from solr\n");


        } catch (ParseException e) {
            e.printStackTrace();
        }

        //return results;

    }


    protected LocationResult parseRecord(JSONObject record) {
        LocationResult result = new LocationResult();



        if (record.containsKey("score")) {
            result.setScore(record.get("score").toString());
        }

        if(record.containsKey("lat") && record.containsKey("lon")) {
            result.setLatLng(record.get("lat").toString(),record.get("lon").toString());
        }

        if(record.containsKey("city") && record.containsKey("name")){
            String name = record.get("name").toString();
            String city = record.get("city").toString();
            String score = record.get("score").toString();
            if (!city.equals("") && !name.equals("")) {
                result.setAddress(name + ", " + city);
            }
            else if (!name.equals("")) {
                result.setAddress(name);
            }
            else {
                result.setAddress(null);
            }
        }
        else {
            result.setAddress(null);
        }


        if (!result.isValid()) {
            result = null;
        }


        return result;



    }


}
