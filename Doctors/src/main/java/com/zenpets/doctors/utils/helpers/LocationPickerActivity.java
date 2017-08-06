package com.zenpets.doctors.utils.helpers;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.zenpets.doctors.utils.TypefaceSpan;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LocationPickerActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,
        GoogleMap.OnMarkerDragListener {

    /** A GOOGLE MAP INSTANCE **/
    private GoogleMap mMap;

    /** A GOOGLE API CLIENT INSTANCE **/
    GoogleApiClient mGoogleApiClient;

    /** A LOCATION REQUEST INSTANCE **/
    LocationRequest mLocationRequest;

    /** A LOCATION INSTANCE **/
    Location mLastLocation;

    /** A MARKER INSTANCE **/
    Marker mMarker;

    /** THE FINAL SELECTED COORDINATES **/
    LatLng selectedCoordinates;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zenpets.doctors.R.layout.location_picker);

        /***** CONFIGURE THE ACTIONBAR *****/
        configAB();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(com.zenpets.doctors.R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        mMap.setBuildingsEnabled(true);
        mMap.setOnMarkerDragListener(this);
//        mMap.setMyLocationEnabled(true);

        /** BUILD AND CONFIGURE THE GOOGLE API CLIENT **/
        buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient()  {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(com.zenpets.doctors.R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Location Picker";
//        String strTitle = getString(R.string.add_a_new_medicine_record);
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getApplicationContext()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(s);
        getSupportActionBar().setSubtitle(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(LocationPickerActivity.this);
        inflater.inflate(com.zenpets.doctors.R.menu.activity_save_cancel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent back = new Intent();
                setResult(RESULT_CANCELED, back);
                this.finish();
                break;
            case com.zenpets.doctors.R.id.menuSave:
                /** SEND THE COORDINATES TO THE CREATE ACCOUNT ACTIVITY **/
                Intent success = new Intent();
                Bundle bundle = new Bundle();
                bundle.putDouble("LATITUDE", selectedCoordinates.latitude);
                bundle.putDouble("LONGITUDE", selectedCoordinates.longitude);
                success.putExtras(bundle);
                setResult(RESULT_OK, success);
                finish();
                break;
            case com.zenpets.doctors.R.id.menuCancel:
                Intent cancel = new Intent();
                setResult(RESULT_CANCELED, cancel);
                this.finish();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        if (mMarker != null)    {
            mMarker.remove();
        }

        selectedCoordinates = new LatLng(location.getLatitude(), location.getLongitude());
        Toast.makeText(getApplicationContext(), "ORIGINAL COORDS: " + String.valueOf(selectedCoordinates), Toast.LENGTH_SHORT).show();
        MarkerOptions options = new MarkerOptions();
        options.position(selectedCoordinates);
        options.title("Current Position");
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMarker = mMap.addMarker(options);
        mMarker.showInfoWindow();
        mMarker.setDraggable(true);

        /** MOVE THE MAP CAMERA **/
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 18));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);

        if (mGoogleApiClient != null)   {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
    }

    @Override
    public void onMarkerDrag(Marker marker) {
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        selectedCoordinates = marker.getPosition();
        Toast.makeText(getApplicationContext(), "NEW COORDINATES: " + String.valueOf(selectedCoordinates), Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}