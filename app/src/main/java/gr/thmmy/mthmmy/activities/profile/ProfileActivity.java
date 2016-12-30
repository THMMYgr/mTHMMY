package gr.thmmy.mthmmy.activities.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.base.BaseActivity;
import gr.thmmy.mthmmy.utils.CircleTransform;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

import static gr.thmmy.mthmmy.activities.profile.ProfileParser.PERSONAL_TEXT_INDEX;
import static gr.thmmy.mthmmy.activities.profile.ProfileParser.THUMBNAIL_URL_INDEX;
import static gr.thmmy.mthmmy.activities.profile.ProfileParser.USERNAME_INDEX;
import static gr.thmmy.mthmmy.activities.profile.ProfileParser.parseProfileSummary;
import static gr.thmmy.mthmmy.session.SessionManager.LOGGED_IN;
import static gr.thmmy.mthmmy.session.SessionManager.LOGIN_STATUS;

/**
 * Activity for user's profile. When creating an Intent of this activity you need to bundle a <b>String</b>
 * containing this user's profile url using the key {@link #EXTRAS_PROFILE_URL}.
 */
public class ProfileActivity extends BaseActivity {
    //Graphic element variables
    private ImageView userThumbnail;
    private TextView userName;
    private TextView personalText;
    private LinearLayout mainContent;
    private MaterialProgressBar progressBar;
    private FloatingActionButton replyFAB;

    //Other variables
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "ProfileActivity";
    static String PACKAGE_NAME;
    /**
     * The key to use when putting profile's url String to {@link ProfileActivity}'s Bundle.
     */
    public static final String EXTRAS_PROFILE_URL = "PROFILE_URL";
    /**
     * {@link ArrayList} of Strings used to hold profile's information. Data are added in {@link ProfileTask}.
     */
    private ArrayList<String> parsedProfileData;
    private ProfileTask profileTask;
    private static final int THUMBNAIL_SIZE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        Bundle extras = getIntent().getExtras();

        //Initializes graphic elements
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        createDrawer();

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);

        userThumbnail = (ImageView) findViewById(R.id.user_thumbnail);
        userName = (TextView) findViewById(R.id.profile_act_username);
        personalText = (TextView) findViewById(R.id.profile_act_personal_text);
        mainContent = (LinearLayout) findViewById(R.id.profile_act_content);

        replyFAB = (FloatingActionButton) findViewById(R.id.profile_fab);
        replyFAB.setEnabled(false);
        replyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
                int tmp_curr_status = sharedPrefs.getInt(LOGIN_STATUS, -1);
                if (tmp_curr_status == -1) {
                    Report.e(TAG, "Error while getting LOGIN_STATUS");
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle("ERROR!")
                            .setMessage("An error occurred while trying to find your LOGIN_STATUS.")
                            .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                } else if (tmp_curr_status != LOGGED_IN) {
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setMessage("You need to be logged in to sent a personal message!")
                            .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                    overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                } else {
                    //TODO
                    //PM
                }
            }
        });

        //Gets info
        parsedProfileData = new ArrayList<>();
        profileTask = new ProfileTask();
        profileTask.execute(extras.getString(EXTRAS_PROFILE_URL)); //Attempt data parsing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileTask != null && profileTask.getStatus() != AsyncTask.Status.RUNNING)
            profileTask.cancel(true);
    }

    /**
     * An {@link AsyncTask} that handles asynchronous fetching of a profile page and parsing it's
     * data. {@link AsyncTask#onPostExecute(Object) OnPostExecute} method calls {@link #populateLayout()}
     * to build graphics.
     * <p>
     * <p>Calling ProfileTask's {@link AsyncTask#execute execute} method needs to have profile's url
     * as String parameter!</p>
     */
    public class ProfileTask extends AsyncTask<String, Void, Boolean> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        private static final String TAG = "TopicTask"; //Separate tag for AsyncTask

        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            replyFAB.setEnabled(false);
        }

        protected Boolean doInBackground(String... profileUrl) {
            Document document;
            String pageUrl = profileUrl[0] + ";wap"; //Profile's page wap url

            Request request = new Request.Builder()
                    .url(pageUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());
                //long parseStartTime = System.nanoTime();
                parsedProfileData = parseProfileSummary(document);
                //long parseEndTime = System.nanoTime();
                return true;
            } catch (SSLHandshakeException e) {
                Report.w(TAG, "Certificate problem (please switch to unsafe connection).");
            } catch (Exception e) {
                Report.e("TAG", "ERROR", e);
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (!result) { //Parse failed!
                Toast.makeText(getBaseContext()
                        , "Fatal error!\n Aborting...", Toast.LENGTH_LONG).show();
                finish();
            }
            //Parse was successful
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            populateLayout();
        }
    }

    /**
     * Simple method that builds the UI of a {@link ProfileActivity}.
     * <p>Use this method <b>only after</b> parsing profile's data with {@link ProfileTask} as it
     * reads from {@link #parsedProfileData}</p>
     */
    private void populateLayout() {
        if (parsedProfileData.get(THUMBNAIL_URL_INDEX) != null)
            //noinspection ConstantConditions
            Picasso.with(this)
                    .load(parsedProfileData.get(THUMBNAIL_URL_INDEX))
                    .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .centerCrop()
                    .error(ResourcesCompat.getDrawable(this.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .placeholder(ResourcesCompat.getDrawable(this.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .transform(new CircleTransform())
                    .into(userThumbnail);

        userName.setText(parsedProfileData.get(USERNAME_INDEX));

        if (parsedProfileData.get(PERSONAL_TEXT_INDEX) != null) {
            personalText.setVisibility(View.VISIBLE);
            personalText.setText(parsedProfileData.get(PERSONAL_TEXT_INDEX));
        } else {
            personalText.setVisibility(View.GONE);
        }

        for (int i = PERSONAL_TEXT_INDEX + 1; i < parsedProfileData.size(); ++i) {
            if (parsedProfileData.get(i).contains("Signature")
                    || parsedProfileData.get(i).contains("Υπογραφή")) {
                WebView signatureEntry = new WebView(this);
                signatureEntry.loadDataWithBaseURL("file:///android_asset/", parsedProfileData.get(i), "text/html", "UTF-8", null);
            }
            TextView entry = new TextView(this);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                entry.setTextColor(getResources().getColor(R.color.primary_text, null));
            } else {
                //noinspection deprecation
                entry.setTextColor(getResources().getColor(R.color.primary_text));

            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                entry.setText(Html.fromHtml(parsedProfileData.get(i), Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                entry.setText(Html.fromHtml(parsedProfileData.get(i)));
            }

            mainContent.addView(entry);
            Log.d(TAG, "new: " + parsedProfileData.get(i));
        }
    }
}
