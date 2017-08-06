package biz.zenpets.users.profile.appointments.modules;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.adapters.appointment.modifier.PastAppointmentsAdapter;
import biz.zenpets.users.utils.models.appointment.user.PastAppointmentsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PastAppointments extends Fragment {

    private AppPrefs getApp()	{
        return (AppPrefs) getActivity().getApplication();
    }

    /** THE USER ID **/
    private String USER_ID = null;

    /** THE APPOINTMENT DATA **/
    private String APPOINTMENT_DATE = null;

    /** THE UPCOMING APPOINTMENTS ADAPTER AND ARRAY LIST **/
    private PastAppointmentsAdapter pastAppointmentsAdapter;
    private final ArrayList<PastAppointmentsData> arrAppointments = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listPastAppointments) RecyclerView listPastAppointments;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.user_appointments_past_list, container, false);
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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* GET THE CURRENT DATE */
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        APPOINTMENT_DATE = sdf.format(cal.getTime());
//        APPOINTMENT_DATE = "2017-05-07";
//        Log.e("APPOINTMENT DATE", APPOINTMENT_DATE);

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();
        
        /* INSTANTIATE THE APPOINTMENTS ADAPTER */
        pastAppointmentsAdapter = new PastAppointmentsAdapter(getActivity(), arrAppointments);
        
        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* FETCH THE LIST OF PAST APPOINTMENTS */
        new fetchPast().execute();
    }

    /***** FETCH THE LIST OF PAST APPOINTMENTS *****/
    private class fetchPast extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* CLEAR THE ARRAY */
            arrAppointments.clear();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
           // String URL_PAST_APPOINTMENTS = "http://leodyssey.com/ZenPets/public/fetchUserPastAppointments";
            String URL_PAST_APPOINTMENTS = "http://192.168.11.2/zenpets/public/fetchUserPastAppointments";
            HttpUrl.Builder builder = HttpUrl.parse(URL_PAST_APPOINTMENTS).newBuilder();
            builder.addQueryParameter("userID", USER_ID);
            builder.addQueryParameter("appointmentDate", APPOINTMENT_DATE);
            String FINAL_URL = builder.build().toString();
//            Log.e("URL", FINAL_URL);
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
                    JSONArray JAAppointments = JORoot.getJSONArray("appointments");
                    if (JAAppointments.length() > 0) {
                        /* AN INSTANCE OF THE PAST APPOINTMENTS DATA MODEL */
                        PastAppointmentsData data;

                        for (int i = 0; i < JAAppointments.length(); i++) {
                            JSONObject JOAppointments = JAAppointments.getJSONObject(i);

                            /* INSTANTIATE THE PAST APPOINTMENTS DATA INSTANCE */
                            data = new PastAppointmentsData();

                            /* GET THE APPOINTMENT ID */
                            if (JOAppointments.has("appointmentID"))    {
                                data.setAppointmentID(JOAppointments.getString("appointmentID"));
                            } else {
                                data.setAppointmentID(null);
                            }

                            /* GET THE DOCTOR ID */
                            if (JOAppointments.has("doctorID")) {
                                data.setDoctorID(JOAppointments.getString("doctorID"));
                            } else {
                                data.setDoctorID(null);
                            }

                            /* GET THE DOCTOR'S PREFIX */
                            if (JOAppointments.has("doctorPrefix")) {
                                data.setDoctorPrefix(JOAppointments.getString("doctorPrefix"));
                            } else {
                                data.setDoctorPrefix(null);
                            }

                            /* GET THE DOCTOR'S NAME */
                            if (JOAppointments.has("doctorName"))   {
                                data.setDoctorName(JOAppointments.getString("doctorName"));
                            } else {
                                data.setDoctorName(null);
                            }

                            /* GET THE DOCTOR'S DISPLAY PROFILE */
                            if (JOAppointments.has("doctorDisplayProfile")) {
                                data.setDoctorDisplayProfile(JOAppointments.getString("doctorDisplayProfile"));
                            } else {
                                data.setDoctorDisplayProfile(null);
                            }

                            /* GET THE CLINIC ID */
                            if (JOAppointments.has("clinicID")) {
                                data.setClinicID(JOAppointments.getString("clinicID"));
                            } else {
                                data.setClinicID(null);
                            }

                            /* GET THE CLINIC NAME */
                            if (JOAppointments.has("clinicName"))   {
                                data.setClinicName(JOAppointments.getString("clinicName"));
                            } else {
                                data.setClinicName(null);
                            }

                            /* GET THE CLINIC ADDRESS */
                            if (JOAppointments.has("clinicAddress"))    {
                                data.setClinicAddress(JOAppointments.getString("clinicAddress"));
                            } else {
                                data.setClinicAddress(null);
                            }

                            /* GET THE CLINIC LATITUDE */
                            if (JOAppointments.has("clinicLatitude"))   {
                                data.setClinicLatitude(Double.valueOf(JOAppointments.getString("clinicLatitude")));
                            } else {
                                data.setClinicLatitude(0.00);
                            }

                            /* GET THE CLINIC LONGITUDE */
                            if (JOAppointments.has("clinicLongitude"))  {
                                data.setClinicLongitude(Double.valueOf(JOAppointments.getString("clinicLongitude")));
                            } else {
                                data.setClinicLongitude(0.00);
                            }

                            /* GET THE PET ID */
                            if (JOAppointments.has("petID"))    {
                                data.setPetID(JOAppointments.getString("petID"));
                            } else {
                                data.setPetID(null);
                            }

                            /* GET THE PET NAME */
                            if (JOAppointments.has("petName"))    {
                                data.setPetName(JOAppointments.getString("petName"));
                            } else {
                                data.setPetName(null);
                            }

                            /* GET THE VISIT REASON ID */
                            if (JOAppointments.has("visitReasonID"))    {
                                data.setVisitReasonID(JOAppointments.getString("visitReasonID"));
                            } else {
                                data.setVisitReasonID(null);
                            }

                            /* GET THE VISIT REASON */
                            if (JOAppointments.has("visitReason"))  {
                                data.setVisitReason(JOAppointments.getString("visitReason"));
                            } else {
                                data.setVisitReason(null);
                            }

                            /* GET THE APPOINTMENT DATE */
                            if (JOAppointments.has("appointmentDate"))  {
                                data.setAppointmentDate(JOAppointments.getString("appointmentDate"));
                            } else {
                                data.setAppointmentDate(null);
                            }

                            /* GET THE APPOINTMENT TIME */
                            if (JOAppointments.has("appointmentTime"))  {
                                data.setAppointmentTime(JOAppointments.getString("appointmentTime"));
                            } else {
                                data.setAppointmentTime(null);
                            }

                            /* GET THE APPOINTMENT STATUS */
                            if (JOAppointments.has("appointmentStatus"))    {
                                data.setAppointmentStatus(JOAppointments.getString("appointmentStatus"));
                            } else {
                                data.setAppointmentStatus("Pending");
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrAppointments.add(data);
                        }

                        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY LAYOUT */
                                listPastAppointments.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listPastAppointments.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                        /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linlaEmpty.setVisibility(View.VISIBLE);
                            listPastAppointments.setVisibility(View.GONE);
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

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** CONFIGURE THE RECYCLER VIEW *****/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listPastAppointments.setLayoutManager(manager);
        listPastAppointments.setHasFixedSize(true);
        listPastAppointments.setAdapter(pastAppointmentsAdapter);
    }
}