package biz.zenpets.users.details.doctor.reviews;

import android.app.Activity;
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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.mikepenz.iconics.view.IconicsImageView;

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
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.helpers.reviews.ReviewHelpful;
import biz.zenpets.users.utils.helpers.reviews.ReviewNotHelpful;
import biz.zenpets.users.utils.models.reviews.ReviewsData;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DoctorReviews extends AppCompatActivity implements ReviewHelpful, ReviewNotHelpful {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** THE INCOMING DOCTOR ID **/
    private String DOCTOR_ID = null;

    /** THE REVIEWS AND SUBSET ADAPTER AND ARRAY LISTS **/
    private AllReviewsAdapter reviewsAdapter;
    private final ArrayList<ReviewsData> arrReviews = new ArrayList<>();

    /** THE HELPFUL REVIEW VOTES **/
    private String HELPFUL_REVIEW_VOTES = null;
    String NON_HELPFUL_REVIEW_VOTES = null;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.txtFeedback) AppCompatTextView txtFeedback;
    @BindView(R.id.txtDoctorName) AppCompatTextView txtDoctorName;
    @BindView(R.id.linlaProgress) LinearLayout linlaProgress;
    @BindView(R.id.listDoctorReviews) RecyclerView listDoctorReviews;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.custom_all_reviews_list);
        ButterKnife.bind(this);

        /* INSTANTIATE THE ADAPTER */
        reviewsAdapter = new AllReviewsAdapter(DoctorReviews.this, arrReviews);

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* CONFIGURE THE TOOLBAR */
        configTB();
    }

    /***** GET THE DOCTOR'S REVIEWS *****/
    private class getDoctorReviews extends AsyncTask<Void, Void, Void>  {

        /* THE REVIEWS COUNT */
        int REVIEW_COUNT = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_DOCTOR_SERVICES = "http://leodyssey.com/ZenPets/public/fetchDoctorReviews";
            String URL_DOCTOR_SERVICES = "http://192.168.11.2/zenpets/public/fetchDoctorReviews";
            HttpUrl.Builder builder = HttpUrl.parse(URL_DOCTOR_SERVICES).newBuilder();
            builder.addQueryParameter("doctorID", DOCTOR_ID);
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
                    JSONArray JAReviews = JORoot.getJSONArray("reviews");
                    if (JAReviews.length() > 0) {
                        /* GET THE REVIEWS COUNT */
                        REVIEW_COUNT = JAReviews.length();

                        /* A REVIEWS DATA INSTANCE */
                        ReviewsData data;
                        for (int i = 0; i < JAReviews.length(); i++) {
                            JSONObject JOReviews = JAReviews.getJSONObject(i);

                            /** INSTANTIATE THE REVIEWS DATA INSTANCE **/
                            data = new ReviewsData();

                            /** GET THE REVIEW ID **/
                            if (JOReviews.has("reviewID"))  {
                                data.setReviewID(JOReviews.getString("reviewID"));
                            } else {
                                data.setReviewID(null);
                            }

                            /** GET THE DOCTOR ID **/
                            if (JOReviews.has("doctorID"))  {
                                data.setDoctorID(JOReviews.getString("doctorID"));
                            } else {
                                data.setDoctorID(null);
                            }

                            /** GET THE USER ID **/
                            if (JOReviews.has("userID"))    {
                                data.setUserID(JOReviews.getString("userID"));
                            } else {
                                data.setUserID(null);
                            }

                            /** GET THE USER NAME **/
                            if (JOReviews.has("userName"))  {
                                data.setUserName(JOReviews.getString("userName"));
                            } else {
                                data.setUserName(null);
                            }

                            /** SET THE POSTER USER ID **/
                            if (getApp().getUserID() != null)   {
                                data.setPosterUserID(getApp().getUserID());
                            } else {
                                data.setPosterUserID(null);
                            }

                            /** GET THE VISIT REASON ID **/
                            if (JOReviews.has("visitReasonID"))    {
                                data.setVisitReasonID(JOReviews.getString("visitReasonID"));
                            } else {
                                data.setVisitReasonID(null);
                            }

                            /** GET THE VISIT REASON **/
                            if (JOReviews.has("visitReason"))   {
                                data.setVisitReason("Visited for " + JOReviews.getString("visitReason"));
                            } else {
                                data.setVisitReason(null);
                            }

                            /** GET THE RECOMMEND STATUS **/
                            if (JOReviews.has("recommendStatus"))    {
                                data.setRecommendStatus(JOReviews.getString("recommendStatus"));
                            } else {
                                data.setRecommendStatus(null);
                            }

                            /** GET THE APPOINTMENT STATUS **/
                            if (JOReviews.has("appointmentStatus"))    {
                                data.setAppointmentStatus(JOReviews.getString("appointmentStatus"));
                            } else {
                                data.setAppointmentStatus(null);
                            }

                            /** GET THE DOCTOR RATING **/
                            if (JOReviews.has("doctorRating"))    {
                                data.setDoctorRating(JOReviews.getString("doctorRating"));
                            } else {
                                data.setDoctorRating(null);
                            }

                            /** GET THE DOCTOR EXPERIENCE **/
                            if (JOReviews.has("doctorExperience"))    {
                                data.setDoctorExperience(JOReviews.getString("doctorExperience"));
                            } else {
                                data.setDoctorExperience(null);
                            }

                            /** GET THE REVIEW TIMESTAMP **/
                            if (JOReviews.has("reviewTimestamp"))    {
                                String reviewTimestamp = JOReviews.getString("reviewTimestamp");
                                long lngTimeStamp = Long.parseLong(reviewTimestamp) * 1000;
                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(lngTimeStamp);
                                Date date = calendar.getTime();
                                PrettyTime prettyTime = new PrettyTime();
                                String strDate = prettyTime.format(date);
                                data.setReviewTimestamp(strDate);
                            } else {
                                data.setReviewTimestamp(null);
                            }

                            /** GET THE HELPFUL VOTES ("YES") **/
                            // String URL_YES_VOTES = "http://leodyssey.com/ZenPets/public/fetchPositiveReviewVotes";
                            String URL_YES_VOTES = "http://192.168.11.2/zenpets/public/fetchPositiveReviewVotes";
                            HttpUrl.Builder builderYesVotes = HttpUrl.parse(URL_YES_VOTES).newBuilder();
                            builderYesVotes.addQueryParameter("reviewID", JOReviews.getString("reviewID"));
                            builderYesVotes.addQueryParameter("reviewVoteText", "Yes");
                            String FINAL_YES_VOTES_URL = builderYesVotes.build().toString();
//                            Log.e("YES VOTES", FINAL_YES_VOTES_URL);
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
                                data.setReviewHelpFulYes(String.valueOf(JAYes.length()));
                            }

                            /** GET THE NOT HELPFUL VOTES ("NO") **/
                            // String URL_NO_VOTES = "http://leodyssey.com/ZenPets/public/fetchNegativeReviewVotes";
                            String URL_NO_VOTES = "http://192.168.11.2/zenpets/public/fetchNegativeReviewVotes";
                            HttpUrl.Builder builderNoVotes = HttpUrl.parse(URL_NO_VOTES).newBuilder();
                            builderNoVotes.addQueryParameter("reviewID", JOReviews.getString("reviewID"));
                            builderNoVotes.addQueryParameter("reviewVoteText", "No");
                            String FINAL_NO_VOTES_URL = builderNoVotes.build().toString();
//                            Log.e("NO VOTES", FINAL_NO_VOTES_URL);
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
                                data.setReviewHelpFulNo(String.valueOf(JANo.length()));
                            }

                            /** ADD THE COLLECTED DATA TO THE ARRAY LIST **/
                            arrReviews.add(data);
                        }

                        /* SHOW THE RECYCLER VIEW AND HIDE THE EMPTY LAYOUT */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                listDoctorReviews.setVisibility(View.VISIBLE);
                                linlaEmpty.setVisibility(View.GONE);
                            }
                        });
                    } else {
                        /* SHOW THE NO REVIEWS CONTAINER */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                linlaEmpty.setVisibility(View.VISIBLE);
                                listDoctorReviews.setVisibility(View.GONE);
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

            /* INSTANTIATE THE ADAPTER */
            reviewsAdapter = new AllReviewsAdapter(DoctorReviews.this, arrReviews);

            /* SET THE SERVICES ADAPTER TO THE RECYCLER VIEW */
            listDoctorReviews.setAdapter(reviewsAdapter);

            /* SET THE REVIEWS COUNT */
            txtFeedback.setText("Feedback (" + REVIEW_COUNT +")");

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            linlaProgress.setVisibility(View.GONE);
        }
    }

    /** GET THE INCOMING DATA **/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID")
                && bundle.containsKey("DOCTOR_PREFIX")
                && bundle.containsKey("DOCTOR_NAME")) {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            String DOCTOR_PREFIX = bundle.getString("DOCTOR_PREFIX");
            String DOCTOR_NAME = bundle.getString("DOCTOR_NAME");
            if (DOCTOR_ID != null && DOCTOR_PREFIX != null && DOCTOR_NAME != null)  {
                /* SET THE DOCTOR'S NAME */
                txtDoctorName.setText(DOCTOR_PREFIX + " " + DOCTOR_NAME);

                /** GET THE DOCTOR'S REVIEWS **/
                new getDoctorReviews().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /***** CONFIGURE THE RECYCLER VIEW *****/
    private void configRecycler() {
        LinearLayoutManager llmReviews = new LinearLayoutManager(this);
        llmReviews.setOrientation(LinearLayoutManager.VERTICAL);
        llmReviews.setAutoMeasureEnabled(true);
        listDoctorReviews.setLayoutManager(llmReviews);
        listDoctorReviews.setHasFixedSize(false);
        listDoctorReviews.setNestedScrollingEnabled(false);
        listDoctorReviews.setAdapter(reviewsAdapter);
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Doctor Details";
//        String strTitle = getString(R.string.add_a_new_medicine_record);
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getApplicationContext()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setSubtitle(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            default:
                break;
        }
        return false;
    }

    /***** THE REVIEWS ADAPTER *****/
    private class AllReviewsAdapter extends RecyclerView.Adapter<AllReviewsAdapter.ReviewsVH> {

        /***** THE ACTIVITY INSTANCE FOR USE IN THE ADAPTER *****/
        private final Activity activity;

        /***** ARRAY LIST TO GET DATA FROM THE ACTIVITY *****/
        private final ArrayList<ReviewsData> arrReviews;

        private AllReviewsAdapter(Activity activity, ArrayList<ReviewsData> arrReviews) {

            /* CAST THE ACTIVITY IN THE GLOBAL ACTIVITY INSTANCE */
            this.activity = activity;

            /* CAST THE CONTENTS OF THE ARRAY LIST IN THE METHOD TO THE LOCAL INSTANCE */
            this.arrReviews = arrReviews;
        }

        @Override
        public int getItemCount() {
            return arrReviews.size();
        }

        @Override
        public void onBindViewHolder(final ReviewsVH holder, final int position) {
            final ReviewsData data = arrReviews.get(position);

            /** SET THE RECOMMEND STATUS **/
            String strRecommendStatus = data.getRecommendStatus();
            if (strRecommendStatus.equalsIgnoreCase("Yes")) {
                holder.imgvwLikeStatus.setIcon("faw-thumbs-o-up");
                holder.imgvwLikeStatus.setColor(ContextCompat.getColor(activity, android.R.color.holo_blue_dark));
            } else if (strRecommendStatus.equalsIgnoreCase("No"))   {
                holder.imgvwLikeStatus.setIcon("faw-thumbs-o-down");
                holder.imgvwLikeStatus.setColor(ContextCompat.getColor(activity, android.R.color.holo_red_dark));
            }

            /** GET THE FIRST CHARACTER OF THE USER NAME **/
            String strFirstChar = data.getUserName().substring(0, 1);
            holder.txtNameStart.setText(strFirstChar);

            /** SET THE VISIT REASON **/
            if (data.getVisitReason() != null)  {
                holder.txtVisitReason.setText(data.getVisitReason());
            }

            /** SET THE VISIT EXPERIENCE **/
            if (data.getDoctorExperience() != null) {
                holder.txtVisitExperience.setText(data.getDoctorExperience());
            }

            /** SET THE USER NAME **/
            if (data.getUserName() != null) {
                holder.txtUserName.setText(data.getUserName());
            }

            /** SET THE TIMESTAMP **/
            if (data.getReviewTimestamp() != null)  {
                holder.txtTimeStamp.setText(data.getReviewTimestamp());
            }

            /** GET THE NUMBER OF HELPFUL VOTES **/
            getHelpVotes helpVotes = new getHelpVotes(DoctorReviews.this);
            helpVotes.execute(holder.txtHelpfulYes, data.getReviewID());

            /** GET THE NUMBER OF NON HELPFUL VOTES **/
            getNonHelpVotes nonHelpVotes = new getNonHelpVotes(DoctorReviews.this);
            nonHelpVotes.execute(holder.txtHelpfulNo, data.getReviewID());

            /** ADD A HELPFUL VOTE **/
            holder.txtHelpfulYes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // String URL_CHECK_VOTE = "http://leodyssey.com/ZenPets/public/checkUserReviewVote";
                    String URL_CHECK_VOTE = "http://192.168.11.2/zenpets/public/checkUserReviewVote";
                    HttpUrl.Builder builder = HttpUrl.parse(URL_CHECK_VOTE).newBuilder();
                    builder.addQueryParameter("reviewID", data.getReviewID());
                    builder.addQueryParameter("userID", data.getPosterUserID());
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

                                            /** GET THE REVIEW VOTE ID **/
                                            String reviewVoteID = JOVotes.getString("reviewVoteID");

                                            /** GET THE VOTE STATUS **/
                                            if (JOVotes.has("reviewVoteText"))  {
                                                String reviewVoteText = JOVotes.getString("reviewVoteText");

                                                if (reviewVoteText.equalsIgnoreCase("Yes"))   {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(activity, "You have already marked this Review as helpful", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else if (reviewVoteText.equalsIgnoreCase("No"))    {
                                                    // String URL_UPDATE_VOTE = "http://leodyssey.com/ZenPets/public/updateReviewVoteYes";
                                                    String URL_UPDATE_VOTE = "http://192.168.11.2/zenpets/public/updateReviewVoteYes";
                                                    OkHttpClient client = new OkHttpClient();
                                                    RequestBody body = new FormBody.Builder()
                                                            .add("reviewVoteID", reviewVoteID)
                                                            .add("reviewVoteText", "Yes")
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
//                                                                Log.e("YES RESULT", String.valueOf(JORoot));
                                                                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                                                                    /** GET THE NUMBER OF HELPFUL VOTES **/
                                                                    getHelpVotes helpVotes = new getHelpVotes(DoctorReviews.this);
                                                                    helpVotes.execute(holder.txtHelpfulYes, data.getReviewID());

                                                                    /** GET THE NUMBER OF NON HELPFUL VOTES **/
                                                                    getNonHelpVotes nonHelpVotes = new getNonHelpVotes(DoctorReviews.this);
                                                                    nonHelpVotes.execute(holder.txtHelpfulNo, data.getReviewID());
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
                                        // String URL_NEW_VOTE = "http://leodyssey.com/ZenPets/public/newReviewVote";
                                        String URL_NEW_VOTE = "http://192.168.11.2/zenpets/public/newReviewVote";
                                        OkHttpClient client = new OkHttpClient();
                                        RequestBody body = new FormBody.Builder()
                                                .add("reviewID", data.getReviewID())
                                                .add("userID", data.getPosterUserID())
                                                .add("reviewVoteText", "Yes")
                                                .add("reviewVoteTimestamp", String.valueOf(System.currentTimeMillis() / 1000))
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
                                                        getHelpVotes helpVotes = new getHelpVotes(DoctorReviews.this);
                                                        helpVotes.execute(holder.txtHelpfulYes, data.getReviewID());

                                                        /** GET THE NUMBER OF NON HELPFUL VOTES **/
                                                        getNonHelpVotes nonHelpVotes = new getNonHelpVotes(DoctorReviews.this);
                                                        nonHelpVotes.execute(holder.txtHelpfulNo, data.getReviewID());
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

            /* ADD A NOT HELPFUL VOTE */
            holder.txtHelpfulNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // String URL_CHECK_VOTE = "http://leodyssey.com/ZenPets/public/checkUserReviewVote";
                    String URL_CHECK_VOTE = "http://192.168.11.2/zenpets/public/checkUserReviewVote";
                    HttpUrl.Builder builder = HttpUrl.parse(URL_CHECK_VOTE).newBuilder();
                    builder.addQueryParameter("reviewID", data.getReviewID());
                    builder.addQueryParameter("userID", data.getPosterUserID());
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

                                            /** GET THE REVIEW VOTE ID **/
                                            String reviewVoteID = JOVotes.getString("reviewVoteID");

                                            /** GET THE VOTE STATUS **/
                                            if (JOVotes.has("reviewVoteText"))  {
                                                String reviewVoteText = JOVotes.getString("reviewVoteText");

                                                if (reviewVoteText.equalsIgnoreCase("No"))   {
                                                    activity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            Toast.makeText(activity, "You have already voted this reply as not helpful", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                                } else if (reviewVoteText.equalsIgnoreCase("Yes"))    {
                                                    // String URL_UPDATE_VOTE = "http://leodyssey.com/ZenPets/public/updateReviewVoteNo";
                                                    String URL_UPDATE_VOTE = "http://192.168.11.2/zenpets/public/updateReviewVoteNo";
                                                    OkHttpClient client = new OkHttpClient();
                                                    RequestBody body = new FormBody.Builder()
                                                            .add("reviewVoteID", reviewVoteID)
                                                            .add("reviewVoteText", "No")
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
//                                                                Log.e("NO RESULT", String.valueOf(JORoot));
                                                                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                                                                    /** GET THE NUMBER OF HELPFUL VOTES **/
                                                                    getHelpVotes helpVotes = new getHelpVotes(DoctorReviews.this);
                                                                    helpVotes.execute(holder.txtHelpfulYes, data.getReviewID());

                                                                    /** GET THE NUMBER OF NON HELPFUL VOTES **/
                                                                    getNonHelpVotes nonHelpVotes = new getNonHelpVotes(DoctorReviews.this);
                                                                    nonHelpVotes.execute(holder.txtHelpfulNo, data.getReviewID());
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
                                        // String URL_NEW_VOTE = "http://leodyssey.com/ZenPets/public/newReviewVote";
                                        String URL_NEW_VOTE = "http://192.168.11.2/zenpets/public/newReviewVote";
                                        OkHttpClient client = new OkHttpClient();
                                        RequestBody body = new FormBody.Builder()
                                                .add("reviewID", data.getReviewID())
                                                .add("userID", data.getPosterUserID())
                                                .add("reviewVoteText", "No")
                                                .add("reviewVoteTimestamp", String.valueOf(System.currentTimeMillis() / 1000))
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
                                                        getHelpVotes helpVotes = new getHelpVotes(DoctorReviews.this);
                                                        helpVotes.execute(holder.txtHelpfulYes, data.getReviewID());

                                                        /** GET THE NUMBER OF NON HELPFUL VOTES **/
                                                        getNonHelpVotes nonHelpVotes = new getNonHelpVotes(DoctorReviews.this);
                                                        nonHelpVotes.execute(holder.txtHelpfulNo, data.getReviewID());
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
        public ReviewsVH onCreateViewHolder(ViewGroup parent, int i) {

            View itemView = LayoutInflater.
                    from(parent.getContext()).
                    inflate(R.layout.custom_all_reviews_item, parent, false);

            return new ReviewsVH(itemView);
        }

        class ReviewsVH extends RecyclerView.ViewHolder	{
            final AppCompatTextView txtNameStart;
            final AppCompatTextView txtUserName;
            final AppCompatTextView txtTimeStamp;
            final IconicsImageView imgvwLikeStatus;
            final AppCompatTextView txtVisitReason;
            final AppCompatTextView txtVisitExperience;
            final AppCompatTextView txtHelpfulNo;
            final AppCompatTextView txtHelpfulYes;

            ReviewsVH(View v) {
                super(v);
                txtNameStart = (AppCompatTextView) itemView.findViewById(R.id.txtNameStart);
                txtUserName = (AppCompatTextView) itemView.findViewById(R.id.txtUserName);
                txtTimeStamp = (AppCompatTextView) itemView.findViewById(R.id.txtTimeStamp);
                imgvwLikeStatus = (IconicsImageView) itemView.findViewById(R.id.imgvwLikeStatus);
                txtVisitReason = (AppCompatTextView) itemView.findViewById(R.id.txtVisitReason);
                txtVisitExperience = (AppCompatTextView) itemView.findViewById(R.id.txtVisitExperience);
                txtHelpfulNo = (AppCompatTextView) itemView.findViewById(R.id.txtHelpfulNo);
                txtHelpfulYes = (AppCompatTextView) itemView.findViewById(R.id.txtHelpfulYes);
            }
        }

        /** GET THE NUMBER OF HELPFUL VOTES **/
        private class getHelpVotes extends AsyncTask<Object, Void, String>  {
            ReviewHelpful votes;
            String result = null;
            View view;

            getHelpVotes(ReviewHelpful votes) {
                this.votes = votes;
            }

            @Override
            protected String doInBackground(Object... params) {
                view = (View) params[0];
                // String URL_YES_VOTES = "http://leodyssey.com/ZenPets/public/fetchPositiveReviewVotes";
                String URL_YES_VOTES = "http://192.168.11.2/zenpets/public/fetchPositiveReviewVotes";
                HttpUrl.Builder builderYesVotes = HttpUrl.parse(URL_YES_VOTES).newBuilder();
                builderYesVotes.addQueryParameter("reviewID", String.valueOf(params[1]));
                builderYesVotes.addQueryParameter("reviewVoteText", "Yes");
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
                votes.checkHelpfulReviewVotes(s);

                /** SET THE HELPFUL REVIEW VOTES VALUES **/
                if (HELPFUL_REVIEW_VOTES != null && s != null && view != null) {
                    AppCompatTextView txtHelpfulYes = (AppCompatTextView) view.findViewById(R.id.txtHelpfulYes);
                    txtHelpfulYes.setText("YES (" + HELPFUL_REVIEW_VOTES + ")");
                }
            }
        }

        /** GET THE NUMBER OF NOT HELPFUL VOTES **/
        private class getNonHelpVotes extends AsyncTask<Object, Void, String> {

            ReviewNotHelpful votes;
            String result = null;
            View view;

            getNonHelpVotes(ReviewNotHelpful votes) {
                this.votes = votes;
            }

            @Override
            protected String doInBackground(Object... params) {
                view = (View) params[0];
                // String URL_NO_VOTES = "http://leodyssey.com/ZenPets/public/fetchNegativeReviewVotes";
                String URL_NO_VOTES = "http://192.168.11.2/zenpets/public/fetchNegativeReviewVotes";
                HttpUrl.Builder builderNoVotes = HttpUrl.parse(URL_NO_VOTES).newBuilder();
                builderNoVotes.addQueryParameter("reviewID", String.valueOf(params[1]));
                builderNoVotes.addQueryParameter("reviewVoteText", "No");
                String FINAL_NO_VOTES_URL = builderNoVotes.build().toString();
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
                votes.checkNotHelpfulReviewVotes(s);

                /** SET THE HELPFUL REVIEW VOTES VALUES **/
                if (NON_HELPFUL_REVIEW_VOTES != null && s != null && view != null) {
                    AppCompatTextView txtHelpfulNo = (AppCompatTextView) view.findViewById(R.id.txtHelpfulNo);
                    txtHelpfulNo.setText("NO (" + NON_HELPFUL_REVIEW_VOTES + ")");
                }
            }
        }
    }

    @Override
    public void checkHelpfulReviewVotes(String response) {
        if (response != null)   {
            HELPFUL_REVIEW_VOTES = response;
        } else {
            HELPFUL_REVIEW_VOTES = "0";
        }
    }

    @Override
    public void checkNotHelpfulReviewVotes(String response) {
        NON_HELPFUL_REVIEW_VOTES = null;
        if (response != null)   {
            NON_HELPFUL_REVIEW_VOTES = response;
        } else {
            NON_HELPFUL_REVIEW_VOTES = "0";
        }
    }
}