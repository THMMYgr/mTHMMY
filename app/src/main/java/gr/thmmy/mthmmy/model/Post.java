package gr.thmmy.mthmmy.model;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Class that defines a topic's post. All member variables are declared final (thus no setters are
 * supplied). Class has two constructors and getter methods for all variables.
 * <p>A post is described by its author's thumbnail image url, author's username, its subject, its
 * content, its index on the forum, its (index) number on the topic, its date of post, author's
 * user color and a list of its attached files <b>when post's author is a deleted user</b>.</p>
 * <p>When the author is an active user, post also needs author's profile url, rank and special rank,
 * gender, number of posts, personal text and number of start to be described <b>in addition to
 * previous fields</b>.</p>
 */
public class Post extends TopicItem {
    public static final int TYPE_POST = 0;
    public static final int TYPE_QUICK_REPLY = 1;
    public static final int TYPE_EDIT = 2;

    //Standard info (exists in every post)
    private final String thumbnailUrl;
    private final String author;
    private String subject;
    private String content;
    private String bbContent;
    private final int postIndex;
    private final int postNumber;
    private final String postDate;
    private final boolean isDeleted;
    private final int userColor;
    private final ArrayList<ThmmyFile> attachedFiles;
    private final String lastEdit;
    private final String postURL;
    private final String postDeleteURL;
    private final String postEditURL;
    private int postType;

    //Extra info
    private final String profileURL;
    private final String rank;
    private final String specialRank;
    private final String gender;
    private final String numberOfPosts;
    private final String personalText;
    private final int numberOfStars;
    private final boolean isUserMentionedInPost;
    private final boolean isUserOnline;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    private Post(String bbContent) {
        this.bbContent = bbContent;
        thumbnailUrl = "";
        author = null;
        subject = null;
        content = null;
        postIndex = -1;
        postNumber = -1;
        postDate = null;
        isDeleted = true;
        profileURL = null;
        userColor = -1;
        rank = "Rank";
        specialRank = "Special rank";
        gender = "Gender";
        numberOfPosts = "Posts: 0";
        personalText = "";
        numberOfStars = 0;
        attachedFiles = null;
        lastEdit = null;
        postURL = null;
        postDeleteURL = null;
        postEditURL = null;
        isUserOnline = false;
        isUserMentionedInPost = false;
        postType = -1;
    }

    /**
     * Constructor for active user's posts. All variables are declared final, once assigned they
     * can not change. Parameters notated as {@link Nullable} can either pass null or empty
     * (strings/ArrayList).
     *
     * @param thumbnailUrl  author's thumbnail url
     * @param author        author's username
     * @param subject       post's subject
     * @param content       post itself
     * @param bbContent
     * @param postIndex     post's index on the forum
     * @param postNumber    posts index number on this topic
     * @param postDate      date of submission
     * @param profileURl    author's profile url
     * @param rank          author's rank
     * @param special_rank  author's special rank
     * @param gender        author's gender
     * @param numberOfPosts author's number of posts
     * @param personalText  author's personal text
     * @param numberOfStars author's number of stars
     * @param userColor     author's user color
     * @param attachedFiles post's attached files
     * @param lastEdit      post's last edit date
     * @param postURL       post's URL
     * @param isUserOnline  author's online status
     */
    public Post(@Nullable String thumbnailUrl, String author, String subject, String content
            , String bbContent, int postIndex, int postNumber, String postDate, String profileURl, @Nullable String rank
            , @Nullable String special_rank, @Nullable String gender, @Nullable String numberOfPosts
            , @Nullable String personalText, int numberOfStars, int userColor
            , @Nullable ArrayList<ThmmyFile> attachedFiles, @Nullable String lastEdit, String postURL
            , @Nullable String postDeleteURL, @Nullable String postEditURL, boolean isUserOnline, boolean isUserMentionedInPost
            , int postType) {
        this.bbContent = bbContent;
        if (Objects.equals(thumbnailUrl, "")) this.thumbnailUrl = null;
        else this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.postIndex = postIndex;
        this.postNumber = postNumber;
        this.postDate = postDate;
        this.isDeleted = false;
        this.userColor = userColor;
        this.attachedFiles = attachedFiles;
        this.lastEdit = lastEdit;
        this.profileURL = profileURl;
        this.rank = rank;
        this.specialRank = special_rank;
        this.gender = gender;
        this.numberOfPosts = numberOfPosts;
        this.personalText = personalText;
        this.numberOfStars = numberOfStars;
        this.postURL = postURL;
        this.postDeleteURL = postDeleteURL;
        this.postEditURL = postEditURL;
        this.isUserOnline = isUserOnline;
        this.isUserMentionedInPost = isUserMentionedInPost;
        this.postType = postType;
    }

