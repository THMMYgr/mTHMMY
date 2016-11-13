package gr.thmmy.mthmmy.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import gr.thmmy.mthmmy.utils.Thmmy;
import okhttp3.CookieJar;
import okhttp3.OkHttpClient;

public class BaseActivity extends AppCompatActivity {

    private static boolean init =false;   //To initialize stuff only once per app start

    protected static OkHttpClient client;
    protected static CookieJar cookieJar;
    protected static SharedPrefsCookiePersistor sharedPrefsCookiePersistor;

    protected static Thmmy.LoginData loginData;

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
            loginData = new Thmmy.LoginData();
            loginData.setStatus(0);
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

    public void setLoginData(Thmmy.LoginData loginData) {
        this.loginData = loginData;
    }
}
