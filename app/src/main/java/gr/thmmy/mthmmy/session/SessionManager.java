package gr.thmmy.mthmmy.session;

import android.content.SharedPreferences;
import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
    This class handles all session related operations (e.g. login, logout)
    and stores data to SharedPreferences (session information and cookies).
*/
public class SessionManager
{
    //Class TAG
    private static final String TAG = "SessionManager";

    //Generic constants
    public static final HttpUrl indexUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php");
    private static final HttpUrl loginUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php?action=login2");
    private static final String guestName = "Guest";

    //Response Codes
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;    //Generic Error
    public static final int WRONG_USER = 2;
    public static final int WRONG_PASSWORD = 3;
    public static final int CONNECTION_ERROR = 4;
    public static final int EXCEPTION = 5;

    //Login status codes
    public static final int LOGGED_OUT = 0;
    public static final int LOGGED_IN = 1;  //Logged in (as a normal user)
    public static final int AS_GUEST = 2;   //User chose to continue as guest

    // Client & Cookies
    private OkHttpClient client;
    private PersistentCookieJar cookieJar;
    private SharedPrefsCookiePersistor cookiePersistor; //Used to explicitly edit cookies in cookieJar

    //Shared Preferences & its keys
    private SharedPreferences sharedPrefs;
    private static final String USERNAME = "Username";
    private static final String LOGOUT_LINK = "LogoutLink";
    private static final String LOGIN_STATUS = "IsLoggedIn";

    //Constructor
    public SessionManager(OkHttpClient client, PersistentCookieJar cookieJar,
                          SharedPrefsCookiePersistor cookiePersistor, SharedPreferences sharedPrefs)
    {
        this.client = client;
        this.cookiePersistor=cookiePersistor;
        this.cookieJar = cookieJar;
        this.sharedPrefs = sharedPrefs;
    }

