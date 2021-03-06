package gr.thmmy.mthmmy.activities.bookmarks;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.model.Bookmark;




public class BookmarksAdapter extends DragItemAdapter<ArrayList<Bookmark>, BookmarksAdapter.BookmarksViewHolder>
{
    private final BookmarksFragment m_fragment;
    private final Drawable m_notificationsEnabled;
    private final Drawable m_notificationsDisabled;

    public BookmarksAdapter(BookmarksFragment fragment, Drawable noteEnabled, Drawable noteDisabled)
    {
        this.m_fragment = fragment;
        this.m_notificationsEnabled = noteEnabled;
        this.m_notificationsDisabled = noteDisabled;
    }

    @Override
    public long getUniqueItemId(int position)
    {
        return m_fragment.bookmarks.get(position).getId().hashCode();
    }

    @NonNull
    @Override
    public BookmarksAdapter.BookmarksViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_bookmarks_row, parent, false);
        return new BookmarksViewHolder(view, view.findViewById(R.id.bookmark_dragable).getId(), true);
    }

    @Override
    public void onBindViewHolder(@NonNull BookmarksViewHolder holder, int position)
    {
        super.onBindViewHolder(holder, position);

        //If this is a drop indicator, use the dashed corner background.
        if (m_fragment.bookmarks.get(position).getId().equals("-1"))
        {
            holder.itemView.findViewById(R.id.bookmark_dragable).setBackgroundResource(R.drawable.bookmark_row_dashed_bg);
            holder.itemView.findViewById(R.id.bookmark_drag_icon).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.toggle_notification).setVisibility(View.GONE);
            holder.itemView.findViewById(R.id.remove_bookmark).setVisibility(View.GONE);
        }

        //Check if bookMarks ArrayList Exists and is not empty.
        if(m_fragment.bookmarks != null && !m_fragment.bookmarks.isEmpty())
        {
            //Check if the current bookmark exists and has a title.
            if (m_fragment.bookmarks.get(position) != null && m_fragment.bookmarks.get(position).getTitle() != null)
            {

                //Set the title.
                holder.m_textView.setText(m_fragment.bookmarks.get(position).getTitle());

                //Set Notifications Enabled Image Indicator.
                if (m_fragment.bookmarks.get(position).isNotificationsEnabled())
                    holder.m_noteView.setImageDrawable(m_notificationsEnabled);

                //Set Notifications Disabled Image Indicator.
                else
                    holder.m_noteView.setImageDrawable(m_notificationsDisabled);


                //On Bookmark Click.
                holder.mGrabView.setOnClickListener(v -> {

                    //Get the activity.
                    Activity activity = m_fragment.getActivity();

                    //Go to the bookmarked activity.
                    if (activity instanceof BookmarksActivity)
                        ((BookmarksActivity) activity).onFragmentRowInteractionListener(
                                m_fragment.type,
                                m_fragment.interactionClick,
                                m_fragment.bookmarks.get(position));
                });


                //On Notifications Toggle.
                holder.m_noteView.setOnClickListener(v -> {

                    //Toggle the current local instance.
                    m_fragment.bookmarks.get(position).toggleNotificationsEnabled();

                    //Get the fragment activity.
                    Activity activity = m_fragment.getActivity();

                    //Check if it is indeed the fragment activity.
                    if (activity instanceof BookmarksActivity)
                    {

                        //Trigger the toggle functionality and set the Enabled notifications image.
                        if (((BookmarksActivity) activity).onFragmentRowInteractionListener(
                                m_fragment.type,
                                m_fragment.interactionToggle,
                                m_fragment.bookmarks.get(position)))
                        {
                            holder.m_noteView.setImageDrawable(m_notificationsEnabled);
                        }

                        //Trigger returned false, so set the notifications disabled image.
                        else
                            holder.m_noteView.setImageDrawable(m_notificationsDisabled);
                    }
                });


                //Remove Item.
                //TODO: AFTER DELETION, UPDATE THE ORDER IN THE PREFERENCES OF ALL
                // ALL THE BOOKMARKS UNDER THIS ONE THAT HAS BEEN DELETED!
                holder.m_removeView.setOnClickListener(v -> {

                    //Get fragment's activity.
                    Activity activity = m_fragment.getActivity();

                    if (activity instanceof BookmarksActivity)
                    {

                        //Trigger the bookmark remove functionality.
                        ((BookmarksActivity) activity).onFragmentRowInteractionListener(
                                m_fragment.type,
                                m_fragment.interactionRemove,
                                m_fragment.bookmarks.get(position));
                        {
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, m_fragment.bookmarks.size());
                            m_fragment.bookmarks.remove(m_fragment.bookmarks.get(position));
                        }
                    }

                    //If the bookmarks are empty then show nothing marked.
                    if (m_fragment.bookmarks.isEmpty())
                    {
                        m_fragment.showNothingBookmarked();
                    }

                });
            }
        }
    }

    @Override
    public int getItemCount()
    {
        if (m_fragment.bookmarks != null)
         return m_fragment.bookmarks.size();

        return 0;
    }

    //View Holder.
    static class BookmarksViewHolder extends DragItemAdapter.ViewHolder
    {

        public final TextView m_textView;
        public final ImageView m_noteView;
        public final ImageView m_removeView;
        public final View m_thisView;

        public BookmarksViewHolder(View itemView, int handleResId, boolean dragOnLongPress)
        {
            super(itemView, handleResId, dragOnLongPress);
            this.m_textView   = itemView.findViewById(R.id.bookmark_title);
            this.m_noteView   = itemView.findViewById(R.id.toggle_notification);
            this.m_removeView = itemView.findViewById(R.id.remove_bookmark);
            this.m_thisView   = itemView;
        }
    }
}
