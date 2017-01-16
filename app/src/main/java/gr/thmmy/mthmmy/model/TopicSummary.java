package gr.thmmy.mthmmy.model;

/**
 * Class that defines the summary of a topic. All member variables are declared final (thus no
 * setters are supplied). Class has one constructor and getter methods for all variables.
 * <p>A topic summary is described by its url, subject, username of its last author and its date and
 * time of this topic's last post.</b>.
 */
public class TopicSummary {
    final String topicUrl;
    final String subject;
    final String lastUser;
    final String dateTimeModified;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    TopicSummary() {
        this.topicUrl = null;
        this.subject = null;
        this.lastUser = null;
        this.dateTimeModified = null;
    }

    /**
     * Constructor specifying all class variables necessary to summarise this topic. All variables
     * are declared final, once assigned they can not change.
     *
     * @param topicUrl         this topic's url
     * @param subject          this topic's subject
     * @param lastUser         username of this topic's last author
     * @param dateTimeModified this topic's date and time of last post
     */
    public TopicSummary(String topicUrl, String subject, String lastUser, String dateTimeModified) {
        this.topicUrl = topicUrl;
        this.subject = subject;
        this.lastUser = lastUser;
        this.dateTimeModified = dateTimeModified;
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
     * Gets username of this topic's last author.
     *
     * @return username of last author
     */
    public String getLastUser() {
        return lastUser;
    }

    /**
     * Gets this topic's date and time of last post.
     *
     * @return this topic's date and time of last post
     */
    public String getDateTimeModified() {
        return dateTimeModified;
    }
}
