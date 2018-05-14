package gr.thmmy.mthmmy.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Bookmark;

import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

//TODO proper handling with adapter etc.
//TODO better UI
//TODO after clicking bookmark and then back button should return to this activity
public class BookmarkActivity extends BaseActivity {
    private TextView boardsTitle;
    private TextView topicsTitle;

    private static Drawable notificationsEnabledButtonImage;
    private static Drawable notificationsDisabledButtonImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmark);

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Bookmarks");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(BOOKMARKS_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationsEnabledButtonImage = getResources().getDrawable(R.drawable.ic_notification_on, null);
        } else {
            notificationsEnabledButtonImage = VectorDrawableCompat.create(getResources(), R.drawable.ic_notification_on, null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationsDisabledButtonImage = getResources().getDrawable(R.drawable.ic_notification_off, null);
        } else {
            notificationsDisabledButtonImage = VectorDrawableCompat.create(getResources(), R.drawable.ic_notification_off, null);
        }

        LinearLayout bookmarksLinearView = findViewById(R.id.bookmarks_container);
        LayoutInflater layoutInflater = getLayoutInflater();

        if(!getBoardsBookmarked().isEmpty()) {
            boardsTitle = new TextView(this);
            boardsTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT
                    , LinearLayout.LayoutParams.WRAP_CONTENT));
            boardsTitle.setText(getString(R.string.board_bookmarks_title));
            boardsTitle.setTypeface(boardsTitle.getTypeface(), Typeface.BOLD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                boardsTitle.setTextColor(getColor(R.color.primary_text));
            } else {
                //noinspection deprecation
                boardsTitle.setTextColor(getResources().getColor(R.color.primary_text));
            }
            boardsTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            boardsTitle.setTextSize(20f);
            bookmarksLinearView.addView(boardsTitle);

            for (final Bookmark bookmarkedBoard : getBoardsBookmarked()) {
                if (bookmarkedBoard != null && bookmarkedBoard.getTitle() != null) {
                    final LinearLayout row = (LinearLayout) layoutInflater.inflate(
                            R.layout.activity_bookmark_board_row, bookmarksLinearView, false);
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
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
                            updateTitles();
                        }
                    });
                    bookmarksLinearView.addView(row);
                }
            }
        }


        if(!getTopicsBookmarked().isEmpty()) {
            topicsTitle = new TextView(this);
            topicsTitle.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT
                    , LinearLayout.LayoutParams.WRAP_CONTENT));
            topicsTitle.setText(getString(R.string.topic_bookmarks_title));
            topicsTitle.setTypeface(topicsTitle.getTypeface(), Typeface.BOLD);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                topicsTitle.setTextColor(getColor(R.color.primary_text));
            } else {
                //noinspection deprecation
                topicsTitle.setTextColor(getResources().getColor(R.color.primary_text));
            }
            topicsTitle.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            topicsTitle.setTextSize(20f);
            bookmarksLinearView.addView(topicsTitle);

            for (final Bookmark bookmarkedTopic : getTopicsBookmarked()) {
                if (bookmarkedTopic != null && bookmarkedTopic.getTitle() != null) {
                    final LinearLayout row = (LinearLayout) layoutInflater.inflate(
                            R.layout.activity_bookmark_topic_row, bookmarksLinearView, false);
                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(BookmarkActivity.this, TopicActivity.class);
                            Bundle extras = new Bundle();
                            extras.putString(BUNDLE_TOPIC_URL, "https://www.thmmy.gr/smf/index.php?topic="
                                    + bookmarkedTopic.getId() + "." + 2147483647);
                            extras.putString(BUNDLE_TOPIC_TITLE, bookmarkedTopic.getTitle());
                            intent.putExtras(extras);
                            startActivity(intent);
                            finish();
                        }
                    });
                    ((TextView) row.findViewById(R.id.bookmark_title)).setText(bookmarkedTopic.getTitle());

                    final ImageButton notificationsEnabledButton = row.findViewById(R.id.toggle_notification);
                    if (!bookmarkedTopic.isNotificationsEnabled()){
                        notificationsEnabledButton.setImageDrawable(notificationsDisabledButtonImage);
                    }

                    notificationsEnabledButton.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if(toggleNotification(bookmarkedTopic)){
                                notificationsEnabledButton.setImageDrawable(notificationsEnabledButtonImage);
                            } else {
                                notificationsEnabledButton.setImageDrawable(notificationsDisabledButtonImage);
                            }
                        }
                    });
                    (row.findViewById(R.id.remove_bookmark)).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            removeBookmark(bookmarkedTopic);
                            row.setVisibility(View.GONE);
                            Toast.makeText(BookmarkActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                            updateTitles();
                        }
                    });
                    bookmarksLinearView.addView(row);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        drawer.setSelection(BOOKMARKS_ID);
        super.onResume();
    }

    private void updateTitles()
    {
        if(getBoardsBookmarked().isEmpty()&&boardsTitle!=null)
            boardsTitle.setVisibility(View.GONE);
        if(getTopicsBookmarked().isEmpty()&&topicsTitle!=null)
            topicsTitle.setVisibility(View.GONE);
    }
}
