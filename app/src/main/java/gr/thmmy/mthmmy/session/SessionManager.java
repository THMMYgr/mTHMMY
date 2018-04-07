package gr.thmmy.mthmmy.session;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.utils.exceptions.ParseException;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

/**
 * This class handles all session related operations (e.g. login, logout)
 * and stores data to SharedPreferences (session information and cookies).
 */
public class SessionManager {
    //Generic constants
    public static final HttpUrl indexUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php?theme=4");
    public static final HttpUrl forumUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php?action=forum;theme=4");
    private static final HttpUrl loginUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php?action=login2");
    public static final HttpUrl unreadUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php?action=unread;all;start=0;theme=4");
    private static final String guestName = "Guest";

    //Response Codes
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;    //Generic Error
    public static final int WRONG_USER = 2;
    public static final int WRONG_PASSWORD = 3;
    public static final int CANCELLED = 4;
    public static final int CONNECTION_ERROR = 5;
    public static final int EXCEPTION = 6;

    // Client & Cookies
    private final OkHttpClient client;
    private final PersistentCookieJar cookieJar;
    private final SharedPrefsCookiePersistor cookiePersistor; //Used to explicitly edit cookies in cookieJar

    //Shared Preferences & its keys
    private final SharedPreferences sharedPrefs;
    private static final String USERNAME = "Username";
    private static final String AVATAR_LINK = "AvatarLink";
    private static final String HAS_AVATAR = "HasAvatar";
    private static final String LOGOUT_LINK = "LogoutLink";
    private static final String LOGGED_IN = "LoggedIn";
    private static final String LOGIN_SCREEN_AS_DEFAULT = "LoginScreenAsDefault";

    //Constructor
    public SessionManager(OkHttpClient client, PersistentCookieJar cookieJar,
                          SharedPrefsCookiePersistor cookiePersistor, SharedPreferences sharedPrefs) {
        this.client = client;
        this.cookiePersistor = cookiePersistor;
        this.cookieJar = cookieJar;
        this.sharedPrefs = sharedPrefs;
    }

    //------------------------------------AUTH BEGINS----------------------------------------------

