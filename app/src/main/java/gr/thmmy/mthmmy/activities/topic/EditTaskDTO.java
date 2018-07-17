package gr.thmmy.mthmmy.activities.topic;

public class EditTaskDTO {
    private int position;
    private final String subject, message, numReplies, seqnum, sc, topic;

    public EditTaskDTO(int position, String subject, String message, String numReplies, String seqnum, String sc, String topic) {
        this.position = position;
        this.subject = subject;
        this.message = message;
        this.numReplies = numReplies;
        this.seqnum = seqnum;
        this.sc = sc;
        this.topic = topic;
    }

    public int getPosition() {
        return position;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
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
}
