package biz.zenpets.users.modifiers.profile;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.location.CitiesAdapter;
import biz.zenpets.users.utils.adapters.location.CountriesAdapter;
import biz.zenpets.users.utils.adapters.location.StatesAdapter;
import biz.zenpets.users.utils.models.location.CitiesData;
import biz.zenpets.users.utils.models.location.CountriesData;
import biz.zenpets.users.utils.models.location.StatesData;
import butterknife.BindView;
import butterknife.ButterKnife;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ProfileEditor extends AppCompatActivity {

    /** STRINGS TO HOLD THE USER DETAILS **/
    private String USER_ID = null;
    private String USER_AUTH_ID = null;
    private String USER_DISPLAY_NAME = null;
    private String USER_EMAIL = null;
    private String USER_PROFILE = null;
    private String USER_PHONE_PREFIX = null;
    private String USER_PHONE_NUMBER = null;
    private String USER_GENDER = "Male";
    private String USER_COUNTRY_ID = null;
    private String USER_STATE_ID = null;
    private String USER_CITY_ID = null;
    private String USER_PROFILE_STATUS = null;

    /** THE SELECTED COUNTRY, STATE AND CITY ID **/
    private String COUNTRY_ID = null;
    private String STATE_ID = null;
    private String CITY_ID = null;

    /** THE COUNTRY ADAPTER AND ARRAYLIST **/
    private CountriesAdapter countriesAdapter;
    private final ArrayList<CountriesData> arrCountries = new ArrayList<>();

    /** THE STATES ADAPTER AND ARRAYLIST **/
    private StatesAdapter statesAdapter;
    private final ArrayList<StatesData> arrStates = new ArrayList<>();

    /** CITIES ADAPTER AND ARRAY LIST **/
    private CitiesAdapter citiesAdapter;
    private final ArrayList<CitiesData> arrCities = new ArrayList<>();

    /** AN IMAGE LOADER INSTANCE **/
    private ImageLoader imageLoader;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.imgvwProfilePicture) AppCompatImageView imgvwProfilePicture;
    @BindView(R.id.edtDisplayName) AppCompatEditText edtDisplayName;
    @BindView(R.id.txtEmailAddress) AppCompatTextView txtEmailAddress;
    @BindView(R.id.edtPhonePrefix) AppCompatEditText edtPhonePrefix;
    @BindView(R.id.edtPhoneNumber) AppCompatEditText edtPhoneNumber;
    @BindView(R.id.spnCountry) AppCompatSpinner spnCountry;
    @BindView(R.id.spnState) AppCompatSpinner spnState;
    @BindView(R.id.spnCity) AppCompatSpinner spnCity;
    @BindView(R.id.groupGender) SegmentedButtonGroup groupGender;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_editor);
        ButterKnife.bind(this);

        /* INSTANTIATE THE IMAGE LOADER INSTANCE */
        imageLoader = ImageLoader.getInstance();

        /* CONFIGURE THE TOOLBAR */
        configTB();

        /* GET THE FIREBASE USER INSTANCE */
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null)   {
            /* GET THE USER'S AUTH ID */
            USER_AUTH_ID = user.getUid();

            /* FETCH THE USER DETAILS */
            new fetchUserDetails().execute();
        }

        /* FETCH THE LIST OF COUNTRIES */
        new fetchCountries().execute();

        /* SELECT THE USER'S GENDER */
        groupGender.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0)  {
                    USER_GENDER = "Male";
//                    Log.e("GENDER", USER_GENDER);
                } else if (position == 1)   {
                    USER_GENDER = "Female";
//                    Log.e("GENDER", USER_GENDER);
                }
            }
        });

        /* SET THE USER'S GENDER */
        if (USER_GENDER != null)    {
            if (USER_GENDER.equalsIgnoreCase("Male"))   {
                groupGender.setPosition(0, true);
            } else {
                groupGender.setPosition(1, true);
            }
        } else {
            groupGender.setPosition(0, true);
        }

        /* SELECT A COUNTRY */
        spnCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                COUNTRY_ID = arrCountries.get(position).getCountryID();

                /* CLEAR THE STATES ARRAY LIST */
                arrStates.clear();

                /* FETCH THE LIST OF STATES */
                if (COUNTRY_ID != null) {
                    new fetchStates().execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* SELECT A STATE */
        spnState.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                STATE_ID = arrStates.get(position).getStateID();

                /* CLEAR THE CITIES ARRAY LIST */
                arrCities.clear();

                /* FETCH THE LIST OF CITIES */
                if (STATE_ID != null)   {
                    new fetchCities().execute();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* SELECT A CITY */
        spnCity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CITY_ID = arrCities.get(position).getCityID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /***** FETCH THE USER DETAILS *****/
    private class fetchUserDetails extends AsyncTask<Void, Void, Void> {

        /** BOOLEAN TO TRACK ERRORS **/
        boolean blnError = false;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_DETAILS = "http://leodyssey.com/ZenPets/public/fetchProfile";
            String URL_USER_DETAILS = "http://192.168.11.2/zenpets/public/fetchProfile";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DETAILS).newBuilder();
            builder.addQueryParameter("userAuthID", USER_AUTH_ID);
            String FINAL_URL = builder.build().toString();
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(FINAL_URL)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
//                Log.e("JORoot", String.valueOf(JORoot));

                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false"))  {
                    /* TOGGLE THE ERROR BOOLEAN */
                    blnError = false;

                    /* GET THE USER ID */
                    if (JORoot.has("userID"))   {
                        USER_ID = JORoot.getString("userID");
                    }

                    /* GET THE USER EMAIL ADDRESS */
                    if (JORoot.has("userEmail"))   {
                        USER_EMAIL = JORoot.getString("userEmail");
                    }

                    /* GET THE USER AUTH ID */
                    if (JORoot.has("userAuthID"))   {
                        USER_AUTH_ID = JORoot.getString("userAuthID");
                    }

                    /* GET THE USER DISPLAY NAME */
                    if (JORoot.has("userName")) {
                        USER_DISPLAY_NAME = JORoot.getString("userName");
                    }

                    /* GET THE USER'S PROFILE */
                    if (JORoot.has("userDisplayProfile"))   {
                        USER_PROFILE = JORoot.getString("userDisplayProfile");
                    }

                    /* GET THE USER'S PREFIX AND PHONE NUMBER */
                    if (JORoot.has("userPhonePrefix") && JORoot.has("userPhoneNumber"))   {
                        USER_PHONE_PREFIX = JORoot.getString("userPhonePrefix");
                        USER_PHONE_NUMBER = JORoot.getString("userPhoneNumber");
                    }

                    /* GET THE USER'S GENDER */
                    if (JORoot.has("userGender"))   {
                        USER_GENDER = JORoot.getString("userGender");
                    } else {
                        USER_GENDER = "Male";
                    }

                    /* GET THE USER'S COUNTRY */
                    if (JORoot.has("countryID"))    {
                        USER_COUNTRY_ID = JORoot.getString("countryID");
                    }

                    /* GET THE USER'S STATE */
                    if (JORoot.has("stateID"))  {
                        USER_STATE_ID = JORoot.getString("stateID");
                    }

                    /* GET THE USER'S CITY */
                    if (JORoot.has("cityID"))   {
                        USER_CITY_ID = JORoot.getString("cityID");
                    }

                    /* GET THE PROFILE STATUS */
                    if (JORoot.has("profileStatus"))    {
                        USER_PROFILE_STATUS = JORoot.getString("profileStatus");
                    }
                } else {
                    /* TOGGLE THE ERROR BOOLEAN */
                    blnError = true;
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* CHECK IF AN ERROR OCCURRED OR DISPLAY THE USER DETAILS */
            if (!blnError)   {
                /* SET THE USER NAME */
                if (USER_DISPLAY_NAME != null)  {
                    edtDisplayName.setText(USER_DISPLAY_NAME);
                }

                /* SET THE PROFILE PICTURE */
                if (USER_PROFILE != null)   {
                    Glide.with(ProfileEditor.this)
                            .load(USER_PROFILE)
                            .crossFade()
                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                            .centerCrop()
                            .into(imgvwProfilePicture);
                }

                /* SET THE EMAIL ADDRESS */
                if (USER_EMAIL != null) {
                    txtEmailAddress.setText(USER_EMAIL);
                }

                /* SET THE PREFIX */
                if (USER_PHONE_PREFIX != null)  {
                    if (!USER_PHONE_PREFIX.equals("null"))   {
                        edtPhonePrefix.setText(USER_PHONE_PREFIX);
                    }
                }

                /* SET THE PHONE NUMBER */
                if ((USER_PHONE_NUMBER != null))    {
                    if (!USER_PHONE_NUMBER.equals("null"))   {
                        edtPhoneNumber.setText(USER_PHONE_NUMBER);
                    }
                }

//                /* SET THE USER'S GENDER */
//                if (USER_GENDER != null)    {
//                    if (USER_GENDER.equalsIgnoreCase("Male"))   {
//                        groupGender.setPosition(0, true);
//                    } else {
//                        groupGender.setPosition(1, true);
//                    }
//                } else {
//                    groupGender.setPosition(0, true);
//                }

                /* SET THE USER'S COUNTRY */
                if (USER_COUNTRY_ID != null && !USER_COUNTRY_ID.equalsIgnoreCase("0"))  {
//                    Log.e("COUNTRY ID", USER_COUNTRY_ID);
                    int countryPosition = getCountryIndex(arrCountries, USER_COUNTRY_ID);
//                    Log.e("COUNTRY POSITION", String.valueOf(countryPosition));
                    spnCountry.setSelection(countryPosition);
                }

                /* SET THE USER'S STATE */
                if (USER_STATE_ID != null && !USER_STATE_ID.equalsIgnoreCase("0"))  {
//                    Log.e("STATE ID", USER_STATE_ID);
                    int statePosition = getStateIndex(arrStates, USER_STATE_ID);
//                    Log.e("STATE POSITION", String.valueOf(statePosition));
                    spnState.setSelection(statePosition);
                }

                /* SET THE USER'S CITY */
                if (USER_CITY_ID != null && !USER_CITY_ID.equalsIgnoreCase("0"))    {
//                    Log.e("CITY ID", USER_CITY_ID);
                    int cityPosition = getCityIndex(arrCities, USER_CITY_ID);
//                    Log.e("CITY POSITION", String.valueOf(cityPosition));
                    spnCity.setSelection(cityPosition);
                }
            }
        }
    }

    /** GET THE USER'S COUNTRY POSITION **/
    private int getCountryIndex(ArrayList<CountriesData> arrCountries, String userCountry) {
        int index = 0;
        for (int i =0; i < arrCountries.size(); i++) {
            if (arrCountries.get(i).getCountryID().equalsIgnoreCase(userCountry))   {
                index = i;
                break;
            }
        }
        return index;
    }

    /** GET THE USER'S STATE POSITION **/
    private int getStateIndex(ArrayList<StatesData> arrStates, String userState) {
        int index = 0;
        for (int i =0; i < arrStates.size(); i++) {
            if (arrStates.get(i).getStateID().equalsIgnoreCase(userState))   {
                index = i;
                break;
            }
        }
        return index;
    }

    /** GET THE USER'S CITY POSITION **/
    private int getCityIndex(ArrayList<CitiesData> arrCities, String userCity) {
        int index = 0;
        for (int i =0; i < arrCities.size(); i++) {
            if (arrCities.get(i).getCityID().equalsIgnoreCase(userCity))   {
                index = i;
                break;
            }
        }
        return index;
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Profile";
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
        MenuInflater inflater = new MenuInflater(ProfileEditor.this);
        inflater.inflate(R.menu.activity_profile_editor, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.menuSave:
                /* UPDATE THE USER DETAILS */
                updateUserDetails();
                break;
            default:
                break;
        }
        return false;
    }

    /** UPDATE THE USER DETAILS **/
    private void updateUserDetails() {

        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtDisplayName.getWindowToken(), 0);

        /* COLLECT THE INFORMATION */
        USER_PHONE_PREFIX = edtPhonePrefix.getText().toString().trim();
        USER_PHONE_NUMBER = edtPhoneNumber.getText().toString().trim();

        /* SET THE USER PROFILE STATUS TO "COMPLETE" */
        USER_PROFILE_STATUS = "Complete";
