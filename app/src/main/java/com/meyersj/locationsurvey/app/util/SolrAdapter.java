package com.meyersj.locationsurvey.app.util;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by meyersj on 7/11/2014.
 */
//public class SolrAdapter extends ArrayAdapter<LocationResult> implements Filterable {
public class SolrAdapter extends ArrayAdapter<String> implements Filterable {


    private Context mContext;
    //private List<LocationResult> mData = new ArrayList<LocationResult>();
    private List<String> mData = new ArrayList<String>();
    private SolrServer mSolrServer;

    public SolrAdapter(SolrServer solrServer, Context context, int resource) {
        super(context, resource);
        mContext = context;
        mSolrServer = solrServer;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public String getItem(int index) {
        return mData.get(index);
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
                        List<String> results = mSolrServer.searchResults(constraint.toString());
                        filterResults.values = results;
                        filterResults.count = results.size();
                    }
                    catch(Exception e) {}

                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence contraint, FilterResults results) {
                if(results != null && results.count > 0) {
                    mData = (List<String>)results.values;
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


