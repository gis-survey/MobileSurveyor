package com.meyersj.mobilesurveyor.app.survey;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.meyersj.mobilesurveyor.app.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jeff on 1/23/15.
 */
public class TransfersAdapter extends ArrayAdapter<String> {

    protected ArrayList<String> transfers;
    protected String curLine;
    protected HashMap<String, String> routeLookup;

    public TransfersAdapter(Context context, ArrayList<String> transfers, String curLine,
                            HashMap<String, String> routeLookup) {
        super(context, android.R.layout.simple_list_item_multiple_choice, transfers);
        this.transfers = transfers;
        this.curLine = curLine;
        this.routeLookup = routeLookup;
    }

    @Override
    public boolean areAllItemsEnabled(){
        return false;
    }

    @Override
    public boolean isEnabled(int position)
    {
        String item = super.getItem(position);
        Log.d("TransfersAdapter", item);
        if (routeLookup.get(curLine).equals(item)){
            return false;

        }
        return true;
    }


}