//        Log.e("GENDER", USER_GENDER);

        /* VALIDATE THE PHONE NUMBER */
        if (TextUtils.isEmpty(USER_PHONE_PREFIX))   {
            edtPhonePrefix.setError("Please provide your Country Code");
        } else if (TextUtils.isEmpty(USER_PHONE_NUMBER))   {
            edtPhoneNumber.setError("Please provide your Mobile Number");
        } else {
            /* UPDATE THE USER PROFILE */
            new updateUserProfile().execute();
        }
    }

    /** UPDATE THE USER PROFILE **/
    private class updateUserProfile extends AsyncTask<Void, Void, Void> {

        /** BOOLEAN TO TRACK USER RECORD CREATION **/
        boolean blnSuccess = false;

        /** STRING TO HOLD THE ERROR MESSAGE, IF ANY **/
        private String strErrorMessage = null;

        /** A PROGRESS DIALOG INSTANCE **/
        ProgressDialog progressDialog = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
            progressDialog = new ProgressDialog(ProfileEditor.this);
            progressDialog.setMessage("Please wait while we update your Profile....");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_UPDATE_PROFILE = "http://leodyssey.com/ZenPets/public/updateProfile";
            String URL_UPDATE_PROFILE = "http://192.168.11.2/zenpets/public/updateProfile";
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("userEmail", USER_EMAIL)
                    .add("userPhonePrefix", USER_PHONE_PREFIX)
                    .add("userPhoneNumber", USER_PHONE_NUMBER)
                    .add("userGender", USER_GENDER)
                    .add("countryID", COUNTRY_ID)
                    .add("stateID", STATE_ID)
                    .add("cityID", CITY_ID)
                    .add("profileComplete", USER_PROFILE_STATUS)
                    .build();
            Request request = new Request.Builder()
                    .url(URL_UPDATE_PROFILE)
                    .post(body)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
//                Log.e("RESULT", strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    /* TOGGLE THE BOOLEAN FLAG TO TRUE */
                    blnSuccess = true;
                } else {
                    /* TOGGLE THE BOOLEAN FLAG TO FALSE */
                    blnSuccess = false;

                    /* SET THE ERROR MESSAGE */
                    strErrorMessage = "There was en error updating your Profile. Please try again....";
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* CHECK IF THE USER RECORD CREATION WAS SUCCESSFUL */
            if (blnSuccess) {
                Toast.makeText(getApplicationContext(), "Your profile was updated successfully", Toast.LENGTH_SHORT).show();
                finish();
            } else {

                /* SHOW THE ERROR MESSAGE */
                new MaterialDialog.Builder(ProfileEditor.this)
                        .title("Profile Update Failed!")
                        .content(strErrorMessage)
                        .positiveText("OKAY")
                        .theme(Theme.LIGHT)
                        .icon(ContextCompat.getDrawable(ProfileEditor.this, R.drawable.ic_info_outline_black_24dp))
                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                        .show();
            }

            /* DISMISS THE DIALOG */
            progressDialog.dismiss();
        }
    }

    /** FETCH THE LIST OF COUNTRIES **/
    private class fetchCountries extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
