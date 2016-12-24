package gr.thmmy.mthmmy.activities.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
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
import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

import static gr.thmmy.mthmmy.activities.profile.ProfileParser.NAME_INDEX;
import static gr.thmmy.mthmmy.activities.profile.ProfileParser.PERSONAL_TEXT_INDEX;
import static gr.thmmy.mthmmy.activities.profile.ProfileParser.THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileParser.parseProfile;
import static gr.thmmy.mthmmy.session.SessionManager.LOGGED_IN;
import static gr.thmmy.mthmmy.session.SessionManager.LOGIN_STATUS;

public class ProfileActivity extends BaseActivity {

    //Graphic elements
    private ImageView userThumbnail;
    private TextView userName;
    private TextView personalText;
    private LinearLayout mainContent;
    private ProgressBar progressBar;
    private FloatingActionButton replyFAB;

    //Other variables
    private ArrayList<String> parsedProfileData;
    @SuppressWarnings("unused")
    private static final String TAG = "ProfileActivity";
    static String PACKAGE_NAME;
    private static final int THUMBNAIL_SIZE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        Bundle extras = getIntent().getExtras();
        //username = getIntent().getExtras().getString("TOPIC_TITLE");

        //Initialize toolbar, drawer and ProgressBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(null);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        createDrawer();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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
                    new AlertDialog.Builder(ProfileActivity.this)
                            .setTitle("ERROR!")
                            .setMessage("An error occurred while trying to find your LOGIN_STATUS.\n" +
                                    "Please sent below info to developers:\n"
                                    + getLocalClassName() + "." + "l"
                                    + Thread.currentThread().getStackTrace()[1].getLineNumber())
                            .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Todo
                                    //Maybe sent info back to developers?
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

        new ProfileTask().execute(extras.getString("PROFILE_URL")); //Attempt data parsing
    }

    public class ProfileTask extends AsyncTask<String, Void, Boolean> {
        //Class variables
        private static final String TAG = "TopicTask"; //Separate tag for AsyncTask

        //Show a progress bar until done
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            replyFAB.setEnabled(false);
        }

        protected Boolean doInBackground(String... strings) {
            Document document;
            String pageUrl = strings[0]; //This page's url


            Request request = new Request.Builder()
                    .url(pageUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());
                //long parseStartTime = System.nanoTime();
                parsedProfileData = parseProfile(document); //Parse data
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
                //Should never happen
                Toast.makeText(getBaseContext()
                        , "Fatal error!\n Aborting...", Toast.LENGTH_LONG).show();
                finish();
            }
            //Parse was successful
            progressBar.setVisibility(ProgressBar.INVISIBLE); //Hide progress bar
            populateLayout(); //Show parsed data
        }
    }

    private void populateLayout() {
        if (parsedProfileData.get(THUMBNAIL_URL) != null)
            Picasso.with(this)
                    .load(parsedProfileData.get(THUMBNAIL_URL))
                    .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .centerCrop()
                    .error(ResourcesCompat.getDrawable(this.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .placeholder(ResourcesCompat.getDrawable(this.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .transform(new CircleTransform())
                    .into(userThumbnail);
        userName.setText(parsedProfileData.get(NAME_INDEX));
        if (parsedProfileData.get(PERSONAL_TEXT_INDEX) != null) {
            personalText.setVisibility(View.VISIBLE);
            personalText.setText(parsedProfileData.get(PERSONAL_TEXT_INDEX));
        } else {
            personalText.setVisibility(View.GONE);
        }

        for (int i = PERSONAL_TEXT_INDEX; i < parsedProfileData.size(); ++i) {
            TextView entry = new TextView(this);
            entry.setTextColor(getResources().getColor(R.color.primary_text));
            entry.setText(Html.fromHtml(parsedProfileData.get(i)));
            mainContent.addView(entry);
        }
    }
}
