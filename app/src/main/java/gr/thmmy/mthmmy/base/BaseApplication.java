package gr.thmmy.mthmmy.base;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.itkacher.okhttpprofiler.OkHttpProfilerInterceptor;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CrashReportingTree;
import io.fabric.sdk.android.Fabric;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import timber.log.Timber;

public class BaseApplication extends Application {
    private static BaseApplication baseApplication; //BaseApplication singleton

    //Firebase Analytics
    private FirebaseAnalytics firebaseAnalytics;

    //Client & SessionManager
    private OkHttpClient client;
    private SessionManager sessionManager;

    //TODO: maybe use PreferenceManager.getDefaultSharedPreferences here as well?
    private static final String SHARED_PREFS = "ThmmySharedPrefs";

    //Display Metrics
    private static float dpWidth;

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
        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        SharedPreferences settingsSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences draftsPrefs = getSharedPreferences(getString(R.string.pref_topic_drafts_key), MODE_PRIVATE);

        if (settingsSharedPrefs.getBoolean(getString(R.string.pref_privacy_crashlytics_enable_key), false))
            startFirebaseCrashlyticsCollection();
        else
            Timber.i("Starting app with Crashlytics disabled.");

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
        boolean enableAnalytics = settingsSharedPrefs.getBoolean(getString(R.string.pref_privacy_analytics_enable_key), false);
        firebaseAnalytics.setAnalyticsCollectionEnabled(enableAnalytics);
        if (enableAnalytics)
            Timber.i("Starting app with Analytics enabled.");
        else
            Timber.i("Starting app with Analytics disabled.");

        SharedPrefsCookiePersistor sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(getApplicationContext());
        PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), sharedPrefsCookiePersistor);
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cookieJar(cookieJar)
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    HttpUrl oldUrl = chain.request().url();
                    if (Objects.equals(chain.request().url().host(), "www.thmmy.gr")) {
                        if (!oldUrl.toString().contains("theme=4")) {
                            //Probably works but needs more testing:
                            HttpUrl newUrl = oldUrl.newBuilder().addQueryParameter("theme", "4").build();
                            request = request.newBuilder().url(newUrl).build();
                        }
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

        sessionManager = new SessionManager(client, cookieJar, sharedPrefsCookiePersistor, sharedPrefs, draftsPrefs);
        Picasso picasso = new Picasso.Builder(getApplicationContext())
                .downloader(new OkHttp3Downloader(client))
                .build();

        Picasso.setSingletonInstance(picasso);  //All following Picasso (with Picasso.with(Context context) requests will use this Picasso object

        //Sets up upload service
        UploadService.NAMESPACE = BuildConfig.APPLICATION_ID;
        UploadService.HTTP_STACK = new OkHttpStack(client);

        //Initialize and create the image loader logic
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder, String tag) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return new IconicsDrawable(ctx).icon(FontAwesome.Icon.faw_user)
                            .paddingDp(10)
                            .color(ContextCompat.getColor(ctx, R.color.primary_light))
                            .backgroundColor(ContextCompat.getColor(ctx, R.color.primary));
                }
                return super.placeholder(ctx, tag);
            }
        });

        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
    }

    //Getters
    public Context getContext() {
        return getApplicationContext();
    }

    public OkHttpClient getClient() {
        return client;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public float getDpWidth() {
        return dpWidth;
    }


    //--------------------Firebase--------------------

    public void logFirebaseAnalyticsEvent(String event, Bundle params) {
        firebaseAnalytics.logEvent(event, params);
    }

    public void setFirebaseAnalyticsCollection(boolean enabled) {
        firebaseAnalytics.setAnalyticsCollectionEnabled(enabled);
        if (!enabled)
            firebaseAnalytics.resetAnalyticsData();
    }

    // Set up Crashlytics, disabled for debug builds
    public void startFirebaseCrashlyticsCollection() {
        if (!Fabric.isInitialized()) {
            Crashlytics crashlyticsKit = new Crashlytics.Builder()
                    .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                    .build();
            // Initialize Fabric with the debug-disabled Crashlytics.
            Fabric.with(this, crashlyticsKit);
            Timber.plant(new CrashReportingTree());
            Timber.i("Crashlytics enabled.");
        } else
            Timber.i("Crashlytics were already initialized for this app session.");
    }
}
