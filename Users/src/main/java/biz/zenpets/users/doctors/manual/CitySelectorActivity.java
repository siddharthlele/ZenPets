package biz.zenpets.users.doctors.manual;

import android.content.Intent;
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
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.models.location.LocationSearchData;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CitySelectorActivity extends AppCompatActivity {

    /** THE INCOMING CITY NAME **/
    private String CITY_NAME = null;

    /** THE LOCALITIES ADAPTER, ARRAY LIST AND FILTERED ARRAY LIST **/
    private LocationSearchAdapter searchAdapter;
    private ArrayList<LocationSearchData> arrLocalities = new ArrayList<>();
    private ArrayList<LocationSearchData> arrFilteredResults = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listLocalities) RecyclerView listLocalities;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** THE SEARCH VIEW INSTANCE **/
    SearchView searchView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.city_selector_list);
        ButterKnife.bind(this);

        /** CONFIGURE THE ACTIONBAR **/
        configAB();
        
        /* INSTANTIATE THE LOCATION SEARCH ADAPTER */
        searchAdapter = new LocationSearchAdapter(arrFilteredResults);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* GET THE INCOMING DATA */
        getIncomingData();
    }

    /***** FETCH THE LIST OF LOCALITIES *****/
    private class fetchLocalities extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_LOCALITIES_LIST = "http://leodyssey.com/ZenPets/public/fetchLocalitiesByCity";
            String URL_LOCALITIES_LIST = "http://192.168.11.2/zenpets/public/fetchLocalitiesByCity";
            HttpUrl.Builder builder = HttpUrl.parse(URL_LOCALITIES_LIST).newBuilder();
            builder.addQueryParameter("cityName", CITY_NAME);
            String FINAL_URL = builder.build().toString();
//            Log.e("URL", FINAL_URL);
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(FINAL_URL)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JALocalities = JORoot.getJSONArray("localities");

                    /* A LOCATION SEARCH DATA INSTANCE */
                    LocationSearchData data;

                    for (int i = 0; i < JALocalities.length(); i++) {
                        JSONObject JOLocalities = JALocalities.getJSONObject(i);

                        /* INSTANTIATE THE LOCATION SEARCH DATA INSTANCE */
                        data = new LocationSearchData();

                        /* GET THE LOCALITY ID */
                        if (JOLocalities.has("localityID")) {
                            data.setLocalityID(JOLocalities.getString("localityID"));
                        } else {
                            data.setLocalityID(null);
                        }

                        /* GET THE CITY ID */
                        if (JOLocalities.has("cityID")) {
                            data.setCityID(JOLocalities.getString("cityID"));
                        } else {
                            data.setCityID(null);
                        }

                        /* SET THE LOCALITY NAME */
                        if (JOLocalities.has("localityName"))   {
                            data.setLocalityName(JOLocalities.getString("localityName"));
                        } else {
                            data.setLocalityName(null);
                        }

                        /* ADD THE GATHERED DATA TO THE ARRAY LIST */
                        arrLocalities.add(data);
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
            arrFilteredResults.addAll(arrLocalities);
//            Log.e("FILTERED SIZE", String.valueOf(arrFilteredResults.size()));

            /* INSTANTIATE THE LOCATION SEARCH ADAPTER */
            searchAdapter = new LocationSearchAdapter(arrFilteredResults);

            /* SET THE ADAPTER TO THE LOCALITIES RECYCLER VIEW */
            listLocalities.setAdapter(searchAdapter);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("CITY_NAME"))    {
            CITY_NAME = bundle.getString("CITY_NAME");
            if (CITY_NAME != null)  {
                /* FETCH THE LIST OF LOCALITIES */
                new fetchLocalities().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listLocalities.setLayoutManager(manager);
        listLocalities.setHasFixedSize(true);
        listLocalities.setAdapter(searchAdapter);
    }

    private class LocationSearchAdapter extends RecyclerView.Adapter<LocationSearchAdapter.LocalitiesVH> implements Filterable {

        /** ARRAY LIST TO GET DATA FROM THE ACTIVITY **/
        private ArrayList<LocationSearchData> mArrayList;
        private ArrayList<LocationSearchData> mFilteredList;

        LocationSearchAdapter(ArrayList<LocationSearchData> arrLocalities) {

            /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
            this.mArrayList = arrLocalities;
            this.mFilteredList = arrLocalities;
        }

        @Override
        public int getItemCount() {
            return mFilteredList.size();
        }

        @Override
        public void onBindViewHolder(LocationSearchAdapter.LocalitiesVH holder, final int position) {
            final LocationSearchData data = mFilteredList.get(position);

            /* SET THE LOCALITY NAME */
            if (data.getLocalityName() != null) {
                holder.txtLocalityName.setText(data.getLocalityName());
            }

            /* PASS THE RESULT BACK TO THE CALLING ACTIVITY */
            holder.txtLocalityName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    searchView.clearFocus();
                    Intent intent = new Intent();
                    intent.putExtra("LOCALITY_NAME", data.getLocalityName());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });
        }

        @Override
        public LocationSearchAdapter.LocalitiesVH onCreateViewHolder(ViewGroup parent, int i) {

            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.city_selector_item, parent, false);

            return new LocationSearchAdapter.LocalitiesVH(itemView);
        }

        class LocalitiesVH extends RecyclerView.ViewHolder	{
            final TextView txtLocalityName;

            LocalitiesVH(View v) {
                super(v);

                txtLocalityName = (TextView) v.findViewById(R.id.txtLocalityName);
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
                        ArrayList<LocationSearchData> filteredList = new ArrayList<>();
                        for (LocationSearchData data : mArrayList) {
                            if (data.getLocalityName().toLowerCase().contains(charString)) {
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
                    mFilteredList = (ArrayList<LocationSearchData>) results.values;
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
        String strTitle = "Find & Book";
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getApplicationContext()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setSubtitle(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_city_selector, menu);
        MenuItem search = menu.findItem(R.id.search);
        searchView = (SearchView) MenuItemCompat.getActionView(search);
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

    private void search(final SearchView searchView) {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchAdapter.getFilter().filter(newText);
                return true;
            }
        });
    }
}