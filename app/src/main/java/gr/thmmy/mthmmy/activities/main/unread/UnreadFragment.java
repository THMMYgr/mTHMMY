package gr.thmmy.mthmmy.activities.main.unread;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.TopicSummary;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.ParseTask;
import gr.thmmy.mthmmy.utils.exceptions.ParseException;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
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
    // Fragment initialization parameters, e.g. ARG_SECTION_NUMBER

    private MaterialProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private UnreadAdapter unreadAdapter;

    private List<TopicSummary> topicSummaries;

    private UnreadTask unreadTask;

    // Required empty public constructor
    public UnreadFragment() {
    }

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
            unreadTask = new UnreadTask();
            unreadTask.execute(SessionManager.unreadUrl.toString());

        }
        Timber.d("onActivityCreated");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_unread, container, false);

        // Set the adapter
        if (rootView instanceof RelativeLayout) {
            progressBar = (MaterialProgressBar) rootView.findViewById(R.id.progressBar);
            unreadAdapter = new UnreadAdapter(getActivity(), topicSummaries, fragmentInteractionListener);

            CustomRecyclerView recyclerView = (CustomRecyclerView) rootView.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.findViewById(R.id.list).getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(unreadAdapter);

            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            if (unreadTask != null && unreadTask.getStatus() != AsyncTask.Status.RUNNING) {
                                unreadTask = new UnreadTask();
                                unreadTask.execute(SessionManager.unreadUrl.toString());
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
        if (unreadTask != null && unreadTask.getStatus() != AsyncTask.Status.RUNNING)
            unreadTask.cancel(true);
    }


    public interface UnreadFragmentInteractionListener extends FragmentInteractionListener {
        void onUnreadFragmentInteraction(TopicSummary topicSummary);
    }

    //---------------------------------------ASYNC TASK-----------------------------------
    private class UnreadTask extends ParseTask {
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public void parse(Document document) throws ParseException {
            Elements unread = document.select("table.bordercolor[cellspacing=1] tr:not(.titlebg)");
            if (!unread.isEmpty()) {
                topicSummaries.clear();
                Log.d("UnreadFragment", unread.html());
                for (Element row : unread) {
                    Elements information = row.select("td");
                    String link = information.get(2).select("a").first().attr("href");
                    String title = information.get(2).select("a").first().text();

                    Element lastUserAndDate = information.get(6);
                    String lastUser = lastUserAndDate.select("a").text();
                    String dateTime = lastUserAndDate.select("span").html();
                    dateTime = dateTime.substring(3, dateTime.indexOf("<br>"));
                    dateTime = dateTime.replace("</b>", "");

                    topicSummaries.add(new TopicSummary(link, title, lastUser, dateTime));
                }
            } else {
                String message = document.select("table.bordercolor[cellspacing=1]").first().text();
                if (message.contains("No messages")){ //It's english
                    message = "No unread posts!";
                }else{ //It's greek
                    message = "Δεν υπάρχουν μη διαβασμένα μυνήματα!";
                }
                topicSummaries.add(new TopicSummary(null, null, null, message));
            }
        }


        @Override
        protected void postParsing(ParseTask.ResultCode result) {
            if (result == ResultCode.SUCCESS)
                unreadAdapter.notifyDataSetChanged();

            progressBar.setVisibility(ProgressBar.INVISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }

    }
}
