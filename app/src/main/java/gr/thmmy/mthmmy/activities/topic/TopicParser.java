package gr.thmmy.mthmmy.activities.topic;

import android.graphics.Color;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Poll;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyFile;
import gr.thmmy.mthmmy.model.TopicItem;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import timber.log.Timber;


/**
 * Singleton used for parsing a topic.
 * <p>Class contains the methods:<ul><li>{@link #parseUsersViewingThisTopic(Document,
 * ParseHelpers.Language)}</li>
 * <li>{@link #parseCurrentPageIndex(Document, ParseHelpers.Language)}</li>
 * <li>{@link #parseTopicNumberOfPages(Document, int, ParseHelpers.Language)}</li>
 * <li>{@link #parseTopic(Document, ParseHelpers.Language)}</li>
 */
public class TopicParser {
    private static Pattern mentionsPattern = Pattern.
            compile("<div class=\"quoteheader\">\\n\\s+?<a href=.+?>(Quote from|Παράθεση από): "
                    + BaseActivity.getSessionManager().getUsername() +"\\s(στις|on)");

    //User colors
    private static final int USER_COLOR_BLACK = Color.parseColor("#000000");
    private static final int USER_COLOR_RED = Color.parseColor("#F44336");
    private static final int USER_COLOR_GREEN = Color.parseColor("#4CAF50");
    private static final int USER_COLOR_BLUE = Color.parseColor("#536DFE");
    static final int USER_COLOR_PINK = Color.parseColor("#FF4081");
    static final int USER_COLOR_YELLOW = Color.parseColor("#FFEB3B");
    static final int USER_COLOR_WHITE = Color.WHITE;

    /**
     * Returns users currently viewing this topic.
     *
     * @param topic    {@link Document} object containing this topic's source code
     * @param language a {@link ParseHelpers.Language} containing this topic's
     *                 language set, this is returned by
     *                 {@link ParseHelpers.Language#getLanguage(Document)}
     * @return String containing html with the usernames of users
     * @see org.jsoup.Jsoup Jsoup
     */
    public static String parseUsersViewingThisTopic(Document topic, ParseHelpers.Language language) {
        if (language == ParseHelpers.Language.GREEK)
            return topic.select("td:containsOwn(διαβάζουν αυτό το θέμα)").first().html();
        return topic.select("td:containsOwn(are viewing this topic)").first().html();
    }

