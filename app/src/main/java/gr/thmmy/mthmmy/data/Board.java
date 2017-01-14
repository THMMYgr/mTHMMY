package gr.thmmy.mthmmy.data;

public class Board {
    private final String url, title, mods, stats, lastPost, lastPostUrl;

    public Board(String url, String title, String mods, String stats, String lastPost, String lastPostUrl) {
        this.url = url;
        this.title = title;
        this.mods = mods;
        this.stats = stats;
        this.lastPost = lastPost;
        this.lastPostUrl = lastPostUrl;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getMods() {
        return mods;
    }

    public String getStats() {
        return stats;
    }

    public String getLastPost() {
        return lastPost;
    }

    public String getLastPostUrl() {
        return lastPostUrl;
    }
}