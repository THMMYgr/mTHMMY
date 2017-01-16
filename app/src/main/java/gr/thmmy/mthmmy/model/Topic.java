package gr.thmmy.mthmmy.model;

/**
 * Class that defines a topic. All member variables are declared final (thus no setters are supplied).
 * Class has one constructor and getter methods for all variables.
 * <p>A topic is described by its url, subject, username of creator, its date and time of this
 * topic's last post, url of this topic's last post, its view and reply stats, whether it's locked or
 * not and whether it's sticky or not.</b>.
 */
public class Topic extends TopicSummary {
    private final String lastPostUrl, stats;
    private final boolean locked, sticky;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    private Topic() {
        super();
        this.lastPostUrl = null;
        this.stats = null;
        this.locked = false;
        this.sticky = false;
    }

    /**
     * Constructor specifying all class variables necessary to describe this topic. All variables
     * are declared final, once assigned they can not change.
     *
     * @param topicUrl    this topic's url
     * @param subject     this topic's subject
     * @param starter     this topic starter's username
     * @param lastPost    username of topic's last post's author
     * @param lastPostUrl url of topic's last post
     * @param stats       this topic's view and reply stats
     * @param locked      whether this topic is locked or not
     * @param sticky      whether this topic is sticky or not
     */
    public Topic(String topicUrl, String subject, String starter, String lastPost, String lastPostUrl,
                 String stats, boolean locked, boolean sticky) {
        super(topicUrl, subject, starter, lastPost);
        this.lastPostUrl = lastPostUrl;
        this.stats = stats;
        this.locked = locked;
        this.sticky = sticky;
    }

    /**
     * Gets this topic's url.
     *
     * @return this topic's url
     */
    public String getUrl() {
        return topicUrl;
    }

    /**
     * Gets this topic's subject.
     *
     * @return this topic's subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets this topic's starter username.
     *
     * @return this topic's starter username
     */
    public String getStarter() {
        return lastUser;
    }

    /**
     * Gets this topic's last post's date and time.
     *
     * @return last post's date and time
     */
    public String getLastPostDateAndTime() {
        return dateTimeModified;
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
}
