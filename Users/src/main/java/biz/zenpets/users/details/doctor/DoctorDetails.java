package biz.zenpets.users.details.doctor;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.creators.appointment.AppointmentSlotCreator;
import biz.zenpets.users.creators.feedback.FeedbackCreator;
import biz.zenpets.users.details.doctor.map.MapDetails;
import biz.zenpets.users.details.doctor.reviews.DoctorReviews;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.clinics.ClinicImagesAdapter;
import biz.zenpets.users.utils.adapters.reviews.ReviewsAdapter;
import biz.zenpets.users.utils.helpers.SubscriptionStatus;
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
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DoctorDetails extends AppCompatActivity implements SubscriptionStatus {

    /** THE INCOMING CLINIC ID AND THE DOCTOR ID **/
    private String DOCTOR_ID = null;
    private String CLINIC_ID = null;

    /** TODAY'S DAY **/
    private String TODAY_DAY = null;

    /** DATA TYPE TO STORE THE DATA **/
    private String CLINIC_NAME;
    private String CLINIC_CURRENCY;
    private String CLINIC_LOGO;
    private String CLINIC_ADDRESS;
    private String CLINIC_CITY = null;
    private String CLINIC_STATE = null;
    private String CLINIC_PIN_CODE = null;
    private String CLINIC_LANDMARK = null;
    private Double CLINIC_LATITUDE;
    private Double CLINIC_LONGITUDE;
    private String DOCTOR_PREFIX;
    private String DOCTOR_NAME;
    private String DOCTOR_PROFILE;
    private String DOCTOR_EXPERIENCE;
    private String DOCTOR_CHARGES;
    private String DOCTOR_PHONE_NUMBER = null;
    private String DOCTOR_LIKES = null;
    private String DOCTOR_VOTES = null;
    private String DOCTOR_LIKES_PERCENT = null;

    /** THE SERVICES AND SUBSET ADAPTER AND ARRAY LISTS **/
//    ServicesAdapter servicesAdapter;
//    final ArrayList<ServicesData> arrServicesSubset = new ArrayList<>();
//    final ArrayList<ServicesData> arrServices = new ArrayList<>();

    /** THE REVIEWS AND SUBSET ADAPTER AND ARRAY LISTS **/
    private ReviewsAdapter reviewsAdapter;
    private final ArrayList<ReviewsData> arrReviewsSubset = new ArrayList<>();

    /** THE CLINIC IMAGES ADAPTER AND ARRAY LIST **/
    private ClinicImagesAdapter imagesAdapter;
    private final ArrayList<ClinicImagesData> arrImages = new ArrayList<>();

    /** PERMISSION REQUEST CONSTANTS **/
    private static final int CALL_PHONE_CONSTANT = 200;

    /** THE TIMING STRINGS **/
    private String SUN_MOR_FROM = null;
    private String SUN_MOR_TO = null;
    private String SUN_AFT_FROM = null;
    private String SUN_AFT_TO = null;
    private String MON_MOR_FROM = null;
    private String MON_MOR_TO = null;
    private String MON_AFT_FROM = null;
    private String MON_AFT_TO = null;
    private String TUE_MOR_FROM = null;
    private String TUE_MOR_TO = null;
    private String TUE_AFT_FROM = null;
    private String TUE_AFT_TO = null;
    private String WED_MOR_FROM = null;
    private String WED_MOR_TO = null;
    private String WED_AFT_FROM = null;
    private String WED_AFT_TO = null;
    private String THU_MOR_FROM = null;
    private String THU_MOR_TO = null;
    private String THU_AFT_FROM = null;
    private String THU_AFT_TO = null;
    private String FRI_MOR_FROM = null;
    private String FRI_MOR_TO = null;
    private String FRI_AFT_FROM = null;
    private String FRI_AFT_TO = null;
    private String SAT_MOR_FROM = null;
    private String SAT_MOR_TO = null;
    private String SAT_AFT_FROM = null;
    private String SAT_AFT_TO = null;

    /** THE DOCTOR'S SUBSCRIPTION STATUS FLAG **/
    private boolean blnSubscriptionStatus = false;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.imgvwClinicCover) AppCompatImageView imgvwClinicCover;
    @BindView(R.id.imgvwDoctorProfile) CircleImageView imgvwDoctorProfile;
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.txtDoctorEducation) AppCompatTextView txtDoctorEducation;
    @BindView(R.id.linlaExperience) LinearLayout linlaExperience;
    @BindView(R.id.txtExperience) AppCompatTextView txtExperience;
    @BindView(R.id.linlaVotes) LinearLayout linlaVotes;
    @BindView(R.id.txtVotes) AppCompatTextView txtVotes;
    @BindView(R.id.txtDoctorCharges) AppCompatTextView txtDoctorCharges;
    @BindView(R.id.txtClinicName) AppCompatTextView txtClinicName;
    @BindView(R.id.txtClinicAddress) AppCompatTextView txtClinicAddress;
    @BindView(R.id.clinicMap) MapView clinicMap;
    @BindView(R.id.linlaTimingMorning) LinearLayout linlaTimingMorning;
    @BindView(R.id.txtMorningOpen) AppCompatTextView txtMorningOpen;
    @BindView(R.id.txtTimingsMorning) AppCompatTextView txtTimingsMorning;
    @BindView(R.id.txtMorningClosed) AppCompatTextView txtMorningClosed;
    @BindView(R.id.linlaTimingAfternoon) LinearLayout linlaTimingAfternoon;
    @BindView(R.id.txtAfternoonOpen) AppCompatTextView txtAfternoonOpen;
    @BindView(R.id.txtTimingAfternoon) AppCompatTextView txtTimingAfternoon;
    @BindView(R.id.txtAfternoonClosed) AppCompatTextView txtAfternoonClosed;
    @BindView(R.id.linlaReviews) LinearLayout linlaReviews;
    @BindView(R.id.listReviews) RecyclerView listReviews;
    @BindView(R.id.linlaNoReviews) LinearLayout linlaNoReviews;
    @BindView(R.id.listClinicImages) RecyclerView listClinicImages;
//    @BindView(R.id.linlaServices) LinearLayout linlaServices;
//    @BindView(R.id.listServices) RecyclerView listServices;
//    @BindView(R.id.linlaNoServices) LinearLayout linlaNoServices;
    @BindView(R.id.btnBook) AppCompatButton btnBook;

    /** THE CUSTOM TIMINGS LAYOUT ELEMENTS **/
    private AppCompatTextView txtSunMorning;
    private AppCompatTextView txtSunAfternoon;
    private AppCompatTextView txtMonMorning;
    private AppCompatTextView txtMonAfternoon;
    private AppCompatTextView txtTueMorning;
    private AppCompatTextView txtTueAfternoon;
    private AppCompatTextView txtWedMorning;
    private AppCompatTextView txtWedAfternoon;
    private AppCompatTextView txtThuMorning;
    private AppCompatTextView txtThuAfternoon;
    private AppCompatTextView txtFriMorning;
    private AppCompatTextView txtFriAfternoon;
    private AppCompatTextView txtSatMorning;
    private AppCompatTextView txtSatAfternoon;

    /** THE CUSTOM SERVICES LAYOUT ELEMENTS **/
//    RecyclerView listDoctorServices;

    /** THE ALL CUSTOM VIEWS **/
    private View custAllTimings;
//    private View custAllServices;

    /** SHOW THE CHARGES DIALOG **/
    @OnClick(R.id.imgvwChargesInfo) void showChargesInfo()    {
        showChargesDialog();
    }

    /** SHOW ALL TIMINGS **/
    @OnClick(R.id.txtAllTimings) void showAllTimings()  {
        showDoctorTimings();
    }

    /** SHOW ALL REVIEWS **/
    @OnClick(R.id.txtAllReviews) void showAllReviews()  {
        Intent intent = new Intent(getApplicationContext(), DoctorReviews.class);
        intent.putExtra("DOCTOR_ID", DOCTOR_ID);
        intent.putExtra("DOCTOR_PREFIX", DOCTOR_PREFIX);
        intent.putExtra("DOCTOR_NAME", DOCTOR_NAME);
        startActivity(intent);
    }

    /** SHOW ALL SERVICES **/
