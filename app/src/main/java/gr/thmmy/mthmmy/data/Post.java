package gr.thmmy.mthmmy.data;

public class Post {
    private final String thumbnailUrl;
    private final String author;
    private final String subject;
    private final String content;
    private final String postDate;
    private final int postNumber;
    private final int postIndex;

    public Post(String thumbnailUrl, String author, String subject, String content, String postDate, int postNumber, int postIndex) {
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.postDate = postDate;
        this.postNumber = postNumber;
        this.postIndex = postIndex;
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

    public String getPostDate() { return postDate;}

    public int getPostNumber() {
        return postNumber;
    }

    public int getPostIndex() {
        return postIndex;
    }
}
