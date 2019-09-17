
package gr.thmmy.mthmmy.activities.main.unread;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.TopicSummary;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.parsing.NewParseTask;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

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
    private UnreadAdapter unreadAdapter;

    private List<TopicSummary> topicSummaries;
    private int numberOfPages = 0;
    private int loadedPages = 0;

    private UnreadTask unreadTask;
    private MarkReadTask markReadTask;

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
        if (topicSummaries.isEmpty()) {
            unreadTask = new UnreadTask(this::onUnreadTaskStarted, this::onUnreadTaskFinished);
            assert SessionManager.unreadUrl != null;
            unreadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.unreadUrl.toString());
        }
        markReadTask = new MarkReadTask();
        Timber.d("onActivityCreated");
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_unread, container, false);

        // Set the adapter
        if (rootView instanceof RelativeLayout) {
            progressBar = rootView.findViewById(R.id.progressBar);
            unreadAdapter = new UnreadAdapter(topicSummaries,
                    fragmentInteractionListener, markReadLinkUrl -> {
                if (!markReadTask.isRunning() && !unreadTask.isRunning()) {
                    markReadTask = new MarkReadTask();
                    markReadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, markReadLinkUrl);
                }
            });

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
                    () -> {
                        if (!unreadTask.isRunning()) {
                            numberOfPages = 0;
                            loadedPages = 0;
                            unreadTask = new UnreadTask(this::onUnreadTaskStarted, this::onUnreadTaskFinished);
                            assert SessionManager.unreadUrl != null;
                            unreadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.unreadUrl.toString());
                        }
                    }
            );
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unreadTask!=null && unreadTask.isRunning())
            unreadTask.cancel(true);
        if (markReadTask!=null && markReadTask.isRunning())
            markReadTask.cancel(true);
        if(topicSummaries!=null)
            topicSummaries.clear();
    }

    public interface UnreadFragmentInteractionListener extends FragmentInteractionListener {
        void onUnreadFragmentInteraction(TopicSummary topicSummary);
    }

    //---------------------------------------ASYNC TASK-----------------------------------

    private void onUnreadTaskStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    private void onUnreadTaskFinished(int resultCode,  ArrayList<TopicSummary> fetchedUnread) {
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            if(fetchedUnread!=null && !fetchedUnread.isEmpty()){
                if(loadedPages==0)
                    topicSummaries.clear();
                topicSummaries.addAll(fetchedUnread);
                unreadAdapter.notifyDataSetChanged();
            }
            loadedPages++;
            if (loadedPages < numberOfPages) {
                unreadTask = new UnreadTask(this::onUnreadTaskStarted, this::onUnreadTaskFinished);
                assert SessionManager.unreadUrl != null;
                unreadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.unreadUrl.toString() + ";start=" + loadedPages * 20);
            }
            else {
                progressBar.setVisibility(ProgressBar.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);
            }
        }
        else{
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            swipeRefreshLayout.setRefreshing(false);
            if (resultCode == NetworkResultCodes.NETWORK_ERROR)
                Toast.makeText(getContext(), "Network error", Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(getContext(), "Unexpected error," +
                        " please contact the developers with the details", Toast.LENGTH_LONG).show();
        }
    }

    private class UnreadTask extends NewParseTask<ArrayList<TopicSummary>> {

        UnreadTask(OnTaskStartedListener onTaskStartedListener, OnNetworkTaskFinishedListener<ArrayList<TopicSummary>> onParseTaskFinishedListener) {
            super(onTaskStartedListener, onParseTaskFinishedListener);
        }

        @Override
        protected ArrayList<TopicSummary> parse(Document document, Response response) throws ParseException {
            Elements unread = document.select("table.bordercolor[cellspacing=1] tr:not(.titlebg)");
            ArrayList<TopicSummary> fetchedTopicSummaries = new ArrayList<>();
            if (!unread.isEmpty()) {
                //topicSummaries.clear();
                for (Element row : unread) {
                    Elements information = row.select("td");
                    String link = information.last().select("a").first().attr("href");
                    String title = information.get(2).select("a").first().text();

                    Element lastUserAndDate = information.get(6);
                    String lastUser = lastUserAndDate.select("a").text();
                    String dateTime = lastUserAndDate.select("span").html();
                    //dateTime = dateTime.replace("<br>", "");
                    dateTime = dateTime.substring(0, dateTime.indexOf("<br>"));
                    dateTime = dateTime.replace("<b>", "");
                    dateTime = dateTime.replace("</b>", "");
                    if (dateTime.contains(" am") || dateTime.contains(" pm") ||
                            dateTime.contains(" πμ") || dateTime.contains(" μμ"))
                        dateTime = dateTime.replaceAll(":[0-5][0-9] ", " ");
                    else
                        dateTime = dateTime.substring(0, dateTime.lastIndexOf(":"));
                    if (!dateTime.contains(","))
                        dateTime = dateTime.replaceAll(".+? ([0-9])", "$1");

                    fetchedTopicSummaries.add(new TopicSummary(link, title, lastUser, dateTime));
                }
                Element topBar = document.select("table:not(.bordercolor):not(#bodyarea):has(td.middletext)").first();

                Element pagesElement = null, markRead = null;
                if (topBar != null) {
                    pagesElement = topBar.select("td.middletext").first();

                    markRead = document.select("table:not(.bordercolor):not([width])").select("a")
                            .first();
                }

                if (numberOfPages == 0 && pagesElement != null) {
                    Elements pages = pagesElement.select("a");
                    if (!pages.isEmpty())
                        numberOfPages = Integer.parseInt(pages.last().text());
                    else
                        numberOfPages = 1;
                }

                if (markRead != null && loadedPages == numberOfPages - 1)
                    fetchedTopicSummaries.add(new TopicSummary(markRead.attr("href"), markRead.text(), null,
                            null));
            } else {
                String message = document.select("table.bordercolor[cellspacing=1]").first().text();
                if (message.contains("No messages")) { //It's english
                    message = "No unread posts!";
                } else { //It's greek
                    message = "Δεν υπάρχουν μη αναγνωσμένα μηνύματα!";
                }
                fetchedTopicSummaries.add(new TopicSummary(null, null, null, message));
            }
            return fetchedTopicSummaries;
        }

        @Override
        protected int getResultCode(Response response, ArrayList<TopicSummary> data) {
            return NetworkResultCodes.SUCCESSFUL;
        }
    }

    private class MarkReadTask extends AsyncTask<String, Void, Integer> {
        private static final int SUCCESS = 0;
        private static final int NETWORK_ERROR = 1;
        private static final int OTHER_ERROR = 2;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Request request = new Request.Builder()
                    .url(strings[0])
                    .build();
            try {
                client.newCall(request).execute();
                return SUCCESS;
            } catch (IOException e) {
                Timber.i(e, "IO Exception");
                return NETWORK_ERROR;
            } catch (Exception e) {
                Timber.e(e, "Exception");
                return OTHER_ERROR;
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            progressBar.setVisibility(ProgressBar.GONE);

            if (result == NETWORK_ERROR) {
                Toast.makeText(getContext()
                        , "Task was unsuccessful!\n Please check your internet conneciton.",
                        Toast.LENGTH_LONG).show();
            } else if (result == OTHER_ERROR) {
                Toast.makeText(getContext()
                        , "Fatal error!\n Task aborted...", Toast.LENGTH_LONG).show();
            } else {
                if (!unreadTask.isRunning()) {
                    numberOfPages = 0;
                    loadedPages = 0;
                    unreadTask = new UnreadTask(UnreadFragment.this::onUnreadTaskStarted, UnreadFragment.this::onUnreadTaskFinished);
                    assert SessionManager.unreadUrl != null;
                    unreadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, SessionManager.unreadUrl.toString());
                }
            }
        }

        //TODO: Maybe extend this task and use isRunning() from ExternalAsyncTask instead (?)
        public boolean isRunning(){
            return getStatus() == AsyncTask.Status.RUNNING;
        }
    }
}
