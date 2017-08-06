package biz.zenpets.users.utils.adapters.location;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.models.location.CountriesData;

public class CountriesAdapter extends ArrayAdapter<CountriesData> {

    /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
    private final Activity activity;

    /***** LAYOUT INFLATER TO USE A CUSTOM LAYOUT *****/
    private LayoutInflater inflater = null;

    /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
    private final ArrayList<CountriesData> arrCountries;

    public CountriesAdapter(Activity activity, ArrayList<CountriesData> arrCountries) {
        super(activity, R.layout.states_row);

        /* CAST THE ACTIVITY FROM THE METHOD TO THE LOCAL ACTIVITY INSTANCE **/
        this.activity = activity;

        /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE **/
        this.arrCountries = arrCountries;

        /* INSTANTIATE THE LAYOUT INFLATER **/
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return arrCountries.size();
    }

    @Override
    public CountriesData getItem(int position) {
        return arrCountries.get(position);
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
            vi = inflater.inflate(R.layout.country_row, parent, false);

            /* INSTANTIATE THE VIEW HOLDER INSTANCE **/
            holder = new ViewHolder();

            /****	CAST THE LAYOUT ELEMENTS	*****/
            holder.txtCountryName = (AppCompatTextView) vi.findViewById(R.id.txtCountryName);
//            holder.txtCurrencySymbol = (AppCompatTextView) vi.findViewById(R.id.txtCurrencySymbol);
            holder.txtCountryName.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Regular.ttf"));

            /* SET THE TAG TO "vi" **/
            vi.setTag(holder);
        } else {
            /* CAST THE VIEW HOLDER INSTANCE **/
            holder = (ViewHolder) vi.getTag();
        }

        /* SET THE COUNTRY NAME **/
        String strCountryName = arrCountries.get(position).getCountryName();
        if (strCountryName != null)	{
            holder.txtCountryName.setText(strCountryName);
        }

        /* SET THE COUNTRY FLAG **/
        Drawable drawable = arrCountries.get(position).getCountryFlag();
        if (drawable != null)   {
            holder.txtCountryName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }

//        /* SET THE CURRENCY SYMBOL **/
//        String strCurrencySymbol = arrCountries.get(position).getCurrencySymbol();
//        if (strCurrencySymbol != null)  {
//            holder.txtCurrencySymbol.setText(strCurrencySymbol);
//        }

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
            vi = inflater.inflate(R.layout.country_row, parent, false);

            /* INSTANTIATE THE VIEW HOLDER INSTANCE **/
            holder = new ViewHolder();

            /****	CAST THE LAYOUT ELEMENTS	*****/
            holder.txtCountryName = (AppCompatTextView) vi.findViewById(R.id.txtCountryName);
//            holder.txtCurrencySymbol = (AppCompatTextView) vi.findViewById(R.id.txtCurrencySymbol);
            holder.txtCountryName.setTypeface(Typeface.createFromAsset(activity.getAssets(), "fonts/Roboto-Regular.ttf"));

            /* SET THE TAG TO "vi" **/
            vi.setTag(holder);
        } else {
            /* CAST THE VIEW HOLDER INSTANCE **/
            holder = (ViewHolder) vi.getTag();
        }

        /* SET THE COUNTRY NAME **/
        String strCountryName = arrCountries.get(position).getCountryName();
        if (strCountryName != null)	{
            holder.txtCountryName.setText(strCountryName);
        }

        /* SET THE COUNTRY FLAG **/
        Drawable drawable = arrCountries.get(position).getCountryFlag();
        if (drawable != null)   {
            holder.txtCountryName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }

//        /* SET THE CURRENCY SYMBOL **/
//        String strCurrencySymbol = arrCountries.get(position).getCurrencySymbol();
//        if (strCurrencySymbol != null)  {
//            holder.txtCurrencySymbol.setText(strCurrencySymbol);
//        }

        return vi;
    }

    private static class ViewHolder	{
        AppCompatTextView txtCountryName;
//        AppCompatTextView txtCurrencySymbol;
    }
}