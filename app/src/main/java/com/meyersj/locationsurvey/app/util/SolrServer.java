package com.meyersj.locationsurvey.app.util;

import android.os.AsyncTask;
import android.util.Log;

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
import java.util.List;

/**
 * Created by meyersj on 7/11/2014.
 */
public class SolrServer {

    private final String TAG = "SolrServer";

    private List<String> solrResults = new ArrayList<String>();

    public SolrServer() {

    }

    public List<String> searchResults(String input) {
        List<String> list = new ArrayList<String>();

        String url = "http://ride.trimet.org/solr/select?start=0&wt=json&qt=dismax&rows=10&q=" + input.replace(" ", "%20");

        new SolrTask().execute(url);

        /*
        if(input.startsWith("a")) {
            list.add("aabas");
            list.add("aaasfa");
            list.add("asgtklab");
        }
        else if (input.startsWith("b")) {
            list.add("bbasf");
            list.add("bbasasfafsf");
            list.add("bbasasfasfafsf");
        }
        else {
            list.add("1111");
            list.add("222222");
            list.add("4444");
        }
        */
        return solrResults;
    }


    private class SolrTask extends AsyncTask<String, String, String> {

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

    private void parseResponse(String response) {
        JSONParser parser = new JSONParser();
        try {
            //Object obj = parser.parse(response);
            JSONObject responseJSON = (JSONObject) parser.parse(response);

            String responseHeader = responseJSON.get("responseHeader").toString();
            //Log.d(TAG, responseHeader);

            //String responseData = responseJSON.get("response").toString();
            //Log.d(TAG, responseData);

            JSONObject responseData = (JSONObject) responseJSON.get("response");
            JSONArray responseDocs = (JSONArray) responseData.get("docs");
            //Log.d(TAG, responseDocs.toString());

            solrResults.clear();

            Log.d(TAG, "*************************************");
            for(Object j: responseDocs) {
                //Log.d(TAG, j.toString());
                JSONObject record = (JSONObject) j;

                solrResults.add(record.get("name").toString());
                Log.d(TAG,record.get("name").toString());
                Log.d(TAG,record.get("score").toString());
                //String name = record.get("name").toString();
                //String city = record.get("city").toString();
                //String zip = record.get("zip_code").toString();
                //String lat = record.get("lat").toString();
                //String lon = record.get("lon").toString();
            }


        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


}
