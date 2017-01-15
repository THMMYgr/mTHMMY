package gr.thmmy.mthmmy.utils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

public class ParseHelpers {
    private static final String TAG = "ParseHelpers";

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
