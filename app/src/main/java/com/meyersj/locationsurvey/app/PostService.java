package com.meyersj.locationsurvey.app;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
    private static final String TYPE = "type";

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public void onCreate() {
		Log.d(TAG, "PostService onCreate() called");
	    super.onCreate();
	}

	@Override
	public void onStart(Intent intent, int startId) {

        Bundle extras = intent.getExtras();
        String[] params = null;
        if (extras != null) {
            String type = extras.getString(TYPE);
            if (type.equals("scan")) {
                params = getScanParams(extras);
            }
            else if (type.equals("pair")){
                params = getPairParams(extras);
            }
            if (params != null) {
                PostTask task = new PostTask();
                task.execute(params);
            }
        }
        else {
            Log.e(TAG, "extras are null");
        }
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

    protected String post(String[] params) {

        String retVal = null;
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(params[0]);

        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(DATA, params[1]));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);
            HttpEntity entityR = response.getEntity();
            Log.d(TAG, EntityUtils.toString(entityR));
            retVal = response.toString();

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


	protected String[] getScanParams(Bundle bundle) {
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
        params[1] = json.toJSONString();
		return params;
	}

    protected String[] getPairParams(Bundle bundle) {
        String[] params = new String[2];
        JSONObject json = new JSONObject();
        json.put(USER_ID, bundle.getString(USER_ID));
        json.put(DATE, bundle.getString(DATE));
        json.put(LINE, bundle.getString(LINE));
        json.put(DIR, bundle.getString(DIR));
        json.put(ON_STOP, bundle.getString(ON_STOP));
        json.put(OFF_STOP, bundle.getString(OFF_STOP));
        params[0] = bundle.getString(URL) + "/insertPair";
        params[1] = json.toJSONString();
        return params;
    }

}
