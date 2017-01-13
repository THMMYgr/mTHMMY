package gr.thmmy.mthmmy.activities.profile.latestPosts;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.base.BaseFragment;
import gr.thmmy.mthmmy.data.PostSummary;
import gr.thmmy.mthmmy.data.TopicSummary;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * {@link RecyclerView.Adapter} that can display a {@link TopicSummary} and makes a call to the
 * specified {@link LatestPostsFragment.LatestPostsFragmentInteractionListener}.
 */
class LatestPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "LatestPostsAdapter";
    private final int VIEW_TYPE_ITEM = 0;
    private final int VIEW_TYPE_LOADING = 1;
    final private LatestPostsFragment.LatestPostsFragmentInteractionListener interactionListener;
    private final ArrayList<PostSummary> parsedTopicSummaries;

    LatestPostsAdapter(BaseFragment.FragmentInteractionListener interactionListener,
                       ArrayList<PostSummary> parsedTopicSummaries) {
        this.interactionListener = (LatestPostsFragment.LatestPostsFragmentInteractionListener) interactionListener;
        this.parsedTopicSummaries = parsedTopicSummaries;
    }

    interface OnLoadMoreListener {
        void onLoadMore();
    }

    @Override
    public int getItemViewType(int position) {
        return parsedTopicSummaries.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.fragment_latest_posts_row, parent, false);
            return new LatestPostViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recycler_loading_item, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof LatestPostViewHolder) {
            PostSummary topic = parsedTopicSummaries.get(position);
            final LatestPostViewHolder latestPostViewHolder = (LatestPostViewHolder) holder;

            latestPostViewHolder.postTitle.setText(topic.getTitle());
            latestPostViewHolder.postDate.setText(topic.getDateTime());
            latestPostViewHolder.post.loadDataWithBaseURL("file:///android_asset/"
                    , topic.getPost(), "text/html", "UTF-8", null);

            latestPostViewHolder.latestPostsRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (interactionListener != null) {
                        // Notify the active callbacks interface (the activity, if the
                        // fragment is attached to one) that a post has been selected.
                        interactionListener.onLatestPostsFragmentInteraction(
                                parsedTopicSummaries.get(holder.getAdapterPosition()));
                    }

                }
            });
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return parsedTopicSummaries == null ? 0 : parsedTopicSummaries.size();
    }

    private static class LatestPostViewHolder extends RecyclerView.ViewHolder {
        final RelativeLayout latestPostsRow;
        final TextView postTitle;
        final TextView postDate;
        final WebView post;

        LatestPostViewHolder(View itemView) {
            super(itemView);
            latestPostsRow = (RelativeLayout) itemView.findViewById(R.id.latest_posts_row);
            postTitle = (TextView) itemView.findViewById(R.id.title);
            postDate = (TextView) itemView.findViewById(R.id.date);
            post = (WebView) itemView.findViewById(R.id.post);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        final MaterialProgressBar progressBar;

        LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (MaterialProgressBar) itemView.findViewById(R.id.recycler_progress_bar);
        }
    }
}