package gr.thmmy.mthmmy.activities.profile;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import mthmmy.utils.Report;

class ProfileParser {
    //Other variables
    @SuppressWarnings("unused")
    private static final String TAG = "ProfileParser";
    static final int THUMBNAIL_URL = 0;
    static final int NAME_INDEX = 1;
    static final int PERSONAL_TEXT_INDEX = 2;

    static ArrayList<String> parseProfile(Document doc) {
        //Method's variables
        ArrayList<String> returnArray = new ArrayList<>();

        //Contains all summary's rows
        Elements summaryRows = doc.select(".bordercolor > tbody:nth-child(1) > tr:nth-child(2) tr");

        { //Find thumbnail url
            Element tmpEl = doc.select(".bordercolor img.avatar").first();
            if (tmpEl != null)
                returnArray.add(THUMBNAIL_URL, tmpEl.attr("abs:src"));
            else //User doesn't have an avatar
                returnArray.add(THUMBNAIL_URL, null);
        }

        { //Find username
            Element tmpEl = summaryRows.first();
            if (tmpEl != null) {
                returnArray.add(NAME_INDEX, tmpEl.select("td").get(1).text());
            } else {
                //Should never get here!
                //Something is wrong.
                Report.e(TAG, "An error occurred while trying to find profile's username.");
            }
        }

        { //Find personal text
            String tmpPersonalText = doc.select("td.windowbg:nth-child(2)").first().text().trim();
            returnArray.add(PERSONAL_TEXT_INDEX, tmpPersonalText);
        }

        for (Element row : summaryRows) {
            String rowText = row.text(), pHtml = "";

            if (row.select("td").size() == 1)
                pHtml = "";
            else if (rowText.contains("Signature") || rowText.contains("Υπογραφή")) {
                { //Fix embedded videos
                    Elements noembedTag = row.select("noembed");
                    ArrayList<String> embededVideosUrls = new ArrayList<>();

                    for (Element _noembed : noembedTag) {
                        embededVideosUrls.add(_noembed.text().substring(_noembed.text()
                                        .indexOf("href=\"https://www.youtube.com/watch?") + 38
                                , _noembed.text().indexOf("target") - 2));
                    }

                    pHtml = row.html();

                    int tmp_counter = 0;
                    while (pHtml.contains("<embed")) {
                        if (tmp_counter > embededVideosUrls.size())
                            break;
                        pHtml = pHtml.replace(
                                pHtml.substring(pHtml.indexOf("<embed"), pHtml.indexOf("/noembed>") + 9)
                                , "<div class=\"embedded-video\">"
                                        + "<a href=\"https://www.youtube.com/"
                                        + embededVideosUrls.get(tmp_counter) + "\" target=\"_blank\">"
                                        + "<img src=\"https://img.youtube.com/vi/"
                                        + embededVideosUrls.get(tmp_counter) + "/default.jpg\" alt=\"\" border=\"0\">"
                                        + "</a>"
                                        //+ "<img class=\"embedded-video-play\" src=\"http://www.youtube.com/yt/brand/media/image/YouTube_light_color_icon.png\">"
                                        + "</div>");
                    }
                }

                //Add stuff to make it work in WebView
                //style.css
                pHtml = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + pHtml);
            } else if (!rowText.contains("Name") && !rowText.contains("Όνομα")) {
                if (Objects.equals(row.select("td").get(1).text(), ""))
                    continue;
                pHtml = "<b>" + row.select("td").first().text() + "</b> "
                        + row.select("td").get(1).text();
            }
            returnArray.add(pHtml);
        }
        return returnArray;
    }
}
