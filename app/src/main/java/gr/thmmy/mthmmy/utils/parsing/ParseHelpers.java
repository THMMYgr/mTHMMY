package gr.thmmy.mthmmy.utils.parsing;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class consists exclusively of static classes (enums) and methods (excluding methods of inner
 * classes). It can be used to resolve a page's language and state or fix embedded videos html code
 * and obfuscated emails.
 */
public class ParseHelpers {

    /**
     * An enum describing a forum page's language by defining the types:<ul>
     * <li>{@link #PAGE_INCOMPLETE}</li>
     * <li>{@link #UNKNOWN_LANGUAGE}</li>
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
         * Page language could not be determined/ not (yet) supported.
         */
        UNKNOWN_LANGUAGE;

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
                if (welcomingGuest != null && welcomingGuest.text().contains("Welcome"))
                    return ENGLISH;
                return PAGE_INCOMPLETE;
            } else if (welcoming.text().contains("Καλώς ορίσατε")) return GREEK;
            else if (welcoming.text().contains("Hey")) return ENGLISH;
            else return UNKNOWN_LANGUAGE;
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
        if(stylesheet!=null){
            if (stylesheet.attr("href").contains("scribbles2"))
                return Theme.SCRIBBLES2;
            else if (stylesheet.attr("href").contains("helios_multi"))
                return Theme.HELIOS_MULTI;
            else if (stylesheet.attr("href").contains("smfone"))
                return Theme.SMFONE_BLUE;
            else if (stylesheet.attr("href").contains("default"))
                return Theme.SMF_DEFAULT;
        }
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


    public static String emojiTagToHtml(String emojiTagedString) {
        HashMap<Pattern, String> tagToHtmlMap = new HashMap<>();
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":)"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/smiley.gif\" alt=\"Smiley\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(";)"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/wink.gif\" alt=\"Wink\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":D"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/cheesy.gif\" alt=\"Cheesy\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(";D"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/grin.gif\" alt=\"Grin\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("&gt;:("), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/angry.gif\" alt=\"Angry\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":("), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/sad.gif\" alt=\"Sad\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":o"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/shocked.gif\" alt=\"Shocked\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("8))"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/cool.gif\" alt=\"Cool\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":???:"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/huh.gif\" alt=\"Huh\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":P"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/tongue.gif\" alt=\"Tongue\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":-["), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/embarrassed.gif\" alt=\"Embarrassed\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":-X"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/lipsrsealed.gif\" alt=\"Lips Sealed\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":-\\"), Pattern.MULTILINE), "<img src =\"https://www.thmmy.gr/smf/Smileys/default_dither/undecided.gif\" alt=\"Undecided\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":-*"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/kiss.gif\" alt=\"Kiss\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":'("), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/cry.gif\" alt=\"Cry\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("&lt;3"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/heart.gif\" alt=\"heart\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^locked^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/locked.gif\" alt=\"kleidaria\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^rollover^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/roll_over.gif\" alt=\"roll_over\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^redface^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/redface.gif\" alt=\"redface\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^confused^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/confused.gif\" alt=\"confused\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^innocent^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/innocent.gif\" alt=\"innocent\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^sleep^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/sleep.gif\" alt=\"sleep\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^sealed^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/lips_sealed.gif\" alt=\"lips_sealed\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^cool^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/cool.bmp\" alt=\"cool\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^crazy^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/crazy.jpg\" alt=\"crazy\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^mad^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/mad.jpg\" alt=\"mad\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^wav^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/wav.gif\" alt=\"wav\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^binkybaby^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/binkybaby.gif\" alt=\"BinkyBaby\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^Police^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/Police.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^dontknow^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/dontknow.gif\" alt=\"DontKnow\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote(":angry4:"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/angry4.gif\" alt=\"angry4\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^angryhot^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/angry_hot.gif\" alt=\"angryAndHot\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^angry^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/angry.gif\" alt=\"angry\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^fouska^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/foyska.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^nysta^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/nista.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^sfinaki^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/10_7_3.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^banghead^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/BangHead.gif\" alt=\"bang_head\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^crybaby^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/crybaby.gif\" alt=\"CryBaby\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^hello^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/hello.gif\" alt=\"Hello\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^jerk^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/jerk.gif\" alt=\"jerk\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^nono^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/nono.gif\" alt=\"NoNo\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^notworthy^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/notworthy.gif\" alt=\"NotWorthy\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^off-topic^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/off -topic.gif\" alt=\"Off-topic\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^puke^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/puke.gif\" alt=\"Puke\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^shout^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/shout.gif\" alt=\"Shout\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^slurp^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/slurp.gif\" alt=\"Slurp\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^superconfused^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/superconfused.gif\" alt=\"SuperConfused\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^superinnocent^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/superinnocent.gif\" alt=\"SuperInnocent\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^cellPhone^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/cellPhone.gif\" alt=\"CellPhone\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^idiot^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/idiot.gif\" alt=\"Idiot\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^knuppel^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/knuppel.gif\" alt=\"Knuppel\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^tickedOff^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/tickedoff.gif\" alt=\"TickedOff\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^peace^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/peace.gif\" alt=\"Peace\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^suspicious^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/suspicious.gif\" alt=\"Suspicious\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^caffine^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/caffine.gif\" alt=\"Caffine\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^argue^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/argue.gif\" alt=\"argue\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^banned2^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/banned2.gif\" alt=\"banned2\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^banned^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/banned.gif\" alt=\"banned\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^bath^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/bath.gif\" alt=\"bath\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^beg^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/beg.gif\" alt=\"beg\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^bluescreen^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/bluescreen.gif\" alt=\"bluescreen\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^boil^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/boil.gif\" alt=\"boil\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^bye^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/bye.gif\" alt=\"bye\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^callmerip^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/callmerip.gif\" alt=\"callmerip\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^carnaval^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/carnaval.gif\" alt=\"carnaval\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^clap^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/clap.gif\" alt=\"clap\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^coffepot^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/coffeepot.gif\" alt=\"coffepot\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^crap^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/crap.gif\" alt=\"crap\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^curses^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/curses.gif\" alt=\"curses\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^funny^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/funny.gif\" alt=\"funny\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^guitar^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/guitar1.gif\" alt=\"guitar\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^kissy^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/icon_kissy.gif\" alt=\"kissy\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^band^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/band.gif\" alt=\"band\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^ivres^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/ivres.gif\" alt=\"ivres\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^kaloe^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/kaloe.gif\" alt=\"kaloe\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^kremala^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/kremala.gif\" alt=\"kremala\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^moon^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/moon.gif\" alt=\"moon\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^mopping^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/mopping.gif\" alt=\"mopping\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^mountza^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/mountza.gif\" alt=\"mountza\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^pcsleep^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/pcsleep.gif\" alt=\"pcsleep\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^pinokio^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/pinokio.gif\" alt=\"pinokio\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^poke^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/poke.gif\" alt=\"poke\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^seestars^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/seestars.gif\" alt=\"seestars\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^sfyri^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/sfyri.gif\" alt=\"sfyri\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^spam^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/spam2.gif\" alt=\"spam\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^super^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/super.gif\" alt=\"super\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^tafos^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/tafos.gif\" alt=\"tafos\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^tomato^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/tomatomourh.gif\" alt=\"tomato\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^ytold^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/ytold.gif\" alt=\"ytold\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^beer^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/beer2.gif\" alt=\"beer\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^yue^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/yu.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^eatpaper^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/a-eatpaper.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^fritz^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/fritz.gif\" alt=\"ο fritz!!!\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^wade^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/wade.gif\" alt=\"o Wade!!!\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^lypi^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/lypi.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^aytoxeir^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/megashok1wq.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^victory^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/victory.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^filarakia^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/filarakia.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^hat^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/bonjour-97213.gif\" alt=\"bonjour\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^miss^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/curtseyqi9.gif\" alt=\"bonjour2\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^rolfmao^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/rofl.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^lock^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/locked.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^que^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/question.gif\" alt=\"question\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^shifty^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/shifty.gif\" alt=\"shifty\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^shy^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/shy.png\" alt=\"shy\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^music_listen^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/music.gif\" alt=\"music_listenning\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^bagface^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/shamed_bag.jpg\" alt=\"bag_face\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^rotate^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/rotfl.gif\" alt=\"rotation\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^love^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/love.jpg\" alt=\"love\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^speech^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/speech.gif\" alt=\"speech\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^facepalm^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/facepalm.gif\" alt=\"\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^shocked^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/shocked.png\" alt=\"shocked\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^ex_shocked^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/extremely_shocked.png\" alt=\"extremely_shocked\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^smurf^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/smurf.gif\" alt=\"smurf\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^monster^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/monster.bmp\" alt=\"monster\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^pig^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/noffe.gif\" alt=\"pig\" border=\"0\">");
        tagToHtmlMap.put(Pattern.compile(Pattern.quote("^lol^"), Pattern.MULTILINE), "<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/LoL.jpg\" alt=\"lol\" border=\"0\">");

        //Needs priority over the rest tags
        final Pattern pattern = Pattern.compile(Pattern.quote("::)"), Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(emojiTagedString);
        emojiTagedString = matcher.replaceAll("<img src=\"https://www.thmmy.gr/smf/Smileys/default_dither/rolleyes.gif\" alt=\"Roll Eyes\" border=\"0\">");

        for (Pattern patternKey : tagToHtmlMap.keySet()) {
            matcher = patternKey.matcher(emojiTagedString);
            emojiTagedString = matcher.replaceAll(tagToHtmlMap.get(patternKey));
        }

        return emojiTagedString;
    }
}
