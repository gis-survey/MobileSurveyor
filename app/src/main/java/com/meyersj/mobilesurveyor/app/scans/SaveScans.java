package com.meyersj.mobilesurveyor.app.scans;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;


public class SaveScans {

    private final String TAG = "SaveScans";
    private Float THRESHOLD = Float.valueOf(1000 * 20);

    private Context context;
    private CurrentLocation currentLoc;
    private ArrayList<Scan> scansBuffer;

    private String url;
    private String user_id;
    private String line;
    private String dir;
    private String mode;
    private HttpClient client;


    private class Scan {
        private Date date;
        private Bundle params;

        public Scan(Date date, Bundle params) {
            this.date = date;
            this.params = params;
        }

        public Date getDate() {
            return this.date;
        }
        public Bundle getParams() {
            return this.params;
        }
    }


    public SaveScans(Context context, Bundle params) {
        this.url = params.getString(Cons.URL);
        this.user_id = params.getString(Cons.USER_ID);
        this.line = params.getString(Cons.LINE);
        this.dir = params.getString(Cons.DIR);
        this.mode = params.getString(Cons.MODE);
        this.context = context;
        this.currentLoc = new CurrentLocation();
        this.scansBuffer = new ArrayList<Scan>();


        Properties prop;
        prop = Utils.getProperties(this.context, Cons.PROPERTIES);

        if( prop.containsKey(Cons.GPS_THRESHOLD)) {
            THRESHOLD = Float.valueOf(prop.getProperty(Cons.GPS_THRESHOLD));
        }

        this.client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();

        //10 second timeout
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);
        httpParams.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

    }

    public void setLocation(String lat, String lon, Float accuracy, String dateString) {
        currentLoc.setLocation(lat, lon, accuracy, dateString);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }


    public String getMode() {
        return this.mode;
    }

    //don't add lat and lon because we might be waiting for a more recent location
    private Bundle buildParams(String uuid, String date) {
        Bundle params = new Bundle();
        params.putString(Cons.URL, url);
        params.putString(Cons.USER_ID, user_id);
        params.putString(Cons.LINE, line);
        params.putString(Cons.DIR, dir);
        params.putString(Cons.MODE, mode);
        params.putString(Cons.UUID, uuid);
        params.putString(Cons.DATE, date);
        params.putString(Cons.TYPE, Cons.SCAN);
        return params;
    }

    //add scan to scans array list without location params
    private void bufferScan(Result rawResult, Date date) {
        Bundle params = buildParams(rawResult.toString(), Utils.dateFormat.format(date));
        Scan scan = new Scan(date, params);
        scansBuffer.add(scan);
    }

    private void postScan(Result rawResult, Date date) {
        Bundle params = buildParams(rawResult.toString(), Utils.dateFormat.format(date));
        post(params);
    }

    public void save(Result rawResult) {
        Date date = new Date();
        Log.d(TAG, rawResult.toString());

        if(currentLoc.getLat() == null || currentLoc.getLon() == null) {
            Log.d(TAG, "current loc null - add buffer");
            bufferScan(rawResult, date);
        }
        else if(currentLoc.getLat().equals("0") || currentLoc.getLon().equals("0")) {
            Log.d(TAG, "acurret loc 0 - add buffer");
            bufferScan(rawResult, date);
        }
        //check time delta between date and currentLoc date
        else if ((Utils.timeDifference(currentLoc.getDate(), date) <= THRESHOLD) &&
                currentLoc.getLat() != null) {
            Log.d(TAG, "posting scan");
            postScan(rawResult, date);
        }
        else {
            Log.d(TAG, "adding scan to buffer - other reasons");
            bufferScan(rawResult, date);
            Log.d(TAG, currentLoc.getLat());
            Log.d(TAG, currentLoc.getLon());
            Log.d(TAG, currentLoc.getDate().toString());
            Log.d(TAG, String.valueOf(Utils.timeDifference(currentLoc.getDate(), date)));
        }
    }

    public void flushBuffer() {
        for(Scan scan: scansBuffer) {
            Float diff = Utils.timeDifference(currentLoc.getDate(), scan.getDate());
            Log.d(TAG, String.valueOf(diff));

            if(diff <= THRESHOLD) {
                post(scan.getParams());
            }
        }
        scansBuffer.clear();
    }

    private void post(Bundle params) {
        params.putString(Cons.LAT, currentLoc.getLat());
        params.putString(Cons.LON, currentLoc.getLon());

        Utils.appendCSV("scans", buildScanRow(params));
        String[] paramsArray = getScanParams(params);

        if (Utils.getProperties(context, Cons.PROPERTIES).getProperty("mode").equals("api")) {
            PostTask task = new PostTask();
            task.execute(paramsArray);
        }

    }


    protected String buildScanRow(Bundle bundle) {
        String row = "";
        row += bundle.getString(Cons.DATE) + ",";
        row += bundle.getString(Cons.UUID) + ",";
        row += bundle.getString(Cons.USER_ID) + ",";
        row += bundle.getString(Cons.LINE) + ",";
        row += bundle.getString(Cons.DIR) + ",";
        row += bundle.getString(Cons.MODE) + ",";
        row += bundle.getString(Cons.LAT) + ",";
        row += bundle.getString(Cons.LON);
        return row;
    }



    protected String[] getScanParams(Bundle bundle) {
        String[] params = new String[2];
        JSONObject json = new JSONObject();
        json.put(Cons.UUID, bundle.getString(Cons.UUID));
        json.put(Cons.DATE, bundle.getString(Cons.DATE));
        json.put(Cons.USER_ID, bundle.getString(Cons.USER_ID));
        json.put(Cons.LINE, bundle.getString(Cons.LINE));
        json.put(Cons.DIR, bundle.getString(Cons.DIR));
        json.put(Cons.MODE, bundle.getString(Cons.MODE));
        json.put(Cons.LON, bundle.getString(Cons.LON));
        json.put(Cons.LAT, bundle.getString(Cons.LAT));
        params[0] = Utils.getUrlApi(context) + "/insertScan";
        params[1] = json.toJSONString();
        return params;
    }


    protected String post(String[] params) {

        String retVal = null;
        Log.d(TAG, "post function");

        HttpPost post = new HttpPost(params[0]);
        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Cons.DATA, params[1]));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);
            HttpEntity entityR = response.getEntity();
            Log.d(TAG, EntityUtils.toString(entityR));
            retVal = response.toString();

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException" + e.toString());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
        }
        return retVal;
    }

    class PostTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            Log.d(TAG, "url:" + params[0]);
            Log.d(TAG, "data:" + params[1]);
            return post(params);
        }
        @Override
        protected void onPostExecute(String response) {
            Log.d(TAG, "onPostExecute(): " + response);
        }
    }


}
