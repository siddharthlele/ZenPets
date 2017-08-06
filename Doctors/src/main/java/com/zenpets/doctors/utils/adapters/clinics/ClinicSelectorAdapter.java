package com.zenpets.doctors.utils.adapters.clinics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.zenpets.doctors.utils.models.clinics.ClinicsData;

import java.util.ArrayList;

public class ClinicSelectorAdapter extends ArrayAdapter<ClinicsData> {

    /** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER **/
    Activity activity;

    /** LAYOUT INFLATER TO USE A CUSTOM LAYOUT **/
    LayoutInflater inflater = null;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    ArrayList<ClinicsData> arrClinics;

    public ClinicSelectorAdapter(@NonNull Activity activity, @LayoutRes int resource, ArrayList<ClinicsData> arrClinics) {
        super(activity, resource);

        /* CAST THE ACTIVITY FROM THE METHOD TO THE LOCAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrClinics = arrClinics;

        /* INSTANTIATE THE LAYOUT INFLATER */
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrClinics.size();
    }

    @Override
    public ClinicsData getItem(int position) {
        return arrClinics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {

        /* Inflate spinner_rows.xml file for each row ( Defined below ) */
        View row = inflater.inflate(com.zenpets.doctors.R.layout.clinic_selector_dropdown, parent, false);
        AppCompatTextView txtClinicName = (AppCompatTextView) row.findViewById(com.zenpets.doctors.R.id.txtClinicName);

        /* SET THE CLINIC NAME */
        String clinicName = arrClinics.get(position).getClinicName();
        if (clinicName != null)	{
            txtClinicName.setText(clinicName);
        }

        return row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /* Inflate spinner_rows.xml file for each row ( Defined below ) */
        View row = inflater.inflate(com.zenpets.doctors.R.layout.clinic_selector_row, parent, false);
        AppCompatTextView txtClinicName = (AppCompatTextView) row.findViewById(com.zenpets.doctors.R.id.txtClinicName);

        /* SET THE CLINIC NAME */
        String clinicName = arrClinics.get(position).getClinicName();
        if (clinicName != null)	{
            txtClinicName.setText(clinicName);
        }

        return row;
    }
}