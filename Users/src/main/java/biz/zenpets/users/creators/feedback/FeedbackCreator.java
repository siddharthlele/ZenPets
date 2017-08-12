package biz.zenpets.users.creators.feedback;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
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
import android.widget.ScrollView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.visit.VisitReasonsAdapter;
import biz.zenpets.users.utils.models.visit.VisitReasonsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FeedbackCreator extends AppCompatActivity {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** THE INCOMING DOCTOR AND CLINIC ID **/
    private String DOCTOR_ID = null;
    private String CLINIC_ID = null;

    /** THE USER ID **/
    private String USER_ID = null;

    /** THE DOCTOR DETAILS **/
    private String DOCTOR_PREFIX;
    private String DOCTOR_NAME;
    private String DOCTOR_PROFILE;

    /** DATA TYPES TO HOLD THE USER SELECTIONS **/
    private String RECOMMEND_STATUS = "Yes";
    private String APPOINTMENT_STATUS = "On Time";
    private String VISIT_REASON_ID = null;
    private String DOCTOR_EXPERIENCE = null;

    /** THE VISIT REASONS ARRAY LIST **/
    private final ArrayList<VisitReasonsData> arrReasons = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.imgvwDoctorProfile)CircleImageView imgvwDoctorProfile;
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.scrollContainer) ScrollView scrollContainer;
    @BindView(R.id.groupRecommend) SegmentedButtonGroup groupRecommend;
    @BindView(R.id.groupStartTime) SegmentedButtonGroup groupStartTime;
    @BindView(R.id.spnVisitReason) AppCompatSpinner spnVisitReason;
    @BindView(R.id.edtExperience) AppCompatEditText edtExperience;
    @BindView(R.id.txtTermsOfService) AppCompatTextView txtTermsOfService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feedback_creator);
        ButterKnife.bind(this);

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();

        /** CONFIGURE THE TOOLBAR **/
        configTB();

        /** GET THE INCOMING DATA **/
        getIncomingData();

        /** CHECK THE RECOMMENDATION STATUS **/
        groupRecommend.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0)  {
                    RECOMMEND_STATUS = "Yes";
                } else if (position == 1)   {
                    RECOMMEND_STATUS = "No";
                }
            }
        });

        /** CHECK THE APPOINTMENT START STATUS **/
        groupStartTime.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0)  {
                    APPOINTMENT_STATUS = "On Time";
                } else if (position == 1)   {
                    APPOINTMENT_STATUS = "Ten Minutes Late";
                } else if (position == 2)   {
                    APPOINTMENT_STATUS = "30 Minutes Late";
                } else if (position == 3)   {
                    APPOINTMENT_STATUS = "More Than An Hour late";
                }
            }
        });

        /** GET THE LIST OF VISIT REASONS **/
        new getReasonsList().execute();

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

    /***** GET THE LIST OF VISIT REASONS *****/
    private class getReasonsList extends AsyncTask<Void, Void, Void>    {

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_VISIT_REASONS = "http://leodyssey.com/ZenPets/public/visitReasons";
            String URL_VISIT_REASONS = "http://192.168.11.2/zenpets/public/visitReasons";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(URL_VISIT_REASONS)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JAReasons = JORoot.getJSONArray("reasons");
                    /** A VISIT REASONS DATA INSTANCE **/
                    VisitReasonsData data;
                    for (int i = 0; i < JAReasons.length(); i++) {
                        JSONObject JOReasons = JAReasons.getJSONObject(i);

                        /** INSTANTIATE THE VISIT REASONS DATA INSTANCE **/
                        data = new VisitReasonsData();

                        /** GET THE VISIT REASON ID **/
                        if (JOReasons.has("visitReasonID")) {
                            data.setVisitReasonID(JOReasons.getString("visitReasonID"));
                        } else {
                            data.setVisitReasonID(null);
                        }

                        /** GET THE VISIT REASON **/
                        if (JOReasons.has("visitReason"))   {
                            data.setVisitReason(JOReasons.getString("visitReason"));
                        } else {
                            data.setVisitReason(null);
                        }

                        /** ADD THE COLLECTED DATA TO THE ARRAY LIST **/
                        arrReasons.add(data);
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

            /** INSTANTIATE THE VISIT REASONS ADAPTER **/
            VisitReasonsAdapter adapter = new VisitReasonsAdapter(FeedbackCreator.this, arrReasons);

            /** SET THE ADAPTER TO THE VISIT REASONS SPINNER **/
            spnVisitReason.setAdapter(adapter);
        }
    }

    /***** FETCH THE DOCTOR DETAILS *****/
    private class fetchDoctorDetails extends AsyncTask<Void, Void, Void>    {

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_PROFILE = "http://leodyssey.com/ZenPets/public/fetchDoctorDetails";
            String URL_DOCTOR_PROFILE = "http://192.168.11.2/zenpets/public/fetchDoctorDetails";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_PROFILE).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
            builder.addQueryParameter("clinicID", CLINIC_ID);
            String FINAL_URL = builder.build().toString();
