package com.meyersj.locationsurvey.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class SetLineActivity extends Activity {

    private final String TAG = "SetLineActivity";
    private final String SCANNER = "com.meyersj.locationsurvey.app.SCANNER";
    private final String ONOFFMAP = "com.meyersj.locationsurvey.app.ONOFFMAP";
    private final String[] TRAINS = {"190", "193", "194", "200"};
    private final String URL = "URL";
    private final String LINE = "LINE";
    private final String DIR = "DIR";
    private final String url = "http://54.244.253.136/submit";

    Spinner line, dir;
    String line_code;
    String dir_code;
    Button record;
    Map<String, String> map = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            readIDs();
        } catch (IOException e) {
            Log.d(TAG, "error opening lines_id.txt");
            e.printStackTrace();
        }
        Log.i(TAG, "onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_line);

        line = (Spinner)findViewById(R.id.line_spinner);
        dir = (Spinner)findViewById(R.id.dir_spinner);
        line.setAdapter(ArrayAdapter.createFromResource(this, R.array.lines, R.layout.spinner));
        record = (Button) findViewById(R.id.record);

        line.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                String selected_line = line.getItemAtPosition(pos).toString();
                Log.d(TAG, selected_line);
                String modified_line = stripSelection(selected_line);
                line_code = map.get(modified_line);
                Log.d(TAG, "line_code: " + line_code);
                int resId = SetLineActivity.this.getResources().getIdentifier(modified_line, "array", SetLineActivity.this.getPackageName());
                dir.setAdapter(ArrayAdapter.createFromResource(SetLineActivity.this, resId, R.layout.spinner));

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        dir.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                String selected_dir = dir.getItemAtPosition(pos).toString();

                Log.d(TAG, selected_dir);
                String modified_dir = stripSelection(selected_dir);
                dir_code = map.get(modified_dir + line_code);
                Log.d(TAG, "dir_code: " + dir_code);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        record.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {
                Intent intent;

                Log.d(TAG, "line_code for intent:" + line_code + ":end");

                if (ifTrain(line_code)) {
                    intent = new Intent(ONOFFMAP);
                    Log.d(TAG, "start map for selection");
                }
                else {
                    intent = new Intent(SCANNER);
                    Log.d(TAG, "start barcode scanner");
                }

                intent.putExtra(URL, url);
                intent.putExtra(LINE, line_code);
                intent.putExtra(DIR, dir_code);
                startActivity(intent);
            }
        });

    }

    protected Boolean ifTrain(String line) {
        Boolean ifTrain = false;

        for(String x : TRAINS ){
            if(x.equals(line)) {
                ifTrain = true;
                break;
            }
        }
        return ifTrain;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            //finish();
            Log.d(TAG, "stopping location service by keydown");
            //Intent locationServiceIntent = new Intent(this, LocationService.class);
            //this.stopService(locationServiceIntent);
        }
        return super.onKeyDown(keyCode, event);
    }

    protected String stripSelection(String in) {
        String newSelec = in.replaceAll("[^A-Za-z]", "");
        Log.d(TAG, "stripped: " + newSelec);
        return newSelec;
    }


    //read Line IDs and route description from text file
    //used to build spinner for selecting route and direction
    protected void readIDs() throws IOException {
        InputStream fileStream = getResources().openRawResource(R.raw.line_ids);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(" ");
            Log.d(TAG, line);
            if(parts.length == 2) {
                Log.d(TAG, parts[0]);
                Log.d(TAG, parts[1]);
                map.put(parts[0], parts[1]);
            }
        }
    }

}