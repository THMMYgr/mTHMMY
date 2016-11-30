package gr.thmmy.mthmmy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.data.TopicSummary;
import gr.thmmy.mthmmy.sections.forum.ForumFragment;
import gr.thmmy.mthmmy.sections.recent.RecentFragment;

import static gr.thmmy.mthmmy.session.SessionManager.LOGGED_OUT;

public class MainActivity extends BaseActivity implements RecentFragment.OnListFragmentInteractionListener, ForumFragment.OnListFragmentInteractionListener {

    //----------------------------------------CLASS VARIABLES-----------------------------------------
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (sessionManager.getLogStatus()== LOGGED_OUT) { //If not logged in
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
        super.onResume();
        updateDrawer();
    }





    @Override
    public void onFragmentInteraction(TopicSummary topicSummary) {
        Intent i = new Intent(MainActivity.this, TopicActivity.class);
        i.putExtra("TOPIC_URL", topicSummary.getTopicUrl());
        i.putExtra("TOPIC_TITLE", topicSummary.getTitle());
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