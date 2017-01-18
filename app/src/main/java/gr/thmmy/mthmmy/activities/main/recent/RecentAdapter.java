package gr.thmmy.mthmmy.activities.main.recent;

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


/**
 * {@link RecyclerView.Adapter} that can display a {@link TopicSummary} and makes a call to the
 * specified {@link RecentFragment.RecentFragmentInteractionListener}.
 */
class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
    private final Context context;
    private final List<TopicSummary> recentList;
    private final RecentFragment.RecentFragmentInteractionListener mListener;

    RecentAdapter(Context context, @NonNull List<TopicSummary> topicSummaryList, BaseFragment.FragmentInteractionListener listener) {
        this.context = context;
        this.recentList = topicSummaryList;
        mListener = (RecentFragment.RecentFragmentInteractionListener) listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_recent_row, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.mTitleView.setText(recentList.get(position).getSubject());
        holder.mDateTimeView.setText(recentList.get(position).getDateTimeModified());
        holder.mUserView.setText(context.getString(R.string.byUser, recentList.get(position).getLastUser()));

        holder.topic = recentList.get(position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onRecentFragmentInteraction(holder.topic);  //?

                }

            }
        });
    }

    @Override
    public int getItemCount() {
        return recentList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
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
}
