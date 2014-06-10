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

import org.apache.http.Header;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

public class PostService extends Service {

    private static final String TAG = "PostService";

    //url params
    private static final String MODE = "mode";
    private static final String URL = "url";
    private static final String LINE = "rte";
    private static final String DIR = "dir";
    private static final String UUID = "uuid";
    private static final String DATE = "date";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String ON_STOP = "on_stop";
    private static final String OFF_STOP = "off_stop";
    private static final String TYPE= "type";

    //BroadcastReceiver receiver;

	//private String lat = "0";
	//private String lon = "0";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "PostService onCreate() called");
	    super.onCreate();

        /*
	    receiver = new BroadcastReceiver() {
	        @Override
	        public void onReceive(Context context, Intent intent) {
	    		Log.d(TAG, "new location received");
                lat = intent.getStringExtra("Latitude");
	    		lon = intent.getStringExtra("Longitude");
	        }
	    };
	    registerReceiver(receiver, new IntentFilter("com.example.LocationReceiver"));
	    */
	}


	@Override
	public void onStart(Intent intent, int startId) {

        Bundle extras = intent.getExtras();
        if (extras != null) {

            if (isNetworkAvailable()) {
                String type = extras.getString(TYPE);

                if (type.equals("scan")) {
                    //get params and do ScanPostTask
                    String[] params = getScanParams(extras);

                    Log.d(TAG, "params after getParams()");
                    for (String x: params) {
                        Log.d(TAG, x);
                    }

                    Log.d(TAG, "execute post task");
                    ScanPostTask task = new ScanPostTask();
                    task.execute(params);


                }
                else if (type.equals("pair")){
                    //get params and do PairPostTask
                    String[] params = getPairParams(extras);

                    Log.d(TAG, "params after getParams()");
                    for (String x: params) {
                        Log.d(TAG, x);
                    }

                    Log.d(TAG, "execute post task");
                    PairPostTask task = new PairPostTask();
                    task.execute(params);
                }


            }
            else {
                Log.d(TAG, "No network connection");
                Toast.makeText(getApplicationContext(),
                        "Network connection not available",
                        Toast.LENGTH_SHORT).show();
            }
        }
        else {
            Log.e(TAG, "extras are null");
        }

	}

    //@Override
    //public void onDestroy() {
        //super.onDestroy();
        //unregisterReceiver(receiver);
        //Toast.makeText(this, "onDestroy for LocationReceiver has been called", Toast.LENGTH_LONG).show();
    //}

	
	class ScanPostTask extends AsyncTask<String[], Void, String> {

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
			postParam.add(new BasicNameValuePair(UUID, passed[1]));
			postParam.add(new BasicNameValuePair(DATE, passed[2]));
			postParam.add(new BasicNameValuePair(LINE, passed[3]));
			postParam.add(new BasicNameValuePair(DIR, passed[4]));
			postParam.add(new BasicNameValuePair(MODE, passed[5]));
			postParam.add(new BasicNameValuePair(LON, passed[6]));
			postParam.add(new BasicNameValuePair(LAT, passed[7]));

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

                Header[] headersP = post.getAllHeaders();

                HttpEntity entityP = post.getEntity();
                Log.d(TAG, EntityUtils.toString(entityP));


                Log.d(TAG, "POST HEADERS");
                for (Header header: headersP) {
                    Log.d(TAG, "Name: " + header.getName());
                    Log.d(TAG, "Value: " + header.getValue());
                }
				response = client.execute(post);

                Header[] headersR = response.getAllHeaders();

                Log.d(TAG, "RESPONSE HEADERS");
                for (Header header: headersR) {
                    Log.d(TAG, "Name: " + header.getName());
                    Log.d(TAG, "Value: " + header.getValue());
                }

                HttpEntity entityR = response.getEntity();
                Log.d(TAG, EntityUtils.toString(entityR));

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
	
	} //End of ScanPostTask class definition
		
	public String[] getScanParams(Bundle bundle) {
		String[] params = new String[8];
		params[0] = bundle.getString(URL);
		params[1] = bundle.getString(UUID);
		params[2] = bundle.getString(DATE);
        params[3] = bundle.getString(LINE);
        params[4] = bundle.getString(DIR);
        params[5] = bundle.getString(MODE);
        params[6] = bundle.getString(LON);
        params[7] = bundle.getString(LAT);
		return params;	
	}


    class PairPostTask extends AsyncTask<String[], Void, String> {

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
            postParam.add(new BasicNameValuePair(DATE, passed[1]));
            postParam.add(new BasicNameValuePair(LINE, passed[2]));
            postParam.add(new BasicNameValuePair(DIR, passed[3]));
            postParam.add(new BasicNameValuePair(ON_STOP, passed[4]));
            postParam.add(new BasicNameValuePair(OFF_STOP, passed[5]));

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
                HttpEntity entity = response.getEntity();

                Log.d(TAG, EntityUtils.toString(entity));

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

    } //End of ScanPostTask class definition


    public String[] getPairParams(Bundle bundle) {
        String[] params = new String[6];
        params[0] = bundle.getString(URL);
        params[1] = bundle.getString(DATE);
        params[2] = bundle.getString(LINE);
        params[3] = bundle.getString(DIR);
        params[4] = bundle.getString(ON_STOP);
        params[5] = bundle.getString(OFF_STOP);
        return params;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}
