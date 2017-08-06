package com.zenpets.doctors.creators.appointment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.PetsSpinnerAdapter;
import com.zenpets.doctors.utils.adapters.VisitReasonsAdapter;
import com.zenpets.doctors.utils.models.PetsData;
import com.zenpets.doctors.utils.models.VisitReasonsData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class AppointmentDetailsCreator extends AppCompatActivity {

    /** THE INCOMING DETAILS **/
    String DOCTOR_ID = null;
    String CLINIC_ID = null;
    String APPOINTMENT_TIME = null;
    String APPOINTMENT_DATE = null;

    /** THE USER DETAILS **/
    String USER_EMAIL = null;
    String USER_ID = null;
    String USER_NAME = null;
    String USER_PHONE = null;

    /** THE PET DETAILS **/
    String PET_ID = null;
    String PET_NAME = null;

    /** THE VISIT REASON **/
    String VISIT_REASON_ID = null;
    String VISIT_REASON = null;

    /** THE VISIT REASONS ADAPTER AND ARRAY LIST **/
    VisitReasonsAdapter visitReasonsAdapter;
    ArrayList<VisitReasonsData> arrReasons = new ArrayList<>();

    /** THE USER PETS SPINNER ADAPTER AND ARRAY LIST **/
    PetsSpinnerAdapter petsAdapter;
    ArrayList<PetsData> arrPets = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(com.zenpets.doctors.R.id.imgvwDoctorProfile) CircleImageView imgvwDoctorProfile;
    @BindView(com.zenpets.doctors.R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(com.zenpets.doctors.R.id.txtClinicDetails) AppCompatTextView txtClinicDetails;
    @BindView(com.zenpets.doctors.R.id.txtDateTime) AppCompatTextView txtDateTime;
    @BindView(com.zenpets.doctors.R.id.inputUserName) TextInputLayout inputUserName;
    @BindView(com.zenpets.doctors.R.id.edtUserName) AppCompatEditText edtUserName;
    @BindView(com.zenpets.doctors.R.id.spnPet) AppCompatSpinner spnPet;
    @BindView(com.zenpets.doctors.R.id.inputEmailAddress) TextInputLayout inputEmailAddress;
    @BindView(com.zenpets.doctors.R.id.edtEmailAddress) AppCompatEditText edtEmailAddress;
    @BindView(com.zenpets.doctors.R.id.inputPhoneNumber) TextInputLayout inputPhoneNumber;
    @BindView(com.zenpets.doctors.R.id.edtPhoneNumber) AppCompatEditText edtPhoneNumber;
    @BindView(com.zenpets.doctors.R.id.spnVisitReason) AppCompatSpinner spnVisitReason;

    /** PUBLISH THE NEW APPOINTMENT **/
    @OnClick(com.zenpets.doctors.R.id.btnBook) void newAppointment()    {
        /* SHOW THE PROGRESS DIALOG PUBLISHING THE NEW APPOINTMENT **/
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Publishing your appointment....");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtUserName.getWindowToken(), 0);

        /* THE URL TO PUBLISH THE APPOINTMENT */
        String URL_NEW_APPOINTMENT = "http://leodyssey.com/ZenPets/public/newDocAppointment";
        // String URL_NEW_APPOINTMENT = "http://192.168.11.2/zenpets/public/newDocAppointment";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("doctorID", DOCTOR_ID)
                .add("clinicID", CLINIC_ID)
                .add("visitReasonID", VISIT_REASON_ID)
                .add("userID", USER_ID)
                .add("petID", PET_ID)
                .add("appointmentDate", APPOINTMENT_DATE)
                .add("appointmentTime", APPOINTMENT_TIME)
                .build();
        Request request = new Request.Builder()
                .url(URL_NEW_APPOINTMENT)
                .post(body)
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
                        /* FINISH THE ACTIVITY */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* DISMISS THE DIALOG */
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Successfully published your new appointment", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    } else {
                        /* DISMISS THE DIALOG */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "There was an error publishing your appointment. Please try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zenpets.doctors.R.layout.appointment_details_creator);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* SELECT A PET */
        spnPet.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PET_ID = arrPets.get(position).getPetID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* SELECT A VISIT REASON */
        spnVisitReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                VISIT_REASON_ID = arrReasons.get(position).getVisitReasonID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
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
                                        Glide.with(AppointmentDetailsCreator.this)
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
        if (bundle.containsKey("DOCTOR_ID")
                && bundle.containsKey("CLINIC_ID")
                && bundle.containsKey("APPOINTMENT_TIME")
                && bundle.containsKey("APPOINTMENT_DATE")) {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            CLINIC_ID = bundle.getString("CLINIC_ID");
            APPOINTMENT_TIME = bundle.getString("APPOINTMENT_TIME");
            APPOINTMENT_DATE = bundle.getString("APPOINTMENT_DATE");

            if (CLINIC_ID != null && DOCTOR_ID != null && APPOINTMENT_TIME != null && APPOINTMENT_DATE != null) {
                /* FETCH THE DOCTOR DETAILS */
                fetchDoctorDetails();

                /* FETCH THE CLINIC DETAILS */
                fetchClinicDetails();

                /* FETCH THE USER DETAILS */
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    /* GET THE EMAIL ADDRESS */
                    USER_EMAIL = user.getEmail();

                    /* FETCH THE USER DETAILS */
                    fetchUserDetails();
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to get required data....", Toast.LENGTH_SHORT).show();
                    finish();
                }

                /* FORMAT THE INCOMING DATE */
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(sdf.parse(APPOINTMENT_DATE + " " + APPOINTMENT_TIME));
//                    Log.e("DATE", String.valueOf(calendar));
                    SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault());
                    String strFormattedDate = format.format(calendar.getTime());
//                    Log.e("DATE", strFormattedDate);

                    /* SET THE SELECTED DATE AND TIME */
                    txtDateTime.setText(strFormattedDate + " - " + APPOINTMENT_TIME);
                } catch (ParseException e) {
                    e.printStackTrace();
                    Log.e("EXCEPTION", e.getMessage());
                }

                /* FETCH THE VISIT REASONS  */
                fetchVisitReasons();

                /* FETCH THE USER'S PETS */
                fetchUserPets();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get the required data", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get the required data", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** FETCH THE USER'S PETS *****/
    private void fetchUserPets() {
        String URL_USER_DETAILS = "http://leodyssey.com/ZenPets/public/fetchUserPets";
        // String URL_USER_DETAILS = "http://192.168.11.2/zenpets/public/fetchUserPets";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DETAILS).newBuilder();
        builder.addQueryParameter("userEmail", USER_EMAIL);
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
                        JSONArray JAPets = JORoot.getJSONArray("pets");

                        /* A PETS DATA INSTANCE */
                        PetsData data;

                        for (int i = 0; i < JAPets.length(); i++) {
                            JSONObject JOPets = JAPets.getJSONObject(i);
                            Log.e("PETS", String.valueOf(JOPets));

                            /* INSTANTIATE THE PETS DATA INSTANCE */
                            data = new PetsData();

                            /* GET THE PET ID */
                            if (JOPets.has("petID")) {
                                data.setPetID(JOPets.getString("petID"));
                            } else {
                                data.setPetID(null);
                            }

                            /* GET THE USER ID */
                            if (JOPets.has("userID"))   {
                                data.setUserID(JOPets.getString("userID"));
                            } else {
                                data.setUserID(null);
                            }

                            /* GET THE PET'S NAME */
                            if (JOPets.has("petName"))  {
                                data.setPetName(JOPets.getString("petName"));
                            } else {
                                data.setPetName(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrPets.add(data);
                        }

                        /* SETUP THE VISIT REASONS COMPONENTS */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE PETS SPINNER ADAPTER */
                                petsAdapter = new PetsSpinnerAdapter(AppointmentDetailsCreator.this, arrPets);

                                /* SET THE ADAPTER TO THE PETS SPINNER */
                                spnPet.setAdapter(petsAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fetchVisitReasons() {
        String URL_VISIT_REASONS = "http://leodyssey.com/ZenPets/public/visitReasons";
        // String URL_VISIT_REASONS = "http://192.168.11.2/zenpets/public/visitReasons";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL_VISIT_REASONS)
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
                        JSONArray JAReasons = JORoot.getJSONArray("reasons");

                        /* A VISIT REASONS DATA INSTANCE */
                        VisitReasonsData data;

                        for (int i = 0; i < JAReasons.length(); i++) {
                            JSONObject JOReasons = JAReasons.getJSONObject(i);
//                            Log.e("REASONS", String.valueOf(JOReasons));

                            /* INSTANTIATE THE VISIT REASONS DATA INSTANCE */
                            data = new VisitReasonsData();

                            /* GET THE VISIT REASON ID */
                            if (JOReasons.has("visitReasonID")) {
                                data.setVisitReasonID(JOReasons.getString("visitReasonID"));
                            } else {
                                data.setVisitReasonID(null);
                            }

                            /* GET THE VISIT REASON TEXT */
                            if (JOReasons.has("visitReason"))   {
                                data.setVisitReason(JOReasons.getString("visitReason"));
                            } else {
                                data.setVisitReason(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrReasons.add(data);
                        }

                        /* SETUP THE VISIT REASONS COMPONENTS */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE DOCTORS ADAPTER */
                                visitReasonsAdapter = new VisitReasonsAdapter(AppointmentDetailsCreator.this, arrReasons);

                                /* SET THE ADAPTER TO THE DOCTORS SPINNER */
                                spnVisitReason.setAdapter(visitReasonsAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** FETCH THE USER DETAILS *****/
    private void fetchUserDetails() {
        String URL_USER_DETAILS = "http://leodyssey.com/ZenPets/public/fetchProfile";
        // String URL_USER_DETAILS = "http://192.168.11.2/zenpets/public/fetchProfile";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DETAILS).newBuilder();
        builder.addQueryParameter("userEmail", USER_EMAIL);
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

                        /* GET THE USER ID */
                        if (JORoot.has("userID")) {
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
                                    edtUserName.setText(USER_NAME);
                                }
                            });
                        } else {
                            USER_NAME = null;
                        }

                        /* GET THE USER EMAIL ADDRESS */
                        if (JORoot.has("userEmail")) {
                            USER_EMAIL = JORoot.getString("userEmail");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    edtEmailAddress.setText(USER_EMAIL);
                                }
                            });
                        } else {
                            USER_EMAIL = null;
                        }

                        /* GET THE USER'S PHONE NUMBER */
                        if (JORoot.has("userPhoneNumber"))  {
                            USER_PHONE = JORoot.getString("userPhoneNumber");
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    edtPhoneNumber.setText(USER_PHONE);
                                }
                            });
                        } else {
                            USER_PHONE = null;
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

        String strTitle = "Enter contact details";
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
            case com.zenpets.doctors.R.id.menuCancel:
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