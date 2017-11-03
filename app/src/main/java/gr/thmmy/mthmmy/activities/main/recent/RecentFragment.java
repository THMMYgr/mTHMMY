package gr.thmmy.mthmmy.activities.main.recent;

import android.graphics.Color;
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

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * {@link RecentFragment.RecentFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RecentFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RecentFragment extends BaseFragment {
    private static final String TAG = "RecentFragment";
    // Fragment initialization parameters, e.g. ARG_SECTION_NUMBER

    private MaterialProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecentAdapter recentAdapter;

    private List<TopicSummary> topicSummaries;

    private RecentTask recentTask;

    // Required empty public constructor
    public RecentFragment() {
    }

    /**
     * Use ONLY this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment Recent.
     */
    public static RecentFragment newInstance(int sectionNumber) {
        RecentFragment fragment = new RecentFragment();
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
            recentTask = new RecentTask();
            recentTask.execute(SessionManager.indexUrl.toString());

        }
        Timber.d("onActivityCreated");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_recent, container, false);

        // Set the adapter
        if (rootView instanceof RelativeLayout) {
            progressBar = rootView.findViewById(R.id.progressBar);
            recentAdapter = new RecentAdapter(getActivity(), topicSummaries, fragmentInteractionListener);

            CustomRecyclerView recyclerView = rootView.findViewById(R.id.list);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(recyclerView.getContext());
            recyclerView.setLayoutManager(linearLayoutManager);
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    linearLayoutManager.getOrientation());
            recyclerView.addItemDecoration(dividerItemDecoration);
            recyclerView.setAdapter(recentAdapter);

            swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
            swipeRefreshLayout.setColorSchemeResources(R.color.accent);
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            if (recentTask != null && recentTask.getStatus() != AsyncTask.Status.RUNNING) {
                                recentTask = new RecentTask();
                                recentTask.execute(SessionManager.indexUrl.toString());
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
        if (recentTask != null && recentTask.getStatus() != AsyncTask.Status.RUNNING)
            recentTask.cancel(true);
    }


    public interface RecentFragmentInteractionListener extends FragmentInteractionListener {
        void onRecentFragmentInteraction(TopicSummary topicSummary);
    }

    //---------------------------------------ASYNC TASK-----------------------------------
    private class RecentTask extends ParseTask {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        public void parse(Document document) throws ParseException {
            Elements recent = document.select("#block8 :first-child div");
            if (!recent.isEmpty()) {
                topicSummaries.clear();
                for (int i = 0; i < recent.size(); i += 3) {
                    String link = recent.get(i).child(0).attr("href");
                    String title = recent.get(i).child(0).attr("title");

                    String lastUser = recent.get(i + 1).text();
                    Pattern pattern = Pattern.compile("\\b (.*)");
                    Matcher matcher = pattern.matcher(lastUser);
                    if (matcher.find())
                        lastUser = matcher.group(1);
                    else
                        throw new ParseException("Parsing failed (lastUser)");

                    String dateTime = recent.get(i + 2).text();
                    pattern = Pattern.compile("\\[(.*)\\]");
                    matcher = pattern.matcher(dateTime);
                    if (matcher.find())
                        dateTime = matcher.group(1);
                    else
                        throw new ParseException("Parsing failed (dateTime)");

                    topicSummaries.add(new TopicSummary(link, title, lastUser, dateTime));
                }
                return;
            }
            throw new ParseException("Parsing failed");
        }

        @Override
        protected void postParsing(ParseTask.ResultCode result) {
            if (result == ResultCode.SUCCESS)
                recentAdapter.notifyDataSetChanged();

            progressBar.setVisibility(ProgressBar.INVISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
