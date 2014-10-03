package com.meyersj.locationsurvey.app.util;

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
            String type = extras.getString(Cons.TYPE);
            if (type.equals(Cons.SCAN)) {
                params = getScanParams(extras);
            }
            else if (type.equals(Cons.PAIR)){
                params = getPairParams(extras);
            }
            if (params != null) {
                PostTask task = new PostTask();
                task.execute(params);
            }
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

        String retVal;
        HttpClient client = new DefaultHttpClient();
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
        json.put(Cons.UUID, bundle.getString(Cons.UUID));
        json.put(Cons.DATE, bundle.getString(Cons.DATE));
        json.put(Cons.USER_ID, bundle.getString(Cons.USER_ID));
        json.put(Cons.LINE, bundle.getString(Cons.LINE));
        json.put(Cons.DIR, bundle.getString(Cons.DIR));
        json.put(Cons.MODE, bundle.getString(Cons.MODE));
        json.put(Cons.LON, bundle.getString(Cons.LON));
        json.put(Cons.LAT, bundle.getString(Cons.LAT));
        params[0] = bundle.getString(Cons.URL) + "/insertScan";
        params[1] = json.toJSONString();
		return params;
	}

    protected String[] getPairParams(Bundle bundle) {
        String[] params = new String[2];
        JSONObject json = new JSONObject();
        json.put(Cons.USER_ID, bundle.getString(Cons.USER_ID));
        json.put(Cons.DATE, bundle.getString(Cons.DATE));
        json.put(Cons.LINE, bundle.getString(Cons.LINE));
        json.put(Cons.DIR, bundle.getString(Cons.DIR));
        json.put(Cons.ON_STOP, bundle.getString(Cons.ON_STOP));
        json.put(Cons.OFF_STOP, bundle.getString(Cons.OFF_STOP));
        json.put(Cons.ON_REVERSED, bundle.getString(Cons.ON_REVERSED));
        json.put(Cons.OFF_REVERSED, bundle.getString(Cons.OFF_REVERSED));
        params[0] = bundle.getString(Cons.URL) + "/insertPair";
        params[1] = json.toJSONString();
        return params;
    }

}
