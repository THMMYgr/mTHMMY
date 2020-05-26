package gr.thmmy.mthmmy.activities.main.unread;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.TopicSummary;
import gr.thmmy.mthmmy.utils.RelativeTimeTextView;
import timber.log.Timber;

class UnreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<TopicSummary> unreadList;
    private final UnreadFragment.UnreadFragmentInteractionListener mListener;
    private final MarkReadInteractionListener markReadListener;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_NADA = 1;
    private final int VIEW_TYPE_MARK_READ = 2;

    UnreadAdapter(@NonNull List<TopicSummary> topicSummaryList,
                  BaseFragment.FragmentInteractionListener listener,
                  MarkReadInteractionListener markReadInteractionListener) {
        this.unreadList = topicSummaryList;
        mListener = (UnreadFragment.UnreadFragmentInteractionListener) listener;
        markReadListener = markReadInteractionListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (unreadList.get(position).getLastPostDateTime() == null) return VIEW_TYPE_MARK_READ;
        return unreadList.get(position).getTopicUrl() == null ? VIEW_TYPE_NADA : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_unread_row, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_NADA) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_unread_empty_row, parent, false);
            return new EmptyViewHolder(view);
        } else if (viewType == VIEW_TYPE_MARK_READ) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_unread_mark_read_row, parent, false);
            return new MarkReadViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        TopicSummary topicSummary = unreadList.get(holder.getAdapterPosition());
        if (holder instanceof UnreadAdapter.EmptyViewHolder) {
            final UnreadAdapter.EmptyViewHolder emptyViewHolder = (UnreadAdapter.EmptyViewHolder) holder;
            emptyViewHolder.text.setText(topicSummary.getLastPostDateTime());
        } else if (holder instanceof UnreadAdapter.ViewHolder) {
            final UnreadAdapter.ViewHolder viewHolder = (UnreadAdapter.ViewHolder) holder;

            viewHolder.mTitleView.setText(topicSummary.getSubject());
            if(BaseApplication.getInstance().isDisplayRelativeTimeEnabled()){
                String timestamp = topicSummary.getLastPostTimestamp();
                try{
                    viewHolder.mDateTimeView.setReferenceTime(Long.valueOf(timestamp));
                }
                catch(NumberFormatException e){
                    Timber.e(e, "Invalid number format: %s", timestamp);
                    viewHolder.mDateTimeView.setText(topicSummary.getLastPostSimplifiedDateTime());
                }
            }
            else
                viewHolder.mDateTimeView.setText(topicSummary.getLastPostSimplifiedDateTime());

            viewHolder.mUserView.setText(topicSummary.getLastUser());
            viewHolder.topic = topicSummary;

            viewHolder.mView.setOnClickListener(v -> {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onUnreadFragmentInteraction(viewHolder.topic);  //?
                }
            });
        } else if (holder instanceof UnreadAdapter.MarkReadViewHolder) {
            final UnreadAdapter.MarkReadViewHolder markReadViewHolder = (UnreadAdapter.MarkReadViewHolder) holder;
            markReadViewHolder.text.setText(unreadList.get(holder.getAdapterPosition()).getSubject());
            markReadViewHolder.topic = unreadList.get(holder.getAdapterPosition());

            markReadViewHolder.mView.setOnClickListener(v -> {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    markReadListener.onMarkReadInteraction(unreadList.get(holder.getAdapterPosition()).getTopicUrl());
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return unreadList.size();
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitleView;
        final TextView mUserView;
        final RelativeTimeTextView mDateTimeView;
        public TopicSummary topic;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.title);
            mUserView = view.findViewById(R.id.lastUser);
            mDateTimeView = view.findViewById(R.id.dateTime);
        }
    }

    private static class EmptyViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        EmptyViewHolder(View view) {
            super(view);
            text = view.findViewById(R.id.text);
        }
    }

    private static class MarkReadViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView text;
        public TopicSummary topic;

        MarkReadViewHolder(View view) {
            super(view);
            mView = view;
            text = view.findViewById(R.id.mark_read);
        }
    }

    interface MarkReadInteractionListener {
        void onMarkReadInteraction(String markReadLinkUrl);
    }
}
