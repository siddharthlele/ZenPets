package com.zenpets.doctors.utils.adapters.doctors.modules;

import android.app.Activity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.models.doctors.EducationData;

import java.util.ArrayList;

public class DoctorEducationAdapter extends RecyclerView.Adapter<DoctorEducationAdapter.ClinicsVH> {

    /** AN ACTIVITY INSTANCE **/
    private Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private ArrayList<EducationData> arrEducation;

    public DoctorEducationAdapter(Activity activity, ArrayList<EducationData> arrEducation) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrEducation = arrEducation;
    }

    @Override
    public int getItemCount() {
        return arrEducation.size();
    }

    @Override
    public void onBindViewHolder(ClinicsVH holder, final int position) {
        EducationData data = arrEducation.get(position);

        /* SET THE EDUCATION NAME */
        if (data.getDoctorEducationName() != null)   {
            holder.txtDoctorEducation.setText(data.getDoctorEducationName());
        }

        /* SET THE COLLEGE AND YEAR OF COMPLETION */
        if (data.getDoctorCollegeName() != null)    {
            holder.txtDoctorCollegeYear.setText(Html.fromHtml(data.getDoctorCollegeName()));
        }
    }

    @Override
    public ClinicsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.doctor_details_education_frag_item, parent, false);

        return new ClinicsVH(itemView);
    }

    class ClinicsVH extends RecyclerView.ViewHolder	{
        AppCompatTextView txtDoctorEducation;
        AppCompatTextView txtDoctorCollegeYear;

        ClinicsVH(View v) {
            super(v);
            txtDoctorEducation = (AppCompatTextView) v.findViewById(R.id.txtDoctorEducation);
            txtDoctorCollegeYear = (AppCompatTextView) v.findViewById(R.id.txtDoctorCollegeYear);
        }

    }
}