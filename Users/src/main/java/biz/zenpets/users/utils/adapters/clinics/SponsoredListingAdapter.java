package biz.zenpets.users.utils.adapters.clinics;

import android.app.Activity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.doctors.DoctorsData;

public class SponsoredListingAdapter extends RecyclerView.Adapter<SponsoredListingAdapter.ClinicsVH> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
    private final ArrayList<DoctorsData> arrDoctors;

    public SponsoredListingAdapter(Activity activity, ArrayList<DoctorsData> arrDoctors) {

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
    public void onBindViewHolder(ClinicsVH holder, final int position) {
        final DoctorsData data = arrDoctors.get(position);

        /* SET THE CLINIC NAME */
        holder.txtClinicName.setText(data.getClinicName());

        /* SET THE CLINIC CITY AND LOCALITY */
        holder.txtClinicAddress.setText(data.getCityName() + ", " + data.getLocalityName());

        /* SET THE CLINIC DISTANCE FROM THE USER'S CURRENT LOCATION */
        holder.txtClinicDistance.setText(data.getClinicDistance() + " km");

        /* SET THE DOCTOR'S CHARGES */
        holder.txtClinicCharges.setText(data.getDoctorCharges());
    }

    @Override
    public ClinicsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.sponsored_clinics_item, parent, false);

        return new ClinicsVH(itemView);
    }

    class ClinicsVH extends RecyclerView.ViewHolder	{

        final LinearLayout linlaClinicContainer;
        final AppCompatTextView txtClinicName;
        final AppCompatTextView txtClinicAddress;
        final LinearLayout linlaClinicCharges;
        final AppCompatTextView txtClinicCharges;
        final LinearLayout linlaClinicReviews;
        final AppCompatTextView txtClinicReviews;
        final LinearLayout linlaClinicDistance;
        final AppCompatTextView txtClinicDistance;

        ClinicsVH(View v) {
            super(v);

            linlaClinicContainer = (LinearLayout) v.findViewById(R.id.linlaClinicContainer);
            txtClinicName = (AppCompatTextView) v.findViewById(R.id.txtClinicName);
            txtClinicAddress = (AppCompatTextView) v.findViewById(R.id.txtClinicAddress);
            linlaClinicCharges = (LinearLayout) v.findViewById(R.id.linlaClinicCharges);
            txtClinicCharges = (AppCompatTextView) v.findViewById(R.id.txtClinicCharges);
            linlaClinicReviews = (LinearLayout) v.findViewById(R.id.linlaClinicReviews);
            txtClinicReviews = (AppCompatTextView) v.findViewById(R.id.txtClinicReviews);
            linlaClinicDistance = (LinearLayout) v.findViewById(R.id.linlaClinicDistance);
            txtClinicDistance = (AppCompatTextView) v.findViewById(R.id.txtClinicDistance);
        }
    }
}