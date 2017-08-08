package biz.zenpets.users.profile.appointments.modules;

import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.doctors.DoctorsList;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.adapters.appointment.modifier.NewUpcomingAppointmentsAdapter;
import biz.zenpets.users.utils.models.appointment.user.UpcomingAppointmentsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UpcomingAppointments extends Fragment {

    private AppPrefs getApp()	{
        return (AppPrefs) getActivity().getApplication();
    }

    private FusedLocationProviderClient mFusedLocationClient;
    protected Location mLastLocation;

    /** THE USER ID **/
    private String USER_ID = null;

    /** THE APPOINTMENT DATA **/
    private String APPOINTMENT_DATE = null;

    /** THE LATLNG INSTANCES FOR CALCULATING THE DISTANCE **/
    LatLng LATLNG_ORIGIN;
    LatLng LATLNG_DESTINATION;

    /** THE UPCOMING APPOINTMENTS ADAPTER AND ARRAY LIST **/
    private NewUpcomingAppointmentsAdapter upcomingAppointmentsAdapter;
    private final ArrayList<UpcomingAppointmentsData> arrAppointments = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listUpcomingAppointments) RecyclerView listUpcomingAppointments;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** MAKE A NEW APPOINTMENT (FAB) **/
    @OnClick(R.id.fabNewAppointment) void fabNewAppointment()   {
        Intent intent = new Intent(getActivity(), DoctorsList.class);
        startActivity(intent);
    }

    /** MAKE A NEW APPOINTMENT (EMPTY BUTTON) **/
    @OnClick(R.id.btnMakeAppointment) void btnNewAppointment()  {
        Intent intent = new Intent(getActivity(), DoctorsList.class);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.user_appointments_upcoming_list, container, false);
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
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        getLastLocation();

        /* GET THE CURRENT DATE */
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        APPOINTMENT_DATE = sdf.format(cal.getTime());
//        APPOINTMENT_DATE = "2017-05-07";
//        Log.e("APPOINTMENT DATE", APPOINTMENT_DATE);

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();
        
        /* INSTANTIATE THE APPOINTMENTS ADAPTER */
        upcomingAppointmentsAdapter = new NewUpcomingAppointmentsAdapter(getActivity(), arrAppointments);
        
        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

