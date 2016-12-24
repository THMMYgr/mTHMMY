package gr.thmmy.mthmmy.activities.topic;

import android.graphics.Color;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.data.Post;

class TopicParser {
    //Languages supported
    private static final String LANGUAGE_GREEK = "Greek";
    private static final String LANGUAGE_ENGLISH = "English";

    //User colors variables
    private static final int USER_COLOR_BLACK = Color.parseColor("#000000");
    private static final int USER_COLOR_RED = Color.parseColor("#F44336");
    private static final int USER_COLOR_GREEN = Color.parseColor("#4CAF50");
    private static final int USER_COLOR_BLUE = Color.parseColor("#536DFE");
    private static final int USER_COLOR_PINK = Color.parseColor("#FF4081");
    private static final int USER_COLOR_YELLOW = Color.parseColor("#FFEB3B");

    @SuppressWarnings("unused")
    private static final String TAG = "TopicParser";

    static String parseUsersViewingThisTopic(Document doc, String language) {
        if (Objects.equals(language, LANGUAGE_GREEK))
            return doc.select("td:containsOwn(διαβάζουν αυτό το θέμα)").first().html();
        return doc.select("td:containsOwn(are viewing this topic)").first().html();
    }

    static int parseCurrentPageIndex(Document doc, String language) {
        int returnPage = 1;

        if (Objects.equals(language, LANGUAGE_GREEK)) {
            //Contains pages
            Elements findCurrentPage = doc.select("td:contains(Σελίδες:)>b");

            for (Element item : findCurrentPage) {
                if (!item.text().contains("...") //It's not "..."
                        && !item.text().contains("Σελίδες:")) { //Nor "Σελίδες:"
                    returnPage = Integer.parseInt(item.text());
                    break;
                }
            }
        } else {
            Elements findCurrentPage = doc.select("td:contains(Pages:)>b");

            for (Element item : findCurrentPage) {
                if (!item.text().contains("...") && !item.text().contains("Pages:")) {
                    returnPage = Integer.parseInt(item.text());
                    break;
                }
            }
        }

        return returnPage;
    }

    static int parseTopicNumberOfPages(Document doc, int thisPage, String language) {
        //Method's variables
        int returnPages = 1;

        if (Objects.equals(language, LANGUAGE_GREEK)) {
            //Contains all pages
            Elements pages = doc.select("td:contains(Σελίδες:)>a.navPages");

            if (pages.size() != 0) {
                returnPages = thisPage; //Initialize the number
                for (Element item : pages) { //Just a max
                    if (Integer.parseInt(item.text()) > returnPages)
                        returnPages = Integer.parseInt(item.text());
                }
            }
        } else {
            //Contains all pages
            Elements pages = doc.select("td:contains(Pages:)>a.navPages");

            if (pages.size() != 0) {
                returnPages = thisPage;
                for (Element item : pages) {
                    if (Integer.parseInt(item.text()) > returnPages)
                        returnPages = Integer.parseInt(item.text());
                }
            }
        }

        return returnPages;
    }

