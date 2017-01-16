package gr.thmmy.mthmmy.model;

/**
 * Class that defines a board of the forum. All member variables are declared final (thus no setters
 * are supplied). Class has one constructor and getter methods for all variables.
 * <p>A forum board is described by the board's url, its title, the moderators assigned to it, its
 * view and reply stats, its latest post's info and url.</p>
 */
public class Board {
    private final String url, title, mods, stats, lastPost, lastPostUrl;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    private Board() {
        url = null;
        title = null;
        mods = null;
        stats = null;
        lastPost = null;
        lastPostUrl = null;
    }

    /**
     * Constructor specifying all class variables necessary to describe this board. All variables
     * are declared final, once assigned they can not change.
     *
     * @param url         this board's url
     * @param title       this board's title
     * @param mods        this board's assigned moderators
     * @param stats       this board's view and reply stats
     * @param lastPost    this board's latest post's info
     * @param lastPostUrl this board's latest post's url
     */
    public Board(String url, String title, String mods, String stats, String lastPost, String lastPostUrl) {
        this.url = url;
        this.title = title;
        this.mods = mods;
        this.stats = stats;
        this.lastPost = lastPost;
        this.lastPostUrl = lastPostUrl;
    }

    /**
     * Gets this board's url.
     *
     * @return this board's url
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets this board's title.
     *
     * @return this board's title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets this board's assigned moderators.
     *
     * @return this board's moderators
     */
    public String getMods() {
        return mods;
    }

    /**
     * Gets this board's view and reply stats.
     *
     * @return this board's stats
     */
    public String getStats() {
        return stats;
    }

    /**
     * Gets this board's latest post's info.
     *
     * @return latest post's info
     */
    public String getLastPost() {
        return lastPost;
    }

    /**
     * Gets this board's latest post's url.
     *
     * @return latest post's url
     */
    public String getLastPostUrl() {
        return lastPostUrl;
    }
}