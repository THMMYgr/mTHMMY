package gr.thmmy.mthmmy.activities.topic.tasks;

import android.util.SparseArray;

import java.util.ArrayList;

import gr.thmmy.mthmmy.activities.topic.tasks.TopicTask;
import gr.thmmy.mthmmy.model.Post;

public class TopicTaskResult {
    private final TopicTask.ResultCode resultCode;
    private final String baseUrl, topicTitle, replyPageUrl;
    private final ArrayList<Post> newPostsList;
    private final int loadedPageTopicId, currentPageIndex, pageCount, focusedPostIndex;
    private final String topicTreeAndMods, topicViewers, lastPageLoadAttemptedUrl;
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
