package gr.thmmy.mthmmy.utils;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ParseHelpers {
    private static final String TAG = "ParseHelpers";

    public enum Language {
        GREEK, ENGLISH, ENGLISH_GUEST, PAGE_INCOMPLETE, UNDEFINED_LANGUAGE;

        /**
         * Returns one of the supported forum languages.
         * <p>Forum supports: <ul><li>{@link #ENGLISH}</li>
         * <li>{@link #GREEK}</li></ul></p>
         *
         * @param page {@link Document} object containing this page's source code
         * @return String containing the language of a topic
         * @see org.jsoup.Jsoup Jsoup
         */
        public static Language getLanguage(Document page) {
            Element welcoming = page.select("h3").first();
            if (welcoming == null) {
                if (page.select("div[id=myuser]").first().text().contains("Welcome"))
                    return ENGLISH_GUEST;
                return PAGE_INCOMPLETE;
            } else if (welcoming.text().contains("Καλώς ορίσατε")) return GREEK;
            else if (welcoming.text().contains("Hey")) return ENGLISH;
            else return UNDEFINED_LANGUAGE;
        }

        public boolean is(Language other) {
            return this == ENGLISH && other == ENGLISH_GUEST ||
                    this == ENGLISH_GUEST && other == ENGLISH ||
                    this == other;
        }
    }

    public enum State {
        UNAUTHORIZED_OR_MISSING, NEW_PM, READY;

        public static State getState(Document page) {
            Elements warnings = page.select("form[id=frmLogin] tr.catbg~tr>td.windowbg");
            if (warnings != null) {
                for (Element warning : warnings) {
                    if (warning.text().contains("The topic or board you are looking for appears " +
                            "to be either missing or off limits to you."))
                        return UNAUTHORIZED_OR_MISSING;
                }
            }
            return READY;
        }
    }

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
                            + "<a href=\"https://www.youtube.com/"
                            + embededVideosUrls.get(tmp_counter) + "\" target=\"_blank\">"
                            + "<img class=\"embedded-video-play\" "
                            + "src=\"http://www.youtube.com/yt/brand/media/image/YouTube_light_color_icon.png\""
                            + "</a>"
                            + "<img src=\"https://img.youtube.com/vi/"
                            + embededVideosUrls.get(tmp_counter)
                            + "/default.jpg\" alt=\"\" border=\"0\" width=\"40%\">"
                            + "</div>");
        }
        return fixed;
    }
}
