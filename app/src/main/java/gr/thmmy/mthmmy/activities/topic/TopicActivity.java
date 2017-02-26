package gr.thmmy.mthmmy.activities.topic;

import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Bookmark;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.ParseHelpers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;
import static gr.thmmy.mthmmy.activities.topic.ReplyParser.replyStatus;

/**
 * Activity for topics. When creating an Intent of this activity you need to bundle a <b>String</b>
 * containing this topics's url using the key {@link #BUNDLE_TOPIC_URL} and a <b>String</b> containing
 * this topic's title using the key {@link #BUNDLE_TOPIC_TITLE}.
 */
@SuppressWarnings("unchecked")
public class TopicActivity extends BaseActivity {
    //Class variables
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "TopicActivity";
    /**
     * The key to use when putting topic's url String to {@link TopicActivity}'s Bundle.
     */
    public static final String BUNDLE_TOPIC_URL = "TOPIC_URL";
    /**
     * The key to use when putting topic's title String to {@link TopicActivity}'s Bundle.
     */
    public static final String BUNDLE_TOPIC_TITLE = "TOPIC_TITLE";
    private static TopicTask topicTask;
    //About posts
    private TopicAdapter topicAdapter;
    private ArrayList<Post> postsList;
    private static final int NO_POST_FOCUS = -1;
    private int postFocus = NO_POST_FOCUS;
    private static int postFocusPosition = 0;
    //Reply
    private FloatingActionButton replyFAB;
    private String replyPageUrl = null;
    //Topic's pages
    private int thisPage = 1;
    private int numberOfPages = 1;
    private final SparseArray<String> pagesUrls = new SparseArray<>();
    //Page select
    private final Handler repeatUpdateHandler = new Handler();
    private final long INITIAL_DELAY = 500;
    private boolean autoIncrement = false;
    private boolean autoDecrement = false;
    private static final int SMALL_STEP = 1;
    private static final int LARGE_STEP = 10;
    private Integer pageRequestValue;
    //Bottom navigation graphics
    LinearLayout bottomNavBar;
    private ImageButton firstPage;
    private ImageButton previousPage;
    private TextView pageIndicator;
    private ImageButton nextPage;
    private ImageButton lastPage;
    //Topic's info
    SpannableStringBuilder topicTreeAndMods = new SpannableStringBuilder("Loading..."),
            topicViewers = new SpannableStringBuilder("Loading...");
    //Other variables
    private MaterialProgressBar progressBar;
    private static String base_url = "";
    private String topicTitle;
    private String parsedTitle;
    private RecyclerView recyclerView;
    private String loadedPageUrl = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Bundle extras = getIntent().getExtras();
        topicTitle = extras.getString(BUNDLE_TOPIC_TITLE);
        String topicPageUrl = extras.getString(BUNDLE_TOPIC_URL);
        ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(
                Uri.parse(topicPageUrl));
        if (!target.is(ThmmyPage.PageCategory.TOPIC)) {
            Report.e(TAG, "Bundle came with a non topic url!\nUrl:\n" + topicPageUrl);
            Toast.makeText(this, "An error has occurred\n Aborting.", Toast.LENGTH_SHORT).show();
            finish();
        }

        thisPageBookmark = new Bookmark(topicTitle, ThmmyPage.getTopicId(topicPageUrl));

        //Initializes graphics
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setText(topicTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        //Makes title scrollable
        toolbarTitle.setHorizontallyScrolling(true);
        toolbarTitle.setMovementMethod(new ScrollingMovementMethod());

        createDrawer();

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);

        postsList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.topic_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        topicAdapter = new TopicAdapter(this, postsList, topicTask, topicTitle);
        recyclerView.setAdapter(topicAdapter);

