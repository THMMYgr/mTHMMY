package gr.thmmy.mthmmy.base;

import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.DISPLAY_COMPACT_TABS;
import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.DISPLAY_RELATIVE_TIME;
import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.USE_GREEK_TIMEZONE;
import static gr.thmmy.mthmmy.activities.upload.UploadActivity.firebaseConfigUploadsCoursesKey;
import static gr.thmmy.mthmmy.utils.io.ResourceUtils.readJSONResourceToString;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.localebro.okhttpprofiler.OkHttpProfilerInterceptor;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.crashreporting.CrashReportingTree;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import timber.log.Timber;

public class BaseApplication extends Application implements Executor{
    private static BaseApplication baseApplication; //BaseApplication singleton

    private CrashReportingTree crashReportingTree;

    //Global variables
    private static String forumUrl;
    private static String forumHost;
    private static String forumHostSimple;

    //Firebase
    private static String firebaseProjectId;
    private FirebaseAnalytics firebaseAnalytics;
    private FirebaseRemoteConfig firebaseRemoteConfig;

    //Client & SessionManager
    private OkHttpClient client;
    private SessionManager sessionManager;

    private boolean displayRelativeTime;
    private boolean displayCompactTabs;
    private boolean useGreekTimezone;

    //Display Metrics
    private static float widthDp;
    private static int widthPxl, heightPxl;

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this; //init singleton

