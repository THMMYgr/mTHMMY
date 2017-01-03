package gr.thmmy.mthmmy.activities.profile;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import mthmmy.utils.Report;

import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.PACKAGE_NAME;

/**
 * Singleton used for parsing user's profile.
 * <p>Class contains the methods:<ul><li>{@link #parseProfileSummary(Document)}</li>
 * </ul></p>
 */
class ProfileParser {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "ProfileParser";
    /**
     * Index of user's thumbnail url in parsed information ArrayList
     * <p><b>Not the url itself!</b></p>
     */
    static final int THUMBNAIL_URL_INDEX = 0;
    /**
     * Index of user's username in parsed information ArrayList
     * <p><b>Not the username itself!</b></p>
     */
    static final int USERNAME_INDEX = 1;
    /**
     * Index of user's personal text in parsed information ArrayList
     * <p><b>Not the text itself!</b></p>
     */
    static final int PERSONAL_TEXT_INDEX = 2;

    /**
     * Returns an {@link ArrayList} of {@link String}s. This method is used to parse all available
     * information in a user profile.
     * <p>
     * User's thumbnail image url, username and personal text are placed at Array's indexes defined
     * by public constants THUMBNAIL_URL_INDEX, USERNAME_INDEX and PERSONAL_TEXT_INDEX respectively.
     *
     * @param profile {@link Document} object containing this profile's source code
     * @return ArrayList containing this profile's parsed information
     * @see org.jsoup.Jsoup Jsoup
     */
    static ArrayList<String> parseProfileSummary(Document profile) {
        //Method's variables
        ArrayList<String> parsedInformation = new ArrayList<>();

        //Contains all summary's rows
        Elements summaryRows = profile.select(".bordercolor > tbody:nth-child(1) > tr:nth-child(2) tr");

        { //Finds thumbnail's url
            Element tmpEl = profile.select(".bordercolor img.avatar").first();
            if (tmpEl != null)
                parsedInformation.add(THUMBNAIL_URL_INDEX, tmpEl.attr("abs:src"));
            else //User doesn't have an avatar
                parsedInformation.add(THUMBNAIL_URL_INDEX, null);
        }

        { //Finds username
            Element tmpEl = summaryRows.first();
            if (tmpEl != null) {
                parsedInformation.add(USERNAME_INDEX, tmpEl.select("td").get(1).text());
            } else {
                //Should never get here!
                //Something is wrong.
                Report.e(PACKAGE_NAME + "." + TAG, "An error occurred while trying to find profile's username.");
                parsedInformation.add(USERNAME_INDEX, null);
            }
        }

        { //Finds personal text
            Element tmpEl = profile.select("td.windowbg:nth-child(2)").first();
            if (tmpEl != null) {
                String tmpPersonalText = tmpEl.text().trim();
                parsedInformation.add(PERSONAL_TEXT_INDEX, tmpPersonalText);
            } else {
                //Should never get here!
                //Something is wrong.
                Report.e(PACKAGE_NAME + "." + TAG, "An error occurred while trying to find profile's personal text.");
                parsedInformation.add(PERSONAL_TEXT_INDEX, null);
            }
        }

        for (Element row : summaryRows) {
            String rowText = row.text(), pHtml = "";

            //Horizontal rule rows
            if (row.select("td").size() == 1)
                pHtml = "";
            else if (rowText.contains("Signature") || rowText.contains("Υπογραφή")) {
                //This needs special handling since it may have css
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
                }

                //Add stuff to make it work in WebView
                //style.css
                pHtml = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + pHtml);
            } else if (!rowText.contains("Name") && !rowText.contains("Όνομα")) { //Don't add username twice
                if (Objects.equals(row.select("td").get(1).text(), ""))
                    continue;
                //Style parsed information with html
                pHtml = "<b>" + row.select("td").first().text() + "</b> "
                        + row.select("td").get(1).text();
            }
            parsedInformation.add(pHtml);
        }
        return parsedInformation;
    }
}
