package gr.thmmy.mthmmy.activities.main.recent;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.base.BaseFragment;
import gr.thmmy.mthmmy.data.TopicSummary;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import mthmmy.utils.Report;
import okhttp3.HttpUrl;
import okhttp3.Request;
import okhttp3.Response;

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

    private ProgressBar progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecentAdapter recentAdapter;

    private List<TopicSummary> topicSummaries;

    private RecentTask recentTask;

    // Required empty public constructor
    public RecentFragment() {}

    /**
     * Use ONLY this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param sectionNumber
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
        if (sectionNumber == 1)//temp
        {
            if (topicSummaries.isEmpty())
            {
                recentTask =new RecentTask();
                recentTask.execute();

            }
        }
        Report.d(TAG, "onActivityCreated");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View rootView = inflater.inflate(R.layout.fragment_recent, container, false);

        // Set the adapter
        if (rootView instanceof RelativeLayout) {
            progressBar = (ProgressBar) rootView.findViewById(R.id.progressBar);
            recentAdapter = new RecentAdapter(topicSummaries, fragmentInteractionListener);

            CustomRecyclerView recyclerView = (CustomRecyclerView) rootView.findViewById(R.id.list);
            recyclerView.setLayoutManager(new LinearLayoutManager(rootView.findViewById(R.id.list).getContext()));
            recyclerView.setAdapter(recentAdapter);

            swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.swiperefresh);
            swipeRefreshLayout.setOnRefreshListener(
                    new SwipeRefreshLayout.OnRefreshListener() {
                        @Override
                        public void onRefresh() {
                            if (recentTask != null && recentTask.getStatus() != AsyncTask.Status.RUNNING) {
                                recentTask = new RecentTask();
                                recentTask.execute();
                            }
                        }

                    }
            );
        }

        return rootView;
    }

    @Override
    protected void cancelTask() {
        if(recentTask!=null&&recentTask.getStatus()!= AsyncTask.Status.RUNNING)
            recentTask.cancel(true);
    }

    public interface RecentFragmentInteractionListener extends FragmentInteractionListener {
        void onFragmentInteraction(TopicSummary topicSummary);
    }

    //---------------------------------------ASYNC TASK-----------------------------------

    public class RecentTask extends AsyncTask<Void, Void, Integer> {
        private static final String TAG = "ForumTask";
        private final HttpUrl thmmyUrl = SessionManager.indexUrl;

        private Document document;


        protected void onPreExecute() {

            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        protected Integer doInBackground(Void... voids) {
            Request request = new Request.Builder()
                    .url(thmmyUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());
                parse(document);
                return 0;
            } catch (IOException e) {
                Report.d(TAG, "Network Error", e);
                return 1;
            } catch (Exception e) {
                Report.d(TAG, "ERROR", e);
                return 2;
            }

        }


        protected void onPostExecute(Integer result) {

            if (result == 0)
                recentAdapter.notifyDataSetChanged();
            else if (result == 1)
                Toast.makeText(getActivity(), "Network error", Toast.LENGTH_SHORT).show();

            progressBar.setVisibility(ProgressBar.INVISIBLE);
            swipeRefreshLayout.setRefreshing(false);
        }

        private void parse(Document document) {
            Elements recent = document.select("#block8 :first-child div");
            if (recent.size() == 30) {
                topicSummaries.clear();

                for (int i = 0; i < recent.size(); i += 3) {
                    String link = recent.get(i).child(0).attr("href");
                    String title = recent.get(i).child(0).attr("title");

                    String lastUser = recent.get(i + 1).text();
                    Pattern pattern = Pattern.compile("\\b (.*)");
                    Matcher matcher = pattern.matcher(lastUser);
                    if (matcher.find())
                        lastUser = matcher.group(1);
                    else {
                        Report.e(TAG, "Parsing failed (lastUser)!");
                        return;
                    }

                    String dateTime = recent.get(i + 2).text();
                    pattern = Pattern.compile("\\[(.*)\\]");
                    matcher = pattern.matcher(dateTime);
                    if (matcher.find())
                        dateTime = matcher.group(1);
                    else {
                        Report.e(TAG, "Parsing failed (dateTime)!");
                        return;
                    }


                    topicSummaries.add(new TopicSummary(link, title, lastUser, dateTime));
                }

                return;
            }
            Report.e(TAG, "Parsing failed!");
        }
    }

}
