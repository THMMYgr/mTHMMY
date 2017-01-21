package gr.thmmy.mthmmy.activities;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Bookmark;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class BookmarkActivity extends BaseActivity {
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        //Initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("Bookmarks");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(BOOKMARKS_ID);

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);

        LinearLayout bookmarksLinearView = (LinearLayout) findViewById(R.id.bookmarks_container);

        TextView tmp = new TextView(this);
        tmp.setText("Your bookmarked boards:");
        bookmarksLinearView.addView(tmp);

        for (Bookmark bookmarkedBoard : getBoardsBookmarked()) {
            if (bookmarkedBoard != null && bookmarkedBoard.getTitle() != null) {
                TextView bookmarkedBoardView = new TextView(this);
                bookmarkedBoardView.setText(bookmarkedBoard.getTitle());
                bookmarksLinearView.addView(bookmarkedBoardView);
            }
        }

        tmp = new TextView(this);
        tmp.setText("Your bookmarked topics:");
        bookmarksLinearView.addView(tmp);

        for (Bookmark bookmarkedTopic : getTopicsBookmarked()) {
            if (bookmarkedTopic != null && bookmarkedTopic.getTitle() != null) {
                TextView bookmarkedTopicView = new TextView(this);
                bookmarkedTopicView.setText(bookmarkedTopic.getTitle());
                bookmarksLinearView.addView(bookmarkedTopicView);
            }
        }
    }
}
