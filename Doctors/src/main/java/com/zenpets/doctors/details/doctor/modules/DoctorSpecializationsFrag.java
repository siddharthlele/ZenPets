package com.zenpets.doctors.details.doctor.modules;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;

import com.zenpets.doctors.utils.adapters.doctors.modules.DoctorSpecializationAdapter;
import com.zenpets.doctors.utils.models.doctors.SpecializationsData;

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
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DoctorSpecializationsFrag extends Fragment {

    /** THE INCOMING DOCTOR ID **/
    String DOCTOR_ID = null;

    /** THE SPECIALIZATIONS ADAPTER AND ARRAY LIST **/
    DoctorSpecializationAdapter adapter;
    ArrayList<SpecializationsData> arrSpecialization = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(com.zenpets.doctors.R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(com.zenpets.doctors.R.id.listDocSpecialization) RecyclerView listDocSpecialization;
    @BindView(com.zenpets.doctors.R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** ADD A NEW SPECIALIZATION **/
    @OnClick(com.zenpets.doctors.R.id.fabNewSpecialization) void fabNewSpecialization()  {
        showNewSpecializationDialog();
    }

    @OnClick(com.zenpets.doctors.R.id.linlaEmpty) void newSpecialization()  {
        showNewSpecializationDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* CAST THE LAYOUT TO A NEW VIEW INSTANCE */
        View view = inflater.inflate(com.zenpets.doctors.R.layout.doctor_details_specializations_frag_list, container, false);
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

    /***** GET THE DOCTOR'S SPECIALIZATIONS *****/
    private void getDoctorSpecializations() {
        /* CLEAR THE ARRAY LIST */
        arrSpecialization.clear();

        /* SHOW THE PROGRESS WHILE LOADING THE DATA */
        linlaHeaderProgress.setVisibility(View.VISIBLE);

        String USER_EDUCATION_URL = "http://leodyssey.com/ZenPets/public/fetchDoctorSpecializations";
        // String USER_EDUCATION_URL = "http://192.168.11.2/zenpets/public/fetchDoctorSpecializations";
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
                        JSONArray JASpecializations = JORoot.getJSONArray("specializations");
                        if (JASpecializations.length() > 0) {
                            /* A SPECIALIZATION DATA INSTANCE */
                            SpecializationsData data;

                            for (int i = 0; i < JASpecializations.length(); i++) {
                                JSONObject JOSpecializations = JASpecializations.getJSONObject(i);

                                /* INSTANTIATE THE SPECIALIZATION DATA INSTANCE */
                                data = new SpecializationsData();

                                /* GET THE SPECIALIZATION ID */
                                if (JOSpecializations.has("doctorSpecializationID"))  {
                                    data.setDoctorSpecializationID(JOSpecializations.getString("doctorSpecializationID"));
                                } else {
                                    data.setDoctorSpecializationID(null);
                                }

                                /* GET THE SPECIALIZATION NAME */
                                if (JOSpecializations.has("doctorSpecializationName"))    {
                                    data.setDoctorSpecializationName(JOSpecializations.getString("doctorSpecializationName"));
                                } else {
                                    data.setDoctorSpecializationName(null);
                                }

                                /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                                arrSpecialization.add(data);
                            }

                            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY VIEW */
                                    listDocSpecialization.setVisibility(View.VISIBLE);
                                    linlaEmpty.setVisibility(View.GONE);

                                    /* CONFIGURE THE RECYCLER VIEW **/
                                    configRecycler();

                                    /* INSTANTIATE THE SPECIALIZATION ADAPTER */
                                    adapter = new DoctorSpecializationAdapter(getActivity(), arrSpecialization);

                                    /* SET THE CLINICS RECYCLER VIEW */
                                    listDocSpecialization.setAdapter(adapter);

                                    /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                    linlaHeaderProgress.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                    listDocSpecialization.setVisibility(View.GONE);
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
                                listDocSpecialization.setVisibility(View.GONE);
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

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID"))    {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            if (!TextUtils.isEmpty(DOCTOR_ID))   {

                /* GET THE DOCTOR'S SPECIALIZATIONS */
                getDoctorSpecializations();

            } else {
                Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_LONG).show();
        }
    }

    /** SHOW THE NEW SPECIALIZATION DIALOG **/
    private void showNewSpecializationDialog() {
        new MaterialDialog.Builder(getActivity())
                .title("New Specialization")
                .content("Add a new specialization for this Doctor. \n\nExample 1: Veterinary Physician\n\nExample 2: Veterinary Surgeon")
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_WORDS | InputType.TYPE_TEXT_FLAG_MULTI_LINE)
                .inputRange(5, 200)
                .theme(Theme.LIGHT)
                .typeface("HelveticaNeueLTW1G-MdCn.otf", "HelveticaNeueLTW1G-Cn.otf")
                .positiveText("ADD")
                .negativeText("Cancel")
                .input("Add a specialization....", null, false, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NonNull final MaterialDialog dialog, CharSequence input) {
                        /* THE URL TO CREATE A NEW SPECIALIZATION RECORD */
                        String URL_NEW_SPECIALIZATION = "http://leodyssey.com/ZenPets/public/newDoctorSpecialization";
                        // String URL_NEW_SPECIALIZATION = "http://192.168.11.2/zenpets/public/newDoctorSpecialization";
                        OkHttpClient client = new OkHttpClient();
                        RequestBody body = new FormBody.Builder()
                                .add("doctorID", DOCTOR_ID)
                                .add("doctorSpecializationName", input.toString())
                                .build();
                        Request request = new Request.Builder()
                                .url(URL_NEW_SPECIALIZATION)
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
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                dialog.dismiss();
                                                Toast.makeText(getActivity(), "Successfully added a new specialization", Toast.LENGTH_SHORT).show();

                                                /* CLEAR THE ARRAY LIST */
                                                arrSpecialization.clear();

                                                /* FETCH THE LIST OF SPECIALIZATIONS AGAIN */
                                                getDoctorSpecializations();
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity(), "There was an error adding the new specialization. Please try again", Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        listDocSpecialization.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listDocSpecialization.setLayoutManager(llm);
    }
}