        replyFAB = (FloatingActionButton) findViewById(R.id.topic_fab);
        replyFAB.setEnabled(false);
        bottomNavBar = (LinearLayout) findViewById(R.id.bottom_navigation_bar);
        if (!sessionManager.isLoggedIn()) replyFAB.hide();
        else {
            replyFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sessionManager.isLoggedIn()) {
                        postsList.add(null);
                        topicAdapter.prepareForReply(new ReplyTask());
                        replyFAB.hide();
                        bottomNavBar.setVisibility(View.GONE);
                        topicAdapter.notifyItemInserted(postsList.size());
                    }
                }
            });
        }

        //Sets bottom navigation bar
        firstPage = (ImageButton) findViewById(R.id.page_first_button);
        previousPage = (ImageButton) findViewById(R.id.page_previous_button);
        pageIndicator = (TextView) findViewById(R.id.page_indicator);
        nextPage = (ImageButton) findViewById(R.id.page_next_button);
        lastPage = (ImageButton) findViewById(R.id.page_last_button);

        initDecrementButton(firstPage, LARGE_STEP);
        initDecrementButton(previousPage, SMALL_STEP);
        initIncrementButton(nextPage, SMALL_STEP);
        initIncrementButton(lastPage, LARGE_STEP);
        paginationEnabled(false);

        //Gets posts
        topicTask = new TopicTask();
        topicTask.execute(extras.getString(BUNDLE_TOPIC_URL)); //Attempt data parsing
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.topic_menu, menu);
        setTopicBookmark(menu.getItem(0));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.menu_bookmark:
                topicMenuBookmarkClick();
                return true;
            case R.id.menu_info:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                LinearLayout infoDialog = (LinearLayout) inflater.inflate(R.layout.dialog_topic_info
                        , null);
                ((TextView) infoDialog.findViewById(R.id.dialog_title)).setText("Info");
                TextView treeAndMods = (TextView) infoDialog.findViewById(R.id.topic_tree_and_mods);
                treeAndMods.setText(topicTreeAndMods);
                treeAndMods.setMovementMethod(LinkMovementMethod.getInstance());
                TextView usersViewing = (TextView) infoDialog.findViewById(R.id.users_viewing);
                usersViewing.setText(topicViewers);
                usersViewing.setMovementMethod(LinkMovementMethod.getInstance());

                builder.setView(infoDialog);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
        drawer.setSelection(-1);
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerView.setAdapter(null);
        if (topicTask != null && topicTask.getStatus() != AsyncTask.Status.RUNNING)
            topicTask.cancel(true);
    }

    //--------------------------------------BOTTOM NAV BAR METHODS----------------------------------

    /**
     * This class is used to implement the repetitive incrementPageRequestValue/decrementPageRequestValue
     * of page value when long pressing one of the page navigation buttons.
     */
    class RepetitiveUpdater implements Runnable {
        private final int step;

        /**
         * @param step number of pages to add/subtract on each repetition
         */
        RepetitiveUpdater(int step) {
            this.step = step;
        }

        public void run() {
            long REPEAT_DELAY = 250;
            if (autoIncrement) {
                incrementPageRequestValue(step);
                repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), REPEAT_DELAY);
            } else if (autoDecrement) {
                decrementPageRequestValue(step);
                repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), REPEAT_DELAY);
            }
        }
    }

    private void paginationEnabled(boolean enabled) {
        firstPage.setEnabled(enabled);
        previousPage.setEnabled(enabled);
        nextPage.setEnabled(enabled);
        lastPage.setEnabled(enabled);
    }

    private void paginationEnabledExcept(boolean enabled, View exception) {
        if (exception == firstPage) {
            previousPage.setEnabled(enabled);
            nextPage.setEnabled(enabled);
            lastPage.setEnabled(enabled);
        } else if (exception == previousPage) {
            firstPage.setEnabled(enabled);
            nextPage.setEnabled(enabled);
            lastPage.setEnabled(enabled);
        } else if (exception == nextPage) {
            firstPage.setEnabled(enabled);
            previousPage.setEnabled(enabled);
            lastPage.setEnabled(enabled);
        } else if (exception == lastPage) {
            firstPage.setEnabled(enabled);
            previousPage.setEnabled(enabled);
            nextPage.setEnabled(enabled);
        } else {
            paginationEnabled(enabled);
        }
    }

    private void initIncrementButton(ImageButton increment, final int step) {
        // Increment once for a click
        increment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!autoIncrement && step == LARGE_STEP) {
                    changePage(numberOfPages - 1);
                } else if (!autoIncrement) {
                    incrementPageRequestValue(step);
                    changePage(pageRequestValue - 1);
                }
            }
        });

        // Auto increment for a long click
        increment.setOnLongClickListener(
                new View.OnLongClickListener() {
                    public boolean onLongClick(View arg0) {
                        paginationEnabledExcept(false, arg0);
                        autoIncrement = true;
                        repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), INITIAL_DELAY);
                        return false;
                    }
                }
        );

        // When the button is released
        increment.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                } else if (rect != null && event.getAction() == MotionEvent.ACTION_UP && autoIncrement) {
                    autoIncrement = false;
                    paginationEnabled(true);
                    changePage(pageRequestValue - 1);
                } else if (rect != null && event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        autoIncrement = false;
                        decrementPageRequestValue(pageRequestValue - thisPage);
                        paginationEnabled(true);
                    }
                }
                return false;
            }
        });
    }

    private void initDecrementButton(ImageButton decrement, final int step) {
        // Decrement once for a click
        decrement.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!autoDecrement && step == LARGE_STEP) {
                    changePage(0);
                } else if (!autoDecrement) {
                    decrementPageRequestValue(step);
                    changePage(pageRequestValue - 1);
                }
            }
        });

        // Auto decrement for a long click
        decrement.setOnLongClickListener(
                new View.OnLongClickListener() {
                    public boolean onLongClick(View arg0) {
                        paginationEnabledExcept(false, arg0);
                        autoDecrement = true;
                        repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), INITIAL_DELAY);
                        return false;
                    }
                }
        );

        // When the button is released
        decrement.setOnTouchListener(new View.OnTouchListener() {
            private Rect rect;

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    rect = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
                } else if (event.getAction() == MotionEvent.ACTION_UP && autoDecrement) {
                    autoDecrement = false;
                    paginationEnabled(true);
                    changePage(pageRequestValue - 1);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) { //TODO fix bug
                        autoIncrement = false;
                        incrementPageRequestValue(thisPage - pageRequestValue);
                        paginationEnabled(true);
                    }
                }
                return false;
            }
        });
    }

    private void incrementPageRequestValue(int step) {
        if (pageRequestValue < numberOfPages - step) {
            pageRequestValue = pageRequestValue + step;
        } else
            pageRequestValue = numberOfPages;
        pageIndicator.setText(pageRequestValue + "/" + String.valueOf(numberOfPages));
    }

    private void decrementPageRequestValue(int step) {
        if (pageRequestValue > step)
            pageRequestValue = pageRequestValue - step;
        else
            pageRequestValue = 1;
        pageIndicator.setText(pageRequestValue + "/" + String.valueOf(numberOfPages));
    }

    private void changePage(int pageRequested) {
        if (pageRequested != thisPage - 1) {
            if (topicTask != null && topicTask.getStatus() != AsyncTask.Status.RUNNING)
                topicTask.cancel(true);

            topicTask = new TopicTask();
            topicTask.execute(pagesUrls.get(pageRequested)); //Attempt data parsing

        }
    }
