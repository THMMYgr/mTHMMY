package gr.thmmy.mthmmy.activities.board;

import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.model.Board;
import gr.thmmy.mthmmy.model.Topic;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

/**
 * {@link RecyclerView.Adapter} that can display a {@link gr.thmmy.mthmmy.model.Board}.
 */
class BoardAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_SUB_BOARD = 0;
    private final int VIEW_TYPE_TOPIC = 1;
    private final int VIEW_TYPE_LOADING = 2;

    private final Context context;
    private ArrayList<Board> parsedSubBoards;
    private ArrayList<Topic> parsedTopics;
    private final ArrayList<Boolean> boardExpandableVisibility = new ArrayList<>();
    private final ArrayList<Boolean> topicExpandableVisibility = new ArrayList<>();

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
        if (position < parsedSubBoards.size()) {
            return VIEW_TYPE_SUB_BOARD;
        }
        else if (position < parsedSubBoards.size() + parsedTopics.size()) {
            if (parsedTopics.get(position - parsedSubBoards.size()) != null)    //??
                return VIEW_TYPE_TOPIC;
        }
        return VIEW_TYPE_LOADING;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SUB_BOARD) {
            View subBoard = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_board_sub_board_row, parent, false);
            return new SubBoardViewHolder(subBoard);
        }
        else if (viewType == VIEW_TYPE_TOPIC) {
            View topic = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_board_topic_row, parent, false);
            return new TopicViewHolder(topic);
        }
        // viewType == VIEW_TYPE_LOADING
        else {
            View loading = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recycler_loading_item, parent, false);
            return new LoadingViewHolder(loading);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof SubBoardViewHolder) {
            final Board subBoard = parsedSubBoards.get(position);
            final SubBoardViewHolder subBoardViewHolder = (SubBoardViewHolder) holder;

            if (boardExpandableVisibility.size() != parsedSubBoards.size()) {
                for (int i = boardExpandableVisibility.size(); i < parsedSubBoards.size(); ++i)
                    boardExpandableVisibility.add(false);
            }

            subBoardViewHolder.boardRow.setOnClickListener(view -> {
                Intent intent = new Intent(context, BoardActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_BOARD_URL, subBoard.getUrl());
                extras.putString(BUNDLE_BOARD_TITLE, subBoard.getTitle());
                intent.putExtras(extras);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            });
            if (boardExpandableVisibility.get(subBoardViewHolder.getBindingAdapterPosition())) {
                subBoardViewHolder.boardExpandable.setVisibility(View.VISIBLE);
                subBoardViewHolder.showHideExpandable.setImageResource(R.drawable.ic_arrow_drop_up_accent_24dp);
            }
            else {
                subBoardViewHolder.boardExpandable.setVisibility(View.GONE);
                subBoardViewHolder.showHideExpandable.setImageResource(R.drawable.ic_arrow_drop_down_accent_24dp);
            }
            subBoardViewHolder.showHideExpandable.setOnClickListener(view -> {
                final int pos = subBoardViewHolder.getBindingAdapterPosition();
                if (pos >=0 && pos < boardExpandableVisibility.size()){
                    final boolean visible = boardExpandableVisibility.get(subBoardViewHolder.getBindingAdapterPosition());
                    if (visible) {
                        subBoardViewHolder.boardExpandable.setVisibility(View.GONE);
                        subBoardViewHolder.showHideExpandable.setImageResource(R.drawable.ic_arrow_drop_down_accent_24dp);
                    }
                    else {
                        subBoardViewHolder.boardExpandable.setVisibility(View.VISIBLE);
                        subBoardViewHolder.showHideExpandable.setImageResource(R.drawable.ic_arrow_drop_up_accent_24dp);
                    }
                    boardExpandableVisibility.set(subBoardViewHolder.getBindingAdapterPosition(), !visible);
                }
            });
            subBoardViewHolder.boardTitle.setText(subBoard.getTitle());
            String mods = subBoard.getMods();
            String stats = subBoard.getStats();
            String lastPost = subBoard.getLastPost();

            if (!mods.isEmpty()) {
                subBoardViewHolder.boardMods.setText(mods);
                subBoardViewHolder.boardMods.setVisibility(View.VISIBLE);
            }

            if (!stats.isEmpty()) {
                subBoardViewHolder.boardStats.setText(stats);
                subBoardViewHolder.boardStats.setVisibility(View.VISIBLE);
            }

            if (!lastPost.isEmpty()) {
                subBoardViewHolder.boardLastPost.setText(lastPost);
                subBoardViewHolder.boardLastPost.setVisibility(View.VISIBLE);
            }

            if (!Objects.equals(subBoard.getLastPostUrl(), "")) {
                subBoardViewHolder.boardLastPost.setOnClickListener(view -> {
                    Intent intent = new Intent(context, TopicActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString(BUNDLE_TOPIC_URL, subBoard.getLastPostUrl());
                    //Doesn't put an already ellipsized topic title in Bundle
                    intent.putExtras(extras);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    context.startActivity(intent);
                });
            }
        }
        else if (holder instanceof TopicViewHolder) {
            final Topic topic = parsedTopics.get(position - parsedSubBoards.size());
            final TopicViewHolder topicViewHolder = (TopicViewHolder) holder;

            if (topicExpandableVisibility.size() != parsedTopics.size()) {
                for (int i = topicExpandableVisibility.size(); i < parsedTopics.size(); ++i)
                    topicExpandableVisibility.add(false);
            }

            topicViewHolder.topicRow.setOnClickListener(view -> {
                Intent intent = new Intent(context, TopicActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_TOPIC_URL, topic.getTopicUrl());
                extras.putString(BUNDLE_TOPIC_TITLE, topic.getSubject());
                intent.putExtras(extras);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });

            final int pos = topicViewHolder.getBindingAdapterPosition() - parsedSubBoards.size();
            if (pos >=0 && pos < topicExpandableVisibility.size() && topicExpandableVisibility.get(pos)) {
                topicViewHolder.topicExpandable.setVisibility(View.VISIBLE);
                topicViewHolder.showHideExpandable.setImageResource(R.drawable.ic_arrow_drop_up_accent_24dp);
            }
            else {
                topicViewHolder.topicExpandable.setVisibility(View.GONE);
                topicViewHolder.showHideExpandable.setImageResource(R.drawable.ic_arrow_drop_down_accent_24dp);
            }

            topicViewHolder.showHideExpandable.setOnClickListener(view -> {
                final int pos2 = topicViewHolder.getBindingAdapterPosition() - parsedSubBoards.size();
                if (pos2 >=0 && pos2 < topicExpandableVisibility.size()) {
                    final boolean visible = topicExpandableVisibility.get(pos2);
                    if (visible) {
                        topicViewHolder.topicExpandable.setVisibility(View.GONE);
                        topicViewHolder.showHideExpandable.setImageResource(R.drawable.ic_arrow_drop_down_accent_24dp);
                    }
                    else {
                        topicViewHolder.topicExpandable.setVisibility(View.VISIBLE);
                        topicViewHolder.showHideExpandable.setImageResource(R.drawable.ic_arrow_drop_up_accent_24dp);
                    }
                    topicExpandableVisibility.set(pos2, !visible);
                }
            });
            topicViewHolder.topicSubject.setTypeface(Typeface.createFromAsset(context.getAssets()
                    , "fonts/fontawesome-webfont.ttf"));
            topicViewHolder.topicUnreadDot.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/fontawesome-webfont.ttf"));
            if (topic.isUnread())
                topicViewHolder.topicUnreadDot.setVisibility(View.VISIBLE);
            else
                topicViewHolder.topicUnreadDot.setVisibility(View.GONE);
            String lockedSticky = topic.getSubject();
            if (topic.isLocked())
                lockedSticky += " " + context.getResources().getString(R.string.fa_lock);
            if (topic.isSticky()) {
                //topicViewHolder.topicSubject.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_pin, 0);
                lockedSticky += " " + context.getResources().getString(R.string.fa_thumbtack);
            }
            topicViewHolder.topicSubject.setText(lockedSticky);
            topicViewHolder.topicStartedBy.setText(context.getString(R.string.topic_started_by, topic.getStarter()));
            topicViewHolder.topicStats.setText(topic.getStats());
            topicViewHolder.topicLastPost.setText(context.getString(R.string.topic_last_post, topic.getLastPostDateTime(), topic.getLastUser()));
            topicViewHolder.topicLastPost.setOnClickListener(view -> {
                Intent intent = new Intent(context, TopicActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_TOPIC_URL, topic.getLastPostUrl());
                //Doesn't put an already ellipsized topic title in Bundle
                intent.putExtras(extras);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                context.startActivity(intent);
            });
        }
        else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        int items = 0;
        if (parsedSubBoards != null) items += parsedSubBoards.size();
        if (parsedTopics != null) items += parsedTopics.size();
        return items;
    }

    private static class SubBoardViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout boardRow, boardExpandable;
        final TextView boardTitle, boardMods, boardStats, boardLastPost;
        final ImageButton showHideExpandable;

        SubBoardViewHolder(View board) {
            super(board);
            boardRow = board.findViewById(R.id.child_board_row);
            boardExpandable = board.findViewById(R.id.child_board_expandable);
            showHideExpandable = board.findViewById(R.id.child_board_expand_collapse_button);
            boardTitle = board.findViewById(R.id.child_board_title);
            boardMods = board.findViewById(R.id.child_board_mods);
            boardStats = board.findViewById(R.id.child_board_stats);
            boardLastPost = board.findViewById(R.id.child_board_last_post);
        }
    }

    private static class TopicViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout topicRow, topicExpandable;
        final TextView topicSubject, topicStartedBy, topicStats, topicLastPost, topicUnreadDot;
        final ImageButton showHideExpandable;

        TopicViewHolder(View topic) {
            super(topic);
            topicRow = topic.findViewById(R.id.topic_row_linear);
            topicExpandable = topic.findViewById(R.id.topic_expandable);
            showHideExpandable = topic.findViewById(R.id.topic_expand_collapse_button);
            topicUnreadDot = topic.findViewById(R.id.topic_unread_dot);
            topicSubject = topic.findViewById(R.id.topic_subject);
            topicStartedBy = topic.findViewById(R.id.topic_started_by);
            topicStats = topic.findViewById(R.id.topic_stats);
            topicLastPost = topic.findViewById(R.id.topic_last_post);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        final MaterialProgressBar progressBar;

        LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.recycler_progress_bar);
        }
    }
}
