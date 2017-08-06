package com.zenpets.doctors.credentials;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.firebase.auth.FirebaseAuth;
import com.zenpets.doctors.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ForgotPasswordActivity extends AppCompatActivity {

    /** THE FIREBASE AUTH INSTANCE **/
    private FirebaseAuth auth;

    /** CAST THE LAYOUT ELEMENTS **/
    @BindView(R.id.edtEmailAddress) AppCompatEditText edtEmailAddress;

    /** VALIDATE EMAIL ADDRESS AND RESET PASSWORD **/
    @OnClick(R.id.btnResetPassword) void resetPassword()    {
        if (!TextUtils.isEmpty(edtEmailAddress.getText().toString().trim()))    {
            auth.sendPasswordResetEmail(edtEmailAddress.getText().toString().trim());{
                String message = "A link to reset your password has been sent to your registered email. \n\nFollow the instructions in the email to create a new password. You can login to Zen Pets after resetting your password";
                new MaterialDialog.Builder(ForgotPasswordActivity.this)
                        .icon(ContextCompat.getDrawable(ForgotPasswordActivity.this, R.drawable.ic_info_outline_black_24dp))
                        .title("Password Reset")
                        .cancelable(true)
                        .content(message)
                        .positiveText("Okay")
                        .theme(Theme.LIGHT)
                        .typeface("HelveticaNeueLTW1G-MdCn.otf", "HelveticaNeueLTW1G-Cn.otf")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                Intent showLanding = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                showLanding.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(showLanding);
                                finish();
                            }
                        }).show();
            }
        } else {
            edtEmailAddress.setError("Enter your registered Email Address");
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forgot_password);
        ButterKnife.bind(this);

        /** INITIALIZE THE FIREBASE AUTH INSTANCE **/
        auth = FirebaseAuth.getInstance();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}