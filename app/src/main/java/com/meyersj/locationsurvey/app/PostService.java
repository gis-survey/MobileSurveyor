package com.meyersj.locationsurvey.app;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class PostService extends Service {

    private static final String MODE = "MODE";
    private static final String URL = "URL";
    private static final String LINE = "LINE";
    private static final String DIR = "DIR";
    private static final String UUID = "UUID";
    private static final String DATE = "DATE";
	private static final String TAG = "PostService";

	private String lat = "0";
	private String lon = "0";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "PostService onCreate() called");
	    super.onCreate();
		
	    BroadcastReceiver receiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	    		Log.d(TAG, "new location received");
                lat = intent.getStringExtra("Latitude");
	    		lon = intent.getStringExtra("Longitude");
	        }
	    };
	    registerReceiver(receiver, new IntentFilter("com.example.LocationReceiver"));
	}

	@Override
	public void onStart(Intent intent, int startId) {

		//create PostTask object and execute with url and parameters
		//TODO exception handling for AsyncTask
		String[] params = getParams(intent.getExtras());
        Log.d(TAG, params[1]);

        if (isNetworkAvailable()) {

            PostTask task = new PostTask();
            task.execute(params);
        }
        else {
            Log.d(TAG, "No network connection");
            Toast.makeText(getApplicationContext(),
                    "Network connection not available",
                    Toast.LENGTH_SHORT).show();
        }
	}
	
	class PostTask extends AsyncTask<String[], Void, String> {

        String retVal = null;

		@Override
		protected String doInBackground(String[]... inParams) {
			String[] passed = inParams[0];
			Log.i(TAG, "Base url: " + passed[0]);
			
			//Create HttpPost object with base url
			HttpClient client = new DefaultHttpClient();
			HttpPost post = new HttpPost(passed[0]);

            //TODO change keys to uppercase in flask api app and change keys to constancs
			//Build parameters
			ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
			postParam.add(new BasicNameValuePair("uuid", passed[1]));
			postParam.add(new BasicNameValuePair("date", passed[2]));
			postParam.add(new BasicNameValuePair("line", passed[3]));
			postParam.add(new BasicNameValuePair("dir", passed[4]));
			postParam.add(new BasicNameValuePair("mode", passed[5]));
			postParam.add(new BasicNameValuePair("lon", passed[6]));
			postParam.add(new BasicNameValuePair("lat", passed[7]));

			//Encode parameters with base URL
			try {
				post.setEntity(new UrlEncodedFormEntity(postParam));
			} catch (UnsupportedEncodingException e1) {
				Log.i(TAG, "UnsupportedEncodingException");
			}
			
			Log.d(TAG, post.toString());

        	//Execute response
			HttpResponse response = null;

			try {
				response = client.execute(post);
                retVal = response.toString();
			} catch (ClientProtocolException e) {
				Log.d(TAG, "ClientProtocolException: " + e.toString());
			    retVal = "ClientProtocolException";
			} catch (IOException e) {
                Log.d(TAG, "IOException: " + e.toString());
                retVal = "IOException";
			}
		    return retVal;
		}
	
		// call setData to update TextView content
		@Override
		protected void onPostExecute(String response) {
			Log.i(TAG, "onPostExecute(): " + response);
		}
	
	} //End of PostTask class definition
		
	public String[] getParams(Bundle bundle) {
		String[] params = new String[8];
		params[0] = bundle.getString(URL);
		params[1] = bundle.getString(UUID);
		params[2] = bundle.getString(DATE);
		params[3] = bundle.getString(LINE);
		params[4] = bundle.getString(DIR);
		params[5] = bundle.getString(MODE);
		params[6] = lon;
		params[7] = lat;
		return params;	
	}

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
