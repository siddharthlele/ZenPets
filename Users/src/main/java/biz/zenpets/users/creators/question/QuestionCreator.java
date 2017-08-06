package biz.zenpets.users.creators.question;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.CardView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.AppPrefs;
import biz.zenpets.users.utils.TypefaceSpan;
import biz.zenpets.users.utils.adapters.pet.PetSpinnerAdapter;
import biz.zenpets.users.utils.adapters.problems.ProblemSpinnerAdapter;
import biz.zenpets.users.utils.models.pet.PetData;
import biz.zenpets.users.utils.models.problems.ProblemData;
import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class QuestionCreator extends AppCompatActivity {

    private AppPrefs getApp()	{
        return (AppPrefs) getApplication();
    }

    /** A BOTTOM SHEET DIALOG INSTANCE **/
    private BottomSheetDialog sheetDialog;

    /** REQUEST CODE FOR SELECTING AN IMAGE **/
    private final int PICK_GALLERY_REQUEST = 1;
    private final int PICK_CAMERA_REQUEST = 2;

    /** A URI INSTANCE **/
    private Uri imageUri;

    /** DATA TYPES TO HOLD THE CONSULTATION DATA **/
    private String USER_ID = null;
    private String PET_ID = null;
    private String PROBLEM_ID = null;
    private String CONSULTATION_TITLE = null;
    private String CONSULTATION_DESCRIPTION = null;
    private String CONSULTATION_DATE = null;
    private String CONSULTATION_PICTURE = null;
    private String FILE_NAME = null;
    private Uri CONSULTATION_URI = null;

    /** THE PETS ADAPTER AND ARRAY LIST **/
    private PetSpinnerAdapter petSpinnerAdapter;
    private final ArrayList<PetData> arrPets = new ArrayList<>();

    /** THE PROBLEMS ADAPTER AND ARRAY LIST **/
    private ProblemSpinnerAdapter problemSpinnerAdapter;
    private final ArrayList<ProblemData> arrProblems = new ArrayList<>();

    /** A PROGRESS DIALOG INSTANCE **/
    private ProgressDialog dialog;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.spnMyPets) AppCompatSpinner spnMyPets;
    @BindView(R.id.spnProblem) AppCompatSpinner spnProblem;
    @BindView(R.id.inputTitle) TextInputLayout inputTitle;
    @BindView(R.id.edtTitle) AppCompatEditText edtTitle;
    @BindView(R.id.inputDescription) TextInputLayout inputDescription;
    @BindView(R.id.edtDescription) AppCompatEditText edtDescription;
    @BindView(R.id.cardConsultThumb) CardView cardConsultThumb;
    @BindView(R.id.imgvwConsultThumb) AppCompatImageView imgvwConsultThumb;
    @BindView(R.id.chkbxAcceptTerms) AppCompatCheckBox chkbxAcceptTerms;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.question_creator);
        ButterKnife.bind(this);

        /* CONFIGURE THE TOOLBAR */
        configTB();

        /* SET THE CURRENT DATE */
        setCurrentDate();

        /* GET THE USER ID */
        USER_ID = getApp().getUserID();
        if (USER_ID != null)    {
            /* FETCH THE USER'S PETS */
            new fetchUserPets().execute();
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required info....", Toast.LENGTH_SHORT).show();
            finish();
        }

        /* SELECT A PET */
        spnMyPets.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PET_ID = arrPets.get(position).getPetID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /* SELECT A PROBLEM */
        spnProblem.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PROBLEM_ID = arrProblems.get(position).getProblemID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    /***** CHECK FOR ALL CONSULTATION DETAILS *****/
    private void checkConsultationDetails() {
        /* HIDE THE KEYBOARD */
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtTitle.getWindowToken(), 0);

        /* GET THE REQUIRED TEXTS */
        CONSULTATION_TITLE = edtTitle.getText().toString();
        CONSULTATION_DESCRIPTION = edtDescription.getText().toString();
        if (CONSULTATION_URI != null && CONSULTATION_TITLE != null)   {
            FILE_NAME = CONSULTATION_TITLE.replaceAll(" ", "_").toLowerCase().trim() + "_" + USER_ID + "_" + CONSULTATION_DATE.replaceAll(" ", "_").toLowerCase().trim();
        } else {
            FILE_NAME = null;
            CONSULTATION_PICTURE = "Null";
        }

        /* CHECK THE REQUIRED DETAILS */
        if (chkbxAcceptTerms.isChecked()) {
            if (TextUtils.isEmpty(CONSULTATION_TITLE))  {
                inputTitle.setErrorEnabled(false);
                inputDescription.setErrorEnabled(false);
                inputTitle.setError("Enter the title");
            } else if (CONSULTATION_TITLE.length() < 10)    {
                inputTitle.setErrorEnabled(false);
                inputDescription.setErrorEnabled(false);
                inputTitle.setError("Title should be more than 10 chars");
            } else if (TextUtils.isEmpty(CONSULTATION_DESCRIPTION)) {
                inputTitle.setErrorEnabled(false);
                inputDescription.setErrorEnabled(false);
                inputDescription.setError("Provide some description....");
            } else if (CONSULTATION_DESCRIPTION.length() < 100) {
                inputTitle.setErrorEnabled(false);
                inputDescription.setErrorEnabled(false);
                inputDescription.setError("Description should be minimum 100 chars");
            } else {
                /* CHECK IF AN IMAGES IS ATTACHED */
                if (CONSULTATION_URI != null)   {
                    /* PUBLISH THE CONSULTATION IMAGE */
                    publishConsultationImage();
                } else {
                    /* PUBLISH CONSULTATION */
                    new publishConsultation().execute();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "You must accept the terms before posting", Toast.LENGTH_LONG).show();
        }
    }

    /***** PUBLISH THE CONSULTATION IMAGE *****/
    private void publishConsultationImage() {
        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we publish your question..");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        /* PUBLISH THE PET PROFILE TO FIREBASE */
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference refStorage = storageReference.child("Consultations").child(FILE_NAME);
        refStorage.putFile(CONSULTATION_URI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Uri downloadURL = taskSnapshot.getDownloadUrl();
                if (downloadURL != null)    {
                    CONSULTATION_PICTURE = String.valueOf(downloadURL);
                    if (CONSULTATION_PICTURE != null)    {
                        /* DISMISS THE DIALOG AND PUBLISH THE QUESTION */
                        dialog.dismiss();
                        new publishConsultation().execute();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(
                                getApplicationContext(),
                                "There was a problem publishing your new Consultation. Please try again by clicking the Save button.",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    dialog.dismiss();
                    Toast.makeText(
                            getApplicationContext(),
                            "There was a problem publishing your new Consultation. Please try again by clicking the Save button.",
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

    /***** PUBLISH THE CONSULTATION *****/
    private class publishConsultation extends AsyncTask<Void, Void, Void>  {

        /* A BOOLEAN TO TRACK THE SUCCESS OR THE FAILURE OF THE PUBLISH PROCESS */
        boolean blnSuccess = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
            dialog = new ProgressDialog(QuestionCreator.this);
            dialog.setMessage("Please wait while we publish your question..");
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.show();

            Log.e("PROBLEM", PROBLEM_ID);
            Log.e("TITLE", CONSULTATION_TITLE);
            Log.e("Description", CONSULTATION_DESCRIPTION);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_NEW_PET = "http://leodyssey.com/ZenPets/public/newConsultation";
            String URL_NEW_PET = "http://192.168.11.2/zenpets/public/newConsultation";
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("userID", USER_ID)
                    .add("petID", PET_ID)
                    .add("problemID", PROBLEM_ID)
                    .add("consultationTitle", CONSULTATION_TITLE)
                    .add("consultationDescription", CONSULTATION_DESCRIPTION)
                    .add("consultationPicture", CONSULTATION_PICTURE)
                    .add("consultationTimestamp", String.valueOf(System.currentTimeMillis() / 1000))
                    .build();
            Request request = new Request.Builder()
                    .url(URL_NEW_PET)
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    /* PUBLISHED SUCCESSFULLY */
                    blnSuccess = true;
                } else {
                    /* PUBLISHING FAILED */
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
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "Successfully published your question", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            } else {
                dialog.dismiss();
                Toast.makeText(getApplicationContext(), "There was an error publishing your question. Please try again", Toast.LENGTH_LONG).show();
            }
        }
    }

    /***** PUBLISH CONSULTATION *****/
//    private void publishConsultation() {
//        /* INSTANTIATE THE PROGRESS DIALOG INSTANCE */
//        dialog = new ProgressDialog(this);
//        dialog.setMessage("Please wait while we publish your question..");
//        dialog.setIndeterminate(false);
//        dialog.setCancelable(false);
//        dialog.show();
//
//        // String URL_NEW_PET = "http://leodyssey.com/ZenPets/public/newConsultation";
//        String URL_NEW_PET = "http://192.168.11.2/zenpets/public/newConsultation";
//        OkHttpClient client = new OkHttpClient();
//        RequestBody body = new FormBody.Builder()
//                .add("userID", USER_ID)
//                .add("petID", PET_ID)
//                .add("problemID", PROBLEM_ID)
//                .add("consultationTitle", CONSULTATION_TITLE)
//                .add("consultationsDescription", CONSULTATION_DESCRIPTION)
//                .add("consultationPicture", CONSULTATION_PICTURE)
//                .add("consultationTimestamp", String.valueOf(System.currentTimeMillis() / 1000))
//                .build();
//        Request request = new Request.Builder()
//                .url(URL_NEW_PET)
//                .post(body)
//                .build();
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(Call call, IOException e) {
//                Log.e("FAILURE", e.toString());
//            }
//
//            @Override
//            public void onResponse(Call call, Response response) throws IOException {
//                try {
//                    String strResult = response.body().string();
//                    JSONObject JORoot = new JSONObject(strResult);
//                    if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
//                        /* DISMISS THE DIALOG */
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                dialog.dismiss();
//                                Toast.makeText(getApplicationContext(), "Successfully published your question", Toast.LENGTH_SHORT).show();
//                                Intent intent = new Intent();
//                                setResult(RESULT_OK, intent);
//                                finish();
//                            }
//                        });
//                    } else {
//                        /* DISMISS THE DIALOG */
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                dialog.dismiss();
//                                Toast.makeText(getApplicationContext(), "There was an error publishing your question. Please try again", Toast.LENGTH_LONG).show();
//                            }
//                        });
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
//    }

    /***** FETCH THE USER'S PETS *****/
    private class fetchUserPets extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_PETS = "http://leodyssey.com/ZenPets/public/fetchUserPets";
            String URL_USER_PETS = "http://192.168.11.2/zenpets/public/fetchUserPets";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_PETS).newBuilder();
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
                    JSONArray JAPets = JORoot.getJSONArray("pets");
                    /* A PETS DATA INSTANCE */
                    PetData data;

                    if (JAPets.length() > 0)    {
                        for (int i = 0; i < JAPets.length(); i++) {
                            JSONObject JOPets = JAPets.getJSONObject(i);

                            /* INSTANTIATE THE PET DATA INSTANCE */
                            data = new PetData();

                            /* GET THE PET ID */
                            if (JOPets.has("petID"))    {
                                data.setPetID(JOPets.getString("petID"));
                            } else {
                                data.setPetID(null);
                            }

                            /* GET THE PET NAME */
                            if (JOPets.has("petName"))    {
                                data.setPetName(JOPets.getString("petName"));
                            } else {
                                data.setPetName(null);
                            }

                            /* GET THE PETS DISPLAY PROFILE */
                            if (JOPets.has("petProfile"))   {
                                data.setPetProfile(JOPets.getString("petProfile"));
                            } else {
                                data.setPetProfile(null);
                            }

                            /* ADD THE COLLECTED DATA TO THE ARRAY LIST */
                            arrPets.add(data);
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

            /* INSTANTIATE THE PETS SPINNER ADAPTER */
            petSpinnerAdapter = new PetSpinnerAdapter(QuestionCreator.this, arrPets);

            /* SET THE ADAPTER TO THE PETS SPINNER */
            spnMyPets.setAdapter(petSpinnerAdapter);

            /* FETCH THE LIST OF PROBLEMS */
            new fetchListOfProblems().execute();
        }
    }

    /***** FETCH THE LIST OF PROBLEMS *****/
    private class fetchListOfProblems extends AsyncTask<Void, Void, Void>   {

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

            /* INSTANTIATE THE PETS SPINNER ADAPTER */
            problemSpinnerAdapter = new ProblemSpinnerAdapter(QuestionCreator.this, arrProblems);

            /* SET THE ADAPTER TO THE PROBLEMS SPINNER */
            spnProblem.setAdapter(problemSpinnerAdapter);
        }
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {

        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

//        String strTitle = getString(R.string.add_a_new_pet);
        String strTitle = "Ask free question";
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
        MenuInflater inflater = new MenuInflater(QuestionCreator.this);
        inflater.inflate(R.menu.activity_question_creator, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
            case R.id.menuAttach:
                /* ATTACH AN IMAGE */
                sheetDialog = new BottomSheetDialog(QuestionCreator.this);
                View view = getLayoutInflater().inflate(R.layout.question_sheet, null);
                sheetDialog.setContentView(view);
                sheetDialog.show();

                /* CAST THE CHOOSER ELEMENTS */
                LinearLayout linlaGallery = (LinearLayout) view.findViewById(R.id.linlaGallery);
                LinearLayout linlaCamera = (LinearLayout) view.findViewById(R.id.linlaCamera);

                /* SELECT A GALLERY IMAGE */
                linlaGallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent getGalleryImage = new Intent();
                        getGalleryImage.setType("image/*");
                        getGalleryImage.setAction(Intent.ACTION_GET_CONTENT);
                        startActivityForResult(Intent.createChooser(getGalleryImage, "Choose a picture"), PICK_GALLERY_REQUEST);
                    }
                });

                /* SELECT A CAMERA IMAGE */
                linlaCamera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
                });

                break;
            case R.id.menuSubmit:
                /* CHECK FOR ALL CONSULTATION DETAILS */
                checkConsultationDetails();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* DISMISS THE BOTTOM SHEET DIALOG */
        sheetDialog.dismiss();

        if (resultCode == RESULT_OK)    {
            /* SHOW THE IMAGE THUMB CARD VIEW */
            cardConsultThumb.setVisibility(View.VISIBLE);

            Bitmap BMP_IMAGE;
            Uri targetURI;
            if (requestCode == PICK_CAMERA_REQUEST)  {
                targetURI = imageUri;
                Bitmap bitmap = BitmapFactory.decodeFile(targetURI.getPath());
                BMP_IMAGE = resizeBitmap(bitmap);
                imgvwConsultThumb.setImageBitmap(BMP_IMAGE);
                imgvwConsultThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);

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

                    /* GET THE FINAL CONSULTATION URI */
                    CONSULTATION_URI = Uri.fromFile(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == PICK_GALLERY_REQUEST) {
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    BMP_IMAGE = resizeBitmap(bitmap);
                    imgvwConsultThumb.setImageBitmap(BMP_IMAGE);
                    imgvwConsultThumb.setScaleType(ImageView.ScaleType.CENTER_CROP);

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

                        /* GET THE FINAL CONSULTATION URI */
                        CONSULTATION_URI = Uri.fromFile(file);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            /* HIDE THE IMAGE THUMB CARD VIEW */
            cardConsultThumb.setVisibility(View.GONE);
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
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        CONSULTATION_DATE = sdf.format(cal.getTime());
    }
}