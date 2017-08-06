package com.zenpets.doctors.utils.helpers;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TimePicker;
import android.widget.Toast;

import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.clinics.ClinicSelectorAdapter;
import com.zenpets.doctors.utils.models.clinics.ClinicsData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class TimingsPickerActivity extends AppCompatActivity {

    /** THE DOCTOR ID AND THE CLINIC ID**/
    String DOCTOR_ID = null;
    String CLINIC_ID = null;

    /** DATA TYPES TO STORE THE TIMING DETAILS **/
    boolean blnSundayMorning = false;
    String strSunMorStatus = null;
    String SUN_MOR_FROM = null;
    String SUN_MOR_TO = null;
    boolean blnSundayAfternoon = false;
    String strSunAftStatus = null;
    String SUN_AFT_FROM = null;
    String SUN_AFT_TO = null;

    boolean blnMondayMorning = false;
    String strMonMorStatus = null;
    String MON_MOR_FROM = null;
    String MON_MOR_TO = null;
    boolean blnMondayAfternoon = false;
    String strMonAftStatus = null;
    String MON_AFT_FROM = null;
    String MON_AFT_TO = null;

    boolean blnTuesdayMorning = false;
    String strTueMorStatus = null;
    String TUE_MOR_FROM = null;
    String TUE_MOR_TO = null;
    boolean blnTuesdayAfternoon = false;
    String strTueAftStatus = null;
    String TUE_AFT_FROM = null;
    String TUE_AFT_TO = null;

    boolean blnWednesdayMorning = false;
    String strWedMorStatus = null;
    String WED_MOR_FROM = null;
    String WED_MOR_TO = null;
    boolean blnWednesdayAfternoon = false;
    String strWedAftStatus = null;
    String WED_AFT_FROM = null;
    String WED_AFT_TO = null;

    boolean blnThursdayMorning = false;
    String strThuMorStatus = null;
    String THU_MOR_FROM = null;
    String THU_MOR_TO = null;
    boolean blnThursdayAfternoon = false;
    String strThuAftStatus = null;
    String THU_AFT_FROM = null;
    String THU_AFT_TO = null;

    boolean blnFridayMorning = false;
    String strFriMorStatus = null;
    String FRI_MOR_FROM = null;
    String FRI_MOR_TO = null;
    boolean blnFridayAfternoon = false;
    String strFriAftStatus = null;
    String FRI_AFT_FROM = null;
    String FRI_AFT_TO = null;

    boolean blnSaturdayMorning = false;
    String strSatMorStatus = null;
    String SAT_MOR_FROM = null;
    String SAT_MOR_TO = null;
    boolean blnSaturdayAfternoon = false;
    String strSatAftStatus = null;
    String SAT_AFT_FROM = null;
    String SAT_AFT_TO = null;

    /** THE CLINIC SELECTOR ADAPTER AND ARRAY LIST **/
    ClinicSelectorAdapter clinicSelectorAdapter;
    ArrayList<ClinicsData> arrClinics = new ArrayList<>();

    /** CAST THE CLINIC SELECTOR SPINNER **/
    @BindView(R.id.spnClinics) AppCompatSpinner spnClinics;

    /** CAST THE SUNDAY LAYOUT ELEMENTS **/
    @BindView(R.id.chkbxSundayMorning) AppCompatCheckBox chkbxSundayMorning;
    @BindView(R.id.edtSundayMorningFrom) AppCompatEditText edtSundayMorningFrom;
    @BindView(R.id.btnSunMorningFrom) AppCompatButton btnSunMorningFrom;
    @BindView(R.id.edtSundayMorningTo) AppCompatEditText edtSundayMorningTo;
    @BindView(R.id.btnSunMorningTo) AppCompatButton btnSunMorningTo;

    @BindView(R.id.chkbxSundayAfternoon) AppCompatCheckBox chkbxSundayAfternoon;
    @BindView(R.id.edtSundayAfternoonFrom) AppCompatEditText edtSundayAfternoonFrom;
    @BindView(R.id.btnSunAfternoonFrom) AppCompatButton btnSunAfternoonFrom;
    @BindView(R.id.edtSundayAfternoonTo) AppCompatEditText edtSundayAfternoonTo;
    @BindView(R.id.btnSunAfternoonTo) AppCompatButton btnSunAfternoonTo;

    /** CAST THE MONDAY LAYOUT ELEMENTS **/
    @BindView(R.id.chkbxMondayMorning) AppCompatCheckBox chkbxMondayMorning;
    @BindView(R.id.edtMondayMorningFrom) AppCompatEditText edtMondayMorningFrom;
    @BindView(R.id.btnMonMorningFrom) AppCompatButton btnMonMorningFrom;
    @BindView(R.id.edtMondayMorningTo) AppCompatEditText edtMondayMorningTo;
    @BindView(R.id.btnMonMorningTo) AppCompatButton btnMonMorningTo;

    @BindView(R.id.chkbxMondayAfternoon) AppCompatCheckBox chkbxMondayAfternoon;
    @BindView(R.id.edtMondayAfternoonFrom) AppCompatEditText edtMondayAfternoonFrom;
    @BindView(R.id.btnMonAfternoonFrom) AppCompatButton btnMonAfternoonFrom;
    @BindView(R.id.edtMondayAfternoonTo) AppCompatEditText edtMondayAfternoonTo;
    @BindView(R.id.btnMonAfternoonTo) AppCompatButton btnMonAfternoonTo;

    /** CAST THE TUESDAY LAYOUT ELEMENTS **/
    @BindView(R.id.chkbxTuesdayMorning) AppCompatCheckBox chkbxTuesdayMorning;
    @BindView(R.id.edtTuesdayMorningFrom) AppCompatEditText edtTuesdayMorningFrom;
    @BindView(R.id.btnTueMorningFrom) AppCompatButton btnTueMorningFrom;
    @BindView(R.id.edtTuesdayMorningTo) AppCompatEditText edtTuesdayMorningTo;
    @BindView(R.id.btnTueMorningTo) AppCompatButton btnTueMorningTo;

    @BindView(R.id.chkbxTuesdayAfternoon) AppCompatCheckBox chkbxTuesdayAfternoon;
    @BindView(R.id.edtTuesdayAfternoonFrom) AppCompatEditText edtTuesdayAfternoonFrom;
    @BindView(R.id.btnTueAfternoonFrom) AppCompatButton btnTueAfternoonFrom;
    @BindView(R.id.edtTuesdayAfternoonTo) AppCompatEditText edtTuesdayAfternoonTo;
    @BindView(R.id.btnTueAfternoonTo) AppCompatButton btnTueAfternoonTo;

    /** CAST THE WEDNESDAY LAYOUT ELEMENTS **/
    @BindView(R.id.chkbxWednesdayAfternoon) AppCompatCheckBox chkbxWednesdayAfternoon;
    @BindView(R.id.edtWednesdayMorningFrom) AppCompatEditText edtWednesdayMorningFrom;
    @BindView(R.id.btnWedMorningFrom) AppCompatButton btnWedMorningFrom;
    @BindView(R.id.edtWednesdayMorningTo) AppCompatEditText edtWednesdayMorningTo;
    @BindView(R.id.btnWedMorningTo) AppCompatButton btnWedMorningTo;

    @BindView(R.id.chkbxWednesdayMorning) AppCompatCheckBox chkbxWednesdayMorning;
    @BindView(R.id.edtWednesdayAfternoonFrom) AppCompatEditText edtWednesdayAfternoonFrom;
    @BindView(R.id.btnWedAfternoonFrom) AppCompatButton btnWedAfternoonFrom;
    @BindView(R.id.edtWednesdayAfternoonTo) AppCompatEditText edtWednesdayAfternoonTo;
    @BindView(R.id.btnWedAfternoonTo) AppCompatButton btnWedAfternoonTo;

    /** CAST THE THURSDAY LAYOUT ELEMENTS **/
    @BindView(R.id.chkbxThursdayMorning) AppCompatCheckBox chkbxThursdayMorning;
    @BindView(R.id.edtThursdayMorningFrom) AppCompatEditText edtThursdayMorningFrom;
    @BindView(R.id.btnThuMorningFrom) AppCompatButton btnThuMorningFrom;
    @BindView(R.id.edtThursdayMorningTo) AppCompatEditText edtThursdayMorningTo;
    @BindView(R.id.btnThuMorningTo) AppCompatButton btnThuMorningTo;

    @BindView(R.id.chkbxThursdayAfternoon) AppCompatCheckBox chkbxThursdayAfternoon;
    @BindView(R.id.edtThursdayAfternoonFrom) AppCompatEditText edtThursdayAfternoonFrom;
    @BindView(R.id.btnThuAfternoonFrom) AppCompatButton btnThuAfternoonFrom;
    @BindView(R.id.edtThursdayAfternoonTo) AppCompatEditText edtThursdayAfternoonTo;
    @BindView(R.id.btnThuAfternoonTo) AppCompatButton btnThuAfternoonTo;

    /** CAST THE FRIDAY LAYOUT ELEMENTS **/
    @BindView(R.id.chkbxFridayMorning) AppCompatCheckBox chkbxFridayMorning;
    @BindView(R.id.edtFridayMorningFrom) AppCompatEditText edtFridayMorningFrom;
    @BindView(R.id.btnFriMorningFrom) AppCompatButton btnFriMorningFrom;
    @BindView(R.id.edtFridayMorningTo) AppCompatEditText edtFridayMorningTo;
    @BindView(R.id.btnFriMorningTo) AppCompatButton btnFriMorningTo;

    @BindView(R.id.chkbxFridayAfternoon) AppCompatCheckBox chkbxFridayAfternoon;
    @BindView(R.id.edtFridayAfternoonFrom) AppCompatEditText edtFridayAfternoonFrom;
    @BindView(R.id.btnFriAfternoonFrom) AppCompatButton btnFriAfternoonFrom;
    @BindView(R.id.edtFridayAfternoonTo) AppCompatEditText edtFridayAfternoonTo;
    @BindView(R.id.btnFriAfternoonTo) AppCompatButton btnFriAfternoonTo;

    /** CAST THE SATURDAY LAYOUT ELEMENTS **/
    @BindView(R.id.chkbxSaturdayMorning) AppCompatCheckBox chkbxSaturdayMorning;
    @BindView(R.id.edtSaturdayMorningFrom) AppCompatEditText edtSaturdayMorningFrom;
    @BindView(R.id.btnSatMorningFrom) AppCompatButton btnSatMorningFrom;
    @BindView(R.id.edtSaturdayMorningTo) AppCompatEditText edtSaturdayMorningTo;
    @BindView(R.id.btnSatMorningTo) AppCompatButton btnSatMorningTo;

    @BindView(R.id.chkbxSaturdayAfternoon) AppCompatCheckBox chkbxSaturdayAfternoon;
    @BindView(R.id.edtSaturdayAfternoonFrom) AppCompatEditText edtSaturdayAfternoonFrom;
    @BindView(R.id.btnSatAfternoonFrom) AppCompatButton btnSatAfternoonFrom;
    @BindView(R.id.edtSaturdayAfternoonTo) AppCompatEditText edtSaturdayAfternoonTo;
    @BindView(R.id.btnSatAfternoonTo) AppCompatButton btnSatAfternoonTo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.timings_picker_activity);
        ButterKnife.bind(this);

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* SELECT A CLINIC TO SHOW THE DOCTOR'S TIMINGS AT IT */
        spnClinics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CLINIC_ID = arrClinics.get(position).getClinicID();
                if (CLINIC_ID == null)  {
                    Toast.makeText(getApplicationContext(), "You haven't added any clinics to your account yet", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* CHECK IF SUNDAY MORNING IS CHECKED */
        chkbxSundayMorning.setOnCheckedChangeListener(sundayMorChecker);

        /* CHECK IF SUNDAY AFTERNOON IS CHECKED */
        chkbxSundayAfternoon.setOnCheckedChangeListener(sundayAftChecker);

        /* CHECK IF MONDAY MORNING IS CHECKED */
        chkbxMondayMorning.setOnCheckedChangeListener(mondayMorChecker);

        /* CHECK IF MONDAY AFTERNOON IS CHECKED */
        chkbxMondayAfternoon.setOnCheckedChangeListener(mondayAftChecker);

        /* CHECK IF TUESDAY MORNING IS CHECKED */
        chkbxTuesdayMorning.setOnCheckedChangeListener(tuesdayMorChecker);

        /* CHECK IF TUESDAY AFTERNOON IS CHECKED */
        chkbxTuesdayAfternoon.setOnCheckedChangeListener(tuesdayAftChecker);

        /* CHECK IF WEDNESDAY MORNING IS CHECKED */
        chkbxWednesdayMorning.setOnCheckedChangeListener(wednesdayMorChecker);

        /* CHECK IF WEDNESDAY AFTERNOON IS CHECKED */
        chkbxWednesdayAfternoon.setOnCheckedChangeListener(wednesdayAftChecker);

        /* CHECK IF THURSDAY MORNING IS CHECKED */
        chkbxThursdayMorning.setOnCheckedChangeListener(thursdayMorChecker);

        /* CHECK IF THURSDAY AFTERNOON IS CHECKED */
        chkbxThursdayAfternoon.setOnCheckedChangeListener(thursdayAftChecker);

        /* CHECK IF FRIDAY MORNING IS CHECKED */
        chkbxFridayMorning.setOnCheckedChangeListener(fridayMorChecker);

        /* CHECK IF FRIDAY AFTERNOON IS CHECKED */
        chkbxFridayAfternoon.setOnCheckedChangeListener(fridayAftChecker);

        /* CHECK IF SATURDAY MORNING IS CHECKED */
        chkbxSaturdayMorning.setOnCheckedChangeListener(saturdayMorChecker);

        /* CHECK IF SATURDAY AFTERNOON IS CHECKED */
        chkbxSaturdayAfternoon.setOnCheckedChangeListener(saturdayAftChecker);
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
                                    /* INSTANTIATE THE CLINICS SELECTOR ADAPTER */
                                    clinicSelectorAdapter = new ClinicSelectorAdapter(
                                            TimingsPickerActivity.this,
                                            R.layout.custom_spinner_row,
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

    /** GET THE INCOMING DATA **/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID"))    {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            if (TextUtils.isEmpty(DOCTOR_ID))   {
                Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
            } else {
                /* FETCH THE LIST OF CLINICS */
                fetchClinics();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
        }
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Clinic Timings";
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(TimingsPickerActivity.this);
        inflater.inflate(R.menu.activity_save_cancel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent back = new Intent();
                setResult(RESULT_CANCELED, back);
                this.finish();
                break;
            case R.id.menuSave:
                /* PROCESS THE SUNDAY TIMINGS */
                if (blnSundayMorning)   {
                    strSunMorStatus = "Open";
                    SUN_MOR_FROM = edtSundayMorningFrom.getText().toString().trim();
                    SUN_MOR_TO = edtSundayMorningTo.getText().toString().trim();
                } else {
                    strSunMorStatus = "Closed";
                    SUN_MOR_FROM = "null";
                    SUN_MOR_TO = "null";
                }
                if (blnSundayAfternoon) {
                    strSunAftStatus = "Open";
                    SUN_AFT_FROM = edtSundayAfternoonFrom.getText().toString().trim();
                    SUN_AFT_TO = edtSundayAfternoonTo.getText().toString().trim();
                } else {
                    strSunAftStatus = "Closed";
                    SUN_AFT_FROM = "null";
                    SUN_AFT_TO = "null";
                }

                /* PROCESS THE MONDAY TIMINGS */
                if (blnMondayMorning)   {
                    strMonMorStatus = "Open";
                    MON_MOR_FROM = edtMondayMorningFrom.getText().toString().trim();
                    MON_MOR_TO = edtMondayMorningTo.getText().toString().trim();
                } else {
                    strMonMorStatus = "Closed";
                    MON_MOR_FROM = "null";
                    MON_MOR_TO = "null";
                }
                if (blnMondayAfternoon) {
                    strMonAftStatus = "Open";
                    MON_AFT_FROM = edtMondayAfternoonFrom.getText().toString().trim();
                    MON_AFT_TO = edtMondayAfternoonTo.getText().toString().trim();
                } else {
                    strMonAftStatus = "Closed";
                    MON_AFT_FROM = "null";
                    MON_AFT_TO = "null";
                }

                /* PROCESS THE TUESDAY TIMINGS */
                if (blnTuesdayMorning)   {
                    strTueMorStatus = "Open";
                    TUE_MOR_FROM = edtTuesdayMorningFrom.getText().toString().trim();
                    TUE_MOR_TO = edtTuesdayMorningTo.getText().toString().trim();
                } else {
                    strTueMorStatus = "Closed";
                    TUE_MOR_FROM = "null";
                    TUE_MOR_TO = "null";
                }
                if (blnTuesdayAfternoon) {
                    strTueAftStatus = "Open";
                    TUE_AFT_FROM = edtTuesdayAfternoonFrom.getText().toString().trim();
                    TUE_AFT_TO = edtTuesdayAfternoonTo.getText().toString().trim();
                } else {
                    strTueAftStatus = "Closed";
                    TUE_AFT_FROM = "null";
                    TUE_AFT_TO = "null";
                }

                /* PROCESS THE WEDNESDAY TIMINGS */
                if (blnWednesdayMorning)   {
                    strWedMorStatus = "Open";
                    WED_MOR_FROM = edtWednesdayMorningFrom.getText().toString().trim();
                    WED_MOR_TO = edtWednesdayMorningTo.getText().toString().trim();
                } else {
                    strWedMorStatus = "Closed";
                    WED_MOR_FROM = "null";
                    WED_MOR_TO = "null";
                }
                if (blnWednesdayAfternoon) {
                    strWedAftStatus = "Open";
                    WED_AFT_FROM = edtWednesdayAfternoonFrom.getText().toString().trim();
                    WED_AFT_TO = edtWednesdayAfternoonTo.getText().toString().trim();
                } else {
                    strWedAftStatus = "Closed";
                    WED_AFT_FROM = "null";
                    WED_AFT_TO = "null";
                }

                /* PROCESS THE THURSDAY TIMINGS */
                if (blnThursdayMorning)   {
                    strThuMorStatus = "Open";
                    THU_MOR_FROM = edtThursdayMorningFrom.getText().toString().trim();
                    THU_MOR_TO = edtThursdayMorningTo.getText().toString().trim();
                } else {
                    strThuMorStatus = "Closed";
                    THU_MOR_FROM = "null";
                    THU_MOR_TO = "null";
                }
                if (blnThursdayAfternoon) {
                    strThuAftStatus = "Open";
                    THU_AFT_FROM = edtThursdayAfternoonFrom.getText().toString().trim();
                    THU_AFT_TO = edtThursdayAfternoonTo.getText().toString().trim();
                } else {
                    strThuAftStatus = "Closed";
                    THU_AFT_FROM = "null";
                    THU_AFT_TO = "null";
                }

                /* PROCESS THE FRIDAY TIMINGS */
                if (blnFridayMorning)   {
                    strFriMorStatus = "Open";
                    FRI_MOR_FROM = edtFridayMorningFrom.getText().toString().trim();
                    FRI_MOR_TO = edtFridayMorningTo.getText().toString().trim();
                } else {
                    strFriMorStatus = "Closed";
                    FRI_MOR_FROM = "null";
                    FRI_MOR_TO = "null";
                }
                if (blnFridayAfternoon) {
                    strFriAftStatus = "Open";
                    FRI_AFT_FROM = edtFridayAfternoonFrom.getText().toString().trim();
                    FRI_AFT_TO = edtFridayAfternoonTo.getText().toString().trim();
                } else {
                    strFriAftStatus = "Closed";
                    FRI_AFT_FROM = "null";
                    FRI_AFT_TO = "null";
                }

                /* PROCESS THE SATURDAY TIMINGS */
                if (blnSaturdayMorning)   {
                    strSatMorStatus = "Open";
                    SAT_MOR_FROM = edtSaturdayMorningFrom.getText().toString().trim();
                    SAT_MOR_TO = edtSaturdayMorningTo.getText().toString().trim();
                } else {
                    strSatMorStatus = "Closed";
                    SAT_MOR_FROM = "null";
                    SAT_MOR_TO = "null";
                }
                if (blnSaturdayAfternoon) {
                    strSatAftStatus = "Open";
                    SAT_AFT_FROM = edtSaturdayAfternoonFrom.getText().toString().trim();
                    SAT_AFT_TO = edtSaturdayAfternoonTo.getText().toString().trim();
                } else {
                    strSatAftStatus = "Closed";
                    SAT_AFT_FROM = "null";
                    SAT_AFT_TO = "null";
                }

                /* THE URL TO CREATE NEW TIMING RECORD */
                String URL_NEW_SPECIALIZATION = "http://leodyssey.com/ZenPets/public/newDoctorTimings";
                // String URL_NEW_SPECIALIZATION = "http://192.168.11.2/zenpets/public/newDoctorTimings";
                OkHttpClient client = new OkHttpClient();
                RequestBody body = new FormBody.Builder()
                        .add("doctorID", DOCTOR_ID)
                        .add("clinicID", CLINIC_ID)
                        .add("sunMorFrom", SUN_MOR_FROM)
                        .add("sunMorTo", SUN_MOR_TO)
                        .add("sunAftFrom", SUN_AFT_FROM)
                        .add("sunAftTo", SUN_AFT_TO)
                        .add("monMorFrom", MON_MOR_FROM)
                        .add("monMorTo", MON_MOR_TO)
                        .add("monAftFrom", MON_AFT_FROM)
                        .add("monAftTo", MON_AFT_TO)
                        .add("tueMorFrom", TUE_MOR_FROM)
                        .add("tueMorTo", TUE_MOR_TO)
                        .add("tueAftFrom", TUE_AFT_FROM)
                        .add("tueAftTo", TUE_AFT_TO)
                        .add("wedMorFrom", WED_MOR_FROM)
                        .add("wedMorTo", WED_MOR_TO)
                        .add("wedAftFrom", WED_AFT_FROM)
                        .add("wedAftTo", WED_AFT_TO)
                        .add("thuMorFrom", THU_MOR_FROM)
                        .add("thuMorTo", THU_MOR_TO)
                        .add("thuAftFrom", THU_AFT_FROM)
                        .add("thuAftTo", THU_AFT_TO)
                        .add("friMorFrom", FRI_MOR_FROM)
                        .add("friMorTo", FRI_MOR_TO)
                        .add("friAftFrom", FRI_AFT_FROM)
                        .add("friAftTo", FRI_AFT_TO)
                        .add("satMorFrom", SAT_MOR_FROM)
                        .add("satMorTo", SAT_MOR_TO)
                        .add("satAftFrom", SAT_AFT_FROM)
                        .add("satAftTo", SAT_AFT_TO)
                        .build();
                Request request = new Request.Builder()
                        .url(URL_NEW_SPECIALIZATION)
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
                                        Toast.makeText(getApplicationContext(), "Successfully added the new Timings", Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(getApplicationContext(), "There was an error adding your new timings. Please try again", Toast.LENGTH_LONG).show();
                                    }
                                });
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                /* FINISH THE ACTIVITY */
                Toast.makeText(getApplicationContext(), "Successfully added the Doctor's Timings....", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();

                break;
            case R.id.menuCancel:
                Intent cancel = new Intent();
                setResult(RESULT_CANCELED, cancel);
                this.finish();
                break;
            default:
                break;
        }
        return false;
    }

    /** SUNDAY MORNING CHECKER **/
    private CompoundButton.OnCheckedChangeListener sundayMorChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnSundayMorning = true;
                btnSunMorningFrom.setEnabled(true);
                btnSunMorningTo.setEnabled(true);
            } else {
                blnSundayMorning = false;
                btnSunMorningFrom.setEnabled(false);
                btnSunMorningTo.setEnabled(false);
            }
        }
    };

    /** SUNDAY AFTERNOON CHECKER **/
    private CompoundButton.OnCheckedChangeListener sundayAftChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnSundayAfternoon = true;
                btnSunAfternoonFrom.setEnabled(true);
                btnSunAfternoonTo.setEnabled(true);
            } else {
                blnSundayAfternoon = false;
                btnSunAfternoonFrom.setEnabled(false);
                btnSunAfternoonTo.setEnabled(false);
            }
        }
    };

    /** MONDAY MORNING CHECKER **/
    private CompoundButton.OnCheckedChangeListener mondayMorChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnMondayMorning = true;
                btnMonMorningFrom.setEnabled(true);
                btnMonMorningTo.setEnabled(true);
            } else {
                blnMondayMorning = false;
                btnMonMorningFrom.setEnabled(false);
                btnMonMorningTo.setEnabled(false);
            }
        }
    };

    /** MONDAY AFTERNOON CHECKER **/
    private CompoundButton.OnCheckedChangeListener mondayAftChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnMondayAfternoon = true;
                btnMonAfternoonFrom.setEnabled(true);
                btnMonAfternoonTo.setEnabled(true);
            } else {
                blnMondayAfternoon = false;
                btnMonAfternoonFrom.setEnabled(false);
                btnMonAfternoonTo.setEnabled(false);
            }
        }
    };

    /** TUESDAY MORNING CHECKER **/
    private CompoundButton.OnCheckedChangeListener tuesdayMorChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnTuesdayMorning = true;
                btnTueMorningFrom.setEnabled(true);
                btnTueMorningTo.setEnabled(true);
            } else {
                blnTuesdayMorning = false;
                btnTueMorningFrom.setEnabled(false);
                btnTueMorningTo.setEnabled(false);
            }
        }
    };

    /** TUESDAY AFTERNOON CHECKER **/
    private CompoundButton.OnCheckedChangeListener tuesdayAftChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnTuesdayAfternoon = true;
                btnTueAfternoonFrom.setEnabled(true);
                btnTueAfternoonTo.setEnabled(true);
            } else {
                blnTuesdayAfternoon = false;
                btnTueAfternoonFrom.setEnabled(false);
                btnTueAfternoonTo.setEnabled(false);
            }
        }
    };

    /** WEDNESDAY MORNING CHECKER **/
    private CompoundButton.OnCheckedChangeListener wednesdayMorChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnWednesdayMorning = true;
                btnWedMorningFrom.setEnabled(true);
                btnWedMorningTo.setEnabled(true);
            } else {
                blnWednesdayMorning = false;
                btnWedMorningFrom.setEnabled(false);
                btnWedMorningTo.setEnabled(false);
            }
        }
    };

    /** WEDNESDAY AFTERNOON CHECKER **/
    private CompoundButton.OnCheckedChangeListener wednesdayAftChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnWednesdayAfternoon = true;
                btnWedAfternoonFrom.setEnabled(true);
                btnWedAfternoonTo.setEnabled(true);
            } else {
                blnWednesdayAfternoon = false;
                btnWedAfternoonFrom.setEnabled(false);
                btnWedAfternoonTo.setEnabled(false);
            }
        }
    };

    /** THURSDAY MORNING CHECKER **/
    private CompoundButton.OnCheckedChangeListener thursdayMorChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnThursdayMorning = true;
                btnThuMorningFrom.setEnabled(true);
                btnThuMorningTo.setEnabled(true);
            } else {
                blnThursdayMorning = false;
                btnThuMorningFrom.setEnabled(false);
                btnThuMorningTo.setEnabled(false);
            }
        }
    };

    /** THURSDAY AFTERNOON CHECKER **/
    private CompoundButton.OnCheckedChangeListener thursdayAftChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnThursdayAfternoon = true;
                btnThuAfternoonFrom.setEnabled(true);
                btnThuAfternoonTo.setEnabled(true);
            } else {
                blnThursdayAfternoon = false;
                btnThuAfternoonFrom.setEnabled(false);
                btnThuAfternoonTo.setEnabled(false);
            }
        }
    };

    /** FRIDAY MORNING CHECKER **/
    private CompoundButton.OnCheckedChangeListener fridayMorChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnFridayMorning = true;
                btnFriMorningFrom.setEnabled(true);
                btnFriMorningTo.setEnabled(true);
            } else {
                blnFridayMorning = false;
                btnFriMorningFrom.setEnabled(false);
                btnFriMorningTo.setEnabled(false);
            }
        }
    };

    /** FRIDAY AFTERNOON CHECKER **/
    private CompoundButton.OnCheckedChangeListener fridayAftChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnFridayAfternoon = true;
                btnFriAfternoonFrom.setEnabled(true);
                btnFriAfternoonTo.setEnabled(true);
            } else {
                blnFridayAfternoon = false;
                btnFriAfternoonFrom.setEnabled(false);
                btnFriAfternoonTo.setEnabled(false);
            }
        }
    };

    /** SATURDAY MORNING CHECKER **/
    private CompoundButton.OnCheckedChangeListener saturdayMorChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnSaturdayMorning = true;
                btnSatMorningFrom.setEnabled(true);
                btnSatMorningTo.setEnabled(true);
            } else {
                blnSaturdayMorning = false;
                btnSatMorningFrom.setEnabled(false);
                btnSatMorningTo.setEnabled(false);
            }
        }
    };

    /** SATURDAY AFTERNOON CHECKER **/
    private CompoundButton.OnCheckedChangeListener saturdayAftChecker = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked)  {
                blnSaturdayAfternoon = true;
                btnSatAfternoonFrom.setEnabled(true);
                btnSatAfternoonTo.setEnabled(true);
            } else {
                blnSaturdayAfternoon = false;
                btnSatAfternoonFrom.setEnabled(false);
                btnSatAfternoonTo.setEnabled(false);
            }
        }
    };

    /** SELECT THE SUNDAY MORNING "FROM" TIME **/
    @OnClick(R.id.btnSunMorningFrom) void sunMorFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtSundayMorningFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE SUNDAY MORNING "TO" TIME **/
    @OnClick(R.id.btnSunMorningTo) void sunMorTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtSundayMorningTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE SUNDAY AFTERNOON "FROM" TIME **/
    @OnClick(R.id.btnSunAfternoonFrom) void sunAftFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtSundayAfternoonFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE SUNDAY AFTERNOON "TO" TIME **/
    @OnClick(R.id.btnSunAfternoonTo) void sunAftTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtSundayAfternoonTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE MONDAY MORNING "FROM" TIME **/
    @OnClick(R.id.btnMonMorningFrom) void monMorFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtMondayMorningFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE MONDAY MORNING "TO" TIME **/
    @OnClick(R.id.btnMonMorningTo) void monMorTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtMondayMorningTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE MONDAY AFTERNOON "FROM" TIME **/
    @OnClick(R.id.btnMonAfternoonFrom) void monAftFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtMondayAfternoonFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE MONDAY AFTERNOON "TO" TIME **/
    @OnClick(R.id.btnMonAfternoonTo) void monAftTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtMondayAfternoonTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE TUESDAY MORNING "FROM" TIME **/
    @OnClick(R.id.btnTueMorningFrom) void tueMorFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtTuesdayMorningFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE TUESDAY MORNING "TO" TIME **/
    @OnClick(R.id.btnTueMorningTo) void tueMorTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtTuesdayMorningTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE TUESDAY AFTERNOON "FROM" TIME **/
    @OnClick(R.id.btnTueAfternoonFrom) void tueAftFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtTuesdayAfternoonFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE TUESDAY AFTERNOON "TO" TIME **/
    @OnClick(R.id.btnTueAfternoonTo) void tueAftTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtTuesdayAfternoonTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE WEDNESDAY MORNING "FROM" TIME **/
    @OnClick(R.id.btnWedMorningFrom) void wedMorFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtWednesdayMorningFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE WEDNESDAY MORNING "TO" TIME **/
    @OnClick(R.id.btnWedMorningTo) void wedMorTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtWednesdayMorningTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE WEDNESDAY AFTERNOON "FROM" TIME **/
    @OnClick(R.id.btnWedAfternoonFrom) void wedAftFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtWednesdayAfternoonFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE WEDNESDAY AFTERNOON "TO" TIME **/
    @OnClick(R.id.btnWedAfternoonTo) void wedAftTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtWednesdayAfternoonTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE THURSDAY MORNING "FROM" TIME **/
    @OnClick(R.id.btnThuMorningFrom) void thuMorFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtThursdayMorningFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE THURSDAY MORNING "TO" TIME **/
    @OnClick(R.id.btnThuMorningTo) void thuMorTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtThursdayMorningTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE THURSDAY AFTERNOON "FROM" TIME **/
    @OnClick(R.id.btnThuAfternoonFrom) void thuAftFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtThursdayAfternoonFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE THURSDAY AFTERNOON "TO" TIME **/
    @OnClick(R.id.btnThuAfternoonTo) void thuAftTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtThursdayAfternoonTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE FRIDAY MORNING "FROM" TIME **/
    @OnClick(R.id.btnFriMorningFrom) void friMorFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtFridayMorningFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE FRIDAY MORNING "TO" TIME **/
    @OnClick(R.id.btnFriMorningTo) void friMorTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtFridayMorningTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE FRIDAY AFTERNOON "FROM" TIME **/
    @OnClick(R.id.btnFriAfternoonFrom) void friAftFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtFridayAfternoonFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE FRIDAY AFTERNOON "TO" TIME **/
    @OnClick(R.id.btnFriAfternoonTo) void friAftTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtFridayAfternoonTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE SATURDAY MORNING "FROM" TIME **/
    @OnClick(R.id.btnSatMorningFrom) void satMorFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtSaturdayMorningFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE SATURDAY MORNING "TO" TIME **/
    @OnClick(R.id.btnSatMorningTo) void satMorTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtSaturdayMorningTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE SATURDAY AFTERNOON "FROM" TIME **/
    @OnClick(R.id.btnSatAfternoonFrom) void satAftFrom() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtSaturdayAfternoonFrom.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    /** SELECT THE SATURDAY AFTERNOON "TO" TIME **/
    @OnClick(R.id.btnSatAfternoonTo) void satAftTo() {
        final Calendar calendar = Calendar.getInstance();
        int mHour = calendar.get(Calendar.HOUR_OF_DAY);
        int mMinute = calendar.get(Calendar.MINUTE);
        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                int hour = hourOfDay % 12;
                String time = String.format(Locale.getDefault(), "%02d:%02d %s", hour == 0 ? 12 : hour, minute, hourOfDay < 12 ? "AM" : "PM");
                edtSaturdayAfternoonTo.setText(time);
            }
        }, mHour, mMinute, false);
        timePickerDialog.show();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}