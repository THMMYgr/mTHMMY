package gr.thmmy.mthmmy.activities.bookmarks;


import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import com.woxthebox.draglistview.DragListView;

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
    public Type type;
    public String interactionClick, interactionToggle, interactionRemove;

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

        //Get the nothing bookmarked text view.
        nothingBookmarkedTextView = rootView.findViewById(R.id.nothing_bookmarked);

        DragListView mDragListView = (DragListView) rootView.findViewById(R.id.fragment_bookmarks_dragList);

        mDragListView.setDragListListener(new DragListView.DragListListener()
        {
            @Override
            public void onItemDragStarted(int position)
            {
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y)
            {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition)
            {

                //TODO: This only works locally. If the user exit the bookmarks
                // or closes the app, the order of the bookmarks will be lost.
                // make sure after the following swapping, to apply the changes
                // in the actual data model of the bookmarks.
                // AFTER SWAPPING UPDATE THE ORDER OF THOSE TWO IN THE PREFERENCES.

                if (fromPosition != toPosition)
                {
                    Bookmark from = bookmarks.get(fromPosition);

                    bookmarks.set(fromPosition, bookmarks.get(toPosition));
                    bookmarks.set(toPosition, from);
                }
            }
        });

        mDragListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        BookmarksAdapter adapter = new BookmarksAdapter(this, bookmarks, notificationsEnabledButtonImage, notificationsDisabledButtonImage);
        mDragListView.setAdapter(adapter, false);
        mDragListView.setCanDragHorizontally(false);

        //Hide Nothing Bookmarked.
        if(this.bookmarks != null && !this.bookmarks.isEmpty())
        {
            hideNothingBookmarked();
        }

        //Show Nothing Bookmarked.
        else {
            showNothingBookmarked();
        }

        return rootView;
    }


    public void showNothingBookmarked() {
        if(nothingBookmarkedTextView!=null)
            nothingBookmarkedTextView.setVisibility(View.VISIBLE);
    }

    public void hideNothingBookmarked(){
        if(nothingBookmarkedTextView!=null)
            nothingBookmarkedTextView.setVisibility(View.INVISIBLE);
    }

}
