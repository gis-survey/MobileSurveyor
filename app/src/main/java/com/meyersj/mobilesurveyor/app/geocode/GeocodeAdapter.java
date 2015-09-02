package com.meyersj.mobilesurveyor.app.geocode;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.meyersj.mobilesurveyor.app.ODKApplication;
import com.meyersj.mobilesurveyor.app.survey.SurveyManager;
import com.meyersj.mobilesurveyor.app.util.Cons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class GeocodeAdapter extends ArrayAdapter<String> implements Filterable {

    private final String TAG = "SolrAdapter";

    private List<String> mData = new ArrayList<String>();
    private Geocoder geocoder;
    private HashMap<String, LocationResult> mResults;

    public GeocodeAdapter(Context context, int resource, ODKApplication app) {
        super(context, resource);
        geocoder = new Geocoder(app.getProperties().getProperty(Cons.SOLR_URL),
                app.getProperties().getProperty(Cons.PELIAS_URL));
        mResults = new HashMap<String, LocationResult>();
    }

    public GeocodeAdapter(Context context, int resource, ODKApplication app, SurveyManager manager, String mode) {
        super(context, resource);
        geocoder = new Geocoder(app.getProperties().getProperty(Cons.SOLR_URL),
                app.getProperties().getProperty(Cons.PELIAS_URL), manager, mode);
        mResults = new HashMap<String, LocationResult>();
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

                    //HashMap<String, LocationResult> map = geocoder.getResultsHash();
                    ArrayList<LocationResult> resultsInOrder = geocoder.getResultsInOrder();
                    //mResults =
                    //ArrayList<LocationResult> results = new ArrayList<LocationResult>();

                    filterResults.values = resultsInOrder;
                    filterResults.count = resultsInOrder.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    ArrayList<LocationResult> records = (ArrayList <LocationResult>) results.values;
                    mData.clear();
                    mResults.clear();
                    for(LocationResult record: records) {
                        mData.add(record.toString());
                        mResults.put(record.toString(), record);
                    }
                    //mData = (List<String>) results.values;
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


