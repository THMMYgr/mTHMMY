package gr.thmmy.mthmmy.base;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.AboutActivity;
import gr.thmmy.mthmmy.activities.BookmarkActivity;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.downloads.DownloadsActivity;
import gr.thmmy.mthmmy.activities.main.MainActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.model.Bookmark;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.ObjectSerializer;
import okhttp3.OkHttpClient;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_TITLE;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_USERNAME;

public abstract class BaseActivity extends AppCompatActivity {
    // Client & Cookies
    protected static OkHttpClient client;

    //SessionManager
    protected static SessionManager sessionManager;

    //Bookmarks
    private static final String BOOKMARKS_SHARED_PREFS = "bookmarksSharedPrefs";
    private static final String BOOKMARKED_TOPICS_KEY = "bookmarkedTopicsKey";
    private static final String BOOKMARKED_BOARDS_KEY = "bookmarkedBoardsKey";
    protected static SharedPreferences bookmarksFile;
    protected static ArrayList<Bookmark> topicsBookmarked;
    protected static ArrayList<Bookmark> boardsBookmarked;
    protected static Drawable bookmarked;
    protected static Drawable notBookmarked;

    //Common UI elements
    protected Toolbar toolbar;
    protected Drawer drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (client == null)
            client = BaseApplication.getInstance().getClient(); //must check every time - e.g.
        // they become null when app restarts after crash
        if (sessionManager == null)
            sessionManager = BaseApplication.getInstance().getSessionManager();

