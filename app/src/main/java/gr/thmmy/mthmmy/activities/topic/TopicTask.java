package gr.thmmy.mthmmy.activities.topic;

import android.os.AsyncTask;
import android.util.SparseArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import gr.thmmy.mthmmy.viewmodel.TopicViewModel;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * An {@link AsyncTask} that handles asynchronous fetching of this topic page and parsing of its
 * data.
 * <p>TopicTask's {@link AsyncTask#execute execute} method needs a topic's url as String
 * parameter.</p>
 */
public class TopicTask extends AsyncTask<String, Void, TopicTaskResult> {
    //input data
    private TopicViewModel viewModel;
    private boolean reloadingPage;
    private ArrayList<Post> lastPostsList;
    //output data
    private ResultCode resultCode;
    private String topicTitle, replyPageUrl, topicTreeAndMods, topicViewers;
    private ArrayList<Post> newPostsList;
    private int loadedPageTopicId = -1;
    private int focusedPostIndex = 0;
    private SparseArray<String> pagesUrls = new SparseArray<>();
    //(possibly) update data
    private int currentPageIndex, pageCount;
    private String baseUrl, lastPageLoadAttemptedUrl;

    //consecutive load constructor
    public TopicTask(boolean reloadingPage, String baseUrl, int currentPageIndex, int pageCount,
                     String lastPageLoadAttemptedUrl, ArrayList<Post> lastPostsList) {
        this.viewModel = viewModel;
        this.reloadingPage = reloadingPage;
        this.baseUrl = baseUrl;
        this.currentPageIndex = currentPageIndex;
        this.pageCount = pageCount;
        this.lastPageLoadAttemptedUrl = lastPageLoadAttemptedUrl;
        this.lastPostsList = lastPostsList;
    }

    //first load constructor
    public TopicTask() {
        this.viewModel = viewModel;
        this.reloadingPage = false;
        this.baseUrl = "";
        this.currentPageIndex = 1;
        this.pageCount = 1;
        this.lastPageLoadAttemptedUrl = "";
        this.lastPostsList = null;
    }

