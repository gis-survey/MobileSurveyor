package com.meyersj.locationsurvey.app.stops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

import com.mapbox.mapboxsdk.geometry.LatLng;
import com.meyersj.locationsurvey.app.R;


public class StopsListActivity extends Activity {

    StopListAdapter listAdapter1;
    StopListAdapter listAdapter2;
    ExpandableListView expListView1;
    ExpandableListView expListView2;
    List<String> listDataHeader1;
    List<String> listDataHeader2;
    HashMap<String, List<Stop>> listDataChild1;
    HashMap<String, List<Stop>> listDataChild2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stops_list);

        // get the listview
        expListView1 = (ExpandableListView) findViewById(R.id.exp_list1);
        expListView2 = (ExpandableListView) findViewById(R.id.exp_list2);


        // preparing list data
        prepareListData();

        listAdapter1 = new StopListAdapter(this, listDataHeader1, listDataChild1);
        listAdapter2 = new StopListAdapter(this, listDataHeader2, listDataChild2);

        // setting list adapter
        expListView1.setAdapter(listAdapter1);
        expListView2.setAdapter(listAdapter2);



        expListView1.setOnChildClickListener(new OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                final Stop selected = (Stop) listAdapter1.getChild(
                        groupPosition, childPosition);
                Toast.makeText(getBaseContext(), selected.getDesc(), Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        });

        expListView2.setOnChildClickListener(new OnChildClickListener() {

            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {

                final Stop selected = (Stop) listAdapter2.getChild(
                        groupPosition, childPosition);
                Toast.makeText(getBaseContext(), selected.getDesc(), Toast.LENGTH_LONG)
                        .show();

                return true;
            }
        });


    }

    /*
     * Preparing the list data
     */
    private void prepareListData() {
        listDataHeader1 = new ArrayList<String>();
        listDataChild1 = new HashMap<String, List<Stop>>();

        listDataHeader2 = new ArrayList<String>();
        listDataChild2 = new HashMap<String, List<Stop>>();

        // Adding child data
        listDataHeader1.add("On");
        listDataHeader2.add("Off");


        // Adding child data
        List<Stop> on = new ArrayList<Stop>();
        LatLng l = new LatLng(46.2141,122.1414);

        on.add(new Stop("Stop 1", "1234", 1, l));
        on.add(new Stop("Stop 2", "2135", 2, l));
        on.add(new Stop("Stop 3", "6323", 3, l));
        on.add(new Stop("Stop 4", "9501", 4, l));


        //List<Stop> off = new ArrayList<String>();
        //off.add("Stop 1");
        //off.add("Stop 2");
        //off.add("Stop 3");
        //off.add("Stop 4");


        listDataChild1.put("On", on); // Header, Child data
        listDataChild2.put("Off", on); // Header, Child data
    }
}