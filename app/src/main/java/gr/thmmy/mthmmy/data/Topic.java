package gr.thmmy.mthmmy.data;

public class Topic extends TopicSummary {
    private final String stats;
    private final boolean locked, sticky;

    public Topic(String topicUrl, String subject, String starter, String lastPost,
                 String stats, boolean locked, boolean sticky) {
        super(topicUrl, subject, starter, lastPost);
        this.stats = stats;
        this.locked = locked;
        this.sticky = sticky;
    }

    public String getSubject() {
        return subject;
    }

    public String getStarter() {
        return lastUser;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isSticky() {
        return sticky;
    }

    public String getUrl() {
        return topicUrl;
    }

    public String getLastPost() {
        return dateTimeModified;
    }

    public String getStats() {
        return stats;
    }
}
