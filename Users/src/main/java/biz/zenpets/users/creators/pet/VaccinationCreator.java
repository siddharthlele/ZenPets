package biz.zenpets.users.creators.pet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Toast;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.pet.VaccinesAdapter;
import biz.zenpets.users.utils.models.pet.VaccinesData;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class VaccinationCreator extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    /** THE INCOMING PET ID **/
    private String PET_ID = null;

    /** DATA TYPES FOR HOLDING THE COLLECTED DATA **/
    private String VACCINE_ID = null;
    private String VACCINATION_NAME = null;
    private String VACCINATION_DATE = null;
    private String VACCINATION_NOTES = null;

    /** A PROGRESS DIALOG **/
    private ProgressDialog dialog;

    /** THE VACCINES SPINNER ADAPTER AND ARRAY LIST **/
    private VaccinesAdapter vaccinesAdapter;
    private final ArrayList<VaccinesData> arrVaccines = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.spnVaccineTypes) AppCompatSpinner spnVaccineTypes;
    @BindView(R.id.txtVaccinationDate) AppCompatTextView txtVaccinationDate;
    @BindView(R.id.inpVaccineNotes) TextInputLayout inpVaccineNotes;
    @BindView(R.id.edtVaccineNotes) AppCompatEditText edtVaccineNotes;

    /** SELECT THE PET'S DATE OF BIRTH **/
    @OnClick(R.id.btnVaccinationDate) void selectVaccinationDate()   {
        Calendar now = Calendar.getInstance();
        DatePickerDialog pickerDialog = DatePickerDialog.newInstance(
                VaccinationCreator.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show(getFragmentManager(), "DatePickerDialog");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vaccination_creator);
        ButterKnife.bind(this);

        /* CONFIGURE THE ACTIONBAR */
        configAB();

        /* GET THE INCOMING DATA */
        getIncomingData();

        /* SET THE CURRENT DATE */
        setCurrentDate();

        /* SELECT A VACCINE */
        spnVaccineTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /* GET THE SELECTED VACCINE ID AND NAME */
                VACCINE_ID = arrVaccines.get(position).getVaccineID();
                VACCINATION_NAME = arrVaccines.get(position).getVaccineName();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /***** CHECK VACCINATION DETAILS *****/
    private void checkVaccineDetails() {
        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtVaccineNotes.getWindowToken(), 0);
        
        /* GET THE VACCINE NOTES */
        if (TextUtils.isEmpty(edtVaccineNotes.getText().toString()))    {
            VACCINATION_NOTES = "Null";
        } else {
            VACCINATION_NOTES = edtVaccineNotes.getText().toString();

            /* POST THE NEW VACCINATION RECORD */
            postVaccinationRecord();
        }
    }

    /***** POST THE NEW VACCINATION RECORD *****/
    private void postVaccinationRecord() {
        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we publish the Vaccination record...");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        // String URL_NEW_PET = "http://leodyssey.com/ZenPets/public/newVaccination";
        String URL_NEW_PET = "http://192.168.11.2/zenpets/public/newVaccination";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("petID", PET_ID)
                .add("vaccineID", VACCINE_ID)
                .add("vaccinationDate", VACCINATION_DATE)
                .add("vaccinationNotes", VACCINATION_NOTES)
                .build();
        Request request = new Request.Builder()
                .url(URL_NEW_PET)
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
                        /* DISMISS THE DIALOG */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "Successfully added Vaccination record", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        });
                    } else {
                        /* DISMISS THE DIALOG */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "There was an error adding the Vaccination record. Please try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** FETCH THE LIST OF VACCINES *****/
    private void fetchVaccines() {
        // String URL_VACCINES = "http://leodyssey.com/ZenPets/public/allVaccines";
        String URL_VACCINES = "http://192.168.11.2/zenpets/public/allVaccines";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL_VACCINES)
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
                        JSONArray JAVaccines = JORoot.getJSONArray("vaccines");

                        /* AN INSTANCE OF THE VACCINES DATA CLASS */
                        VaccinesData data;

                        for (int i = 0; i < JAVaccines.length(); i++) {
                            JSONObject JOVaccines = JAVaccines.getJSONObject(i);
//                            Log.e("VACCINES", String.valueOf(JOVaccines));

                            /* INSTANTIATE THE VACCINES DATA INSTANCE */
                            data = new VaccinesData();

                            /* GET THE VACCINE ID */
                            if (JOVaccines.has("vaccineID"))    {
                                data.setVaccineID(JOVaccines.getString("vaccineID"));
                            } else {
                                data.setVaccineID(null);
                            }

                            /* GET THE VACCINE NAME */
                            if (JOVaccines.has("vaccineName"))  {
                                data.setVaccineName(JOVaccines.getString("vaccineName"));
                            } else {
                                data.setVaccineName(null);
                            }

                            /* GET THE VACCINE DESCRIPTION */
                            if (JOVaccines.has("vaccineDescription"))   {
                                data.setVaccineDescription(JOVaccines.getString("vaccineDescription"));
                            } else {
                                data.setVaccineDescription(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrVaccines.add(data);
                        }

                        /* INSTANTIATE THE VACCINES ADAPTER AND SET THE ADAPTER TO THE SPINNER */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE BREEDS ADAPTER */
                                vaccinesAdapter = new VaccinesAdapter(VaccinationCreator.this, arrVaccines);

                                /* SET THE ADAPTER TO THE VACCINES SPINNER */
                                spnVaccineTypes.setAdapter(vaccinesAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "New Vaccination";
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
        MenuInflater inflater = new MenuInflater(VaccinationCreator.this);
        inflater.inflate(R.menu.activity_save_cancel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.menuSave:
                /* CHECK VACCINATION DETAILS */
                checkVaccineDetails();
                break;
            case R.id.menuCancel:
                /* CANCEL CATEGORY CREATION */
                finish();
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
                /* FETCH THE LIST OF VACCINES */
                fetchVaccines();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** SET THE CURRENT DATE *****/
    private void setCurrentDate() {
        /* SET THE CURRENT DATE (DISPLAY ONLY !!!!) */
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());
        txtVaccinationDate.setText(formattedDate);

        /* SET THE CURRENT DATE (DATABASE ONLY !!!!) */
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        VACCINATION_DATE = sdf.format(cal.getTime());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int month, int date) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        /* FOR THE DATABASE ONLY !!!! */
        VACCINATION_DATE = sdf.format(cal.getTime());

        /* FOR DISPLAY ONLY !!!! */
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String selectedDate = dateFormat.format(cal.getTime());
        txtVaccinationDate.setText(selectedDate);
    }
}