package com.zenpets.doctors.credentials;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.zenpets.doctors.R;
import com.zenpets.doctors.landing.NewLandingActivity;
import com.zenpets.doctors.legal.PrivacyPolicyActivity;
import com.zenpets.doctors.legal.SellerAgreementActivity;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.location.CitiesAdapter;
import com.zenpets.doctors.utils.adapters.location.CountriesAdapter;
import com.zenpets.doctors.utils.adapters.location.StatesAdapter;
import com.zenpets.doctors.utils.models.location.CitiesData;
import com.zenpets.doctors.utils.models.location.CountriesData;
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
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUpActivity extends AppCompatActivity {

    /** A FIREBASE AUTH INSTANCE **/
    private FirebaseAuth auth;

    /** A FIREBASE USER INSTANCE **/
    private FirebaseUser user;

    /** STRINGS TO HOLD THE USER PROVIDED INFORMATION **/
    String DOCTOR_NAME = null;
    String DOCTOR_EMAIL = null;
    String DOCTOR_DISPLAY_PROFILE = null;
    String DOCTOR_PHONE_PREFIX = null;
    String DOCTOR_PHONE_NUMBER = null;
    String DOCTOR_GENDER = "Male";
    String COUNTRY_ID = null;
    String STATE_ID = null;
    String CITY_ID = null;
    String DOCTOR_DISPLAY_PROFILE_FILE_NAME = null;
    Uri DOCTOR_DISPLAY_PROFILE_URI = null;
    private String PASSWORD = null;
    private String CONFIRM_PASSWORD = null;

    /** BOOLEAN TO CHECK IF ACCOUNT EXISTS **/
    private boolean blnExists = false;

    /** BOOLEAN TO TRACK USER RECORD CREATION **/
    private boolean blnSuccess = false;

    /** STRING TO HOLD THE ERROR MESSAGE, IF ANY **/
    private String strErrorMessage = null;

    /** THE REQUEST CODES **/
    private int REQUEST_GALLERY = 1;
    private int REQUEST_CAMERA = 2;

    /** THE URI'S **/
    Uri imageUri = null;
    Uri targetURI = null;

    /** AN IMAGE LOADER INSTANCE **/
    ImageLoader imageLoader;

    /** THE COUNTRY ADAPTER AND ARRAYLIST **/
    CountriesAdapter countriesAdapter;
    ArrayList<CountriesData> arrCountries = new ArrayList<>();

    /** THE STATES ADAPTER AND ARRAYLIST **/
    StatesAdapter statesAdapter;
    ArrayList<StatesData> arrStates = new ArrayList<>();

    /** CITIES ADAPTER AND ARRAY LIST **/
    CitiesAdapter citiesAdapter;
    ArrayList<CitiesData> arrCities = new ArrayList<>();

    /** A PROGRESS DIALOG INSTANCE **/
    ProgressDialog dialog;

    @BindView(R.id.inputEmailAddress) TextInputLayout inputEmailAddress;
    @BindView(R.id.edtEmailAddress) AppCompatEditText edtEmailAddress;
    @BindView(R.id.inputPassword) TextInputLayout inputPassword;
    @BindView(R.id.edtPassword) AppCompatEditText edtPassword;
    @BindView(R.id.inputConfirmPassword) TextInputLayout inputConfirmPassword;
    @BindView(R.id.edtConfirmPassword) AppCompatEditText edtConfirmPassword;
    @BindView(R.id.inputFullName) TextInputLayout inputFullName;
    @BindView(R.id.edtFullName) AppCompatEditText edtFullName;
    @BindView(R.id.edtPhonePrefix) AppCompatEditText edtPhonePrefix;
    @BindView(R.id.edtPhoneNumber) AppCompatEditText edtPhoneNumber;
    @BindView(R.id.groupGender) SegmentedButtonGroup groupGender;
    @BindView(R.id.spnCountry) AppCompatSpinner spnCountry;
    @BindView(R.id.spnState) AppCompatSpinner spnState;
    @BindView(R.id.spnCity) AppCompatSpinner spnCity;
    @BindView(R.id.imgvwProfilePicture) AppCompatImageView imgvwProfilePicture;
    @BindView(R.id.txtTermsOfService) AppCompatTextView txtTermsOfService;

    /** SELECT THE PROFILE PICTURE **/
    @OnClick(R.id.imgvwProfilePicture) void selectProfilePicture()  {
        AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this, R.style.ZenPetsDialog);
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
        setContentView(R.layout.sign_up_activity);
        ButterKnife.bind(this);

        /* INSTANTIATE THE IMAGE LOADER INSTANCE */
        imageLoader = ImageLoader.getInstance();

        /* INSTANTIATE THE FIREBASE AUTH INSTANCE **/
        auth = FirebaseAuth.getInstance();

        /* SET THE TERMS OF SERVICE TEXT **/
        setTermsAndConditions(txtTermsOfService);

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

        /* SELECT THE USER'S GENDER */
        groupGender.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0)  {
                    DOCTOR_GENDER = "Male";
                } else if (position == 1)   {
                    DOCTOR_GENDER = "Female";
                }
            }
        });
    }

    /***** CHECK FOR ALL ACCOUNT DETAILS  *****/
    private void checkAccountDetails() {

        /* HIDE THE KEYBOARD **/
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtEmailAddress.getWindowToken(), 0);

        /* COLLECT THE NECESSARY DATA **/
        DOCTOR_NAME = edtFullName.getText().toString().trim();
        DOCTOR_EMAIL = edtEmailAddress.getText().toString().trim();
        DOCTOR_PHONE_PREFIX = edtPhonePrefix.getText().toString().trim();
        DOCTOR_PHONE_NUMBER = edtPhoneNumber.getText().toString().trim();
        PASSWORD = edtPassword.getText().toString().trim();
        CONFIRM_PASSWORD = edtConfirmPassword.getText().toString().trim();

        if (!TextUtils.isEmpty(DOCTOR_NAME) && !TextUtils.isEmpty(DOCTOR_EMAIL))  {
            DOCTOR_DISPLAY_PROFILE_FILE_NAME = DOCTOR_NAME.replaceAll(" ", "_").toLowerCase().trim() + "_" + DOCTOR_EMAIL;;
        } else {
            DOCTOR_DISPLAY_PROFILE_FILE_NAME = null;
        }
        boolean blnValidEmail = isValidEmail(DOCTOR_EMAIL);

        /* VALIDATE THE DATA **/
        if (TextUtils.isEmpty(DOCTOR_EMAIL))    {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            inputEmailAddress.setError("Provide your Email Address");
            edtEmailAddress.requestFocus();
        } else if (!blnValidEmail)  {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            inputEmailAddress.setError("Provide a valid Email Address");
            edtEmailAddress.requestFocus();
        } else if (TextUtils.isEmpty(PASSWORD)) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            inputPassword.setError("Choose a Password");
            edtPassword.requestFocus();
        } else if (PASSWORD.length() < 8)   {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            inputPassword.setError("Password has to be at least 8 characters");
            edtPassword.requestFocus();
        } else if (TextUtils.isEmpty(CONFIRM_PASSWORD)) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            inputConfirmPassword.setError("Confirm your Password");
            edtConfirmPassword.requestFocus();
        } else if (CONFIRM_PASSWORD.length() < 8)   {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            inputConfirmPassword.setError("password has to be at least 8 characters");
            edtConfirmPassword.requestFocus();
        } else if (!CONFIRM_PASSWORD.equals(PASSWORD)) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            inputConfirmPassword.setError("Passwords don't match.");
            edtConfirmPassword.requestFocus();
        } else if (TextUtils.isEmpty(DOCTOR_NAME)) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            inputFullName.setError("Provide your Full Name");
            edtFullName.requestFocus();
        } else if (TextUtils.isEmpty(DOCTOR_PHONE_PREFIX)) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            edtPhonePrefix.setError("Enter the dialing prefix");
            edtPhonePrefix.requestFocus();
        } else if (TextUtils.isEmpty(DOCTOR_PHONE_NUMBER)) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            edtPhoneNumber.setError("Enter your phone number");
            edtPhoneNumber.requestFocus();
        } else if (COUNTRY_ID == null) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Select your Country", Toast.LENGTH_LONG).show();
            spnCountry.requestFocus();
        } else if (STATE_ID == null) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Select your State", Toast.LENGTH_LONG).show();
            spnState.requestFocus();
        } else if (CITY_ID == null) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Select your City", Toast.LENGTH_LONG).show();
            spnCity.requestFocus();
        } else if (DOCTOR_DISPLAY_PROFILE_FILE_NAME == null) {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Select a Profile Picture", Toast.LENGTH_LONG).show();
            imgvwProfilePicture.requestFocus();
        } else if (DOCTOR_DISPLAY_PROFILE_URI == null)  {
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);
            Toast.makeText(getApplicationContext(), "Select a Profile Picture", Toast.LENGTH_LONG).show();
            imgvwProfilePicture.requestFocus();
        } else {
            /* DISABLE THE ERRORS */
            inputFullName.setErrorEnabled(false);
            inputEmailAddress.setErrorEnabled(false);
            inputPassword.setErrorEnabled(false);
            inputConfirmPassword.setErrorEnabled(false);

            /* CREATE THE NEW ACCOUNT */
            createAccount();
        }
    }

    /***** CREATE THE NEW ACCOUNT *****/
    private void createAccount() {

        /* SHOW THE PROGRESS DIALOG WHILE UPLOADING THE IMAGE **/
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we create your new account....");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        auth.createUserWithEmailAndPassword(DOCTOR_EMAIL, PASSWORD).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful())   {
                    /* GET THE NEW USERS INSTANCE */
                    user = auth.getCurrentUser();

                    if (user != null)   {
                        DOCTOR_EMAIL = user.getEmail();
                        Log.e("EMAIL", DOCTOR_EMAIL);
                        /* CHECK IF THE USER RECORD EXISTS IN THE PRIMARY DATABASE */
                        checkUserExists();
                    }
                } else {
                    dialog.dismiss();
                    Toast.makeText(
                            getApplicationContext(),
                            "There was a problem creating your new account. Please try again by clicking the Save button.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                dialog.dismiss();
                Toast.makeText(SignUpActivity.this, "EXCEPTION: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /***** CHECK IF THE USER RECORD EXISTS IN THE PRIMARY DATABASE *****/
    private void checkUserExists() {
        /* THE URL TO CREATE THE USER'S RECORD **/
        String URL_USER_RECORD = "http://leodyssey.com/ZenPets/public/profileExists";
        // String URL_USER_RECORD = "http://192.168.11.2/zenpets/public/profileExists";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_RECORD).newBuilder();
        builder.addQueryParameter("userEmail", DOCTOR_EMAIL);
        String FINAL_URL = builder.build().toString();
        Log.e("FINAL URL", FINAL_URL);
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
                    Log.e("EXISTS", String.valueOf(JORoot));
                    if (JORoot.has("message"))  {
                        String strMessage = JORoot.getString("message");
                        if (strMessage.equalsIgnoreCase("User record doesn't exist"))    {
                            blnExists = false;
                        } else if (strMessage.equalsIgnoreCase("User record exists")) {
                            blnExists = true;
                        }

                        if (!blnExists) {
                            /* UPLOAD THE USER PROFILE PICTURE */
                            uploadProfilePicture();
                        } else {
                            dialog.dismiss();
                            Intent intent = new Intent(SignUpActivity.this, NewLandingActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /* UPLOAD THE USER PROFILE PICTURE */
    private void uploadProfilePicture() {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference refStorage = storageReference.child("Profiles").child(DOCTOR_DISPLAY_PROFILE_FILE_NAME);
        refStorage.putFile(DOCTOR_DISPLAY_PROFILE_URI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadURL = taskSnapshot.getDownloadUrl();
                DOCTOR_DISPLAY_PROFILE = String.valueOf(downloadURL);
                Log.e("PROFILE", DOCTOR_DISPLAY_PROFILE);
                if (DOCTOR_DISPLAY_PROFILE != null)    {
                    /* CREATE THE USERS PROFILE RECORD */
                    createUserRecord();
                } else {
                    dialog.dismiss();
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

    /***** CREATE THE USERS PROFILE RECORD *****/
    private void createUserRecord() {

            /* THE URL TO CREATE A NEW DOCTOR **/
        String URL_NEW_DOCTOR = "http://leodyssey.com/ZenPets/public/register";
        // String URL_NEW_DOCTOR = "http://192.168.11.2/zenpets/public/register";

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("userAuthID", user.getUid())
                .add("userName", DOCTOR_NAME)
                .add("userEmail", DOCTOR_EMAIL)
                .add("userDisplayProfile", DOCTOR_DISPLAY_PROFILE)
                .add("userPhonePrefix", DOCTOR_PHONE_PREFIX)
                .add("userPhoneNumber", DOCTOR_PHONE_NUMBER)
                .add("userGender", DOCTOR_GENDER)
                .add("countryID", COUNTRY_ID)
                .add("stateID", STATE_ID)
                .add("cityID", CITY_ID)
                .add("userType", "Doctor")
                .build();
        Request request = new Request.Builder()
                .url(URL_NEW_DOCTOR)
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
                    Log.e("SIGN UP", String.valueOf(JORoot));
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                        /* TOGGLE THE BOOLEAN FLAG TO TRUE **/
                        blnSuccess = true;
                    } else {
                        /* TOGGLE THE BOOLEAN FLAG TO FALSE **/
                        blnSuccess = false;

                        /* SET THE ERROR MESSAGE **/
                        strErrorMessage = "There was en error signing you up. Please submit your details again.";
                    }

                    /* CHECK IF THE USER RECORD CREATION WAS SUCCESSFUL **/
                    if (blnSuccess) {
                        dialog.dismiss();
                        auth.signOut();
                        Intent intent = new Intent(SignUpActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        dialog.dismiss();

                        /* DELETE THE USER **/
                        user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())    {
                                    /* SHOW THE ERROR MESSAGE **/
                                    new MaterialDialog.Builder(SignUpActivity.this)
                                            .title("Registration Failed!")
                                            .content(strErrorMessage)
                                            .positiveText("OKAY")
                                            .theme(Theme.LIGHT)
                                            .icon(ContextCompat.getDrawable(SignUpActivity.this, R.drawable.ic_info_outline_black_24dp))
                                            .typeface("HelveticaNeueLTW1G-MdCn.otf", "HelveticaNeueLTW1G-Cn.otf")
                                            .show();
                                }
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** SET THE TERMS AND CONDITIONS **/
    private void setTermsAndConditions(AppCompatTextView view) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(getResources().getString(R.string.terms_part_1));
        spanTxt.append(getResources().getString(R.string.terms_part_2));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent showSellerAgreement = new Intent(getApplicationContext(), SellerAgreementActivity.class);
                startActivity(showSellerAgreement);
            }
        }, spanTxt.length() - getResources().getString(R.string.terms_part_2).length(), spanTxt.length(), 0);
        spanTxt.append(getResources().getString(R.string.terms_part_3));
        spanTxt.setSpan(new ForegroundColorSpan(Color.BLACK), 48, spanTxt.length(), 0);
        spanTxt.append(getResources().getString(R.string.terms_part_4));
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                Intent showPrivacyPolicy = new Intent(getApplicationContext(), PrivacyPolicyActivity.class);
                startActivity(showPrivacyPolicy);
            }
        }, spanTxt.length() - " Privacy Policy".length(), spanTxt.length(), 0);
        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {

        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Sign Up";
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
        MenuInflater inflater = new MenuInflater(SignUpActivity.this);
        inflater.inflate(R.menu.activity_save_cancel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menuSave:
                /* CHECK FOR ALL ACCOUNT DETAILS  */
                checkAccountDetails();
                break;
            case R.id.menuCancel:
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    /** FETCH AN IMAGE FROM THE GALLERY **/
    private void getGalleryImage() {
        Intent getGalleryImage = new Intent();
        getGalleryImage.setType("image/*");
        getGalleryImage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(getGalleryImage, "Choose a picture"), REQUEST_GALLERY);
    }

    /** FETCH AN IMAGE FROM THE CAMERA **/
    @SuppressWarnings("ResultOfMethodCallIgnored")
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

        /** A BITMAP INSTANCE **/
        Bitmap BMP_IMAGE;

        if (resultCode == RESULT_OK && requestCode == REQUEST_CAMERA)  {
            targetURI = imageUri;
            Bitmap bitmap = BitmapFactory.decodeFile(targetURI.getPath());
            BMP_IMAGE = resizeBitmap(bitmap);
            imgvwProfilePicture.setImageBitmap(BMP_IMAGE);
            imgvwProfilePicture.setScaleType(ImageView.ScaleType.CENTER_CROP);

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
                DOCTOR_DISPLAY_PROFILE_URI = Uri.fromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (resultCode == RESULT_OK && requestCode == REQUEST_GALLERY) {
            Uri uri = data.getData();
            targetURI = uri;
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                BMP_IMAGE = resizeBitmap(bitmap);
                imgvwProfilePicture.setImageBitmap(BMP_IMAGE);
                imgvwProfilePicture.setScaleType(ImageView.ScaleType.CENTER_CROP);

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
                    DOCTOR_DISPLAY_PROFILE_URI = Uri.fromFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** RESIZE THE BITMAP **/
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
                                countriesAdapter = new CountriesAdapter(SignUpActivity.this, arrCountries);

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
                                statesAdapter = new StatesAdapter(SignUpActivity.this, arrStates);

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
                String strResult = response.body().string();
                try {
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
                                citiesAdapter = new CitiesAdapter(SignUpActivity.this, arrCities);

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

    /** VALIDATE EMAIL SYNTAX / FORMAT **/
    private static boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}