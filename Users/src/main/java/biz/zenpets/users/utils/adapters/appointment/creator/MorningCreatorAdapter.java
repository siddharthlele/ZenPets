package biz.zenpets.users.utils.adapters.appointment.creator;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.creators.appointment.AppointmentDetailsCreator;
import biz.zenpets.users.utils.models.appointment.slots.MorningTimeSlotsData;

public class MorningCreatorAdapter extends RecyclerView.Adapter<MorningCreatorAdapter.SlotsVH> {

    /** AN ACTIVITY INSTANCE **/
    private final Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<MorningTimeSlotsData> arrSlots;

    public MorningCreatorAdapter(Activity activity, ArrayList<MorningTimeSlotsData> arrSlots) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrSlots = arrSlots;
    }

    @Override
    public int getItemCount() {
        return arrSlots.size();
    }

    @Override
    public void onBindViewHolder(SlotsVH holder, final int position) {
        final MorningTimeSlotsData data = arrSlots.get(position);

        /* SET THE APPOINTMENT TIME */
        if (data.getAppointmentTime() != null)   {
            holder.txtTimeSlot.setText(data.getAppointmentTime());
        }

        /* SET THE APPOINTMENT STATUS */
        if (data.getAppointmentStatus() != null)    {
            if (data.getAppointmentStatus().equalsIgnoreCase("Available"))  {
                holder.txtTimeSlot.setTextColor(ContextCompat.getColor(activity, R.color.primary));
            } else if (data.getAppointmentStatus().equalsIgnoreCase("Unavailable"))  {
                holder.txtTimeSlot.setTextColor(ContextCompat.getColor(activity, android.R.color.darker_gray));
            }
        } else {
            holder.txtTimeSlot.setTextColor(ContextCompat.getColor(activity, android.R.color.darker_gray));
        }

        /* PASS THE DATA TO THE DETAILS ACTIVITY */
        if (data.getAppointmentStatus().equalsIgnoreCase("Available"))  {
            holder.txtTimeSlot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, AppointmentDetailsCreator.class);
                    intent.putExtra("DOCTOR_ID", data.getDoctorID());
                    intent.putExtra("CLINIC_ID", data.getClinicID());
                    intent.putExtra("APPOINTMENT_TIME", data.getAppointmentTime());
                    intent.putExtra("APPOINTMENT_DATE", data.getAppointmentDate());
                    activity.startActivityForResult(intent, 101);
                }
            });
        }
    }

    @Override
    public SlotsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.time_slot_item, parent, false);

        return new SlotsVH(itemView);
    }

    class SlotsVH extends RecyclerView.ViewHolder	{
        final AppCompatTextView txtTimeSlot;

        SlotsVH (View v) {
            super(v);
            txtTimeSlot = (AppCompatTextView) v.findViewById(R.id.txtTimeSlot);
        }

    }
}