package gr.thmmy.mthmmy.viewmodel;

import androidx.lifecycle.ViewModel;

import java.util.ArrayList;

import gr.thmmy.mthmmy.activities.inbox.tasks.InboxTask;
import gr.thmmy.mthmmy.model.Inbox;
import gr.thmmy.mthmmy.model.PM;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;

public class InboxViewModel extends ViewModel implements InboxTask.OnNetworkTaskFinishedListener<Inbox> {
    private static final String INBOX_URL = "https://www.thmmy.gr/smf/index.php?action=pm";
    /**
     * caches the expand/collapse state of the user extra info in the current page for the recyclerview
     */
    private ArrayList<Boolean> userExtraInfoVisibile = new ArrayList<>();

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
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            userExtraInfoVisibile.clear();
            for (PM pm : inbox.getPms())
                userExtraInfoVisibile.add(false);
        }
    }

    public Inbox getInbox() {
        return inbox;
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
