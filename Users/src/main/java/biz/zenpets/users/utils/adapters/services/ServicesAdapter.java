package biz.zenpets.users.utils.adapters.services;

import android.app.Activity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.services.ServicesData;

public class ServicesAdapter extends RecyclerView.Adapter<ServicesAdapter.ServicesVH> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
    private final ArrayList<ServicesData> arrServices;

    public ServicesAdapter(Activity activity, ArrayList<ServicesData> arrServices) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrServices = arrServices;
    }

    @Override
    public int getItemCount() {
        return arrServices.size();
    }

    @Override
    public void onBindViewHolder(ServicesVH holder, final int position) {
        ServicesData data = arrServices.get(position);

        /** SET THE SERVICE NAME **/
        if (data.getServiceName() != null)  {
            holder.txtDoctorService.setText(data.getServiceName());
        }
    }

    @Override
    public ServicesVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.doctors_details_services_item, parent, false);

        return new ServicesVH(itemView);
    }

    class ServicesVH extends RecyclerView.ViewHolder	{
        final AppCompatTextView txtDoctorService;

        ServicesVH(View v) {
            super(v);
            txtDoctorService = (AppCompatTextView) v.findViewById(R.id.txtDoctorService);
        }
    }
}