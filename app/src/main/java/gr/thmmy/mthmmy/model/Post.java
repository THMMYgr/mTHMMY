package gr.thmmy.mthmmy.model;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.utils.FileManager.ThmmyFile;

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
public class Post {
    //Standard info (exists in every post)
    private final String thumbnailUrl;
    private final String author;
    private final String subject;
    private final String content;
    private final int postIndex;
    private final int postNumber;
    private final String postDate;
    private final boolean isDeleted;
    private final int userColor;
    private final ArrayList<ThmmyFile> attachedFiles;

    //Extra info
    private final String profileURL;
    private final String rank;
    private final String specialRank;
    private final String gender;
    private final String numberOfPosts;
    private final String personalText;
    private final int numberOfStars;

    // Suppresses default constructor
    @SuppressWarnings("unused")
    private Post() {
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
     */
    public Post(@Nullable String thumbnailUrl, String author, String subject, String content
            , int postIndex, int postNumber, String postDate, String profileURl, @Nullable String rank
            , @Nullable String special_rank, @Nullable String gender, @Nullable String numberOfPosts
            , @Nullable String personalText, int numberOfStars, int userColor
            , @Nullable ArrayList<ThmmyFile> attachedFiles) {
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
        this.profileURL = profileURl;
        this.rank = rank;
        this.specialRank = special_rank;
        this.gender = gender;
        this.numberOfPosts = numberOfPosts;
        this.personalText = personalText;
        this.numberOfStars = numberOfStars;
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
     * @param postIndex     post's index on the forum
     * @param postNumber    posts index number on this topic
     * @param postDate      date of submission
     * @param userColor     author's user color
     * @param attachedFiles post's attached files
     */
    public Post(@Nullable String thumbnailUrl, String author, String subject, String content
            , int postIndex, int postNumber, String postDate, int userColor
            , @Nullable ArrayList<ThmmyFile> attachedFiles) {
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
        profileURL = null;
        rank = "Rank";
        specialRank = "Special rank";
        gender = "Gender";
        numberOfPosts = "Posts: 0";
        personalText = "";
        numberOfStars = 0;
    }

    //Getters

    /**
     * Gets this post author's thumbnail url.
     *
     * @return author's thumbnail url
     */
    @Nullable
    public String getThumbnailUrl() {
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
     * Gets this post's attached files.
     *
     * @return attached files
     */
    @Nullable
    public ArrayList<ThmmyFile> getAttachedFiles() {
        return attachedFiles;
    }
}