    /**
     * Constructor for deleted user's posts. All variables are declared final, once assigned they
     * can not change. Parameters notated as {@link Nullable} can either pass null or empty
     * (strings/ArrayList).
     *
     * @param thumbnailUrl  author's thumbnail url
     * @param author        author's username
     * @param subject       post's subject
     * @param content       post itself
     * @param bbContent     post content in bb form
     * @param postIndex     post's index on the forum
     * @param postNumber    posts index number on this topic
     * @param postDate      date of submission
     * @param userColor     author's user color
     * @param attachedFiles post's attached files
     * @param lastEdit      post's last edit date
     * @param postURL       post's URL
     */
    public Post(@Nullable String thumbnailUrl, String author, String subject, String content
            , String bbContent, int postIndex, int postNumber, String postDate, int userColor
            , @Nullable ArrayList<ThmmyFile> attachedFiles, @Nullable String lastEdit, String postURL
            , @Nullable String postDeleteURL, @Nullable String postEditURL, boolean isUserMentionedInPost
            , int postType) {
        this.bbContent = bbContent;
        if (Objects.equals(thumbnailUrl, "")) this.thumbnailUrl = null;
        else this.thumbnailUrl = thumbnailUrl;
        this.author = author;
        this.subject = subject;
        this.content = content;
        this.postIndex = postIndex;
        this.postNumber = postNumber;
        this.postDate = postDate;
        this.isDeleted = true;
        this.userColor = userColor;
        this.attachedFiles = attachedFiles;
        this.lastEdit = lastEdit;
        profileURL = null;
        rank = "Rank";
        specialRank = "Special rank";
        gender = "Gender";
        numberOfPosts = "Posts: 0";
        personalText = "";
        numberOfStars = 0;
        this.postURL = postURL;
        this.postDeleteURL = postDeleteURL;
        this.postEditURL = postEditURL;
        this.isUserOnline = false;
        this.isUserMentionedInPost = isUserMentionedInPost;
        this.postType = postType;
    }

    public static Post newQuickReply() {
        return new Post(null, null, null, null, null, 0, 0, null,
                0, null, null, null, null, null, false, TYPE_QUICK_REPLY);
    }

    public static Post newQuickReply(String subject, String content) {
        return new Post(null, null, subject, null, content, 0, 0, null,
                0, null, null, null, null, null, false, TYPE_QUICK_REPLY);
    }

    //Getters

    /**
     * Gets this post author's thumbnail url.
     *
     * @return author's thumbnail url
     */
    @Nullable
    public String getThumbnailURL() {
        return thumbnailUrl;
    }

    /**
     * Gets this post
     *
     * @return post's content
     */
    @Nullable
    public String getContent() {
        return content;
    }

    public String getBbContent() {
        return bbContent;
    }

    public void setBbContent(String bbContent) {
        this.bbContent = bbContent;
    }

    /**
     * Gets this post's author.
     *
     * @return post's author
     */

    @Nullable
    public String getAuthor() {
        return author;
    }

    /**
     * Gets this post's subject.
     *
     * @return post's subject
     */
    @Nullable
    public String getSubject() {
        return subject;
    }

    /**
     * Gets this post's date of submission.
     *
     * @return post's date
     */
    @Nullable
    public String getPostDate() {
        return postDate;
    }

    /**
     * Gets post's index number on this topic.
     *
     * @return post's number on topic
     */
    public int getPostNumber() {
        return postNumber;
    }

    /**
     * Gets this post's index on the forum.
     *
     * @return post's index on the forum
     */
    public int getPostIndex() {
        return postIndex;
    }

    /**
     * Is true if post's author is a deleted user, false otherwise.
     *
     * @return true is author is deleted, false otherwise
     */
    public boolean isDeleted() {
        return isDeleted;
    }

    /**
     * Gets this post's author profile url.
     *
     * @return author's profile url
     */
    @Nullable
    public String getProfileURL() {
        return profileURL;
    }

    /**
     * Gets this post's author rank.
     *
     * @return author's rank
     */
    @Nullable
    public String getRank() {
        return rank;
    }

    /**
     * Gets this post's author special rank.
     *
     * @return author's special rank
     */
    @Nullable
    public String getSpecialRank() {
        return specialRank;
    }

    /**
     * Gets this post's author gender.
     *
     * @return author's gender
     */
    @Nullable
    public String getGender() {
        return gender;
    }

    /**
     * Gets this post's author number of posts.
     *
     * @return author's number of posts
     */
    @Nullable
    public String getNumberOfPosts() {
        return numberOfPosts;
    }

    /**
     * Gets this post's author personal text.
     *
     * @return author's personal text
     */
    @Nullable
    public String getPersonalText() {
        return personalText;
    }

    /**
     * Gets this post's author number of stars.
     *
     * @return author's number of stars
     */
    public int getNumberOfStars() {
        return numberOfStars;
    }

    /**
     * Gets this post's author user color.
     *
     * @return author's user color
     */
    public int getUserColor() {
        return userColor;
    }

    /**
     * Gets the online status of this post's author.
     *
     * @return online status of this post's author
     */

    @Nullable
    public boolean getUserOnlineStatus() {
        return isUserOnline;
    }

    /**
     * Gets this post's attached files.
     *
     * @return attached files
     */
    @Nullable
    public ArrayList<ThmmyFile> getAttachedFiles() {
        return attachedFiles;
    }

    /**
     * Gets this post's last edit date or null if post hasn't been edited.
     *
     * @return date of last edit or null
     */
    @Nullable
    public String getLastEdit() {
        return lastEdit;
    }

    /**
     * Gets this post's url.
     *
     * @return post's url
     */
    @Nullable
    public String getPostURL() {
        return postURL;
    }

    /**
     * Gets this post's delete url.
     *
     * @return post's delete url
     */
    @Nullable
    public String getPostDeleteURL() {
        return postDeleteURL;
    }

    /**
     * Gets this post's modify url.
     *
     * @return post's edit url
     */
    @Nullable
    public String getPostEditURL() {
        return postEditURL;
    }

    public int getPostType() {
        return postType;
    }

    public boolean isUserMentionedInPost() {
        return isUserMentionedInPost;
    }

    public void setPostType(int postType) {
        this.postType = postType;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
