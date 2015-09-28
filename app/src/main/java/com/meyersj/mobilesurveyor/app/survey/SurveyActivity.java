package com.meyersj.mobilesurveyor.app.survey;

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

    protected SurveyFragmentPagerAdapter pagerAdapter;
    protected ViewPager mViewPager;
    protected Button previousBtn;
    protected Button nextBtn;
    protected static SurveyManager manager;
    protected static Fragment[] fragments;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survey);

        Bundle extras = getODKExtras();
        String line = extras.getString(Cons.LINE);
        String dir = extras.getString(Cons.DIR);
        manager = new SurveyManager(getApplicationContext(), this, line, dir, extras);
        SelectedStops selectedStops = new SelectedStops(this);

        pagerAdapter = new SurveyFragmentPagerAdapter(getSupportFragmentManager(), SurveyActivity.this, extras, selectedStops);
        mViewPager = (ViewPager) findViewById(R.id.survey_pager);
        mViewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        previousBtn = (Button) this.findViewById(R.id.previous_fragment);
        nextBtn = (Button) this.findViewById(R.id.next_fragment);

        mViewPager.setOffscreenPageLimit(HEADERS.length);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                MapFragment fragment = (MapFragment) fragments[position];
                fragment.updateView(manager);
                toggleNavButtons(mViewPager.getCurrentItem());
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        fragments = new Fragment[HEADERS.length];
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

    public class SurveyFragmentPagerAdapter extends FragmentPagerAdapter {
        final int PAGE_COUNT = HEADERS.length;
        private Context context;
        private Bundle extras;
        private SelectedStops selectedStops;

        public SurveyFragmentPagerAdapter(FragmentManager fm, Context context, Bundle extras, SelectedStops selectedStops) {
            super(fm);
            this.context = context;
            this.extras = extras;
            this.selectedStops = selectedStops;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            if(fragments[position] != null) return fragments[position];
            switch (position) {
                case 0:
                    fragments[0] = new TransfersMapFragment();
                    ((TransfersMapFragment) fragments[0]).initialize(manager, mViewPager, extras);
                    break;
                case 1:
                    fragments[1] = new PickLocationFragment();
                    ((PickLocationFragment) fragments[1]).initialize(manager, "origin", extras);
                    break;
                case 2:
                    fragments[2] = new StopFragment();
                    ((StopFragment) fragments[2]).initialize(manager, extras, Cons.BOARD, selectedStops);
                    break;
                case 3:
                    fragments[3] = new StopFragment();
                    ((StopFragment) fragments[3]).initialize(manager, extras, Cons.ALIGHT, selectedStops);
                    break;
                case 4:
                    fragments[4] = new PickLocationFragment();
                    ((PickLocationFragment) fragments[4]).initialize(manager, "destination", extras);
                    break;
            }
            return fragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Generate title based on item position
            return HEADERS[position];
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