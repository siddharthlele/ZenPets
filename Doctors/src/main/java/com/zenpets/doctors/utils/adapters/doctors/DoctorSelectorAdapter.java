package com.zenpets.doctors.utils.adapters.doctors;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.zenpets.doctors.utils.models.doctors.DoctorsData;

import java.util.ArrayList;

public class DoctorSelectorAdapter extends ArrayAdapter<DoctorsData> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    Activity activity;

    /** LAYOUT INFLATER TO USE A CUSTOM LAYOUT **/
    LayoutInflater inflater = null;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    ArrayList<DoctorsData> arrDoctors;

    public DoctorSelectorAdapter(@NonNull Activity activity, @LayoutRes int resource, ArrayList<DoctorsData> arrDoctors) {
        super(activity, resource);

        /* CAST THE ACTIVITY FROM THE METHOD TO THE LOCAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrDoctors= arrDoctors;

        /* INSTANTIATE THE LAYOUT INFLATER */
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrDoctors.size();
    }

    @Override
    public DoctorsData getItem(int position) {
        return arrDoctors.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = inflater.inflate(com.zenpets.doctors.R.layout.doctor_selector_dropdown, parent, false);
        AppCompatTextView txtDoctorName = (AppCompatTextView) row.findViewById(com.zenpets.doctors.R.id.txtDoctorName);

        /** SET THE DOCTOR'S NAME **/
        String doctorName = arrDoctors.get(position).getDoctorName();
        if (doctorName != null)	{
            txtDoctorName.setText(doctorName);
        }

        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /********** Inflate spinner_rows.xml file for each row ( Defined below ) ************/
        View row = inflater.inflate(com.zenpets.doctors.R.layout.doctor_selector_row, parent, false);
        AppCompatTextView txtDoctorName = (AppCompatTextView) row.findViewById(com.zenpets.doctors.R.id.txtDoctorName);

        /** SET THE DOCTOR'S NAME **/
        String doctorName = arrDoctors.get(position).getDoctorName();
        if (doctorName != null)	{
            txtDoctorName.setText(doctorName);
        }

        return row;
    }
}