//            Log.e("DOCTOR'S URL", FINAL_URL);
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

                    /** GET THE DOCTOR'S PREFIX AND NAME **/
                    DOCTOR_PREFIX = JORoot.getString("doctorPrefix");
                    DOCTOR_NAME = JORoot.getString("doctorName");

                    /** GET THE DOCTOR'S PROFILE PICTURE **/
                    DOCTOR_PROFILE = JORoot.getString("doctorDisplayProfile");
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
            txtDoctorName.setText(DOCTOR_PREFIX + " " + DOCTOR_NAME);

            /* SET THE DOCTOR'S DISPLAY PROFILE */
            Picasso.with(FeedbackCreator.this)
                    .load(DOCTOR_PROFILE)
                    .placeholder(R.drawable.beagle)
                    .centerCrop()
                    .fit()
                    .into(imgvwDoctorProfile);
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

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Post Feedback";
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
        MenuInflater inflater = new MenuInflater(getApplicationContext());
        inflater.inflate(R.menu.activity_feedback_creator, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.menuSubmit:
                /** VALIDATE FEEDBACK DATA **/
                validateData();
                break;
            default:
                break;
        }
        return false;
    }

    /***** VALIDATE FEEDBACK DATA *****/
    @SuppressWarnings("deprecation")
    private void validateData() {
        /** HIDE THE KEYBOARD **/
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtExperience.getWindowToken(), 0);

        /** GET THE DATA **/
        DOCTOR_EXPERIENCE = edtExperience.getText().toString().trim();

        /** VERIFY ALL REQUIRED DATA **/
        int sdk = Build.VERSION.SDK_INT;
        if (TextUtils.isEmpty(RECOMMEND_STATUS))    {
            if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
                groupRecommend.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.error_background));
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    groupRecommend.setBackground(ContextCompat.getDrawable(this, R.drawable.error_background));
                }
            }
            scrollContainer.smoothScrollTo(0, groupRecommend.getTop());
        } else if (TextUtils.isEmpty(APPOINTMENT_STATUS))   {
            if(sdk < Build.VERSION_CODES.JELLY_BEAN) {
                groupStartTime.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.error_background));
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    groupStartTime.setBackground(ContextCompat.getDrawable(this, R.drawable.error_background));
                }
            }
            scrollContainer.smoothScrollTo(0, groupStartTime.getTop());
        } else if (TextUtils.isEmpty(DOCTOR_EXPERIENCE))    {
            edtExperience.setError("Please provide your experience");
            edtExperience.requestFocus();
        } else {
            edtExperience.setError(null);

            /** POST THE FEEDBACK **/
            postFeedback();
        }
    }

    private void postFeedback() {
        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we publish your feedback..");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        // String URL_NEW_PET = "http://leodyssey.com/ZenPets/public/newDoctorReview";
        String URL_NEW_PET = "http://192.168.11.2/zenpets/public/newDoctorReview";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("doctorID", DOCTOR_ID)
                .add("userID", USER_ID)
                .add("visitReasonID", VISIT_REASON_ID)
                .add("recommendStatus", RECOMMEND_STATUS)
                .add("appointmentStatus", APPOINTMENT_STATUS)
                .add("doctorExperience", DOCTOR_EXPERIENCE)
                .add("reviewTimestamp", String.valueOf(System.currentTimeMillis() / 1000))
                .build();
        Request request = new Request.Builder()
                .url(URL_NEW_PET)
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
                        /* DISMISS THE DIALOG */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Successfully published your feedback", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getApplicationContext(), "There was an error publishing your feedback. Please try again", Toast.LENGTH_LONG).show();
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