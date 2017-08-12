package biz.zenpets.users.utils.adapters.pet;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.details.pet.PetDetails;
import biz.zenpets.users.utils.models.pet.PetData;

public class UserPetsAdapter extends RecyclerView.Adapter<UserPetsAdapter.PetsVH> {

    /** AN ACTIVITY INSTANCE **/
    private final Activity activity;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<PetData> arrPets;

    public UserPetsAdapter(Activity activity, ArrayList<PetData> arrPets) {

        /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrPets = arrPets;
    }

    @Override
    public int getItemCount() {
        return arrPets.size();
    }

    @Override
    public void onBindViewHolder(PetsVH holder, final int position) {
        final PetData data = arrPets.get(position);

        /* SET THE PET'S NAME */
        if (data.getPetName() != null)  {
            holder.txtPetName.setText(data.getPetName());
        }

        /* SET THE PET'S DETAILS (GENDER, BREED AND AGE) */
        if (data.getPetGender() != null  && data.getBreedName() != null && data.getPetDOB() != null)   {
            String gender = data.getPetGender();
            String breed = data.getBreedName();
            String age = data.getPetDOB();
            String combinedDetails = gender + " " + breed + ", aged " + age;
            holder.txtPetAge.setText(combinedDetails);
        }

        /* SET THE PET'S PROFILE PICTURE */
        if (data.getPetProfile() != null)   {
            Glide.with(activity)
                    .load(data.getPetProfile())
                    .placeholder(R.drawable.beagle)
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .into(holder.imgvwPetPicture);
        } else {
            holder.imgvwPetPicture.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.beagle));
        }

        /* SHOW THE PET DETAILS */
        holder.petCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, PetDetails.class);
                intent.putExtra("PET_ID", data.getPetID());
                activity.startActivity(intent);
            }
        });

        /* SHOW THE PET POPUP MENU */
        holder.imgvwPetOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu pm = new PopupMenu(activity, v);
                pm.getMenuInflater().inflate(R.menu.pm_user_pet_item, pm.getMenu());
                pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId())   {
                            case R.id.menuDetails:
                                Intent intent = new Intent(activity, PetDetails.class);
                                intent.putExtra("PET_ID", data.getPetID());
                                activity.startActivity(intent);
                                break;
                            case R.id.menuEdit:
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
    public PetsVH onCreateViewHolder(ViewGroup parent, int i) {

        View itemView = LayoutInflater.
                from(parent.getContext()).
                inflate(R.layout.user_pets_item, parent, false);

        return new PetsVH(itemView);
    }

    class PetsVH extends RecyclerView.ViewHolder	{
        final CardView petCard;
        final AppCompatImageView imgvwPetOptions;
        final AppCompatImageView imgvwPetPicture;
//        final AppCompatTextView txtGender;
        final AppCompatTextView txtPetName;
        final AppCompatTextView txtPetAge;

        PetsVH(View v) {
            super(v);
            petCard = (CardView) v.findViewById(R.id.petCard);
            imgvwPetOptions = (AppCompatImageView) v.findViewById(R.id.imgvwPetOptions);
            imgvwPetPicture = (AppCompatImageView) v.findViewById(R.id.imgvwPetPicture);
//            txtGender = (AppCompatTextView) v.findViewById(R.id.txtGender);
            txtPetName = (AppCompatTextView) v.findViewById(R.id.txtPetName);
            txtPetAge = (AppCompatTextView) v.findViewById(R.id.txtPetAge);
        }

    }
}