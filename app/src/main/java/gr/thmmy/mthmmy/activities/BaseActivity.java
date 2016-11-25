package gr.thmmy.mthmmy.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BaseActivity extends AppCompatActivity {

//----------------------------------------CLASS VARIABLES-----------------------------------------
    /* --Response Codes-- */
    static final int LOGGED_OUT = 0;
    static final int LOGGED_IN = 1;
    static final int WRONG_USER = 2;
    static final int WRONG_PASSWORD = 3;
    static final int FAILED = 4;
    static final int CERTIFICATE_ERROR = 5;
    static final int OTHER_ERROR = 6;
    /* --Response Codes End-- */
    /* --Shared Preferences-- */
    static final String USER_NAME = "userNameKey";
    static final String GUEST_PREF_USERNAME = "GUEST";
    static final String LOG_STATUS = "isLoggedIn";
    private static final String SHARED_PREFS_NAME = "thmmySharedPrefs";
    static SharedPreferences _prefs;
    /* --Shared Preferences End-- */

    /* --Client Stuff-- */
    static OkHttpClient client;
    private static CookieJar cookieJar;
    private static SharedPrefsCookiePersistor sharedPrefsCookiePersistor;
    /* --Client Stuff End-- */

    //Other variables
    private static boolean init = false; //To initialize stuff only once per app start
    private static final String TAG = "BaseActivity";
//--------------------------------------CLASS VARIABLES END---------------------------------------

//-------------------------------------CLIENT AND COOKIES-----------------------------------------
    private static CookieJar getCookieJar() {
        return cookieJar;
    }

    private static SharedPrefsCookiePersistor getSharedPrefsCookiePersistor() {
        return sharedPrefsCookiePersistor;
    }

    public static OkHttpClient getClient() {
        return client;
    }
//-----------------------------------CLIENT AND COOKIES END---------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!init) {
            _prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
            sharedPrefsCookiePersistor = new SharedPrefsCookiePersistor(BaseActivity.this);
            cookieJar = new PersistentCookieJar(new SetCookieCache(), sharedPrefsCookiePersistor);
            client = new OkHttpClient.Builder()
                    .cookieJar(cookieJar)
                    .build();
            init = true;
        }

    }

    /*
        THMMY CLASS
            -- inner class of BaseActivity

        This class handles all session related operations (e.g. login, logout)
        Also stores data to SharedPreferences file.
     */