//            progressDialog = new ProgressDialog(ProfileEditor.this);
//            progressDialog.setMessage("Please wait....");
//            progressDialog.setIndeterminate(false);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String COUNTRY_URL = "http://leodyssey.com/ZenPets/public/allCountries";
            String COUNTRY_URL = "http://192.168.11.2/zenpets/public/allCountries";
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(COUNTRY_URL)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);

                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JACountries = JORoot.getJSONArray("countries");

                    /* A COUNTRY DATA POJO INSTANCE */
                    CountriesData data;

                    for (int i = 0; i < JACountries.length(); i++) {
                        JSONObject JOCountries = JACountries.getJSONObject(i);
//                        Log.e("COUNTRIES", String.valueOf(JOCountries));

                        /* INSTANTIATE THE COUNTRY DATA POJO INSTANCE */
                        data = new CountriesData();

                        /* GET THE COUNTRY ID */
                        if (JOCountries.has("countryID"))    {
                            String countryID = JOCountries.getString("countryID");
                            data.setCountryID(countryID);
                        } else {
                            data.setCountryID(null);
                        }

                        /* GET THE COUNTRY NAME */
                        if (JOCountries.has("countryName")) {
                            String countryName = JOCountries.getString("countryName");
                            data.setCountryName(countryName);
                        } else {
                            data.setCountryName(null);
                        }

                        /* GET THE CURRENCY NAME */
                        if (JOCountries.has("currencyName"))    {
                            String currencyName = JOCountries.getString("currencyName");
                            data.setCurrencyName(currencyName);
                        } else {
                            data.setCurrencyName(null);
                        }

                        /* GET THE CURRENCY CODE */
                        if (JOCountries.has("currencyCode"))    {
                            String currencyCode = JOCountries.getString("currencyCode");
                            data.setCurrencyCode(currencyCode);
                        } else {
                            data.setCurrencyCode(null);
                        }

                        /* GET THE CURRENCY SYMBOL */
                        if (JOCountries.has("currencySymbol"))  {
                            String currencySymbol = JOCountries.getString("currencySymbol");
                            data.setCurrencySymbol(currencySymbol);
                        } else {
                            data.setCurrencySymbol(null);
                        }

                        /* GET THE COUNTRY FLAG */
                        if (JOCountries.has("countryFlag")) {
                            String countryFlag = JOCountries.getString("countryFlag");
                            Bitmap bmpCountryFlag = imageLoader.loadImageSync(countryFlag);
                            Drawable drawable = new BitmapDrawable(getResources(), bmpCountryFlag);
                            data.setCountryFlag(drawable);
                        } else {
                            data.setCountryFlag(null);
                        }

                        /* ADD THE COLLECTED INFO TO THE ARRAYLIST */
                        arrCountries.add(data);
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

            /* INSTANTIATE THE COUNTRIES ADAPTER */
            countriesAdapter = new CountriesAdapter(ProfileEditor.this, arrCountries);

            /* SET THE COUNTRIES SPINNER */
            spnCountry.setAdapter(countriesAdapter);

//            /* DISMISS THE PROGRESS DIALOG */
//            progressDialog.dismiss();
        }
    }

    /** FETCH THE LIST OF STATES **/
    private class fetchStates extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
