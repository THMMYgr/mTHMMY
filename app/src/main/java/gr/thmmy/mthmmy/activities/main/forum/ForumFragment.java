package gr.thmmy.mthmmy.activities.main.forum;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.Board;
import gr.thmmy.mthmmy.model.Category;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.ParseTask;
import gr.thmmy.mthmmy.utils.exceptions.ParseException;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.HttpUrl;
import okhttp3.Request;
import timber.log.Timber;

/**
 * A {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ForumFragment.ForumFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ForumFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ForumFragment extends BaseFragment {
    private static final String TAG = "ForumFragment";
    // Fragment initialization parameters, e.g. ARG_SECTION_NUMBER

    private MaterialProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ForumAdapter forumAdapter;

    private List<Category> categories;

    private ForumTask forumTask;

    // Required empty public constructor
    public ForumFragment() {
    }

    /**
     * Use ONLY this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Forum.
     */
    public static ForumFragment newInstance(int sectionNumber) {
        ForumFragment fragment = new ForumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, TAG);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        categories = new ArrayList<>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (categories.isEmpty()) {
            forumTask = new ForumTask();
            forumTask.execute();

        }
        Timber.d("onActivityCreated");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_forum, container, false);

        // Set the adapter
        if (rootView instanceof RelativeLayout) {
            progressBar = (MaterialProgressBar) rootView.findViewById(R.id.progressBar);
            forumAdapter = new ForumAdapter(getContext(), categories, fragmentInteractionListener);
            forumAdapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
                @Override
                public void onParentExpanded(int parentPosition) {
                    if (BaseActivity.getSessionManager().isLoggedIn()) {
                        if (forumTask.getStatus() == AsyncTask.Status.RUNNING)
                            forumTask.cancel(true);
                        forumTask = new ForumTask();
                        forumTask.setUrl(categories.get(parentPosition).getCategoryURL());
                        forumTask.execute();
                    }
                }

                @Override
                public void onParentCollapsed(int parentPosition) {
                    if (BaseActivity.getSessionManager().isLoggedIn()) {
                        if (forumTask.getStatus() == AsyncTask.Status.RUNNING)
                            forumTask.cancel(true);
                        forumTask = new ForumTask();
                        forumTask.setUrl(categories.get(parentPosition).getCategoryURL());
                        forumTask.execute();
                    }
                }
            });

            CustomRecyclerView recyclerView = (CustomRecyclerView) rootView.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.findViewById(R.id.list).getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(forumAdapter);

            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            if (forumTask != null && forumTask.getStatus() != AsyncTask.Status.RUNNING) {
                                forumTask = new ForumTask();
                                forumTask.execute(SessionManager.indexUrl.toString());
                            }
                        }

                    }
            );

        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (forumTask != null && forumTask.getStatus() != AsyncTask.Status.RUNNING)
            forumTask.cancel(true);
    }

    public interface ForumFragmentInteractionListener extends FragmentInteractionListener {
        void onForumFragmentInteraction(Board board);
    }

    //---------------------------------------ASYNC TASK-----------------------------------

    private class ForumTask extends ParseTask {
        private HttpUrl forumUrl = SessionManager.forumUrl;   //may change upon collapse/expand

        private final List<Category> fetchedCategories;

        ForumTask() {
            fetchedCategories = new ArrayList<>();
        }

        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Request prepareRequest(String... params) {
            return new Request.Builder()
                    .url(forumUrl)
                    .build();
        }


        @Override
        public void parse(Document document) throws ParseException {
            Elements categoryBlocks = document.select(".tborder:not([style])>table[cellpadding=5]");
            if (categoryBlocks.size() != 0) {
                for (Element categoryBlock : categoryBlocks) {
                    Element categoryElement = categoryBlock.select("td[colspan=2]>[name]").first();
                    String categoryUrl = categoryElement.attr("href");
                    Category category = new Category(categoryElement.text(), categoryUrl);

                    if (categoryUrl.contains("sa=collapse") || !BaseActivity.getSessionManager().isLoggedIn()) {
                        category.setExpanded(true);
                        Elements boardsElements = categoryBlock.select("b [name]");
                        for (Element boardElement : boardsElements) {
                            Board board = new Board(boardElement.attr("href"), boardElement.text(), null, null, null, null);
                            category.getBoards().add(board);
                        }
                    } else
                        category.setExpanded(false);

                    fetchedCategories.add(category);
                }
                categories.clear();
                categories.addAll(fetchedCategories);
                fetchedCategories.clear();
            } else
                throw new ParseException("Parsing failed");
        }

        @Override
        protected void postParsing(ParseTask.ResultCode result) {
            if (result == ResultCode.SUCCESS)
                forumAdapter.notifyParentDataSetChanged(false);

            progressBar.setVisibility(ProgressBar.INVISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }

        public void setUrl(String string)   //TODO delete and simplify e.g. in prepareRequest possible?
        {
            forumUrl = HttpUrl.parse(string);
        }
    }
}
