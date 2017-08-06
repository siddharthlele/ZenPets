package biz.zenpets.users.details.pet;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.joda.time.LocalDate;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.details.pet.modules.PetMedicalRecords;
import biz.zenpets.users.details.pet.modules.PetVaccinations;
import biz.zenpets.users.utils.TypefaceSpan;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class NewPetDetails extends AppCompatActivity {

    /** THE INCOMING PET ID **/
    private String PET_ID = null;

    /** STRING TO HOLD THE PET DETAILS **/
    private String PET_NAME = null;
    private String PET_PROFILE = null;
    private String PET_GENDER = null;
    private String PET_AGE = null;
    private String BREED_NAME = null;

    /** THE TAB ICONS **/
    private int[] tabIcons = {
            R.drawable.ic_action_needle_dark,
            R.drawable.ic_action_heartbeat_dark
    };

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.myToolbar) Toolbar myToolbar;
    @BindView(R.id.imgvwPetProfile) CircleImageView imgvwPetProfile;
    @BindView(R.id.txtPetName) AppCompatTextView txtPetName;
    @BindView(R.id.txtPetDetails) AppCompatTextView txtPetDetails;
    @BindView(R.id.tabLayout) TabLayout tabLayout;
    @BindView(R.id.viewPager) ViewPager viewPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_pet_details);
        ButterKnife.bind(this);

        /* CONFIGURE THE TOOLBAR */
        configToolbar();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /** SETUP THE VIEW PAGER **/
        setupViewPager(viewPager);

        /** SETUP THE TAB LAYOUT **/
        tabLayout.setupWithViewPager(viewPager);

        /** SETUP THE TAB ICONS **/
//        setupTabIcons();

        /** APPLY THE CUSTOM FONT TO THE TITLES **/
        changeTabsFont();
    }

    /***** SETUP THE TAB ICONS *****/
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
    }

    /** SETUP THE VIEW PAGER **/
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new PetVaccinations(), "Vaccinations");
        adapter.addFragment(new PetMedicalRecords(), "Records");
        viewPager.setAdapter(adapter);
    }

    /** THE PAGER ADAPTER **/
    private class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    /** APPLY THE CUSTOM FONT TO THE TITLES **/
    private void changeTabsFont() {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Roboto-Regular.ttf"));
                }
            }
        }
    }

    /** CONFIGURE THE TOOLBAR **/
    @SuppressWarnings("ConstantConditions")
    private void configToolbar() {
        setSupportActionBar(myToolbar);
        String strTitle = "My Appointments";
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

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("PET_ID"))   {
            PET_ID = bundle.getString("PET_ID");
            if (PET_ID != null) {
                /* FETCH THE PET DETAILS */
                new fetchPetDetails().execute();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** FETCH THE PET DETAILS *****/
    private class fetchPetDetails extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            /* SHOW THE PROGRESS WHILE LOADING THE DATA */
//            linlaHeaderProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_PET_DETAILS = "http://leodyssey.com/ZenPets/public/fetchPetDetails";
            String URL_PET_DETAILS = "http://192.168.11.2/zenpets/public/fetchPetDetails";
            HttpUrl.Builder builder = HttpUrl.parse(URL_PET_DETAILS).newBuilder();
            builder.addQueryParameter("petID", PET_ID);
            String FINAL_URL = builder.build().toString();
            Log.e("PET DETAILS", FINAL_URL);
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

                    /* GET THE PET NAME */
                    if (JORoot.has("petName"))  {
                        PET_NAME = JORoot.getString("petName");
                    }

                    /* GET THE PET PROFILE */
                    if (JORoot.has("petProfile"))   {
                        PET_PROFILE = JORoot.getString("petProfile");
                    } else {
                        PET_PROFILE = null;
                    }

                    /* GET THE PET DETAILS */
                    if (JORoot.has("petTypeName")
                            && JORoot.has("breedName")
                            && JORoot.has("petGender")
                            && JORoot.has("petDOB"))  {
                        BREED_NAME = JORoot.getString("breedName");
                        PET_GENDER = JORoot.getString("petGender");
                        PET_AGE = calculatePetAge(JORoot.getString("petDOB"));
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

            /* SET THE PET NAME */
            txtPetName.setText(PET_NAME);
            myToolbar.setTitle(PET_NAME);

            /* SET THE PET PROFILE */
            if (PET_PROFILE != null) {
                Picasso.with(NewPetDetails.this)
                        .load(PET_PROFILE)
                        .centerCrop()
                        .fit()
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .into(imgvwPetProfile, new com.squareup.picasso.Callback() {
                            @Override
                            public void onSuccess() {
                            }

                            @Override
                            public void onError() {
                                Picasso.with(NewPetDetails.this)
                                        .load(PET_PROFILE)
                                        .centerCrop()
                                        .fit()
                                        .into(imgvwPetProfile);
                            }
                        });
            } else {
                imgvwPetProfile.setImageDrawable(ContextCompat.getDrawable(NewPetDetails.this, R.drawable.beagle));
            }

            /* SET THE PET DETAILS */
            if (PET_GENDER != null && BREED_NAME != null && PET_AGE != null)   {
                String combinedDetails = PET_GENDER + " " + BREED_NAME + ", aged " + PET_AGE;
                txtPetDetails.setText(combinedDetails);
            }

//            /* FETCH THE PET'S VACCINATION RECORDS */
//            new fetchVaccinationRecords().execute();
        }
    }

    private String calculatePetAge(String petDOB) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            /* SET THE DATE OF BIRTH TO A CALENDAR DATE */
            Date dtDOB = format.parse(petDOB);
            Calendar calDOB = Calendar.getInstance();
            calDOB.setTime(dtDOB);
            int dobYear = calDOB.get(Calendar.YEAR);
            int dobMonth = calDOB.get(Calendar.MONTH) + 1;
            int dobDate = calDOB.get(Calendar.DATE);

            /* SET THE CURRENT DATE TO A CALENDAR INSTANCE */
            Calendar calNow = Calendar.getInstance();
            int nowYear = calNow.get(Calendar.YEAR);
            int nowMonth = calNow.get(Calendar.MONTH) + 1;
            int nowDate = calNow.get(Calendar.DATE);

            LocalDate dateDOB = new LocalDate(dobYear, dobMonth, dobDate);
            LocalDate dateNOW = new LocalDate(nowYear, nowMonth, nowDate);
            Period period = new Period(dateDOB, dateNOW, PeriodType.yearMonthDay());
//            Log.e("AGE", period.getYears() + " Years, " + period.getMonths() + " Months and " + period.getDays() + " Days");

            /* CALCULATE THE PET'S AGE */
            return period.getYears() + " Years, " + period.getMonths() + " Months and " + period.getDays() + " Days";
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}