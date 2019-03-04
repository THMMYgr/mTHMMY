package gr.thmmy.mthmmy.model;

import java.util.Date;

public class RecentItem {
    private int boardId, postId, topicId, posterId;
    private String boardTitle, topicTitle, poster;
    private Date timestamp;

    public RecentItem(int boardId, String boardTitle, int postId, String poster, int posterId, int timestamp,
                      int topicId, String topicTitle) {
        this.boardId = boardId;
        this.postId = postId;
        this.poster = poster;
        this.posterId = posterId;
        this.topicId = topicId;
        this.boardTitle = boardTitle;
        this.topicTitle = topicTitle;
        this.timestamp = new Date(timestamp);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getBoardId() {
        return boardId;
    }

    public int getPosterId() {
        return posterId;
    }

    public int getPostId() {
        return postId;
    }

    public int getTopicId() {
        return topicId;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public String getPoster() {
        return poster;
    }

    public String getTopicTitle() {
        return topicTitle;
    }
}
