package biz.zenpets.users.utils.adapters.pet;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.pet.VaccinationsData;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class VaccinationsAdapter extends RecyclerView.Adapter<VaccinationsAdapter.VaccinationVH> {

    /** AN ACTIVITY INSTANCE **/
    private final Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<VaccinationsData> arrVaccinations;

    public VaccinationsAdapter(Activity activity, ArrayList<VaccinationsData> arrVaccinations) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrVaccinations = arrVaccinations;
    }

    @Override
    public int getItemCount() {
        return arrVaccinations.size();
    }

    @Override
    public void onBindViewHolder(VaccinationVH holder, final int position) {
        final VaccinationsData data = arrVaccinations.get(position);

        /* SET THE VACCINE NAME */
        if (data.getVaccineName() != null)  {
            holder.txtVaccineName.setText(data.getVaccineName());
        }

        /* SET THE VACCINATION DATE */
        if (data.getVaccinationDate() != null)   {
            holder.txtVaccineDate.setText(data.getVaccinationDate());
        }

        /* SET THE VACCINATION NOTES */
        if (data.getVaccinationNotes() != null)  {
            holder.txtVaccineNotes.setText(data.getVaccinationNotes());
        } else {
            holder.txtVaccineNotes.setText("Note not added");
        }

        /* SHOW THE PET POPUP MENU */
        holder.imgvwOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(activity, v);
                pm.getMenuInflater().inflate(R.menu.pm_vaccination_item, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())   {
                            case R.id.menuDelete:
                                String strTitle = "Delete Record";
                                String strMessage = activity.getResources().getString(R.string.vaccination_delete_message);
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
                                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction dialogAction) {
                                                /* GET THE STORAGE REFERENCE */
                                                if (data.getVaccinationPicture() != null)   {
                                                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                                    StorageReference strRefFile = storageReference.getStorage().getReferenceFromUrl(data.getVaccinationPicture());
                                                    strRefFile.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            // String URL_PET_DETAILS = "http://leodyssey.com/ZenPets/public/deleteVaccination";
                                                            String URL_PET_DETAILS = "http://192.168.11.2/zenpets/public/deleteVaccination";
                                                            HttpUrl.Builder builder = HttpUrl.parse(URL_PET_DETAILS).newBuilder();
                                                            builder.addQueryParameter("vaccinationID", data.getVaccinationID());
                                                            String FINAL_URL = builder.build().toString();
                                                            OkHttpClient client = new OkHttpClient();
                                                            Request request = new Request.Builder()
                                                                    .url(FINAL_URL)
                                                                    .build();
                                                            client.newCall(request).enqueue(new Callback() {
                                                                @Override
                                                                public void onFailure(Call call, IOException e) {
//                                                                    Log.e("FAILURE", e.toString());
                                                                }

                                                                @Override
                                                                public void onResponse(Call call, Response response) throws IOException {
                                                                    try {
                                                                        String strResult = response.body().string();
                                                                        JSONObject JORoot = new JSONObject(strResult);
                                                                        if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                                                                            activity.runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    Toast.makeText(activity, "The Vaccination record was successfully deleted", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        } else {
                                                                            activity.runOnUiThread(new Runnable() {
                                                                                @Override
                                                                                public void run() {
                                                                                    Toast.makeText(activity, "There was a problem deleting the record. Please try again.", Toast.LENGTH_SHORT).show();
                                                                                }
                                                                            });
                                                                        }
                                                                    } catch (JSONException e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            /* SHOW THE ERROR */
                                                            Toast.makeText(activity, "The Pet could not be deleted. Please try again", Toast.LENGTH_LONG).show();
                                                        }
                                                    });
                                                }
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
    public VaccinationVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.vaccination_item, parent, false);

        return new VaccinationVH(itemView);
    }

    class VaccinationVH extends RecyclerView.ViewHolder	{
        final AppCompatImageView imgvwOptions;
        final AppCompatTextView txtVaccineName;
        final AppCompatTextView txtVaccineDate;
        final AppCompatTextView txtVaccineNotes;

        VaccinationVH(View v) {
            super(v);
            imgvwOptions = (AppCompatImageView) v.findViewById(R.id.imgvwOptions);
            txtVaccineName = (AppCompatTextView) v.findViewById(R.id.txtVaccineName);
            txtVaccineDate = (AppCompatTextView) v.findViewById(R.id.txtVaccineDate);
            txtVaccineNotes = (AppCompatTextView) v.findViewById(R.id.txtVaccineNotes);
        }

    }
}