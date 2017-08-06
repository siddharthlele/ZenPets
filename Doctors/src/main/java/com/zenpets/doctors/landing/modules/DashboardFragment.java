package com.zenpets.doctors.landing.modules;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.zenpets.doctors.R;
import com.zenpets.doctors.modules.DoctorsCalendar;
import com.zenpets.doctors.modules.DoctorsProfile;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class DashboardFragment extends Fragment {

    /** SHOW THE LIST OF DOCTORS **/
    @OnClick(R.id.linlaDoctors) void showDoctors()  {
        Intent intent = new Intent(getActivity(), DoctorsProfile.class);
        startActivity(intent);
    }

    /** SHOW THE PROMOTE CLINIC ACTIVITY **/
    @OnClick(R.id.linlaPromote) void showPromote()  {
        Toast.makeText(getActivity(), "Coming soon....", Toast.LENGTH_SHORT).show();
    }

    /** SHOW THE CONSULTATIONS ACTIVITY **/
    @OnClick(R.id.linlaConsult) void showConsultations()   {
    }

    /** SHOW THE DOCTOR'S FEEDBACK **/
    @OnClick(R.id.linlaFeedback) void showFeedback()    {
    }

    /** SHOW THE HEALTH TIPS ACTIVITY **/
    @OnClick(R.id.linlaTips) void showTips()    {
    }

    /** SHOW THE DOCTOR'S CALENDAR **/
    @OnClick(R.id.linlaCalendar) void showCalendar()    {
        Intent intent = new Intent(getActivity(), DoctorsCalendar.class);
        startActivity(intent);
    }

    /** SHOW THE DOCTOR'S PATIENTS **/
    @OnClick(R.id.linlaPatients) void showPatients()    {
    }

    /** SHOW THE DOCTOR'S RECORDS **/
    @OnClick(R.id.linlaReports) void showReports()  {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.home_dashboard_fragment, container, false);
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
    }
}