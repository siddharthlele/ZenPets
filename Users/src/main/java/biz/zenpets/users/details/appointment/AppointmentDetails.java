package biz.zenpets.users.details.appointment;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.details.doctor.map.MapDetails;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.clinics.ClinicImagesAdapter;
import biz.zenpets.users.utils.adapters.reviews.ReviewsAdapter;
import biz.zenpets.users.utils.models.clinics.ClinicImagesData;
import biz.zenpets.users.utils.models.reviews.ReviewsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class AppointmentDetails extends AppCompatActivity {

    /** THE INCOMING APPOINTMENT ID **/
    String APPOINTMENT_ID = null;

    /** THE STRINGS TO HOLD THE APPOINTMENT INFORMATION **/
    String DOCTOR_ID = null;
    String DOCTOR_PREFIX = null;
    String DOCTOR_NAME = null;
    String DOCTOR_DISPLAY_PROFILE = null;
    String DOCTOR_PHONE_PREFIX = null;
    String DOCTOR_PHONE_NUMBER = null;
    String DOCTOR_CHARGES = null;
    String CLINIC_ID = null;
    String CLINIC_NAME = null;
    String CLINIC_ADDRESS = null;
    String CLINIC_PIN_CODE = null;
    String CLINIC_LANDMARK = null;
    Double CLINIC_LATITUDE = null;
    Double CLINIC_LONGITUDE = null;
    String CLINIC_DISTANCE = null;
    String PET_ID = null;
    String PET_NAME = null;
    String PET_DISPLAY_PROFILE = null;
    String VISIT_REASON_ID = null;
    String VISIT_REASON_TEXT = null;
    String APPOINTMENT_DATE = null;
    String APPOINTMENT_TIME = null;
    String APPOINTMENT_STATUS = null;
    String CURRENCY_SYMBOL = null;
    String STATE_NAME = null;
    String CITY_NAME = null;
    String LOCALITY_NAME = null;

    /** A FUSED LOCATION PROVIDER CLIENT INSTANCE**/
    private FusedLocationProviderClient mFusedLocationClient;

    /** A LOCATION INSTANCE **/
    protected Location mLastLocation;

    /** THE LATLNG INSTANCES FOR CALCULATING THE DISTANCE **/
    LatLng LATLNG_ORIGIN;
    LatLng LATLNG_DESTINATION;

    /** THE REVIEWS AND SUBSET ADAPTER AND ARRAY LISTS **/
    private ReviewsAdapter reviewsAdapter;
    private final ArrayList<ReviewsData> arrReviewsSubset = new ArrayList<>();

    /** THE CLINIC IMAGES ADAPTER AND ARRAY LIST **/
    private ClinicImagesAdapter imagesAdapter;
    private final ArrayList<ClinicImagesData> arrImages = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.imgvwDoctorProfile) CircleImageView imgvwDoctorProfile;
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.txtAppointmentDate) AppCompatTextView txtAppointmentDate;
    @BindView(R.id.txtAppointmentTime) AppCompatTextView txtAppointmentTime;
    @BindView(R.id.txtDoctorCharges) AppCompatTextView txtDoctorCharges;
    @BindView(R.id.txtAppointmentStatus) AppCompatTextView txtAppointmentStatus;
    @BindView(R.id.txtClinicName) AppCompatTextView txtClinicName;
    @BindView(R.id.txtClinicAddress) AppCompatTextView txtClinicAddress;
    @BindView(R.id.clinicMap) MapView clinicMap;
    @BindView(R.id.txtDistance) AppCompatTextView txtDistance;
    @BindView(R.id.linlaReviews) LinearLayout linlaReviews;
    @BindView(R.id.listReviews) RecyclerView listReviews;
    @BindView(R.id.linlaNoReviews) LinearLayout linlaNoReviews;
    @BindView(R.id.listClinicImages) RecyclerView listClinicImages;

    /** SHOW THE CHARGES INFO **/
    @OnClick(R.id.imgvwChargesInfo) void showChargesInfo()  {
        showChargesDialog();
    }

    /** SHOW THE DIRECTIONS **/
    @OnClick(R.id.txtDirections) void showDirections()  {
    }

    /** SHOW ALL REVIEWS **/
    @OnClick(R.id.txtAllReviews) void showReviews() {
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment_details);
        ButterKnife.bind(this);
        clinicMap.onCreate(savedInstanceState);
        clinicMap.onResume();
        clinicMap.setClickable(false);

        /* INSTANTIATE THE LOCATION CLIENT */
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(AppointmentDetails.this);

        /* FETCH THE USER'S LOCATION */
        getLastLocation();

        /* INSTANTIATE THE ADAPTERS */
        reviewsAdapter = new ReviewsAdapter(AppointmentDetails.this, arrReviewsSubset);
        imagesAdapter = new ClinicImagesAdapter(AppointmentDetails.this, arrImages);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* FETCH THE FIRST 3 REVIEWS FOR THE DOCTOR */
        new fetchReviewsSubset().execute();

        /* FETCH CLINIC IMAGES */
        new fetchClinicImages().execute();

        /* CONFIGURE THE TOOLBAR */
        configTB();
    }

    /***** FETCH THE APPOINTMENT DETAILS *****/
    private class fetchAppointmentDetails extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_APPOINTMENT_DETAILS = "http://leodyssey.com/ZenPets/public/fetchAppointmentDetails";
            String URL_APPOINTMENT_DETAILS = "http://192.168.11.2/zenpets/public/fetchAppointmentDetails";
            HttpUrl.Builder builder = HttpUrl.parse(URL_APPOINTMENT_DETAILS).newBuilder();
            builder.addQueryParameter("appointmentID", APPOINTMENT_ID);
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

                    /* GET THE DOCTOR ID */
                    if (JORoot.has("doctorID")) {
                        DOCTOR_ID = JORoot.getString("doctorID");
                    } else {
                        DOCTOR_ID = null;
                    }

                    /* GET THE DOCTOR'S PREFIX AND NAME */
                    if (JORoot.has("doctorPrefix") && JORoot.has("doctorName"))   {
                        DOCTOR_PREFIX = JORoot.getString("doctorPrefix");
                        DOCTOR_NAME = JORoot.getString("doctorName");
                    } else {
                        DOCTOR_PREFIX = null;
                        DOCTOR_NAME = null;
                    }

                    /* GET THE DOCTOR'S DISPLAY PROFILE */
                    if (JORoot.has("doctorDisplayProfile")) {
                        DOCTOR_DISPLAY_PROFILE = JORoot.getString("doctorDisplayProfile");
                    } else {
                        DOCTOR_DISPLAY_PROFILE = null;
                    }

                    /* GET THE DOCTOR'S PHONE PREFIX AND NUMBER */
                    if (JORoot.has("doctorPhonePrefix") && JORoot.has("doctorPhoneNumber")) {
                        DOCTOR_PHONE_PREFIX = JORoot.getString("doctorPhonePrefix");
                        DOCTOR_PHONE_NUMBER = JORoot.getString("doctorPhoneNumber");
                    } else {
                        DOCTOR_PHONE_PREFIX = null;
                        DOCTOR_PHONE_NUMBER = null;
                    }

                    /* GET THE DOCTOR'S CHARGES */
                    if (JORoot.has("doctorCharges"))    {
                        DOCTOR_CHARGES = JORoot.getString("doctorCharges");
                    } else {
                        DOCTOR_CHARGES = null;
                    }

                    /* GET THE CLINIC ID */
                    if (JORoot.has("clinicID")) {
                        CLINIC_ID = JORoot.getString("clinicID");
                    } else {
                        CLINIC_ID = null;
                    }

                    /* GET THE CLINIC NAME */
                    if (JORoot.has("clinicName"))   {
                        CLINIC_NAME = JORoot.getString("clinicName");
                    } else {
                        CLINIC_NAME = null;
                    }

                    /* GET THE CLINIC ADDRESS */
                    if (JORoot.has("clinicAddress"))    {
                        CLINIC_ADDRESS = JORoot.getString("clinicAddress");
                    } else {
                        CLINIC_ADDRESS = null;
                    }

                    /* GET THE CLINIC LATITUDE AND LONGITUDE */
                    if (JORoot.has("clinicLatitude") && JORoot.has("clinicLongitude"))   {
                        CLINIC_LATITUDE = Double.valueOf(JORoot.getString("clinicLatitude"));
                        CLINIC_LONGITUDE = Double.valueOf(JORoot.getString("clinicLongitude"));
                        LATLNG_DESTINATION = new LatLng(CLINIC_LATITUDE, CLINIC_LONGITUDE);
                        String URL_DISTANCE = getUrl(LATLNG_ORIGIN, LATLNG_DESTINATION);
                        OkHttpClient clientDistance = new OkHttpClient();
                        Request requestDistance = new Request.Builder()
                                .url(URL_DISTANCE)
                                .build();
                        Call callDistance = clientDistance.newCall(requestDistance);
                        Response respDistance = callDistance.execute();
                        String strDistance = respDistance.body().string();
                        JSONObject JORootDistance = new JSONObject(strDistance);
                        JSONArray array = JORootDistance.getJSONArray("routes");
                        JSONObject JORoutes = array.getJSONObject(0);
                        JSONArray JOLegs= JORoutes.getJSONArray("legs");
                        JSONObject JOSteps = JOLegs.getJSONObject(0);
                        JSONObject JODistance = JOSteps.getJSONObject("distance");
                        if (JODistance.has("text")) {
                            CLINIC_DISTANCE = JODistance.getString("text");
                        } else {
                            CLINIC_DISTANCE = "Not Available";
                        }
                    } else {
                        CLINIC_LATITUDE = null;
                        CLINIC_LONGITUDE = null;
                    }

                    /* GET THE CLINIC PIN CODE */
                    if (JORoot.has("clinicPinCode"))    {
                        CLINIC_PIN_CODE = JORoot.getString("clinicPinCode");
                    } else {
                        CLINIC_PIN_CODE = null;
                    }

                    /* GET THE CLINIC LANDMARK */
                    if (JORoot.has("clinicLandmark"))   {
                        CLINIC_LANDMARK = JORoot.getString("clinicLandmark");
                    } else {
                        CLINIC_LANDMARK = null;
                    }

                    /* GET THE PET ID */
                    if (JORoot.has("petID"))    {
                        PET_ID = JORoot.getString("petID");
                    } else {
                        PET_ID = null;
                    }

                    /* GET THE PET NAME */
                    if (JORoot.has("petName"))  {
                        PET_NAME = JORoot.getString("petName");
                    } else {
                        PET_NAME = null;
                    }

                    /* GET THE PET'S DISPLAY PROFILE */
                    if (JORoot.has("petProfile"))   {
                        PET_DISPLAY_PROFILE = JORoot.getString("petProfile");
                    } else {
                        PET_DISPLAY_PROFILE = null;
                    }

                    /* GET THE VISIT REASON ID */
                    if (JORoot.has("visitReasonID"))    {
                        VISIT_REASON_ID = JORoot.getString("visitReasonID");
                    } else {
                        VISIT_REASON_ID = null;
                    }

                    /* GET THE VISIT REASON TEXT */
                    if (JORoot.has("visitReason"))  {
                        VISIT_REASON_TEXT = JORoot.getString("visitReason");
                    } else {
                        VISIT_REASON_TEXT = null;
                    }

                    /* GET THE APPOINTMENT DATE */
                    if (JORoot.has("appointmentDate"))  {
                        APPOINTMENT_DATE = JORoot.getString("appointmentDate");
                    } else {
                        APPOINTMENT_DATE = null;
                    }

                    /* GET THE APPOINTMENT TIME */
                    if (JORoot.has("appointmentTime"))  {
                        APPOINTMENT_TIME = JORoot.getString("appointmentTime");
                    } else {
                        APPOINTMENT_TIME = null;
                    }

                    /* GET THE APPOINTMENT STATUS */
                    if (JORoot.has("appointmentStatus"))    {
                        APPOINTMENT_STATUS = JORoot.getString("appointmentStatus");
                    } else {
                        APPOINTMENT_STATUS = null;
                    }

                    /* GET THE CURRENCY SYMBOL */
                    if (JORoot.has("currencySymbol"))   {
                        CURRENCY_SYMBOL = JORoot.getString("currencySymbol");
                    } else {
                        CURRENCY_SYMBOL = null;
                    }

                    /* GET THE STATE NAME */
                    if (JORoot.has("stateName"))    {
                        STATE_NAME = JORoot.getString("stateName");
                    } else {
                        STATE_NAME = null;
                    }

                    /* GET THE CITY NAME */
                    if (JORoot.has("cityName")) {
                        CITY_NAME = JORoot.getString("cityName");
                    } else {
                        CITY_NAME = null;
                    }

                    /* GET THE LOCALITY NAME */
                    if (JORoot.has("localityName")) {
                        LOCALITY_NAME = JORoot.getString("localityName");
                    } else {
                        LOCALITY_NAME = null;
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

            /* SET THE DOCTOR'S NAME */
            if (DOCTOR_PREFIX != null && DOCTOR_NAME != null)   {
                txtDoctorName.setText(DOCTOR_PREFIX + " " + DOCTOR_NAME);
            }

            /* SET THE DOCTOR'S DISPLAY PROFILE */
            if (DOCTOR_DISPLAY_PROFILE != null) {
                Picasso.with(AppointmentDetails.this)
                        .load(DOCTOR_DISPLAY_PROFILE)
                        .centerCrop()
                        .fit()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imgvwDoctorProfile, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(AppointmentDetails.this)
                                        .load(DOCTOR_DISPLAY_PROFILE)
                                        .centerCrop()
                                        .fit()
                                        .into(imgvwDoctorProfile);
                            }
                        });
            }

            /* SET THE APPOINTMENT DATE */
            if (APPOINTMENT_DATE != null)   {
                txtAppointmentDate.setText(APPOINTMENT_DATE);
            }

            /* SET THE APPOINTMENT TIME */
            if (APPOINTMENT_TIME != null)
                txtAppointmentTime.setText(APPOINTMENT_TIME);

            /* SET THE CONSULTATION FEES */
            if (CURRENCY_SYMBOL != null && DOCTOR_CHARGES != null)
                txtDoctorCharges.setText(CURRENCY_SYMBOL + " " + DOCTOR_CHARGES);

            /* SET THE APPOINTMENT STATUS */
            if (APPOINTMENT_STATUS != null) {
                if (APPOINTMENT_STATUS.equalsIgnoreCase("Confirmed"))    {
                    txtAppointmentStatus.setText("This appointment has been \"" + APPOINTMENT_STATUS + "\"");
                } else if (APPOINTMENT_STATUS.equalsIgnoreCase("Completed")) {
                    txtAppointmentStatus.setText("This appointment has been \"" + APPOINTMENT_STATUS + "\"");
                } else if (APPOINTMENT_STATUS.equalsIgnoreCase("Pending")) {
                    txtAppointmentStatus.setText("This appointment is \"" + APPOINTMENT_STATUS + "\" and has not been confirmed by the doctor yet");
                }
            }

            /* SET THE CLINIC NAME */
            if (CLINIC_NAME != null)    {
                txtClinicName.setText(CLINIC_NAME);
            }

            /* SET THE CLINIC ADDRESS */
            if (CLINIC_ADDRESS != null
                    && CITY_NAME != null
                    && STATE_NAME != null
                    && CLINIC_PIN_CODE != null
                    && CLINIC_LANDMARK != null)    {
                txtClinicAddress.setText(CLINIC_ADDRESS + ", " + CITY_NAME + ", " + STATE_NAME + ", " + CLINIC_PIN_CODE + "\n" + CLINIC_LANDMARK);
            }

            /* SET THE CLINIC'S LOCATION ON THE MAP */
            if (CLINIC_LATITUDE != null && CLINIC_LONGITUDE != null) {
                final LatLng latLng = new LatLng(CLINIC_LATITUDE, CLINIC_LONGITUDE);
                clinicMap.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(AppointmentDetails.this, R.raw.zen_map_style);
                        googleMap.setMapStyle(mapStyleOptions);
                        googleMap.getUiSettings().setMapToolbarEnabled(false);
                        googleMap.getUiSettings().setAllGesturesEnabled(false);
                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        googleMap.setBuildingsEnabled(true);
                        googleMap.setTrafficEnabled(false);
                        googleMap.setIndoorEnabled(false);
                        MarkerOptions options = new MarkerOptions();
                        options.position(latLng);
                        options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        Marker mMarker = googleMap.addMarker(options);
                        googleMap.addMarker(options);

                        /* MOVE THE MAP CAMERA */
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 10));
                        googleMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);

                        /** SHOW THE MAP DETAILS AND DIRECTIONS **/
                        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                            @Override
                            public void onMapClick(LatLng latLng) {
                                Intent intent = new Intent(getApplicationContext(), MapDetails.class);
                                intent.putExtra("DOCTOR_ID", DOCTOR_ID);
                                intent.putExtra("DOCTOR_NAME", DOCTOR_PREFIX + " " + DOCTOR_NAME);
                                intent.putExtra("CLINIC_ID", CLINIC_ID);
                                intent.putExtra("CLINIC_NAME", CLINIC_NAME);
                                intent.putExtra("CLINIC_LATITUDE", CLINIC_LATITUDE);
                                intent.putExtra("CLINIC_LONGITUDE", CLINIC_LONGITUDE);
                                intent.putExtra("CLINIC_ADDRESS", CLINIC_ADDRESS);
                                startActivity(intent);
                            }
                        });
                    }
                });
            }

            /* SET THE DISTANCE TO THE CLINIC */
            if (CLINIC_DISTANCE != null)
                txtDistance.setText(CLINIC_DISTANCE);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** FETCH THE FIRST 3 REVIEWS FOR THE DOCTOR *****/
    private class fetchReviewsSubset extends AsyncTask<Void, Void, Void>    {

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_SERVICES = "http://leodyssey.com/ZenPets/public/fetchDoctorReviewsSubset";
            String URL_DOCTOR_SERVICES = "http://192.168.11.2/zenpets/public/fetchDoctorReviewsSubset";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_SERVICES).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
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
                    JSONArray JAReviews = JORoot.getJSONArray("reviews");
                    if (JAReviews.length() > 0) {
                        /* A REVIEWS DATA INSTANCE */
                        ReviewsData data;
                        for (int i = 0; i < JAReviews.length(); i++) {
                            JSONObject JOReviews = JAReviews.getJSONObject(i);
                            /** INSTANTIATE THE REVIEWS DATA INSTANCE **/
                            data = new ReviewsData();

                            /** GET THE REVIEW ID **/
                            if (JOReviews.has("reviewID"))  {
                                data.setReviewID(JOReviews.getString("reviewID"));
                            } else {
                                data.setReviewID(null);
                            }

                            /** GET THE DOCTOR ID **/
                            if (JOReviews.has("doctorID"))  {
                                data.setDoctorID(JOReviews.getString("doctorID"));
                            } else {
                                data.setDoctorID(null);
                            }

                            /** GET THE USER ID **/
                            if (JOReviews.has("userID"))    {
                                data.setUserID(JOReviews.getString("userID"));
                            } else {
                                data.setUserID(null);
                            }

                            /** GET THE USER NAME **/
                            if (JOReviews.has("userName"))  {
                                data.setUserName(JOReviews.getString("userName"));
                            } else {
                                data.setUserName(null);
                            }

                            /** GET THE VISIT REASON ID **/
                            if (JOReviews.has("visitReasonID"))    {
                                data.setVisitReasonID(JOReviews.getString("visitReasonID"));
                            } else {
                                data.setVisitReasonID(null);
                            }

                            /** GET THE VISIT REASON **/
                            if (JOReviews.has("visitReason"))   {
                                data.setVisitReason("Visited for " + JOReviews.getString("visitReason"));
                            } else {
                                data.setVisitReason(null);
                            }

                            /** GET THE RECOMMEND STATUS **/
                            if (JOReviews.has("recommendStatus"))    {
                                data.setRecommendStatus(JOReviews.getString("recommendStatus"));
                            } else {
                                data.setRecommendStatus(null);
                            }

                            /** GET THE APPOINTMENT STATUS **/
                            if (JOReviews.has("appointmentStatus"))    {
                                data.setAppointmentStatus(JOReviews.getString("appointmentStatus"));
                            } else {
                                data.setAppointmentStatus(null);
                            }

                            /** GET THE DOCTOR RATING **/
                            if (JOReviews.has("doctorRating"))    {
                                data.setDoctorRating(JOReviews.getString("doctorRating"));
                            } else {
                                data.setDoctorRating(null);
                            }

                            /** GET THE DOCTOR EXPERIENCE **/
                            if (JOReviews.has("doctorExperience"))    {
                                data.setDoctorExperience(JOReviews.getString("doctorExperience"));
                            } else {
                                data.setDoctorExperience(null);
                            }

                            /** GET THE REVIEW TIMESTAMP **/
                            if (JOReviews.has("reviewTimestamp"))    {
                                String reviewTimestamp = JOReviews.getString("reviewTimestamp");
                                long lngTimeStamp = Long.parseLong(reviewTimestamp) * 1000;
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(lngTimeStamp);
                                Date date = calendar.getTime();
                                PrettyTime prettyTime = new PrettyTime();
                                String strDate = prettyTime.format(date);
                                data.setReviewTimestamp(strDate);
                            } else {
                                data.setReviewTimestamp(null);
                            }

                            /** ADD THE COLLECTED DATA TO THE ARRAY LIST **/
                            arrReviewsSubset.add(data);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /** SHOW THE RECYCLER VIEW AND HIDE THE EMPTY REVIEWS VIEW **/
                                linlaReviews.setVisibility(View.VISIBLE);
                                linlaNoReviews.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* SHOW THE NO REVIEWS CONTAINER */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaNoReviews.setVisibility(View.VISIBLE);
                                linlaReviews.setVisibility(View.GONE);
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

            /** INSTANTIATE THE ADAPTER **/
            reviewsAdapter = new ReviewsAdapter(AppointmentDetails.this, arrReviewsSubset);

            /** SET THE SERVICES ADAPTER TO THE RECYCLER VIEW **/
            listReviews.setAdapter(reviewsAdapter);
        }
    }

    /***** FETCH CLINIC IMAGES *****/
    private class fetchClinicImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_CLINIC_IMAGES = "http://leodyssey.com/ZenPets/public/fetchClinicImages";
            String URL_CLINIC_IMAGES = "http://192.168.11.2/zenpets/public/fetchClinicImages";
            HttpUrl.Builder builder = HttpUrl.parse(URL_CLINIC_IMAGES).newBuilder();
            builder.addQueryParameter("clinicID", CLINIC_ID);
            String FINAL_URL = builder.build().toString();
