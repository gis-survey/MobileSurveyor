package com.meyersj.mobilesurveyor.app.scans;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Endpoints;
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
    //private String mode;
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
        //this.mode = params.getString(Cons.MODE);
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

    //public void setMode(String mode) {
    //    this.mode = mode;
    //}

    //public String getMode() {
    //    return this.mode;
    //}

    //don't add lat and lon because we might be waiting for a more recent location
    private Bundle buildParams(String uuid, String date, String mode) {
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
    private void bufferScan(Result rawResult, Date date, String mode) {
        Bundle params = buildParams(rawResult.toString(), Utils.dateFormat.format(date), mode);
        Scan scan = new Scan(date, params);
        scansBuffer.add(scan);
    }

    private void postScan(Result rawResult, Date date, String mode) {
        Bundle params = buildParams(rawResult.toString(), Utils.dateFormat.format(date), mode);
        post(params);
    }

    public void save(Result rawResult, String mode) {
        Date date = new Date();
        Log.d(TAG, rawResult.toString());

        // lat-lon is empty so buffer data until gps location is updated
        if(currentLoc.getLat() == null || currentLoc.getLon() == null) {
            Log.d(TAG, "current loc null - add buffer");
            bufferScan(rawResult, date, mode);
        }
        // lat-lon is 0 so buffer data until gps location is updated
        else if(currentLoc.getLat().equals("0") || currentLoc.getLon().equals("0")) {
            bufferScan(rawResult, date, mode);
        }
        // check time delta between current timestamp and timestamp from last gps location
        // if it exceeds THRESHOLD then buffer data until new location is recieved
        else if ((Utils.timeDifference(currentLoc.getDate(), date) <= THRESHOLD) &&
                currentLoc.getLat() != null) {
            Log.d(TAG, "posting scan");
            postScan(rawResult, date, mode);
        }
        else {
            Log.d(TAG, "adding scan to buffer - other reasons");
            bufferScan(rawResult, date, mode);
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
        String[] params = new String[9];
        params[0] = Utils.getUrlApi(context) + Endpoints.INSERT_SCAN;
        params[1] = bundle.getString(Cons.UUID);
        params[2] = bundle.getString(Cons.DATE);
        params[3] = bundle.getString(Cons.USER_ID);
        params[4] = bundle.getString(Cons.LINE);
        params[5] = bundle.getString(Cons.DIR);
        params[6] = bundle.getString(Cons.MODE);
        params[7] = bundle.getString(Cons.LON);
        params[8] = bundle.getString(Cons.LAT);
        return params;
    }


    protected String post(String[] params) {

        String retVal = null;
        Log.d(TAG, "post function");

        HttpPost post = new HttpPost(params[0]);
        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Cons.UUID, params[1]));
        postParam.add(new BasicNameValuePair(Cons.DATE, params[2]));
        postParam.add(new BasicNameValuePair(Cons.USER_ID, params[3]));
        postParam.add(new BasicNameValuePair(Cons.LINE, params[4]));
        postParam.add(new BasicNameValuePair(Cons.DIR, params[5]));
        postParam.add(new BasicNameValuePair(Cons.MODE, params[6]));
        postParam.add(new BasicNameValuePair(Cons.LON, params[7]));
        postParam.add(new BasicNameValuePair(Cons.LAT, params[8]));

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
