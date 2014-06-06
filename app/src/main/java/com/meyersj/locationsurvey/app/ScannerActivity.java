package com.meyersj.locationsurvey.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;



import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class ScannerActivity extends Activity implements ZXingScannerView.ResultHandler {

    private String TAG = "ScannerActivity";

    private static final String MODE = "MODE";
    private static final String ON = "ON";
    private static final String OFF = "OFF";
    private static final String URL = "URL";
    private static final String LINE = "LINE";
    private static final String DIR = "DIR";
    private static final String UUID = "UUID";
    private static final String DATE = "DATE";

    private ZXingScannerView mScannerView;
    private LinearLayout btnLayout;
    private Button onBtn;
    private Button offBtn;
    private static Context mContext;
    private Bundle params;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        mContext = getApplicationContext();
        mScannerView = new ZXingScannerView(this);   // Programmatically initialize the scanner view
        setButtonsLayout();
        setButtonListeners();
        setFormats();
        params = getIntent().getExtras();
        params.putString(MODE, ON);
        setContentView(mScannerView);                // Set the scanner view as the content view
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "start location service");
        startService(new Intent(this, LocationService.class));
    }

    public void onStop() {
        super.onStart();
        Log.d(TAG, "scanner activity stopped");

    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here

        Toast.makeText(getApplicationContext(),
                "Scan successful, ID: " + rawResult.getText(),
                Toast.LENGTH_SHORT).show();

        Log.d(TAG, rawResult.getText()); // Prints scan results
        Log.d(TAG, rawResult.getBarcodeFormat().toString()); // Prints the scan format (qrcode, pdf417 etc.)

        //Intent post = new Intent(getApplicationContext(), PostService.class);
        //post.putExtras(extras);
        //context.startService(post);

        postResults(rawResult);
        mScannerView.startCamera();
    }

    private void setButtonsLayout() {
        btnLayout = new LinearLayout(mContext);
        //btnLayout.setBackgroundColor(Color.BLACK);
        onBtn = new Button(mContext);
        offBtn = new Button(mContext);

        onBtn.setText("On");
        offBtn.setText("Off");

        onBtn.setTextColor(Color.parseColor("#ffffffff"));
        offBtn.setTextColor(Color.parseColor("#ffffffff"));

        onBtn.setTextSize(20);
        offBtn.setTextSize(20);

        onBtn.setBackground(getResources().getDrawable(R.drawable.red_button));
        offBtn.setBackground(getResources().getDrawable(R.drawable.black_button));

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
                offBtn.setBackground(getResources().getDrawable(R.drawable.black_button));
                params.putString(MODE, ON);
                Log.d(TAG, ON);
            }
        });

        offBtn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                offBtn.setBackground(getResources().getDrawable(R.drawable.red_button));
                onBtn.setBackground(getResources().getDrawable(R.drawable.black_button));
                params.putString(MODE, OFF);
                Log.d(TAG, OFF);
            }

        });
    }

    private Boolean checkParams(Bundle check) {

        Boolean retVal = false;
        if (check != null) {

            if (check.containsKey(URL) &&
                    check.containsKey(LINE) &&
                    check.containsKey(DIR) &&
                    check.containsKey(UUID) &&
                    check.containsKey(MODE) &&
                    check.containsKey(DATE)) {
                retVal = true;
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

    private void setFormats() {
        List<BarcodeFormat> formats = new ArrayList<BarcodeFormat>();
        formats.add(BarcodeFormat.QR_CODE);
        mScannerView.setFormats(formats);
    }

    private void postResults(Result rawResult) {
        Date date = new Date();
        params.putString(UUID, rawResult.toString());
        params.putString(DATE, dateFormat.format(date).toString());

        if (checkParams(params)) {
            //Intent broadcast = new Intent("com.example.BroadcastScanReceiver");
            //broadcast.putExtras(params);
            Log.d(TAG, "posting results");

            Intent post = new Intent(getApplicationContext(), PostService.class);
            post.putExtras(params);
            getApplicationContext().startService(post);

            //sendBroadcast(broadcast);
        }
        else {
            Log.e(TAG, "params are not valid");
        }

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            //finish();
            Log.d(TAG, "stopping location service by keydown");
            stopService(new Intent(this, LocationService.class));
            //Intent locationServiceIntent = new Intent(this, LocationService.class);
            //stopService(new Intent(this, LocationService.class));


        }
        return super.onKeyDown(keyCode, event);
    }

}
