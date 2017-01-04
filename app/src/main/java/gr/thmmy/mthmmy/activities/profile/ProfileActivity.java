package gr.thmmy.mthmmy.activities.profile;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.base.BaseActivity;
import gr.thmmy.mthmmy.activities.profile.latestPosts.LatestPostsFragment;
import gr.thmmy.mthmmy.activities.profile.summary.SummaryFragment;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.data.TopicSummary;
import gr.thmmy.mthmmy.utils.CircleTransform;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

/**
 * Activity for user profile. When creating an Intent of this activity you need to bundle a <b>String</b>
 * containing this user's profile url using the key {@link #BUNDLE_PROFILE_URL}, a <b>String</b> containing
 * this user's avatar url and a <b>String</b> containing the username.
 */
public class ProfileActivity extends BaseActivity implements LatestPostsFragment.LatestPostsFragmentInteractionListener{
    //Graphics
    private TextView personalTextView;
    private MaterialProgressBar progressBar;
    private FloatingActionButton replyFAB;
    private ViewPager viewPager;
    //Other variables and constants
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "ProfileActivity";
    /**
     * The key to use when putting profile's url String to {@link ProfileActivity}'s Bundle.
     */
    public static final String BUNDLE_PROFILE_URL = "PROFILE_URL";
    /**
     * The key to use when putting user's thumbnail url String to {@link ProfileActivity}'s Bundle.
     * If user doesn't have a thumbnail put an empty string.
     */
    public static final String BUNDLE_THUMBNAIL_URL = "THUMBNAIL_URL";
    /**
     * The key to use when putting username String to {@link ProfileActivity}'s Bundle.
     */
    public static final String BUNDLE_USERNAME = "USERNAME";
    private static final int THUMBNAIL_SIZE = 200;
    private ProfileTask profileTask;
    private String personalText;
    private String profileUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Bundle extras = getIntent().getExtras();
        String thumbnailUrl = extras.getString(BUNDLE_THUMBNAIL_URL);
        String username = extras.getString(BUNDLE_USERNAME);
        profileUrl = extras.getString(BUNDLE_PROFILE_URL);

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

        ImageView thumbnailView = (ImageView) findViewById(R.id.user_thumbnail);
        if (thumbnailUrl != null)
            //noinspection ConstantConditions
            Picasso.with(this)
                    .load(thumbnailUrl)
                    .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .centerCrop()
                    .error(ResourcesCompat.getDrawable(this.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .placeholder(ResourcesCompat.getDrawable(this.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .transform(new CircleTransform())
                    .into(thumbnailView);
        TextView usernameView = (TextView) findViewById(R.id.profile_activity_username);
        usernameView.setText(username);
        personalTextView = (TextView) findViewById(R.id.profile_activity_personal_text);

        viewPager = (ViewPager) findViewById(R.id.profile_tab_container);

        replyFAB = (FloatingActionButton) findViewById(R.id.profile_fab); //TODO hide fab while logged out
        replyFAB.setEnabled(false);
        replyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionManager.isLoggedIn()) {
                    //TODO
                    //PM
                } else {
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
                }
            }
        });

        profileTask = new ProfileTask();
        profileTask.execute(profileUrl); //Attempt data parsing
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (profileTask != null && profileTask.getStatus() != AsyncTask.Status.RUNNING)
            profileTask.cancel(true);
    }

    @Override
    public void onLatestPostsFragmentInteraction(TopicSummary topicSummary) {
        Intent i = new Intent(ProfileActivity.this, TopicActivity.class);
        i.putExtra(BUNDLE_TOPIC_URL, topicSummary.getTopicUrl());
        i.putExtra(BUNDLE_TOPIC_TITLE, topicSummary.getTitle());
        startActivity(i);
    }

    /**
     * An {@link AsyncTask} that handles asynchronous fetching of a profile page and parsing this
     * user's personal text.
     * <p>ProfileTask's {@link AsyncTask#execute execute} method needs a profile's url as String
     * parameter!</p>
     */
    public class ProfileTask extends AsyncTask<String, Void, Boolean> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        @SuppressWarnings("unused")
        private static final String TAG = "ProfileTask"; //Separate tag for AsyncTask
        Document profilePage;

        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            replyFAB.setEnabled(false);
        }

        protected Boolean doInBackground(String... profileUrl) {
            String pageUrl = profileUrl[0] + ";wap"; //Profile's page wap url

            Request request = new Request.Builder()
                    .url(pageUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                profilePage = Jsoup.parse(response.body().string());
                { //Finds personal text
                    Element tmpEl = profilePage.select("td.windowbg:nth-child(2)").first();
                    if (tmpEl != null) {
                        personalText = tmpEl.text().trim();
                    } else {
                        //Should never get here!
                        //Something is wrong.
                        Report.e(TAG, "An error occurred while trying to find profile's personal text.");
                        personalText = null;
                    }
                }
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

            if (personalText != null) {
                personalTextView.setVisibility(View.VISIBLE);
                personalTextView.setText(personalText);
            } else {
                personalTextView.setVisibility(View.GONE);
            }

            setupViewPager(viewPager, profilePage);
            TabLayout tabLayout = (TabLayout) findViewById(R.id.profile_tabs);
            tabLayout.setupWithViewPager(viewPager);
        }
    }

    /**
     * Simple method that sets up the {@link ViewPager} of a {@link ProfileActivity}
     * @param viewPager the ViewPager to be setup
     * @param profilePage this profile's parsed page
     */
    private void setupViewPager(ViewPager viewPager, Document profilePage) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFrag(SummaryFragment.newInstance(profilePage), "SUMMARY");
        adapter.addFrag(LatestPostsFragment.newInstance(profileUrl), "LATEST POSTS");
        //adapter.addFrag(new );
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }
        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }
        @Override
        public int getCount() {
            return mFragmentList.size();
        }
        void addFrag(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }
}