    static ArrayList<Post> parseTopic(Document doc, String language) {
        //Method's variables
        final int NO_INDEX = -1;
        ArrayList<Post> returnList = new ArrayList<>();
        Elements rows;

        if (Objects.equals(language, LANGUAGE_GREEK))
            rows = doc.select("form[id=quickModForm]>table>tbody>tr:matches(στις)");
        else {
            rows = doc.select("form[id=quickModForm]>table>tbody>tr:matches(on)");
        }

        for (Element item : rows) { //For every post
            //Variables to pass
            String p_userName, p_thumbnailUrl, p_subject, p_post, p_postDate, p_profileURL, p_rank,
                    p_specialRank, p_gender, p_personalText, p_numberOfPosts;
            int p_postNum, p_postIndex, p_numberOfStars, p_userColor;
            boolean p_isDeleted = false;
            ArrayList<String[]> p_attachedFiles;

            //Initialize variables
            p_profileURL = null;
            p_rank = "Rank";
            p_specialRank = "Special rank";
            p_gender = "";
            p_personalText = "";
            p_numberOfPosts = "";
            p_numberOfStars = 0;
            p_userColor = USER_COLOR_YELLOW;
            p_attachedFiles = new ArrayList<>();

            //Language independent parsing
            //Find thumbnail url
            Element thumbnailUrl = item.select("img.avatar").first();
            p_thumbnailUrl = null; //In case user doesn't have an avatar
            if (thumbnailUrl != null) {
                p_thumbnailUrl = thumbnailUrl.attr("abs:src");
            }

            //Find subject
            p_subject = item.select("div[id^=subject_]").first().select("a").first().text();

            //Find post's text
            p_post = item.select("div").select(".post").first().outerHtml();

            { //Fix embedded videos
                Elements noembedTag = item.select("div").select(".post").first().select("noembed");
                ArrayList<String> embededVideosUrls = new ArrayList<>();

                for (Element _noembed : noembedTag) {
                    embededVideosUrls.add(_noembed.text().substring(_noembed.text()
                                    .indexOf("href=\"https://www.youtube.com/watch?") + 38
                            , _noembed.text().indexOf("target") - 2));
                }

                int tmp_counter = 0;
                while (p_post.contains("<embed")) {
                    if (tmp_counter > embededVideosUrls.size())
                        break;
                    p_post = p_post.replace(
                            p_post.substring(p_post.indexOf("<embed"), p_post.indexOf("/noembed>") + 9)
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
            p_post = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + p_post);

            //Find post's index
            Element postIndex = item.select("a[name^=msg]").first();
            if (postIndex == null)
                p_postIndex = NO_INDEX;
            else {
                String tmp = postIndex.attr("name");
                p_postIndex = Integer.parseInt(tmp.substring(tmp.indexOf("msg") + 3));
            }

            //Language dependent parsing
            Element userName;
            if (Objects.equals(language, LANGUAGE_GREEK)) {
                //Find username
                userName = item.select("a[title^=Εμφάνιση προφίλ του μέλους]").first();
                if (userName == null) { //Deleted profile
                    p_isDeleted = true;
                    p_userName = item
                            .select("td:has(div.smalltext:containsOwn(Επισκέπτης))[style^=overflow]")
                            .first().text();
                    p_userName = p_userName.substring(0, p_userName.indexOf(" Επισκέπτης"));
                    p_userColor = USER_COLOR_BLACK;
                } else {
                    p_userName = userName.html();
                    p_profileURL = userName.attr("href");
                }

                //Find post's submit date
                Element postDate = item.select("div.smalltext:matches(στις:)").first();
                p_postDate = postDate.text();
                p_postDate = p_postDate.substring(p_postDate.indexOf("στις:") + 6
                        , p_postDate.indexOf(" »"));

                //Find post's number
                Element postNum = item.select("div.smalltext:matches(Απάντηση #)").first();
                if (postNum == null) { //Topic starter
                    p_postNum = 0;
                } else {
                    String tmp_str = postNum.text().substring(12);
                    p_postNum = Integer.parseInt(tmp_str.substring(0, tmp_str.indexOf(" στις")));
                }


                //Find attached file's urls, names and info, if present
                Elements postAttachments = item.select("div:containsOwn(έγινε λήψη):containsOwn(φορές.)");
                if (postAttachments != null) {
                    Elements attachedFiles = postAttachments.select("a");
                    String postAttachmentsText = postAttachments.text();

                    for (int i = 0; i < attachedFiles.size(); ++i) {
                        String[] attachedArray = new String[3];

                        //Get file's url and filename
                        Element tmpAttachedFileUrlAndName = attachedFiles.get(i);
                        attachedArray[0] = tmpAttachedFileUrlAndName.attr("href");
                        attachedArray[1] = tmpAttachedFileUrlAndName.text().substring(1);

                        //Get file's info (size and download count)
                        String postAttachmentsTextSbstr = postAttachmentsText.substring(
                                postAttachmentsText.indexOf(attachedArray[1]));

                        attachedArray[2] = postAttachmentsTextSbstr.substring(attachedArray[1]
                                .length(), postAttachmentsTextSbstr.indexOf("φορές.")) + "φορές.)";

                        p_attachedFiles.add(attachedArray);
                    }
                }
            } else {
                //Find username
                userName = item.select("a[title^=View the profile of]").first();
                if (userName == null) { //Deleted profile
                    p_isDeleted = true;
                    p_userName = item
                            .select("td:has(div.smalltext:containsOwn(Guest))[style^=overflow]")
                            .first().text();
                    p_userName = p_userName.substring(0, p_userName.indexOf(" Guest"));
                    p_userColor = USER_COLOR_BLACK;
                } else {
                    p_userName = userName.html();
                    p_profileURL = userName.attr("href");
                }

                //Find post's submit date
                Element postDate = item.select("div.smalltext:matches(on:)").first();
                p_postDate = postDate.text();
                p_postDate = p_postDate.substring(p_postDate.indexOf("on:") + 4
                        , p_postDate.indexOf(" »"));

                //Find post's number
                Element postNum = item.select("div.smalltext:matches(Reply #)").first();
                if (postNum == null) { //Topic starter
                    p_postNum = 0;
                } else {
                    String tmp_str = postNum.text().substring(9);
                    p_postNum = Integer.parseInt(tmp_str.substring(0, tmp_str.indexOf(" on")));
                }


                //Find attached file's urls, names and info, if present
                Elements postAttachments = item.select("div:containsOwn(downloaded):containsOwn(times.)");
                if (postAttachments != null) {
                    Elements attachedFiles = postAttachments.select("a");
                    String postAttachmentsText = postAttachments.text();

                    for (int i = 0; i < attachedFiles.size(); ++i) {
                        String[] attachedArray = new String[3];

                        //Get file's url and filename
                        Element tmpAttachedFileUrlAndName = attachedFiles.get(i);
                        attachedArray[0] = tmpAttachedFileUrlAndName.attr("href");
                        attachedArray[1] = tmpAttachedFileUrlAndName.text().substring(1);

                        //Get file's info (size and download count)
                        String postAttachmentsTextSbstr = postAttachmentsText.substring(
                                postAttachmentsText.indexOf(attachedArray[1]));

                        attachedArray[2] = postAttachmentsTextSbstr.substring(attachedArray[1]
                                .length(), postAttachmentsTextSbstr.indexOf("times.")) + "times.)";

                        p_attachedFiles.add(attachedArray);
                    }
                }
            }

            if (!p_isDeleted) { //Active user
                //Get extra info
                int postsLineIndex = -1;
                int starsLineIndex = -1;

                Element info = userName.parent().nextElementSibling(); //Get sibling "div"
                List<String> infoList = Arrays.asList(info.html().split("<br>"));

                if (Objects.equals(language, LANGUAGE_GREEK)) {
                    for (String line : infoList) {
                        if (line.contains("Μηνύματα:")) {
                            postsLineIndex = infoList.indexOf(line);
                            //Remove any line breaks and spaces on the start and end
                            p_numberOfPosts = line.replace("\n", "").replace("\r", "").trim();
                        }
                        if (line.contains("Φύλο:")) {
                            if (line.contains("alt=\"Άντρας\""))
                                p_gender = "Φύλο: Άντρας";
                            else
                                p_gender = "Φύλο: Γυναίκα";
                        }
                        if (line.contains("alt=\"*\"")) {
                            starsLineIndex = infoList.indexOf(line);
                            Document starsHtml = Jsoup.parse(line);
                            p_numberOfStars = starsHtml.select("img[alt]").size();
                            p_userColor = colorPicker(starsHtml.select("img[alt]").first()
                                    .attr("abs:src"));
                        }
                    }
                } else {
                    for (String line : infoList) {
                        if (line.contains("Posts:")) {
                            postsLineIndex = infoList.indexOf(line);
                            //Remove any line breaks and spaces on the start and end
                            p_numberOfPosts = line.replace("\n", "").replace("\r", "").trim();
                        }
                        if (line.contains("Gender:")) {
                            if (line.contains("alt=\"Male\""))
                                p_gender = "Gender: Male";
                            else
                                p_gender = "Gender: Female";
                        }
                        if (line.contains("alt=\"*\"")) {
                            starsLineIndex = infoList.indexOf(line);
                            Document starsHtml = Jsoup.parse(line);
                            p_numberOfStars = starsHtml.select("img[alt]").size();
                            p_userColor = colorPicker(starsHtml.select("img[alt]").first()
                                    .attr("abs:src"));
                        }
                    }
                }

                //If this member has no stars yet ==> New member,
                //or is just a member
                if (starsLineIndex == -1 || starsLineIndex == 1) {
                    //In this case:
                    p_rank = infoList.get(0).trim(); //First line has the rank
                    p_specialRank = null; //They don't have a special rank
                } else if (starsLineIndex == 2) { //This member has a special rank
                    p_specialRank = infoList.get(0).trim(); //First line has the special rank
                    p_rank = infoList.get(1).trim(); //Second line has the rank
                }
                for (int i = postsLineIndex + 1; i < infoList.size() - 1; ++i) {
                    //Search under "Posts:"
                    //and above "Personal Message", "View Profile" etc buttons

                    String thisLine = infoList.get(i);
                    //If this line isn't empty and doesn't contain user's avatar
                    if (!Objects.equals(thisLine, "") && thisLine != null
                            && !Objects.equals(thisLine, " \n")
                            && !thisLine.contains("avatar")
                            && !thisLine.contains("<a href=")) {
                        p_personalText = thisLine; //Then this line has user's personal text
                        //Remove any line breaks and spaces on the start and end
                        p_personalText = p_personalText.replace("\n", "").replace("\r", "").trim();
                    }
                }
                //Add new post in postsList, extended information needed
                returnList.add(new Post(p_thumbnailUrl, p_userName, p_subject, p_post, p_postIndex
                        , p_postNum, p_postDate, p_profileURL, p_rank, p_specialRank, p_gender
                        , p_numberOfPosts, p_personalText, p_numberOfStars, p_userColor
                        , p_attachedFiles));

            } else { //Deleted user
                //Add new post in postsList, only standard information needed
                returnList.add(new Post(p_thumbnailUrl, p_userName, p_subject, p_post, p_postIndex
                        , p_postNum, p_postDate, p_userColor, p_attachedFiles));
            }
        }
        return returnList;
    }

    static String defineLanguage(Document doc) {
        if (doc.select("h3").text().contains("Καλώς ορίσατε")) {
            return LANGUAGE_GREEK;
        } else { //Default is english (eg. guest's language)
            return LANGUAGE_ENGLISH;
        }
    }

    private static int colorPicker(String starsUrl) {
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
}
