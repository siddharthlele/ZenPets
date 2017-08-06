package com.zenpets.doctors.utils.adapters.doctors.modules;

import android.app.Activity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.models.doctors.SpecializationsData;

import java.util.ArrayList;

public class DoctorSpecializationAdapter extends RecyclerView.Adapter<DoctorSpecializationAdapter.ClinicsVH> {

    /** AN ACTIVITY INSTANCE **/
    private Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private ArrayList<SpecializationsData> arrSpecialization;

    public DoctorSpecializationAdapter(Activity activity, ArrayList<SpecializationsData> arrSpecialization) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrSpecialization = arrSpecialization;
    }

    @Override
    public int getItemCount() {
        return arrSpecialization.size();
    }

    @Override
    public void onBindViewHolder(ClinicsVH holder, final int position) {
        SpecializationsData data = arrSpecialization.get(position);

        /* SET THE SPECIALIZATION NAME */
        if (data.getDoctorSpecializationName() != null)   {
            holder.txtDoctorSpecialization.setText(data.getDoctorSpecializationName());
        }
    }

    @Override
    public ClinicsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.doctor_details_specializations_frag_item, parent, false);

        return new ClinicsVH(itemView);
    }

    class ClinicsVH extends RecyclerView.ViewHolder	{
        AppCompatTextView txtDoctorSpecialization;

        ClinicsVH(View v) {
            super(v);
            txtDoctorSpecialization = (AppCompatTextView) v.findViewById(R.id.txtDoctorSpecialization);
        }

    }
}