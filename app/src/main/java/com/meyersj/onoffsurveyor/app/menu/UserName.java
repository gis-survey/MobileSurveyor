/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.onoffsurveyor.app.menu;

import android.app.Activity;
import android.widget.EditText;


public class UserName {

    private EditText usernameEdit;

    public UserName (Activity activity, int id) {
        usernameEdit = (EditText) activity.findViewById(id);
        usernameEdit.clearFocus();
    }

    public String getUser() {
        return usernameEdit.getText().toString();
    }

}
