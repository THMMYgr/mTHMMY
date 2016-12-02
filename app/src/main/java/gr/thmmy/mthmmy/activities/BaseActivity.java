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
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import gr.thmmy.mthmmy.R;
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

    public static OkHttpClient getClient()
    {
        return client;
    }

    //TODO: move stuff below
    //------------------------------------------DRAWER STUFF----------------------------------------

    private static final int LOGINLOGOUT_ID=0;
    private static final int ABOUT_ID=1;

    protected PrimaryDrawerItem loginLogout, about;
    protected IconicsDrawable loginIcon,logoutIcon, aboutIcon;
    /**
     * Call only after initializing Toolbar
     */
    protected void createDrawer()//TODO
    {
        loginIcon =new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_sign_in)
                .color(Color.BLACK)
                .sizeDp(24);
        logoutIcon =new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_sign_out)
                .color(Color.BLACK)
                .sizeDp(24);
        loginLogout = new PrimaryDrawerItem().withIdentifier(LOGINLOGOUT_ID).withName(R.string.logout).withIcon(logoutIcon);
        aboutIcon =new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_info_circle)
                .color(Color.BLACK)
                .sizeDp(24);
        about = new PrimaryDrawerItem().withIdentifier(ABOUT_ID).withName(R.string.about).withIcon(aboutIcon);
        drawer = new DrawerBuilder().withActivity(BaseActivity.this)
                .withToolbar(toolbar)
                .addDrawerItems(loginLogout,about)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if(drawerItem.equals(LOGINLOGOUT_ID))
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
                            Intent i = new Intent(BaseActivity.this, AboutActivity.class);
                            startActivity(i);
                        }
                        drawer.closeDrawer();
                        return true;
                    }
                })
                .build();
        drawer.setSelection(-1);

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
            {
                //Swap logout with login
                loginLogout.withName(R.string.login).withIcon(loginIcon);
                drawer.updateItem(loginLogout);
            }
            else
            {
                //Swap login with logout
                loginLogout.withName(R.string.logout).withIcon(logoutIcon);
                drawer.updateItem(loginLogout);
            }
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