//        /* FETCH THE LIST OF UPCOMING APPOINTMENTS */
//        new fetchUpcoming().execute();
    }

    @SuppressWarnings("MissingPermission")
    private void getLastLocation() {
        mFusedLocationClient.getLastLocation()
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            mLastLocation = task.getResult();

                            /* GET THE ORIGIN LATLNG */
                            LATLNG_ORIGIN = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());

                            /* FETCH THE LIST OF UPCOMING APPOINTMENTS */
                            new fetchUpcoming().execute();
                        } else {
                            Log.e("EXCEPTION", String.valueOf(task.getException()));
                        }
                    }
                });
    }

    private class fetchUpcoming extends AsyncTask<Void, Void, Void> {

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
           // String URL_UPCOMING_APPOINTMENTS = "http://leodyssey.com/ZenPets/public/fetchUserUpcomingAppointments";
             String URL_UPCOMING_APPOINTMENTS = "http://192.168.11.2/zenpets/public/fetchUserUpcomingAppointments";
            HttpUrl.Builder builder = HttpUrl.parse(URL_UPCOMING_APPOINTMENTS).newBuilder();
            builder.addQueryParameter("userID", USER_ID);
//            builder.addQueryParameter("appointmentDate", APPOINTMENT_DATE);
            builder.addQueryParameter("appointmentDate", "2017-07-08");
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
                        /* AN INSTANCE OF THE UPCOMING APPOINTMENTS DATA MODEL */
                        UpcomingAppointmentsData data;

                        for (int i = 0; i < JAAppointments.length(); i++) {
                            JSONObject JOAppointments = JAAppointments.getJSONObject(i);

                            /* INSTANTIATE THE UPCOMING APPOINTMENTS DATA INSTANCE */
                            data = new UpcomingAppointmentsData();

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

                            /* GET THE CITY NAME */
                            if (JOAppointments.has("cityName")) {
                                data.setCityName(JOAppointments.getString("cityName"));
                            } else {
                                data.setCityName(null);
                            }

                            /* GET THE LOCALITY NAME */
                            if (JOAppointments.has("localityName")) {
                                data.setLocalityName(JOAppointments.getString("localityName"));
                            } else {
                                data.setLocalityName(null);
                            }

                            /* GET THE CLINIC LATITUDE */
                            if (JOAppointments.has("clinicLatitude") && JOAppointments.has("clinicLongitude"))   {
                                Double lat = Double.valueOf(JOAppointments.getString("clinicLatitude"));
                                Double lng = Double.valueOf(JOAppointments.getString("clinicLongitude"));
                                LATLNG_DESTINATION = new LatLng(lat, lng);
                                String URL_DISTANCE = getUrl(LATLNG_ORIGIN, LATLNG_DESTINATION);
                                OkHttpClient clientDistance = new OkHttpClient();
                                Request requestDistance = new Request.Builder()
                                        .url(URL_DISTANCE)
                                        .build();
                                Call callDistance = clientDistance.newCall(requestDistance);
                                Response respDistance = callDistance.execute();
                                String strDistance = respDistance.body().string();
                                JSONObject JORootDistance = new JSONObject(strDistance);
                                JSONArray array = JORootDistance.getJSONArray("routes");
                                JSONObject JORoutes = array.getJSONObject(0);
                                JSONArray JOLegs= JORoutes.getJSONArray("legs");
                                JSONObject JOSteps = JOLegs.getJSONObject(0);
                                JSONObject JODistance = JOSteps.getJSONObject("distance");
                                if (JODistance.has("text")) {
                                    String distance = JODistance.getString("text");
                                    data.setDistanceToClinic(distance);
                                } else {
                                    data.setDistanceToClinic("N.A.");
                                }
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

//                            /* GET THE CLINIC LATITUDE */
//                            if (JOAppointments.has("clinicLatitude"))   {
//                                data.setClinicLatitude(Double.valueOf(JOAppointments.getString("clinicLatitude")));
//                            } else {
//                                data.setClinicLatitude(0.00);
//                            }
//
//                            /* GET THE CLINIC LONGITUDE */
//                            if (JOAppointments.has("clinicLongitude"))  {
//                                data.setClinicLongitude(Double.valueOf(JOAppointments.getString("clinicLongitude")));
//                            } else {
//                                data.setClinicLongitude(0.00);
//                            }

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
                                String appointmentDate = JOAppointments.getString("appointmentDate");
                                String strMonth = getMonth(appointmentDate);
                                String strDate = getDate(appointmentDate);
                                data.setAppointmentDate(strDate + " " + strMonth);
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
                                listUpcomingAppointments.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listUpcomingAppointments.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                        /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linlaEmpty.setVisibility(View.VISIBLE);
                            listUpcomingAppointments.setVisibility(View.GONE);
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

            /* INSTANTIATE THE APPOINTMENTS ADAPTER */
            upcomingAppointmentsAdapter = new NewUpcomingAppointmentsAdapter(getActivity(), arrAppointments);

            /* SET THE ADAPTER TO THE RECYCLER VIEW */
            listUpcomingAppointments.setAdapter(upcomingAppointmentsAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /** CREATE THE DIRECTIONS URL **/
    private String getUrl(LatLng origin, LatLng dest) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;

        // Output format
        String output = "json";

//        Log.e("URL", "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters);

        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }

    /***** GET THE DATE *****/
    private String getDate(String date) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
            Calendar cal  = Calendar.getInstance();
            cal.setTime(d);
            String strDate = new SimpleDateFormat("dd").format(cal.getTime());
            return strDate;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /***** GET THE MONTH NAME *****/
    private String getMonth(String date) {
        try {
            Date d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(date);
            Calendar cal  = Calendar.getInstance();
            cal.setTime(d);
            String monthName = new SimpleDateFormat("MMMM").format(cal.getTime());
            return monthName;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }

    /***** CONFIGURE THE RECYCLER VIEW *****/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listUpcomingAppointments.setLayoutManager(manager);
        listUpcomingAppointments.setHasFixedSize(true);
        listUpcomingAppointments.setAdapter(upcomingAppointmentsAdapter);
    }
}