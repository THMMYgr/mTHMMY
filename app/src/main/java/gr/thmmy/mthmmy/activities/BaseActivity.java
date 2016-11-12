package gr.thmmy.mthmmy.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

public class BaseActivity extends AppCompatActivity {

    private static boolean init =false;   //To initialize stuff only once per app start

    private static OkHttpClient client;
    protected static CookieJar cookieJar;
    protected static SharedPrefsCookiePersistor sharedPrefsCookiePersistor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!init)
        {
            sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(BaseActivity.this);
            cookieJar = new PersistentCookieJar(new SetCookieCache(), sharedPrefsCookiePersistor);
            client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();
            init =true;
        }

    }

    public static CookieJar getCookieJar()
    {
        return cookieJar;
    }

    public static SharedPrefsCookiePersistor getSharedPrefsCookiePersistor() {
        return sharedPrefsCookiePersistor;
    }

    public static OkHttpClient getClient() {
        return client;
    }
}
