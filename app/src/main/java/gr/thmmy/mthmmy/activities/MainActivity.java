package gr.thmmy.mthmmy.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.data.TopicSummary;
import gr.thmmy.mthmmy.sections.recent.RecentFragment;
import gr.thmmy.mthmmy.utils.Thmmy;

public class MainActivity extends BaseActivity implements RecentFragment.OnListFragmentInteractionListener {
    /**
     * The {@link ViewPager} that will host the section contents.
     */

    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        if (!prefs.getBoolean(IS_LOGGED_IN, false)) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each section of the activity
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        ViewPager mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        //TODO: Drawer
//        new DrawerBuilder().withActivity(this)
//                .withToolbar(toolbar)
//                .build();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);
        SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        if (prefs.getBoolean(IS_LOGGED_IN, false)
                && prefs.getString(USER_NAME, null) != GUEST_PREF_USERNAME)
            hideLogin();
        else
            hideLogout();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            Intent i = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_logout)
            new LogoutTask().execute();
        else {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideLogin() {
        MenuItem login = menu.findItem(R.id.action_login);
        MenuItem logout = menu.findItem(R.id.action_logout);
        login.setVisible(false);
        logout.setVisible(true);
    }

    private void hideLogout() {
        MenuItem login = menu.findItem(R.id.action_login);
        MenuItem logout = menu.findItem(R.id.action_logout);
        login.setVisible(true);
        logout.setVisible(false);
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
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return RecentFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 1 total pages.
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

    private class LogoutTask extends AsyncTask<Void, Void, Integer> {
        protected Integer doInBackground(Void... voids) {
            return Thmmy.logout(loginData);
        }

        protected void onPreExecute() {
            //TODO: a progressbar maybe?
        }

        protected void onPostExecute(Integer result) {
            if (result == Thmmy.LOGGED_OUT) {
                SharedPreferences.Editor editor = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE).edit();
                editor.putString(USER_NAME, null);
                editor.putBoolean(IS_LOGGED_IN, false);
                editor.apply();
                Toast.makeText(getBaseContext(), "Logged out successfully!", Toast.LENGTH_LONG).show();

                hideLogout();
            } else
                hideLogin();
        }
    }
}
