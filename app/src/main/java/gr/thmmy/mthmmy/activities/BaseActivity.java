package gr.thmmy.mthmmy.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import okhttp3.CookieJar;

public class BaseActivity extends AppCompatActivity {

    private static boolean cookiesInit=false;   //To initialize cookie stuff only once per app start
    protected static CookieJar cookieJar;
    protected static SharedPrefsCookiePersistor sharedPrefsCookiePersistor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(!cookiesInit)
        {
            sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(BaseActivity.this);
            cookieJar = new PersistentCookieJar(new SetCookieCache(), sharedPrefsCookiePersistor);
            cookiesInit=true;
        }

    }

    public static CookieJar getCookieJar()
    {
        return cookieJar;
    }

    public static SharedPrefsCookiePersistor getSharedPrefsCookiePersistor() {
        return sharedPrefsCookiePersistor;
    }
}
