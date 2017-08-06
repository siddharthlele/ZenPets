package biz.zenpets.users.utils.adapters.appointment.modifier;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.mikepenz.iconics.view.IconicsImageView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.appointment.user.UpcomingAppointmentsData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UpcomingAppointmentsAdapter extends RecyclerView.Adapter<UpcomingAppointmentsAdapter.SlotsVH> {

    /** AN ACTIVITY INSTANCE **/
    private final Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<UpcomingAppointmentsData> arrAppointments;

    public UpcomingAppointmentsAdapter(Activity activity, ArrayList<UpcomingAppointmentsData> arrAppointments) {

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
    public void onBindViewHolder(final SlotsVH holder, int position) {
        final UpcomingAppointmentsData data = arrAppointments.get(position);

        /* SET THE DOCTOR'S NAME */
        if (data.getDoctorPrefix() != null && data.getDoctorName() != null) {
            holder.txtDoctorName.setText(data.getDoctorPrefix() + " " + data.getDoctorName());
        }

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

        /* SHOW THE APPOINTMENT POPUP MENU */
        holder.imgvwAppointmentOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(activity, v);
                pm.getMenuInflater().inflate(R.menu.pm_user_appointment_item, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())   {
//                            case R.id.menuEdit:
//                                Intent intent = new Intent(activity, AppointmentSlotEditor.class);
//                                intent.putExtra("APPOINTMENT_ID", data.getAppointmentID());
//                                intent.putExtra("CLINIC_ID", data.getClinicID());
//                                intent.putExtra("DOCTOR_ID", data.getDoctorID());
//                                activity.startActivity(intent);
//                                break;
                            case R.id.menuDelete:
                                String strTitle = "Delete Record";
                                String strMessage = activity.getResources().getString(R.string.appointment_delete_prompt);
                                String strYes = activity.getResources().getString(R.string.generic_mb_yes);
                                String strNo = activity.getResources().getString(R.string.generic_mb_no);

                                new MaterialDialog.Builder(activity)
                                        .icon(ContextCompat.getDrawable(activity, R.drawable.ic_info_outline_black_24dp))
                                        .title(strTitle)
                                        .cancelable(true)
                                        .content(strMessage)
                                        .positiveText(strYes)
                                        .negativeText(strNo)
                                        .theme(Theme.LIGHT)
                                        .typeface("RobotoCondensed-Regular.ttf", "Roboto-Regular.ttf")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
                                                deleteAppointment(data.getAppointmentID(), holder.getAdapterPosition());
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
                                                dialog.dismiss();
                                            }
                                        }).show();
                                break;
                            default:
                                break;
                        }
                        return false;
                    }
                });
                pm.show();
            }
        });
        
        /* SHOW THE DIRECTIONS */
        holder.linlaDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGoogleMapsInstalled())    {

                    String strDestination =
                            "&daddr=" + Double.toString(data.getClinicLatitude()) + "," + Double.toString(data.getClinicLongitude());

                    Intent intent = new Intent(
                            android.content.Intent.ACTION_VIEW,
                            Uri.parse("http://maps.google.com/maps?" + /*strOrigin +*/ strDestination + "&dirflg=d"));

                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK & Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
                    activity.startActivity(intent);
                } else {
                    new MaterialDialog.Builder(activity)
                            .title("Google Maps Required")
                            .content("Google Maps is necessary to show you the directions to the Clinic. Please install Google Maps to use this feature")
                            .positiveText("OKAY")
                            .theme(Theme.LIGHT)
                            .icon(ContextCompat.getDrawable(activity, R.drawable.ic_info_outline_black_24dp))
                            .typeface("RobotoCondensed-Regular.ttf", "Roboto-Regular.ttf")
                            .show();
                }
            }
        });

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
                inflate(R.layout.user_appointments_upcoming_item, parent, false);

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
        final LinearLayout linlaDirections;
        final IconicsImageView imgvwDirections;

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
            linlaDirections = (LinearLayout) v.findViewById(R.id.linlaDirections);
            imgvwDirections = (IconicsImageView) v.findViewById(R.id.imgvwDirections);
        }
    }

    /** CHECK IF GOOGLE MAPS IS INSTALLED **/
    @SuppressWarnings("unused")
    private boolean isGoogleMapsInstalled() {
        try {
            ApplicationInfo info = activity.getPackageManager().getApplicationInfo("com.google.android.apps.maps", 0 );
            return true;
        } catch(PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** DELETE THE USER'S APPOINTMENT **/
    private void deleteAppointment(String appointmentID, final int position) {
        // String URL_DELETE_APPOINTMENT = "http://leodyssey.com/ZenPets/public/deleteAppointment";
        String URL_DELETE_APPOINTMENT = "http://192.168.11.2/zenpets/public/deleteAppointment";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("appointmentID", appointmentID)
                .build();
        Request request = new Request.Builder()
                .url(URL_DELETE_APPOINTMENT)
                .post(body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FAILURE", e.toString());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String strResult = response.body().string();
//                    Log.e("RESULT", strResult);
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                arrAppointments.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeRemoved(position, arrAppointments.size());
                                Toast.makeText(activity, "Your appointment was successfully deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(activity, "There was a problem deleting the appointment. Please try again.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}