package com.example.franj.retomaps;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private static final int REQUEST_CODE = 11;
    private Geocoder geocoder;
    private LocationManager manager;
    private Marker personalMarker, customPositionMarker;
    private LatLng personalPosition,customPosition;
    private List<Address> personalAddress,customAddress;
    private Location userLocation,customLocation;
    private TextView txt_Description;


    //public interface listener

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        manager = (LocationManager) getSystemService(LOCATION_SERVICE);
        geocoder = new Geocoder(this, Locale.getDefault());
        //solicitar permisos
        txt_Description = findViewById(R.id.txt_Description);

        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        }, REQUEST_CODE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11);
        } else {
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }

    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            userLocation=location;
            personalPosition = null;
            personalPosition = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLng(personalPosition));
            personalMarker.setPosition(personalPosition);
            try {
                personalAddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                String addres = personalAddress.get(0).getAddressLine(0).split(",")[0];
                personalMarker.setTitle(getString(R.string.userPosition)+addres);

            } catch (IOException e) {
                Toast.makeText(MapsActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
            }


        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMapLongClickListener(this);
        personalPosition = new LatLng(3.341757, -76.530808);

        try {
            personalAddress = geocoder.getFromLocation(3.341757,-76.530808,1);
            String addres = personalAddress.get(0).getAddressLine(0);
            personalMarker = mMap.addMarker(new MarkerOptions().position(personalPosition).title(getString(R.string.userPosition)+addres));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(personalPosition));
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 11) {
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, locationListener);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
        }
    }


    @Override
    public void onMapLongClick(LatLng latLng) {
        customPosition = latLng;
        customLocation= new Location("custom Location");
        customLocation.setLongitude(latLng.longitude);
        customLocation.setLatitude(latLng.latitude);
        double distance = userLocation.distanceTo(customLocation);
        try {
            customAddress = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            String addres = customAddress.get(0).getAddressLine(0).split(",")[0];
            if (customPositionMarker==null){
                customPositionMarker = mMap.addMarker(new MarkerOptions().position(customPosition).title(getString(R.string.userPosition)+addres));

            }
            else{
                customPositionMarker.setPosition(latLng);
                customPositionMarker.setTitle(getString(R.string.customPosition)+addres);
            }
            if (distance<50){
                txt_Description.setText(getString(R.string.currentPlace));
            }
            else{
                txt_Description.setText(getString(R.string.distance)+distance);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
