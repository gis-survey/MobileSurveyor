package com.meyersj.mobilesurveyor.app.locations;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.meyersj.mobilesurveyor.app.util.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class GeocodeAdapter extends ArrayAdapter<String> implements Filterable {

    private final String TAG = "SolrAdapter";

    private Context mContext;
    private List<String> mData = new ArrayList<String>();
    private Geocoder geocoder;
    private HashMap<String, LocationResult> mResults;

    public GeocodeAdapter(Context context, int resource, String url) {
        super(context, resource);
        mContext = context;
        geocoder = new Geocoder(url);
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
        }

        return locationResult;
    }

    public void clearResults() {
        geocoder.clearResults();
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                // This method is called in a worker thread
                FilterResults filterResults = new FilterResults();
                if(constraint != null) {
                    geocoder.lookup(constraint.toString());
                    mResults = geocoder.getResultsHash();
                    ArrayList<String> resultsInOrder = geocoder.getResultsInOrder();
                    filterResults.values = resultsInOrder;
                    filterResults.count = resultsInOrder.size();
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


