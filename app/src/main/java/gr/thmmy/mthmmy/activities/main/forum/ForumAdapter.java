package gr.thmmy.mthmmy.activities.main.forum;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.base.BaseFragment;
import gr.thmmy.mthmmy.data.TopicSummary;


/**
 * {@link RecyclerView.Adapter} that can display a {@link TopicSummary} and makes a call to the
 * specified {@link ForumFragment.ForumFragmentInteractionListener}.
 */
class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ViewHolder> {
    private final List<TopicSummary> recentList;
    private final ForumFragment.ForumFragmentInteractionListener mListener;

    ForumAdapter(List<TopicSummary> topicSummaryList, BaseFragment.FragmentInteractionListener listener) {
        this.recentList = topicSummaryList;
        mListener = (ForumFragment.ForumFragmentInteractionListener)listener;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_recent_row, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        holder.mTitleView.setText(recentList.get(position).getTitle());
        holder.mDateTimeView.setText(recentList.get(position).getDateTimeModified());
        holder.mUserView.setText("by " + recentList.get(position).getLastUser());

        holder.topic = recentList.get(position);

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onFragmentInteraction(holder.topic);  //?

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
