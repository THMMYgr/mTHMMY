package gr.thmmy.mthmmy.activities.main.forum;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bignerdranch.expandablerecyclerview.ExpandableRecyclerAdapter;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.Board;
import gr.thmmy.mthmmy.model.Category;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.parsing.NewParseTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.views.CustomRecyclerView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
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
            forumTask = new ForumTask(this::onForumTaskStarted, this::onForumTaskFinished);
            forumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

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
            progressBar = rootView.findViewById(R.id.progressBar);
            forumAdapter = new ForumAdapter(getContext(), categories, fragmentInteractionListener);
            forumAdapter.setExpandCollapseListener(new ExpandableRecyclerAdapter.ExpandCollapseListener() {
                @Override
                public void onParentExpanded(int parentPosition) {
                    if (BaseActivity.getSessionManager().isLoggedIn()) {
                        if (forumTask.getStatus() == AsyncTask.Status.RUNNING)
                            forumTask.cancel(true);
                        forumTask = new ForumTask(ForumFragment.this::onForumTaskStarted, ForumFragment.this::onForumTaskFinished);
                        forumTask.setUrl(categories.get(parentPosition).getCategoryURL());
                        forumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }

                @Override
                public void onParentCollapsed(int parentPosition) {
                    if (BaseActivity.getSessionManager().isLoggedIn()) {
                        if (forumTask.getStatus() == AsyncTask.Status.RUNNING)
                            forumTask.cancel(true);
                        forumTask = new ForumTask(ForumFragment.this::onForumTaskStarted, ForumFragment.this::onForumTaskFinished);
                        forumTask.setUrl(categories.get(parentPosition).getCategoryURL());
                        forumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                    }
                }
            });

            CustomRecyclerView recyclerView = rootView.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(forumAdapter);

            swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
            swipeRefreshLayout.setColorSchemeResources(R.color.accent);
            swipeRefreshLayout.setOnRefreshListener(() -> {
                        if (forumTask != null && forumTask.getStatus() != AsyncTask.Status.RUNNING) {
                            forumTask = new ForumTask(ForumFragment.this::onForumTaskStarted, ForumFragment.this::onForumTaskFinished);
                            //forumTask.execute(SessionManager.indexUrl.toString());
                            forumTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                        }
                    }
            );

        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (forumTask!=null){
            try{
                if(forumTask.isRunning())
                    forumTask.cancel(true);
            }    // Yes, it happens even though we checked
            catch (NullPointerException ignored){ }
        }
    }

    public interface ForumFragmentInteractionListener extends FragmentInteractionListener {
        void onForumFragmentInteraction(Board board);
    }

    private void onForumTaskStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void onForumTaskFinished(int resultCode, ArrayList<Category> fetchedCategories) {
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            categories.clear();
            categories.addAll(fetchedCategories);
            forumAdapter.notifyParentDataSetChanged(false);
        } else if (resultCode == NetworkResultCodes.NETWORK_ERROR) {
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "Unexpected error," +
                    " please contact the developers with the details", Toast.LENGTH_LONG).show();
        }

        progressBar.setVisibility(ProgressBar.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    //---------------------------------------ASYNC TASK-----------------------------------

    private class ForumTask extends NewParseTask<ArrayList<Category>> {
        private HttpUrl forumUrl = SessionManager.forumUrl;   //may change upon collapse/expand

        ForumTask(OnTaskStartedListener onTaskStartedListener,
                  OnNetworkTaskFinishedListener<ArrayList<Category>> onParseTaskFinishedListener) {
            super(onTaskStartedListener, onParseTaskFinishedListener);
        }

        @Override
        protected ArrayList<Category> parse(Document document, Response response) throws ParseException {
            Elements categoryBlocks = document.select(".tborder:not([style])>table[cellpadding=5]");
            if (categoryBlocks.size() != 0) {
                ArrayList<Category> fetchedCategories = new ArrayList<>();
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
                return fetchedCategories;
            } else
                throw new ParseException("Parsing failed");
        }

        @Override
        protected Response sendRequest(OkHttpClient client, String... input) throws IOException {
            Request request = new Request.Builder()
                    .url(forumUrl)
                    .build();
            return client.newCall(request).execute();
        }

        @Override
        protected int getResultCode(Response response, ArrayList<Category> data) {
            return NetworkResultCodes.SUCCESSFUL;
        }

        //TODO delete and simplify e.g. in prepareRequest possible?
        public void setUrl(String string) {
            forumUrl = HttpUrl.parse(string);
        }
    }
}
