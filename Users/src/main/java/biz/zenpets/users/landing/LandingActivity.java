package biz.zenpets.users.landing;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import biz.zenpets.users.R;
import biz.zenpets.users.landing.modules.HomeFragment;
import biz.zenpets.users.landing.modules.ProfileFragment;
import biz.zenpets.users.landing.modules.QuestionsFragment;
import biz.zenpets.users.modifiers.profile.ProfileEditor;
import biz.zenpets.users.utils.AppPrefs;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LandingActivity extends AppCompatActivity {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** A FIREBASE USER INSTANCE **/
    FirebaseUser user;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.bottomNavigation) BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing_activity);
        ButterKnife.bind(this);

        /** CAPTURE IP ADDRESS **/
//        captureIPAddress();

        /* SELECT AND SHOW THE DEFAULT FRAGMENT (HOME FRAGMENT) */
        Menu menu = bottomNavigation.getMenu();
        selectFragment(menu.getItem(0));

        /* CHANGE THE FRAGMENT BASED ON BOTTOM NAV SELECTION */
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectFragment(item);
                return false;
            }
        });

        /* DETERMINE IF THE USER IS LOGGED IN */
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            /* FETCH THE USER'S PROFILE */
            new fetchUserProfile().execute();
        }
    }

    /***** FETCH THE USER'S PROFILE *****/
    private class fetchUserProfile extends AsyncTask<Void, Void, Void>  {

        /** BOOLEAN TO CHECK IF PROFILE IS COMPLETE **/
        boolean blnProfileComplete = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_DETAILS = "http://leodyssey.com/ZenPets/public/fetchProfile";
            String URL_USER_DETAILS = "http://192.168.11.2/zenpets/public/fetchProfile";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DETAILS).newBuilder();
            builder.addQueryParameter("userAuthID", user.getUid());
            String FINAL_URL = builder.build().toString();
//            Log.e("USER PROFILE", FINAL_URL);
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

                    /* GET THE USER ID */
                    if (JORoot.has("userID")) {
                        String USER_ID = JORoot.getString("userID");

                        /* SET USER ID TO THE APP'S PRIVATE SHARED PREFERENCES */
                        getApp().setUserID(USER_ID);
                    }

                    String profileComplete;
                    /* GET THE PROFILE STATUS */
                    if (JORoot.has("profileComplete"))  {
                        profileComplete = JORoot.getString("profileComplete");
                    } else {
                        profileComplete = null;
                    }

                    /* CHECK FOR PROFILE COMPLETE STATUS */
                    if (profileComplete != null && profileComplete.equalsIgnoreCase("Incomplete")) {
                        blnProfileComplete = false;
                    } else {
                        blnProfileComplete = true;
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

            /* CHECK IG THE PROFILE IS COMPLETE. OR SHOW THE DIALOG PROMPT */
            if (!blnProfileComplete)    {
                showProfileComplete();
            }
        }
    }

    private void showProfileComplete() {
        String message = "You need to complete your Profile before you can start accessing some sections of Zen Pets. To complete your profile Details, click on the \"Complete Profile\" button.";
        new MaterialDialog.Builder(LandingActivity.this)
                .icon(ContextCompat.getDrawable(LandingActivity.this, R.drawable.ic_info_outline_black_24dp))
                .title("Profile Incomplete")
                .cancelable(true)
                .content(message)
                .positiveText("Complete Profile")
                .negativeText("Later")
                .theme(Theme.LIGHT)
                .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(LandingActivity.this, ProfileEditor.class);
                        startActivity(intent);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        /* SET PROFILE STATUS */
                        getApp().setProfileStatus("Incomplete");
                        dialog.dismiss();
                    }
                }).show();
    }

    /***** CAPTURE IP ADDRESS *****/
//    private void captureIPAddress() {
//        String URL_IP_ADDRESS = "https://icanhazip.com/";
//        OkHttpClient client = new OkHttpClient.Builder()
//                .connectTimeout(60, TimeUnit.SECONDS)
//                .writeTimeout(60, TimeUnit.SECONDS)
//                .readTimeout(60, TimeUnit.SECONDS)
//                .build();
//        Request request = new Request.Builder()
//                .url(URL_IP_ADDRESS)
//                .build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e("FAILURE", e.toString());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                String strResult = response.body().string();
//                Log.e("RESULT", strResult);
//            }
//        });
//    }

    private void selectFragment(MenuItem item) {
        item.setChecked(true);

        switch (item.getItemId())   {
            case R.id.menuHomeFragment:
                switchFragment(new HomeFragment());
                break;
            case R.id.menuQuestionFragment:
                switchFragment(new QuestionsFragment());
                break;
            case R.id.menuProfileFragment:
                switchFragment(new ProfileFragment());
                break;
            default:
                break;
        }
    }

    /** METHOD TO CHANGE THE FRAGMENT **/
    private void switchFragment(Fragment fragment) {
        if (fragment == null)
            return;

        FragmentManager manager = getSupportFragmentManager();
        if (manager != null)    {
            FragmentTransaction transaction = manager.beginTransaction();
            if (transaction != null)    {
                transaction.replace(R.id.mainContent, fragment);
                transaction.commit();
            }
        }
    }
}