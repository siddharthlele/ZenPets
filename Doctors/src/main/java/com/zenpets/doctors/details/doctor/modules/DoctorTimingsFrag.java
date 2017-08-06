package com.zenpets.doctors.details.doctor.modules;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.adapters.clinics.ClinicSelectorAdapter;
import com.zenpets.doctors.utils.helpers.TimingsPickerActivity;
import com.zenpets.doctors.utils.models.clinics.ClinicsData;

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

public class DoctorTimingsFrag extends Fragment {

    /** THE INCOMING DOCTOR ID **/
    String DOCTOR_ID = null;

    /** THE CLINIC ID **/
    String CLINIC_ID = null;

    /** THE CLINIC SELECTOR ADAPTER AND ARRAY LIST **/
    ClinicSelectorAdapter clinicSelectorAdapter;
    ArrayList<ClinicsData> arrClinics = new ArrayList<>();

    /** THE TIMING STRINGS **/
    String SUN_MOR_FROM = null;
    String SUN_MOR_TO = null;
    String SUN_AFT_FROM = null;
    String SUN_AFT_TO = null;
    String MON_MOR_FROM = null;
    String MON_MOR_TO = null;
    String MON_AFT_FROM = null;
    String MON_AFT_TO = null;
    String TUE_MOR_FROM = null;
    String TUE_MOR_TO = null;
    String TUE_AFT_FROM = null;
    String TUE_AFT_TO = null;
    String WED_MOR_FROM = null;
    String WED_MOR_TO = null;
    String WED_AFT_FROM = null;
    String WED_AFT_TO = null;
    String THU_MOR_FROM = null;
    String THU_MOR_TO = null;
    String THU_AFT_FROM = null;
    String THU_AFT_TO = null;
    String FRI_MOR_FROM = null;
    String FRI_MOR_TO = null;
    String FRI_AFT_FROM = null;
    String FRI_AFT_TO = null;
    String SAT_MOR_FROM = null;
    String SAT_MOR_TO = null;
    String SAT_AFT_FROM = null;
    String SAT_AFT_TO = null;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.spnClinics) AppCompatSpinner spnClinics;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;
    @BindView(R.id.scrollTimings) ScrollView scrollTimings;
    @BindView(R.id.txtSunMorning) AppCompatTextView txtSunMorning;
    @BindView(R.id.txtSunAfternoon) AppCompatTextView txtSunAfternoon;
    @BindView(R.id.txtMonMorning) AppCompatTextView txtMonMorning;
    @BindView(R.id.txtMonAfternoon) AppCompatTextView txtMonAfternoon;
    @BindView(R.id.txtTueMorning) AppCompatTextView txtTueMorning;
    @BindView(R.id.txtTueAfternoon) AppCompatTextView txtTueAfternoon;
    @BindView(R.id.txtWedMorning) AppCompatTextView txtWedMorning;
    @BindView(R.id.txtWedAfternoon) AppCompatTextView txtWedAfternoon;
    @BindView(R.id.txtThuMorning) AppCompatTextView txtThuMorning;
    @BindView(R.id.txtThuAfternoon) AppCompatTextView txtThuAfternoon;
    @BindView(R.id.txtFriMorning) AppCompatTextView txtFriMorning;
    @BindView(R.id.txtFriAfternoon) AppCompatTextView txtFriAfternoon;
    @BindView(R.id.txtSatMorning) AppCompatTextView txtSatMorning;
    @BindView(R.id.txtSatAfternoon) AppCompatTextView txtSatAfternoon;

    /** ADD DOCTOR TIMINGS **/
    @OnClick(R.id.linlaEmpty) void configureTimings()   {
        Intent intent = new Intent(getActivity(), TimingsPickerActivity.class);
        intent.putExtra("DOCTOR_ID", DOCTOR_ID);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /* GET THE INCOMING DATA */
        getIncomingData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* CAST THE LAYOUT TO A NEW VIEW INSTANCE */
        View view = inflater.inflate(R.layout.doctor_details_timings_frag, container, false);
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

        /* SELECT A CLINIC TO SHOW THE DOCTOR'S TIMINGS AT IT */
        spnClinics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CLINIC_ID = arrClinics.get(position).getClinicID();
                if (CLINIC_ID != null)  {
                    /* GET THE DOCTOR'S TIMINGS */
                    getDoctorTimings();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /***** FETCH THE CLINICS THE DOCTOR PRACTICES AT *****/
    private void fetchClinics() {
        String USER_CLINIC_URL = "http://leodyssey.com/ZenPets/public/checkDoctorClinic";
        // String USER_CLINIC_URL = "http://192.168.11.2/zenpets/public/checkDoctorClinic";
        HttpUrl.Builder builder = HttpUrl.parse(USER_CLINIC_URL).newBuilder();
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
                        final JSONArray JAClinics = JORoot.getJSONArray("clinics");
                        if (JAClinics.length() > 0) {

                            /* A CLINICS DATA INSTANCE */
                            ClinicsData data;
                            for (int i = 0; i < JAClinics.length(); i++) {
                                JSONObject JOClinics = JAClinics.getJSONObject(i);

                                /* INSTANTIATE THE CLINICS DATA INSTANCE */
                                data = new ClinicsData();

                                /* GET THE CLINIC ID */
                                if (JOClinics.has("clinicID"))  {
                                    String clinicID = JOClinics.getString("clinicID");
                                    data.setClinicID(clinicID);
                                } else {
                                    data.setClinicID(null);
                                }

                                /* GET THE CLINIC NAME */
                                if (JOClinics.has("clinicName"))    {
                                    String clinicName = JOClinics.getString("clinicName");
                                    data.setClinicName(clinicName);
                                } else {
                                    data.setClinicName(null);
                                }

                                /* GET THE CLINIC ADDRESS */
                                if (JOClinics.has("cityName") && JOClinics.has("localityName")) {
                                    String cityName = JOClinics.getString("cityName");
                                    String localityName = JOClinics.getString("localityName");
                                    String finalAddress = localityName + ", " + cityName;
                                    data.setClinicAddress(finalAddress);
                                } else {
                                    data.setClinicAddress(null);
                                }

                                /* GET THE CLINIC LOGO */
                                if (JOClinics.has("clinicLogo"))    {
                                    String clinicLogo = JOClinics.getString("clinicLogo");
                                    if (clinicLogo.equals("") || clinicLogo.equalsIgnoreCase("null")) {
                                        data.setClinicLogo(null);
                                    } else {
                                        data.setClinicLogo(clinicLogo);
                                    }
                                } else {
                                    data.setClinicLogo(null);
                                }

                                /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                                arrClinics.add(data);
                            }

                            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    /* INSTANTIATE THE CLINICS SELECTOR ADAPTER */
                                    clinicSelectorAdapter = new ClinicSelectorAdapter(
                                            getActivity(),
                                            R.layout.clinic_selector_row,
                                            arrClinics);

                                    /* SET THE CLINICS SPINNER */
                                    spnClinics.setAdapter(clinicSelectorAdapter);
                                }
                            });

                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(getActivity(), "You haven't added any clinics to your account yet", Toast.LENGTH_SHORT).show();
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

    /***** GET THE DOCTOR'S TIMINGS *****/
    private void getDoctorTimings() {
        String TIMINGS_URL = "http://leodyssey.com/ZenPets/public/fetchDoctorTimings";
        // String TIMINGS_URL = "http://192.168.11.2/zenpets/public/fetchDoctorTimings";
        HttpUrl.Builder builder = HttpUrl.parse(TIMINGS_URL).newBuilder();
        builder.addQueryParameter("doctorID", DOCTOR_ID);
        builder.addQueryParameter("clinicID", CLINIC_ID);
        String FINAL_URL = builder.build().toString();
        Log.e("URL", FINAL_URL);
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
                        JSONArray JATimings = JORoot.getJSONArray("timings");
                        if (JATimings.length() > 0) {
                            for (int i = 0; i < JATimings.length(); i++) {
                                JSONObject JOTimings = JATimings.getJSONObject(i);

                                /* GET THE SUNDAY MORNING TIMINGS */
                                if (JOTimings.has("sunMorFrom") && JOTimings.has("sunMorTo")) {
                                    if (!JOTimings.getString("sunMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("sunMorTo").equalsIgnoreCase("null"))  {
                                        SUN_MOR_FROM = JOTimings.getString("sunMorFrom");
                                        SUN_MOR_TO = JOTimings.getString("sunMorTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtSunMorning.setText(SUN_MOR_FROM + " - " + SUN_MOR_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtSunMorning.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtSunMorning.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE SUNDAY AFTERNOON TIMINGS */
                                if (JOTimings.has("sunAftFrom") && JOTimings.has("sunAftTo")) {
                                    if (!JOTimings.getString("sunAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("sunAftTo").equalsIgnoreCase("null"))  {
                                        SUN_AFT_FROM = JOTimings.getString("sunAftFrom");
                                        SUN_AFT_TO = JOTimings.getString("sunAftTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtSunAfternoon.setText(SUN_AFT_FROM + " - " + SUN_AFT_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtSunAfternoon.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtSunAfternoon.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE MONDAY MORNING TIMINGS */
                                if (JOTimings.has("monMorFrom") && JOTimings.has("monMorTo")) {
                                    if (!JOTimings.getString("monMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("monMorTo").equalsIgnoreCase("null"))  {
                                        MON_MOR_FROM = JOTimings.getString("monMorFrom");
                                        MON_MOR_TO = JOTimings.getString("monMorTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtMonMorning.setText(MON_MOR_FROM + " - " + MON_MOR_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtMonMorning.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtMonMorning.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE MONDAY AFTERNOON TIMINGS */
                                if (JOTimings.has("monAftFrom") && JOTimings.has("monAftTo")) {
                                    if (!JOTimings.getString("monAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("monAftTo").equalsIgnoreCase("null"))  {
                                        MON_AFT_FROM = JOTimings.getString("monAftFrom");
                                        MON_AFT_TO = JOTimings.getString("monAftTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtMonAfternoon.setText(MON_AFT_FROM + " - " + MON_AFT_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtMonAfternoon.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtMonAfternoon.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE TUESDAY MORNING TIMINGS */
                                if (JOTimings.has("tueMorFrom") && JOTimings.has("tueMorTo")) {
                                    if (!JOTimings.getString("tueMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("tueMorTo").equalsIgnoreCase("null"))  {
                                        TUE_MOR_FROM = JOTimings.getString("tueMorFrom");
                                        TUE_MOR_TO = JOTimings.getString("tueMorTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtTueMorning.setText(TUE_MOR_FROM + " - " + TUE_MOR_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtTueMorning.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtTueMorning.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE TUESDAY AFTERNOON TIMINGS */
                                if (JOTimings.has("tueAftFrom") && JOTimings.has("tueAftTo")) {
                                    if (!JOTimings.getString("tueAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("tueAftTo").equalsIgnoreCase("null"))  {
                                        TUE_AFT_FROM = JOTimings.getString("tueAftFrom");
                                        TUE_AFT_TO = JOTimings.getString("tueAftTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtTueAfternoon.setText(TUE_AFT_FROM + " - " + TUE_AFT_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtTueAfternoon.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtTueAfternoon.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE WEDNESDAY MORNING TIMINGS */
                                if (JOTimings.has("wedMorFrom") && JOTimings.has("wedMorTo")) {
                                    if (!JOTimings.getString("wedMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("wedMorTo").equalsIgnoreCase("null"))  {
                                        WED_MOR_FROM = JOTimings.getString("wedMorFrom");
                                        WED_MOR_TO = JOTimings.getString("wedMorTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtWedMorning.setText(WED_MOR_FROM + " - " + WED_MOR_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtWedMorning.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtWedMorning.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE WEDNESDAY AFTERNOON TIMINGS */
                                if (JOTimings.has("wedAftFrom") && JOTimings.has("wedAftTo")) {
                                    if (!JOTimings.getString("wedAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("wedAftTo").equalsIgnoreCase("null"))  {
                                        WED_AFT_FROM = JOTimings.getString("wedAftFrom");
                                        WED_AFT_TO = JOTimings.getString("wedAftTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtWedAfternoon.setText(WED_AFT_FROM + " - " + WED_AFT_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtWedAfternoon.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtWedAfternoon.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE THURSDAY MORNING TIMINGS */
                                if (JOTimings.has("thuMorFrom") && JOTimings.has("thuMorTo")) {
                                    if (!JOTimings.getString("thuMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("thuMorTo").equalsIgnoreCase("null"))  {
                                        THU_MOR_FROM = JOTimings.getString("thuMorFrom");
                                        THU_MOR_TO = JOTimings.getString("thuMorTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtThuMorning.setText(THU_MOR_FROM + " - " + THU_MOR_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtThuMorning.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtThuMorning.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE THURSDAY AFTERNOON TIMINGS */
                                if (JOTimings.has("thuAftFrom") && JOTimings.has("thuAftTo")) {
                                    if (!JOTimings.getString("thuAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("thuAftTo").equalsIgnoreCase("null"))  {
                                        THU_AFT_FROM = JOTimings.getString("thuAftFrom");
                                        THU_AFT_TO = JOTimings.getString("thuAftTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtThuAfternoon.setText(THU_AFT_FROM + " - " + THU_AFT_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtThuAfternoon.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtThuAfternoon.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE FRIDAY MORNING TIMINGS */
                                if (JOTimings.has("friMorFrom") && JOTimings.has("friMorTo")) {
                                    if (!JOTimings.getString("friMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("friMorTo").equalsIgnoreCase("null"))  {
                                        FRI_MOR_FROM = JOTimings.getString("friMorFrom");
                                        FRI_MOR_TO = JOTimings.getString("friMorTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtFriMorning.setText(FRI_MOR_FROM + " - " + FRI_MOR_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtFriMorning.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtFriMorning.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE FRIDAY AFTERNOON TIMINGS */
                                if (JOTimings.has("friAftFrom") && JOTimings.has("friAftTo")) {
                                    if (!JOTimings.getString("friAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("friAftTo").equalsIgnoreCase("null"))  {
                                        FRI_AFT_FROM = JOTimings.getString("friAftFrom");
                                        FRI_AFT_TO = JOTimings.getString("friAftTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtFriAfternoon.setText(FRI_AFT_FROM + " - " + FRI_AFT_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtFriAfternoon.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtFriAfternoon.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE SATURDAY MORNING TIMINGS */
                                if (JOTimings.has("satMorFrom") && JOTimings.has("satMorTo")) {
                                    if (!JOTimings.getString("satMorFrom").equalsIgnoreCase("null") && !JOTimings.getString("satMorTo").equalsIgnoreCase("null"))  {
                                        SAT_MOR_FROM = JOTimings.getString("satMorFrom");
                                        SAT_MOR_TO = JOTimings.getString("satMorTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtSatMorning.setText(SAT_MOR_FROM + " - " + SAT_MOR_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtSatMorning.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtSatMorning.setText("Closed");
                                        }
                                    });
                                }

                                /* GET THE SATURDAY AFTERNOON TIMINGS */
                                if (JOTimings.has("satAftFrom") && JOTimings.has("satAftTo")) {
                                    if (!JOTimings.getString("satAftFrom").equalsIgnoreCase("null") && !JOTimings.getString("satAftTo").equalsIgnoreCase("null"))  {
                                        SAT_AFT_FROM = JOTimings.getString("satAftFrom");
                                        SAT_AFT_TO = JOTimings.getString("satAftTo");
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtSatAfternoon.setText(SAT_AFT_FROM + " - " + SAT_AFT_TO);
                                            }
                                        });
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                txtSatAfternoon.setText("Closed");
                                            }
                                        });
                                    }
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtSatAfternoon.setText("Closed");
                                        }
                                    });
                                }
                            }
                            /* SHOW THE SCROLL VIEW AND HIDE THE EMPTY LAYOUT */
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scrollTimings.setVisibility(View.VISIBLE);
                                    linlaEmpty.setVisibility(View.GONE);
                                }
                            });
                        } else {
                            /* HIDE THE SCROLL VIEW AND SHOW THE EMPTY LAYOUT */
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    scrollTimings.setVisibility(View.GONE);
                                    linlaEmpty.setVisibility(View.VISIBLE);
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

    /** GET THE INCOMING DATA **/
    private void getIncomingData() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID"))    {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            if (!TextUtils.isEmpty(DOCTOR_ID))   {
                /* FETCH THE LIST OF CLINICS */
                fetchClinics();
            } else {
                Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_LONG).show();
        }
    }
}