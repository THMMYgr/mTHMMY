package gr.thmmy.mthmmy.model;

public class Bookmark {
    private final String title, id;

    public Bookmark(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }
}
