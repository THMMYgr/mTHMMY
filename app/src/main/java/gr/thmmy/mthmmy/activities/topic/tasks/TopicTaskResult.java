package gr.thmmy.mthmmy.activities.topic.tasks;

import android.util.SparseArray;

import java.util.ArrayList;

import gr.thmmy.mthmmy.activities.topic.tasks.TopicTask;
import gr.thmmy.mthmmy.model.Post;

public class TopicTaskResult {
    private final TopicTask.ResultCode resultCode;
    /**
     * Holds this topic's base url. For example a topic with url similar to
     * "https://www.thmmy.gr/smf/index.php?topic=1.15;topicseen" or
     * "https://www.thmmy.gr/smf/index.php?topic=1.msg1#msg1"
     * has the base url "https://www.thmmy.gr/smf/index.php?topic=1"
     */
    private final String baseUrl;
    /**
     * Holds this topic's title. At first this gets the value of the topic title that came with
     * bundle and is rendered in the toolbar while parsing this topic. Later, if a different topic
     * title is parsed from the html source, it gets updated.
     */
    private final String topicTitle;
    /**
     * This topic's reply url
     */
    private final String replyPageUrl;
    private final ArrayList<Post> newPostsList;
    /**
     * The topicId of the loaded page
     */
    private final int loadedPageTopicId;
    /**
     * Holds current page's index (starting from 1, not 0)
     */
    private final int currentPageIndex;
    /**
     * Holds the requested topic's number of pages
     */
    private final int pageCount;
    /**
     * The index of the post that has focus
     */
    private final int focusedPostIndex;
    //Topic's info related
    private final String topicTreeAndMods;
    private final String topicViewers;
    /**
     * The url of the last page that was attempted to be loaded
     */
    private final String lastPageLoadAttemptedUrl;
    private final SparseArray<String> pagesUrls;

    public TopicTaskResult(TopicTask.ResultCode resultCode, String baseUrl, String topicTitle,
                           String replyPageUrl, ArrayList<Post> newPostsList, int loadedPageTopicId,
                           int currentPageIndex, int pageCount, int focusedPostIndex, String topicTreeAndMods,
                           String topicViewers, String lastPageLoadAttemptedUrl, SparseArray<String> pagesUrls) {
        this.resultCode = resultCode;
        this.baseUrl = baseUrl;
        this.topicTitle = topicTitle;
        this.replyPageUrl = replyPageUrl;
        this.newPostsList = newPostsList;
        this.loadedPageTopicId = loadedPageTopicId;
        this.currentPageIndex = currentPageIndex;
        this.pageCount = pageCount;
        this.focusedPostIndex = focusedPostIndex;
        this.topicTreeAndMods = topicTreeAndMods;
        this.topicViewers = topicViewers;
        this.lastPageLoadAttemptedUrl = lastPageLoadAttemptedUrl;
        this.pagesUrls = pagesUrls;
    }

    public TopicTask.ResultCode getResultCode() {
        return resultCode;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getTopicTitle() {
        return topicTitle;
    }

    public String getReplyPageUrl() {
        return replyPageUrl;
    }

    public ArrayList<Post> getNewPostsList() {
        return newPostsList;
    }

    public int getLoadedPageTopicId() {
        return loadedPageTopicId;
    }

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public int getPageCount() {
        return pageCount;
    }

    public int getFocusedPostIndex() {
        return focusedPostIndex;
    }

    public String getTopicTreeAndMods() {
        return topicTreeAndMods;
    }

    public String getTopicViewers() {
        return topicViewers;
    }

    public String getLastPageLoadAttemptedUrl() {
        return lastPageLoadAttemptedUrl;
    }

    public SparseArray<String> getPagesUrls() {
        return pagesUrls;
    }
}
