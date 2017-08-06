package biz.zenpets.users.utils.adapters.pet;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.pet.PetData;
import de.hdodenhof.circleimageview.CircleImageView;

public class PetSpinnerAdapter extends ArrayAdapter<PetData> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /** LAYOUT INFLATER TO USE A CUSTOM LAYOUT **/
    private LayoutInflater inflater = null;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    private final ArrayList<PetData> arrPets;

    public PetSpinnerAdapter(@NonNull Activity activity, ArrayList<PetData> arrPets) {
        super(activity, R.layout.pet_row_item);

        /* CAST THE ACTIVITY FROM THE METHOD TO THE LOCAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrPets = arrPets;

        /* INSTANTIATE THE LAYOUT INFLATER */
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrPets.size();
    }

    @Override
    public PetData getItem(int position) {
        return arrPets.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {

        /* A VIEW HOLDER INSTANCE **/
        ViewHolder holder;

        /* CAST THE CONVERT VIEW IN A VIEW INSTANCE **/
        View vi = convertView;

        /* CHECK CONVERT VIEW STATUS **/
        if (convertView == null)	{
            /* CAST THE CONVERT VIEW INTO THE VIEW INSTANCE vi **/
            vi = inflater.inflate(R.layout.pet_row_item, parent, false);

            /* INSTANTIATE THE VIEW HOLDER INSTANCE **/
            holder = new ViewHolder();

            /* CAST THE LAYOUT ELEMENTS */
            holder.imgvwPetProfile = (CircleImageView) vi.findViewById(R.id.imgvwPetProfile);
            holder.txtPetName = (AppCompatTextView) vi.findViewById(R.id.txtPetName);

            /* SET THE TAG TO "vi" **/
            vi.setTag(holder);
        } else {
            /* CAST THE VIEW HOLDER INSTANCE **/
            holder = (ViewHolder) vi.getTag();
        }

        /* SET THE PET NAME **/
        String strPetName = arrPets.get(position).getPetName();
        if (strPetName != null)	{
            holder.txtPetName.setText(strPetName);
        }

        /* SET THE PET PROFILE */
        if (arrPets.get(position).getPetProfile() != null)  {
            Glide.with(activity)
                    .load(arrPets.get(position).getPetProfile())
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .into(holder.imgvwPetProfile);
        } else {
            holder.imgvwPetProfile.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.beagle));
        }

        return vi;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {

        /* A VIEW HOLDER INSTANCE **/
        ViewHolder holder;

        /* CAST THE CONVERT VIEW IN A VIEW INSTANCE **/
        View vi = convertView;

        /* CHECK CONVERT VIEW STATUS **/
        if (convertView == null)	{
            /* CAST THE CONVERT VIEW INTO THE VIEW INSTANCE vi **/
            vi = inflater.inflate(R.layout.pet_row_item, parent, false);

            /* INSTANTIATE THE VIEW HOLDER INSTANCE **/
            holder = new ViewHolder();

            /* CAST THE LAYOUT ELEMENTS */
            holder.imgvwPetProfile = (CircleImageView) vi.findViewById(R.id.imgvwPetProfile);
            holder.txtPetName = (AppCompatTextView) vi.findViewById(R.id.txtPetName);

            /* SET THE TAG TO "vi" **/
            vi.setTag(holder);
        } else {
            /* CAST THE VIEW HOLDER INSTANCE **/
            holder = (ViewHolder) vi.getTag();
        }

        /* SET THE PET NAME **/
        String strPetName = arrPets.get(position).getPetName();
        if (strPetName != null)	{
            holder.txtPetName.setText(strPetName);
        }

        /* SET THE PET PROFILE */
        if (arrPets.get(position).getPetProfile() != null) {
            Glide.with(activity)
                    .load(arrPets.get(position).getPetProfile())
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .centerCrop()
                    .into(holder.imgvwPetProfile);
        }

        return vi;
    }

    private static class ViewHolder	{
        CircleImageView imgvwPetProfile;
        AppCompatTextView txtPetName;
    }
}