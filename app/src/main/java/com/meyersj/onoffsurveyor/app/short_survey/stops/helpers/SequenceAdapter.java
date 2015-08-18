/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.onoffsurveyor.app.short_survey.stops.helpers;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meyersj.onoffsurveyor.app.R;
import com.meyersj.onoffsurveyor.app.short_survey.stops.Stop;

import java.util.ArrayList;
import java.util.HashMap;

public class SequenceAdapter extends ArrayAdapter<Stop> {

    private Context context;
    private ArrayList<Stop> stopsList;
    private int selectedIndex = -1;
    private HashMap<String, Integer> stopsIndexHash;

    private String TAG = "StopListAdapter";



    public SequenceAdapter(Context context, ArrayList<Stop> stopsList) {
        super(context, R.layout.listview_stop_sequence_item, stopsList);
        this.context = context;
        this.stopsList = stopsList;

        stopsIndexHash = new HashMap<String, Integer>();

        for(Integer i = 0; i < stopsList.size(); i++) {
            stopsIndexHash.put(stopsList.get(i).getDesc(), i);
        }

    }

    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View view = convertView;


        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview_stop_sequence_item, null);

        }

        Stop stop = stopsList.get(position);

        if (stop != null) {

            TextView label = (TextView) view.findViewById(R.id.text);

            if (label != null) {

                label.setText(stop.getDesc());

                if(position == selectedIndex)
                {
                    label.setBackground(context.getResources().getDrawable(R.drawable.shape_grey_squared));
                    label.setTextColor(context.getResources().getColor(R.color.black));

                }

                else
                {
                    label.setBackground(context.getResources().getDrawable(R.drawable.shape_dark_grey_squared));
                    label.setTextColor(context.getResources().getColor(R.color.light_grey));
                }
            }
        }


        return view;
    }

    public Integer getItemIndex(String label) {
        return stopsIndexHash.get(label);
    }


    public void setSelectedIndex(int i)
    {
        selectedIndex = i;
        notifyDataSetChanged();
    }

    public void clearSelected() {
        selectedIndex = -1;
        notifyDataSetChanged();
    }

    @Override
    public Stop getItem(int position)
    {
        return stopsList.get(position);
    }

}
