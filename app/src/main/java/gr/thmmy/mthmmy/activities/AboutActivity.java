package gr.thmmy.mthmmy.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.AppBarLayout;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.io.AssetUtils;

public class AboutActivity extends BaseActivity {
    private static final int TIME_INTERVAL = 1000;
    private static final int TIMES_TO_PRESS = 4;
    private long mVersionLastPressedTime;
    private int mVersionPressedCounter;

    private AppBarLayout appBar;
    private CoordinatorLayout coordinatorLayout;
    private ScrollView mainContent;
    private AlertDialog alertDialog;
    private FrameLayout easterEggImage;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        String versionName = BuildConfig.VERSION_NAME;

        boolean gitExists = true;

        String commitHash = BuildConfig.COMMIT_HASH;
        if (commitHash.length() > 8)
            commitHash = commitHash.substring(0, 8);
        else
            gitExists = false;

        String versionInfo = "";
        if(gitExists)
            versionInfo = "-" + BuildConfig.CURRENT_BRANCH + "-" + commitHash
                    + (BuildConfig.IS_CLEAN ? "" : "-dirty")
                    + " ";  // Avoid last letter being cut in italics styled TextView

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

        mainContent = findViewById(R.id.scrollview);
        easterEggImage = findViewById(R.id.trollPicFrame);

        // Set Easter egg on logo image
        ImageView logoImageView = findViewById(R.id.logoView);
        logoImageView.setOnClickListener(view -> {
            if (mVersionLastPressedTime + TIME_INTERVAL > System.currentTimeMillis()) {
                if (mVersionPressedCounter == TIMES_TO_PRESS)
                    showEasterEgg();
                mVersionLastPressedTime = System.currentTimeMillis();
                ++mVersionPressedCounter;
            } else {
                mVersionLastPressedTime = System.currentTimeMillis();
                mVersionPressedCounter = 0;
            }
        });

        TextView versionTextView = findViewById(R.id.version);
        if (versionTextView != null) {
            if (BuildConfig.DEBUG)
                versionTextView.setText(getString(R.string.version, versionName + versionInfo));
            else
                versionTextView.setText(getString(R.string.version, versionName));

            if(gitExists){
                versionTextView.setOnClickListener(view -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/ThmmyNoLife/mTHMMY/commit/" + BuildConfig.COMMIT_HASH));
                    startActivity(intent);
                });
            }

            versionTextView.setOnLongClickListener(view -> {
                Toast.makeText(getApplicationContext(), BaseApplication.getFirebaseProjectId(), Toast.LENGTH_SHORT).show();
                return true;
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

    @Override
    public void onBackPressed() {
        if(easterEggImage.getVisibility()==View.INVISIBLE)
            super.onBackPressed();
        else
            hideEasterEgg();
    }

    public void displayLibraries(View v) {
        String libraryType = v.getTag().toString();
        String title="", fileName="";
        switch(libraryType) {
            case "APACHE":
                title=getString(R.string.apache_v2_0_libraries);
                fileName="apache_libraries.html";
                break;
            case "MIT":
                title=getString(R.string.the_mit_libraries);
                fileName="mit_libraries.html";
                break;
            case "EPL":
                title=getString(R.string.epl_libraries);
                fileName="epl_libraries.html";
                break;
            case "OTHER":
                title=getString(R.string.other_libraries);
                fileName="other_libraries.html";
                break;
            default:
                break;
        }

        String htmlContent = AssetUtils.readFileToText(this,fileName);

        LayoutInflater inflater = LayoutInflater.from(this);
        WebView webView = (WebView) inflater.inflate(R.layout.dialog_licenses, coordinatorLayout, false);
        webView.setBackgroundColor(Color.argb(1, 255, 255, 255));
        webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null);

        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.95);
        alertDialog = new AlertDialog.Builder(this, R.style.AppTheme_Dark_Dialog)
                .setTitle(title)
                .setView(webView)
                .setPositiveButton(android.R.string.ok, null)
                .show();
        if(alertDialog.getWindow()!=null)
            alertDialog.getWindow().setLayout(width, height);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void showEasterEgg(){
        if(getResources().getConfiguration().orientation==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);  //TODO: why?
            appBar.setVisibility(View.INVISIBLE);
            mainContent.setVisibility(View.INVISIBLE);
            easterEggImage.setVisibility(View.VISIBLE);
            drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }
    }

    private void hideEasterEgg(){
        appBar.setVisibility(View.VISIBLE);
        mainContent.setVisibility(View.VISIBLE);
        easterEggImage.setVisibility(View.INVISIBLE);
        drawer.getDrawerLayout().setDrawerLockMode(DrawerLayout.LOCK_MODE_UNDEFINED);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }
}
