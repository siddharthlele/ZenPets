package com.zenpets.doctors.details.doctor.modules;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.squareup.timessquare.CalendarPickerView;
import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.adapters.clinics.ClinicSelectorAdapter;
import com.zenpets.doctors.utils.helpers.TimingsPickerActivity;
import com.zenpets.doctors.utils.models.clinics.ClinicsData;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewDoctorTimingsFrag extends Fragment {

    /** THE INCOMING DOCTOR ID **/
    String DOCTOR_ID = null;

    /** THE CLINIC ID **/
    String CLINIC_ID = null;
    private CalendarPickerView calendar;

    /** THE CLINIC SELECTOR ADAPTER AND ARRAY LIST **/
    ClinicSelectorAdapter clinicSelectorAdapter;
    ArrayList<ClinicsData> arrClinics = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.spnClinics) AppCompatSpinner spnClinics;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** ADD DOCTOR TIMINGS **/
    @OnClick(R.id.linlaEmpty) void configureTimings()   {
        Intent intent = new Intent(getActivity(), TimingsPickerActivity.class);
        intent.putExtra("DOCTOR_ID", DOCTOR_ID);
        startActivityForResult(intent, 101);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* CAST THE LAYOUT TO A NEW VIEW INSTANCE */
        View view = inflater.inflate(R.layout.doctor_details_new_timings_frag, container, false);
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

        /* SELECT A CLINIC TO SHOW THE DOCTOR'S TIMINGS AT IT */
        spnClinics.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CLINIC_ID = arrClinics.get(position).getClinicID();
                if (CLINIC_ID != null)  {
                    /* GET THE DOCTOR'S TIMINGS */
//                    getDoctorTimings();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }
}