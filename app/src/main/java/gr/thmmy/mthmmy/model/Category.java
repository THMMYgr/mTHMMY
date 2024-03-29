package gr.thmmy.mthmmy.model;

import static android.R.attr.id;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.ArrayList;
import java.util.List;

public class Category implements Parent<Board> {
    private final String title;
    private final String categoryURL;
    private boolean expanded = false;
    private final List<Board> boards;

    public Category(String title, String categoryURL) {
        this.title = title;
        this.categoryURL = categoryURL;
        boards = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getCategoryURL() {
        return categoryURL;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public List<Board> getBoards() {
        return boards;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    @Override
    public List<Board> getChildList() {
        return getBoards();
    }

    @Override
    public boolean isInitiallyExpanded() {
        return expanded;
    }


}
