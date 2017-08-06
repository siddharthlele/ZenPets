package biz.zenpets.users.utils.adapters.appointment.modifier;

import android.app.Activity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.mikepenz.iconics.view.IconicsImageView;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.appointment.user.PastAppointmentsData;

public class PastAppointmentsAdapter extends RecyclerView.Adapter<PastAppointmentsAdapter.SlotsVH> {

    /** AN ACTIVITY INSTANCE **/
    private final Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<PastAppointmentsData> arrAppointments;

    public PastAppointmentsAdapter(Activity activity, ArrayList<PastAppointmentsData> arrAppointments) {

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
    public void onBindViewHolder(SlotsVH holder, final int position) {
        final PastAppointmentsData data = arrAppointments.get(position);

        /* SET THE APPOINTMENT STATUS */
        if (data.getAppointmentStatus() != null)    {
            holder.txtAppointmentStatus.setText(data.getAppointmentStatus());
        } else {
            holder.txtAppointmentStatus.setText("Unknown");
        }

        /* SHOW OR HIDE THE APPOINTMENT OPTIONS */
        if (data.getAppointmentStatus().equalsIgnoreCase("Confirmed")
                || data.getAppointmentStatus().equalsIgnoreCase("Completed"))  {
            holder.imgvwAppointmentOptions.setVisibility(View.GONE);
        } else if (data.getAppointmentStatus().equalsIgnoreCase("Pending")) {
            holder.imgvwAppointmentOptions.setVisibility(View.VISIBLE);
        } else {
            holder.imgvwAppointmentOptions.setVisibility(View.GONE);
        }

        /* SET THE DOCTOR'S NAME */
        if (data.getDoctorPrefix() != null && data.getDoctorName() != null) {
            holder.txtDoctorName.setText(data.getDoctorPrefix() + " " + data.getDoctorName());
        }

        /* SET THE CLINIC NAME */
        if (data.getClinicName() != null)   {
            holder.txtClinicName.setText(data.getClinicName());
        }

        /* SET THE CLINIC ADDRESS */
        if (data.getClinicAddress() != null)    {
            holder.txtClinicAddress.setText(data.getClinicAddress());
        }

        /* SET THE APPOINTMENT DATE */
        if (data.getAppointmentDate() != null)  {
            holder.txtAppointmentDate.setText(/*"Appointment on " + */data.getAppointmentDate());
        }

        /* SET THE APPOINTMENT TIME */
        if (data.getAppointmentTime() != null)  {
            holder.txtAppointmentTime.setText(/*"Appointment at " + */data.getAppointmentTime());
        }

        /* SHOW THE APPOINTMENT STATUS */
        holder.imgvwStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (data.getAppointmentStatus().equalsIgnoreCase("Confirmed")) {
                    new MaterialDialog.Builder(activity)
                            .title("Appointment Confirmed")
                            .content("Your appointment has been confirmed by the Doctor and is as scheduled. \n\nYou cannot make any changes to this appointment.")
                            .positiveText("Got It")
                            .theme(Theme.LIGHT)
                            .icon(ContextCompat.getDrawable(activity, R.drawable.ic_info_outline_black_24dp))
                            .typeface("RobotoCondensed-Regular.ttf", "Roboto-Regular.ttf")
                            .show();
                } else if (data.getAppointmentStatus().equalsIgnoreCase("Pending")) {
                    new MaterialDialog.Builder(activity)
                            .title("Appointment Pending")
                            .content("Your appointment is currently pending and hasn't been confirmed by the Doctor yet. \n\nYou can make changes to this appointment before it is confirmed by the Doctor.")
                            .positiveText("Got It")
                            .theme(Theme.LIGHT)
                            .icon(ContextCompat.getDrawable(activity, R.drawable.ic_info_outline_black_24dp))
                            .typeface("RobotoCondensed-Regular.ttf", "Roboto-Regular.ttf")
                            .show();
                } else if (data.getAppointmentStatus().equalsIgnoreCase("Completed")){
                    new MaterialDialog.Builder(activity)
                            .title("Appointment Completed")
                            .content("Your appointment was marked complete by the Doctor. \n\nYou cannot make any changes to this appointment.")
                            .positiveText("Got It")
                            .theme(Theme.LIGHT)
                            .icon(ContextCompat.getDrawable(activity, R.drawable.ic_info_outline_black_24dp))
                            .typeface("RobotoCondensed-Regular.ttf", "Roboto-Regular.ttf")
                            .show();
                } else {
                    new MaterialDialog.Builder(activity)
                            .title("Status Unknown")
                            .content("There was a problem getting your appointment's status.")
                            .positiveText("Got It")
                            .theme(Theme.LIGHT)
                            .icon(ContextCompat.getDrawable(activity, R.drawable.ic_info_outline_black_24dp))
                            .typeface("RobotoCondensed-Regular.ttf", "Roboto-Regular.ttf")
                            .show();
                }
            }
        });
    }

    @Override
    public SlotsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.user_appointments_past_item, parent, false);

        return new SlotsVH(itemView);
    }

    class SlotsVH extends RecyclerView.ViewHolder	{
        final AppCompatTextView txtDoctorName;
        final AppCompatImageView imgvwAppointmentOptions;
        final AppCompatTextView txtClinicName;
        final AppCompatTextView txtClinicAddress;
        final AppCompatTextView txtAppointmentStatus;
        final IconicsImageView imgvwStatus;
        final AppCompatTextView txtAppointmentDate;
        final AppCompatTextView txtAppointmentTime;

        SlotsVH (View v) {
            super(v);
            txtDoctorName = (AppCompatTextView) v.findViewById(R.id.txtDoctorName);
            imgvwAppointmentOptions = (AppCompatImageView) v.findViewById(R.id.imgvwAppointmentOptions);
            txtClinicName = (AppCompatTextView) v.findViewById(R.id.txtClinicName);
            txtClinicAddress = (AppCompatTextView) v.findViewById(R.id.txtClinicAddress);
            txtAppointmentStatus = (AppCompatTextView) v.findViewById(R.id.txtAppointmentStatus);
            imgvwStatus = (IconicsImageView) v.findViewById(R.id.imgvwStatus);
            txtAppointmentDate = (AppCompatTextView) v.findViewById(R.id.txtAppointmentDate);
            txtAppointmentTime = (AppCompatTextView) v.findViewById(R.id.txtAppointmentTime);
        }
    }
}