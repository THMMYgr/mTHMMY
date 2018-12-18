package gr.thmmy.mthmmy.viewmodel;

import android.os.AsyncTask;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import gr.thmmy.mthmmy.activities.shoutbox.SendShoutTask;
import gr.thmmy.mthmmy.activities.shoutbox.ShoutboxTask;
import gr.thmmy.mthmmy.model.Shoutbox;
import gr.thmmy.mthmmy.session.SessionManager;

public class ShoutboxViewModel extends ViewModel {
    private MutableLiveData<Shoutbox> shoutboxMutableLiveData = new MutableLiveData<>();
    private ShoutboxTask shoutboxTask;
    private ShoutboxTask.OnTaskStartedListener onShoutboxTaskStarted;
    private ShoutboxTask.OnNetworkTaskFinishedListener<Shoutbox> onShoutboxTaskFinished;
    private SendShoutTask.OnTaskStartedListener onSendShoutTaskStarted;
    private SendShoutTask.OnNetworkTaskFinishedListener<Void> onSendShoutTaskFinished;

    public void loadShoutbox(boolean force) {
        if (shoutboxMutableLiveData.getValue() == null || force) {
            if (shoutboxTask != null && shoutboxTask.getStatus() == AsyncTask.Status.RUNNING)
                shoutboxTask.cancel(true);
            shoutboxTask = new ShoutboxTask(onShoutboxTaskStarted, onShoutboxTaskFinished);
            shoutboxTask.execute(SessionManager.shoutboxUrl.toString());
        }
    }

    public void sendShout(String shout) {
        if (shoutboxMutableLiveData.getValue() == null) throw new IllegalStateException("Shoutbox task has not finished yet!");
        Shoutbox shoutbox = shoutboxMutableLiveData.getValue();
        new SendShoutTask(onSendShoutTaskStarted, onSendShoutTaskFinished)
                .execute(shoutbox.getSendShoutUrl(), shout, shoutbox.getSc(),
                        shoutbox.getShoutName(), shoutbox.getShoutSend(), shoutbox.getShoutUrl());
    }

    public void setShoutbox(Shoutbox shoutbox) {
        shoutboxMutableLiveData.setValue(shoutbox);
    }

    public MutableLiveData<Shoutbox> getShoutboxMutableLiveData() {
        return shoutboxMutableLiveData;
    }

    public void setOnSendShoutTaskFinished(SendShoutTask.OnNetworkTaskFinishedListener<Void> onSendShoutTaskFinished) {
        this.onSendShoutTaskFinished = onSendShoutTaskFinished;
    }

    public void setOnSendShoutTaskStarted(SendShoutTask.OnTaskStartedListener onSendShoutTaskStarted) {
        this.onSendShoutTaskStarted = onSendShoutTaskStarted;
    }

    public void setOnShoutboxTaskFinished(ShoutboxTask.OnNetworkTaskFinishedListener<Shoutbox> onShoutboxTaskFinished) {
        this.onShoutboxTaskFinished = onShoutboxTaskFinished;
    }

    public void setOnShoutboxTaskStarted(ShoutboxTask.OnTaskStartedListener onShoutboxTaskStarted) {
        this.onShoutboxTaskStarted = onShoutboxTaskStarted;
    }
}
