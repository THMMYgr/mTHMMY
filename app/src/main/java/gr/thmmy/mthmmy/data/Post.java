package gr.thmmy.mthmmy.data;

/**
 * Created by ezero on 14/9/2016.
 */
public class Post
{
    private final String author;
    private final String dateTime;
    private String content;

    public Post(String author, String dateTime, String content) {
        this.author = author;
        this.dateTime = dateTime;
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public String getAuthor() {
        return author;
    }

    public String getDateTime() {
        return dateTime;
    }
}
