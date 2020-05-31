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
import gr.thmmy.mthmmy.views.RelativeTimeTextView;
import timber.log.Timber;

class UnreadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<TopicSummary> unreadList;
    private final UnreadFragment.UnreadFragmentInteractionListener mListener;

    UnreadAdapter(@NonNull List<TopicSummary> topicSummaryList,
                  BaseFragment.FragmentInteractionListener listener) {
        this.unreadList = topicSummaryList;
        mListener = (UnreadFragment.UnreadFragmentInteractionListener) listener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_unread_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        TopicSummary topicSummary = unreadList.get(holder.getAdapterPosition());
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
}
