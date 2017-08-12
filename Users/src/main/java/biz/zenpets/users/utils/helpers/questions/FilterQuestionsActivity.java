package biz.zenpets.users.utils.helpers.questions;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.problems.NewProblemsListAdapter;
import biz.zenpets.users.utils.models.problems.ProblemData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class FilterQuestionsActivity extends AppCompatActivity {

    /** THE INCOMING PROBLEM ID AND TEXT **/
    String INCOMING_PROBLEM_ID = null;

    /** THE SELECTED PROBLEM ID AND PROBLEM TEXT **/
    private String SELECTED_PROBLEM_ID = null;
    String SELECTED_PROBLEM_TEXT = null;

    /** THE PROBLEMS LIST ADAPTER AND ARRAY LIST **/
    private NewProblemsListAdapter problemsAdapter;
    private final ArrayList<ProblemData> arrProblems = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.txtClear) AppCompatTextView txtClear;
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.listProblems) ListView listProblems;
    @BindView(R.id.btnApplyFilter) AppCompatButton btnApplyFilter;

    /** APPLY THE FILTER AND PASS BACK THE SELECTED PROBLEM ID **/
    @OnClick(R.id.btnApplyFilter) void applyFilter()    {
        Intent intent = new Intent();
        intent.putExtra("PROBLEM_ID", SELECTED_PROBLEM_ID);
        intent.putExtra("PROBLEM_TEXT", SELECTED_PROBLEM_TEXT);
        setResult(RESULT_OK, intent);
        finish();
    }

    /** CLEAR THE SELECTION **/
    @OnClick(R.id.txtClear) void clearSelection()   {
        if (INCOMING_PROBLEM_ID != null)    {

            /* CLEAR THE SELECTION */
            SELECTED_PROBLEM_ID = null;
            SELECTED_PROBLEM_TEXT = null;

            /* REMOVE THE SELECTION */
            problemsAdapter.setSelectedIndex(-1);
            problemsAdapter.notifyDataSetChanged();
            txtClear.setVisibility(View.GONE);
            btnApplyFilter.setVisibility(View.VISIBLE);

        } else {
            /* REMOVE THE SELECTION */
            problemsAdapter.setSelectedIndex(-1);
            problemsAdapter.notifyDataSetChanged();
            txtClear.setVisibility(View.GONE);
            btnApplyFilter.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.filter_questions_activity);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* FETCH THE LIST OF PROBLEMS */
        new fetchListOfProblems().execute();

        listProblems.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                /* MODIFY THE ADAPTER TO REFLECT THE SELECTION*/
                problemsAdapter.setSelectedIndex(position);
                problemsAdapter.notifyDataSetChanged();

                /* GET THE SELECTED PROBLEM ID */
                SELECTED_PROBLEM_ID = arrProblems.get(position).getProblemID();
                SELECTED_PROBLEM_TEXT = arrProblems.get(position).getProblemText();

                /* SHOW OR HIDE THE APPLY BUTTON */
                if (position < 0)   {
                    btnApplyFilter.setVisibility(View.GONE);
                } else {
                    btnApplyFilter.setVisibility(View.VISIBLE);
                }

                /* SHOW OR HIDE THE CLEAR BUTTON */
                if (position < 0)   {
                    txtClear.setVisibility(View.GONE);
                } else {
                    txtClear.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /***** FETCH THE LIST OF PROBLEMS *****/
    private class fetchListOfProblems extends AsyncTask<Void, Void, Void>   {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* SHOW THE PROGRESS WHILE FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_PROBLEM_TYPES = "http://leodyssey.com/ZenPets/public/allProblemTypes";
            String URL_PROBLEM_TYPES = "http://192.168.11.2/zenpets/public/allProblemTypes";
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(URL_PROBLEM_TYPES)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    JSONArray JAProblems = JORoot.getJSONArray("problems");
                    ProblemData data;
                    for (int i = 0; i < JAProblems.length(); i++) {
                        JSONObject JOProblems = JAProblems.getJSONObject(i);
                        data = new ProblemData();

                        /* GET THE PROBLEM ID */
                        if (JOProblems.has("problemID"))    {
                            data.setProblemID(JOProblems.getString("problemID"));
                        } else {
                            data.setProblemID(null);
                        }

                        /* GET THE PROBLEM TEXT */
                        if (JOProblems.has("problemText"))    {
                            data.setProblemText(JOProblems.getString("problemText"));
                        } else {
                            data.setProblemText(null);
                        }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                        arrProblems.add(data);
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

            /* INSTANTIATE THE PROBLEMS LIST ADAPTER */
            problemsAdapter = new NewProblemsListAdapter(FilterQuestionsActivity.this, arrProblems);

            /* SET THE ADAPTER TO THE PROBLEMS RECYCLER VIEW */
            listProblems.setAdapter(problemsAdapter);

            /* HIDE THE PROGRESS AFTER FETCHING THE DATA */
            linlaHeaderProgress.setVisibility(View.GONE);

            /* GET THE INCOMING DATA (IF AVAILABLE) */
            Bundle bundle = getIntent().getExtras();
            if (bundle != null && bundle.containsKey("PROBLEM_ID")) {
                INCOMING_PROBLEM_ID = bundle.getString("PROBLEM_ID");
                int intProblemPosition = getProblemIndex(arrProblems, INCOMING_PROBLEM_ID);
                listProblems.setSelection(intProblemPosition);

                /* MODIFY THE ADAPTER TO REFLECT THE SELECTION*/
                problemsAdapter.setSelectedIndex(intProblemPosition);
                problemsAdapter.notifyDataSetChanged();

                /* SHOW THE CLEAR BUTTON */
                txtClear.setVisibility(View.VISIBLE);
            } else {
                INCOMING_PROBLEM_ID = null;
            }
        }
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "Filter";
//        String strTitle = getString(R.string.add_a_new_pet);
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
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    /***** GET THE PROBLEM POSITION *****/
    private int getProblemIndex(ArrayList<ProblemData> array, String problemID) {
        int index = 0;
        for (int i =0; i < array.size(); i++) {
            if (array.get(i).getProblemID().equalsIgnoreCase(problemID))   {
                index = i;
                break;
            }
        }
        return index;
    }
}