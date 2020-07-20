package gr.thmmy.mthmmy.activities.topic.tasks;

import android.os.AsyncTask;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.activities.topic.TopicParser;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.model.TopicItem;
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
    private static final Pattern msgPattern = Pattern.compile("msg(\\d+)");

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
        Document topic = null;

        String newPageUrl = strings[0];

        //TODO: Perhaps decode all URLs app-wide (i.e. in BaseApplication)?
        try {
            //Decodes e.g. any %3B to ;
            newPageUrl = URLDecoder.decode(newPageUrl, StandardCharsets.UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            Timber.e(e, "Unsupported Encoding");
        }

        //Finds the index of message focus if present
        int postFocus = 0;
        Matcher matcher = msgPattern.matcher(newPageUrl);
        if (matcher.find())
            postFocus = Integer.parseInt(Objects.requireNonNull(matcher.group(1)));

        Request request = new Request.Builder()
                .url(newPageUrl)
                .build();
        try {
            Response response = BaseApplication.getInstance().getClient().newCall(request).execute();
            topic = Jsoup.parse(response.body().string());

            ParseHelpers.Language language = ParseHelpers.Language.getLanguage(topic);

            //Finds topic's tree, mods and users viewing
            String topicTreeAndMods = topic.select("div.nav").first().html();
            String topicViewers = TopicParser.parseUsersViewingThisTopic(topic, language);

            //Finds reply page url
            String replyPageUrl = null;
            Element replyButton = topic.select("a:has(img[alt=Reply])").first();
            if (replyButton == null)
                replyButton = topic.select("a:has(img[alt=Απάντηση])").first();
            if (replyButton != null) replyPageUrl = replyButton.attr("href");

            //Finds topic title if missing
            String topicTitle = topic.select("td[id=top_subject]").first().text();
            if (topicTitle.contains("Topic:"))
                topicTitle = topicTitle.substring(topicTitle.indexOf("Topic:") + 7
                        , topicTitle.indexOf("(Read") - 1);
            else
                topicTitle = topicTitle.substring(topicTitle.indexOf("Θέμα:") + 6
                        , topicTitle.indexOf("(Αναγνώστηκε") - 1);

            //Finds current page's index
            int currentPageIndex = TopicParser.parseCurrentPageIndex(topic, language);

            //Finds number of pages
            int pageCount = TopicParser.parseTopicNumberOfPages(topic, currentPageIndex, language);

            ArrayList<TopicItem> newPostsList = TopicParser.parseTopic(topic, language);

            int loadedPageTopicId = Integer.parseInt(ThmmyPage.getTopicId(newPageUrl));

            //Finds the position of the focused message if present
            int focusedPostIndex = 0;
            for (int i = 0; i < newPostsList.size(); ++i) {
                if (newPostsList.get(i) instanceof Post && ((Post) newPostsList.get(i)).getPostIndex() == postFocus) {
                    focusedPostIndex = i;
                    break;
                }
            }
            return new TopicTaskResult(ResultCode.SUCCESS, topicTitle, replyPageUrl, newPostsList, loadedPageTopicId,
                    currentPageIndex, pageCount, focusedPostIndex, topicTreeAndMods, topicViewers);
        } catch (IOException e) {
            return new TopicTaskResult(ResultCode.NETWORK_ERROR, null, null, null,
                    0, 0, 0, 0, null, null);
        } catch (Exception e) {
            if (isUnauthorized(topic)) {
                return new TopicTaskResult(ResultCode.UNAUTHORIZED, null, null, null,
                        0, 0, 0, 0, null, null);
            } else {
                Timber.e(e, "Topic parse failed");
                return new TopicTaskResult(ResultCode.PARSING_ERROR, null, null, null,
                        0, 0, 0, 0, null, null);
            }
        }
    }

    private boolean isUnauthorized(Document document) {
        return document != null && !document.select("body:contains(The topic or board you" +
                " are looking for appears to be either missing or off limits to you.)," +
                "body:contains(Το θέμα ή πίνακας που ψάχνετε ή δεν υπάρχει ή δεν " +
                "είναι προσβάσιμο από εσάς.)").isEmpty();
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
