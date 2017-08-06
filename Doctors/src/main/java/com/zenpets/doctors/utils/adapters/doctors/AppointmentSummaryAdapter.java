package com.zenpets.doctors.utils.adapters.doctors;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.zenpets.doctors.R;
import com.zenpets.doctors.details.appointment.AppointmentDetails;
import com.zenpets.doctors.utils.models.doctors.AppointmentsData;

import java.util.ArrayList;

public class AppointmentSummaryAdapter extends RecyclerView.Adapter<AppointmentSummaryAdapter.AppointmentsVH> {

    /** AN ACTIVITY INSTANCE **/
    private Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private ArrayList<AppointmentsData> arrAppointments;

    public AppointmentSummaryAdapter(Activity activity, ArrayList<AppointmentsData> arrAppointments) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrAppointments = arrAppointments;
    }

    @Override
    public int getItemCount() {
        return arrAppointments.size();
    }

    @Override
    public void onBindViewHolder(AppointmentsVH holder, final int position) {
        final AppointmentsData data = arrAppointments.get(position);

        /* SET THE APPOINTMENT TIME */
        if (data.getAppointmentTime() != null)   {
            holder.txtTime.setText(data.getAppointmentTime());
        }

        /* SET THE USER NAME */
        if (data.getUserName() != null) {
            holder.txtUserName.setText(data.getUserName());
        }

        /* SET THE VISIT REASON */
        if (data.getVisitReason() != null)  {
            holder.txtVisitReason.setText(data.getVisitReason());
        }

        /* SHOW THE LIST OF APPOINTMENT OPTIONS  */
        holder.imgvwAppointmentOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(activity, v);
                pm.getMenuInflater().inflate(R.menu.popup_appointments, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())   {
                            case R.id.menuDetails:
                                Intent intent = new Intent(activity, AppointmentDetails.class);
                                intent.putExtra("APPOINTMENT_ID", data.getAppointmentID());
                                activity.startActivity(intent);
                                break;
                            case R.id.menuCancel:
                                break;
                            default:
                                break;
                        }
                        return true;
                    }
                });
                pm.show();
            }
        });
    }

    @Override
    public AppointmentsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.appointment_summary_item, parent, false);

        return new AppointmentsVH(itemView);
    }

    class AppointmentsVH extends RecyclerView.ViewHolder	{
        AppCompatTextView txtTime;
        AppCompatTextView txtUserName;
        AppCompatTextView txtVisitReason;
        AppCompatImageView imgvwAppointmentOptions;

        AppointmentsVH (View v) {
            super(v);
            txtTime = (AppCompatTextView) v.findViewById(R.id.txtTime);
            txtUserName = (AppCompatTextView) v.findViewById(R.id.txtUserName);
            txtVisitReason = (AppCompatTextView) v.findViewById(R.id.txtVisitReason);
            imgvwAppointmentOptions = (AppCompatImageView) v.findViewById(R.id.imgvwAppointmentOptions);
        }

    }
}