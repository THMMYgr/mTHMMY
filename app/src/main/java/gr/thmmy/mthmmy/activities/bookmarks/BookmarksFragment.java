package gr.thmmy.mthmmy.activities.bookmarks;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.model.Bookmark;

//TODO refactor using RecyclerView
public class BookmarksFragment extends Fragment {
    enum Type {TOPIC, BOARD}
    private static final String ARG_SECTION_NUMBER = "SECTION_NUMBER";
    private static final String ARG_BOOKMARKS = "BOOKMARKS";

    static final String INTERACTION_CLICK_TOPIC_BOOKMARK = "CLICK_TOPIC_BOOKMARK";
    static final String INTERACTION_TOGGLE_TOPIC_NOTIFICATION = "TOGGLE_TOPIC_NOTIFICATION";
    static final String INTERACTION_REMOVE_TOPIC_BOOKMARK = "REMOVE_TOPIC_BOOKMARK";

    static final String INTERACTION_CLICK_BOARD_BOOKMARK = "CLICK_BOARD_BOOKMARK";
    static final String INTERACTION_TOGGLE_BOARD_NOTIFICATION = "TOGGLE_BOARD_NOTIFICATION";
    static final String INTERACTION_REMOVE_BOARD_BOOKMARK= "REMOVE_BOARD_BOOKMARK";

    private TextView nothingBookmarkedTextView;

    private ArrayList<Bookmark> bookmarks = null;
    private Type type;
    private String interactionClick, interactionToggle, interactionRemove;

    private Drawable notificationsEnabledButtonImage;
    private Drawable notificationsDisabledButtonImage;

    public BookmarksFragment() {/* Required empty public constructor */}

    private BookmarksFragment(Type type) {
        this.type=type;
        if(type==Type.TOPIC){
            this.interactionClick=INTERACTION_CLICK_TOPIC_BOOKMARK;
            this.interactionToggle=INTERACTION_TOGGLE_TOPIC_NOTIFICATION;
            this.interactionRemove=INTERACTION_REMOVE_TOPIC_BOOKMARK;
        }
        else if (type==Type.BOARD){
            this.interactionClick=INTERACTION_CLICK_BOARD_BOOKMARK;
            this.interactionToggle=INTERACTION_TOGGLE_BOARD_NOTIFICATION;
            this.interactionRemove=INTERACTION_REMOVE_BOARD_BOOKMARK;
        }
    }

    /**
     * Use ONLY this factory method to create a new instance of
     * the desired fragment using the provided parameters.
     *
     * @return A new instance of fragment Forum.
     */
    protected static BookmarksFragment newInstance(int sectionNumber, String bookmarks, Type type) {
        BookmarksFragment fragment = new BookmarksFragment(type);
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(ARG_BOOKMARKS, bookmarks);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            String bundledBookmarks = getArguments().getString(ARG_BOOKMARKS);
            if (bundledBookmarks != null) {
                bookmarks = Bookmark.stringToArrayList(bundledBookmarks);
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
        //bookmarks container
        final LinearLayout bookmarksLinearView = rootView.findViewById(R.id.bookmarks_container);
        nothingBookmarkedTextView = rootView.findViewById(R.id.nothing_bookmarked);

        if(this.bookmarks != null && !this.bookmarks.isEmpty()) {
            hideNothingBookmarked();
            for (final Bookmark bookmark : bookmarks) {
                if (bookmark != null && bookmark.getTitle() != null) {
                    final LinearLayout row = (LinearLayout) layoutInflater.inflate(
                            R.layout.fragment_bookmarks_row, bookmarksLinearView, false);
                    row.setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarksActivity)
                            ((BookmarksActivity) activity).onFragmentRowInteractionListener(type, interactionClick, bookmark);
                    });
                    ((TextView) row.findViewById(R.id.bookmark_title)).setText(bookmark.getTitle());

                    final ImageButton notificationsEnabledButton = row.findViewById(R.id.toggle_notification);
                    if (!bookmark.isNotificationsEnabled()) {
                        notificationsEnabledButton.setImageDrawable(notificationsDisabledButtonImage);
                    }

                    notificationsEnabledButton.setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarksActivity) {
                            if (((BookmarksActivity) activity).onFragmentRowInteractionListener(type, interactionToggle, bookmark))
                                notificationsEnabledButton.setImageDrawable(notificationsEnabledButtonImage);
                            else
                                notificationsEnabledButton.setImageDrawable(notificationsDisabledButtonImage);
                        }
                    });

                    (row.findViewById(R.id.remove_bookmark)).setOnClickListener(view -> {
                        Activity activity = getActivity();
                        if (activity instanceof BookmarksActivity){
                            ((BookmarksActivity) activity).onFragmentRowInteractionListener(type, interactionRemove, bookmark);
                            bookmarks.remove(bookmark);
                        }
                        row.setVisibility(View.GONE);

                        if (bookmarks.isEmpty()){
                            showNothingBookmarked();
                        }
                    });
                    bookmarksLinearView.addView(row);
                }
            }
        } else
            showNothingBookmarked();

        return rootView;
    }


    private void showNothingBookmarked() {
        if(nothingBookmarkedTextView!=null)
            nothingBookmarkedTextView.setVisibility(View.VISIBLE);
    }

    private void hideNothingBookmarked(){
        if(nothingBookmarkedTextView!=null)
            nothingBookmarkedTextView.setVisibility(View.INVISIBLE);
    }

}
