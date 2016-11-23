package gr.thmmy.mthmmy.data;

public class Post {
    private final String thumbnailUrl;
    private final String author;
    private final String subject;
    private final String content;
    private final int postNumber;

    public Post(String thumbnailUrl, String author, String subject, String content, int postNumber) {
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.postNumber = postNumber;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getSubject() {
        return subject;
    }

    public int getPostNumber() {
        return postNumber;
    }
}
