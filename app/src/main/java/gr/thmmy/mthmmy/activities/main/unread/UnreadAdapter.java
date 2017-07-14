package gr.thmmy.mthmmy.activities.main.unread;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.TopicSummary;

class UnreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final Context context;
    private final List<TopicSummary> unreadList;
    private final UnreadFragment.UnreadFragmentInteractionListener mListener;

    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_NADA = 1;

    UnreadAdapter(Context context, @NonNull List<TopicSummary> topicSummaryList, BaseFragment.FragmentInteractionListener listener) {
        this.context = context;
        this.unreadList = topicSummaryList;
        mListener = (UnreadFragment.UnreadFragmentInteractionListener) listener;
    }

    @Override
    public int getItemViewType(int position) {
        return unreadList.get(position).getTopicUrl() == null ? VIEW_TYPE_NADA : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_unread_row, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_NADA) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_unread_empty_row, parent, false);
            return new EmptyViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UnreadAdapter.EmptyViewHolder) {
            final UnreadAdapter.EmptyViewHolder emptyViewHolder = (UnreadAdapter.EmptyViewHolder) holder;
            emptyViewHolder.text.setText(unreadList.get(position).getDateTimeModified());
        } else if (holder instanceof UnreadAdapter.ViewHolder) {
            final UnreadAdapter.ViewHolder viewHolder = (UnreadAdapter.ViewHolder) holder;

            viewHolder.mTitleView.setText(unreadList.get(position).getSubject());
            viewHolder.mDateTimeView.setText(unreadList.get(position).getDateTimeModified());
            viewHolder.mUserView.setText(context.getString(R.string.byUser, unreadList.get(position).getLastUser()));

            viewHolder.topic = unreadList.get(position);

            viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (null != mListener) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that an item has been selected.
                        mListener.onUnreadFragmentInteraction(viewHolder.topic);  //?

                    }

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
        final TextView mDateTimeView;
        public TopicSummary topic;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = (TextView) view.findViewById(R.id.title);
            mUserView = (TextView) view.findViewById(R.id.lastUser);
            mDateTimeView = (TextView) view.findViewById(R.id.dateTime);
        }
    }

    private static class EmptyViewHolder extends RecyclerView.ViewHolder {
        final TextView text;

        EmptyViewHolder(View view) {
            super(view);
            text = (TextView) view.findViewById(R.id.text);
        }
    }
}