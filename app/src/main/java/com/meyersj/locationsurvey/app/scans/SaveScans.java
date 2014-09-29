package com.meyersj.locationsurvey.app.scans;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.zxing.Result;
import com.meyersj.locationsurvey.app.util.Cons;
import com.meyersj.locationsurvey.app.util.PostService;
import com.meyersj.locationsurvey.app.util.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;


public class SaveScans {

    private final String TAG = "SaveScans";
    private Float THRESHOLD = Float.valueOf(1000 * 20);


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

        //check time delta between date and currentLoc date
        if (currentLoc.timeDifference(date) <= THRESHOLD) {
            Log.d(TAG, "posting scan");
            postScan(rawResult, date);
            //For debugging to write to csv
            /*
            Utils.appendCSV("current," +
                    Utils.dateFormat.format(date) + "," +
                    currentLoc.getAccuracy() + "," +
                    currentLoc.getLat() + "," + currentLoc.getLon());
            */
        }
        else {
            Log.d(TAG, "adding scan to buffer");
            bufferScan(rawResult, date);
        }
    }


    public void flushBuffer() {
        Integer total = 0;
        Integer count = 0;

        Log.d(TAG, "flushing buffer");
        for(Scan scan: scansBuffer) {
            total += 1;
            Float diff = currentLoc.timeDifference(scan.getDate());

            if(diff <= THRESHOLD) {
                count += 1;
                post(scan.getParams());
                Log.d(TAG, "using new location");
                //For debugging to write to csv
                /*
                Utils.appendCSV("valid_buffer," +
                        Utils.dateFormat.format(scan.getDate()) + "," +
                        currentLoc.getAccuracy() + "," +
                        currentLoc.getLat() + "," + currentLoc.getLon());
                */
            }
            else {
                Log.d(TAG, "too old, deleting");
                //For debugging to write to csv
                /*
                Utils.appendCSV("old_buffer," +
                        Utils.dateFormat.format(scan.getDate()) + "," +
                        currentLoc.getAccuracy() + "," +
                        currentLoc.getLat() + "," + currentLoc.getLon());
                */
            }

        }

        //for debug - show how many flushed records were valid

        //String message = "Flush: count=" + String.valueOf(count) + " total=" + String.valueOf(total);
        //Utils.longToastCenter(context, message);
        scansBuffer.clear();
    }

    private void post(Bundle params) {
        params.putString(Cons.LAT, currentLoc.getLat());
        params.putString(Cons.LON, currentLoc.getLon());
        Intent post = new Intent(context, PostService.class);
        post.putExtras(params);
        context.startService(post);
    }

}
