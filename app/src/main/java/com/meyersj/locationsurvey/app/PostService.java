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
import org.json.simple.JSONObject;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

public class PostService extends Service {

    private static final String TAG = "PostService";

    //url params
    private static final String USER_ID = "user_id";
    private static final String DATA = "data";
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

    private Crypter mCrypter;


	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "PostService onCreate() called");
	    super.onCreate();

        try {
            mCrypter = new Crypter(new AndroidKeyczarReader(getResources(), "keys"));
        } catch (KeyczarException e) {
            //mPlaintext.setText(R.string.problem);
            Log.d(TAG, "Couldn't load keyczar keys", e);
        }

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

			//Build parameters
			ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
			postParam.add(new BasicNameValuePair(DATA, passed[1]));

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
		String[] params = new String[2];
        JSONObject json = new JSONObject();
        json.put(UUID, bundle.getString(UUID));
        json.put(DATE, bundle.getString(DATE));
        json.put(USER_ID, bundle.getString(USER_ID));
        json.put(LINE, bundle.getString(LINE));
        json.put(DIR, bundle.getString(DIR));
        json.put(MODE, bundle.getString(MODE));
        json.put(LON, bundle.getString(LON));
        json.put(LAT, bundle.getString(LAT));
        params[0] = bundle.getString(URL) + "/insertScan";
		params[1] = encryptMessage(json.toJSONString());
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
            postParam.add(new BasicNameValuePair(DATA, passed[1]));

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
        String[] params = new String[2];
        JSONObject json = new JSONObject();
        json.put(USER_ID, bundle.getString(USER_ID));
        Log.d(TAG, "user: " + bundle.getString(USER_ID));
        json.put(DATE, bundle.getString(DATE));
        json.put(LINE, bundle.getString(LINE));
        json.put(DIR, bundle.getString(DIR));
        json.put(ON_STOP, bundle.getString(ON_STOP));
        json.put(OFF_STOP, bundle.getString(OFF_STOP));
        params[0] = bundle.getString(URL) + "/insertPair";
        params[1] = encryptMessage(json.toJSONString());
        return params;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    protected String encryptMessage(String message) {
        String ciphertext = null;
        try {
            ciphertext = mCrypter.encrypt(message);
        } catch (KeyczarException e) {
            Log.d(TAG, "Couldn't encrypt message", e);
        }

        return ciphertext;
    }

    protected String decryptMessage(String ciphertext) {
        String decrypt = null;
        try {
            decrypt = mCrypter.decrypt(ciphertext);


        } catch (KeyczarException e) {
            Log.d(TAG, "Couldn't decrypt message", e);
        }
        return decrypt;
    }

}
