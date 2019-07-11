package gr.thmmy.mthmmy.model;

public class PM {

    private String thumbnailUrl;
    private String author;
    private String authorProfileUrl;
    private String subject;
    private String content;
    private String pmDate;
    private String deleteUrl, replyUrl, quoteUrl;
    private int authorColor;
    private String authorGender;
    private String authorNumberOfPosts;
    private String authorRank, authorSpecialRank, authorPersonalText;
    private int authorNumberOfStars;
    private boolean isUserMentioned;

    public int getAuthorColor() {
        return authorColor;
    }

    public String getAuthorPersonalText() {
        return authorPersonalText;
    }

    public void setAuthorPersonalText(String authorPersonalText) {
        this.authorPersonalText = authorPersonalText;
    }

    public String getAuthorNumberOfPosts() {
        return authorNumberOfPosts;
    }

    public String getAuthorRank() {
        return authorRank;
    }

    public void setAuthorRank(String rank) {
        this.authorRank = rank;
    }

    public String getAuthorSpecialRank() {
        return authorSpecialRank;
    }

    public void setAuthorSpecialRank(String authorSpecialRank) {
        this.authorSpecialRank = authorSpecialRank;
    }

    public String getAuthorGender() {
        return authorGender;
    }

    public void setAuthorGender(String authorGender) {
        this.authorGender = authorGender;
    }

    public int getAuthorNumberOfStars() {
        return authorNumberOfStars;
    }

    public void setAuthorNumberOfStars(int authorNumberOfStars) {
        this.authorNumberOfStars = authorNumberOfStars;
    }

    public void setAuthorNumberOfPosts(String authorNumberOfPosts) {
        this.authorNumberOfPosts = authorNumberOfPosts;
    }

    public String getReplyUrl() {
        return replyUrl;
    }

    public void setReplyUrl(String replyUrl) {
        this.replyUrl = replyUrl;
    }

    public String getQuoteUrl() {
        return quoteUrl;
    }

    public void setQuoteUrl(String quoteUrl) {
        this.quoteUrl = quoteUrl;
    }

    public String getDeleteUrl() {
        return deleteUrl;
    }

    public void setDeleteUrl(String deleteUrl) {
        this.deleteUrl = deleteUrl;
    }

    public void setAuthorColor(int authorColor) {
        this.authorColor = authorColor;
    }

    public boolean isUserMentioned() {
        return isUserMentioned;
    }

    public void setUserMentioned(boolean userMentioned) {
        isUserMentioned = userMentioned;
    }

    public String getAuthorProfileUrl() {
        return authorProfileUrl;
    }

    public void setAuthorProfileUrl(String authorProfileUrl) {
        this.authorProfileUrl = authorProfileUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setPmDate(String pmDate) {
        this.pmDate = pmDate;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public String getSubject() {
        return subject;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getPmDate() {
        return pmDate;
    }
}