//    @OnClick(R.id.txtAllServices) void showAllServices()    {
//        showDoctorServices();
//    }

    /** NEW FEEDBACK **/
    @OnClick(R.id.btnFeedback) void newFeedback()   {
        Intent intentNewFeedback = new Intent(getApplicationContext(), FeedbackCreator.class);
        intentNewFeedback.putExtra("DOCTOR_ID", DOCTOR_ID);
        intentNewFeedback.putExtra("CLINIC_ID", CLINIC_ID);
        startActivity(intentNewFeedback);
    }

    /** CALL / NEW APPOINTMENT **/
    @OnClick(R.id.btnBook) void newAppointment()    {
        if (blnSubscriptionStatus)  {
            Intent intentAppointment = new Intent(getApplicationContext(), AppointmentSlotCreator.class);
            intentAppointment.putExtra("DOCTOR_ID", DOCTOR_ID);
            intentAppointment.putExtra("CLINIC_ID", CLINIC_ID);
            startActivity(intentAppointment);
        } else {
            if (ContextCompat.checkSelfPermission(DoctorDetails.this,
                    Manifest.permission.CALL_PHONE)
                    != PackageManager.PERMISSION_GRANTED)   {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CALL_PHONE))    {
                    /** SHOW THE DIALOG **/
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setCancelable(false);
                    builder.setIcon(R.drawable.ic_info_outline_black_24dp);
                    builder.setTitle("Permission Required");
                    builder.setMessage("\nZen Pets requires the permission to call the Doctor's phone number. \n\nFor a seamless experience, we recommend granting Zen Pets this permission.");
                    builder.setPositiveButton("Grant Permission", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(
                                    DoctorDetails.this,
                                    new String[]{Manifest.permission.CALL_PHONE}, CALL_PHONE_CONSTANT);
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
                            new String[]{Manifest.permission.CALL_PHONE},
                            CALL_PHONE_CONSTANT);
                }
            } else {
                /* CALL THE PHONE NUMBER */
                callPhone();
            }
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_details);
        ButterKnife.bind(this);
        clinicMap.onCreate(savedInstanceState);
        clinicMap.onResume();
        clinicMap.setClickable(false);

        /** INSTANTIATE THE ADAPTERS **/
//        servicesAdapter = new ServicesAdapter(DoctorDetails.this, arrServices);
        reviewsAdapter = new ReviewsAdapter(DoctorDetails.this, arrReviewsSubset);
        imagesAdapter = new ClinicImagesAdapter(DoctorDetails.this, arrImages);

        /** CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /** INFLATE THE CUSTOM VIEWS **/
        custAllTimings = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_all_timings_view, null);
//        custAllServices = LayoutInflater.from(getApplicationContext()).inflate(R.layout.custom_all_services_list, null);

        /* GET TODAY'S DAY */
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE", Locale.getDefault());
        Date d = new Date();
        TODAY_DAY = sdf.format(d);
//        Log.e("TODAY", TODAY_DAY);

        /** GET THE INCOMING DATA **/
        getIncomingData();

        /** FETCH THE BOOKMARK STATUS **/
        new getSubscriptionStatus(this).execute();

        /** FETCH THE DOCTOR'S EDUCATIONAL QUALIFICATIONS **/
        new fetchDoctorEducation().execute();

