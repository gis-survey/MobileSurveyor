package com.meyersj.locationsurvey.app.scans;

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
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.meyersj.locationsurvey.app.util.Cons;
import com.meyersj.locationsurvey.app.R;
import com.meyersj.locationsurvey.app.util.Utils;


public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private String TAG = "ScannerActivity";
    //Number of seconds before gps reading is too old
    private Float THRESHOLD = Float.valueOf(1000 * 20);

    private ZXingScannerView mScannerView;
    private LinearLayout btnLayout;
    private Button onBtn;
    private Button offBtn;
    private Context context;
    private BroadcastReceiver receiver;
    private SaveScans saveScans;
    private StopLookup stopLookup;
    private String mode;
    private TextView modeText;
    private TextView stopText;
    private TextView eolText;
    private Date recentLoc;


    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        context = getApplicationContext();
        mScannerView = new ZXingScannerView(this);
        Bundle params = getIntent().getExtras();

        Properties prop = Utils.getProperties(context, Cons.PROPERTIES);
        String url = Utils.getUrlApi(context) + "/stopLookup";
        Log.d(TAG, url);

        if( prop.containsKey(Cons.GPS_THRESHOLD)) {
            THRESHOLD = Float.valueOf(prop.getProperty(Cons.GPS_THRESHOLD));
        }


        saveScans = new SaveScans(getApplicationContext(), params);
        setupStopTextLayout();

        //display on and off buttons only if 'back of bus' mode is not selected
        if (params.containsKey(Cons.OFF_MODE) &&
                params.get(Cons.OFF_MODE).toString().equals("false") ){
            setupButtonsLayout();
            setupButtonListeners();
            saveScans.setMode(Cons.ON);
            params.putString(Cons.MODE, Cons.ON);
        }
        else {
            params.putString(Cons.MODE, Cons.OFF);
            saveScans.setMode(Cons.OFF);
        }

        stopLookup = new StopLookup(
                getApplicationContext(), stopText, eolText, url,
                params.getString(Cons.LINE), params.getString(Cons.DIR));

        setFormats();
        textDefault();

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

    @Override
    public void handleResult(Result rawResult) {
        // make a beep when scan is successful

        final ToneGenerator tg_on = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        //tg_on.startTone(ToneGenerator.TONE_PROP_BEEP);

        final ToneGenerator tg_off = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
        //tg_off.startTone(ToneGenerator.TONE_PROP_BEEP2);


        String message = "";
        if(saveScans.getMode().equals(Cons.ON)) {
            message = "ON - Scan successful";
            tg_on.startTone(ToneGenerator.TONE_PROP_BEEP2);
        }
        else if(saveScans.getMode().equals(Cons.OFF)) {
            message = "OFF - Scan successful";
            tg_off.startTone(ToneGenerator.TONE_PROP_BEEP);
        }

        Utils.shortToastUpper(getApplicationContext(), message);

        Log.d(TAG, rawResult.getText());
        Log.d(TAG, rawResult.getBarcodeFormat().toString());

        saveScans.save(rawResult);

        // pause before restarting camera to prevent multiple scans at once
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            public void run() {
                mScannerView.startCamera();
            }
        }, 500);
    }

    private void buildLayoutParams(TextView current, TextView above) {
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rl.setMargins(8,3,3,3);
        if(above != null) {
            rl.addRule(RelativeLayout.BELOW, above.getId());
        }
        current.setLayoutParams(rl);
    }

    private void setupStopTextLayout() {
        modeText = new TextView(context, null);
        modeText.setId(1);
        modeText.setTextAppearance(context, R.style.ModeText);
        //modeText.setText("Current mode: " + saveScans.getMode());

        stopText = new TextView(context, null);
        stopText.setId(2);
        stopText.setTextAppearance(context, R.style.StopsText);

        eolText = new TextView(context, null);
        eolText.setId(3);
        eolText.setTextAppearance(context, R.style.EOLText);
        //eolText.setText("");

        RelativeLayout upperLayout = new RelativeLayout(context);
        upperLayout.setGravity(Gravity.TOP);

        buildLayoutParams(modeText, null);
        buildLayoutParams(stopText, modeText);
        buildLayoutParams(eolText, stopText);

        /*
        RelativeLayout.LayoutParams rl = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rl.setMargins(3, 3, 3, 3);
        stopText.setLayoutParams(rl);
        modeText.setLayoutParams(rl);

        rl = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        rl.setMargins(3, 3, 3, 3);
        rl.addRule(RelativeLayout.BELOW, stopText.getId());
        eolText.setLayoutParams(rl);
        */


        upperLayout.addView(modeText);
        upperLayout.addView(stopText);
        upperLayout.addView(eolText);
        mScannerView.addView(upperLayout);
    }

    private void textDefault() {
        modeText.setText(Cons.CUR_MODE + saveScans.getMode().toUpperCase());
        stopText.setText(Cons.NEAR_STOP + "searching...");
        eolText.setText("");
    }

    // create two side by side buttons
    // 'ON' and 'OFF' modes
    private void setupButtonsLayout() {
        btnLayout = new LinearLayout(context);

        onBtn = new Button(context, null, R.style.ButtonText);
        offBtn = new Button(context, null, R.style.ButtonText);

        onBtn.setText("ON MODE");
        offBtn.setText("OFF");

        onBtn.setHeight(250);
        offBtn.setHeight(250);

        onBtn.setTextSize(20);
        offBtn.setTextSize(20);

        onBtn.setBackground(getResources().getDrawable(R.drawable.green_button));
        offBtn.setBackground(getResources().getDrawable(R.drawable.red_button));

        onBtn.setGravity(Gravity.CENTER);
        offBtn.setGravity(Gravity.CENTER);

        onBtn.setShadowLayer(2, 1, 1, Color.BLACK);
        offBtn.setShadowLayer(2, 1, 1, Color.BLACK);

        LinearLayout.LayoutParams ll = new LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT, 1.5f);
        ll.setMargins(3,3,3,3);

        btnLayout.setGravity(Gravity.BOTTOM);
        btnLayout.addView(onBtn, ll);
        btnLayout.addView(offBtn, ll);

        mScannerView.addView(btnLayout);
    }

    private void setupButtonListeners() {
        onBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBtn.setBackground(getResources().getDrawable(R.drawable.green_button));
                offBtn.setBackground(getResources().getDrawable(R.drawable.red_button));
                saveScans.setMode(Cons.ON);
                onBtn.setText("ON MODE");
                offBtn.setText("OFF");
                modeText.setText(Cons.CUR_MODE + saveScans.getMode().toUpperCase());
            }
        });

        offBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                offBtn.setBackground(getResources().getDrawable(R.drawable.green_button));
                onBtn.setBackground(getResources().getDrawable(R.drawable.red_button));
                saveScans.setMode(Cons.OFF);
                offBtn.setText("OFF MODE");
                onBtn.setText("ON");
                modeText.setText(Cons.CUR_MODE + saveScans.getMode().toUpperCase());
            }
        });
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