        if (sessionManager.isLoggedIn()) {
            if (bookmarked == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    bookmarked = getResources().getDrawable(R.drawable.ic_bookmark_true, null);
                } else //noinspection deprecation
                    bookmarked = getResources().getDrawable(R.drawable.ic_bookmark_true);
            }
            if (notBookmarked == null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    notBookmarked = getResources().getDrawable(R.drawable.ic_bookmark_false, null);
                } else //noinspection deprecation
                    notBookmarked = getResources().getDrawable(R.drawable.ic_bookmark_false);
            }
            if (topicsBookmarked == null || boardsBookmarked == null) {
                bookmarksFile = getSharedPreferences(BOOKMARKS_SHARED_PREFS, Context.MODE_PRIVATE);
                loadSavedBookmarks();
            }
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
        if (drawer != null)    //close drawer animation after returning to activity
            drawer.closeDrawer();
    }


    public static OkHttpClient getClient() {
        return client;
    }

    public static SessionManager getSessionManager() {
        return sessionManager;
    }

    //TODO: move stuff below (?)
    //------------------------------------------DRAWER STUFF----------------------------------------
    protected static final int HOME_ID = 0;
    protected static final int DOWNLOADS_ID = 1;
    protected static final int BOOKMARKS_ID = 2;
    protected static final int LOG_ID = 3;
    protected static final int ABOUT_ID = 4;

    private AccountHeader accountHeader;
    private ProfileDrawerItem profileDrawerItem;
    private PrimaryDrawerItem homeItem, downloadsItem, bookmarksItem, loginLogoutItem, aboutItem;
    private IconicsDrawable homeIcon, homeIconSelected, downloadsIcon, downloadsIconSelected,
            bookmarksIcon, bookmarksIconSelected, loginIcon, logoutIcon, aboutIcon,
            aboutIconSelected;

    /**
     * Call only after initializing Toolbar
     */
    protected void createDrawer() {
        final int primaryColor = ContextCompat.getColor(this, R.color.iron);
        final int selectedPrimaryColor = ContextCompat.getColor(this, R.color.primary_dark);
        final int selectedSecondaryColor = ContextCompat.getColor(this, R.color.accent);

        //Drawer Icons
        homeIcon = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_home)
                .color(primaryColor);

        homeIconSelected = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_home)
                .color(selectedSecondaryColor);

        downloadsIcon = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_download)
                .color(primaryColor);

        downloadsIconSelected = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_download)
                .color(selectedSecondaryColor);

        bookmarksIcon = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_bookmark)
                .color(primaryColor);

        bookmarksIconSelected = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_bookmark)
                .color(selectedSecondaryColor);

        loginIcon = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_sign_in)
                .color(primaryColor);

        logoutIcon = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_sign_out)
                .color(primaryColor);

        aboutIcon = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_info_circle)
                .color(primaryColor);

        aboutIconSelected = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_info_circle)
                .color(selectedSecondaryColor);

        //Drawer Items
        homeItem = new PrimaryDrawerItem()
                .withTextColor(primaryColor)
                .withSelectedColor(selectedPrimaryColor)
                .withSelectedTextColor(selectedSecondaryColor)
                .withIdentifier(HOME_ID)
                .withName(R.string.home)
                .withIcon(homeIcon)
                .withSelectedIcon(homeIconSelected);


        if (sessionManager.isLoggedIn()) //When logged in
        {
            loginLogoutItem = new PrimaryDrawerItem()
                    .withTextColor(primaryColor)
                    .withSelectedColor(selectedSecondaryColor)
                    .withIdentifier(LOG_ID)
                    .withName(R.string.logout)
                    .withIcon(logoutIcon)
                    .withSelectable(false);
            downloadsItem = new PrimaryDrawerItem()
                    .withTextColor(primaryColor)
                    .withSelectedColor(selectedPrimaryColor)
                    .withSelectedTextColor(selectedSecondaryColor)
                    .withIdentifier(DOWNLOADS_ID)
                    .withName(R.string.downloads)
                    .withIcon(downloadsIcon)
                    .withSelectedIcon(downloadsIconSelected);
            bookmarksItem = new PrimaryDrawerItem()
                    .withTextColor(primaryColor)
                    .withSelectedColor(selectedPrimaryColor)
                    .withSelectedTextColor(selectedSecondaryColor)
                    .withIdentifier(BOOKMARKS_ID)
                    .withName(R.string.bookmark)
                    .withIcon(bookmarksIcon)
                    .withSelectedIcon(bookmarksIconSelected);
        } else
            loginLogoutItem = new PrimaryDrawerItem()
                    .withTextColor(primaryColor)
                    .withSelectedColor(selectedSecondaryColor)
                    .withIdentifier(LOG_ID).withName(R.string.login)
                    .withIcon(loginIcon)
                    .withSelectable(false);

        aboutItem = new PrimaryDrawerItem()
                .withTextColor(primaryColor)
                .withSelectedColor(selectedPrimaryColor)
                .withSelectedTextColor(selectedSecondaryColor)
                .withIdentifier(ABOUT_ID)
                .withName(R.string.about)
                .withIcon(aboutIcon)
                .withSelectedIcon(aboutIconSelected);

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
                        if (sessionManager.isLoggedIn()) {
                            Intent intent = new Intent(BaseActivity.this, ProfileActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString(BUNDLE_PROFILE_URL, "https://www.thmmy.gr/smf/index.php?action=profile");
                            if (!sessionManager.hasAvatar())
                                extras.putString(BUNDLE_THUMBNAIL_URL, "");
                            else
                                extras.putString(BUNDLE_THUMBNAIL_URL, sessionManager.getAvatarLink());
                            extras.putString(BUNDLE_USERNAME, sessionManager.getUsername());
                            intent.putExtras(extras);
                            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            return false;
                        }
                        return true;

                    }
                })
                .build();

        //Drawer
        DrawerBuilder drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDrawerWidthDp((int) BaseApplication.getInstance().getDpWidth() / 2)
                .withSliderBackgroundColor(ContextCompat.getColor(this, R.color.primary_light))
                .withAccountHeader(accountHeader)
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.equals(HOME_ID)) {
                            if (!(BaseActivity.this instanceof MainActivity)) {
                                Intent i = new Intent(BaseActivity.this, MainActivity.class);
                                startActivity(i);
                            }
                        } else if (drawerItem.equals(DOWNLOADS_ID)) {
                            if (!(BaseActivity.this instanceof DownloadsActivity)) {
                                Intent i = new Intent(BaseActivity.this, DownloadsActivity.class);
                                Bundle extras = new Bundle();
                                extras.putString(BUNDLE_DOWNLOADS_URL, "");
                                extras.putString(BUNDLE_DOWNLOADS_TITLE, null);
                                i.putExtras(extras);
                                startActivity(i);
                            }
                        } else if (drawerItem.equals(BOOKMARKS_ID)) {
                            if (!(BaseActivity.this instanceof BookmarkActivity)) {
                                Intent i = new Intent(BaseActivity.this, BookmarkActivity.class);
                                startActivity(i);
                            }
                        } else if (drawerItem.equals(LOG_ID)) {
                            if (!sessionManager.isLoggedIn()) //When logged out or if user is guest
                            {
                                Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                                overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                            } else
                                new LogoutTask().execute();
                        } else if (drawerItem.equals(ABOUT_ID)) {
                            if (!(BaseActivity.this instanceof AboutActivity)) {
                                Intent i = new Intent(BaseActivity.this, AboutActivity.class);
                                startActivity(i);
                            }

                        }

                        drawer.closeDrawer();
                        return true;
                    }
                });

        if (sessionManager.isLoggedIn())
            drawerBuilder.addDrawerItems(homeItem, downloadsItem, bookmarksItem, loginLogoutItem, aboutItem);
        else
            drawerBuilder.addDrawerItems(homeItem, loginLogoutItem, aboutItem);

        drawer = drawerBuilder.build();

        drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);
        drawer.setOnDrawerNavigationListener(new Drawer.OnDrawerNavigationListener() {
            @Override
            public boolean onNavigationClickListener(View clickedView) {
                onBackPressed();
                return true;
            }
        });
    }

    protected void updateDrawer() {
        if (drawer != null) {
            if (!sessionManager.isLoggedIn()) //When logged out or if user is guest
            {
                drawer.removeItem(DOWNLOADS_ID);
                loginLogoutItem.withName(R.string.login).withIcon(loginIcon); //Swap logout with login
                profileDrawerItem.withName(sessionManager.getUsername()).withIcon(new IconicsDrawable(this)
                        .icon(FontAwesome.Icon.faw_user)
                        .paddingDp(10)
                        .color(ContextCompat.getColor(this, R.color.primary_light))
                        .backgroundColor(ContextCompat.getColor(this, R.color.primary)));
            } else {
                loginLogoutItem.withName(R.string.logout).withIcon(logoutIcon); //Swap login with logout
                profileDrawerItem.withName(sessionManager.getUsername()).withIcon(sessionManager.getAvatarLink());
            }
            accountHeader.updateProfile(profileDrawerItem);
            drawer.updateItem(loginLogoutItem);

        }
    }


