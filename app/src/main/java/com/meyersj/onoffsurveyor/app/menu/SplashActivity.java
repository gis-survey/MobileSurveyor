/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */

package com.meyersj.onoffsurveyor.app.menu;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.meyersj.onoffsurveyor.app.R;
import com.meyersj.onoffsurveyor.app.long_survey.SurveyActivity;
import com.meyersj.onoffsurveyor.app.util.Cons;
import com.meyersj.onoffsurveyor.app.util.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import butterknife.Bind;
import butterknife.ButterKnife;


public class SplashActivity extends Activity {

    private final String TAG = "SetLineActivity";
    private final String SCANNER = "com.meyersj.mobilesurveyor.app.SCANNER";
    private final String ONOFFMAP = "com.meyersj.mobilesurveyor.app.ONOFFMAP";
    private static final int RESULT_SETTINGS = 1;

    private Context context;

    @Bind(R.id.username) TextView userText;
    @Bind(R.id.route_spinner) Spinner routeSpinner;
    @Bind(R.id.direction_spinner) Spinner directionSpinner;
    @Bind(R.id.start_collection) Button startButton;
    @Bind(R.id.radio_mode_group) LinearLayout radioGroup;

    private String line_code;
    private String dir_code;
    private Boolean offMode = false;
    private Map<String, String> map = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starting);
        ButterKnife.bind(this);

        context = getApplicationContext();
        loadPreferences(context);
        readIDs();

        final int listStyle = android.R.layout.simple_dropdown_item_1line;

        routeSpinner.setAdapter(ArrayAdapter.createFromResource(this, R.array.lines, listStyle));
        routeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                String selected_line = routeSpinner.getItemAtPosition(pos).toString();
                Log.d(TAG, selected_line);
                String modified_line = stripSelection(selected_line);
                line_code = map.get(modified_line);

                Boolean ifTrain = false;
                for (String train : Utils.getMapRoutes(context)) {
                    if (train.equals(line_code)) {
                        ifTrain = true;
                        radioGroup.setVisibility(View.GONE);
                    }
                }

                if (!ifTrain) {
                    radioGroup.setVisibility(View.VISIBLE);
                }

                int resId = context.getResources().getIdentifier(modified_line, "array", context.getPackageName());
                directionSpinner.setAdapter(ArrayAdapter.createFromResource(SplashActivity.this, resId, listStyle));

            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        directionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                String selected_dir = directionSpinner.getItemAtPosition(pos).toString();

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

        startButton.setOnClickListener(new Button.OnClickListener() {
            @Override

            public void onClick(View v) {
                Intent intent;

                if (ifTrain(line_code)) {
                    // start map
                    intent = new Intent(ONOFFMAP);
                } else {
                    // start scanner
                    intent = new Intent(SCANNER);
                }

                intent.putExtra(Cons.USER_ID, userText.getText().toString());
                intent.putExtra(Cons.OFF_MODE, offMode);
                intent.putExtra(Cons.LINE, line_code);
                intent.putExtra(Cons.DIR, dir_code);
                startActivity(intent);
            }
        });
    }

    public void onScanModeClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radio_mode_front:
                if (checked)
                    offMode = false;
                    break;
            case R.id.radio_mode_back:
                if (checked)
                    offMode = true;
                    break;
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
        // add settings to action bar
        getMenuInflater().inflate(R.menu.settings_action, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch(item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(context, SettingsActivity.class);
                startActivityForResult(intent, RESULT_SETTINGS);
                return true;
            case R.id.survey_activity:
                intent = new Intent(context, SurveyActivity.class);
                startActivityForResult(intent, RESULT_SETTINGS);
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

    private void loadPreferences(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        //this should only execute after program was installed for first time
        //grab default urls from properties and update sharedprefs with those
        if(!sharedPref.contains(Cons.SET_PREFS)) {
            Properties prop = Utils.getProperties(context, Cons.PROPERTIES);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean(Cons.SET_PREFS, true);
            editor.putString(Cons.BASE_URL, prop.getProperty(Cons.BASE_URL));
            editor.putString(Cons.MAP_RTES, prop.getProperty(Cons.MAP_RTES));
            editor.commit();
        }
    }


}