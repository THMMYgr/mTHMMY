package gr.thmmy.mthmmy.activities.main.recent;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.collection.ArraySortedMap;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.TopicSummary;


/**
 * {@link RecyclerView.Adapter} that can display a {@link TopicSummary} and makes a call to the
 * specified {@link RecentFragment.RecentFragmentInteractionListener}.
 */
class RecentAdapter extends RecyclerView.Adapter<RecentAdapter.ViewHolder> {
    private final Context context;
    private final List<DocumentReference> postSummaries;
    private final RecentFragment.RecentFragmentInteractionListener mListener;

    RecentAdapter(Context context, @NonNull List<DocumentReference> postSummaries, BaseFragment.FragmentInteractionListener listener) {
        this.context = context;
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();
        for (DocumentReference documentReference : postSummaries) {
            Task<DocumentSnapshot> documentSnapshotTask = documentReference.get();
            tasks.add(documentSnapshotTask);
        }
        Tasks.whenAllSuccess(tasks).addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {
                ArrayList<Map> posts = new ArrayList<>();
                for (Object object : objects) {
                    posts.add((Map) object);
                }
            }
        })
        this.postSummaries = postSummaries;
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
        ArraySortedMap map = (ArraySortedMap) postSummaries.get("posts");
        ArraySortedMap post = (ArraySortedMap) map.get(position);
        holder.mTitleView.setText(post.get("topicTitle").toString());
        holder.mDateTimeView.setText(post.get("timestamp").toString());
        holder.mUserView.setText(post.get("poster").toString());

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
            mTitleView = view.findViewById(R.id.title);
            mUserView = view.findViewById(R.id.lastUser);
            mDateTimeView = view.findViewById(R.id.dateTime);
        }
    }
}
