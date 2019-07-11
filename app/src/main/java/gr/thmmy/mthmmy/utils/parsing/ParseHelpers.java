package gr.thmmy.mthmmy.utils.parsing;

import android.graphics.Color;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.base.BaseActivity;
import timber.log.Timber;

/**
 * This class consists exclusively of static classes (enums) and methods (excluding methods of inner
 * classes). It can be used to resolve a page's language, number of pages and state or fix embedded videos html code
 * and obfuscated emails.
 */
public class ParseHelpers {

    public static final int USER_COLOR_PINK = Color.parseColor("#FF4081");
    public static final int USER_COLOR_YELLOW = Color.parseColor("#FFEB3B");
    public static final int USER_COLOR_WHITE = Color.WHITE;
    //User colors
    private static final int USER_COLOR_BLACK = Color.parseColor("#000000");
    private static final int USER_COLOR_RED = Color.parseColor("#F44336");
    private static final int USER_COLOR_GREEN = Color.parseColor("#4CAF50");
    private static final int USER_COLOR_BLUE = Color.parseColor("#536DFE");
    public static Pattern mentionsPattern = Pattern.
            compile("<div class=\"quoteheader\">\\n\\s+?<a href=.+?>(Quote from|Παράθεση από): "
                    + BaseActivity.getSessionManager().getUsername() +"\\s(στις|on)");

    /**
     * Returns the color of a user according to user's rank on forum.
     *
     * @param starsUrl String containing the URL of a user's stars
     * @return an int corresponding to the right color
     */
    public static int colorPicker(String starsUrl) {
        if (starsUrl.contains("/star.gif"))
            return USER_COLOR_YELLOW;
        else if (starsUrl.contains("/starmod.gif"))
            return USER_COLOR_GREEN;
        else if (starsUrl.contains("/stargmod.gif"))
            return USER_COLOR_BLUE;
        else if (starsUrl.contains("/staradmin.gif"))
            return USER_COLOR_RED;
        else if (starsUrl.contains("/starweb.gif"))
            return USER_COLOR_BLACK;
        else if (starsUrl.contains("/oscar.gif"))
            return USER_COLOR_PINK;
        return USER_COLOR_YELLOW;
    }

    /**
     * An enum describing a forum page's language by defining the types:<ul>
     * <li>{@link #PAGE_INCOMPLETE}</li>
     * <li>{@link #UNDEFINED_LANGUAGE}</li>
     * <li>{@link #ENGLISH}</li>
     * <li>{@link #GREEK}</li>
     * </ul>
     */
    public enum Language {
        /**
         * Page language is greek.
         */
        GREEK,
        /**
         * Page language is english.
         */
        ENGLISH,
        /**
         * Page is incomplete. Data are not enough to determine the language.
         */
        PAGE_INCOMPLETE,
        /**
         * Page language is not (yet) supported.
         */
        UNDEFINED_LANGUAGE;

        /**
         * Returns one of the supported forum languages.
         *
         * @param page {@link Document} object containing this page's source code
         * @return language of this page
         * @see org.jsoup.Jsoup Jsoup
         */
        public static Language getLanguage(Document page) {
            Element welcoming = page.select("h3").first();
            if (welcoming == null) {
                Element welcomingGuest = page.select("div[id=myuser]").first();
                if (welcomingGuest != null) {
                    if (welcomingGuest.text().contains("Welcome")) return ENGLISH;
                }
                return PAGE_INCOMPLETE;
            } else if (welcoming.text().contains("Καλώς ορίσατε")) return GREEK;
            else if (welcoming.text().contains("Hey")) return ENGLISH;
            else return UNDEFINED_LANGUAGE;
        }
    }

    public enum Theme {
        SCRIBBLES2,
        SMF_DEFAULT,
        SMFONE_BLUE,
        HELIOS_MULTI,
        THEME_UNKNOWN
    }

    public static Theme parseTheme(Document page) {
        Element stylesheet = page.select("link[rel=stylesheet]").first();
        if (stylesheet.attr("href").contains("scribbles2"))
            return Theme.SCRIBBLES2;
        else if (stylesheet.attr("href").contains("helios_multi"))
            return Theme.HELIOS_MULTI;
        else if (stylesheet.attr("href").contains("smfone"))
            return Theme.SMFONE_BLUE;
        else if (stylesheet.attr("href").contains("default"))
            return Theme.SMF_DEFAULT;
        else
            return Theme.THEME_UNKNOWN;
    }

