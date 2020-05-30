package gr.thmmy.mthmmy.activities.downloads;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.upload.UploadActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.Download;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseTask;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

import static gr.thmmy.mthmmy.activities.upload.UploadActivity.BUNDLE_UPLOAD_CATEGORY;

public class DownloadsActivity extends BaseActivity implements DownloadsAdapter.OnLoadMoreListener {
    /**
     * The key to use when putting download's url String to {@link DownloadsActivity}'s Bundle.
     */
    public static final String BUNDLE_DOWNLOADS_URL = "DOWNLOADS_URL";
    /**
     * The key to use when putting download's title String to {@link DownloadsActivity}'s Bundle.
     */
    public static final String BUNDLE_DOWNLOADS_TITLE = "DOWNLOADS_TITLE";
    private static final String downloadsIndexUrl = "https://www.thmmy.gr/smf/index.php?action=tpmod;dl;";
    private String downloadsUrl;
    private String downloadsNav;
    private String downloadsTitle;
    private final ArrayList<Download> parsedDownloads = new ArrayList<>();

    private MaterialProgressBar progressBar;
    private RecyclerView recyclerView;
    private DownloadsAdapter downloadsAdapter;
    private FloatingActionButton uploadFAB;

    private ParseDownloadPageTask parseDownloadPageTask;
    private int numberOfPages = -1;
    private int pagesLoaded = 0;
    private boolean isLoadingMore;
    private static final int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        Bundle extras = getIntent().getExtras();
        downloadsTitle = extras.getString(BUNDLE_DOWNLOADS_TITLE);
        downloadsUrl = extras.getString(BUNDLE_DOWNLOADS_URL);
        if (downloadsUrl != null && !Objects.equals(downloadsUrl, "")) {
            ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(Uri.parse(downloadsUrl));
            if (!target.is(ThmmyPage.PageCategory.DOWNLOADS)) {
                Timber.e("Bundle came with a non downloads url!\nUrl:\n%s", downloadsUrl);
                Toast.makeText(this, "An error has occurred\nAborting.", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else downloadsUrl = downloadsIndexUrl;

        //Initialize toolbar
        toolbar = findViewById(R.id.toolbar);
        if (downloadsTitle == null || downloadsTitle.equals(""))
            toolbar.setTitle("Downloads");
        else
            toolbar.setTitle(downloadsTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();
        drawer.setSelection(DOWNLOADS_ID);

        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.downloads_recycler_view);
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());
        recyclerView.addItemDecoration(dividerItemDecoration);
        downloadsAdapter = new DownloadsAdapter(this, parsedDownloads);
        recyclerView.setAdapter(downloadsAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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

        uploadFAB = findViewById(R.id.upload_fab);
        uploadFAB.setEnabled(false);
        uploadFAB.hide();

        parseDownloadPageTask = new ParseDownloadPageTask();
        parseDownloadPageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadsUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.downloads_menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_upload:
                Intent intent = new Intent(DownloadsActivity.this, UploadActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_UPLOAD_CATEGORY, downloadsNav);
                intent.putExtras(extras);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onLoadMore() {
        if (pagesLoaded < numberOfPages) {
            parsedDownloads.add(null);
            downloadsAdapter.notifyItemInserted(parsedDownloads.size());    //This gets a warning - change it!

            //Load data
            parseDownloadPageTask = new ParseDownloadPageTask();
            if (downloadsUrl.contains("tpstart"))
                parseDownloadPageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadsUrl.substring(0
                        , downloadsUrl.lastIndexOf(";tpstart=")) + ";tpstart=" + pagesLoaded * 10);
            else parseDownloadPageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, downloadsUrl + ";tpstart=" + pagesLoaded * 10);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        drawer.setSelection(DOWNLOADS_ID);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        if (parseDownloadPageTask != null && parseDownloadPageTask.getStatus() != AsyncTask.Status.RUNNING)
            parseDownloadPageTask.cancel(true);
    }

    /**
     * An {@link ParseTask} that handles asynchronous fetching of a downloads page and parsing it's
     * data. {@link ParseTask#postExecution(ResultCode) postExecution} method calls {@link RecyclerView#swapAdapter}
     * to build graphics.
     * <p>
     * <p>Calling TopicTask's {@link ParseTask#execute execute} method needs to have download's page url
     * as String parameter!</p>
     */
    private class ParseDownloadPageTask extends ParseTask {
        private Download.DownloadItemType type;
        private Download download;

        @Override
        protected void onPreExecute() {
            if (!isLoadingMore) progressBar.setVisibility(ProgressBar.VISIBLE);
            if (uploadFAB.getVisibility() != View.GONE) uploadFAB.setEnabled(false);
        }

        @Override
        protected void parse(Document downloadPage) throws ParseException {
            try {
                Element downloadsNavElement = downloadPage.select("div.nav").first();
                downloadsNav = downloadsNavElement.text();

                if (downloadsTitle == null || Objects.equals(downloadsTitle, ""))
                    downloadsTitle = downloadsNavElement.select("b>a.nav").last().text();

                //Removes loading item
                if (isLoadingMore) {
                    if (parsedDownloads.size() > 0)
                        parsedDownloads.remove(parsedDownloads.size() - 1);
                }

                if (ThmmyPage.resolvePageCategory(Uri.parse(url)).is(ThmmyPage.PageCategory.DOWNLOADS_CATEGORY))
                    type = Download.DownloadItemType.DOWNLOADS_CATEGORY;
                else
                    type = Download.DownloadItemType.DOWNLOADS_FILE;

                Elements pages = downloadPage.select("a.navPages");
                if (pages != null) {
                    for (Element page : pages) {
                        int pageNumber = Integer.parseInt(page.text());
                        if (pageNumber > numberOfPages) numberOfPages = pageNumber;
                    }
                } else numberOfPages = 1;

                Elements rows = downloadPage.select("table.tborder>tbody>tr");
                if (type == Download.DownloadItemType.DOWNLOADS_CATEGORY) {
                    Elements navigationLinks = downloadPage.select("div.nav>b");
                    for (Element row : rows) {
                        if (row.select("td").size() == 1) continue;

                        String url = row.select("b>a").first().attr("href"),
                                title = row.select("b>a").first().text(),
                                subtitle = row.select("div.smalltext:not(:has(a))").text();
                        if (!row.select("td").last().hasClass("windowbg2")) {
                            if (navigationLinks.size() < 4) {

                                parsedDownloads.add(new Download(type, url, title, subtitle, null,
                                        true, null));
                            } else {
                                String stats = row.text();
                                stats = stats.replace(title, "").replace(subtitle, "").trim();
                                parsedDownloads.add(new Download(type, url, title, subtitle, stats,
                                        false, null));
                            }
                        } else {
                            String stats = row.text();
                            stats = stats.replace(title, "").replace(subtitle, "").trim();
                            parsedDownloads.add(new Download(type, url, title, subtitle, stats,
                                    false, null));
                        }
                    }
                } else {
                    download = new Download(type,
                            rows.select("b>a").first().attr("href"),
                            rows.select("b>a").first().text(),
                            rows.select("div.smalltext:not(:has(a))").text(),
                            rows.select("span:not(:has(a))").first().text(),
                            false,
                            rows.select("span:has(a)").first().text());
                    parsedDownloads.add(download);
                }
            } catch (Exception e) {
                throw new ParseException("Parsing failed (DownloadsActivity)");
            }
        }

        @Override
        protected void postParsing() {
            if (type == Download.DownloadItemType.DOWNLOADS_FILE) {
                OkHttpClient client = BaseApplication.getInstance().getClient();
                String fileName = null;
                try {
                    Response response = client.newCall(new Request.Builder().url(download.getUrl()).head().build()).execute();
                    String contentDisposition = response.headers("Content-Disposition").toString();   //check if link provides an attachment
                    if (contentDisposition.contains("attachment"))
                        fileName = contentDisposition.split("\"")[1];
                    download.setFileName(fileName);
                } catch (Exception e) {
                    Timber.e(e, "Couldn't extract fileName.");
                }
            }
        }

        @Override
        protected void postExecution(ResultCode result) {
            if (downloadsTitle != null && !downloadsTitle.equals("")
                    && !downloadsTitle.equals("Αρχεία για λήψη")
                    && toolbar.getTitle() != downloadsTitle)
                toolbar.setTitle(downloadsTitle);

            ++pagesLoaded;
            if (uploadFAB.getVisibility() != View.GONE) uploadFAB.setEnabled(true);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            downloadsAdapter.notifyDataSetChanged();
            isLoadingMore = false;
        }
    }
}