    /**
     * Returns current topic's page index.
     *
     * @param topic    {@link Document} object containing this topic's source code
     * @param language a {@link ParseHelpers.Language} containing this topic's
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
     * Returns the number of this topic's pages.
     *
     * @param topic       {@link Document} object containing this topic's source code
     * @param currentPage an int containing current page of this topic
     * @param language    a {@link ParseHelpers.Language} containing this topic's
     *                    language set, this is returned by
     *                    {@link ParseHelpers.Language#getLanguage(Document)}
     * @return int containing the number of pages
     * @see org.jsoup.Jsoup Jsoup
     */
    public static int parseTopicNumberOfPages(Document topic, int currentPage, ParseHelpers.Language language) {
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
     * This method parses all the information of a topic and it's posts.
     *
     * @param topic    {@link Document} object containing this topic's source code
     * @param language a {@link ParseHelpers.Language} containing this topic's
     *                 language set, this is returned by
     *                 {@link ParseHelpers.Language#getLanguage(Document)}
     * @return {@link ArrayList} of {@link Post}s
     * @see org.jsoup.Jsoup Jsoup
     */
    public static ArrayList<TopicItem> parseTopic(Document topic, ParseHelpers.Language language) {
        //Method's variables
        final int NO_INDEX = -1;

        ArrayList<TopicItem> parsedPostsList = new ArrayList<>();

        Poll poll = findPoll(topic);
        if (poll != null)
            parsedPostsList.add(poll);

        Elements postRows;

        //Each row is a post
        if (language == ParseHelpers.Language.GREEK)
            postRows = topic.select("form[id=quickModForm]>table>tbody>tr:matches(στις)");
        else {
            postRows = topic.select("form[id=quickModForm]>table>tbody>tr:matches(on)");
        }

        for (Element thisRow : postRows) {
            //Variables for Post constructor
            String p_userName, p_thumbnailURL, p_subject, p_post, p_postDate, p_profileURL, p_rank,
                    p_specialRank, p_gender, p_personalText, p_numberOfPosts, p_postLastEditDate,
                    p_postURL, p_deletePostURL, p_editPostURL;
            int p_postNum, p_postIndex, p_numberOfStars, p_userColor;
            boolean p_isDeleted = false, p_isUserMentionedInPost = false;
            ArrayList<ThmmyFile> p_attachedFiles;

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
            p_postLastEditDate = null;
            p_deletePostURL = null;
            p_editPostURL = null;

            //Language independent parsing
            //Finds thumbnail url
            Element thumbnailUrl = thisRow.select("img.avatar").first();
            p_thumbnailURL = null; //In case user doesn't have an avatar
            if (thumbnailUrl != null) {
                p_thumbnailURL = thumbnailUrl.attr("abs:src");
            }

            //Finds subject
            p_subject = thisRow.select("div[id^=subject_]").first().select("a").first().text();

            //Finds post's link
            p_postURL = thisRow.select("div[id^=subject_]").first().select("a").first().attr("href");

            //Finds post's text
            p_post = ParseHelpers.youtubeEmbeddedFix(thisRow.select("div").select(".post").first());

            //Adds stuff to make it work in WebView
            //style.css
            p_post = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + p_post);

            //Finds post's index
            //This is an int assigned by the forum used for post focusing and quotes, it is not
            //the same as reply index.
            Element postIndex = thisRow.select("a[name^=msg]").first();
            if (postIndex != null) {
                String tmp = postIndex.attr("name");
                p_postIndex = Integer.parseInt(tmp.substring(tmp.indexOf("msg") + 3));
            } else {
                postIndex = thisRow.select("div[id^=subject]").first();
                if (postIndex == null)
                    p_postIndex = NO_INDEX;
                else {
                    String tmp = postIndex.attr("id");
                    p_postIndex = Integer.parseInt(tmp.substring(tmp.indexOf("subject") + 8));
                }
            }

            Element postLastEditDate = thisRow.select("td.smalltext[id^=modified_]").first();
            if (postLastEditDate != null && !Objects.equals(postLastEditDate.text(), ""))
                p_postLastEditDate = postLastEditDate.text();

            //Language dependent parsing
            Element userName;
            if (language == ParseHelpers.Language.GREEK) {
                //Finds username and profile's url
                userName = thisRow.select("a[title^=Εμφάνιση προφίλ του μέλους]").first();
                if (userName == null) { //Deleted profile
                    p_isDeleted = true;
                    p_userName = thisRow
                            .select("td:has(div.smalltext:containsOwn(Επισκέπτης))[style^=overflow]")
                            .first().text();
                    p_userName = p_userName.substring(0, p_userName.indexOf(" Επισκέπτης"));
                    p_userColor = USER_COLOR_YELLOW;
                } else {
                    p_userName = userName.html();
                    p_profileURL = userName.attr("href");
                }

                //Finds post delete url
                Element postDelete = thisRow.select("a:has(img[alt='Διαγραφή'])").first();
                if (postDelete != null) {
                    p_deletePostURL = postDelete.attr("href");
                }

                Element postEdit = thisRow.select("a:has(img[alt='Αλλαγή'])").first();
                if (postEdit != null)
                    p_editPostURL = postEdit.attr("href");

                //Finds post's submit date
                Element postDate = thisRow.select("div.smalltext:matches(στις:)").first();
                p_postDate = postDate.text();
                p_postDate = p_postDate.substring(p_postDate.indexOf("στις:") + 6
                        , p_postDate.indexOf(" »"));

                //Finds post's reply index number
                Element postNum = thisRow.select("div.smalltext:matches(Απάντηση #)").first();
                if (postNum == null) { //Topic starter
                    p_postNum = 0;
                } else {
                    String tmp_str = postNum.text().substring(12);
                    p_postNum = Integer.parseInt(tmp_str.substring(0, tmp_str.indexOf(" στις")));
                }


                //Finds attached file's urls, names and info, if present
                Elements postAttachments = thisRow.select("div:containsOwn(έγινε λήψη):containsOwn(φορές.)");
                if (postAttachments != null) {
                    Elements attachedFiles = postAttachments.select("a");
                    String postAttachmentsText = postAttachments.text();

                    for (int i = 0; i < attachedFiles.size(); ++i) {
                        URL attachedUrl;

                        //Gets file's url and filename
                        Element tmpAttachedFileUrlAndName = attachedFiles.get(i);
                        try {
                            attachedUrl = new URL(tmpAttachedFileUrlAndName.attr("href"));
                        } catch (MalformedURLException e) {
                            Timber.e(e, "Attached file malformed url");
                            break;
                        }
                        String attachedFileName = tmpAttachedFileUrlAndName.wholeText().substring(1);

                        //Gets file's info (size and download count)
                        String postAttachmentsTextSbstr = postAttachmentsText.substring(
                                postAttachmentsText.indexOf(attachedFileName));

                        String attachedFileInfo = postAttachmentsTextSbstr.substring(attachedFileName
                                .length(), postAttachmentsTextSbstr.indexOf("φορές.")) + "φορές.)";

                        p_attachedFiles.add(new ThmmyFile(attachedUrl, attachedFileName, attachedFileInfo));
                    }
                }
            } else {
                //Finds username
                userName = thisRow.select("a[title^=View the profile of]").first();
                if (userName == null) { //Deleted profile
                    p_isDeleted = true;
                    p_userName = thisRow
                            .select("td:has(div.smalltext:containsOwn(Guest))[style^=overflow]")
                            .first().text();
                    p_userName = p_userName.substring(0, p_userName.indexOf(" Guest"));
                    p_userColor = USER_COLOR_YELLOW;
                } else {
                    p_userName = userName.html();
                    p_profileURL = userName.attr("href");
                }

                //Finds post delete url
                Element postDelete = thisRow.select("a:has(img[alt='Remove message'])").first();
                if (postDelete != null) {
                    p_deletePostURL = postDelete.attr("href");
                }

                //Finds post modify url
                Element postEdit = thisRow.select("a:has(img[alt='Modify message'])").first();
                if (postEdit != null) {
                    p_editPostURL = postEdit.attr("href");
                }

                //Finds post's submit date
                Element postDate = thisRow.select("div.smalltext:matches(on:)").first();
                p_postDate = postDate.text();
                p_postDate = p_postDate.substring(p_postDate.indexOf("on:") + 4
                        , p_postDate.indexOf(" »"));

                //Finds post's reply index number
                Element postNum = thisRow.select("div.smalltext:matches(Reply #)").first();
                if (postNum == null) { //Topic starter
                    p_postNum = 0;
                } else {
                    String tmp_str = postNum.text().substring(9);
                    p_postNum = Integer.parseInt(tmp_str.substring(0, tmp_str.indexOf(" on")));
                }


                //Finds attached file's urls, names and info, if present
                Elements postAttachments = thisRow.select("div:containsOwn(downloaded):containsOwn(times.)");
                if (postAttachments != null) {
                    Elements attachedFiles = postAttachments.select("a");
                    String postAttachmentsText = postAttachments.text();

                    for (int i = 0; i < attachedFiles.size(); ++i) {
                        URL attachedUrl;

                        //Gets file's url and filename
                        Element tmpAttachedFileUrlAndName = attachedFiles.get(i);
                        try {
                            attachedUrl = new URL(tmpAttachedFileUrlAndName.attr("href"));
                        } catch (MalformedURLException e) {
                            Timber.e(e, "Attached file malformed url");
                            break;
                        }
                        String attachedFileName = tmpAttachedFileUrlAndName.wholeText().substring(1);

                        //Gets file's info (size and download count)
                        String postAttachmentsTextSbstr = postAttachmentsText.substring(
                                postAttachmentsText.indexOf(attachedFileName));

                        String attachedFileInfo = postAttachmentsTextSbstr.substring(attachedFileName
                                .length(), postAttachmentsTextSbstr.indexOf("times.")) + "times.)";

                        p_attachedFiles.add(new ThmmyFile(attachedUrl, attachedFileName, attachedFileInfo));
                    }
                }
            }

            if (!p_isDeleted) { //Active user
                //Gets extra info
                int postsLineIndex = -1;
                int starsLineIndex = -1;

                Element usersExtraInfo = userName.parent().nextElementSibling(); //Get sibling "div"
                List<String> infoList = Arrays.asList(usersExtraInfo.html().split("<br>"));

                if (language == ParseHelpers.Language.GREEK) {
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
                    p_rank = infoList.get(0).trim(); //First line has the rank
                    p_specialRank = null; //They don't have a special rank
                } else if (starsLineIndex == 2) { //This member has a special rank
                    p_specialRank = infoList.get(0).trim(); //First line has the special rank
                    p_rank = infoList.get(1).trim(); //Second line has the rank
                }
                for (int i = postsLineIndex + 1; i < infoList.size() - 1; ++i) {
                    //Searches under "Posts:"
                    //and above "Personal Message", "View Profile" etc buttons
                    String thisLine = infoList.get(i);
                    if (!Objects.equals(thisLine, "") && thisLine != null
                            && !Objects.equals(thisLine, " \n")
                            && !thisLine.contains("avatar")
                            && !thisLine.contains("<a href=")) {
                        p_personalText = thisLine;
                        p_personalText = p_personalText.replace("\n", "").replace("\r", "").trim();
                    }
                }

                //Checks post for mentions of this user (if the user is logged in)
                if (BaseActivity.getSessionManager().isLoggedIn() &&
                        mentionsPattern.matcher(p_post).find()) {
                    p_isUserMentionedInPost = true;
                }

                //Add new post in postsList, extended information needed
                parsedPostsList.add(new Post(p_thumbnailURL, p_userName, p_subject, p_post, null, p_postIndex
                        , p_postNum, p_postDate, p_profileURL, p_rank, p_specialRank, p_gender
                        , p_numberOfPosts, p_personalText, p_numberOfStars, p_userColor
                        , p_attachedFiles, p_postLastEditDate, p_postURL, p_deletePostURL, p_editPostURL
                        , p_isUserMentionedInPost, Post.TYPE_POST));

            } else { //Deleted user
                //Add new post in postsList, only standard information needed
                parsedPostsList.add(new Post(p_thumbnailURL, p_userName, p_subject, p_post
                        , null, p_postIndex, p_postNum, p_postDate, p_userColor, p_attachedFiles
                        , p_postLastEditDate, p_postURL, p_deletePostURL, p_editPostURL
                        , p_isUserMentionedInPost, Post.TYPE_POST));
            }
        }
        return parsedPostsList;
    }

    private static Poll findPoll(Document topic) {
        Pattern integerPattern = Pattern.compile("[0-9]+");
        Element table = topic.select("table.tborder").first();
        try {
            String question;
            ArrayList<Poll.Entry> entries = new ArrayList<>();
            int availableVoteCount = 0, selectedEntryIndex = -1;
            String pollFormUrl = null, sc = null, removeVoteUrl = null, showVoteResultsUrl = null,
                    showOptionsUrl = null;
            boolean pollResultsHidden = false;

            Element pollColumn = table.select("tr[class=windowbg]").first().child(1);
            question = pollColumn.ownText().trim();
            Element form = pollColumn.select("form").first();

            if (form != null) {
                // poll in vote mode
                pollFormUrl = form.attr("action");
                sc = form.select("input[name=sc]").first().attr("value");

                List<Node> possibleEntriesRows = form.select("td:has(input[id^=options])").first().childNodes();
                for (Node possibleEntry : possibleEntriesRows) {
                    String possibleEntryHtml = possibleEntry.outerHtml();
                    if (!possibleEntryHtml.equals(" ") && !possibleEntryHtml.equals("<br>") && !possibleEntryHtml.startsWith("<input")) {
                        entries.add(new Poll.Entry(possibleEntryHtml.trim()));
                    }
                }

                Elements formTableRows = form.select("tbody>tr");
                Elements links;

                if (formTableRows.size() == 3) {
                    String prompt = formTableRows.first().child(0).text().trim();
                    Matcher integerMatcher = integerPattern.matcher(prompt);
                    if (integerMatcher.find()) {
                        availableVoteCount = Integer.parseInt(prompt.substring(integerMatcher.start(), integerMatcher.end()));
                    }
                    links = formTableRows.get(1).child(1).select("a");
                } else {
                    availableVoteCount = 1;
                    links = formTableRows.first().child(1).select("a");
                }

                if (links != null && links.size() > 0) {
                    showVoteResultsUrl = links.first().attr("href");
                }
            } else {
                // poll in results mode
                Elements entryRows = pollColumn.select("table[cellspacing] tr");
                for (int i = 0; i < entryRows.size(); i++) {
                    Element entryRow = entryRows.get(i);
                    Elements entryColumns = entryRow.select("td");

                    if (entryColumns.first().attr("style").contains("font-weight: bold;"))
                        selectedEntryIndex = i;

                    String optionName = entryColumns.first().html();
                    int voteCount = 0;

                    if (entryColumns.size() < 2) pollResultsHidden = true;
                    if (!pollResultsHidden) {
                        String voteCountDescription = entryColumns.last().text();
                        Matcher integerMatcher = integerPattern.matcher(voteCountDescription);
                        if (integerMatcher.find()) {
                            voteCount = Integer.parseInt(voteCountDescription.substring(integerMatcher.start(),
                                    integerMatcher.end()));
                        }
                    }

                    entries.add(new Poll.Entry(optionName, voteCount));
                }

                Elements links = pollColumn.select("td[style=padding-left: 15px;] > a");
                if (links != null && links.size() > 0) {
                    if (links.first().text().equals("Remove Vote") || links.first().text().equals("Αφαίρεση ψήφου"))
                        removeVoteUrl = links.first().attr("href");
                    else if (links.first().text().equals("Voting options") || links.first().text().equals("Επιλογές ψηφοφορίας"))
                        showOptionsUrl = links.first().attr("href");
                }
            }
            return new Poll(question, entries.toArray(new Poll.Entry[0]), availableVoteCount,
                    pollFormUrl, sc, removeVoteUrl, showVoteResultsUrl, showOptionsUrl, selectedEntryIndex, pollResultsHidden);
        } catch (Exception e) {
            Timber.v(e, "Could not parse a poll");
        }

        return null;
    }

    /**
     * Returns the color of a user according to user's rank on forum.
     *
     * @param starsUrl String containing the URL of a user's stars
     * @return an int corresponding to the right color
     */
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
