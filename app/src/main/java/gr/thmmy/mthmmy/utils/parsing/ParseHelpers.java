package gr.thmmy.mthmmy.utils.parsing;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * This class consists exclusively of static classes (enums) and methods (excluding methods of inner
 * classes). It can be used to resolve a page's language and state or fix embedded videos html code
 * and obfuscated emails.
 */
public class ParseHelpers {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "ParseHelpers";

    /**
     * An enum describing a forum page's language by defining the types:<ul>
     * <li>{@link #PAGE_INCOMPLETE}</li>
     * <li>{@link #UNDEFINED_LANGUAGE}</li>
     * <li>{@link #ENGLISH}</li>
     * <li>{@link #ENGLISH_GUEST}</li>
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
         * Page language is english and the user is guest.
         */
        ENGLISH_GUEST,
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
                    if (welcomingGuest.text().contains("Welcome")) return ENGLISH_GUEST;
                }
                return PAGE_INCOMPLETE;
            } else if (welcoming.text().contains("Καλώς ορίσατε")) return GREEK;
            else if (welcoming.text().contains("Hey")) return ENGLISH;
            else return UNDEFINED_LANGUAGE;
        }

        /**
         * This method defines a custom equality check for {@link Language} enums.
         * <p>Method returns true if parameter's Target is the same as the object and in the specific
         * cases described below, false otherwise.</p><ul>
         * <li>{@link #ENGLISH}.is({@link #ENGLISH_GUEST}) returns true</li>
         * <li>{@link #ENGLISH_GUEST}.is({@link #ENGLISH}) returns true</li>
         *
         * @param other another Language
         * @return true if <b>enums</b> are equal, false otherwise
         */
        public boolean is(Language other) {
            return this == ENGLISH && other == ENGLISH_GUEST
                    || this == ENGLISH_GUEST && other == ENGLISH
                    || this == other;
        }
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
        deobfuscateElements(document.select("span.__cf_email__"), true);
        return document;
    }

    /**
     * Use this method instead of parse() if you are targetting specific elements
     */
    public static void deobfuscateElements(Elements elements, boolean found){
        if(!found)
            elements = elements.select("span.__cf_email__");

        for (Element obfuscatedElement : elements) {
            String deobfuscatedEmail = deobfuscateEmail(obfuscatedElement.attr("data-cfemail"));
            Element parent = obfuscatedElement.parent();
            if (parent.is("a")&&parent.attr("href").contains("email-protection"))
                parent.attr("href", "mailto:"+deobfuscatedEmail);
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
