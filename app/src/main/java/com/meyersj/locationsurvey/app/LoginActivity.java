package com.meyersj.locationsurvey.app;

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

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.keyczar.Crypter;
import org.keyczar.exceptions.KeyczarException;

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
    private static final String BASE_URL = "base_url";
    private static final String URL = "url";
    private static final String USER_NAME = "username";
    private static final String PASSWORD = "password";
    private static final String USER_ID = "user_id";
    private static final String USER_MATCH = "user_match";
    private static final String PASS_MATCH = "password_match";
    private static final String CRED = "credentials";

    private Crypter mCrypter;
    private EditText username;
    private EditText password;
    private Button login, skip_login;
    private String loginEncrypt;
    private Properties prop;
    String url;



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        username = (EditText) findViewById(R.id.username);
        password = (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login);
        skip_login = (Button) findViewById(R.id.skip_login);

        prop = getProperties();
        url = prop.getProperty(BASE_URL);

        login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String name = username.getText().toString();
                String pass = password.getText().toString();

                JSONObject json = new JSONObject();
                json.put(USER_NAME, name);
                json.put(PASSWORD, pass);

                String credentials = json.toJSONString();
                loginEncrypt = encryptMessage(credentials);

                Log.d(TAG, "login Credentials: " + credentials);
                Log.d(TAG, "login Credentials Encrypted: " + loginEncrypt);

                String[] params = new String[2];
                params[0] = url + "/verifyUser";
                params[1] = loginEncrypt;

                //close keypad
                InputMethodManager inputManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

                inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                //verify login credentials
                VerifyLoginTask task = new VerifyLoginTask();
                task.execute(params);

            }
        });

        skip_login.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(SETLINE);
                intent.putExtra(URL, url);
                intent.putExtra(USER_ID, "1");
                startActivity(intent);
            }
        });





        try {
            mCrypter = new Crypter(new AndroidKeyczarReader(getResources(), "keys"));
        } catch (KeyczarException e) {
            //mPlaintext.setText(R.string.problem);
            Log.d(TAG, "Couldn't load keyczar keys", e);
        }

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
        String decrypt;
        try {
            decrypt = mCrypter.decrypt(ciphertext);

        } catch (KeyczarException e) {
            Log.d(TAG, "Couldn't decrypt message", e);
            decrypt = ciphertext;
        }
        return decrypt;
    }


    class VerifyLoginTask extends AsyncTask<String[], Void, String> {

        @Override
        protected String doInBackground(String[]... inParams) {

            String[] passed = inParams[0];
            Log.i(TAG, "Base url: " + passed[0]);
            String responseString = null;
            String decryptResponseString = null;

            //Create HttpPost object with base url
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(passed[0]);

            //Build parameters
            ArrayList<NameValuePair> postParam = new ArrayList<NameValuePair>();
            postParam.add(new BasicNameValuePair(CRED, passed[1]));

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
                StatusLine statusLine = response.getStatusLine();
                Log.d(TAG, statusLine.toString());
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    out.close();
                    responseString = out.toString();
                    decryptResponseString = decryptMessage(responseString);

                    Log.d(TAG, "response: " + responseString);
                    Log.d(TAG, "decrypted response: " + decryptResponseString);
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }

            } catch (ClientProtocolException e) {
                Log.d(TAG, "ClientProtocolException: " + e.toString());
            } catch (IOException e) {
                Log.d(TAG, "IOException: " + e.toString());
            }
            return decryptResponseString;
        }

        // call setData to update TextView content
        @Override
        protected void onPostExecute(String response) {
            if (response != null) {
                validateResponse(response);
            }
        }

    } //End of ScanPostTask class definition

    protected Properties getProperties() {
        Properties properties = null;

        try {
            InputStream inputStream = this.getResources().getAssets().open("config.properties");
            properties = new Properties();
            properties.load(inputStream);
            Log.d(TAG, "properties are now loaded");
            //System.out.println("properties: " + properties);
        } catch (IOException e) {
            Log.e(TAG, "properties failed to load, " + e);
        }
        return properties;
    }

    private void validateResponse(String jsonInput) {

        JSONParser parser=new JSONParser();

        try{
            Object obj = parser.parse(jsonInput);
            JSONObject results = (JSONObject) obj;
            Log.d(TAG, results.toString());


            String user_match = results.get(USER_MATCH).toString();
            String password_match = results.get(PASS_MATCH).toString();
            String user_id = results.get(USER_ID).toString();

            Log.d(TAG, "user_match: " + user_match);
            Log.d(TAG, "password_match: " + password_match);
            Log.d(TAG, "user_id: " + user_id);

            if (user_match.equals("false")) {
                Log.d(TAG, "username did not match");
                Toast toast = Toast.makeText(this, (String) "No record of that user, please re-enter.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
            else if (password_match.equals("false")) {
                Log.d(TAG, "password not correct");
                Toast toast = Toast.makeText(this, (String) "Incorrect password, please re-enter.", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }

            //user and password match
            //move user to SetLineActivity
            else {
                Intent intent = new Intent(SETLINE);
                intent.putExtra(URL, url);
                intent.putExtra(USER_ID, user_id);
                startActivity(intent);
            }

        }catch(ParseException pe){
            Log.e(TAG, pe.toString());
        }
    }


}
