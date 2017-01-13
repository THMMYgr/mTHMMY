package gr.thmmy.mthmmy.activities.profile.latestPosts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
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
import gr.thmmy.mthmmy.activities.base.BaseFragment;
import gr.thmmy.mthmmy.data.PostSummary;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Use the {@link LatestPostsFragment#newInstance} factory method to create an instance of this fragment.
 */
public class LatestPostsFragment extends BaseFragment implements LatestPostsAdapter.OnLoadMoreListener{
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "LatestPostsFragment";
    /**
     * The key to use when putting profile's url String to {@link LatestPostsFragment}'s Bundle.
     */
    private static final String PROFILE_URL = "PROFILE_URL";
    /**
     * {@link ArrayList} of {@link PostSummary} objects used to hold profile's latest posts. Data
     * are added in {@link LatestPostsTask}.
     */
    private ArrayList<PostSummary> parsedTopicSummaries;
    private RecyclerView mainContent;
    private LatestPostsAdapter latestPostsAdapter;
    private int numberOfPages = -1;
    private int pagesLoaded = 0;
    private String profileUrl;
    private LatestPostsTask profileLatestPostsTask;
    private MaterialProgressBar progressBar;
    private boolean isLoadingMore;
    private static final int visibleThreshold = 5;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_latest_posts, container, false);
        latestPostsAdapter = new LatestPostsAdapter(fragmentInteractionListener, parsedTopicSummaries);
        mainContent = (RecyclerView) rootView.findViewById(R.id.profile_latest_posts_recycler);
        mainContent.setAdapter(latestPostsAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mainContent.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mainContent.getContext(),
                layoutManager.getOrientation());
        mainContent.addItemDecoration(dividerItemDecoration);

        //latestPostsAdapter.setOnLoadMoreListener();
        mainContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = layoutManager.getItemCount();
                lastVisibleItem = layoutManager.findLastVisibleItemPosition();

                if (!isLoadingMore && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    isLoadingMore = true;
                    onLoadMore();
                }
            }
        });
        progressBar = (MaterialProgressBar) rootView.findViewById(R.id.progressBar);
        return rootView;
    }

    @Override
    public void onLoadMore() {
        if (pagesLoaded < numberOfPages) {
            parsedTopicSummaries.add(null);
            latestPostsAdapter.notifyItemInserted(parsedTopicSummaries.size() - 1);

            //Load data
            profileLatestPostsTask = new LatestPostsTask();
            profileLatestPostsTask.execute(profileUrl + ";sa=showPosts;start=" + pagesLoaded * 15);
            ++pagesLoaded;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (parsedTopicSummaries.isEmpty()) {
            profileLatestPostsTask = new LatestPostsTask();
            profileLatestPostsTask.execute(profileUrl + ";sa=showPosts");
            pagesLoaded = 1;
        }
        Report.d(TAG, "onActivityCreated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (profileLatestPostsTask != null && profileLatestPostsTask.getStatus() != AsyncTask.Status.RUNNING)
            profileLatestPostsTask.cancel(true);
    }

    public interface LatestPostsFragmentInteractionListener extends FragmentInteractionListener {
        void onLatestPostsFragmentInteraction(PostSummary postSummary);
    }

    /**
     * An {@link AsyncTask} that handles asynchronous fetching of a profile page and parsing this
     * user's latest posts.
     * <p>LatestPostsTask's {@link AsyncTask#execute execute} method needs a profile's url as String
     * parameter!</p>
     */
    public class LatestPostsTask extends AsyncTask<String, Void, Boolean> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        @SuppressWarnings("unused")
        private static final String TAG = "LatestPostsTask"; //Separate tag for AsyncTask

        protected void onPreExecute() {
            if (!isLoadingMore) progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        protected Boolean doInBackground(String... profileUrl) {
            Request request = new Request.Builder()
                    .url(profileUrl[0])
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
                Report.d(TAG, "Parse failed!");
                Toast.makeText(getContext()
                        , "Fatal error!\n Aborting...", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
            //Parse was successful
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            latestPostsAdapter = new LatestPostsAdapter(fragmentInteractionListener, parsedTopicSummaries);
            mainContent.swapAdapter(latestPostsAdapter, false);
            //latestPostsAdapter.notifyDataSetChanged();
            isLoadingMore = false;
        }

        private boolean parseLatestPosts(Document latestPostsPage) {
            Elements latestPostsRows = latestPostsPage.
                    select("td:has(table:Contains(Show Posts)):not([style]) > table");
            if (latestPostsRows.isEmpty()) {
                latestPostsRows = latestPostsPage.
                        select("td:has(table:Contains(Εμφάνιση μηνυμάτων)):not([style]) > table");
            }
            //Removes loading item
            if (isLoadingMore) {
                parsedTopicSummaries.remove(parsedTopicSummaries.size() - 1);
            }

            for (Element row : latestPostsRows) {
                String pTopicUrl, pTopicTitle, pDateTime, pPost;
                if (Integer.parseInt(row.attr("cellpadding")) == 4) {
                    if (numberOfPages == -1) {
                        Elements pages = row.select("tr.catbg3 a");
                        for (Element page : pages) {
                            if (Integer.parseInt(page.text()) > numberOfPages)
                                numberOfPages = Integer.parseInt(page.text());
                        }
                    }
                } else {
                    Elements rowHeader = row.select("td.middletext");
                    if (rowHeader.size() != 2) {
                        return false;
                    } else {
                        pTopicTitle = rowHeader.first().text().trim();
                        pTopicUrl = rowHeader.first().select("a").last().attr("href");
                        pDateTime = rowHeader.last().text();
                    }
                    pPost = row.select("div.post").first().outerHtml();

                    { //Fixes embedded videos
                        Elements noembedTag = row.select("div").select(".post").first().select("noembed");
                        ArrayList<String> embededVideosUrls = new ArrayList<>();

                        for (Element _noembed : noembedTag) {
                            embededVideosUrls.add(_noembed.text().substring(_noembed.text()
                                            .indexOf("href=\"https://www.youtube.com/watch?") + 38
                                    , _noembed.text().indexOf("target") - 2));
                        }

                        int tmp_counter = 0;
                        while (pPost.contains("<embed")) {
                            if (tmp_counter > embededVideosUrls.size())
                                break;
                            pPost = pPost.replace(
                                    pPost.substring(pPost.indexOf("<embed"), pPost.indexOf("/noembed>") + 9)
                                    , "<div class=\"embedded-video\">"
                                            + "<a href=\"https://www.youtube.com/"
                                            + embededVideosUrls.get(tmp_counter) + "\" target=\"_blank\">"
                                            + "<img src=\"https://img.youtube.com/vi/"
                                            + embededVideosUrls.get(tmp_counter) + "/default.jpg\" alt=\"\" border=\"0\">"
                                            + "</a>"
                                            + "</div>");
                        }
                    }
                    //Add stuff to make it work in WebView
                    //style.css
                    pPost = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />" + pPost);

                    parsedTopicSummaries.add(new PostSummary(pTopicUrl, pTopicTitle, pDateTime, pPost));
                }
            }
            return true;
        }
    }
}
