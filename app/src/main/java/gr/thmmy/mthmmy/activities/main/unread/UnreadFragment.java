package gr.thmmy.mthmmy.activities.main.unread;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.TopicSummary;
import gr.thmmy.mthmmy.session.InvalidSessionException;
import gr.thmmy.mthmmy.session.MarkAsReadTask;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.parsing.NewParseTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.views.CustomRecyclerView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.Response;

/**
 * A {@link BaseFragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UnreadFragment.UnreadFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UnreadFragment#newInstance} factory method to
 * create an instance of this fragment.
 */

public class UnreadFragment extends BaseFragment {
    private static final String TAG = "UnreadFragment";

    private MaterialProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton markAsReadFAB;
    private TextView noUnreadTopicsTextView;

    private UnreadAdapter unreadAdapter;

    private List<TopicSummary> topicSummaries;
    private int numberOfPages = 0;
    private int loadedPages = 0;

    private UnreadTask unreadTask;
    private MarkAsReadTask markAsReadTask;

    // Required empty public constructor
    public UnreadFragment() {}

    /**
     * Use ONLY this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Unread.
     */
    public static UnreadFragment newInstance(int sectionNumber) {
        UnreadFragment fragment = new UnreadFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, TAG);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        topicSummaries = new ArrayList<>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (topicSummaries.isEmpty()){
            hideMarkAsReadFAB();
            unreadTask = new UnreadTask(this::onUnreadTaskStarted, UnreadFragment.this::onUnreadTaskCancelled, this::onUnreadTaskFinished);
            assert SessionManager.unreadUrl != null;
            unreadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.unreadUrl.toString());
        }
        else
            showMarkAsReadFAB();
        markAsReadTask = new MarkAsReadTask(UnreadFragment.this::onMarkAsReadTaskStarted, UnreadFragment.this::onMarkAsReadTaskFinished);
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_unread, container, false);

        // Set the adapter
        if (rootView instanceof CoordinatorLayout) {
            progressBar = rootView.findViewById(R.id.progressBar);
            noUnreadTopicsTextView = rootView.findViewById(R.id.no_unread_topics);
            markAsReadFAB = rootView.findViewById(R.id.unread_fab);
            
            unreadAdapter = new UnreadAdapter(topicSummaries, fragmentInteractionListener);

            CustomRecyclerView recyclerView = rootView.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(unreadAdapter);

            swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
            swipeRefreshLayout.setColorSchemeResources(R.color.accent);
            swipeRefreshLayout.setOnRefreshListener(
                    this::startUnreadTask
            );
        }
        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelUnreadTaskIfRunning();
        if (markAsReadTask !=null){
            try{
                if(markAsReadTask.isRunning())
                    markAsReadTask.cancel(true);
            }    // Yes, it happens even though we checked
            catch (NullPointerException ignored){ }
        }
        if(topicSummaries!=null)
            topicSummaries.clear();
    }

    private void startUnreadTask(){
        if (unreadTask!=null) {
            try{
                if(!unreadTask.isRunning()){
                    numberOfPages = 0;
                    loadedPages = 0;
                    unreadTask = new UnreadTask(UnreadFragment.this::onUnreadTaskStarted, UnreadFragment.this::onUnreadTaskCancelled, UnreadFragment.this::onUnreadTaskFinished);
                    assert SessionManager.unreadUrl != null;
                    unreadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.unreadUrl.toString());
                }
            }
            catch (NullPointerException ignored){ }
        }
    }

    private void cancelUnreadTaskIfRunning(){
        if (unreadTask!=null){
            try{
                if(unreadTask.isRunning())
                    unreadTask.cancel(true);
            }    // Yes, it happens even though we checked
            catch (NullPointerException ignored){ }
        }
    }

    public interface UnreadFragmentInteractionListener extends FragmentInteractionListener {
        void onUnreadFragmentInteraction(TopicSummary topicSummary);
    }

    private void showMarkAsReadFAB() {
        markAsReadFAB.setOnClickListener(v -> showMarkAsReadConfirmationDialog());
        markAsReadFAB.show();
        markAsReadFAB.setTag(true);
    }

    private void hideMarkAsReadFAB() {
        markAsReadFAB.setOnClickListener(null);
        markAsReadFAB.hide();
        markAsReadFAB.setTag(false);
    }

    private void showMarkAsReadConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Mark all as read");
        builder.setMessage("Are you sure that you want to mark ALL topics as read?");
        builder.setPositiveButton("Yep", (dialogInterface, i) -> {
            if (!markAsReadTask.isRunning()){
                markAsReadTask = new MarkAsReadTask(this::onMarkAsReadTaskStarted, this::onMarkAsReadTaskFinished);
                markAsReadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        builder.setNegativeButton("Nope", (dialogInterface, i) -> {});
        builder.create().show();
    }

    private void hideProgressUI(){
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
    }

    //---------------------------------------UNREAD TASK-----------------------------------

    private void onUnreadTaskStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void onUnreadTaskCancelled() {
        swipeRefreshLayout.setRefreshing(false);
    }

    private void onUnreadTaskFinished(int resultCode,  ArrayList<TopicSummary> fetchedUnread) {
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            if(!fetchedUnread.isEmpty()){
                if(loadedPages==0)
                    topicSummaries.clear();
                topicSummaries.addAll(fetchedUnread);
                noUnreadTopicsTextView.setVisibility(View.INVISIBLE);
                showMarkAsReadFAB();
            }
            else {
                topicSummaries.clear();
                hideMarkAsReadFAB();
                noUnreadTopicsTextView.setVisibility(View.VISIBLE);
            }
            unreadAdapter.notifyDataSetChanged();
            loadedPages++;
            if (loadedPages < numberOfPages) {
                unreadTask = new UnreadTask(this::onUnreadTaskStarted, UnreadFragment.this::onUnreadTaskCancelled, this::onUnreadTaskFinished);
                assert SessionManager.unreadUrl != null;
                unreadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.unreadUrl.toString() + ";start=" + loadedPages * 20);
            }
            else
                hideProgressUI();
        }
        else{
            hideProgressUI();
            if (resultCode == NetworkResultCodes.NETWORK_ERROR)
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            else if (resultCode == SessionManager.INVALID_SESSION)
                Toast.makeText(getContext(), "Session verification failed. Please try logging in again.", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getContext(), "Unexpected error," +
                        " please contact the developers with the details", Toast.LENGTH_LONG).show();
        }
    }

    private class UnreadTask extends NewParseTask<ArrayList<TopicSummary>> {
        UnreadTask(OnTaskStartedListener onTaskStartedListener, OnTaskCancelledListener onTaskCancelledListener,  OnNetworkTaskFinishedListener<ArrayList<TopicSummary>> onParseTaskFinishedListener) {
            super(onTaskStartedListener, onTaskCancelledListener, onParseTaskFinishedListener);
        }

        @Override
        protected ArrayList<TopicSummary> parse(Document document, Response response) throws ParseException, InvalidSessionException {
            if(!document.select("td:containsOwn(Only registered members are allowed to access this section.)").isEmpty())
                throw new InvalidSessionException();

            Elements unread = document.select("table.bordercolor[cellspacing=1] tr:not(.titlebg)");
            ArrayList<TopicSummary> fetchedTopicSummaries = new ArrayList<>();
            if (!unread.isEmpty()) {
                for (Element row : unread) {
                    Elements information = row.select("td");
                    String link = information.last().select("a").first().attr("href");
                    String title = information.get(2).select("a").first().text();

                    Element lastUserAndDate = information.get(6);
                    String lastUser = lastUserAndDate.select("a").text();
                    String dateTime = lastUserAndDate.select("span").html();
                    dateTime = dateTime.substring(0, dateTime.indexOf("<br>"));
                    dateTime = dateTime.replace("<b>", "");
                    dateTime = dateTime.replace("</b>", "");

                    fetchedTopicSummaries.add(new TopicSummary(link, title, lastUser, dateTime));
                }
                Element topBar = document.select("table:not(.bordercolor):not(#bodyarea):has(td.middletext)").first();

                Element pagesElement = null;
                if (topBar != null)
                    pagesElement = topBar.select("td.middletext").first();


                if (numberOfPages == 0 && pagesElement != null) {
                    Elements pages = pagesElement.select("a");
                    if (!pages.isEmpty())
                        numberOfPages = Integer.parseInt(pages.last().text());
                    else
                        numberOfPages = 1;
                }

                return fetchedTopicSummaries;
            }
            return new ArrayList<>();
        }

        @Override
        protected int getResultCode(Response response, ArrayList<TopicSummary> topicSummaries) {
            return NetworkResultCodes.SUCCESSFUL;
        }
    }

    //---------------------------------------MARK AS READ TASK------------------------------------------
    private void onMarkAsReadTaskStarted() {
        cancelUnreadTaskIfRunning();
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void onMarkAsReadTaskFinished(int resultCode,  Void v) {
        hideProgressUI();
        if (resultCode == NetworkResultCodes.SUCCESSFUL)
                startUnreadTask();
        else{
            hideProgressUI();
            if (resultCode == NetworkResultCodes.NETWORK_ERROR)
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            else if (resultCode == SessionManager.INVALID_SESSION)
                Toast.makeText(getContext(), "Session verification failed. Please try logging out and back in again", Toast.LENGTH_LONG).show();
            else
                Toast.makeText(getContext(), "Unexpected error," +
                        " please contact the developers with the details", Toast.LENGTH_LONG).show();
        }
    }
}
