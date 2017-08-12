package biz.zenpets.users.doctors.manual;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.models.location.CountriesData;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CountrySelectorActivity extends AppCompatActivity {

    /** THE COUNTRIES ADAPTER, ARRAY LIST AND FILTERED ARRAY LIST **/
    private CountriesSelectorAdapter adapter;
    private ArrayList<CountriesData> arrCountries = new ArrayList<>();
    private ArrayList<CountriesData> arrFilteredResults = new ArrayList<>();

    /** AN IMAGE LOADER INSTANCE **/
    private ImageLoader imageLoader;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listCountries) RecyclerView listCountries;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.country_selector_list);
        ButterKnife.bind(this);

        /** CONFIGURE THE ACTIONBAR **/
        configAB();
        
        /* INSTANTIATE THE LOCATION SEARCH ADAPTER */
        adapter = new CountriesSelectorAdapter(arrFilteredResults);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();
        
        /* FETCH THE LIST OF COUNTRIES */
        new fetchCountries().execute();
    }

    /***** FETCH THE LIST OF COUNTRIES *****/
    private class fetchCountries extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);

            /* INSTANTIATE THE IMAGE LOADER INSTANCE */
            imageLoader = ImageLoader.getInstance();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String COUNTRY_URL = "http://leodyssey.com/ZenPets/public/allCountries";
            String COUNTRY_URL = "http://192.168.11.2/zenpets/public/allCountries";
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(COUNTRY_URL)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);

                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JACountries = JORoot.getJSONArray("countries");

                    /* A COUNTRY DATA POJO INSTANCE */
                    CountriesData data;

                    for (int i = 0; i < JACountries.length(); i++) {
                        JSONObject JOCountries = JACountries.getJSONObject(i);
//                        Log.e("COUNTRIES", String.valueOf(JOCountries));

                        /* INSTANTIATE THE COUNTRY DATA POJO INSTANCE */
                        data = new CountriesData();

                        /* GET THE COUNTRY ID */
                        if (JOCountries.has("countryID"))    {
                            String countryID = JOCountries.getString("countryID");
                            data.setCountryID(countryID);
                        } else {
                            data.setCountryID(null);
                        }

                        /* GET THE COUNTRY NAME */
                        if (JOCountries.has("countryName")) {
                            String countryName = JOCountries.getString("countryName");
                            data.setCountryName(countryName);
                        } else {
                            data.setCountryName(null);
                        }

                        /* GET THE CURRENCY NAME */
                        if (JOCountries.has("currencyName"))    {
                            String currencyName = JOCountries.getString("currencyName");
                            data.setCurrencyName(currencyName);
                        } else {
                            data.setCurrencyName(null);
                        }

                        /* GET THE CURRENCY CODE */
                        if (JOCountries.has("currencyCode"))    {
                            String currencyCode = JOCountries.getString("currencyCode");
                            data.setCurrencyCode(currencyCode);
                        } else {
                            data.setCurrencyCode(null);
                        }

                        /* GET THE COUNTRY FLAG */
                        if (JOCountries.has("countryFlag")) {
                            String countryFlag = JOCountries.getString("countryFlag");
                            Bitmap bmpCountryFlag = imageLoader.loadImageSync(countryFlag);
                            Drawable drawable = new BitmapDrawable(getResources(), bmpCountryFlag);
                            data.setCountryFlag(drawable);
                        } else {
                            data.setCountryFlag(null);
                        }

                        /* ADD THE COLLECTED INFO TO THE ARRAYLIST */
                        arrCountries.add(data);
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* CAST THE PRIMARY ARRAY DATA TO THE FILTERED ARRAY */
            arrFilteredResults.addAll(arrCountries);

            /* INSTANTIATE THE LOCATION SEARCH ADAPTER */
            adapter = new CountriesSelectorAdapter(arrFilteredResults);

            /* SET THE ADAPTER TO THE COUNTRIES RECYCLER VIEW */
            listCountries.setAdapter(adapter);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** THE COUNTRIES ADAPTER *****/
    private class CountriesSelectorAdapter extends RecyclerView.Adapter<CountriesSelectorAdapter.CountriesVH> implements Filterable {

        /** ARRAY LIST TO GET DATA FROM THE ACTIVITY **/
        private ArrayList<CountriesData> mArrayList;
        private ArrayList<CountriesData> mFilteredList;

        CountriesSelectorAdapter(ArrayList<CountriesData> arrCountries) {

            /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
            this.mArrayList = arrCountries;
            this.mFilteredList = arrCountries;
        }

        @Override
        public int getItemCount() {
            return mFilteredList.size();
        }

        @Override
        public void onBindViewHolder(CountriesVH holder, final int position) {
            final CountriesData data = mFilteredList.get(position);
            
            /* SET THE COUNTRY NAME **/
            String strCountryName = data.getCountryName();
            if (strCountryName != null)	{
                holder.txtCountryName.setText(strCountryName);
            }
            
            /* SET THE COUNTRY FLAG **/
            Drawable drawable = data.getCountryFlag();
            if (drawable != null)   {
                holder.txtCountryName.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
            }

            /** SHOW THE STATE SELECTOR ACTIVITY **/
            holder.txtCountryName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(CountrySelectorActivity.this, StateSelectorActivity.class);
                    intent.putExtra("COUNTRY_ID", data.getCountryID());
                    startActivityForResult(intent, 102);
                }
            });
        }

        @Override
        public CountriesVH onCreateViewHolder(ViewGroup parent, int i) {

            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.country_selector_item, parent, false);

            return new CountriesVH(itemView);
        }

        class CountriesVH extends RecyclerView.ViewHolder {
            TextView txtCountryName;

            CountriesVH(View v) {
                super(v);
                txtCountryName = (TextView) v.findViewById(R.id.txtCountryName);
            }
        }

        @Override
        public Filter getFilter() {
            return new Filter() {

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    String charString = constraint.toString();

                    if (charString.isEmpty()) {
                        mFilteredList = mArrayList;
                    } else {
                        ArrayList<CountriesData> filteredList = new ArrayList<>();
                        for (CountriesData data : mArrayList) {
                            if (data.getCountryName().toLowerCase().contains(charString)) {
                                filteredList.add(data);
                            }
                        }
                        mFilteredList = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = mFilteredList;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    //noinspection unchecked
                    mFilteredList = (ArrayList<CountriesData>) results.values;
                    notifyDataSetChanged();
                }
            };
        }
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Select Country";
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getApplicationContext()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(s);
        getSupportActionBar().setSubtitle(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_city_selector, menu);
        MenuItem search = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(search);
        search(searchView);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    private void search(SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listCountries.setLayoutManager(manager);
        listCountries.setHasFixedSize(true);
        listCountries.setAdapter(adapter);
    }
}