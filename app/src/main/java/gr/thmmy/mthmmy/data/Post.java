package gr.thmmy.mthmmy.data;

public class Post
{
    private final String thumbnailUrl;
    private final String author;
    private final String subject;
    private String content;

    public Post(String thumbnailUrl, String author, String subject, String content) {
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.subject = subject;
        this.content = content;
    }

    public String getThumbnailUrl() { return thumbnailUrl;}

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getDateTime() {
        return subject;
    }
}
