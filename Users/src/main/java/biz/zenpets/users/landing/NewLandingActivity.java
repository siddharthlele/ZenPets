package biz.zenpets.users.landing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import biz.zenpets.users.R;
import butterknife.ButterKnife;

public class NewLandingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_landing_activity);
        ButterKnife.bind(this);
    }
}