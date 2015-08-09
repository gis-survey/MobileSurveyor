package com.meyersj.mobilesurveyor.app.stops.helpers;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends ArrayAdapter<String> {

    private static final String TAG = "MarkerAdapter";

    private List<String> stopsList;
    private Context context;
    private Filter stopsFilter;
    private List<String> origStopsList;

    public SearchAdapter(Context aContext, int resource, List<String> objects) {
        super(aContext, resource, objects);
        //super(aContext, resource, objects);
        stopsList = objects;
        origStopsList = new ArrayList<String>(objects);
        context = aContext;
    }

    @Override
    public Filter getFilter() {
        if (stopsFilter == null)
            stopsFilter = new StopsFilter();

        return stopsFilter;
    }

    private class StopsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = origStopsList;
                results.count = origStopsList.size();

            } else {

                ArrayList<String> nStopsList = new ArrayList<String>();

                for (String stop : origStopsList) {
                    if (stop.toUpperCase().contains(constraint.toString().toUpperCase())) {
                        nStopsList.add(stop);
                    }
                    else {
                    }
                }
                results.values = nStopsList;
                results.count = nStopsList.size();



            }
            Log.d(TAG, String.valueOf(results.count));
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {

            if (results.count == 0)
                notifyDataSetInvalidated();
            else {
                stopsList = (List<String>) results.values;

                notifyDataSetChanged();
                clear();
                for(String s : stopsList){
                    add(s);
                }
                notifyDataSetChanged();
            }
        }
    }
}