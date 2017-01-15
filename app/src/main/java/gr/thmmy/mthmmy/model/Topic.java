package gr.thmmy.mthmmy.model;

public class Topic extends TopicSummary {
    private final String lastPostUrl, stats;
    private final boolean locked, sticky;

    public Topic(String topicUrl, String subject, String starter, String lastPost, String lastPostUrl,
                 String stats, boolean locked, boolean sticky) {
        super(topicUrl, subject, starter, lastPost);
        this.lastPostUrl = lastPostUrl;
        this.stats = stats;
        this.locked = locked;
        this.sticky = sticky;
    }

    public String getUrl() {
        return topicUrl;
    }

    public String getSubject() {
        return subject;
    }

    public String getStarter() {
        return lastUser;
    }

    public String getLastPost() {
        return dateTimeModified;
    }

    public String getLastPostUrl() {
        return lastPostUrl;
    }

    public String getStats() {
        return stats;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isSticky() {
        return sticky;
    }
}
