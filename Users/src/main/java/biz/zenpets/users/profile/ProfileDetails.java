package biz.zenpets.users.profile;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileDetails extends AppCompatActivity {

    /** THE USER'S EMAIL ADDRESS **/
    private String USER_AUTH_ID = null;

    /** STRINGS TO HOLD THE USER DATA **/
    private String USER_NAME = null;
    private String USER_DISPLAY_PROFILE = null;
    private String USER_EMAIL = null;
    private String USER_PHONE_PREFIX = null;
    private String USER_PHONE_NUMBER = null;
    private String USER_CITY = null;
    private String USER_STATE = null;
    private String USER_COUNTRY = null;
    private String USER_GENDER = null;
//    String USER_DOB = null;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaProgress) LinearLayout linlaProgress;
    @BindView(R.id.imgvwProfilePicture) AppCompatImageView imgvwProfilePicture;
    @BindView(R.id.txtUserName) AppCompatTextView txtUserName;
    @BindView(R.id.txtEmailAddress) AppCompatTextView txtEmailAddress;
    @BindView(R.id.txtPhoneNumber) AppCompatTextView txtPhoneNumber;
    @BindView(R.id.txtCity) AppCompatTextView txtCity;
    @BindView(R.id.txtGender) AppCompatTextView txtGender;
//    @BindView(R.id.txtDOB) AppCompatTextView txtDOB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_details);
        ButterKnife.bind(this);

        /* CONFIGURE THE TOOLBAR */
        configTB();

        /* GET THE USER LOGGED IN USER EMAIL ADDRESS */
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            USER_AUTH_ID = user.getUid();
            if (USER_AUTH_ID != null) {
                /* FETCH THE USER PROFILE */
                new fetchUserProfile().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private class fetchUserProfile extends AsyncTask<Void, Void, Void>  {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_PROFILE = "http://leodyssey.com/ZenPets/public/fetchProfile";
            String URL_USER_PROFILE = "http://192.168.11.2/zenpets/public/fetchProfile";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_PROFILE).newBuilder();
            builder.addQueryParameter("userAuthID", USER_AUTH_ID);
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
//                Log.e("PROFILE", String.valueOf(JORoot));
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                    /* GET THE USER'S NAME */
                    if (JORoot.has("userName")) {
                        USER_NAME = JORoot.getString("userName");
                    }

                    /* GET THE USER'S DISPLAY PROFILE */
                    if (JORoot.has("userDisplayProfile"))   {
                        USER_DISPLAY_PROFILE = JORoot.getString("userDisplayProfile");
                    }

                    /* GET THE USER'S EMAIL ADDRESS */
                    if (JORoot.has("userEmail"))    {
                        USER_EMAIL = JORoot.getString("userEmail");
                    }

                    /* GET THE PHONE PREFIX */
                    if (JORoot.has("userPhonePrefix"))  {
                        USER_PHONE_PREFIX = JORoot.getString("userPhonePrefix");
                    }

                    /* GET THE PHONE NUMBER */
                    if (JORoot.has("userPhoneNumber"))  {
                        USER_PHONE_NUMBER = JORoot.getString("userPhoneNumber");
                    }

                    /* GET THE CITY NAME */
                    if (JORoot.has("cityName")) {
                        USER_CITY = JORoot.getString("cityName");
                    }

                    /* GET THE STATE NAME */
                    if (JORoot.has("stateName")) {
                        USER_STATE = JORoot.getString("stateName");
                    }

                    /* GET THE COUNTRY NAME */
                    if (JORoot.has("countryName"))  {
                        USER_COUNTRY = JORoot.getString("countryName");
                    }

                    /* GET THE USER'S GENDER */
                    if (JORoot.has("userGender"))   {
                        USER_GENDER = JORoot.getString("userGender");
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

            /* SET THE USER NAME */
            txtUserName.setText(USER_NAME);

            /* SET THE USER DISPLAY PROFILE */
            Picasso.with(ProfileDetails.this)
                    .load(USER_DISPLAY_PROFILE)
                    .centerCrop()
                    .fit()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imgvwProfilePicture, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(ProfileDetails.this)
                                    .load(USER_DISPLAY_PROFILE)
                                    .centerCrop()
                                    .fit()
                                    .into(imgvwProfilePicture);
                        }
                    });

            /* SET THE USER'S EMAIL ADDRESS */
            txtEmailAddress.setText(USER_EMAIL);

            /* SET THE USER'S PHONE NUMBER */
            txtPhoneNumber.setText(USER_PHONE_PREFIX + " " + USER_PHONE_NUMBER);

            /* SET THE USER'S LOCATION */
            txtCity.setText(USER_CITY + ", " + USER_STATE + ", " + USER_COUNTRY);

            /* SET THE USER'S GENDER */
            txtGender.setText(USER_GENDER);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            linlaProgress.setVisibility(View.GONE);
        }
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
//        String strTitle = getString(R.string.add_a_new_pet);
        String strTitle = "Profile";
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getApplicationContext()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(s);
        getSupportActionBar().setSubtitle(null);
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = new MenuInflater(ProfileDetails.this);
//        inflater.inflate(R.menu.activity_profile_details, menu);
//        return super.onCreateOptionsMenu(menu);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
//            case R.id.menuEdit:
//                /**** EDIT THE PROFILE *****/
//                Intent intent = new Intent(getApplicationContext(), ProfileEditor.class);
//                startActivity(intent);
//                break;
            default:
                break;
        }
        return false;
    }
}