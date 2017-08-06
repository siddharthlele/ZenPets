package com.zenpets.doctors.profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zenpets.doctors.R;
import com.zenpets.doctors.creators.clinic.ClinicCreator;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.clinics.DoctorClinicsAdapter;
import com.zenpets.doctors.utils.models.clinics.ClinicsData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class DoctorClinicsActivity extends AppCompatActivity {

    /** THE FIREBASE USER INSTANCE **/
    FirebaseUser user;

    /** THE LOGGED IN USER'S EMAIL ADDRESS **/
    String USER_EMAIL = null;

    /** THE CLINICS ADAPTER AND ARRAY LIST **/
    DoctorClinicsAdapter adapter;
    ArrayList<ClinicsData> arrClinics = new ArrayList<>();

    /** THE REQUEST CODES **/
    private int REQUEST_NEW_CLINIC = 101;
    private int REQUEST_EDIT_CLINIC = 102;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listClinics) RecyclerView listClinics;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.doctor_clinics_list);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* GET THE USER DETAILS */
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            /* GET THE USER ID */
            USER_EMAIL = user.getEmail();

            /* CONFIGURE THE RECYCLER VIEW **/
            configRecycler();

            /* FETCH THE CLINICS THE DOCTOR PRACTICES AT */
            fetchClinics();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_SHORT).show();
        }
    }

    /***** FETCH THE CLINICS THE DOCTOR PRACTICES AT *****/
    private void fetchClinics() {
        String USER_CLINIC_URL = "http://leodyssey.com/ZenPets/public/checkDoctorClinic";
        // String USER_CLINIC_URL = "http://192.168.11.2/zenpets/public/checkDoctorClinic";
        HttpUrl.Builder builder = HttpUrl.parse(USER_CLINIC_URL).newBuilder();
        builder.addQueryParameter("doctorEmail", USER_EMAIL);
        String FINAL_URL = builder.build().toString();
//        Log.e("CLINIC URL", FINAL_URL);
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
                        final JSONArray JAClinics = JORoot.getJSONArray("clinics");

                        /* A CLINICS DATA INSTANCE */
                        ClinicsData data;

                        for (int i = 0; i < JAClinics.length(); i++) {
                            JSONObject JOClinics = JAClinics.getJSONObject(i);
//                            Log.e("CLINICS", String.valueOf(JOClinics));

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
                            if (JOClinics.has("clinicAddress") && JOClinics.has("countryName") && JOClinics.has("stateName") && JOClinics.has("cityName") && JOClinics.has("localityName") && JOClinics.has("clinicPinCode")) {
                                String clinicAddress = JOClinics.getString("clinicAddress");
                                String countryName = JOClinics.getString("countryName");
                                String stateName = JOClinics.getString("stateName");
                                String cityName = JOClinics.getString("cityName");
                                String localityName = JOClinics.getString("localityName");
                                String clinicPinCode = JOClinics.getString("clinicPinCode");
                                String finalAddress = clinicAddress + ", " + cityName + ", " + localityName + ", " + stateName + ", " + countryName + ", " + clinicPinCode;
                                data.setClinicAddress(finalAddress);
                            } else {
                                data.setClinicAddress(null);
                            }

                            /* GET THE CLINIC LANDMARK */
                            if (JOClinics.has("clinicLandmark"))    {
                                String clinicLandmark = JOClinics.getString("clinicLandmark");
                                data.setClinicLandmark(clinicLandmark);
                            } else {
                                data.setClinicLandmark(null);
                            }

                            /* GET THE CLINIC SUBSCRIPTION */
                            if (JOClinics.has("clinicSubscription"))    {
                                String clinicSubscription = JOClinics.getString("clinicSubscription");
                                data.setClinicSubscription(clinicSubscription);
                            } else {
                                data.setClinicSubscription("Free");
                            }

                            /* GET THE CLINIC LOGO */
                            if (JOClinics.has("clinicLogo"))    {
                                String clinicLogo = JOClinics.getString("clinicLogo");
                                if (clinicLogo.equals("") || clinicLogo.equalsIgnoreCase("null") || clinicLogo == null) {
                                    data.setClinicLogo(null);
                                } else {
                                    data.setClinicLogo(clinicLogo);
                                }
                            } else {
                                data.setClinicLogo(null);
                            }

                            /* GET THE CLINIC PHONE 1 */
                            if (JOClinics.has("clinicPhone1"))  {
                                String clinicPhone1 = JOClinics.getString("clinicPhone1");
                                data.setClinicPhone1(clinicPhone1);
                            } else {
                                data.setClinicPhone1(null);
                            }

                            /* GET THE CLINIC PHONE 2 */
                            if (JOClinics.has("clinicPhone2"))  {
                                String clinicPhone2 = JOClinics.getString("clinicPhone2");
                                if (clinicPhone2.equals("") || clinicPhone2.equalsIgnoreCase("null") || clinicPhone2 == null) {
                                    data.setClinicPhone2("N.A");
                                } else {
                                    data.setClinicPhone2(clinicPhone2);
                                }
                            } else {
                                data.setClinicPhone2(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrClinics.add(data);
                        }

                        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE DOCTORS CLINICS ADAPTER */
                                adapter = new DoctorClinicsAdapter(DoctorClinicsActivity.this, arrClinics);

                                /* SET THE CLINICS RECYCLER VIEW */
                                listClinics.setAdapter(adapter);
                            }
                        });
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

        String strTitle = "Your Clinics";
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
        MenuInflater inflater = new MenuInflater(DoctorClinicsActivity.this);
        inflater.inflate(R.menu.activity_creator, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menuNew:
                Intent intent = new Intent(getApplicationContext(), ClinicCreator.class);
                startActivityForResult(intent, REQUEST_NEW_CLINIC);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_NEW_CLINIC && resultCode == RESULT_OK)  {
            /* CLEAR THE ARRAY LIST */
            arrClinics.clear();

            /* FETCH THE CLINICS THE DOCTOR PRACTICES AT */
            fetchClinics();
        } else if (requestCode == REQUEST_EDIT_CLINIC && resultCode == RESULT_OK)   {
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listClinics.setLayoutManager(manager);
        listClinics.setHasFixedSize(true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}