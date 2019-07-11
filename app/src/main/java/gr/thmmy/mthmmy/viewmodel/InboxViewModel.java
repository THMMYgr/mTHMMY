package gr.thmmy.mthmmy.viewmodel;

import androidx.lifecycle.ViewModel;

import gr.thmmy.mthmmy.activities.inbox.tasks.InboxTask;
import gr.thmmy.mthmmy.model.Inbox;

public class InboxViewModel extends ViewModel implements InboxTask.OnNetworkTaskFinishedListener<Inbox> {
    private static final String INBOX_URL = "https://www.thmmy.gr/smf/index.php?action=pm";

    private InboxTask currentInboxTask;

    private Inbox inbox;
    private InboxTask.OnNetworkTaskFinishedListener<Inbox> onInboxTaskFinishedListener;
    private InboxTask.OnTaskStartedListener onInboxTaskStartedListener;

    public void loadInbox() {
        currentInboxTask = new InboxTask();
        currentInboxTask.setOnTaskStartedListener(onInboxTaskStartedListener);
        currentInboxTask.setOnNetworkTaskFinishedListener(this);
        currentInboxTask.execute(INBOX_URL);
    }

    public void setOnInboxTaskFinishedListener(InboxTask.OnNetworkTaskFinishedListener<Inbox> onInboxTaskFinishedListener) {
        this.onInboxTaskFinishedListener = onInboxTaskFinishedListener;
    }

    public void setOnInboxTaskStartedListener(InboxTask.OnTaskStartedListener onInboxTaskStartedListener) {
        this.onInboxTaskStartedListener = onInboxTaskStartedListener;
    }

    @Override
    public void onNetworkTaskFinished(int resultCode, Inbox inbox) {
        this.inbox = inbox;
        onInboxTaskFinishedListener.onNetworkTaskFinished(resultCode, inbox);
    }

    public Inbox getInbox() {
        return inbox;
    }
}
