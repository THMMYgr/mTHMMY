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
    //Parsing variables
    private static String currentPage;
    private static String postRowSelection;
    private static String userNameSelection;
    private static String guestSelection;
    private static int postDateSubstrSelection;
    private static String postNumberSelection;
    private static int postNumSubstrSelection;
    private static String numberOfPostsSelection;
    private static String genderSelection;
    private static String genderAltMale;
    private static String genderAltFemale;

    //User colors variables
    private static final int USER_COLOR_BLACK = Color.parseColor("#000000");
    private static final int USER_COLOR_RED = Color.parseColor("#F44336");
    private static final int USER_COLOR_GREEN = Color.parseColor("#4CAF50");
    private static final int USER_COLOR_BLUE = Color.parseColor("#536DFE");
    private static final int USER_COLOR_PINK = Color.parseColor("#FF4081");
    private static final int USER_COLOR_YELLOW = Color.parseColor("#FFEB3B");

    @SuppressWarnings("unused")
    private static final String TAG = "TopicParser";

    static int parseCurrentPageIndex(Document doc) {
        defineLanguange(doc);

        int returnPage = 1;
        //Contains pages
        Elements findCurrentPage = doc.select("td:contains(" + currentPage + ")>b");

        for (Element item : findCurrentPage) {
            if (!item.text().contains("...") //It's not "..."
                    && !item.text().contains(currentPage)) { //Nor "Pages:"/"Σελίδες:"
                returnPage = Integer.parseInt(item.text());
                break;
            }
        }
        return returnPage;
    }

    static int parseTopicNumberOfPages(Document doc, int thisPage) {
        defineLanguange(doc);

        //Method's variables
        int returnPages = 1;

        //Contains all pages
        Elements pages = doc.select("td:contains(" + currentPage + ")>a.navPages");

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
        defineLanguange(doc);

        //Method's variables
        final int NO_INDEX = -1;
        ArrayList<Post> returnList = new ArrayList<>();

        Elements rows = doc.select("form[id=quickModForm]>table>tbody>tr:matches("
                + postRowSelection +")");

        for (Element item : rows) { //For every post
            //Variables to pass
            String p_userName, p_thumbnailUrl, p_subject, p_post, p_postDate, p_rank,
                    p_specialRank, p_gender, p_personalText, p_numberOfPosts;
            int p_postNum, p_postIndex, p_numberOfStars, p_userColor;
            boolean p_isDeleted = false;

            //Initialize variables
            p_rank = "Rank";
            p_specialRank = "Special rank";
            p_gender = "";
            p_personalText = "";
            p_numberOfPosts = "";
            p_numberOfStars = 0;
            p_userColor = USER_COLOR_YELLOW;

            //Find the Username
            Element userName = item.select("a[title^=" + userNameSelection + "]").first();
            if (userName == null) { //Deleted profile
                p_isDeleted = true;
                p_userName = item
                        .select("td:has(div.smalltext:containsOwn("
                                + guestSelection + "))[style^=overflow]")
                        .first().text();
                p_userName = p_userName.substring(0, p_userName.indexOf(" " + guestSelection));
                p_userColor = USER_COLOR_BLACK;
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
            p_post = item.select("div").select(".post").first().outerHtml();

            {
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
                            , "<iframe width=\"100%\" height=\"auto\" src=\""
                                    + "https://www.youtube.com/embed/"
                                    + embededVideosUrls.get(tmp_counter)
                                    + "\" frameborder=\"0\" allowfullscreen></iframe>"
                    );
                }
            }

            //Add stuff to make it work in WebView
            p_post = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />"
                    + p_post); //style.css

            //Find post's submit date
            Element postDate = item.select("div.smalltext:matches(" + postRowSelection + ":)").first();
            p_postDate = postDate.text();
            p_postDate = p_postDate.substring(p_postDate.indexOf(postRowSelection + ":") + postDateSubstrSelection
                    , p_postDate.indexOf(" »"));

            //Find post's number
            Element postNum = item.select("div.smalltext:matches(" + postNumberSelection + ")").first();
            if (postNum == null) { //Topic starter
                p_postNum = 0;
            } else {
                String tmp_str = postNum.text().substring(postNumSubstrSelection);
                p_postNum = Integer.parseInt(tmp_str.substring(0, tmp_str.indexOf(" " + postRowSelection)));
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
                    if (line.contains(numberOfPostsSelection)) {
                        postsLineIndex = infoList.indexOf(line);
                        //Remove any line breaks and spaces on the start and end
                        p_numberOfPosts = line.replace("\n", "")
                                .replace("\r", "").trim();
                    }
                    if (line.contains(genderSelection)) {
                        if (line.contains("alt=\"" + genderAltMale + "\""))
                            p_gender = genderSelection + " " + genderAltMale;
                        else
                            p_gender = genderSelection + " " + genderAltFemale;
                    }
                    if (line.contains("alt=\"*\"")) {
                        starsLineIndex = infoList.indexOf(line);
                        Document starsHtml = Jsoup.parse(line);
                        p_numberOfStars = starsHtml.select("img[alt]").size();
                        p_userColor = colorPicker(starsHtml.select("img[alt]").first().attr("abs:src"));
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
                        , p_postIndex, p_postNum, p_postDate, p_rank
                        , p_specialRank, p_gender, p_numberOfPosts, p_personalText
                        , p_numberOfStars, p_userColor));

            } else { //Deleted user
                //Add new post in postsList, only standard information needed
                returnList.add(new Post(p_thumbnailUrl, p_userName, p_subject
                        , p_post, p_postIndex, p_postNum, p_postDate, p_userColor));
            }
        }
        return returnList;
    }

    private static void defineLanguange(Document doc){
        //English parsing variables
        final String en_currentPage = "Pages:";
        final String en_postRowSelection = "on";
        final String en_userNameSelection = "View the profile of";
        final String en_guestSelection = "Guest";
        final String en_postsNumberSelection = "Reply #";
        final String en_numberOfPostsSelection = "Posts:";
        final String en_genderSelection = "Gender:";
        final String en_genderAltMale = "Male";
        final String en_genderAltFemale = "Female";

        //Greek parsing variables
        final String gr_currentPage = "Σελίδες:";
        final String gr_postRowSelection = "στις";
        final String gr_userNameSelection = "Εμφάνιση προφίλ του μέλους";
        final String gr_guestSelection = "Επισκέπτης";
        final String gr_postsNumberSelection = "Απάντηση #";
        final String gr_numberOfPostsSelection = "Μηνύματα:";
        final String gr_genderSelection = "Φύλο:";
        final String gr_genderAltMale = "Άντρας";
        final String gr_genderAltFemale = "Γυναίκα";

        if(doc.select("h3").text().contains("Καλώς ορίσατε")){
            currentPage = gr_currentPage;
            postRowSelection = gr_postRowSelection;
            userNameSelection = gr_userNameSelection;
            guestSelection = gr_guestSelection;
            postDateSubstrSelection = 6;
            postNumberSelection = gr_postsNumberSelection;
            postNumSubstrSelection = 12;
            numberOfPostsSelection = gr_numberOfPostsSelection;
            genderSelection = gr_genderSelection;
            genderAltMale = gr_genderAltMale;
            genderAltFemale = gr_genderAltFemale;
        }
        else{ //Means default is english (eg. guest's language)
            currentPage = en_currentPage;
            postRowSelection = en_postRowSelection;
            userNameSelection = en_userNameSelection;
            guestSelection = en_guestSelection;
            postDateSubstrSelection = 4;
            postNumberSelection = en_postsNumberSelection;
            postNumSubstrSelection = 9;
            numberOfPostsSelection = en_numberOfPostsSelection;
            genderSelection = en_genderSelection;
            genderAltMale = en_genderAltMale;
            genderAltFemale = en_genderAltFemale;
        }
    }
    private static int colorPicker(String starsUrl){
        if(starsUrl.contains("/star.gif"))
            return USER_COLOR_YELLOW;
        else if(starsUrl.contains("/starmod.gif"))
            return USER_COLOR_GREEN;
        else if(starsUrl.contains("/stargmod.gif"))
            return USER_COLOR_BLUE;
        else if(starsUrl.contains("/staradmin.gif"))
            return USER_COLOR_RED;
        else if(starsUrl.contains("/starweb.gif"))
            return USER_COLOR_BLACK;
        else if(starsUrl.contains("/oscar.gif"))
            return USER_COLOR_PINK;
        return USER_COLOR_YELLOW;
    }
}
