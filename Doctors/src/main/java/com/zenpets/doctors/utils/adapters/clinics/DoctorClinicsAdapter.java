package com.zenpets.doctors.utils.adapters.clinics;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.zenpets.doctors.R;
import com.zenpets.doctors.details.clinic.ClinicContainer;
import com.zenpets.doctors.utils.models.clinics.ClinicsData;

import java.util.ArrayList;

public class DoctorClinicsAdapter extends RecyclerView.Adapter<DoctorClinicsAdapter.ClinicsVH> {

    /** AN ACTIVITY INSTANCE **/
    private Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private ArrayList<ClinicsData> arrClinics;

    public DoctorClinicsAdapter(Activity activity, ArrayList<ClinicsData> arrClinics) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrClinics = arrClinics;
    }

    @Override
    public int getItemCount() {
        return arrClinics.size();
    }

    @Override
    public void onBindViewHolder(ClinicsVH holder, final int position) {
        final ClinicsData data = arrClinics.get(position);

        /* SET THE CLINIC LOGO */
        if (data.getClinicLogo() != null)   {
            Glide.with(activity)
                    .load(data.getClinicLogo())
                    .placeholder(R.drawable.empty_camera)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .into(holder.imgvwClinicLogo);
        } else {
            holder.imgvwClinicLogo.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.empty_camera));
        }

        /* SET THE CLINIC NAME */
        if (data.getClinicName() != null)   {
            holder.txtClinicName.setText(data.getClinicName());
        }

        /* SET THE CLINIC ADDRESS */
        if (data.getClinicAddress() != null)    {
            holder.txtClinicAddress.setText(data.getClinicAddress());
        }

        /* SHOW THE CLINIC DETAILS */
        holder.linlaClinicContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ClinicContainer.class);
                intent.putExtra("CLINIC_ID", data.getClinicID());
                activity.startActivityForResult(intent, 102);
            }
        });
    }

    @Override
    public ClinicsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.doctor_clinics_item, parent, false);

        return new ClinicsVH(itemView);
    }

    class ClinicsVH extends RecyclerView.ViewHolder	{
        LinearLayout linlaClinicContainer;
        AppCompatImageView imgvwClinicLogo;
        AppCompatTextView txtClinicName;
        AppCompatTextView txtClinicAddress;

        ClinicsVH(View v) {
            super(v);
            linlaClinicContainer = (LinearLayout) v.findViewById(R.id.linlaClinicContainer);
            imgvwClinicLogo = (AppCompatImageView) v.findViewById(R.id.imgvwClinicLogo);
            txtClinicName = (AppCompatTextView) v.findViewById(R.id.txtClinicName);
            txtClinicAddress = (AppCompatTextView) v.findViewById(R.id.txtClinicAddress);
        }

    }
}