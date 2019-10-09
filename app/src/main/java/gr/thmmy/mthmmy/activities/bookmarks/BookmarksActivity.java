package gr.thmmy.mthmmy.activities.bookmarks;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.activities.widget.WidgetProvider;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Bookmark;

import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;
import static gr.thmmy.mthmmy.activities.widget.WidgetProvider.BOOKMARK_WIDGETS_KEY;
import static gr.thmmy.mthmmy.activities.widget.WidgetProvider.BOOKMARK_WIDGET_SHARED_PREFS;
import static gr.thmmy.mthmmy.session.SessionManager.boardUrl;
import static gr.thmmy.mthmmy.session.SessionManager.topicUrl;

//TODO proper handling with adapter etc.
//TODO after clicking bookmark and then back button should return to this activity
public class BookmarksActivity extends BaseActivity {
    private boolean isCalledForWidgetSetup = false;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            isCalledForWidgetSetup = true;

            // Finds the widget id from the intent.
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            // Sets the result to CANCELED. In case the activity is started from intent to setup a
            // widget, this will cause the widget host to cancel out of the widget placement if they
            // press the back button.
            setResult(RESULT_CANCELED);

            // If they gave us an intent without the widget id, just bail.
            if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                finish();
            }
        }

        setContentView(R.layout.activity_bookmarks);

        if (!isCalledForWidgetSetup) {
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
        }

        //Creates the adapter that will return a fragment for each section of the activity
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.addFragment(BookmarksFragment.newInstance(1,
                Bookmark.arrayListToString(getTopicsBookmarked()),
                BookmarksFragment.Type.TOPIC, isCalledForWidgetSetup), "Topics");
        sectionsPagerAdapter.addFragment(BookmarksFragment.newInstance(2,
                Bookmark.arrayListToString(getBoardsBookmarked()),
                BookmarksFragment.Type.BOARD, isCalledForWidgetSetup), "Boards");

        //Sets up the ViewPager with the sections adapter.
        ViewPager viewPager = findViewById(R.id.bookmarks_container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.bookmark_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        if (drawer != null)
            drawer.setSelection(BOOKMARKS_ID);
        super.onResume();
    }

    public boolean onFragmentRowInteractionListener(BookmarksFragment.Type type, String interactionType, Bookmark bookmark) {
        if (type == BookmarksFragment.Type.TOPIC)
            return onTopicInteractionListener(interactionType, bookmark);
        else if (type == BookmarksFragment.Type.BOARD)
            return onBoardInteractionListener(interactionType, bookmark);

        return false;
    }

    private boolean onTopicInteractionListener(String interactionType, Bookmark bookmarkedTopic) {
        // Handles clicks during widget setups
        if (isCalledForWidgetSetup) {
            handleWidgetCreation(BookmarksFragment.Type.TOPIC, bookmarkedTopic);
            return true;
        }

        // Default behavior
        switch (interactionType) {
            case BookmarksFragment.INTERACTION_CLICK_TOPIC_BOOKMARK:
                Intent intent = new Intent(BookmarksActivity.this, TopicActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_TOPIC_URL, topicUrl
                        + bookmarkedTopic.getId() + "." + 2147483647);
                extras.putString(BUNDLE_TOPIC_TITLE, bookmarkedTopic.getTitle());
                intent.putExtras(extras);
                startActivity(intent);
                break;
            case BookmarksFragment.INTERACTION_TOGGLE_TOPIC_NOTIFICATION:
                return toggleNotification(bookmarkedTopic);
            case BookmarksFragment.INTERACTION_REMOVE_TOPIC_BOOKMARK:
                removeBookmark(bookmarkedTopic);
                Toast.makeText(BookmarksActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    private boolean onBoardInteractionListener(String interactionType, Bookmark bookmarkedBoard) {
        // Handles clicks during widget setups
        if (isCalledForWidgetSetup) {
            handleWidgetCreation(BookmarksFragment.Type.BOARD, bookmarkedBoard);
            return true;
        }

        // Default behavior
        switch (interactionType) {
            case BookmarksFragment.INTERACTION_CLICK_BOARD_BOOKMARK:
                Intent intent = new Intent(BookmarksActivity.this, BoardActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_BOARD_URL, boardUrl
                        + bookmarkedBoard.getId() + ".0");
                extras.putString(BUNDLE_BOARD_TITLE, bookmarkedBoard.getTitle());
                intent.putExtras(extras);
                startActivity(intent);
                break;
            case BookmarksFragment.INTERACTION_TOGGLE_BOARD_NOTIFICATION:
                return toggleNotification(bookmarkedBoard);
            case BookmarksFragment.INTERACTION_REMOVE_BOARD_BOOKMARK:
                removeBookmark(bookmarkedBoard);
                Toast.makeText(BookmarksActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return true;
    }

    private void handleWidgetCreation(BookmarksFragment.Type type, Bookmark bookmark) {
        // Saves the bookmark in our prefs
        SharedPreferences widgetSharedPrefs = getSharedPreferences(BOOKMARK_WIDGET_SHARED_PREFS, Context.MODE_PRIVATE);
        ArrayList<Bookmark> tmpArrayList = new ArrayList<>();
        tmpArrayList.add(bookmark);
        SharedPreferences.Editor widgetSharedPrefsEditor = widgetSharedPrefs.edit();

        if (type == BookmarksFragment.Type.TOPIC) {
            widgetSharedPrefsEditor.putString(BOOKMARK_WIDGETS_KEY + "_t_" + mAppWidgetId, Bookmark.arrayListToString(tmpArrayList));
        } else if (type == BookmarksFragment.Type.BOARD) {
            widgetSharedPrefsEditor.putString(BOOKMARK_WIDGETS_KEY + "_b_" + mAppWidgetId, Bookmark.arrayListToString(tmpArrayList));
        } else {
            finish();
        }
        widgetSharedPrefsEditor.apply();

        // Push widget update to surface
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        WidgetProvider.updateAppWidget(this, appWidgetManager, mAppWidgetId, 0); // Todo: check if there are already notifications available

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages. If it becomes too memory intensive,
     * it may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        @Override
        public int getItemPosition(@NonNull Object object) {
            @SuppressWarnings("RedundantCast")
            int position = fragmentList.indexOf((Fragment) object);
            return position == -1 ? POSITION_NONE : position;
        }
    }
}
