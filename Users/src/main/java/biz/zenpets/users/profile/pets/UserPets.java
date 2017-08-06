package biz.zenpets.users.profile.pets;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import biz.zenpets.users.creators.pet.PetCreator;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.pet.UserPetsAdapter;
import biz.zenpets.users.utils.models.pet.PetData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class UserPets extends AppCompatActivity {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** THE USER ID **/
    private String USER_ID = null;

    /** THE PETS ADAPTER AND ARRAY LIST **/
    private UserPetsAdapter petsAdapter;
    private final ArrayList<PetData> arrPets = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listPets) RecyclerView listPets;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** ADD A NEW PET **/
    @OnClick(R.id.linlaEmpty) void newPet() {
        Intent addNewPet = new Intent(this, PetCreator.class);
        startActivityForResult(addNewPet, 101);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_pets_list);
        ButterKnife.bind(this);

        /* CONFIGURE THE TOOLBAR */
        configTB();

        /* INSTANTIATE THE USER PETS ADAPTER */
        petsAdapter = new UserPetsAdapter(UserPets.this, arrPets);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();
        if (USER_ID != null)    {
            /* FETCH THE USER'S PETS */
            new fetchUserPets().execute();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /****** FETCH THE USER'S PETS *****/
    private class fetchUserPets extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
           // String URL_USER_PETS = "http://leodyssey.com/ZenPets/public/fetchUserPets";
            String URL_USER_PETS = "http://192.168.11.2/zenpets/public/fetchUserPets";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_PETS).newBuilder();
            builder.addQueryParameter("userID", USER_ID);
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
                    JSONArray JAPets = JORoot.getJSONArray("pets");

                    /* A PETS DATA INSTANCE */
                    PetData data;

                    if (JAPets.length() > 0)    {
                        for (int i = 0; i < JAPets.length(); i++) {
                            JSONObject JOPets = JAPets.getJSONObject(i);

                            /* INSTANTIATE THE PET DATA INSTANCE */
                            data = new PetData();

                            /* GET THE PET ID */
                            if (JOPets.has("petID"))    {
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

                            /* GET THE PET TYPE IF */
                            if (JOPets.has("petTypeID"))    {
                                data.setPetTypeID(JOPets.getString("petTypeID"));
                            } else {
                                data.setPetTypeID(null);
                            }

                            /* GET THE PET TYPE NAME */
                            if (JOPets.has("petTypeName"))    {
                                data.setPetTypeName(JOPets.getString("petTypeName"));
                            } else {
                                data.setPetTypeName(null);
                            }

                            /* GET THE BREED ID */
                            if (JOPets.has("breedID"))  {
                                data.setBreedID(JOPets.getString("breedID"));
                            } else {
                                data.setBreedID(null);
                            }

                            /* GET THE BREED NAME */
                            if (JOPets.has("breedName"))  {
                                data.setBreedName(JOPets.getString("breedName"));
                            } else {
                                data.setBreedName(null);
                            }

                            /* GET THE PET NAME */
                            if (JOPets.has("petName"))    {
                                data.setPetName(JOPets.getString("petName"));
                            } else {
                                data.setPetName(null);
                            }

                            /* GET THE PET GENDER */
                            if (JOPets.has("petGender"))  {
                                data.setPetGender(JOPets.getString("petGender"));
                            } else {
                                data.setPetGender(null);
                            }

                            /* GET THE PET DATE OF BIRTH */
                            if (JOPets.has("petDOB"))   {
                                String strPetDOB = JOPets.getString("petDOB");
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                try {
                                    /* SET THE DATE OF BIRTH TO A CALENDAR DATE */
                                    Date dtDOB = format.parse(strPetDOB);
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
                                    String strPetAge = period.getYears() + " Years, " + period.getMonths() + " Months and " + period.getDays() + " Days";
                                    data.setPetDOB(strPetAge);
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                data.setPetDOB(null);
                            }

                            /* GET THE PETS DISPLAY PROFILE */
                            if (JOPets.has("petProfile"))   {
                                data.setPetProfile(JOPets.getString("petProfile"));
                            } else {
                                data.setPetProfile(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrPets.add(data);
                        }

                        /* SHOW THE RECYCLER VIEW, CONFIGURE THE RECYCLER VIEW AND SET THE ADAPTER */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY VIEW */
                                listPets.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listPets.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                            linlaEmpty.setVisibility(View.VISIBLE);
                            listPets.setVisibility(View.GONE);
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

            /* INSTANTIATE THE USER PETS ADAPTER */
            petsAdapter = new UserPetsAdapter(UserPets.this, arrPets);

            /* SET THE ADAPTER TO THE RECYCLER VIEW */
            listPets.setAdapter(petsAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "My Pets";
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
        MenuInflater inflater = new MenuInflater(UserPets.this);
        inflater.inflate(R.menu.activity_common_creator, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.menuNew:
                Intent addNewPet = new Intent(this, PetCreator.class);
                startActivityForResult(addNewPet, 101);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 101)  {
            /* CLEAR THE ARRAY */
            arrPets.clear();

            /* FETCH THE LIST OF PETS AGAIN */
            new fetchUserPets().execute();
        }
    }

    /***** CONFIGURE THE RECYCLER VIEW *****/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listPets.setLayoutManager(manager);
        listPets.setHasFixedSize(true);
        listPets.setAdapter(petsAdapter);
    }
}