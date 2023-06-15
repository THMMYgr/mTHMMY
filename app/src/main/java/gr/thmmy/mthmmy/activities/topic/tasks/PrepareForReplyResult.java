package gr.thmmy.mthmmy.activities.topic.tasks;

public class PrepareForReplyResult {
    private final String numReplies, seqnum, sc, topic, builtQuotes;
    private boolean successful;


    public PrepareForReplyResult(boolean successful, String numReplies, String seqnum, String sc, String topic, String builtQuotes) {
        this.successful = successful;
        this.numReplies = numReplies;
        this.seqnum = seqnum;
        this.sc = sc;
        this.topic = topic;
        this.builtQuotes = builtQuotes;
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

    public String getBuiltQuotes() {
        return builtQuotes;
    }

    public boolean isSuccessful() {
        return successful;
    }
}
