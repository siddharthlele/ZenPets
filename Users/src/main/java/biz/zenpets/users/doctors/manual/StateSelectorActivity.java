package biz.zenpets.users.doctors.manual;

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
import biz.zenpets.users.utils.models.location.StatesData;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class StateSelectorActivity extends AppCompatActivity {
    
    /** THE INCOMING COUNTRY ID **/
    private String COUNTRY_ID = null;

    /** THE COUNTRIES ADAPTER, ARRAY LIST AND FILTERED ARRAY LIST **/
    private StatesSelectorAdapter adapter;
    private ArrayList<StatesData> arrStates = new ArrayList<>();
    private ArrayList<StatesData> arrFilteredResults = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaProgress) LinearLayout linlaProgress;
    @BindView(R.id.listStates) RecyclerView listStates;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.states_selector_list);
        ButterKnife.bind(this);

        /** CONFIGURE THE ACTIONBAR **/
        configAB();
        
        /* INSTANTIATE THE LOCATION SEARCH ADAPTER */
        adapter = new StatesSelectorAdapter(arrFilteredResults);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* GET THE INCOMING DATA */
        getIncomingData();
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("COUNTRY_ID"))    {
            COUNTRY_ID = bundle.getString("COUNTRY_ID");
            if (COUNTRY_ID != null)  {
                /* FETCH THE LIST OF STATES */
                new fetchStates().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** FETCH THE LIST OF STATES *****/
    private class fetchStates extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String STATES_URL = "http://leodyssey.com/ZenPets/public/countryStates/" + COUNTRY_ID;
            String STATES_URL = "http://192.168.11.2/zenpets/public/countryStates/" + COUNTRY_ID;
            OkHttpClient okHttpClient = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(STATES_URL)
                    .build();
            Call call = okHttpClient.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
//                Log.e("RESULT", strResult);
                JSONObject JORoot = new JSONObject(strResult);

                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JAStates = JORoot.getJSONArray("states");

                    /* A STATES DATA POJO INSTANCE */
                    StatesData data;

                    for (int i = 0; i < JAStates.length(); i++) {
                        JSONObject JOStates = JAStates.getJSONObject(i);
//                        Log.e("STATES", String.valueOf(JOStates));

                        /* INSTANTIATE THE STATES DATA POJO INSTANCE */
                        data = new StatesData();

                        /* GET THE STATE ID */
                        if (JOStates.has("stateID"))    {
                            String stateID = JOStates.getString("stateID");
                            data.setStateID(stateID);
                        } else {
                            data.setStateID(null);
                        }

                        /* GET THE STATE NAME */
                        if (JOStates.has("stateName"))   {
                            String stateName = JOStates.getString("stateName");
                            data.setStateName(stateName);
                        } else {
                            data.setStateName(null);
                        }

                        /* GET THE COUNTRY ID */
                        if (JOStates.has("countryID"))  {
                            String countryID = JOStates.getString("countryID");
                            data.setCountryID(countryID);
                        } else {
                            data.setCountryID(null);
                        }

                        /* ADD THE COLLECTED INFO TO THE ARRAYLIST */
                        arrStates.add(data);
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
            arrFilteredResults.addAll(arrStates);

            /* INSTANTIATE THE LOCATION SEARCH ADAPTER */
            adapter = new StatesSelectorAdapter(arrFilteredResults);

            /* SET THE ADAPTER TO THE STATES RECYCLER VIEW */
            listStates.setAdapter(adapter);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            linlaProgress.setVisibility(View.GONE);
        }
    }

    /***** THE COUNTRIES ADAPTER *****/
    private class StatesSelectorAdapter extends RecyclerView.Adapter<StatesSelectorAdapter.StatesVH> implements Filterable {

        /** ARRAY LIST TO GET DATA FROM THE ACTIVITY **/
        private ArrayList<StatesData> mArrayList;
        private ArrayList<StatesData> mFilteredList;

        StatesSelectorAdapter(ArrayList<StatesData> arrStates) {

            /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
            this.mArrayList = arrStates;
            this.mFilteredList = arrStates;
        }

        @Override
        public int getItemCount() {
            return mFilteredList.size();
        }

        @Override
        public void onBindViewHolder(StatesSelectorAdapter.StatesVH holder, final int position) {
            final StatesData data = mFilteredList.get(position);
            
            /* SET THE STATE NAME **/
            String strStateName = data.getStateName();
            if (strStateName != null)	{
                holder.txtCountryName.setText(strStateName);
            }

            /** SHOW THE CITY SELECTOR ACTIVITY **/
//            holder.txtCountryName.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    Intent intent = new Intent(StateSelectorActivity.this, CitySelectorActivity.class);
//                    intent.putExtra("COUNTRY_ID", data.getStateID());
//                    startActivityForResult(intent, 102);
//                }
//            });
        }

        @Override
        public StatesSelectorAdapter.StatesVH onCreateViewHolder(ViewGroup parent, int i) {

            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.country_selector_item, parent, false);

            return new StatesSelectorAdapter.StatesVH(itemView);
        }

        class StatesVH extends RecyclerView.ViewHolder {
            TextView txtCountryName;

            StatesVH(View v) {
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
                        ArrayList<StatesData> filteredList = new ArrayList<>();
                        for (StatesData data : mArrayList) {
                            if (data.getStateName().toLowerCase().contains(charString)) {
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
                    mFilteredList = (ArrayList<StatesData>) results.values;
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
        String strTitle = "Select State";
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
        listStates.setLayoutManager(manager);
        listStates.setHasFixedSize(true);
        listStates.setAdapter(adapter);
    }
}