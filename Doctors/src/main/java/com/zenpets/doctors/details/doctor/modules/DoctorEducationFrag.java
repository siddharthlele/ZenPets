package com.zenpets.doctors.details.doctor.modules;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zenpets.doctors.R;
import com.zenpets.doctors.creators.doctor.EducationRecordCreator;
import com.zenpets.doctors.utils.adapters.doctors.modules.DoctorEducationAdapter;
import com.zenpets.doctors.utils.models.doctors.EducationData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DoctorEducationFrag extends Fragment {

    /** THE INCOMING DOCTOR ID **/
    String DOCTOR_ID = null;

    /** THE EDUCATION QUALIFICATIONS ADAPTER AND ARRAY LIST **/
    DoctorEducationAdapter adapter;
    ArrayList<EducationData> arrEducation = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listDocEducation) RecyclerView listDocEducation;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** ADD A NEW EDUCATIONAL QUALIFICATION **/
    @OnClick(R.id.fabNewEducation) void newFabNewEducation()   {
        Intent intent = new Intent(getActivity(), EducationRecordCreator.class);
        intent.putExtra("DOCTOR_ID", DOCTOR_ID);
        startActivityForResult(intent, 101);
    }

    @OnClick(R.id.linlaEmpty) void newEducation()   {
        Intent intent = new Intent(getActivity(), EducationRecordCreator.class);
        intent.putExtra("DOCTOR_ID", DOCTOR_ID);
        startActivityForResult(intent, 101);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* CAST THE LAYOUT TO A NEW VIEW INSTANCE */
        View view = inflater.inflate(R.layout.doctor_details_education_frag_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* INDICATE THAT THE FRAGMENT SHOULD RETAIN IT'S STATE */
        setRetainInstance(true);

        /* INDICATE THAT THE FRAGMENT HAS AN OPTIONS MENU */
        setHasOptionsMenu(true);

        /* INVALIDATE THE EARLIER OPTIONS MENU SET IN OTHER FRAGMENTS / ACTIVITIES */
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* GET THE INCOMING DATA */
        getIncomingData();
    }

    /***** GET THE DOCTOR'S EDUCATIONAL QUALIFICATIONS *****/
    private void getDoctorEducation() {
        /* CLEAR THE ARRAY */
        arrEducation.clear();

        /* SHOW THE PROGRESS WHILE LOADING THE DATA */
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        String USER_EDUCATION_URL = "http://192.168.11.2/zenpets/public/fetchDoctorEducation";
        HttpUrl.Builder builder = HttpUrl.parse(USER_EDUCATION_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
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
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        final JSONArray JAQualifications = JORoot.getJSONArray("qualifications");
                        if (JAQualifications.length() > 0) {
                            /* A EDUCATION DATA INSTANCE */
                            EducationData data;

                            for (int i = 0; i < JAQualifications.length(); i++) {
                                JSONObject JOQualifications = JAQualifications.getJSONObject(i);

                                /* INSTANTIATE THE EDUCATION DATA INSTANCE */
                                data = new EducationData();

                                /* GET THE EDUCATION ID */
                                if (JOQualifications.has("doctorEducationID"))  {
                                    data.setDoctorEducationID(JOQualifications.getString("doctorEducationID"));
                                } else {
                                    data.setDoctorEducationID(null);
                                }

                                /* GET THE COLLEGE NAME */
                                if (JOQualifications.has("doctorCollegeName") && JOQualifications.has("doctorEducationYear"))  {
                                    String doctorCollegeName = JOQualifications.getString("doctorCollegeName");
                                    String doctorEducationYear = JOQualifications.getString("doctorEducationYear");
                                    data.setDoctorCollegeName("Completed from <b>" + doctorCollegeName + "</b> in <b>" + doctorEducationYear + "</b>");
                                } else {
                                    data.setDoctorCollegeName(null);
                                }

                                /* GET THE QUALIFICATION / DEGREE NAME */
                                if (JOQualifications.has("doctorEducationName"))    {
                                    data.setDoctorEducationName(JOQualifications.getString("doctorEducationName"));
                                } else {
                                    data.setDoctorEducationName(null);
                                }

                                /* GET THE YEAR OF COMPLETION */
                                if (JOQualifications.has("doctorEducationYear"))    {
                                    data.setDoctorEducationYear(JOQualifications.getString("doctorEducationYear"));
                                } else {
                                    data.setDoctorEducationYear(null);
                                }

                                /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                                arrEducation.add(data);
                            }

                            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY VIEW */
                                    listDocEducation.setVisibility(View.VISIBLE);
                                    linlaEmpty.setVisibility(View.GONE);

                                    /* CONFIGURE THE RECYCLER VIEW **/
                                    configRecycler();

                                    /* INSTANTIATE THE EDUCATION ADAPTER */
                                    adapter = new DoctorEducationAdapter(getActivity(), arrEducation);

                                    /* SET THE CLINICS RECYCLER VIEW */
                                    listDocEducation.setAdapter(adapter);

                                    /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                    linlaHeaderProgress.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                    listDocEducation.setVisibility(View.GONE);
                                    linlaEmpty.setVisibility(View.VISIBLE);

                                    /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                    linlaHeaderProgress.setVisibility(View.GONE);
                                }
                            });
                        }
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                listDocEducation.setVisibility(View.GONE);
                                linlaEmpty.setVisibility(View.VISIBLE);

                                /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                linlaHeaderProgress.setVisibility(View.GONE);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* CLEAR THE ARRAY LIST */
        arrEducation.clear();

        /* GET THE DOCTOR'S EDUCATIONAL QUALIFICATIONS */
        getDoctorEducation();
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID"))    {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            if (!TextUtils.isEmpty(DOCTOR_ID))   {
                /* GET THE DOCTOR'S EDUCATIONAL QUALIFICATIONS */
                getDoctorEducation();

            } else {
                Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_LONG).show();
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        listDocEducation.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listDocEducation.setLayoutManager(llm);
    }
}