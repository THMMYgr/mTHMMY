package gr.thmmy.mthmmy.activities.profile.latestPosts;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.model.PostSummary;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

/**
 * Use the {@link LatestPostsFragment#newInstance} factory method to create an instance of this fragment.
 */
public class LatestPostsFragment extends BaseFragment implements LatestPostsAdapter.OnLoadMoreListener {
    /**
     * The key to use when putting profile's url String to {@link LatestPostsFragment}'s Bundle.
     */
    private static final String PROFILE_URL = "PROFILE_URL";
    /**
     * {@link ArrayList} of {@link PostSummary} objects used to hold profile's latest posts. Data
     * are added in {@link LatestPostsTask}.
     */
    private ArrayList<PostSummary> parsedTopicSummaries;
    private LatestPostsAdapter latestPostsAdapter;
    private int numberOfPages = -1;
    private int pagesLoaded = 0;
    private String profileUrl;
    private LatestPostsTask profileLatestPostsTask;
    private MaterialProgressBar progressBar;
    private boolean isLoadingMore;
    private boolean userHasPosts = true;
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
        final View rootView = inflater.inflate(R.layout.fragment_profile_latest_posts, container, false);
        latestPostsAdapter = new LatestPostsAdapter(this.getContext(), fragmentInteractionListener, parsedTopicSummaries);
        RecyclerView mainContent = rootView.findViewById(R.id.profile_latest_posts_recycler);
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

                if (userHasPosts && !isLoadingMore &&
                        totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    isLoadingMore = true;
                    onLoadMore();
                }
            }
        });
        progressBar = rootView.findViewById(R.id.progressBar);
        return rootView;
    }

    @Override
    public void onLoadMore() {
        if (pagesLoaded < numberOfPages) {
            parsedTopicSummaries.add(null);
            latestPostsAdapter.notifyItemInserted(parsedTopicSummaries.size() - 1);

            //Load data
            profileLatestPostsTask = new LatestPostsTask();
            profileLatestPostsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, profileUrl + ";sa=showPosts;start=" + pagesLoaded * 15);
            ++pagesLoaded;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (parsedTopicSummaries.isEmpty() && userHasPosts) {
            profileLatestPostsTask = new LatestPostsTask();
            profileLatestPostsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, profileUrl + ";sa=showPosts");
            pagesLoaded = 1;
        }
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
    private class LatestPostsTask extends AsyncTask<String, Void, Boolean> {
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
                Timber.w("Certificate problem (please switch to unsafe connection).");
            } catch (Exception e) {
                Timber.e(e, "Exception");
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (Boolean.FALSE.equals(result))
                Timber.e(new ParseException("Parsing failed(latest posts)"),"ParseException");

            progressBar.setVisibility(ProgressBar.INVISIBLE);
            latestPostsAdapter.notifyDataSetChanged();
            isLoadingMore = false;
        }

        //TODO: better parse error handling (ParseException etc.)
        private boolean parseLatestPosts(Document latestPostsPage) {
            //td:contains( Sorry, no matches were found)
            Elements latestPostsRows = latestPostsPage.
                    select("td:has(table:Contains(Show Posts)):not([style]) > table");
            if (latestPostsRows.isEmpty())
                latestPostsRows = latestPostsPage.
                        select("td:has(table:Contains(Εμφάνιση μηνυμάτων)):not([style]) > table");

            //Removes loading item
            if (isLoadingMore)
                parsedTopicSummaries.remove(parsedTopicSummaries.size() - 1);

            if (!latestPostsRows.select("td:contains(Sorry, no matches were found)").isEmpty() ||
                    !latestPostsRows.select("td:contains(Δυστυχώς δεν βρέθηκε τίποτα)").isEmpty()){
                userHasPosts = false;
                parsedTopicSummaries.add(null);
                return true;
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
                    if (rowHeader.size() != 2)
                        return false;
                    else {
                        pTopicTitle = rowHeader.first().text().replaceAll("\\u00a0","").trim();
                        pTopicUrl = rowHeader.first().select("a").last().attr("href");
                        pDateTime = rowHeader.last().text();
                    }
                    pPost = ParseHelpers.youtubeEmbeddedFix(row.select("div.post").first());

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
