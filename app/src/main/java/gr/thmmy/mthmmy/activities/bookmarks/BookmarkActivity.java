package gr.thmmy.mthmmy.activities.bookmarks;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.widget.Toast;

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

//TODO proper handling with adapter etc.
//TODO after clicking bookmark and then back button should return to this activity
public class BookmarkActivity extends BaseActivity {
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

        //Creates the adapter that will return a fragment for each section of the activity
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        sectionsPagerAdapter.addFragment(TopicBookmarksFragment.newInstance(1, Bookmark.arrayToString(getTopicsBookmarked())), "Topics");
        sectionsPagerAdapter.addFragment(BoardBookmarksFragment.newInstance(2, Bookmark.arrayToString(getBoardsBookmarked())), "Boards");

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

    public boolean onTopicInteractionListener(String interactionType, Bookmark bookmarkedTopic){
        if (interactionType.equals(TopicBookmarksFragment.INTERACTION_CLICK_TOPIC_BOOKMARK)){
            Intent intent = new Intent(BookmarkActivity.this, TopicActivity.class);
            Bundle extras = new Bundle();
            extras.putString(BUNDLE_TOPIC_URL, "https://www.thmmy.gr/smf/index.php?topic="
                    + bookmarkedTopic.getId() + "." + 2147483647);
            extras.putString(BUNDLE_TOPIC_TITLE, bookmarkedTopic.getTitle());
            intent.putExtras(extras);
            startActivity(intent);
            finish();
        } else if (interactionType.equals(TopicBookmarksFragment.INTERACTION_TOGGLE_TOPIC_NOTIFICATION)) {
            return toggleNotification(bookmarkedTopic);
        } else if (interactionType.equals(TopicBookmarksFragment.INTERACTION_REMOVE_TOPIC_BOOKMARK)){
            removeBookmark(bookmarkedTopic);
            Toast.makeText(BookmarkActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
        }
        return true;
    }

    public void onBoardInteractionListener(String interactionType, Bookmark bookmarkedBoard){
        if (interactionType.equals(BoardBookmarksFragment.INTERACTION_CLICK_BOARD_BOOKMARK)){
            Intent intent = new Intent(BookmarkActivity.this, BoardActivity.class);
            Bundle extras = new Bundle();
            extras.putString(BUNDLE_BOARD_URL, "https://www.thmmy.gr/smf/index.php?board="
                    + bookmarkedBoard.getId() + ".0");
            extras.putString(BUNDLE_BOARD_TITLE, bookmarkedBoard.getTitle());
            intent.putExtras(extras);
            startActivity(intent);
            finish();
        } else if (interactionType.equals(BoardBookmarksFragment.INTERACTION_REMOVE_BOARD_BOOKMARK)){
            removeBookmark(bookmarkedBoard);
            Toast.makeText(BookmarkActivity.this, "Bookmark removed", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages. If it becomes too memory intensive,
     * it may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
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
