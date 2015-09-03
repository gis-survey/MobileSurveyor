package com.meyersj.mobilesurveyor.app;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.meyersj.mobilesurveyor.app.util.Cons;
import com.newrelic.agent.android.NewRelic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class ODKApplication extends Application {

    private final String TAG = getClass().getCanonicalName();
    private Properties properties;

    @Override
    public void onCreate() {
        super.onCreate();
        readProperties();

        String token = properties.getProperty(Cons.NEWRELIC_TOKEN, "");
        //if(!token.isEmpty()) {
        //    NewRelic.withApplicationToken(token).start(this);
        //}
    }

    private void readProperties() {
        Resources resources = this.getResources();
        AssetManager assetManager = resources.getAssets();

        // read properties from /assets directory
        try {
            InputStream inputStream = assetManager.open("config.properties");
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
            Log.d(TAG, e.toString());
        }
    }

    public Properties getProperties() {
        return properties;
    }
}
