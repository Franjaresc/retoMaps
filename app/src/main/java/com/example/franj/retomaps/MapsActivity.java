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
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private GoogleMap mMap;
    private static final int REQUEST_CODE = 11;
    private Geocoder geocoder;
    private LocationManager manager;
    private Marker personalMarker, customPositionMarker;
    private LatLng personalPosition,customPosition;
    private ArrayList<LatLng> customPositions;
    private List<Address> personalAddress,customAddress;
    private Location userLocation,customLocation;
    private TextView txt_Description;
    private Button btn_SaveLocation,btn_ClearMarkers,btn_ShowSavedMarkers;
    private ArrayList<Marker> savedMarkers;





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
        savedMarkers=new ArrayList<>();
        customPositions = new ArrayList<>();
        txt_Description = findViewById(R.id.txt_Description);
        btn_SaveLocation = findViewById(R.id.btn_SaveLocation);
        btn_SaveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(customPosition!=null){
                    customPositions.add(customPosition);
                    getNearLocation();
                }
            }
        });
        btn_ClearMarkers=findViewById(R.id.btn_ClearMarkers);
        btn_ClearMarkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearAllSavePlaces();
            }
        });
        btn_ShowSavedMarkers = findViewById(R.id.btn_ShowSavedMarkers);
        btn_ShowSavedMarkers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAllSavePlaces();
            }
        });

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
            if (personalMarker==null){
                personalPosition = new LatLng(location.getLatitude(), location.getLongitude());

                try {
                    personalAddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    String addres = personalAddress.get(0).getAddressLine(0);
                    personalMarker = mMap.addMarker(new MarkerOptions().position(personalPosition).title(getString(R.string.userPosition)+addres));
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(personalPosition));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else{
                userLocation=location;
                personalPosition = null;
                personalPosition = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(personalPosition,15));
                personalMarker.setPosition(personalPosition);
                try {
                    personalAddress = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
                    String addres = personalAddress.get(0).getAddressLine(0).split(",")[0];
                    personalMarker.setTitle(getString(R.string.userPosition)+addres);

                } catch (IOException e) {
                    Toast.makeText(MapsActivity.this,e.getMessage(), Toast.LENGTH_LONG).show();
                }
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
        double distance = Math.round((userLocation.distanceTo(customLocation)*100))/100d;
        try {
            customAddress = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            String addres = customAddress.get(0).getAddressLine(0).split(",")[0];
            if (customPositionMarker==null){
                customPositionMarker = mMap.addMarker(new MarkerOptions().position(customPosition).title(getString(R.string.customPosition)+addres).snippet(getString(R.string.distance)+distance+" m").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            }
            else{
                customPositionMarker.setPosition(latLng);
                customPositionMarker.setTitle(getString(R.string.customPosition)+addres);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String getNearLocation(){
        String nearLocation="";
        List<Address> nearAddress=null;
        double distance = Double.MAX_VALUE;
        Location vLocation = new Location("variable location");
        try {
            for (int i = 0; i<customPositions.size();i++){
                vLocation.setLatitude(customPositions.get(i).latitude);
                vLocation.setLongitude(customPositions.get(i).longitude);
                double vDistance = Math.round((userLocation.distanceTo(vLocation)*100)/100d);
                if (vDistance<distance){
                    nearAddress = geocoder.getFromLocation(vLocation.getLatitude(),vLocation.getLongitude(),1);
                    distance=vDistance;
                }

            }
            if (nearAddress!=null){
                if (distance<100){
                    String addres = nearAddress.get(0).getAddressLine(0).split(",")[0];
                    txt_Description.setText(getString(R.string.currentPlace)+addres);
                }
                else{
                    String addres = nearAddress.get(0).getAddressLine(0).split(",")[0];
                    txt_Description.setText(getString(R.string.nearPlace)+addres);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }


        return nearLocation;
    }

    public void showAllSavePlaces(){
        List<Address> nearAddress;
        Location vLocation = new Location("variable location");
        try {
            for (int i = 0; i<customPositions.size();i++){
                nearAddress = geocoder.getFromLocation(customPositions.get(i).latitude,customPositions.get(i).longitude,1);
                vLocation.setLatitude(customPositions.get(i).latitude);
                vLocation.setLongitude(customPositions.get(i).longitude);
                double distance = Math.round((userLocation.distanceTo(vLocation)*100)/100d);
                String addres = nearAddress.get(0).getAddressLine(0).split(",")[0];
                Marker newMarker = mMap.addMarker(new MarkerOptions().position(customPositions.get(i)).icon((BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))).title(getString(R.string.customPosition)+addres).snippet(getString(R.string.distance)+distance+" m"));
                savedMarkers.add(newMarker);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }

    }

    public void clearAllSavePlaces(){
        for (int i = 0; i<savedMarkers.size();i++){
            savedMarkers.get(i).remove();
        }
    }

}