    //------------------------------------AUTH BEGINS----------------------------------------------
    /**
     *  Login function with two options: (username, password) or nothing (using saved cookies).
     *  Always call it in a separate thread.
     */
    public int login(String... strings)
    {
        Log.i(TAG, "Logging in...");

        //Build the login request for each case
        Request request;
        if (strings.length == 2)
        {
            clearSessionData();

            String loginName = strings[0];
            String password = strings[1];

            RequestBody formBody = new FormBody.Builder()
                    .add("user", loginName)
                    .add("passwrd", password)
                    .add("cookielength", "-1") //-1 is forever
                    .build();
            request = new Request.Builder()
                    .url(loginUrl)
                    .post(formBody)
                    .build();
        }
        else
        {
            request = new Request.Builder()
                    .url(loginUrl)
                    .build();
        }

        try {
            //Make request & handle response
            Response response = client.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());

            Element logoutButton = document.getElementById("logoutbtn"); //Attempt to find logout button
            if (logoutButton != null) //If logout button exists, login was successful
            {
                Log.i(TAG, "Login successful!");
                setPersistentCookieSession();   //Store cookies

                //Edit SharedPreferences, save session's data
                sharedPrefs.edit().putInt(LOGIN_STATUS, LOGGED_IN).apply();
                sharedPrefs.edit().putString(USERNAME, extractUserName(document)).apply();
                sharedPrefs.edit().putString(LOGOUT_LINK, HttpUrl.parse(logoutButton.attr("href")).toString()).apply();

                return SUCCESS;
            }
            else
            {
                Log.i(TAG, "Login failed.");

                //Investigate login failure
                Elements error = document.select("b:contains(That username does not exist.)");
                if (error.size() == 1) { //Wrong username
                    Log.i(TAG, "Wrong Username");
                    return WRONG_USER;
                }

                error = document.select("body:contains(Password incorrect)");
                if (error.size() == 1) { //Wrong password
                    Log.i(TAG, "Wrong Password");
                    return WRONG_PASSWORD;
                }

                //Other error e.g. session was reset server-side
                clearSessionData(); //Clear invalid saved data
                return FAILURE;
            }
            //Handle exception
        }
        catch (IOException e) {
            Log.w(TAG, "Login IOException: "+ e.getMessage(), e);
            return CONNECTION_ERROR;
        }
        catch (Exception e) {
            Log.w(TAG, "Login Exception (other): "+ e.getMessage(), e);
            return EXCEPTION;
        }
    }

    /**
     *  A function that checks the validity of the current saved session (if it exists).
     *  If LOGIN_STATUS is true, it will call login() with cookies. This can only return
     *  the codes {SUCCESS, FAILURE, CONNECTION_ERROR, EXCEPTION}. CONNECTION_ERROR and EXCEPTION
     *  are simply considered a SUCCESS (e.g. no internet connection), at least until a more
     *  thorough handling of different exceptions is implemented (if considered mandatory).
     *  Always call it in a separate thread in a way that won't hinder performance (e.g. after
     *  fragments' data are retrieved).
     */
    public void validateSession()
    {
        Log.i(TAG, "Validating session...");

        //Check if user is currently logged in
        int status = sharedPrefs.getInt(LOGIN_STATUS,LOGGED_OUT);
        if(status==LOGGED_IN)
        {
            int loginResult = login();
            if(loginResult == SUCCESS || loginResult == CONNECTION_ERROR || loginResult == EXCEPTION)
                return;
        }
        else if(status==AS_GUEST)
            return;

        clearSessionData();
    }

    /**
     *  Call this function when user explicitly chooses to continue as a guest (UI thread).
     */
    public void guestLogin()
    {
        Log.i("TAG", "Continuing as a guest, as chosen by the user.");
        clearSessionData();
        sharedPrefs.edit().putInt(LOGIN_STATUS, AS_GUEST).apply();
    }


    /**
     *  Logout function. Always call it in a separate thread.
     */
    public int logout()
    {
        Log.i(TAG, "Logging out...");

        Request request = new Request.Builder()
                .url(sharedPrefs.getString(LOGOUT_LINK,"LogoutLink"))
                .build();

        try {
            //Make request & handle response
            Response response = client.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());

            Elements loginButton = document.select("[value=Login]");  //Attempt to find login button
            if (!loginButton.isEmpty()) //If login button exists, logout was successful
            {
                Log.i("Logout", "Logout successful!");
                return SUCCESS;
            } else {
                Log.i(TAG, "Logout failed.");
                return FAILURE;
            }
        } catch (IOException e) {
            Log.w(TAG, "Logout IOException: "+ e.getMessage(), e);
            return CONNECTION_ERROR;
        } catch (Exception e) {
            Log.w(TAG, "Logout Exception: "+ e.getMessage(), e);
            return EXCEPTION;
        } finally {
            //All data should always be cleared from device regardless the result of logout
            clearSessionData();
            guestLogin();
        }
    }
    //--------------------------------------AUTH ENDS-----------------------------------------------

    //---------------------------------------GETTERS------------------------------------------------
    public String getUsername() {
        return sharedPrefs.getString(USERNAME, "Username");
    }

    public int getLogStatus() {
        return sharedPrefs.getInt(LOGIN_STATUS, LOGGED_OUT);
    }

    //--------------------------------------GETTERS END---------------------------------------------

    //------------------------------------OTHER FUNCTIONS-------------------------------------------
    private void setPersistentCookieSession()
    {
        List<Cookie> cookieList = cookieJar.loadForRequest(indexUrl);

        if (cookieList.size() == 2)
        {
            if ((cookieList.get(0).name().equals("THMMYgrC00ki3"))
                    && (cookieList.get(1).name().equals("PHPSESSID"))) {
                Cookie.Builder builder = new Cookie.Builder();
                builder.name(cookieList.get(1).name())
                        .value(cookieList.get(1).value())
                        .domain(cookieList.get(1).domain())
                        .expiresAt(cookieList.get(0).expiresAt());
                cookieList.remove(1);
                cookieList.add(builder.build());
                cookiePersistor.clear();
                cookiePersistor.saveAll(cookieList);
            }
        }
    }

    private void clearSessionData()
    {
        cookieJar.clear();
        sharedPrefs.edit().clear().apply(); //Clear session data
        sharedPrefs.edit().putString(USERNAME, guestName).apply();
        sharedPrefs.edit().putInt(LOGIN_STATUS, LOGGED_OUT).apply(); //User logs out
        Log.i(TAG,"Session data cleared.");
    }

    private String extractUserName(Document doc)
    {
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
    //----------------------------------OTHER FUNCTIONS END-----------------------------------------

}
