/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.mobilesurveyor.app.scans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Endpoints;
import com.meyersj.mobilesurveyor.app.util.Utils;


public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private String TAG = "ScannerActivity";
    private static final String LOCATION_BROADCAST = "com.meyersj.mobilesurveyor.LocationReceiver";
    private Float THRESHOLD = Float.valueOf(1000 * 20); // seconds before gps is stale

    private ZXingScannerView mScannerView;

    private Context context;
    private BroadcastReceiver receiver;
    private Save save;
    private StopLookup stopLookup;
    private ModeSelector mode;
    private TextView stopText;
    private TextView eolText;
    private Date recentLoc;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        context = getApplicationContext();
        setContentView(R.layout.activity_scanner);
        setupTexts();
        mScannerView = (ZXingScannerView) findViewById(R.id.scanner);
        Bundle params = getIntent().getExtras();
        Properties prop = Utils.getProperties(context, Cons.PROPERTIES);
        String url = Utils.getUrlApi(context) + Endpoints.STOP_LOOKUP;
        Log.d(TAG, url);

        if( prop.containsKey(Cons.GPS_THRESHOLD)) {
            THRESHOLD = Float.valueOf(prop.getProperty(Cons.GPS_THRESHOLD));
        }

        Boolean isOffMode = (Boolean) params.get(Cons.OFF_MODE);
        save = new Save(getApplicationContext(), params);
        mode = new ModeSelector(this, R.id.on_mode_button, R.id.off_mode_button, isOffMode);
        stopLookup = new StopLookup(context, stopText, eolText, url,
                params.getString(Cons.LINE), params.getString(Cons.DIR));
        setFormats();
        Log.d(TAG, params.getString(Cons.USER_ID));
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();// Start camera on resume

        startService(new Intent(this, LocationService.class));
        if(Utils.timeDifference(recentLoc, new Date()) > THRESHOLD) {
            stopText.setText(Cons.NEAR_STOP + "searching...");
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String lat = intent.getStringExtra("Latitude");
                String lon = intent.getStringExtra("Longitude");
                String date = intent.getStringExtra("Date");
                recentLoc = Utils.parseDate(date);
                Float accuracy = Float.valueOf(intent.getStringExtra("Accuracy"));
                //Utils.shortToast(ScannerActivity.this.context, "GPS updated");
                save.setLocation(lat, lon, accuracy, date);
                stopLookup.findStop(lat, lon);
                save.flushBuffer();
            }
        };

        registerReceiver(receiver, new IntentFilter(LOCATION_BROADCAST));

        if(!Utils.isGPSEnabled(getApplicationContext())) {
            alertMessageNoGps();
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        stopService(new Intent(this, LocationService.class));
        unregisterReceiver(receiver);
    }

    @Override
    public void handleResult(Result rawResult) {
        String message = "";
        if(mode.getMode().equals(Cons.ON)) {
            message = "ON - Scan successful";
            final ToneGenerator tg_on = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            tg_on.startTone(ToneGenerator.TONE_PROP_BEEP2);
        }
        else if(mode.getMode().equals(Cons.OFF)) {
            message = "OFF - Scan successful";
            final ToneGenerator tg_off = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            tg_off.startTone(ToneGenerator.TONE_PROP_BEEP);
        }
        Utils.shortToastUpper(getApplicationContext(), message);
        Log.d(TAG, rawResult.getText());
        Log.d(TAG, rawResult.getBarcodeFormat().toString());
        save.save(rawResult, mode.getMode());
        // pause before restarting camera to prevent multiple scans at once
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mScannerView.startCamera();
            }
        }, 500);
    }

    private void setupTexts() {
        stopText = (TextView) findViewById(R.id.stop_text);
        eolText = (TextView) findViewById(R.id.eol_text);
        stopText.setText(Cons.NEAR_STOP + "searching...");
        eolText.setText("");
    }

    private void setFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
    }

    private void alertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("GPS is disabled. Please enable it before scanning.")
                .setCancelable(false)
                .setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });

        final AlertDialog alert = builder.create();
        alert.show();
    }

}
