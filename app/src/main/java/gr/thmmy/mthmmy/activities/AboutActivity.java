package gr.thmmy.mthmmy.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.base.BaseActivity;

public class AboutActivity extends BaseActivity {
    private static final int TIME_INTERVAL = 1000;
    private static final int TIMES_TO_PRESS = 4;
    private long mVersionLastPressedTime;
    private int mVersionPressedCounter;

    private AppBarLayout appBar;
    private FrameLayout trollGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        String versionName = BuildConfig.VERSION_NAME;

        //Initialize appbar
        appBar = (AppBarLayout) findViewById(R.id.appbar);
        //Initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.about);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        createDrawer();
        drawer.setSelection(ABOUT_ID);

        final ScrollView mainContent = (ScrollView) findViewById(R.id.scrollview);
        trollGif = (FrameLayout) findViewById(R.id.trollPicFrame);

        TextView tv = (TextView) findViewById(R.id.version);
        if (tv != null)
            tv.setText(getString(R.string.version, versionName));

        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mVersionLastPressedTime + TIME_INTERVAL > System.currentTimeMillis()) {
                    if (mVersionPressedCounter == TIMES_TO_PRESS) {
                        appBar.setVisibility(View.INVISIBLE);
                        mainContent.setVisibility(View.INVISIBLE);
                        trollGif.setVisibility(View.VISIBLE);
                        drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
                        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    }
                    mVersionLastPressedTime = System.currentTimeMillis();
                    ++mVersionPressedCounter;
                } else {
                    mVersionLastPressedTime = System.currentTimeMillis();
                    mVersionPressedCounter = 0;
                }
            }
        });

        //TODO: add licenses
    }

    @Override
    protected void onResume() {
        drawer.setSelection(ABOUT_ID);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (trollGif.getVisibility() == View.VISIBLE) {
            Toast toast = Toast.makeText(this, "NO EXIT FROM HERE!!\n\nHA HA HA!", Toast.LENGTH_SHORT);
            TextView v = (TextView) toast.getView().findViewById(android.R.id.message);
            if (v != null) v.setGravity(Gravity.CENTER);
            toast.show();
        } else
            super.onBackPressed();
    }
}