//------------------------------------BOTTOM NAV BAR METHODS END------------------------------------

    /**
     * An {@link AsyncTask} that handles asynchronous fetching of a topic page and parsing it's
     * data. {@link AsyncTask#onPostExecute(Object) OnPostExecute} method calls {@link RecyclerView#swapAdapter}
     * to build graphics.
     * <p>
     * <p>Calling TopicTask's {@link AsyncTask#execute execute} method needs to have profile's url
     * as String parameter!</p>
     */
    class TopicTask extends AsyncTask<String, Void, Integer> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        private static final String TAG = "TopicTask"; //Separate tag for AsyncTask
        private static final int SUCCESS = 0;
        private static final int NETWORK_ERROR = 1;
        private static final int OTHER_ERROR = 2;
        private static final int SAME_PAGE = 3;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            paginationEnabled(false);
            if (replyFAB.getVisibility() != View.GONE) replyFAB.setEnabled(false);
        }

        protected Integer doInBackground(String... strings) {
            Document document;
            base_url = strings[0].substring(0, strings[0].lastIndexOf(".")); //New topic's base url
            String newPageUrl = strings[0];

            //Finds the index of message focus if present
            {
                postFocus = NO_POST_FOCUS;
                if (newPageUrl.contains("msg")) {
                    String tmp = newPageUrl.substring(newPageUrl.indexOf("msg") + 3);
                    if (tmp.contains(";"))
                        postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf(";")));
                    else if (tmp.contains("#"))
                        postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf("#")));
                }
            }
            //Checks if the page to be loaded is the one already shown
            if (!Objects.equals(loadedPageUrl, "") && loadedPageUrl.contains(base_url)) {
                if (newPageUrl.contains("topicseen#new") || newPageUrl.contains("#new"))
                    if (thisPage == numberOfPages)
                        return SAME_PAGE;
                if (newPageUrl.contains("msg")) {
                    String tmpUrlSbstr = newPageUrl.substring(newPageUrl.indexOf("msg") + 3);
                    if (tmpUrlSbstr.contains("msg"))
                        tmpUrlSbstr = tmpUrlSbstr.substring(0, tmpUrlSbstr.indexOf("msg") - 1);
                    int testAgainst = Integer.parseInt(tmpUrlSbstr);
                    for (Post post : postsList) {
                        if (post.getPostIndex() == testAgainst) {
                            return SAME_PAGE;
                        }
                    }
                } else if (Integer.parseInt(newPageUrl.substring(base_url.length() + 1)) / 15 + 1 == thisPage) //TODO fix bug
                    return SAME_PAGE;
            } else if (!Objects.equals(loadedPageUrl, "")) topicTitle = null;

            loadedPageUrl = newPageUrl;
            replyPageUrl = null;
            Request request = new Request.Builder()
                    .url(newPageUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());
                parse(document);
                return SUCCESS;
            } catch (IOException e) {
                Report.i(TAG, "IO Exception", e);
                return NETWORK_ERROR;
            } catch (Exception e) {
                Report.e(TAG, "Exception", e);
                return OTHER_ERROR;
            }
        }

        protected void onPostExecute(Integer parseResult) {
            //Finds the position of the focused message if present
            for (int i = 0; i < postsList.size(); ++i) {
                if (postsList.get(i).getPostIndex() == postFocus) {
                    postFocusPosition = i;
                    break;
                }
            }

            switch (parseResult) {
                case SUCCESS:
                    if (topicTitle == null || Objects.equals(topicTitle, "")) {
                        thisPageBookmark = new Bookmark(parsedTitle, ThmmyPage.getTopicId(loadedPageUrl));
                        invalidateOptionsMenu();
                        //setTopicBookmark(menu.getItem(0));
                    }

                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    topicAdapter.customNotifyDataSetChanged(new TopicTask());
                    if (replyPageUrl == null) replyFAB.hide();
                    if (replyFAB.getVisibility() != View.GONE) replyFAB.setEnabled(true);

                    //Set current page
                    pageIndicator.setText(String.valueOf(thisPage) + "/" + String.valueOf(numberOfPages));
                    pageRequestValue = thisPage;

                    paginationEnabled(true);

                    if (topicTitle == null || Objects.equals(topicTitle, ""))
                        toolbar.setTitle(parsedTitle);
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    break;
                case SAME_PAGE:
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    topicAdapter.customNotifyDataSetChanged(new TopicTask());
                    if (replyFAB.getVisibility() != View.GONE) replyFAB.setEnabled(true);
                    paginationEnabled(true);
                    Toast.makeText(TopicActivity.this, "That's the same page.", Toast.LENGTH_SHORT).show();
                    //TODO change focus
                    break;
                default:
                    //Parse failed - should never happen
                    Report.d(TAG, "Parse failed!");
                    Toast.makeText(getBaseContext(), "Fatal Error", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        }

        /**
         * All the parsing a topic needs.
         *
         * @param topic {@link Document} object containing this topic's source code
         * @see org.jsoup.Jsoup Jsoup
         */
        private void parse(Document topic) {
            ParseHelpers.Language language = ParseHelpers.Language.getLanguage(topic);

            //Finds topic's tree, mods and users viewing
            {
                topicTreeAndMods = getSpannableFromHtml(topic.select("div.nav").first().html());
                topicViewers = getSpannableFromHtml(TopicParser.parseUsersViewingThisTopic(topic, language));
            }

            //Finds reply page url
            {
                Element replyButton = topic.select("a:has(img[alt=Reply])").first();
                if (replyButton == null)
                    replyButton = topic.select("a:has(img[alt=Απάντηση])").first();
                if (replyButton != null) replyPageUrl = replyButton.attr("href");
            }

            //Finds topic title if missing
            if (topicTitle == null || Objects.equals(topicTitle, "")) {
                parsedTitle = topic.select("td[id=top_subject]").first().text();
                if (parsedTitle.contains("Topic:")) {
                    parsedTitle = parsedTitle.substring(parsedTitle.indexOf("Topic:") + 7
                            , parsedTitle.indexOf("(Read") - 2);
                } else {
                    parsedTitle = parsedTitle.substring(parsedTitle.indexOf("Θέμα:") + 6
                            , parsedTitle.indexOf("(Αναγνώστηκε") - 2);
                    Report.d(TAG, parsedTitle);
                }
            }

            { //Finds current page's index
                thisPage = TopicParser.parseCurrentPageIndex(topic, language);
            }
            { //Finds number of pages
                numberOfPages = TopicParser.parseTopicNumberOfPages(topic, thisPage, language);

                for (int i = 0; i < numberOfPages; i++) {
                    //Generate each page's url from topic's base url +".15*numberOfPage"
                    pagesUrls.put(i, base_url + "." + String.valueOf(i * 15));
                }
            }

            postsList.clear();
            postsList.addAll(TopicParser.parseTopic(topic, language));
        }

        private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
            int start = strBuilder.getSpanStart(span);
            int end = strBuilder.getSpanEnd(span);
            int flags = strBuilder.getSpanFlags(span);
            ClickableSpan clickable = new ClickableSpan() {
                @Override
                public void onClick(View view) {
                    ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(Uri.parse(span.getURL()));
                    if (target.is(ThmmyPage.PageCategory.BOARD)) {
                        Intent intent = new Intent(getApplicationContext(), BoardActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(BUNDLE_BOARD_URL, span.getURL());
                        extras.putString(BUNDLE_BOARD_TITLE, "");
                        intent.putExtras(extras);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    } else if (target.is(ThmmyPage.PageCategory.PROFILE)) {
                        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(BUNDLE_PROFILE_URL, span.getURL());
                        extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, "");
                        extras.putString(BUNDLE_PROFILE_USERNAME, "");
                        intent.putExtras(extras);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    } else if (target.is(ThmmyPage.PageCategory.INDEX))
                        finish();
                }
            };
            strBuilder.setSpan(clickable, start, end, flags);
            strBuilder.removeSpan(span);
        }

        private SpannableStringBuilder getSpannableFromHtml(String html) {
            CharSequence sequence = Html.fromHtml(html);
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
            URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
            for (URLSpan span : urls) {
                makeLinkClickable(strBuilder, span);
            }
            return strBuilder;
        }
    }

    class ReplyTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            paginationEnabled(false);
            replyFAB.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(String... message) {
            Document document;
            String numReplies, seqnum, sc, subject, topic;

            Request request = new Request.Builder()
                    .url(replyPageUrl + ";wap2")
                    .build();
            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());

                numReplies = replyPageUrl.substring(replyPageUrl.indexOf("num_replies=") + 12);
                seqnum = document.select("input[name=seqnum]").first().attr("value");
                sc = document.select("input[name=sc]").first().attr("value");
                subject = document.select("input[name=subject]").first().attr("value");
                topic = document.select("input[name=topic]").first().attr("value");
            } catch (IOException e) {
                Report.e(TAG, "Post failed.", e);
                return false;
            } catch (Selector.SelectorParseException e) {
                Report.e(TAG, "Post failed.", e);
                return false;
            }

            RequestBody postBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("message", message[0])
                    .addFormDataPart("num_replies", numReplies)
                    .addFormDataPart("seqnum", seqnum)
                    .addFormDataPart("sc", sc)
                    .addFormDataPart("subject", subject)
                    .addFormDataPart("topic", topic)
                    .build();

            Request post = new Request.Builder()
                    .url("https://www.thmmy.gr/smf/index.php?action=post2")
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .post(postBody)
                    .build();

            try {
                client.newCall(post).execute();
                Response response = client.newCall(post).execute();
                switch (replyStatus(response)) {
                    case SUCCESSFUL:
                        return true;
                    case NEW_REPLY_WHILE_POSTING:
                        //TODO this...
                        return true;
                    default:
                        Report.e(TAG, "Malformed post. Request string:\n" + post.toString());
                        return true;
                }
            } catch (IOException e) {
                Report.e(TAG, "Post failed.", e);
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressBar.setVisibility(ProgressBar.GONE);
            replyFAB.setVisibility(View.VISIBLE);
            bottomNavBar.setVisibility(View.VISIBLE);
            if (!result)
                Toast.makeText(TopicActivity.this, "Post failed!", Toast.LENGTH_SHORT).show();
            paginationEnabled(true);
            replyFAB.setEnabled(true);
        }
    }
}