package gr.thmmy.mthmmy.data;

public class Post {
    //Standard info
    private final String thumbnailUrl;
    private final String author;
    private final String subject;
    private final String content;
    private final int postIndex;

    //Extra info
    private final int postNumber;
    private final String postDate;
    private final boolean isDeleted;
    private final String rank;
    private final String specialRank;
    private final String gender;
    private final String numberOfPosts;
    private final String personalText;
    private final String urlOfStars;
    private int numberOfStars;

    public Post(String thumbnailUrl, String author, String subject, String content
            , int postIndex, int postNumber, String postDate, String rank
            , String special_rank, String gender, String numberOfPosts
            , String personalText, String urlOfStars, int numberOfStars) {
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.postIndex = postIndex;
        this.postNumber = postNumber;
        this.postDate = postDate;
        this.isDeleted = false;
        this.rank = rank;
        this.specialRank = special_rank;
        this.gender = gender;
        this.numberOfPosts = numberOfPosts;
        this.personalText = personalText;
        this.urlOfStars = urlOfStars;
        this.numberOfStars = numberOfStars;
    }

    public Post(String thumbnailUrl, String author, String subject, String content
            , int postIndex, int postNumber, String postDate) {
        this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.postIndex = postIndex;
        this.postNumber = postNumber;
        this.postDate = postDate;
        this.isDeleted = true;
        rank = "Rank";
        specialRank = "Special rank";
        gender = "Gender";
        numberOfPosts = "Posts: 0";
        personalText = "";
        urlOfStars = "";
    }

    //Getters
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

    public int getPostIndex() { return postIndex;}

    public boolean isDeleted() {
        return isDeleted;
    }

    public String getRank() {return rank; }

    public String getSpecialRank() {
        return specialRank;
    }

    public String getGender() {
        return gender;
    }

    public String getNumberOfPosts() {
        return numberOfPosts;
    }

    public String getPersonalText() {
        return personalText;
    }

    public String getUrlOfStars() {return urlOfStars; }

    public int getNumberOfStars() {return numberOfStars; }
}
