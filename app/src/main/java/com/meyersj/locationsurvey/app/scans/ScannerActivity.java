package com.meyersj.locationsurvey.app.scans;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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
    private StopLookup stopLookup;
    private TextView stopText;
    private Date recentLoc;
    private Float THRESHOLD = Float.valueOf(1000 * 20);


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mContext = getApplicationContext();
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        Bundle params = getIntent().getExtras();

        Properties prop = Utils.getProperties(mContext, Cons.PROPERTIES);
        if( prop.containsKey(Cons.GPS_THRESHOLD)) {
            THRESHOLD = Float.valueOf(prop.getProperty(Cons.GPS_THRESHOLD));
        }

        setupStopTextLayout();

        //display on and off buttons only if 'off' mode is not selected
        if (params.containsKey(Cons.OFF_MODE) &&
                params.get(Cons.OFF_MODE).toString().equals("false") ){
            setupButtonsLayout();
            setupButtonListeners();
            params.putString(Cons.MODE, Cons.ON);
        }
        else {
            Log.d(TAG, "off mode is true");
            params.putString(Cons.MODE, Cons.OFF);
        }

        saveScans = new SaveScans(getApplicationContext(), params);
        stopLookup = new StopLookup(
                getApplicationContext(), stopText, "https://216.25.208.109:8493/api/stopLookup",
                params.getString(Cons.LINE), params.getString(Cons.DIR));

        setFormats();

        Log.d(TAG, params.getString(Cons.USER_ID));
        setContentView(mScannerView); // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();// Start camera on resume

        startService(new Intent(this, LocationService.class));
        if(Utils.timeDifference(recentLoc, new Date()) > THRESHOLD) {
            stopText.setText(Cons.NEAR_STOP + "no current near stop");
        }

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String lat = intent.getStringExtra("Latitude");
                String lon = intent.getStringExtra("Longitude");
                String date = intent.getStringExtra("Date");
                recentLoc = Utils.parseDate(date);
                Float accuracy = Float.valueOf(intent.getStringExtra("Accuracy"));

                Utils.shortToast(mContext, "GPS updated");
                saveScans.setLocation(lat, lon, accuracy, date);
                stopLookup.findStop(lat, lon);
                saveScans.flushBuffer();
            }
        };

        registerReceiver(receiver, new IntentFilter("com.example.LocationReceiver"));

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


    public void onStop() {
        super.onStart();
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

    private void setupStopTextLayout() {
        //LinearLayout layout = new LinearLayout(mContext, null, R.style.StopLayout);

        //LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
        //        android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
        //        android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        //ll.setMargins(3,3,3,3);

        stopText = new TextView(mContext, null);
        stopText.setGravity(Gravity.BOTTOM);
        stopText.setTextAppearance(mContext, R.style.SeqListHeader);
        stopText.setText(Cons.NEAR_STOP + "no current near stop");

        //layout.addView(stopText, ll);
        //layout.setBackground(getResources().getDrawable(R.drawable.shape_rect_grey_fade_round_none_nopress));
        //layout.setGravity(Gravity.BOTTOM);
        //mScannerView.addView(layout);

        mScannerView.addView(stopText);
    }

    private void setupButtonsLayout() {
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

    private void setupButtonListeners() {
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

    /*
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            //stopService(new Intent(this, LocationService.class));
            //unregisterReceiver(receiver);
        }
        return super.onKeyDown(keyCode, event);
    }
    */

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
