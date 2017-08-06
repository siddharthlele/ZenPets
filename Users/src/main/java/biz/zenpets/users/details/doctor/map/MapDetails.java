package biz.zenpets.users.details.doctor.map;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
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
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.helpers.directions.DataParser;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MapDetails extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /** THE INCOMING DATA **/
    private String DOCTOR_ID = null;
    private String DOCTOR_NAME = null;
    private String CLINIC_ID = null;
    private String CLINIC_NAME = null;
    private Double CLINIC_LATITUDE = null;
    private Double CLINIC_LONGITUDE = null;
    private String CLINIC_ADDRESS = null;

    /** A GOOGLE API CLIENT INSTANCE **/
    private GoogleApiClient mGoogleApiClient;

    /** A LOCATION REQUEST INSTANCE **/
    private LocationRequest mLocationRequest;

    /** A LOCATION INSTANCE **/
    private Location mLastLocation;

    /** A MARKER INSTANCE **/
    private Marker mMarker;

    /** THE ORIGIN AND DESTINATION COORDINATES (LatLng) **/
    private LatLng origin;
    private LatLng destination;

    /** DECLARE A GOOGLE MAP INSTANCE **/
    private GoogleMap clinicMap;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.txtClinicName) AppCompatTextView txtClinicName;
    @BindView(R.id.txtClinicAddress) AppCompatTextView txtClinicAddress;
    @BindView(R.id.txtDistance) AppCompatTextView txtDistance;

    /** SHOW THE DIRECTIONS IN GOOGLE MAP **/
    @OnClick(R.id.txtDirections) void showDirections() {
        if (isGoogleMapsInstalled())    {
            String strOrigin =
                    "saddr=" + Double.toString(origin.latitude) + "," + Double.toString(origin.longitude);

            String strDestination =
                    "&daddr=" + Double.toString(destination.latitude) + "," + Double.toString(destination.longitude);

            Intent intent = new Intent(
                    android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?" + strOrigin + strDestination + "&dirflg=d"));

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
            startActivity(intent);
        } else {
            new MaterialDialog.Builder(MapDetails.this)
                    .title("Google Maps Required")
                    .content("Google Maps is necessary to show you the directions to the Clinic. Please install Google Maps to use this feature")
                    .positiveText("OKAY")
                    .theme(Theme.LIGHT)
                    .icon(ContextCompat.getDrawable(MapDetails.this, R.drawable.ic_info_outline_black_24dp))
                    .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                    .show();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_details);
        ButterKnife.bind(this);

        /* CONFIGURE THE TOOLBAR */
        configTB();

        /* GET THE INCOMING DATA */
        getIncomingData();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.clinicMap);
        mapFragment.getMapAsync(this);
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

        origin = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions options = new MarkerOptions();
        options.position(origin);
//        options.title("Current Position");
        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mMarker = clinicMap.addMarker(options);
        mMarker.showInfoWindow();
        mMarker.setDraggable(true);

        /** MOVE THE MAP CAMERA **/
        clinicMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 10));
        clinicMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);

        destination = new LatLng(CLINIC_LATITUDE, CLINIC_LONGITUDE);
//        Log.e("CLINIC COORDS", String.valueOf(destination));
        MarkerOptions clinicOptions = new MarkerOptions();
        clinicOptions.position(destination);
        clinicOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        clinicMap.addMarker(clinicOptions);

        float distance[] = new float[1];
        Location.distanceBetween(origin.latitude, origin.longitude, destination.latitude, destination.longitude, distance);

        // Getting URL to the Google Directions API
        String url = getUrl(origin, destination);
//        Log.e("onMapClick", url.toString());
        FetchUrl FetchUrl = new FetchUrl();

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url);
        //move map camera
        clinicMap.moveCamera(CameraUpdateFactory.newLatLng(origin));
        clinicMap.animateCamera(CameraUpdateFactory.zoomTo(15));

        if (mGoogleApiClient != null)   {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /** DOWNLOAD THE URL TO FETCH THE DIRECTIONS **/
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuffer sb = new StringBuffer();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
//            Log.e("downloadUrl", data.toString());
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    /** FETCH DATA FROM THE DIRECTIONS URL **/
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
//                Log.e("Background Task data", data.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /** PARSE THE RESULT OF THE DIRECTIONS URL CALL **/
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        String distance = null;

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                DataParser parser = new DataParser();

                JSONArray array = jObject.getJSONArray("routes");
                JSONObject JORoutes = array.getJSONObject(0);
                JSONArray JOLegs= JORoutes.getJSONArray("legs");
                JSONObject JOSteps = JOLegs.getJSONObject(0);
                JSONObject JODistance = JOSteps.getJSONObject("distance");
                if (JODistance.has("text")) {
                    distance = JODistance.getString("text");
                }

                // Starts parsing data
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(10);
                lineOptions.color(Color.RED);
            }

            /** SET THE APPROXIMATE DISTANCE **/
            txtDistance.setText("~ " + distance);

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null) {
                clinicMap.addPolyline(lineOptions);
            } else {
//                Log.e("onPostExecute","without Polylines drawn");
            }
        }
    }

    /** CREATE THE DIRECTIONS URL **/
    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

//        Log.e("URL", "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters);

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        clinicMap = googleMap;
        clinicMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        clinicMap.setBuildingsEnabled(true);
        clinicMap.getUiSettings().setMapToolbarEnabled(false);

        /** SET THE MAP STYLE **/
        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.zen_map_style);
        clinicMap.setMapStyle(mapStyleOptions);

        /** BUILD AND CONFIGURE THE GOOGLE API CLIENT **/
        buildGoogleApiClient();
    }

    private synchronized void buildGoogleApiClient()  {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID")
                && bundle.containsKey("DOCTOR_NAME")
                && bundle.containsKey("CLINIC_ID")
                && bundle.containsKey("CLINIC_NAME")
                && bundle.containsKey("CLINIC_LATITUDE")
                && bundle.containsKey("CLINIC_LONGITUDE")
                && bundle.containsKey("CLINIC_ADDRESS")) {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            DOCTOR_NAME = bundle.getString("DOCTOR_NAME");
            CLINIC_ID = bundle.getString("CLINIC_ID");
            CLINIC_NAME = bundle.getString("CLINIC_NAME");
            CLINIC_LATITUDE = bundle.getDouble("CLINIC_LATITUDE");
            CLINIC_LONGITUDE = bundle.getDouble("CLINIC_LONGITUDE");
            CLINIC_ADDRESS = bundle.getString("CLINIC_ADDRESS");

            if (DOCTOR_NAME != null)    {
                txtDoctorName.setText(DOCTOR_NAME);
            }

            if (CLINIC_NAME != null)    {
                txtClinicName.setText(CLINIC_NAME);
            }

            if (CLINIC_ADDRESS != null) {
                txtClinicAddress.setText(CLINIC_ADDRESS);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
            return true;
        } catch(PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Doctor Details";
//        String strTitle = getString(R.string.add_a_new_medicine_record);
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getApplicationContext()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setSubtitle(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}