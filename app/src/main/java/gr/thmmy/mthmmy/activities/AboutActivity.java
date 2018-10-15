package gr.thmmy.mthmmy.activities;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;

public class AboutActivity extends BaseActivity {
    private static final int TIME_INTERVAL = 1000;
    private static final int TIMES_TO_PRESS = 4;
    private long mVersionLastPressedTime;
    private int mVersionPressedCounter;

    private AppBarLayout appBar;
    private CoordinatorLayout coordinatorLayout;
    private AlertDialog alertDialog;
    private FrameLayout trollGif;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        String versionName = BuildConfig.VERSION_NAME;

        //Initialize appbar
        appBar = findViewById(R.id.appbar);
        coordinatorLayout = findViewById(R.id.main_content);
        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.about);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(ABOUT_ID);

        final ScrollView mainContent = findViewById(R.id.scrollview);
        trollGif = findViewById(R.id.trollPicFrame);

        TextView tv = findViewById(R.id.version);
        if (tv != null) {
            if (BuildConfig.DEBUG)
                tv.setText(getString(R.string.version, versionName + "-debug"));
            else
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
        }

        TextView privacyPolicy = findViewById(R.id.privacy_policy_header);
        privacyPolicy.setMovementMethod(new LinkMovementMethod());
        SpannableString spannableString = new SpannableString(privacyPolicy.getText());
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), 0);
        privacyPolicy.setText(spannableString);
        privacyPolicy.setOnClickListener(view -> showPrivacyPolicyDialog());

    }

    @Override
    protected void onResume() {
        drawer.setSelection(ABOUT_ID);
        super.onResume();
    }

    public void displayApacheLibraries(View v) {
        LayoutInflater inflater = LayoutInflater.from(this);
        WebView webView = (WebView) inflater.inflate(R.layout.dialog_licenses, coordinatorLayout, false);
        webView.loadUrl("file:///android_asset/apache_libraries.html");
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.95);
        alertDialog = new AlertDialog.Builder(this, R.style.AppTheme_Dark_Dialog)
                .setTitle(getString(R.string.apache_v2_0_libraries))
                .setView(webView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        if(alertDialog.getWindow()!=null)
            alertDialog.getWindow().setLayout(width, height);
    }

    public void displayMITLibraries(View v) {
        LayoutInflater inflater = LayoutInflater.from(this);
        WebView webView = (WebView) inflater.inflate(R.layout.dialog_licenses, coordinatorLayout, false);
        webView.loadUrl("file:///android_asset/mit_libraries.html");
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.95);
        alertDialog = new AlertDialog.Builder(this, R.style.AppTheme_Dark_Dialog)
                .setTitle(getString(R.string.the_mit_libraries))
                .setView(webView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        if(alertDialog.getWindow()!=null)
            alertDialog.getWindow().setLayout(width, height);
    }

}