//            Log.e("IMAGES", FINAL_URL);
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
                    JSONArray JAImages = JORoot.getJSONArray("images");
                    /* A CLINIC IMAGES DATA INSTANCE */
                    ClinicImagesData data;
                    for (int i = 0; i < JAImages.length(); i++) {
                        JSONObject JOImages = JAImages.getJSONObject(i);
                        /** INSTANTIATE THE CLINIC IMAGES DATA INSTANCE **/
                        data = new ClinicImagesData();

                            /* GET THE IMAGE ID */
                        if (JOImages.has("imageID"))    {
                            data.setImageID(JOImages.getString("imageID"));
                        } else {
                            data.setImageID(null);
                        }

                            /* GET THE CLINIC ID */
                        if (JOImages.has("clinicID"))   {
                            data.setClinicID(JOImages.getString("clinicID"));
                        } else {
                            data.setClinicID(null);
                        }

                            /* GET THE CLINIC IMAGE URL */
                        if (JOImages.has("imageURL"))   {
                            data.setImageURL(JOImages.getString("imageURL"));
                        } else {
                            data.setImageURL(null);
                        }

                        /** ADD THE COLLECTED DATA TO THE ARRAY LIST **/
                        arrImages.add(data);
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

            /** INSTANTIATE THE ADAPTER **/
            imagesAdapter = new ClinicImagesAdapter(AppointmentDetails.this, arrImages);

            /** SET THE SERVICES ADAPTER TO THE RECYCLER VIEW **/
            listClinicImages.setAdapter(imagesAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
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

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("APPOINTMENT_ID"))   {
            APPOINTMENT_ID = bundle.getString("APPOINTMENT_ID");
            if (APPOINTMENT_ID != null) {
                /* FETCH THE APPOINTMENT DETAILS */
                new fetchAppointmentDetails().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info...", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info...", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** SHOW THE CHARGES INFO *****/
    private void showChargesDialog() {
        new MaterialDialog.Builder(this)
                .icon(ContextCompat.getDrawable(this, R.drawable.ic_info_outline_black_24dp))
                .title("Consultation Fees")
                .cancelable(true)
                .content("The fees are indicative and might vary depending on the services required and offered. \n\nNOTE: the fees are payable at the Clinic. There are no charges for booking an appointment")
                .positiveText("Dismiss")
                .theme(Theme.LIGHT)
                .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        clinicMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        clinicMap.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        clinicMap.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clinicMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        clinicMap.onLowMemory();
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

                            /* GET THE ORIGIN LATLNG */
                            LATLNG_ORIGIN = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                        } else {
                            Log.e("EXCEPTION", String.valueOf(task.getException()));
                        }
                    }
                });
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Appointment Details";
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

    /***** CONFIGURE THE RECYCLER VIEW *****/
    private void configRecycler() {
        LinearLayoutManager reviews = new LinearLayoutManager(this);
        reviews.setOrientation(LinearLayoutManager.VERTICAL);
        reviews.setAutoMeasureEnabled(true);
        listReviews.setLayoutManager(reviews);
        listReviews.setHasFixedSize(true);
        listReviews.setNestedScrollingEnabled(false);
        listReviews.setAdapter(reviewsAdapter);

        LinearLayoutManager llmClinicImages = new LinearLayoutManager(this);
        llmClinicImages.setOrientation(LinearLayoutManager.HORIZONTAL);
        llmClinicImages.setAutoMeasureEnabled(true);
        listClinicImages.setLayoutManager(llmClinicImages);
        listClinicImages.setHasFixedSize(true);
        listClinicImages.setNestedScrollingEnabled(false);
        listClinicImages.setAdapter(imagesAdapter);
    }
}