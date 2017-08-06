package biz.zenpets.users.landing.modules;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.creators.question.QuestionCreator;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.questions.QuestionsAdapter;
import biz.zenpets.users.utils.helpers.questions.FilterQuestionsActivity;
import biz.zenpets.users.utils.models.questions.QuestionsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class QuestionsFragment extends Fragment {

    /** THE PROBLEM ID STRING TO FILTER CONSULTATION QUESTIONS **/
    private String PROBLEM_ID = null;

    /** THE CONSULTATIONS ADAPTER AND ARRAY LIST **/
    private QuestionsAdapter adapter;
    private final ArrayList<QuestionsData> arrConsultations = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listConsult) RecyclerView listConsult;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** ADD A NEW CONSULTATION QUESTION **/
    @OnClick(R.id.btnAskQuestion) void newConsultation()    {
        Intent intent = new Intent(getActivity(), QuestionCreator.class);
        startActivityForResult(intent, 101);
    }

    /** SHOW THE FILTERS FOR FILTERING CONSULTATION QUESTIONS **/
    @OnClick(R.id.linlaFilter) void showFilters()   {
        Intent intent = new Intent(getActivity(), FilterQuestionsActivity.class);
        startActivityForResult(intent, 102);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        /* CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.fragment_question_list, container, false);
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

        /* INSTANTIATE THE DOCTORS ADAPTER */
        adapter = new QuestionsAdapter(getActivity(), arrConsultations);

        /* CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /* FETCH LIST OF CONSULTATIONS */
        new fetchConsultations().execute();
    }

    /***** FETCH LIST OF CONSULTATIONS *****/
    private class fetchConsultations extends AsyncTask<Void, Void, Void>    {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_QUESTIONS = "http://leodyssey.com/ZenPets/public/fetchConsultations";
            String URL_USER_QUESTIONS = "http://192.168.11.2/zenpets/public/fetchConsultations";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_QUESTIONS).newBuilder();
            if (PROBLEM_ID != null) {
                builder.addQueryParameter("problemID", PROBLEM_ID);
            }
            String FINAL_URL = builder.build().toString();
            Log.e("QUESTIONS", FINAL_URL);
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
                    JSONArray JAConsultations = JORoot.getJSONArray("consultations");
                    if (JAConsultations.length() > 0) {
                        /* AN INSTANCE OF THE QUESTIONS DATA MODEL */
                        QuestionsData data;

                        for (int i = 0; i < JAConsultations.length(); i++) {
                            JSONObject JOConsultations = JAConsultations.getJSONObject(i);

                            /* INSTANTIATE THE USER QUESTIONS DATA INSTANCE */
                            data = new QuestionsData();

                            /* GET THE CONSULTATION ID */
                            if (JOConsultations.has("consultationID"))  {
                                data.setConsultationID(JOConsultations.getString("consultationID"));
                            } else {
                                data.setConsultationID(null);
                            }

                            /* GET THE USER ID */
                            if (JOConsultations.has("userID"))  {
                                data.setUserID(JOConsultations.getString("userID"));
                            } else {
                                data.setUserID(null);
                            }

                            /* GET THE PET ID */
                            if (JOConsultations.has("petID"))   {
                                data.setPetID(JOConsultations.getString("petID"));
                            } else {
                                data.setPetID(null);
                            }

                            /* GET THE CONSULTATION TITLE */
                            if (JOConsultations.has("consultationTitle"))   {
                                data.setConsultationTitle(JOConsultations.getString("consultationTitle"));
                            } else {
                                data.setConsultationTitle(null);
                            }

                            /* GET THE CONSULTATION DESCRIPTION */
                            if (JOConsultations.has("consultationDescription"))    {
                                data.setConsultationsDescription(JOConsultations.getString("consultationDescription"));
                            } else {
                                data.setConsultationsDescription(null);
                            }

                            /* GET THE CONSULTATION IMAGE */
                            if (JOConsultations.has("consultationPicture") && !JOConsultations.getString("consultationPicture").equalsIgnoreCase("null")) {
                                data.setConsultationPicture(JOConsultations.getString("consultationPicture"));
                            } else {
                                data.setConsultationPicture(null);
                            }

                            /* GET THE CONSULTATION TIME STAMP */
                            if (JOConsultations.has("consultationTimestamp"))   {
                                String consultationTimestamp = JOConsultations.getString("consultationTimestamp");
                                long lngTimeStamp = Long.parseLong(consultationTimestamp) * 1000;
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(lngTimeStamp);
                                Date date = calendar.getTime();
                                PrettyTime prettyTime = new PrettyTime();
                                String strDate = prettyTime.format(date);
                                data.setConsultationTimestamp(strDate);
                            } else {
                                data.setConsultationTimestamp(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrConsultations.add(data);
                        }

                        /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY LAYOUT */
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listConsult.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listConsult.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linlaEmpty.setVisibility(View.VISIBLE);
                            listConsult.setVisibility(View.GONE);
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

            /* INSTANTIATE THE DOCTORS ADAPTER */
            adapter = new QuestionsAdapter(getActivity(), arrConsultations);

            /* SET THE ADAPTER TO THE RECYCLER VIEW */
            listConsult.setAdapter(adapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 102) {
            Bundle bundle = data.getExtras();
            if (bundle.containsKey("PROBLEM_ID")) {
                PROBLEM_ID = bundle.getString("PROBLEM_ID");
                if (PROBLEM_ID != null) {

                    /* CLEAR THE ARRAY LIST */
                    arrConsultations.clear();

                    /* FETCH LIST OF CONSULTATIONS */
                    new fetchConsultations().execute();
                }
            }
        }
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listConsult.setLayoutManager(manager);
        listConsult.setHasFixedSize(true);
        listConsult.setAdapter(adapter);
    }

    /** CONFIGURE THE TOOLBAR **/
    @SuppressWarnings("ConstantConditions")
    private void configToolbar() {
        Toolbar myToolbar = (Toolbar) getActivity().findViewById(R.id.myToolbar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(myToolbar);
        String strTitle = "Questions";
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