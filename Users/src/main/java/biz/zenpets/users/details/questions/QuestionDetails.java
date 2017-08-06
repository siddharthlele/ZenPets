package biz.zenpets.users.details.questions;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.mikepenz.iconics.view.IconicsImageView;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.PrettyTime;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.helpers.BookmarkStatus;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.helpers.votes.HelpfulVotes;
import biz.zenpets.users.utils.helpers.votes.NotHelpfulVotes;
import biz.zenpets.users.utils.models.questions.RepliesData;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class QuestionDetails extends AppCompatActivity implements BookmarkStatus, HelpfulVotes, NotHelpfulVotes {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** THE INCOMING CONSULTATION ID **/
    private static String CONSULTATION_ID = null;

    /** THE USER ID **/
    private static String USER_ID = null;

    /** THE REPLIES ADAPTER AND ARRAY LIST **/
    private RepliesAdapter repliesAdapter;
    private final ArrayList<RepliesData> arrReplies = new ArrayList<>();

    /** THE BOOKMARK STATUS FLAG **/
    private boolean blnBookmarkStatus = false;

    /** THE HELPFUL VOTES **/
    private String HELPFUL_VOTES = null;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.txtConsultationTitle)AppCompatTextView txtConsultationTitle;
    @BindView(R.id.txtConsultationDescription) AppCompatTextView txtConsultationDescription;
    @BindView(R.id.txtConsultationFor) AppCompatTextView txtConsultationFor;
    @BindView(R.id.txtConsultationTimestamp) AppCompatTextView txtConsultationTimestamp;
    @BindView(R.id.txtConsultationViews) AppCompatTextView txtConsultationViews;
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listAnswers) RecyclerView listAnswers;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_details);
        ButterKnife.bind(this);

        /** INSTANTIATE THE ADAPTER **/
        repliesAdapter = new RepliesAdapter(QuestionDetails.this, arrReplies);

        /** CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /** CONFIGURE THE TOOLBAR **/
        configTB();

        /** GET THE USER ID **/
        USER_ID = getApp().getUserID();

        /** FETCH THE BOOKMARK STATUS **/
        new getBookmarkStatus(this).execute();

        /** GET THE INCOMING DATA **/
        getIncomingData();
    }

    /***** FETCH CONSULTATION DETAILS *****/
    private class fetchConsultationDetails extends AsyncTask<Void, Void, Void>  {

        String CONSULTATION_TITLE = null;
        String CONSULTATION_DESCRIPTION = null;
        String CONSULTATION_TIMESTAMP = null;
        String PET_GENDER = null;
        String BREED_NAME = null;
        String PET_AGE = null;

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_CONSULTATION_DETAILS = "http://leodyssey.com/ZenPets/public/consultationDetails";
            String URL_CONSULTATION_DETAILS = "http://192.168.11.2/zenpets/public/consultationDetails";
            HttpUrl.Builder builder = HttpUrl.parse(URL_CONSULTATION_DETAILS).newBuilder();
            builder.addQueryParameter("consultationID", CONSULTATION_ID);
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
                        for (int i = 0; i < JAConsultations.length(); i++) {
                            JSONObject JOConsultations = JAConsultations.getJSONObject(i);

                            /** GET THE CONSULTATION TITLE **/
                            if (JOConsultations.has("consultationTitle"))   {
                                CONSULTATION_TITLE = JOConsultations.getString("consultationTitle");
                            }

                            /** SET THE CONSULTATION DESCRIPTION **/
                            if (JOConsultations.has("consultationDescription")) {
                                CONSULTATION_DESCRIPTION = JOConsultations.getString("consultationDescription");
                            }

                            /** SET THE QUESTION TIME STAMP **/
                            if (JOConsultations.has("consultationTimestamp"))    {
                                String consultationTimestamp = JOConsultations.getString("consultationTimestamp");
                                long lngTimeStamp = Long.parseLong(consultationTimestamp) * 1000;
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(lngTimeStamp);
                                Date date = calendar.getTime();
                                PrettyTime prettyTime = new PrettyTime();
                                CONSULTATION_TIMESTAMP = prettyTime.format(date);
                            }

                            /** SET THE "CONSULTATION FOR" **/
                            if (JOConsultations.has("petGender") && JOConsultations.has("breedName") && JOConsultations.has("petDOB"))   {
                                PET_GENDER = JOConsultations.getString("petGender");
                                BREED_NAME = JOConsultations.getString("breedName");
                                String petDOB = JOConsultations.getString("petDOB");
                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                                try {
                                    Date dtDOB = format.parse(petDOB);
                                    Calendar calDOB = Calendar.getInstance();
                                    calDOB.setTime(dtDOB);
                                    int dobYear = calDOB.get(Calendar.YEAR);
                                    int dobMonth = calDOB.get(Calendar.MONTH) + 1;
                                    int dobDate = calDOB.get(Calendar.DATE);

                                    /** CALCULATE THE PET'S AGE **/
                                    Period petAge = getPetAge(dobYear, dobMonth, dobDate);
                                    PET_AGE = petAge.getYears() + " year old";
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
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

            /* SET THE CONSULTATION TITLE */
            txtConsultationTitle.setText(CONSULTATION_TITLE);

            /* SET THE CONSULTATION DESCRIPTION */
            txtConsultationDescription.setText(CONSULTATION_DESCRIPTION);

            /* SET THE CONSULTATION TIME STAMP */
            txtConsultationTimestamp.setText(CONSULTATION_TIMESTAMP);

            /* SET THE CONSULTATION FOR TEXT */
            txtConsultationFor.setText("Asked for " + PET_AGE + " " + PET_GENDER + " " + BREED_NAME);
        }
    }

    @Override
    public void checkHelpfulVotes(String response) {
        if (response != null)   {
            HELPFUL_VOTES = response;
        } else {
            HELPFUL_VOTES = "0";
        }
    }

    @Override
    public void checkNotHelpfulVotes(String response) {
        String NON_HELPFUL_VOTES = null;
        if (response != null)   {
            NON_HELPFUL_VOTES = response;
        } else {
            NON_HELPFUL_VOTES = "0";
        }
    }

    /***** FETCH THE LIST OF REPLIES ON THE CONSULTATION QUESTION  *****/
    private class fetchReplies extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /** SHOW THE PROGRESS WHILE FETCHING THE DATA **/
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_CONSULTATION_REPLIES = "http://leodyssey.com/ZenPets/public/fetchConsultationReplies";
            String URL_CONSULTATION_REPLIES = "http://192.168.11.2/zenpets/public/fetchConsultationReplies";
            HttpUrl.Builder builder = HttpUrl.parse(URL_CONSULTATION_REPLIES).newBuilder();
            builder.addQueryParameter("consultationID", CONSULTATION_ID);
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
                    JSONArray JAReplies = JORoot.getJSONArray("replies");
                    if (JAReplies.length() > 0) {
                        RepliesData data;
                        for (int i = 0; i < JAReplies.length(); i++) {
                            JSONObject JOReplies = JAReplies.getJSONObject(i);
                            data = new RepliesData();

                            /** GET THE REPLY ID **/
                            if (JOReplies.has("replyID"))   {
                                data.setReplyID(JOReplies.getString("replyID"));
                            } else {
                                data.setReplyID(null);
                            }

                            /** GET THE USER ID **/
                            String userID = getApp().getUserID();
                            data.setUserID(userID);

                            /** GET THE CONSULTATION ID **/
                            if (JOReplies.has("consultationID"))    {
                                data.setConsultationID(JOReplies.getString("consultationID"));
                            } else {
                                data.setConsultationID(null);
                            }

                            /** GET THE DOCTOR ID **/
                            if (JOReplies.has("doctorID"))    {
                                data.setDoctorID(JOReplies.getString("doctorID"));
                            } else {
                                data.setDoctorID(null);
                            }

                            /** GET THE DOCTOR PREFIX **/
                            if (JOReplies.has("doctorPrefix") && JOReplies.has("doctorName"))  {
                                String doctorPrefix = JOReplies.getString("doctorPrefix");
                                String doctorName = JOReplies.getString("doctorName");
                                data.setDoctorName(doctorPrefix + " " + doctorName);
                            } else {
                                data.setDoctorName(null);
                            }

                            /** GET THE DOCTOR'S PROFILE PICTURE **/
                            if (JOReplies.has("doctorDisplayProfile"))  {
                                data.setDoctorProfile(JOReplies.getString("doctorDisplayProfile"));
                            } else {
                                data.setDoctorProfile(null);
                            }

                            /** GET THE STATE NAME **/
                            if (JOReplies.has("stateName"))  {
                                data.setStateName(JOReplies.getString("stateName"));
                            } else {
                                data.setStateName(null);
                            }

                            /** GET THE CITY NAME **/
                            if (JOReplies.has("cityName"))  {
                                data.setCityName(JOReplies.getString("cityName"));
                            } else {
                                data.setCityName(null);
                            }

                            /** GET THE REPLY TEXT **/
                            if (JOReplies.has("replyText")) {
                                data.setReplyText(JOReplies.getString("replyText"));
                            } else {
                                data.setReplyText(null);
                            }

                            /** GET THE TIME STAMP **/
                            if (JOReplies.has("replyTimestamp"))    {
                                String replyTimestamp = JOReplies.getString("replyTimestamp");
                                long lngTimeStamp = Long.parseLong(replyTimestamp) * 1000;
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(lngTimeStamp);
                                Date date = calendar.getTime();
                                PrettyTime prettyTime = new PrettyTime();
                                String strDate = prettyTime.format(date);
                                data.setReplyTimestamp(strDate);
                            } else {
                                data.setReplyTimestamp(null);
                            }

                            /** GET THE HELPFUL VOTES ("YES") **/
                            // String URL_YES_VOTES = "http://leodyssey.com/ZenPets/public/fetchYesVotes";
                            String URL_YES_VOTES = "http://192.168.11.2/zenpets/public/fetchYesVotes";
                            HttpUrl.Builder builderYesVotes = HttpUrl.parse(URL_YES_VOTES).newBuilder();
                            builderYesVotes.addQueryParameter("replyID", JOReplies.getString("replyID"));
                            builderYesVotes.addQueryParameter("voteStatus", "1");
                            String FINAL_YES_VOTES_URL = builderYesVotes.build().toString();
                            OkHttpClient clientYesVotes = new OkHttpClient();
                            Request requestYesVotes = new Request.Builder()
                                    .url(FINAL_YES_VOTES_URL)
                                    .build();
                            Call callYesVotes = clientYesVotes.newCall(requestYesVotes);
                            Response responseYesVotes = callYesVotes.execute();
                            String strResultYesVotes = responseYesVotes.body().string();
                            JSONObject JORootYesVotes = new JSONObject(strResultYesVotes);
                            if (JORootYesVotes.has("error") && JORootYesVotes.getString("error").equalsIgnoreCase("false")) {
                                JSONArray JAYes = JORootYesVotes.getJSONArray("votes");

                                /** SET THE NUMBER OF YES VOTES **/
                                data.setReplyYesVote(String.valueOf(JAYes.length()));
                            }

                            /** GET THE NOT HELPFUL VOTES ("NO") **/
                            // String URL_NO_VOTES = "http://leodyssey.com/ZenPets/public/fetchNoVotes";
                            String URL_NO_VOTES = "http://192.168.11.2/zenpets/public/fetchNoVotes";
                            HttpUrl.Builder builderNoVotes = HttpUrl.parse(URL_NO_VOTES).newBuilder();
                            builderNoVotes.addQueryParameter("replyID", JOReplies.getString("replyID"));
                            builderNoVotes.addQueryParameter("voteStatus", "0");
                            String FINAL_NO_VOTES_URL = builderNoVotes.build().toString();
                            OkHttpClient clientNoVotes = new OkHttpClient();
                            Request requestNoVotes = new Request.Builder()
                                    .url(FINAL_NO_VOTES_URL)
                                    .build();
                            Call callNoVotes = clientNoVotes.newCall(requestNoVotes);
                            Response responseNoVotes = callNoVotes.execute();
                            String strResultNoVotes = responseNoVotes.body().string();
                            JSONObject JORootNoVotes = new JSONObject(strResultNoVotes);
                            if (JORootNoVotes.has("error") && JORootNoVotes.getString("error").equalsIgnoreCase("false")) {
                                JSONArray JANo = JORootNoVotes.getJSONArray("votes");

                                /** SET THE NUMBER OF NO VOTES **/
                                data.setReplyNoVote(String.valueOf(JANo.length()));
                            }

                            /** ADD THE COLLECTED DATA TO THE ARRAY LIST **/
                            arrReplies.add(data);
                        }

                            /** SHOW THE RECYCLER VIEW, CONFIGURE THE RECYCLER VIEW AND SET THE ADAPTER **/
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /** SHOW THE RECYCLER VIEW AND HIDE THE EMPTY VIEW **/
                                listAnswers.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /** HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW **/
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listAnswers.setVisibility(View.GONE);
                            }
                        });
                    }
                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            /** HIDE THE RECYCLER VIEW AND SHOW THE EMPTY VIEW **/
                            linlaEmpty.setVisibility(View.VISIBLE);
                            listAnswers.setVisibility(View.GONE);
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

            /** INSTANTIATE THE ANSWERS ADAPTER **/
            repliesAdapter = new RepliesAdapter(QuestionDetails.this, arrReplies);

            /** SET THE ADAPTER TO THE RECYCLER VIEW **/
            listAnswers.setAdapter(repliesAdapter);

            /** HIDE THE PROGRESS AFTER FETCHING THE DATA **/
            linlaHeaderProgress.setVisibility(View.GONE);
        }
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("CONSULTATION_ID"))  {
            CONSULTATION_ID = bundle.getString("CONSULTATION_ID");
            if (CONSULTATION_ID != null)    {
                /** FETCH CONSULTATION DETAILS **/
                new fetchConsultationDetails().execute();

                /** FETCH CONSULTATION REPLIES **/
                new fetchReplies().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info..", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info..", Toast.LENGTH_SHORT).show();
        }
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Question";
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
        MenuInflater inflater = new MenuInflater(QuestionDetails.this);
        inflater.inflate(R.menu.activity_question_details, menu);

        if (blnBookmarkStatus) {
            menu.findItem(R.id.menuBookmark).setIcon(R.drawable.ic_bookmark_white_24dp);
        } else if (!blnBookmarkStatus) {
            menu.findItem(R.id.menuBookmark).setIcon(R.drawable.ic_bookmark_border_white_24dp);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onCheckStatus(String response) {
        /** CAST THE RESULT IN THE BOOLEAN FLAG **/
        blnBookmarkStatus = Boolean.parseBoolean(response);

        /** INVALIDATE THE OPTIONS MENU **/
        invalidateOptionsMenu();
    }

    /***** GET THE BOOKMARK STATUS (CHECK IF THE USER HAS BOOKMARKED THE QUESTION) *****/
    private static class getBookmarkStatus extends AsyncTask<Void, Void, String> {
        final BookmarkStatus delegate;
        boolean blnResult = false;

        getBookmarkStatus(BookmarkStatus delegate) {
            this.delegate = delegate;
        }

        @Override
        protected String doInBackground(Void... params) {
            // String URL_BOOKMARK_STATUS = "http://leodyssey.com/ZenPets/public/getUserConsultationBookmarkStatus";
            String URL_BOOKMARK_STATUS = "http://192.168.11.2/zenpets/public/getUserConsultationBookmarkStatus";
            HttpUrl.Builder builder = HttpUrl.parse(URL_BOOKMARK_STATUS).newBuilder();
            builder.addQueryParameter("consultationID", CONSULTATION_ID);
            builder.addQueryParameter("userID", USER_ID);
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
                    JSONArray JABookmarks = JORoot.getJSONArray("bookmarks");
                    if (JABookmarks.length() > 0) {
                        blnResult = true;
                    } else {
                        blnResult = false;
                    }
                } else {
                    blnResult = false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }

            return String.valueOf(blnResult);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            delegate.onCheckStatus(s);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.menuBookmark:
                /** CHECK IF A BOOKMARK IS ALREADY ADDED **/
                if (blnBookmarkStatus)  {
                    /** REMOVE THE BOOKMARK **/
                    new removeBookmark().execute();
                } else {
                    /** ADD A NEW BOOKMARK **/
                    new addBookmark().execute();
                }
                break;
//            case R.id.menuShare:
//                break;
            default:
                break;
        }
        return false;
    }

    /***** ADD A BOOKMARK ON THE QUESTION *****/
    private class addBookmark extends AsyncTask<Void, Void, Void>   {

        /* A BOOLEAN TO TRACK THE ADDITION STATUS */
        boolean blnSuccess = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_NEW_BOOKMARK = "http://leodyssey.com/ZenPets/public/newConsultationBookmark";
            String URL_NEW_BOOKMARK = "http://192.168.11.2/zenpets/public/newConsultationBookmark";
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("consultationID", CONSULTATION_ID)
                    .add("userID", USER_ID)
                    .build();
            Request request = new Request.Builder()
                    .url(URL_NEW_BOOKMARK)
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    blnSuccess = true;
                } else {
                    blnSuccess = false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (blnSuccess) {
                /** FETCH THE BOOKMARK STATUS **/
                new getBookmarkStatus(QuestionDetails.this).execute();

                /** INVALIDATE THE OPTIONS MENU **/
                invalidateOptionsMenu();
            }
        }
    }

    /***** REMOVE A USER'S BOOKMARK ON THE QUESTION *****/
    private class removeBookmark extends AsyncTask<Void, Void, Void>    {

        /* A BOOLEAN TO TRACK THE DELETE STATUS */
        boolean blnSuccess = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DELETE_BOOKMARK = "http://leodyssey.com/ZenPets/public/removeConsultationBookmark";
            String URL_DELETE_BOOKMARK = "http://192.168.11.2/zenpets/public/removeConsultationBookmark";
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("consultationID", CONSULTATION_ID)
                    .add("userID", USER_ID)
                    .build();
            Request request = new Request.Builder()
                    .url(URL_DELETE_BOOKMARK)
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    blnSuccess = true;
                } else {
                    blnSuccess = false;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            /* CHECK IF THE DELETION WAS SUCCESSFUL */
            if (blnSuccess) {
                /** FETCH THE BOOKMARK STATUS **/
                new getBookmarkStatus(QuestionDetails.this).execute();

                /** INVALIDATE THE OPTIONS MENU **/
                invalidateOptionsMenu();
            }
        }
    }

    /***** CALCULATE THE PET'S AGE *****/
    private Period getPetAge(int year, int month, int date) {
        LocalDate dob = new LocalDate(year, month, date);
        LocalDate now = new LocalDate();
        return Period.fieldDifference(dob, now);
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        listAnswers.setLayoutManager(manager);
        listAnswers.setHasFixedSize(true);
        listAnswers.setNestedScrollingEnabled(false);
        listAnswers.setAdapter(repliesAdapter);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /***** THE REPLIES ADAPTER *****/
    private class RepliesAdapter extends RecyclerView.Adapter<RepliesAdapter.ConsultationsVH> {

        /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
        private final Activity activity;

        /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
        private final ArrayList<RepliesData> arrReplies;

        RepliesAdapter(Activity activity, ArrayList<RepliesData> arrReplies) {

            /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
            this.activity = activity;

            /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
            this.arrReplies = arrReplies;
        }

        @Override
        public int getItemCount() {
            return arrReplies.size();
        }

        @Override
        public void onBindViewHolder(final RepliesAdapter.ConsultationsVH holder, final int position) {
            final RepliesData data = arrReplies.get(position);

            /* SET THE REPLY TEXT */
            if (data.getReplyText() != null)    {
                holder.txtReplyText.setText(data.getReplyText());
            }

            /* SET THE TIME STAMP */
            if (data.getReplyTimestamp() != null)   {
                holder.txtReplyTimestamp.setText(data.getReplyTimestamp());
            }

            /* SET THE DOCTOR'S NAME */
            if (data.getDoctorName() != null)   {
                holder.txtDoctorName.setText(data.getDoctorName());
            }

            /* SET THE DOCTOR PROFILE */
            if (data.getDoctorProfile() != null)   {
                Glide.with(activity)
                        .load(data.getDoctorProfile())
                        .crossFade()
                        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                        .centerCrop()
                        .into(holder.imgvwDoctorProfile);
            } else {
                holder.imgvwDoctorProfile.setImageDrawable(ContextCompat.getDrawable(activity, R.drawable.beagle));
            }

            /* SET THE DOCTOR'S LOCATION */
            if (data.getStateName() != null && data.getCityName() != null)  {
                holder.txtDoctorLocation.setText(data.getCityName() + ", " + data.getStateName());
                holder.txtDoctorLocation.setVisibility(View.VISIBLE);
            } else {
                holder.txtDoctorLocation.setVisibility(View.GONE);
            }

            /** GET THE NUMBER OF HELPFUL VOTES **/
            new getHelpVotes(QuestionDetails.this).execute(data.getReplyID());

            /** GET THE NUMBER OF NON HELPFUL VOTES **/
            new getNonHelpVotes(QuestionDetails.this).execute(holder.txtReplyHelpful, data.getReplyID());

            /** ADD A NOT HELPFUL VOTE ON THE REPLY **/
            holder.txtHelpfulNo.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    // String URL_CHECK_VOTE = "http://leodyssey.com/ZenPets/public/checkUserVote";
                    String URL_CHECK_VOTE = "http://192.168.11.2/zenpets/public/checkUserVote";
                    HttpUrl.Builder builder = HttpUrl.parse(URL_CHECK_VOTE).newBuilder();
                    builder.addQueryParameter("replyID", data.getReplyID());
                    builder.addQueryParameter("userID", data.getUserID());
                    String FINAL_URL = builder.build().toString();
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder()
                            .url(FINAL_URL)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("FAILURE", e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String strResult = response.body().string();
                                JSONObject JORoot = new JSONObject(strResult);
                                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                                    JSONArray JAVotes = JORoot.getJSONArray("votes");
                                    if (JAVotes.length() > 0) {
                                        /** CHECK IF THE USER VOTE IF "HELPFUL YES" OR "HELPFUL NO" **/
                                        for (int i = 0; i < JAVotes.length(); i++) {
                                            JSONObject JOVotes = JAVotes.getJSONObject(i);

                                            /** GET THE VOTE ID **/
                                            String voteID = JOVotes.getString("voteID");

                                            /** GET THE VOTE STATUS **/
                                            if (JOVotes.has("voteStatus"))  {
                                                String voteStatus = JOVotes.getString("voteStatus");

                                                if (voteStatus.equalsIgnoreCase("0"))   {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(activity, "You have already voted this reply as not helpful", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else if (voteStatus.equalsIgnoreCase("1"))    {
                                                    // String URL_UPDATE_VOTE = "http://leodyssey.com/ZenPets/public/updateReplyVoteYes";
                                                    String URL_UPDATE_VOTE = "http://192.168.11.2/zenpets/public/updateReplyVoteYes";
                                                    OkHttpClient client = new OkHttpClient();
                                                    RequestBody body = new FormBody.Builder()
                                                            .add("voteID", voteID)
                                                            .add("voteStatus", "0")
                                                            .build();
                                                    Request request = new Request.Builder()
                                                            .url(URL_UPDATE_VOTE)
                                                            .post(body)
                                                            .build();
                                                    client.newCall(request).enqueue(new Callback() {
                                                        @Override
                                                        public void onFailure(Call call, IOException e) {
                                                            Log.e("FAILURE", e.toString());
                                                        }

                                                        @Override
                                                        public void onResponse(Call call, Response response) throws IOException {
                                                            try {
                                                                String strResult = response.body().string();
                                                                JSONObject JORoot = new JSONObject(strResult);
                                                                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                                                                    /** GET THE NUMBER OF HELPFUL VOTES **/
                                                                    getHelpVotes helpVotes = new getHelpVotes(QuestionDetails.this);
                                                                    helpVotes.execute(data.getReplyID());

                                                                    /** GET THE NUMBER OF NON HELPFUL VOTES **/
                                                                    new getNonHelpVotes(QuestionDetails.this).execute(holder.txtReplyHelpful, data.getReplyID());
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    } else {
                                        /** PUBLISH THE NEW VOTE **/
                                        // String URL_NEW_VOTE = "http://leodyssey.com/ZenPets/public/newReplyVote";
                                        String URL_NEW_VOTE = "http://192.168.11.2/zenpets/public/newReplyVote";
                                        OkHttpClient client = new OkHttpClient();
                                        RequestBody body = new FormBody.Builder()
                                                .add("replyID", data.getReplyID())
                                                .add("userID", data.getUserID())
                                                .add("voteStatus", "0")
                                                .add("voteTimestamp", String.valueOf(System.currentTimeMillis() / 1000))
                                                .build();
                                        Request request = new Request.Builder()
                                                .url(URL_NEW_VOTE)
                                                .post(body)
                                                .build();
                                        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Log.e("FAILURE", e.toString());
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                try {
                                                    String strResult = response.body().string();
                                                    JSONObject JORoot = new JSONObject(strResult);
                                                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                                                        /** GET THE NUMBER OF HELPFUL VOTES **/
                                                        getHelpVotes helpVotes = new getHelpVotes(QuestionDetails.this);
                                                        helpVotes.execute(data.getReplyID());

                                                        /** GET THE NUMBER OF NON HELPFUL VOTES **/
                                                        new getNonHelpVotes(QuestionDetails.this).execute(holder.txtReplyHelpful, data.getReplyID());
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });

            /* ADD A HELPFUL YES VOTE ON THE REPLY */
            holder.txtHelpfulYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // String URL_CHECK_VOTE = "http://leodyssey.com/ZenPets/public/checkUserVote";
                    String URL_CHECK_VOTE = "http://192.168.11.2/zenpets/public/checkUserVote";
                    HttpUrl.Builder builder = HttpUrl.parse(URL_CHECK_VOTE).newBuilder();
                    builder.addQueryParameter("replyID", data.getReplyID());
                    builder.addQueryParameter("userID", data.getUserID());
                    String FINAL_URL = builder.build().toString();
                    OkHttpClient client = new OkHttpClient();
                    final Request request = new Request.Builder()
                            .url(FINAL_URL)
                            .build();
                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("FAILURE", e.toString());
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            try {
                                String strResult = response.body().string();
                                JSONObject JORoot = new JSONObject(strResult);
                                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                                    JSONArray JAVotes = JORoot.getJSONArray("votes");
                                    if (JAVotes.length() > 0)   {
                                        /** CHECK IF THE USER VOTE IF "HELPFUL YES" OR "HELPFUL NO" **/
                                        for (int i = 0; i < JAVotes.length(); i++) {
                                            JSONObject JOVotes = JAVotes.getJSONObject(i);

                                            /** GET THE VOTE ID **/
                                            String voteID = JOVotes.getString("voteID");

                                            /** GET THE VOTE STATUS **/
                                            if (JOVotes.has("voteStatus"))  {
                                                String voteStatus = JOVotes.getString("voteStatus");

                                                if (voteStatus.equalsIgnoreCase("1"))   {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(activity, "You have already voted this reply as helpful", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else if (voteStatus.equalsIgnoreCase("0"))    {
                                                    // String URL_UPDATE_VOTE = "http://leodyssey.com/ZenPets/public/updateReplyVoteYes";
                                                    String URL_UPDATE_VOTE = "http://192.168.11.2/zenpets/public/updateReplyVoteYes";
                                                    OkHttpClient client = new OkHttpClient();
                                                    RequestBody body = new FormBody.Builder()
                                                            .add("voteID", voteID)
                                                            .add("voteStatus", "1")
                                                            .build();
                                                    Request request = new Request.Builder()
                                                            .url(URL_UPDATE_VOTE)
                                                            .post(body)
                                                            .build();
                                                    client.newCall(request).enqueue(new Callback() {
                                                        @Override
                                                        public void onFailure(Call call, IOException e) {
                                                            Log.e("FAILURE", e.toString());
                                                        }

                                                        @Override
                                                        public void onResponse(Call call, Response response) throws IOException {
                                                            try {
                                                                String strResult = response.body().string();
                                                                JSONObject JORoot = new JSONObject(strResult);
                                                                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                                                                    /** GET THE NUMBER OF HELPFUL VOTES **/
                                                                    getHelpVotes helpVotes = new getHelpVotes(QuestionDetails.this);
                                                                    helpVotes.execute(data.getReplyID());

                                                                    /** GET THE NUMBER OF NON HELPFUL VOTES **/
                                                                    new getNonHelpVotes(QuestionDetails.this).execute(holder.txtReplyHelpful, data.getReplyID());
                                                                }
                                                            } catch (JSONException e) {
                                                                e.printStackTrace();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    } else {
                                        /** PUBLISH THE NEW VOTE **/
                                        // String URL_NEW_VOTE = "http://leodyssey.com/ZenPets/public/newReplyVote";
                                        String URL_NEW_VOTE = "http://192.168.11.2/zenpets/public/newReplyVote";
                                        OkHttpClient client = new OkHttpClient();
                                        RequestBody body = new FormBody.Builder()
                                                .add("replyID", data.getReplyID())
                                                .add("userID", data.getUserID())
                                                .add("voteStatus", "1")
                                                .add("voteTimestamp", String.valueOf(System.currentTimeMillis() / 1000))
                                                .build();
                                        Request request = new Request.Builder()
                                                .url(URL_NEW_VOTE)
                                                .post(body)
                                                .build();
                                        client.newCall(request).enqueue(new Callback() {
                                            @Override
                                            public void onFailure(Call call, IOException e) {
                                                Log.e("FAILURE", e.toString());
                                            }

                                            @Override
                                            public void onResponse(Call call, Response response) throws IOException {
                                                try {
                                                    String strResult = response.body().string();
                                                    JSONObject JORoot = new JSONObject(strResult);
                                                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                                                        /** GET THE NUMBER OF HELPFUL VOTES **/
                                                        getHelpVotes helpVotes = new getHelpVotes(QuestionDetails.this);
                                                        helpVotes.execute(data.getReplyID());

                                                        /** GET THE NUMBER OF NON HELPFUL VOTES **/
                                                        new getNonHelpVotes(QuestionDetails.this).execute(holder.txtReplyHelpful, data.getReplyID());
                                                    }
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }

        @Override
        public RepliesAdapter.ConsultationsVH onCreateViewHolder(ViewGroup parent, int i) {

            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.replies_item, parent, false);

            return new RepliesAdapter.ConsultationsVH(itemView);
        }

        class ConsultationsVH extends RecyclerView.ViewHolder	{
            final CircleImageView imgvwDoctorProfile;
            final AppCompatTextView txtDoctorName;
            final AppCompatTextView txtDoctorLocation;
            final AppCompatTextView txtReplyTimestamp;
            final AppCompatTextView txtReplyText;
            final AppCompatTextView txtReplyHelpful;
            final IconicsImageView imgvwFlagReply;
            final AppCompatTextView txtHelpfulYes;
            final AppCompatTextView txtHelpfulNo;

            ConsultationsVH(View v) {
                super(v);
                imgvwDoctorProfile = (CircleImageView) v.findViewById(R.id.imgvwDoctorProfile);
                txtDoctorName = (AppCompatTextView) v.findViewById(R.id.txtDoctorName);
                txtDoctorLocation = (AppCompatTextView) v.findViewById(R.id.txtDoctorLocation);
                txtReplyTimestamp = (AppCompatTextView) v.findViewById(R.id.txtReplyTimestamp);
                txtReplyText = (AppCompatTextView) v.findViewById(R.id.txtReplyText);
                txtReplyHelpful = (AppCompatTextView) v.findViewById(R.id.txtReplyHelpful);
                imgvwFlagReply = (IconicsImageView) v.findViewById(R.id.imgvwFlagReply);
                txtHelpfulYes = (AppCompatTextView) v.findViewById(R.id.txtHelpfulYes);
                txtHelpfulNo = (AppCompatTextView) v.findViewById(R.id.txtHelpfulNo);
            }
        }

        /** GET THE NUMBER OF HELPFUL VOTES **/
        private class getHelpVotes extends AsyncTask<String, Void, String>  {
            final HelpfulVotes votes;
            String result = null;

            getHelpVotes(HelpfulVotes votes) {
                this.votes = votes;
            }

            @Override
            protected String doInBackground(String... params) {
                // String URL_YES_VOTES = "http://leodyssey.com/ZenPets/public/fetchYesVotes";
                String URL_YES_VOTES = "http://192.168.11.2/zenpets/public/fetchYesVotes";
                HttpUrl.Builder builderYesVotes = HttpUrl.parse(URL_YES_VOTES).newBuilder();
                builderYesVotes.addQueryParameter("replyID", params[0]);
                builderYesVotes.addQueryParameter("voteStatus", "1");
                String FINAL_YES_VOTES_URL = builderYesVotes.build().toString();
                OkHttpClient clientYesVotes = new OkHttpClient();
                Request requestYesVotes = new Request.Builder()
                        .url(FINAL_YES_VOTES_URL)
                        .build();
                Call call = clientYesVotes.newCall(requestYesVotes);
                try {
                    Response response = call.execute();
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JAYes = JORoot.getJSONArray("votes");
                        result = String.valueOf(JAYes.length());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                votes.checkHelpfulVotes(s);
            }
        }

        private class getNonHelpVotes extends AsyncTask<Object, Void, String> {

            final NotHelpfulVotes votes;
            String result = null;
            View view;

            getNonHelpVotes(NotHelpfulVotes votes) {
                this.votes = votes;
            }

            @Override
            protected String doInBackground(Object... params) {
                view = (View) params[0];
                // String URL_YES_VOTES = "http://leodyssey.com/ZenPets/public/fetchYesVotes";
                String URL_YES_VOTES = "http://192.168.11.2/zenpets/public/fetchYesVotes";
                HttpUrl.Builder builderYesVotes = HttpUrl.parse(URL_YES_VOTES).newBuilder();
                builderYesVotes.addQueryParameter("replyID", String.valueOf(params[1]));
                builderYesVotes.addQueryParameter("voteStatus", "0");
                String FINAL_NO_VOTES_URL = builderYesVotes.build().toString();
                OkHttpClient clientYesVotes = new OkHttpClient();
                Request requestYesVotes = new Request.Builder()
                        .url(FINAL_NO_VOTES_URL)
                        .build();
                Call call = clientYesVotes.newCall(requestYesVotes);
                try {
                    Response response = call.execute();
                    String strResult = response.body().string();
                    JSONObject JORoot = new JSONObject(strResult);
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        JSONArray JAYes = JORoot.getJSONArray("votes");
                        result = String.valueOf(JAYes.length());
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                votes.checkNotHelpfulVotes(s);

                /** SET THE HELPFUL VALUES **/
                if (HELPFUL_VOTES != null && s != null && view != null) {
                    AppCompatTextView helpful = (AppCompatTextView) view.findViewById(R.id.txtReplyHelpful);
                    helpful.setText(HELPFUL_VOTES + " / " + s + " found this helpful");
                }
            }
        }
    }
}