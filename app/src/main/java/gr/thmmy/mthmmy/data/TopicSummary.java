package gr.thmmy.mthmmy.data;

public class TopicSummary {
    private final String topicUrl;
    private final String title;
    private final String lastUser;
    private final String dateTimeModified;
    private final String post;


    public TopicSummary(String topicUrl, String title, String lastUser, String dateTimeModified) {
        this.topicUrl = topicUrl;
        this.title = title;
        this.lastUser = lastUser;
        this.dateTimeModified = dateTimeModified;
        this.post = "";
    }

    public TopicSummary(String topicUrl, String title, String username, String dateTimeModified,
                        String post) {
        this.topicUrl = topicUrl;
        this.title = title;
        this.lastUser = username;
        this.dateTimeModified = dateTimeModified;
        this.post = post;
    }

    public String getTopicUrl() {
        return topicUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getLastUser() {
        return lastUser;
    }

    public String getDateTimeModified() {
        return dateTimeModified;
    }

    public String getPost(){ return post;}
}
