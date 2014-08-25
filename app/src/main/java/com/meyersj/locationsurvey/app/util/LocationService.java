package com.meyersj.locationsurvey.app.util;

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

public class LocationService extends Service {
	public static final String BROADCAST_ACTION = "com.example.LocationReceiver";
	private final String TAG = "LocationService";
	private static final int INTERVAL = 1000 * 15;
    private static final int TWO_MINUTES = 1000 * 60 * 2;
	private static final int intervalTime = 1000 * 30;
	private static final float intervalDist = 0;
	public LocationManager locationManager;
	public MyLocationListener listener;
	public Location previousBestLocation = null;
	
	Intent intent;
	int counter = 0;
	
	
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
	    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, INTERVAL, intervalDist, listener);
	    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, INTERVAL, intervalDist, listener);
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
	    boolean isSignificantlyNewer = timeDelta > INTERVAL;
	    boolean isSignificantlyOlder = timeDelta < INTERVAL;
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
		        Log.i("**************************************", "Location changed");
		        if(isBetterLocation(loc, previousBestLocation)) {
		            loc.getLatitude();
		            loc.getLongitude();             
		            
		            String lat = Double.toString(loc.getLatitude());
		            String lon = Double.toString(loc.getLongitude());
		            
		            
		            intent.putExtra("Latitude", lat);
		            intent.putExtra("Longitude", lon);     
		            intent.putExtra("Provider", loc.getProvider());
		            Log.d(TAG, lat + ' ' + lon);
		            Toast.makeText( getApplicationContext(), "GPS updated", Toast.LENGTH_SHORT ).show();
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
