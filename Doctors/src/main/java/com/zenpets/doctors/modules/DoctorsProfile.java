package com.zenpets.doctors.modules;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zenpets.doctors.R;
import com.zenpets.doctors.creators.clinic.ClinicCreator;
import com.zenpets.doctors.details.doctor.DoctorDetails;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.clinics.DoctorClinicsAdapter;
import com.zenpets.doctors.utils.adapters.doctors.DoctorSelectorAdapter;
import com.zenpets.doctors.utils.models.clinics.ClinicsData;
import com.zenpets.doctors.utils.models.doctors.DoctorsData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DoctorsProfile extends AppCompatActivity {

    /** THE FIREBASE USER INSTANCE **/
    FirebaseUser user;

    /** THE LOGGED IN DOCTOR'S EMAIL ADDRESS AND DOCTOR ID **/
    String DOCTOR_EMAIL = null;
    String DOCTOR_ID = null;

    /** THE REQUEST CODES **/
    private int REQUEST_NEW_CLINIC = 101;
    private int REQUEST_EDIT_DOCTOR = 102;

    /** THE DOCTORS ADAPTER AND ARRAY LIST **/
    DoctorSelectorAdapter doctorSelectorAdapter;
    ArrayList<DoctorsData> arrDoctors = new ArrayList<>();

    /** THE CLINICS ADAPTER AND ARRAY LIST **/
    DoctorClinicsAdapter adapter;
    ArrayList<ClinicsData> arrClinics = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.cardDoctor) CardView cardDoctor;
    @BindView(R.id.spnDoctors) AppCompatSpinner spnDoctors;
    @BindView(R.id.imgvwProfile) CircleImageView imgvwProfile;
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.txtDoctorEducation) AppCompatTextView txtDoctorEducation;
    @BindView(R.id.txtDoctorExperience) AppCompatTextView txtDoctorExperience;
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listClinics) RecyclerView listClinics;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** SHOW THE DOCTOR'S DETAILS **/
    @OnClick(R.id.cardDoctor) void doctorDetails()  {
        if (DOCTOR_ID != null)  {
            Intent intent = new Intent(DoctorsProfile.this, DoctorDetails.class);
            intent.putExtra("DOCTOR_ID", DOCTOR_ID);
            startActivityForResult(intent, REQUEST_EDIT_DOCTOR);
        } else {
            Toast.makeText(getApplicationContext(), "No Doctors were found on this account", Toast.LENGTH_SHORT).show();
        }
    }

    /** ADD A NEW CLINIC **/
    @OnClick(R.id.txtAddClinic) void newClinic()    {
        Intent intent = new Intent(getApplicationContext(), ClinicCreator.class);
        intent.putExtra("DOCTOR_ID", DOCTOR_ID);
        startActivityForResult(intent, REQUEST_NEW_CLINIC);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctors_profile);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* GET THE USER DETAILS */
        user = FirebaseAuth.getInstance().getCurrentUser();
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
        spnDoctors.setOnItemSelectedListener(selectDoctor);
    }

    private AdapterView.OnItemSelectedListener selectDoctor = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            /* CLEAR THE CLINIC ARRAY LIST */
            arrClinics.clear();

            DOCTOR_ID = arrDoctors.get(position).getDoctorID();
//            Log.e("SELECTED DOCTOR", DOCTOR_ID);
            if (DOCTOR_ID != null)  {
                /* FETCH THE DOCTOR'S PROFILE */
                fetchDoctorProfile();

                /* FETCH THE CLINICS THE DOCTOR PRACTICES AT */
                fetchClinics();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

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
                                doctorSelectorAdapter = new DoctorSelectorAdapter(DoctorsProfile.this, R.layout.doctor_selector_row, arrDoctors);

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

    /***** FETCH THE DOCTOR'S PROFILE *****/
    private void fetchDoctorProfile() {
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

                            /* GET THE DOCTOR'S EXPERIENCE */
                            if (JODoctors.has("doctorExperience")) {
                                final String doctorExperience = JODoctors.getString("doctorExperience");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtDoctorExperience.setText(doctorExperience + " years experience");
                                    }
                                });
                            }

                            /* GET THE USER DISPLAY PROFILE */
                            if (JODoctors.has("doctorDisplayProfile")) {
                                final String DOCTOR_DISPLAY_PROFILE = JODoctors.getString("doctorDisplayProfile");
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(getApplicationContext())
                                                .load(DOCTOR_DISPLAY_PROFILE)
                                                .crossFade()
                                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                                .centerCrop()
                                                .into(imgvwProfile);
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

    /***** FETCH THE CLINICS THE DOCTOR PRACTICES AT *****/
    private void fetchClinics() {
        String USER_CLINIC_URL = "http://leodyssey.com/ZenPets/public/checkDoctorClinic";
        // String USER_CLINIC_URL = "http://192.168.11.2/zenpets/public/checkDoctorClinic";
        HttpUrl.Builder builder = HttpUrl.parse(USER_CLINIC_URL).newBuilder();
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
                                    /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY VIEW */
                                    listClinics.setVisibility(View.VISIBLE);
                                    linlaEmpty.setVisibility(View.GONE);

                                    /* CONFIGURE THE RECYCLER VIEW **/
                                    configRecycler();

                                    /* INSTANTIATE THE COUNTRIES ADAPTER */
                                    adapter = new DoctorClinicsAdapter(DoctorsProfile.this, arrClinics);

                                    /* SET THE CLINICS RECYCLER VIEW */
                                    listClinics.setAdapter(adapter);
                                }
                            });

                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                    listClinics.setVisibility(View.GONE);
                                    linlaEmpty.setVisibility(View.VISIBLE);
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

        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "DOCTORS";
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
            default:
                break;
        }
        return false;
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listClinics.setLayoutManager(manager);
        listClinics.setHasFixedSize(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_CLINIC && resultCode == RESULT_OK)  {
            /* CLEAR THE ARRAY LIST */
            arrClinics.clear();

            /* FETCH THE CLINICS THE DOCTOR PRACTICES AT */
            fetchClinics();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}