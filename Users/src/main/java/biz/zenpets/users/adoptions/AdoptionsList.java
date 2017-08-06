package biz.zenpets.users.adoptions;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.adoptions.AdoptionsAdapter;
import biz.zenpets.users.utils.models.adoptions.AdoptionsData;
import biz.zenpets.users.utils.models.adoptions.AdoptionsImageData;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AdoptionsList extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    /** STRING TO HOLD THE DETECTED CITY NAME AND LOCALITY FOR QUERYING THE DOCTORS INFORMATION **/
    private String DETECTED_CITY = null;
    private String FINAL_CITY_ID = null;

    /** A GOOGLE API CLIENT INSTANCE **/
    private GoogleApiClient mGoogleApiClient;

    /** THE USERS CURRENT COORDINATES **/
    private Location currentLocation;

    /** PLAY SERVICE REQUEST CODE **/
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;

    /* BOOLEAN TO TRACK FETCH CITY AND LOCALITY ERROR */
    private boolean blnFetchCityError = false;

    /** THE ADOPTION ADAPTER AND ARRAY LISTS **/
    private AdoptionsAdapter adoptionsAdapter;
    private final ArrayList<AdoptionsData> arrAdoptions = new ArrayList<>();
    private ArrayList<AdoptionsImageData> arrImages = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.txtLocation) AppCompatTextView txtLocation;
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listAdoptions) RecyclerView listAdoptions;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adoptions_list);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* INSTANTIATE THE ADAPTER */
        adoptionsAdapter = new AdoptionsAdapter(AdoptionsList.this, arrAdoptions);

        /* CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /* CHECK PLAY SERVICES AVAILABILITY */
        if (checkPlayServices()) {
            /* CONFIGURE THE GOOGLE API CLIENT */
            buildGoogleApiClient();
        }
    }

    /***** FETCH THE LIST OF ADOPTIONS *****/
    private class fetchAdoptionsList extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_ADOPTIONS = "http://leodyssey.com/ZenPets/public/fetchAdoptions";
            String URL_USER_ADOPTIONS = "http://192.168.11.2/zenpets/public/fetchAdoptions";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_ADOPTIONS).newBuilder();
            builder.addQueryParameter("cityID", FINAL_CITY_ID);
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
                    JSONArray JAAdoptions = JORoot.getJSONArray("adoptions");
                    if (JAAdoptions.length() > 0) {
                        /* AN INSTANCE OF THE ADOPTIONS DATA MODEL */
                        AdoptionsData data;
                        for (int i = 0; i < JAAdoptions.length(); i++) {
                            JSONObject JOAdoptions = JAAdoptions.getJSONObject(i);
//                            Log.e("ADOPTION", String.valueOf(JOAdoptions));

                            /* INSTANTIATE THE ADOPTIONS DATA INSTANCE */
                            data = new AdoptionsData();

                            /* GET THE ADOPTION ID */
                            if (JOAdoptions.has("adoptionID"))  {
                                data.setAdoptionID(JOAdoptions.getString("adoptionID"));
                            } else {
                                data.setAdoptionID(null);
                            }

                            /* GET THE PET TYPE ID */
                            if (JOAdoptions.has("petTypeID"))   {
                                data.setPetTypeID(JOAdoptions.getString("petTypeID"));
                            } else {
                                data.setPetTypeID(null);
                            }

                            /* GET THE PET TYPE NAME */
                            if (JOAdoptions.has("petTypeName")) {
                                data.setPetTypeName(JOAdoptions.getString("petTypeName"));
                            } else {
                                data.setPetTypeName(null);
                            }

                            /* GET THE BREED ID */
                            if (JOAdoptions.has("breedID")) {
                                data.setBreedID(JOAdoptions.getString("breedID"));
                            } else {
                                data.setBreedID(null);
                            }

                            /* SET THE BREED NAME */
                            if (JOAdoptions.has("breedName"))   {
                                data.setBreedName(JOAdoptions.getString("breedName"));
                            } else {
                                data.setBreedName(null);
                            }

                            /* SET THE USER ID */
                            if (JOAdoptions.has("userID"))  {
                                data.setUserID(JOAdoptions.getString("userID"));
                            } else {
                                data.setUserID(null);
                            }

                            /* GET THE CITY ID */
                            if (JOAdoptions.has("cityID"))  {
                                data.setCityID(JOAdoptions.getString("cityID"));
                            } else {
                                data.setCityID(null);
                            }

                            /* GET THE CITY NAME */
                            if (JOAdoptions.has("cityName"))    {
                                data.setCityName(JOAdoptions.getString("cityName"));
                            } else {
                                data.setCityName(null);
                            }

                            /* GET THE ADOPTION NAME */
                            if (JOAdoptions.has("adoptionName") && !JOAdoptions.getString("adoptionName").equalsIgnoreCase("null")) {
                                data.setAdoptionName(JOAdoptions.getString("adoptionName"));
                            } else {
                                data.setAdoptionName(null);
                            }

                            /* GET THE ADOPTION DESCRIPTION */
                            if (JOAdoptions.has("adoptionDescription")) {
                                data.setAdoptionDescription(JOAdoptions.getString("adoptionDescription"));
                            } else {
                                data.setAdoptionDescription(null);
                            }

                            /* GET THE GENDER */
                            if (JOAdoptions.has("adoptionGender"))  {
                                data.setAdoptionGender(JOAdoptions.getString("adoptionGender"));
                            } else {
                                data.setAdoptionGender(null);
                            }

                            /* GET THE VACCINATION STATUS */
                            if (JOAdoptions.has("adoptionVaccination")) {
                                data.setAdoptionVaccination(JOAdoptions.getString("adoptionVaccination"));
                            } else {
                                data.setAdoptionVaccination(null);
                            }

                            /* GET THE DEWORMED STATUS */
                            if (JOAdoptions.has("adoptionDewormed"))    {
                                data.setAdoptionDewormed(JOAdoptions.getString("adoptionDewormed"));
                            } else {
                                data.setAdoptionDewormed(null);
                            }

                            /* GET THE NEUTERED STATUS */
                            if (JOAdoptions.has("adoptionNeutered"))    {
                                data.setAdoptionNeutered(JOAdoptions.getString("adoptionNeutered"));
                            } else {
                                data.setAdoptionNeutered(null);
                            }

                            /* GET THE TIME STAMP */
                            if (JOAdoptions.has("adoptionTimeStamp"))   {
                                String adoptionTimeStamp = JOAdoptions.getString("adoptionTimeStamp");
                                long lngTimeStamp = Long.parseLong(adoptionTimeStamp) * 1000;
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(lngTimeStamp);
                                Date date = calendar.getTime();
                                PrettyTime prettyTime = new PrettyTime();
                                String strDate = prettyTime.format(date);
                                data.setAdoptionTimeStamp(strDate);
                            } else {
                                data.setAdoptionTimeStamp(null);
                            }

                            /* GET THE ADOPTION STATUS */
                            if (JOAdoptions.has("adoptionStatus"))  {
                                data.setAdoptionStatus(JOAdoptions.getString("adoptionStatus"));
                            } else {
                                data.setAdoptionStatus(null);
                            }

                            /* GET THE ADOPTION IMAGES */
                            // String URL_ADOPTION_IMAGES = "http://leodyssey.com/ZenPets/public/fetchAdoptionImages";
                            String URL_ADOPTION_IMAGES = "http://192.168.11.2/zenpets/public/fetchAdoptionImages";
                            HttpUrl.Builder builderImages = HttpUrl.parse(URL_ADOPTION_IMAGES).newBuilder();
                            builderImages.addQueryParameter("adoptionID", JOAdoptions.getString("adoptionID"));
                            String FINAL_IMAGES_URL = builderImages.build().toString();
                            OkHttpClient clientImages = new OkHttpClient();
                            Request requestImages = new Request.Builder()
                                    .url(FINAL_IMAGES_URL)
                                    .build();
                            Call callImages = clientImages.newCall(requestImages);
                            Response responseImages = callImages.execute();
                            String strResultImages = responseImages.body().string();
                            JSONObject JORootImages = new JSONObject(strResultImages);
                            if (JORootImages.has("error") && JORootImages.getString("error").equalsIgnoreCase("false")) {
                                JSONArray JAImages = JORootImages.getJSONArray("images");
                                /* AN INSTANCE OF THE ADOPTION IMAGES DATA CLASS */
                                AdoptionsImageData imageData;

                                for (int j = 0; j < JAImages.length(); j++) {
                                    JSONObject JOImages = JAImages.getJSONObject(j);

                                    /* INSTANTIATE THE ADOPTION IMAGES DATA */
                                    imageData = new AdoptionsImageData();

                                    /* GET THE IMAGE ID */
                                    if (JOImages.has("imageID"))    {
                                        imageData.setImageID(JOImages.getString("imageID"));
                                    } else {
                                        imageData.setImageID(null);
                                    }

                                    /* GET THE ADOPTION ID */
                                    if (JOImages.has("adoptionID")) {
                                        imageData.setAdoptionID(JOImages.getString("adoptionID"));
                                    } else {
                                        imageData.setAdoptionID(null);
                                    }

                                    /* GET THE IMAGE URL */
                                    if (JOImages.has("imageURL"))   {
                                        imageData.setImageURL(JOImages.getString("imageURL"));
                                    } else {
                                        imageData.setImageURL(null);
                                    }

                                    /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                                    arrImages.add(imageData);
                                }
                                data.setImages(arrImages);
                                arrImages = new ArrayList<>();
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrAdoptions.add(data);
                        }

                        /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY LAYOUT */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAdoptions.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listAdoptions.setVisibility(View.GONE);
                            }
                        });
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            /* INSTANTIATE THE ADOPTIONS ADAPTER */
            adoptionsAdapter = new AdoptionsAdapter(AdoptionsList.this, arrAdoptions);

            /* SET THE ADAPTER TO THE ADOPTIONS RECYCLER VIEW */
            listAdoptions.setAdapter(adoptionsAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Find & Adopt";
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
        /* A LOCATION REQUEST INSTANCE */
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        int hasLocationPermission = ContextCompat.checkSelfPermission(
                AdoptionsList.this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(
                    AdoptionsList.this, Manifest.permission.ACCESS_FINE_LOCATION))   {
                new MaterialDialog.Builder(AdoptionsList.this)
                        .title("Permission Required!")
                        .content("The application needs the Location permission to be granted to show you the appropriate list of Doctors available near you. To determine that, Zen Pets needs to know your current location. \nPress the \"Okay\" button to grant access now. Press the \"Cancel\" button to close this screen.")
                        .positiveText("OKAY")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                ActivityCompat.requestPermissions(AdoptionsList.this,
                                        new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                                        101);
                            }
                        })
                        .negativeText("CANCEL")
                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                                finish();
                            }
                        })
                        .theme(Theme.LIGHT)
                        .icon(ContextCompat.getDrawable(AdoptionsList.this, R.drawable.ic_info_outline_black_24dp))
                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                        .show();
            }
            ActivityCompat.requestPermissions(AdoptionsList.this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    101);
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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

        Geocoder geocoder = new Geocoder(AdoptionsList.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 1);
            if (addresses.size() > 0)   {
                DETECTED_CITY = addresses.get(0).getLocality();
//                Log.e("CITY", DETECTED_CITY);

                if (DETECTED_CITY != null || !DETECTED_CITY.equalsIgnoreCase("null")) {
                    /* GET THE CITY ID */
                    new fetchCityID().execute();

                    /* SET THE LOCATION */
                    txtLocation.setText(DETECTED_CITY);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("EXCEPTION", e.getMessage());
        }

        if (mGoogleApiClient != null)   {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    /***** GET THE CITY ID *****/
    private class fetchCityID extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

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
                    if (city.equalsIgnoreCase("") || city == null || city.equalsIgnoreCase("null") || city.equalsIgnoreCase("[]")) {
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

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);

            if (blnFetchCityError)   {
                /* SHOW THE ERROR MESSAGE */
                new MaterialDialog.Builder(AdoptionsList.this)
                        .title("Location not Served!")
                        .content("There was a problem fetching Doctors near or at your current location. ")
                        .positiveText("OKAY")
                        .theme(Theme.LIGHT)
                        .icon(ContextCompat.getDrawable(AdoptionsList.this, R.drawable.ic_info_outline_black_24dp))
                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                        .show();
            } else {
                /* FETCH THE LIST OF ADOPTIONS */
                new fetchAdoptionsList().execute();
            }
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listAdoptions.setLayoutManager(manager);
        listAdoptions.setHasFixedSize(true);
        listAdoptions.setAdapter(adoptionsAdapter);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}