    /**
     * Login function with two options: (username, password) or nothing (using saved cookies).
     * Always call it in a separate thread.
     */
    public int login(String... strings) {
        Timber.i("Logging in...");

        //Build the login request for each case
        Request request;
        if (strings.length == 2) {
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
        } else {
            request = new Request.Builder()
                    .url(loginUrl)
                    .build();
        }

        try {
            //Make request & handle response
            Response response = client.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());

            if (validateRetrievedCookies())
            {
                Timber.i("Login successful!");
                setPersistentCookieSession();   //Store cookies

                //Edit SharedPreferences, save session's data
                sharedPrefs.edit().putBoolean(LOGGED_IN, true).apply();
                sharedPrefs.edit().putBoolean(LOGIN_SCREEN_AS_DEFAULT, false).apply();
                sharedPrefs.edit().putString(USERNAME, extractUserName(document)).apply();
                String avatar = extractAvatarLink(document);
                if (avatar != null) {
                    sharedPrefs.edit().putBoolean(HAS_AVATAR, true).apply();
                    sharedPrefs.edit().putString(AVATAR_LINK, extractAvatarLink(document)).apply();
                } else
                    sharedPrefs.edit().putBoolean(HAS_AVATAR, false).apply();


                sharedPrefs.edit().putString(LOGOUT_LINK, extractLogoutLink(document)).apply();

                return SUCCESS;
            } else {
                Timber.i("Login failed.");

                //Investigate login failure
                Elements error = document.select("b:contains(That username does not exist.)");
                if (error.size() == 1) { //Wrong username
                    Timber.i("Wrong Username");
                    return WRONG_USER;
                }

                error = document.select("body:contains(Password incorrect)");
                if (error.size() == 1) { //Wrong password
                    Timber.i("Wrong Password");
                    return WRONG_PASSWORD;
                }

                //Other error e.g. session was reset server-side
                clearSessionData(); //Clear invalid saved data
                return FAILURE;
            }
            //Handle exception
        } catch (InterruptedIOException e) {
            Timber.i("Login InterruptedIOException");    //users cancels LoginTask
            return CANCELLED;
        } catch (IOException e) {
            Timber.w(e ,"Login IOException");
            return CONNECTION_ERROR;
        } catch (Exception e) {
            Timber.e(e, "Login Exception (other)");
            return EXCEPTION;
        }
    }

    /**
     * A function that checks the validity of the current saved session (if it exists).
     * If isLoggedIn() is true, it will call login() with cookies. On failure, this can only return
     * the code FAILURE. CANCELLED, CONNECTION_ERROR and EXCEPTION are simply considered a SUCCESS
     * (e.g. no internet connection), at least until a more thorough handling of different
     * exceptions is implemented (if considered mandatory).
     * Always call it in a separate thread in a way that won't hinder performance (e.g. after
     * fragments' data are retrieved).
     */
    public void validateSession() {
        Timber.i("Validating session...");

        if (isLoggedIn()) {
            int loginResult = login();
            if (loginResult != FAILURE)
                return;
        } else if (isLoginScreenDefault())
            return;

        sharedPrefs.edit().putBoolean(LOGIN_SCREEN_AS_DEFAULT, true).apply();
        clearSessionData();
    }

    /**
     * Call this function when user explicitly chooses to continue as a guest (UI thread).
     */
    public void guestLogin() {
        Timber.i("Continuing as a guest, as chosen by the user.");
        clearSessionData();
        sharedPrefs.edit().putBoolean(LOGIN_SCREEN_AS_DEFAULT, false).apply();
    }


    /**
     * Logout function. Always call it in a separate thread.
     */
    public int logout() {
        Timber.i("Logging out...");

        Request request = new Request.Builder()
                .url(sharedPrefs.getString(LOGOUT_LINK, "LogoutLink"))
                .build();

        try {
            //Make request & handle response
            Response response = client.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());

            Elements loginButton = document.select("[value=Login]");  //Attempt to find login button
            if (!loginButton.isEmpty()) //If login button exists, logout was successful
            {
                Timber.i("Logout successful!");
                return SUCCESS;
            } else {
                Timber.i("Logout failed.");
                return FAILURE;
            }
        } catch (IOException e) {
            Timber.w("Logout IOException", e);
            return CONNECTION_ERROR;
        } catch (Exception e) {
            Timber.e(e, "Logout Exception");
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

    public String getAvatarLink() {
        return sharedPrefs.getString(AVATAR_LINK, "AvatarLink");
    }

    public boolean hasAvatar() {
        return sharedPrefs.getBoolean(HAS_AVATAR, false);
    }

    public boolean isLoggedIn() {
        return sharedPrefs.getBoolean(LOGGED_IN, false);
    }

    public boolean isLoginScreenDefault() {
        return sharedPrefs.getBoolean(LOGIN_SCREEN_AS_DEFAULT, true);
    }

    public String getCookieHeader() {
        return cookiePersistor.loadAll().get(0).toString();
    }

    //--------------------------------------GETTERS END---------------------------------------------

    //------------------------------------OTHER FUNCTIONS-------------------------------------------
    private boolean validateRetrievedCookies() {
        List<Cookie> cookieList = cookieJar.loadForRequest(indexUrl);
        return (cookieList.size() == 2) && (cookieList.get(0).name().equals("THMMYgrC00ki3")) && (cookieList.get(1).name().equals("PHPSESSID"));
    }

    // Call validateRetrievedCookies() first
    private void setPersistentCookieSession() {
        List<Cookie> cookieList = cookieJar.loadForRequest(indexUrl);
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

    private void clearSessionData() {
        cookieJar.clear();
        sharedPrefs.edit().clear().apply(); //Clear session data
        sharedPrefs.edit().putString(USERNAME, guestName).apply();
        sharedPrefs.edit().putBoolean(LOGGED_IN, false).apply(); //User logs out
        Timber.i("Session data cleared.");
    }

    @NonNull
    private String extractUserName(@NonNull Document doc) {
        //Scribbles2 Theme
        Elements user = doc.select("div[id=myuser] > h3");
        String userName = null;

        if (user.size() == 1) {
            String txt = user.first().ownText();

            Pattern pattern = Pattern.compile(", (.*?),");
            Matcher matcher = pattern.matcher(txt);
            if (matcher.find())
                userName = matcher.group(1);
        } else {
            //Helios_Multi and SMF_oneBlue
            user = doc.select("td.smalltext[width=100%] b");
            if (user.size() == 1)
                userName = user.first().ownText();
            else {
                //SMF Default Theme
                user = doc.select("td.titlebg2[height=32] b");
                if (user.size() == 1)
                    userName = user.first().ownText();
            }
        }

        if (userName != null && !userName.isEmpty())
            return userName;

        Timber.e(new ParseException("Parsing failed(username extraction)"),"ParseException");
        return "User"; //return a default username
    }


    @Nullable
    private String extractAvatarLink(@NonNull Document doc) {
        Elements avatar = doc.getElementsByClass("avatar");
        if (!avatar.isEmpty())
            return avatar.first().attr("src");

        Timber.i("Extracting avatar's link failed!");
        return null;
    }

    @NonNull
    private String extractLogoutLink(@NonNull Document doc) {
        Elements logoutLink = doc.select("a[href^=https://www.thmmy.gr/smf/index.php?action=logout;sesc=]");

        if (!logoutLink.isEmpty()) {
            String link = logoutLink.first().attr("href");
            if (link != null && !link.isEmpty())
                return link;
        }
        Timber.e(new ParseException("Parsing failed(logoutLink extraction)"),"ParseException");
        return "https://www.thmmy.gr/smf/index.php?action=logout"; //return a default link
    }
    //----------------------------------OTHER FUNCTIONS END-----------------------------------------

}