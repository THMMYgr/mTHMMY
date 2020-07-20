package gr.thmmy.mthmmy.activities.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

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
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Bookmark;

import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

public class BookmarksActivity extends BaseActivity {
    private static final String TOPIC_URL = "https://www.thmmy.gr/smf/index.php?topic=";
    private static final String BOARD_URL = "https://www.thmmy.gr/smf/index.php?board=";

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
        sectionsPagerAdapter.addFragment(BookmarksFragment.newInstance(1, Bookmark.arrayListToString(getTopicsBookmarked()), BookmarksFragment.Type.TOPIC), "Topics");
        sectionsPagerAdapter.addFragment(BookmarksFragment.newInstance(2, Bookmark.arrayListToString(getBoardsBookmarked()), BookmarksFragment.Type.BOARD), "Boards");

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

    public boolean onFragmentRowInteractionListener(BookmarksFragment.Type type, String interactionType, Bookmark bookmark) {
        if(type== BookmarksFragment.Type.TOPIC)
            return onTopicInteractionListener(interactionType, bookmark);
        else if (type==BookmarksFragment.Type.BOARD)
            return onBoardInteractionListener(interactionType, bookmark);

        return false;
    }

    private boolean onTopicInteractionListener(String interactionType, Bookmark bookmarkedTopic) {
        switch (interactionType) {
            case BookmarksFragment.INTERACTION_CLICK_TOPIC_BOOKMARK:
                Intent intent = new Intent(BookmarksActivity.this, TopicActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_TOPIC_URL, TOPIC_URL
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
        switch (interactionType) {
            case BookmarksFragment.INTERACTION_CLICK_BOARD_BOOKMARK:
                Intent intent = new Intent(BookmarksActivity.this, BoardActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_BOARD_URL, BOARD_URL
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
