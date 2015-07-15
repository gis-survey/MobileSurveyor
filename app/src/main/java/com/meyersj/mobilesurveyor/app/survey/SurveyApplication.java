/*
 * Copyright © 2015 Jeffrey Meyers
 * This program is released under the "MIT License".
 * Please see the file COPYING in the source
 * distribution of this software for license terms.
 */

package com.meyersj.mobilesurveyor.app.survey;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

@ReportsCrashes(
    formKey = "",
    formUri = "http://api.meyersj.com/crash"
)

public class SurveyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //ACRA.init(this);
    }

}
