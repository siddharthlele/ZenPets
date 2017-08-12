package biz.zenpets.users.creators.adoption;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
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

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.adoptions.AdoptionsAlbumAdapter;
import biz.zenpets.users.utils.adapters.pet.BreedsAdapter;
import biz.zenpets.users.utils.adapters.pet.PetTypesAdapter;
import biz.zenpets.users.utils.models.adoptions.AdoptionAlbumData;
import biz.zenpets.users.utils.models.adoptions.AdoptionsImageData;
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

public class AdoptionCreator extends AppCompatActivity {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** THE USER AUTH ID **/
    private String USER_AUTH_ID = null;

    /** DATA TYPES TO HOLD THE NEW ADOPTION LISTING DETAILS **/
    private String ADOPTION_ID = null;
    private String USER_ID = null;
    private String CITY_ID = null;
    private String PET_TYPE_ID = null;
    private String PET_BREED_ID = null;
    private String PET_NAME = null;
    private String PET_GENDER = "Male";
    private String PET_VACCINATED = "Yes";
    private String PET_DEWORMED = "Yes";
    private String PET_NEUTERED = "Yes";
    private String PET_DESCRIPTION = null;

    /** THE PET TYPES ADAPTER AND ARRAY LIST **/
    private PetTypesAdapter petTypesAdapter;
    private final ArrayList<PetTypesData> arrPetTypes = new ArrayList<>();

    /** THE BREEDS ADAPTER AND ARRAY LIST **/
    private BreedsAdapter breedsAdapter;
    private final ArrayList<BreedsData> arrBreeds = new ArrayList<>();

    /** THE ADAPTER AND ARRAY LIST FOR THE CLINIC ALBUMS **/
    private AdoptionsAlbumAdapter adapter;
    private final ArrayList<AdoptionAlbumData> arrAlbums = new ArrayList<>();

    /** THE ARRAYLIST TO HOLD THE UPLOADED FIREBASE IMAGES DATA **/
    private AdoptionsImageData data;

