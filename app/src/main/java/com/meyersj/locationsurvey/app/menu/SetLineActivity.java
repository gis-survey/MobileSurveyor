package com.meyersj.locationsurvey.app.menu;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import com.meyersj.locationsurvey.app.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


import java.util.Properties;



public class SetLineActivity extends Activity {

    private final String TAG = "SetLineActivity";
    private final String SCANNER = "com.meyersj.locationsurvey.app.SCANNER";
    private final String ONOFFMAP = "com.meyersj.locationsurvey.app.ONOFFMAP";
    private String[] TRAINS = {"190", "193", "194", "200"};

    private static final String URL = "url";
    private static final String LINE = "rte";
    private static final String DIR = "dir";
    private static final String USER_ID = "user_id";
    private static final String OFF_MODE = "off_mode";

    private Properties prop;
    private Spinner line, dir;
    private String line_code;
    private String dir_code;
    private String user_id;
    private Boolean offMode = false;
    private String url;
    private Button record;
    private Button logout;
    private Switch modeSwitch;

    private Map<String, String> map = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        readIDs();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_line);

        line = (Spinner)findViewById(R.id.line_spinner);
        dir = (Spinner)findViewById(R.id.dir_spinner);
        line.setAdapter(ArrayAdapter.createFromResource(this, R.array.lines, R.layout.spinner));
        record = (Button) findViewById(R.id.record);
        logout = (Button) findViewById(R.id.logout);
        modeSwitch = (Switch) findViewById(R.id.offSwitch);


        getExtras();

        line.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                String selected_line = line.getItemAtPosition(pos).toString();
                Log.d(TAG, selected_line);
                String modified_line = stripSelection(selected_line);
                line_code = map.get(modified_line);

                Boolean ifTrain = false;
                for (String train: TRAINS) {
                    if (train.equals(line_code)) {
                        Log.d(TAG, "we have a train");
                        ifTrain = true;
                    }
                }

                if (ifTrain) {
                    modeSwitch.setVisibility(View.INVISIBLE);
                }
                else {
                    modeSwitch.setVisibility(View.VISIBLE);
                }

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
                intent.putExtra(USER_ID, user_id);
                intent.putExtra(OFF_MODE, offMode);
                Log.d(TAG, "user: " + user_id);
                intent.putExtra(LINE, line_code);
                intent.putExtra(DIR, dir_code);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(SetLineActivity.this);
                builder.setMessage("Are you sure you want to logout?")
                        //.setMessage(message)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //do nothing
                            }
                        });

                AlertDialog select = builder.create();
                select.show();
            }
        });
    }

    public void onSwitchClicked(View view) {

        Boolean on = ((Switch) view).isChecked();

        if (on) {
            Log.d(TAG, "switched on");
            offMode = true;
        }
        else {
            Log.d(TAG, "switched off");
            offMode = false;
        }
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
            //do nothing
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    protected String stripSelection(String in) {
        String newSelec = in.replaceAll("[^A-Za-z]", "");
        return newSelec;
    }


    protected void getExtras() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            if(extras.containsKey(URL)) {
                url = extras.getString(URL);
                Log.d(TAG, url);
            }
            if(extras.containsKey(USER_ID)) {
                user_id = extras.getString(USER_ID);
                Log.d(TAG, extras.getString(USER_ID));
            }
        }
    }

    //read Line IDs and route description from text file
    //used to build spinner for selecting route and direction
    protected void readIDs() {
        InputStream fileStream = getResources().openRawResource(R.raw.line_ids);

        try {
            //readIDs();
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileStream));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(" ");
                //Log.d(TAG, line);
                if(parts.length == 2) {
                    map.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            Log.d(TAG, "error opening lines_id.txt");
            e.printStackTrace();
        }
    }

}