//            progressDialog = new ProgressDialog(ProfileEditor.this);
//            progressDialog.setMessage("Please wait....");
//            progressDialog.setIndeterminate(false);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String STATES_URL = "http://leodyssey.com/ZenPets/public/countryStates/" + COUNTRY_ID;
            String STATES_URL = "http://192.168.11.2/zenpets/public/countryStates/" + COUNTRY_ID;
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(STATES_URL)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
//                Log.e("RESULT", strResult);
                JSONObject JORoot = new JSONObject(strResult);

                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JAStates = JORoot.getJSONArray("states");

                    /* A STATES DATA POJO INSTANCE */
                    StatesData data;

                    for (int i = 0; i < JAStates.length(); i++) {
                        JSONObject JOStates = JAStates.getJSONObject(i);
//                        Log.e("STATES", String.valueOf(JOStates));

                        /* INSTANTIATE THE STATES DATA POJO INSTANCE */
                        data = new StatesData();

                        /* GET THE STATE ID */
                        if (JOStates.has("stateID"))    {
                            String stateID = JOStates.getString("stateID");
                            data.setStateID(stateID);
                        } else {
                            data.setStateID(null);
                        }

                        /* GET THE STATE NAME */
                        if (JOStates.has("stateName"))   {
                            String stateName = JOStates.getString("stateName");
                            data.setStateName(stateName);
                        } else {
                            data.setStateName(null);
                        }

                        /* GET THE COUNTRY ID */
                        if (JOStates.has("countryID"))  {
                            String countryID = JOStates.getString("countryID");
                            data.setCountryID(countryID);
                        } else {
                            data.setCountryID(null);
                        }

                        /* ADD THE COLLECTED INFO TO THE ARRAYLIST */
                        arrStates.add(data);
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

            /* INSTANTIATE THE STATES ADAPTER */
            statesAdapter = new StatesAdapter(ProfileEditor.this, arrStates);

            /* SET THE STATES SPINNER */
            spnState.setAdapter(statesAdapter);

//            /* DISMISS THE PROGRESS DIALOG */
//            progressDialog.dismiss();
        }
    }

    /** FETCH THE LIST OF CITIES **/
    private class fetchCities extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
