package biz.zenpets.users.utils.adapters.appointment.modifier;

import android.app.Activity;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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

public class NewUpcomingAppointmentsAdapter extends RecyclerView.Adapter<NewUpcomingAppointmentsAdapter.SlotsVH> {

    /** AN ACTIVITY INSTANCE **/
    private final Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<UpcomingAppointmentsData> arrAppointments;

    public NewUpcomingAppointmentsAdapter(Activity activity, ArrayList<UpcomingAppointmentsData> arrAppointments) {

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

        /* SET THE CLINIC CITY AND LOCALITY */
        if (data.getCityName() != null && data.getLocalityName() != null)   {
            holder.txtAppointmentLocation.setText(data.getLocalityName() + ", " + data.getCityName());
        }

        /* SET THE DISTANCE TO THE CLINIC */
        if (data.getDistanceToClinic() != null) {
            holder.txtAppointmentDistance.setText(data.getDistanceToClinic());
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
    }

    @Override
    public SlotsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.user_appointments_upcoming_item_new, parent, false);

        return new SlotsVH(itemView);
    }

    class SlotsVH extends RecyclerView.ViewHolder	{
        AppCompatTextView txtAppointmentDate;
        AppCompatTextView txtAppointmentTime;
        AppCompatTextView txtDoctorName;
        AppCompatTextView txtClinicName;
        AppCompatTextView txtAppointmentLocation;
        AppCompatTextView txtAppointmentDistance;
        IconicsImageView imgvwAppointmentOptions;

        SlotsVH (View v) {
            super(v);
            txtAppointmentDate = (AppCompatTextView) v.findViewById(R.id.txtAppointmentDate);
            txtAppointmentTime = (AppCompatTextView) v.findViewById(R.id.txtAppointmentTime);
            txtDoctorName = (AppCompatTextView) v.findViewById(R.id.txtDoctorName);
            txtClinicName = (AppCompatTextView) v.findViewById(R.id.txtClinicName);
            txtAppointmentLocation = (AppCompatTextView) v.findViewById(R.id.txtAppointmentLocation);
            txtAppointmentDistance = (AppCompatTextView) v.findViewById(R.id.txtAppointmentDistance);
            imgvwAppointmentOptions = (IconicsImageView) v.findViewById(R.id.imgvwAppointmentOptions);
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