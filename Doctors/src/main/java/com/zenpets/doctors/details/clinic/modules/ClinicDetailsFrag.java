package com.zenpets.doctors.details.clinic.modules;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.adapters.clinics.images.ClinicAlbumAdapter;
import com.zenpets.doctors.utils.models.clinics.images.ClinicAlbumData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ClinicDetailsFrag extends Fragment {

    /** THE INCOMING CLINIC ID **/
    String CLINIC_ID = null;

    /** DATA TYPE TO STORE THE DATA **/
    private Double CLINIC_LATITUDE;
    private Double CLINIC_LONGITUDE;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.imgvwClinicCover) AppCompatImageView imgvwClinicCover;
    @BindView(R.id.txtClinicName) AppCompatTextView txtClinicName;
    @BindView(R.id.txtClinicAddress) AppCompatTextView txtClinicAddress;
    @BindView(R.id.clinicMap) MapView clinicMap;
    @BindView(R.id.txtPhone1) AppCompatTextView txtPhone1;
    @BindView(R.id.txtPhone2) AppCompatTextView txtPhone2;
    @BindView(R.id.txtClinicSubscription) AppCompatTextView txtClinicSubscription;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.clinic_details_frag, container, false);
        ButterKnife.bind(this, view);
        clinicMap = (MapView) view.findViewById(R.id.clinicMap);
        clinicMap.onCreate(savedInstanceState);
        clinicMap.onResume();
        clinicMap.setClickable(false);
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

        /** GET THE INCOMING CLINIC ID **/
        getIncomingData();
    }

    /* GET THE CLINIC DETAILS */
    private void getClinicDetails() {
        /* THE URL TO FETCH THE CLINIC DETAILS **/
        String URL_CLINIC_DETAILS = "http://leodyssey.com/ZenPets/public/clinicDetails";
        // String URL_CLINIC_DETAILS = "http://192.168.11.2/zenpets/public/clinicDetails";
        HttpUrl.Builder builder = HttpUrl.parse(URL_CLINIC_DETAILS).newBuilder();
        builder.addQueryParameter("clinicID", CLINIC_ID);
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
                        JSONArray JAClinics = JORoot.getJSONArray("clinics");
                        for (int i = 0; i < JAClinics.length(); i++) {
                            JSONObject JOClinics = JAClinics.getJSONObject(i);
                            Log.e("CLINIC", String.valueOf(JOClinics));

                            /* GET THE CLINIC NAME */
                            if (JOClinics.has("clinicName")) {
                                final String clinicName = JOClinics.getString("clinicName");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtClinicName.setText(clinicName);
                                    }
                                });
                            }

                            /* GET THE CLINIC PHONE 1 */
                            if (JOClinics.has("clinicPhone1"))  {
                                final String clinicPhone1 = JOClinics.getString("clinicPhone1");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtPhone1.setText(clinicPhone1);
                                    }
                                });
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtPhone1.setText("N.A.");
                                    }
                                });
                            }

                            /* GET THE CLINIC PHONE 2 */
                            if (JOClinics.has("clinicPhone2"))  {
                                final String clinicPhone2 = JOClinics.getString("clinicPhone2");
                                if (clinicPhone2.equals("") || clinicPhone2.equalsIgnoreCase("null") || clinicPhone2 == null) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtPhone2.setText("N.A.");
                                        }
                                    });
                                } else {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            txtPhone2.setText(clinicPhone2);
                                        }
                                    });
                                }
                            } else {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtPhone2.setText("N.A.");
                                    }
                                });
                            }

                            /* GET THE CLINIC ADDRESS */
                            if (JOClinics.has("clinicAddress") && JOClinics.has("countryName") && JOClinics.has("stateName") && JOClinics.has("cityName") && JOClinics.has("localityName") && JOClinics.has("clinicPinCode")) {
                                String clinicAddress = JOClinics.getString("clinicAddress");
                                String countryName = JOClinics.getString("countryName");
                                String stateName = JOClinics.getString("stateName");
                                String cityName = JOClinics.getString("cityName");
                                String localityName = JOClinics.getString("localityName");
                                String clinicPinCode = JOClinics.getString("clinicPinCode");
                                final String finalAddress = clinicAddress + ", " + cityName + ", " + localityName + ", " + stateName + ", " + countryName + ", " + clinicPinCode;
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtClinicAddress.setText(finalAddress);
                                    }
                                });
                            }

                            /* GET THE CLINIC LOGO */
                            if (JOClinics.has("clinicLogo"))    {
                                final String clinicLogo = JOClinics.getString("clinicLogo");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (clinicLogo != null)   {
                                            Glide.with(getActivity())
                                                    .load(clinicLogo)
                                                    .crossFade()
                                                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                                                    .centerCrop()
                                                    .into(imgvwClinicCover);
                                        }
                                    }
                                });
                            }

                            /* GET THE CLINIC LATITUDE AND LONGITUDE */
                            if (JOClinics.has("clinicLatitude") && JOClinics.has("clinicLongitude")) {
                                CLINIC_LATITUDE = Double.valueOf(JOClinics.getString("clinicLatitude"));
                                CLINIC_LONGITUDE = Double.valueOf(JOClinics.getString("clinicLongitude"));
                                if (CLINIC_LATITUDE != null && CLINIC_LONGITUDE != null)    {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            final LatLng latLng = new LatLng(CLINIC_LATITUDE, CLINIC_LONGITUDE);
                                            clinicMap.getMapAsync(new OnMapReadyCallback() {
                                                @Override
                                                public void onMapReady(GoogleMap googleMap) {
                                                    googleMap.getUiSettings().setMapToolbarEnabled(false);
                                                    googleMap.getUiSettings().setAllGesturesEnabled(false);
                                                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                                    googleMap.setBuildingsEnabled(true);
                                                    googleMap.setTrafficEnabled(false);
                                                    googleMap.setIndoorEnabled(false);
                                                    MarkerOptions options = new MarkerOptions();
                                                    options.position(latLng);
                                                    options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                                    Marker mMarker = googleMap.addMarker(options);
                                                    googleMap.addMarker(options);

                                                    /* MOVE THE MAP CAMERA */
                                                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mMarker.getPosition(), 18));
                                                    googleMap.animateCamera(CameraUpdateFactory.zoomTo(18), 2000, null);
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            /* GET THE CLINIC SUBSCRIPTION */
                            if (JOClinics.has("clinicSubscription"))    {
                                final String clinicSubscription = JOClinics.getString("clinicSubscription");
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        txtClinicSubscription.setText(clinicSubscription);
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

    /* GET THE INCOMING CLINIC ID */
    private void getIncomingData() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle.containsKey("CLINIC_ID"))    {
            CLINIC_ID = bundle.getString("CLINIC_ID");
            if (CLINIC_ID != null)  {
                /* GET THE CLINIC DETAILS */
                getClinicDetails();
            } else {
                Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        } else {
            Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        clinicMap.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        clinicMap.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        clinicMap.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clinicMap.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        clinicMap.onLowMemory();
    }
}