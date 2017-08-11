package biz.zenpets.users.details.pet;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
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
import biz.zenpets.users.creators.pet.VaccinationCreator;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.pet.VaccinationsAdapter;
import biz.zenpets.users.utils.models.pet.VaccinationsData;
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

public class PetDetails extends AppCompatActivity {

    /** THE INCOMING PET ID **/
    private String PET_ID = null;

    /** STRING TO HOLD THE PET DETAILS **/
    private String PET_NAME = null;
    private String PET_PROFILE = null;
    private String PET_GENDER = null;
    private String PET_AGE = null;
    private String BREED_NAME = null;

    /** THE VACCINATIONS ADAPTER AND ARRAY LIST **/
    private VaccinationsAdapter vaccinationsAdapter;
    private final ArrayList<VaccinationsData> arrVaccinations = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.imgvwPetProfile) CircleImageView imgvwPetProfile;
    @BindView(R.id.txtPetName) AppCompatTextView txtPetName;
    @BindView(R.id.txtPetAge) AppCompatTextView txtPetAge;
    @BindView(R.id.listVaccinations) RecyclerView listVaccinations;
    @BindView(R.id.linlaEmptyVaccinations) LinearLayout linlaEmptyVaccinations;

    /** ADD A NEW VACCINATION RECORD **/
    @OnClick(R.id.linlaEmptyVaccinations) void newVaccination()   {
        Intent intent = new Intent(PetDetails.this, VaccinationCreator.class);
        intent.putExtra("PET_ID", PET_ID);
        startActivityForResult(intent, 101);
    }

    /** ADD A NEW VACCINATION RECORD **/
    @OnClick(R.id.txtAddNew) void newTextVaccination()   {
        Intent intent = new Intent(PetDetails.this, VaccinationCreator.class);
        intent.putExtra("PET_ID", PET_ID);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_details);
        ButterKnife.bind(this);

        /* CONFIGURE THE ADAPTER */
        vaccinationsAdapter = new VaccinationsAdapter(PetDetails.this, arrVaccinations);

        /* CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* CONFIGURE THE TOOLBAR */
        configTB();
    }

    /***** FETCH THE PET DETAILS *****/
    private class fetchPetDetails extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_PET_DETAILS = "http://leodyssey.com/ZenPets/public/fetchPetDetails";
            String URL_PET_DETAILS = "http://192.168.11.2/zenpets/public/fetchPetDetails";
            HttpUrl.Builder builder = HttpUrl.parse(URL_PET_DETAILS).newBuilder();
            builder.addQueryParameter("petID", PET_ID);
            String FINAL_URL = builder.build().toString();
