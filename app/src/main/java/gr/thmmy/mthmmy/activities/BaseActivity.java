package gr.thmmy.mthmmy.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import gr.thmmy.mthmmy.session.SessionManager;
import okhttp3.OkHttpClient;

public class BaseActivity extends AppCompatActivity
{
    // Client & Cookies
    protected static OkHttpClient client;
    private static PersistentCookieJar cookieJar;
    private static SharedPrefsCookiePersistor sharedPrefsCookiePersistor;

    //Shared Preferences
    private static final String SHARED_PREFS_NAME = "ThmmySharedPrefs";
    protected static SharedPreferences sharedPrefs;

    //SessionManager
    protected static SessionManager sessionManager;

    //Other variables
    private static boolean init = false; //To initialize stuff only once per app start


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

}
