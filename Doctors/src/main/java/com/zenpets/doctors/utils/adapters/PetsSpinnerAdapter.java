package com.zenpets.doctors.utils.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.models.PetsData;

import java.util.ArrayList;

public class PetsSpinnerAdapter extends ArrayAdapter<PetsData> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    Activity activity;

    /** LAYOUT INFLATER TO USE A CUSTOM LAYOUT **/
    LayoutInflater inflater = null;

    /** ARRAYLIST TO GET DATA FROM THE ACTIVITY **/
    ArrayList<PetsData> arrPets;

    public PetsSpinnerAdapter(@NonNull Activity activity, ArrayList<PetsData> arrPets) {
        super(activity, R.layout.visit_reasons_item);

        /* CAST THE ACTIVITY FROM THE METHOD TO THE LOCAL ACTIVITY INSTANCE */
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAYLIST IN THE METHOD TO THE LOCAL INSTANCE */
        this.arrPets= arrPets;

        /* INSTANTIATE THE LAYOUT INFLATER */
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrPets.size();
    }

    @Override
    public PetsData getItem(int position) {
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
            vi = inflater.inflate(R.layout.visit_reasons_item, parent, false);

            /* INSTANTIATE THE VIEW HOLDER INSTANCE **/
            holder = new ViewHolder();

            /* CAST THE LAYOUT ELEMENTS */
            holder.txtVisitReason = (AppCompatTextView) vi.findViewById(R.id.txtVisitReason);

            /* SET THE TAG TO "vi" **/
            vi.setTag(holder);
        } else {
            /* CAST THE VIEW HOLDER INSTANCE **/
            holder = (ViewHolder) vi.getTag();
        }

        /* SET THE PET'S NAME **/
        String strPetName = arrPets.get(position).getPetName();
        if (strPetName != null)	{
            holder.txtVisitReason.setText(strPetName);
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
            vi = inflater.inflate(R.layout.visit_reasons_item, parent, false);

            /* INSTANTIATE THE VIEW HOLDER INSTANCE **/
            holder = new ViewHolder();

            /*	CAST THE LAYOUT ELEMENTS	*/
            holder.txtVisitReason = (AppCompatTextView) vi.findViewById(R.id.txtVisitReason);

            /* SET THE TAG TO "vi" **/
            vi.setTag(holder);
        } else {
            /* CAST THE VIEW HOLDER INSTANCE **/
            holder = (ViewHolder) vi.getTag();
        }

        /* SET THE PET'S NAME **/
        String strPetName = arrPets.get(position).getPetName();
        if (strPetName != null)	{
            holder.txtVisitReason.setText(strPetName);
        }

        return vi;
    }

    private static class ViewHolder	{
        AppCompatTextView txtVisitReason;
    }
}