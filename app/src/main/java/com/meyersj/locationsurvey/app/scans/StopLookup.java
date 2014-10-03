package com.meyersj.locationsurvey.app.scans;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import com.meyersj.locationsurvey.app.util.Cons;
import com.meyersj.locationsurvey.app.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


public class StopLookup {

    private final String TAG = "StopLookup";
    private Context context;
    private String url;
    private String line;
    private String dir;
    private TextView stopText;

    public StopLookup(Context context, TextView stopText, String url, String line, String dir) {
        this.context = context;
        this.stopText = stopText;
        this.url = url;
        this.line = line;
        this.dir = dir;
    }

    public void findStop(String lat, String lon) {
        StopLookupTask task = new StopLookupTask();
        task.execute(buildParams(lat, lon));
    }

    class StopLookupTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            return lookup(params);
        }
        @Override
        protected void onPostExecute(String response) {
            handleResponse(response);
        }
    }

    private String lookup(String[] params) {
        String retVal;
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(params[0]);

        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Cons.DATA, params[1]));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);
            HttpEntity entityR = response.getEntity();
            retVal = EntityUtils.toString(entityR);
            Log.d(TAG, retVal);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException");
            Log.e(TAG, e.toString());
            retVal = "UnsupportedEncodingException";
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
            Log.e(TAG, e.toString());
            retVal = "ClientProtocolException";
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
            Log.e(TAG, e.toString());
            retVal = "IOException";
        }

        return retVal;
    }

    private String[] buildParams(String lat, String lon) {
        String[] params = new String[2];
        JSONObject json = new JSONObject();
        json.put(Cons.LINE, line);
        json.put(Cons.DIR, dir);
        json.put(Cons.LAT, lat);
        json.put(Cons.LON, lon);
        params[0] = this.url;
        params[1] = json.toJSONString();
        return params;
    }

    private void handleResponse(String response) {
        String message = Cons.NEAR_STOP + "error finding near stop";
        JSONParser parser = new JSONParser();

        try{
            Object obj = parser.parse(response);
            JSONObject results = (JSONObject) obj;
            String error = results.get("error").toString();
            String stopName = results.get("stop_name").toString();
            message = error.equals("false") ? Cons.NEAR_STOP + stopName : message;

        } catch(ParseException pe){
            Log.e(TAG, pe.toString());
        }
        stopText.setText(message);
    }
}
