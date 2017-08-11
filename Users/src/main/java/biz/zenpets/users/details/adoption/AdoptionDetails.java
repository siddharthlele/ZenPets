package biz.zenpets.users.details.adoption;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.adoptions.AdoptionsImagesAdapter;
import biz.zenpets.users.utils.models.adoptions.AdoptionsImageData;
import biz.zenpets.users.utils.models.adoptions.messages.AdoptionMessagesData;
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

public class AdoptionDetails extends AppCompatActivity {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** THE INCOMING ADOPTION ID **/
    private String ADOPTION_ID = null;

    /** THE LOGGED IN / POSTER'S USER ID **/
    private String POSTER_ID = null;

    /** THE ADOPTION DETAILS DATA **/
    private String PET_TYPE_ID = null;
    private String PET_TYPE_NAME = null;
    private String BREED_ID = null;
    private String BREED_NAME = null;
    private String USER_ID = null;
    private String USER_NAME = null;
    private String CITY_ID = null;
    private String CITY_NAME = null;
    private String ADOPTION_NAME = null;
    private String ADOPTION_DESCRIPTION = null;
    private String ADOPTION_GENDER = null;
    private String ADOPTION_VACCINATION = null;
    private String ADOPTION_DEWORMED = null;
    private String ADOPTION_NEUTERED = null;
    private String ADOPTION_TIMESTAMP = null;
    private String ADOPTION_STATUS = null;

    /** THE ADOPTION MESSAGE **/
    private String ADOPTION_MESSAGE = null;

    /** THE ADOPTION IMAGES ADAPTER AND ARRAY LIST **/
    private AdoptionsImagesAdapter adapter;
    private final ArrayList<AdoptionsImageData> arrImages = new ArrayList<>();

