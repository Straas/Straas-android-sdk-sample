package io.straas.android.media.demo;

import android.Manifest;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.media.session.MediaControllerCompat;
import android.widget.Toast;

import io.straas.android.sdk.media.StraasMediaCore;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

import static android.content.Context.LOCATION_SERVICE;

public class LocationCollector {

    public static final int REQUEST_CODE = OperationActivity.LOCATION_PERMISSION_REQUEST_CODE;

    public static final String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };

    private static final long MIN_INTERVAL = 1000;
    private static final float MIN_DISTANCE = 0.1f;

    private Context mContext;
    private MediaControllerCompat mMediaController;

    public LocationCollector(Context context, MediaControllerCompat mediaController) {
        mContext = context;
        mMediaController = mediaController;
    }

    public boolean checkPermission() {
        return EasyPermissions.hasPermissions(mContext, PERMISSIONS);
    }

    @AfterPermissionGranted(REQUEST_CODE)
    public void start() {
        if (!checkPermission()) {
            return;
        }

        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        String provider = null;
        if (isNetworkEnabled) {
            provider = LocationManager.NETWORK_PROVIDER;
        } else if (isGpsEnabled) {
            provider = LocationManager.GPS_PROVIDER;
        }
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, MIN_INTERVAL, MIN_DISTANCE, mLocationListener);
        }
    }

    public void stop() {
        LocationManager locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
        locationManager.removeUpdates(mLocationListener);
        setLocation(null);
    }

    private void showCurrentLocation() {
        mMediaController.sendCommand(StraasMediaCore.COMMAND_GET_LOCATION, null,
                new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        super.onReceiveResult(resultCode, resultData);
                        resultData.setClassLoader(Location.class.getClassLoader());
                        Location location = resultData.getParcelable(StraasMediaCore.KEY_LOCATION);
                        String text = null;
                        if (location != null) {
                            text = "Location: " + location.getLatitude() + ", " + location.getLongitude();
                        }
                        Toast.makeText(mContext, text, Toast.LENGTH_SHORT).show();
                    }
                });
    }

     private void setLocation(Location location) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(StraasMediaCore.KEY_LOCATION, location);
        mMediaController.getTransportControls().sendCustomAction(StraasMediaCore.COMMAND_SET_LOCATION, bundle);
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            setLocation(location);
            showCurrentLocation();
        }

        @Override public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override public void onProviderEnabled(String provider) {
        }

        @Override public void onProviderDisabled(String provider) {
        }
    };
}
