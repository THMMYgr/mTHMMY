package gr.thmmy.mthmmy.data;

import com.bignerdranch.expandablerecyclerview.model.Parent;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.id;

public class Category implements Parent<Board>
{
    private final String name;
    private final String categoryURL;
    private boolean expanded = false;
    private List<Board> boards;

    public Category(String name, String categoryURL) {
        this.name = name;
        this.categoryURL = categoryURL;
        boards = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
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
