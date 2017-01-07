package gr.thmmy.mthmmy.activities.board;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.data.Board;
import gr.thmmy.mthmmy.data.Topic;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

/**
 * {@link RecyclerView.Adapter} that can display a {@link gr.thmmy.mthmmy.data.Board}.
 */
class BoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "BoardAdapter";
    private final int VIEW_TYPE_SUB_BOARD = 0;
    private final int VIEW_TYPE_TOPIC = 1;
    private final int VIEW_TYPE_LOADING = 2;

    private final Context context;
    private ArrayList<Board> parsedSubBoards = new ArrayList<>();
    private ArrayList<Topic> parsedTopics = new ArrayList<>();

    BoardAdapter(Context context, ArrayList<Board> parsedSubBoards, ArrayList<Topic> parsedTopics) {
        this.context = context;
        this.parsedSubBoards = parsedSubBoards;
        this.parsedTopics = parsedTopics;
    }

    interface OnLoadMoreListener {
        void onLoadMore();
    }

    @Override
    public int getItemViewType(int position) {
        if (position < parsedSubBoards.size())
            return VIEW_TYPE_SUB_BOARD;
        else if (parsedTopics.get(position - parsedSubBoards.size()) != null)
            return VIEW_TYPE_TOPIC;
        else return VIEW_TYPE_LOADING;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SUB_BOARD) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_board_sub_board, parent, false);
            return new SubBoardViewHolder(view);
        } else if (viewType == VIEW_TYPE_TOPIC) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_board_topic, parent, false);
            return new TopicViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recycler_loading_item, parent, false);
            return new LoadingViewHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof SubBoardViewHolder) {
            Board subBoard = parsedSubBoards.get(position);
            final SubBoardViewHolder subBoardViewHolder = (SubBoardViewHolder) holder;

            subBoardViewHolder.boardRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, BoardActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString(BUNDLE_BOARD_URL, parsedSubBoards.get(holder.
                            getAdapterPosition()).getUrl());
                    extras.putString(BUNDLE_BOARD_TITLE, parsedSubBoards.get(holder.
                            getAdapterPosition()).getTitle());
                    intent.putExtras(extras);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            subBoardViewHolder.boardTitle.setText(subBoard.getTitle());
            subBoardViewHolder.boardMods.setText(context.getString(R.string.child_board_mods, subBoard.getMods()));
            subBoardViewHolder.boardStats.setText(subBoard.getStats());
            subBoardViewHolder.boardLastPost.setText(subBoard.getLastPost());
        } else if (holder instanceof TopicViewHolder) {
            Topic topic = parsedTopics.get(position - parsedSubBoards.size());
            final TopicViewHolder topicViewHolder = (TopicViewHolder) holder;

            topicViewHolder.topicRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, TopicActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString(BUNDLE_TOPIC_URL, parsedTopics.get(holder.
                            getAdapterPosition()).getUrl());
                    extras.putString(BUNDLE_TOPIC_TITLE, parsedTopics.get(holder.
                            getAdapterPosition()).getSubject());
                    intent.putExtras(extras);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });
            topicViewHolder.topicSubject.setText(topic.getSubject());
            String lockedSticky = "";
            if (topic.isLocked())
                lockedSticky += context.getResources().getString(R.string.fa_lock);
            if (topic.isSticky())
                lockedSticky += context.getResources().getString(R.string.fa_sticky);
            topicViewHolder.topicLockedSticky.setText(lockedSticky);
            topicViewHolder.topicStartedBy.setText(context.getString(R.string.topic_started_by, topic.getStarter()));
            topicViewHolder.topicStats.setText(topic.getStats());
            topicViewHolder.topicLastPost.setText(context.getString(R.string.topic_last_post, topic.getLastPost()));
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        if (parsedSubBoards == null && parsedTopics == null) return 0;
        else if (parsedSubBoards == null) return parsedTopics.size();
        else if (parsedTopics == null) return parsedSubBoards.size();
        else return parsedSubBoards.size() + parsedTopics.size();
    }

    private static class SubBoardViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout boardRow;
        final TextView boardTitle, boardMods, boardStats, boardLastPost;

        SubBoardViewHolder(View itemView) {
            super(itemView);
            boardRow = (LinearLayout) itemView.findViewById(R.id.child_board_row);
            boardTitle = (TextView) itemView.findViewById(R.id.child_board_title);
            boardMods = (TextView) itemView.findViewById(R.id.child_board_mods);
            boardStats = (TextView) itemView.findViewById(R.id.child_board_stats);
            boardLastPost = (TextView) itemView.findViewById(R.id.child_board_last_post);
        }
    }

    private static class TopicViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout topicRow;
        final TextView topicSubject, topicLockedSticky, topicStartedBy, topicStats, topicLastPost;

        TopicViewHolder(View itemView) {
            super(itemView);
            topicRow = (LinearLayout) itemView.findViewById(R.id.topic_row_linear);
            topicSubject = (TextView) itemView.findViewById(R.id.topic_subject);
            topicLockedSticky = (TextView) itemView.findViewById(R.id.topic_locked_sticky);
            topicStartedBy = (TextView) itemView.findViewById(R.id.topic_started_by);
            topicStats = (TextView) itemView.findViewById(R.id.topic_stats);
            topicLastPost = (TextView) itemView.findViewById(R.id.topic_last_post);
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
