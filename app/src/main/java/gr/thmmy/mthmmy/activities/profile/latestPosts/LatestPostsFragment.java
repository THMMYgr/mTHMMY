package gr.thmmy.mthmmy.activities.profile.latestPosts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.base.BaseActivity;
import gr.thmmy.mthmmy.data.TopicSummary;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Use the {@link LatestPostsFragment#newInstance} factory method to create an instance of this fragment.
 */
public class LatestPostsFragment extends Fragment {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "LatestPostsFragment";
    /**
     * The key to use when putting profile's url String to {@link LatestPostsFragment}'s Bundle.
     */
    static final String PROFILE_URL = "PROFILE_DOCUMENT";
    /**
     * {@link ArrayList} of {@link TopicSummary} objects used to hold profile's latest posts. Data
     * are added in {@link LatestPostsFragment.ProfileLatestPostsTask}.
     */
    static ArrayList<TopicSummary> parsedTopicSummaries;
    private RecyclerView mainContent;
    private LatestPostsAdapter latestPostsAdapter = new LatestPostsAdapter();
    private int numberOfPages = -1;
    private String profileUrl;
    private ProfileLatestPostsTask profileLatestPostsTask;
    private MaterialProgressBar progressBar;
    private boolean isLoading;
    static int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    public LatestPostsFragment() {
        // Required empty public constructor
    }

    /**
     * Use ONLY this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @param profileUrl String containing this profile's url
     * @return A new instance of fragment Summary.
     */
    public static LatestPostsFragment newInstance(String profileUrl) {
        LatestPostsFragment fragment = new LatestPostsFragment();
        Bundle args = new Bundle();
        args.putString(PROFILE_URL, profileUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileUrl = getArguments().getString(PROFILE_URL);
        parsedTopicSummaries = new ArrayList<>();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (parsedTopicSummaries.isEmpty()) {
            profileLatestPostsTask = new ProfileLatestPostsTask();
            profileLatestPostsTask.execute(profileUrl);
        }
        Report.d(TAG, "onActivityCreated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (profileLatestPostsTask != null && profileLatestPostsTask.getStatus() != AsyncTask.Status.RUNNING)
            profileLatestPostsTask.cancel(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.profile_fragment_latest_posts, container, false);

        mainContent = (RecyclerView) rootView.findViewById(R.id.profile_latest_posts_recycler);
        mainContent.setAdapter(latestPostsAdapter);

        final LatestPostsAdapter.OnLoadMoreListener onLoadMoreListener = new LatestPostsAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                parsedTopicSummaries.add(null);
                latestPostsAdapter.notifyItemInserted(parsedTopicSummaries.size() - 1);

                //Removes loading item
                parsedTopicSummaries.remove(parsedTopicSummaries.size() - 1);
                latestPostsAdapter.notifyItemRemoved(parsedTopicSummaries.size());

                //Load data
                profileLatestPostsTask = new ProfileLatestPostsTask();
                profileLatestPostsTask.execute(profileUrl);
            }
        };

        latestPostsAdapter.setOnLoadMoreListener(onLoadMoreListener);
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) mainContent.getLayoutManager();
        mainContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    onLoadMoreListener.onLoadMore();
                    isLoading = true;
                }
            }
        });
        progressBar = (MaterialProgressBar) rootView.findViewById(R.id.progressBar);
        return rootView;
    }

    /**
     * An {@link AsyncTask} that handles asynchronous fetching of a profile page and parsing this
     * user's personal text.
     * <p>ProfileTask's {@link AsyncTask#execute execute} method needs a profile's url as String
     * parameter!</p>
     */
    public class ProfileLatestPostsTask extends AsyncTask<String, Void, Boolean> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        @SuppressWarnings("unused")
        private static final String TAG = "ProfileLatestPostsTask"; //Separate tag for AsyncTask

        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        protected Boolean doInBackground(String... profileUrl) {
            String pageUrl = profileUrl[0] + ";sa=showPosts"; //Profile's page wap url

            Request request = new Request.Builder()
                    .url(pageUrl)
                    .build();
            try {
                Response response = BaseActivity.getClient().newCall(request).execute();
                return parseLatestPosts(Jsoup.parse(response.body().string()));
            } catch (SSLHandshakeException e) {
                Report.w(TAG, "Certificate problem (please switch to unsafe connection).");
            } catch (Exception e) {
                Report.e("TAG", "ERROR", e);
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (!result) { //Parse failed!
                Toast.makeText(getContext()
                        , "Fatal error!\n Aborting...", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
            //Parse was successful
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            latestPostsAdapter.notifyDataSetChanged();
            isLoading = false;
        }

        private boolean parseLatestPosts(Document latestPostsPage) {
            Elements latestPostsRows = latestPostsPage.
                    select("td:has(table:Contains(Show Posts)):not([style]) > table");
            if (latestPostsRows == null)
                latestPostsRows = latestPostsPage.
                        select("td:has(table:Contains(Show Posts)):not([style]) > table");

            for (Element row : latestPostsRows) {
                String pTopicUrl, pTopicTitle, pDateTime, pPost;

                if (row.text().contains("Show Posts Pages: ") || row.text().contains("")) {
                    if (numberOfPages == -1) {
                        Elements pages = row.select("tr.catbg3 a");
                        for (Element page : pages) {
                            if (Integer.parseInt(page.text()) >= numberOfPages)
                                numberOfPages = Integer.parseInt(page.text());
                        }
                    }
                } else {
                    Elements rowHeader = row.select("td.middletext");
                    if (rowHeader.size() != 2) {
                        return false;
                    } else {
                        pTopicTitle = rowHeader.text();
                        pTopicUrl = rowHeader.first().select("a").last().attr("href");
                        pDateTime = rowHeader.last().text();
                    }
                    pPost = rowHeader.select("div.post").first().html();

                    parsedTopicSummaries.add(new TopicSummary(pTopicUrl, pTopicTitle, "", pDateTime, pPost));
                }
            }
            return true;
        }
    }
}
