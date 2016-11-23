package gr.thmmy.mthmmy.activities;

import android.app.ProgressDialog;
import android.content.Intent;
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

import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.data.TopicSummary;
import gr.thmmy.mthmmy.sections.recent.RecentFragment;

import static gr.thmmy.mthmmy.activities.BaseActivity.Thmmy.logout;

public class MainActivity extends BaseActivity implements RecentFragment.OnListFragmentInteractionListener {

//----------------------------------------CLASS VARIABLES-----------------------------------------
    private static final String TAG = "MainActivity";
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (_prefs.getInt(LOG_STATUS, OTHER_ERROR) != LOGGED_IN) { //If not logged in
            //Go to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }

        //Initialize toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Create the adapter that will return a fragment for each section of the activity
        SectionsPagerAdapter mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        //Set up the ViewPager with the sections adapter.
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
        //Inflate the menu; this adds items to the action bar if it is present.
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_main, menu);

        if (_prefs.getInt(LOG_STATUS, OTHER_ERROR) == LOGGED_IN
                && !Objects.equals(_prefs.getString(USER_NAME, null), GUEST_PREF_USERNAME)) {
            //Will enter when logged out or if user is guest
            hideLogin();
        } else
            //Will enter when logged in
            hideLogout();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_about) {
            //Go to about
            Intent i = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(i);
            return true;
        } else if (id == R.id.action_logout)
            //Attempt logout
            new LogoutTask().execute();
        else {
            //Go to login
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
        }

        return super.onOptionsItemSelected(item);
    }

    private void hideLogin() { //Hide login AND show logout
        MenuItem login = menu.findItem(R.id.action_login);
        MenuItem logout = menu.findItem(R.id.action_logout);
        login.setVisible(false);
        logout.setVisible(true);
    }

    private void hideLogout() { //Hide logout AND show login
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
//-------------------------------FragmentPagerAdapter END-------------------------------------------

//-------------------------------------------LOGOUT-------------------------------------------------
    private class LogoutTask extends AsyncTask<Void, Void, Integer> { //Attempt logout
        ProgressDialog progressDialog;

        protected Integer doInBackground(Void... voids) {
            return logout();
        }

        protected void onPreExecute() { //Show a progress dialog until done
            progressDialog = new ProgressDialog(MainActivity.this,
                    R.style.AppTheme);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging out...");
            progressDialog.show();
        }

        protected void onPostExecute(Integer result) { //Handle attempt result
            progressDialog.dismiss(); //Hide progress dialog
            if (result == LOGGED_OUT) { //Successful logout
                /*
                    At this point result is LOGGED_OUT
                    BUT pref's LOGIN_STATUS variable is LOGGED_IN!!
                    and USER_NAME is GUEST
                */
                Toast.makeText(getBaseContext(), "Logged out successfully!", Toast.LENGTH_LONG).show();
                hideLogout();
            } else //Logout failed
                hideLogin();
        }
    }
//-----------------------------------------LOGOUT END-----------------------------------------------
}