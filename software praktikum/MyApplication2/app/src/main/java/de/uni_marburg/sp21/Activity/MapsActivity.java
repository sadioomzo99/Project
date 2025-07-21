package de.uni_marburg.sp21.Activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;

import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import android.widget.Toast;


import com.example.sp21.R;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;



import static de.uni_marburg.sp21.Util.Constants.COURSE_LOCATION;
import static de.uni_marburg.sp21.Util.Constants.DEFAULT_ZOOM;
import static de.uni_marburg.sp21.Util.Constants.FiNE_LOCATION;
import static de.uni_marburg.sp21.Util.Constants.LAT;
import static de.uni_marburg.sp21.Util.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static de.uni_marburg.sp21.Util.Constants.LON;
import static de.uni_marburg.sp21.Util.Constants.MY_LOCATION;
import static de.uni_marburg.sp21.Util.Constants.NAME;
import static de.uni_marburg.sp21.Util.Constants.NO_CONNECTION;


/**
 * @author Oumar SADIO and Abdulmallek Ali
 *this Class is responsible for the Map
 */

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {


    private Boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    Location location;
    private FusedLocationProviderClient fusedLocationProviderClient;


    private LatLng latLng;
    double latitude;
    double longitude;
     String Name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);;
        getLocationPermission();

    }


    /**
     * provides the device Location if the LocationPermission are granted
     */

    private void getDeviceLocation() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                fusedLocationProviderClient.getLastLocation().addOnCompleteListener(this,new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            Log.d("MapActivity", "onComplete: found Location!");
                            location=task.getResult();
                            if (location != null && checkConn()) {
                                double lat = location.getLatitude();
                                double lon = location.getLongitude();
                                moveCamera(new LatLng(lat, lon),
                                        DEFAULT_ZOOM, MY_LOCATION);
                                mMap.addMarker(new MarkerOptions().position(new LatLng(lat,lon))
                                        .icon(BitmapDescriptorFactory
                                                .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                            }
                        } else {
//                            mMap.moveCamera(CameraUpdateFactory
//                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                            Toast.makeText(MapsActivity.this, "unable to get current location",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("MapActivity", "getDeviceLocation: SecurityException:" + e.getMessage());
        }
    }

    /**
     * responsible for checking the internet connection
     * @return true if there is internet connection and false if it is not the case
     */
   private boolean checkConn() {
       ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
       NetworkInfo wifi=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
       NetworkInfo mobile=connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
       if (wifi!= null && wifi.isConnected()||mobile!=null &&mobile.isConnected()) {
           return true;
       }else {
           return false;
       }
    }

    /**
     * responsible for moving the camera
     * @param latLng location
     * @param zoom the range of the camera
     * @param title name above the marker
     */
    private void moveCamera(LatLng latLng, float zoom, String title) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        MarkerOptions options = new MarkerOptions()
                .position(latLng).title(title);
        mMap.addMarker(options);
    }
    /**
     *  responsible for initializing the Map
     */

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapsActivity.this);

    }

    /**
     * checks if the user allowed the access to its current location
     * if allowed then LocationPermission is Granted and if there is internet connection the Map will be initialized
     * and the @method  getDeviceLocation will be triggered
     */

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FiNE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                if(checkConn()) {
                    initMap();
                    getDeviceLocation();
                }else {
                    Toast.makeText(MapsActivity.this,NO_CONNECTION,Toast.LENGTH_SHORT).show();
                }
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    //initialize map
                    initMap();
                }
            }
        }
    }

    /**
     * this method checks if the user allowed the application to use the location and if there is internet connection
     * else the @method  getLocationPermission will be triggered
     */

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted && checkConn()) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);

                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * it calls the @method updateLocationUI and getDeviceLocation
     * if there is some internet connection then run the loadCompanyLocation method
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        updateLocationUI();
        getDeviceLocation();
        if (checkConn()) {
            loadCompanyLocation();

        }
    }

    /**
     * The Methode retrieve Longitude and Latitude from DetailsAdapter,
     * and provide the location of Corresponding Company with Marker in Map.
     */
    private  void  loadCompanyLocation(){
        Intent prevIntent = getIntent();
            latitude = prevIntent.getDoubleExtra(LAT,-1);
            longitude = prevIntent.getDoubleExtra(LON,-1);
             Name=prevIntent.getStringExtra(NAME);
            latLng = new LatLng(latitude, longitude);
            moveCamera(latLng,DEFAULT_ZOOM,Name);
            mMap.addMarker(new MarkerOptions().position(latLng)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
    }
 }

