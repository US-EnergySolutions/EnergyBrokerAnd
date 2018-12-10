package com.em_projects.energybroker.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;
import com.em_projects.energybroker.R;
import com.em_projects.energybroker.config.Constants;
import com.em_projects.energybroker.services.location.locationListenerService;
import com.em_projects.energybroker.view.map.adapters.CustomInfoWindowGoogleMap;
import com.em_projects.energybroker.view.map.model.InfoWindowData;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.*;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MapActivity";

    // Permissions components
    private int RC_APP_PERMISSION = 123;
    private String[] appPermissions = {
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    // Location Components
    private BroadcastReceiver locationReceiver;
    private boolean isGpsAvailable = false;

    // Map Components
    private GoogleMap map;
    private double lat = 47;
    private double lng = 28;
    private float zoom = 18;
    private Marker marker;

    // Helpers
    private Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Log.d(TAG, "onCreate");
        context = this;

        // Init Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getPermissions();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setStyle(map);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMinZoomPreference(11);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), zoom));
        setMapLongClick(map);
//        setPoiClick(map);
        setMapClick(map);
        setOnInfoWindowClickListener(map);
        setOnMarkerClickListener(map);
        updateMap(lat, lng);
    }

    private void setStyle(GoogleMap map) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.map_style));

            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }
    }

    private void updateMap(double lat, double lng) {
        // Remove the previous marker
        LatLng latLng = new LatLng(lat, lng);
        // Add a marker and move the camera
        if (marker != null) marker.remove();
        marker = map.addMarker(new MarkerOptions().position(latLng).title("My Marker"));
        map.moveCamera(CameraUpdateFactory.newLatLng(latLng));
    }

    private void setOnMarkerClickListener(final GoogleMap map) {
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                InfoWindowData info = new InfoWindowData();
                info.setImage(marker.getTitle());
                info.setHotel("Hotel : " + marker.getPosition().toString());
                info.setFood("Food : ");
                info.setTransport("Reach ");

                CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(context);
                map.setInfoWindowAdapter(customInfoWindow);
                marker.setTag(info);
                marker.showInfoWindow();
                return true;
            }
        });
    }

    private void setOnInfoWindowClickListener(GoogleMap map) {
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Toast.makeText(context, "Info Window Click " + marker.getPosition().toString(),
                        Toast.LENGTH_SHORT).show();
                marker.hideInfoWindow();
            }
        });
    }

    private void setMapLongClick(final GoogleMap map) {
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                String snippet = String.format(Locale.getDefault(),
                        "Lat: %1$.5f, Long: %2$.5f",
                        latLng.latitude,
                        latLng.longitude);

                // if (marker != null) marker.remove();
                marker = map.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(getString(R.string.dropped_pin))
                        .snippet(snippet));
            }
        });
    }

    private void setPoiClick(final GoogleMap map) {
        map.setOnPoiClickListener(new GoogleMap.OnPoiClickListener() {
            @Override
            public void onPoiClick(PointOfInterest poi) {
                Marker poiMarker = map.addMarker(new MarkerOptions()
                        .position(poi.latLng)
                        .title(poi.name));
                poiMarker.showInfoWindow();
            }
        });
    }

    private void setMapClick(final GoogleMap map) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Toast.makeText(context, "lat: " + latLng.latitude + "\nlng: " + latLng.longitude, Toast.LENGTH_SHORT).show();
//                LatLng snowqualmie = new LatLng(47.5287132, -121.8253906);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng)
                        .title("Snowqualmie Falls")
                        .snippet("Snoqualmie Falls is located 25 miles east of Seattle.")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                InfoWindowData info = new InfoWindowData();
                info.setImage("snowqualmie");
                info.setHotel("Hotel : excellent hotels available" + latLng.toString());
                info.setFood("Food : all types of restaurants available");
                info.setTransport("Reach the site by bus, car and train.");

                CustomInfoWindowGoogleMap customInfoWindow = new CustomInfoWindowGoogleMap(context);
                map.setInfoWindowAdapter(customInfoWindow);

                Marker m = map.addMarker(markerOptions);
                m.setTag(info);
                m.showInfoWindow();
            }
        });
    }

    private void registerLocalLocationReceiver() {
        IntentFilter locationIntentFilter = new IntentFilter();
        locationIntentFilter.addAction(Constants.Companion.getLOCATION_NOT_AVAILABLE());
        locationIntentFilter.addAction(Constants.Companion.getLOCATION_AVAILABLE());
        LocalBroadcastManager.getInstance(context).registerReceiver(locationReceiver, locationIntentFilter);
    }

    private void startLocationMonitoring() {
        // GPS reception listener
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                isGpsAvailable = Constants.Companion.getLOCATION_AVAILABLE().equalsIgnoreCase(action);
                if (isGpsAvailable) {
                    lat = intent.getDoubleExtra(Constants.Companion.getNAME_LATITUDE_DATA(), lat);
                    lng = intent.getDoubleExtra(Constants.Companion.getNAME_LONGITUDE_DATA(), lng);
                    updateMap(lat, lng);
                }
            }
        };
        registerLocalLocationReceiver();
        Intent locationServiceIntent = new Intent(context, locationListenerService.class);
        startService(locationServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Change the map type based on the user's selection.
        switch (item.getItemId()) {
            case R.id.normal_map:
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case R.id.hybrid_map:
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case R.id.satellite_map:
                map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                return true;
            case R.id.terrain_map:
                map.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onPause() {
        super.onPause();
        stopLocationMonitoring();
    }

    private void stopLocationMonitoring() {
        // Stop listening to Location changes
        unregisterLocalLocationReceiver();
        Intent locationServiceIntent = new Intent(context, locationListenerService.class);
        stopService(locationServiceIntent);
    }

    private void unregisterLocalLocationReceiver() {
        if (null != locationReceiver) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(locationReceiver);
        }
    }


// ************************************   EasyPermissions   ************************************

    private void getPermissions() {
        if (hasAppPermissions()) {
            Log.d(TAG, "appPermissionsTask");
            startLocationMonitoring();
        } else {
            EasyPermissions.requestPermissions(
                    this,
                    context.getString(R.string.must_garnt_permission),
                    RC_APP_PERMISSION,
                    appPermissions
            );
        }
    }

    private boolean hasAppPermissions() {
        return EasyPermissions.hasPermissions(this, appPermissions);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // EasyPermissions handles the request result.
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
        // Check if it not happen when permissions have been pre granted
        startLocationMonitoring();
    }

    @Override
    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            String yes = getString(R.string.yes);
            String no = getString(R.string.no);

            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(
                    this,
                    getString(R.string.returned_from_app_settings_to_activity,
                            hasAppPermissions() ? yes : no),
                    Toast.LENGTH_LONG)
                    .show();

            // Check if it not happen when permissions have been pre granted
        }
    }
}