package gr.thmmy.mthmmy.data;

import java.util.ArrayList;

public class Board {
    private final String title;
    private final String boardURL;

    private ArrayList <Board> subBoards;
    private ArrayList <TopicSummary> topicSummaries;

    public Board(String title, String boardURL) {
        this.title = title;
        this.boardURL = boardURL;
        subBoards = new ArrayList<>();
        topicSummaries = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getBoardURL() {
        return boardURL;
    }

    public ArrayList<Board> getSubBoards() {
        return subBoards;
    }

    public ArrayList<TopicSummary> getTopicSummaries() {
        return topicSummaries;
    }
}
