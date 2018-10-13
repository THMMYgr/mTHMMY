package gr.thmmy.mthmmy.activities.bookmarks;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.app.Fragment;
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
 * Use the {@link BoardBookmarksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BoardBookmarksFragment extends Fragment {
    protected static final String ARG_SECTION_NUMBER = "SECTION_NUMBER";
    protected static final String ARG_BOARD_BOOKMARKS = "BOARD_BOOKMARKS";

    public static final String INTERACTION_CLICK_BOARD_BOOKMARK = "CLICK_BOARD_BOOKMARK";
    public static final String INTERACTION_TOGGLE_BOARD_NOTIFICATION = "TOGGLE_BOARD_NOTIFICATION";
    public static final String INTERACTION_REMOVE_BOARD_BOOKMARK= "REMOVE_BOARD_BOOKMARK";

    ArrayList<Bookmark> boardBookmarks = null;

    private static Drawable notificationsEnabledButtonImage;
    private static Drawable notificationsDisabledButtonImage;

    // Required empty public constructor
    public BoardBookmarksFragment() {
    }

    /**
     * Use ONLY this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Forum.
     */
    public static BoardBookmarksFragment newInstance(int sectionNumber, String boardBookmarks) {
        BoardBookmarksFragment fragment = new BoardBookmarksFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_BOARD_BOOKMARKS, boardBookmarks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String bundledBoardBookmarks = getArguments().getString(ARG_BOARD_BOOKMARKS);
            if (bundledBoardBookmarks != null) {
                boardBookmarks = Bookmark.arrayFromString(bundledBoardBookmarks);
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
        //bookmarks_board_container
        final LinearLayout bookmarksLinearView = rootView.findViewById(R.id.bookmarks_container);

        if(this.boardBookmarks != null && !this.boardBookmarks.isEmpty()) {
            for (final Bookmark bookmarkedBoard : boardBookmarks) {
                if (bookmarkedBoard != null && bookmarkedBoard.getTitle() != null) {
                    final LinearLayout row = (LinearLayout) layoutInflater.inflate(
                            R.layout.fragment_bookmarks_board_row, bookmarksLinearView, false);
                    row.setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarkActivity){
                            ((BookmarkActivity) activity).onBoardInteractionListener(INTERACTION_CLICK_BOARD_BOOKMARK, bookmarkedBoard);
                        }
                    });
                    ((TextView) row.findViewById(R.id.bookmark_title)).setText(bookmarkedBoard.getTitle());

                    final ImageButton notificationsEnabledButton = row.findViewById(R.id.toggle_notification);
                    if (!bookmarkedBoard.isNotificationsEnabled()) {
                        notificationsEnabledButton.setImageDrawable(notificationsDisabledButtonImage);
                    }

                    notificationsEnabledButton.setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarkActivity) {
                            if (((BookmarkActivity) activity).onBoardInteractionListener(INTERACTION_TOGGLE_BOARD_NOTIFICATION, bookmarkedBoard)) {
                                notificationsEnabledButton.setImageDrawable(notificationsEnabledButtonImage);
                            } else {
                                notificationsEnabledButton.setImageDrawable(notificationsDisabledButtonImage);
                            }
                        }
                    });

                    (row.findViewById(R.id.remove_bookmark)).setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarkActivity){
                            ((BookmarkActivity) activity).onBoardInteractionListener(INTERACTION_REMOVE_BOARD_BOOKMARK, bookmarkedBoard);
                            boardBookmarks.remove(bookmarkedBoard);
                        }
                        row.setVisibility(View.GONE);

                        if (boardBookmarks.isEmpty()){
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
        emptyBookmarksCategory.setText(getString(R.string.empty_board_bookmarks));
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
