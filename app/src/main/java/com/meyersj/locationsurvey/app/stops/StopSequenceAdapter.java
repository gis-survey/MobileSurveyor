package com.meyersj.locationsurvey.app.stops;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meyersj.locationsurvey.app.R;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jeff on 8/23/14.
 */
public class StopSequenceAdapter extends ArrayAdapter<Stop> {

    private Context context;
    private ArrayList<Stop> stopsList;
    private int selectedIndex = -1;
    private HashMap<String, Integer> stopsIndexHash;

    private String TAG = "StopListAdapter";



    public StopSequenceAdapter(Context context, ArrayList<Stop> stopsList) {
        super(context, R.layout.stop_seq_list_item, stopsList);
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
            view = inflater.inflate(R.layout.stop_seq_list_item, null);

        }

        Stop stop = stopsList.get(position);

        if (stop != null) {

            TextView label = (TextView) view.findViewById(R.id.text);

            if (label != null) {

                label.setText(stop.getDesc());

                if(position == selectedIndex)
                {
                    label.setBackground(context.getResources().getDrawable(R.drawable.shape_rect_light_grey_nofade));
                    label.setTextColor(context.getResources().getColor(R.color.black));

                }

                else
                {
                    label.setBackground(context.getResources().getDrawable(R.drawable.shape_rect_grey_nofade));
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
