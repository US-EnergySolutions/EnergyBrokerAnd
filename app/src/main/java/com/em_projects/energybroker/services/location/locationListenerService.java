package com.em_projects.energybroker.services.location;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.em_projects.energybroker.config.Constants;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class locationListenerService extends Service {
    private static final String TAG = "locationListenerService";

    private final String providerGPS = LocationManager.GPS_PROVIDER; // The name of the provider with which we would like to register.
    private final String providerNTW = LocationManager.NETWORK_PROVIDER;
    private final long minTime = 2000; //  Minimum time interval between location updates (in milliseconds).
    private final float minDistance = 1; // Minimum distance between location updates (in meters).
    public LocationListener listener = null; // LocationListener whose onLocationChanged(Location) method will be called for each location update.

    // Location components
    private LocationManager locationManager;
    private Location location;
    private Context context;

    private SharedPreferences preferences;
    private boolean enableGeoLocation = true;
    private ScheduledExecutorService scheduleTaskExecutor;
    private Future<?> future;
    private int period = 1;

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            //if (null == location)
            location = getLastKnownLocation();
            if (null != location) {
                //period = 15;
                resetTimer();
            }
            if (enableGeoLocation) sendBroadcastMessage(null != location);
        }
    };

    @SuppressLint("MissingPermission")
    private Location getLastKnownLocation() {
        Location bestLocation = null;
        try {
            LocationManager mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            List<String> providers = mLocationManager.getProviders(false);
            for (String provider : providers) {
                Location l = mLocationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, "getLastKnownLocation", ex);
        } finally {
            return bestLocation;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        isLocationEnabled();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        context = this;
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if ("enable_geo_location".equals(key)) {
                    enableGeoLocation = sharedPreferences.getBoolean("enable_geo_location", true);
                    initTimer();
                }
            }
        });
        enableGeoLocation = preferences.getBoolean("enable_geo_location", true);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        listener = new LocationListener() {

            @Override
            public void onLocationChanged(Location location) {
                boolean isAvailable = location != null;
                locationListenerService.this.location = location;
                sendBroadcastMessage(isAvailable);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {
                String msg = "onStatusChanged Provider " + s + " is NOT available";
                Log.d(TAG, "onStatusChanged: " + s);
                // Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderEnabled(String s) {
                sendBroadcastMessage(true);
                String msg = "Provider " + s + " is available now";
                // Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onProviderDisabled(String s) {
                sendBroadcastMessage(false);
                String msg = "Provider " + s + " is NOT available";
                // Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        };
        try {
            locationManager.requestLocationUpdates(providerGPS, minTime, minDistance, listener);
        } catch (SecurityException ex) {
            Log.e(TAG, "requestLocationUpdates", ex);
        }
        initTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void isLocationEnabled() {
        boolean available = false;
        if (null != locationManager) {
            available = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        }
        resetTimer();
        sendBroadcastMessage(available);
    }

    private void sendBroadcastMessage(boolean isAvailable) {
        String action = isAvailable ? Constants.Companion.getLOCATION_AVAILABLE() : Constants.Companion.getLOCATION_NOT_AVAILABLE();
        if (true == enableGeoLocation) {
            double latitude = location == null ? 0 : location.getLatitude();
            double longitude = location == null ? 0 : location.getLongitude();
            Intent intent = new Intent(action);
            intent.putExtra(Constants.Companion.getNAME_LATITUDE_DATA(), latitude);
            intent.putExtra(Constants.Companion.getNAME_LONGITUDE_DATA(), longitude);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private void initTimer() {
        scheduleTaskExecutor = Executors.newScheduledThreadPool(5);
        future = scheduleTaskExecutor.scheduleAtFixedRate(timerRunnable, 0, period, TimeUnit.MINUTES);
    }

    private void resetTimer() {
        future.cancel(true);
        future = scheduleTaskExecutor.scheduleAtFixedRate(timerRunnable, period, period, TimeUnit.MINUTES);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != locationManager) {
            locationManager.removeUpdates(listener);
        }
        scheduleTaskExecutor.shutdown();
    }
}
