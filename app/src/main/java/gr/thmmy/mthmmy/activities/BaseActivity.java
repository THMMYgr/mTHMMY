package gr.thmmy.mthmmy.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
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
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

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
            //initialize and create the image loader logic
            DrawerImageLoader.init(new AbstractDrawerImageLoader() {
                @Override
                public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                    Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
                }
                @Override
                public void cancel(ImageView imageView) {
                    Picasso.with(imageView.getContext()).cancelRequest(imageView);
                }

                @Override
                public Drawable placeholder(Context ctx, String tag) {
                    if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                        return new IconicsDrawable(ctx, FontAwesome.Icon.faw_user_circle);
                    }

                    return super.placeholder(ctx, tag);
                }


            });
            init = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(drawer!=null)    //close drawer animation after returning to activity
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
        if (sessionManager.getLogStatus()!= LOGGED_IN) //When logged out or if user is guest
            loginLogoutItem = new PrimaryDrawerItem().withIdentifier(LOG_ID).withName(R.string.login).withIcon(loginIcon).withSelectable(false);
        else
            loginLogoutItem = new PrimaryDrawerItem().withIdentifier(LOG_ID).withName(R.string.logout).withIcon(logoutIcon).withSelectable(false);
        aboutItem = new PrimaryDrawerItem().withIdentifier(ABOUT_ID).withName(R.string.about).withIcon(aboutIcon);

        //Profile
        profileDrawerItem = new ProfileDrawerItem().withName(sessionManager.getUsername());

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
                        return true;    //don't close drawer (for now)
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
            {
                loginLogoutItem.withName(R.string.login).withIcon(loginIcon); //Swap logout with login
                profileDrawerItem.withName(sessionManager.getUsername()).withIcon(FontAwesome.Icon.faw_user_circle);
            }
            else
            {
                loginLogoutItem.withName(R.string.logout).withIcon(logoutIcon); //Swap login with logout
                profileDrawerItem.withName(sessionManager.getUsername()).withIcon(sessionManager.getAvatarLink());
            }
            accountHeader.updateProfile(profileDrawerItem);
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