    @Override
    protected TopicTaskResult doInBackground(String... strings) {
        Document document = null;
        String newPageUrl = strings[0];

        //Finds the index of message focus if present
        int postFocus = -1;
        {
            if (newPageUrl.contains("msg")) {
                String tmp = newPageUrl.substring(newPageUrl.indexOf("msg") + 3);
                if (tmp.contains(";"))
                    postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf(";")));
                else if (tmp.contains("#"))
                    postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf("#")));
            }
        }
        //Checks if the page to be loaded is the one already shown
        if (!reloadingPage && !Objects.equals(lastPageLoadAttemptedUrl, "") && newPageUrl.contains(baseUrl)) {
            if (newPageUrl.contains("topicseen#new") || newPageUrl.contains("#new"))
                if (currentPageIndex == pageCount)
                    resultCode = ResultCode.SAME_PAGE;
            if (newPageUrl.contains("msg")) {
                String tmpUrlSbstr = newPageUrl.substring(newPageUrl.indexOf("msg") + 3);
                if (tmpUrlSbstr.contains("msg"))
                    tmpUrlSbstr = tmpUrlSbstr.substring(0, tmpUrlSbstr.indexOf("msg") - 1);
                int testAgainst = Integer.parseInt(tmpUrlSbstr);
                for (Post post : lastPostsList) {
                    if (post.getPostIndex() == testAgainst) {
                        resultCode = ResultCode.SAME_PAGE;
                    }
                }
            } else if ((Objects.equals(newPageUrl, baseUrl) && currentPageIndex == 1) ||
                    Integer.parseInt(newPageUrl.substring(baseUrl.length() + 1)) / 15 + 1 ==currentPageIndex)
                resultCode = ResultCode.SAME_PAGE;
        } else if (!Objects.equals(lastPageLoadAttemptedUrl, "")) topicTitle = null;
        if (reloadingPage) reloadingPage = !reloadingPage;

        lastPageLoadAttemptedUrl = newPageUrl;
        if (strings[0].substring(0, strings[0].lastIndexOf(".")).contains("topic="))
            baseUrl = strings[0].substring(0, strings[0].lastIndexOf(".")); //New topic's base url
        replyPageUrl = null;
        Request request = new Request.Builder()
                .url(newPageUrl)
                .build();
        try {
            Response response = BaseApplication.getInstance().getClient().newCall(request).execute();
            document = Jsoup.parse(response.body().string());
            newPostsList = parse(document);

            loadedPageTopicId = Integer.parseInt(ThmmyPage.getTopicId(lastPageLoadAttemptedUrl));

            //Finds the position of the focused message if present
            for (int i = 0; i < newPostsList.size(); ++i) {
                if (newPostsList.get(i).getPostIndex() == postFocus) {
                    focusedPostIndex = i;
                    break;
                }
            }
            resultCode = ResultCode.SUCCESS;
        } catch (IOException e) {
            Timber.i(e, "IO Exception");
            resultCode = ResultCode.NETWORK_ERROR;
        } catch (ParseException e) {
            if (isUnauthorized(document))
                resultCode = ResultCode.UNAUTHORIZED;
            Timber.e(e, "Parsing Error");
            resultCode = ResultCode.PARSING_ERROR;
        } catch (Exception e) {
            Timber.e(e, "Exception");
            resultCode = ResultCode.OTHER_ERROR;
        }
        return new TopicTaskResult(resultCode, baseUrl, topicTitle, replyPageUrl, newPostsList,
                loadedPageTopicId, currentPageIndex, pageCount, focusedPostIndex, topicTreeAndMods,
                topicViewers, lastPageLoadAttemptedUrl, pagesUrls);
    }

    /**
     * All the parsing a topic needs.
     *
     * @param topic {@link Document} object containing this topic's source code
     * @see org.jsoup.Jsoup Jsoup
     */
    private ArrayList<Post> parse(Document topic) throws ParseException {
        try {
            ParseHelpers.Language language = ParseHelpers.Language.getLanguage(topic);

            //Finds topic's tree, mods and users viewing
            {
                topicTreeAndMods = topic.select("div.nav").first().html();
                topicViewers = TopicParser.parseUsersViewingThisTopic(topic, language);
            }

            //Finds reply page url
            {
                Element replyButton = topic.select("a:has(img[alt=Reply])").first();
                if (replyButton == null)
                    replyButton = topic.select("a:has(img[alt=Απάντηση])").first();
                if (replyButton != null) replyPageUrl = replyButton.attr("href");
            }

            //Finds topic title if missing
            {
                topicTitle = topic.select("td[id=top_subject]").first().text();
                if (topicTitle.contains("Topic:")) {
                    topicTitle = topicTitle.substring(topicTitle.indexOf("Topic:") + 7
                            , topicTitle.indexOf("(Read") - 2);
                } else {
                    topicTitle = topicTitle.substring(topicTitle.indexOf("Θέμα:") + 6
                            , topicTitle.indexOf("(Αναγνώστηκε") - 2);
                    Timber.d("Parsed title: %s", topicTitle);
                }
            }

            { //Finds current page's index
                currentPageIndex = TopicParser.parseCurrentPageIndex(topic, language);
            }
            { //Finds number of pages
                pageCount = TopicParser.parseTopicNumberOfPages(topic, currentPageIndex, language);

                for (int i = 0; i < pageCount; i++) {
                    //Generate each page's url from topic's base url +".15*numberOfPage"
                    pagesUrls.put(i, baseUrl + "." + String.valueOf(i * 15));
                }
            }
            return TopicParser.parseTopic(topic, language);
        } catch (Exception e) {
            throw new ParseException("Parsing failed (TopicTask)");
        }
    }

    private boolean isUnauthorized(Document document) {
        return document != null && document.select("body:contains(The topic or board you" +
                " are looking for appears to be either missing or off limits to you.)," +
                "body:contains(Το θέμα ή πίνακας που ψάχνετε ή δεν υπάρχει ή δεν " +
                "είναι προσβάσιμο από εσάς.)").size() > 0;
    }

    public enum ResultCode {
        SUCCESS, NETWORK_ERROR, PARSING_ERROR, OTHER_ERROR, SAME_PAGE, UNAUTHORIZED
    }

    public interface OnTopicTaskCompleted {
        void onTopicTaskCompleted(TopicTaskResult result);
    }
}
