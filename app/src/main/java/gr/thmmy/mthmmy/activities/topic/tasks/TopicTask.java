package gr.thmmy.mthmmy.activities.topic.tasks;

import android.os.AsyncTask;
import android.util.SparseArray;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.activities.topic.TopicParser;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
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
    private TopicTaskObserver topicTaskObserver;
    private OnTopicTaskCompleted finishListener;

    public TopicTask(TopicTaskObserver topicTaskObserver, OnTopicTaskCompleted finishListener) {
        this.topicTaskObserver = topicTaskObserver;
        this.finishListener = finishListener;
    }

    @Override
    protected void onPreExecute() {
        topicTaskObserver.onTopicTaskStarted();
    }

    @Override
    protected TopicTaskResult doInBackground(String... strings) {
        String topicTitle = null;
        String topicTreeAndMods = "";
        String topicViewers = "";
        ArrayList<Post> newPostsList = null;
        int loadedPageTopicId = -1;
        int focusedPostIndex = 0;
        SparseArray<String> pagesUrls = new SparseArray<>();
        int currentPageIndex = 1;
        int pageCount = 1;
        String baseUrl = "";
        String lastPageLoadAttemptedUrl = "";

        Document topic = null;
        String newPageUrl = strings[0];

        //Finds the index of message focus if present
        int postFocus = 0;
        {
            if (newPageUrl.contains("msg")) {
                String tmp = newPageUrl.substring(newPageUrl.indexOf("msg") + 3);
                if (tmp.contains(";"))
                    postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf(";")));
                else if (tmp.contains("#"))
                    postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf("#")));
            }
        }

        lastPageLoadAttemptedUrl = newPageUrl;
        if (strings[0].substring(0, strings[0].lastIndexOf(".")).contains("topic="))
            baseUrl = strings[0].substring(0, strings[0].lastIndexOf(".")); //New topic's base url
        String replyPageUrl = null;
        Request request = new Request.Builder()
                .url(newPageUrl)
                .build();
        ResultCode resultCode;
        try {
            Response response = BaseApplication.getInstance().getClient().newCall(request).execute();
            topic = Jsoup.parse(response.body().string());

            ParseHelpers.Language language = ParseHelpers.Language.getLanguage(topic);

            //Finds topic's tree, mods and users viewing
            topicTreeAndMods = topic.select("div.nav").first().html();
            topicViewers = TopicParser.parseUsersViewingThisTopic(topic, language);

            //Finds reply page url
            Element replyButton = topic.select("a:has(img[alt=Reply])").first();
            if (replyButton == null)
                replyButton = topic.select("a:has(img[alt=Απάντηση])").first();
            if (replyButton != null) replyPageUrl = replyButton.attr("href");

            //Finds topic title if missing
            topicTitle = topic.select("td[id=top_subject]").first().text();
            if (topicTitle.contains("Topic:")) {
                topicTitle = topicTitle.substring(topicTitle.indexOf("Topic:") + 7
                        , topicTitle.indexOf("(Read") - 2);
            } else {
                topicTitle = topicTitle.substring(topicTitle.indexOf("Θέμα:") + 6
                        , topicTitle.indexOf("(Αναγνώστηκε") - 2);
                Timber.d("Parsed title: %s", topicTitle);
            }

            //Finds current page's index
            currentPageIndex = TopicParser.parseCurrentPageIndex(topic, language);

            //Finds number of pages
            pageCount = TopicParser.parseTopicNumberOfPages(topic, currentPageIndex, language);

            for (int i = 0; i < pageCount; i++) {
                //Generate each page's url from topic's base url +".15*numberOfPage"
                pagesUrls.put(i, baseUrl + "." + String.valueOf(i * 15));
            }

            newPostsList = TopicParser.parseTopic(topic, language);

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
        } catch (Exception e) {
            if (isUnauthorized(topic)) {
                resultCode = ResultCode.UNAUTHORIZED;
            } else {
                Timber.e(e, "Parsing Error");
                resultCode = ResultCode.PARSING_ERROR;
            }
        }
        return new TopicTaskResult(resultCode, baseUrl, topicTitle, replyPageUrl, newPostsList,
                loadedPageTopicId, currentPageIndex, pageCount, focusedPostIndex, topicTreeAndMods,
                topicViewers, lastPageLoadAttemptedUrl, pagesUrls);
    }

    private boolean isUnauthorized(Document document) {
        return document != null && document.select("body:contains(The topic or board you" +
                " are looking for appears to be either missing or off limits to you.)," +
                "body:contains(Το θέμα ή πίνακας που ψάχνετε ή δεν υπάρχει ή δεν " +
                "είναι προσβάσιμο από εσάς.)").size() > 0;
    }

    @Override
    protected void onPostExecute(TopicTaskResult topicTaskResult) {
        finishListener.onTopicTaskCompleted(topicTaskResult);
    }

    public enum ResultCode {
        SUCCESS, NETWORK_ERROR, PARSING_ERROR, UNAUTHORIZED
    }

    public interface TopicTaskObserver {
        void onTopicTaskStarted();

        void onTopicTaskCancelled();
    }

    public interface OnTopicTaskCompleted {
        void onTopicTaskCompleted(TopicTaskResult result);
    }
}
