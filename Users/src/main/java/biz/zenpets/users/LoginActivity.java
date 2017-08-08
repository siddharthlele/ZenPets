package biz.zenpets.users;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import biz.zenpets.users.landing.LandingActivity;
import biz.zenpets.users.landing.NewLandingActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    /** THE FIREBASE AUTH INSTANCE **/
    private FirebaseAuth mAuth;

    /** A FIREBASE USER INSTANCE **/
    private FirebaseUser user;

    /** THE FIREBASE AUTH STATE LISTENER INSTANCE **/
    private FirebaseAuth.AuthStateListener mAuthListener;

    /** A GOOGLE API CLIENT INSTANCE **/
    private GoogleApiClient apiClient;

    /** THE STATIC REQUEST CODE FOR PERFORMING THE GOOGLE SIGN IN **/
    private static final int GOOGLE_SIGN_IN = 101;

    /** THE FACEBOOK CALLBACK MANAGER **/
    private CallbackManager callbackManager;

    /** THE USER DETAILS **/
    private String USER_DISPLAY_NAME = null;
    private String USER_AUTH_ID = null;
    private String USER_EMAIL = null;
    private String USER_PROFILE = null;
    private String USER_ACCOUNT_STATUS = "Incomplete";

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.fbLogin) LoginButton fbLogin;

    @OnClick(R.id.imgbtnGoogleSignIn) void googleLogin()   {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(apiClient);
        startActivityForResult(signInIntent, GOOGLE_SIGN_IN);
    }

    @OnClick(R.id.imgbtnFacebookSignIn) void facebookLogin()   {
        fbLogin.performClick();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);
        ButterKnife.bind(this);

        /* INITIALIZE THE FIREBASE AUTH INSTANCE **/
        mAuth = FirebaseAuth.getInstance();

        /* START THE AUTH STATE LISTENER **/
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    USER_AUTH_ID = user.getUid();
                    USER_DISPLAY_NAME = user.getDisplayName();
                    USER_EMAIL = user.getEmail();
                    if (user.getProviderData().get(1).getProviderId().equalsIgnoreCase("google.com"))   {
                        USER_PROFILE = String.valueOf(user.getProviderData().get(1).getPhotoUrl()) + "?sz=600";
                    } else if (user.getProviderData().get(1).getProviderId().equalsIgnoreCase("facebook.com")){
                        USER_PROFILE = "https://graph.facebook.com/" + user.getProviderData().get(1).getUid() + "/picture?width=4000";
                    }

                    /* CHECK IF THE USER RECORD EXISTS **/
                    new checkUserExists().execute();
                }
            }
        };

        /* CONFIGURE THE GOOGLE SIGN IN OPTIONS **/
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .requestProfile()
                .build();

        /* CONFIGURE THE GOOGLE API CLIENT INSTANCE **/
        apiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        /* CONFIGURE FOR THE FACEBOOK LOGIN **/
        callbackManager = CallbackManager.Factory.create();
        fbLogin.setReadPermissions("email", "public_profile");
        fbLogin.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
