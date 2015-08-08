package com.meyersj.mobilesurveyor.app.scans;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;

/**
 * Created by jeff on 8/8/15.
 */
public class ModeSelector {

    private Button onButton;
    private Button offButton;
    private String mode = Cons.ON;

    public ModeSelector(Activity activity, int onID, int offID, Boolean isOffMode) {
        onButton = (Button) activity.findViewById(onID);
        offButton = (Button) activity.findViewById(offID);
        if(!isOffMode) {
            setListeners(activity);
        }
        else {
            mode = Cons.OFF;
            onButton.setVisibility(View.GONE);
            offButton.setVisibility(View.GONE);
        }
    }


    private void setListeners(Activity activity) {
       final Resources resources = activity.getResources();

        onButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {
               mode = Cons.ON;
               onButton.setBackground(resources.getDrawable(R.drawable.shape_green_round));
               offButton.setBackground(resources.getDrawable(R.drawable.shape_grey_round));
           }
       });

       offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = Cons.OFF;
                offButton.setBackground(resources.getDrawable(R.drawable.shape_green_round));
                onButton.setBackground(resources.getDrawable(R.drawable.shape_grey_round));
            }
        });

    }

    public String getMode() {
        return mode;
    }

}
