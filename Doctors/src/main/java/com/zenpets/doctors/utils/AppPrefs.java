package com.zenpets.doctors.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.zenpets.doctors.R;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class AppPrefs extends Application {

    /***** SHARED PREFERENCES INSTANCE *****/
    public SharedPreferences mPreferences;

    /** THE DOCTOR'S ID **/
    private final String DOCTOR_ID = "doctorID";

    @Override
    public void onCreate() {
        super.onCreate();

        /** INSTANTIATE THE PREFERENCE MANAGER **/
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /* INSTANTIATE THE CALLIGRAPHY LIBRARY */
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        /* INSTANTIATE THE IMAGE LOADER **/
        initImageLoader(getApplicationContext());
    }

    /** INSTANTIATE THE IMAGE LOADER **/
    private void initImageLoader(Context context) {
        ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(context);
        config.threadPriority(Thread.NORM_PRIORITY - 2);
        config.denyCacheImageMultipleSizesInMemory();
        config.diskCacheFileNameGenerator(new Md5FileNameGenerator());
        config.diskCacheSize(50 * 1024 * 1024); // 50 MiB
        config.tasksProcessingOrder(QueueProcessingType.LIFO);
        config.writeDebugLogs(); // Remove for release app

        /* Initialize ImageLoader with configuration. */
        ImageLoader.getInstance().init(config.build());
    }

    /** SET THE LOGGED IN DOCTOR'S ID **/
//    public void setDoctorID(String strUserID) {
//        final SharedPreferences.Editor edit = mPreferences.edit();
//        edit.putString(DOCTOR_ID, strUserID);
//        edit.apply();
//    }

    /** GET THE LOGGED IN DOCTOR'S ID **/
//    public String getDoctorID()	{
//        return mPreferences.getString(DOCTOR_ID, null);
//    }
}