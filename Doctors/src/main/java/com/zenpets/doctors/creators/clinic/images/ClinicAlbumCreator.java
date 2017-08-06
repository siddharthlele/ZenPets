package com.zenpets.doctors.creators.clinic.images;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.darsh.multipleimageselect.activities.AlbumSelectActivity;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Image;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.zenpets.doctors.R;
import com.zenpets.doctors.utils.TypefaceSpan;
import com.zenpets.doctors.utils.adapters.clinics.images.ClinicAlbumCreatorAdapter;
import com.zenpets.doctors.utils.models.clinics.images.ClinicAlbumCreatorData;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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

public class ClinicAlbumCreator extends AppCompatActivity {

    /** THE INCOMING CLINIC ID **/
    String CLINIC_ID = null;

    /** THE ADAPTER AND ARRAY LIST FOR THE CLINIC ALBUMS **/
    ClinicAlbumCreatorAdapter adapter;
    ArrayList<ClinicAlbumCreatorData> arrAlbums = new ArrayList<>();

    /** THE PROGRESS DIALOG **/
    ProgressDialog dialog;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.linlaHeaderProgress) LinearLayout linlaHeaderProgress;
    @BindView(R.id.gridClinicImages) RecyclerView gridClinicImages;
    @BindView(R.id.linlaEmpty) LinearLayout linlaEmpty;

    /** SELECT CLINIC IMAGES **/
    @OnClick(R.id.linlaEmpty) void selectImages()    {
        Intent intent = new Intent(this, AlbumSelectActivity.class);
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 8);
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clinic_album_creator_list);
        ButterKnife.bind(this);

        /** CONFIGURE THE ACTIONBAR **/
        configAB();

        /** CONFIGURE THE RECYCLER VIEW **/
        configRecycler();

        /** GET THE INCOMING DATA **/
        getIncomingData();
    }

    /***** UPLOAD THE CLINIC IMAGES *****/
    private void uploadClinicImages() {
        dialog = new ProgressDialog(this);
        dialog.setMessage("Please wait while we publish your clinic pictures");
        dialog.setIndeterminate(false);
        dialog.setCancelable(false);
        dialog.show();

        if (arrAlbums != null || arrAlbums.size() > 0) {
            for (int i = 0; i < arrAlbums.size(); i++) {

                Bitmap bitmap = arrAlbums.get(i).getBmpClinicImage();
                String root = Environment.getExternalStorageDirectory().toString();
                File myDir = new File(root + "/ZenPets/Clinic");
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
                    String FILE_NAME = "CLINIC_" + CLINIC_ID + "_" + fName;
                    StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                    StorageReference refStorage = storageReference.child("Clinic Images").child(FILE_NAME);
                    refStorage.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @SuppressWarnings("VisibleForTests")
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Uri downloadURL = taskSnapshot.getDownloadUrl();
                            if (downloadURL != null) {
                                /* THE URL TO UPLOAD A NEW CLINIC IMAGE */
                                String URL_POST_ADOPTION_IMAGE = "http://leodyssey.com/ZenPets/public/postClinicImages";
                                // String URL_POST_ADOPTION_IMAGE = "http://192.168.11.2/zenpets/public/postClinicImages";

                                OkHttpClient client = new OkHttpClient();
                                RequestBody body = new FormBody.Builder()
                                        .add("clinicID", CLINIC_ID)
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
                                            Log.e("IMAGE UPLOAD RESULT", String.valueOf(JORoot));
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
                            Toast.makeText(getApplicationContext(), "New clinic images published successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    });
                }
            }
        }
    }

    /***** GET THE INCOMING DATA *****/
    private void getIncomingData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("CLINIC_ID"))    {
            CLINIC_ID = bundle.getString("CLINIC_ID");
            if (CLINIC_ID == null)  {
                Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                gridClinicImages.setVisibility(View.GONE);
                linlaEmpty.setVisibility(View.VISIBLE);
            }
        } else {
            Toast.makeText(getApplicationContext(), "Failed to get required information", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /***** CONFIGURE THE ACTIONBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configAB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Add Clinic Images";
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
        MenuInflater inflater = new MenuInflater(ClinicAlbumCreator.this);
        inflater.inflate(R.menu.activity_clinic_image_uploader, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.menuUpload:
                /** CHECK THE ARRAY SIZE **/
                if (arrAlbums.size() == 0)  {
                    Toast.makeText(getApplicationContext(), getString(R.string.clinic_images_empty), Toast.LENGTH_LONG).show();
                } else {
                    /** UPLOAD THE CLINIC IMAGES**/
                    uploadClinicImages();
                }
                break;
            case R.id.cancel:
                finish();
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {

            /** CLEAR THE ARRAY LIST **/
            arrAlbums.clear();

            /** GET THE ARRAY LIST OF IMAGES RETURNED BY THE INTENT **/
            ArrayList<Image> images = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES);

            /** A NEW INSTANCE OF THE CLINIC ALBUMS MODEL **/
            ClinicAlbumCreatorData albums;
            for (int i = 0, l = images.size(); i < l; i++) {
                /***** INSTANTIATE THE ClinicAlbumCreatorData INSTANCE "albums" *****/
                albums = new ClinicAlbumCreatorData();

                /** GET THE IMAGE PATH **/
                String strPath = images.get(i).path;
                File filePath = new File(strPath);
                Bitmap bitmap = BitmapFactory.decodeFile(filePath.getAbsolutePath());
                Bitmap bmpImage = resizeBitmap(bitmap);
                albums.setBmpClinicImage(bmpImage);

                /** SET THE IMAGE NUMBER **/
                String strNumber = String.valueOf(i + 1);
                albums.setTxtImageNumber(strNumber);

                /** ADD THE COLLECTED DATA TO THE ARRAY LIST **/
                arrAlbums.add(albums);
            }

            /** INSTANTIATE THE ADAPTER **/
            adapter = new ClinicAlbumCreatorAdapter(ClinicAlbumCreator.this, arrAlbums);

            /** SET THE ADAPTER TO THE RECYCLER VIEW **/
            gridClinicImages.setAdapter(adapter);
            gridClinicImages.setVisibility(View.VISIBLE);
            linlaEmpty.setVisibility(View.GONE);
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

    /** CONFIGURE THE RECYCLER VIEW **/
    private void configRecycler() {
        int intOrientation = getResources().getConfiguration().orientation;
        gridClinicImages.setHasFixedSize(true);
        GridLayoutManager glm = null;
        boolean isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet)   {
            if (intOrientation == 1)	{
                glm = new GridLayoutManager(this, 2);
            } else if (intOrientation == 2) {
                glm = new GridLayoutManager(this, 4);
            }
        } else {
            if (intOrientation == 1)    {
                glm = new GridLayoutManager(this, 2);
            } else if (intOrientation == 2) {
                glm = new GridLayoutManager(this, 4);
            }
        }
        gridClinicImages.setLayoutManager(glm);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}