        // Initialize Timber
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());

        Resources resources = getApplicationContext().getResources();
        forumUrl = resources.getString(R.string.forum_url);
        forumHost = resources.getString(R.string.forum_host);
        forumHostSimple= resources.getString(R.string.forum_host_simple);

        //Shared Preferences
        SharedPreferences sessionSharedPrefs = getSharedPreferences(getString(R.string.session_shared_prefs), MODE_PRIVATE);
        SharedPreferences settingsSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences draftsPrefs = getSharedPreferences(getString(R.string.pref_topic_drafts_key), MODE_PRIVATE);

        initFirebase(settingsSharedPrefs);

        SharedPrefsCookiePersistor sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(getApplicationContext());
        PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), sharedPrefsCookiePersistor);

        initOkHttp(cookieJar);

        sessionManager = new SessionManager(client, cookieJar, sharedPrefsCookiePersistor, sessionSharedPrefs, draftsPrefs);

        //Sets up upload service
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.HTTP_STACK = new OkHttpStack(client);

        //Initialize and create the image loader logic for the drawer
        initDrawerImageLoader();

        setDisplayMetrics();

        displayRelativeTime = settingsSharedPrefs.getBoolean(DISPLAY_RELATIVE_TIME, true);
        displayCompactTabs = settingsSharedPrefs.getBoolean(DISPLAY_COMPACT_TABS, true);
        useGreekTimezone = settingsSharedPrefs.getBoolean(USE_GREEK_TIMEZONE, true);
    }

    private void initFirebase(SharedPreferences settingsSharedPrefs) {
        if (settingsSharedPrefs.getBoolean(getString(R.string.pref_privacy_crashlytics_enable_key), false)) {
            Timber.i("Starting app with Firebase Crashlytics enabled.");
            setFirebaseCrashlyticsEnabled(true);
        }
        else {
            Timber.i("Starting app with Firebase Crashlytics disabled.");
            setFirebaseCrashlyticsEnabled(false);
        }

        firebaseProjectId = FirebaseApp.getInstance().getOptions().getProjectId();
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        boolean enableAnalytics = settingsSharedPrefs.getBoolean(getString(R.string.pref_privacy_analytics_enable_key), false);
        firebaseAnalytics.setAnalyticsCollectionEnabled(enableAnalytics);
        if (enableAnalytics)
            Timber.i("Starting app with Firebase Analytics enabled.");
        else
            Timber.i("Starting app with Firebase Analytics disabled.");

        // Firebase Remote Config (uploads courses)
        InputStream inputStream = getApplicationContext().getResources().openRawResource(R.raw.uploads_courses);
        String uploadsCourses = readJSONResourceToString(inputStream);
        Map<String, Object> uploadsCoursesMap = new HashMap<>();
        uploadsCoursesMap.put(firebaseConfigUploadsCoursesKey, uploadsCourses);

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600)
                .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);
        firebaseRemoteConfig.setDefaultsAsync(uploadsCoursesMap);
        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
                        Timber.i("Firebase remote config params updated: %s", updated);
                    } else
                        Timber.w("Fetching Firebase remote config params failed!");
                });
    }

    private void initOkHttp(PersistentCookieJar cookieJar) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    HttpUrl oldUrl = chain.request().url();
                    if (Objects.equals(chain.request().url().host(), forumHost)
                            && !oldUrl.toString().contains("theme=4")) {
                        //Probably works but needs more testing:
                        HttpUrl newUrl = oldUrl.newBuilder().addQueryParameter("theme", "4").build();
                        request = request.newBuilder().url(newUrl).build();
                    }
                    return chain.proceed(request);
                })
                .connectTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG)
            builder.addInterceptor(new OkHttpProfilerInterceptor());

        client = builder.build();
    }

    private void initDrawerImageLoader() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder, String tag) {
                Glide.with(imageView.getContext()).load(uri).circleCrop().error(placeholder).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.with(imageView.getContext()).clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return ContextCompat.getDrawable(BaseApplication.getInstance(), R.drawable.ic_default_user_avatar);
                }
                return super.placeholder(ctx, tag);
            }
        });
    }

    private void setDisplayMetrics() {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

        widthPxl = displayMetrics.widthPixels;
        widthDp = widthPxl / displayMetrics.density;

        heightPxl = displayMetrics.heightPixels;
    }


    //-------------------- Getters --------------------
    public static BaseApplication getInstance() {
        return baseApplication;
    }

    public static String getForumUrl() {
        return forumUrl;
    }

    public static String getForumHost() {
        return forumHost;
    }

    public static String getForumHostSimple() {
        return forumHostSimple;
    }

    public Context getContext() {
        return getApplicationContext();
    }

    public OkHttpClient getClient() {
        return client;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public float getWidthInDp() {
        return widthDp;
    }

    public int getWidthInPixels() {
        return widthPxl;
    }

    public int getHeightInPixels() {
        return heightPxl;
    }

    public boolean isDisplayRelativeTimeEnabled() {
        return displayRelativeTime;
    }

    public boolean isDisplayCompactTabsEnabled() {
        return displayCompactTabs;
    }

    public boolean isUseGreekTimezoneEnabled() {
        return useGreekTimezone;
    }

    //-------------------- Firebase --------------------

    public void logFirebaseAnalyticsEvent(String event, Bundle params) {
        firebaseAnalytics.logEvent(event, params);
    }

    public void setFirebaseAnalyticsEnabled(boolean enabled) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
        if (!enabled)
            firebaseAnalytics.resetAnalyticsData();

        if (enabled)
            Timber.i("Firebase Analytics enabled.");
        else
            Timber.i("Firebase Analytics disabled.");
    }

    public void setFirebaseCrashlyticsEnabled(boolean enable) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enable);
        if (enable) {
            crashReportingTree = new CrashReportingTree();
            Timber.plant(crashReportingTree);
            Timber.i("CrashReporting tree planted.");
            Timber.i("Firebase Crashlytics enabled.");
        }
        else {
            if (crashReportingTree != null) {
                Timber.uproot(crashReportingTree);
                Timber.i("CrashReporting tree uprooted.");
            }
            Timber.i("Firebase Crashlytics disabled.");
        }
    }

    public static String getFirebaseProjectId() {
        return firebaseProjectId;
    }

    // Implement Executor (for Firebase remote config)
    @Override
    public void execute(Runnable runnable) {
        runnable.run();
    }
}
