package biz.zenpets.users.profile.adoptions;

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
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.creators.adoption.AdoptionCreator;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.adoptions.AdoptionsAdapter;
import biz.zenpets.users.utils.models.adoptions.AdoptionsData;
import biz.zenpets.users.utils.models.adoptions.AdoptionsImageData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserAdoptions extends AppCompatActivity {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** THE USER ID **/
    private String USER_ID = null;

    /** THE ADOPTION ADAPTER AND ARRAY LISTS **/
    private AdoptionsAdapter adoptionsAdapter;
    private final ArrayList<AdoptionsData> arrAdoptions = new ArrayList<>();
    private ArrayList<AdoptionsImageData> arrImages = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaProgress) LinearLayout linlaProgress;
    @BindView(R.id.listAdoptions) RecyclerView listAdoptions;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** CREATE A NEW ADOPTION LISTING **/
    @OnClick(R.id.linlaEmpty) void newAdoptionListing()    {
        Intent intent = new Intent(UserAdoptions.this, AdoptionCreator.class);
        startActivityForResult(intent, 101);
    }

    /** CREATE A NEW ADOPTION LISTING **/
    @OnClick(R.id.fabNewAdoptionListing) void fabNewAdoptionListing()  {
        Intent intent = new Intent(UserAdoptions.this, AdoptionCreator.class);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_adoptions_list);
        ButterKnife.bind(this);

        /* CONFIGURE THE TOOLBAR */
        configTB();

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();
        if (USER_ID != null) {
            /* FETCH THE USER'S ADOPTION LISTINGS */
            new fetchUserAdoptions().execute();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
        }
    }

    /***** FETCH THE LIST OF THE USER'S ADOPTION LISTINGS *****/
    private class fetchUserAdoptions extends AsyncTask<Void, Void, Void>  {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS BAR WHILE LOADING THE DATA */
            linlaProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_ADOPTIONS = "http://leodyssey.com/ZenPets/public/listUserAdoptions";
            String URL_USER_ADOPTIONS = "http://192.168.11.2/zenpets/public/listUserAdoptions";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_ADOPTIONS).newBuilder();
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
                    JSONArray JAAdoptions = JORoot.getJSONArray("adoptions");

                    if (JAAdoptions.length() > 0)   {
                        /* AN INSTANCE OF THE ADOPTIONS DATA MODEL */
                        AdoptionsData data;

                        for (int i = 0; i < JAAdoptions.length(); i++) {
                            JSONObject JOAdoptions = JAAdoptions.getJSONObject(i);
//                            Log.e("ADOPTION", String.valueOf(JOAdoptions));

                            /* INSTANTIATE THE ADOPTIONS DATA INSTANCE */
                            data = new AdoptionsData();

                            /* GET THE ADOPTION ID */
                            if (JOAdoptions.has("adoptionID"))  {
                                data.setAdoptionID(JOAdoptions.getString("adoptionID"));
                            } else {
                                data.setAdoptionID(null);
                            }

                            /* GET THE PET TYPE ID */
                            if (JOAdoptions.has("petTypeID"))   {
                                data.setPetTypeID(JOAdoptions.getString("petTypeID"));
                            } else {
                                data.setPetTypeID(null);
                            }

                            /* GET THE PET TYPE NAME */
                            if (JOAdoptions.has("petTypeName")) {
                                data.setPetTypeName(JOAdoptions.getString("petTypeName"));
                            } else {
                                data.setPetTypeName(null);
                            }

                            /* GET THE BREED ID */
                            if (JOAdoptions.has("breedID")) {
                                data.setBreedID(JOAdoptions.getString("breedID"));
                            } else {
                                data.setBreedID(null);
                            }

                            /* SET THE BREED NAME */
                            if (JOAdoptions.has("breedName"))   {
                                data.setBreedName(JOAdoptions.getString("breedName"));
                            } else {
                                data.setBreedName(null);
                            }

                            /* GET THE CITY ID */
                            if (JOAdoptions.has("cityID"))  {
                                data.setCityID(JOAdoptions.getString("cityID"));
                            } else {
                                data.setCityID(null);
                            }

                            /* GET THE CITY NAME */
                            if (JOAdoptions.has("cityName"))    {
                                data.setCityName(JOAdoptions.getString("cityName"));
                            } else {
                                data.setCityName(null);
                            }

                            /* GET THE ADOPTION NAME */
                            if (JOAdoptions.has("adoptionName") && !JOAdoptions.getString("adoptionName").equalsIgnoreCase("null")) {
                                data.setAdoptionName(JOAdoptions.getString("adoptionName"));
                            } else {
                                data.setAdoptionName(null);
                            }

                            /* GET THE ADOPTION DESCRIPTION */
                            if (JOAdoptions.has("adoptionDescription")) {
                                data.setAdoptionDescription(JOAdoptions.getString("adoptionDescription"));
                            } else {
                                data.setAdoptionDescription(null);
                            }

                            /* GET THE GENDER */
                            if (JOAdoptions.has("adoptionGender"))  {
                                data.setAdoptionGender(JOAdoptions.getString("adoptionGender"));
                            } else {
                                data.setAdoptionGender(null);
                            }

                            /* GET THE VACCINATION STATUS */
                            if (JOAdoptions.has("adoptionVaccination")) {
                                data.setAdoptionVaccination(JOAdoptions.getString("adoptionVaccination"));
                            } else {
                                data.setAdoptionVaccination(null);
                            }

                            /* GET THE DEWORMED STATUS */
                            if (JOAdoptions.has("adoptionDewormed"))    {
                                data.setAdoptionDewormed(JOAdoptions.getString("adoptionDewormed"));
                            } else {
                                data.setAdoptionDewormed(null);
                            }

                            /* GET THE NEUTERED STATUS */
                            if (JOAdoptions.has("adoptionNeutered"))    {
                                data.setAdoptionNeutered(JOAdoptions.getString("adoptionNeutered"));
                            } else {
                                data.setAdoptionNeutered(null);
                            }

                            /* GET THE TIME STAMP */
                            if (JOAdoptions.has("adoptionTimeStamp"))   {
                                String adoptionTimeStamp = JOAdoptions.getString("adoptionTimeStamp");
                                long lngTimeStamp = Long.parseLong(adoptionTimeStamp) * 1000;
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(lngTimeStamp);
                                Date date = calendar.getTime();
                                PrettyTime prettyTime = new PrettyTime();
                                String strDate = prettyTime.format(date);
                                data.setAdoptionTimeStamp(strDate);
                            } else {
                                data.setAdoptionTimeStamp(null);
                            }

                            /* GET THE ADOPTION STATUS */
                            if (JOAdoptions.has("adoptionStatus"))  {
                                data.setAdoptionStatus(JOAdoptions.getString("adoptionStatus"));
                            } else {
                                data.setAdoptionStatus(null);
                            }

                            /* GET THE ADOPTION IMAGES */
                            // String URL_ADOPTION_IMAGES = "http://leodyssey.com/ZenPets/public/fetchAdoptionImages";
                            String URL_ADOPTION_IMAGES = "http://192.168.11.2/zenpets/public/fetchAdoptionImages";
                            HttpUrl.Builder builderImages = HttpUrl.parse(URL_ADOPTION_IMAGES).newBuilder();
                            builderImages.addQueryParameter("adoptionID", JOAdoptions.getString("adoptionID"));
                            String FINAL_IMAGES_URL = builderImages.build().toString();
                            OkHttpClient clientImages = new OkHttpClient();
                            Request requestImages = new Request.Builder()
                                    .url(FINAL_IMAGES_URL)
                                    .build();
                            Call callImages = clientImages.newCall(requestImages);
                            Response responseImages = callImages.execute();
                            String strResultImages = responseImages.body().string();
                            JSONObject JORootImages = new JSONObject(strResultImages);
                            if (JORootImages.has("error") && JORootImages.getString("error").equalsIgnoreCase("false")) {
                                JSONArray JAImages = JORootImages.getJSONArray("images");
                                /* AN INSTANCE OF THE ADOPTION IMAGES DATA CLASS */
                                AdoptionsImageData imageData;

                                for (int j = 0; j < JAImages.length(); j++) {
                                    JSONObject JOImages = JAImages.getJSONObject(j);

                                    /* INSTANTIATE THE ADOPTION IMAGES DATA */
                                    imageData = new AdoptionsImageData();

                                    /* GET THE IMAGE ID */
                                    if (JOImages.has("imageID"))    {
                                        imageData.setImageID(JOImages.getString("imageID"));
                                    } else {
                                        imageData.setImageID(null);
                                    }

                                    /* GET THE ADOPTION ID */
                                    if (JOImages.has("adoptionID")) {
                                        imageData.setAdoptionID(JOImages.getString("adoptionID"));
                                    } else {
                                        imageData.setAdoptionID(null);
                                    }

                                    /* GET THE IMAGE URL */
                                    if (JOImages.has("imageURL"))   {
                                        imageData.setImageURL(JOImages.getString("imageURL"));
                                    } else {
                                        imageData.setImageURL(null);
                                    }

                                    /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                                    arrImages.add(imageData);
                                }
                                data.setImages(arrImages);
                                arrImages = new ArrayList<>();
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrAdoptions.add(data);
                        }

                        /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY LAYOUT */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listAdoptions.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listAdoptions.setVisibility(View.GONE);
                            }
                        });
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
            /* CONFIGURE THE RECYCLER VIEW **/
            configRecycler();

            /* INSTANTIATE THE ADOPTIONS ADAPTER */
            adoptionsAdapter = new AdoptionsAdapter(UserAdoptions.this, arrAdoptions);

            /* SET THE ADAPTER TO THE ADOPTIONS RECYCLER VIEW */
            listAdoptions.setAdapter(adoptionsAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaProgress.setVisibility(View.GONE);
        }
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "My Adoption Listings";
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == 101)  {

            /* CLEAR THE ARRAY LIST */
            arrAdoptions.clear();

            /* FETCH THE LIST OF USER ADOPTIONS AGAIN */
            new fetchUserAdoptions().execute();
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listAdoptions.setLayoutManager(manager);
        listAdoptions.setHasFixedSize(true);
    }
}