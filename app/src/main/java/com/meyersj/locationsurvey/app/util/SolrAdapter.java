package com.meyersj.locationsurvey.app.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class SolrAdapter extends ArrayAdapter<String> implements Filterable {

    private final String TAG = "SolrAdapter";

    private Context mContext;
    private List<String> mData = new ArrayList<String>();
    private SolrQuery mSolrQuery;
    private HashMap<String, LocationResult> mResults;

    public SolrAdapter(Context context, int resource, String url) {
        super(context, resource);
        mContext = context;
        mSolrQuery = new SolrQuery(url);
    }

    @Override
    public int getCount() {
        return mData.size();
    }


    @Override
    public String getItem(int index) {
        return mData.get(index);
    }


    //used to retrieve item picked after user selection
    public LocationResult getLocationResultItem(String name) {

        LocationResult locationResult = null;
        if (mResults.containsKey(name)) {
            locationResult = mResults.get(name);
            Log.d(TAG, "Found key: " + name);
        }
        else {
            Log.e(TAG, "Current mResults does not contain key: " + name);
        }


        return locationResult;
    }

    public List<String> getNames(Map mp) {
        List<String> names = new ArrayList<String>();

        //add score to beginning of string and add to list
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            String name = mResults.get(pairs.getKey()).getScore().toString() + "|" + (String) pairs.getKey();
            names.add(name);
        }

        //sort names in descending order
        //higher scores will be first
        Collections.sort(names);
        Collections.reverse(names);

        //remove score from beginning of string
        for(int i = 0; i < names.size(); i++) {
            String n = names.get(i);
            names.set(i, n.substring(n.indexOf("|") + 1));
        }

        return names;
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                List<String> results;
                // This method is called in a worker thread
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    try {
                        //fetch data from Solr Server
                        mSolrQuery.solrLookup(constraint.toString());
                        mResults = mSolrQuery.getSolrResults();

                        //extract names to be displayed in UI
                        results = getNames(mResults);
                        filterResults.values = results;
                        filterResults.count = results.size();

                    } catch (Exception e) {
                        Log.e(TAG, "Filter failed");
                    }
                }

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {

                if(results != null && results.count > 0) {
                    mData = (List<String>) results.values;
                    notifyDataSetChanged();
                }
                else {
                    notifyDataSetInvalidated();
                }
            }
        };
        return myFilter;
    }

}


