package biz.zenpets.users.utils.adapters.location;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.location.StatesData;

public class StatesAdapter extends ArrayAdapter<StatesData> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /***** LAYOUT INFLATER TO USE A CUSTOM LAYOUT *****/
    private LayoutInflater inflater = null;

    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
    private final ArrayList<StatesData> arrStates;

    public StatesAdapter(Activity activity, ArrayList<StatesData> arrStates) {
        super(activity, R.layout.states_row);

        /* CAST THE ACTIVITY FROM THE METHOD TO THE LOCAL ACTIVITY INSTANCE **/
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE **/
        this.arrStates = arrStates;

        /* INSTANTIATE THE LAYOUT INFLATER **/
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrStates.size();
    }

    @Override
    public StatesData getItem(int position) {
        return arrStates.get(position);
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
            vi = inflater.inflate(R.layout.states_dropdown_row, parent, false);

            /* INSTANTIATE THE VIEW HOLDER INSTANCE **/
            holder = new ViewHolder();

            /****	CAST THE LAYOUT ELEMENTS	*****/
            holder.txtStateName = (AppCompatTextView) vi.findViewById(R.id.txtStateName);
            holder.txtStateName.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Regular.ttf"));

            /* SET THE TAG TO "vi" **/
            vi.setTag(holder);
        } else {
            /* CAST THE VIEW HOLDER INSTANCE **/
            holder = (ViewHolder) vi.getTag();
        }

        /* SET THE STATE NAME **/
        String strStateName = arrStates.get(position).getStateName();
        if (strStateName != null)	{
            holder.txtStateName.setText(strStateName);
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
            vi = inflater.inflate(R.layout.states_row, parent, false);

            /* INSTANTIATE THE VIEW HOLDER INSTANCE **/
            holder = new ViewHolder();

            /****	CAST THE LAYOUT ELEMENTS	*****/
            holder.txtStateName = (AppCompatTextView) vi.findViewById(R.id.txtStateName);
            holder.txtStateName.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Regular.ttf"));

            /* SET THE TAG TO "vi" **/
            vi.setTag(holder);
        } else {
            /* CAST THE VIEW HOLDER INSTANCE **/
            holder = (ViewHolder) vi.getTag();
        }

        /* SET THE STATE NAME **/
        String strStateName = arrStates.get(position).getStateName();
        if (strStateName != null)	{
            holder.txtStateName.setText(strStateName);
        }

        return vi;
    }

    private static class ViewHolder	{
        AppCompatTextView txtStateName;
    }
}