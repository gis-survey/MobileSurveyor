/*
 * Copyright © 2015 Jeffrey Meyers.
 *
 * This program is released under the "MIT License".
 * Please see the file COPYING in this distribution for license terms.
 */


package com.meyersj.mobilesurveyor.app.util;



import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.widget.Toast;

import org.joda.time.DateTime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;


public class Utils {

    public static final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static final DateFormat csvDateFormat = new SimpleDateFormat("yyyyMMdd");

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    //read properties from config file in assets
    public static Properties getProperties(Context context, String filename) {
        Properties properties = null;

        try {
            InputStream inputStream = context.getResources().getAssets().open(filename);
            properties = new Properties();
            properties.load(inputStream);
        } catch (IOException e) {
        }
        return properties;
    }


    public static void shortToastCenter(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void shortToastUpper(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, -300);
        toast.show();
    }

    public static void longToastCenter(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public static void appendCSV(String type, String text) {
        String date = Utils.csvDateFormat.format(new Date());
        String path = "sdcard/LocationSurvey/" + type;
        String filename = type + "_" + date + ".csv";

        File folder = new File(path);
        folder.mkdirs();
        File logFile = new File(path + "/" + filename);

        if (!logFile.exists()) {

            try {
                logFile.createNewFile();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isGPSEnabled(Context context) {
        final LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return false;
        }
        return true;
    }

    public static Date parseDate(String dateString) {
        Date date = null;
        try {
            date = Utils.dateFormat.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    //returns difference in milliseconds
    public static Float timeDifference(Date currentDate, Date compareDate) {
        DateTime current = new DateTime(currentDate);
        DateTime compare = new DateTime(compareDate);

        float diff = compare.getMillis() - current.getMillis();
        return Math.abs(diff);
    }

    public static String getUrlApi(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String url = "";
        String defaultUrl = getProperties(context, Cons.PROPERTIES).getProperty(Cons.BASE_URL);
        url = sharedPref.getString(Cons.BASE_URL, defaultUrl);
        return url;
    }

    public static String[] getMapRoutes(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String routesStr = getProperties(context, Cons.PROPERTIES).getProperty(Cons.MAP_RTES);
        return sharedPref.getString(Cons.MAP_RTES, routesStr).split(",");
    }


}
