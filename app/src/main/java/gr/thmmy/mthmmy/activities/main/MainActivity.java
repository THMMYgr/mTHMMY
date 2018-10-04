package gr.thmmy.mthmmy.activities.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.PreferenceManager;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.downloads.DownloadsActivity;
import gr.thmmy.mthmmy.activities.main.forum.ForumFragment;
import gr.thmmy.mthmmy.activities.main.recent.RecentFragment;
import gr.thmmy.mthmmy.activities.main.shoutbox.ShoutboxFragment;
import gr.thmmy.mthmmy.activities.main.unread.UnreadFragment;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Board;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.model.TopicSummary;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_TITLE;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

public class MainActivity extends BaseActivity implements RecentFragment.RecentFragmentInteractionListener, ForumFragment.ForumFragmentInteractionListener, UnreadFragment.UnreadFragmentInteractionListener {

    //-----------------------------------------CLASS VARIABLES------------------------------------------
    private static final int TIME_INTERVAL = 2000;
    private SharedPreferences sharedPrefs;
    private static final String DRAWER_INTRO = "DRAWER_INTRO";
    private long mBackPressed;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intentFilter = getIntent();
        redirectToActivityFromIntent(intentFilter);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.app_preferences, false);

        if (sessionManager.isLoginScreenDefault()) {
            //Go to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }

        //Initialize drawer
        createDrawer();

        //Create the adapter that will return a fragment for each section of the activity
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.addFragment(RecentFragment.newInstance(1), "RECENT");
        sectionsPagerAdapter.addFragment(ForumFragment.newInstance(2), "FORUM");
        if (sessionManager.isLoggedIn())
            sectionsPagerAdapter.addFragment(UnreadFragment.newInstance(3), "UNREAD");

        FragmentTransaction initialFragmentTransaction = getSupportFragmentManager().beginTransaction();
        RecentFragment initialRecentFragment = RecentFragment.newInstance(1);
        initialFragmentTransaction.add(R.id.fragment_container, initialRecentFragment);
        initialFragmentTransaction.commit();

        bottomNavigation = findViewById(R.id.main_bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(menuItem -> {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            switch (menuItem.getItemId()) {
                case R.id.action_recent:
                    fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    RecentFragment recentFragment = RecentFragment.newInstance(1);
                    fragmentTransaction.replace(R.id.fragment_container, recentFragment);
                    fragmentTransaction.commit();
                    return true;
                case R.id.action_forum:
                    ForumFragment forumFragment = ForumFragment.newInstance(2);
                    fragmentTransaction.replace(R.id.fragment_container, forumFragment);
                    fragmentTransaction.commit();
                    return true;
                case R.id.action_unread:
                    UnreadFragment unreadFragment = UnreadFragment.newInstance(3);
                    fragmentTransaction.replace(R.id.fragment_container, unreadFragment);
                    fragmentTransaction.commit();
                    return true;
                case R.id.action_shoutbox:
                    ShoutboxFragment shoutboxFragment = ShoutboxFragment.newInstance(4);
                    fragmentTransaction.replace(R.id.fragment_container, shoutboxFragment);
                    fragmentTransaction.commit();
                    return true;
                default:
                    return false;
            }
        });

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        setMainActivity(this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        redirectToActivityFromIntent(intent);
        setIntent(intent);
    }

    @Override
    protected void onResume() {
        drawer.setSelection(HOME_ID);
        if (!sharedPrefs.getBoolean(DRAWER_INTRO, false)) {
            drawer.openDrawer();
            sharedPrefs.edit().putBoolean(DRAWER_INTRO, true).apply();
        }
        updateTabs();
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return;
        } else if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
            super.onBackPressed();
            return;
        } else {
            Toast.makeText(getBaseContext(), "Press back again to exit!"
                    , Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    @Override
    public void onRecentFragmentInteraction(TopicSummary topicSummary) {
        Intent i = new Intent(MainActivity.this, TopicActivity.class);
        i.putExtra(BUNDLE_TOPIC_URL, topicSummary.getTopicUrl());
        i.putExtra(BUNDLE_TOPIC_TITLE, topicSummary.getSubject());
        startActivity(i);
    }

    @Override
    public void onForumFragmentInteraction(Board board) {
        Intent i = new Intent(MainActivity.this, BoardActivity.class);
        i.putExtra(BUNDLE_BOARD_URL, board.getUrl());
        i.putExtra(BUNDLE_BOARD_TITLE, board.getTitle());
        startActivity(i);
    }

    @Override
    public void onUnreadFragmentInteraction(TopicSummary topicSummary) {
        if (topicSummary.getTopicUrl() != null) {
            Intent i = new Intent(MainActivity.this, TopicActivity.class);
            i.putExtra(BUNDLE_TOPIC_URL, topicSummary.getTopicUrl());
            i.putExtra(BUNDLE_TOPIC_TITLE, topicSummary.getSubject());
            startActivity(i);
        } else
            Timber.e("onUnreadFragmentInteraction TopicSummary came without a link");
    }

//---------------------------------FragmentPagerAdapter---------------------------------------------

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages. If it becomes too memory intensive,
     * it may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
            notifyDataSetChanged();
        }

        void removeFragment(int position) {
            fragmentList.remove(position);
            fragmentTitleList.remove(position);
            notifyDataSetChanged();
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(Object object) {
            @SuppressWarnings("RedundantCast")
            int position = fragmentList.indexOf((Fragment) object);
            return position == -1 ? POSITION_NONE : position;
        }
    }

    public void updateTabs() {
        if (!sessionManager.isLoggedIn() && sectionsPagerAdapter.getCount() == 3)
            sectionsPagerAdapter.removeFragment(2);
        else if (sessionManager.isLoggedIn() && sectionsPagerAdapter.getCount() == 2)
            sectionsPagerAdapter.addFragment(UnreadFragment.newInstance(3), "UNREAD");
    }
//-------------------------------FragmentPagerAdapter END-------------------------------------------

    private void redirectToActivityFromIntent(Intent intent) {
        if (intent != null) {
            Uri uri = intent.getData();
            if (uri != null) {
                ThmmyPage.PageCategory page = ThmmyPage.resolvePageCategory(uri);
                if (!page.is(ThmmyPage.PageCategory.NOT_THMMY)) {
                    if (page.is(ThmmyPage.PageCategory.BOARD)) {
                        Intent redirectIntent = new Intent(MainActivity.this, BoardActivity.class);
                        redirectIntent.putExtra(BUNDLE_BOARD_URL, uri.toString());
                        redirectIntent.putExtra(BUNDLE_BOARD_TITLE, "");
                        startActivity(redirectIntent);
                    } else if (page.is(ThmmyPage.PageCategory.TOPIC)) {
                        Intent redirectIntent = new Intent(MainActivity.this, TopicActivity.class);
                        redirectIntent.putExtra(BUNDLE_TOPIC_URL, uri.toString());
                        redirectIntent.putExtra(BUNDLE_TOPIC_TITLE, "");
                        startActivity(redirectIntent);
                    } else if (page.is(ThmmyPage.PageCategory.PROFILE)) {
                        Intent redirectIntent = new Intent(MainActivity.this, ProfileActivity.class);
                        redirectIntent.putExtra(BUNDLE_PROFILE_URL, uri.toString());
                        redirectIntent.putExtra(BUNDLE_PROFILE_THUMBNAIL_URL, "");
                        redirectIntent.putExtra(BUNDLE_PROFILE_USERNAME, "");
                        startActivity(redirectIntent);
                    } else if (page.is(ThmmyPage.PageCategory.DOWNLOADS)) {
                        Intent redirectIntent = new Intent(MainActivity.this, DownloadsActivity.class);
                        redirectIntent.putExtra(BUNDLE_DOWNLOADS_URL, uri.toString());
                        redirectIntent.putExtra(BUNDLE_DOWNLOADS_TITLE, "");
                        startActivity(redirectIntent);
                    } else if (!page.is(ThmmyPage.PageCategory.INDEX)) {
                        Toast.makeText(this, "This thmmy sector is not yet supported.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, "This is not thmmy.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}