package gr.thmmy.mthmmy.activities.main.forum;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bignerdranch.expandablerecyclerview.ChildViewHolder;
import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;
import com.bignerdranch.expandablerecyclerview.ParentViewHolder;

import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.base.BaseFragment;
import gr.thmmy.mthmmy.data.Board;
import gr.thmmy.mthmmy.data.Category;
import gr.thmmy.mthmmy.data.TopicSummary;


/**
 * {@link RecyclerView.Adapter} that can display a {@link TopicSummary} and makes a call to the
 * specified {@link ForumFragment.ForumFragmentInteractionListener}.
 */
class ForumAdapter extends ExpandableRecyclerAdapter<Category, Board, ForumAdapter.CategoryViewHolder, ForumAdapter.BoardViewHolder> {
    private final Context context;
    private final LayoutInflater layoutInflater;

    private final List<Category> categories;
    private final ForumFragment.ForumFragmentInteractionListener mListener;

    ForumAdapter(Context context, @NonNull List<Category> categories, BaseFragment.FragmentInteractionListener listener) {
        super(categories);
        this.context = context;
        this.categories = categories;
        mListener = (ForumFragment.ForumFragmentInteractionListener)listener;
        layoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateParentViewHolder(@NonNull ViewGroup parentViewGroup, int viewType) {
        View categoryView = layoutInflater.inflate(R.layout.fragment_forum_category_row, parentViewGroup, false);
        return new CategoryViewHolder(categoryView);
    }

    @NonNull
    @Override
    public BoardViewHolder onCreateChildViewHolder(@NonNull ViewGroup childViewGroup, int viewType) {
        View boardView = layoutInflater.inflate(R.layout.fragment_forum_board_row, childViewGroup, false);
        return new BoardViewHolder(boardView);
    }

    @Override
    public void onBindParentViewHolder(@NonNull CategoryViewHolder parentViewHolder, int parentPosition, @NonNull Category parent) {
        parentViewHolder.bind(parent);
    }

    @Override
    public void onBindChildViewHolder(@NonNull BoardViewHolder childViewHolder, int parentPosition, int childPosition, @NonNull Board child) {
        childViewHolder.bind(child);
    }


    class CategoryViewHolder extends ParentViewHolder {

        private TextView categoryTextview;

        CategoryViewHolder(View itemView) {
            super(itemView);
            categoryTextview = (TextView) itemView.findViewById(R.id.category);
        }

        void bind(Category category) {
            categoryTextview.setText(category.getName());
        }

    }

    class BoardViewHolder extends ChildViewHolder {

        private TextView boardTextView;

        BoardViewHolder(View itemView) {
            super(itemView);
            boardTextView = (TextView) itemView.findViewById(R.id.board);
        }

        void bind(Board board) {
            boardTextView.setText(board.getName());
        }
    }
}
