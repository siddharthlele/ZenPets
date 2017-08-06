package biz.zenpets.users.utils;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import biz.zenpets.users.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class AppPrefs extends Application {

    /***** SHARED PREFERENCES INSTANCE *****/
    private SharedPreferences mPreferences;

    /** THE USER ID **/
    private final String USER_ID = "userID";

    /** THE PROFILE STATUS **/
    private final String PROFILE_STATUS = "profileStatus";

    @Override
    public void onCreate() {
        super.onCreate();

        /* INSTANTIATE THE FACEBOOK SDK **/
        FacebookSdk.sdkInitialize(this);
        AppEventsLogger.activateApp(this);

        /* INSTANTIATE THE PREFERENCE MANAGER */
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        /* INSTANTIATE THE CALLIGRAPHY LIBRARY */
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());

        /* INSTANTIATE THE IMAGE LOADER **/
        initImageLoader(getApplicationContext());

//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "com.zenpets.users", PackageManager.GET_SIGNATURES);
//            for (Signature signature: info.signatures)	{
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.e("FACEBOOK APP SIGNATURE", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
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

    /***** SET THE LOGGED IN USER'S ID *****/
    public void setUserID(String strUserID) {
        final SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString(USER_ID, strUserID);
        edit.apply();
    }

    /***** GET THE LOGGED IN USERS'S ID *****/
    public String getUserID()	{
        return mPreferences.getString(USER_ID, null);
    }

    /***** SET THE USER'S PROFILE STATUS *****/
    public void setProfileStatus(String profileStatus) {
        final SharedPreferences.Editor edit = mPreferences.edit();
        edit.putString(PROFILE_STATUS, profileStatus);
        edit.apply();
    }

    /***** GET THE USERS'S PROFILE STATUS *****/
    public String getProfileStatus()	{
        return mPreferences.getString(PROFILE_STATUS, null);
    }
}