//-------------------------------------------LOGOUT-------------------------------------------------

    /**
     * Result toast will always display a success, because when user chooses logout all data are
     * cleared regardless of the actual outcome
     */
    protected class LogoutTask extends AsyncTask<Void, Void, Integer> { //Attempt logout
        ProgressDialog progressDialog;

        protected Integer doInBackground(Void... voids) {
            return sessionManager.logout();
        }

        protected void onPreExecute() { //Show a progress dialog until done
            progressDialog = new ProgressDialog(BaseActivity.this,
                    R.style.AppTheme_Dark_Dialog);
            progressDialog.setCancelable(false);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Logging out...");
            progressDialog.show();
        }

        protected void onPostExecute(Integer result) {
            Toast.makeText(getBaseContext(), "Logged out successfully!", Toast.LENGTH_LONG).show();
            updateDrawer();
            progressDialog.dismiss();
        }
    }
//-----------------------------------------LOGOUT END-----------------------------------------------

//---------------------------------------------BOOKMARKS--------------------------------------------

    protected ArrayList<Bookmark> getBoardsBookmarked() {
        return boardsBookmarked;
    }

    protected ArrayList<Bookmark> getTopicsBookmarked() {
        return topicsBookmarked;
    }

    protected void setTopicBookmark(ImageButton bookmarkView, final Bookmark bookmark) {
        if (matchExists(bookmark, topicsBookmarked)) {
            bookmarkView.setImageDrawable(bookmarked);
        } else {
            bookmarkView.setImageDrawable(notBookmarked);
        }
        bookmarkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (matchExists(bookmark, topicsBookmarked)) {
                    ((ImageButton) view).setImageDrawable(notBookmarked);
                    toggleTopicToBookmarks(bookmark);
                    Toast.makeText(BaseActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                } else {
                    ((ImageButton) view).setImageDrawable(bookmarked);
                    toggleTopicToBookmarks(bookmark);
                    Toast.makeText(BaseActivity.this, "Bookmark added", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    protected void setBoardBookmark(ImageButton bookmarkView, final Bookmark bookmark) {
        if (matchExists(bookmark, boardsBookmarked)) {
            bookmarkView.setImageDrawable(bookmarked);
        } else {
            bookmarkView.setImageDrawable(notBookmarked);
        }
        bookmarkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (matchExists(bookmark, boardsBookmarked)) {
                    ((ImageButton) view).setImageDrawable(notBookmarked);
                    toggleBoardToBookmarks(bookmark);
                    Toast.makeText(BaseActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                } else {
                    ((ImageButton) view).setImageDrawable(bookmarked);
                    toggleBoardToBookmarks(bookmark);
                    Toast.makeText(BaseActivity.this, "Bookmark added", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadSavedBookmarks() {
        String tmpString = bookmarksFile.getString(BOOKMARKED_TOPICS_KEY, null);
        if (tmpString != null)
            try {
                topicsBookmarked = (ArrayList<Bookmark>) ObjectSerializer.deserialize(tmpString);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        else {
            topicsBookmarked = new ArrayList<>();
            topicsBookmarked.add(null);
        }

        tmpString = bookmarksFile.getString(BOOKMARKED_BOARDS_KEY, null);
        if (tmpString != null)
            try {
                boardsBookmarked = (ArrayList<Bookmark>) ObjectSerializer.deserialize(tmpString);
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        else {
            boardsBookmarked = new ArrayList<>();
            boardsBookmarked.add(null);
        }
    }

    private void toggleBoardToBookmarks(Bookmark bookmark) {
        if (boardsBookmarked == null) return;
        if (matchExists(bookmark, boardsBookmarked)) {
            if (boardsBookmarked.size() == 1) boardsBookmarked.set(0, null);
            else boardsBookmarked.remove(findIndex(bookmark, boardsBookmarked));
        } else boardsBookmarked.add(bookmark);
        updateBoardBookmarks();
    }

    private void toggleTopicToBookmarks(Bookmark bookmark) {
        if (topicsBookmarked == null) return;
        if (matchExists(bookmark, topicsBookmarked))
            topicsBookmarked.remove(findIndex(bookmark, topicsBookmarked));
        else topicsBookmarked.add(bookmark);
        updateTopicBookmarks();
    }

    private void updateBoardBookmarks() {
        String tmpString = null;
        if (!(boardsBookmarked.size() == 1 && boardsBookmarked.get(0) == null)) {
            try {
                tmpString = ObjectSerializer.serialize(boardsBookmarked);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences.Editor editor = bookmarksFile.edit();
        editor.putString(BOOKMARKED_BOARDS_KEY, tmpString).apply();
    }

    private void updateTopicBookmarks() {
        String tmpString = null;
        if (!(topicsBookmarked.size() == 1 && topicsBookmarked.get(0) == null)) {
            try {
                tmpString = ObjectSerializer.serialize(topicsBookmarked);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        SharedPreferences.Editor editor = bookmarksFile.edit();
        editor.putString(BOOKMARKED_TOPICS_KEY, tmpString).apply();
    }

    private boolean matchExists(Bookmark bookmark, ArrayList<Bookmark> array) {
        if (!array.isEmpty()) {
            for (Bookmark b : array) {
                if (b != null) {
                    return Objects.equals(b.getId(), bookmark.getId())
                            && Objects.equals(b.getTitle(), bookmark.getTitle());
                }
            }
        }
        return false;
    }

    private int findIndex(Bookmark bookmark, ArrayList<Bookmark> array) {
        if (array.size() == 1 && array.get(0) == null) return -1;
        if (!array.isEmpty()) {
            for (int i = 0; i < array.size(); ++i) {
                if (array.get(i) != null && Objects.equals(array.get(i).getId(), bookmark.getId())
                        && Objects.equals(array.get(i).getTitle(), bookmark.getTitle()))
                    return i;
            }
        }
        return -1;
    }
//-------------------------------------------BOOKMARKS END------------------------------------------
}
