package biz.zenpets.users.details.doctor.images;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import biz.zenpets.users.R;
import biz.zenpets.users.utils.TypefaceSpan;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ClinicGalleryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.clinic_gallery_activity);

        /***** CONFIGURE THE TOOLBAR *****/
        configTB();

        /** CAST THE VIEW PAGER INSTANCE **/
        ViewPager viewPager = (ViewPager) findViewById(R.id.pager);

        /** GET THE INCOMING POSITION **/
        Bundle bundle = getIntent().getExtras();
        String[] strImages = bundle.getStringArray("array");
        int position = bundle.getInt("position");

        /** CONVERT THE STRING ARRAY TO AN ARRAY LIST **/
        List<String> arrImages = new ArrayList<>(Arrays.asList(strImages));

        /** INSTANTIATE AND SET THE ADAPTER TO THE VIEW PAGER **/
        ClinicFullScreenAdapter adapter = new ClinicFullScreenAdapter(arrImages);
        viewPager.setAdapter(adapter);

        /** DISPLAY THE SELECTED IMAGE FIRST **/
        viewPager.setCurrentItem(position);
    }

    private class ClinicFullScreenAdapter extends PagerAdapter {

        final List<String> images;
        LayoutInflater inflater;

        ClinicFullScreenAdapter(List<String> arrImages) {
            this.images = arrImages;
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            PhotoView imgvwClinicImage;

            inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View viewLayout = inflater.inflate(R.layout.clinic_gallery_fullscreen, container, false);

            imgvwClinicImage = (PhotoView) viewLayout.findViewById(R.id.imgvwClinicImage);

            Glide.with(ClinicGalleryActivity.this)
                    .load(images.get(position))
                    .crossFade()
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(imgvwClinicImage);
            container.addView(viewLayout);

            return viewLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((LinearLayout) object);
        }
    }

    /***** CONFIGURE THE TOOLBAR *****/
    @SuppressWarnings("ConstantConditions")
    private void configTB() {
        Toolbar myToolbar = (Toolbar) findViewById(R.id.myToolbar);
        setSupportActionBar(myToolbar);

        String strTitle = "Images";
//        String strTitle = getString(R.string.add_a_new_medicine_record);
        SpannableString s = new SpannableString(strTitle);
        s.setSpan(new TypefaceSpan(getApplicationContext()), 0, s.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle(s);
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}