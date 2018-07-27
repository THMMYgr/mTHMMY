package gr.thmmy.mthmmy.activities.topic;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
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

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.topic.tasks.DeleteTask;
import gr.thmmy.mthmmy.activities.topic.tasks.EditTask;
import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForEditTask;
import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForReply;
import gr.thmmy.mthmmy.activities.topic.tasks.ReplyTask;
import gr.thmmy.mthmmy.activities.topic.tasks.TopicTask;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Bookmark;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.CustomLinearLayoutManager;
import gr.thmmy.mthmmy.utils.HTMLUtils;
import gr.thmmy.mthmmy.viewmodel.TopicViewModel;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

import static gr.thmmy.mthmmy.services.NotificationService.NEW_POST_TAG;

/**
 * Activity for parsing and rendering topics. When creating an Intent of this activity you need to
 * bundle a <b>String</b> containing this topic's url using the key {@link #BUNDLE_TOPIC_URL}.
 * You can also bundle a <b>String</b> containing this topic's title, if its available, using the
 * key {@link #BUNDLE_TOPIC_TITLE} for faster title rendering.
 */
@SuppressWarnings("unchecked")
public class TopicActivity extends BaseActivity implements TopicTask.TopicTaskObserver,
        DeleteTask.DeleteTaskCallbacks, ReplyTask.ReplyTaskCallbacks, PrepareForEditTask.PrepareForEditCallbacks,
        EditTask.EditTaskCallbacks, PrepareForReply.PrepareForReplyCallbacks {
    //Activity's variables
    /**
     * The key to use when putting topic's url String to {@link TopicActivity}'s Bundle.
     */
    public static final String BUNDLE_TOPIC_URL = "TOPIC_URL";
    /**
     * The key to use when putting topic's title String to {@link TopicActivity}'s Bundle.
     */
    public static final String BUNDLE_TOPIC_TITLE = "TOPIC_TITLE";
    private MaterialProgressBar progressBar;
    private TextView toolbarTitle;
    private RecyclerView recyclerView;
    //Posts related
    private TopicAdapter topicAdapter;
    /**
     * Holds a list of this topic's posts
     */
    private ArrayList<Post> postsList;
    //Reply related
    private FloatingActionButton replyFAB;
    //Topic's pages related
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        viewModel = ViewModelProviders.of(this).get(TopicViewModel.class);
        viewModel.setTopicTaskObserver(this);
        viewModel.setDeleteTaskCallbacks(this);
        viewModel.setReplyFinishListener(this);
        viewModel.setPrepareForEditCallbacks(this);
        viewModel.setEditTaskCallbacks(this);
        viewModel.setPrepareForReplyCallbacks(this);

        Bundle extras = getIntent().getExtras();
        String topicTitle = extras.getString(BUNDLE_TOPIC_TITLE);
        String topicPageUrl = extras.getString(BUNDLE_TOPIC_URL);
        ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(
                Uri.parse(topicPageUrl));
        if (!target.is(ThmmyPage.PageCategory.TOPIC)) {
            Timber.e("Bundle came with a non topic url!\nUrl: %s", topicPageUrl);
            Toast.makeText(this, "An error has occurred\n Aborting.", Toast.LENGTH_SHORT).show();
            finish();
        }

        topicPageUrl = ThmmyPage.sanitizeTopicUrl(topicPageUrl);


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
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        CustomLinearLayoutManager layoutManager = new CustomLinearLayoutManager(
                getApplicationContext(), topicPageUrl);
        recyclerView.setLayoutManager(layoutManager);
        topicAdapter = new TopicAdapter(this, postsList);
        recyclerView.setAdapter(topicAdapter);

        replyFAB = findViewById(R.id.topic_fab);
        replyFAB.hide();
        bottomNavBar = findViewById(R.id.bottom_navigation_bar);
        if (!sessionManager.isLoggedIn()) replyFAB.hide();
        else {
            replyFAB.setOnClickListener(view -> {
                if (sessionManager.isLoggedIn()) {
                    viewModel.prepareForReply(postsList, topicAdapter.getToQuoteList());
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

        viewModel.getTopicTaskResult().observe(this, topicTaskResult -> {
            if (topicTaskResult == null) {
                progressBar.setVisibility(ProgressBar.VISIBLE);
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

                        pageIndicator.setText(String.valueOf(topicTaskResult.getCurrentPageIndex()) + "/" +
                                String.valueOf(topicTaskResult.getPageCount()));
                        pageRequestValue = topicTaskResult.getCurrentPageIndex();
                        paginationEnabled(true);

                        if (topicTaskResult.getCurrentPageIndex() == topicTaskResult.getPageCount()) {
                            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                            if (notificationManager != null)
                                notificationManager.cancel(NEW_POST_TAG, topicTaskResult.getLoadedPageTopicId());
                        }

                        progressBar.setVisibility(ProgressBar.GONE);
                        if (topicTaskResult.getReplyPageUrl() == null) {
                            replyFAB.hide();
                        } else {
                            replyFAB.show();
                        }
                        topicAdapter.resetTopic();
                        break;
                    case NETWORK_ERROR:
                        Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_SHORT).show();
                        break;
                    case SAME_PAGE:
                        progressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(getBaseContext(), "That's the same page", Toast.LENGTH_SHORT).show();
                        //TODO change focus
                        break;
                    case UNAUTHORIZED:
                        progressBar.setVisibility(ProgressBar.GONE);
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
        viewModel.getPrepareForReplyResult().observe(this, prepareForReplyResult -> {
            if (prepareForReplyResult != null) {
                //prepare for a reply
                postsList.add(Post.newQuickReply());
                topicAdapter.notifyItemInserted(postsList.size());
                recyclerView.scrollToPosition(postsList.size() - 1);
                progressBar.setVisibility(ProgressBar.GONE);
                replyFAB.hide();
                bottomNavBar.setVisibility(View.GONE);
            }

        });
        viewModel.getPrepareForEditResult().observe(this, result -> {
            if (result != null && result.isSuccessful()) {
                viewModel.setEditingPost(true);
                postsList.get(result.getPosition()).setPostType(Post.TYPE_EDIT);
                topicAdapter.notifyItemChanged(result.getPosition());
                recyclerView.scrollToPosition(result.getPosition());
                progressBar.setVisibility(ProgressBar.GONE);
                replyFAB.hide();
                bottomNavBar.setVisibility(View.GONE);
            }
        });
        viewModel.initialLoad(topicPageUrl);
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
                treeAndMods.setText(new SpannableStringBuilder("Loading..."));
                treeAndMods.setMovementMethod(LinkMovementMethod.getInstance());
                TextView usersViewing = infoDialog.findViewById(R.id.users_viewing);
                usersViewing.setText(new SpannableStringBuilder("Loading..."));
                usersViewing.setMovementMethod(LinkMovementMethod.getInstance());
                viewModel.getTopicTaskResult().observe(this, topicTaskResult -> {
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
                sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, viewModel.getTopicUrl());
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
        } else if (viewModel.isWritingReply()) {
            postsList.remove(postsList.size() - 1);
            topicAdapter.notifyItemRemoved(postsList.size());
            topicAdapter.setBackButtonHidden();
            viewModel.setWritingReply(false);
            replyFAB.show();
            bottomNavBar.setVisibility(View.VISIBLE);
            return;
        } else if (viewModel.isEditingPost()) {
            postsList.get(viewModel.getPostBeingEditedPosition()).setPostType(Post.TYPE_POST);
            topicAdapter.notifyItemChanged(viewModel.getPostBeingEditedPosition());
            topicAdapter.setBackButtonHidden();
            viewModel.setEditingPost(false);
            replyFAB.show();
            bottomNavBar.setVisibility(View.VISIBLE);
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
        viewModel.stopLoading();
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
        increment.setOnClickListener(v -> {
            if (!autoIncrement && step == LARGE_STEP) {
                incrementPageRequestValue(viewModel.getPageCount());
                viewModel.changePage(viewModel.getPageCount() - 1);
            } else if (!autoIncrement) {
                incrementPageRequestValue(step);
                viewModel.changePage(pageRequestValue - 1);
            }
        });

        // Auto increment for a long click
        increment.setOnLongClickListener(
                arg0 -> {
                    paginationDisable(arg0);
                    autoIncrement = true;
                    repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), INITIAL_DELAY);
                    return false;
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
                    viewModel.changePage(pageRequestValue - 1);
                } else if (rect != null && event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        autoIncrement = false;
                        decrementPageRequestValue(pageRequestValue - viewModel.getCurrentPageIndex());
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
        decrement.setOnClickListener(v -> {
            if (!autoDecrement && step == LARGE_STEP) {
                decrementPageRequestValue(viewModel.getPageCount());
                viewModel.changePage(0);
            } else if (!autoDecrement) {
                decrementPageRequestValue(step);
                viewModel.changePage(pageRequestValue - 1);
            }
        });

        // Auto decrement for a long click
        decrement.setOnLongClickListener(
                arg0 -> {
                    paginationDisable(arg0);
                    autoDecrement = true;
                    repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), INITIAL_DELAY);
                    return false;
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
                    viewModel.changePage(pageRequestValue - 1);
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (rect != null &&
                            !rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        autoIncrement = false;
                        incrementPageRequestValue(viewModel.getCurrentPageIndex() - pageRequestValue);
                        paginationEnabled(true);
                    }
                }
                return false;
            }
        });
    }

    private void incrementPageRequestValue(int step) {
        if (pageRequestValue < viewModel.getPageCount() - step) {
            pageRequestValue = pageRequestValue + step;
        } else
            pageRequestValue = viewModel.getPageCount();
        pageIndicator.setText(pageRequestValue + "/" + String.valueOf(viewModel.getPageCount()));
    }

    private void decrementPageRequestValue(int step) {
        if (pageRequestValue > step)
            pageRequestValue = pageRequestValue - step;
        else
            pageRequestValue = 1;
        pageIndicator.setText(pageRequestValue + "/" + String.valueOf(viewModel.getPageCount()));
    }

    //------------------------------------BOTTOM NAV BAR METHODS END------------------------------------

    @Override
    public void onTopicTaskStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void onTopicTaskCancelled() {
        progressBar.setVisibility(ProgressBar.GONE);
    }

    @Override
    public void onReplyTaskStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void onReplyTaskFinished(boolean success) {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        postsList.remove(postsList.size() - 1);
        topicAdapter.notifyItemRemoved(postsList.size());

        progressBar.setVisibility(ProgressBar.GONE);
        replyFAB.show();
        bottomNavBar.setVisibility(View.VISIBLE);
        viewModel.setWritingReply(false);

        if (success) {
            if ((postsList.get(postsList.size() - 1).getPostNumber() + 1) % 15 == 0) {
                viewModel.loadUrl(viewModel.getBaseUrl() + "." + 2147483647);
            } else {
                viewModel.reloadPage();
            }
        } else {
            Toast.makeText(TopicActivity.this, "Post failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPrepareForReplyStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void onPrepareForReplyCancelled() {
        progressBar.setVisibility(ProgressBar.GONE);
    }

    @Override
    public void onDeleteTaskStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void onDeleteTaskFinished(boolean result) {
        progressBar.setVisibility(ProgressBar.GONE);

        if (result) {
            viewModel.reloadPage();
        } else {
            Toast.makeText(TopicActivity.this, "Post deleted!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPrepareEditStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void onPrepareEditCancelled() {
        progressBar.setVisibility(ProgressBar.GONE);
    }

    @Override
    public void onEditTaskStarted() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
    }

    @Override
    public void onEditTaskFinished(boolean result, int position) {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

        postsList.get(position).setPostType(Post.TYPE_POST);
        topicAdapter.notifyItemChanged(position);
        viewModel.setEditingPost(false);
        progressBar.setVisibility(ProgressBar.GONE);
        replyFAB.show();
        bottomNavBar.setVisibility(View.VISIBLE);

        if (result) {
            viewModel.reloadPage();
        } else {
            Toast.makeText(TopicActivity.this, "Edit failed!", Toast.LENGTH_SHORT).show();
        }
    }
}