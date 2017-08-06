package biz.zenpets.users.landing.modules;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import biz.zenpets.users.R;
import biz.zenpets.users.profile.ProfileDetails;
import biz.zenpets.users.profile.adoptions.UserAdoptions;
import biz.zenpets.users.profile.appointments.UserAppointments;
import biz.zenpets.users.profile.pets.UserPets;
import biz.zenpets.users.profile.questions.UserQuestions;
import biz.zenpets.users.utils.TypefaceSpan;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileFragment extends Fragment {

    /** THE USER AUTH ID **/
    private String USER_AUTH_ID = null;

    /** THE USER NAME AND DISPLAY PROFILE **/
    private String USER_NAME = null;
    private String USER_DISPLAY_PROFILE = null;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.imgvwProfile) CircleImageView imgvwProfile;
    @BindView(R.id.txtUserName) AppCompatTextView txtUserName;

    /** SHOW THE PROFILE DETAILS **/
    @OnClick(R.id.txtViewProfile) void showProfile()    {
        Intent intent = new Intent(getActivity(), ProfileDetails.class);
        startActivity(intent);
    }

    /** SHOW THE USER'S PETS **/
    @OnClick(R.id.linlaMyPets) void showUserPets()  {
        Intent intent = new Intent(getActivity(), UserPets.class);
        startActivity(intent);
    }

    /** SHOW THE USER'S DOCTORS **/
    @OnClick(R.id.linlaMyAppointments) void showUserAppointments()    {
        Intent intent = new Intent(getActivity(), UserAppointments.class);
        startActivity(intent);
    }

    /** SHOW THE USER'S CONSULTATION QUERIES **/
    @OnClick(R.id.linlaMyConsultations) void showUserQueries() {
        Intent intent = new Intent(getActivity(), UserQuestions.class);
        startActivity(intent);
    }

    /** SHOW THE USER'S ADOPTION LISTINGS **/
    @OnClick(R.id.linlaMyAdoptions) void showUserAdoptions()    {
        Intent intent = new Intent(getActivity(), UserAdoptions.class);
        startActivity(intent);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /* INDICATE THAT THE FRAGMENT SHOULD RETAIN IT'S STATE **/
        setRetainInstance(true);

        /* INDICATE THAT THE FRAGMENT HAS AN OPTIONS MENU **/
        setHasOptionsMenu(true);

        /* INVALIDATE THE EARLIER OPTIONS MENU SET IN OTHER FRAGMENTS / ACTIVITIES **/
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /* CONFIGURE THE TOOLBAR */
        configToolbar();

        /* FETCH THE USER'S NAME AND DISPLAY PROFILE */
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            USER_AUTH_ID = user.getUid();
            if (USER_AUTH_ID != null) {
                /* FETCH THE USER'S PROFILE DETAILS */
                new fetchProfileDetails().execute();
            } else {
                Toast.makeText(getActivity(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
        }
    }

    private class fetchProfileDetails extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_PROFILE = "http://leodyssey.com/ZenPets/public/fetchProfile";
            String URL_USER_PROFILE = "http://192.168.11.2/zenpets/public/fetchProfile";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_PROFILE).newBuilder();
            builder.addQueryParameter("userAuthID", USER_AUTH_ID);
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

                    /* GET THE USER'S NAME */
                    if (JORoot.has("userName")) {
                        USER_NAME = JORoot.getString("userName");
                    }

                    /* GET THE USER'S DISPLAY PROFILE */
                    if (JORoot.has("userDisplayProfile"))   {
                        USER_DISPLAY_PROFILE = JORoot.getString("userDisplayProfile");
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

            /* SET THE USER NAME */
            txtUserName.setText(USER_NAME);

            /* SET THE USER DISPLAY PROFILE */
            Picasso.with(getActivity())
                    .load(USER_DISPLAY_PROFILE)
                    .centerCrop()
                    .fit()
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(imgvwProfile, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onError() {
                            Picasso.with(getActivity())
                                    .load(USER_DISPLAY_PROFILE)
                                    .centerCrop()
                                    .fit()
                                    .into(imgvwProfile);
                        }
                    });

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /** CONFIGURE THE TOOLBAR **/
    @SuppressWarnings("ConstantConditions")
    private void configToolbar() {
        Toolbar myToolbar = (Toolbar) getActivity().findViewById(R.id.myToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);
        String strTitle = "Profile";
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getActivity()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(true);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(s);
        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                break;
            default:
                break;
        }
        return false;
    }
}