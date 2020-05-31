package gr.thmmy.mthmmy.model;

import static gr.thmmy.mthmmy.utils.parsing.ThmmyDateTimeParser.convertToTimestamp;
import static gr.thmmy.mthmmy.utils.parsing.ThmmyDateTimeParser.simplifyDateTime;

/**
 * Class that defines the summary of a topic. All member variables are declared final (thus no
 * setters are supplied). Class has one constructor and getter methods for all variables.
 * <p>A topic summary is described by its url, subject, username of its last author and its date and
 * time of this topic's last post.</b>.
 */
public class TopicSummary {
    private final String topicUrl;
    private final String subject;
    private final String lastUser;
    private final String lastPostDateTime;
    private final String lastPostSimplifiedDateTime;
    private final String lastPostTimestamp;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    TopicSummary() {
        this.topicUrl = null;
        this.subject = null;
        this.lastUser = null;
        this.lastPostDateTime = null;
        this.lastPostSimplifiedDateTime = null;
        this.lastPostTimestamp = null;
    }

    /**
     * Constructor specifying all class variables necessary to summarize this topic. All variables
     * are declared final, once assigned they can not change.
     *
     * @param topicUrl         this topic's url
     * @param subject          this topic's subject
     * @param lastUser         username of this topic's last post's author
     * @param lastPostDateTime this topic's date and time of last post
     */
    public TopicSummary(String topicUrl, String subject, String lastUser, String lastPostDateTime) {
        this.topicUrl = topicUrl;
        this.subject = subject;
        this.lastUser = lastUser;
        this.lastPostDateTime = lastPostDateTime;
        this.lastPostTimestamp = convertToTimestamp(lastPostDateTime);
        this.lastPostSimplifiedDateTime = simplifyDateTime(lastPostDateTime);
    }

    /**
     * Gets this topic's url.
     *
     * @return this topic's url
     */
    public String getTopicUrl() {
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
     * Gets username of this topic's last post's author.
     *
     * @return username of last post's author
     */
    public String getLastUser() {
        return lastUser;
    }

    /**
     * Gets this topic's date and time of last post.
     *
     * @return this topic's date and time of last post
     */
    public String getLastPostDateTime() {
        return lastPostDateTime;
    }

    /**
     * Gets this topic's simplified date and time of last post.
     *
     * @return this topic's simplified date and time of last post
     */
    public String getLastPostSimplifiedDateTime() {
        return lastPostSimplifiedDateTime;
    }

    /**
     * Gets the timestamp of this topic's last post.
     *
     * @return the timestamp of this topic's last post
     */
    public String getLastPostTimestamp() {
        return lastPostTimestamp;
    }
}
