package cz.uhk.umte.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";

    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final float DEFAULT_ZOOM = 15f;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderCLient;

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        if(mLocationPermissionsGranted) {
            getDeviceLocation();

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getLocationPermissions();
    }

    private void initMap() {
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(MapActivity.this);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting current device location");
        mFusedLocationProviderCLient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderCLient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful() && task.getResult() != null){
                            Log.d(TAG,"onComplete: found location ");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG,"onComplete: current location is null ");
                            Toast.makeText(MapActivity.this, "Unable to get your location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SwecurityException: " + e.getMessage() );
        }
    }

    private void moveCamera (LatLng latLng, float zoom) {
        Log.d(TAG,"moveCamera: moving camera to: lat: "+ latLng.latitude + ", lng " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }
    private void getLocationPermissions() {
        Log.d(TAG, "getLocationPermissions: getting permisions");
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
        FINE_LOCATION) ==PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
            COARSE_LOCATION) ==PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permission,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, permission,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG,"onRequestPermissionsResult: called");
        mLocationPermissionsGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE :{
                if(grantResults.length > 0) {
                    for(int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG,"onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                }
                Log.d(TAG,"onRequestPermissionsResult: permission granted");
                mLocationPermissionsGranted = true;
                //initialize our map
                initMap();
            }
        }
    }

}
