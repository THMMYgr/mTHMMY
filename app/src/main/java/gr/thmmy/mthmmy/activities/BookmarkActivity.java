package gr.thmmy.mthmmy.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Bookmark;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

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
        LayoutInflater layoutInflater = getLayoutInflater();

        TextView tmp = new TextView(this);
        tmp.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT));
        tmp.setText(getString(R.string.board_bookmarks_title));
        tmp.setTypeface(tmp.getTypeface(), Typeface.BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tmp.setTextColor(getColor(R.color.primary_text));
        } else {
            //noinspection deprecation
            tmp.setTextColor(getResources().getColor(R.color.primary_text));
        }
        tmp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tmp.setTextSize(20f);
        bookmarksLinearView.addView(tmp);

        for (final Bookmark bookmarkedBoard : getBoardsBookmarked()) {
            if (bookmarkedBoard != null && bookmarkedBoard.getTitle() != null) {
                Log.d("TAG", bookmarkedBoard.getTitle() + " - " + bookmarkedBoard.getId());
                final LinearLayout row = (LinearLayout) layoutInflater.inflate(
                        R.layout.activity_bookmark_row, bookmarksLinearView, false);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.d("TAG", "https://www.thmmy.gr/smf/index.php?board="
                                + bookmarkedBoard.getId() + ".0");
                        Log.d("TAG", bookmarkedBoard.getTitle());
                        Intent intent = new Intent(BookmarkActivity.this, BoardActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(BUNDLE_BOARD_URL, "https://www.thmmy.gr/smf/index.php?board="
                                + bookmarkedBoard.getId() + ".0");
                        extras.putString(BUNDLE_BOARD_TITLE, bookmarkedBoard.getTitle());
                        intent.putExtras(extras);
                        startActivity(intent);
                        finish();
                    }
                });
                ((TextView) row.findViewById(R.id.bookmark_title)).setText(bookmarkedBoard.getTitle());
                (row.findViewById(R.id.remove_bookmark)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeBookmark(bookmarkedBoard);
                        row.setVisibility(View.GONE);
                    }
                });
                bookmarksLinearView.addView(row);
            }
        }

        tmp = new TextView(this);
        tmp.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT
                , LinearLayout.LayoutParams.WRAP_CONTENT));
        tmp.setText(getString(R.string.topic_bookmarks_title));
        tmp.setTypeface(tmp.getTypeface(), Typeface.BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            tmp.setTextColor(getColor(R.color.primary_text));
        } else {
            //noinspection deprecation
            tmp.setTextColor(getResources().getColor(R.color.primary_text));
        }
        tmp.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        tmp.setTextSize(20f);
        bookmarksLinearView.addView(tmp);

        for (final Bookmark bookmarkedTopic : getTopicsBookmarked()) {
            if (bookmarkedTopic != null && bookmarkedTopic.getTitle() != null) {
                Log.d("TAG", bookmarkedTopic.getTitle() + " - " + bookmarkedTopic.getId());
                final LinearLayout row = (LinearLayout) layoutInflater.inflate(
                        R.layout.activity_bookmark_row, bookmarksLinearView, false);
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(BookmarkActivity.this, TopicActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(BUNDLE_TOPIC_URL, "https://www.thmmy.gr/smf/index.php?topic="
                                + bookmarkedTopic.getId() + ".0");
                        extras.putString(BUNDLE_TOPIC_TITLE, bookmarkedTopic.getTitle());
                        intent.putExtras(extras);
                        startActivity(intent);
                        finish();
                    }
                });
                ((TextView) row.findViewById(R.id.bookmark_title)).setText(bookmarkedTopic.getTitle());
                (row.findViewById(R.id.remove_bookmark)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        removeBookmark(bookmarkedTopic);
                        row.setVisibility(View.GONE);
                    }
                });
                bookmarksLinearView.addView(row);
            }
        }
    }
}
