package biz.zenpets.users.utils.adapters.doctors;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.details.doctor.DoctorDetails;
import biz.zenpets.users.utils.models.doctors.DoctorsData;

public class DoctorsListAdapter extends RecyclerView.Adapter<DoctorsListAdapter.DoctorsVH> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
    private final ArrayList<DoctorsData> arrDoctors;

    public DoctorsListAdapter(Activity activity, ArrayList<DoctorsData> arrDoctors) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrDoctors = arrDoctors;
    }

    @Override
    public int getItemCount() {
        return arrDoctors.size();
    }

    @Override
    public void onBindViewHolder(final DoctorsVH holder, final int position) {
        final DoctorsData data = arrDoctors.get(position);

        /* SET THE CLINIC NAME */
        holder.txtClinicName.setText(data.getClinicName());

        /* SET THE CLINIC ADDRESS */
        holder.txtClinicAddress.setText(data.getClinicAddress());

        /* SET THE CLINIC DISTANCE FROM THE USER'S CURRENT LOCATION */
        holder.txtDoctorDistance.setText(data.getClinicDistance() + " km");

        /* SET THE DOCTOR'S NAME */
        holder.txtDoctorName.setText(data.getDoctorName());
        
        /* SET THE DOCTOR'S DISPLAY PROFILE */
//        Picasso.with(activity)
//                .load(data.getDoctorDisplayProfile())
//                .centerCrop()
//                .fit()
//                .networkPolicy(NetworkPolicy.OFFLINE)
//                .into(holder.imgvwDoctorProfile, new com.squareup.picasso.Callback() {
//                    @Override
//                    public void onSuccess() {
//                    }
//
//                    @Override
//                    public void onError() {
//                        Picasso.with(activity)
//                                .load(data.getDoctorDisplayProfile())
//                                .centerCrop()
//                                .fit()
//                                .into(holder.imgvwDoctorProfile);
//                    }
//                });

        /* SET THE DOCTOR'S EXPERIENCE */
        holder.txtDoctorExp.setText(data.getDoctorExperience() + " yrs exp");

        /* SET THE DOCTOR'S CHARGES */
        holder.txtDoctorCharges.setText(data.getDoctorCharges());

        /* SET THE TOTAL NUMBER OF VOTES */
        if (data.getDoctorLikes() != null)  {
            holder.txtDoctorLikesTotal.setText(data.getDoctorVotes());
        } else {
            holder.txtDoctorLikesTotal.setText("0 Votes");
        }

        /* SET THE LIKES PERCENTAGE */
        holder.txtDoctorLikesPercent.setText(data.getDoctorLikesPercent());

        /* SHOW THE DOCTOR DETAILS */
        holder.linlaDoctorContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, DoctorDetails.class);
                intent.putExtra("DOCTOR_ID", data.getDoctorID());
                intent.putExtra("CLINIC_ID", data.getClinicID());
                activity.startActivity(intent);
            }
        });
    }

    @Override
    public DoctorsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.doctors_item, parent, false);

        return new DoctorsVH(itemView);
    }

    class DoctorsVH extends RecyclerView.ViewHolder	{

        final LinearLayout linlaDoctorContainer;
        final AppCompatTextView txtDoctorName;
//        final CircleImageView imgvwDoctorProfile;
        final AppCompatTextView txtDoctorLikesPercent;
        final AppCompatTextView txtDoctorLikesTotal;
        final AppCompatTextView txtClinicName;
        final AppCompatTextView txtClinicAddress;
        final LinearLayout linlaDoctorExp;
        final AppCompatTextView txtDoctorExp;
        final LinearLayout linlaDoctorCharges;
        final AppCompatTextView txtDoctorCharges;
        final LinearLayout linlaDoctorDistance;
        final AppCompatTextView txtDoctorDistance;

        DoctorsVH(View v) {
            super(v);

            linlaDoctorContainer = (LinearLayout) v.findViewById(R.id.linlaDoctorContainer);
            txtDoctorName = (AppCompatTextView) v.findViewById(R.id.txtDoctorName);
//            imgvwDoctorProfile = (CircleImageView) v.findViewById(R.id.imgvwDoctorProfile);
            txtDoctorLikesPercent = (AppCompatTextView) v.findViewById(R.id.txtDoctorLikesPercent);
            txtDoctorLikesTotal = (AppCompatTextView) v.findViewById(R.id.txtDoctorLikesTotal);
            txtClinicName = (AppCompatTextView) v.findViewById(R.id.txtClinicName);
            txtClinicAddress = (AppCompatTextView) v.findViewById(R.id.txtClinicAddress);
            linlaDoctorExp = (LinearLayout) v.findViewById(R.id.linlaDoctorExp);
            txtDoctorExp = (AppCompatTextView) v.findViewById(R.id.txtDoctorExp);
            linlaDoctorCharges = (LinearLayout) v.findViewById(R.id.linlaDoctorCharges);
            txtDoctorCharges = (AppCompatTextView) v.findViewById(R.id.txtDoctorCharges);
            linlaDoctorDistance = (LinearLayout) v.findViewById(R.id.linlaDoctorDistance);
            txtDoctorDistance = (AppCompatTextView) v.findViewById(R.id.txtDoctorDistance);
        }
    }
}