//            progressDialog = new ProgressDialog(ProfileEditor.this);
//            progressDialog.setMessage("Please wait....");
//            progressDialog.setIndeterminate(false);
//            progressDialog.setCancelable(false);
//            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String STATES_URL = "http://leodyssey.com/ZenPets/public/stateCities/" + STATE_ID;
            String STATES_URL = "http://192.168.11.2/zenpets/public/stateCities/" + STATE_ID;
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(STATES_URL)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);

                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JACities = JORoot.getJSONArray("cities");

                    /* A CITIES DATA POJO INSTANCE */
                    CitiesData data;

                    for (int i = 0; i < JACities.length(); i++) {
                        JSONObject JOCities = JACities.getJSONObject(i);
//                        Log.e("CITIES", String.valueOf(JOStates));

                        /* INSTANTIATE THE CITIES DATA POJO INSTANCE */
                        data = new CitiesData();

                        /* GET THE CITY ID */
                        if (JOCities.has("cityID"))    {
                            String cityID = JOCities.getString("cityID");
                            data.setCityID(cityID);
                        } else {
                            data.setCityID(null);
                        }

                        /* GET THE CITY NAME */
                        if (JOCities.has("cityName"))   {
                            String cityName = JOCities.getString("cityName");
                            data.setCityName(cityName);
                        } else {
                            data.setCityName(null);
                        }

                        /* GET THE STATE ID */
                        if (JOCities.has("stateID"))  {
                            String stateID = JOCities.getString("stateID");
                            data.setStateID(stateID);
                        } else {
                            data.setStateID(null);
                        }

                        /* ADD THE COLLECTED INFO TO THE ARRAYLIST */
                        arrCities.add(data);
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

            /* INSTANTIATE THE CITIES ADAPTER */
            citiesAdapter = new CitiesAdapter(ProfileEditor.this, arrCities);

            /* SET THE CITIES SPINNER */
            spnCity.setAdapter(citiesAdapter);

//            /* DISMISS THE PROGRESS DIALOG */
//            progressDialog.dismiss();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}