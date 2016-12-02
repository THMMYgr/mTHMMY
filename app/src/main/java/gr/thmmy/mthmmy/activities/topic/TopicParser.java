package gr.thmmy.mthmmy.activities.topic;

import android.util.Log;

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
    private static final String TAG = "TopicParser";

    static int parseCurrentPageIndex(Document doc) {
        int returnPage = 1;
        Elements findCurrentPage = doc.select("td:contains(Pages:)>b"); //Contains pages

        for (Element item : findCurrentPage) {
            if (!item.text().contains("...") //It's not "..."
                    && !item.text().contains("Pages")) { //Nor "Pages"
                returnPage = Integer.parseInt(item.text());
                break;
            }
        }
        return returnPage;
    }

    static int parseTopicNumberOfPages(Document doc, int thisPage) {
        //Method's variables
        int returnPages = 1;
        Elements pages = doc.select("td:contains(Pages:)>a.navPages"); //Contains all pages

        if (pages.size() != 0) {
            returnPages = thisPage; //Initialize the number
            for (Element item : pages) { //Just a max
                if (Integer.parseInt(item.text()) > returnPages)
                    returnPages = Integer.parseInt(item.text());
            }
        }
        return returnPages;
    }

    static ArrayList<Post> parseTopic(Document doc) {
        //Method's variables
        final int NO_INDEX = -1;
        ArrayList<Post> returnList = new ArrayList<>();

        Elements rows = doc.select("form[id=quickModForm]>table>tbody>tr:matches(on)");

        for (Element item : rows) { //For every post
            //Variables to pass
            String p_userName, p_thumbnailUrl, p_subject, p_post, p_postDate, p_rank,
                    p_specialRank, p_gender, p_personalText, p_numberOfPosts, p_urlOfStars;
            int p_postNum, p_postIndex, p_numberOfStars;
            boolean p_isDeleted = false;

            //Initialize variables
            p_rank = "Rank";
            p_specialRank = "Special rank";
            p_gender = "";
            p_personalText = "";
            p_numberOfPosts = "";
            p_urlOfStars = "";
            p_numberOfStars = 0;

            //Find the Username
            Element userName = item.select("a[title^=View the profile of]").first();
            if (userName == null) { //Deleted profile
                p_isDeleted = true;
                p_userName = item
                        .select("td:has(div.smalltext:containsOwn(Guest))[style^=overflow]")
                        .first().text();
                p_userName = p_userName.substring(0, p_userName.indexOf(" Guest"));
            } else
                p_userName = userName.html();

            //Find thumbnail url
            Element thumbnailUrl = item.select("img.avatar").first();
            p_thumbnailUrl = null; //In case user doesn't have an avatar
            if (thumbnailUrl != null) {
                p_thumbnailUrl = thumbnailUrl.attr("abs:src");
            }

            //Find subject
            p_subject = item.select("div[id^=subject_]").first().select("a").first().text();

            //Find post's text
            p_post = item.select("div").select(".post").first().html();
            //Add stuff to make it work in WebView
            p_post = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
                    + p_post); //style.css

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

            //Find post's index
            Element postIndex = item.select("a[name^=msg]").first();
            if (postIndex == null)
                p_postIndex = NO_INDEX;
            else {
                String tmp = postIndex.attr("name");
                p_postIndex = Integer.parseInt(tmp.substring(tmp.indexOf("msg") + 3));
            }

            if (!p_isDeleted) { //Active user
                //Get extra info
                int postsLineIndex = -1;
                int starsLineIndex = -1;

                Element info = userName.parent().nextElementSibling(); //Get sibling "div"
                List<String> infoList = Arrays.asList(info.html().split("<br>"));

                for (String line : infoList) {
                    Log.i(TAG, line);
                    if (line.contains("Posts:")) {
                        postsLineIndex = infoList.indexOf(line);
                        //Remove any line breaks and spaces on the start and end
                        p_numberOfPosts = line.replace("\n", "")
                                .replace("\r", "").trim();
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
                        p_urlOfStars = starsHtml.select("img[alt]").first().attr("abs:src");
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
                        p_personalText = p_personalText.replace("\n", "")
                                .replace("\r", "").trim();
                    }
                }
                //Add new post in postsList, extended information needed
                returnList.add(new Post(p_thumbnailUrl, p_userName, p_subject, p_post
                        , p_postIndex, p_postNum, p_postDate, false, p_rank
                        , p_specialRank, p_gender, p_numberOfPosts, p_personalText
                        , p_urlOfStars, p_numberOfStars));

            } else { //Deleted user
                //Add new post in postsList, only standard information needed
                returnList.add(new Post(p_thumbnailUrl, p_userName, p_subject
                        , p_post, p_postIndex, p_postNum, p_postDate, true));
            }
        }
        return returnList;
    }
}
