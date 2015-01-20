package com.meyersj.mobilesurveyor.app.survey;

import android.content.Context;
import android.util.Log;
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



    /*
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = new TextView(ListHighlightTestActivity.this);
            convertView.setPadding(10, 10, 10, 10);
            ((TextView)convertView).setTextColor(Color.WHITE);
        }

        convertView.setBackgroundColor((position == curSelected) ?
                Color.argb(0x80, 0x20, 0xa0, 0x40) : Color.argb(0, 0, 0, 0));
        ((TextView)convertView).setText((String)getItem(position));

        return convertView;
    }

    public long getItemId(int position)
    {
        return position;
    }

    public Object getItem(int position)
    {
        return "item " + position;
    }

    public int getCount()
    {
        return 20;
    }
    */


    /*

    public View getView(int position, View convertView, ViewGroup parent) {
        // assign the view we are converting to a local variable
        View view = convertView;
        // first check to see if the view is null. if so, we have to inflate it.
        // to inflate it basically means to render, or show, the view.
        if (view == null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.stop_seq_list_item, null);
        }
        String route = routesList.get(position);
        Log.d(TAG, route);
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

    public void setSelectedIndex(int i)
    {
        selectedIndex = i;
        notifyDataSetChanged();
    }
    */
}
