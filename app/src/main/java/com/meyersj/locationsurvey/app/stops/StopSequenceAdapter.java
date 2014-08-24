package com.meyersj.locationsurvey.app.stops;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meyersj.locationsurvey.app.R;

import java.util.ArrayList;

/**
 * Created by jeff on 8/23/14.
 */
public class StopSequenceAdapter extends ArrayAdapter<Stop> {

    private Context context;
    private ArrayList<Stop> stopsList;
    private int selectedIndex = -1;
    private int selectedColor = Color.parseColor("#1b1b1b");

    private String TAG = "StopListAdapter";

    public StopSequenceAdapter(Context context, ArrayList<Stop> stopsList) {
        super(context, R.layout.list_item_2, stopsList);
        this.context = context;
        this.stopsList = stopsList;
    }


    public View getView(int position, View convertView, ViewGroup parent) {

        // assign the view we are converting to a local variable
        View view = convertView;


        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.list_item_2, null);

        }


		/*
		 * Recall that the variable position is sent if as an argument to this method.
		 * The variable simply refers to the position of the current object in the list. (The ArrayAdapter
		 * iterates through the list we sent it)
		 *
		 * Therefore, i refers to the current Item object.
		 */

        Stop stop = stopsList.get(position);

        if (stop != null) {

            // This is how you obtain a reference to the TextViews.
            // These TextViews are created in the XML files we defined.

            TextView label = (TextView) view.findViewById(R.id.text1);

            // check to see if each individual textview is null.
            // if not, assign some text!
            if (label != null) {
                label.setText(stop.getLabel());

                if(position == selectedIndex)
                {
                    label.setBackground(context.getResources().getDrawable(R.drawable.blue_box));
                }

                else
                {
                    label.setBackground(context.getResources().getDrawable(R.drawable.grey_box));
                }
            }
        }


        return view;
    }

    public void setSelectedIndex(int i)
    {
        Log.d(TAG, String.valueOf(i));
        selectedIndex = i;
        notifyDataSetChanged();
    }

    @Override
    public Stop getItem(int position)
    {
        return stopsList.get(position);
    }

}
