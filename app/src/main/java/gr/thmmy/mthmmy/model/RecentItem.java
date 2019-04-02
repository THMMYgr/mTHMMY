package gr.thmmy.mthmmy.model;

import java.util.Date;
import java.util.Map;

public class RecentItem {
    private int boardId, postId, topicId, posterId;
    private String boardTitle, topicTitle, poster;
    private long timestamp;

    public RecentItem(Map<String, Object> map) {
        this.boardId = ((Long) map.get("boardId")).intValue();
        this.postId = ((Long) map.get("postId")).intValue();
        this.poster = String.valueOf(map.get("poster"));
        this.posterId = ((Long) map.get("posterId")).intValue();
        this.topicId = ((Long) map.get("topicId")).intValue();
        this.boardTitle = String.valueOf(map.get("boardTitle"));
        this.topicTitle = String.valueOf(map.get("topicTitle"));
        this.timestamp = (long)(map.get("timestamp")) * 1000;
    }

    public long getTimestamp() {
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
