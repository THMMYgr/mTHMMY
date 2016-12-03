package gr.thmmy.mthmmy.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.main.MainActivity;
import gr.thmmy.mthmmy.session.SessionManager;
import okhttp3.OkHttpClient;

import static gr.thmmy.mthmmy.session.SessionManager.LOGGED_IN;

public class BaseActivity extends AppCompatActivity
{
    // Client & Cookies
    protected static OkHttpClient client;
    private static PersistentCookieJar cookieJar;
    private static SharedPrefsCookiePersistor sharedPrefsCookiePersistor;

    //Shared Preferences
    protected static final String SHARED_PREFS_NAME = "ThmmySharedPrefs";
    protected static SharedPreferences sharedPrefs;

    //SessionManager
    protected static SessionManager sessionManager;

    //Other variables
    private static boolean init = false; //To initialize stuff only once per app start

    //Common UI elements
    protected Toolbar toolbar;
    protected Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!init) {
            sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
            sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(BaseActivity.this);
            cookieJar = new PersistentCookieJar(new SetCookieCache(), sharedPrefsCookiePersistor);
            client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();
            sessionManager = new SessionManager(client, cookieJar, sharedPrefsCookiePersistor, sharedPrefs);
            init = true;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(drawer!=null)    //temporary solution to solve drawer animation closing after returning to activity
            drawer.closeDrawer();
    }

    public static OkHttpClient getClient()
    {
        return client;
    }

    //TODO: move stuff below
    //------------------------------------------DRAWER STUFF----------------------------------------
    protected static final int HOME_ID=0;
    protected static final int LOG_ID =1;
    protected static final int ABOUT_ID=2;

    private AccountHeader accountHeader;
    private ProfileDrawerItem profileDrawerItem;
    private PrimaryDrawerItem homeItem, loginLogoutItem, aboutItem;
    private IconicsDrawable homeIcon,loginIcon,logoutIcon, aboutIcon;

    /**
     * Call only after initializing Toolbar
     */
    protected void createDrawer()//TODO
    {
        //Drawer Icons
        homeIcon =new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_home)
                .color(Color.BLACK);

        loginIcon =new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_sign_in)
                .color(Color.BLACK);

        logoutIcon =new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_sign_out)
                .color(Color.BLACK);

        aboutIcon =new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_info_circle)
                .color(Color.BLACK);

        //Drawer Items
        homeItem = new PrimaryDrawerItem().withIdentifier(HOME_ID).withName(R.string.home).withIcon(homeIcon);
        loginLogoutItem = new PrimaryDrawerItem().withIdentifier(LOG_ID).withName(R.string.logout).withIcon(logoutIcon);
        aboutItem = new PrimaryDrawerItem().withIdentifier(ABOUT_ID).withName(R.string.about).withIcon(aboutIcon);

        //Profile
        profileDrawerItem = new ProfileDrawerItem().withName(sessionManager.getUsername()); //TODO: set profile picture

        //AccountHeader
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withSelectionListEnabledForSingleProfile(false)
                .withHeaderBackground(R.color.primary)
                .addProfiles(profileDrawerItem)
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        //TODO: display profile stuff
                        return false;
                    }
                })
                .build();

        //Drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(accountHeader)
                .addDrawerItems(homeItem,loginLogoutItem,aboutItem)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if(drawerItem.equals(HOME_ID))
                        {
                            if(!(BaseActivity.this instanceof MainActivity))
                            {
                                Intent i = new Intent(BaseActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        }
                        else if(drawerItem.equals(LOG_ID))
                        {
                            if (sessionManager.getLogStatus()!= LOGGED_IN) //When logged out or if user is guest
                            {
                                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                            }
                            else
                                new LogoutTask().execute();
                        }
                        else if(drawerItem.equals(ABOUT_ID))
                        {
                            if(!(BaseActivity.this instanceof AboutActivity))
                            {
                                Intent i = new Intent(BaseActivity.this, AboutActivity.class);
                                startActivity(i);
                            }

                        }
                        else
                            drawer.closeDrawer();
                        return true;
                    }
                })
                .build();

        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        drawer.setOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
            @Override
            public boolean onNavigationClickListener(View clickedView) {
                onBackPressed();
                return true;
            }
        });
    }

    protected void updateDrawer()
    {
        if(drawer!=null)
        {
            if (sessionManager.getLogStatus()!= LOGGED_IN) //When logged out or if user is guest
                loginLogoutItem.withName(R.string.login).withIcon(loginIcon); //Swap logout with login
            else
                loginLogoutItem.withName(R.string.logout).withIcon(logoutIcon); //Swap login with logout

            ProfileDrawerItem p = new ProfileDrawerItem().withName(sessionManager.getUsername()); //TODO: set profile picture
            accountHeader.updateProfile(p);
            drawer.updateItem(loginLogoutItem);

        }
    }


    //-------------------------------------------LOGOUT-------------------------------------------------
    /**
     *  Result toast will always display a success, because when user chooses logout all data are
     *  cleared regardless of the actual outcome
     */
    protected class LogoutTask extends AsyncTask<Void, Void, Integer> { //Attempt logout
        ProgressDialog progressDialog;

        protected Integer doInBackground(Void... voids) {
            return sessionManager.logout();
        }

        protected void onPreExecute()
        { //Show a progress dialog until done
            progressDialog = new ProgressDialog(BaseActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging out...");
            progressDialog.show();
        }

        protected void onPostExecute(Integer result)
        {
            Toast.makeText(getBaseContext(), "Logged out successfully!", Toast.LENGTH_LONG).show();
            updateDrawer();
            progressDialog.dismiss();
        }
    }
//-----------------------------------------LOGOUT END-----------------------------------------------


}
