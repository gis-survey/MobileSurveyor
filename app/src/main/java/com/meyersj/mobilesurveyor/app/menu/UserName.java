package com.meyersj.mobilesurveyor.app.menu;

import android.app.Activity;
import android.widget.EditText;

import com.meyersj.mobilesurveyor.app.R;

/**
 * Created by jeff on 8/8/15.
 */
public class UserName {

    private EditText usernameEdit;

    public UserName (Activity activity, int id) {
        usernameEdit = (EditText) activity.findViewById(id);
    }

    public String getUser() {
        return usernameEdit.getText().toString();
    }

}
