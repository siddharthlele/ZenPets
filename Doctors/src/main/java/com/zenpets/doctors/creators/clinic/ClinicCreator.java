package com.zenpets.doctors.creators.clinic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
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
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;

import com.zenpets.doctors.utils.AppPrefs;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.location.CitiesAdapter;
import com.zenpets.doctors.utils.adapters.location.CountriesAdapter;
import com.zenpets.doctors.utils.adapters.location.LocalitiesAdapter;
import com.zenpets.doctors.utils.adapters.location.StatesAdapter;
import com.zenpets.doctors.utils.helpers.LocationPickerActivity;
import com.zenpets.doctors.utils.models.location.CitiesData;
import com.zenpets.doctors.utils.models.location.CountriesData;
import com.zenpets.doctors.utils.models.location.LocalitiesData;
import com.zenpets.doctors.utils.models.location.StatesData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ClinicCreator extends AppCompatActivity {

    AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** A FIREBASE USER INSTANCE **/
    FirebaseUser user;

    /** THE DOCTOR ID **/
    String DOCTOR_ID = null;

    /** A FIREBASE STORAGE REFERENCE **/
    StorageReference storageReference;

    /** THE REQUEST CODES **/
    private int REQUEST_LOCATION = 1;
    private int REQUEST_GALLERY = 2;
    private int REQUEST_CAMERA = 3;

    /****** DATA TYPES FOR PROFILE DETAILS *****/
    String CLINIC_ID = null;
    String CLINIC_NAME = null;
    String PHONE_NUMBER_1 = null;
    String PHONE_NUMBER_2 = null;
    String POSTAL_ADDRESS = null;
    String COUNTRY_ID = null;
    String STATE_ID = null;
    String CITY_ID = null;
    String LOCALITY_ID = null;
    String PIN_CODE = null;
    String LANDMARK = null;
    Double CLINIC_LATITUDE;
    Double CLINIC_LONGITUDE;
    String FILE_NAME = null;
    private Uri LOGO_URI = null;
    String LOGO_URL = null;

    /** COUNTRY ADAPTER AND ARRAY LIST **/
    CountriesAdapter countriesAdapter;
    ArrayList<CountriesData> arrCountries = new ArrayList<>();

    /** STATE ADAPTER AND ARRAY LIST **/
    StatesAdapter statesAdapter;
    ArrayList<StatesData> arrStates = new ArrayList<>();

    /** CITIES ADAPTER AND ARRAY LIST **/
    CitiesAdapter citiesAdapter;
    ArrayList<CitiesData> arrCities = new ArrayList<>();

    /** THE LOCALITIES ADAPTER AND ARRAY LIST **/
    LocalitiesAdapter localitiesAdapter;
    ArrayList<LocalitiesData> arrLocalities = new ArrayList<>();

    /** THE URI'S **/
    Uri imageUri;
    Uri targetURI;

    /** AN IMAGE LOADER INSTANCE **/
    ImageLoader imageLoader;

    /** A PROGRESS DIALOG INSTANCE **/
    ProgressDialog dialog;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(com.zenpets.doctors.R.id.inputClinicName) TextInputLayout inputClinicName;
    @BindView(com.zenpets.doctors.R.id.edtClinicName) AppCompatEditText edtClinicName;
    @BindView(com.zenpets.doctors.R.id.inputPhone1) TextInputLayout inputPhone1;
    @BindView(com.zenpets.doctors.R.id.edtPhone1) AppCompatEditText edtPhone1;
    @BindView(com.zenpets.doctors.R.id.inputPhone2) TextInputLayout inputPhone2;
    @BindView(com.zenpets.doctors.R.id.edtPhone2) AppCompatEditText edtPhone2;
    @BindView(com.zenpets.doctors.R.id.inputPostalAddress) TextInputLayout inputPostalAddress;
    @BindView(com.zenpets.doctors.R.id.edtPostalAddress) AppCompatEditText edtPostalAddress;
    @BindView(com.zenpets.doctors.R.id.spnCountry) AppCompatSpinner spnCountry;
    @BindView(com.zenpets.doctors.R.id.spnState) AppCompatSpinner spnState;
    @BindView(com.zenpets.doctors.R.id.spnCity) AppCompatSpinner spnCity;
    @BindView(com.zenpets.doctors.R.id.spnLocalities) AppCompatSpinner spnLocalities;
    @BindView(com.zenpets.doctors.R.id.inputPinCode) TextInputLayout inputPinCode;
    @BindView(com.zenpets.doctors.R.id.edtPinCode) AppCompatEditText edtPinCode;
    @BindView(com.zenpets.doctors.R.id.inputLandmark) TextInputLayout inputLandmark;
    @BindView(com.zenpets.doctors.R.id.edtLandmark) AppCompatEditText edtLandmark;
    @BindView(com.zenpets.doctors.R.id.txtLocation) AppCompatTextView txtLocation;
    @BindView(com.zenpets.doctors.R.id.imgvwLogo) AppCompatImageView imgvwLogo;

    /** SELECT THE LOCATION ON THE MAP **/
    @OnClick(com.zenpets.doctors.R.id.txtLocationPicker) void locationPicker()  {
        Intent intent = new Intent(this, LocationPickerActivity.class);
        startActivityForResult(intent, REQUEST_LOCATION);
    }

    /** PICK AN IMAGE **/
    @OnClick(com.zenpets.doctors.R.id.imgvwLogo) void pickLogo()    {
        AlertDialog.Builder builder = new AlertDialog.Builder(ClinicCreator.this, com.zenpets.doctors.R.style.ZenPetsDialog);
        builder.setTitle("Choose Image Source");
        builder.setItems(new CharSequence[]{"Gallery", "Camera"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                switch (which)  {
                    case 0:
                        getGalleryImage();
                        break;
                    case 1:
                        getCameraImage();
                        break;
                    default:
                        break;
                }
            }
        }); builder.show();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zenpets.doctors.R.layout.creator_clinic);
        ButterKnife.bind(this);

        /* GET THE INCOMING DATA */
        fetchIncomingData();
    }

    /* GET THE INCOMING DATA */
    private void fetchIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("DOCTOR_ID"))    {
            DOCTOR_ID = bundle.getString("DOCTOR_ID");
            if (DOCTOR_ID != null)  {

                /* GET THE USER DETAILS */
                user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {

                    /* INSTANTIATE THE IMAGE LOADER INSTANCE */
                    imageLoader = ImageLoader.getInstance();

                    /* CONFIGURE THE ACTIONBAR */
                    configAB();

                    /* FETCH THE LIST OF COUNTRIES */
                    fetchCountriesList();

                    /* SELECT A COUNTRY */
                    spnCountry.setOnItemSelectedListener(selectCountry);

                    /* SELECT A STATE */
                    spnState.setOnItemSelectedListener(selectState);

                    /* SELECT A CITY */
                    spnCity.setOnItemSelectedListener(selectCity);

                    /* SELECT A LOCALITY */
                    spnLocalities.setOnItemSelectedListener(selectLocality);
                } else {
                    Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
                    finish();
                }
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /***** CHECK FOR ALL CLINIC DETAILS  *****/
    private void checkClinicDetails() {

        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtClinicName.getWindowToken(), 0);

        /* COLLECT ALL THE DATA */
        CLINIC_NAME = edtClinicName.getText().toString().trim();
        PHONE_NUMBER_1 = edtPhone1.getText().toString().trim();
        PHONE_NUMBER_2 = edtPhone2.getText().toString().trim();
        POSTAL_ADDRESS = edtPostalAddress.getText().toString().trim();
        PIN_CODE = edtPinCode.getText().toString().trim();
        LANDMARK = edtLandmark.getText().toString().trim();

        /* GENERATE THE FILE NAME */
        if (!(LOGO_URI == null) && !TextUtils.isEmpty(CLINIC_NAME))    {
            FILE_NAME = CLINIC_NAME.replaceAll(" ", "_").toLowerCase().trim();
        } else {
            FILE_NAME = null;
        }

        /* VALIDATE THE DATA */
        if (TextUtils.isEmpty(CLINIC_NAME)) {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            inputClinicName.setError(getString(com.zenpets.doctors.R.string.clinic_name_empty));
        } else if (TextUtils.isEmpty(PHONE_NUMBER_1)) {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            inputPhone1.setError(getString(com.zenpets.doctors.R.string.clinic_phone_empty));
        } else if (TextUtils.isEmpty(POSTAL_ADDRESS)) {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            inputPostalAddress.setError(getString(com.zenpets.doctors.R.string.clinic_postal_address_empty));
        } else if (TextUtils.isEmpty(PIN_CODE)) {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            inputPinCode.setError(getString(com.zenpets.doctors.R.string.clinic_pin_code_empty));
        } else if (TextUtils.isEmpty(COUNTRY_ID) || COUNTRY_ID == null) {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Please select a Country", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(STATE_ID) || STATE_ID == null) {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Please select a State", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(CITY_ID)) {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Please select a City", Toast.LENGTH_LONG).show();
        } else if (TextUtils.isEmpty(LOCALITY_ID) || LOCALITY_ID == null)   {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Please select a Locality", Toast.LENGTH_LONG).show();
        } else if (CLINIC_LONGITUDE == null || CLINIC_LATITUDE == null) {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Please select / mark your Location on the Map", Toast.LENGTH_LONG).show();
        } else if (FILE_NAME == null)    {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), getString(com.zenpets.doctors.R.string.clinic_logo_empty), Toast.LENGTH_LONG).show();
        } else {
            inputClinicName.setErrorEnabled(false);
            inputPhone1.setErrorEnabled(false);
            inputPostalAddress.setErrorEnabled(false);
            inputPinCode.setErrorEnabled(false);

            /* UPLOAD THE CLINIC LOGO */
            uploadClinicLogo();
        }
    }

    /***** UPLOAD THE CLINIC LOGO *****/
    private void uploadClinicLogo() {

        /* SHOW THE PROGRESS DIALOG WHILE UPLOADING THE IMAGE **/
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we create your new account....");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        storageReference = FirebaseStorage.getInstance().getReference();
        final StorageReference refStorage = storageReference.child("Clinic Logos").child(FILE_NAME);
        refStorage.putFile(LOGO_URI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadURL = taskSnapshot.getDownloadUrl();
                LOGO_URL = String.valueOf(downloadURL);
                if (LOGO_URL != null)    {
                    /* CREATE THE NEW CLINIC */
                    createClinic();
                } else {
                    Toast.makeText(
                            getApplicationContext(),
                            "There was a problem creating your new account. Please try again by clicking the Save button.",
                            Toast.LENGTH_LONG).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("UPLOAD EXCEPTION", e.toString());
            }
        });
    }

    /***** CREATE THE NEW CLINIC *****/
    private void createClinic() {

        /* THE URL TO CREATE A NEW CLINIC RECORD */
        String URL_NEW_ADOPTION_LISTING = "http://leodyssey.com/ZenPets/public/newClinic";
        // String URL_NEW_ADOPTION_LISTING = "http://192.168.11.2/zenpets/public/newClinic";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("userID", DOCTOR_ID)
                .add("countryID", COUNTRY_ID)
                .add("stateID", STATE_ID)
                .add("cityID", CITY_ID)
                .add("localityID", LOCALITY_ID)
                .add("clinicName", CLINIC_NAME)
                .add("clinicAddress", POSTAL_ADDRESS)
                .add("clinicLandmark", LANDMARK)
                .add("clinicPinCode", PIN_CODE)
                .add("clinicLatitude", String.valueOf(CLINIC_LATITUDE))
                .add("clinicLongitude", String.valueOf(CLINIC_LONGITUDE))
                .add("clinicSubscription", "Free")
                .add("clinicLogo", LOGO_URL)
                .add("clinicPhone1", PHONE_NUMBER_1)
                .add("clinicPhone2", PHONE_NUMBER_2)
                .build();
        Request request = new Request.Builder()
                .url(URL_NEW_ADOPTION_LISTING)
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
                    Log.e("RESULT", strResult);
                    final JSONObject JORoot = new JSONObject(strResult);
                    Log.e("USER RECORD RESULT", String.valueOf(JORoot));
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        /* FINISH THE ACTIVITY */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* GET THE CLINIC ID FOR UPLOADING THE ADOPTION IMAGES */
                                if (JORoot.has("clinicID")) {
                                    try {
                                        /* GET THE CLINIC ID */
                                        CLINIC_ID = JORoot.getString("clinicID");

                                        /* CREATE THE TRANSACTIONAL (doctor_clinics TABLE) RECORD */
                                        createTransRecord();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    dialog.dismiss();
                                    Toast.makeText(getApplicationContext(), "There was an error posting the new Adoption. Please try again", Toast.LENGTH_LONG).show();
                                }

//                                /* DISMISS THE DIALOG */
//                                dialog.dismiss();
//
//                                Toast.makeText(getApplicationContext(), "The new Clinic was successfully created.", Toast.LENGTH_LONG).show();
//                                finish();
                            }
                        });
                    } else {
                        /* FINISH THE ACTIVITY */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                /* DISMISS THE DIALOG */
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "There was an error creating the new Clinic. Please try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** CREATE THE TRANSACTIONAL TABLE RECORD FOR MAPPING THE DOCTOR AND THE CLINIC THEY WORK AT *****/
    private void createTransRecord() {
        /* THE URL TO CREATE A NEW CLINIC RECORD */
        String URL_NEW_TRANS_RECORD = "http://leodyssey.com/ZenPets/public/newDocClinicTrans";
        // String URL_NEW_TRANS_RECORD = "http://192.168.11.2/zenpets/public/newDocClinicTrans";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("doctorID", DOCTOR_ID)
                .add("clinicID", CLINIC_ID)
                .build();
        Request request = new Request.Builder()
                .url(URL_NEW_TRANS_RECORD)
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
                        /* FINISH THE ACTIVITY */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* DISMISS THE DIALOG */
                                dialog.dismiss();

                                Toast.makeText(getApplicationContext(), "The new Clinic was successfully created.", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        });
                    } else {
                        /* FINISH THE ACTIVITY */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* DISMISS THE DIALOG */
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "There was an error creating the new Clinic. Please try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** FETCH AN IMAGE FROM THE GALLERY *****/
    private void getGalleryImage() {
        Intent getGalleryImage = new Intent();
        getGalleryImage.setType("image/*");
        getGalleryImage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(getGalleryImage, "Choose a picture"), REQUEST_GALLERY);
    }

    /***** FETCH AN IMAGE FROM THE CAMERA *****/
    private void getCameraImage() {
        Intent getCameraImage = new Intent("android.media.action.IMAGE_CAPTURE");
        File cameraFolder;
        if (Environment.getExternalStorageState().equals (Environment.MEDIA_MOUNTED))
            cameraFolder = new File(android.os.Environment.getExternalStorageDirectory(), "ZenPets/");
        else
            cameraFolder = getCacheDir();
        if(!cameraFolder.exists())
            cameraFolder.mkdirs();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss", Locale.getDefault());
        String timeStamp = dateFormat.format(new Date());
        String imageFileName = "picture_" + timeStamp + ".jpg";

        File photo = new File(Environment.getExternalStorageDirectory(), "ZenPets/" + imageFileName);
        getCameraImage.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);

        startActivityForResult(getCameraImage, REQUEST_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);

        /* A BITMAP INSTANCE */
        Bitmap BMP_IMAGE;

        if (resultCode == RESULT_OK && requestCode == REQUEST_CAMERA)  {
            targetURI = imageUri;
            Bitmap bitmap = BitmapFactory.decodeFile(targetURI.getPath());
            BMP_IMAGE = resizeBitmap(bitmap);
            imgvwLogo.setImageBitmap(BMP_IMAGE);
            imgvwLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);

            /** STORE THE BITMAP AS A FILE AND USE THE FILE'S URI **/
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/ZenPets");
            myDir.mkdirs();
            String fName = "photo.jpg";
            File file = new File(myDir, fName);
            if (file.exists()) file.delete();

            try {
                FileOutputStream out = new FileOutputStream(file);
                BMP_IMAGE.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

                /** GET THE FINAL PROFILE URI **/
                LOGO_URI = Uri.fromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_GALLERY) {
            Uri uri = data.getData();
            targetURI = uri;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                BMP_IMAGE = resizeBitmap(bitmap);
                imgvwLogo.setImageBitmap(BMP_IMAGE);
                imgvwLogo.setScaleType(ImageView.ScaleType.CENTER_CROP);

                /* STORE THE BITMAP AS A FILE AND USE THE FILE'S URI */
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ZenPets");
                myDir.mkdirs();
                String fName = "photo.jpg";
                File file = new File(myDir, fName);
                if (file.exists()) file.delete();

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    BMP_IMAGE.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();

                    /** GET THE FINAL PROFILE URI **/
                    LOGO_URI = Uri.fromFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (resultCode == RESULT_OK && requestCode == REQUEST_LOCATION)    {
            Bundle bundle = data.getExtras();
            CLINIC_LATITUDE = bundle.getDouble("LATITUDE");
            CLINIC_LONGITUDE = bundle.getDouble("LONGITUDE");

            /* GET THE APPROXIMATE ADDRESS FOR DISPLAY */
            try {
                Geocoder geocoder;
                List<Address> addresses;
                geocoder = new Geocoder(this, Locale.getDefault());
                addresses = geocoder.getFromLocation(CLINIC_LATITUDE, CLINIC_LONGITUDE, 1);
                String address = addresses.get(0).getAddressLine(0);
                if (!TextUtils.isEmpty(address))    {
                    txtLocation.setText(address);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /***** RESIZE THE BITMAP *****/
    private Bitmap resizeBitmap(Bitmap image)   {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = 1024;
            height = (int) (width / bitmapRatio);
        } else {
            height = 768;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(com.zenpets.doctors.R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Clinic Details";
//        String strTitle = getString(R.string.add_a_new_pet);
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
        MenuInflater inflater = new MenuInflater(ClinicCreator.this);
        inflater.inflate(com.zenpets.doctors.R.menu.activity_save_cancel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case com.zenpets.doctors.R.id.menuSave:
                /* CHECK FOR ALL CLINIC DETAILS  */
                checkClinicDetails();
                break;
            case com.zenpets.doctors.R.id.menuCancel:
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    /** SELECT A COUNTRY **/
    private final AdapterView.OnItemSelectedListener selectCountry = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            COUNTRY_ID = arrCountries.get(position).getCountryID();

            /* CLEAR THE STATES ARRAY LIST */
            arrStates.clear();

            /* FETCH THE LIST OF STATES */
            if (COUNTRY_ID != null) {
                /* FETCH THE LIST OF STATES IN THE SELECTED COUNTRY */
                fetchStates();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /** SELECT A USER_STATE **/
    private final AdapterView.OnItemSelectedListener selectState = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            STATE_ID = arrStates.get(position).getStateID();

            /* CLEAR THE CITIES ARRAY LIST */
            arrCities.clear();

            /* FETCH THE LIST OF CITIES */
            if (STATE_ID != null)   {
                /* FETCH THE LIST OF CITIES IN THE SELECTED STATE */
                fetchCities();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /** SELECT THE CITY **/
    private final AdapterView.OnItemSelectedListener selectCity = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            CITY_ID = arrCities.get(position).getCityID();

            /* CLEAR THE LOCALITIES ARRAY LIST */
            arrLocalities.clear();

            /* FETCH THE LIST OF LOCALITIES */
            if (CITY_ID != null)   {
                /* FETCH THE LIST OF LOCALITIES IN THE SELECTED CITY */
                fetchLocalities();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /** SELECT A LOCALITY **/
    private final AdapterView.OnItemSelectedListener selectLocality = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            LOCALITY_ID = arrLocalities.get(position).getLocalityID();
            Log.e("SELECTED LOCALITY", arrLocalities.get(position).getLocalityName());
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    /***** FETCH THE LIST OF COUNTRIES *****/
    private void fetchCountriesList() {
        String COUNTRY_URL = "http://leodyssey.com/ZenPets/public/allCountries";
        // String COUNTRY_URL = "http://192.168.11.2/zenpets/public/allCountries";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(COUNTRY_URL)
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
                        JSONArray JACountries = JORoot.getJSONArray("countries");

                        /* A COUNTRY DATA POJO INSTANCE */
                        CountriesData data;

                        for (int i = 0; i < JACountries.length(); i++) {
                            JSONObject JOCountries = JACountries.getJSONObject(i);
//                            Log.e("COUNTRIES", String.valueOf(JOCountries));

                           /* INSTANTIATE THE COUNTRY DATA POJO INSTANCE */
                            data = new CountriesData();

                           /* GET THE COUNTRY ID */
                            if (JOCountries.has("countryID"))    {
                                String countryID = JOCountries.getString("countryID");
                                data.setCountryID(countryID);
                            } else {
                                data.setCountryID(null);
                            }

                            /* GET THE COUNTRY NAME */
                            if (JOCountries.has("countryName")) {
                                String countryName = JOCountries.getString("countryName");
                                data.setCountryName(countryName);
                            } else {
                                data.setCountryName(null);
                            }

                            /* GET THE CURRENCY NAME */
                            if (JOCountries.has("currencyName"))    {
                                String currencyName = JOCountries.getString("currencyName");
                                data.setCurrencyName(currencyName);
                            } else {
                                data.setCurrencyName(null);
                            }

                            /* GET THE CURRENCY CODE */
                            if (JOCountries.has("currencyCode"))    {
                                String currencyCode = JOCountries.getString("currencyCode");
                                data.setCurrencyCode(currencyCode);
                            } else {
                                data.setCurrencyCode(null);
                            }

                            /* GET THE CURRENCY SYMBOL */
                            if (JOCountries.has("currencySymbol"))  {
                                String currencySymbol = JOCountries.getString("currencySymbol");
                                data.setCurrencySymbol(currencySymbol);
                            } else {
                                data.setCurrencySymbol(null);
                            }

                            /* GET THE COUNTRY FLAG */
                            if (JOCountries.has("countryFlag")) {
                                String countryFlag = JOCountries.getString("countryFlag");
                                Bitmap bmpCountryFlag = imageLoader.loadImageSync(countryFlag);
                                Drawable drawable = new BitmapDrawable(getResources(), bmpCountryFlag);
                                data.setCountryFlag(drawable);
                            } else {
                                data.setCountryFlag(null);
                            }

                            /* ADD THE COLLECTED INFO TO THE ARRAY LIST */
                            arrCountries.add(data);
                        }

                        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE COUNTRIES ADAPTER */
                                countriesAdapter = new CountriesAdapter(ClinicCreator.this, arrCountries);

                                /* SET THE COUNTRIES SPINNER */
                                spnCountry.setAdapter(countriesAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** FETCH THE LIST OF STATES IN THE SELECTED COUNTRY *****/
    private void fetchStates() {
        String STATES_URL = "http://leodyssey.com/ZenPets/public/countryStates/" + COUNTRY_ID;
        // String STATES_URL = "http://192.168.11.2/zenpets/public/countryStates/" + COUNTRY_ID;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(STATES_URL)
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
                        JSONArray JAStates = JORoot.getJSONArray("states");

                        /* A STATES DATA POJO INSTANCE */
                        StatesData data;

                        for (int i = 0; i < JAStates.length(); i++) {
                            JSONObject JOStates = JAStates.getJSONObject(i);
//                            Log.e("STATES", String.valueOf(JOStates));

                            /* INSTANTIATE THE STATES DATA POJO INSTANCE */
                            data = new StatesData();

                            /* GET THE STATE ID */
                            if (JOStates.has("stateID"))    {
                                String stateID = JOStates.getString("stateID");
                                data.setStateID(stateID);
                            } else {
                                data.setStateID(null);
                            }

                            /* GET THE STATE NAME */
                            if (JOStates.has("stateName"))   {
                                String stateName = JOStates.getString("stateName");
                                data.setStateName(stateName);
                            } else {
                                data.setStateName(null);
                            }

                            /* GET THE COUNTRY ID */
                            if (JOStates.has("countryID"))  {
                                String countryID = JOStates.getString("countryID");
                                data.setCountryID(countryID);
                            } else {
                                data.setCountryID(null);
                            }

                            /* ADD THE COLLECTED INFO TO THE ARRAY LIST */
                            arrStates.add(data);
                        }

                        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE STATES ADAPTER */
                                statesAdapter = new StatesAdapter(ClinicCreator.this, arrStates);

                                /* SET THE STATES SPINNER */
                                spnState.setAdapter(statesAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** FETCH THE LIST OF CITIES IN THE SELECTED STATE *****/
    private void fetchCities() {

        String STATES_URL = "http://leodyssey.com/ZenPets/public/stateCities/" + STATE_ID;
        // String STATES_URL = "http://192.168.11.2/zenpets/public/stateCities/" + STATE_ID;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(STATES_URL)
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
                        JSONArray JACities = JORoot.getJSONArray("cities");

                        /* A CITIES DATA POJO INSTANCE */
                        CitiesData data;

                        for (int i = 0; i < JACities.length(); i++) {
                            JSONObject JOCities = JACities.getJSONObject(i);
//                            Log.e("CITIES", String.valueOf(JOCities));

                            /* INSTANTIATE THE CITIES DATA POJO INSTANCE */
                            data = new CitiesData();

                            /* GET THE CITY ID */
                            if (JOCities.has("cityID"))    {
                                String cityID = JOCities.getString("cityID");
                                data.setCityID(cityID);
                            } else {
                                data.setCityID(null);
                            }

                            /* GET THE CITY NAME */
                            if (JOCities.has("cityName"))   {
                                String cityName = JOCities.getString("cityName");
                                data.setCityName(cityName);
                            } else {
                                data.setCityName(null);
                            }

                            /* GET THE STATE ID */
                            if (JOCities.has("stateID"))  {
                                String stateID = JOCities.getString("stateID");
                                data.setStateID(stateID);
                            } else {
                                data.setStateID(null);
                            }

                            /* ADD THE COLLECTED INFO TO THE ARRAYLIST */
                            arrCities.add(data);
                        }

                        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE CITIES ADAPTER */
                                citiesAdapter = new CitiesAdapter(ClinicCreator.this, arrCities);

                                /* SET THE CITIES SPINNER */
                                spnCity.setAdapter(citiesAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** FETCH THE LIST OF LOCALITIES IN THE SELECTED CITY *****/
    private void fetchLocalities() {

        String LOCALITIES_URL = "http://leodyssey.com/ZenPets/public/cityLocalities/" + CITY_ID;
        // String LOCALITIES_URL = "http://192.168.11.2/zenpets/public/cityLocalities/" + CITY_ID;
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(LOCALITIES_URL)
                .build();
        Log.e("URL", LOCALITIES_URL);
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
                        JSONArray JALocalities = JORoot.getJSONArray("localities");

                        /* A LOCALITIES DATA POJO INSTANCE */
                        LocalitiesData data;

                        for (int i = 0; i < JALocalities.length(); i++) {
                            JSONObject JOLocalities = JALocalities.getJSONObject(i);
//                            Log.e("LOCALITIES", String.valueOf(JOLocalities));

                            /* INSTANTIATE THE LOCALITIES DATA POJO INSTANCE */
                            data = new LocalitiesData();

                            /* GET THE LOCALITY ID */
                            if (JOLocalities.has("localityID"))  {
                                String localityID = JOLocalities.getString("localityID");
                                data.setLocalityID(localityID);
                            } else {
                                data.setLocalityID(null);
                            }

                            /* GET THE LOCALITY NAME */
                            if (JOLocalities.has("localityName"))   {
                                String localityName = JOLocalities.getString("localityName");
                                data.setLocalityName(localityName);
                            } else {
                                data.setLocalityName(null);
                            }

                            /* GET THE CITY ID */
                            if (JOLocalities.has("cityID"))    {
                                String cityID = JOLocalities.getString("cityID");
                                data.setCityID(cityID);
                            } else {
                                data.setCityID(null);
                            }

                            /* ADD THE COLLECTED INFO TO THE ARRAYLIST */
                            arrLocalities.add(data);
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE LOCALITIES ADAPTER */
                                localitiesAdapter = new LocalitiesAdapter(ClinicCreator.this, arrLocalities);

                                /* SET THE LOCALITIES SPINNER */
                                spnLocalities.setAdapter(localitiesAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}