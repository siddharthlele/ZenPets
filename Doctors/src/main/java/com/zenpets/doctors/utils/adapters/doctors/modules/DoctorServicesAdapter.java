package com.zenpets.doctors.utils.adapters.doctors.modules;

import android.app.Activity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zenpets.doctors.utils.models.doctors.ServicesData;

import java.util.ArrayList;

public class DoctorServicesAdapter extends RecyclerView.Adapter<DoctorServicesAdapter.ClinicsVH> {

    /** AN ACTIVITY INSTANCE **/
    private Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private ArrayList<ServicesData> arrServices;

    public DoctorServicesAdapter(Activity activity, ArrayList<ServicesData> arrServices) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrServices = arrServices;
    }

    @Override
    public int getItemCount() {
        return arrServices.size();
    }

    @Override
    public void onBindViewHolder(ClinicsVH holder, final int position) {
        ServicesData data = arrServices.get(position);

        /* SET THE SERVICE NAME */
        if (data.getDoctorServiceName() != null)   {
            holder.txtDoctorService.setText(data.getDoctorServiceName());
        }
    }

    @Override
    public ClinicsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(com.zenpets.doctors.R.layout.doctor_details_services_frag_item, parent, false);

        return new ClinicsVH(itemView);
    }

    class ClinicsVH extends RecyclerView.ViewHolder	{
        AppCompatTextView txtDoctorService;

        ClinicsVH(View v) {
            super(v);
            txtDoctorService = (AppCompatTextView) v.findViewById(com.zenpets.doctors.R.id.txtDoctorService);
        }

    }
}