//---------------------------------------INNER CLASS BEGINS---------------------------------------
    public static class Thmmy {
        //Class variables
        private static final HttpUrl loginUrl = HttpUrl
                .parse("https://www.thmmy.gr/smf/index.php?action=login2");
        private static final HttpUrl indexUrl = HttpUrl
                .parse("https://www.thmmy.gr/smf/index.php");


//-------------------------------------------LOGIN------------------------------------------------
        //Two options: (username, password, duration) or nothing - cookies
        static void login(String... strings) {
            Log.d("Login", "Logging in...");
            Request request;

            if (strings.length == 3) { //Actual login
                String loginName = strings[0];
                String password = strings[1];
                String duration = strings[2];

                ((PersistentCookieJar) getCookieJar()).clear();

                RequestBody formBody = new FormBody.Builder() //Build login form
                        .add("user", loginName)
                        .add("passwrd", password)
                        .add("cookielength", duration) //Forever is -1
                        .build();
                request = new Request.Builder() //Build the request
                        .url(loginUrl)
                        .post(formBody)
                        .build();
            } else { //Already logged in, just get cookies
                request = new Request.Builder() //Build the request
                        .url(loginUrl)
                        .build();
            }

            try {
                Response response = client.newCall(request).execute(); //Make the request
                /* --Handle response-- */
                Document document = Jsoup.parse(response.body().string());
                Element logout = document.getElementById("logoutbtn"); //Get logout button

                if (logout != null) { //If there is a logout button, then I successfully logged in
                    Log.i("Login", "Login successful");
                    setPersistentCookieSession();

                    //Edit SharedPreferences, save session's data
                    _prefs.edit().putString(USER_NAME, extractUserName(document)).apply();
                    _prefs.edit().putInt(LOG_STATUS, LOGGED_IN).apply();
                } else { //I am not logged in, what went wrong?
                    Log.w("Login", "Login failed");
                    _prefs.edit().putInt(LOG_STATUS, FAILED).apply(); //Request failed

                    //Making error more specific

                    Elements error = document.select("b:contains(That username does not exist.)");
                    if (error.size() == 1) { //Wrong username
                        _prefs.edit().putInt(LOG_STATUS, WRONG_USER).apply();
                        Log.d("Login", "Wrong Username");
                    }

                    error = document.select("body:contains(Password incorrect)");
                    if (error.size() == 1) { //Wrong password
                        _prefs.edit().putInt(LOG_STATUS, WRONG_PASSWORD).apply();
                        Log.d("Login", "Wrong Password");
                    }

                    ((PersistentCookieJar) getCookieJar()).clear();

                }
                //Request exception handling
            } catch (SSLHandshakeException e) {
                _prefs.edit().putInt(LOG_STATUS, CERTIFICATE_ERROR).apply();
                Log.w("Login", "Certificate problem");

            } catch (Exception e) {
                _prefs.edit().putInt(LOG_STATUS, OTHER_ERROR).apply();
                Log.e("Login", "Error", e);
            }
        }
//--------------------------------------LOGIN ENDS------------------------------------------------

//---------------------------------------LOGOUT---------------------------------------------------
        static int logout() {
            String _logout_link = "";
            { //Find current logout link
                try {
                    //Build and make a request for the index (home) page
                    Request request = new Request.Builder()
                            .url(indexUrl)
                            .build();
                    Response response = client.newCall(request).execute();
                    Document document = Jsoup.parse(response.body().string());
                    Element logout = document.getElementById("logoutbtn"); //Find the logout button
                    _logout_link = HttpUrl.parse(logout.attr("href")).toString(); //Get the url
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (Objects.equals(_logout_link, "")) { //If logout button wasn't found
                return OTHER_ERROR; //Something went wrong
            }

            //Attempt logout
            OkHttpClient client = getClient();
            Request request = new Request.Builder()
                    .url(_logout_link)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                Document document = Jsoup.parse(response.body().string());

                Elements login = document.select("[pageValue=Login]"); //Find login button
                ((PersistentCookieJar) getCookieJar()).clear();
                if (!login.isEmpty()) { //If found, logout was successful
                    Log.i("Logout", "Logout successful");
                    _prefs.edit().clear().apply(); //Clear session data
                    //User is now guest
                    _prefs.edit().putString(USER_NAME, GUEST_PREF_USERNAME).apply();
                    _prefs.edit().putInt(LOG_STATUS, LOGGED_IN).apply();
                    return LOGGED_OUT;
                } else {
                    Log.w("Logout", "Logout failed");
                    return FAILED;
                }
            } catch (SSLHandshakeException e) {
                Log.w("Logout", "Certificate problem (please switch to unsafe connection).");
                return CERTIFICATE_ERROR;

            } catch (Exception e) {
                Log.d("Logout", "ERROR", e);
                return OTHER_ERROR;
            }
        }
//----------------------------------------LOGOUT ENDS---------------------------------------------

//-------------------------------------------MISC-------------------------------------------------
        private static String extractUserName(Document doc) {
            if (doc != null) {
                Elements user = doc.select("div[id=myuser] > h3");

                if (user.size() == 1) {
                    String txt = user.first().ownText();

                    Pattern pattern = Pattern.compile(", (.*?),");
                    Matcher matcher = pattern.matcher(txt);
                    if (matcher.find())
                        return matcher.group(1);
                }
            }

            return null;
        }

        private static void setPersistentCookieSession() {
            List<Cookie> cookieList = getCookieJar().loadForRequest(HttpUrl
                    .parse("https://www.thmmy.gr"));

            if (cookieList.size() == 2) {
                if ((cookieList.get(0).name().equals("THMMYgrC00ki3"))
                        && (cookieList.get(1).name().equals("PHPSESSID"))) {
                    Cookie.Builder builder = new Cookie.Builder();
                    builder.name(cookieList.get(1).name())
                            .value(cookieList.get(1).value())
                            .domain(cookieList.get(1).domain())
                            .expiresAt(cookieList.get(0).expiresAt());
                    cookieList.remove(1);
                    cookieList.add(builder.build());
                    getSharedPrefsCookiePersistor().clear();
                    getSharedPrefsCookiePersistor().saveAll(cookieList);
                }
            }
        }
    }
//----------------------------------------INNER CLASS ENDS----------------------------------------
}
