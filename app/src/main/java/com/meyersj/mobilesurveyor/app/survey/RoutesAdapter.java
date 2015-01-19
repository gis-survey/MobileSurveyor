package com.meyersj.mobilesurveyor.app.survey;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.stops.Stop;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by jeff on 8/23/14.
 */
public class RoutesAdapter extends ArrayAdapter<String> {

    private Context context;
    private ArrayList<String> routesList;
    private int selectedIndex = -1;

    private String TAG = "RoutesAdapter";

    public RoutesAdapter(Context context, ArrayList<String> routesList) {
        super(context, R.layout.stop_seq_list_item, routesList);
        this.context = context;
        this.routesList = routesList;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        // assign the view we are converting to a local variable
        View view = convertView;
        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) view.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.stop_seq_list_item, null);
        }
        String route = routesList.get(position);
        if (route != null) {
            TextView label = (TextView) view.findViewById(R.id.text);
            if (label != null) {
                label.setText(route);
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

    @Override
    public String getItem(int position)
    {
        return routesList.get(position);
    }

}
