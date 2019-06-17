package gr.thmmy.mthmmy.activities.topic.tasks;

public class PrepareForEditResult {
    private final String postText, commitEditUrl, numReplies, seqnum, sc, topic, icon;
    private int position;
    private boolean successful;

    public PrepareForEditResult(String postText, String commitEditUrl, String numReplies, String seqnum,
                                String sc, String topic, String icon, int position, boolean successful) {
        this.postText = postText;
        this.commitEditUrl = commitEditUrl;
        this.numReplies = numReplies;
        this.seqnum = seqnum;
        this.sc = sc;
        this.topic = topic;
        this.icon = icon;
        this.position = position;
        this.successful = successful;
    }

    public String getPostText() {
        return postText;
    }

    public String getCommitEditUrl() {
        return commitEditUrl;
    }

    public String getNumReplies() {
        return numReplies;
    }

    public String getSeqnum() {
        return seqnum;
    }

    public String getSc() {
        return sc;
    }

    public String getTopic() {
        return topic;
    }

    public String getIcon() {
        return icon;
    }

    public int getPosition() {
        return position;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
