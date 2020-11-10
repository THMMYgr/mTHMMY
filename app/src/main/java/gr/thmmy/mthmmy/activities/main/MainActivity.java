package gr.thmmy.mthmmy.activities.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.downloads.DownloadsActivity;
import gr.thmmy.mthmmy.activities.main.forum.ForumFragment;
import gr.thmmy.mthmmy.activities.main.recent.RecentFragment;
import gr.thmmy.mthmmy.activities.main.unread.UnreadFragment;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.activities.settings.SettingsActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
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
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ImageView drawerButton;

    //Fix for vector drawables on android <21
    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Intent intentFilter = getIntent();
        redirectToActivityFromIntent(intentFilter);
        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.app_preferences_user, false);

        if (sessionManager.isLoginScreenDefault()) {
            //Go to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.putExtra("REDIRECT", true);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
            return; //Avoid executing the code below
        }

        //Initialize drawer
        createDrawer();

        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.container);
        drawerButton = findViewById(R.id.main_activity_open_drawer_btn);

        //Create the adapter that will return a fragment for each section of the activity
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.addFragment(RecentFragment.newInstance(1), "RECENT");
        sectionsPagerAdapter.addFragment(ForumFragment.newInstance(2), "FORUM");
        if (sessionManager.isLoggedIn())
            sectionsPagerAdapter.addFragment(UnreadFragment.newInstance(3), "UNREAD");

        //Set up the ViewPager with the sections adapter.
        viewPager.setAdapter(sectionsPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        int preferredTab = Integer.parseInt(sharedPrefs.getString(SettingsActivity.DEFAULT_HOME_TAB, "0"));
        if ((preferredTab != 3 && preferredTab != 4) || sessionManager.isLoggedIn())
            tabLayout.getTabAt(preferredTab).select();

        for (int i = 0; i < tabLayout.getTabCount(); i++)
            updateTabIcon(i);

        setMainActivity(this);
        setDrawerButtonListener();
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
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "Press back again to exit!"
                    , Toast.LENGTH_SHORT).show();
        }
        mBackPressed = System.currentTimeMillis();
    }

    @Override
    public void onRecentFragmentInteraction(TopicSummary topicSummary) {
        Intent i = new Intent(MainActivity.this, TopicActivity.class);
        i.putExtra(BUNDLE_TOPIC_URL, topicSummary.getTopicUrl());
        i.putExtra(BUNDLE_TOPIC_TITLE, topicSummary.getSubject());
        i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
            updateTabIcon(fragmentList.size() - 1);
        }

        void removeFragment(int position) {
            getSupportFragmentManager().beginTransaction().remove(fragmentList.get(position)).commit();
            fragmentList.remove(position);
            fragmentTitleList.remove(position);
            notifyDataSetChanged();
            if (viewPager.getCurrentItem() == position)
                viewPager.setCurrentItem(position - 1);
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

    public void updateTabIcon(int position) {
        if (position >= tabLayout.getTabCount()) return;
        if (position == 0)
            tabLayout.getTabAt(0).setIcon(getResources().getDrawable(R.drawable.ic_access_time_white_24dp));
        else if (position == 1)
            tabLayout.getTabAt(1).setIcon(getResources().getDrawable(R.drawable.ic_forum_white_24dp));
        else if (position == 2)
            tabLayout.getTabAt(2).setIcon(getResources().getDrawable(R.drawable.ic_fiber_new_white_24dp));
    }


    public void updateTabs() {
        if (!sessionManager.isLoggedIn() && sectionsPagerAdapter.getCount() == 3)
            sectionsPagerAdapter.removeFragment(2);
        else if (sessionManager.isLoggedIn() && sectionsPagerAdapter.getCount() == 2)
            sectionsPagerAdapter.addFragment(UnreadFragment.newInstance(3), "UNREAD");

        for (int i = 0; i < tabLayout.getTabCount(); i++)
            updateTabIcon(i);
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
                        Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "This thmmy sector is not yet supported.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "This is not thmmy.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    private void setDrawerButtonListener()
    {
        this.drawerButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                drawer.openDrawer();
            }
        });
    }
}