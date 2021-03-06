package gr.thmmy.mthmmy.activities.bookmarks;


import android.app.Activity;
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
    static final String INTERACTION_REMOVE_BOARD_BOOKMARK = "REMOVE_BOARD_BOOKMARK";

    private TextView nothingBookmarkedTextView;

    public ArrayList<Bookmark> bookmarks = null;
    public Type type;
    public String interactionClick, interactionToggle, interactionRemove;

    private Drawable notificationsEnabledButtonImage;
    private Drawable notificationsDisabledButtonImage;

    public BookmarksFragment() {/* Required empty public constructor */}

    private BookmarksFragment(Type type) {
        this.type = type;
        if (type == Type.TOPIC) {
            this.interactionClick = INTERACTION_CLICK_TOPIC_BOOKMARK;
            this.interactionToggle = INTERACTION_TOGGLE_TOPIC_NOTIFICATION;
            this.interactionRemove = INTERACTION_REMOVE_TOPIC_BOOKMARK;
        }
        else if (type == Type.BOARD) {
            this.interactionClick = INTERACTION_CLICK_BOARD_BOOKMARK;
            this.interactionToggle = INTERACTION_TOGGLE_BOARD_NOTIFICATION;
            this.interactionRemove = INTERACTION_REMOVE_BOARD_BOOKMARK;
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


        //Create the adapter.
        BookmarksAdapter adapter = new BookmarksAdapter(this, notificationsEnabledButtonImage, notificationsDisabledButtonImage);

        //Get the drag list view.
        DragListView mDragListView = (DragListView) rootView.findViewById(R.id.fragment_bookmarks_dragList);


        //Set the Drag List Listener.
        mDragListView.setDragListListener(new DragListView.DragListListener()
        {
            @Override
            public void onItemDragStarted(int position)
            {
                //Create a new array of bookmarks.
                ArrayList<Bookmark> new_bookmarks = new ArrayList<Bookmark>();

                //For each bookmark in the current bookmarks array.
                for (int i = 0; i < bookmarks.size(); i++)
                {
                    //Create an indicator bookmark.
                    Bookmark indicator = new Bookmark("Drop Here", "-1", true);

                    //Add the indicator followed by the current actual bookmark.
                    if (position != i-1 && position != i)
                        new_bookmarks.add(indicator);

                    new_bookmarks.add(bookmarks.get(i));
                }

                //Add one last indicator.
                if (position != bookmarks.size() - 1)
                    new_bookmarks.add(new Bookmark("Drop Here", "-1", true));

                //Replace the bookmarks with the new bookmarks that contains the indicators.
                bookmarks = new_bookmarks;

                //Notify the adapter that the bookmarks array has changed!
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onItemDragging(int itemPosition, float x, float y)
            {

            }

            @Override
            public void onItemDragEnded(int fromPosition, int toPosition)
            {
                //It's hard to explain what this does.
                int actualPos = fromPosition;

                //It's hard to explain what this does.
                if (fromPosition != 0)
                    actualPos = 2 * fromPosition;

                //If the drag and drop is not the same item.
                if (actualPos != toPosition)
                {

                    //Get the from bookmark.
                    Bookmark from = bookmarks.get(actualPos);
                    Bookmark to   = bookmarks.get(toPosition);

                    //You can only drop items in the indicator boxes!!!
                    //Indicator boxes are Bookmark objects with id "-1".
                    if (to.getId().equals("-1"))
                    {
                        //Swap the indicator with the actual.
                        bookmarks.set(actualPos, to);
                        bookmarks.set(toPosition, from);

                        //Get the fragments activity.
                        Activity unknownActivity = getActivity();

                        //Update the order of the bookmarks in the preferences.
                        if (unknownActivity instanceof BookmarksActivity)
                        {
                            //Cast to BookmarksActivity.
                            BookmarksActivity activity = (BookmarksActivity)unknownActivity;

                            //Update the preferences.
                            activity.updateBookmarks(bookmarks);
                        }
                    }

                    //------------------------Clean up the indicator boxes------------------------//
                }

                //Find all the indicator boxes in the bookmarks array.
                ArrayList<Bookmark> books_to_delete = new ArrayList<Bookmark>();
                for (int i = 0; i < bookmarks.size(); i++)
                {
                    Bookmark book = bookmarks.get(i);

                    if (book.getId().equals("-1"))
                        books_to_delete.add(book);
                }


                //Remove all the indicators.
                for (int i = 0; i < books_to_delete.size(); i++)
                {
                    bookmarks.remove(books_to_delete.get(i));
                }

                //------------------------Clean up the indicator boxes------------------------//

                //Notify the adapter, because I made changes to the bookmarks array.
                adapter.notifyDataSetChanged();
            }

        });

        //====================================This is the code for the Drag and Drop Functionality====================================//
        mDragListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mDragListView.setAdapter(adapter, false);
        mDragListView.setCanDragHorizontally(false);
        //====================================This is the code for the Drag and Drop Functionality====================================//

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
