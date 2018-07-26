package gr.thmmy.mthmmy.activities.topic;

public class PrepareForReplyResult {
    private final String numReplies, seqnum, sc, topic, buildedQuotes;


    public PrepareForReplyResult(String numReplies, String seqnum, String sc, String topic, String buildedQuotes) {
        this.numReplies = numReplies;
        this.seqnum = seqnum;
        this.sc = sc;
        this.topic = topic;
        this.buildedQuotes = buildedQuotes;
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

    public String getBuildedQuotes() {
        return buildedQuotes;
    }
}
