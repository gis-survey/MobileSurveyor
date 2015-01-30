package com.meyersj.mobilesurveyor.app.survey;

import android.app.ActionBar;
import android.app.FragmentTransaction;
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
import com.meyersj.mobilesurveyor.app.util.ConfirmFragment;

public class SurveyActivity extends FragmentActivity implements ActionBar.TabListener {

    public static final int SURVEY_FRAGMENTS = 4;//5;
    public static final String[] HEADERS = {"Origin", "Destination", "On-Off", "Transfers" };// "Confirm"};

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
        manager = new SurveyManager(getApplicationContext(), getParent(), "9", "1");
        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(getSupportFragmentManager());
        final ActionBar actionBar = getActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setTitle("TransitSurveyor");
        //actionBar.setIcon(getApplicationContext().getResources().getDrawable(R.drawable.bus_icon50));
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        mViewPager = (ViewPager) findViewById(R.id.survey_pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(SURVEY_FRAGMENTS);
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
                MapFragment fragment = (MapFragment) fragments[position];
                fragment.updateView(manager);
                toggleNavButtons(mViewPager.getCurrentItem());
            }
        });
        // For each of the sections in the app, add a tab to the action bar.
        fragments = new Fragment[SURVEY_FRAGMENTS];
        fragments[0] = new PickLocationFragment(manager, "origin");
        fragments[1] = new PickLocationFragment(manager, "destination");
        fragments[2] = new OnOffFragment(manager);
        fragments[3] = new TransfersMapFragment(manager, mViewPager);
        //fragments[4] = new ConfirmFragment(this, manager, mViewPager);
        for (int i = 0; i < SURVEY_FRAGMENTS; i++) {
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
            }
        });
    }

    protected void toggleNavButtons(int item) {
        previousBtn.setEnabled(true);
        nextBtn.setEnabled(true);
        if(item == 0)
            previousBtn.setEnabled(false);
        if(item == SURVEY_FRAGMENTS - 1)
            nextBtn.setEnabled(false);
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
            return SURVEY_FRAGMENTS;
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

}