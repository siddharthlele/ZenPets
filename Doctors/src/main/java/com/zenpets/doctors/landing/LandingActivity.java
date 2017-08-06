package com.zenpets.doctors.landing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.zenpets.doctors.R;
import com.zenpets.doctors.creators.clinic.ClinicCreator;
import com.zenpets.doctors.landing.modules.CalendarFragment;
import com.zenpets.doctors.landing.modules.ConsultationsFragment;
import com.zenpets.doctors.landing.modules.DashboardFragment;
import com.zenpets.doctors.landing.modules.HelpFragment;
import com.zenpets.doctors.landing.modules.PatientsFragment;
import com.zenpets.doctors.landing.modules.ProfileFragment;
import com.zenpets.doctors.landing.modules.ReportsFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LandingActivity extends AppCompatActivity {

    /** THE FIREBASE USER INSTANCE **/
    FirebaseUser user;

    /** THE LOGGED IN USER'S EMAIL ADDRESS **/
    String USER_EMAIL = null;

    /** DECLARE THE USER PROFILE HEADER **/
    private AppCompatImageView imgvwClinicLogo;
    private AppCompatTextView txtClinicName;

    /** THE CLINIC DETAILS **/
    private String CLINIC_NAME = null;
    private String CLINIC_LOGO = null;

    /** DECLARE THE LAYOUT ELEMENTS **/
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    /** A FRAGMENT INSTANCE **/
    private Fragment mContent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.old_landing_activity);

        /** CONFIGURE THE TOOLBAR **/
        configToolbar();

        /** CONFIGURE THE NAVIGATION BAR **/
        configureNavBar();

        /** GET THE USER DETAILS **/
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            /** CHECK IF THE USER HAS VERIFIED **/
            if (!user.isEmailVerified())    {
                /** SHOW THE UNVERIFIED DIALOG **/
                showVerificationDialog(user);
            }

            /** GET THE USER ID **/
            USER_EMAIL = user.getEmail();

            /** CHECK IF THE USER HAS REGISTERED THEIR CLINIC **/
            checkClinicDetails();
        }

        /** SHOW THE FIRST FRAGMENT (DASHBOARD) **/
        if (savedInstanceState == null) {
            Fragment mContent = new DashboardFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame, mContent, "KEY_FRAG")
                    .commit();
        }
    }

    /***** CHECK IF THE USER HAS REGISTERED THEIR CLINIC *****/
    private void checkClinicDetails() {
        String USER_CLINIC_URL = "http://leodyssey.com/ZenPets/public/checkDoctorClinic";
        // String USER_CLINIC_URL = "http://192.168.11.2/zenpets/public/checkDoctorClinic";
        HttpUrl.Builder builder = HttpUrl.parse(USER_CLINIC_URL).newBuilder();
        builder.addQueryParameter("doctorEmail", USER_EMAIL);
        String FINAL_URL = builder.build().toString();
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
//                        Log.e("CLINIC", String.valueOf(JAClinics));
                        if (JAClinics.length() == 0)    {
                            /* SHOW THE ADD CLINIC INFORMATION DIALOG */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showClinicDetailsDialog();
                                    txtClinicName.setVisibility(View.GONE);
                                    imgvwClinicLogo.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        for (int i = 0; i < JAClinics.length(); i++) {
                                            JSONObject JOClinics = JAClinics.getJSONObject(i);

                                            /* GET THE CLINIC NAME */
                                            if (JOClinics.has("clinicName"))  {
                                                CLINIC_NAME = JOClinics.getString("clinicName");
                                                txtClinicName.setText(CLINIC_NAME);
                                                txtClinicName.setVisibility(View.VISIBLE);
                                            }

                                            /* GET THE CLINIC LOGO */
                                            if (JOClinics.has("clinicLogo"))  {
                                                CLINIC_LOGO = JOClinics.getString("clinicLogo");
                                                if (CLINIC_LOGO != null)   {
                                                    Glide.with(LandingActivity.this)
                                                            .load(CLINIC_LOGO)
                                                            .crossFade()
                                                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                                            .centerCrop()
                                                            .into(imgvwClinicLogo);
                                                    imgvwClinicLogo.setVisibility(View.VISIBLE);
                                                }
                                            }
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** CONFIGURE THE NAVIGATION BAR **/
    private void configureNavBar() {

        /** INITIALIZE THE NAVIGATION VIEW **/
        navigationView = (NavigationView) findViewById(R.id.navigationView);
        View view = navigationView.getHeaderView(0);
        imgvwClinicLogo = (AppCompatImageView) view.findViewById(R.id.imgvwClinicLogo);
        txtClinicName = (AppCompatTextView) view.findViewById(R.id.txtClinicName);

        /** CHANGE THE FRAGMENTS ON NAVIGATION ITEM SELECTION **/
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                /** CHECK IF AN ITEM IS CHECKED / NOT CHECKED **/
                if (menuItem.isChecked())   {
                    menuItem.setChecked(false);
                }  else {
                    menuItem.setChecked(true);
                }

                /** Closing drawer on item click **/
                drawerLayout.closeDrawers();

                /** CHECK SELECTED ITEM AND SHOW APPROPRIATE FRAGMENT **/
                switch (menuItem.getItemId()){
                    case R.id.dashHome:
                        mContent = new DashboardFragment();
                        switchFragment(mContent);
                        return true;
                    case R.id.dashCalendar:
                        mContent = new CalendarFragment();
                        switchFragment(mContent);
                        return true;
                    case R.id.dashPatients:
                        mContent = new PatientsFragment();
                        switchFragment(mContent);
                        return true;
                    case R.id.dashReports:
                        mContent = new ReportsFragment();
                        switchFragment(mContent);
                        return true;
                    case R.id.dashProfile:
                        mContent = new ProfileFragment();
                        switchFragment(mContent);
                        return true;
                    case R.id.dashConsultations:
                        mContent = new ConsultationsFragment();
                        switchFragment(mContent);
                        return true;
                    case R.id.dashHelp:
                        mContent = new HelpFragment();
                        switchFragment(mContent);
                        return true;
                    case R.id.dashShare:{
                        AlertDialog.Builder builder = new AlertDialog.Builder(LandingActivity.this, R.style.ZenPetsDialog);
                        builder.setIcon(R.drawable.ic_info_outline_black_24dp);
                        builder.setTitle("Share with?");
                        builder.setItems(new CharSequence[]{"1: Share with fellow Veterinarians", "2: Share with friends and family"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                switch (which)  {
                                    case 0:
                                        Intent intentVets = new Intent(Intent.ACTION_SEND);
                                        intentVets.setType("text/plain");
                                        intentVets.putExtra(intentVets.EXTRA_SUBJECT, "Zen Pets");
                                        String msgVets = getResources().getString(R.string.share_vets);
                                        intentVets.putExtra(Intent.EXTRA_TEXT, msgVets);
                                        startActivity(Intent.createChooser(intentVets, "Select one...."));
                                        break;
                                    case 1:
                                        Intent intentOthers = new Intent(Intent.ACTION_SEND);
                                        intentOthers.setType("text/plain");
                                        intentOthers.putExtra(intentOthers.EXTRA_SUBJECT, "Zen Pets");
                                        String msgOthers = getResources().getString(R.string.share_vets);
                                        intentOthers.putExtra(Intent.EXTRA_TEXT, msgOthers);
                                        startActivity(Intent.createChooser(intentOthers, "Select one...."));
                                        break;
                                    default:
                                        break;
                                }
                            }
                        }); builder.show();
                    }
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(), "Somethings Wrong", Toast.LENGTH_SHORT).show();
                        return true;
                }
            }
        });
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configToolbar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.myToolbar);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        if (drawerLayout != null) {
            drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        }

        mDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
                syncState();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
                syncState();
            }
        };
        drawerLayout.addDrawerListener(mDrawerToggle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())   {
            case android.R.id.home:
                if (drawerLayout.isDrawerOpen(navigationView))    {
                    drawerLayout.closeDrawer(navigationView);
                } else {
                    drawerLayout.openDrawer(navigationView);
                }
                return  true;

            default: return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    /** METHOD TO CHANGE THE FRAGMENT **/
    private void switchFragment(Fragment fragment) {

        /** HIDE THE NAV DRAWER **/
        drawerLayout.closeDrawer(GravityCompat.START);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, fragment)
                .commit();
    }

    /** SHOW THE VERIFICATION DIALOG **/
    private void showVerificationDialog(final FirebaseUser user) {
        String message = getResources().getString(R.string.unverified_message);
        new MaterialDialog.Builder(LandingActivity.this)
                .icon(ContextCompat.getDrawable(LandingActivity.this, R.drawable.ic_info_outline_black_24dp))
                .title("Account Not Verified")
                .cancelable(false)
                .content(message)
                .positiveText("Send Verification Email")
                .negativeText("Later")
                .theme(Theme.LIGHT)
                .typeface("HelveticaNeueLTW1G-MdCn.otf", "HelveticaNeueLTW1G-Cn.otf")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        user.sendEmailVerification();
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    /** SHOW THE ADD CLINIC INFORMATION DIALOG **/
    private void showClinicDetailsDialog() {
        String message = getResources().getString(R.string.clinic_profile);
        new MaterialDialog.Builder(LandingActivity.this)
                .icon(ContextCompat.getDrawable(LandingActivity.this, R.drawable.ic_info_outline_black_24dp))
                .title("Additional Details Needed")
                .cancelable(true)
                .content(message)
                .positiveText("Add Clinic Details")
                .negativeText("Later")
                .theme(Theme.LIGHT)
                .typeface("HelveticaNeueLTW1G-MdCn.otf", "HelveticaNeueLTW1G-Cn.otf")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Intent intent = new Intent(LandingActivity.this, ClinicCreator.class);
                        startActivityForResult(intent, 101);
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}