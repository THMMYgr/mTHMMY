package gr.thmmy.mthmmy.model;

public class PostSummary {
    private final String topicUrl;
    private final String title;
    private final String dateTime;
    private final String post;

    public PostSummary(String topicUrl, String title, String dateTime,
                        String post) {
        this.topicUrl = topicUrl;
        this.title = title;
        this.dateTime = dateTime;
        this.post = post;
    }

    public String getTopicUrl() {
        return topicUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getPost(){ return post;}
}
