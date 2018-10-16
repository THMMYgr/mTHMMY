package gr.thmmy.mthmmy.activities.bookmarks;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.model.Bookmark;

/**
 * A {@link Fragment} subclass.
 * Use the {@link TopicBookmarksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicBookmarksFragment extends Fragment {
    protected static final String ARG_SECTION_NUMBER = "SECTION_NUMBER";
    protected static final String ARG_TOPIC_BOOKMARKS = "TOPIC_BOOKMARKS";

    public static final String INTERACTION_CLICK_TOPIC_BOOKMARK = "CLICK_TOPIC_BOOKMARK";
    public static final String INTERACTION_TOGGLE_TOPIC_NOTIFICATION = "TOGGLE_TOPIC_NOTIFICATION";
    public static final String INTERACTION_REMOVE_TOPIC_BOOKMARK = "REMOVE_TOPIC_BOOKMARK";

    ArrayList<Bookmark> topicBookmarks = null;

    private static Drawable notificationsEnabledButtonImage;
    private static Drawable notificationsDisabledButtonImage;

    // Required empty public constructor
    public TopicBookmarksFragment() {
    }

    /**
     * Use ONLY this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Forum.
     */
    public static TopicBookmarksFragment newInstance(int sectionNumber, String topicBookmarks) {
        TopicBookmarksFragment fragment = new TopicBookmarksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_TOPIC_BOOKMARKS, topicBookmarks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String bundledTopicBookmarks = getArguments().getString(ARG_TOPIC_BOOKMARKS);
            if (bundledTopicBookmarks != null) {
                topicBookmarks = Bookmark.arrayFromString(bundledTopicBookmarks);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notificationsEnabledButtonImage = getResources().getDrawable(R.drawable.ic_notification_on, null);
        else
            notificationsEnabledButtonImage = VectorDrawableCompat.create(getResources(), R.drawable.ic_notification_on, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notificationsDisabledButtonImage = getResources().getDrawable(R.drawable.ic_notification_off, null);
        else
            notificationsDisabledButtonImage = VectorDrawableCompat.create(getResources(), R.drawable.ic_notification_off, null);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater layoutInflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflates the layout for this fragment
        final View rootView = layoutInflater.inflate(R.layout.fragment_bookmarks, container, false);
        //bookmarks_topic_container
        final LinearLayout bookmarksLinearView = rootView.findViewById(R.id.bookmarks_container);

        if(this.topicBookmarks != null && !this.topicBookmarks.isEmpty()) {
            for (final Bookmark bookmarkedTopic : topicBookmarks) {
                if (bookmarkedTopic != null && bookmarkedTopic.getTitle() != null) {
                    final LinearLayout row = (LinearLayout) layoutInflater.inflate(
                            R.layout.fragment_bookmarks_row, bookmarksLinearView, false);
                    row.setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarkActivity) {
                            ((BookmarkActivity) activity).onTopicInteractionListener(INTERACTION_CLICK_TOPIC_BOOKMARK, bookmarkedTopic);
                        }
                    });
                    ((TextView) row.findViewById(R.id.bookmark_title)).setText(bookmarkedTopic.getTitle());

                    final ImageButton notificationsEnabledButton = row.findViewById(R.id.toggle_notification);
                    if (!bookmarkedTopic.isNotificationsEnabled()) {
                        notificationsEnabledButton.setImageDrawable(notificationsDisabledButtonImage);
                    }

                    notificationsEnabledButton.setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarkActivity) {
                            if (((BookmarkActivity) activity).onTopicInteractionListener(INTERACTION_TOGGLE_TOPIC_NOTIFICATION, bookmarkedTopic)) {
                                notificationsEnabledButton.setImageDrawable(notificationsEnabledButtonImage);
                            } else {
                                notificationsEnabledButton.setImageDrawable(notificationsDisabledButtonImage);
                            }
                        }
                    });
                    (row.findViewById(R.id.remove_bookmark)).setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarkActivity) {
                            ((BookmarkActivity) activity).onTopicInteractionListener(INTERACTION_REMOVE_TOPIC_BOOKMARK, bookmarkedTopic);
                            topicBookmarks.remove(bookmarkedTopic);
                        }
                        row.setVisibility(View.GONE);

                        if (topicBookmarks.isEmpty()){
                            bookmarksLinearView.addView(bookmarksListEmptyMessage());
                        }
                    });
                    bookmarksLinearView.addView(row);
                }
            }
        } else {
            bookmarksLinearView.addView(bookmarksListEmptyMessage());
        }


        return rootView;
    }

    private TextView bookmarksListEmptyMessage() {
        TextView emptyBookmarksCategory = new TextView(this.getContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 12, 0, 0);
        emptyBookmarksCategory.setLayoutParams(params);
        emptyBookmarksCategory.setText(getString(R.string.empty_topic_bookmarks));
        emptyBookmarksCategory.setTypeface(emptyBookmarksCategory.getTypeface(), Typeface.BOLD);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            emptyBookmarksCategory.setTextColor(this.getContext().getColor(R.color.primary_text));
        } else {
            //noinspection deprecation
            emptyBookmarksCategory.setTextColor(this.getContext().getResources().getColor(R.color.primary_text));
        }
        emptyBookmarksCategory.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        return emptyBookmarksCategory;
    }
}
