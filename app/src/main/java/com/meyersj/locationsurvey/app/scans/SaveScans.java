package com.meyersj.locationsurvey.app.scans;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;
import com.meyersj.locationsurvey.app.util.Constants;
import com.meyersj.locationsurvey.app.util.PostService;
import com.meyersj.locationsurvey.app.util.Utils;

import java.util.ArrayList;
import java.util.Date;



public class SaveScans {


    private final String TAG = "SaveScans";

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

    private Context context;
    private CurrentLocation currentLoc;
    private ArrayList<Scan> scansBuffer;

    private String url;
    private String user_id;
    private String line;
    private String dir;
    private String mode;


    public SaveScans(Context context, Bundle params) {
        this.url = params.getString(Constants.URL);
        this.user_id = params.getString(Constants.USER_ID);
        this.line = params.getString(Constants.LINE);
        this.dir = params.getString(Constants.DIR);
        this.mode = params.getString(Constants.MODE);
        this.context = context;
        this.currentLoc = new CurrentLocation();
        this.scansBuffer = new ArrayList<Scan>();
    }

    public void setLocation(String lat, String lon, Float accuracy, String dateString) {
        currentLoc.setLocation(lat, lon, accuracy, dateString);
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    //don't add lat and lon because we might be waiting for a more recent location
    private Bundle buildParams(String uuid, String date) {
        Bundle params = new Bundle();
        params.putString(Constants.URL, url);
        params.putString(Constants.USER_ID, user_id);
        params.putString(Constants.LINE, line);
        params.putString(Constants.DIR, dir);
        params.putString(Constants.MODE, mode);
        params.putString(Constants.UUID, uuid);
        params.putString(Constants.DATE, date);
        params.putString(Constants.TYPE, Constants.SCAN);
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

        //check time delta between date and currentLoc date
        if (currentLoc.timeDifference(date) <= 15) {
            Log.d(TAG, "posting scan");
            postScan(rawResult, date);
        }
        else {
            Log.d(TAG, "adding scan to buffer");
            bufferScan(rawResult, date);
        }
    }


    public void flushBuffer() {
        Log.d(TAG, "flushing buffer");
        for(Scan scan: scansBuffer) {
            Log.d(TAG, "blah");
            //Bundle params = scan.getParams();
            //TODO check for how old the scan is versus when the location was recieved
            post(scan.getParams());
        }
        scansBuffer.clear();
    }

    private void post(Bundle params) {
        params.putString(Constants.LAT, currentLoc.getLat());
        params.putString(Constants.LAT, currentLoc.getLon());
        Intent post = new Intent(context, PostService.class);
        post.putExtras(params);
        context.startService(post);
    }

}
