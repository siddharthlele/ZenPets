package com.zenpets.doctors.details.appointment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.zenpets.doctors.utils.TypefaceSpan;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AppointmentDetails extends AppCompatActivity {

    /** THE INCOMING APPOINTMENT ID **/
    String APPOINTMENT_ID = null;

    /** THE APPOINTMENT DATE AND TIME **/
    String APPOINTMENT_DATE = null;
    String APPOINTMENT_TIME = null;

    /** THE DOCTOR ID AND CLINIC ID **/
    String DOCTOR_ID = null;
    String CLINIC_ID = null;

    /** THE USER DETAILS **/
    String USER_ID = null;
    String USER_NAME = null;
    String USER_DISPLAY_PROFILE = null;

    /** THE PET DETAILS **/
    String PET_ID = null;
    String PET_NAME = null;
    String PET_DISPLAY_PROFILE = null;

    /** THE VISIT REASON **/
    String VISIT_REASON = null;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(com.zenpets.doctors.R.id.imgvwDoctorProfile) CircleImageView imgvwDoctorProfile;
    @BindView(com.zenpets.doctors.R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(com.zenpets.doctors.R.id.txtClinicDetails) AppCompatTextView txtClinicDetails;
    @BindView(com.zenpets.doctors.R.id.txtDateTime) AppCompatTextView txtDateTime;
    @BindView(com.zenpets.doctors.R.id.imgvwUserProfile) CircleImageView imgvwUserProfile;
    @BindView(com.zenpets.doctors.R.id.txtUserName) AppCompatTextView txtUserName;
    @BindView(com.zenpets.doctors.R.id.imgvwPetProfile) CircleImageView imgvwPetProfile;
    @BindView(com.zenpets.doctors.R.id.txtPetName) AppCompatTextView txtPetName;
    @BindView(com.zenpets.doctors.R.id.txtVisitReason) AppCompatTextView txtVisitReason;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zenpets.doctors.R.layout.appointment_details);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* GET THE INCOMING DATA */
        getIncomingData();
    }

    /***** FETCH APPOINTMENT DETAILS *****/
    private void fetchAppointmentDetails() {
        String URL_APPOINTMENT_DETAILS = "http://leodyssey.com/ZenPets/public/fetchAppointmentDetails";
        // String URL_APPOINTMENT_DETAILS = "http://192.168.11.2/zenpets/public/fetchAppointmentDetails";
        HttpUrl.Builder builder = HttpUrl.parse(URL_APPOINTMENT_DETAILS).newBuilder();
        builder.addQueryParameter("appointmentID", APPOINTMENT_ID);
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
                        Log.e("DETAILS", String.valueOf(JORoot));

                        /* GET THE DOCTOR ID */
                        if (JORoot.has("doctorID")) {
                            DOCTOR_ID = JORoot.getString("doctorID");
                        } else {
                            DOCTOR_ID = null;
                        }

                        /* GET THE USER ID */
                        if (JORoot.has("userID"))   {
                            USER_ID = JORoot.getString("userID");
                        } else {
                            USER_ID = null;
                        }

                        /* GET THE USER NAME */
                        if (JORoot.has("userName")) {
                            USER_NAME = JORoot.getString("userName");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtUserName.setText(USER_NAME);
                                }
                            });
                        } else {
                            USER_NAME = null;
                        }

                        /* GET THE USER DISPLAY PROFILE */
                        if (JORoot.has("userDisplayProfile"))   {
                            USER_DISPLAY_PROFILE = JORoot.getString("userDisplayProfile");
                            if (USER_DISPLAY_PROFILE != null && !USER_DISPLAY_PROFILE.equalsIgnoreCase("null")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(AppointmentDetails.this)
                                                .load(USER_DISPLAY_PROFILE)
                                                .crossFade()
                                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                                .centerCrop()
                                                .into(imgvwUserProfile);
                                    }
                                });
                            }
                        } else {
                            USER_DISPLAY_PROFILE = null;
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
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtPetName.setText(PET_NAME);
                                }
                            });
                        } else {
                            PET_NAME = null;
                        }

                        /* GET THE PET DISPLAY PROFILE */
                        if (JORoot.has("petProfile"))   {
                            PET_DISPLAY_PROFILE = JORoot.getString("petProfile");
                            if (PET_DISPLAY_PROFILE != null && !PET_DISPLAY_PROFILE.equalsIgnoreCase("null")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(AppointmentDetails.this)
                                                .load(PET_DISPLAY_PROFILE)
                                                .crossFade()
                                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                                .centerCrop()
                                                .into(imgvwPetProfile);
                                    }
                                });
                            }
                        } else {
                            PET_DISPLAY_PROFILE = null;
                        }

                        /* GET THE PURPOSE OF THE VISIT */
                        if (JORoot.has("visitReason"))  {
                            VISIT_REASON = JORoot.getString("visitReason");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtVisitReason.setText(VISIT_REASON);
                                }
                            });
                        } else {
                            VISIT_REASON = null;
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

                        /* FORMAT THE APPOINTMENT DATE */
                        if ((APPOINTMENT_TIME != null && APPOINTMENT_DATE != null)) {
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(sdf.parse(APPOINTMENT_DATE + " " + APPOINTMENT_TIME));
                                SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                                final String strFormattedDate = format.format(calendar.getTime());

                                /* SET THE SELECTED DATE AND TIME */
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtDateTime.setText(strFormattedDate + " - " + APPOINTMENT_TIME);
                                    }
                                });
                            } catch (ParseException e) {
                                e.printStackTrace();
                                Log.e("EXCEPTION", e.getMessage());
                            }
                        }

                        if (DOCTOR_ID != null)  {
                            /* FETCH THE DOCTOR DETAILS */
                            fetchDoctorDetails();

                            /* FETCH THE CLINIC DETAILS */
                            fetchClinicDetails();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
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
                                        Glide.with(AppointmentDetails.this)
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

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("APPOINTMENT_ID"))   {
            APPOINTMENT_ID = bundle.getString("APPOINTMENT_ID");
            if (APPOINTMENT_ID != null) {
                /* FETCH APPOINTMENT DETAILS */
                fetchAppointmentDetails();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required data", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(com.zenpets.doctors.R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "APPOINTMENT";
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}