    /**
     * An enum describing the state of a forum page by defining the types:<ul>
     * <li>{@link #UNAUTHORIZED_OR_MISSING}</li>
     * <li>{@link #NEW_PM}</li>
     * <li>{@link #READY}</li>
     * </ul>
     */
    public enum State {
        /**
         * This page is either missing or is off limits.
         */
        UNAUTHORIZED_OR_MISSING,
        /**
         * The page has a popup window from a new personal message.
         */
        NEW_PM,
        /**
         * The page is ready for use.
         */
        READY;

        /**
         * This method checks the state of a page.
         *
         * @param page a {@link Document} containing this page's source code
         * @return page's State
         */
        public static State getState(Document page) {
            Elements warnings = page.select("form[id=frmLogin] tr.catbg~tr>td.windowbg");
            if (warnings != null) {
                for (Element warning : warnings) {
                    if (warning.text().contains("The topic or board you are looking for appears " +
                            "to be either missing or off limits to you."))
                        return UNAUTHORIZED_OR_MISSING;
                    else if (warning.text().contains("Το θέμα ή πίνακας που ψάχνετε ή δεν υπάρχει ή " +
                            "δεν είναι προσβάσιμο από εσάς. "))
                        return UNAUTHORIZED_OR_MISSING;
                }
            }
            return READY;
        }
    }

    /**
     * This method fixes html so that embedded videos will render properly and be lightweight.
     *
     * @param html an {@link Element} containing the (outer) html to be fixed
     * @return fixed html String
     */
    public static String youtubeEmbeddedFix(Element html) {
        //Fixes embedded videos
        Elements noembedTag = html.select("noembed");
        ArrayList<String> embededVideosUrls = new ArrayList<>();

        for (Element _noembed : noembedTag) {
            embededVideosUrls.add(_noembed.text().substring(_noembed.text()
                            .indexOf("href=\"https://www.youtube.com/watch?") + 38
                    , _noembed.text().indexOf("target") - 2));
        }

        String fixed = html.outerHtml();
        int tmp_counter = 0;
        while (fixed.contains("<embed")) {
            if (tmp_counter > embededVideosUrls.size())
                break;
            fixed = fixed.replace(
                    fixed.substring(fixed.indexOf("<embed"), fixed.indexOf("/noembed>") + 9)
                    , "<div class=\"yt\">"
                            + "<a href=\"https://www.youtube.com/watch?v="
                            + embededVideosUrls.get(tmp_counter) + "\" target=\"_blank\">"
                            + "<img class=\"embedded-video-play\" "
                            + "src=\"YouTube_light_color_icon.png\">"
                            + "</a>"
                            + "<img src=\"https://img.youtube.com/vi/"
                            + embededVideosUrls.get(tmp_counter)
                            + "/default.jpg\" alt=\"\" border=\"0\" width=\"40%\">"
                            + "</div>");
            ++tmp_counter;
        }
        return fixed;
    }

    /**
     * Returns the number of this page's pages.
     *
     * @param topic       {@link Document} object containing this page's source code
     * @param currentPage an int containing current page of this page
     * @param language    a {@link ParseHelpers.Language} containing this topic's
     *                    language set, this is returned by
     *                    {@link ParseHelpers.Language#getLanguage(Document)}
     * @return int containing the number of pages
     * @see org.jsoup.Jsoup Jsoup
     */
    public static int parseNumberOfPages(Document topic, int currentPage, ParseHelpers.Language language) {
        int returnPages = 1;

        if (language == ParseHelpers.Language.GREEK) {
            Elements pages = topic.select("td:contains(Σελίδες:)>a.navPages");

            if (pages.size() != 0) {
                returnPages = currentPage;
                for (Element item : pages) {
                    if (Integer.parseInt(item.text()) > returnPages)
                        returnPages = Integer.parseInt(item.text());
                }
            }
        } else {
            Elements pages = topic.select("td:contains(Pages:)>a.navPages");

            if (pages.size() != 0) {
                returnPages = currentPage;
                for (Element item : pages) {
                    if (Integer.parseInt(item.text()) > returnPages)
                        returnPages = Integer.parseInt(item.text());
                }
            }
        }

        return returnPages;
    }

