package gr.thmmy.mthmmy.session;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

import gr.thmmy.mthmmy.utils.parsing.ParseException;
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
    public static final HttpUrl shoutboxUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php?action=tpmod;sa=shoutbox;theme=4");
    static final String baseLogoutLink = "https://www.thmmy.gr/smf/index.php?action=logout;sesc=";
    static final String baseMarkAllAsReadLink = "https://www.thmmy.gr/smf/index.php?action=markasread;sa=all;sesc=";
    private static final String guestName = "Guest";

    //Response Codes - make sure they do not overlap with NetworkResultCodes, just in case
    public static final int SUCCESS = 20;
    public static final int FAILURE = 21;    //Generic Error
    public static final int WRONG_USER = 22;
    public static final int WRONG_PASSWORD = 23;
    public static final int CANCELLED = 24;
    public static final int CONNECTION_ERROR = 25;
    public static final int EXCEPTION = 26;
    public static final int BANNED_USER = 27;
    public static final int INVALID_SESSION = 28;

    // Client & Cookies
    private final OkHttpClient client;
    private final PersistentCookieJar cookieJar;
    private final SharedPrefsCookiePersistor cookiePersistor; //Used to explicitly edit cookies in cookieJar

    //Shared Preferences & its keys
    private final SharedPreferences sessionSharedPrefs;
    private final SharedPreferences draftsPrefs;
    private static final String USERNAME = "Username";
    private static final String USER_ID = "UserID";
    private static final String AVATAR_LINK = "AvatarLink";
    private static final String HAS_AVATAR = "HasAvatar";
    private static final String LOGGED_IN = "LoggedIn";
    private static final String LOGIN_SCREEN_AS_DEFAULT = "LoginScreenAsDefault";

    //Constructor
    public SessionManager(OkHttpClient client, PersistentCookieJar cookieJar,
                          SharedPrefsCookiePersistor cookiePersistor, SharedPreferences sessionSharedPrefs, SharedPreferences draftsPrefs) {
        this.client = client;
        this.cookiePersistor = cookiePersistor;
        this.cookieJar = cookieJar;
        this.sessionSharedPrefs = sessionSharedPrefs;
        this.draftsPrefs = draftsPrefs;
    }

    //------------------------------------ AUTH ----------------------------------------------

    /**
     * Login function with two options: (username, password) or nothing (using saved cookies).
     * Always call it in a separate thread.
     */
    public int login(String username, String password) {
        Timber.d("Logging in...");

        //Build the login request for each case
        Request request;
        clearSessionData();

        RequestBody formBody = new FormBody.Builder()
                .add("user", username)
                .add("passwrd", password)
                .add("cookielength", "-1") //-1 is forever
                .build();
        request = new Request.Builder()
                .url(loginUrl)
                .post(formBody)
                .build();


        try {
            //Make request & handle response
            Response response = client.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());

            if (validateRetrievedCookies()) {
                Timber.i("Login successful!");
                setPersistentCookieSession();   //Store cookies

                //Edit SharedPreferences, save session's data
                SharedPreferences.Editor editor = sessionSharedPrefs.edit();
                setLoginScreenAsDefault(false);
                editor.putBoolean(LOGGED_IN, true);
                editor.putString(USERNAME, extractUserName(document));
                editor.putInt(USER_ID, extractUserId(document));
                String avatar = extractAvatarLink(document);
                if (avatar != null)
                    editor.putString(AVATAR_LINK, avatar);
                editor.putBoolean(HAS_AVATAR, avatar != null);
                editor.apply();

                return SUCCESS;
            } else {
                Timber.i("Login failed.");

                //Investigate login failure
                Elements error = document.select("b:contains(That username does not exist.)");
                if (error.size() > 0) { //Wrong username
                    Timber.i("Wrong Username");
                    return WRONG_USER;
                }

                error = document.select("body:contains(Password incorrect)");
                if (error.size() > 0) { //Wrong password
                    Timber.i("Wrong Password");
                    return WRONG_PASSWORD;
                }

                error = document.select("body:contains(you are banned from using this forum!),body:contains(έχετε αποκλειστεί από αυτή τη δημόσια συζήτηση!)");
                if (error.size() > 0) { //User is banned
                    Timber.i("User is banned");
                    return BANNED_USER;
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
     * Call this function when user explicitly chooses to continue as a guest (UI thread).
     */
    public void guestLogin() {
        Timber.i("Continuing as a guest, as chosen by the user.");
        clearSessionData();
        setLoginScreenAsDefault(false);
    }

    void logoutCleanup() {
        clearSessionData();
        guestLogin();
    }

    private void clearSessionData() {
        cookieJar.clear();
        sessionSharedPrefs.edit().clear().apply(); //Clear session data
        sessionSharedPrefs.edit().putString(USERNAME, guestName).apply();
        sessionSharedPrefs.edit().putInt(USER_ID, -1).apply();
        sessionSharedPrefs.edit().putBoolean(LOGGED_IN, false).apply(); //User logs out
        draftsPrefs.edit().clear().apply(); //Clear saved drafts
        Timber.i("Session data cleared.");
    }

    //--------------------------------------- GETTERS ------------------------------------------------
    public String getUsername() {
        return sessionSharedPrefs.getString(USERNAME, USERNAME);
    }

    public int getUserId() {
        return sessionSharedPrefs.getInt(USER_ID, -1);
    }

    public String getAvatarLink() {
        return sessionSharedPrefs.getString(AVATAR_LINK, AVATAR_LINK);
    }

    public Cookie getThmmyCookie() {
        List<Cookie> cookieList = cookieJar.loadForRequest(indexUrl);
        for(Cookie cookie: cookieList) {
            if(cookie.name().equals("THMMYgrC00ki3"))
                return cookie;
        }
        return null;
    }

    public boolean hasAvatar() {
        return sessionSharedPrefs.getBoolean(HAS_AVATAR, false);
    }

    public boolean isLoggedIn() {
        return sessionSharedPrefs.getBoolean(LOGGED_IN, false);
    }

    public boolean isLoginScreenDefault() {
        return sessionSharedPrefs.getBoolean(LOGIN_SCREEN_AS_DEFAULT, true);
    }

    //------------------------------------ OTHER -------------------------------------------
    private boolean validateRetrievedCookies() {
        List<Cookie> cookieList = cookieJar.loadForRequest(indexUrl);
        for(Cookie cookie: cookieList) {
            if(cookie.name().equals("THMMYgrC00ki3"))
                return true;
        }
        return false;
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

    private void setLoginScreenAsDefault(boolean b){
        sessionSharedPrefs.edit().putBoolean(LOGIN_SCREEN_AS_DEFAULT, b).apply();
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

    private int extractUserId(@NonNull Document doc) {
        try{
            Elements elements = doc.select("a:containsOwn(Εμφάνιση των μηνυμάτων σας), a:containsOwn(Show own posts)");
            if (elements.size() == 1) {
                String link = elements.first().attr("href");

                Pattern pattern = Pattern.compile("https://www.thmmy.gr/smf/index.php\\?action=profile;u=(\\d*);sa=showPosts");
                Matcher matcher = pattern.matcher(link);
                if (matcher.find())
                    return Integer.parseInt(matcher.group(1));
            }
        } catch (Exception e) {
            Timber.e(new ParseException("Parsing failed(user id extraction)"),"ParseException");
        }
        Timber.e(new ParseException("Parsing failed(user id extraction)"),"ParseException");
        return -1;
    }

    @Nullable
    private String extractAvatarLink(@NonNull Document doc) {
        Elements avatar = doc.getElementsByClass("avatar");
        if (!avatar.isEmpty())
            return avatar.first().attr("src");

        Timber.i("Extracting avatar's link failed!");
        return null;
    }
}