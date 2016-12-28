package gr.thmmy.mthmmy.data;

import java.util.ArrayList;

public class Board {
    private final String name;
    private final String boardURL;

    private ArrayList <Board> subBoards;
    private ArrayList <TopicSummary> topicSummaries;

    public Board(String name, String boardURL) {
        this.name = name;
        this.boardURL = boardURL;
        subBoards = new ArrayList<>();
        topicSummaries = new ArrayList<>();
    }

    public String getName() {
        return name;
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
