package com.zenpets.doctors.creators.appointment;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.doctors.slots.AfternoonTimeSlotsAdapter;
import com.zenpets.doctors.utils.adapters.doctors.slots.MorningTimeSlotsAdapter;
import com.zenpets.doctors.utils.models.doctors.slots.AfternoonTimeSlotsData;
import com.zenpets.doctors.utils.models.doctors.slots.MorningTimeSlotsData;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import noman.weekcalendar.WeekCalendar;
import noman.weekcalendar.listener.OnDateClickListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AppointmentSlotCreator extends AppCompatActivity {

    /** THE INCOMING DOCTOR ID AND CLINIC ID **/
    String DOCTOR_ID = null;
    String CLINIC_ID = null;

    /** THE CURRENT DATE AND THE SELECTED DAY **/
    String CURRENT_DATE = null;
    String SELECTED_DAY = null;

    /** THE START TIME AND END TIME **/
    String MORNING_START_TIME = null;
    String MORNING_END_TIME = null;
    String AFTERNOON_START_TIME = null;
    String AFTERNOON_END_TIME = null;

    /** THE MORNING TIME SLOTS ADAPTER AND ARRAY LIST **/
    MorningTimeSlotsAdapter morningTimeSlotsAdapter;
    ArrayList<MorningTimeSlotsData> arrMorningSlots = new ArrayList<>();
    MorningTimeSlotsData morningData;

    /** THE AFTERNOON TIME SLOTS ADAPTER AND ARRAY LIST **/
    AfternoonTimeSlotsAdapter afternoonTimeSlotsAdapter;
    ArrayList<AfternoonTimeSlotsData> arrAfternoonSlots = new ArrayList<>();
    AfternoonTimeSlotsData afternoonData;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.weekCalendar) WeekCalendar weekCalendar;
    @BindView(R.id.imgvwDoctorProfile) CircleImageView imgvwDoctorProfile;
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.txtClinicDetails) AppCompatTextView txtClinicDetails;
    @BindView(R.id.listMorningTimes) RecyclerView listMorningTimes;
    @BindView(R.id.txtMorningClosed) AppCompatTextView txtMorningClosed;
    @BindView(R.id.listAfternoonTimes) RecyclerView listAfternoonTimes;
    @BindView(R.id.txtAfternoonClosed) AppCompatTextView txtAfternoonClosed;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment_slot_chooser);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* GET THE SELECTED DATE */
        weekCalendar.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(DateTime dateTime) {
                Date date = dateTime.toDate();
//                Log.e("DATE", String.valueOf(date));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                SimpleDateFormat currentDay = new SimpleDateFormat("EE", Locale.getDefault());
                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SELECTED_DAY = currentDay.format(cal.getTime());
                CURRENT_DATE = currentDate.format(cal.getTime());
//                Log.e("CURRENT DATE", CURRENT_DATE);

                /* FETCH THE MORNING TIME SLOTS  */
                if (SELECTED_DAY.equalsIgnoreCase("sun"))   {
                    fetchSundayMorningSlots();
                    fetchSundayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("mon"))    {
                    fetchMondayMorningSlots();
                    fetchMondayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("tue"))    {
                    fetchTuesdayMorningSlots();
                    fetchTuesdayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("wed"))    {
                    fetchWednesdayMorningSlots();
                    fetchWednesdayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("thu"))    {
                    fetchThursdayMorningSlots();
                    fetchThursdayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("fri"))    {
                    fetchFridayMorningSlots();
                    fetchFridayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("sat"))    {
                    fetchSaturdayMorningSlots();
                    fetchSaturdayAfternoonSlots();
                }
            }
        });
    }

    /* GET THE INCOMING DATA */
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID") && bundle.containsKey("CLINIC_ID")) {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            CLINIC_ID = bundle.getString("CLINIC_ID");
            if (DOCTOR_ID != null && CLINIC_ID != null) {
                /* FETCH THE DOCTOR DETAILS */
                fetchDoctorDetails();

                /* FETCH THE CLINIC DETAILS */
                fetchClinicDetails();

                /* GET TODAY'S DAY */
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat currentDay = new SimpleDateFormat("EE", Locale.getDefault());
                SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SELECTED_DAY = currentDay.format(cal.getTime());
                CURRENT_DATE = currentDate.format(cal.getTime());
//                Log.e("CURRENT DATE", CURRENT_DATE);

                /* FETCH THE MORNING TIME SLOTS  */
                if (SELECTED_DAY.equalsIgnoreCase("sun"))   {
                    fetchSundayMorningSlots();
                    fetchSundayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("mon"))    {
                    fetchMondayMorningSlots();
                    fetchMondayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("tue"))    {
                    fetchTuesdayMorningSlots();
                    fetchTuesdayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("wed"))    {
                    fetchWednesdayMorningSlots();
                    fetchWednesdayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("thu"))    {
                    fetchThursdayMorningSlots();
                    fetchThursdayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("fri"))    {
                    fetchFridayMorningSlots();
                    fetchFridayAfternoonSlots();
                } else if (SELECTED_DAY.equalsIgnoreCase("sat"))    {
                    fetchSaturdayMorningSlots();
                    fetchSaturdayAfternoonSlots();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required data", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** FETCH THE DOCTOR DETAILS *****/
    private void fetchDoctorDetails() {
        String URL_USER_DETAILS = "http://leodyssey.com/ZenPets/public/fetchDoctorProfile";
        // String URL_USER_DETAILS = "http://192.168.11.2/zenpets/public/fetchDoctorProfile";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DETAILS).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("PROFILE URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
//                    Log.e("ROOT", String.valueOf(JORoot));
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JADoctors = JORoot.getJSONArray("doctors");
                        for (int i = 0; i < JADoctors.length(); i++) {
                            JSONObject JODoctors = JADoctors.getJSONObject(i);

                            /* GET THE DOCTOR'S PREFIX */
                            if (JODoctors.has("doctorPrefix") && JODoctors.has("doctorName")) {
                                final String DOCTOR_PREFIX = JODoctors.getString("doctorPrefix");
                                final String DOCTOR_NAME = JODoctors.getString("doctorName");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtDoctorName.setText(DOCTOR_PREFIX + " " + DOCTOR_NAME);
                                    }
                                });
                            }

                            /* GET THE USER DISPLAY PROFILE */
                            if (JODoctors.has("doctorDisplayProfile")) {
                                final String DOCTOR_DISPLAY_PROFILE = JODoctors.getString("doctorDisplayProfile");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(AppointmentSlotCreator.this)
                                                .load(DOCTOR_DISPLAY_PROFILE)
                                                .crossFade()
                                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                                .centerCrop()
                                                .into(imgvwDoctorProfile);
                                    }
                                });
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** FETCH THE CLINIC DETAILS *****/
    private void fetchClinicDetails() {
        String URL_USER_CLINICS = "http://leodyssey.com/ZenPets/public/checkDoctorClinic";
        // String URL_USER_CLINICS = "http://192.168.11.2/zenpets/public/checkDoctorClinic";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_CLINICS).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        String FINAL_URL = builder.build().toString();
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        final JSONArray JAClinics = JORoot.getJSONArray("clinics");
                        if (JAClinics.length() > 0) {
                            for (int i = 0; i < JAClinics.length(); i++) {
                                JSONObject JOClinics = JAClinics.getJSONObject(i);

                                /* SET THE CLINIC DETAILS */
                                if (JOClinics.has("clinicName") && JOClinics.has("cityName") && JOClinics.has("localityName")) {
                                    final String clinicName = JOClinics.getString("clinicName");
                                    String cityName = JOClinics.getString("cityName");
                                    String localityName = JOClinics.getString("localityName");
                                    final String finalAddress = localityName + ", " + cityName;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtClinicDetails.setText(clinicName + ", " + finalAddress);
                                        }
                                    });
                                }
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE SUNDAY MORNING SLOTS **/
    private void fetchSundayMorningSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchSundayMorningTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchSundayMorningTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE MORNING START TIME (FROM) */
                                if (JOTimings.has("sunMorFrom"))    {
                                    MORNING_START_TIME = JOTimings.getString("sunMorFrom");
                                } else {
                                    MORNING_START_TIME = null;
                                }

                                /* GET THE MORNING END TIME (TO) */
                                if (JOTimings.has("sunMorTo"))  {
                                    MORNING_END_TIME = JOTimings.getString("sunMorTo");
                                } else {
                                    MORNING_END_TIME = null;
                                }
                            }
                        } else {
                            MORNING_START_TIME = null;
                            MORNING_END_TIME = null;
                        }
                    } else {
                        MORNING_START_TIME = null;
                        MORNING_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayMorningSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listMorningTimes.setVisibility(View.GONE);
                                txtMorningClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE SUNDAY AFTERNOON SLOTS **/
    private void fetchSundayAfternoonSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchSundayAfternoonTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchSundayAfternoonTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE AFTERNOON START TIME (FROM) */
                                if (JOTimings.has("sunAftFrom"))    {
                                    AFTERNOON_START_TIME = JOTimings.getString("sunAftFrom");
                                } else {
                                    AFTERNOON_START_TIME = null;
                                }

                                /* GET THE AFTERNOON END TIME (TO) */
                                if (JOTimings.has("sunAftTo"))  {
                                    AFTERNOON_END_TIME = JOTimings.getString("sunAftTo");
                                } else {
                                    AFTERNOON_END_TIME = null;
                                }
                            }
                        } else {
                            AFTERNOON_START_TIME = null;
                            AFTERNOON_END_TIME = null;
                        }
                    } else {
                        AFTERNOON_START_TIME = null;
                        AFTERNOON_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayAfternoonSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAfternoonTimes.setVisibility(View.GONE);
                                txtAfternoonClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE MONDAY MORNING SLOTS **/
    private void fetchMondayMorningSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchMondayMorningTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchMondayMorningTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE MORNING START TIME (FROM) */
                                if (JOTimings.has("monMorFrom"))    {
                                    MORNING_START_TIME = JOTimings.getString("monMorFrom");
                                } else {
                                    MORNING_START_TIME = null;
                                }

                                /* GET THE MORNING END TIME (TO) */
                                if (JOTimings.has("monMorTo"))  {
                                    MORNING_END_TIME = JOTimings.getString("monMorTo");
                                } else {
                                    MORNING_END_TIME = null;
                                }
                            }
                        } else {
                            MORNING_START_TIME = null;
                            MORNING_END_TIME = null;
                        }
                    } else {
                        MORNING_START_TIME = null;
                        MORNING_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayMorningSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listMorningTimes.setVisibility(View.GONE);
                                txtMorningClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE MONDAY AFTERNOON SLOTS **/
    private void fetchMondayAfternoonSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchMondayAfternoonTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchMondayAfternoonTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE AFTERNOON START TIME (FROM) */
                                if (JOTimings.has("monAftFrom"))    {
                                    AFTERNOON_START_TIME = JOTimings.getString("monAftFrom");
                                } else {
                                    AFTERNOON_START_TIME = null;
                                }

                                /* GET THE AFTERNOON END TIME (TO) */
                                if (JOTimings.has("monAftTo"))  {
                                    AFTERNOON_END_TIME = JOTimings.getString("monAftTo");
                                } else {
                                    AFTERNOON_END_TIME = null;
                                }
                            }
                        } else {
                            AFTERNOON_START_TIME = null;
                            AFTERNOON_END_TIME = null;
                        }
                    } else {
                        AFTERNOON_START_TIME = null;
                        AFTERNOON_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayAfternoonSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAfternoonTimes.setVisibility(View.GONE);
                                txtAfternoonClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE TUESDAY MORNING SLOTS **/
    private void fetchTuesdayMorningSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchTuesdayMorningTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchTuesdayMorningTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE MORNING START TIME (FROM) */
                                if (JOTimings.has("tueMorFrom"))    {
                                    MORNING_START_TIME = JOTimings.getString("tueMorFrom");
                                } else {
                                    MORNING_START_TIME = null;
                                }

                                /* GET THE MORNING END TIME (TO) */
                                if (JOTimings.has("tueMorTo"))  {
                                    MORNING_END_TIME = JOTimings.getString("tueMorTo");
                                } else {
                                    MORNING_END_TIME = null;
                                }
                            }
                        } else {
                            MORNING_START_TIME = null;
                            MORNING_END_TIME = null;
                        }
                    } else {
                        MORNING_START_TIME = null;
                        MORNING_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayMorningSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listMorningTimes.setVisibility(View.GONE);
                                txtMorningClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE TUESDAY AFTERNOON SLOTS **/
    private void fetchTuesdayAfternoonSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchTuesdayAfternoonTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchTuesdayAfternoonTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE AFTERNOON START TIME (FROM) */
                                if (JOTimings.has("tueAftFrom"))    {
                                    AFTERNOON_START_TIME = JOTimings.getString("tueAftFrom");
                                } else {
                                    AFTERNOON_START_TIME = null;
                                }

                                /* GET THE AFTERNOON END TIME (TO) */
                                if (JOTimings.has("tueAftTo"))  {
                                    AFTERNOON_END_TIME = JOTimings.getString("tueAftTo");
                                } else {
                                    AFTERNOON_END_TIME = null;
                                }
                            }
                        } else {
                            AFTERNOON_START_TIME = null;
                            AFTERNOON_END_TIME = null;
                        }
                    } else {
                        AFTERNOON_START_TIME = null;
                        AFTERNOON_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayAfternoonSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAfternoonTimes.setVisibility(View.GONE);
                                txtAfternoonClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE WEDNESDAY MORNING SLOTS **/
    private void fetchWednesdayMorningSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchWednesdayMorningTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchWednesdayMorningTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE MORNING START TIME (FROM) */
                                if (JOTimings.has("wedMorFrom"))    {
                                    MORNING_START_TIME = JOTimings.getString("wedMorFrom");
                                } else {
                                    MORNING_START_TIME = null;
                                }

                                /* GET THE MORNING END TIME (TO) */
                                if (JOTimings.has("wedMorTo"))  {
                                    MORNING_END_TIME = JOTimings.getString("wedMorTo");
                                } else {
                                    MORNING_END_TIME = null;
                                }
                            }
                        } else {
                            MORNING_START_TIME = null;
                            MORNING_END_TIME = null;
                        }
                    } else {
                        MORNING_START_TIME = null;
                        MORNING_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayMorningSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listMorningTimes.setVisibility(View.GONE);
                                txtMorningClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE WEDNESDAY AFTERNOON SLOTS **/
    private void fetchWednesdayAfternoonSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchWednesdayAfternoonTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchWednesdayAfternoonTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE AFTERNOON START TIME (FROM) */
                                if (JOTimings.has("wedAftFrom"))    {
                                    AFTERNOON_START_TIME = JOTimings.getString("wedAftFrom");
                                } else {
                                    AFTERNOON_START_TIME = null;
                                }

                                /* GET THE AFTERNOON END TIME (TO) */
                                if (JOTimings.has("wedAftTo"))  {
                                    AFTERNOON_END_TIME = JOTimings.getString("wedAftTo");
                                } else {
                                    AFTERNOON_END_TIME = null;
                                }
                            }
                        } else {
                            AFTERNOON_START_TIME = null;
                            AFTERNOON_END_TIME = null;
                        }
                    } else {
                        AFTERNOON_START_TIME = null;
                        AFTERNOON_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayAfternoonSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAfternoonTimes.setVisibility(View.GONE);
                                txtAfternoonClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE THURSDAY MORNING SLOTS **/
    private void fetchThursdayMorningSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchThursdayMorningTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchThursdayMorningTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE MORNING START TIME (FROM) */
                                if (JOTimings.has("thuMorFrom"))    {
                                    MORNING_START_TIME = JOTimings.getString("thuMorFrom");
                                } else {
                                    MORNING_START_TIME = null;
                                }

                                /* GET THE MORNING END TIME (TO) */
                                if (JOTimings.has("thuMorTo"))  {
                                    MORNING_END_TIME = JOTimings.getString("thuMorTo");
                                } else {
                                    MORNING_END_TIME = null;
                                }
                            }
                        } else {
                            MORNING_START_TIME = null;
                            MORNING_END_TIME = null;
                        }
                    } else {
                        MORNING_START_TIME = null;
                        MORNING_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayMorningSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listMorningTimes.setVisibility(View.GONE);
                                txtMorningClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE THURSDAY AFTERNOON SLOTS **/
    private void fetchThursdayAfternoonSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchThursdayAfternoonTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchThursdayAfternoonTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE AFTERNOON START TIME (FROM) */
                                if (JOTimings.has("thuAftFrom"))    {
                                    AFTERNOON_START_TIME = JOTimings.getString("thuAftFrom");
                                } else {
                                    AFTERNOON_START_TIME = null;
                                }

                                /* GET THE AFTERNOON END TIME (TO) */
                                if (JOTimings.has("thuAftTo"))  {
                                    AFTERNOON_END_TIME = JOTimings.getString("thuAftTo");
                                } else {
                                    AFTERNOON_END_TIME = null;
                                }
                            }
                        } else {
                            AFTERNOON_START_TIME = null;
                            AFTERNOON_END_TIME = null;
                        }
                    } else {
                        AFTERNOON_START_TIME = null;
                        AFTERNOON_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayAfternoonSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAfternoonTimes.setVisibility(View.GONE);
                                txtAfternoonClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE FRIDAY MORNING SLOTS **/
    private void fetchFridayMorningSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchFridayMorningTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchFridayMorningTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE MORNING START TIME (FROM) */
                                if (JOTimings.has("friMorFrom"))    {
                                    MORNING_START_TIME = JOTimings.getString("friMorFrom");
                                } else {
                                    MORNING_START_TIME = null;
                                }

                                /* GET THE MORNING END TIME (TO) */
                                if (JOTimings.has("friMorTo"))  {
                                    MORNING_END_TIME = JOTimings.getString("friMorTo");
                                } else {
                                    MORNING_END_TIME = null;
                                }
                            }
                        } else {
                            MORNING_START_TIME = null;
                            MORNING_END_TIME = null;
                        }
                    } else {
                        MORNING_START_TIME = null;
                        MORNING_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayMorningSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listMorningTimes.setVisibility(View.GONE);
                                txtMorningClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE FRIDAY AFTERNOON SLOTS **/
    private void fetchFridayAfternoonSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchFridayAfternoonTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchFridayAfternoonTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE AFTERNOON START TIME (FROM) */
                                if (JOTimings.has("friAftFrom"))    {
                                    AFTERNOON_START_TIME = JOTimings.getString("friAftFrom");
                                } else {
                                    AFTERNOON_START_TIME = null;
                                }

                                /* GET THE AFTERNOON END TIME (TO) */
                                if (JOTimings.has("friAftTo"))  {
                                    AFTERNOON_END_TIME = JOTimings.getString("friAftTo");
                                } else {
                                    AFTERNOON_END_TIME = null;
                                }
                            }
                        } else {
                            AFTERNOON_START_TIME = null;
                            AFTERNOON_END_TIME = null;
                        }
                    } else {
                        AFTERNOON_START_TIME = null;
                        AFTERNOON_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayAfternoonSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAfternoonTimes.setVisibility(View.GONE);
                                txtAfternoonClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE SATURDAY MORNING SLOTS **/
    private void fetchSaturdayMorningSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchSaturdayMorningTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchSaturdayMorningTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE MORNING START TIME (FROM) */
                                if (JOTimings.has("satMorFrom"))    {
                                    MORNING_START_TIME = JOTimings.getString("satMorFrom");
                                } else {
                                    MORNING_START_TIME = null;
                                }

                                /* GET THE MORNING END TIME (TO) */
                                if (JOTimings.has("satMorTo"))  {
                                    MORNING_END_TIME = JOTimings.getString("satMorTo");
                                } else {
                                    MORNING_END_TIME = null;
                                }
                            }
                        } else {
                            MORNING_START_TIME = null;
                            MORNING_END_TIME = null;
                        }
                    } else {
                        MORNING_START_TIME = null;
                        MORNING_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayMorningSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listMorningTimes.setVisibility(View.GONE);
                                txtMorningClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH THE SATURDAY AFTERNOON SLOTS **/
    private void fetchSaturdayAfternoonSlots() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchSaturdayAfternoonTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchSaturdayAfternoonTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("URL", FINAL_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(FINAL_URL)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE AFTERNOON START TIME (FROM) */
                                if (JOTimings.has("satAftFrom"))    {
                                    AFTERNOON_START_TIME = JOTimings.getString("satAftFrom");
                                } else {
                                    AFTERNOON_START_TIME = null;
                                }

                                /* GET THE AFTERNOON END TIME (TO) */
                                if (JOTimings.has("satAftTo"))  {
                                    AFTERNOON_END_TIME = JOTimings.getString("satAftTo");
                                } else {
                                    AFTERNOON_END_TIME = null;
                                }
                            }
                        } else {
                            AFTERNOON_START_TIME = null;
                            AFTERNOON_END_TIME = null;
                        }
                    } else {
                        AFTERNOON_START_TIME = null;
                        AFTERNOON_END_TIME = null;
                    }

                    /* CALCULATE THE TIME SLOTS */
                    if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                        /* DISPLAY THE MORNING SLOTS */
                        new displayAfternoonSlots().execute();
                    } else {
                        /* HIDE THE LIST */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAfternoonTimes.setVisibility(View.GONE);
                                txtAfternoonClosed.setVisibility(View.VISIBLE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** DISPLAY THE MORNING SLOTS  **/
    private class displayMorningSlots extends AsyncTask<Void, Void, Void>  {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* CLEAR THE ARRAY LIST */
            arrMorningSlots.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            try {
                final Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(sdf.parse(CURRENT_DATE + " " + MORNING_START_TIME));
                if (startCalendar.get(Calendar.MINUTE) < 15) {
                    startCalendar.set(Calendar.MINUTE, 0);
                } else {
                    startCalendar.add(Calendar.MINUTE, 15); // overstep hour and clear minutes
                    startCalendar.clear(Calendar.MINUTE);
                }

                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(sdf.parse(CURRENT_DATE + " " + MORNING_END_TIME));
                endCalendar.add(Calendar.HOUR_OF_DAY, 0);

                endCalendar.clear(Calendar.MINUTE);
                endCalendar.clear(Calendar.SECOND);
                endCalendar.clear(Calendar.MILLISECOND);

                final SimpleDateFormat slotTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                while (endCalendar.after(startCalendar)) {
                    morningData = new MorningTimeSlotsData();

                    /* SET THE TIME SLOT */
                    String slotStartTime = slotTime.format(startCalendar.getTime());
                    morningData.setAppointmentTime(slotStartTime);
                    startCalendar.add(Calendar.MINUTE, 15);
//                    Log.e("QUERY TIME", slotStartTime);

                    /* SET THE DOCTOR ID */
                    morningData.setDoctorID(DOCTOR_ID);

                    /* SET THE CLINIC ID */
                    morningData.setClinicID(CLINIC_ID);

                    /* SET THE APPOINTMENT DATE */
                    morningData.setAppointmentDate(CURRENT_DATE);

                    String URL_APPOINTMENT_STATUS = "http://leodyssey.com/ZenPets/public/checkAvailability";
                    // String URL_APPOINTMENT_STATUS = "http://192.168.11.2/zenpets/public/checkAvailability";
                    HttpUrl.Builder builder = HttpUrl.parse(URL_APPOINTMENT_STATUS).newBuilder();
                    builder.addQueryParameter("doctorID", DOCTOR_ID);
                    builder.addQueryParameter("clinicID", CLINIC_ID);
                    builder.addQueryParameter("appointmentDate", CURRENT_DATE);
                    builder.addQueryParameter("appointmentTime", slotStartTime);
                    String FINAL_URL = builder.build().toString();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(FINAL_URL)
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JAAppointments = JORoot.getJSONArray("appointments");
//                        Log.e("AVAILABILITY", String.valueOf(JAAppointments));
                        if (JAAppointments.length() > 0)    {
                            morningData.setAppointmentStatus("Unavailable");
//                            Log.e("STATUS", "Unavailable");
                        } else {
                            morningData.setAppointmentStatus("Available");
//                            Log.e("STATUS", "Available");
                        }
                    }

                    /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                    arrMorningSlots.add(morningData);
                }
            } catch (ParseException e) {
                Log.e("MORNING EXCEPTION", e.getMessage());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* CONFIGURE THE MORNING RECYCLER VIEW */
            configMorningRecycler();

            /* INSTANTIATE THE TIME SLOTS ADAPTER */
            morningTimeSlotsAdapter = new MorningTimeSlotsAdapter(AppointmentSlotCreator.this, arrMorningSlots);

            /* SET THE TIME SLOTS ADAPTER TO THE AFTERNOON RECYCLER VIEW */
            listMorningTimes.setAdapter(morningTimeSlotsAdapter);

            /* SET THE LIST VISIBILITY */
            listMorningTimes.setVisibility(View.VISIBLE);
            txtMorningClosed.setVisibility(View.GONE);
        }
    }

    /** DISPLAY THE AFTERNOON SLOTS **/
    private class displayAfternoonSlots extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* CLEAR THE ARRAY LIST */
            arrAfternoonSlots.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
            try {
                final Calendar startCalendar = Calendar.getInstance();
                startCalendar.setTime(sdf.parse(CURRENT_DATE + " " + AFTERNOON_START_TIME));
                if (startCalendar.get(Calendar.MINUTE) < 15) {
                    startCalendar.set(Calendar.MINUTE, 0);
                } else {
                    startCalendar.add(Calendar.MINUTE, 15); // overstep hour and clear minutes
                    startCalendar.clear(Calendar.MINUTE);
                }

                Calendar endCalendar = Calendar.getInstance();
                endCalendar.setTime(sdf.parse(CURRENT_DATE + " " + AFTERNOON_END_TIME));
                endCalendar.add(Calendar.HOUR_OF_DAY, 0);

                endCalendar.clear(Calendar.MINUTE);
                endCalendar.clear(Calendar.SECOND);
                endCalendar.clear(Calendar.MILLISECOND);

                final SimpleDateFormat slotTime = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                while (endCalendar.after(startCalendar)) {
                    afternoonData = new AfternoonTimeSlotsData();

                    /* SET THE TIME SLOT */
                    String slotStartTime = slotTime.format(startCalendar.getTime());
                    afternoonData.setAppointmentTime(slotStartTime);
                    startCalendar.add(Calendar.MINUTE, 15);
//                    Log.e("QUERY TIME", slotStartTime);

                    /* SET THE DOCTOR ID */
                    afternoonData.setDoctorID(DOCTOR_ID);

                    /* SET THE CLINIC ID */
                    afternoonData.setClinicID(CLINIC_ID);

                    /* SET THE APPOINTMENT DATE */
                    afternoonData.setAppointmentDate(CURRENT_DATE);

                    String URL_APPOINTMENT_STATUS = "http://leodyssey.com/ZenPets/public/checkAvailability";
                    // String URL_APPOINTMENT_STATUS = "http://192.168.11.2/zenpets/public/checkAvailability";
                    HttpUrl.Builder builder = HttpUrl.parse(URL_APPOINTMENT_STATUS).newBuilder();
                    builder.addQueryParameter("doctorID", DOCTOR_ID);
                    builder.addQueryParameter("clinicID", CLINIC_ID);
                    builder.addQueryParameter("appointmentDate", CURRENT_DATE);
                    builder.addQueryParameter("appointmentTime", slotStartTime);
                    String FINAL_URL = builder.build().toString();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(FINAL_URL)
                            .build();
                    Call call = client.newCall(request);
                    Response response = call.execute();
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JAAppointments = JORoot.getJSONArray("appointments");
//                        Log.e("AVAILABILITY", String.valueOf(JAAppointments));
                        if (JAAppointments.length() > 0)    {
                            afternoonData.setAppointmentStatus("Unavailable");
//                            Log.e("STATUS", "Unavailable");
                        } else {
                            afternoonData.setAppointmentStatus("Available");
//                            Log.e("STATUS", "Available");
                        }
                    }

                    /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                    arrAfternoonSlots.add(afternoonData);
                }
            } catch (ParseException e) {
                Log.e("MORNING EXCEPTION", e.getMessage());
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* CONFIGURE THE AFTERNOON RECYCLER VIEW */
            configAfternoonRecycler();

            /* INSTANTIATE THE TIME SLOTS ADAPTER */
            afternoonTimeSlotsAdapter = new AfternoonTimeSlotsAdapter(AppointmentSlotCreator.this, arrAfternoonSlots);

            /* SET THE TIME SLOTS ADAPTER TO THE AFTERNOON RECYCLER VIEW */
            listAfternoonTimes.setAdapter(afternoonTimeSlotsAdapter);

            /* SET THE LIST VISIBILITY */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            txtAfternoonClosed.setVisibility(View.GONE);
        }
    }

    private void configMorningRecycler() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int intOrientation = getResources().getConfiguration().orientation;
                listMorningTimes.setHasFixedSize(true);
                GridLayoutManager glm = null;
                boolean isTablet = getResources().getBoolean(R.bool.isTablet);
                if (isTablet)   {
                    if (intOrientation == 1)	{
                        glm = new GridLayoutManager(AppointmentSlotCreator.this, 2);
                    } else if (intOrientation == 2) {
                        glm = new GridLayoutManager(AppointmentSlotCreator.this, 3);
                    }
                } else {
                    if (intOrientation == 1)    {
                        glm = new GridLayoutManager(AppointmentSlotCreator.this, 4);
                    } else if (intOrientation == 2) {
                        glm = new GridLayoutManager(AppointmentSlotCreator.this, 8);
                    }
                }
                listMorningTimes.setLayoutManager(glm);
            }
        });
    }

    /* CONFIGURE THE AFTERNOON RECYCLER VIEW */
    private void configAfternoonRecycler() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                int intOrientation = getResources().getConfiguration().orientation;
                listAfternoonTimes.setHasFixedSize(true);
                GridLayoutManager glm = null;
                boolean isTablet = getResources().getBoolean(R.bool.isTablet);
                if (isTablet)   {
                    if (intOrientation == 1)	{
                        glm = new GridLayoutManager(AppointmentSlotCreator.this, 2);
                    } else if (intOrientation == 2) {
                        glm = new GridLayoutManager(AppointmentSlotCreator.this, 3);
                    }
                } else {
                    if (intOrientation == 1)    {
                        glm = new GridLayoutManager(AppointmentSlotCreator.this, 4);
                    } else if (intOrientation == 2) {
                        glm = new GridLayoutManager(AppointmentSlotCreator.this, 8);
                    }
                }
                listAfternoonTimes.setLayoutManager(glm);
            }
        });
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Select a time slot";
//        String strTitle = getString(R.string.add_a_new_pet);
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
                finish();
                break;
            case R.id.menuCancel:
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

        if (resultCode == RESULT_OK && requestCode == 101)  {
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}