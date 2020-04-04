package gr.thmmy.mthmmy.activities.inbox.tasks;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Inbox;
import gr.thmmy.mthmmy.model.PM;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.parsing.NewParseTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import okhttp3.Response;

public class InboxTask extends NewParseTask<Inbox> {
    @Override
    protected Inbox parse(Document document, Response response) throws ParseException {
        Inbox inbox = new Inbox();
        ParseHelpers.deobfuscateElements(document.select("span.__cf_email__,a.__cf_email__"), true);

        ParseHelpers.Language language = ParseHelpers.Language.getLanguage(document);

        inbox.setCurrentPageIndex(ParseHelpers.parseCurrentPageIndexInbox(document, language));
        inbox.setNumberOfPages(ParseHelpers.parseNumberOfPagesInbox(document, inbox.getCurrentPageIndex(), language));

        ArrayList<PM> pmList = parsePMs(document, language);
        inbox.setPms(pmList);
        return inbox;
    }

    @Override
    protected int getResultCode(Response response, Inbox data) {
        return NetworkResultCodes.SUCCESSFUL;
    }

    private ArrayList<PM> parsePMs(Document document, ParseHelpers.Language language) {
        ArrayList<PM> pms = new ArrayList<>();
        Elements pmContainerContainers = document.select("td[style=padding: 1px 1px 0 1px;]");
        for (Element pmContainerContainer : pmContainerContainers) {
            PM pm = new PM();
            boolean isAuthorDeleted;
            Element pmContainer = pmContainerContainer.select("table[style=table-layout: fixed;]").first().child(0);

            Element thumbnail = pmContainer.select("img.avatar").first();
            // User might not have an avatar
            if (thumbnail != null)
                pm.setThumbnailUrl(thumbnail.attr("src"));

            Element subjectAndDateContainer = pmContainer.select("td[align=left]").first();
            pm.setSubject(subjectAndDateContainer.select("b").first().text());
            Element dateContainer = subjectAndDateContainer.select("div").first();
            pm.setPmDate(subjectAndDateContainer.select("div").first().text());

            String content = ParseHelpers.youtubeEmbeddedFix(pmContainer.select("div.personalmessage").first());
            //Adds stuff to make it work in WebView
            //style.css
            content = "<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + content;
            pm.setContent(content);

            pm.setQuoteUrl(pmContainer.select("img[src=https://www.thmmy.gr/smf/Themes/scribbles2_114/images/buttons/quote.gif]")
                    .first().parent().attr("href"));
            pm.setReplyUrl(pmContainer.select("img[src=https://www.thmmy.gr/smf/Themes/scribbles2_114/images/buttons/im_reply.gif]")
                    .first().parent().attr("href"));
            pm.setDeleteUrl(pmContainer.select("img[src=https://www.thmmy.gr/smf/Themes/scribbles2_114/images/buttons/delete.gif]")
                    .first().parent().attr("href"));

            // language specific parsing
            Element username;
            if (language == ParseHelpers.Language.GREEK) {
                //Finds username and profile's url
                username = pmContainer.select("a[title^=Εμφάνιση προφίλ του μέλους]").first();
                if (username == null) { //Deleted profile
                    isAuthorDeleted = true;
                    String authorName = pmContainer.select("td:has(div.smalltext:containsOwn(Επισκέπτης))[style^=overflow]")
                            .first().text();
                    authorName = authorName.substring(0, authorName.indexOf(" Επισκέπτης"));
                    pm.setAuthor(authorName);
                    pm.setAuthorColor(ParseHelpers.USER_COLOR_YELLOW);
                } else {
                    isAuthorDeleted = false;
                    pm.setAuthor(username.html());
                    pm.setAuthorProfileUrl(username.attr("href"));
                }

                String date = dateContainer.text();
                date = date.substring(date.indexOf("στις:") + 6, date.indexOf(" »"));
                pm.setPmDate(date);
            } else {
                //Finds username
                username = pmContainer.select("a[title^=View the profile of]").first();
                if (username == null) { //Deleted profile
                    isAuthorDeleted = true;
                    String authorName = pmContainer
                            .select("td:has(div.smalltext:containsOwn(Guest))[style^=overflow]")
                            .first().text();
                    authorName = authorName.substring(0, authorName.indexOf(" Guest"));
                    pm.setAuthor(authorName);
                    pm.setAuthorColor(ParseHelpers.USER_COLOR_YELLOW);
                } else {
                    isAuthorDeleted = false;
                    pm.setAuthor(username.html());
                    pm.setAuthorProfileUrl(username.attr("href"));
                }

                String date = dateContainer.text();
                date = date.substring(date.indexOf("on:") + 4, date.indexOf(" »"));
                pm.setPmDate(date);
            }

            if (!isAuthorDeleted) {
                int postsLineIndex = -1;
                int starsLineIndex = -1;

                Element authorInfoContainer = pmContainer.select("div.smalltext").first();
                List<String> infoList = Arrays.asList(authorInfoContainer.html().split("<br>"));

                if (language == ParseHelpers.Language.GREEK) {
                    for (String line : infoList) {
                        if (line.contains("Μηνύματα:")) {
                            postsLineIndex = infoList.indexOf(line);
                            //Remove any line breaks and spaces on the start and end
                            pm.setAuthorNumberOfPosts(line.replace("\n", "").replace("\r", "").trim());
                        }
                        if (line.contains("Φύλο:")) {
                            if (line.contains("alt=\"Άντρας\""))
                                pm.setAuthorGender("Φύλο: Άντρας");
                            else
                                pm.setAuthorGender("Φύλο: Γυναίκα");
                        }
                        if (line.contains("alt=\"*\"")) {
                            starsLineIndex = infoList.indexOf(line);
                            Document starsHtml = Jsoup.parse(line);
                            pm.setAuthorNumberOfStars(starsHtml.select("img[alt]").size());
                            pm.setAuthorColor(ParseHelpers.colorPicker(starsHtml.select("img[alt]").first()
                                    .attr("abs:src")));
                        }
                    }
                } else {
                    for (String line : infoList) {
                        if (line.contains("Posts:")) {
                            postsLineIndex = infoList.indexOf(line);
                            //Remove any line breaks and spaces on the start and end
                            pm.setAuthorNumberOfPosts(line.replace("\n", "").replace("\r", "").trim());
                        }
                        if (line.contains("Gender:")) {
                            if (line.contains("alt=\"Male\""))
                                pm.setAuthorGender("Gender: Male");
                            else
                                pm.setAuthorGender("Gender: Female");
                        }
                        if (line.contains("alt=\"*\"")) {
                            starsLineIndex = infoList.indexOf(line);
                            Document starsHtml = Jsoup.parse(line);
                            pm.setAuthorNumberOfStars(starsHtml.select("img[alt]").size());
                            pm.setAuthorColor(ParseHelpers.colorPicker(starsHtml.select("img[alt]").first()
                                    .attr("abs:src")));
                        }
                    }
                }

                //If this member has no stars yet ==> New member,
                //or is just a member
                if (starsLineIndex == -1 || starsLineIndex == 1) {
                    pm.setAuthorRank(infoList.get(0).trim()); //First line has the rank
                    //They don't have a special rank
                } else if (starsLineIndex == 2) { //This member has a special rank
                    pm.setAuthorSpecialRank(infoList.get(0).trim());//First line has the special rank
                    pm.setAuthorRank(infoList.get(1).trim());//Second line has the rank
                }
                for (int i = postsLineIndex + 1; i < infoList.size() - 1; ++i) {
                    //Searches under "Posts:"
                    //and above "Personal Message", "View Profile" etc buttons
                    String thisLine = infoList.get(i);
                    if (!Objects.equals(thisLine, "") && thisLine != null
                            && !Objects.equals(thisLine, " \n")
                            && !thisLine.contains("avatar")
                            && !thisLine.contains("<a href=")) {
                        String personalText = thisLine;
                        personalText = personalText.replace("\n", "").replace("\r", "").trim();
                        pm.setAuthorPersonalText(personalText);
                    }
                }

                //Checks post for mentions of this user (if the user is logged in)
                if (BaseActivity.getSessionManager().isLoggedIn() &&
                        ParseHelpers.mentionsPattern.matcher(pm.getContent()).find()) {
                    pm.setUserMentioned(true);
                }
            }
            pms.add(pm);
        }
        return pms;
    }
}


