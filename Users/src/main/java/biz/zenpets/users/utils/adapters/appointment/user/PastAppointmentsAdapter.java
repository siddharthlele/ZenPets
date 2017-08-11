package biz.zenpets.users.utils.adapters.appointment.user;

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
import biz.zenpets.users.details.appointment.AppointmentDetails;
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

        /* SET THE APPOINTMENT DATE */
        if (data.getAppointmentDate() != null)  {
            holder.txtAppointmentDate.setText(/*"Appointment on " + */data.getAppointmentDate());
        }

        /* SET THE APPOINTMENT TIME */
        if (data.getAppointmentTime() != null)  {
            holder.txtAppointmentTime.setText(/*"Appointment at " + */data.getAppointmentTime());
        }

        /* SET THE DOCTOR'S NAME */
        if (data.getDoctorPrefix() != null && data.getDoctorName() != null) {
            holder.txtDoctorName.setText(data.getDoctorPrefix() + " " + data.getDoctorName());
        }

        /* SET THE CLINIC NAME */
        if (data.getClinicName() != null)   {
            holder.txtClinicName.setText(data.getClinicName());
        }

        /* SET THE VISIT REASON */
        if (data.getVisitReason() != null)  {
            holder.txtVisitReason.setText("Visiting for \"" + data.getVisitReason() + "\"");
        }

        /* SET THE CLINIC CITY AND LOCALITY */
        if (data.getCityName() != null && data.getLocalityName() != null)   {
            holder.txtAppointmentLocation.setText(data.getLocalityName() + ", " + data.getCityName());
        }

        /* SET THE DISTANCE TO THE CLINIC */
        if (data.getDistanceToClinic() != null) {
            holder.txtAppointmentDistance.setText(data.getDistanceToClinic());
        }

        /* SHOW THE APPOINTMENT DETAILS */
        holder.linlaAppointment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AppointmentDetails.class);
                intent.putExtra("APPOINTMENT_ID", data.getAppointmentID());
                activity.startActivity(intent);
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
        LinearLayout linlaAppointment;
        AppCompatTextView txtAppointmentDate;
        AppCompatTextView txtAppointmentTime;
        AppCompatTextView txtDoctorName;
        AppCompatTextView txtClinicName;
        AppCompatTextView txtVisitReason;
        AppCompatTextView txtAppointmentLocation;
        AppCompatTextView txtAppointmentDistance;

        SlotsVH (View v) {
            super(v);
            linlaAppointment = (LinearLayout) v.findViewById(R.id.linlaAppointment);
            txtAppointmentDate = (AppCompatTextView) v.findViewById(R.id.txtAppointmentDate);
            txtAppointmentTime = (AppCompatTextView) v.findViewById(R.id.txtAppointmentTime);
            txtDoctorName = (AppCompatTextView) v.findViewById(R.id.txtDoctorName);
            txtClinicName = (AppCompatTextView) v.findViewById(R.id.txtClinicName);
            txtVisitReason = (AppCompatTextView) v.findViewById(R.id.txtVisitReason);
            txtAppointmentLocation = (AppCompatTextView) v.findViewById(R.id.txtAppointmentLocation);
            txtAppointmentDistance = (AppCompatTextView) v.findViewById(R.id.txtAppointmentDistance);
        }
    }
}