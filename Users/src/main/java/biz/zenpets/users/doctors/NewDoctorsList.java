package biz.zenpets.users.doctors;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.doctors.manual.CitySelectorActivity;
import biz.zenpets.users.doctors.manual.CountrySelectorActivity;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.doctors.DoctorsListAdapter;
import biz.zenpets.users.utils.helpers.doctors.LocationHelper;
import biz.zenpets.users.utils.models.doctors.DoctorsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class NewDoctorsList extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /** STRING TO HOLD THE DETECTED CITY NAME AND LOCALITY FOR QUERYING THE DOCTORS INFORMATION **/
    private String DETECTED_CITY = null;
    private String FINAL_CITY_ID = null;
    private String DETECTED_LOCALITY = null;
    private String FINAL_LOCALITY_ID = null;

    /** A GOOGLE API CLIENT INSTANCE **/
    private GoogleApiClient mGoogleApiClient;

    /** THE USERS CURRENT COORDINATES **/
    private Location currentLocation;

    /** PLAY SERVICE REQUEST CODE **/
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    /** THE LOCATION SELECTOR REQUEST CODE **/
    private final static int LOCATION_SELECTOR_REQUEST = 101;

    /** PERMISSION REQUEST CONSTANTS **/
    private static final int ACCESS_FINE_LOCATION_CONSTANT = 200;

    /* BOOLEAN TO TRACK FETCH CITY AND LOCALITY ERROR */
    private boolean blnFetchCityError = false;
    private boolean blnFetchLocalityError = false;

    /** THE DOCTORS ADAPTER AND THE ARRAY LIST **/
    private DoctorsListAdapter adapter;
    private final ArrayList<DoctorsData> arrDoctors = new ArrayList<>();

    /** THE SPONSORED LISTING ADAPTER AND ARRAY LIST **/
//    SponsoredListingAdapter sponsoredListingAdapter;
//    ArrayList<DoctorsData> arrSponsored = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.mainContent) View mLayout;
    @BindView(R.id.txtLocation) AppCompatTextView txtLocation;
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
//    @BindView(R.id.listSponsored) RecyclerView listSponsored;
    @BindView(R.id.listDoctors) RecyclerView listDoctors;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** SHOW THE FILTERS **/
//    @OnClick(R.id.fabFilter) void showFilters() {
//        Intent intent = new Intent(this, FilterDoctorsActivity.class);
//        startActivityForResult(intent, 101);
//    }

    /** LOCATION SELECTOR **/
    @OnClick(R.id.linlaLocationSelector) void locationSelector()    {

        /* FETCH THE LOCATION MANUALLY */
        fetchLocationManually();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctors_list);
        ButterKnife.bind(this);

        /** CONFIGURE THE ACTIONBAR **/
        configAB();

        /* GET TODAY'S DAY */
//        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
//        Date d = new Date();
//        String TODAY_DAY = sdf.format(d);
//        Log.e("TODAY", TODAY_DAY);

        /** CHECK PLAY SERVICES AVAILABILITY **/
        if (checkPlayServices()) {
            /** CONFIGURE THE GOOGLE API CLIENT **/
            buildGoogleApiClient();
        }

        /** INSTANTIATE THE ADAPTER **/
        adapter = new DoctorsListAdapter(NewDoctorsList.this, arrDoctors);

        /** CONFIGURE THE RECYCLER VIEW **/
        configRecycler();
    }

    /***** FETCH THE LIST OF DOCTORS *****/
    private class fetchDoctorsList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTORS_LIST = "http://leodyssey.com/ZenPets/public/fetchDoctors";
            String URL_DOCTORS_LIST = "http://192.168.11.2/zenpets/public/fetchDoctors";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTORS_LIST).newBuilder();
            builder.addQueryParameter("cityID", FINAL_CITY_ID);
            builder.addQueryParameter("localityID", FINAL_LOCALITY_ID);
            String FINAL_URL = builder.build().toString();
