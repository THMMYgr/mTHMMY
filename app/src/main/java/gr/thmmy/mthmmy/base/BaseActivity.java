package gr.thmmy.mthmmy.base;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.AboutActivity;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.bookmarks.BookmarkActivity;
import gr.thmmy.mthmmy.activities.downloads.DownloadsActivity;
import gr.thmmy.mthmmy.activities.main.MainActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.activities.settings.SettingsActivity;
import gr.thmmy.mthmmy.activities.upload.UploadActivity;
import gr.thmmy.mthmmy.model.Bookmark;
import gr.thmmy.mthmmy.model.ThmmyFile;
import gr.thmmy.mthmmy.services.DownloadHelper;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.FileUtils;
import gr.thmmy.mthmmy.utils.LaunchType;
import gr.thmmy.mthmmy.viewmodel.BaseViewModel;
import okhttp3.OkHttpClient;
import ru.noties.markwon.LinkResolverDef;
import ru.noties.markwon.Markwon;
import ru.noties.markwon.SpannableConfiguration;
import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_TITLE;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;
import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.DEFAULT_HOME_TAB;
import static gr.thmmy.mthmmy.services.DownloadHelper.SAVE_DIR;
import static gr.thmmy.mthmmy.session.SessionManager.SUCCESS;
import static gr.thmmy.mthmmy.utils.FileUtils.getMimeType;
import static gr.thmmy.mthmmy.utils.LaunchType.LAUNCH_TYPE.FIRST_LAUNCH_EVER;
import static gr.thmmy.mthmmy.utils.LaunchType.LAUNCH_TYPE.INDETERMINATE;

public abstract class BaseActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{
    // Client & Cookies
    protected static OkHttpClient client;

    //SessionManager
    protected static SessionManager sessionManager;

    //Bookmarks
    private static final String BOOKMARKS_SHARED_PREFS = "bookmarksSharedPrefs";
    private static final String BOOKMARKED_TOPICS_KEY = "bookmarkedTopicsKey";
    private static final String BOOKMARKED_BOARDS_KEY = "bookmarkedBoardsKey";
    protected Bookmark thisPageBookmark;
    private MenuItem thisPageBookmarkMenuButton;
    private SharedPreferences bookmarksFile;
    private ArrayList<Bookmark> topicsBookmarked;
    private ArrayList<Bookmark> boardsBookmarked;

    //Common UI elements
    protected Toolbar toolbar;
    protected Drawer drawer;

    private MainActivity mainActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (client == null)
            client = BaseApplication.getInstance().getClient(); //must check every time - e.g.

        // they become null when app restarts after crash
        if (sessionManager == null)
            sessionManager = BaseApplication.getInstance().getSessionManager();

        if (topicsBookmarked == null || boardsBookmarked == null) {
            bookmarksFile = getSharedPreferences(BOOKMARKS_SHARED_PREFS, Context.MODE_PRIVATE);
            loadSavedBookmarks();
        }

        BaseViewModel baseViewModel = ViewModelProviders.of(this).get(BaseViewModel.class);
        baseViewModel.getCurrentPageBookmark().observe(this, thisPageBookmark -> {
            setTopicBookmark(thisPageBookmarkMenuButton);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDrawer();
        if(LaunchType.getLaunchType()==FIRST_LAUNCH_EVER||LaunchType.getLaunchType()== INDETERMINATE)
            showUserConsentDialog();
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
    protected static final int UPLOAD_ID = 2;
    protected static final int BOOKMARKS_ID = 3;
    protected static final int LOG_ID = 4;
    protected static final int ABOUT_ID = 5;
    protected static final int SETTINGS_ID = 6;

    private AccountHeader accountHeader;
    private ProfileDrawerItem profileDrawerItem;
    private PrimaryDrawerItem downloadsItem, uploadItem, loginLogoutItem;
    private IconicsDrawable loginIcon, logoutIcon;

    /**
     * Call only after initializing Toolbar
     */
    protected void createDrawer() {
        final int primaryColor = ContextCompat.getColor(this, R.color.iron);
        final int selectedPrimaryColor = ContextCompat.getColor(this, R.color.primary_dark);
        final int selectedSecondaryColor = ContextCompat.getColor(this, R.color.accent);

        PrimaryDrawerItem homeItem, bookmarksItem, settingsItem, aboutItem;
        IconicsDrawable homeIcon, homeIconSelected, downloadsIcon, downloadsIconSelected, uploadIcon, uploadIconSelected, settingsIcon,
                settingsIconSelected, bookmarksIcon, bookmarksIconSelected, aboutIcon, aboutIconSelected;

        //Drawer Icons
        homeIcon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_home)
                .color(primaryColor);

        homeIconSelected = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_home)
                .color(selectedSecondaryColor);

        bookmarksIcon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_bookmark)
                .color(primaryColor);

