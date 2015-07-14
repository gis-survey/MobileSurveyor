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
