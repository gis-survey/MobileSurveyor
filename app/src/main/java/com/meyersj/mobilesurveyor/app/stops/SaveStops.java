package com.meyersj.mobilesurveyor.app.stops;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.geometry.BoundingBox;
import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Endpoints;
import com.meyersj.mobilesurveyor.app.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by jeff on 8/8/15.
 */
public class SaveStops {

    private final String TAG = "SaveStops";
    private Activity activity;
    private Bundle args;
    private Button submit;
    private StopsManager manager;
    private MapView mv;
    private BoundingBox bbox;
    private HttpClient client;

    public SaveStops (final Activity activity, final StopsManager manager, MapView mv, BoundingBox bbox, Bundle args) {
        this.activity = activity;
        this.args = args;
        this.mv = mv;
        this.bbox = bbox;
        submit = (Button) activity.findViewById(R.id.submit);
        this.manager = manager;

        //TODO move to seperate class for all other posts
        client = new DefaultHttpClient();
        HttpParams httpParams = client.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, 10 * 1000);
        HttpConnectionParams.setSoTimeout(httpParams, 10 * 1000);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (manager.validateSelection()) {
                    //verify correct locations
                    if (manager.validateStopSequence()) {
                        verifyPOST();
                    }
                    else {
                        Utils.longToastCenter(activity.getApplicationContext(),
                                "Invalid stop sequence based on route direction");
                    }
                }
                else {
                    Utils.longToastCenter(activity.getApplicationContext(),
                            "Both boarding and alighting locations must be set");
                }
            }
        });

    }

    protected void resetMap() {
        manager.clearCurrentMarker();
        mv.zoomToBoundingBox(bbox, true, false, true, true);
    }


    protected void verifyPOST() {

        final Stop board = (Stop) manager.getBoard();
        final Stop alight = (Stop) manager.getAlight();

        if (Utils.isNetworkAvailable(activity.getApplicationContext())) {
            String boardLoc = board.getTitle();
            String alightLoc = alight.getTitle();
            String message = Cons.BOARD + ": " + boardLoc + "\n\n" + Cons.ALIGHT + ": " + alightLoc;
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Are you sure you want to submit these locations?")
                    .setMessage(message)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //get stop ids
                            String onStop = board.getDescription();
                            String offStop = alight.getDescription();
                            postResults(onStop, offStop);
                            resetMap();
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    });

            AlertDialog select = builder.create();
            select.show();
        } else {
            Utils.shortToastCenter(activity, "Please enable network connections.");
        }
    }

    protected void postResults(String onStop, String offStop) {
        Log.d(TAG, "posting results");
        Date date = new Date();

        Bundle extras = new Bundle();
        extras.putString(Cons.URL, args.getString(Cons.URL));
        extras.putString(Cons.LINE, args.getString(Cons.LINE));
        extras.putString(Cons.DIR, args.getString(Cons.DIR));
        extras.putString(Cons.DATE, Utils.dateFormat.format(date));
        extras.putString(Cons.ON_STOP, onStop);
        extras.putString(Cons.OFF_STOP, offStop);
        extras.putString(Cons.USER_ID, args.getString(Cons.USER_ID));
        extras.putString(Cons.TYPE, Cons.PAIR);

        Utils.appendCSV("stops", buildPairRow(extras));
        if (Utils.getProperties(activity, Cons.PROPERTIES).getProperty("mode").equals("api")) {
            String[] params = getPairParams(extras);
            PostTask task = new PostTask();
            task.execute(params);
        }
    }

    protected String buildPairRow(Bundle bundle) {
        String row = "";
        row += bundle.getString(Cons.DATE) + ",";
        row += bundle.getString(Cons.USER_ID) + ",";
        row += bundle.getString(Cons.LINE) + ",";
        row += bundle.getString(Cons.DIR) + ",";
        row += bundle.getString(Cons.ON_STOP) + ",";
        row += bundle.getString(Cons.OFF_STOP) + ",";
        row += bundle.getString(Cons.ON_REVERSED) + ",";
        row += bundle.getString(Cons.OFF_REVERSED);
        return row;
    }

    protected String[] getPairParams(Bundle bundle) {
        String[] params = new String[9];
        params[0] = Utils.getUrlApi(activity) + Endpoints.INSERT_PAIR;
        params[1] = bundle.getString(Cons.USER_ID);
        params[2] = bundle.getString(Cons.DATE);
        params[3] = bundle.getString(Cons.LINE);
        params[4] = bundle.getString(Cons.DIR);
        params[5] = bundle.getString(Cons.ON_STOP);
        params[6] = bundle.getString(Cons.OFF_STOP);
        params[7] = bundle.getString(Cons.ON_REVERSED);
        params[8] = bundle.getString(Cons.OFF_REVERSED);
        return params;
    }

    class PostTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            return post(params);
        }
        @Override
        protected void onPostExecute(String response) {
            Log.d(TAG, "onPostExecute(): " + response);
        }
    }


    protected String post(String[] params) {
        String retVal = null;
        HttpPost post = new HttpPost(params[0]);
        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Cons.USER_ID, params[1]));
        postParam.add(new BasicNameValuePair(Cons.DATE, params[2]));
        postParam.add(new BasicNameValuePair(Cons.LINE, params[3]));
        postParam.add(new BasicNameValuePair(Cons.DIR, params[4]));
        postParam.add(new BasicNameValuePair(Cons.ON_STOP, params[5]));
        postParam.add(new BasicNameValuePair(Cons.OFF_STOP, params[6]));
        postParam.add(new BasicNameValuePair(Cons.ON_REVERSED, params[7]));
        postParam.add(new BasicNameValuePair(Cons.OFF_REVERSED, params[8]));

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



}
