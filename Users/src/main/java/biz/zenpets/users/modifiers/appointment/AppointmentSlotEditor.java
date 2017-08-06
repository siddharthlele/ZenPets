package biz.zenpets.users.modifiers.appointment;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

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

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.appointment.modifier.AfternoonModifierAdapter;
import biz.zenpets.users.utils.adapters.appointment.modifier.MorningModifierAdapter;
import biz.zenpets.users.utils.models.appointment.slots.AfternoonTimeSlotsData;
import biz.zenpets.users.utils.models.appointment.slots.MorningTimeSlotsData;
import biz.zenpets.users.utils.models.calendar.ZenCalendarData;
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

public class AppointmentSlotEditor extends AppCompatActivity {

    /** THE INCOMING DATA **/
    private String APPOINTMENT_ID = null;
    private String CLINIC_ID = null;
    private String DOCTOR_ID = null;

    /** THE CURRENT DATE AND THE SELECTED DAY **/
    private String CURRENT_DATE = null;
    private String SELECTED_DAY = null;

    /** THE START TIME AND END TIME **/
    private String MORNING_START_TIME = null;
    private String MORNING_END_TIME = null;
    private String AFTERNOON_START_TIME = null;
    private String AFTERNOON_END_TIME = null;

    /** THE ZEN CALENDAR ADAPTER AND ARRAY LIST **/
    private ZenCalendarAdapter calendarAdapter;
    private final ArrayList<ZenCalendarData> arrDates = new ArrayList<>();

    /** THE MORNING TIME SLOTS ADAPTER AND ARRAY LIST **/
    private MorningModifierAdapter morningModifierAdapter;
    private final ArrayList<MorningTimeSlotsData> arrMorningSlots = new ArrayList<>();
    private MorningTimeSlotsData morningData;

    /** THE AFTERNOON TIME SLOTS ADAPTER AND ARRAY LIST **/
    private AfternoonModifierAdapter afternoonModifierAdapter;
    private final ArrayList<AfternoonTimeSlotsData> arrAfternoonSlots = new ArrayList<>();
    private AfternoonTimeSlotsData afternoonData;

