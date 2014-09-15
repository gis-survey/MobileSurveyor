package com.meyersj.locationsurvey.app.scans;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;


import java.util.ArrayList;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.meyersj.locationsurvey.app.util.Constants;
import com.meyersj.locationsurvey.app.R;
import com.meyersj.locationsurvey.app.util.Utils;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private String TAG = "ScannerActivity";

    private static final String MODE = "mode";
    private static final String URL = "url";
    private static final String LINE = "rte";
    private static final String DIR = "dir";
    private static final String UUID = "uuid";
    private static final String DATE = "date";
    private static final String ON = "on";
    private static final String OFF = "off";
    private static final String LAT = "lat";
    private static final String LON = "lon";
    private static final String TYPE = "type";
    private static final String USER_ID = "user_id";
    private static final String OFF_MODE = "off_mode";

    private ZXingScannerView mScannerView;
    private LinearLayout btnLayout;
    private Button onBtn;
    private Button offBtn;
    private Context mContext;
    //private Bundle params;



    BroadcastReceiver receiver;
    private SaveScans saveScans;
    //private String lat = "0";
    //private String lon = "0";

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mContext = getApplicationContext();
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        Bundle params = getIntent().getExtras();

        //display on and off buttons only if 'off' mode is not selected
        if (params.containsKey(OFF_MODE) &&
                params.get(OFF_MODE).toString().equals("false") ){
            setButtonsLayout();
            setButtonListeners();
            params.putString(MODE, ON);
        }
        else {
            Log.d(TAG, "off mode is true");
            params.putString(MODE, OFF);
        }

        saveScans = new SaveScans(getApplicationContext(), params);

        setFormats();

        Log.d(TAG, params.getString(USER_ID));
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();// Start camera on resume

        startService(new Intent(this, LocationService.class));


        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String lat = intent.getStringExtra("Latitude");
                String lon = intent.getStringExtra("Longitude");
                String date = intent.getStringExtra("Date");
                Float accuracy = Float.valueOf(intent.getStringExtra("Accuracy"));

                Log.d(TAG, "new location received: " + lat + "-" + lon);
                saveScans.setLocation(lat, lon, accuracy, date);
                saveScans.flushBuffer();

                //TODO flush buffer each time new location is recieved
                //saveScans.flushBuffer();


            }
        };
        registerReceiver(receiver, new IntentFilter("com.example.LocationReceiver"));

        //verifyGPS();
        if (!Utils.isNetworkAvailable(getApplicationContext())) {
            Utils.longToastCenter(getApplicationContext(), "Please enable network connections.");
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
        stopService(new Intent(this, LocationService.class));
        unregisterReceiver(receiver);
    }


    public void onStop() {
        super.onStart();
        Log.d(TAG, "scanner activity stopped");

    }

    @Override
    public void handleResult(Result rawResult) {

        final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        tg.startTone(ToneGenerator.TONE_PROP_BEEP2);

        Utils.shortToast(getApplicationContext(), "Scan successful");

        Log.d(TAG, rawResult.getText()); // Prints scan results
        Log.d(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        //Intent post = new Intent(getApplicationContext(), PostService.class);

        saveScans.save(rawResult);


        // pause before restarting camera to prevent multiple scans
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mScannerView.startCamera();
            }
        }, 500);

    }

    private void setButtonsLayout() {
        btnLayout = new LinearLayout(mContext);
        //btnLayout.setBackgroundColor(Color.BLACK);
        onBtn = new Button(mContext, null, R.style.ButtonText);
        offBtn = new Button(mContext, null, R.style.ButtonText);

        onBtn.setText("On");
        offBtn.setText("Off");

        onBtn.setTextSize(20);
        offBtn.setTextSize(20);

        onBtn.setBackground(getResources().getDrawable(R.drawable.red_button));
        offBtn.setBackground(getResources().getDrawable(R.drawable.grey_button));

        onBtn.setGravity(Gravity.CENTER);
        offBtn.setGravity(Gravity.CENTER);

        onBtn.setShadowLayer(2, 1, 1, Color.BLACK);
        offBtn.setShadowLayer(2, 1, 1, Color.BLACK);

        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f);

        ll.setMargins(3,3,3,3);

        btnLayout.addView(onBtn, ll);
        btnLayout.addView(offBtn, ll);

        mScannerView.addView(btnLayout);
    }

    private void setButtonListeners() {
        onBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtn.setBackground(getResources().getDrawable(R.drawable.red_button));
                offBtn.setBackground(getResources().getDrawable(R.drawable.grey_button));
                saveScans.setMode(Constants.ON);
                Log.d(TAG, ON);
            }
        });

        offBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                offBtn.setBackground(getResources().getDrawable(R.drawable.red_button));
                onBtn.setBackground(getResources().getDrawable(R.drawable.grey_button));
                saveScans.setMode(Constants.OFF);
                Log.d(TAG, OFF);
            }

        });
    }

    /*
    private Boolean checkParams(Bundle check) {

        Boolean retVal = false;
        if (check != null) {

            if (check.containsKey(URL) &&
                    check.containsKey(LINE) &&
                    check.containsKey(DIR) &&
                    check.containsKey(UUID) &&
                    check.containsKey(MODE) &&
                    check.containsKey(DATE) &&
                    check.containsKey(LAT) &&
                    check.containsKey(LON)) {

                if (check.getString(LAT).equals("0") ||
                        check.getString(LON).equals("0")) {
                    Log.d(TAG, "lat and lon have not been set");
                }
                else {
                    Log.d(TAG, "params are valid");
                    retVal = true;
                }

            }
            else {
                Log.d(TAG, "params do no contain correct extras");
            }
        }
        else {
            Log.d(TAG, "params are empty");
        }
        return retVal;
    }
    */

    private void setFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
    }

    /*
    private void saveScan(Result rawResult) {
        Date date = new Date();

        currentLoc.timeDifference(date);
        //TODO check time delta between date and currentLoc date

        postResults(rawResult, date);
    }
    */

    /*
    private void postResults(Result rawResult, Date date) {

        params.putString(UUID, rawResult.toString());
        params.putString(DATE, Utils.dateFormat.format(date));
        params.putString(LAT, lat);
        params.putString(LON, lon);

        if (checkParams(params)) {
            Log.d(TAG, "posting results");
            Log.d(TAG, params.getString(LINE));
            Log.d(TAG, params.getString(LAT));
            Log.d(TAG, params.getString(LON));
            Log.d(TAG, "MY URL: " + params.getString(URL));

            params.putString(TYPE, "scan");
            Intent post = new Intent(getApplicationContext(), PostService.class);
            post.putExtras(params);
            getApplicationContext().startService(post);
        }
        else {
            Log.e(TAG, "params are not valid");
        }
    }
    */

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            Log.d(TAG, "stopping location service by keydown");
            stopService(new Intent(this, LocationService.class));
        }
        return super.onKeyDown(keyCode, event);
    }

}
