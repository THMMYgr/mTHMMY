package gr.thmmy.mthmmy.model;

/**
 * Class that defines the model of a post as need in notifications. All member variables are
 * declared final (thus no setters are supplied). Class has one constructor and getter methods for
 * all variables.
 * <p>PostNotification model is described by its post's id, its topic's id & title and by its poster
 * </p>.
 */
public class PostNotification {
    private final int postId;
    private final int topicId;
    private final String topicTitle;
    private final String poster;
    private final int boardId;
    private final String boardTitle;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    PostNotification() {
        this.postId = -1;
        this.topicId = -1;
        this.topicTitle = null;
        this.poster = null;
        this.boardId = -1;
        this.boardTitle = null;
    }

    /**
     * Constructor specifying all class variables necessary to summarize this post. All variables
     * are declared final, once assigned they cannot change.
     *
     * @param postId         this post's id
     * @param topicId         this post's topicId
     * @param topicTitle          this post's topicTitle
     * @param poster         username of this post's author
     * @param boardId         one of this post's boardIds (-1 if it is a topic notification)
     * @param boardTitle         one of this post's boardTitles (null if it is a topic notification)
     */
    public PostNotification(int postId, int topicId, String topicTitle, String poster, int boardId, String boardTitle) {
        this.postId = postId;
        this.topicId = topicId;
        this.topicTitle = topicTitle;
        this.poster = poster;
        this.boardId = boardId;
        this.boardTitle = boardTitle;

    }

    /**
     * Gets this post's Id.
     *
     * @return this  post's Id
     */
    public int getPostId() {
        return postId;
    }

    /**
     * Gets this post's topicId.
     *
     * @return this post's topicId
     */
    public int getTopicId() {
        return topicId;
    }

    /**
     * Gets this post's topicTitle.
     *
     * @return this post's topicTitle
     */
    public String getTopicTitle() {
        return topicTitle;
    }

    /**
     * Gets username of this post's author.
     *
     * @return username of this post's author
     */
    public String getPoster() {
        return poster;
    }

    /**
     * Gets this post's boardId.
     *
     * @return this post's boardId
     */
    public int getBoardId() {
        return boardId;
    }

    /**
     * Gets this post's boardTitle.
     *
     * @return this post's boardTitle
     */
    public String getBoardTitle() {
        return boardTitle;
    }
}





