package com.zenpets.doctors.landing.modules;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.zenpets.doctors.profile.DoctorClinicsActivity;
import com.zenpets.doctors.utils.TypefaceSpan;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    /** THE FIREBASE USER INSTANCE **/
    FirebaseUser user;

    /** THE LOGGED IN DOCTOR'S EMAIL ADDRESS AND DOCTOR ID **/
    String DOCTOR_EMAIL = null;
    String DOCTOR_ID = null;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(com.zenpets.doctors.R.id.imgvwProfile) CircleImageView imgvwProfile;
    @BindView(com.zenpets.doctors.R.id.txtUserName) AppCompatTextView txtUserName;

    /** SHOW THE DOCTOR'S PROFILE **/
    @OnClick(com.zenpets.doctors.R.id.txtViewProfile) void showProfile()    {
    }

    /** SHOW THE CLINICS THE DOCTORS PRACTICES AT **/
    @OnClick(com.zenpets.doctors.R.id.linlaClinics) void showClinics()  {
        Intent intent = new Intent(getActivity(), DoctorClinicsActivity.class);
        startActivity(intent);
    }

    /** SHOW THE TIPS POSTED BY THE DOCTOR **/
    @OnClick(com.zenpets.doctors.R.id.linlaMyTips) void showDoctorTips()   {
    }

    /** SHOW THE PATIENT RECORDS **/
    @OnClick(com.zenpets.doctors.R.id.linlaPatients) void showRecords() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(com.zenpets.doctors.R.layout.home_profile_fragment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /** INDICATE THAT THE FRAGMENT SHOULD RETAIN IT'S STATE **/
        setRetainInstance(true);

        /** INDICATE THAT THE FRAGMENT HAS AN OPTIONS MENU **/
        setHasOptionsMenu(true);

        /** INVALIDATE THE EARLIER OPTIONS MENU SET IN OTHER FRAGMENTS / ACTIVITIES **/
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* GET THE USER DETAILS */
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            /** GET THE USER ID **/
            DOCTOR_EMAIL = user.getEmail();

            /* FETCH THE USER'S PROFILE */
            fetchUserProfile();
        } else {
            Toast.makeText(getActivity(), "Failed to get required data....", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    /***** FETCH THE USER'S PROFILE *****/
    private void fetchUserProfile() {
        String URL_USER_DETAILS = "http://leodyssey.com/ZenPets/public/fetchDoctorProfile";
        // String URL_USER_DETAILS = "http://192.168.11.2/zenpets/public/fetchDoctorProfile";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DETAILS).newBuilder();
        builder.addQueryParameter("doctorEmail", DOCTOR_EMAIL);
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
                        /* GET THE USER ID */
                        if (JORoot.has("doctorID"))   {
                            DOCTOR_ID = JORoot.getString("doctorID");
                        }

                        /* GET THE DOCTOR'S PREFIX */
                        if (JORoot.has("doctorPrefix") && JORoot.has("doctorName")) {
                            final String DOCTOR_PREFIX = JORoot.getString("doctorPrefix");
                            final String DOCTOR_NAME = JORoot.getString("doctorName");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    txtUserName.setText(DOCTOR_PREFIX + " " + DOCTOR_NAME);
                                }
                            });
                        }

                        /* GET THE USER DISPLAY PROFILE */
                        if (JORoot.has("doctorDisplayProfile")) {
                            final String DOCTOR_DISPLAY_PROFILE = JORoot.getString("doctorDisplayProfile");
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Glide.with(getActivity())
                                            .load(DOCTOR_DISPLAY_PROFILE)
                                            .crossFade()
                                            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                            .centerCrop()
                                            .into(imgvwProfile);
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

    /***** CONFIGURE THE ACTIONBAR *****/
    private void configAB() {
//        String strTitle = getResources().getString(R.string.dash_home);
        String strTitle = "Profile";
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getActivity()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(s);
    }
}