        bookmarksIconSelected = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_bookmark)
                .color(selectedSecondaryColor);

        downloadsIcon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_file_download)
                .color(primaryColor);

        downloadsIconSelected = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_file_download)
                .color(selectedSecondaryColor);

        uploadIcon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_file_upload)
                .color(primaryColor);

        uploadIconSelected = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_file_upload)
                .color(selectedSecondaryColor);

        settingsIcon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_settings)
                .color(primaryColor);

        settingsIconSelected = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_settings)
                .color(selectedSecondaryColor);

        loginIcon = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_sign_in)
                .color(primaryColor);

        logoutIcon = new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_sign_out)
                .color(primaryColor);

        aboutIcon = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_info)
                .color(primaryColor);

        aboutIconSelected = new IconicsDrawable(this)
                .icon(GoogleMaterial.Icon.gmd_info)
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
            uploadItem = new PrimaryDrawerItem()
                    .withTextColor(primaryColor)
                    .withSelectedColor(selectedPrimaryColor)
                    .withSelectedTextColor(selectedSecondaryColor)
                    .withIdentifier(UPLOAD_ID)
                    .withName(R.string.upload)
                    .withIcon(uploadIcon)
                    .withSelectedIcon(uploadIconSelected);
        } else
            loginLogoutItem = new PrimaryDrawerItem()
                    .withTextColor(primaryColor)
                    .withSelectedColor(selectedSecondaryColor)
                    .withIdentifier(LOG_ID).withName(R.string.login)
                    .withIcon(loginIcon)
                    .withSelectable(false);

        settingsItem = new PrimaryDrawerItem()
                .withTextColor(primaryColor)
                .withSelectedColor(selectedPrimaryColor)
                .withSelectedTextColor(selectedSecondaryColor)
                .withIdentifier(SETTINGS_ID)
                .withName(R.string.settings)
                .withIcon(settingsIcon)
                .withSelectedIcon(settingsIconSelected);

        bookmarksItem = new PrimaryDrawerItem()
                .withTextColor(primaryColor)
                .withSelectedColor(selectedPrimaryColor)
                .withSelectedTextColor(selectedSecondaryColor)
                .withIdentifier(BOOKMARKS_ID)
                .withName(R.string.bookmark)
                .withIcon(bookmarksIcon)
                .withSelectedIcon(bookmarksIconSelected);

        aboutItem = new PrimaryDrawerItem()
                .withTextColor(primaryColor)
                .withSelectedColor(selectedPrimaryColor)
                .withSelectedTextColor(selectedSecondaryColor)
                .withIdentifier(ABOUT_ID)
                .withName(R.string.about)
                .withIcon(aboutIcon)
                .withSelectedIcon(aboutIconSelected);

        //Profile
        profileDrawerItem = new ProfileDrawerItem().withName(sessionManager.getUsername()).withIdentifier(0);

        //AccountHeader
        accountHeader = new AccountHeaderBuilder()
                .withActivity(this)
                .withCompactStyle(true)
                .withSelectionListEnabledForSingleProfile(false)
                .withHeaderBackground(R.color.primary)
                .addProfiles(profileDrawerItem)
                .withOnAccountHeaderListener((view, profile, currentProfile) -> {
                    if (sessionManager.isLoggedIn()) {
                        Intent intent = new Intent(BaseActivity.this, ProfileActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(BUNDLE_PROFILE_URL, "https://www.thmmy.gr/smf/index.php?action=profile");
                        if (!sessionManager.hasAvatar())
                            extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, "");
                        else
                            extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, sessionManager.getAvatarLink());
                        extras.putString(BUNDLE_PROFILE_USERNAME, sessionManager.getUsername());
                        intent.putExtras(extras);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        return false;
                    } else
                        startLoginActivity();
                    return true;

                })
                .build();

        //Drawer
        DrawerBuilder drawerBuilder = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withDrawerWidthDp((int) BaseApplication.getInstance().getDpWidth() / 2)
                .withSliderBackgroundColor(ContextCompat.getColor(this, R.color.primary_light))
                .withAccountHeader(accountHeader)
                .withOnDrawerItemClickListener((view, position, drawerItem) -> {
                    if (drawerItem.equals(HOME_ID)) {
                        if (!(BaseActivity.this instanceof MainActivity)) {
                            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
                            startActivity(intent);
                        }
                    } else if (drawerItem.equals(DOWNLOADS_ID)) {
                        if (!(BaseActivity.this instanceof DownloadsActivity)) {
                            Intent intent = new Intent(BaseActivity.this, DownloadsActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString(BUNDLE_DOWNLOADS_URL, "");
                            extras.putString(BUNDLE_DOWNLOADS_TITLE, null);
                            intent.putExtras(extras);
                            startActivity(intent);
                        }
                    } else if (drawerItem.equals(UPLOAD_ID)) {
                        if (!(BaseActivity.this instanceof UploadActivity)) {
                            Intent intent = new Intent(BaseActivity.this, UploadActivity.class);
                            startActivity(intent);
                        }
                    } else if (drawerItem.equals(BOOKMARKS_ID)) {
                        if (!(BaseActivity.this instanceof BookmarkActivity)) {
                            Intent intent = new Intent(BaseActivity.this, BookmarkActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                    } else if (drawerItem.equals(LOG_ID)) {
                        if (!sessionManager.isLoggedIn()) //When logged out or if user is guest
                            startLoginActivity();
                        else
                            new LogoutTask().execute();
                    } else if (drawerItem.equals(ABOUT_ID)) {
                        if (!(BaseActivity.this instanceof AboutActivity)) {
                            Intent intent = new Intent(BaseActivity.this, AboutActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                    } else if (drawerItem.equals(SETTINGS_ID)) {
                        if (!(BaseActivity.this instanceof SettingsActivity)) {
                            Intent intent = new Intent(BaseActivity.this, SettingsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent);
                        }
                    }

                    drawer.closeDrawer();
                    return true;
                });

        if (sessionManager.isLoggedIn())
            drawerBuilder.addDrawerItems(homeItem, bookmarksItem, downloadsItem, uploadItem, settingsItem, loginLogoutItem, aboutItem);
        else
            drawerBuilder.addDrawerItems(homeItem, bookmarksItem, settingsItem, loginLogoutItem, aboutItem);

        drawer = drawerBuilder.build();

        if (!(BaseActivity.this instanceof MainActivity))
            drawer.getActionBarDrawerToggle().setDrawerIndicatorEnabled(false);

        drawer.setOnDrawerNavigationListener(clickedView -> {
            onBackPressed();
            return true;
        });
    }

    private void updateDrawer() {
        if (drawer != null) {
            if (!sessionManager.isLoggedIn()) //When logged out or if user is guest
            {
                drawer.removeItem(DOWNLOADS_ID);
                drawer.removeItem(UPLOAD_ID);
                loginLogoutItem.withName(R.string.login).withIcon(loginIcon); //Swap logout with login
                profileDrawerItem.withName(sessionManager.getUsername());
                setDefaultAvatar();
            } else {
                if (!drawer.getDrawerItems().contains(downloadsItem)) {
                    drawer.addItemAtPosition(downloadsItem, 2);
                }
                if (!drawer.getDrawerItems().contains(uploadItem)) {
                    drawer.addItemAtPosition(uploadItem, 3);
                }
                loginLogoutItem.withName(R.string.logout).withIcon(logoutIcon); //Swap login with logout
                profileDrawerItem.withName(sessionManager.getUsername());
                if (sessionManager.hasAvatar())
                    profileDrawerItem.withIcon(sessionManager.getAvatarLink());
                else
                    setDefaultAvatar();
            }
            accountHeader.updateProfile(profileDrawerItem);
            drawer.updateItem(loginLogoutItem);

        }
    }

    private void setDefaultAvatar() {
        profileDrawerItem.withIcon(new IconicsDrawable(this)
                .icon(FontAwesome.Icon.faw_user)
                .paddingDp(10)
                .color(ContextCompat.getColor(this, R.color.primary_light))
                .backgroundColor(ContextCompat.getColor(this, R.color.primary)));
    }

//-------------------------------------------LOGOUT-------------------------------------------------

    /**
     * Result toast will always display a success, because when user chooses logout all data are
     * cleared regardless of the actual outcome
     */
    private class LogoutTask extends AsyncTask<Void, Void, Integer> { //Attempt logout
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
            if (result == SUCCESS) {
                SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                if (sharedPrefs.getString(DEFAULT_HOME_TAB, "0").equals("2")) {
                    SharedPreferences.Editor editor = sharedPrefs.edit();
                    editor.putString(DEFAULT_HOME_TAB, "0").apply();
                }
            }

            updateDrawer();
            if (mainActivity != null)
                mainActivity.updateTabs();
            progressDialog.dismiss();
            //if (BaseActivity.this instanceof TopicActivity){
            Intent intent = new Intent(BaseActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            //}
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

    protected void setTopicBookmark(MenuItem thisPageBookmarkMenuButton) {
        this.thisPageBookmarkMenuButton = thisPageBookmarkMenuButton;
        if (thisPageBookmark.matchExists(topicsBookmarked)) {
            thisPageBookmarkMenuButton.setIcon(R.drawable.ic_bookmark_true_accent_24dp);
        } else {
            thisPageBookmarkMenuButton.setIcon(R.drawable.ic_bookmark_false_accent_24dp);
        }
    }

    protected void refreshTopicBookmark() {
        if (thisPageBookmarkMenuButton == null) {
            return;
        }
        loadSavedBookmarks();
        if (thisPageBookmark.matchExists(topicsBookmarked)) {
            thisPageBookmarkMenuButton.setIcon(R.drawable.ic_bookmark_true_accent_24dp);
        } else {
            thisPageBookmarkMenuButton.setIcon(R.drawable.ic_bookmark_false_accent_24dp);
        }
    }

    protected void topicMenuBookmarkClick() {
        if (thisPageBookmark.matchExists(topicsBookmarked)) {
            thisPageBookmarkMenuButton.setIcon(R.drawable.ic_bookmark_false_accent_24dp);
            toggleTopicToBookmarks(thisPageBookmark);
            Toast.makeText(getBaseContext(), "Bookmark removed", Toast.LENGTH_SHORT).show();
        } else {
            thisPageBookmarkMenuButton.setIcon(R.drawable.ic_bookmark_true_accent_24dp);
            toggleTopicToBookmarks(thisPageBookmark);
            Toast.makeText(getBaseContext(), "Bookmark added", Toast.LENGTH_SHORT).show();
        }
    }

    protected void setBoardBookmark(final ImageButton thisPageBookmarkImageButton) {
        if (thisPageBookmark.matchExists(boardsBookmarked)) {
            thisPageBookmarkImageButton.setImageResource(R.drawable.ic_bookmark_true_accent_24dp);
        } else {
            thisPageBookmarkImageButton.setImageResource(R.drawable.ic_bookmark_false_accent_24dp);
        }
        thisPageBookmarkImageButton.setOnClickListener(view -> {
            if (thisPageBookmark.matchExists(boardsBookmarked)) {
                thisPageBookmarkImageButton.setImageResource(R.drawable.ic_bookmark_false_accent_24dp);
                Toast.makeText(getBaseContext(), "Bookmark removed", Toast.LENGTH_SHORT).show();
            } else {
                thisPageBookmarkImageButton.setImageResource(R.drawable.ic_bookmark_true_accent_24dp);
                Toast.makeText(getBaseContext(), "Bookmark added", Toast.LENGTH_SHORT).show();
            }
            toggleBoardToBookmarks(thisPageBookmark);
        });
    }

    protected void refreshBoardBookmark(final ImageButton thisPageBookmarkImageButton) {
        if (thisPageBookmarkImageButton == null)
            return;
        loadSavedBookmarks();
        if (thisPageBookmark.matchExists(boardsBookmarked)) {
            thisPageBookmarkImageButton.setImageResource(R.drawable.ic_bookmark_true_accent_24dp);
        } else {
            thisPageBookmarkImageButton.setImageResource(R.drawable.ic_bookmark_false_accent_24dp);
        }
    }

    private void loadSavedBookmarks() {
        String tmpString = bookmarksFile.getString(BOOKMARKED_TOPICS_KEY, null);
        if (tmpString != null)
            topicsBookmarked = Bookmark.arrayFromString(tmpString);
        else {
            topicsBookmarked = new ArrayList<>();
        }

        tmpString = bookmarksFile.getString(BOOKMARKED_BOARDS_KEY, null);
        if (tmpString != null)
            boardsBookmarked = Bookmark.arrayFromString(tmpString);
        else {
            boardsBookmarked = new ArrayList<>();
        }
    }

    private void toggleBoardToBookmarks(Bookmark bookmark) {
        if (boardsBookmarked == null) return;
        if (bookmark.matchExists(boardsBookmarked)) {
            boardsBookmarked.remove(bookmark.findIndex(boardsBookmarked));
        } else boardsBookmarked.add(new Bookmark(bookmark.getTitle(), bookmark.getId(), false));
        updateBoardBookmarks();
    }

    private void toggleTopicToBookmarks(Bookmark bookmark) {
        if (topicsBookmarked == null) return;
        if (bookmark.matchExists(topicsBookmarked)) {
            topicsBookmarked.remove(bookmark.findIndex(topicsBookmarked));
            FirebaseMessaging.getInstance().unsubscribeFromTopic(bookmark.getId());
        } else {
            topicsBookmarked.add(new Bookmark(bookmark.getTitle(), bookmark.getId(), true));
            FirebaseMessaging.getInstance().subscribeToTopic(bookmark.getId());
        }
        updateTopicBookmarks();
    }

    private void updateBoardBookmarks() {
        String tmpString;
        tmpString = Bookmark.arrayToString(boardsBookmarked);
        SharedPreferences.Editor editor = bookmarksFile.edit();
        editor.putString(BOOKMARKED_BOARDS_KEY, tmpString).apply();
    }

    private void updateTopicBookmarks() {
        String tmpString;
        tmpString = Bookmark.arrayToString(topicsBookmarked);
        SharedPreferences.Editor editor = bookmarksFile.edit();
        editor.putString(BOOKMARKED_TOPICS_KEY, tmpString).apply();
    }

    protected void removeBookmark(Bookmark bookmark) {
        if (bookmark.matchExists(boardsBookmarked)) toggleBoardToBookmarks(bookmark);
        else if (bookmark.matchExists(topicsBookmarked)) toggleTopicToBookmarks(bookmark);
    }

    protected boolean toggleNotification(Bookmark bookmark) {
        if (bookmark.matchExists(topicsBookmarked)) {
            topicsBookmarked.get(bookmark.findIndex(topicsBookmarked)).toggleNotificationsEnabled();
            updateTopicBookmarks();

            if (topicsBookmarked.get(bookmark.findIndex(topicsBookmarked)).isNotificationsEnabled()) {
                FirebaseMessaging.getInstance().subscribeToTopic(bookmark.getId());
            } else {
                FirebaseMessaging.getInstance().unsubscribeFromTopic(bookmark.getId());
            }

            return topicsBookmarked.get(bookmark.findIndex(topicsBookmarked)).isNotificationsEnabled();
        }
        return false;
    }
//-------------------------------------------BOOKMARKS END------------------------------------------

    //-------PERMS---------
    private static final int PERMISSIONS_REQUEST_CODE = 69;

    //True if permissions are OK
    private boolean checkPerms() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

            return !(checkSelfPermission(PERMISSIONS_STORAGE[0]) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_DENIED);
        }
        return true;
    }

    //Display popup for user to grant permission
    private void requestPerms() { //Runtime permissions request for devices with API >= 23
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

            requestPermissions(PERMISSIONS_STORAGE, PERMISSIONS_REQUEST_CODE);
        }
    }


    @Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        switch (permsRequestCode) {
            case PERMISSIONS_REQUEST_CODE:
                downloadFile();
                break;
        }
    }


    //----------------------------------DOWNLOAD----------------------
    private ThmmyFile tempThmmyFile;

    public void downloadFile(ThmmyFile thmmyFile) {
        if (checkPerms())
            prepareDownload(thmmyFile);
        else {
            tempThmmyFile = thmmyFile;
            requestPerms();
        }
    }

    //Uses temp file - called after permission grant
    private void downloadFile() {
        if (checkPerms())
            prepareDownload(tempThmmyFile);
    }

    private void prepareDownload(ThmmyFile thmmyFile) {
        String fileName = thmmyFile.getFilename();
        if (FileUtils.fileNameExists(fileName))
            openDownloadPrompt(thmmyFile);
        else
            DownloadHelper.enqueueDownload(thmmyFile);
    }

    private void openDownloadPrompt(final ThmmyFile thmmyFile) {
        View view = getLayoutInflater().inflate(R.layout.download_prompt_dialog, null);
        final BottomSheetDialog dialog = new BottomSheetDialog(this);
        dialog.setContentView(view);
        TextView downloadPromptTextView = view.findViewById(R.id.downloadPromptTextView);
        downloadPromptTextView.setText(getString(R.string.downloadPromptText, thmmyFile.getFilename()));
        Button cancelButton = view.findViewById(R.id.cancel);
        Button openButton = view.findViewById(R.id.open);
        Button downloadButton = view.findViewById(R.id.download);
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        openButton.setOnClickListener(v -> {
            dialog.dismiss();
            try {
                String fileName = thmmyFile.getFilename();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Uri fileUri = FileProvider.getUriForFile(getApplicationContext(), getPackageName() + ".provider", new File(SAVE_DIR, fileName));
                intent.setDataAndType(fileUri, getMimeType(fileName));
                BaseActivity.this.startActivity(intent);
            } catch (Exception e) {
                Timber.e(e, "Couldn't open downloaded file...");
                Toast.makeText(getBaseContext(), "Couldn't open file...", Toast.LENGTH_SHORT).show();
            }

        });
        downloadButton.setOnClickListener(v -> {
            dialog.dismiss();
            DownloadHelper.enqueueDownload(thmmyFile);
        });
        dialog.show();
    }

    //----------------------------PRIVACY POLICY------------------

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_pp_accepted_key)))
            if(!sharedPreferences.getBoolean(key, false))
                showUserConsentDialog();
    }

    private void showUserConsentDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        builder.setTitle("User Agreement");
        builder.setMessage(R.string.user_agreement_dialog_text);
        builder.setPositiveButton("Yes, I want to help", (dialogInterface, i) -> FirebaseMessaging.getInstance().setAutoInitEnabled(true));
        builder.setNegativeButton("Nope, leave me alone", (dialogInterface, i) -> FirebaseMessaging.getInstance().setAutoInitEnabled(true));
        builder.setNeutralButton("Privacy Policy", (dialog, which) -> {/*Will be overridden below*/});
        builder.setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dialogInterface -> {
            Button button = alertDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            button.setOnClickListener(view -> showPrivacyPolicyDialog());
        }); // Overridden like this so it won't be dismissed when user touches this button
        alertDialog.show();
    }

    private void showPrivacyPolicyDialog() {
        TextView privacyPolicyTextView = new TextView(this);
        privacyPolicyTextView.setPadding(30,20,30,20);
        privacyPolicyTextView.setTextColor(ContextCompat.getColor(this, R.color.primary_text));
        SpannableConfiguration configuration = SpannableConfiguration.builder(this).linkResolver(new LinkResolverDef()).build();
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("PRIVACY.md")));
            String line;

            while ((line = reader.readLine()) != null) {
                stringBuilder.append(line);
                stringBuilder.append("\n");
            }
            Markwon.setMarkdown(privacyPolicyTextView, configuration, stringBuilder.toString());
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setView(privacyPolicyTextView);
            builder.setPositiveButton("Close", (dialogInterface, i) -> dialogInterface.dismiss());
            builder.show();
        } catch (IOException e) {
            Timber.e(e, "Error reading Privacy Policy from assets.");
        } catch (Exception e) {
            Timber.e(e, "Error in Privacy Policy dialog.");
        } finally {
            try {
                if(reader!=null)
                    reader.close();
            } catch (IOException e) {
                Timber.e(e, "Error in Privacy Policy dialog (closing reader).");
            }
        }
    }



    //----------------------------------MISC----------------------
    protected void setMainActivity(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    private void startLoginActivity(){
        Intent intent = new Intent(BaseActivity.this, LoginActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
}
