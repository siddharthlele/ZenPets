package com.zenpets.doctors.creators.doctor;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.doctors.DoctorPrefixAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class EducationRecordCreator extends AppCompatActivity {

    /** THE INCOMING DOCTOR ID **/
    String DOCTOR_ID = null;

    /** DATA TYPES TO HOLD THE ENTERED DATA **/
    String DOCTOR_EDUCATION = null;
    String COLLEGE_NAME = null;
    String EDUCATION_YEAR = null;

    /** A PROGRESS DIALOG INSTANCE **/
    ProgressDialog dialog;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.spnEducation) AppCompatSpinner spnEducation;
    @BindView(R.id.inputCollege) TextInputLayout inputCollege;
    @BindView(R.id.edtCollege) AppCompatEditText edtCollege;
    @BindView(R.id.spnYear) AppCompatSpinner spnYear;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.education_record_creator);
        ButterKnife.bind(this);

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* POPULATE THE EDUCATIONAL QUALIFICATIONS SPINNER */
        String[] strServes = getResources().getStringArray(R.array.doctor_education);
        final List<String> arrEducation;
        arrEducation = Arrays.asList(strServes);
        spnEducation.setAdapter(new DoctorPrefixAdapter(
                EducationRecordCreator.this,
                R.layout.custom_spinner_row,
                arrEducation));

        /* CHANGE THE EDUCATIONAL QUALIFICATION */
        spnEducation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DOCTOR_EDUCATION = arrEducation.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* POPULATE THE YEARS SPINNER **/
        final ArrayList<String> years = new ArrayList<>();
        int thisYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = 1900; i <= thisYear; i++) {
            years.add(Integer.toString(i));
        }
        spnYear.setAdapter(new DoctorPrefixAdapter(
                EducationRecordCreator.this,
                R.layout.custom_spinner_row,
                years));

        /* CHANGE THE EDUCATIONAL QUALIFICATION YEAR */
        spnYear.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                EDUCATION_YEAR = years.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /***** CHECK ALL QUALIFICATION DETAILS  *****/
    private void checkQualifications() {
        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtCollege.getWindowToken(), 0);

        /* COLLECT ALL THE DATA */
        COLLEGE_NAME = edtCollege.getText().toString().trim();

        /* VALIDATE THE DATA **/
        if (TextUtils.isEmpty(COLLEGE_NAME))    {
            inputCollege.setError("Please provide the college name");
        } else {

            /* SHOW THE PROGRESS DIALOG WHILE UPLOADING THE IMAGE **/
            dialog = new ProgressDialog(this);
            dialog.setMessage("Adding new educational qualification....");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();

            /* THE URL TO CREATE A NEW EDUCATIONAL RECORD */
            String URL_NEW_TRANS_RECORD = "http://leodyssey.com/ZenPets/public/newDoctorEducation";
            // String URL_NEW_TRANS_RECORD = "http://192.168.11.2/zenpets/public/newDoctorEducation";
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("doctorID", DOCTOR_ID)
                    .add("doctorCollegeName", COLLEGE_NAME)
                    .add("doctorEducationName", DOCTOR_EDUCATION)
                    .add("doctorEducationYear", EDUCATION_YEAR)
                    .build();
            Request request = new Request.Builder()
                    .url(URL_NEW_TRANS_RECORD)
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
                                    Toast.makeText(getApplicationContext(), "Successfully added the Educational Qualification", Toast.LENGTH_SHORT).show();
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
                                    Toast.makeText(getApplicationContext(), "There was an error adding the new educational qualification. Please try again", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Clinic Details";
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
        MenuInflater inflater = new MenuInflater(EducationRecordCreator.this);
        inflater.inflate(R.menu.activity_save_cancel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menuSave:
                /* CHECK ALL QUALIFICATION DETAILS  */
                checkQualifications();
                break;
            case R.id.menuCancel:
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    /** GET THE INCOMING DATA **/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID"))    {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            if (TextUtils.isEmpty(DOCTOR_ID))   {
                Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}