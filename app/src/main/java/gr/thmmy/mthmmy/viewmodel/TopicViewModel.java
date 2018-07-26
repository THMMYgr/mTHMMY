package gr.thmmy.mthmmy.viewmodel;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import java.util.ArrayList;

import gr.thmmy.mthmmy.activities.settings.SettingsActivity;
import gr.thmmy.mthmmy.activities.topic.DeleteTask;
import gr.thmmy.mthmmy.activities.topic.EditTask;
import gr.thmmy.mthmmy.activities.topic.PrepareForReply;
import gr.thmmy.mthmmy.activities.topic.PrepareForReplyResult;
import gr.thmmy.mthmmy.activities.topic.PrepareForEditResult;
import gr.thmmy.mthmmy.activities.topic.PrepareForEditTask;
import gr.thmmy.mthmmy.activities.topic.ReplyTask;
import gr.thmmy.mthmmy.activities.topic.TopicTask;
import gr.thmmy.mthmmy.activities.topic.TopicTaskResult;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.session.SessionManager;

public class TopicViewModel extends BaseViewModel implements TopicTask.OnTopicTaskCompleted,
        PrepareForReply.OnPrepareForReplyFinished, PrepareForEditTask.OnPrepareEditFinished {

    private boolean editingPost = false;
    private boolean writingReply = false;
    /**
     * holds the adapter position of the post being edited
     */
    private int postEditedPosition;

    private TopicTask currentTopicTask;
    private PrepareForEditTask currentPrepareForEditTask;
    private PrepareForReply currentPrepareForReplyTask;

    private TopicTask.TopicTaskObserver topicTaskObserver;
    private DeleteTask.DeleteTaskCallbacks deleteTaskCallbacks;
    private ReplyTask.ReplyTaskCallbacks replyFinishListener;
    private PrepareForEditTask.PrepareForEditCallbacks prepareForEditCallbacks;
    private EditTask.EditTaskCallbacks editTaskCallbacks;
    private PrepareForReply.PrepareForReplyCallbacks prepareForReplyCallbacks;

    private MutableLiveData<TopicTaskResult> topicTaskResult = new MutableLiveData<>();
    private MutableLiveData<PrepareForReplyResult> prepareForReplyResult = new MutableLiveData<>();
    private MutableLiveData<PrepareForEditResult> prepareForEditResult = new MutableLiveData<>();

    public void setTopicTaskObserver(TopicTask.TopicTaskObserver topicTaskObserver) {
        this.topicTaskObserver = topicTaskObserver;
    }

    public void setDeleteTaskCallbacks(DeleteTask.DeleteTaskCallbacks deleteTaskCallbacks) {
        this.deleteTaskCallbacks = deleteTaskCallbacks;
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

    public void setPrepareForReplyCallbacks(PrepareForReply.PrepareForReplyCallbacks prepareForReplyCallbacks) {
        this.prepareForReplyCallbacks = prepareForReplyCallbacks;
    }

    public MutableLiveData<TopicTaskResult> getTopicTaskResult() {
        return topicTaskResult;
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

    public int getPostEditedPosition() {
        return postEditedPosition;
    }

    public boolean canReply() {
        return topicTaskResult.getValue() != null && topicTaskResult.getValue().getReplyPageUrl() != null;
    }

    public boolean isWritingReply() {
        return writingReply;
    }

    public void setWritingReply(boolean writingReply) {
        this.writingReply = writingReply;
    }

    public String getBaseUrl() {
        if (topicTaskResult.getValue() != null) {
            return topicTaskResult.getValue().getBaseUrl();
        } else {
            return "";
        }
    }

    public String getTopicUrl() {
        if (topicTaskResult.getValue() != null) {
            return topicTaskResult.getValue().getLastPageLoadAttemptedUrl();
        } else {
            throw new NullPointerException("Topic task has not finished yet!");
        }
    }

    public String getTopicTitle() {
        if (topicTaskResult.getValue() != null) {
            return topicTaskResult.getValue().getTopicTitle();
        } else {
            throw new NullPointerException("Topic task has not finished yet!");
        }
    }

    public void initialLoad(String pageUrl) {
        currentTopicTask = new TopicTask(topicTaskObserver, this);
        currentTopicTask.execute(pageUrl);
    }

    public void loadUrl(String pageUrl) {
        stopLoading();
        currentTopicTask = new TopicTask(topicTaskObserver, this);
        currentTopicTask.execute(pageUrl);
    }

    public void reloadPage() {
        stopLoading();
        currentTopicTask = new TopicTask(topicTaskObserver, this);
        currentTopicTask.execute(topicTaskResult.getValue().getLastPageLoadAttemptedUrl());
    }

    public void changePage(int pageRequested) {
        if (pageRequested != topicTaskResult.getValue().getCurrentPageIndex() - 1) {
            stopLoading();
            currentTopicTask = new TopicTask(topicTaskObserver, this);
            currentTopicTask.execute(topicTaskResult.getValue().getPagesUrls().get(pageRequested));
        }
    }

    public void prepareForReply(ArrayList<Post> postsList, ArrayList<Integer> toQuoteList) {
        if (topicTaskResult.getValue() == null)
            throw new NullPointerException("Topic task has not finished yet!");
        stopLoading();
        changePage(topicTaskResult.getValue().getPageCount() - 1);
         currentPrepareForReplyTask = new PrepareForReply(prepareForReplyCallbacks, this,
                 topicTaskResult.getValue().getReplyPageUrl(), postsList);
         currentPrepareForReplyTask.execute(toQuoteList.toArray(new Integer[0]));
    }

    public void postReply(Context context, String subject, String reply) {
        if (prepareForReplyResult.getValue() == null) {
            throw new NullPointerException("Reply preparation was not found!");
        }
        PrepareForReplyResult replyForm = prepareForReplyResult.getValue();
        boolean includeAppSignature = true;
        SessionManager sessionManager = BaseActivity.getSessionManager();
        if (sessionManager.isLoggedIn()) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            includeAppSignature = prefs.getBoolean(SettingsActivity.APP_SIGNATURE_ENABLE_KEY, true);
        }
        new ReplyTask(replyFinishListener, includeAppSignature).execute(subject, reply,
                replyForm.getNumReplies(), replyForm.getSeqnum(), replyForm.getSc(), replyForm.getTopic());
    }

    public void deletePost(String postDeleteUrl) {
        new DeleteTask(deleteTaskCallbacks).execute(postDeleteUrl);
    }

    public void prepareForEdit(int position, String postEditURL) {
        if (topicTaskResult.getValue() == null)
            throw new NullPointerException("Topic task has not finished yet!");
        stopLoading();
        currentPrepareForEditTask = new PrepareForEditTask(prepareForEditCallbacks, this, position,
                topicTaskResult.getValue().getReplyPageUrl());
        currentPrepareForEditTask.execute(postEditURL);
    }

    public void editPost(int position, String subject, String message) {
        if (prepareForEditResult.getValue() == null)
            throw new NullPointerException("Edit preparation was not found!");
        PrepareForEditResult editResult = prepareForEditResult.getValue();
        new EditTask(editTaskCallbacks, position).execute(editResult.getCommitEditUrl(), message,
                editResult.getNumReplies(), editResult.getSeqnum(), editResult.getSc(), subject, editResult.getTopic());
    }

    /**
     * cancel tasks that change the ui
     * topic, prepare for edit, prepare for reply tasks need to cancel all other ui changing tasks
     * before starting
     */
    public void stopLoading() {
        if (currentTopicTask != null && currentTopicTask.getStatus() == AsyncTask.Status.RUNNING) {
            currentTopicTask.cancel(true);
            topicTaskObserver.onTopicTaskCancelled();
        }
        if (currentPrepareForEditTask != null && currentPrepareForEditTask.getStatus() == AsyncTask.Status.RUNNING) {
            currentPrepareForEditTask.cancel(true);
            prepareForEditCallbacks.onPrepareEditCancelled();
        }
        if (currentPrepareForReplyTask != null && currentPrepareForReplyTask.getStatus() == AsyncTask.Status.RUNNING) {
            currentPrepareForReplyTask.cancel(true);
            prepareForReplyCallbacks.onPrepareForReplyCancelled();
        }
        // no need to cancel reply, edit and delete task, user should not have to wait for the ui
        // after he is done posting, editing or deleting
    }

    @Override
    public void onTopicTaskCompleted(TopicTaskResult result) {
        topicTaskResult.setValue(result);
    }

    @Override
    public void onPrepareForReplyFinished(PrepareForReplyResult result) {
        writingReply = true;
        prepareForReplyResult.setValue(result);
    }

    @Override
    public void onPrepareEditFinished(PrepareForEditResult result, int position) {
        editingPost = true;
        postEditedPosition = position;
        prepareForEditResult.setValue(result);
    }
}
