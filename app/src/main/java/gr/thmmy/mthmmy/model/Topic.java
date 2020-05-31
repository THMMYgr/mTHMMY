package gr.thmmy.mthmmy.model;

/**
 * Class that defines a topic. All member variables are declared final (thus no setters are supplied).
 * Class has one constructor and getter methods for all variables.
 * <p>A topic is described by its url, subject, username of creator, its date and time of this
 * topic's last post, url of this topic's last post, its view and reply stats, whether it's locked or
 * not, whether it's sticky or not and whether it contains an unread post or not.</b>.
 */
public class Topic extends TopicSummary {
    private final String lastPostUrl, starter, stats;
    private final boolean locked, sticky, unread;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    private Topic() {
        super();
        this.lastPostUrl = null;
        this.starter = null;
        this.stats = null;
        this.locked = false;
        this.sticky = false;
        this.unread = false;
    }

    /**
     * Constructor specifying all class variables necessary to describe this topic. All variables
     * are declared final, once assigned they can not change.
     *
     * @param topicUrl    this topic's url
     * @param subject     this topic's subject
     * @param starter     this topic starter's username
     * @param lastUser    username of topic's last post's author
     * @param lastPostUrl url of topic's last post
     * @param stats       this topic's view and reply stats
     * @param locked      whether this topic is locked or not
     * @param sticky      whether this topic is sticky or not
     * @param unread      whether this topic contains an unread post or not
     */
    public Topic(String topicUrl, String subject, String starter, String lastUser, String LastPostDateTime, String lastPostUrl,
                 String stats, boolean locked, boolean sticky, boolean unread) {
        super(topicUrl, subject, lastUser, LastPostDateTime);
        this.lastPostUrl = lastPostUrl;
        this.starter = starter;
        this.stats = stats;
        this.locked = locked;
        this.sticky = sticky;
        this.unread = unread;
    }

    /**
     * Gets this topic's starter username.
     *
     * @return this topic's starter username
     */
    public String getStarter() {
        return starter;
    }

    /**
     * Gets this topic's last post's url.
     *
     * @return last post's url
     */
    public String getLastPostUrl() {
        return lastPostUrl;
    }

    /**
     * Gets this topic's view and reply stats.
     *
     * @return this topic's view and reply stats
     */
    public String getStats() {
        return stats;
    }

    /**
     * Gets this topic's lock status. True if topic is locked, false otherwise.
     *
     * @return this topic's lock status
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * Gets this topic's sticky status. True if topic is locked, false otherwise.
     *
     * @return this topic's sticky status
     */
    public boolean isSticky() {
        return sticky;
    }

    /**
     * Gets this topic's unread status. True if it contains an unread post, false otherwise.
     *
     * @return this topic's unread status
     */
    public boolean isUnread() {
        return unread;
    }
}
