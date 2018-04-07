package gr.thmmy.mthmmy.activities.topic;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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
import gr.thmmy.mthmmy.utils.CustomLinearLayoutManager;
import gr.thmmy.mthmmy.utils.ParseHelpers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;
import static gr.thmmy.mthmmy.activities.topic.Posting.replyStatus;

/**
 * Activity for parsing and rendering topics. When creating an Intent of this activity you need to
 * bundle a <b>String</b> containing this topic's url using the key {@link #BUNDLE_TOPIC_URL}.
 * You can also bundle a <b>String</b> containing this topic's title, if its available, using the
 * key {@link #BUNDLE_TOPIC_TITLE} for faster title rendering.
 */
@SuppressWarnings("unchecked")
public class TopicActivity extends BaseActivity {
    //Activity's variables
    /**
     * The key to use when putting topic's url String to {@link TopicActivity}'s Bundle.
     */
    public static final String BUNDLE_TOPIC_URL = "TOPIC_URL";
    /**
     * The key to use when putting topic's title String to {@link TopicActivity}'s Bundle.
     */
    public static final String BUNDLE_TOPIC_TITLE = "TOPIC_TITLE";
    private static TopicTask topicTask;
    private MaterialProgressBar progressBar;
    private TextView toolbarTitle;
    /**
     * Holds this topic's base url. For example a topic with url similar to
     * "https://www.thmmy.gr/smf/index.php?topic=1.15;topicseen" or
     * "https://www.thmmy.gr/smf/index.php?topic=1.msg1#msg1"
     * has the base url "https://www.thmmy.gr/smf/index.php?topic=1"
     */
    private static String base_url = "";
    /**
     * Holds this topic's title. At first this gets the value of the topic title that came with
     * bundle and is rendered in the toolbar while parsing this topic. Later, after topic's parsing
     * is done, it gets the value of {@link #parsedTitle} if bundle title and parsed title differ.
     */
    private String topicTitle;
    /**
     * Holds this topic's title as parsed from the html source. If this (parsed) title is different
     * than the one that came with activity's bundle then the parsed title is preferred over the
     * bundle one and gets rendered in the toolbar.
     */
    private String parsedTitle;
    private String topicPageUrl;
    private RecyclerView recyclerView;
    /**
     * Holds the url of this page
     */
    private String loadedPageUrl = "";
    /**
     * Becomes true after user has posted in this topic and the page is being reloaded and false
     * when topic's reloading is done
     */
    private boolean reloadingPage = false;
    //Posts related
    private TopicAdapter topicAdapter;
    /**
     * Holds a list of this topic's posts
     */
    private ArrayList<Post> postsList;
    /**
     * Gets assigned to {@link #postFocus} when there is no post focus information in the url
     */
    private static final int NO_POST_FOCUS = -1;
    /**
     * Holds the index of the post that has focus
     */
    private int postFocus = NO_POST_FOCUS;
    /**
     * Holds the position in the {@link #postsList} of the post with focus
     */
    private static int postFocusPosition = 0;
    //Reply related
    private FloatingActionButton replyFAB;
    /**
     * Holds this topic's reply url
     */
    private String replyPageUrl = null;
    //Topic's pages related
    /**
     * Holds current page's index (starting from 1, not 0)
     */
    private int thisPage = 1;
    /**
     * Holds this topic's number of pages
     */
    private int numberOfPages = 1;
    /**
     * Holds a list of this topic's pages urls
     */
    private final SparseArray<String> pagesUrls = new SparseArray<>();
    //Page select related
    /**
     * Used for handling bottom navigation bar's buttons long click user interactions
     */
    private final Handler repeatUpdateHandler = new Handler();
    /**
     * Holds the initial time delay before a click on bottom navigation bar is considered long
     */
    private final long INITIAL_DELAY = 500;
    private boolean autoIncrement = false;
    private boolean autoDecrement = false;
    /**
     * Holds the number of pages to be added or subtracted from current page on each step while a
     * long click is held in either next or previous buttons
     */
    private static final int SMALL_STEP = 1;
    /**
     * Holds the number of pages to be added or subtracted from current page on each step while a
     * long click is held in either first or last buttons
     */
    private static final int LARGE_STEP = 10;
    /**
     * Holds the value (index) of the page to be requested when a user interaction with bottom
     * navigation bar occurs
     */
    private Integer pageRequestValue;
    //Bottom navigation bar graphics related
    private LinearLayout bottomNavBar;
    private ImageButton firstPage;
    private ImageButton previousPage;
    private TextView pageIndicator;
    private ImageButton nextPage;
    private ImageButton lastPage;
    //Topic's info related
    private SpannableStringBuilder topicTreeAndMods = new SpannableStringBuilder("Loading..."),
            topicViewers = new SpannableStringBuilder("Loading...");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Bundle extras = getIntent().getExtras();
        topicTitle = extras.getString(BUNDLE_TOPIC_TITLE);
        topicPageUrl = extras.getString(BUNDLE_TOPIC_URL);
        ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(
                Uri.parse(topicPageUrl));
        if (!target.is(ThmmyPage.PageCategory.TOPIC)) {
            Timber.e("Bundle came with a non topic url!\nUrl: %s", topicPageUrl);
            Toast.makeText(this, "An error has occurred\n Aborting.", Toast.LENGTH_SHORT).show();
            finish();
        }

