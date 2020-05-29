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
    private static final String baseLogoutLink = "https://www.thmmy.gr/smf/index.php?action=logout;sesc=";
    private static final String baseMarkAllAsReadLink = "https://www.thmmy.gr/smf/index.php?action=markasread;sa=all;sesc=";
    private static final String guestName = "Guest";

    //Response Codes
    public static final int SUCCESS = 0;
    public static final int FAILURE = 1;    //Generic Error
    public static final int WRONG_USER = 2;
    public static final int WRONG_PASSWORD = 3;
    public static final int CANCELLED = 4;
    public static final int CONNECTION_ERROR = 5;
    public static final int EXCEPTION = 6;
    public static final int BANNED_USER = 7;

    // Client & Cookies
    private final OkHttpClient client;
    private final PersistentCookieJar cookieJar;
    private final SharedPrefsCookiePersistor cookiePersistor; //Used to explicitly edit cookies in cookieJar

    //Shared Preferences & its keys
    private final SharedPreferences sharedPrefs;
    private final SharedPreferences draftsPrefs;
    private static final String USERNAME = "Username";
    private static final String USER_ID = "UserID";
    private static final String AVATAR_LINK = "AvatarLink";
    private static final String HAS_AVATAR = "HasAvatar";
    private static final String SESC = "Sesc";
    private static final String LOGOUT_LINK = "LogoutLink";
    private static final String MARK_ALL_AS_READ_LINK = "MarkAllAsReadLink";
    private static final String LOGGED_IN = "LoggedIn";
    private static final String LOGIN_SCREEN_AS_DEFAULT = "LoginScreenAsDefault";

    //Constructor
    public SessionManager(OkHttpClient client, PersistentCookieJar cookieJar,
                          SharedPrefsCookiePersistor cookiePersistor, SharedPreferences sharedPrefs, SharedPreferences draftsPrefs) {
        this.client = client;
        this.cookiePersistor = cookiePersistor;
        this.cookieJar = cookieJar;
        this.sharedPrefs = sharedPrefs;
        this.draftsPrefs = draftsPrefs;
    }

    //------------------------------------AUTH BEGINS----------------------------------------------

    /**
     * Login function with two options: (username, password) or nothing (using saved cookies).
     * Always call it in a separate thread.
     */
    public int login(String... strings) {
        Timber.d("Logging in...");

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

            if (validateRetrievedCookies()) {
                Timber.i("Login successful!");
                setPersistentCookieSession();   //Store cookies

                //Edit SharedPreferences, save session's data
                SharedPreferences.Editor editor = sharedPrefs.edit();
                setLoginScreenAsDefault(false);
                editor.putBoolean(LOGGED_IN, true);
                editor.putString(USERNAME, extractUserName(document));
                editor.putInt(USER_ID, extractUserId(document));
                String avatar = extractAvatarLink(document);
                if (avatar != null)
                    editor.putString(AVATAR_LINK, avatar);
                editor.putBoolean(HAS_AVATAR, avatar != null);
                String sesc = extractSesc(document);
                editor.putString(SESC, sesc);
                editor.putString(LOGOUT_LINK, generateLogoutLink(sesc));
                editor.putString(MARK_ALL_AS_READ_LINK, generateMarkAllAsReadLink(sesc));
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
     * A function that checks the validity of the current saved session (if it exists).
     * If isLoggedIn() is true, it will call login() with cookies. On failure, this can only return
     * the code FAILURE. CANCELLED, CONNECTION_ERROR and EXCEPTION are simply considered a SUCCESS
     * (e.g. no internet connection), at least until a more thorough handling of different
     * exceptions is implemented (if considered mandatory).
     * Always call it in a separate thread in a way that won't hinder performance (e.g. after
     * fragments' data are retrieved).
     */
    void validateSession() {
        Timber.e("Validating session...");
        if (isLoggedIn()) {
            Timber.e("Refreshing session...");
            int loginResult = login();
            if (loginResult != FAILURE)
                return;
        } else if (isLoginScreenDefault())
            return;

        setLoginScreenAsDefault(true);
        clearSessionData();
    }

    /**
     * Call this function when user explicitly chooses to continue as a guest (UI thread).
     */
    public void guestLogin() {
        Timber.i("Continuing as a guest, as chosen by the user.");
        clearSessionData();
        setLoginScreenAsDefault(false);
    }

    /**
     * Logout function. Always call it in a separate thread.
     */
    public int logout() {
        Timber.i("Logging out...");
        try {
            Request request = new Request.Builder()
                    .url(getLogoutLink())
                    .build();
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
            Timber.w(e, "Logout IOException");
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

    public void refreshSescFromUrl(String url){
        String sesc = extractSescFromLink(url);
        if(sesc!=null){
            setSesc(sesc);
            setLogoutLink(generateLogoutLink(sesc));
            setMarkAsReadLink(sesc);
        }
    }
    //--------------------------------------AUTH ENDS-----------------------------------------------

    //---------------------------------------GETTERS------------------------------------------------
    public String getUsername() {
        return sharedPrefs.getString(USERNAME, USERNAME);
    }

    public int getUserId() {
        return sharedPrefs.getInt(USER_ID, -1);
    }

    public String getAvatarLink() {
        return sharedPrefs.getString(AVATAR_LINK, AVATAR_LINK);
    }

    public Cookie getThmmyCookie() {
        List<Cookie> cookieList = cookieJar.loadForRequest(indexUrl);
        for(Cookie cookie: cookieList) {
            if(cookie.name().equals("THMMYgrC00ki3"))
                return cookie;
        }
        return null;
    }

    public String getMarkAllAsReadLink() {
        String markAsReadLink = sharedPrefs.getString(MARK_ALL_AS_READ_LINK, null);
        if(markAsReadLink == null){ //For older versions, extract it from logout link (otherwise user would have to login again)
            String sesc = extractSescFromLink(getLogoutLink());
            if(sesc!=null) {
                setSesc(sesc);
                markAsReadLink = generateMarkAllAsReadLink(sesc);
                setMarkAsReadLink(markAsReadLink);
                return markAsReadLink;
            }
        }
        return markAsReadLink;  // Warning: it can be null
    }

    private String getLogoutLink() {
        return sharedPrefs.getString(LOGOUT_LINK, null);
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

    //--------------------------------------GETTERS END---------------------------------------------

    //---------------------------------------SETTERS------------------------------------------------
    private void setSesc(String sesc){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(SESC, sesc);
        editor.apply();
    }

    private void setMarkAsReadLink(String markAllAsReadLink){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(MARK_ALL_AS_READ_LINK, markAllAsReadLink);
        editor.apply();
    }

    private void setLogoutLink(String logoutLink){
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(LOGOUT_LINK, logoutLink);
        editor.apply();
    }

    //--------------------------------------SETTERS END---------------------------------------------

    //------------------------------------OTHER FUNCTIONS-------------------------------------------
    private boolean validateRetrievedCookies() {
        List<Cookie> cookieList = cookieJar.loadForRequest(indexUrl);
        for(Cookie cookie: cookieList)
        {
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

    private void clearSessionData() {
        cookieJar.clear();
        sharedPrefs.edit().clear().apply(); //Clear session data
        sharedPrefs.edit().putString(USERNAME, guestName).apply();
        sharedPrefs.edit().putInt(USER_ID, -1).apply();
        sharedPrefs.edit().putBoolean(LOGGED_IN, false).apply(); //User logs out
        draftsPrefs.edit().clear().apply(); //Clear saved drafts
        Timber.i("Session data cleared.");
    }

    private void setLoginScreenAsDefault(boolean b){
        sharedPrefs.edit().putBoolean(LOGIN_SCREEN_AS_DEFAULT, b).apply();
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

    private String extractSesc(@NonNull Document doc) {
        Elements logoutLink = doc.select("a[href^=https://www.thmmy.gr/smf/index.php?action=logout;sesc=]");
        if (!logoutLink.isEmpty()) {
            String link = logoutLink.first().attr("href");
            return extractSescFromLink(link);
        }
        Timber.e(new ParseException("Parsing failed(extractSesc)"),"ParseException");
        return null;
    }

    private String extractSescFromLink(String link){
        if (link != null){
            Pattern pattern = Pattern.compile(".+;sesc=(\\w+)");
            Matcher matcher = pattern.matcher(link);
            if (matcher.find())
                return matcher.group(1);
        }
        Timber.e(new ParseException("Parsing failed(extractSescFromLogoutLink)"),"ParseException");
        return null;
    }

    private String generateLogoutLink(String sesc){
        return baseLogoutLink + sesc;
    }

    private String generateMarkAllAsReadLink(String sesc){
        return baseMarkAllAsReadLink + sesc;
    }
    //----------------------------------OTHER FUNCTIONS END-----------------------------------------

}