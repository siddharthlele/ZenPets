package biz.zenpets.users.details.pet.modules;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.adapters.pet.VaccinationsAdapter;
import biz.zenpets.users.utils.models.pet.VaccinationsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PetVaccinations extends Fragment {

    /** THE INCOMING PET ID **/
    private String PET_ID = null;

    /** THE VACCINATIONS ADAPTER AND ARRAY LIST **/
    private VaccinationsAdapter vaccinationsAdapter;
    private final ArrayList<VaccinationsData> arrVaccinations = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listVaccinations) RecyclerView listVaccinations;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.pet_vaccinations_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /** INDICATE THAT THE FRAGMENT SHOULD RETAIN IT'S STATE **/
        setRetainInstance(true);

        /** INDICATE THAT THE FRAGMENT HAS AN OPTIONS MENU **/
        setHasOptionsMenu(true);

        /** INVALIDATE THE EARLIER OPTIONS MENU SET IN OTHER FRAGMENTS / ACTIVITIES **/
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* CONFIGURE THE ADAPTER */
        vaccinationsAdapter = new VaccinationsAdapter(getActivity(), arrVaccinations);

        /* CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /* GET THE INCOMING DATA */
        getIncomingData();
    }

    /***** FETCH THE PET'S VACCINATION RECORDS *****/
    private class fetchVaccinationRecords extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);

            /* CLEAR THE ARRAY LIST */
            arrVaccinations.clear();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_PET_VACCINATIONS = "http://leodyssey.com/ZenPets/public/petVaccinations";
            String URL_PET_VACCINATIONS = "http://192.168.11.2/zenpets/public/petVaccinations";
            HttpUrl.Builder builder = HttpUrl.parse(URL_PET_VACCINATIONS).newBuilder();
            builder.addQueryParameter("petID", PET_ID);
            String FINAL_URL = builder.build().toString();
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
                    JSONArray JAVaccinations = JORoot.getJSONArray("vaccinations");
                    if (JAVaccinations.length() > 0)    {

                        /* AN INSTANCE OF THE VACCINATIONS DATA OBJECT */
                        VaccinationsData data;

                        for (int i = 0; i < JAVaccinations.length(); i++) {
                            JSONObject JOVaccinations = JAVaccinations.getJSONObject(i);

                            /* INSTANTIATE THE VACCINATIONS DATA INSTANCE */
                            data = new VaccinationsData();

                            /* GET THE VACCINATION ID */
                            if (JOVaccinations.has("vaccinationID"))    {
                                data.setVaccinationID(JOVaccinations.getString("vaccinationID"));
                            } else {
                                data.setVaccinationID(null);
                            }

                            /* GET THE PET ID */
                            if (JOVaccinations.has("petID"))    {
                                data.setPetID(JOVaccinations.getString("petID"));
                            } else {
                                data.setPetID(null);
                            }

                            /* GET THE VACCINE ID */
                            if (JOVaccinations.has("vaccineID"))    {
                                data.setVaccineID(JOVaccinations.getString("vaccineID"));
                            } else {
                                data.setVaccineID(null);
                            }

                            /* GET THE VACCINE NAME */
                            if (JOVaccinations.has("vaccineName"))  {
                                data.setVaccineName(JOVaccinations.getString("vaccineName"));
                            } else {
                                data.setVaccineName(null);
                            }

                            /* GET THE VACCINATION DATE */
                            if (JOVaccinations.has("vaccinationDate"))  {
                                data.setVaccinationDate(JOVaccinations.getString("vaccinationDate"));
                            } else {
                                data.setVaccinationDate(null);
                            }

                            /* GET THE VACCINATION NOTES */
                            if (JOVaccinations.has("vaccinationNotes") && !JOVaccinations.getString("vaccinationNotes").equalsIgnoreCase("null")) {
                                data.setVaccinationNotes(JOVaccinations.getString("vaccinationNotes"));
                            } else {
                                data.setVaccinationNotes(null);
                            }

//                            /* GET THE VACCINATION PICTURE */
//                            if (JOVaccinations.has("vaccinationPicture")
//                                    && !JOVaccinations.getString("vaccinationPicture").equalsIgnoreCase("null")
//                                    && !JOVaccinations.getString("vaccinationPicture").equalsIgnoreCase("")) {
//                                data.setVaccinationPicture(JOVaccinations.getString("vaccinationPicture"));
//                            } else {
//                                data.setVaccinationPicture(null);
//                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrVaccinations.add(data);
                        }

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY VIEW */
                                listVaccinations.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listVaccinations.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW */
                            linlaEmpty.setVisibility(View.VISIBLE);
                            listVaccinations.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* CONFIGURE THE ADAPTER */
            vaccinationsAdapter = new VaccinationsAdapter(getActivity(), arrVaccinations);

            /* SET THE ADAPTER TO THE RECYCLER VIEW */
            listVaccinations.setAdapter(vaccinationsAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle.containsKey("PET_ID"))   {
            PET_ID = bundle.getString("PET_ID");
            if (PET_ID != null) {
                /* FETCH THE PET VACCINATIONS */
                new fetchVaccinationRecords().execute();
            } else {
                Toast.makeText(getActivity(), "Failed to get required info.", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        } else {
            Toast.makeText(getActivity(), "Failed to get required info.", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listVaccinations.setLayoutManager(manager);
        listVaccinations.setHasFixedSize(true);
        listVaccinations.setNestedScrollingEnabled(true);
        listVaccinations.setAdapter(vaccinationsAdapter);
    }
}