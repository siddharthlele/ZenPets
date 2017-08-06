package com.zenpets.doctors.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.zenpets.doctors.creators.appointment.AppointmentSlotCreator;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.clinics.ClinicSelectorAdapter;
import com.zenpets.doctors.utils.adapters.doctors.DoctorAppointmentsAdapter;
import com.zenpets.doctors.utils.adapters.doctors.DoctorSelectorAdapter;
import com.zenpets.doctors.utils.models.clinics.ClinicsData;
import com.zenpets.doctors.utils.models.doctors.AppointmentsData;
import com.zenpets.doctors.utils.models.doctors.DoctorsData;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import noman.weekcalendar.WeekCalendar;
import noman.weekcalendar.listener.OnDateClickListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DoctorsCalendar extends AppCompatActivity {

    /** THE LOGGED IN DOCTOR'S EMAIL ADDRESS AND DOCTOR'S ID **/
    String DOCTOR_EMAIL = null;
    String DOCTOR_ID = null;

    /** THE SELECTED CLINIC ID **/
    String CLINIC_ID = null;

    /** THE SELECTED DATE **/
    String SELECTED_DATE = null;

    /** THE DOCTORS ADAPTER AND ARRAY LIST **/
    DoctorSelectorAdapter doctorSelectorAdapter;
    ArrayList<DoctorsData> arrDoctors = new ArrayList<>();

    /** THE CLINIC SELECTOR ADAPTER AND ARRAY LIST **/
    ClinicSelectorAdapter clinicSelectorAdapter;
    ArrayList<ClinicsData> arrClinics = new ArrayList<>();

    /** THE APPOINTMENTS ADAPTER AND ARRAY LIST **/
    DoctorAppointmentsAdapter doctorAppointmentsAdapter;
    ArrayList<AppointmentsData> arrAppointments = new ArrayList<>();

    /** THE REQUEST CODES **/
    private int REQUEST_NEW_APPOINTMENT = 101;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(com.zenpets.doctors.R.id.spnDoctors) AppCompatSpinner spnDoctors;
    @BindView(com.zenpets.doctors.R.id.spnClinics) AppCompatSpinner spnClinics;
    @BindView(com.zenpets.doctors.R.id.weekCalendar) WeekCalendar weekCalendar;
    @BindView(com.zenpets.doctors.R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(com.zenpets.doctors.R.id.listAppointments) RecyclerView listAppointments;
    @BindView(com.zenpets.doctors.R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zenpets.doctors.R.layout.doctor_calendar_list);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /* GET THE CURRENT DATE */
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SELECTED_DATE = sdf.format(cal.getTime());
//        Log.e("SELECTED DATE", SELECTED_DATE);

        /* GET THE USER DETAILS */
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            /* GET THE DOCTOR'S EMAIL */
            DOCTOR_EMAIL = user.getEmail();

            /* FETCH THE LIST OF DOCTORS ON ACCOUNT */
            fetchDoctors();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required data....", Toast.LENGTH_SHORT).show();
            finish();
        }

        /* SELECT A DOCTOR TO SHOW THEIR PROFILE */
        spnDoctors.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DOCTOR_ID = arrDoctors.get(position).getDoctorID();
                if (DOCTOR_ID != null)  {
                    /* FETCH THE CLINICS THE DOCTOR PRACTICES AT */
                    fetchClinics();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* SELECT A CLINIC */
        spnClinics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CLINIC_ID = arrClinics.get(position).getClinicID();

                /* FETCH THE LIST OF TODAY'S APPOINTMENTS */
                fetchAppointments();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* GET THE SELECTED DATE */
        weekCalendar.setOnDateClickListener(new OnDateClickListener() {
            @Override
            public void onDateClick(DateTime dateTime) {
                Date date = dateTime.toDate();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                SELECTED_DATE = sdf.format(date.getTime());
//                Log.e("SELECTED DATE", SELECTED_DATE);

                /* FETCH THE LIST OF TODAY'S APPOINTMENTS */
                fetchAppointments();
            }
        });
    }

    /***** FETCH THE LIST OF TODAY'S APPOINTMENTS *****/
    private void fetchAppointments() {
        /* CLEAR THE ARRAY LIST */
        arrAppointments.clear();

        /* SHOW THE PROGRESS WHILE LOADING THE DATA */
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        String URL_APPOINTMENTS = "http://leodyssey.com/ZenPets/public/fetchDoctorAppointments";
        // String URL_APPOINTMENTS = "http://192.168.11.2/zenpets/public/fetchDoctorAppointments";
        HttpUrl.Builder builder = HttpUrl.parse(URL_APPOINTMENTS).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        builder.addQueryParameter("appointmentDate", SELECTED_DATE);
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
                        JSONArray JAAppointments = JORoot.getJSONArray("appointments");
                        if (JAAppointments.length() > 0) {
                            /* A APPOINTMENTS DATA INSTANCE */
                            AppointmentsData data;

                            for (int i = 0; i < JAAppointments.length(); i++) {
                                JSONObject JOAppointments = JAAppointments.getJSONObject(i);

                                /* INSTANTIATE THE APPOINTMENTS DATA INSTANCE */
                                data = new AppointmentsData();

                                /* GET THE APPOINTMENT ID */
                                if (JOAppointments.has("appointmentID"))    {
                                    data.setAppointmentID(JOAppointments.getString("appointmentID"));
                                } else {
                                    data.setAppointmentID(null);
                                }

                                /* GET THE DOCTOR ID */
                                if (JOAppointments.has("doctorID")) {
                                    data.setDoctorID(JOAppointments.getString("doctorID"));
                                } else {
                                    data.setDoctorID(null);
                                }

                                /* GET THE CLINIC ID */
                                if (JOAppointments.has("clinicID")) {
                                    data.setClinicID(JOAppointments.getString("clinicID"));
                                } else {
                                    data.setClinicID(null);
                                }

                                /* GET THE USER ID */
                                if (JOAppointments.has("userID"))   {
                                    data.setUserID(JOAppointments.getString("userID"));
                                } else {
                                    data.setUserID(null);
                                }

                                /* GET THE USER NAME */
                                if (JOAppointments.has("userName")) {
                                    data.setUserName(JOAppointments.getString("userName"));
                                } else {
                                    data.setUserName(null);
                                }

                                /* GET THE PET ID */
                                if (JOAppointments.has("petID"))    {
                                    data.setPetID(JOAppointments.getString("petID"));
                                } else {
                                    data.setPetID(null);
                                }

                                /* GET THE PET NAME */
                                if (JOAppointments.has("petName"))  {
                                    data.setPetName(JOAppointments.getString("petName"));
                                } else {
                                    data.setPetName(null);
                                }

                                /* GET THE VISIT REASON ID */
                                if (JOAppointments.has("visitReasonID"))    {
                                    data.setVisitReasonID(JOAppointments.getString("visitReasonID"));
                                } else {
                                    data.setVisitReasonID(null);
                                }

                                /* GET THE VISIT REASON */
                                if (JOAppointments.has("visitReason"))  {
                                    data.setVisitReason(JOAppointments.getString("visitReason"));
                                } else {
                                    data.setVisitReason(null);
                                }

                                /* GET THE APPOINTMENT DATE */
                                if (JOAppointments.has("appointmentDate"))  {
                                    data.setAppointmentDate(JOAppointments.getString("appointmentDate"));
                                } else {
                                    data.setAppointmentDate(null);
                                }

                                /* GET THE APPOINTMENT TIME */
                                if (JOAppointments.has("appointmentTime"))  {
                                    data.setAppointmentTime(JOAppointments.getString("appointmentTime"));
                                } else {
                                    data.setAppointmentTime(null);
                                }

                                /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                                arrAppointments.add(data);
                            }

                            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY VIEW */
                                    listAppointments.setVisibility(View.VISIBLE);
                                    linlaEmpty.setVisibility(View.GONE);

                                    /* CONFIGURE THE RECYCLER VIEW **/
                                    configRecycler();

                                    /* INSTANTIATE THE SPECIALIZATION ADAPTER */
                                    doctorAppointmentsAdapter = new DoctorAppointmentsAdapter(DoctorsCalendar.this, arrAppointments);

                                    /* SET THE CLINICS RECYCLER VIEW */
                                    listAppointments.setAdapter(doctorAppointmentsAdapter);

                                    /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                    linlaHeaderProgress.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                    listAppointments.setVisibility(View.GONE);
                                    linlaEmpty.setVisibility(View.VISIBLE);

                                    /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                    linlaHeaderProgress.setVisibility(View.GONE);
                                }
                            });
                        }
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                    /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                listAppointments.setVisibility(View.GONE);
                                    linlaEmpty.setVisibility(View.VISIBLE);

                                    /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                    linlaHeaderProgress.setVisibility(View.GONE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* FETCH THE LIST OF DOCTORS ON ACCOUNT */
    private void fetchDoctors() {
        String URL_USER_DOCTORS = "http://leodyssey.com/ZenPets/public/fetchUserDoctors";
        // String URL_USER_DOCTORS = "http://192.168.11.2/zenpets/public/fetchUserDoctors";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DOCTORS).newBuilder();
        builder.addQueryParameter("userEmail", DOCTOR_EMAIL);
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
                        JSONArray JADoctors = JORoot.getJSONArray("doctors");

                        /* AN INSTANCE OF THE DOCTORS DATA MODEL */
                        DoctorsData data;

                        for (int i = 0; i < JADoctors.length(); i++) {
                            JSONObject JODoctors = JADoctors.getJSONObject(i);
//                            Log.e("DOCTORS", String.valueOf(JODoctors));

                            /* INSTANTIATE THE INSTANCE OF THE DOCTORS DATA MODEL */
                            data = new DoctorsData();

                            /* GET THE DOCTOR'S ID */
                            if (JODoctors.has("doctorID"))  {
                                data.setDoctorID(JODoctors.getString("doctorID"));
                            } else {
                                data.setDoctorID(null);
                            }

                            /* GET THE DOCTOR'S NAME */
                            if (JODoctors.has("doctorPrefix") && JODoctors.has("doctorName"))  {
                                data.setDoctorName(JODoctors.getString("doctorPrefix") + " " + JODoctors.getString("doctorName"));
                            } else {
                                data.setDoctorName(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrDoctors.add(data);
                        }

                        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE DOCTORS ADAPTER */
                                doctorSelectorAdapter = new DoctorSelectorAdapter(DoctorsCalendar.this, com.zenpets.doctors.R.layout.doctor_selector_row, arrDoctors);

                                /* SET THE ADAPTER TO THE DOCTORS SPINNER */
                                spnDoctors.setAdapter(doctorSelectorAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* FETCH THE LIST OF CLINICS ON ACCOUNT */
    private void fetchClinics() {
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

                            /* A CLINICS DATA INSTANCE */
                            ClinicsData data;
                            for (int i = 0; i < JAClinics.length(); i++) {
                                JSONObject JOClinics = JAClinics.getJSONObject(i);

                                /* INSTANTIATE THE CLINICS DATA INSTANCE */
                                data = new ClinicsData();

                                /* GET THE CLINIC ID */
                                if (JOClinics.has("clinicID"))  {
                                    String clinicID = JOClinics.getString("clinicID");
                                    data.setClinicID(clinicID);
                                } else {
                                    data.setClinicID(null);
                                }

                                /* GET THE CLINIC NAME */
                                if (JOClinics.has("clinicName"))    {
                                    String clinicName = JOClinics.getString("clinicName");
                                    data.setClinicName(clinicName);
                                } else {
                                    data.setClinicName(null);
                                }

                                /* GET THE CLINIC ADDRESS */
                                if (JOClinics.has("cityName") && JOClinics.has("localityName")) {
                                    String cityName = JOClinics.getString("cityName");
                                    String localityName = JOClinics.getString("localityName");
                                    String finalAddress = localityName + ", " + cityName;
                                    data.setClinicAddress(finalAddress);
                                } else {
                                    data.setClinicAddress(null);
                                }

                                /* GET THE CLINIC LOGO */
                                if (JOClinics.has("clinicLogo"))    {
                                    String clinicLogo = JOClinics.getString("clinicLogo");
                                    if (clinicLogo.equals("") || clinicLogo.equalsIgnoreCase("null")) {
                                        data.setClinicLogo(null);
                                    } else {
                                        data.setClinicLogo(clinicLogo);
                                    }
                                } else {
                                    data.setClinicLogo(null);
                                }

                                /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                                arrClinics.add(data);
                            }

                            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* INSTANTIATE THE CLINICS SELECTOR ADAPTER */
                                    clinicSelectorAdapter = new ClinicSelectorAdapter(
                                            DoctorsCalendar.this,
                                            com.zenpets.doctors.R.layout.clinic_selector_row,
                                            arrClinics);

                                    /* SET THE CLINICS SPINNER */
                                    spnClinics.setAdapter(clinicSelectorAdapter);
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getApplicationContext(), "You haven't added any clinics to your account yet", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(com.zenpets.doctors.R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "CALENDAR";
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(DoctorsCalendar.this);
        inflater.inflate(com.zenpets.doctors.R.menu.activity_creator, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case com.zenpets.doctors.R.id.menuNew:
                Intent intent = new Intent(getApplicationContext(), AppointmentSlotCreator.class);
                intent.putExtra("DOCTOR_ID", DOCTOR_ID);
                intent.putExtra("CLINIC_ID", CLINIC_ID);
                startActivityForResult(intent, REQUEST_NEW_APPOINTMENT);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == REQUEST_NEW_APPOINTMENT)  {

            /* UPDATE THE LIST OF APPOINTMENTS */
            fetchAppointments();
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listAppointments.setLayoutManager(manager);
        listAppointments.setHasFixedSize(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}