//            Log.e("URL", FINAL_URL);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(FINAL_URL)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JAClinics = JORoot.getJSONArray("doctors");
                    if (JAClinics.length() > 0) {

                        /* A DOCTORS DATA INSTANCE */
                        DoctorsData data;

                        for (int i = 0; i < JAClinics.length(); i++) {
                            JSONObject JOClinics = JAClinics.getJSONObject(i);

                            /* INSTANTIATE THE DOCTORS DATA INSTANCE */
                            data = new DoctorsData();

                            /* GET THE CLINIC ID */
                            String clinicID = JOClinics.getString("clinicID");
                            data.setClinicID(clinicID);

                            /* GET THE CLINIC NAME */
                            String clinicName = JOClinics.getString("clinicName");
                            data.setClinicName(clinicName);

                            /* GET THE CLINIC ADDRESS */
                            String clinicAddress = JOClinics.getString("clinicAddress");
                            data.setClinicAddress(clinicAddress);

                            /* GET THE CLINIC LATITUDE AND LONGITUDE */
                            String clinicLatitude = JOClinics.getString("clinicLatitude");
                            String clinicLongitude = JOClinics.getString("clinicLongitude");
                            if (clinicLatitude != null
                                    && !clinicLatitude.equalsIgnoreCase("0")
                                    && clinicLongitude != null
                                    && !clinicLongitude.equalsIgnoreCase("0"))    {

                                Double latitude = Double.valueOf(clinicLatitude);
                                Double longitude = Double.valueOf(clinicLongitude);
                                Location clinicLocation = new Location("Clinic");
                                clinicLocation.setLatitude(latitude);
                                clinicLocation.setLongitude(longitude);

                                /* CALCULATE THE DISTANCE */
                                float distance = currentLocation.distanceTo(clinicLocation) / 1000;

                                /* ROUND UP THE DISTANCE TO 3 DECIMALS */
                                float finalDistance = roundUpDistance(distance, 2);
                                data.setClinicDistance(String.valueOf(finalDistance));
                            } else {
                                data.setClinicDistance("Unknown");
                            }

                            /* GET THE DOCTOR ID */
                            String doctorID = JOClinics.getString("doctorID");
                            data.setDoctorID(doctorID);

                            /* GET THE DOCTOR'S NAME */
                            String doctorPrefix = JOClinics.getString("doctorPrefix");
                            String doctorName = JOClinics.getString("doctorName");
                            data.setDoctorName(doctorPrefix + " " + doctorName);

                            /* GET THE DOCTOR'S DISPLAY PROFILE */
                            String doctorDisplayProfile = JOClinics.getString("doctorDisplayProfile");
                            data.setDoctorDisplayProfile(doctorDisplayProfile);

                           /* GET THE DOCTOR'S EXPERIENCE */
                            String doctorExperience = JOClinics.getString("doctorExperience");
                            data.setDoctorExperience(doctorExperience);

                            /* GET THE CURRENCY CODE AND THE DOCTOR'S CHARGES */
                            String currencySymbol = JOClinics.getString("currencySymbol");
                            String doctorCharges = JOClinics.getString("doctorCharges");
                            data.setDoctorCharges(currencySymbol + " " + doctorCharges);

                            /* THE TOTAL VOTES, TOTAL LIKES AND TOTAL DISLIKES */
                            int TOTAL_VOTES = 0;
                            int TOTAL_LIKES = 0;

                            /* GET THE POSITIVE REVIEWS / FEEDBACK FOR THE DOCTORS */
                            // String URL_POSITIVE_REVIEWS = "http://leodyssey.com/ZenPets/public/fetchPositiveReviews";
                            String URL_POSITIVE_REVIEWS = "http://192.168.11.2/zenpets/public/fetchPositiveReviews";
                            HttpUrl.Builder builderPositive = HttpUrl.parse(URL_POSITIVE_REVIEWS).newBuilder();
                            builderPositive.addQueryParameter("doctorID", doctorID);
                            builderPositive.addQueryParameter("recommendStatus", "Yes");
                            String FINAL_URL_POSITIVE = builderPositive.build().toString();
                            OkHttpClient clientPositive = new OkHttpClient();
                            Request requestPositive = new Request.Builder()
                                    .url(FINAL_URL_POSITIVE)
                                    .build();
                            Call callPositive = clientPositive.newCall(requestPositive);
                            Response responsePositive = callPositive.execute();
                            String strPositiveReview = responsePositive.body().string();
                            JSONObject JORootPositive = new JSONObject(strPositiveReview);
                            if (JORootPositive.has("error") && JORootPositive.getString("error").equalsIgnoreCase("false")) {
                                JSONArray JAPositiveReviews = JORootPositive.getJSONArray("reviews");
                                TOTAL_LIKES = JAPositiveReviews.length();
                                TOTAL_VOTES = TOTAL_VOTES + JAPositiveReviews.length();
                            }

                            /* GET THE POSITIVE REVIEWS / FEEDBACK FOR THE DOCTORS */
                            // String URL_DOCTOR_REVIEWS = "http://leodyssey.com/ZenPets/public/fetchNegativeReviews";
                            String URL_DOCTOR_REVIEWS = "http://192.168.11.2/zenpets/public/fetchNegativeReviews";
                            HttpUrl.Builder builderNegative = HttpUrl.parse(URL_DOCTOR_REVIEWS).newBuilder();
                            builderNegative.addQueryParameter("doctorID", doctorID);
                            builderNegative.addQueryParameter("recommendStatus", "No");
                            String FINAL_URL_Negative = builderNegative.build().toString();
                            OkHttpClient clientNegative = new OkHttpClient();
                            Request requestReviews = new Request.Builder()
                                    .url(FINAL_URL_Negative)
                                    .build();
                            Call reviewCall = clientNegative.newCall(requestReviews);
                            Response responseNegative = reviewCall.execute();
                            String strNegativeReview = responseNegative.body().string();
                            JSONObject JORootNegative = new JSONObject(strNegativeReview);
                            if (JORootNegative.has("error") && JORootNegative.getString("error").equalsIgnoreCase("false")) {
                                JSONArray JANegativeReviews = JORootNegative.getJSONArray("reviews");
                                TOTAL_VOTES = TOTAL_VOTES + JANegativeReviews.length();
                            }

                            /** GET THE TOTAL LIKES **/
                            data.setDoctorLikes(String.valueOf(TOTAL_LIKES));

                            /** CALCULATE THE PERCENTAGE OF LIKES **/
                            double percentLikes = ((double)TOTAL_LIKES / TOTAL_VOTES) * 100;
                            int finalPercentLikes = (int)percentLikes;
                            data.setDoctorLikesPercent(String.valueOf(finalPercentLikes) + "%");

                            /** GET THE TOTAL NUMBER OF REVIEWS / VOTES **/
                            Resources resReviews = getResources();
                            String reviewQuantity = null;
                            if (TOTAL_VOTES == 0)   {
                                reviewQuantity = resReviews.getQuantityString(R.plurals.votes, TOTAL_VOTES, TOTAL_VOTES);
                            } else if (TOTAL_VOTES == 1)    {
                                reviewQuantity = resReviews.getQuantityString(R.plurals.votes, TOTAL_VOTES, TOTAL_VOTES);
                            } else if (TOTAL_VOTES > 1) {
                                reviewQuantity = resReviews.getQuantityString(R.plurals.votes, TOTAL_VOTES, TOTAL_VOTES);
                            }
                            data.setDoctorVotes(reviewQuantity);

                            /* ADD THE GATHERED DATA TO THE ARRAY LIST */
                            arrDoctors.add(data);

                            /* SHOW THE RECYCLER AND HIDE THE EMPTY LAYOUT */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listDoctors.setVisibility(View.VISIBLE);
                                    linlaEmpty.setVisibility(View.GONE);
                                }
                            });
                        }
                    } else {
                        /* HIDE THE RECYCLER AND SHOW THE EMPTY LAYOUT */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listDoctors.setVisibility(View.GONE);
                                linlaEmpty.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } else {
                    /* HIDE THE RECYCLER AND SHOW THE EMPTY LAYOUT */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            listDoctors.setVisibility(View.GONE);
                            linlaEmpty.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* INSTANTIATE THE DOCTORS ADAPTER */
            adapter = new DoctorsListAdapter(NewDoctorsList.this, arrDoctors);

            /* SET THE DOCTORS RECYCLER VIEW */
            listDoctors.setAdapter(adapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /** ROUND UP THE DISTANCE **/
    private static float roundUpDistance(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Find & Book";
//        String strTitle = getString(R.string.add_a_new_pet);
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
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == LOCATION_SELECTOR_REQUEST)    {

            Bundle bundle = data.getExtras();
            DETECTED_LOCALITY = bundle.getString("LOCALITY_NAME");
//            Log.e("DETECTED LOCALITY", DETECTED_LOCALITY);

            /* SET THE LOCATION */
            txtLocation.setText(DETECTED_LOCALITY + ", " + DETECTED_CITY);

            /* CLEAR THE ARRAY LIST */
            arrDoctors.clear();

            /* GET THE CITY ID */
            new fetchCityID().execute();
        }
    }

    /***** VERIFY GOOGLE PLAY SERVICES AVAILABLE ON THE DEVICE *****/
    private boolean checkPlayServices() {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int result = availability.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            if (availability.isUserResolvableError(result)) {
                availability.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(), "This device is not supported.", Toast.LENGTH_LONG) .show();
            }
            return false;
        }
        return true;
    }

    /***** CREATE AND INSTANTIATE THE GOOGLE API CLIENT INSTANCE *****/
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
    
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        /* CHECK FOR PERMISSION STATUS */
        if (ContextCompat.checkSelfPermission(NewDoctorsList.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)   {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION))    {
                /** SHOW THE DIALOG **/
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setIcon(R.drawable.ic_info_outline_black_24dp);
                builder.setTitle("Permission Required");
                builder.setMessage("\nZen Pets requires the Location Permission to show you the appropriate listings near / at your location. \n\nFor a seamless experience, we recommend granting Zen Pets this permission.");
                builder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(
                                NewDoctorsList.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Not Now", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_FINE_LOCATION_CONSTANT);
            }
        } else {
            /* INSTANTIATE A LOCATION REQUEST */
            fetchLocation();
        }
    }

    /***** INSTANTIATE A LOCATION REQUEST *****/
    @SuppressWarnings("MissingPermission")
    private void fetchLocation() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == ACCESS_FINE_LOCATION_CONSTANT)   {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)    {
                /* INSTANTIATE A LOCATION REQUEST */
                fetchLocation();
            } else {
                /** SHOW THE DIALOG **/
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(false);
                builder.setIcon(R.drawable.ic_info_outline_black_24dp);
                builder.setTitle("Select Location");
                builder.setMessage("\nSince the location permission was denied, Zen Pets requires you to manually select your location to display the relevant information.\n\nThe next few screens will prompt you to select the Country, State, City and locality.");
                builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(NewDoctorsList.this, CountrySelectorActivity.class);
                        startActivityForResult(intent, 102);
                    }
                });
                builder.setNegativeButton("Nevermind", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        finish();
                    }
                });
                builder.show();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("CONNECTION FAILED", String.valueOf(connectionResult.getErrorCode()));
    }

    @Override
    public void onLocationChanged(Location location) {
        currentLocation = location;
        currentLocation.setLatitude(location.getLatitude());
        currentLocation.setLongitude(location.getLongitude());

        Double lat = 18.5158916;
        Double lng = 73.8351813;

        Geocoder geocoder = new Geocoder(NewDoctorsList.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
//            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses.size() > 0)   {
                DETECTED_CITY = addresses.get(0).getLocality();
                DETECTED_LOCALITY = addresses.get(0).getSubLocality();
//                Log.e("LOCALITY", DETECTED_LOCALITY);
                Toast.makeText(getApplicationContext(), "LOCALITY: " + DETECTED_LOCALITY, Toast.LENGTH_LONG).show();

                if (DETECTED_CITY != null)  {
                    if (!DETECTED_CITY.equalsIgnoreCase("null")) {
                        if (DETECTED_LOCALITY != null)  {
                            if (!DETECTED_LOCALITY.equalsIgnoreCase("null"))   {
                                /* GET THE CITY ID */
                                new fetchCityID().execute();

                               /* SET THE LOCATION */
                                txtLocation.setText(DETECTED_LOCALITY + ", " + DETECTED_CITY);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            new MaterialDialog.Builder(NewDoctorsList.this)
                    .title("Location Error")
                    .content("Zen pets failed to detect your location. \nClick the \"Select Manually\" button and select your current location (locality)")
                    .positiveText("Select Manually")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            /* FETCH THE LOCATION MANUALLY */
                            fetchLocationManually();
                        }
                    })
                    .theme(Theme.LIGHT)
                    .icon(ContextCompat.getDrawable(NewDoctorsList.this, R.drawable.ic_info_outline_black_24dp))
                    .typeface("RobotoCondensed-Regular.ttf", "Roboto-Regular.ttf")
                    .show();
        }

        if (mGoogleApiClient != null)   {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /** DETECT THE CITY MANUALLY **/
    private class fetchCity extends AsyncTask<Location, Void, Void>  {

        @Override
        protected Void doInBackground(Location... params) {
            Location location = params[0];
            if (location != null)   {
//                Log.e("LAT", String.valueOf(location.getLatitude()));
//                Log.e("LON", String.valueOf(location.getLongitude()));

                try {
                    Geocoder gcd = new Geocoder(NewDoctorsList.this, Locale.getDefault());
                    List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    if (addresses.size() > 0) {
                        DETECTED_CITY = addresses.get(0).getLocality();
//                        Log.e("DETECTED CITY", DETECTED_CITY);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (DETECTED_CITY != null)  {
                Intent intent = new Intent(NewDoctorsList.this, CitySelectorActivity.class);
                intent.putExtra("CITY_NAME", DETECTED_CITY);
                startActivityForResult(intent, LOCATION_SELECTOR_REQUEST);
            } else {
                Toast.makeText(getApplicationContext(), "Failed to detect your location. Please try again", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /***** FETCH THE CITY ID *****/
    private class fetchCityID extends AsyncTask<Void, Void, Void>   {

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_CITY_ID = "http://leodyssey.com/ZenPets/public/getCityID";
            String URL_CITY_ID = "http://192.168.11.2/zenpets/public/getCityID";
            HttpUrl.Builder builder = HttpUrl.parse(URL_CITY_ID).newBuilder();
            builder.addQueryParameter("cityName", DETECTED_CITY);
            String FINAL_URL = builder.build().toString();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(FINAL_URL)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    String city = JORoot.getString("city");
                    if (city.equalsIgnoreCase("") || city.equalsIgnoreCase("null") || city.equalsIgnoreCase("[]")) {
                        /* ERROR FETCHING RESULT */
                        blnFetchCityError = true;
                    } else {
                        JSONArray JACity = JORoot.getJSONArray("city");

                        for (int i = 0; i < JACity.length(); i++) {
                            JSONObject JOCity = JACity.getJSONObject(i);

                            /* GET THE CITY ID */
                            if (JOCity.has("cityID"))   {
                                FINAL_CITY_ID = JOCity.getString("cityID");
                            }  else {
                                /* ERROR FETCHING RESULT */
                                blnFetchCityError = true;
                            }
                        }
                        /* NO ERRORS DETECTED */
                        blnFetchCityError = false;
                    }
                } else {
                    /* ERROR FETCHING RESULT */
                    blnFetchCityError = true;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (blnFetchCityError)   {
                /* SHOW THE ERROR MESSAGE */
                new MaterialDialog.Builder(NewDoctorsList.this)
                        .title("Location not Served!")
                        .content("There was a problem fetching Doctors near or at your current location. ")
                        .positiveText("OKAY")
                        .theme(Theme.LIGHT)
                        .icon(ContextCompat.getDrawable(NewDoctorsList.this, R.drawable.ic_info_outline_black_24dp))
                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                        .show();
            } else {
                /* GET THE LOCALITY ID */
                new fetchLocalityID().execute();
            }
        }
    }

    /***** FETCH THE LOCALITY ID *****/
    private class fetchLocalityID extends AsyncTask<Void, Void, Void>   {

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_LOCALITY_ID = "http://leodyssey.com/ZenPets/public/getLocalityID";
            String URL_LOCALITY_ID = "http://192.168.11.2/zenpets/public/getLocalityID";
            HttpUrl.Builder builder = HttpUrl.parse(URL_LOCALITY_ID).newBuilder();
            builder.addQueryParameter("localityName", DETECTED_LOCALITY);
            String FINAL_URL = builder.build().toString();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(FINAL_URL)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false"))  {
                    String locality = JORoot.getString("locality");
                    if (locality.equalsIgnoreCase("") || locality.equalsIgnoreCase("null") || locality.equalsIgnoreCase("[]")) {
                        /* ERROR FETCHING RESULT */
                        blnFetchLocalityError = true;
                    } else {
                        JSONArray JALocality = JORoot.getJSONArray("locality");

                        for (int i = 0; i < JALocality.length(); i++) {
                            JSONObject JOLocality = JALocality.getJSONObject(i);

                            /* GET THE CITY ID */
                            if (JOLocality.has("localityID"))   {
                                FINAL_LOCALITY_ID = JOLocality.getString("localityID");
                            }  else {
                                /* ERROR FETCHING RESULT */
                                blnFetchLocalityError = true;
                            }
                        }

                        /* NO ERRORS DETECTED */
                        blnFetchLocalityError = false;
                    }
                } else {
                    /* ERROR FETCHING RESULT */
                    blnFetchLocalityError = true;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (blnFetchLocalityError)   {

                /* SHOW THE ERROR MESSAGE */
                new MaterialDialog.Builder(NewDoctorsList.this)
                        .title("Location not Served!")
                        .content("There was a problem fetching Doctors near or at your current location. ")
                        .positiveText("OKAY")
                        .theme(Theme.LIGHT)
                        .icon(ContextCompat.getDrawable(NewDoctorsList.this, R.drawable.ic_info_outline_black_24dp))
                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                        .show();
            } else {
                /* FETCH THE LIST OF DOCTORS */
                new fetchDoctorsList().execute();
            }
        }
    }

    /***** FETCH THE LOCATION MANUALLY *****/
    private void fetchLocationManually() {
        LocationHelper locationHelper = new LocationHelper();
        locationHelper.getLocation(NewDoctorsList.this, new LocationHelper.LocationResult() {
            @Override
            public void gotLocation(Location location) {
                if (location != null)   {
                    new fetchCity().execute(location);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to detect your location. Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listDoctors.setLayoutManager(manager);
        listDoctors.setHasFixedSize(true);
        listDoctors.setAdapter(adapter);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}