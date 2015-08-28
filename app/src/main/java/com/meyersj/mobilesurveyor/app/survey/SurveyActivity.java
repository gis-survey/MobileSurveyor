package com.meyersj.mobilesurveyor.app.survey;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.SelectedStops;
import com.meyersj.mobilesurveyor.app.survey.Location.PickLocationFragment;
import com.meyersj.mobilesurveyor.app.survey.OnOff.StopFragment;
import com.meyersj.mobilesurveyor.app.survey.Transfer.TransfersMapFragment;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

public class SurveyActivity extends FragmentActivity implements ActionBar.TabListener {

    private final String TAG = getClass().getCanonicalName();
    protected final String ODK_ACTION = "com.meyersj.mobilesurveyor.app.ODK_SURVEY";
    protected static final String[] HEADERS = {"Routes", "Start", "On", "Off", "End"};

    protected AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    protected ViewPager mViewPager;
    protected Button previousBtn;
    protected Button nextBtn;
    protected static SurveyManager manager;
    protected static Fragment[] fragments;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);
        previousBtn = (Button) this.findViewById(R.id.previous_fragment);
        nextBtn = (Button) this.findViewById(R.id.next_fragment);
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        final ActionBar actionBar = getActionBar();
        Bundle extras = getODKExtras();
        String line = extras != null ? extras.getString(Cons.LINE, Cons.DEFAULT_RTE) : Cons.DEFAULT_RTE;
        String dir = extras != null ? extras.getString(Cons.DIR, Cons.DEFAULT_DIR) : Cons.DEFAULT_DIR;
        manager = new SurveyManager(getApplicationContext(), this, line, dir, extras);
        SelectedStops selectedStops = new SelectedStops(this);

        actionBar.setHomeButtonEnabled(false);
        actionBar.setTitle("TransitSurveyor");
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPager = (ViewPager) findViewById(R.id.survey_pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(HEADERS.length);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                MapFragment fragment = (MapFragment) fragments[position];
                fragment.updateView(manager);
                toggleNavButtons(mViewPager.getCurrentItem());
            }
        });

        fragments = new Fragment[HEADERS.length];
        fragments[0] = new TransfersMapFragment();
        ((TransfersMapFragment) fragments[0]).initialize(manager, mViewPager, extras);
        fragments[1] = new PickLocationFragment();
        ((PickLocationFragment) fragments[1]).initialize(manager, "origin", extras);
        fragments[2] = new StopFragment();
        ((StopFragment) fragments[2]).initialize(manager, extras, Cons.BOARD, selectedStops);
        fragments[3] = new StopFragment();
        ((StopFragment) fragments[3]).initialize(manager, extras, Cons.ALIGHT, selectedStops);
        fragments[4] = new PickLocationFragment();
        ((PickLocationFragment) fragments[4]).initialize(manager, "destination", extras);
        //fragments[5] = new ConfirmFragment();
        //((ConfirmFragment) fragments[5]).setParams(this, manager, mViewPager);

        for (int i = 0; i < HEADERS.length; i++) {
            actionBar.addTab(actionBar.newTab().setText(HEADERS[i]).setTabListener(this));
        }
        toggleNavButtons(mViewPager.getCurrentItem());
        previousBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
                toggleNavButtons(mViewPager.getCurrentItem());
            }
        });
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
                toggleNavButtons(mViewPager.getCurrentItem());
                if(mViewPager.getCurrentItem() != HEADERS.length - 1) return;
                validateSubmit();
            }

        });
    }

    protected void exitWithSurveyBundle(Boolean valid) {
        int result  = this.RESULT_CANCELED;
        Intent intent = new Intent();
        if (valid) {
            intent = manager.addExtras(intent);
            result = RESULT_OK;
        }
        setResult(result, intent);
        finish();
    }

    protected void validateSubmit() {
        Boolean[] validate = manager.validate();
        Log.d(TAG,validate.toString());
        for (int i = 0; i < validate.length; i++) {
            if (!validate[i]) {
                String msg = "";
                switch (i) {
                    case 0: msg = "you must include the current route/direction"; break;
                    case 1: msg = "missing information about start location"; break;
                    case 2: msg = "on location is incomplete"; break;
                    case 3: msg = "off location is incomplete"; break;
                    case 4: msg = "missing information about ending location"; break;
                }
                if(!msg.isEmpty()) {
                    Utils.shortToastCenter(getApplicationContext(), msg);
                    mViewPager.setCurrentItem(i);
                    return;
                }
            }
        }
        exitWithSurveyBundle(true);
    }

    protected void toggleNavButtons(int item) {
        previousBtn.setEnabled(true);
        nextBtn.setEnabled(true);
        if(item == 0) previousBtn.setEnabled(false);
        if(item == HEADERS.length - 1) {
            nextBtn.setText("Submit");
            //nextBtn.setEnabled(false);
        }
        else {
            nextBtn.setText("Next");
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    public static class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            return fragments[i];
        }

        @Override
        public int getCount() {
            return HEADERS.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "Section " + (position + 1);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            manager.unfinishedExit(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    protected Bundle getODKExtras() {
        Intent intent = this.getIntent();
        String action = intent.getAction();
        if (action.equals(ODK_ACTION)) {
            return intent.getExtras();
        }
        return null;
    }

}