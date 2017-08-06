package biz.zenpets.users.profile.questions.modules;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

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
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.adapters.questions.UserQuestionsAdapter;
import biz.zenpets.users.utils.helpers.questions.FilterQuestionsActivity;
import biz.zenpets.users.utils.models.questions.UserQuestionsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MyPublicQuestions extends Fragment {

    private AppPrefs getApp()	{
        return (AppPrefs) getActivity().getApplication();
    }

    /** THE USER ID **/
    private String USER_ID = null;

    /** THE SELECTED PROBLEM ID (FOR FILTERING THE LIST OF QUESTIONS) **/
    private String PROBLEM_ID = null;
    String PROBLEM_TEXT = null;

    /** THE USER QUESTIONS ADAPTER AND ARRAY LIST **/
    private UserQuestionsAdapter questionsAdapter;
    private final ArrayList<UserQuestionsData> arrQuestions = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.txtFilter) AppCompatTextView txtFilter;
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listMyConsultations) RecyclerView listMyConsultations;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** FILTER THE LIST OF CONSULTATION QUESTION **/
    @OnClick(R.id.linlaFilter) void filterQuestions()   {
        Intent intent = new Intent(getActivity(), FilterQuestionsActivity.class);
        if (PROBLEM_ID != null) {
            intent.putExtra("PROBLEM_ID", PROBLEM_ID);
            startActivityForResult(intent, 102);
        } else {
            startActivityForResult(intent, 102);
        }
    }

    /** ASK A NEW QUESTION **/
    @OnClick(R.id.btnAskQuestion) void askQuestion()    {
        Intent intent = new Intent(getActivity(), QuestionCreator.class);
        startActivityForResult(intent, 101);
    }

    /** ASK A NEW QUESTION **/
    @OnClick(R.id.fabNewQuestion) void newQuestion()    {
        Intent intent = new Intent(getActivity(), QuestionCreator.class);
        startActivityForResult(intent, 101);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        /** CAST THE LAYOUT TO A NEW VIEW INSTANCE **/
        View view = inflater.inflate(R.layout.my_public_questions, container, false);
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

        /* INSTANTIATE THE ADAPTER */
        questionsAdapter = new UserQuestionsAdapter(getActivity(), arrQuestions);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();
        if (USER_ID != null) {
            /* FETCH THE USER'S CONSULTATION QUESTIONS */
            new fetchUserQuestions().execute();
        } else {
            Toast.makeText(getActivity(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
        }
    }

    /***** FETCH THE USER'S CONSULTATION QUESTIONS *****/
    private class fetchUserQuestions extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* CLEAR THE ARRAY */
            arrQuestions.clear();

            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_QUESTIONS = "http://leodyssey.com/ZenPets/public/listUserConsultations";
            String URL_USER_QUESTIONS = "http://192.168.11.2/zenpets/public/listUserConsultations";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_QUESTIONS).newBuilder();
            builder.addQueryParameter("userID", USER_ID);
            if (PROBLEM_ID != null) {
                builder.addQueryParameter("problemID", PROBLEM_ID);
            }
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
                    JSONArray JAConsultations = JORoot.getJSONArray("consultations");
                    if (JAConsultations.length() > 0) {
                        /* AN INSTANCE OF THE USER QUESTIONS DATA MODEL */
                        UserQuestionsData data;

                        for (int i = 0; i < JAConsultations.length(); i++) {
                            JSONObject JOConsultations = JAConsultations.getJSONObject(i);

                            /* INSTANTIATE THE USER QUESTIONS DATA INSTANCE */
                            data = new UserQuestionsData();

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
                            arrQuestions.add(data);
                        }

                        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY LAYOUT */
                                listMyConsultations.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);

                                /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                linlaHeaderProgress.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listMyConsultations.setVisibility(View.GONE);

                                /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                                linlaHeaderProgress.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    /* HIDE THE RECYCLER VIEW AND SHOW THE EMPTY LAYOUT */
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linlaEmpty.setVisibility(View.VISIBLE);
                            listMyConsultations.setVisibility(View.GONE);

                            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
                            linlaHeaderProgress.setVisibility(View.GONE);
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

            /* INSTANTIATE THE USER QUESTIONS ADAPTER */
            questionsAdapter = new UserQuestionsAdapter(getActivity(), arrQuestions);

            /* SET THE CONSULTATION QUESTIONS RECYCLER VIEW */
            listMyConsultations.setAdapter(questionsAdapter);

            /* HIDE THE PROGRESS AFTER LOADING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** CONFIGURE THE RECYCLER VIEW *****/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listMyConsultations.setLayoutManager(manager);
        listMyConsultations.setHasFixedSize(true);
        listMyConsultations.setAdapter(questionsAdapter);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && requestCode == 101) {
            /* CLEAR THE ARRAY LIST */
            arrQuestions.clear();

            /* FETCH THE QUESTIONS AGAIN */
            new fetchUserQuestions().execute();

        } else if (resultCode == Activity.RESULT_OK && requestCode == 102)  {
            Bundle bundle = data.getExtras();
            if (bundle.containsKey("PROBLEM_ID")) {
                PROBLEM_ID = bundle.getString("PROBLEM_ID");
                PROBLEM_TEXT = bundle.getString("PROBLEM_TEXT");
                if (PROBLEM_ID != null && PROBLEM_TEXT != null) {
                    /* SET THE PROBLEM TYPE */
                    txtFilter.setText(PROBLEM_TEXT);

                    /* CLEAR THE ARRAY LIST */
                    arrQuestions.clear();

                    /* FETCH LIST OF CONSULTATIONS */
                    new fetchUserQuestions().execute();
                } else {
                    /* EXPLICITLY MARK THE PROBLEM ID "NULL" */
                    PROBLEM_ID = null;

                    /* SET THE PROBLEM TYPE TO THE DEFAULT "FILTER" */
                    txtFilter.setText("Filter Questions");

                    /* CLEAR THE ARRAY LIST */
                    arrQuestions.clear();

                    /* FETCH LIST OF CONSULTATIONS */
                    new fetchUserQuestions().execute();
                }
            }
        }
    }
}