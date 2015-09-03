package com.meyersj.mobilesurveyor.app.survey;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.mapbox.mapboxsdk.views.MapView;
import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.SelectedStops;
import com.meyersj.mobilesurveyor.app.survey.Location.PickLocationFragment;
import com.meyersj.mobilesurveyor.app.survey.OnOff.StopFragment;
import com.meyersj.mobilesurveyor.app.survey.Transfer.TransfersMapFragment;
import com.meyersj.mobilesurveyor.app.util.Cons;
import com.meyersj.mobilesurveyor.app.util.Utils;

public class SurveyActivity extends FragmentActivity { //implements ActionBar.TabListener {

    private final String TAG = getClass().getCanonicalName();
    protected final String ODK_ACTION = "com.meyersj.mobilesurveyor.app.ODK_SURVEY";
    protected static final String[] HEADERS = {"Routes", "Start", "On", "Off", "End"};

    //protected AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    protected SurveyFragmentPagerAdapter pagerAdapter;
    protected ViewPager mViewPager;
    protected Button previousBtn;
    protected Button nextBtn;
    protected static SurveyManager manager;
    protected static Fragment[] fragments;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);


        pagerAdapter = new SurveyFragmentPagerAdapter(getSupportFragmentManager(), SurveyActivity.this);
        ViewPager viewPager = (ViewPager) findViewById(R.id.survey_pager);
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);

        previousBtn = (Button) this.findViewById(R.id.previous_fragment);
        nextBtn = (Button) this.findViewById(R.id.next_fragment);

        //final ActionBar actionBar = getActionBar();
        Bundle extras = getODKExtras();
        String line = extras.getString(Cons.LINE);
        String dir = extras.getString(Cons.DIR);
        manager = new SurveyManager(getApplicationContext(), this, line, dir, extras);
        SelectedStops selectedStops = new SelectedStops(this);

        //if(actionBar != null) {
        //    actionBar.setHomeButtonEnabled(false);
        //    actionBar.setTitle("TransitSurveyor");
        //    actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        //}
        //mViewPager = (ViewPager) findViewById(R.id.survey_pager);

        //TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        //tabLayout.setupWithViewPager(mViewPager);


        /*
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
        */



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


        //for (int i = 0; i < HEADERS.length; i++) {
        //    actionBar.addTab(actionBar.newTab().setText(HEADERS[i]).setTabListener(this));
        //}

        /*
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
        */
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

    /*
    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {
        mViewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction fragmentTransaction) {}
    */

    public class SurveyFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = HEADERS.length;
        private Context context;

        public SurveyFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            return fragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return HEADERS[position];
        }
    }

    /*
    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        private Context context;

        public AppSectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
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
    */



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
        Bundle extras;
        if (action.equals(ODK_ACTION)) {
            extras = intent.getExtras();
            String line = extras.getString(Cons.LINE, "");
            String dir = extras.getString(Cons.DIR, "");
            if(line.isEmpty() || dir.isEmpty()) {
                Utils.shortToastCenter(this, "Error: Route and/or Direction not selected");
                finish();
            }
        }
        else {
            extras = new Bundle();
            extras.putString(Cons.LINE, Cons.DEFAULT_RTE);
            extras.putString(Cons.DIR, Cons.DEFAULT_DIR);
        }
        return extras;
    }

}