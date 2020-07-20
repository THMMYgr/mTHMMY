package gr.thmmy.mthmmy.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import androidx.core.content.ContextCompat;
import androidx.multidex.MultiDexApplication;
import androidx.preference.PreferenceManager;

import com.bumptech.glide.Glide;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.crashreporting.CrashReportingTree;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.DISPLAY_RELATIVE_TIME;

// TODO: Replace MultiDexApplication with Application after KitKat support is dropped
public class BaseApplication extends MultiDexApplication {
    private static BaseApplication baseApplication; //BaseApplication singleton

    private CrashReportingTree crashReportingTree;

    //Firebase
    private static String firebaseProjectId;
    private FirebaseAnalytics firebaseAnalytics;

    //Client & SessionManager
    private OkHttpClient client;
    private SessionManager sessionManager;

    private boolean displayRelativeTime;

    //Display Metrics
    private static float widthDp;
    private static int widthPxl, heightPxl;

    public static BaseApplication getInstance() {
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this; //init singleton

        // Initialize Timber
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());

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
    }

    private void initFirebase(SharedPreferences settingsSharedPrefs){
        if (settingsSharedPrefs.getBoolean(getString(R.string.pref_privacy_crashlytics_enable_key), false)){
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
    }

    private void initOkHttp(PersistentCookieJar cookieJar){
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    HttpUrl oldUrl = chain.request().url();
                    if (Objects.equals(chain.request().url().host(), "www.thmmy.gr")
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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) { // Just for KitKats
            // Necessary because our servers don't have the right cipher suites.
            // https://github.com/square/okhttp/issues/4053
            List<CipherSuite> cipherSuites = new ArrayList<>(ConnectionSpec.MODERN_TLS.cipherSuites());
            cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA);
            cipherSuites.add(CipherSuite.TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA);

            ConnectionSpec legacyTls = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .cipherSuites(cipherSuites.toArray(new CipherSuite[0]))
                    .build();

            builder.connectionSpecs(Arrays.asList(legacyTls, ConnectionSpec.CLEARTEXT));
        }

        if (BuildConfig.DEBUG)
            builder.addInterceptor(new OkHttpProfilerInterceptor());

        client = builder.build();
    }

    private void initDrawerImageLoader(){
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
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        return ContextCompat.getDrawable(BaseApplication.getInstance(), R.drawable.ic_default_user_avatar);
                    else {  // Just for KitKats
                        return new IconicsDrawable(ctx).icon(FontAwesome.Icon.faw_user)
                                .paddingDp(10)
                                .color(ContextCompat.getColor(ctx, R.color.iron))
                                .backgroundColor(ContextCompat.getColor(ctx, R.color.primary_lighter));
                    }
                }
                return super.placeholder(ctx, tag);
            }
        });
    }

    private void setDisplayMetrics(){
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();

        widthPxl = displayMetrics.widthPixels;
        widthDp = widthPxl / displayMetrics.density;

        heightPxl = displayMetrics.heightPixels;
    }


    //-------------------- Getters --------------------
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

    //-------------------- Firebase --------------------

    public void logFirebaseAnalyticsEvent(String event, Bundle params) {
        firebaseAnalytics.logEvent(event, params);
    }

    public void setFirebaseAnalyticsEnabled(boolean enabled) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
        if (!enabled)
            firebaseAnalytics.resetAnalyticsData();

        if(enabled)
            Timber.i("Firebase Analytics enabled.");
        else
            Timber.i("Firebase Analytics disabled.");
    }

    public void setFirebaseCrashlyticsEnabled(boolean enable) {
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(enable);
        if(enable){
            crashReportingTree = new CrashReportingTree();
            Timber.plant(crashReportingTree);
            Timber.i("CrashReporting tree planted.");
            Timber.i("Firebase Crashlytics enabled.");
        }
        else{
            if(crashReportingTree!=null) {
                Timber.uproot(crashReportingTree);
                Timber.i("CrashReporting tree uprooted.");
            }
            Timber.i("Firebase Crashlytics disabled.");
        }
    }

    public static String getFirebaseProjectId(){
        return firebaseProjectId;
    }
}