//            Log.e("PET DETAILS", FINAL_URL);
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

                    /* GET THE PET NAME */
                    if (JORoot.has("petName"))  {
                        PET_NAME = JORoot.getString("petName");
                    }

                    /* GET THE PET PROFILE */
                    if (JORoot.has("petProfile"))   {
                        PET_PROFILE = JORoot.getString("petProfile");
                    } else {
                        PET_PROFILE = null;
                    }

                    /* GET THE PET DETAILS */
                    if (JORoot.has("petTypeName")
                            && JORoot.has("breedName")
                            && JORoot.has("petGender")
                            && JORoot.has("petDOB"))  {
                        BREED_NAME = JORoot.getString("breedName");
                        PET_GENDER = JORoot.getString("petGender");
                        PET_AGE = calculatePetAge(JORoot.getString("petDOB"));
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

            /* SET THE PET NAME */
            txtPetName.setText(PET_NAME);

            /* SET THE PET PROFILE */
            if (PET_PROFILE != null) {
                Picasso.with(PetDetails.this)
                        .load(PET_PROFILE)
                        .centerCrop()
                        .fit()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imgvwPetProfile, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(PetDetails.this)
                                        .load(PET_PROFILE)
                                        .centerCrop()
                                        .fit()
                                        .into(imgvwPetProfile);
                            }
                        });
            } else {
                imgvwPetProfile.setImageDrawable(ContextCompat.getDrawable(PetDetails.this, R.drawable.beagle));
            }

            /* SET THE PET DETAILS */
            if (PET_GENDER != null && BREED_NAME != null && PET_AGE != null)   {
                String combinedDetails = PET_GENDER + " " + BREED_NAME + ", aged " + PET_AGE;
                txtPetAge.setText(combinedDetails);
            }

            /* FETCH THE PET'S VACCINATION RECORDS */
            new fetchVaccinationRecords().execute();
        }
    }

    /***** FETCH THE PET'S VACCINATION RECORDS *****/
    private class fetchVaccinationRecords extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_PET_VACCINATIONS = "http://leodyssey.com/ZenPets/public/petVaccinations";
            String URL_PET_VACCINATIONS = "http://192.168.11.2/zenpets/public/petVaccinations";
            HttpUrl.Builder builder = HttpUrl.parse(URL_PET_VACCINATIONS).newBuilder();
            builder.addQueryParameter("petID", PET_ID);
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
                    JSONArray JAVaccinations = JORoot.getJSONArray("vaccinations");
                    if (JAVaccinations.length() > 0)    {

                        /* AN INSTANCE OF THE VACCINATIONS DATA OBJECT */
                        VaccinationsData data;

                        for (int i = 0; i < JAVaccinations.length(); i++) {
                            JSONObject JOVaccinations = JAVaccinations.getJSONObject(i);

                            /* INSTANTIATE THE VACCINATIONS DATA INSTANCE */
                            data = new VaccinationsData();

                            /* GET THE VACCINATION ID */
                            if (JOVaccinations.has("vaccinationID"))    {
                                data.setVaccinationID(JOVaccinations.getString("vaccinationID"));
                            } else {
                                data.setVaccinationID(null);
                            }

                            /* GET THE PET ID */
                            if (JOVaccinations.has("petID"))    {
                                data.setPetID(JOVaccinations.getString("petID"));
                            } else {
                                data.setPetID(null);
                            }

                            /* GET THE VACCINE ID */
                            if (JOVaccinations.has("vaccineID"))    {
                                data.setVaccineID(JOVaccinations.getString("vaccineID"));
                            } else {
                                data.setVaccineID(null);
                            }

                            /* GET THE VACCINE NAME */
                            if (JOVaccinations.has("vaccineName"))  {
                                data.setVaccineName(JOVaccinations.getString("vaccineName"));
                            } else {
                                data.setVaccineName(null);
                            }

                            /* GET THE VACCINATION DATE */
                            if (JOVaccinations.has("vaccinationDate"))  {
                                data.setVaccinationDate(JOVaccinations.getString("vaccinationDate"));
                            } else {
                                data.setVaccinationDate(null);
                            }

                            /* GET THE VACCINATION NOTES */
                            if (JOVaccinations.has("vaccinationNotes") && !JOVaccinations.getString("vaccinationNotes").equalsIgnoreCase("null")) {
                                data.setVaccinationNotes(JOVaccinations.getString("vaccinationNotes"));
                            } else {
                                data.setVaccinationNotes(null);
                            }

//                            /* GET THE VACCINATION PICTURE */
//                            if (JOVaccinations.has("vaccinationPicture") && !JOVaccinations.getString("vaccinationPicture").equalsIgnoreCase("null")) {
//                                data.setVaccinationPicture(JOVaccinations.getString("vaccinationPicture"));
//                            } else {
//                                data.setVaccinationPicture(null);
//                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrVaccinations.add(data);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY VIEW */
                                listVaccinations.setVisibility(View.VISIBLE);
                                linlaEmptyVaccinations.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                linlaEmptyVaccinations.setVisibility(View.VISIBLE);
                                listVaccinations.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                            linlaEmptyVaccinations.setVisibility(View.VISIBLE);
                            listVaccinations.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* CONFIGURE THE ADAPTER */
            vaccinationsAdapter = new VaccinationsAdapter(PetDetails.this, arrVaccinations);

            /* SET THE ADAPTER TO THE RECYCLER VIEW */
            listVaccinations.setAdapter(vaccinationsAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("PET_ID"))   {
            PET_ID = bundle.getString("PET_ID");
            if (PET_ID != null) {
                /* FETCH THE PET DETAILS */
                new fetchPetDetails().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private String calculatePetAge(String petDOB) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            /* SET THE DATE OF BIRTH TO A CALENDAR DATE */
            Date dtDOB = format.parse(petDOB);
            Calendar calDOB = Calendar.getInstance();
            calDOB.setTime(dtDOB);
            int dobYear = calDOB.get(Calendar.YEAR);
            int dobMonth = calDOB.get(Calendar.MONTH) + 1;
            int dobDate = calDOB.get(Calendar.DATE);

            /* SET THE CURRENT DATE TO A CALENDAR INSTANCE */
            Calendar calNow = Calendar.getInstance();
            int nowYear = calNow.get(Calendar.YEAR);
            int nowMonth = calNow.get(Calendar.MONTH) + 1;
            int nowDate = calNow.get(Calendar.DATE);

            LocalDate dateDOB = new LocalDate(dobYear, dobMonth, dobDate);
            LocalDate dateNOW = new LocalDate(nowYear, nowMonth, nowDate);
            Period period = new Period(dateDOB, dateNOW, PeriodType.yearMonthDay());
//            Log.e("AGE", period.getYears() + " Years, " + period.getMonths() + " Months and " + period.getDays() + " Days");

            /* CALCULATE THE PET'S AGE */
            return period.getYears() + " Years, " + period.getMonths() + " Months and " + period.getDays() + " Days";
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listVaccinations.setLayoutManager(manager);
        listVaccinations.setHasFixedSize(true);
        listVaccinations.setNestedScrollingEnabled(false);
        listVaccinations.setAdapter(vaccinationsAdapter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 101)  {
            /* CLEAR THE ARRAY LIST */
            arrVaccinations.clear();

            /* FETCH THE LIST OF VACCINATIONS */
            new fetchVaccinationRecords().execute();
        }
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Pet Details";
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
                this.finish();
                break;
            default:
                break;
        }
        return false;
    }
}