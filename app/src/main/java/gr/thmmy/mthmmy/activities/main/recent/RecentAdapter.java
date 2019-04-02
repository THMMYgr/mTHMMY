package gr.thmmy.mthmmy.activities.main.recent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.curioustechizen.ago.RelativeTimeTextView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.RecentItem;
import gr.thmmy.mthmmy.model.TopicSummary;


/**
 * {@link RecyclerView.Adapter} that can display a {@link TopicSummary} and makes a call to the
 * specified {@link RecentFragment.RecentFragmentInteractionListener}.
 */
class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
    private final Context context;
    private final List<RecentItem> recentItems;
    private final RecentFragment.RecentFragmentInteractionListener mListener;

    RecentAdapter(Context context, @NonNull List<RecentItem> recentItems, BaseFragment.FragmentInteractionListener listener) {
        this.context = context;

        this.recentItems = recentItems;
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
        RecentItem recentItem = recentItems.get(position);
        holder.mTitleView.setText(recentItem.getTopicTitle());
        holder.mDateTimeView.setReferenceTime(recentItem.getTimestamp());
        holder.mUserView.setText(recentItem.getPoster());

        holder.mView.setOnClickListener(v -> {

            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onRecentFragmentInteraction(recentItems.get(holder.getAdapterPosition()));  //?

            }

        });
    }

    @Override
    public int getItemCount() {
        return recentItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final View mView;
        final TextView mTitleView;
        final TextView mUserView;
        final RelativeTimeTextView mDateTimeView;

        ViewHolder(View view) {
            super(view);
            mView = view;
            mTitleView = view.findViewById(R.id.title);
            mUserView = view.findViewById(R.id.lastUser);
            mDateTimeView = view.findViewById(R.id.dateTime);
        }
    }
}
