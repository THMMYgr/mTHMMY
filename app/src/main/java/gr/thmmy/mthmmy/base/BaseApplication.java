package gr.thmmy.mthmmy.base;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.okhttp.OkHttpStack;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CrashReportingTree;
import io.fabric.sdk.android.Fabric;
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

    //Shared Preferences
    private final String SHARED_PREFS_NAME = "ThmmySharedPrefs";

    //Display Metrics
    private static float dpHeight, dpWidth;

    public static BaseApplication getInstance() {
        return baseApplication;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication = this; //init singleton

        // Set up Crashlytics, disabled for debug builds
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();
        // Initialize Fabric with the debug-disabled crashlytics.
        Fabric.with(this, crashlyticsKit);
        // Initialize timber
        if (BuildConfig.DEBUG)
            Timber.plant(new Timber.DebugTree());
        else
            Timber.plant(new CrashReportingTree());

        // Analytics init
        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
        SharedPrefsCookiePersistor sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(getApplicationContext());
        PersistentCookieJar cookieJar = new PersistentCookieJar(new SetCookieCache(), sharedPrefsCookiePersistor);
        client = new OkHttpClient.Builder()
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
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build();
        sessionManager = new SessionManager(client, cookieJar, sharedPrefsCookiePersistor, sharedPrefs);
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
        dpHeight = displayMetrics.heightPixels / displayMetrics.density;
        dpWidth = displayMetrics.widthPixels / displayMetrics.density;
    }

    public void logFirebaseAnalyticsEvent(String event, Bundle params) {
        firebaseAnalytics.logEvent(event, params);
    }

    //Getters

    public OkHttpClient getClient() {
        return client;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public float getDpHeight() {
        return dpHeight;
    }

    public float getDpWidth() {
        return dpWidth;
    }
}