        thisPageBookmark = new Bookmark(topicTitle, ThmmyPage.getTopicId(topicPageUrl));

        //Initializes graphics
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setSingleLine(true);
        toolbarTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        toolbarTitle.setMarqueeRepeatLimit(-1);
        toolbarTitle.setText(topicTitle);
        toolbarTitle.setSelected(true);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();

        progressBar = findViewById(R.id.progressBar);

        postsList = new ArrayList<>();

        recyclerView = findViewById(R.id.topic_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setOnTouchListener(
                new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        v.performClick();
                        return topicTask != null && topicTask.getStatus() == AsyncTask.Status.RUNNING;
                    }
                }
        );
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        CustomLinearLayoutManager layoutManager = new CustomLinearLayoutManager(
                getApplicationContext(), loadedPageUrl);
        recyclerView.setLayoutManager(layoutManager);
        topicAdapter = new TopicAdapter(this, postsList, base_url, topicTask);
        recyclerView.setAdapter(topicAdapter);

        replyFAB = findViewById(R.id.topic_fab);
        replyFAB.setEnabled(false);
        bottomNavBar = findViewById(R.id.bottom_navigation_bar);
        if (!sessionManager.isLoggedIn()) replyFAB.hide();
        else {
            replyFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sessionManager.isLoggedIn()) {
                        PrepareForReply prepareForReply = new PrepareForReply();
                        prepareForReply.execute(topicAdapter.getToQuoteList());
                    }
                }
            });
        }

        //Sets bottom navigation bar
        firstPage = findViewById(R.id.page_first_button);
        previousPage = findViewById(R.id.page_previous_button);
        pageIndicator = findViewById(R.id.page_indicator);
        nextPage = findViewById(R.id.page_next_button);
        lastPage = findViewById(R.id.page_last_button);

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
        super.onCreateOptionsMenu(menu);
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
                TextView treeAndMods = infoDialog.findViewById(R.id.topic_tree_and_mods);
                treeAndMods.setText(topicTreeAndMods);
                treeAndMods.setMovementMethod(LinkMovementMethod.getInstance());
                TextView usersViewing = infoDialog.findViewById(R.id.users_viewing);
                usersViewing.setText(topicViewers);
                usersViewing.setMovementMethod(LinkMovementMethod.getInstance());

                builder.setView(infoDialog);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.menu_share:
                Intent sendIntent  = new Intent(android.content.Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, topicPageUrl);
                startActivity(Intent.createChooser(sendIntent, "Share via"));
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
        else if(postsList.get(postsList.size()-1)==null)
        {
            postsList.remove(postsList.size() - 1);
            topicAdapter.notifyItemRemoved(postsList.size());
            topicAdapter.setBackButtonHidden();
            replyFAB.setVisibility(View.INVISIBLE);
            bottomNavBar.setVisibility(View.INVISIBLE);
            paginationEnabled(true);
            replyFAB.setEnabled(true);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTopicBookmark();
        drawer.setSelection(-1);
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
    private class RepetitiveUpdater implements Runnable {
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

    private void paginationDisable(View exception) {
        if (exception == firstPage) {
            previousPage.setEnabled(false);
            nextPage.setEnabled(false);
            lastPage.setEnabled(false);
        } else if (exception == previousPage) {
            firstPage.setEnabled(false);
            nextPage.setEnabled(false);
            lastPage.setEnabled(false);
        } else if (exception == nextPage) {
            firstPage.setEnabled(false);
            previousPage.setEnabled(false);
            lastPage.setEnabled(false);
        } else if (exception == lastPage) {
            firstPage.setEnabled(false);
            previousPage.setEnabled(false);
            nextPage.setEnabled(false);
        } else {
            paginationEnabled(false);
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
                        paginationDisable(arg0);
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
                        paginationDisable(arg0);
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
                    if (rect != null &&
                            !rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
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
     * An {@link AsyncTask} that handles asynchronous fetching of this topic page and parsing of its
     * data.
     * <p>TopicTask's {@link AsyncTask#execute execute} method needs a topic's url as String
     * parameter.</p>
     */
    class TopicTask extends AsyncTask<String, Void, Integer> {
        private static final int SUCCESS = 0;
        private static final int NETWORK_ERROR = 1;
        private static final int OTHER_ERROR = 2;
        private static final int SAME_PAGE = 3;

        ArrayList<Post> localPostsList;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            paginationEnabled(false);
            if (replyFAB.getVisibility() != View.GONE) replyFAB.setEnabled(false);
        }

        protected Integer doInBackground(String... strings) {
            Document document;
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
            if (!reloadingPage && !Objects.equals(loadedPageUrl, "") && newPageUrl.contains(base_url)) {
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
                } else if ((Objects.equals(newPageUrl, base_url) && thisPage == 1) ||
                        Integer.parseInt(newPageUrl.substring(base_url.length() + 1)) / 15 + 1 == thisPage)
                    return SAME_PAGE;
            } else if (!Objects.equals(loadedPageUrl, "")) topicTitle = null;
            if (reloadingPage) reloadingPage = !reloadingPage;

            loadedPageUrl = newPageUrl;
            if (strings[0].substring(0, strings[0].lastIndexOf(".")).contains("topic="))
                base_url = strings[0].substring(0, strings[0].lastIndexOf(".")); //New topic's base url
            replyPageUrl = null;
            Request request = new Request.Builder()
                    .url(newPageUrl)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());
                localPostsList = parse(document);

                //Finds the position of the focused message if present
                for (int i = 0; i < localPostsList.size(); ++i) {
                    if (localPostsList.get(i).getPostIndex() == postFocus) {
                        postFocusPosition = i;
                        break;
                    }
                }
                return SUCCESS;
            } catch (IOException e) {
                Timber.i(e, "IO Exception");
                return NETWORK_ERROR;
            } catch (Exception e) {
                Timber.e(e, "Exception");
                return OTHER_ERROR;
            }
        }

        protected void onPostExecute(Integer parseResult) {
            switch (parseResult) {
                case SUCCESS:
                    if (topicTitle == null || Objects.equals(topicTitle, "")
                            || !Objects.equals(topicTitle, parsedTitle)) {
                        toolbarTitle.setText(parsedTitle);
                        topicTitle = parsedTitle;
                        thisPageBookmark = new Bookmark(parsedTitle, ThmmyPage.getTopicId(loadedPageUrl));
                        invalidateOptionsMenu();
                    }

                    if (!(postsList.isEmpty() || postsList.size() == 0)) {
                        recyclerView.getRecycledViewPool().clear(); //Avoid inconsistency detected bug
                        postsList.clear();
                        topicAdapter.notifyItemRangeRemoved(0, postsList.size() - 1);
                    }
                    postsList.addAll(localPostsList);
                    topicAdapter.notifyItemRangeInserted(0, postsList.size());
                    progressBar.setVisibility(ProgressBar.INVISIBLE);

                    if (replyPageUrl == null) {
                        replyFAB.hide();
                        topicAdapter.resetTopic(base_url, new TopicTask(), false);
                    } else topicAdapter.resetTopic(base_url, new TopicTask(), true);

                    if (replyFAB.getVisibility() != View.GONE) replyFAB.setEnabled(true);

                    //Set current page
                    pageIndicator.setText(String.valueOf(thisPage) + "/" + String.valueOf(numberOfPages));
                    pageRequestValue = thisPage;

                    paginationEnabled(true);
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    break;
                case SAME_PAGE:
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    if (replyPageUrl == null) {
                        replyFAB.hide();
                        topicAdapter.resetTopic(base_url, new TopicTask(), false);
                    } else topicAdapter.resetTopic(base_url, new TopicTask(), true);
                    if (replyFAB.getVisibility() != View.GONE) replyFAB.setEnabled(true);
                    paginationEnabled(true);
                    Toast.makeText(TopicActivity.this, "That's the same page.", Toast.LENGTH_SHORT).show();
                    //TODO change focus
                    break;
                default:
                    //Parse failed - should never happen
                    Timber.d("Parse failed!");  //TODO report ParseException!!!
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
        private ArrayList<Post> parse(Document topic) {
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
            {
                parsedTitle = topic.select("td[id=top_subject]").first().text();
                if (parsedTitle.contains("Topic:")) {
                    parsedTitle = parsedTitle.substring(parsedTitle.indexOf("Topic:") + 7
                            , parsedTitle.indexOf("(Read") - 2);
                } else {
                    parsedTitle = parsedTitle.substring(parsedTitle.indexOf("Θέμα:") + 6
                            , parsedTitle.indexOf("(Αναγνώστηκε") - 2);
                    Timber.d("Parsed title: %s", parsedTitle);
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

            return TopicParser.parseTopic(topic, language);
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
            CharSequence sequence;
            if (Build.VERSION.SDK_INT >= 24) {
                sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
            } else {
                //noinspection deprecation
                sequence = Html.fromHtml(html);
            }
            SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
            URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
            for (URLSpan span : urls) {
                makeLinkClickable(strBuilder, span);
            }
            return strBuilder;
        }
    }

    class PrepareForReply extends AsyncTask<ArrayList<Integer>, Void, Boolean> {
        String numReplies, seqnum, sc, topic, buildedQuotes = "";

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            paginationEnabled(false);
            replyFAB.setEnabled(false);
            replyFAB.hide();
            bottomNavBar.setVisibility(View.GONE);
        }

        @Override
        protected Boolean doInBackground(ArrayList<Integer>... quoteList) {
            Document document;
            Request request = new Request.Builder()
                    .url(replyPageUrl + ";wap2")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());

                numReplies = replyPageUrl.substring(replyPageUrl.indexOf("num_replies=") + 12);
                seqnum = document.select("input[name=seqnum]").first().attr("value");
                sc = document.select("input[name=sc]").first().attr("value");
                topic = document.select("input[name=topic]").first().attr("value");
            } catch (IOException | Selector.SelectorParseException e) {
                Timber.e(e, "Prepare failed.");
                return false;
            }

            for (Integer quotePosition : quoteList[0]) {
                request = new Request.Builder()
                        .url("https://www.thmmy.gr/smf/index.php?action=quotefast;quote=" +
                                postsList.get(quotePosition).getPostIndex() +
                                ";" + "sesc=" + sc + ";xml")
                        .build();

                try {
                    Response response = client.newCall(request).execute();
                    String body = response.body().string();
                    buildedQuotes += body.substring(body.indexOf("<quote>") + 7, body.indexOf("</quote>"));
                    buildedQuotes += "\n\n";
                } catch (IOException | Selector.SelectorParseException e) {
                    Timber.e(e, "Quote building failed.");
                    return false;
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            postsList.add(null);
            topicAdapter.notifyItemInserted(postsList.size());
            topicAdapter.prepareForReply(new ReplyTask(), topicTitle, numReplies, seqnum, sc,
                    topic, buildedQuotes);
            recyclerView.scrollToPosition(postsList.size() - 1);
            progressBar.setVisibility(ProgressBar.GONE);
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
        protected Boolean doInBackground(String... args) {
            RequestBody postBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("message", args[1])
                    .addFormDataPart("num_replies", args[2])
                    .addFormDataPart("seqnum", args[3])
                    .addFormDataPart("sc", args[4])
                    .addFormDataPart("subject", args[0])
                    .addFormDataPart("topic", args[5])
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
                        Timber.e("Malformed post. Request string:\n" + post.toString());
                        return true;
                }
            } catch (IOException e) {
                Timber.e(e, "Post failed.");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            View view = getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }

            postsList.remove(postsList.size() - 1);
            topicAdapter.notifyItemRemoved(postsList.size());

            progressBar.setVisibility(ProgressBar.GONE);
            replyFAB.setVisibility(View.VISIBLE);
            bottomNavBar.setVisibility(View.VISIBLE);

            if (!result)
                Toast.makeText(TopicActivity.this, "Post failed!", Toast.LENGTH_SHORT).show();
            paginationEnabled(true);
            replyFAB.setEnabled(true);

            if (result) {
                topicTask = new TopicTask();
                if ((postsList.get(postsList.size() - 1).getPostNumber() + 1) % 15 == 0)
                    topicTask.execute(base_url + "." + 2147483647);
                else {
                    reloadingPage = true;
                    topicTask.execute(loadedPageUrl);
                }
            }
        }
    }
}