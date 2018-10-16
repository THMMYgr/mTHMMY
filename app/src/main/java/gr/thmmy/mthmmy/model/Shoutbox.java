package gr.thmmy.mthmmy.model;

import androidx.annotation.NonNull;
import timber.log.Timber;

public class Shoutbox {
    private Shout[] shouts;
    private String sc, sendShoutUrl, shoutName, shoutSend, shoutUrl;

    public Shoutbox(Shout[] shouts, String sc, String sendShoutUrl, String shoutName, String shoutSend, String shoutUrl) {
        this.shouts = shouts;
        this.sc = sc;
        this.sendShoutUrl = sendShoutUrl;
        this.shoutName = shoutName;
        this.shoutSend = shoutSend;
        this.shoutUrl = shoutUrl;
    }

    public Shout[] getShouts() {
        return shouts;
    }

    public String getSc() {
        return sc;
    }

    public String getSendShoutUrl() {
        return sendShoutUrl;
    }

    public String getShoutName() {
        return shoutName;
    }

    public String getShoutSend() {
        return shoutSend;
    }

    public String getShoutUrl() {
        return shoutUrl;
    }
}
