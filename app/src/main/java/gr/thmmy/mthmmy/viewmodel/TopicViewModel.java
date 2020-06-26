package gr.thmmy.mthmmy.viewmodel;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

import gr.thmmy.mthmmy.activities.settings.SettingsActivity;
import gr.thmmy.mthmmy.activities.topic.tasks.DeleteTask;
import gr.thmmy.mthmmy.activities.topic.tasks.EditTask;
import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForEditResult;
import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForEditTask;
import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForReplyResult;
import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForReplyTask;
import gr.thmmy.mthmmy.activities.topic.tasks.RemoveVoteTask;
import gr.thmmy.mthmmy.activities.topic.tasks.ReplyTask;
import gr.thmmy.mthmmy.activities.topic.tasks.SubmitVoteTask;
import gr.thmmy.mthmmy.activities.topic.tasks.TopicTask;
import gr.thmmy.mthmmy.activities.topic.tasks.TopicTaskResult;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Poll;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.TopicItem;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.ExternalAsyncTask;
import gr.thmmy.mthmmy.utils.networking.NetworkTask;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import timber.log.Timber;

public class TopicViewModel extends BaseViewModel implements TopicTask.OnTopicTaskCompleted,
        PrepareForReplyTask.OnPrepareForReplyFinished, PrepareForEditTask.OnPrepareEditFinished {
    /**
     * topic state
     */
    private boolean editingPost = false;
    private boolean writingReply = false;
    /**
     * A list of {@link Post#getPostIndex()} for building quotes for replying
     */
    private ArrayList<Integer> toQuoteList = new ArrayList<>();
    /**
     * caches the expand/collapse state of the user extra info in the current page for the recyclerview
     */
    private ArrayList<Boolean> isUserExtraInfoVisibile = new ArrayList<>();
    /**
     * holds the adapter position of the post being edited
     */
    private int postBeingEditedPosition;

    private TopicTask currentTopicTask;
    private PrepareForEditTask currentPrepareForEditTask;
    private PrepareForReplyTask currentPrepareForReplyTask;

    //callbacks for topic activity
    private TopicTask.TopicTaskObserver topicTaskObserver;
    private ExternalAsyncTask.OnTaskStartedListener deleteTaskStartedListener;
    private NetworkTask.OnNetworkTaskFinishedListener<Void> deleteTaskFinishedListener;
    private ReplyTask.ReplyTaskCallbacks replyFinishListener;
    private PrepareForEditTask.PrepareForEditCallbacks prepareForEditCallbacks;
    private EditTask.EditTaskCallbacks editTaskCallbacks;
    private PrepareForReplyTask.PrepareForReplyCallbacks prepareForReplyCallbacks;
    private ExternalAsyncTask.OnTaskStartedListener voteTaskStartedListener;
    private NetworkTask.OnNetworkTaskFinishedListener<Void> voteTaskFinishedListener;
    private ExternalAsyncTask.OnTaskStartedListener removeVoteTaskStartedListener;
    private NetworkTask.OnNetworkTaskFinishedListener<Void> removeVoteTaskFinishedListener;

    /**
     * Holds the value (index) of the page to be requested when a user interaction with bottom
     * navigation bar occurs, aka the value that the page indicator shows
     */
    private MutableLiveData<Integer> pageIndicatorIndex = new MutableLiveData<>();

    private MutableLiveData<String> replyPageUrl = new MutableLiveData<>();
    private MutableLiveData<Integer> pageTopicId = new MutableLiveData<>();
    private MutableLiveData<String> topicTitle = new MutableLiveData<>();
    private MutableLiveData<ArrayList<TopicItem>> topicItems = new MutableLiveData<>();
    private MutableLiveData<Integer> focusedPostIndex = new MutableLiveData<>();
    private MutableLiveData<TopicTask.ResultCode> topicTaskResultCode = new MutableLiveData<>();
    private MutableLiveData<String> topicTreeAndMods = new MutableLiveData<>();
    private MutableLiveData<String> topicViewers = new MutableLiveData<>();
    private String topicUrl;
    private int currentPageIndex;
    private int pageCount;

    private MutableLiveData<PrepareForReplyResult> prepareForReplyResult = new MutableLiveData<>();
    private MutableLiveData<PrepareForEditResult> prepareForEditResult = new MutableLiveData<>();

    public void loadUrl(String pageUrl) {
        stopLoading();
        topicUrl = pageUrl;
        currentTopicTask = new TopicTask(topicTaskObserver, this);
        currentTopicTask.execute(pageUrl);
    }

    public void reloadPage() {
        if (topicUrl == null) throw new NullPointerException("No topic task has been requested yet!");
        Timber.i("Reloading page");
        loadUrl(topicUrl);
    }

    /**
     * In contrast to {@link TopicViewModel#reloadPage()} this method gets rid of any arguments
     * in the url before refreshing
     */
    public void resetPage() {
        if (topicUrl == null) throw new NullPointerException("No topic task has been requested yet!");
        Timber.i("Resetting page");
        loadUrl(ParseHelpers.getBaseURL(topicUrl) + "." + String.valueOf(currentPageIndex * 15));
    }

    public void resetPageThen(Runnable runnable) {
        if (topicUrl == null) throw new NullPointerException("No topic task has been requested yet!");
        Timber.i("Resetting page");
        stopLoading();
        currentTopicTask = new TopicTask(topicTaskObserver, result -> {
            TopicViewModel.this.onTopicTaskCompleted(result);
            runnable.run();
        });
        currentTopicTask.execute(ParseHelpers.getBaseURL(topicUrl) + "." + String.valueOf(currentPageIndex * 15));
    }

    public void loadPageIndicated() {
        if (pageIndicatorIndex.getValue() == null)
            throw new NullPointerException("No page has been loaded yet!");
        int pageRequested = pageIndicatorIndex.getValue() - 1;
        if (pageRequested != currentPageIndex - 1) {
            Timber.i("Changing to page " + pageRequested + 1);
            loadUrl(ParseHelpers.getBaseURL(topicUrl) + "." + pageRequested * 15);
            pageIndicatorIndex.setValue(pageRequested + 1);
        } else {
            stopLoading();
        }
    }

    public boolean submitVote(LinearLayout optionsLayout) {
        if (topicItems.getValue() == null) throw new NullPointerException("Topic task has not finished yet!");
        ArrayList<Integer> votes = new ArrayList<>();
        if (optionsLayout.getChildAt(0) instanceof RadioGroup) {
            RadioGroup optionsRadioGroup = (RadioGroup) optionsLayout.getChildAt(0);
            votes.add(optionsRadioGroup.getCheckedRadioButtonId());
        } else if (optionsLayout.getChildAt(0) instanceof CheckBox) {
            for (int i = 0; i < optionsLayout.getChildCount(); i++) {
                CheckBox checkBox = (CheckBox) optionsLayout.getChildAt(i);
                if (checkBox.isChecked())
                    votes.add(i);
            }
        }
        int[] votesArray = new int[votes.size()];
        for (int i = 0; i < votes.size(); i++) votesArray[i] = votes.get(i);
        Poll poll = (Poll) topicItems.getValue().get(0);
        if (poll.getAvailableVoteCount() < votesArray.length) return false;
        SubmitVoteTask submitVoteTask = new SubmitVoteTask(votesArray);
        submitVoteTask.setOnTaskStartedListener(voteTaskStartedListener);
        submitVoteTask.setOnNetworkTaskFinishedListener(voteTaskFinishedListener);
        submitVoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, poll.getPollFormUrl(), poll.getSc());
        return true;
    }

    public void removeVote() {
        if (topicItems.getValue() == null) throw new NullPointerException("Topic task has not finished yet!");
        RemoveVoteTask removeVoteTask = new RemoveVoteTask();
        removeVoteTask.setOnTaskStartedListener(removeVoteTaskStartedListener);
        removeVoteTask.setOnNetworkTaskFinishedListener(removeVoteTaskFinishedListener);
        removeVoteTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, ((Poll) topicItems.getValue().get(0)).getRemoveVoteUrl());
    }

    public void prepareForReply() {
        if (replyPageUrl.getValue() == null)
            throw new NullPointerException("Topic task has not finished yet!");
        stopLoading();
        setPageIndicatorIndex(pageCount, true);
        Timber.i("Preparing for reply");
        currentPrepareForReplyTask = new PrepareForReplyTask(prepareForReplyCallbacks, this,
                replyPageUrl.getValue());
        currentPrepareForReplyTask.execute(toQuoteList.toArray(new Integer[0]));
    }

    public void postReply(Context context, String subject, String reply) {
        if (prepareForReplyResult.getValue() == null)
            throw new NullPointerException("Reply preparation was not found!");

        PrepareForReplyResult replyForm = prepareForReplyResult.getValue();
        boolean includeAppSignature = true;
        SessionManager sessionManager = BaseActivity.getSessionManager();
        if (sessionManager.isLoggedIn()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            includeAppSignature = prefs.getBoolean(SettingsActivity.POSTING_APP_SIGNATURE_ENABLE_KEY, true);
        }
        toQuoteList.clear();
        Timber.i("Posting reply");
        new ReplyTask(replyFinishListener, includeAppSignature).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, subject, reply,
                replyForm.getNumReplies(), replyForm.getSeqnum(), replyForm.getSc(), replyForm.getTopic());
    }

    public void deletePost(String postDeleteUrl) {
        Timber.i("Deleting post");
        new DeleteTask(deleteTaskStartedListener, deleteTaskFinishedListener).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, postDeleteUrl);
    }

    public void prepareForEdit(int position, String postEditURL) {
        if (replyPageUrl.getValue() == null)
            throw new NullPointerException("Topic task has not finished yet!");
        stopLoading();
        Timber.i("Preparing for edit");
        currentPrepareForEditTask = new PrepareForEditTask(prepareForEditCallbacks, this, position,
                replyPageUrl.getValue());
        currentPrepareForEditTask.execute(postEditURL);
    }

    public void editPost(int position, String subject, String message) {
        if (prepareForEditResult.getValue() == null)
            throw new NullPointerException("Edit preparation was not found!");
        PrepareForEditResult editResult = prepareForEditResult.getValue();
        Timber.i("Editing post");
        new EditTask(editTaskCallbacks, position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, editResult.getCommitEditUrl(), message,
                editResult.getNumReplies(), editResult.getSeqnum(), editResult.getSc(), subject, editResult.getTopic(), editResult.getIcon());
    }

    /**
     * cancel tasks that change the ui
     * topic, prepare for edit, prepare for reply tasks need to cancel all other ui changing tasks
     * before starting
     */
    public void stopLoading() {
        if (currentTopicTask != null && currentTopicTask.getStatus() == AsyncTask.Status.RUNNING) {
            Timber.i("Canceling topic task");
            currentTopicTask.cancel(true);
            pageIndicatorIndex.setValue(currentPageIndex);
            topicTaskObserver.onTopicTaskCancelled();
        }
        if (currentPrepareForEditTask != null && currentPrepareForEditTask.getStatus() == AsyncTask.Status.RUNNING) {
            Timber.i("Canceling prepare for edit task");
            currentPrepareForEditTask.cancel(true);
            prepareForEditCallbacks.onPrepareEditCancelled();
        }
        if (currentPrepareForReplyTask != null && currentPrepareForReplyTask.getStatus() == AsyncTask.Status.RUNNING) {
            Timber.i("Canceling prepare for reply task");
            currentPrepareForReplyTask.cancel(true);
            prepareForReplyCallbacks.onPrepareForReplyCancelled();
        }
        // no need to cancel reply, edit and delete task, user should not have to wait for the ui
        // after he is done posting, editing or deleting
    }

    // callbacks for viewmodel
    @Override
    public void onTopicTaskCompleted(TopicTaskResult result) {
        if (result.getResultCode() == TopicTask.ResultCode.SUCCESS) {
            currentPageIndex = result.getCurrentPageIndex();
            pageCount = result.getPageCount();
            topicTreeAndMods.setValue(result.getTopicTreeAndMods());
            topicViewers.setValue(result.getTopicViewers());
            pageTopicId.setValue(result.getLoadedPageTopicId());
            replyPageUrl.setValue(result.getReplyPageUrl());
            topicTitle.setValue(result.getTopicTitle());
            pageIndicatorIndex.setValue(result.getCurrentPageIndex());
            topicItems.setValue(result.getNewPostsList());
            focusedPostIndex.setValue(result.getFocusedPostIndex());
            isUserExtraInfoVisibile.clear();
            for (int i = 0; i < result.getNewPostsList().size(); i++)
                isUserExtraInfoVisibile.add(false);
        }
        topicTaskResultCode.setValue(result.getResultCode());
    }

    @Override
    public void onPrepareForReplyFinished(PrepareForReplyResult result) {
        prepareForReplyResult.setValue(result);
    }

    @Override
    public void onPrepareEditFinished(PrepareForEditResult result, int position) {
        postBeingEditedPosition = position;
        prepareForEditResult.setValue(result);
    }

    public void incrementPageRequestValue(int step, boolean changePage) {
        if (pageIndicatorIndex.getValue() == null)
            throw new NullPointerException("No page has been loaded yet!");
        int oldIndicatorIndex = pageIndicatorIndex.getValue();
        if (oldIndicatorIndex <= pageCount - step)
            pageIndicatorIndex.setValue(pageIndicatorIndex.getValue() + step);
        else
            pageIndicatorIndex.setValue(pageCount);
        if (changePage && oldIndicatorIndex != pageIndicatorIndex.getValue()) loadPageIndicated();
    }

    public void decrementPageRequestValue(int step, boolean changePage) {
        if (pageIndicatorIndex.getValue() == null)
            throw new NullPointerException("No page has been loaded yet!");
        int oldIndicatorIndex = pageIndicatorIndex.getValue();
        if (oldIndicatorIndex > step)
            pageIndicatorIndex.setValue(pageIndicatorIndex.getValue() - step);
        else
            pageIndicatorIndex.setValue(1);
        if (changePage && oldIndicatorIndex != pageIndicatorIndex.getValue()) loadPageIndicated();
    }

    public void setPageIndicatorIndex(int pageIndicatorIndex, boolean changePage) {
        if (this.pageIndicatorIndex.getValue() == null)
            throw new NullPointerException("No page has been loaded yet!");
        int oldIndicatorIndex = this.pageIndicatorIndex.getValue();
        this.pageIndicatorIndex.setValue(pageIndicatorIndex);
        if (changePage && oldIndicatorIndex != this.pageIndicatorIndex.getValue()) loadPageIndicated();
    }

    // <-------------Just getters, setters and helper methods below here---------------->

    public int getTopicId() {
        if (pageTopicId.getValue() == null)
            throw  new NullPointerException("No page has been loaded yet!");
        return pageTopicId.getValue();
    }


    public void setRemoveVoteTaskStartedListener(ExternalAsyncTask.OnTaskStartedListener removeVoteTaskStartedListener) {
        this.removeVoteTaskStartedListener = removeVoteTaskStartedListener;
    }

    public void setRemoveVoteTaskFinishedListener(NetworkTask.OnNetworkTaskFinishedListener<Void> removeVoteTaskFinishedListener) {
        this.removeVoteTaskFinishedListener = removeVoteTaskFinishedListener;
    }

    public void setVoteTaskStartedListener(ExternalAsyncTask.OnTaskStartedListener voteTaskStartedListener) {
        this.voteTaskStartedListener = voteTaskStartedListener;
    }

    public void setVoteTaskFinishedListener(NetworkTask.OnNetworkTaskFinishedListener<Void> voteTaskFinishedListener) {
        this.voteTaskFinishedListener = voteTaskFinishedListener;
    }

    public MutableLiveData<String> getTopicViewers() {
        return topicViewers;
    }

    public MutableLiveData<String> getTopicTreeAndMods() {
        return topicTreeAndMods;
    }

    public MutableLiveData<TopicTask.ResultCode> getTopicTaskResultCode() {
        return topicTaskResultCode;
    }

    public MutableLiveData<Integer> getFocusedPostIndex() {
        return focusedPostIndex;
    }

    public MutableLiveData<ArrayList<TopicItem>> getTopicItems() {
        return topicItems;
    }

    public MutableLiveData<String> getReplyPageUrl() {
        return replyPageUrl;
    }

    public MutableLiveData<Integer> getPageTopicId() {
        return pageTopicId;
    }

    public MutableLiveData<String> getTopicTitle() {
        return topicTitle;
    }

    public String getTopicUrl() {
        return topicUrl;
    }

    public MutableLiveData<Integer> getPageIndicatorIndex() {
        return pageIndicatorIndex;
    }

    public boolean isUserExtraInfoVisible(int position) {
        return isUserExtraInfoVisibile.get(position);
    }

    public void hideUserInfo(int position) {
        isUserExtraInfoVisibile.set(position, false);
    }

    public void toggleUserInfo(int position) {
        isUserExtraInfoVisibile.set(position, !isUserExtraInfoVisibile.get(position));
    }

    public ArrayList<Integer> getToQuoteList() {
        return toQuoteList;
    }

    public void postIndexToggle(Integer postIndex) {
        if (toQuoteList.contains(postIndex))
            toQuoteList.remove(postIndex);
        else
            toQuoteList.add(postIndex);
    }

    public void setTopicTaskObserver(TopicTask.TopicTaskObserver topicTaskObserver) {
        this.topicTaskObserver = topicTaskObserver;
    }


    public void setDeleteTaskStartedListener(ExternalAsyncTask.OnTaskStartedListener deleteTaskStartedListener) {
        this.deleteTaskStartedListener = deleteTaskStartedListener;
    }

    public void setDeleteTaskFinishedListener(NetworkTask.OnNetworkTaskFinishedListener<Void> deleteTaskFinishedListener) {
        this.deleteTaskFinishedListener = deleteTaskFinishedListener;
    }

    public void setReplyFinishListener(ReplyTask.ReplyTaskCallbacks replyFinishListener) {
        this.replyFinishListener = replyFinishListener;
    }

    public void setPrepareForEditCallbacks(PrepareForEditTask.PrepareForEditCallbacks prepareForEditCallbacks) {
        this.prepareForEditCallbacks = prepareForEditCallbacks;
    }

    public void setEditTaskCallbacks(EditTask.EditTaskCallbacks editTaskCallbacks) {
        this.editTaskCallbacks = editTaskCallbacks;
    }

    public void setPrepareForReplyCallbacks(PrepareForReplyTask.PrepareForReplyCallbacks prepareForReplyCallbacks) {
        this.prepareForReplyCallbacks = prepareForReplyCallbacks;
    }

    public MutableLiveData<PrepareForReplyResult> getPrepareForReplyResult() {
        return prepareForReplyResult;
    }

    public MutableLiveData<PrepareForEditResult> getPrepareForEditResult() {
        return prepareForEditResult;
    }

    public void setEditingPost(boolean editingPost) {
        this.editingPost = editingPost;
    }

    public boolean isEditingPost() {
        return editingPost;
    }

    public int getPostBeingEditedPosition() {
        return postBeingEditedPosition;
    }

    public boolean canReply() {
        return replyPageUrl.getValue() != null;
    }

    public boolean isWritingReply() {
        return writingReply;
    }

    public void setWritingReply(boolean writingReply) {
        this.writingReply = writingReply;
    }

    public int getCurrentPageIndex() {
        if (currentPageIndex == 0) throw  new NullPointerException("No page has been loaded yet!");
        return currentPageIndex;
    }

    public int getPageCount() {
        if (pageCount == 0) throw  new NullPointerException("No page has been loaded yet!");
        return pageCount;
    }

    public String getPostBeingEditedText() {
        if (prepareForEditResult.getValue() == null)
            throw new NullPointerException("Edit preparation was not found!");
        return prepareForEditResult.getValue().getPostText();
    }

    public String getBuildedQuotes() {
        if (prepareForReplyResult.getValue() == null)
            throw new NullPointerException("Reply preparation was not found");
        return prepareForReplyResult.getValue().getBuildedQuotes();
    }

    public int postCount() {
        if (topicItems.getValue() == null)
            throw  new NullPointerException("No page has been loaded yet!");
        return topicItems.getValue().size();
    }
}
