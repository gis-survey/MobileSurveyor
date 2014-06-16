package com.meyersj.locationsurvey.app.util;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import com.mapbox.mapboxsdk.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MarkerAdapter extends ArrayAdapter<String> {

    private static final String TAG = "MarkerAdapter";

    private List<String> stopsList;
    private Context context;
    private Filter stopsFilter;
    private List<String> origStopsList;



    public MarkerAdapter(Context aContext, int resource, List<String> objects) {
        super(aContext, resource, objects);
        //super(aContext, resource, objects);

        stopsList = objects;
        origStopsList = new ArrayList<String>(objects);


        //for(String s: objects) {
        //    origStopsList.add(s);
        //    stopsList.add(s);
        //}

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
                //Log.d(TAG, "constraint is null");
            } else {

                //Log.d(TAG, "constraint is not null");
                //Log.d(TAG, constraint.toString().toUpperCase());
                ArrayList<String> nStopsList = new ArrayList<String>();

                for (String stop : origStopsList) {
                    //stop.toUpperCase().contains(constraint.toString().toUpperCase())
                    //stop.toUpperCase().startsWith(constraint.toString().toUpperCase())

                    if (stop.toUpperCase().contains(constraint.toString().toUpperCase())) {
                        nStopsList.add(stop);
                        Log.d(TAG, "match: " + stop);
                    }
                    else {
                        Log.d(TAG, "no match: " + stop);
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