    /** THE ADOPTION MESSAGES ADAPTER AND ARRAY LIST **/
    private AdoptionMessagesAdapter messagesAdapter;
    private final ArrayList<AdoptionMessagesData> arrMessages = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.txtAdoptionName) AppCompatTextView txtAdoptionName;
    @BindView(R.id.txtGender) AppCompatTextView txtGender;
    @BindView(R.id.txtAdoptionDescription) AppCompatTextView txtAdoptionDescription;
    @BindView(R.id.txtNoImages) AppCompatTextView txtNoImages;
    @BindView(R.id.listAdoptionImages) RecyclerView listAdoptionImages;
    @BindView(R.id.txtTimeStamp) AppCompatTextView txtTimeStamp;
    @BindView(R.id.txtVaccinated) AppCompatTextView txtVaccinated;
    @BindView(R.id.txtDewormed) AppCompatTextView txtDewormed;
    @BindView(R.id.txtNeutered) AppCompatTextView txtNeutered;
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listMessages) RecyclerView listMessages;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;
    @BindView(R.id.edtComment) AppCompatEditText edtComment;

    /** PUBLISH A NEW MESSAGE **/
    @OnClick(R.id.imgbtnComment) void newMessage()  {
        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtComment.getWindowToken(), 0);

        /* COLLECT THE ADOPTION MESSAGE */
        ADOPTION_MESSAGE = edtComment.getText().toString().trim();

        /* VALIDATE THE MESSAGE IS NOT NULL */
        if (TextUtils.isEmpty(ADOPTION_MESSAGE)) {
            edtComment.setError("Please type a message....");
            edtComment.requestFocus();
        } else  {
            /* PUBLISH THE ADOPTION MESSAGE */
            postMessage();
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adoption_details);
        ButterKnife.bind(this);

        /* GET THE LOGGED IN / POSTER'S USER ID */
        POSTER_ID = getApp().getUserID();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* INSTANTIATE THE ADOPTION IMAGES ADAPTER */
        adapter = new AdoptionsImagesAdapter(AdoptionDetails.this, arrImages);

        /* INSTANTIATE THE ADOPTION MESSAGES ADAPTER */
        messagesAdapter = new AdoptionMessagesAdapter(AdoptionDetails.this, arrMessages);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* CONFIGURE THE TOOLBAR */
        configTB();
    }

    /***** FETCH THE ADOPTION LISTING DETAILS *****/
    private class getAdoptionDetails extends AsyncTask<Void, Void, Void>    {

        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {

            /* SHOW THE PROGRESS DIALOG WHILE FETCHING THE ADOPTION DETAILS **/
            dialog = new ProgressDialog(AdoptionDetails.this);
            dialog.setMessage("Please wait while fetch the details....");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_ADOPTIONS = "http://leodyssey.com/ZenPets/public/fetchAdoptionDetails";
            String URL_USER_ADOPTIONS = "http://192.168.11.2/zenpets/public/fetchAdoptionDetails";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_ADOPTIONS).newBuilder();
            builder.addQueryParameter("adoptionID", ADOPTION_ID);
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
//                    Log.e("ROOT", String.valueOf(JORoot));

                    /* GET THE PET TYPE ID */
                    if (JORoot.has("petTypeID"))   {
                        PET_TYPE_ID = JORoot.getString("petTypeID");
                    } else {
                        PET_TYPE_ID = null;
                    }

                    /* GET THE PET TYPE NAME */
                    if (JORoot.has("petTypeName")) {
                        PET_TYPE_NAME = JORoot.getString("petTypeName");
                    } else {
                        PET_TYPE_NAME = null;
                    }

                    /* GET THE BREED ID */
                    if (JORoot.has("breedID")) {
                        BREED_ID = JORoot.getString("breedID");
                    } else {
                        BREED_ID = null;
                    }

                    /* SET THE BREED NAME */
                    if (JORoot.has("breedName"))   {
                        BREED_NAME = JORoot.getString("breedName");
                    } else {
                        BREED_NAME = null;
                    }

                    /* SET THE USER ID */
                    if (JORoot.has("userID"))  {
                        USER_ID = JORoot.getString("userID");
                    } else {
                        USER_ID = null;
                    }

                    /* SET THE USER NAME */
                    if (JORoot.has("userName")) {
                        USER_NAME = JORoot.getString("userName");
                    } else {
                        USER_NAME = null;
                    }

                    /* GET THE CITY ID */
                    if (JORoot.has("cityID"))  {
                        CITY_ID = JORoot.getString("cityID");
                    } else {
                        CITY_ID = null;
                    }

                    /* GET THE CITY NAME */
                    if (JORoot.has("cityName"))    {
                        CITY_NAME = JORoot.getString("cityName");
                    } else {
                        CITY_NAME = null;
                    }

                    /* GET THE ADOPTION NAME */
                    if (JORoot.has("adoptionName") && !JORoot.getString("adoptionName").equalsIgnoreCase("null")) {
                        ADOPTION_NAME = JORoot.getString("adoptionName");
                    } else {
                        ADOPTION_NAME = null;
                    }

                    /* GET THE ADOPTION DESCRIPTION */
                    if (JORoot.has("adoptionDescription")) {
                        ADOPTION_DESCRIPTION = JORoot.getString("adoptionDescription");
                    } else {
                        ADOPTION_DESCRIPTION = null;
                    }

                    /* GET THE GENDER */
                    if (JORoot.has("adoptionGender"))  {
                        ADOPTION_GENDER = JORoot.getString("adoptionGender");
                    } else {
                        ADOPTION_GENDER = null;
                    }

                    /* GET THE VACCINATION STATUS */
                    if (JORoot.has("adoptionVaccination")) {
                        ADOPTION_VACCINATION = JORoot.getString("adoptionVaccination");
                    } else {
                        ADOPTION_VACCINATION = null;
                    }

                    /* GET THE DEWORMED STATUS */
                    if (JORoot.has("adoptionDewormed"))    {
                        ADOPTION_DEWORMED = JORoot.getString("adoptionDewormed");
                    } else {
                        ADOPTION_DEWORMED = null;
                    }

                    /* GET THE NEUTERED STATUS */
                    if (JORoot.has("adoptionNeutered"))    {
                        ADOPTION_NEUTERED = JORoot.getString("adoptionNeutered");
                    } else {
                        ADOPTION_NEUTERED = null;
                    }

                    /* GET THE TIME STAMP */
                    if (JORoot.has("adoptionTimeStamp"))   {
                        String adoptionTimeStamp = JORoot.getString("adoptionTimeStamp");
                        long lngTimeStamp = Long.parseLong(adoptionTimeStamp) * 1000;
                        Calendar calendar = Calendar.getInstance(Locale.getDefault());
                        calendar.setTimeInMillis(lngTimeStamp);
                        Date date = calendar.getTime();
                        PrettyTime prettyTime = new PrettyTime();
                        ADOPTION_TIMESTAMP = prettyTime.format(date);
                    } else {
                        ADOPTION_TIMESTAMP = null;
                    }

                    /* GET THE ADOPTION STATUS */
                    if (JORoot.has("adoptionStatus"))  {
                        ADOPTION_STATUS = JORoot.getString("adoptionStatus");
                    } else {
                        ADOPTION_STATUS = null;
                    }

                    /** GET THE ADOPTION IMAGES **/
                    // String URL_ADOPTION_IMAGES = "http://leodyssey.com/ZenPets/public/fetchAdoptionImages";
                    String URL_ADOPTION_IMAGES = "http://192.168.11.2/zenpets/public/fetchAdoptionImages";
                    HttpUrl.Builder builderImages = HttpUrl.parse(URL_ADOPTION_IMAGES).newBuilder();
                    builderImages.addQueryParameter("adoptionID", JORoot.getString("adoptionID"));
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

            /* SET THE ADOPTION NAME */
            if (ADOPTION_NAME != null)  {
                txtAdoptionName.setText(ADOPTION_NAME);
            } else {
                txtAdoptionName.setText("Unnamed");
            }

            /* SET THE DESCRIPTION */
            if (ADOPTION_DESCRIPTION != null)   {
                txtAdoptionDescription.setText(ADOPTION_DESCRIPTION);
            }

            /* SET THE TIMESTAMP (DATE OF CREATION )*/
            if (ADOPTION_TIMESTAMP != null)    {
                txtTimeStamp.setText("Posted " + ADOPTION_TIMESTAMP);
            }

            /* SET THE PET'S GENDER */
            if (ADOPTION_GENDER != null)    {
                if (ADOPTION_GENDER.equalsIgnoreCase("male"))  {
                    txtGender.setText(getResources().getString(R.string.gender_male));
                    txtGender.setTextColor(ContextCompat.getColor(AdoptionDetails.this, android.R.color.holo_blue_dark));
                } else if (ADOPTION_GENDER.equalsIgnoreCase("female")) {
                    txtGender.setText(AdoptionDetails.this.getResources().getString(R.string.gender_female));
                    txtGender.setTextColor(ContextCompat.getColor(AdoptionDetails.this, android.R.color.holo_red_dark));
                }
            }

            /* SET THE VACCINATED STATUS */
            if (ADOPTION_VACCINATION != null)  {
                txtVaccinated.setText(ADOPTION_VACCINATION);
                if (ADOPTION_VACCINATION.equalsIgnoreCase("yes"))  {
                    txtVaccinated.setTextColor(ContextCompat.getColor(AdoptionDetails.this, android.R.color.holo_green_dark));
                } else if (ADOPTION_VACCINATION.equalsIgnoreCase("no"))    {
                    txtVaccinated.setTextColor(ContextCompat.getColor(AdoptionDetails.this, android.R.color.holo_red_dark));
                }
            }

            /* SET THE DEWORMED STATUS */
            if (ADOPTION_DEWORMED != null) {
                txtDewormed.setText(ADOPTION_DEWORMED);
                if (ADOPTION_DEWORMED.equalsIgnoreCase("yes")) {
                    txtDewormed.setTextColor(ContextCompat.getColor(AdoptionDetails.this, android.R.color.holo_green_dark));
                } else if (ADOPTION_DEWORMED.equalsIgnoreCase("no"))   {
                    txtDewormed.setTextColor(ContextCompat.getColor(AdoptionDetails.this, android.R.color.holo_red_dark));
                }
            }

            /* SET THE NEUTERED STATUS */
            if (ADOPTION_NEUTERED != null) {
                txtNeutered.setText(ADOPTION_NEUTERED);
                if (ADOPTION_NEUTERED.equalsIgnoreCase("yes")) {
                    txtNeutered.setTextColor(ContextCompat.getColor(AdoptionDetails.this, android.R.color.holo_green_dark));
                } else if (ADOPTION_NEUTERED.equalsIgnoreCase("no"))   {
                    txtNeutered.setTextColor(ContextCompat.getColor(AdoptionDetails.this, android.R.color.holo_red_dark));
                }
            }

            /* IF ADOPTION IMAGES ARE AVAILABLE, DISPLAY THE RECYCLER VIEW */
            if (arrImages.size() > 0)   {
                /* INSTANTIATE THE ADOPTION IMAGES ADAPTER */
                adapter = new AdoptionsImagesAdapter(AdoptionDetails.this, arrImages);

                /* SET THE ADAPTER TO THE RECYCLER VIEW */
                listAdoptionImages.setAdapter(adapter);

                /* SHOW THE RECYCLER VIEW */
                listAdoptionImages.setVisibility(View.VISIBLE);
                txtNoImages.setVisibility(View.GONE);
            } else {
                /* HIDE THE RECYCLER VIEW */
                listAdoptionImages.setVisibility(View.GONE);
                txtNoImages.setVisibility(View.GONE);
            }

            /* DISMISS THE DIALOG AFTER FETCHING THE ADOPTION DETAILS */
            dialog.dismiss();

            /** FETCH THE ADOPTION MESSAGES **/
            new fetchAdoptionMessages().execute();
        }
    }

    /***** FETCH THE LIST OF MESSAGES ON THE ADOPTION LISTING *****/
    private class fetchAdoptionMessages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE MESSAGES */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_ADOPTIONS = "http://leodyssey.com/ZenPets/public/fetchAdoptionDetails";
            String URL_USER_ADOPTIONS = "http://192.168.11.2/zenpets/public/fetchAdoptionMessages";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_ADOPTIONS).newBuilder();
            builder.addQueryParameter("adoptionID", ADOPTION_ID);
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
                    JSONArray JAMessages = JORoot.getJSONArray("messages");
                    if (JAMessages.length() > 0) {
                        AdoptionMessagesData data;
                        for (int i = 0; i < JAMessages.length(); i++) {
                            JSONObject JOMessages = JAMessages.getJSONObject(i);
//                            Log.e("MESSAGE", String.valueOf(JOMessages));

                            /* INSTANTIATE THE ADOPTION MESSAGE DATA OBJECT */
                            data = new AdoptionMessagesData();

                            /* GET THE MESSAGE ID */
                            if (JOMessages.has("messageID"))    {
                                data.setMessageID(JOMessages.getString("messageID"));
                            } else {
                                data.setMessageID(null);
                            }

                            /* GET THE USER ID */
                            if (JOMessages.has("userID"))   {
                                data.setUserID(JOMessages.getString("userID"));
                                if (JOMessages.getString("userID").equalsIgnoreCase(USER_ID))   {
                                    data.setUserPoster(true);
                                } else {
                                    data.setUserPoster(false);
                                }
                            } else {
                                data.setUserID(null);
                                data.setUserPoster(false);
                            }

                            /* GET THE MESSAGE TEXT */
                            if (JOMessages.has("messageText"))  {
                                data.setMessageText(JOMessages.getString("messageText"));
                            } else {
                                data.setMessageText(null);
                            }

                            if (JOMessages.has("messageTimeStamp")) {
                                String messageTimeStamp = JOMessages.getString("messageTimeStamp");
                                long lngTimeStamp = Long.parseLong(messageTimeStamp) * 1000;
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(lngTimeStamp);
                                Date date = calendar.getTime();
                                PrettyTime prettyTime = new PrettyTime();
                                data.setMessageTimeStamp(prettyTime.format(date));
                            } else {
                                data.setMessageTimeStamp(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrMessages.add(data);
                        }
                        /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY MESSAGE VIEW */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listMessages.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* SHOW THE EMPTY MESSAGES VIEW AND HIDE THE RECYCLER VIEW */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listMessages.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    /* SHOW THE EMPTY MESSAGES VIEW AND HIDE THE RECYCLER VIEW */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linlaEmpty.setVisibility(View.VISIBLE);
                            listMessages.setVisibility(View.GONE);
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

            /* INSTANTIATE THE ADOPTION MESSAGES ADAPTER */
            messagesAdapter = new AdoptionMessagesAdapter(AdoptionDetails.this, arrMessages);

            /* SET THE ADAPTER TO THE RECYCLER VIEW */
            listMessages.setAdapter(messagesAdapter);

            /* HIDE THE PROGRESS AFTER FETCHING THE MESSAGES */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** PUBLISH THE ADOPTION MESSAGE *****/
    private void postMessage() {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage("Publishing your message....");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        // String URL_NEW_ADOPTION_MESSAGE = "http://leodyssey.com/ZenPets/public/newAdoptionMessage";
        String URL_NEW_ADOPTION_MESSAGE = "http://192.168.11.2/zenpets/public/newAdoptionMessage";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("adoptionID", ADOPTION_ID)
                .add("userID", POSTER_ID)
                .add("messageText", ADOPTION_MESSAGE)
                .add("messageTimeStamp", String.valueOf(System.currentTimeMillis() / 1000))
                .build();
        Request request = new Request.Builder()
                .url(URL_NEW_ADOPTION_MESSAGE)
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
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* DISMISS THE DIALOG */
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Message successfully posted", Toast.LENGTH_SHORT).show();
                                arrMessages.clear();
                                new fetchAdoptionMessages().execute();
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* DISMISS THE DIALOG */
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "There was a problem posting your message....", Toast.LENGTH_SHORT).show();
                            }
                        });
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
        if (bundle.containsKey("ADOPTION_ID")) {
            ADOPTION_ID = bundle.getString("ADOPTION_ID");
            if (ADOPTION_ID != null) {
                /* GET THE ADOPTION DETAILS */
                new getAdoptionDetails().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void configRecycler() {
        /* CONFIGURE THE ADOPTION IMAGES RECYCLER VIEW */
        LinearLayoutManager llmAdoptions = new LinearLayoutManager(this);
        llmAdoptions.setOrientation(LinearLayoutManager.HORIZONTAL);
        llmAdoptions.setAutoMeasureEnabled(true);
        listAdoptionImages.setLayoutManager(llmAdoptions);
        listAdoptionImages.setHasFixedSize(true);
        listAdoptionImages.setAdapter(adapter);

        /* CONFIGURE THE MESSAGES RECYCLER VIEW */
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        manager.setAutoMeasureEnabled(true);
        listMessages.setLayoutManager(manager);
        listMessages.setHasFixedSize(true);
        listMessages.setNestedScrollingEnabled(false);
        listMessages.setAdapter(messagesAdapter);
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Adoption Details";
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /***** THE ADOPTION MESSAGES ADAPTER *****/
    private class AdoptionMessagesAdapter extends RecyclerView.Adapter<AdoptionMessagesAdapter.MessagesVH> {

        /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
        private final Activity activity;

        /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
        private final ArrayList<AdoptionMessagesData> arrMessages;

        AdoptionMessagesAdapter(Activity activity, ArrayList<AdoptionMessagesData> arrMessages) {

            /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
            this.activity = activity;

            /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
            this.arrMessages = arrMessages;
        }

        @Override
        public int getItemCount() {
            return arrMessages.size();
        }

        @Override
        public void onBindViewHolder(MessagesVH holder, final int position) {
            AdoptionMessagesData data = arrMessages.get(position);

            if (data.isUserPoster())    {
                /* SHOW THE OUTGOING MESSAGE CONTAINER AND HIDE THE INCOMING MESSAGE CONTAINER */
                holder.linlaOutgoing.setVisibility(View.VISIBLE);
                holder.linlaIncoming.setVisibility(View.GONE);

                /* SET THE MESSAGE TEXT */
                if (data.getMessageText() != null)  {
                    holder.txtOutgoingMessage.setText(data.getMessageText());
                }

                /* SET THE TIME STAMP */
                if (data.getMessageTimeStamp() != null) {
                    holder.txtOutgoingTimeStamp.setText(data.getMessageTimeStamp());
                }

            } else {
                /* SHOW THE INCOMING MESSAGE CONTAINER AND HIDE THE OUTGOING MESSAGE CONTAINER */
                holder.linlaIncoming.setVisibility(View.VISIBLE);
                holder.linlaOutgoing.setVisibility(View.GONE);

                /* SET THE MESSAGE TEXT */
                if (data.getMessageText() != null)  {
                    holder.txtIncomingMessage.setText(data.getMessageText());
                }

                /* SET THE TIME STAMP */
                if (data.getMessageTimeStamp() != null) {
                    holder.txtIncomingTimeStamp.setText(data.getMessageTimeStamp());
                }
            }
        }

        @Override
        public MessagesVH onCreateViewHolder(ViewGroup parent, int i) {

            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.adoption_message_item, parent, false);

            return new MessagesVH(itemView);
        }

        class MessagesVH extends RecyclerView.ViewHolder	{

            final LinearLayout linlaIncoming;
            final AppCompatTextView txtIncomingMessage;
            final AppCompatTextView txtIncomingTimeStamp;
            final LinearLayout linlaOutgoing;
            final AppCompatTextView txtOutgoingMessage;
            final AppCompatTextView txtOutgoingTimeStamp;

            MessagesVH(View v) {
                super(v);

                linlaIncoming = (LinearLayout) v.findViewById(R.id.linlaIncoming);
                txtIncomingMessage = (AppCompatTextView) v.findViewById(R.id.txtIncomingMessage);
                txtIncomingTimeStamp = (AppCompatTextView) v.findViewById(R.id.txtIncomingTimeStamp);
                linlaOutgoing = (LinearLayout) v.findViewById(R.id.linlaOutgoing);
                txtOutgoingMessage = (AppCompatTextView) v.findViewById(R.id.txtOutgoingMessage);
                txtOutgoingTimeStamp = (AppCompatTextView) v.findViewById(R.id.txtOutgoingTimeStamp);
            }
        }
    }
}