    /**
     * Returns current pages's page index.
     *
     * @param topic    {@link Document} object containing this page's source code
     * @param language a {@link ParseHelpers.Language} containing this page's
     *                 language set, this is returned by
     *                 {@link ParseHelpers.Language#getLanguage(Document)}
     * @return int containing parsed topic's current page
     * @see org.jsoup.Jsoup Jsoup
     */
    public static int parseCurrentPageIndex(Document topic, ParseHelpers.Language language) {
        int parsedPage = 1;

        if (language == ParseHelpers.Language.GREEK) {
            Elements findCurrentPage = topic.select("td:contains(Σελίδες:)>b");

            for (Element item : findCurrentPage) {
                if (!item.text().contains("...")
                        && !item.text().contains("Σελίδες:")) {
                    parsedPage = Integer.parseInt(item.text());
                    break;
                }
            }
        } else {
            Elements findCurrentPage = topic.select("td:contains(Pages:)>b");

            for (Element item : findCurrentPage) {
                if (!item.text().contains("...") && !item.text().contains("Pages:")) {
                    parsedPage = Integer.parseInt(item.text());
                    break;
                }
            }
        }

        return parsedPage;
    }

    /**
     * Method that extracts the base URL from a topic's page URL. For example a topic with url similar to
     * "https://www.thmmy.gr/smf/index.php?topic=1.15;topicseen" or
     * "https://www.thmmy.gr/smf/index.php?topic=1.msg1#msg1"
     * has the base url "https://www.thmmy.gr/smf/index.php?topic=1"
     *
     * @param topicURL a topic's page URL
     * @return the base URL of the given topic
     */
    public static String getBaseURL(String topicURL) {
        String forumUrl = "https://www.thmmy.gr/smf/index.php?";
        Matcher baseUrlMatcher = Pattern.compile("topic=[0-9]+").matcher(topicURL);
        if (baseUrlMatcher.find())
            return forumUrl + topicURL.substring(baseUrlMatcher.start(), baseUrlMatcher.end());
        else return "";
    }

    /**
     * Method that replaces CloudFlare-obfuscated emails with deobfuscated ones
     * Replace Jsoup.parse with this wherever needed
     *
     * @param html html to parse
     * @return a document with deobfuscated emails
     */
    public static Document parse(String html){
        Document document = Jsoup.parse(html);
        deobfuscateElements(document.select("span.__cf_email__,a.__cf_email__"), true);
        return document;
    }

    /**
     * Use this method instead of parse() if you are targeting specific elements
     */
    public static void deobfuscateElements(Elements elements, boolean found){
        if(!found)
            elements = elements.select("span.__cf_email__,a.__cf_email__");

        for (Element obfuscatedElement : elements) {
            String deobfuscatedEmail = deobfuscateEmail(obfuscatedElement.attr("data-cfemail"));
            if(obfuscatedElement.is("span")){
                Element parent = obfuscatedElement.parent();
                if (parent.is("a")&&parent.attr("href").contains("email-protection"))
                    parent.attr("href", "mailto:"+deobfuscatedEmail);
            }
            else if (obfuscatedElement.attr("href").contains("email-protection"))
                obfuscatedElement.attr("href", "mailto:"+deobfuscatedEmail);

            obfuscatedElement.replaceWith(new TextNode(deobfuscatedEmail, ""));
        }
    }


    /**
     * @param obfuscatedEmail CloudFlare-obfuscated email
     * @return deobfuscated email
     */
    private static String deobfuscateEmail(String obfuscatedEmail){
        //Deobfuscate
        final StringBuilder stringBuilder = new StringBuilder();
        final int r = Integer.parseInt(obfuscatedEmail.substring(0, 2), 16);
        for (int n = 2; n < obfuscatedEmail.length(); n += 2) {
            final int i = Integer.parseInt(obfuscatedEmail.substring(n, n + 2), 16) ^ r;
            stringBuilder.append(Character.toString((char) i));
        }

        Timber.i("Email deobfuscated.");
        return stringBuilder.toString();
    }
}
