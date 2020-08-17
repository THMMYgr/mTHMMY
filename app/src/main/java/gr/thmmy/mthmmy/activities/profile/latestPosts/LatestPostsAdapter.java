package gr.thmmy.mthmmy.activities.profile.latestPosts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.PostSummary;
import gr.thmmy.mthmmy.model.TopicSummary;
import gr.thmmy.mthmmy.views.ReactiveWebView;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TopicSummary} and makes a call to the
 * specified {@link LatestPostsFragment.LatestPostsFragmentInteractionListener}.
 */
class LatestPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_EMPTY = -1;
    private static final int VIEW_TYPE_ITEM = 0;
    private final Context context;
    private final LatestPostsFragment.LatestPostsFragmentInteractionListener interactionListener;
    private final ArrayList<PostSummary> parsedTopicSummaries;

    LatestPostsAdapter(Context context, BaseFragment.FragmentInteractionListener interactionListener,
                       ArrayList<PostSummary> parsedTopicSummaries) {
        this.context = context;
        this.interactionListener = (LatestPostsFragment.LatestPostsFragmentInteractionListener) interactionListener;
        this.parsedTopicSummaries = parsedTopicSummaries;
    }

    @Override
    public int getItemViewType(int position) {
        if (parsedTopicSummaries.get(position) == null && position == 0) return VIEW_TYPE_EMPTY;
        return VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.fragment_profile_latest_posts_row, parent, false);
            return new LatestPostViewHolder(view);
        } else {    // viewType == VIEW_TYPE_EMPTY
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.fragment_profile_latest_posts_empty_message, parent, false);
            return new RecyclerView.ViewHolder(view){};
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        PostSummary topic = parsedTopicSummaries.get(position);
        final LatestPostViewHolder latestPostViewHolder = (LatestPostViewHolder) holder;
        latestPostViewHolder.postTitle.setText(topic.getSubject());
        latestPostViewHolder.postDate.setText(topic.getDateTime());
        latestPostViewHolder.post.setBackgroundColor(Color.argb(1, 255, 255, 255));
        latestPostViewHolder.post.loadDataWithBaseURL("file:///android_asset/"
                , topic.getPost(), "text/html", "UTF-8", null);

        latestPostViewHolder.latestPostsRow.setOnClickListener(v -> {
            if (interactionListener != null) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that a post has been selected.
                interactionListener.onLatestPostsFragmentInteraction(
                        parsedTopicSummaries.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return parsedTopicSummaries == null ? 0 : parsedTopicSummaries.size();
    }

    private static class LatestPostViewHolder extends RecyclerView.ViewHolder {
        final RelativeLayout latestPostsRow;
        final TextView postTitle;
        final TextView postDate;
        final ReactiveWebView post;

        LatestPostViewHolder(View itemView) {
            super(itemView);
            latestPostsRow = itemView.findViewById(R.id.latest_posts_row);
            postTitle = itemView.findViewById(R.id.title);
            postDate = itemView.findViewById(R.id.date);
            post = itemView.findViewById(R.id.post);
        }
    }
}