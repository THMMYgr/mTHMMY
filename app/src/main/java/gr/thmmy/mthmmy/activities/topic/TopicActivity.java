package gr.thmmy.mthmmy.activities.topic;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
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
import gr.thmmy.mthmmy.activities.settings.SettingsActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Bookmark;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.CustomLinearLayoutManager;
import gr.thmmy.mthmmy.utils.HTMLUtils;
import gr.thmmy.mthmmy.utils.parsing.ParseException;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import gr.thmmy.mthmmy.viewmodel.TopicViewModel;
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
import static gr.thmmy.mthmmy.services.NotificationService.NEW_POST_TAG;

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
     * Holds the topicId of this page
     */
    private int loadedPageTopicId = -1;
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
     * Holds the index of the post that has focus
     */

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
    private TopicViewModel viewModel;

    //Topic's info related
    private SpannableStringBuilder topicTreeAndMods = new SpannableStringBuilder("Loading..."),
            topicViewers = new SpannableStringBuilder("Loading...");

    boolean includeAppSignaturePreference = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Bundle extras = getIntent().getExtras();
        topicTitle = extras.getString(BUNDLE_TOPIC_TITLE);
        String maybeTopicTitle = topicTitle;
        topicPageUrl = extras.getString(BUNDLE_TOPIC_URL);
        ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(
                Uri.parse(topicPageUrl));
        if (!target.is(ThmmyPage.PageCategory.TOPIC)) {
            Timber.e("Bundle came with a non topic url!\nUrl: %s", topicPageUrl);
            Toast.makeText(this, "An error has occurred\n Aborting.", Toast.LENGTH_SHORT).show();
            finish();
        }

        topicPageUrl = ThmmyPage.sanitizeTopicUrl(topicPageUrl);

        if (sessionManager.isLoggedIn()) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            includeAppSignaturePreference = sharedPrefs.getBoolean(SettingsActivity.APP_SIGNATURE_ENABLE_KEY, true);
        }

        thisPageBookmark = new Bookmark(topicTitle, ThmmyPage.getTopicId(topicPageUrl), true);

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
        topicAdapter = new TopicAdapter(this, postsList, viewModel.getTopicTaskResultMutableLiveData().getValue().getBaseUrl(), topicTask);
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
        //topicTask = new TopicTask();
        //topicTask.execute(topicPageUrl); //Attempt data parsing

        viewModel = ViewModelProviders.of(this).get(TopicViewModel.class);
        viewModel.getTopicTaskResultMutableLiveData().observe(this, topicTaskResult -> {
            if (topicTaskResult == null) {
                hideControls();
            } else {
                switch (topicTaskResult.getResultCode()) {
                    case SUCCESS:
                        if (topicTitle == null || Objects.equals(topicTitle, "")
                                || !Objects.equals(topicTitle, topicTaskResult.getTopicTitle())) {
                            toolbarTitle.setText(topicTaskResult.getTopicTitle());
                        }

                        if (!postsList.isEmpty()) {
                            recyclerView.getRecycledViewPool().clear(); //Avoid inconsistency detected bug
                            postsList.clear();
                            if (topicTitle != null) toolbarTitle.setText(topicTitle);
                            topicAdapter.notifyItemRangeRemoved(0, postsList.size() - 1);
                        }
                        postsList.addAll(topicTaskResult.getNewPostsList());
                        topicAdapter.notifyItemRangeInserted(0, postsList.size());
                        topicAdapter.prepareForDelete(new TopicActivity.DeleteTask());
                        topicAdapter.prepareForPrepareForEdit(new TopicActivity.PrepareForEdit());

                        pageIndicator.setText(String.valueOf(thisPage) + "/" + String.valueOf(numberOfPages));
                        pageRequestValue = topicTaskResult.getCurrentPageIndex();

                        if (topicTaskResult.getCurrentPageIndex() == topicTaskResult.getPageCount()) {
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            if (notificationManager != null)
                                notificationManager.cancel(NEW_POST_TAG, loadedPageTopicId);
                        }

                        showControls();
                    case NETWORK_ERROR:
                        Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_SHORT).show();
                        break;
                    case SAME_PAGE:
                        showControls();
                        Toast.makeText(getBaseContext(), "That's the same page", Toast.LENGTH_SHORT).show();
                        //TODO change focus
                        break;
                    case UNAUTHORIZED:
                        showControls();
                        Toast.makeText(getBaseContext(), "This topic is either missing or off limits to you", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        //Parse failed - should never happen
                        Timber.d("Parse failed!");  //TODO report ParseException!!!
                        Toast.makeText(getBaseContext(), "Fatal Error", Toast.LENGTH_SHORT).show();
                        finish();
                        break;
                }
            }

        });
        viewModel.initialLoad(topicPageUrl);

    }

    public void hideControls() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        paginationEnabled(false);
        if (replyFAB.getVisibility() != View.GONE) replyFAB.setEnabled(false);
    }

    public void showControls() {
        progressBar.setVisibility(ProgressBar.INVISIBLE);
        if (replyPageUrl == null) {
            replyFAB.hide();
            topicAdapter.resetTopic(viewModel.getTopicTaskResultMutableLiveData().getValue().getBaseUrl(), new TopicTask(), false);
        } else topicAdapter.resetTopic(viewModel.getTopicTaskResultMutableLiveData().getValue().getBaseUrl(), new TopicTask(), true);
        paginationEnabled(true);
        if (replyFAB.getVisibility() != View.GONE) replyFAB.setEnabled(true);
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
                //treeAndMods.setText(new SpannableStringBuilder("Loading..."));
                treeAndMods.setMovementMethod(LinkMovementMethod.getInstance());
                TextView usersViewing = infoDialog.findViewById(R.id.users_viewing);
                //usersViewing.setText(new SpannableStringBuilder("Loading..."));
                usersViewing.setMovementMethod(LinkMovementMethod.getInstance());
                viewModel.getTopicTaskResultMutableLiveData().observe(this, topicTaskResult -> {
                    if (topicTaskResult == null) {
                        usersViewing.setText(new SpannableStringBuilder("Loading..."));
                        treeAndMods.setText(new SpannableStringBuilder("Loading..."));
                    } else {
                        String treeAndModsString = topicTaskResult.getTopicTreeAndMods();
                        treeAndMods.setText(HTMLUtils.getSpannableFromHtml(this, treeAndModsString));
                        String topicViewersString = topicTaskResult.getTopicViewers();
                        usersViewing.setText(HTMLUtils.getSpannableFromHtml(this, topicViewersString));
                    }
                });

                builder.setView(infoDialog);
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            case R.id.menu_share:
                Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                sendIntent.setType("text/plain");
                sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, topicPageUrl);
                startActivity(Intent.createChooser(sendIntent, "Share via"));
                return true;                    //invalidateOptionsMenu();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen()) {
            drawer.closeDrawer();
            return;
        } else if (postsList != null && postsList.size() > 0 && postsList.get(postsList.size() - 1) == null) {
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

        if (sessionManager.isLoggedIn()) {
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
            includeAppSignaturePreference = sharedPrefs.getBoolean(SettingsActivity.APP_SIGNATURE_ENABLE_KEY, true);
        }
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

    @SuppressLint("ClickableViewAccessibility")
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

    @SuppressLint("ClickableViewAccessibility")
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

    class PrepareForReply extends AsyncTask<ArrayList<Integer>, Void, Boolean> {
        String numReplies, seqnum, sc, topic, buildedQuotes = "";

        @Override
        protected void onPreExecute() {
            changePage(numberOfPages - 1);
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
            postsList.add(Post.newQuickReply());
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
            final String sentFrommTHMMY = includeAppSignaturePreference
                    ? "\n[right][size=7pt][i]sent from [url=https://play.google.com/store/apps/details?id=gr.thmmy.mthmmy]mTHMMY  [/url][/i][/size][/right]"
                    : "";
            RequestBody postBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("message", args[1] + sentFrommTHMMY)
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
                        Timber.e("Malformed post. Request string: %s", post.toString());
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
                    topicTask.execute(viewModel.getTopicTaskResultMutableLiveData().getValue().getBaseUrl() + "." + 2147483647);
                else {
                    reloadingPage = true;
                    topicTask.execute(loadedPageUrl);
                }
            }
        }
    }

    class DeleteTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            paginationEnabled(false);
            replyFAB.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(String... args) {
            Request delete = new Request.Builder()
                    .url(args[0])
                    .header("User-Agent",
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36")
                    .build();

            try {
                client.newCall(delete).execute();
                Response response = client.newCall(delete).execute();
                //Response response = client.newCall(delete).execute();
                switch (replyStatus(response)) {
                    case SUCCESSFUL:
                        return true;
                    default:
                        Timber.e("Something went wrong. Request string: %s", delete.toString());
                        return true;
                }
            } catch (IOException e) {
                Timber.e(e, "Delete failed.");
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean result) {
            progressBar.setVisibility(ProgressBar.GONE);
            replyFAB.setVisibility(View.VISIBLE);
            bottomNavBar.setVisibility(View.VISIBLE);

            if (!result)
                Toast.makeText(TopicActivity.this, "Post deleted!", Toast.LENGTH_SHORT).show();
            paginationEnabled(true);
            replyFAB.setEnabled(true);

            if (result) {
                topicTask = new TopicTask();
                reloadingPage = true;
                topicTask.execute(loadedPageUrl);
            }
        }
    }

    class PrepareForEdit extends AsyncTask<Integer, Void, Boolean> {
        int position;
        String commitEditURL, numReplies, seqnum, sc, topic, postText = "";

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            paginationEnabled(false);
            replyFAB.setEnabled(false);
            replyFAB.hide();
            bottomNavBar.setVisibility(View.GONE);
            topicAdapter.disablePostEditing();
        }

        @Override
        protected Boolean doInBackground(Integer... positions) {
            Document document;
            position = positions[0];
            String url = postsList.get(position).getPostEditURL();
            Request request = new Request.Builder()
                    .url(url + ";wap2")
                    .build();

            try {
                Response response = client.newCall(request).execute();
                document = Jsoup.parse(response.body().string());

                Element message = document.select("textarea").first();
                postText = message.text();

                commitEditURL = document.select("form").first().attr("action");
                numReplies = replyPageUrl.substring(replyPageUrl.indexOf("num_replies=") + 12);
                seqnum = document.select("input[name=seqnum]").first().attr("value");
                sc = document.select("input[name=sc]").first().attr("value");
                topic = document.select("input[name=topic]").first().attr("value");

                return true;
            } catch (IOException | Selector.SelectorParseException e) {
                Timber.e(e, "Prepare failed.");
                return false;
            }
        }


        @Override
        protected void onPostExecute(Boolean result) {
            postsList.get(position).setPostType(Post.TYPE_EDIT);
            topicAdapter.notifyItemChanged(position);
            topicAdapter.prepareForEdit(new EditTask(), commitEditURL, numReplies, seqnum, sc, topic, postText);
            recyclerView.scrollToPosition(position);
            progressBar.setVisibility(ProgressBar.GONE);
        }
    }

    public class EditTask extends AsyncTask<EditTaskDTO, Void, Boolean> {
        EditTaskDTO dto;

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            paginationEnabled(false);
            replyFAB.setEnabled(false);
        }

        @Override
        protected Boolean doInBackground(EditTaskDTO... editTaskDTOS) {
            dto = editTaskDTOS[0];
            RequestBody postBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("message", dto.getMessage())
                    .addFormDataPart("num_replies", dto.getNumReplies())
                    .addFormDataPart("seqnum", dto.getSeqnum())
                    .addFormDataPart("sc", dto.getSc())
                    .addFormDataPart("subject", dto.getSubject())
                    .addFormDataPart("topic", dto.getTopic())
                    .build();
            Request post = new Request.Builder()
                    .url(dto.getUrl())
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
                        Timber.e("Malformed post. Request string: %s", post.toString());
                        return true;
                }
            } catch (IOException e) {
                Timber.e(e, "Edit failed.");
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

            postsList.get(dto.getPosition()).setPostType(Post.TYPE_POST);
            topicAdapter.notifyItemChanged(dto.getPosition());

            progressBar.setVisibility(ProgressBar.GONE);
            replyFAB.setVisibility(View.VISIBLE);
            bottomNavBar.setVisibility(View.VISIBLE);

            if (!result)
                Toast.makeText(TopicActivity.this, "Edit failed!", Toast.LENGTH_SHORT).show();
            paginationEnabled(true);
            replyFAB.setEnabled(true);
            topicAdapter.enablePostEditing();

            if (result) {
                topicTask = new TopicTask();
                reloadingPage = true;
                topicTask.execute(loadedPageUrl);
            }
        }
    }
}