package biz.zenpets.users.utils.adapters.adoptions;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.details.adoption.AdoptionDetails;
import biz.zenpets.users.utils.models.adoptions.AdoptionsData;
import biz.zenpets.users.utils.models.adoptions.AdoptionsImageData;

public class AdoptionsAdapter extends RecyclerView.Adapter<AdoptionsAdapter.AdoptionsVH> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
    private final ArrayList<AdoptionsData> arrAdoptions;

    public AdoptionsAdapter(Activity activity, ArrayList<AdoptionsData> arrAdoptions) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrAdoptions = arrAdoptions;
    }

    @Override
    public int getItemCount() {
        return arrAdoptions.size();
    }

    @Override
    public void onBindViewHolder(AdoptionsVH holder, final int position) {
        final AdoptionsData data = arrAdoptions.get(position);

        /* SET THE ADOPTION NAME */
        if (data.getAdoptionName() != null) {
            holder.txtAdoptionName.setText(data.getAdoptionName());
        } else {
            holder.txtAdoptionName.setText("Unnamed");
        }

        /* SET THE DESCRIPTION */
        if (data.getAdoptionDescription() != null)  {
            holder.txtAdoptionDescription.setText(data.getAdoptionDescription());
        }

        /* SET THE TIMESTAMP (DATE OF CREATION )*/
        if (data.getAdoptionTimeStamp() != null)    {
            holder.txtTimeStamp.setText("Posted " + data.getAdoptionTimeStamp());
        }

        /* SET THE PET'S GENDER */
        if (data.getAdoptionGender().equalsIgnoreCase("male"))  {
            holder.txtGender.setText(activity.getResources().getString(R.string.gender_male));
            holder.txtGender.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_blue_dark));
        } else if (data.getAdoptionGender().equalsIgnoreCase("female")) {
            holder.txtGender.setText(activity.getResources().getString(R.string.gender_female));
            holder.txtGender.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark));
        }

        /* SET THE VACCINATED STATUS */
        if (data.getAdoptionVaccination() != null)  {
            holder.txtVaccinated.setText(data.getAdoptionVaccination());
            if (data.getAdoptionVaccination().equalsIgnoreCase("yes"))  {
                holder.txtVaccinated.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_green_dark));
            } else if (data.getAdoptionVaccination().equalsIgnoreCase("no"))    {
                holder.txtVaccinated.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark));
            }
        }

        /* SET THE DEWORMED STATUS */
        if (data.getAdoptionDewormed() != null) {
            holder.txtDewormed.setText(data.getAdoptionDewormed());
            if (data.getAdoptionDewormed().equalsIgnoreCase("yes")) {
                holder.txtDewormed.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_green_dark));
            } else if (data.getAdoptionDewormed().equalsIgnoreCase("no"))   {
                holder.txtDewormed.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark));
            }
        }

        /* SET THE NEUTERED STATUS */
        if (data.getAdoptionNeutered() != null) {
            holder.txtNeutered.setText(data.getAdoptionNeutered());
            if (data.getAdoptionNeutered().equalsIgnoreCase("yes")) {
                holder.txtNeutered.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_green_dark));
            } else if (data.getAdoptionNeutered().equalsIgnoreCase("no"))   {
                holder.txtNeutered.setTextColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark));
            }
        }

        /* SHOW THE ADOPTION DETAILS */
        holder.linlaAdoptionContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, AdoptionDetails.class);
                intent.putExtra("ADOPTION_ID", data.getAdoptionID());
                activity.startActivity(intent);
            }
        });

        /* SET THE IMAGES */
        ArrayList<AdoptionsImageData> arrImages = data.getImages();

        if (arrImages.size() > 0)   {
            /* CONFIGURE THE RECYCLER VIEW */
            LinearLayoutManager llmAppointments = new LinearLayoutManager(activity);
            llmAppointments.setOrientation(LinearLayoutManager.HORIZONTAL);
            llmAppointments.setAutoMeasureEnabled(true);
            holder.listAdoptionImages.setLayoutManager(llmAppointments);
            holder.listAdoptionImages.setHasFixedSize(true);
            holder.listAdoptionImages.setNestedScrollingEnabled(false);

            /* CONFIGURE THE ADAPTER */
            AdoptionsImagesAdapter adapter = new AdoptionsImagesAdapter(activity, arrImages);

            /* SET THE ADAPTER TO THE RECYCLER VIEW */
            holder.listAdoptionImages.setAdapter(adapter);

            /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY TEXT */
            holder.listAdoptionImages.setVisibility(View.VISIBLE);
            holder.txtNoImages.setVisibility(View.GONE);
        } else {
            /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY TEXT */
            holder.listAdoptionImages.setVisibility(View.GONE);
            holder.txtNoImages.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public AdoptionsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.user_adoptions_item, parent, false);

        return new AdoptionsVH(itemView);
    }

    class AdoptionsVH extends RecyclerView.ViewHolder	{

        final LinearLayout linlaAdoptionContainer;
        final AppCompatTextView txtAdoptionName;
        final AppCompatTextView txtGender;
        final AppCompatTextView txtAdoptionDescription;
        final AppCompatTextView txtNoImages;
        final RecyclerView listAdoptionImages;
        final AppCompatTextView txtTimeStamp;
        final AppCompatTextView txtVaccinated;
        final AppCompatTextView txtDewormed;
        final AppCompatTextView txtNeutered;

        AdoptionsVH(View v) {
            super(v);

            linlaAdoptionContainer = (LinearLayout) v.findViewById(R.id.linlaAdoptionContainer);
            txtAdoptionName = (AppCompatTextView) v.findViewById(R.id.txtAdoptionName);
            txtGender = (AppCompatTextView) v.findViewById(R.id.txtGender);
            txtAdoptionDescription = (AppCompatTextView) v.findViewById(R.id.txtAdoptionDescription);
            txtNoImages = (AppCompatTextView) v.findViewById(R.id.txtNoImages);
            listAdoptionImages = (RecyclerView) v.findViewById(R.id.listAdoptionImages);
            txtTimeStamp = (AppCompatTextView) v.findViewById(R.id.txtTimeStamp);
            txtVaccinated = (AppCompatTextView) v.findViewById(R.id.txtVaccinated);
            txtDewormed = (AppCompatTextView) v.findViewById(R.id.txtDewormed);
            txtNeutered = (AppCompatTextView) v.findViewById(R.id.txtNeutered);
        }
    }
}