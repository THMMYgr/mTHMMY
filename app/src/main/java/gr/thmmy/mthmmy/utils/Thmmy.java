package gr.thmmy.mthmmy.utils;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.activities.BaseActivity;
import okhttp3.Cookie;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class Thmmy
{
    private static final HttpUrl loginUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php?action=login2");
    private static final HttpUrl indexUrl = HttpUrl.parse("https://www.thmmy.gr/smf/index.php");

    public static final int LOGGED_OUT = 0;
    public static final int LOGGED_IN = 1;
    public static final int WRONG_USER= 2;
    public static final int WRONG_PASSWORD= 3;
    public static final int FAILED= 4;
    public static final int CERTIFICATE_ERROR = 5;
    public static final int OTHER_ERROR = 6;




    //-------------------------------------------LOGIN--------------------------------------------------
    //Two options: (username, password, duration) or nothing - cookies
    public static LoginData login(String... strings)
    {
        Log.d("Login","Logging in...");
        LoginData loginData = new LoginData();
        Request request;

        if(strings.length==3)
        {
            String loginName = strings[0];
            String password = strings[1];
            String duration = strings[2];

            ((PersistentCookieJar) BaseActivity.getCookieJar()).clear();

            RequestBody formBody = new FormBody.Builder()
                    .add("user", loginName)
                    .add("passwrd", password)
                    .add("cookielength", duration)  //Forever is -1
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

        OkHttpClient client = BaseActivity.getClient();

        try
        {
            Response response = client.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());

            Element logout = document.getElementById("logoutbtn");

            if (logout != null)
            {
                Log.i("Login", "Login successful");
                setPersistentCookieSession();
                loginData.setUsername(extractUserName(document));
                loginData.setLogoutLink(HttpUrl.parse(logout.attr("href")));
                loginData.setStatus(LOGGED_IN);
            }
            else
            {
                Log.w("Login", "Login failed");
                loginData.setStatus(FAILED);

                //Making error more specific
                Elements error = document.select("b:contains(That username does not exist.)");

                if (error.size()==1)
                {
                    loginData.setStatus(WRONG_USER);
                    Log.d("Login","Wrong Username");
                }

                error = document.select("body:contains(Password incorrect)");
                if (error.size()==1)
                {
                    Log.d("Login","Wrong Password");
                    loginData.setStatus(WRONG_PASSWORD);
                }

                ((PersistentCookieJar) BaseActivity.getCookieJar()).clear();

            }
        } catch (SSLHandshakeException e) {
            Log.w("Login", "Certificate problem");
            loginData.setStatus(CERTIFICATE_ERROR);

        } catch (Exception e) {
            Log.e("Login", "Error", e);
            loginData.setStatus(OTHER_ERROR);
        }



        return loginData;


    }

    //To maintain data between activities/ between activity state change (possibly temporary solution)
    public static class LoginData implements Parcelable
    {
        private int status;
        private String username;
        private HttpUrl logoutLink;

        public LoginData() {}

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }


        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }


        public HttpUrl getLogoutLink() {
            return logoutLink;
        }

        public void setLogoutLink(HttpUrl logoutLink) {
            this.logoutLink = logoutLink;
        }


        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(status);
            out.writeString(username);
            out.writeString(logoutLink.toString());
        }

        public static final Parcelable.Creator<LoginData> CREATOR
                = new Parcelable.Creator<LoginData>() {
            public LoginData createFromParcel(Parcel in) {
                return new LoginData(in);
            }

            public LoginData[] newArray(int size) {
                return new LoginData[size];
            }
        };

        private LoginData(Parcel in) {
            status = in.readInt();
            username=in.readString();
            logoutLink=HttpUrl.parse(in.readString());
        }
    }

    private static boolean setPersistentCookieSession()
    {
        List<Cookie> cookieList = BaseActivity.getCookieJar().loadForRequest(HttpUrl.parse("https://www.thmmy.gr"));

        if (cookieList.size() == 2) {
            if ((cookieList.get(0).name().equals("THMMYgrC00ki3")) && (cookieList.get(1).name().equals("PHPSESSID")))
            {
                Cookie.Builder builder = new Cookie.Builder();
                builder.name(cookieList.get(1).name())
                        .value(cookieList.get(1).value())
                        .domain(cookieList.get(1).domain())
                        .expiresAt(cookieList.get(0).expiresAt());
                cookieList.remove(1);
                cookieList.add(builder.build());
                BaseActivity.getSharedPrefsCookiePersistor().clear();
                BaseActivity.getSharedPrefsCookiePersistor().saveAll(cookieList);
                return true;
            }
        }
        return false;
    }
    //-------------------------------------LOGIN ENDS-----------------------------------------------



    //--------------------------------------LOGOUT--------------------------------------------------
    public static int logout(LoginData loginData)
    {
        OkHttpClient client = BaseActivity.getClient();
        Request request = new Request.Builder()
                .url(loginData.getLogoutLink())
                .build();

        try {
            Response response = client.newCall(request).execute();
            Document document = Jsoup.parse(response.body().string());

            Elements login =  document.select("[value=Login]");
            ((PersistentCookieJar) BaseActivity.getCookieJar()).clear();
            if(!login.isEmpty())
            {
                Log.i("Logout", "Logout successful");
                loginData.setStatus(LOGGED_OUT);
                return LOGGED_OUT;
            }
            else
            {
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




//----------------------------------------LOGOUT ENDS-----------------------------------------------




//-------------------------------------------MISC---------------------------------------------------
    public static String extractUserName(Document doc)
    {
        if(doc!=null)
        {
            Elements user = doc.select("div[id=myuser] > h3");

            if (user.size()==1)
            {
                String txt = user.first().ownText();

                Pattern pattern = Pattern.compile(", (.*?),");
                Matcher matcher = pattern.matcher(txt);
                if (matcher.find())
                    return matcher.group(1);
            }
        }

        return null;
    }

}
