package gr.thmmy.mthmmy.activities.board;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Board;
import gr.thmmy.mthmmy.model.Bookmark;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.model.Topic;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

public class BoardActivity extends BaseActivity implements BoardAdapter.OnLoadMoreListener {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "BoardActivity";
    /**
     * The key to use when putting board's url String to {@link BoardActivity}'s Bundle.
     */
    public static final String BUNDLE_BOARD_URL = "BOARD_URL";
    /**
     * The key to use when putting board's title String to {@link BoardActivity}'s Bundle.
     */
    public static final String BUNDLE_BOARD_TITLE = "BOARD_TITLE";

    private MaterialProgressBar progressBar;
    private FloatingActionButton newTopicFAB;
    private BoardTask boardTask;

    private BoardAdapter boardAdapter;
    private final ArrayList<Board> parsedSubBoards = new ArrayList<>();
    private final ArrayList<Topic> parsedTopics = new ArrayList<>();

    private String boardUrl;
    private String boardTitle;

    private int numberOfPages = -1;
    private int pagesLoaded = 0;
    private boolean isLoadingMore;
    private static final int visibleThreshold = 5;
    private int lastVisibleItem, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board);

        Bundle extras = getIntent().getExtras();
        boardTitle = extras.getString(BUNDLE_BOARD_TITLE);
        boardUrl = extras.getString(BUNDLE_BOARD_URL);
        ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(Uri.parse(boardUrl));
        if (!target.is(ThmmyPage.PageCategory.BOARD)) {
            Report.e(TAG, "Bundle came with a non board url!\nUrl:\n" + boardUrl);
            Toast.makeText(this, "An error has occurred\nAborting.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Initializes graphics
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (boardTitle != null && !Objects.equals(boardTitle, "")) toolbar.setTitle(boardTitle);
        else toolbar.setTitle("Board");
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        thisPageBookmark = new Bookmark(boardTitle, ThmmyPage.getBoardId(boardUrl));
        thisPageBookmarkButton = (ImageButton) findViewById(R.id.bookmark);
        setBoardBookmark();
        createDrawer();

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);
        newTopicFAB = (FloatingActionButton) findViewById(R.id.board_fab);
        newTopicFAB.setEnabled(false);
        newTopicFAB.hide();
        /*if (!sessionManager.isLoggedIn()) newTopicFAB.hide();
        else {
            newTopicFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sessionManager.isLoggedIn()) {
                        //TODO PM
                    } else {
                        new AlertDialog.Builder(BoardActivity.this)
                                .setMessage("You need to be logged in to create a new topic!")
                                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(BoardActivity.this, LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
                                    }
                                })
                                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                })
                                .show();
                    }
                }
            });
        }*/

        boardAdapter = new BoardAdapter(getApplicationContext(), parsedSubBoards, parsedTopics);
        RecyclerView mainContent = (RecyclerView) findViewById(R.id.board_recycler_view);
        mainContent.setAdapter(boardAdapter);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        mainContent.setLayoutManager(layoutManager);
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mainContent.getContext(),
                layoutManager.getOrientation());
        mainContent.addItemDecoration(dividerItemDecoration);

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

        boardTask = new BoardTask();
        boardTask.execute(boardUrl);
    }

    @Override
    public void onLoadMore() {
        if (pagesLoaded < numberOfPages) {
            parsedTopics.add(null);
            boardAdapter.notifyItemInserted(parsedSubBoards.size() + parsedTopics.size());

            //Load data
            boardTask = new BoardTask();
            boardTask.execute(boardUrl.substring(0, boardUrl.lastIndexOf(".")) + "." + pagesLoaded * 20);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (boardTask != null && boardTask.getStatus() != AsyncTask.Status.RUNNING)
            boardTask.cancel(true);
    }

    /**
     * An {@link AsyncTask} that handles asynchronous fetching of a board page and parsing it's content.
     * <p>BoardTask's {@link AsyncTask#execute execute} method needs a boards's url as String
     * parameter!</p>
     */
    public class BoardTask extends AsyncTask<String, Void, Void> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        @SuppressWarnings("unused")
        private static final String TAG = "BoardTask"; //Separate tag for AsyncTask

        @Override
        protected void onPreExecute() {
            if (!isLoadingMore) progressBar.setVisibility(ProgressBar.VISIBLE);
            if (newTopicFAB.getVisibility() != View.GONE) newTopicFAB.setEnabled(false);
        }

        @Override
        protected Void doInBackground(String... boardUrl) {
            Request request = new Request.Builder()
                    .url(boardUrl[0])
                    .build();
            try {
                Response response = BaseActivity.getClient().newCall(request).execute();
                parseBoard(Jsoup.parse(response.body().string()));
            } catch (SSLHandshakeException e) {
                Report.w(TAG, "Certificate problem (please switch to unsafe connection).");
            } catch (Exception e) {
                Report.e("TAG", "ERROR", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void voids) {
            if (boardTitle == null || Objects.equals(boardTitle, "")) toolbar.setTitle(boardTitle);

            //Parse was successful
            ++pagesLoaded;
            if (newTopicFAB.getVisibility() != View.GONE) newTopicFAB.setEnabled(true);
            progressBar.setVisibility(ProgressBar.INVISIBLE);
            boardAdapter.notifyDataSetChanged();
            isLoadingMore = false;
        }

        private void parseBoard(Document boardPage) {
            if (boardTitle == null || Objects.equals(boardTitle, ""))
                boardTitle = boardPage.select("div.nav a.nav").last().text();

            //Removes loading item
            if (isLoadingMore) {
                if (parsedTopics.size() > 0) parsedTopics.remove(parsedTopics.size() - 1);
            }
            //Finds number of pages
            if (numberOfPages == -1) {
                numberOfPages = 1;
                try {
                    Elements pages = boardPage.select("table.tborder td.catbg[height=30]").first()
                            .select("a.navPages");
                    if (pages != null && !pages.isEmpty()) {
                        for (Element page : pages) {
                            if (Integer.parseInt(page.text()) > numberOfPages)
                                numberOfPages = Integer.parseInt(page.text());
                        }
                    }
                } catch (NullPointerException nullP) {
                    //It just means this board has only one page of topics.
                }
            }
            { //Finds sub boards
                Elements subBoardRows = boardPage.select("div.tborder>table>tbody>tr");
                if (subBoardRows != null && !subBoardRows.isEmpty()) {
                    for (Element subBoardRow : subBoardRows) {
                        if (!Objects.equals(subBoardRow.className(), "titlebg")) {
                            String pUrl = "", pTitle = "", pMods = "", pStats = "",
                                    pLastPost = "No posts yet", pLastPostUrl = "";
                            Elements subBoardColumns = subBoardRow.select(">td");
                            for (Element subBoardCol : subBoardColumns) {
                                if (Objects.equals(subBoardCol.className(), "windowbg"))
                                    pStats = subBoardCol.text();
                                else if (Objects.equals(subBoardCol.className(), "smalltext")) {
                                    pLastPost = subBoardCol.text();
                                    if (pLastPost.contains(" in ")) {
                                        pLastPost = pLastPost.substring(0, pLastPost.indexOf(" in ")) +
                                                "\n" +
                                                pLastPost.substring(pLastPost.indexOf(" in ") + 1, pLastPost.indexOf(" by ")) +
                                                "\n" +
                                                pLastPost.substring(pLastPost.lastIndexOf(" by ") + 1);
                                        pLastPostUrl = subBoardCol.select("a").first().attr("href");
                                    } else if (pLastPost.contains(" σε ")) {
                                        pLastPost = pLastPost.substring(0, pLastPost.indexOf(" σε ")) +
                                                "\n" +
                                                pLastPost.substring(pLastPost.indexOf(" σε ") + 1, pLastPost.indexOf(" από ")) +
                                                "\n" +
                                                pLastPost.substring(pLastPost.lastIndexOf(" από ") + 1);
                                        pLastPostUrl = subBoardCol.select("a").first().attr("href");
                                    } else {
                                        pLastPost = "No posts yet.";
                                        pLastPostUrl = "";
                                    }
                                } else {
                                    pUrl = subBoardCol.select("a").first().attr("href");
                                    pTitle = subBoardCol.select("a").first().text();
                                    if (subBoardCol.select("div.smalltext").first() != null) {
                                        pMods = subBoardCol.select("div.smalltext").first().text();
                                    }
                                }
                            }
                            parsedSubBoards.add(new Board(pUrl, pTitle, pMods, pStats, pLastPost, pLastPostUrl));
                        }
                    }
                }
            }
            { //Finds topics
                Elements topicRows = boardPage.select("table.bordercolor>tbody>tr");
                if (topicRows != null && !topicRows.isEmpty()) {
                    for (Element topicRow : topicRows) {
                        if (!Objects.equals(topicRow.className(), "titlebg")) {
                            String pTopicUrl, pSubject, pStartedBy, pLastPost, pLastPostUrl, pStats;
                            boolean pLocked = false, pSticky = false;
                            Elements topicColumns = topicRow.select(">td");
                            {
                                Element column = topicColumns.get(2);
                                Element tmp = column.select("span[id^=msg_] a").first();
                                pTopicUrl = tmp.attr("href");
                                pSubject = tmp.text();
                                if (column.select("img[id^=stickyicon]").first() != null)
                                    pSticky = true;
                                if (column.select("img[id^=lockicon]").first() != null)
                                    pLocked = true;
                            }
                            pStartedBy = topicColumns.get(3).text();
                            pStats = "Replies " + topicColumns.get(4).text() + ", Views " + topicColumns.get(5).text();

                            pLastPost = topicColumns.last().text();
                            if (pLastPost.contains("by")) {
                                pLastPost = pLastPost.substring(0, pLastPost.indexOf("by")) +
                                        "\n" + pLastPost.substring(pLastPost.indexOf("by"));
                            } else {
                                pLastPost = pLastPost.substring(0, pLastPost.indexOf("από")) +
                                        "\n" + pLastPost.substring(pLastPost.indexOf("από"));
                            }
                            pLastPostUrl = topicColumns.last().select("a:has(img)").first().attr("href");
                            parsedTopics.add(new Topic(pTopicUrl, pSubject, pStartedBy, pLastPost, pLastPostUrl,
                                    pStats, pLocked, pSticky));
                        }
                    }
                }
            }
        }
    }
}
