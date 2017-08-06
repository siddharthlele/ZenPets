package com.zenpets.doctors.details.doctor.modules;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zenpets.doctors.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DoctorDetailsFrag extends Fragment {

    /** THE INCOMING DOCTOR ID **/
    String DOCTOR_ID = null;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.imgvwDoctorProfile) CircleImageView imgvwDoctorProfile;
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.txtDoctorGender) AppCompatTextView txtDoctorGender;
    @BindView(R.id.txtDoctorExperience) AppCompatTextView txtDoctorExperience;
    @BindView(R.id.txtDoctorCharges) AppCompatTextView txtDoctorCharges;
    @BindView(R.id.txtDoctorSummary) AppCompatTextView txtDoctorSummary;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE */
        View view = inflater.inflate(R.layout.doctor_details_frag, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /** INDICATE THAT THE FRAGMENT SHOULD RETAIN IT'S STATE */
        setRetainInstance(true);

        /** INDICATE THAT THE FRAGMENT HAS AN OPTIONS MENU */
        setHasOptionsMenu(true);

        /** INVALIDATE THE EARLIER OPTIONS MENU SET IN OTHER FRAGMENTS / ACTIVITIES */
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** GET THE INCOMING DATA */
        getIncomingData();
    }

    /* GET THE DOCTOR DETAILS */
    private void getDoctorDetails() {
        String URL_USER_DETAILS = "http://leodyssey.com/ZenPets/public/fetchDoctorProfile";
        // String URL_USER_DETAILS = "http://192.168.11.2/zenpets/public/fetchDoctorProfile";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_DETAILS).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        String FINAL_URL = builder.build().toString();
//        Log.e("PROFILE URL", FINAL_URL);
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
                        JSONArray JADoctors = JORoot.getJSONArray("doctors");
                        for (int i = 0; i < JADoctors.length(); i++) {
                            JSONObject JODoctors = JADoctors.getJSONObject(i);

                            /* GET THE DOCTOR'S PREFIX */
                            if (JODoctors.has("doctorPrefix") && JODoctors.has("doctorName")) {
                                final String DOCTOR_PREFIX = JODoctors.getString("doctorPrefix");
                                final String DOCTOR_NAME = JODoctors.getString("doctorName");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtDoctorName.setText(DOCTOR_PREFIX + " " + DOCTOR_NAME);
                                    }
                                });
                            }

                            /* GET THE DOCTOR'S EXPERIENCE */
                            if (JODoctors.has("doctorExperience")) {
                                final String doctorExperience = JODoctors.getString("doctorExperience");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtDoctorExperience.setText(doctorExperience + " years experience");
                                    }
                                });
                            }

                            /* GET THE DOCTOR'S CHARGES AND THE CURRENCY SYMBOL */
                            if (JODoctors.has("doctorCharges") && JODoctors.has("currencySymbol"))  {
                                final String doctorCharges = JODoctors.getString("doctorCharges");
                                final String currencySymbol = JODoctors.getString("currencySymbol");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtDoctorCharges.setText(currencySymbol + " " + doctorCharges);
                                    }
                                });
                            }

                            /* GET THE DOCTOR'S GENDER */
                            if (JODoctors.has("doctorGender"))  {
                                final String doctorGender = JODoctors.getString("doctorGender");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtDoctorGender.setText(doctorGender);
                                    }
                                });
                            }

                            /* GET THE DOCTOR'S SUMMARY */
                            if (JODoctors.has("doctorSummary")) {
                                final String doctorSummary = JODoctors.getString("doctorSummary");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtDoctorSummary.setText(doctorSummary);
                                    }
                                });
                            }

                            /* GET THE USER DISPLAY PROFILE */
                            if (JODoctors.has("doctorDisplayProfile")) {
                                final String DOCTOR_DISPLAY_PROFILE = JODoctors.getString("doctorDisplayProfile");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Glide.with(getActivity())
                                                .load(DOCTOR_DISPLAY_PROFILE)
                                                .crossFade()
                                                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                                .centerCrop()
                                                .into(imgvwDoctorProfile);
                                    }
                                });
                            }
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID"))    {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            if (!TextUtils.isEmpty(DOCTOR_ID))   {

                /* GET THE DOCTOR DETAILS */
                getDoctorDetails();

            } else {
                Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_LONG).show();
        }
    }
}