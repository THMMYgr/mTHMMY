package gr.thmmy.mthmmy.viewmodel;

import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import gr.thmmy.mthmmy.activities.inbox.tasks.InboxTask;
import gr.thmmy.mthmmy.model.Inbox;
import gr.thmmy.mthmmy.model.PM;
import gr.thmmy.mthmmy.pagination.BottomPaginationView;
import gr.thmmy.mthmmy.utils.ExternalAsyncTask;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import timber.log.Timber;

public class InboxViewModel extends ViewModel implements InboxTask.OnNetworkTaskFinishedListener<Inbox>,
        BottomPaginationView.OnPageRequestedListener {
    private static final String INBOX_URL = "https://www.thmmy.gr/smf/index.php?action=pm";
    /**
     * caches the expand/collapse state of the user extra info in the current page for the recyclerview
     */
    private ArrayList<Boolean> userExtraInfoVisibile = new ArrayList<>();

    private MutableLiveData<Integer> pageIndicatorIndex = new MutableLiveData<>();
    private MutableLiveData<Integer> pageCount = new MutableLiveData<>();

    private InboxTask currentInboxTask;

    private Inbox inbox;
    private InboxTask.OnNetworkTaskFinishedListener<Inbox> onInboxTaskFinishedListener;
    private InboxTask.OnTaskStartedListener onInboxTaskStartedListener;
    private InboxTask.OnTaskCancelledListener onInboxTaskCancelledListener;

    public void loadInbox() {
        loadUrl(INBOX_URL);
    }

    public void loadInboxPage(int index) {
        loadUrl(INBOX_URL + ";f=inbox;sort=date;start=" + 15*(index-1));
    }

    public void loadUrl(String url) {
        stopLoading();
        currentInboxTask = new InboxTask();
        currentInboxTask.setOnTaskStartedListener(onInboxTaskStartedListener);
        currentInboxTask.setOnNetworkTaskFinishedListener(this);
        currentInboxTask.setOnTaskCancelledListener(onInboxTaskCancelledListener);
        currentInboxTask.execute(url);
    }

    public void stopLoading() {
        if (currentInboxTask != null && currentInboxTask.getStatus() == AsyncTask.Status.RUNNING) {
            Timber.i("Canceling inbox task");
            currentInboxTask.cancel(true);
            onInboxTaskCancelledListener.onTaskCanceled();
        }
    }

    public void setOnInboxTaskFinishedListener(InboxTask.OnNetworkTaskFinishedListener<Inbox> onInboxTaskFinishedListener) {
        this.onInboxTaskFinishedListener = onInboxTaskFinishedListener;
    }

    public void setOnInboxTaskStartedListener(InboxTask.OnTaskStartedListener onInboxTaskStartedListener) {
        this.onInboxTaskStartedListener = onInboxTaskStartedListener;
    }

    @Override
    public void onPageRequested(int index) {
        pageIndicatorIndex.setValue(index);
        loadInboxPage(index);
    }

    @Override
    public void onNetworkTaskFinished(int resultCode, Inbox inbox) {
        this.inbox = inbox;
        onInboxTaskFinishedListener.onNetworkTaskFinished(resultCode, inbox);
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            userExtraInfoVisibile.clear();
            for (PM pm : inbox.getPms())
                userExtraInfoVisibile.add(false);

            pageIndicatorIndex.setValue(inbox.getCurrentPageIndex());
            pageCount.setValue(inbox.getNumberOfPages());
        }
    }

    public void setOnInboxTaskCancelledListener(InboxTask.OnTaskCancelledListener onInboxTaskCancelledListener) {
        this.onInboxTaskCancelledListener = onInboxTaskCancelledListener;
    }

    public Inbox getInbox() {
        return inbox;
    }

    public MutableLiveData<Integer> getPageCount() {
        return pageCount;
    }

    public MutableLiveData<Integer> getPageIndicatorIndex() {
        return pageIndicatorIndex;
    }

    public boolean isUserExtraInfoVisible(int position) {
        return userExtraInfoVisibile.get(position);
    }

    public void hideUserInfo(int position) {
        userExtraInfoVisibile.set(position, false);
    }

    public void toggleUserInfo(int position) {
        userExtraInfoVisibile.set(position, !userExtraInfoVisibile.get(position));
    }
}
