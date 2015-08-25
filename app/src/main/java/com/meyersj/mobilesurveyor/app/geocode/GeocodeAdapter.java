package com.meyersj.mobilesurveyor.app.geocode;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.meyersj.mobilesurveyor.app.ODKApplication;
import com.meyersj.mobilesurveyor.app.util.Cons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GeocodeAdapter extends ArrayAdapter<String> implements Filterable {

    private final String TAG = "SolrAdapter";

    private Context mContext;
    private List<String> mData = new ArrayList<String>();
    private Geocoder geocoder;
    private HashMap<String, LocationResult> mResults;

    public GeocodeAdapter(Context context, int resource, ODKApplication app) {
        super(context, resource);
        mContext = context;
        geocoder = new Geocoder(app.getProperties().getProperty(Cons.SOLR_URL),
                app.getProperties().getProperty(Cons.PELIAS_URL));
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