//        /** FETCH THE FIRST 3 SERVICES OFFERED BY THE DOCTOR **/
//        new fetchServicesSubset().execute();

        /** FETCH THE FIRST 3 REVIEWS FOR THE DOCTOR **/
        new fetchReviewsSubset().execute();

        /** FETCH CLINIC IMAGES **/
        new fetchClinicImages().execute();

        /** CONFIGURE THE TOOLBAR **/
        configTB();
    }

    /***** FETCH THE DOCTOR DETAILS *****/
    private class fetchDoctorDetails extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchDoctorDetails";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchDoctorDetails";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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

                    /* GET THE DOCTOR'S PREFIX AND NAME */
                    DOCTOR_PREFIX = JORoot.getString("doctorPrefix");
                    DOCTOR_NAME = JORoot.getString("doctorName");

                    /* GET THE DOCTOR'S PROFILE PICTURE */
                    DOCTOR_PROFILE = JORoot.getString("doctorDisplayProfile");

                    /* GET THE CLINIC'S LOGO */
                    CLINIC_LOGO = JORoot.getString("clinicLogo");

                    /** GET THE DOCTOR'S EXPERIENCE **/
                    DOCTOR_EXPERIENCE = JORoot.getString("doctorExperience");

                    /* GET THE DOCTOR'S SELECTED CURRENCY AND CHARGES */
                    CLINIC_CURRENCY = JORoot.getString("currencySymbol");
                    DOCTOR_CHARGES = JORoot.getString("doctorCharges");

                    /* GET THE CLINIC NAME */
                    CLINIC_NAME = JORoot.getString("clinicName");

                    /* GET THE CLINIC ADDRESS */
                    CLINIC_ADDRESS = JORoot.getString("clinicAddress");
                    CLINIC_CITY = JORoot.getString("cityName");
                    CLINIC_STATE = JORoot.getString("stateName");
                    CLINIC_PIN_CODE = JORoot.getString("clinicPinCode");
                    CLINIC_LANDMARK = JORoot.getString("clinicLandmark");

                    /* GET THE DOCTOR'S PHONE NUMBER */
                    if (JORoot.has("doctorPhonePrefix") && JORoot.has("doctorPhoneNumber")) {
                        String doctorPhonePrefix = JORoot.getString("doctorPhonePrefix");
                        String doctorPhoneNumber = JORoot.getString("doctorPhoneNumber");
                        DOCTOR_PHONE_NUMBER = doctorPhonePrefix + doctorPhoneNumber;
                    }

                    /* GET THE CLINIC LATITUDE AND LONGITUDE */
                    CLINIC_LATITUDE = Double.valueOf(JORoot.getString("clinicLatitude"));
                    CLINIC_LONGITUDE = Double.valueOf(JORoot.getString("clinicLongitude"));

                        /* THE TOTAL VOTES, TOTAL LIKES AND TOTAL DISLIKES */
                    int TOTAL_VOTES = 0;
                    int TOTAL_LIKES = 0;

                    /* GET THE POSITIVE REVIEWS / FEEDBACK FOR THE DOCTORS */
                    // String URL_POSITIVE_REVIEWS = "http://leodyssey.com/ZenPets/public/fetchPositiveReviews";
                    String URL_POSITIVE_REVIEWS = "http://192.168.11.2/zenpets/public/fetchPositiveReviews";
                    HttpUrl.Builder builderPositive = HttpUrl.parse(URL_POSITIVE_REVIEWS).newBuilder();
                    builderPositive.addQueryParameter("doctorID", DOCTOR_ID);
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
                    builderNegative.addQueryParameter("doctorID", DOCTOR_ID);
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
                    DOCTOR_LIKES = String.valueOf(TOTAL_LIKES);

                    /** CALCULATE THE PERCENTAGE OF LIKES **/
                    double percentLikes = ((double)TOTAL_LIKES / TOTAL_VOTES) * 100;
                    int finalPercentLikes = (int)percentLikes;
                    DOCTOR_LIKES_PERCENT = String.valueOf(finalPercentLikes) + "%";

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
                    DOCTOR_VOTES = reviewQuantity;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            txtDoctorName.setText(DOCTOR_PREFIX + " " + DOCTOR_NAME);

            Glide.with(DoctorDetails.this)
                    .load(DOCTOR_PROFILE)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .into(imgvwDoctorProfile);

            Glide.with(DoctorDetails.this)
                    .load(CLINIC_LOGO)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .into(imgvwClinicCover);

            txtExperience.setText(DOCTOR_EXPERIENCE + " yrs experience");

            txtClinicName.setText(CLINIC_NAME);

            txtDoctorCharges.setText(CLINIC_CURRENCY + " " + DOCTOR_CHARGES);

            txtClinicAddress.setText(CLINIC_ADDRESS + ", " + CLINIC_CITY + ", " + CLINIC_STATE + ", " + CLINIC_PIN_CODE + "\n" + CLINIC_LANDMARK);

            if (CLINIC_LATITUDE != null && CLINIC_LONGITUDE != null) {
                final LatLng latLng = new LatLng(CLINIC_LATITUDE, CLINIC_LONGITUDE);
                clinicMap.getMapAsync(new OnMapReadyCallback() {
                    @Override
                    public void onMapReady(GoogleMap googleMap) {
                        MapStyleOptions mapStyleOptions = MapStyleOptions.loadRawResourceStyle(DoctorDetails.this, R.raw.zen_map_style);
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

            txtVotes.setText(DOCTOR_LIKES_PERCENT + " (" + DOCTOR_VOTES + ")");

            if (TODAY_DAY.equalsIgnoreCase("Sunday"))   {
                new checkSundayMorning().execute();
                new checkSundayAfternoon().execute();
            } else if (TODAY_DAY.equalsIgnoreCase("Monday"))    {
                new checkMondayMorning().execute();
                new checkMondayAfternoon().execute();
            } else if (TODAY_DAY.equalsIgnoreCase("Tuesday"))   {
                new checkTuesdayMorning().execute();
                new checkTuesdayAfternoon().execute();
            } else if (TODAY_DAY.equalsIgnoreCase("Wednesday")) {
                new checkWednesdayMorning().execute();
                new checkWednesdayAfternoon().execute();
            } else if (TODAY_DAY.equalsIgnoreCase("Thursday"))  {
                new checkThursdayMorning().execute();
                new checkThursdayAfternoon().execute();
            } else if (TODAY_DAY.equalsIgnoreCase("Friday"))    {
                new checkFridayMorning().execute();
                new checkFridayAfternoon().execute();
            } else if (TODAY_DAY.equalsIgnoreCase("Saturday"))  {
                new checkSaturdayMorning().execute();
                new checkSaturdayAfternoon().execute();
            }
        }
    }

    /***** FETCH THE DOCTOR'S EDUCATIONAL QUALIFICATIONS *****/
    private class fetchDoctorEducation extends AsyncTask<Void, Void, Void>  {

        /** A STRING BUILDER INSTANCE **/
        StringBuilder sb;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_EDUCATION = "http://leodyssey.com/ZenPets/public/fetchDoctorEducation";
            String URL_DOCTOR_EDUCATION = "http://192.168.11.2/zenpets/public/fetchDoctorEducation";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_EDUCATION).newBuilder();
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
                    JSONArray JAQualifications = JORoot.getJSONArray("qualifications");
                    sb = new StringBuilder();
                    for (int i = 0; i < JAQualifications.length(); i++) {
                        JSONObject JOQualifications = JAQualifications.getJSONObject(i);

                        if (JOQualifications.has("doctorEducationName"))    {
                            String doctorEducationName = JOQualifications.getString("doctorEducationName");
                            sb.append(doctorEducationName + ", ");
                        }
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

            String strEducation = sb.toString();
            if (strEducation.endsWith(", "))    {
                strEducation = strEducation.substring(0, strEducation.length() - 2);
                txtDoctorEducation.setText(strEducation);
            } else {
                txtDoctorEducation.setText(strEducation);
            }
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
            imagesAdapter = new ClinicImagesAdapter(DoctorDetails.this, arrImages);

            /** SET THE SERVICES ADAPTER TO THE RECYCLER VIEW **/
            listClinicImages.setAdapter(imagesAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
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
            reviewsAdapter = new ReviewsAdapter(DoctorDetails.this, arrReviewsSubset);

            /** SET THE SERVICES ADAPTER TO THE RECYCLER VIEW **/
            listReviews.setAdapter(reviewsAdapter);
        }
    }

    /***** FETCH THE FIRST 3 SERVICES OFFERED BY THE DOCTOR *****/
//    private class fetchServicesSubset extends AsyncTask<Void, Void, Void>   {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            // String URL_DOCTOR_SERVICES = "http://leodyssey.com/ZenPets/public/fetchServicesSubset";
//            String URL_DOCTOR_SERVICES = "http://192.168.11.2/zenpets/public/fetchServicesSubset";
//            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_SERVICES).newBuilder();
//            builder.addQueryParameter("doctorID", DOCTOR_ID);
//            String FINAL_URL = builder.build().toString();
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder()
//                    .url(FINAL_URL)
//                    .build();
//            Call call = client.newCall(request);
//            try {
//                Response response = call.execute();
//                String strResult = response.body().string();
//                JSONObject JORoot = new JSONObject(strResult);
//                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
//                    JSONArray JAServices = JORoot.getJSONArray("services");
//                    if (JAServices.length() > 0) {
//                        /* A SERVICES DATA INSTANCE */
//                        ServicesData data;
//                        for (int i = 0; i < JAServices.length(); i++) {
//                            JSONObject JOServices = JAServices.getJSONObject(i);
//
//                            /** INSTANTIATE THE SERVICES DATA INSTANCE **/
//                            data = new ServicesData();
//
//                            /** GET THE SERVICE NAME **/
//                            if (JOServices.has("doctorServiceName"))    {
//                                data.setServiceName(JOServices.getString("doctorServiceName"));
//                            } else {
//                                data.setServiceName(null);
//                            }
//
//                            /** ADD THE COLLECTED DATA TO THE ARRAY LIST **/
//                            arrServicesSubset.add(data);
//                        }
//
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                /** SHOW THE RECYCLER VIEW AND HIDE THE EMPTY SERVICES VIEW **/
//                                linlaServices.setVisibility(View.VISIBLE);
//                                linlaNoServices.setVisibility(View.GONE);
//                            }
//                        });
//                    } else {
//                        /* SHOW THE NO SERVICES CONTAINER */
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                linlaNoServices.setVisibility(View.VISIBLE);
//                                linlaServices.setVisibility(View.GONE);
//                            }
//                        });
//                    }
//                }
//            } catch (IOException | JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//
//            /** INSTANTIATE THE ADAPTER **/
//            servicesAdapter = new ServicesAdapter(DoctorDetails.this, arrServicesSubset);
//
//            /** SET THE SERVICES ADAPTER TO THE RECYCLER VIEW **/
//            listServices.setAdapter(servicesAdapter);
//        }
//    }

    /** CHECK THE SUNDAY MORNING TIMINGS **/
    private class checkSundayMorning extends AsyncTask<Void, Void, Void>    {

        String sunMorFrom = null;
        String sunMorTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchSundayMorningTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchSundayMorningTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
//                        Log.e("TIMING", String.valueOf(JOTimings));

                        if (JOTimings.has("sunMorFrom") && JOTimings.has("sunMorTo"))  {
                            sunMorFrom = JOTimings.getString("sunMorFrom");
                            sunMorTo = JOTimings.getString("sunMorTo");
                        } else {
                            sunMorFrom = null;
                            sunMorTo = null;
                        }
                    }
                } else {
                    sunMorFrom = null;
                    sunMorTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!sunMorFrom.equalsIgnoreCase("null") && !sunMorTo.equalsIgnoreCase("null")) {
                txtTimingsMorning.setVisibility(View.VISIBLE);
                txtMorningOpen.setVisibility(View.VISIBLE);
                txtMorningClosed.setVisibility(View.GONE);
                txtTimingsMorning.setText(sunMorFrom + " - " + sunMorTo);
            } else {
                txtTimingsMorning.setVisibility(View.GONE);
                txtMorningOpen.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE SUNDAY AFTERNOON TIMINGS **/
    private class checkSundayAfternoon extends AsyncTask<Void, Void, Void>  {

        String sunAftFrom = null;
        String sunAftTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchSundayAfternoonTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchSundayAfternoonTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
//                        Log.e("TIMING", String.valueOf(JOTimings));

                        if (JOTimings.has("sunAftFrom") && JOTimings.has("sunAftTo"))  {
                            sunAftFrom = JOTimings.getString("sunAftFrom");
                            sunAftTo = JOTimings.getString("sunAftTo");
                        } else {
                            sunAftFrom = null;
                            sunAftTo = null;
                        }
                    }
                } else {
                    sunAftFrom = null;
                    sunAftTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!sunAftFrom.equalsIgnoreCase("null") && !sunAftTo.equalsIgnoreCase("null")) {
                linlaTimingAfternoon.setVisibility(View.VISIBLE);
                txtTimingAfternoon.setVisibility(View.VISIBLE);
                txtAfternoonOpen.setVisibility(View.VISIBLE);
                txtAfternoonClosed.setVisibility(View.GONE);
                txtTimingAfternoon.setText(sunAftFrom + " - " + sunAftTo);
            } else {
                txtTimingAfternoon.setVisibility(View.GONE);
                txtAfternoonOpen.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE MONDAY MORNING TIMINGS **/
    private class checkMondayMorning extends AsyncTask<Void, Void, Void>    {

        String monMorFrom = null;
        String monMorTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchMondayMorningTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchMondayMorningTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);

                        if (JOTimings.has("monMorFrom") && JOTimings.has("monMorTo"))  {
                            monMorFrom = JOTimings.getString("monMorFrom");
                            monMorTo = JOTimings.getString("monMorTo");
                        } else {
                            monMorFrom = null;
                            monMorTo = null;
                        }
                    }
                } else {
                    monMorFrom = null;
                    monMorTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!monMorFrom.equalsIgnoreCase("null") && !monMorTo.equalsIgnoreCase("null")) {
                txtTimingsMorning.setVisibility(View.VISIBLE);
                txtMorningOpen.setVisibility(View.VISIBLE);
                txtMorningClosed.setVisibility(View.GONE);
                txtTimingsMorning.setText(monMorFrom + " - " + monMorTo);
            } else {
                txtTimingsMorning.setVisibility(View.GONE);
                txtMorningOpen.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE MONDAY AFTERNOON TIMINGS **/
    private class checkMondayAfternoon extends AsyncTask<Void, Void, Void>  {

        String monAftFrom = null;
        String monAftTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchMondayAfternoonTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchMondayAfternoonTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
            String FINAL_URL = builder.build().toString();
//            Log.e("MONDAY AFTERNOON", FINAL_URL);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);

                        if (JOTimings.has("monAftFrom") && JOTimings.has("monAftTo"))  {
                            monAftFrom = JOTimings.getString("monAftFrom");
                            monAftTo = JOTimings.getString("monAftTo");
                        } else {
                            monAftFrom = null;
                            monAftTo = null;
                        }
                    }
                } else {
                    monAftFrom = null;
                    monAftTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (monAftFrom != null
                    && monAftTo != null
                    && !monAftFrom.equalsIgnoreCase("null")
                    && !monAftTo.equalsIgnoreCase(null)) {
                linlaTimingAfternoon.setVisibility(View.VISIBLE);
                txtTimingAfternoon.setVisibility(View.VISIBLE);
                txtAfternoonOpen.setVisibility(View.VISIBLE);
                txtAfternoonClosed.setVisibility(View.GONE);
                txtTimingAfternoon.setText(monAftFrom + " - " + monAftTo);
            } else {
                txtTimingAfternoon.setVisibility(View.GONE);
                txtAfternoonOpen.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }

//            if (!monAftFrom.equalsIgnoreCase("null") && monAftFrom != null
//                    && !monAftTo.equalsIgnoreCase("null") && monAftTo != null) {
//                linlaTimingAfternoon.setVisibility(View.VISIBLE);
//                txtTimingAfternoon.setVisibility(View.VISIBLE);
//                txtAfternoonOpen.setVisibility(View.VISIBLE);
//                txtAfternoonClosed.setVisibility(View.GONE);
//                txtTimingAfternoon.setText(monAftFrom + " - " + monAftTo);
//            } else {
//                txtTimingAfternoon.setVisibility(View.GONE);
//                txtAfternoonOpen.setVisibility(View.GONE);
//                txtAfternoonClosed.setVisibility(View.VISIBLE);
//            }
        }
    }

    /***** CHECK THE TUESDAY MORNING TIMINGS *****/
    private class checkTuesdayMorning extends AsyncTask<Void, Void, Void>   {

        String tueMorFrom = null;
        String tueMorTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchTuesdayMorningTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchTuesdayMorningTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);

                        if (JOTimings.has("tueMorFrom") && JOTimings.has("tueMorTo"))  {
                            tueMorFrom = JOTimings.getString("tueMorFrom");
                            tueMorTo = JOTimings.getString("tueMorTo");
                        } else {
                            tueMorFrom = null;
                            tueMorTo = null;
                        }
                    }
                } else {
                    tueMorFrom = null;
                    tueMorTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!tueMorFrom.equalsIgnoreCase("null") && !tueMorTo.equalsIgnoreCase("null")) {
                txtTimingsMorning.setVisibility(View.VISIBLE);
                txtMorningOpen.setVisibility(View.VISIBLE);
                txtMorningClosed.setVisibility(View.GONE);
                txtTimingsMorning.setText(tueMorFrom + " - " + tueMorTo);
            } else {
                txtTimingsMorning.setVisibility(View.GONE);
                txtMorningOpen.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /***** CHECK THE TUESDAY AFTERNOON TIMINGS *****/
    private class checkTuesdayAfternoon extends AsyncTask<Void, Void, Void> {

        String tueAftFrom = null;
        String tueAftTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchTuesdayAfternoonTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchTuesdayAfternoonTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
//                            Log.e("TIMING", String.valueOf(JOTimings));

                        if (JOTimings.has("tueAftFrom") && JOTimings.has("tueAftTo"))  {
                            tueAftFrom = JOTimings.getString("tueAftFrom");
                            tueAftTo = JOTimings.getString("tueAftTo");
                        } else {
                            tueAftFrom = null;
                            tueAftTo = null;
                        }
                    }
                } else {
                    tueAftFrom = null;
                    tueAftTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!tueAftFrom.equalsIgnoreCase("null") && !tueAftTo.equalsIgnoreCase("null")) {
                linlaTimingAfternoon.setVisibility(View.VISIBLE);
                txtTimingAfternoon.setVisibility(View.VISIBLE);
                txtAfternoonOpen.setVisibility(View.VISIBLE);
                txtAfternoonClosed.setVisibility(View.GONE);
                txtTimingAfternoon.setText(tueAftFrom + " - " + tueAftTo);
            } else {
                txtTimingAfternoon.setVisibility(View.GONE);
                txtAfternoonOpen.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE WEDNESDAY MORNING TIMINGS **/
    private class checkWednesdayMorning extends AsyncTask<Void, Void, Void> {

        String wedMorFrom = null;
        String wedMorTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchWednesdayMorningTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchWednesdayMorningTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
                        if (JOTimings.has("wedMorFrom") && JOTimings.has("wedMorTo"))  {
                            wedMorFrom = JOTimings.getString("wedMorFrom");
                            wedMorTo = JOTimings.getString("wedMorTo");
                        } else {
                            wedMorFrom = null;
                            wedMorTo = null;
                        }
                    }
                } else {
                    wedMorFrom = null;
                    wedMorTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!wedMorFrom.equalsIgnoreCase("null") && !wedMorTo.equalsIgnoreCase("null")) {
                txtTimingsMorning.setVisibility(View.VISIBLE);
                txtMorningOpen.setVisibility(View.VISIBLE);
                txtMorningClosed.setVisibility(View.GONE);
                txtTimingsMorning.setText(wedMorFrom + " - " + wedMorTo);
            } else {
                txtTimingsMorning.setVisibility(View.GONE);
                txtMorningOpen.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE WEDNESDAY AFTERNOON TIMINGS **/
    private class checkWednesdayAfternoon extends AsyncTask<Void, Void, Void>   {

        String wedAftFrom = null;
        String wedAftTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchWednesdayAfternoonTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchWednesdayAfternoonTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
                        if (JOTimings.has("wedAftFrom") && JOTimings.has("wedAftTo"))  {
                            wedAftFrom = JOTimings.getString("wedAftFrom");
                            wedAftTo = JOTimings.getString("wedAftTo");
                        } else {
                            wedAftFrom = null;
                            wedAftTo = null;
                        }
                    }
                } else {
                    wedAftFrom = null;
                    wedAftTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!wedAftFrom.equalsIgnoreCase("null") && !wedAftTo.equalsIgnoreCase("null")) {
                linlaTimingAfternoon.setVisibility(View.VISIBLE);
                txtTimingAfternoon.setVisibility(View.VISIBLE);
                txtAfternoonOpen.setVisibility(View.VISIBLE);
                txtAfternoonClosed.setVisibility(View.GONE);
                txtTimingAfternoon.setText(wedAftFrom + " - " + wedAftTo);
            } else {
                txtTimingAfternoon.setVisibility(View.GONE);
                txtAfternoonOpen.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE THURSDAY MORNING TIMINGS **/
    private class checkThursdayMorning extends AsyncTask<Void, Void, Void>  {

        String thuMorFrom = null;
        String thuMorTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchThursdayMorningTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchThursdayMorningTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
//                            Log.e("TIMING", String.valueOf(JOTimings));

                        if (JOTimings.has("thuMorFrom") && JOTimings.has("thuMorTo"))  {
                            thuMorFrom = JOTimings.getString("thuMorFrom");
                            thuMorTo = JOTimings.getString("thuMorTo");
                        } else {
                            thuMorFrom = null;
                            thuMorTo = null;
                        }
                    }
                } else {
                    thuMorFrom = null;
                    thuMorTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!thuMorFrom.equalsIgnoreCase("null") && !thuMorTo.equalsIgnoreCase("null")) {
                txtTimingsMorning.setVisibility(View.VISIBLE);
                txtMorningOpen.setVisibility(View.VISIBLE);
                txtMorningClosed.setVisibility(View.GONE);
                txtTimingsMorning.setText(thuMorFrom + " - " + thuMorTo);
            } else {
                txtTimingsMorning.setVisibility(View.GONE);
                txtMorningOpen.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE THURSDAY AFTERNOON TIMINGS **/
    private class checkThursdayAfternoon extends AsyncTask<Void, Void, Void>    {

        String thuAftFrom = null;
        String thuAftTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchThursdayAfternoonTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchThursdayAfternoonTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
                        if (JOTimings.has("thuAftFrom") && JOTimings.has("thuAftTo"))  {
                            thuAftFrom = JOTimings.getString("thuAftFrom");
                            thuAftTo = JOTimings.getString("thuAftTo");
                        } else {
                            thuAftFrom = null;
                            thuAftTo = null;
                        }
                    }
                } else {
                    thuAftFrom = null;
                    thuAftTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!thuAftFrom.equalsIgnoreCase("null") && !thuAftTo.equalsIgnoreCase("null")) {
                linlaTimingAfternoon.setVisibility(View.VISIBLE);
                txtTimingAfternoon.setVisibility(View.VISIBLE);
                txtAfternoonOpen.setVisibility(View.VISIBLE);
                txtAfternoonClosed.setVisibility(View.GONE);
                txtTimingAfternoon.setText(thuAftFrom + " - " + thuAftTo);
            } else {
                txtTimingAfternoon.setVisibility(View.GONE);
                txtAfternoonOpen.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE FRIDAY MORNING TIMINGS **/
    private class checkFridayMorning extends AsyncTask<Void, Void, Void>    {

        String friMorFrom = null;
        String friMorTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchFridayMorningTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchFridayMorningTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
                        if (JOTimings.has("friMorFrom") && JOTimings.has("friMorTo"))  {
                            friMorFrom = JOTimings.getString("friMorFrom");
                            friMorTo = JOTimings.getString("friMorTo");
                        } else {
                            friMorFrom = null;
                            friMorTo = null;
                        }
                    }
                } else {
                    friMorFrom = null;
                    friMorTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!friMorFrom.equalsIgnoreCase("null") && !friMorTo.equalsIgnoreCase("null")) {
                txtTimingsMorning.setVisibility(View.VISIBLE);
                txtMorningOpen.setVisibility(View.VISIBLE);
                txtMorningClosed.setVisibility(View.GONE);
                txtTimingsMorning.setText(friMorFrom + " - " + friMorTo);
            } else {
                txtTimingsMorning.setVisibility(View.GONE);
                txtMorningOpen.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE FRIDAY AFTERNOON TIMINGS **/
    private class checkFridayAfternoon extends AsyncTask<Void, Void, Void>  {

        String friAftFrom = null;
        String friAftTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchFridayAfternoonTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchFridayAfternoonTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
                        if (JOTimings.has("friAftFrom") && JOTimings.has("friAftTo"))  {
                            friAftFrom = JOTimings.getString("friAftFrom");
                            friAftTo = JOTimings.getString("friAftTo");
                        } else {
                            friAftFrom = null;
                            friAftTo = null;
                        }
                    }
                } else {
                    friAftFrom = null;
                    friAftTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!friAftFrom.equalsIgnoreCase("null") && !friAftTo.equalsIgnoreCase("null")) {
                linlaTimingAfternoon.setVisibility(View.VISIBLE);
                txtTimingAfternoon.setVisibility(View.VISIBLE);
                txtAfternoonOpen.setVisibility(View.VISIBLE);
                txtAfternoonClosed.setVisibility(View.GONE);
                txtTimingAfternoon.setText(friAftFrom + " - " + friAftTo);
            } else {
                txtTimingAfternoon.setVisibility(View.GONE);
                txtAfternoonOpen.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE SATURDAY MORNING TIMINGS **/
    private class checkSaturdayMorning extends AsyncTask<Void, Void, Void>  {

        String satMorFrom = null;
        String satMorTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchSaturdayMorningTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchSaturdayMorningTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
//                            Log.e("TIMING", String.valueOf(JOTimings));

                        if (JOTimings.has("satMorFrom") && JOTimings.has("satMorTo"))  {
                            satMorFrom = JOTimings.getString("satMorFrom");
                            satMorTo = JOTimings.getString("satMorTo");
                        } else {
                            satMorFrom = null;
                            satMorTo = null;
                        }
                    }
                } else {
                    satMorFrom = null;
                    satMorTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!satMorFrom.equalsIgnoreCase("null") && !satMorTo.equalsIgnoreCase("null")) {
                txtTimingsMorning.setVisibility(View.VISIBLE);
                txtMorningOpen.setVisibility(View.VISIBLE);
                txtMorningClosed.setVisibility(View.GONE);
                txtTimingsMorning.setText(satMorFrom + " - " + satMorTo);
            } else {
                txtTimingsMorning.setVisibility(View.GONE);
                txtMorningOpen.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** CHECK THE SATURDAY AFTERNOON TIMINGS **/
    private class checkSaturdayAfternoon extends AsyncTask<Void, Void, Void>    {

        String satAftFrom = null;
        String satAftTo = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchSaturdayAfternoonTimings";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchSaturdayAfternoonTimings";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    for (int i = 0; i < JATimings.length(); i++) {
                        JSONObject JOTimings = JATimings.getJSONObject(i);
//                            Log.e("TIMING", String.valueOf(JOTimings));

                        if (JOTimings.has("satAftFrom") && JOTimings.has("satAftTo"))  {
                            satAftFrom = JOTimings.getString("satAftFrom");
                            satAftTo = JOTimings.getString("satAftTo");
                        } else {
                            satAftFrom = null;
                            satAftTo = null;
                        }
                    }
                } else {
                    satAftFrom = null;
                    satAftTo = null;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!satAftFrom.equalsIgnoreCase("null") && !satAftTo.equalsIgnoreCase("null")) {
                linlaTimingAfternoon.setVisibility(View.VISIBLE);
                txtTimingAfternoon.setVisibility(View.VISIBLE);
                txtAfternoonOpen.setVisibility(View.VISIBLE);
                txtAfternoonClosed.setVisibility(View.GONE);
                txtTimingAfternoon.setText(satAftFrom + " - " + satAftTo);
            } else {
                txtTimingAfternoon.setVisibility(View.GONE);
                txtAfternoonOpen.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID") && bundle.containsKey("CLINIC_ID"))    {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            CLINIC_ID = bundle.getString("CLINIC_ID");
            if (DOCTOR_ID != null && CLINIC_ID != null)  {
                /** FETCH THE DOCTOR DETAILS **/
                new fetchDoctorDetails().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** SHOW THE CHARGES DIALOG *****/
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

    @Override
    public void onCheckStatus(String response) {
        /** CAST THE RESULT IN THE BOOLEAN FLAG **/
        blnSubscriptionStatus = Boolean.parseBoolean(response);

        /** SET THE CALL / BOOK APPOINTMENT TEXT **/
        if (blnSubscriptionStatus)  {
            btnBook.setText("Book Appointment");
        } else {
            btnBook.setText("Call");
        }

        /** INVALIDATE THE OPTIONS MENU **/
        invalidateOptionsMenu();
    }

    /***** GET THE BOOKMARK STATUS (CHECK IF THE USER HAS BOOKMARKED THE QUESTION) *****/
    private class getSubscriptionStatus extends AsyncTask<Void, Void, String> {
        final SubscriptionStatus delegate;
        boolean blnResult = false;

        getSubscriptionStatus(SubscriptionStatus delegate) {
            this.delegate = delegate;
        }

        @Override
        protected String doInBackground(Void... params) {
            // String URL_BOOKMARK_STATUS = "http://leodyssey.com/ZenPets/public/fetchDoctorSubscription";
            String URL_BOOKMARK_STATUS = "http://192.168.11.2/zenpets/public/fetchDoctorSubscription";
            HttpUrl.Builder builder = HttpUrl.parse(URL_BOOKMARK_STATUS).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            String FINAL_URL = builder.build().toString();
//            Log.e("url", FINAL_URL);
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
                    JSONArray JASubscriptions = JORoot.getJSONArray("subscriptions");
                    if (JASubscriptions.length() > 0) {
                        /** CHECK IF THE SUBSCRIPTION IF GREATER THAN "1" (1 BEING THE FREE TIER) **/
                        for (int i = 0; i < JASubscriptions.length(); i++) {
                            JSONObject JOSubscriptions = JASubscriptions.getJSONObject(i);

                            if (JOSubscriptions.has("subscriptionID"))  {
                                String subscriptionID = JOSubscriptions.getString("subscriptionID");
                                if (subscriptionID.equalsIgnoreCase("1"))   {
                                    blnResult = false;
                                } else {
                                    blnResult = true;
                                }
                            } else {
                                blnResult = false;
                            }
                        }
                    } else {
                        blnResult = false;
                    }
                } else {
                    blnResult = false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return String.valueOf(blnResult);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            delegate.onCheckStatus(s);

            /** INVALIDATE THE OPTIONS MENU **/
            invalidateOptionsMenu();
        }
    }

    /***** FETCH THE DOCTOR'S TIMINGS *****/
    private class fetchDoctorTimings extends AsyncTask<Void, Void, Void>    {

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchDoctorTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchDoctorTimings";
            HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JATimings = JORoot.getJSONArray("timings");
                    if (JATimings.length() > 0) {
                        for (int i = 0; i < JATimings.length(); i++) {
                            JSONObject JOTimings = JATimings.getJSONObject(i);

                            /* GET THE SUNDAY MORNING TIMINGS */
                            if (JOTimings.has("sunMorFrom") && JOTimings.has("sunMorTo")) {
                                if (!JOTimings.getString("sunMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("sunMorTo").equalsIgnoreCase("null"))  {
                                    SUN_MOR_FROM = JOTimings.getString("sunMorFrom");
                                    SUN_MOR_TO = JOTimings.getString("sunMorTo");
                                } else {
                                    SUN_MOR_FROM = null;
                                    SUN_MOR_TO = null;
                                }
                            } else {
                                SUN_MOR_FROM = null;
                                SUN_MOR_TO = null;
                            }

                            /* GET THE SUNDAY AFTERNOON TIMINGS */
                            if (JOTimings.has("sunAftFrom") && JOTimings.has("sunAftTo")) {
                                if (!JOTimings.getString("sunAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("sunAftTo").equalsIgnoreCase("null"))  {
                                    SUN_AFT_FROM = JOTimings.getString("sunAftFrom");
                                    SUN_AFT_TO = JOTimings.getString("sunAftTo");
                                } else {
                                    SUN_AFT_FROM = null;
                                    SUN_AFT_TO = null;
                                }
                            } else {
                                SUN_AFT_FROM = null;
                                SUN_AFT_TO = null;
                            }

                            /* GET THE MONDAY MORNING TIMINGS */
                            if (JOTimings.has("monMorFrom") && JOTimings.has("monMorTo")) {
                                if (!JOTimings.getString("monMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("monMorTo").equalsIgnoreCase("null"))  {
                                    MON_MOR_FROM = JOTimings.getString("monMorFrom");
                                    MON_MOR_TO = JOTimings.getString("monMorTo");
                                } else {
                                    MON_MOR_FROM = null;
                                    MON_MOR_TO = null;
                                }
                            } else {
                                MON_MOR_FROM = null;
                                MON_MOR_TO = null;
                            }

                            /* GET THE MONDAY AFTERNOON TIMINGS */
                            if (JOTimings.has("monAftFrom") && JOTimings.has("monAftTo")) {
                                if (!JOTimings.getString("monAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("monAftTo").equalsIgnoreCase("null"))  {
                                    MON_AFT_FROM = JOTimings.getString("monAftFrom");
                                    MON_AFT_TO = JOTimings.getString("monAftTo");
                                } else {
                                    MON_AFT_FROM = null;
                                    MON_AFT_TO = null;
                                }
                            } else {
                                MON_AFT_FROM = null;
                                MON_AFT_TO = null;
                            }

                            /* GET THE TUESDAY MORNING TIMINGS */
                            if (JOTimings.has("tueMorFrom") && JOTimings.has("tueMorTo")) {
                                if (!JOTimings.getString("tueMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("tueMorTo").equalsIgnoreCase("null"))  {
                                    TUE_MOR_FROM = JOTimings.getString("tueMorFrom");
                                    TUE_MOR_TO = JOTimings.getString("tueMorTo");
                                } else {
                                    TUE_MOR_FROM = null;
                                    TUE_MOR_TO = null;
                                }
                            } else {
                                TUE_MOR_FROM = null;
                                TUE_MOR_TO = null;
                            }

                            /* GET THE TUESDAY AFTERNOON TIMINGS */
                            if (JOTimings.has("tueAftFrom") && JOTimings.has("tueAftTo")) {
                                if (!JOTimings.getString("tueAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("tueAftTo").equalsIgnoreCase("null"))  {
                                    TUE_AFT_FROM = JOTimings.getString("tueAftFrom");
                                    TUE_AFT_TO = JOTimings.getString("tueAftTo");
                                } else {
                                    TUE_AFT_FROM = null;
                                    TUE_AFT_TO = null;
                                }
                            } else {
                                TUE_AFT_FROM = null;
                                TUE_AFT_TO = null;
                            }

                            /* GET THE WEDNESDAY MORNING TIMINGS */
                            if (JOTimings.has("wedMorFrom") && JOTimings.has("wedMorTo")) {
                                if (!JOTimings.getString("wedMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("wedMorTo").equalsIgnoreCase("null"))  {
                                    WED_MOR_FROM = JOTimings.getString("wedMorFrom");
                                    WED_MOR_TO = JOTimings.getString("wedMorTo");
                                } else {
                                    WED_MOR_FROM = null;
                                    WED_MOR_TO = null;
                                }
                            } else {
                                WED_MOR_FROM = null;
                                WED_MOR_TO = null;
                            }

                            /* GET THE WEDNESDAY AFTERNOON TIMINGS */
                            if (JOTimings.has("wedAftFrom") && JOTimings.has("wedAftTo")) {
                                if (!JOTimings.getString("wedAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("wedAftTo").equalsIgnoreCase("null"))  {
                                    WED_AFT_FROM = JOTimings.getString("wedAftFrom");
                                    WED_AFT_TO = JOTimings.getString("wedAftTo");
                                } else {
                                    WED_AFT_FROM = null;
                                    WED_AFT_TO = null;
                                }
                            } else {
                                WED_AFT_FROM = null;
                                WED_AFT_TO = null;
                            }

                            /* GET THE THURSDAY MORNING TIMINGS */
                            if (JOTimings.has("thuMorFrom") && JOTimings.has("thuMorTo")) {
                                if (!JOTimings.getString("thuMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("thuMorTo").equalsIgnoreCase("null"))  {
                                    THU_MOR_FROM = JOTimings.getString("thuMorFrom");
                                    THU_MOR_TO = JOTimings.getString("thuMorTo");
                                } else {
                                    THU_MOR_FROM = null;
                                    THU_MOR_TO = null;
                                }
                            } else {
                                THU_MOR_FROM = null;
                                THU_MOR_TO = null;
                            }

                            /* GET THE THURSDAY AFTERNOON TIMINGS */
                            if (JOTimings.has("thuAftFrom") && JOTimings.has("thuAftTo")) {
                                if (!JOTimings.getString("thuAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("thuAftTo").equalsIgnoreCase("null"))  {
                                    THU_AFT_FROM = JOTimings.getString("thuAftFrom");
                                    THU_AFT_TO = JOTimings.getString("thuAftTo");
                                } else {
                                    THU_AFT_FROM = null;
                                    THU_AFT_TO = null;
                                }
                            } else {
                                THU_AFT_FROM = null;
                                THU_AFT_TO = null;
                            }

                            /* GET THE FRIDAY MORNING TIMINGS */
                            if (JOTimings.has("friMorFrom") && JOTimings.has("friMorTo")) {
                                if (!JOTimings.getString("friMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("friMorTo").equalsIgnoreCase("null"))  {
                                    FRI_MOR_FROM = JOTimings.getString("friMorFrom");
                                    FRI_MOR_TO = JOTimings.getString("friMorTo");
                                } else {
                                    FRI_MOR_FROM = null;
                                    FRI_MOR_TO = null;
                                }
                            } else {
                                FRI_MOR_FROM = null;
                                FRI_MOR_TO = null;
                            }

                            /* GET THE FRIDAY AFTERNOON TIMINGS */
                            if (JOTimings.has("friAftFrom") && JOTimings.has("friAftTo")) {
                                if (!JOTimings.getString("friAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("friAftTo").equalsIgnoreCase("null"))  {
                                    FRI_AFT_FROM = JOTimings.getString("friAftFrom");
                                    FRI_AFT_TO = JOTimings.getString("friAftTo");
                                } else {
                                    FRI_AFT_FROM = null;
                                    FRI_AFT_TO = null;
                                }
                            } else {
                                FRI_AFT_FROM = null;
                                FRI_AFT_TO = null;
                            }

                            /* GET THE SATURDAY MORNING TIMINGS */
                            if (JOTimings.has("satMorFrom") && JOTimings.has("satMorTo")) {
                                if (!JOTimings.getString("satMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("satMorTo").equalsIgnoreCase("null"))  {
                                    SAT_MOR_FROM = JOTimings.getString("satMorFrom");
                                    SAT_MOR_TO = JOTimings.getString("satMorTo");
                                } else {
                                    SAT_MOR_FROM = null;
                                    SAT_MOR_TO = null;
                                }
                            } else {
                                SAT_MOR_FROM = null;
                                SAT_MOR_TO = null;
                            }

                            /* GET THE SATURDAY AFTERNOON TIMINGS */
                            if (JOTimings.has("satAftFrom") && JOTimings.has("satAftTo")) {
                                if (!JOTimings.getString("satAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("satAftTo").equalsIgnoreCase("null"))  {
                                    SAT_AFT_FROM = JOTimings.getString("satAftFrom");
                                    SAT_AFT_TO = JOTimings.getString("satAftTo");
                                } else {
                                    SAT_AFT_FROM = null;
                                    SAT_AFT_TO = null;
                                }
                            } else {
                                SAT_AFT_FROM = null;
                                SAT_AFT_TO = null;
                            }
                        }
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

            /* SET THE SUNDAY MORNING TIMINGS */
            if (SUN_MOR_FROM != null && SUN_MOR_TO != null) {
                txtSunMorning.setText(SUN_MOR_FROM + " - " + SUN_MOR_TO);
            } else {
                txtSunMorning.setText("Closed");
            }

            /* SET THE SUNDAY AFTERNOON TIMINGS */
            if (SUN_AFT_FROM != null && SUN_AFT_TO != null) {
                txtSunAfternoon.setText(SUN_AFT_FROM + " - " + SUN_AFT_TO);
            } else {
                txtSunAfternoon.setText("Closed");
            }

            /* SET THE MONDAY MORNING TIMINGS */
            if (MON_MOR_FROM != null && MON_MOR_TO != null) {
                txtMonMorning.setText(MON_MOR_FROM + " - " + MON_MOR_TO);
            } else {
                txtMonMorning.setText("Closed");
            }

            /* SET THE MONDAY AFTERNOON TIMINGS */
            if (MON_AFT_FROM != null && MON_AFT_TO != null) {
                txtMonAfternoon.setText(MON_AFT_FROM + " - " + MON_AFT_TO);
            } else {
                txtMonAfternoon.setText("Closed");
            }

            /* SET THE TUESDAY MORNING TIMINGS */
            if (TUE_MOR_FROM != null && TUE_MOR_TO != null) {
                txtTueMorning.setText(TUE_MOR_FROM + " - " + TUE_MOR_TO);
            } else {
                txtTueMorning.setText("Closed");
            }

            /* SET THE TUESDAY AFTERNOON TIMINGS */
            if (TUE_AFT_FROM != null && TUE_AFT_TO != null) {
                txtTueAfternoon.setText(TUE_AFT_FROM + " - " + TUE_AFT_TO);
            } else {
                txtTueAfternoon.setText("Closed");
            }

            /* SET THE WEDNESDAY MORNING TIMINGS */
            if (WED_MOR_FROM != null && WED_MOR_TO != null) {
                txtWedMorning.setText(WED_MOR_FROM + " - " + WED_MOR_TO);
            } else {
                txtWedMorning.setText("Closed");
            }

            /* SET THE WEDNESDAY AFTERNOON TIMINGS */
            if (WED_AFT_FROM != null && WED_AFT_TO != null) {
                txtWedAfternoon.setText(WED_AFT_FROM + " - " + WED_AFT_TO);
            } else {
                txtWedAfternoon.setText("Closed");
            }

            /* SET THE THURSDAY MORNING TIMINGS */
            if (THU_MOR_FROM != null && THU_MOR_TO != null)   {
                txtThuMorning.setText(THU_MOR_FROM + " - " + THU_MOR_TO);
            } else {
                txtThuMorning.setText("Closed");
            }

            /* SET THE THURSDAY AFTERNOON TIMINGS */
            if (THU_AFT_FROM != null && THU_AFT_TO != null) {
                txtThuAfternoon.setText(THU_AFT_FROM + " - " + THU_AFT_TO);
            } else {
                txtThuAfternoon.setText("Closed");
            }

            /* SET THE FRIDAY MORNING TIMINGS */
            if (FRI_MOR_FROM != null && FRI_MOR_TO != null) {
                txtFriMorning.setText(FRI_MOR_FROM + " - " + FRI_MOR_TO);
            } else {
                txtFriMorning.setText("Closed");
            }

            /* SET THE FRIDAY AFTERNOON TIMINGS */
            if (FRI_AFT_FROM != null && FRI_AFT_TO != null) {
                txtFriAfternoon.setText(FRI_AFT_FROM + " - " + FRI_AFT_TO);
            } else {
                txtFriAfternoon.setText("Closed");
            }

            /* SET THE SATURDAY MORNING TIMINGS */
            if (SAT_MOR_FROM != null && SAT_MOR_TO != null) {
                txtSatMorning.setText(SAT_MOR_FROM + " - " + SAT_MOR_TO);
            } else {
                txtSatMorning.setText("Closed");
            }

            /* SET THE SATURDAY AFTERNOON TIMINGS */
            if (SAT_AFT_FROM != null && SAT_AFT_TO != null) {
                txtSatAfternoon.setText(SAT_AFT_FROM + " - " + SAT_AFT_TO);
            } else {
                txtSatAfternoon.setText("Closed");
            }
        }
    }

    /***** SHOW ALL TIMINGS *****/
    @SuppressWarnings("ConstantConditions")
    private void showDoctorTimings() {
        MaterialDialog dialog = new MaterialDialog.Builder(DoctorDetails.this)
                .typeface("Roboto-Regular.ttf", "Roboto-Light.ttf")
                .title("ALL TIMINGS")
                .customView(custAllTimings, false)
                .positiveText("Dismiss")
                .build();

        /** CAST THE LAYOUT ELEMENTS **/
        txtSunMorning = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtSunMorning);
        txtSunAfternoon = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtSunAfternoon);
        txtMonMorning = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtMonMorning);
        txtMonAfternoon = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtMonAfternoon);
        txtTueMorning = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtTueMorning);
        txtTueAfternoon = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtTueAfternoon);
        txtWedMorning = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtWedMorning);
        txtWedAfternoon = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtWedAfternoon);
        txtThuMorning = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtThuMorning);
        txtThuAfternoon = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtThuAfternoon);
        txtFriMorning = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtFriMorning);
        txtFriAfternoon = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtFriAfternoon);
        txtSatMorning = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtSatMorning);
        txtSatAfternoon = (AppCompatTextView) dialog.getCustomView().findViewById(R.id.txtSatAfternoon);

        /* FETCH THE DOCTOR'S TIMINGS */
        new fetchDoctorTimings().execute();

        /** SHOW THE DIALOG **/
        dialog.show();
    }

    /***** SHOW ALL SERVICES *****/
//    private void showDoctorServices() {
//        /** CLEAR THE ARRAY LIST **/
//        arrServices.clear();
//
//        /** CONFIGURE THE DIALOG **/
//        MaterialDialog dialog = new MaterialDialog.Builder(DoctorDetails.this)
//                .theme(Theme.LIGHT)
//                .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
//                .title("ALL SERVICES")
//                .customView(custAllServices, false)
//                .positiveText("Dismiss")
//                .build();
//
//        /** CAST AND CONFIGURE THE RECYCLER VIEW **/
//        listDoctorServices = (RecyclerView) dialog.getCustomView().findViewById(R.id.listDoctorServices);
//        LinearLayoutManager llmServices = new LinearLayoutManager(this);
//        llmServices.setOrientation(LinearLayoutManager.VERTICAL);
//        listDoctorServices.setLayoutManager(llmServices);
//
//        /** FETCH THE LIST OF SERVICES **/
//        new fetchDoctorServices().execute();
//
//        /** SHOW THE DIALOG **/
//        dialog.show();
//    }

    /***** FETCH THE LIST OF SERVICES *****/
//    private class fetchDoctorServices extends AsyncTask<Void, Void, Void>   {
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            // String URL_DOCTOR_SERVICES = "http://leodyssey.com/ZenPets/public/fetchDoctorServices";
//            String URL_DOCTOR_SERVICES = "http://192.168.11.2/zenpets/public/fetchDoctorServices";
//            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_SERVICES).newBuilder();
//            builder.addQueryParameter("doctorID", DOCTOR_ID);
//            String FINAL_URL = builder.build().toString();
//            OkHttpClient client = new OkHttpClient();
//            Request request = new Request.Builder()
//                    .url(FINAL_URL)
//                    .build();
//            Call call = client.newCall(request);
//            try {
//                Response response = call.execute();
//                String strResult = response.body().string();
//                JSONObject JORoot = new JSONObject(strResult);
//                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
//                    JSONArray JAServices = JORoot.getJSONArray("services");
//                    if (JAServices.length() > 0) {
//                        /* A SERVICES DATA INSTANCE */
//                        ServicesData data;
//                        for (int i = 0; i < JAServices.length(); i++) {
//                            JSONObject JOServices = JAServices.getJSONObject(i);
//
//                            /** INSTANTIATE THE SERVICES DATA INSTANCE **/
//                            data = new ServicesData();
//
//                            /** GET THE SERVICE NAME **/
//                            if (JOServices.has("doctorServiceName"))    {
//                                data.setServiceName(JOServices.getString("doctorServiceName"));
//                            } else {
//                                data.setServiceName(null);
//                            }
//
//                            /** ADD THE COLLECTED DATA TO THE ARRAY LIST **/
//                            arrServices.add(data);
//                        }
//                    }
//                }
//            } catch (IOException | JSONException e) {
//                e.printStackTrace();
//            }
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//
//            /** INSTANTIATE THE ADAPTER **/
//            servicesAdapter = new ServicesAdapter(DoctorDetails.this, arrServices);
//
//            /** SET THE SERVICES ADAPTER TO THE RECYCLER VIEW **/
//            listDoctorServices.setAdapter(servicesAdapter);
//        }
//    }

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

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
//        LinearLayoutManager services = new LinearLayoutManager(this);
//        services.setOrientation(LinearLayoutManager.VERTICAL);
//        listServices.setLayoutManager(services);
//        listServices.setHasFixedSize(true);
//        listServices.setAdapter(servicesAdapter);

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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == CALL_PHONE_CONSTANT)   {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)    {
                /* CALL THE PHONE NUMBER */
                callPhone();
            } else {
                /* DIAL THE PHONE NUMBER */
                dialPhone();
            }
        }
    }

    /***** CALL THE PHONE NUMBER *****/
    @SuppressWarnings("MissingPermission")
    private void callPhone() {
        String myData= "tel:" + DOCTOR_PHONE_NUMBER;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(myData));
        startActivity(intent);
    }

    /***** DIAL THE PHONE NUMBER *****/
    private void dialPhone() {
        String myData= "tel:" + DOCTOR_PHONE_NUMBER;
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse(myData));
        startActivity(intent);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}