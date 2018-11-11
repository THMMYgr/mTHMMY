package gr.thmmy.mthmmy.activities.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
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
//TODO after clicking bookmark and then back button should return to this activity
public class BookmarksActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);

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

        //Creates the adapter that will return a fragment for each section of the activity
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.addFragment(BookmarksTopicFragment.newInstance(1, Bookmark.arrayToString(getTopicsBookmarked())), "Topics");
        sectionsPagerAdapter.addFragment(BookmarksBoardFragment.newInstance(2, Bookmark.arrayToString(getBoardsBookmarked())), "Boards");

        //Sets up the ViewPager with the sections adapter.
        ViewPager viewPager = findViewById(R.id.bookmarks_container);
        viewPager.setAdapter(sectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.bookmark_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        drawer.setSelection(BOOKMARKS_ID);
        super.onResume();
    }

    public boolean onTopicInteractionListener(String interactionType, Bookmark bookmarkedTopic) {
        switch (interactionType) {
            case BookmarksTopicFragment.INTERACTION_CLICK_TOPIC_BOOKMARK:
                Intent intent = new Intent(BookmarksActivity.this, TopicActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_TOPIC_URL, "https://www.thmmy.gr/smf/index.php?topic="
                        + bookmarkedTopic.getId() + "." + 2147483647);
                extras.putString(BUNDLE_TOPIC_TITLE, bookmarkedTopic.getTitle());
                intent.putExtras(extras);
                startActivity(intent);
                break;
            case BookmarksTopicFragment.INTERACTION_TOGGLE_TOPIC_NOTIFICATION:
                return toggleNotification(bookmarkedTopic);
            case BookmarksTopicFragment.INTERACTION_REMOVE_TOPIC_BOOKMARK:
                removeBookmark(bookmarkedTopic);
                Toast.makeText(BookmarksActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    public boolean onBoardInteractionListener(String interactionType, Bookmark bookmarkedBoard) {
        switch (interactionType) {
            case BookmarksBoardFragment.INTERACTION_CLICK_BOARD_BOOKMARK:
                Intent intent = new Intent(BookmarksActivity.this, BoardActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_BOARD_URL, "https://www.thmmy.gr/smf/index.php?board="
                        + bookmarkedBoard.getId() + ".0");
                extras.putString(BUNDLE_BOARD_TITLE, bookmarkedBoard.getTitle());
                intent.putExtras(extras);
                startActivity(intent);
                break;
            case BookmarksBoardFragment.INTERACTION_TOGGLE_BOARD_NOTIFICATION:
                return toggleNotification(bookmarkedBoard);
            case BookmarksBoardFragment.INTERACTION_REMOVE_BOARD_BOOKMARK:
                removeBookmark(bookmarkedBoard);
                Toast.makeText(BookmarksActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
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
        public int getItemPosition(Object object) {
            @SuppressWarnings("RedundantCast")
            int position = fragmentList.indexOf((Fragment) object);
            return position == -1 ? POSITION_NONE : position;
        }
    }
}