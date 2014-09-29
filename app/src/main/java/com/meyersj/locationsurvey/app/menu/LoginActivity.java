package com.meyersj.locationsurvey.app.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.mapbox.mapboxsdk.geometry.LatLng;
import com.meyersj.locationsurvey.app.R;
import com.meyersj.locationsurvey.app.util.Cons;
import com.meyersj.locationsurvey.app.util.Utils;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Created by meyersj on 6/12/2014.
 */
public class LoginActivity extends Activity {

    private static final String TAG = "LoginActivity";
    private final String SETLINE = "com.meyersj.locationsurvey.app.SETLINE";

    private EditText username;
    private EditText password;
    private Button login, skip_login;
    private Properties prop;
    String url;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        skip_login = (Button) findViewById(R.id.skip_login);

        prop = Utils.getProperties(getApplicationContext(), Cons.PROPERTIES);
        url = prop.getProperty(Cons.BASE_URL);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = username.getText().toString();
                String pass = password.getText().toString();

                JSONObject json = new JSONObject();
                json.put(Cons.USER_NAME, name);
                json.put(Cons.PASSWORD, pass);

                String credentials = json.toJSONString();

                String[] params = new String[2];
                params[0] = url + "/verifyUser";
                params[1] = credentials;

                //close keypad
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                //verify login credentials
                //start SetLineActivity if credentials are valid
                VerifyLoginTask task = new VerifyLoginTask();
                task.execute(params);

            }
        });

        skip_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SETLINE);
                intent.putExtra(Cons.URL, url);
                intent.putExtra(Cons.USER_ID, "testuser");
                startActivity(intent);
            }
        });

    }

    class VerifyLoginTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {
            String[] params = inParams[0];
            Log.d(TAG, "url:" + params[0]);
            Log.d(TAG, "data:" + params[1]);
            return post(params);
        }
        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                validateResponse(response);
            }
        }
    }

    protected String post(String[] params) {

        String responseString = null;
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(params[0]);

        ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
        postParam.add(new BasicNameValuePair(Cons.CRED, params[1]));

        try {
            post.setEntity(new UrlEncodedFormEntity(postParam));
            HttpResponse response = client.execute(post);
            HttpEntity entityR = response.getEntity();
            responseString = EntityUtils.toString(entityR);
            Log.d(TAG, responseString);

        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "UnsupportedEncodingException");
            Log.e(TAG, e.toString());
        } catch (ClientProtocolException e) {
            Log.e(TAG, "ClientProtocolException: " + e.toString());
            Log.e(TAG, e.toString());
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.toString());
            Log.e(TAG, e.toString());
        }
        return responseString;
    }

    private void validateResponse(String jsonInput) {

        JSONParser parser = new JSONParser();

        try{
            Object obj = parser.parse(jsonInput);
            JSONObject results = (JSONObject) obj;

            String user_match = results.get(Cons.USER_MATCH).toString();
            String password_match = results.get(Cons.PASS_MATCH).toString();
            String user_id = results.get(Cons.USER_ID).toString();

            if (user_match.equals("false")) {
                Log.d(TAG, "username did not match");
                Utils.shortToastCenter(getApplicationContext(),
                        "No record of that user, please re-enter username.");
            }
            else if (password_match.equals("false")) {
                Log.d(TAG, "password not correct");
                Utils.shortToastCenter(getApplicationContext(),
                        "Incorrect password, please re-enter.");
                password.setText("");
            }

            //user and password match
            //move user to SetLineActivity
            else {
                Intent intent = new Intent(SETLINE);
                intent.putExtra(Cons.URL, url);
                intent.putExtra(Cons.USER_ID, user_id);
                password.setText("");
                startActivity(intent);
            }

        } catch(ParseException pe){
            Log.e(TAG, pe.toString());
        }
    }


}
