/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.mobilesurveyor.app.short_survey.scans;

import android.app.Activity;
import android.content.res.Resources;
import android.view.View;
import android.widget.Button;

import com.meyersj.mobilesurveyor.app.R;
import com.meyersj.mobilesurveyor.app.util.Cons;


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
               onButton.setBackground(resources.getDrawable(R.drawable.shape_green_rounded));
               offButton.setBackground(resources.getDrawable(R.drawable.shape_grey_rounded));
           }
       });

       offButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mode = Cons.OFF;
                offButton.setBackground(resources.getDrawable(R.drawable.shape_green_rounded));
                onButton.setBackground(resources.getDrawable(R.drawable.shape_grey_rounded));
            }
        });

    }

    public String getMode() {
        return mode;
    }

}
