package com.zenpets.doctors.details.clinic.modules;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.zenpets.doctors.R;
import com.zenpets.doctors.creators.clinic.images.ClinicAlbumCreator;
import com.zenpets.doctors.utils.adapters.clinics.images.ClinicAlbumAdapter;
import com.zenpets.doctors.utils.models.clinics.images.ClinicAlbumData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ClinicImagesFrag extends Fragment {

    /** THE INCOMING CLINIC ID **/
    String CLINIC_ID = null;

    /** FLAG TO CHECK CLINIC IMAGES AVAILABLE **/
    boolean blnImagesFlag = true;

    /** THE CLINIC IMAGES ADAPTER AND ARRAY LIST **/
    ClinicAlbumAdapter adapter;
    ArrayList<ClinicAlbumData> arrImages = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.gridClinicImages) RecyclerView gridClinicImages;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** SELECT CLINIC IMAGES **/
    @OnClick(R.id.linlaEmpty) void selectImages()    {
        Intent intent = new Intent(getActivity(), ClinicAlbumCreator.class);
        intent.putExtra("CLINIC_ID", CLINIC_ID);
        startActivityForResult(intent, 101);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.clinic_images_list_frag, container, false);
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

        /** CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /** GET THE INCOMING DATA **/
        getIncomingData();
    }

    private class fetchClinicImages extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /** SHOW THE PROGRESS WHILE FETCHING THE DATA **/
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            String URL_CLINIC_IMAGES = "http://leodyssey.com/ZenPets/public/fetchClinicImages";
            // String URL_CLINIC_IMAGES = "http://192.168.11.2/zenpets/public/fetchClinicImages";
            HttpUrl.Builder builder = HttpUrl.parse(URL_CLINIC_IMAGES).newBuilder();
            builder.addQueryParameter("clinicID", CLINIC_ID);
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
                    JSONArray JAImages = JORoot.getJSONArray("images");
                    if (JAImages.length() > 0)  {
                        /** AN INSTANCE OF THE CLINIC ALBUM DATA **/
                        ClinicAlbumData data;
                        for (int i = 0; i < JAImages.length(); i++) {
                            JSONObject JOImages = JAImages.getJSONObject(i);

                            /* INSTANTIATE THE CLINIC ALBUM DATA INSTANCE  */
                            data = new ClinicAlbumData();

                            /* GET THE IMAGE ID */
                            if (JOImages.has("imageID"))    {
                                data.setImageID(JOImages.getString("imageID"));
                            } else {
                                data.setImageID(null);
                            }

                            /* GET THE CLINIC ID */
                            if (JOImages.has("clinicID"))   {
                                data.setClinicID(JOImages.getString("clinicID"));
                            } else {
                                data.setClinicID(null);
                            }

                            /* GET THE CLINIC IMAGE URL */
                            if (JOImages.has("imageURL"))   {
                                data.setImageURL(JOImages.getString("imageURL"));
                            } else {
                                data.setImageURL(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrImages.add(data);
                        }

                        /** SET THE FLAG **/
                        blnImagesFlag = true;
                        getActivity().invalidateOptionsMenu();
                    } else {
                        /** SHOW THE EMPTY CONTAINER AND HIDE THE RECYCLER VIEW **/
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                gridClinicImages.setVisibility(View.GONE);
                                blnImagesFlag = false;
                                getActivity().invalidateOptionsMenu();
                            }
                        });
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

            /** INSTANTIATE THE IMAGES ADAPTER **/
            adapter = new ClinicAlbumAdapter(getActivity(), arrImages);

            /** SET THE ADAPTER TO THE RECYCLER VIEW **/
            gridClinicImages.setAdapter(adapter);

            /** HIDE THE PROGRESS AFTER FETCHING THE DATA **/
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /* GET THE INCOMING CLINIC ID */
    private void getIncomingData() {
        Bundle bundle = getActivity().getIntent().getExtras();
        if (bundle.containsKey("CLINIC_ID"))    {
            CLINIC_ID = bundle.getString("CLINIC_ID");
            if (CLINIC_ID != null)  {
                /** FETCH THE CLINIC IMAGES **/
                new fetchClinicImages().execute();
            } else {
                Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            }
        } else {
            Toast.makeText(getActivity(), "Failed to get required information", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        int intOrientation = getActivity().getResources().getConfiguration().orientation;
        gridClinicImages.setHasFixedSize(true);
        GridLayoutManager glm = null;
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet)   {
            if (intOrientation == 1)	{
                glm = new GridLayoutManager(getActivity(), 2);
            } else if (intOrientation == 2) {
                glm = new GridLayoutManager(getActivity(), 4);
            }
        } else {
            if (intOrientation == 1)    {
                glm = new GridLayoutManager(getActivity(), 2);
            } else if (intOrientation == 2) {
                glm = new GridLayoutManager(getActivity(), 4);
            }
        }
        gridClinicImages.setLayoutManager(glm);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuAdd:
                Intent intent = new Intent(getActivity(), ClinicAlbumCreator.class);
                intent.putExtra("CLINIC_ID", CLINIC_ID);
                startActivityForResult(intent, 101);
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.frag_clinic_images_upload, menu);

        if (blnImagesFlag)  {
            MenuItem menuAdd = menu.findItem(R.id.menuAdd);
            menuAdd.setVisible(false);
        } else {
            MenuItem menuAdd = menu.findItem(R.id.menuAdd);
            menuAdd.setVisible(true);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }
}