    /** A PROGRESS DIALOG INSTANCE **/
    private ProgressDialog dialog;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.spnPetTypes) AppCompatSpinner spnPetTypes;
    @BindView(R.id.spnBreeds) AppCompatSpinner spnBreeds;
    @BindView(R.id.inputPetName) TextInputLayout inputPetName;
    @BindView(R.id.edtPetName) AppCompatEditText edtPetName;
    @BindView(R.id.groupGender) SegmentedButtonGroup groupGender;
    @BindView(R.id.groupVaccination) SegmentedButtonGroup groupVaccination;
    @BindView(R.id.groupDewormed) SegmentedButtonGroup groupDewormed;
    @BindView(R.id.groupNeutered) SegmentedButtonGroup groupNeutered;
    @BindView(R.id.inputDescription) TextInputLayout inputDescription;
    @BindView(R.id.edtDescription) AppCompatEditText edtDescription;
    @BindView(R.id.cardImagesSelector) CardView cardImagesSelector;
    @BindView(R.id.gridAdoptionImages) RecyclerView gridAdoptionImages;

    @OnClick(R.id.cardImagesSelector) void cardSelectImages()   {
        Intent intent = new Intent(this, AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 4);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    @OnClick(R.id.imgvwPetImages) void imageSelectImages()  {
        Intent intent = new Intent(this, AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 4);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adoption_creator);
        ButterKnife.bind(this);

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();

        /* FETCH THE USER'S NAME AND DISPLAY PROFILE */
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            USER_AUTH_ID = user.getUid();
            if (USER_AUTH_ID != null) {
                /* FETCH THE USER'S PROFILE DETAILS */
                fetchProfileDetails();
            } else {
                Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
        }

        /* CONFIGURE THE TOOLBAR */
        configTB();

        /* CONFIGURE THE RECYCLER VIEW */
        configRecycler();

        /* FETCH ALL PET TYPES */
        fetchPetTypes();

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

        /* SELECT THE PET'S GENDER */
        groupGender.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0)  {
                    PET_GENDER = "Male";
                } else if (position == 1)   {
                    PET_GENDER = "Female";
                }
            }
        });

        /* SELECT THE PET'S VACCINATION STATUS */
        groupVaccination.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0)  {
                    PET_VACCINATED = "Yes";
                } else if (position == 1)   {
                    PET_VACCINATED = "No";
                }
            }
        });

        /* SELECT THE PET'S DEWORMING STATUS */
        groupDewormed.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0)  {
                    PET_DEWORMED = "Yes";
                } else if (position == 1)   {
                    PET_DEWORMED = "No";
                }
            }
        });

        /* SELECT THE PET'S NEUTERED STATUS */
        groupNeutered.setOnClickedButtonPosition(new SegmentedButtonGroup.OnClickedButtonPosition() {
            @Override
            public void onClickedButtonPosition(int position) {
                if (position == 0)  {
                    PET_NEUTERED = "Yes";
                } else if (position == 1)   {
                    PET_NEUTERED = "No";
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
                                petTypesAdapter = new PetTypesAdapter(AdoptionCreator.this, arrPetTypes);

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
                                breedsAdapter = new BreedsAdapter(AdoptionCreator.this, arrBreeds);

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

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);
        String strTitle = "New Adoption Listing";
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
        MenuInflater inflater = new MenuInflater(AdoptionCreator.this);
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
                /* CHECK FOR ALL PET ADOPTION DETAILS  */
                checkDetails();
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

    /***** CHECK FOR ALL PET ADOPTION DETAILS  *****/
    private void checkDetails() {
        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtPetName.getWindowToken(), 0);

        /* COLLECT THE REQUIRED DATA */
        if (edtPetName.getText().toString() != null)    {
            PET_NAME = edtPetName.getText().toString();
        } else {
            PET_NAME = "Null";
        }
        PET_DESCRIPTION = edtDescription.getText().toString().trim();

        if (TextUtils.isEmpty(PET_DESCRIPTION)) {
            edtDescription.setError("Please provide the Pet's Description");
            edtDescription.requestFocus();
        } else  {
            /* POST THE ADOPTION LISTING */
            postListing();
        }
    }

    /***** POST THE ADOPTION LISTING *****/
    private void postListing() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we publish your new Adoption listing");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        // String URL_NEW_ADOPTION = "http://leodyssey.com/ZenPets/public/newAdoption";
        String URL_NEW_ADOPTION = "http://192.168.11.2/zenpets/public/newAdoption";
        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("petTypeID", PET_TYPE_ID)
                .add("breedID", PET_BREED_ID)
                .add("userID", USER_ID)
                .add("cityID", CITY_ID)
                .add("adoptionName", PET_NAME)
                .add("adoptionDescription", PET_DESCRIPTION)
                .add("adoptionGender", PET_GENDER)
                .add("adoptionVaccination", PET_VACCINATED)
                .add("adoptionDewormed", PET_DEWORMED)
                .add("adoptionNeutered", PET_NEUTERED)
                .add("adoptionTimeStamp", String.valueOf(System.currentTimeMillis() / 1000))
                .add("adoptionStatus", "Open")
                .build();
        Request request = new Request.Builder()
                .url(URL_NEW_ADOPTION)
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
                        /* GET THE ADOPTION ID FOR UPLOADING THE ADOPTION IMAGES */
                        if (JORoot.has("adoptionID")) {
                            ADOPTION_ID = JORoot.getString("adoptionID");
                            /* PUBLISH THE ADOPTION IMAGES */
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    publishAdoptionImages();
                                }
                            });
                        } else {
                            Toast.makeText(getApplicationContext(), "There was an error posting the new Adoption. Please try again", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        /* DISMISS THE DIALOG */
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                Toast.makeText(getApplicationContext(), "There was an error posting the new adoption. Please try again", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /***** PUBLISH THE ADOPTION IMAGES *****/
    private void publishAdoptionImages() {
        if (arrAlbums != null || arrAlbums.size() > 0)  {
            for (int i = 0; i < arrAlbums.size(); i++) {
                data = new AdoptionsImageData();

                Bitmap bitmap = arrAlbums.get(i).getBmpAdoptionImage();
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ZenPets/Adoptions");
                myDir.mkdirs();
                final String imageNumber = String.valueOf(i + 1);
                String fName = "photo" + imageNumber + ".jpg";
                File file = new File(myDir, fName);
                if (file.exists()) file.delete();

                try {
                    FileOutputStream out = new FileOutputStream(file);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
                    out.flush();
                    out.close();

                    /* GET THE FINAL ADOPTION IMAGE URI */
                    Uri uri = Uri.fromFile(file);
                    String FILE_NAME;
                    if (PET_NAME != null)   {
                        FILE_NAME = PET_NAME.replaceAll(" ", "_").toLowerCase().trim() + "_" + USER_ID + "_" + fName;
                    } else {
                        FILE_NAME = "ADOPTION_" + USER_ID + "_" + fName;
                    }
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    StorageReference refStorage = storageReference.child("Adoptions").child(FILE_NAME);
                    refStorage.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadURL = taskSnapshot.getDownloadUrl();
                            if (downloadURL != null) {
                                /* THE URL TO UPLOAD A NEW ADOPTION IMAGE */
                                // String URL_POST_ADOPTION_IMAGE = "http://leodyssey.com/ZenPets/public/postAdoptionImages";
                                String URL_POST_ADOPTION_IMAGE = "http://192.168.11.2/zenpets/public/postAdoptionImages";

                                OkHttpClient client = new OkHttpClient();
                                RequestBody body = new FormBody.Builder()
                                        .add("adoptionID", ADOPTION_ID)
                                        .add("imageURL", String.valueOf(downloadURL))
                                        .build();
                                Request request = new Request.Builder()
                                        .url(URL_POST_ADOPTION_IMAGE)
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
//                                            Log.e("IMAGE UPLOAD RESULT", String.valueOf(JORoot));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (i + 1 == arrAlbums.size())  {
                    /* DISMISS THE DIALOG */
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            Toast.makeText(getApplicationContext(), "New Adoption listed successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                }
            }
        }
    }

    /***** FETCH THE USER'S PROFILE DETAILS *****/
    private void fetchProfileDetails() {
        // String URL_USER_PROFILE = "http://leodyssey.com/ZenPets/public/fetchProfile";
        String URL_USER_PROFILE = "http://192.168.11.2/zenpets/public/fetchProfile";
        HttpUrl.Builder builder = HttpUrl.parse(URL_USER_PROFILE).newBuilder();
        builder.addQueryParameter("userAuthID", USER_AUTH_ID);
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
//                    Log.e("RESULT", String.valueOf(JORoot));
                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {

                        /* GET THE CITY ID */
                        if (JORoot.has("cityID"))   {
                            CITY_ID = JORoot.getString("cityID");
                        } else {
                            CITY_ID = null;
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        int intOrientation = getResources().getConfiguration().orientation;
        gridAdoptionImages.setHasFixedSize(true);
        GridLayoutManager glm = null;
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet)   {
            if (intOrientation == 1)	{
                glm = new GridLayoutManager(this, 2);
                glm.setAutoMeasureEnabled(true);
            } else if (intOrientation == 2) {
                glm = new GridLayoutManager(this, 4);
                glm.setAutoMeasureEnabled(true);
            }
        } else {
            if (intOrientation == 1)    {
                glm = new GridLayoutManager(this, 2);
                glm.setAutoMeasureEnabled(true);
            } else if (intOrientation == 2) {
                glm = new GridLayoutManager(this, 4);
                glm.setAutoMeasureEnabled(true);
            }
        }
        gridAdoptionImages.setLayoutManager(glm);
        gridAdoptionImages.setNestedScrollingEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            /* CLEAR THE ARRAY LIST */
            arrAlbums.clear();

            /* GET THE ARRAY LIST OF IMAGES RETURNED BY THE INTENT */
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);

            /* A NEW INSTANCE OF THE CLINIC ALBUMS MODEL */
            AdoptionAlbumData albums;
            for (int i = 0, l = images.size(); i < l; i++) {
                /* INSTANTIATE THE AdoptionAlbumData INSTANCE "albums" */
                albums = new AdoptionAlbumData();

                /* GET THE IMAGE PATH */
                String strPath = images.get(i).path;
                File filePath = new File(strPath);
                Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
                Bitmap bmpImage = resizeBitmap(bitmap);
                albums.setBmpAdoptionImage(bmpImage);

                /* SET THE IMAGE NUMBER */
                String strNumber = String.valueOf(i + 1);
                albums.setTxtImageNumber(strNumber);

                /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                arrAlbums.add(albums);
            }

            /* INSTANTIATE THE ADAPTER */
            adapter = new AdoptionsAlbumAdapter(AdoptionCreator.this, arrAlbums);

            /* SET THE ADAPTER TO THE RECYCLER VIEW */
            gridAdoptionImages.setAdapter(adapter);
            gridAdoptionImages.setVisibility(View.VISIBLE);
            cardImagesSelector.setVisibility(View.GONE);
        }
    }

    /** RESIZE THE BITMAP **/
    private Bitmap resizeBitmap(Bitmap image)   {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 0) {
            width = 800;
            height = (int) (width / bitmapRatio);
        } else {
            height = 800;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
}