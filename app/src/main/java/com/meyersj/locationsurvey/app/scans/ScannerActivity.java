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
import com.meyersj.locationsurvey.app.util.Cons;
import com.meyersj.locationsurvey.app.R;
import com.meyersj.locationsurvey.app.util.Utils;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private String TAG = "ScannerActivity";

    private ZXingScannerView mScannerView;
    private LinearLayout btnLayout;
    private Button onBtn;
    private Button offBtn;
    private Context mContext;
    private BroadcastReceiver receiver;
    private SaveScans saveScans;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mContext = getApplicationContext();
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        Bundle params = getIntent().getExtras();

        //display on and off buttons only if 'off' mode is not selected
        if (params.containsKey(Cons.OFF_MODE) &&
                params.get(Cons.OFF_MODE).toString().equals("false") ){
            setButtonsLayout();
            setButtonListeners();
            params.putString(Cons.MODE, Cons.ON);
        }
        else {
            Log.d(TAG, "off mode is true");
            params.putString(Cons.MODE, Cons.OFF);
        }

        saveScans = new SaveScans(getApplicationContext(), params);

        setFormats();

        Log.d(TAG, params.getString(Cons.USER_ID));
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
                saveScans.setMode(Cons.ON);
            }
        });

        offBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                offBtn.setBackground(getResources().getDrawable(R.drawable.red_button));
                onBtn.setBackground(getResources().getDrawable(R.drawable.grey_button));
                saveScans.setMode(Cons.OFF);
            }

        });
    }

    private void setFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
    }

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
