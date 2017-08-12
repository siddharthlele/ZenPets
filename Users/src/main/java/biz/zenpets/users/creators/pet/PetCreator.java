package biz.zenpets.users.creators.pet;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.pet.BreedsAdapter;
import biz.zenpets.users.utils.adapters.pet.PetTypesAdapter;
import biz.zenpets.users.utils.models.pet.BreedsData;
import biz.zenpets.users.utils.models.pet.PetTypesData;
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

public class PetCreator extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** THE USER EMAIL **/
    String USER_EMAIL = null;

    /****** DATA TYPES FOR THE PET DETAILS *****/
    private String USER_ID = null;
    private String PET_TYPE_ID = null;
    private String PET_BREED_ID = null;
    private String PET_NAME = null;
    private String PET_GENDER = "Male";
    private String PET_DOB = null;
    private String PET_PROFILE = null;
    private String FILE_NAME = null;
    private Uri PET_URI = null;

    /** A URI INSTANCE **/
    private Uri imageUri;

    /** REQUEST CODE FOR SELECTING AN IMAGE **/
    private final int PICK_GALLERY_REQUEST = 1;
    private final int PICK_CAMERA_REQUEST = 2;

    /** A PROGRESS DIALOG **/
    private ProgressDialog dialog;

    /** THE PET TYPES ADAPTER AND ARRAY LIST **/
    private PetTypesAdapter petTypesAdapter;
    private final ArrayList<PetTypesData> arrPetTypes = new ArrayList<>();

    /** THE BREEDS ADAPTER AND ARRAY LIST **/
    private BreedsAdapter breedsAdapter;
    private final ArrayList<BreedsData> arrBreeds = new ArrayList<>();

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaProgress) LinearLayout linlaProgress;
    @BindView(R.id.spnPetTypes) AppCompatSpinner spnPetTypes;
    @BindView(R.id.spnBreeds) AppCompatSpinner spnBreeds;
    @BindView(R.id.inputPetName) TextInputLayout inputPetName;
    @BindView(R.id.edtPetName) AppCompatEditText edtPetName;
    @BindView(R.id.groupGender) SegmentedButtonGroup groupGender;
    @BindView(R.id.txtPetDOB) AppCompatTextView txtPetDOB;
    @BindView(R.id.imgvwPetThumb) AppCompatImageView imgvwPetThumb;

    /** SELECT AN IMAGE OF THE MEDICINE **/
    @OnClick(R.id.imgvwPetThumb) void selectImage()    {
        AlertDialog.Builder builder = new AlertDialog.Builder(PetCreator.this, R.style.ZenPetsDialog);
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

    /** SELECT THE PET'S DATE OF BIRTH **/
    @OnClick(R.id.btnDOBSelector) void selectDOB()   {
        Calendar now = Calendar.getInstance();
        DatePickerDialog pickerDialog = DatePickerDialog.newInstance(
                PetCreator.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        pickerDialog.show(getFragmentManager(), "DatePickerDialog");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pet_creator);
        ButterKnife.bind(this);

        /* SET THE CURRENT DATE */
        setCurrentDate();

        /* CONFIGURE THE TOOLBAR */
        configTB();

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();

        /* FETCH ALL PET TYPES */
        fetchPetTypes();

        /* SELECT THE PET'S GENDER */
        groupGender.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
//                Toast.makeText(getApplicationContext(), "Clicked: " + position, Toast.LENGTH_SHORT).show();
                if (position == 0)  {
                    PET_GENDER = "Male";
                } else if (position == 1)   {
                    PET_GENDER = "Female";
                }
            }
        });

        /* SELECT A PET TYPE */
        spnPetTypes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                /* GET THE SELECTED PET TYPE ID */
                PET_TYPE_ID = arrPetTypes.get(position).getPetTypeID();

                /* CLEAR THE BREEDS ARRAY LIST */
                arrBreeds.clear();

                /* FETCH THE LIST OF BREEDS IN THE SELECTED PET TYPE */
                fetchBreeds();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* SELECT A BREED */
        spnBreeds.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PET_BREED_ID = arrBreeds.get(position).getBreedID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /***** FETCH THE LIST OF BREEDS IN THE SELECTED PET TYPE *****/
    private void fetchBreeds() {
        // String URL_BREEDS = "http://leodyssey.com/ZenPets/public/allPetBreeds";
        String URL_BREEDS = "http://192.168.11.2/zenpets/public/allPetBreeds";
        HttpUrl.Builder builder = HttpUrl.parse(URL_BREEDS).newBuilder();
        builder.addQueryParameter("petTypeID", PET_TYPE_ID);
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
                        JSONArray JABreeds = JORoot.getJSONArray("breeds");

                        /* AND INSTANCE OF THE BREEDS DATA MODEL */
                        BreedsData data;

                        for (int i = 0; i < JABreeds.length(); i++) {
                            JSONObject JOBreeds = JABreeds.getJSONObject(i);
//                            Log.e("BREEDS", String.valueOf(JOBreeds));

                            /* INSTANTIATE THE PET TYPES DATA INSTANCE */
                            data = new BreedsData();

                            /* GET THE BREED ID */
                            if (JOBreeds.has("breedID"))    {
                                data.setBreedID(JOBreeds.getString("breedID"));
                            } else {
                                data.setBreedID(null);
                            }

                            /* GET THE PET TYPE ID */
                            if (JOBreeds.has("petTypeID"))  {
                                data.setPetTypeID(JOBreeds.getString("petTypeID"));
                            } else {
                                data.setPetTypeID(null);
                            }

                            /* GET THE BREED NAME */
                            if (JOBreeds.has("breedName"))  {
                                data.setBreedName(JOBreeds.getString("breedName"));
                            } else {
                                data.setBreedName(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrBreeds.add(data);
                        }

                        /* INSTANTIATE THE BREEDS ADAPTER AND SET THE ADAPTER TO THE SPINNER */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE BREEDS ADAPTER */
                                breedsAdapter = new BreedsAdapter(PetCreator.this, arrBreeds);

                                /* SET THE ADAPTER TO THE BREEDS SPINNER */
                                spnBreeds.setAdapter(breedsAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** FETCH ALL PET TYPES *****/
    private void fetchPetTypes() {
        // String URL_PET_TYPES = "http://leodyssey.com/ZenPets/public/petTypes";
        String URL_PET_TYPES = "http://192.168.11.2/zenpets/public/petTypes";
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(URL_PET_TYPES)
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
                        JSONArray JATypes = JORoot.getJSONArray("types");

                        /* AND INSTANCE OF THE PET TYPE DATA MODEL */
                        PetTypesData data;

                        for (int i = 0; i < JATypes.length(); i++) {
                            JSONObject JOTypes = JATypes.getJSONObject(i);
//                            Log.e("TYPES", String.valueOf(JOTypes));

                            /* INSTANTIATE THE PET TYPES DATA INSTANCE */
                            data = new PetTypesData();

                            /* GET THE PET TYPE ID */
                            if (JOTypes.has("petTypeID"))   {
                                data.setPetTypeID(JOTypes.getString("petTypeID"));
                            } else {
                                data.setPetTypeID(null);
                            }

                            /* GET THE PET TYPE */
                            if (JOTypes.has("petTypeName")) {
                                data.setPetTypeName(JOTypes.getString("petTypeName"));
                            } else {
                                data.setPetTypeName(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrPetTypes.add(data);
                        }

                        /* INSTANTIATE THE PET TYPES ADAPTER AND SET THE ADAPTER TO THE SPINNER */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                /* INSTANTIATE THE PET TYPES ADAPTER */
                                petTypesAdapter = new PetTypesAdapter(PetCreator.this, arrPetTypes);

                                /* SET THE ADAPTER TO THE PET TYPES SPINNER */
                                spnPetTypes.setAdapter(petTypesAdapter);
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {

        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

//        String strTitle = getString(R.string.add_a_new_pet);
        String strTitle = "Add a new Pet";
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
        MenuInflater inflater = new MenuInflater(PetCreator.this);
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
                /* CHECK FOR ALL PET DETAILS  */
                checkPetDetails();
                break;
            case R.id.menuCancel:
                /* CANCEL NEW PET CREATION */
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    /***** CHECK FOR ALL PET DETAILS  *****/
    private void checkPetDetails() {
        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtPetName.getWindowToken(), 0);

        /* GET THE REQUIRED TEXTS */
        PET_NAME = edtPetName.getText().toString().trim();
        if (!TextUtils.isEmpty(PET_NAME))   {
            FILE_NAME = PET_NAME.replaceAll(" ", "_").toLowerCase().trim();
        }

        if (TextUtils.isEmpty(PET_NAME))   {
            inputPetName.setError("Enter the Pet's name");
        } else if (PET_URI == null)   {
            Toast.makeText(getApplicationContext(), "Please select your Pet's image", Toast.LENGTH_LONG).show();
        } else {
            /* POST THE PETS DISPLAY PICTURE */
            postPetPicture();
        }
    }

    /***** POST THE PETS DISPLAY PICTURE *****/
    private void postPetPicture() {
        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we add the new Pet to your account..");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        /* PUBLISH THE PET PROFILE TO FIREBASE */
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference refStorage = storageReference.child("Pets").child(FILE_NAME);
        refStorage.putFile(PET_URI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadURL = taskSnapshot.getDownloadUrl();
                if (downloadURL != null)    {
                    PET_PROFILE = String.valueOf(downloadURL);
                    if (PET_PROFILE != null)    {
                        /* PUBLISH THE NEW PET */
                        publishNewPet();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(
                                getApplicationContext(),
                                "There was a problem adding your new Pet. Please try again by clicking the Save button.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    dialog.dismiss();
                    Toast.makeText(
                            getApplicationContext(),
                            "There was a problem adding your new Pet. Please try again by clicking the Save button.",
                            Toast.LENGTH_LONG).show();
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    /***** PUBLISH THE NEW PET *****/
    private void publishNewPet() {
        // String URL_NEW_PET = "http://leodyssey.com/ZenPets/public/newPet";
        String URL_NEW_PET = "http://192.168.11.2/zenpets/public/newPet";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("userID", USER_ID)
                .add("petTypeID", PET_TYPE_ID)
                .add("breedID", PET_BREED_ID)
                .add("petName", PET_NAME)
                .add("petGender", PET_GENDER)
                .add("petDOB", PET_DOB)
                .add("petProfile", PET_PROFILE)
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
                                Toast.makeText(getApplicationContext(), "Successfully added your Pet", Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(getApplicationContext(), "There was an error adding your Pet. Please try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** FETCH AN IMAGE FROM THE GALLERY **/
    private void getGalleryImage() {
        Intent getGalleryImage = new Intent();
        getGalleryImage.setType("image/*");
        getGalleryImage.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(getGalleryImage, "Choose a picture"), PICK_GALLERY_REQUEST);
    }

    /** FETCH AN IMAGE FROM THE CAMERA **/
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

        startActivityForResult(getCameraImage, PICK_CAMERA_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Bitmap BMP_IMAGE;
        Uri targetURI;
        if (resultCode == RESULT_OK && requestCode == PICK_CAMERA_REQUEST)  {
            targetURI = imageUri;
            Bitmap bitmap = BitmapFactory.decodeFile(targetURI.getPath());
            BMP_IMAGE = resizeBitmap(bitmap);
            imgvwPetThumb.setImageBitmap(BMP_IMAGE);
            imgvwPetThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);

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

                /* GET THE FINAL PET URI */
                PET_URI = Uri.fromFile(file);
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (resultCode == RESULT_OK && requestCode == PICK_GALLERY_REQUEST) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                BMP_IMAGE = resizeBitmap(bitmap);
                imgvwPetThumb.setImageBitmap(BMP_IMAGE);
                imgvwPetThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);

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

                    /* GET THE FINAL PET URI */
                    PET_URI = Uri.fromFile(file);
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

    /***** SET THE CURRENT DATE *****/
    private void setCurrentDate() {
        /* SET THE CURRENT DATE (DISPLAY ONLY !!!!) */
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String formattedDate = dateFormat.format(new Date());
        txtPetDOB.setText(formattedDate);

        /* SET THE CURRENT DATE (DATABASE ONLY !!!!) */
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        PET_DOB = sdf.format(cal.getTime());
    }

    @Override
    public void onDateSet(DatePickerDialog view, int year, int month, int date) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        /* FOR THE DATABASE ONLY !!!! */
        PET_DOB = sdf.format(cal.getTime());
//        Log.e("INITIAL PET DOB", PET_DOB);

        /* FOR DISPLAY ONLY !!!! */
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.LONG, Locale.getDefault());
        String selectedDate = dateFormat.format(cal.getTime());
        txtPetDOB.setText(selectedDate);
    }
}