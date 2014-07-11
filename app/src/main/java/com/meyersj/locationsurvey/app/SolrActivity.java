package com.meyersj.locationsurvey.app;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.widget.AutoCompleteTextView;

import com.meyersj.locationsurvey.app.util.SolrServer;
import com.meyersj.locationsurvey.app.util.SolrAdapter;

/**
 * Created by meyersj on 7/11/2014.
 */
public class SolrActivity extends ActionBarActivity {

    private final String TAG = "SolrActivity";
    private AutoCompleteTextView solrSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solr);

        solrSearch = (AutoCompleteTextView) findViewById(R.id.solr_input);

        SolrAdapter adapter = new SolrAdapter(new SolrServer(),this,android.R.layout.simple_list_item_1);
        solrSearch.setAdapter(adapter);

        //ArrayList<String> options=new ArrayList<String>();

        //options.add("option 1");
        //options.add("option 2");
        //options.add("option 3");

// use default spinner item to show options in spinner
        //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,options);
        //solrInput.setAdapter(adapter);


    }
}
