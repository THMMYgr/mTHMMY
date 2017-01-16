package gr.thmmy.mthmmy.model;

/**
 * Class that defines the summary of a post. All member variables are declared final (thus no
 * setters are supplied). Class has one constructor and getter methods for all variables.
 * <p>A post summary is described by its url, subject, date and time of post and its content</b>.
 */
public class PostSummary {
    private final String postUrl;
    private final String subject;
    private final String dateTime;
    private final String post;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    private PostSummary() {
        this.postUrl = null;
        this.subject = null;
        this.dateTime = null;
        this.post = null;
    }

    /**
     * Constructor specifying all class variables necessary to summarise this post. All variables
     * are declared final, once assigned they can not change.
     *
     * @param postUrl  this post's url
     * @param subject  this post's subject
     * @param dateTime this post's date and time of submission
     * @param post     this post's content
     */
    public PostSummary(String postUrl, String subject, String dateTime,
                       String post) {
        this.postUrl = postUrl;
        this.subject = subject;
        this.dateTime = dateTime;
        this.post = post;
    }

    /**
     * Gets this post's url.
     *
     * @return post's url
     */
    public String getPostUrl() {
        return postUrl;
    }

    /**
     * Gets this post's subject.
     *
     * @return post's subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Gets this post's date and time of submission.
     *
     * @return post's date and time
     */
    public String getDateTime() {
        return dateTime;
    }

    /**
     * Gets this post's content.
     *
     * @return post's content
     */
    public String getPost() {
        return post;
    }
}