    /** BOOLEAN TO CHECK IF VIEW IS COLLAPSED **/
    private boolean blnMorningCollapsed = false;
    private boolean blnAfternoonCollapsed = false;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.listDates) RecyclerView listDates;
    @BindView(R.id.imgvwDoctorProfile) CircleImageView imgvwDoctorProfile;
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.txtClinicDetails) AppCompatTextView txtClinicDetails;
    @BindView(R.id.linlaMorningProgress) LinearLayout linlaMorningProgress;
    @BindView(R.id.listMorningTimes) RecyclerView listMorningTimes;
    @BindView(R.id.txtMorningClosed) AppCompatTextView txtMorningClosed;
    @BindView(R.id.linlaAfternoonProgress) LinearLayout linlaAfternoonProgress;
    @BindView(R.id.listAfternoonTimes) RecyclerView listAfternoonTimes;
    @BindView(R.id.txtAfternoonClosed) AppCompatTextView txtAfternoonClosed;

    /** COLLAPSE THE MORNING TIMINGS **/
    @OnClick(R.id.linlaMorning) void collapseMorning()   {
        if (blnMorningCollapsed)    {
            expandMorningTimings(listMorningTimes);
        } else {
            collapseMorningTimings(listMorningTimes);
        }
    }

    /** COLLAPSE THE AFTERNOON TIMINGS **/
    @OnClick(R.id.linlaAfternoon) void collapseAfternoon()  {
        if (blnAfternoonCollapsed)  {
            expandAfternoonTimings(listAfternoonTimes);
        } else {
            collapseAfternoonTimings(listAfternoonTimes);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.appointment_slot_editor);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* INSTANTIATE THE ZEN CALENDAR ADAPTER */
        calendarAdapter = new ZenCalendarAdapter(AppointmentSlotEditor.this, arrDates);

        /* INSTANTIATE THE MORNING TIME SLOTS ADAPTER */
        morningModifierAdapter = new MorningModifierAdapter(AppointmentSlotEditor.this, arrMorningSlots);

        /* INSTANTIATE THE AFTERNOON TIME SLOTS ADAPTER */
        afternoonModifierAdapter = new AfternoonModifierAdapter(AppointmentSlotEditor.this, arrAfternoonSlots);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /** GET THE START DATE **/
        DateTime today = new DateTime().withTimeAtStartOfDay();
        Date dt = today.toDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        SimpleDateFormat currentDay = new SimpleDateFormat("EE", Locale.getDefault());
        SimpleDateFormat currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SELECTED_DAY = currentDay.format(cal.getTime());
        CURRENT_DATE = currentDate.format(cal.getTime());
//        Log.e("SELECTED DAY", SELECTED_DAY);
//        Log.e("CURRENT DATE", CURRENT_DATE);

        /* FETCH THE TIME SLOTS  */
        if (SELECTED_DAY.equalsIgnoreCase("sun"))   {
            new fetchSundayMorningSlots().execute();
            new fetchSundayAfternoonSlots().execute();
        } else if (SELECTED_DAY.equalsIgnoreCase("mon"))    {
            new fetchMondayMorningSlots().execute();
            new fetchMondayAfternoonSlots().execute();
        } else if (SELECTED_DAY.equalsIgnoreCase("tue"))    {
            new fetchTuesdayMorningSlots().execute();
            new fetchTuesdayAfternoonSlots().execute();
        } else if (SELECTED_DAY.equalsIgnoreCase("wed"))    {
            new fetchWednesdayMorningSlots().execute();
            new fetchWednesdayAfternoonSlots().execute();
        } else if (SELECTED_DAY.equalsIgnoreCase("thu"))    {
            new fetchThursdayMorningSlots().execute();
            new fetchThursdayAfternoonSlots().execute();
        } else if (SELECTED_DAY.equalsIgnoreCase("fri"))    {
            new fetchFridayMorningSlots().execute();
            new fetchFridayAfternoonSlots().execute();
        } else if (SELECTED_DAY.equalsIgnoreCase("sat"))    {
            new fetchSaturdayMorningSlots().execute();
            new fetchSaturdayAfternoonSlots().execute();
        }

        /** POPULATE THE ZEN CALENDAR DATES **/
        ZenCalendarData data;
        for (int i = 0; i < 14; i++) {
            data = new ZenCalendarData();
            Date date = today.plusDays(i).withTimeAtStartOfDay().toDate();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(date);

            /* SET THE LONG DATE */
            SimpleDateFormat longDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String LONG_DATE = longDate.format(calendar.getTime());
            data.setLongDate(LONG_DATE);

            /* SET THE SHORT DATE */
            SimpleDateFormat shortDate = new SimpleDateFormat("dd", Locale.getDefault());
            String SHORT_DATE = shortDate.format(calendar.getTime());
            data.setShortDate(SHORT_DATE);

            /* SET THE LONG DAY */
            SimpleDateFormat longDay = new SimpleDateFormat("EE", Locale.getDefault());
            String LONG_DAY = longDay.format(calendar.getTime());
            data.setLongDay(LONG_DAY);

            /* SET THE SHORT DATE (FOR DISPLAY PURPOSES ONLY!!) */
            String SHORT_DAY = LONG_DAY.substring(0, 1);
            data.setShortDay(SHORT_DAY);

            arrDates.add(data);

            /* CHECK WHEN THE FOR LOOP ENDS */
            if (i + 1 == 15) {
                /* INSTANTIATE THE ZEN CALENDAR ADAPTER */
                calendarAdapter = new ZenCalendarAdapter(AppointmentSlotEditor.this, arrDates);

                /* SET THE ZEN CALENDAR ADAPTER TO THE RECYCLER VIEW */
            }
        }
    }

    /***** CONFIGURE THE RECYCLER VIEW *****/
    private void configRecycler() {
        /* CONFIGURE THE TIME SLOTS RECYCLER VIEW */
        LinearLayoutManager dates = new LinearLayoutManager(this);
        dates.setOrientation(LinearLayoutManager.HORIZONTAL);
        dates.setAutoMeasureEnabled(true);
        listDates.setLayoutManager(dates);
        listDates.setHasFixedSize(true);
        listDates.setAdapter(calendarAdapter);

        /* CHECK IF DEVICE IS A TABLET */
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        /* GET THE DEVICE ORIENTATION */
        int intOrientation = getResources().getConfiguration().orientation;

        /* CONFIGURE THE MORNING TIME SLOTS RECYCLER VIEW */
        listMorningTimes.setHasFixedSize(true);
        GridLayoutManager managerMorning = null;
        if (isTablet)   {
            if (intOrientation == 1)	{
                managerMorning = new GridLayoutManager(AppointmentSlotEditor.this, 2);
            } else if (intOrientation == 2) {
                managerMorning = new GridLayoutManager(AppointmentSlotEditor.this, 3);
            }
        } else {
            if (intOrientation == 1)    {
                managerMorning = new GridLayoutManager(AppointmentSlotEditor.this, 4);
            } else if (intOrientation == 2) {
                managerMorning = new GridLayoutManager(AppointmentSlotEditor.this, 8);
            }
        }
        listMorningTimes.setLayoutManager(managerMorning);
        listMorningTimes.setNestedScrollingEnabled(false);
        listMorningTimes.setAdapter(morningModifierAdapter);

        /* CONFIGURE THE AFTERNOON TIME SLOTS RECYCLER VIEW */
        listAfternoonTimes.setHasFixedSize(true);
        GridLayoutManager managerAfternoon = null;
        if (isTablet)   {
            if (intOrientation == 1)	{
                managerAfternoon = new GridLayoutManager(AppointmentSlotEditor.this, 2);
            } else if (intOrientation == 2) {
                managerAfternoon = new GridLayoutManager(AppointmentSlotEditor.this, 3);
            }
        } else {
            if (intOrientation == 1)    {
                managerAfternoon = new GridLayoutManager(AppointmentSlotEditor.this, 4);
            } else if (intOrientation == 2) {
                managerAfternoon = new GridLayoutManager(AppointmentSlotEditor.this, 8);
            }
        }
        listAfternoonTimes.setLayoutManager(managerAfternoon);
        listAfternoonTimes.setNestedScrollingEnabled(false);
        listAfternoonTimes.setAdapter(afternoonModifierAdapter);
    }

    /***** FETCH THE CLINIC DETAILS *****/
    private class fetchClinicDetails extends AsyncTask<Void, Void, Void>    {

        /** THE STRINGS TO HOLD THE CLINIC DETAILS **/
        String CLINIC_NAME = null;
        String CLINIC_ADDRESS = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_CLINICS = "http://leodyssey.com/ZenPets/public/clinicDetails";
            String URL_USER_CLINICS = "http://192.168.11.2/zenpets/public/clinicDetails";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_CLINICS).newBuilder();
            builder.addQueryParameter("clinicID", CLINIC_ID);
            String FINAL_URL = builder.build().toString();
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(FINAL_URL)
                    .build();
            Call call = client.newCall(request);
            Response response;
            try {
                response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                    /* SET THE CLINIC DETAILS */
                    if (JORoot.has("clinicName") && JORoot.has("cityName") && JORoot.has("localityName")) {
                        CLINIC_NAME = JORoot.getString("clinicName");
                        String cityName = JORoot.getString("cityName");
                        String localityName = JORoot.getString("localityName");
                        CLINIC_ADDRESS = localityName + ", " + cityName;
                    }

//                    final JSONArray JAClinics = JORoot.getJSONArray("clinics");
//                    if (JAClinics.length() > 0) {
//                        for (int i = 0; i < JAClinics.length(); i++) {
//                            JSONObject JOClinics = JAClinics.getJSONObject(i);
//
//                            /* SET THE CLINIC DETAILS */
//                            if (JOClinics.has("clinicName") && JOClinics.has("cityName") && JOClinics.has("localityName")) {
//                                CLINIC_NAME = JOClinics.getString("clinicName");
//                                String cityName = JOClinics.getString("cityName");
//                                String localityName = JOClinics.getString("localityName");
//                                CLINIC_ADDRESS = localityName + ", " + cityName;
//                            }
//                        }
//                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* SET THE CLINIC DETAILS */
            txtClinicDetails.setText(CLINIC_NAME + ", " + CLINIC_ADDRESS);

            /* FETCH THE DOCTOR DETAILS */
            new fetchDoctorDetails().execute();
        }
    }

    /***** FETCH THE DOCTOR DETAILS *****/
    private class fetchDoctorDetails extends AsyncTask<Void, Void, Void>    {

        /** STRINGS TO HOLD THE DOCTOR DETAILS **/
        String DOCTOR_PREFIX = null;
        String DOCTOR_NAME = null;
        String DOCTOR_DISPLAY_PROFILE = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_DETAILS = "http://leodyssey.com/ZenPets/public/fetchDoctorProfile";
            String URL_USER_DETAILS = "http://192.168.11.2/zenpets/public/fetchDoctorProfile";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DETAILS).newBuilder();
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
                    JSONArray JADoctors = JORoot.getJSONArray("doctors");
                    for (int i = 0; i < JADoctors.length(); i++) {
                        JSONObject JODoctors = JADoctors.getJSONObject(i);

                        /* GET THE DOCTOR'S PREFIX */
                        if (JODoctors.has("doctorPrefix") && JODoctors.has("doctorName")) {
                            DOCTOR_PREFIX = JODoctors.getString("doctorPrefix");
                            DOCTOR_NAME = JODoctors.getString("doctorName");
                        } else {
                            DOCTOR_PREFIX = null;
                            DOCTOR_NAME = null;
                        }

                        /* GET THE USER DISPLAY PROFILE */
                        if (JODoctors.has("doctorDisplayProfile")) {
                            DOCTOR_DISPLAY_PROFILE = JODoctors.getString("doctorDisplayProfile");
                        } else {
                            DOCTOR_DISPLAY_PROFILE = null;
                        }
                    }
                }
            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* SET THE DOCTOR'S NAME */
            if (DOCTOR_NAME != null && DOCTOR_PREFIX != null)   {
                txtDoctorName.setText(DOCTOR_PREFIX + " " + DOCTOR_NAME);
            }

            /* SET THE DOCTOR'S DISPLAY PROFILE */
            if (DOCTOR_DISPLAY_PROFILE != null) {
                Picasso.with(AppointmentSlotEditor.this)
                        .load(DOCTOR_DISPLAY_PROFILE)
                        .centerCrop()
                        .fit()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imgvwDoctorProfile, new Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(AppointmentSlotEditor.this)
                                        .load(DOCTOR_DISPLAY_PROFILE)
                                        .centerCrop()
                                        .fit()
                                        .into(imgvwDoctorProfile);
                            }
                        });
            }
        }
    }

    /** FETCH THE SUNDAY MORNING SLOTS **/
    private class fetchSundayMorningSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listMorningTimes.setVisibility(View.GONE);
            linlaMorningProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchSundayMorningTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchSundayMorningTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listMorningTimes.setVisibility(View.VISIBLE);
            linlaMorningProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayMorningSlots().execute();
            } else {
                /* HIDE THE LIST */
                listMorningTimes.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE SUNDAY AFTERNOON SLOTS **/
    private class fetchSundayAfternoonSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.GONE);
            linlaAfternoonProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchSundayAfternoonTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchSundayAfternoonTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            linlaAfternoonProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayAfternoonSlots().execute();
            } else {
                /* HIDE THE LIST */
                listAfternoonTimes.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE MONDAY MORNING SLOTS **/
    private class fetchMondayMorningSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listMorningTimes.setVisibility(View.GONE);
            linlaMorningProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchMondayMorningTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchMondayMorningTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listMorningTimes.setVisibility(View.VISIBLE);
            linlaMorningProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayMorningSlots().execute();
            } else {
                /* HIDE THE LIST */
                listMorningTimes.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE MONDAY AFTERNOON SLOTS **/
    private class fetchMondayAfternoonSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.GONE);
            linlaAfternoonProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchMondayAfternoonTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchMondayAfternoonTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            linlaAfternoonProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayAfternoonSlots().execute();
            } else {
                /* HIDE THE LIST */
                listAfternoonTimes.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE TUESDAY MORNING SLOTS **/
    private class fetchTuesdayMorningSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listMorningTimes.setVisibility(View.GONE);
            linlaMorningProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchTuesdayMorningTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchTuesdayMorningTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listMorningTimes.setVisibility(View.VISIBLE);
            linlaMorningProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayMorningSlots().execute();
            } else {
                /* HIDE THE LIST */
                listMorningTimes.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE TUESDAY AFTERNOON SLOTS **/
    private class fetchTuesdayAfternoonSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.GONE);
            linlaAfternoonProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchTuesdayAfternoonTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchTuesdayAfternoonTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            linlaAfternoonProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayAfternoonSlots().execute();
            } else {
                /* HIDE THE LIST */
                listAfternoonTimes.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE WEDNESDAY MORNING SLOTS **/
    private class fetchWednesdayMorningSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listMorningTimes.setVisibility(View.GONE);
            linlaMorningProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchWednesdayMorningTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchWednesdayMorningTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listMorningTimes.setVisibility(View.VISIBLE);
            linlaMorningProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayMorningSlots().execute();
            } else {
                /* HIDE THE LIST */
                listMorningTimes.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE TUESDAY AFTERNOON SLOTS **/
    private class fetchWednesdayAfternoonSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.GONE);
            linlaAfternoonProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchWednesdayAfternoonTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchWednesdayAfternoonTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            linlaAfternoonProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayAfternoonSlots().execute();
            } else {
                /* HIDE THE LIST */
                listAfternoonTimes.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE THURSDAY MORNING SLOTS **/
    private class fetchThursdayMorningSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listMorningTimes.setVisibility(View.GONE);
            linlaMorningProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchThursdayMorningTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchThursdayMorningTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listMorningTimes.setVisibility(View.VISIBLE);
            linlaMorningProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayMorningSlots().execute();
            } else {
                /* HIDE THE LIST */
                listMorningTimes.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE THURSDAY AFTERNOON SLOTS **/
    private class fetchThursdayAfternoonSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.GONE);
            linlaAfternoonProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchThursdayAfternoonTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchThursdayAfternoonTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            linlaAfternoonProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayAfternoonSlots().execute();
            } else {
                /* HIDE THE LIST */
                listAfternoonTimes.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE FRIDAY MORNING SLOTS **/
    private class fetchFridayMorningSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listMorningTimes.setVisibility(View.GONE);
            linlaMorningProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchFridayMorningTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchFridayMorningTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listMorningTimes.setVisibility(View.VISIBLE);
            linlaMorningProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayMorningSlots().execute();
            } else {
                /* HIDE THE LIST */
                listMorningTimes.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE FRIDAY AFTERNOON SLOTS **/
    private class fetchFridayAfternoonSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.GONE);
            linlaAfternoonProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchFridayAfternoonTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchFridayAfternoonTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            linlaAfternoonProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayAfternoonSlots().execute();
            } else {
                /* HIDE THE LIST */
                listAfternoonTimes.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE SATURDAY MORNING SLOTS **/
    private class fetchSaturdayMorningSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listMorningTimes.setVisibility(View.GONE);
            linlaMorningProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchSaturdayMorningTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchSaturdayMorningTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listMorningTimes.setVisibility(View.VISIBLE);
            linlaMorningProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (MORNING_START_TIME != null && !MORNING_START_TIME.equalsIgnoreCase("null") && MORNING_END_TIME != null && !MORNING_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayMorningSlots().execute();
            } else {
                /* HIDE THE LIST */
                listMorningTimes.setVisibility(View.GONE);
                txtMorningClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** FETCH THE SATURDAY AFTERNOON SLOTS **/
    private class fetchSaturdayAfternoonSlots extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.GONE);
            linlaAfternoonProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchSaturdayAfternoonTimings";
            String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchSaturdayAfternoonTimings";
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            linlaAfternoonProgress.setVisibility(View.GONE);

            /* CALCULATE THE TIME SLOTS */
            if (AFTERNOON_START_TIME != null && !AFTERNOON_START_TIME.equalsIgnoreCase("null") && AFTERNOON_END_TIME != null && !AFTERNOON_END_TIME.equalsIgnoreCase("null")) {
                /* DISPLAY THE MORNING SLOTS */
                new displayAfternoonSlots().execute();
            } else {
                /* HIDE THE LIST */
                listAfternoonTimes.setVisibility(View.GONE);
                txtAfternoonClosed.setVisibility(View.VISIBLE);
            }
        }
    }

    /** DISPLAY THE MORNING SLOTS  **/
    private class displayMorningSlots extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listMorningTimes.setVisibility(View.GONE);
            linlaMorningProgress.setVisibility(View.VISIBLE);

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

                    // String URL_APPOINTMENT_STATUS = "http://leodyssey.com/ZenPets/public/checkAvailability";
                    String URL_APPOINTMENT_STATUS = "http://192.168.11.2/zenpets/public/checkAvailability";
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
            } catch (ParseException | IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* INSTANTIATE THE TIME SLOTS ADAPTER */
            morningModifierAdapter = new MorningModifierAdapter(AppointmentSlotEditor.this, arrMorningSlots);

            /* SET THE TIME SLOTS ADAPTER TO THE AFTERNOON RECYCLER VIEW */
            listMorningTimes.setAdapter(morningModifierAdapter);

            /* SET THE LIST VISIBILITY */
            listMorningTimes.setVisibility(View.VISIBLE);
            txtMorningClosed.setVisibility(View.GONE);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listMorningTimes.setVisibility(View.VISIBLE);
            linlaMorningProgress.setVisibility(View.GONE);
        }
    }

    /** DISPLAY THE AFTERNOON SLOTS **/
    private class displayAfternoonSlots extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.GONE);
            linlaAfternoonProgress.setVisibility(View.VISIBLE);

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

                    // String URL_APPOINTMENT_STATUS = "http://leodyssey.com/ZenPets/public/checkAvailability";
                    String URL_APPOINTMENT_STATUS = "http://192.168.11.2/zenpets/public/checkAvailability";
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
            } catch (ParseException | IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* INSTANTIATE THE AFTERNOON TIME SLOTS ADAPTER */
            afternoonModifierAdapter = new AfternoonModifierAdapter(AppointmentSlotEditor.this, arrAfternoonSlots);

            /* SET THE TIME SLOTS ADAPTER TO THE AFTERNOON RECYCLER VIEW */
            listAfternoonTimes.setAdapter(afternoonModifierAdapter);

            /* SET THE LIST VISIBILITY */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            txtAfternoonClosed.setVisibility(View.GONE);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            listAfternoonTimes.setVisibility(View.VISIBLE);
            linlaAfternoonProgress.setVisibility(View.GONE);
        }
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("APPOINTMENT_ID") && bundle.containsKey("CLINIC_ID") && bundle.containsKey("DOCTOR_ID")) {
            APPOINTMENT_ID = bundle.getString("APPOINTMENT_ID");
            CLINIC_ID = bundle.getString("CLINIC_ID");
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            if (APPOINTMENT_ID != null && CLINIC_ID != null && DOCTOR_ID != null)   {
                /* FETCH THE CLINIC DETAILS */
                new fetchClinicDetails().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Edit the time slot";
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

    /** EXPAND THE MORNING TIMINGS **/
    private void expandMorningTimings(final ViewGroup v) {
        blnMorningCollapsed = false;
        v.measure(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RecyclerView.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    /** COLLAPSE THE MORNING TIMINGS **/
    private void collapseMorningTimings(final View v) {
        blnMorningCollapsed = true;
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    /** EXPAND THE AFTERNOON TIMINGS **/
    private void expandAfternoonTimings(final View v) {
        blnAfternoonCollapsed = false;
        v.measure(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? RecyclerView.LayoutParams.WRAP_CONTENT
                        : (int)(targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    /** COLLAPSE THE AFTERNOON TIMINGS **/
    private void collapseAfternoonTimings(final View v) {
        blnAfternoonCollapsed = true;
        final int initialHeight = v.getMeasuredHeight();

        Animation a = new Animation()
        {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if(interpolatedTime == 1){
                    v.setVisibility(View.GONE);
                }else{
                    v.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    v.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int)(initialHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private class ZenCalendarAdapter extends RecyclerView.Adapter<ZenCalendarAdapter.CalendarVH> {

        /** AN ACTIVITY INSTANCE **/
        private final Activity activity;

        /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
        private final ArrayList<ZenCalendarData> arrDates;

        /** SELECTED ITEM **/
        private int selectedPosition = 0;

        ZenCalendarAdapter(Activity activity, ArrayList<ZenCalendarData> arrDates) {
        
            /* CAST THE ACTIVITY IN THE GLOBAL INSTANCE */
            this.activity = activity;

            /* CAST THE CONTENTS OF THE LOCAL ARRAY LIST IN THE METHOD TO THE GLOBAL INSTANCE */
            this.arrDates = arrDates;
        }

        @Override
        public int getItemCount() {
            return arrDates.size();
        }

        @Override
        public void onBindViewHolder(final ZenCalendarAdapter.CalendarVH holder, int position) {
            final ZenCalendarData data = arrDates.get(position);

            /* SET THE SELECTION AND MARK WITH DRAWABLE */
            if (selectedPosition == position)   {
                holder.txtShortDate.setBackgroundDrawable(ContextCompat.getDrawable(activity, R.drawable.circular_btn_bg));
            } else {
                holder.txtShortDate.setBackgroundDrawable(null);
            }

            /* SET THE SHORT DAY */
            if (data.getShortDay() != null) {
                holder.txtShortDay.setText(data.getShortDay());
            }

            /* SET THE SHORT DATE */
            if (data.getShortDate() != null)    {
                holder.txtShortDate.setText(data.getShortDate());
            }

            /** SHOW THE AVAILABILITY ON THE SELECTED DATE **/
            holder.linlaDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedPosition = holder.getAdapterPosition();
                    notifyDataSetChanged();

                    String strLongDay = data.getLongDay();
//                    Log.e("LONG DAY", strLongDay);

                    /* FETCH THE TIME SLOTS  */
                    if (strLongDay.equalsIgnoreCase("sun"))   {
                        new fetchSundayMorningSlots().execute();
                        new fetchSundayAfternoonSlots().execute();
                    } else if (strLongDay.equalsIgnoreCase("mon"))    {
                        new fetchMondayMorningSlots().execute();
                        new fetchMondayAfternoonSlots().execute();
                    } else if (strLongDay.equalsIgnoreCase("tue"))    {
                        new fetchTuesdayMorningSlots().execute();
                        new fetchTuesdayAfternoonSlots().execute();
                    } else if (strLongDay.equalsIgnoreCase("wed"))    {
                        new fetchWednesdayMorningSlots().execute();
                        new fetchWednesdayAfternoonSlots().execute();
                    } else if (strLongDay.equalsIgnoreCase("thu"))    {
                        new fetchThursdayMorningSlots().execute();
                        new fetchThursdayAfternoonSlots().execute();
                    } else if (strLongDay.equalsIgnoreCase("fri"))    {
                        new fetchFridayMorningSlots().execute();
                        new fetchFridayAfternoonSlots().execute();
                    } else if (strLongDay.equalsIgnoreCase("sat"))    {
                        new fetchSaturdayMorningSlots().execute();
                        new fetchSaturdayAfternoonSlots().execute();
                    }
                }
            });
        }

        @Override
        public ZenCalendarAdapter.CalendarVH onCreateViewHolder(ViewGroup parent, int i) {

            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.zen_calendar_item, parent, false);

            return new ZenCalendarAdapter.CalendarVH(itemView);
        }

        class CalendarVH extends RecyclerView.ViewHolder	{

            final LinearLayout linlaDate;
            final AppCompatTextView txtShortDay;
            final AppCompatTextView txtShortDate;

            CalendarVH(View v) {
                super(v);
                linlaDate = (LinearLayout) v.findViewById(R.id.linlaDate);
                txtShortDay = (AppCompatTextView) v.findViewById(R.id.txtShortDay);
                txtShortDate = (AppCompatTextView) v.findViewById(R.id.txtShortDate);

                /* GET THE DISPLAY SIZE */
                DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
                int width = (int) ((float) metrics.widthPixels);
                linlaDate.getLayoutParams().width = width / 7;
            }
        }
    }
}