//                Log.e("FB SUCCESS", String.valueOf(loginResult));
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FB ERROR", String.valueOf(error));
            }
        });
    }

    /***** CHECK IF THE USER RECORD EXISTS *****/
    private class checkUserExists extends AsyncTask<Void, Void, Void>   {

        /** BOOLEAN TO CHECK IF ACCOUNT EXISTS **/
        boolean blnExists = false;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_RECORD = "http://leodyssey.com/ZenPets/public/profileExists";
            String URL_USER_RECORD = "http://192.168.11.2/zenpets/public/profileExists";
            HttpUrl.Builder builder = HttpUrl.parse(URL_USER_RECORD).newBuilder();
            builder.addQueryParameter("userAuthID", USER_AUTH_ID);
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
                if (JORoot.has("message"))  {
                    String strMessage = JORoot.getString("message");
                    if (strMessage.equalsIgnoreCase("User record doesn't exist"))    {
                        blnExists = false;
                    } else if (strMessage.equalsIgnoreCase("User record exists")) {
                        blnExists = true;
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

            if (!blnExists) {
                /* CREATE THE USERS PROFILE RECORD */
                new createUserRecord().execute();
            } else {
                Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    private class createUserRecord extends AsyncTask<Void, Void, Void>  {

        /** BOOLEAN TO TRACK USER RECORD CREATION **/
        boolean blnSuccess = false;

        /** STRING TO HOLD THE ERROR MESSAGE, IF ANY **/
        private String strErrorMessage = null;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            // String URL_USER_RECORD = "http://leodyssey.com/ZenPets/public/register";
            String URL_USER_RECORD = "http://192.168.11.2/zenpets/public/register";
            OkHttpClient client = new OkHttpClient();
            RequestBody body = new FormBody.Builder()
                    .add("userAuthID", user.getUid())
                    .add("userName", USER_DISPLAY_NAME)
                    .add("userEmail", USER_EMAIL)
                    .add("userDisplayProfile", USER_PROFILE)
                    .add("userPhonePrefix", "null")
                    .add("userPhoneNumber", "null")
                    .add("userGender", "null")
                    .add("countryID", "0")
                    .add("stateID", "0")
                    .add("cityID", "0")
                    .add("userType", "User")
                    .add("profileComplete", USER_ACCOUNT_STATUS)
                    .build();
            Request request = new Request.Builder()
                    .url(URL_USER_RECORD)
                    .post(body)
                    .build();
            Call call = client.newCall(request);
            try {
                Response response = call.execute();
                String strResult = response.body().string();
                JSONObject JORoot = new JSONObject(strResult);
                if (JORoot.has("error") && JORoot.getString("error").equalsIgnoreCase("false")) {
                    /* TOGGLE THE BOOLEAN FLAG TO TRUE */
                    blnSuccess = true;
                } else {
                    /* TOGGLE THE BOOLEAN FLAG TO FALSE */
                    blnSuccess = false;

                    /* SET THE ERROR MESSAGE */
                    strErrorMessage = "There was en error signing you in. Please try signing in again.";
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            if (!blnSuccess) {
                /* LOGOUT THE USER */
                mAuth.signOut();

                /* SHOW THE ERROR MESSAGE */
                new MaterialDialog.Builder(LoginActivity.this)
                        .title("Registration Failed!")
                        .content(strErrorMessage)
                        .positiveText("OKAY")
                        .theme(Theme.LIGHT)
                        .icon(ContextCompat.getDrawable(LoginActivity.this, R.drawable.ic_info_outline_black_24dp))
                        .typeface("Roboto-Medium.ttf", "Roboto-Regular.ttf")
                        .show();
            } else {
                Intent intent = new Intent(LoginActivity.this, LandingActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GOOGLE_SIGN_IN)  {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
//                Log.e("Google Login Status", String.valueOf(result.getStatus()));
                Toast.makeText(getApplicationContext(), "Google sign in failed. Please try again..", Toast.LENGTH_LONG).show();
            }
        } else {
            /* FOR THE FACEBOOK LOGIN **/
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    /** HANDLE THE FACEBOOK LOGIN SUCCESS **/
    private void handleFacebookAccessToken(AccessToken accessToken) {
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())    {
                            Toast.makeText(getApplicationContext(), "Facebook login successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("FB EXCEPTION", String.valueOf(task.getException()));
                            Toast.makeText(getApplicationContext(), "Facebook sign in failed. Please try again..", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    /** HANDLE THE GOOGLE LOGIN SUCCESS **/
    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())    {
                            Toast.makeText(getApplicationContext(), "Google sign in successful", Toast.LENGTH_SHORT).show();
                        } else {
//                            Log.e("Google Login", String.valueOf(task.getException()));
                            Toast.makeText(getApplicationContext(), "Google authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}