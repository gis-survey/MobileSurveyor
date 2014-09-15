package com.meyersj.locationsurvey.app.scans;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.meyersj.locationsurvey.app.util.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LocationService extends Service {
	public static final String BROADCAST_ACTION = "com.example.LocationReceiver";
    private static final int INTERVAL_TIME = 1000 * 5;
    private static final float INTERVAL_DIST = 0;
	private final String TAG = "LocationService";
    //private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");



	public LocationManager locationManager;
	public MyLocationListener listener;
	public Location previousBestLocation = null;
	
	Intent intent;
	
	
	@Override
	public void onCreate() {
		Log.d(TAG, "LocationService started");
	    super.onCreate();
	    intent = new Intent(BROADCAST_ACTION);      
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	    listener = new MyLocationListener();        
	    //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL_TIME, INTERVAL_DIST, listener);
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL_TIME, INTERVAL_DIST, listener);

        Date date = new Date();
        String dateString = Utils.dateFormat.format(date);
        Log.d(TAG, "Start Loc: " + dateString);
	}
	
	@Override
	public IBinder onBind(Intent intent) {
	    return null;
	}
	
	protected boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }
	
	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > INTERVAL_TIME;
	    boolean isSignificantlyOlder = timeDelta < INTERVAL_TIME;
	    boolean isNewer = timeDelta > 0;
	
	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }
	
	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;
	
	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());
	
	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}
	
	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    Log.d(TAG, "LocationService onDestroy()");
	    locationManager.removeUpdates(listener);        
	}   
	
	public static Thread performOnBackgroundThread(final Runnable runnable) {
	    final Thread t = new Thread() {
	        @Override
	        public void run() {
	            try {
	                runnable.run();
	            } finally {
	
	            }
	        }
	    };
	    t.start();
	    return t;
	}
	
	
		public class MyLocationListener implements LocationListener
		{
		
		    public void onLocationChanged(final Location loc)
		    {
		        Log.d(TAG, "Location changed");
		        if(isBetterLocation(loc, previousBestLocation)) {

		            String accuracy = Float.toString(loc.getAccuracy());
		            String lat = Double.toString(loc.getLatitude());
		            String lon = Double.toString(loc.getLongitude());
                    String provider = loc.getProvider();
                    Date date = new Date();
                    String dateString = Utils.dateFormat.format(date);

                    Log.d(TAG, "found better location");
                    Log.d(TAG, "Provider: " + provider);
                    Log.d(TAG, "GPS: " + lat + " " + lon);
                    Log.d(TAG, "Accuracy: " + accuracy);
                    Log.d(TAG, "Date: " + dateString + "\n");

                    Utils.appendCSV(dateString + "," + accuracy + "," + lat + "," + lon);
		            
		            intent.putExtra("Latitude", lat);
		            intent.putExtra("Longitude", lon);
                    intent.putExtra("Accuracy", accuracy);
                    intent.putExtra("Date", dateString);

                    String message = lat + " - " + lon + ": " + accuracy;
		            Toast.makeText( getApplicationContext(), message, Toast.LENGTH_SHORT ).show();
		            sendBroadcast(intent);          
		        }                               
		    }
		
		    public void onProviderDisabled(String provider)
		    {
		        //Toast.makeText( getApplicationContext(), "GPS Disabled", Toast.LENGTH_SHORT ).show();
		    }
		
		    public void onProviderEnabled(String provider)
		    {
		        //Toast.makeText( getApplicationContext(), "GPS Enabled", Toast.LENGTH_SHORT).show();
		    }
		
		    public void onStatusChanged(String provider, int status, Bundle extras) {
		   
		
		    }
		}
}
