package com.meyersj.mobilesurveyor.app.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;


public class StartingActivity extends Activity {

    private final String TAG = "SetLineActivity";
    private final String SCANNER = "com.meyersj.mobilesurveyor.app.SCANNER";
    private final String ONOFFMAP = "com.meyersj.mobilesurveyor.app.ONOFFMAP";
    private static final int RESULT_SETTINGS = 1;

    private Context context;
    private Spinner line, dir;
    private String line_code;
    private String dir_code;
    private UserName user;
    private Boolean offMode = false;
    private Button record;
    private Switch modeSwitch;

    private Map<String, String> map = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        context = getApplicationContext();
        readIDs();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        line = (Spinner)findViewById(R.id.line_spinner);
        dir = (Spinner)findViewById(R.id.dir_spinner);
        line.setAdapter(ArrayAdapter.createFromResource(this, R.array.lines, R.layout.spinner));
        record = (Button) findViewById(R.id.record);
        modeSwitch = (Switch) findViewById(R.id.offSwitch);
        user = new UserName(this, R.id.username);

        line.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                String selected_line = line.getItemAtPosition(pos).toString();
                Log.d(TAG, selected_line);
                String modified_line = stripSelection(selected_line);
                line_code = map.get(modified_line);

                Boolean ifTrain = false;
                for (String train: Utils.getMapRoutes(context)) {
                    if (train.equals(line_code)) {
                        Log.d(TAG, "we have a train, launch map instead of scanner");
                        ifTrain = true;
                    }
                }

                if (ifTrain) {
                    modeSwitch.setVisibility(View.GONE);
                }
                else {
                    modeSwitch.setVisibility(View.VISIBLE);
                }

                Log.d(TAG, "line_code: " + line_code);
                int resId = StartingActivity.this.getResources().getIdentifier(modified_line, "array", StartingActivity.this.getPackageName());
                dir.setAdapter(ArrayAdapter.createFromResource(StartingActivity.this, resId, R.layout.spinner));

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

                intent.putExtra(Cons.USER_ID, user.getUser());
                intent.putExtra(Cons.OFF_MODE, offMode);
                Log.d(TAG, "user: " + user.getUser());
                intent.putExtra(Cons.LINE, line_code);
                intent.putExtra(Cons.DIR, dir_code);
                startActivity(intent);
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

        for(String x : Utils.getMapRoutes(context)){
            if(x.equals(line)) {
                ifTrain = true;
                break;
            }
        }
        return ifTrain;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                Intent i = new Intent(context, SettingsActivity.class);
                startActivityForResult(i, RESULT_SETTINGS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected String stripSelection(String in) {
        return in.replaceAll("[^A-Za-z]", "");
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
                Log.d(TAG, line);
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