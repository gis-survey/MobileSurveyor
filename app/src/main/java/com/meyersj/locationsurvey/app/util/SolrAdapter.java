package com.meyersj.locationsurvey.app.util;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by meyersj on 7/11/2014.
 */
//public class SolrAdapter extends ArrayAdapter<LocationResult> implements Filterable {
public class SolrAdapter extends ArrayAdapter<String> implements Filterable {

    private final String TAG = "SolrAdapter";


    private Context mContext;
    //private List<LocationResult> mData = new ArrayList<LocationResult>();
    private List<String> mData = new ArrayList<String>();
    private SolrQuery mSolrQuery;
    private HashMap<String, LocationResult> mResults;





    public SolrAdapter(Context context, int resource) {
        super(context, resource);
        mContext = context;
        mSolrQuery = new SolrQuery();

    }

    @Override
    public int getCount() {
        return mData.size();
    }


    @Override
    public String getItem(int index) {
        return mData.get(index);
    }



    public LocationResult getLocationResultItem(String name) {

        LocationResult locationResult = null;
        //Log.d(TAG, "selected: " + name);

        Log.d(TAG, "all results: ");
        Log.d(TAG, String.valueOf(mResults.size()));

        Iterator it = mResults.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            Log.d(TAG, ":" + name + ":");
            Log.d(TAG, ":" + (String) pairs.getKey() + ":\n");



            if(name.equals((String) pairs.getKey())) {
                Log.d(TAG, "equal");
                locationResult = mResults.get(name);
                Log.d(TAG, locationResult.getLatLng().toString());

                if (mResults.containsKey(name)) {
                     locationResult = mResults.get(name);

                     Log.d(TAG, "containKey");
                }
                else {
                    Log.d(TAG, "does not containKey");
                }
            }
            else {
                Log.d(TAG, "not equal");
            }

            //it.remove(); // avoids a ConcurrentModificationException




        }

        if (mResults.containsKey(name)) {
            locationResult = mResults.get(name);
        }
        else {
            Log.d(TAG, "no match found");
        }




        return locationResult;
    }



    public List<String> getNames(Map mp) {
        List<String> names = new ArrayList<String>();

        Log.d(TAG, "start getNames");
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pairs = (Map.Entry)it.next();
            String name = mResults.get(pairs.getKey()).getScore().toString() + "|" + (String) pairs.getKey();
            //String name = (String) pairs.getKey();
            names.add(name);
            Log.d(TAG, (String) pairs.getKey());
            //it.remove(); // avoids a ConcurrentModificationException
        }
        Log.d(TAG, "end getNames\n");



        Collections.sort(names);
        Collections.reverse(names);

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
                // This method is called in a worker thread

                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    try {
                        // Here is the method (synchronous) that fetches the data
                        // from the server
                        //List<String> results = mSolrServer.searchResults(constraint.toString());

                        //mResults = mSolrServer.searchResults2(constraint.toString());

                        //TODO this needs to be sorted on score
                        //List<String> results2 = getNames(mResults);


                        mSolrQuery.solrLookup(constraint.toString());
                        mResults = mSolrQuery.getSolrResults();
                        List<String> results = getNames(mResults);



                        //filterResults.values = null;
                        //filterResults.count = 0;

                        filterResults.values = results;
                        filterResults.count = results.size();

                        //filterResults.values = mResults;


                    }
                    catch(Exception e) {}

                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    mData = (List<String>)results.values;

                    Log.d(TAG, "START mData updated");
                    for(String s: mData) {
                        Log.d(TAG, s);
                    }
                    Log.d(TAG, "END mData updated");


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


