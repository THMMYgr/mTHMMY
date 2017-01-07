package gr.thmmy.mthmmy.data;

public class TopicSummary {
    final String topicUrl;
    final String subject;
    final String lastUser;
    final String dateTimeModified;

    public TopicSummary(String topicUrl, String subject, String lastUser, String dateTimeModified) {
        this.topicUrl = topicUrl;
        this.subject = subject;
        this.lastUser = lastUser;
        this.dateTimeModified = dateTimeModified;
    }

    public String getTopicUrl() {
        return topicUrl;
    }

    public String getSubject() {
        return subject;
    }

    public String getLastUser() {
        return lastUser;
    }

    public String getDateTimeModified() {
        return dateTimeModified;
    }
}
