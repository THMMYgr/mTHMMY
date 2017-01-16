package gr.thmmy.mthmmy.activities.main;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.base.BaseActivity;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.main.forum.ForumFragment;
import gr.thmmy.mthmmy.activities.main.recent.RecentFragment;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.model.Board;
import gr.thmmy.mthmmy.model.TopicSummary;

import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

public class MainActivity extends BaseActivity implements RecentFragment.RecentFragmentInteractionListener, ForumFragment.ForumFragmentInteractionListener {

    //----------------------------------------CLASS VARIABLES-----------------------------------------
    private static final String TAG = "MainActivity";
    private static final int TIME_INTERVAL = 2000;
    private long mBackPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (sessionManager.isLoginScreenDefault()) {
            //Go to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }

        //Initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initialize drawer
        createDrawer();

        //Create the adapter that will return a fragment for each section of the activity
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    protected void onResume() {
        drawer.setSelection(HOME_ID);
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        if(drawer.isDrawerOpen()){
            drawer.closeDrawer();
            return;
        }
        else if (mBackPressed + TIME_INTERVAL > System.currentTimeMillis()) {
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

//---------------------------------FragmentPagerAdapter---------------------------------------------

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages. If it becomes too memory intensive,
     * it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch(position) {
                case 0: return RecentFragment.newInstance(position +1);
                case 1: return ForumFragment.newInstance(position +1);
                default: return RecentFragment.newInstance(position +1); //temp (?)
            }
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "RECENT POSTS";
                case 1:
                    return "FORUM";
            }

            return null;
        }
    }
//-------------------------------FragmentPagerAdapter END-------------------------------------------

}