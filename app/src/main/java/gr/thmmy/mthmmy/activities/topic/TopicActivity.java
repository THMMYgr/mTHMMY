package gr.thmmy.mthmmy.activities.topic;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.topic.tasks.EditTask;
import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForEditTask;
import gr.thmmy.mthmmy.activities.topic.tasks.PrepareForReplyTask;
import gr.thmmy.mthmmy.activities.topic.tasks.ReplyTask;
import gr.thmmy.mthmmy.activities.topic.tasks.TopicTask;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.Bookmark;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.model.TopicItem;
import gr.thmmy.mthmmy.utils.HTMLUtils;
import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import gr.thmmy.mthmmy.viewmodel.TopicViewModel;
import gr.thmmy.mthmmy.views.CustomLinearLayoutManager;
import gr.thmmy.mthmmy.views.editorview.EmojiKeyboard;
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
public class TopicActivity extends BaseActivity implements TopicAdapter.OnPostFocusChangeListener {
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
    private CustomLinearLayoutManager layoutManager;
    private RecyclerView recyclerView;
    //Posts related
    private TopicAdapter topicAdapter;
    /**
     * Holds a list of this topic's posts
     */
    private ArrayList<TopicItem> topicItems;
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

    //Bottom navigation bar graphics related
    private LinearLayout bottomNavBar;
    private ImageButton firstPage;
    private ImageButton previousPage;
    private TextView pageIndicator;
    private ImageButton nextPage;
    private ImageButton lastPage;
    private Snackbar snackbar;
    private TopicViewModel viewModel;
    private EmojiKeyboard emojiKeyboard;
    private AlertDialog topicInfoDialog;

    //Fix for vector drawables on android <21
    static {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        // get TopicViewModel instance
        viewModel = ViewModelProviders.of(this).get(TopicViewModel.class);
        subscribeUI();

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
        //TODO if topicTitle provided is null make bookmark button unclickable until title is fetched (also for BoardActivity)
        thisPageBookmark = new Bookmark(topicTitle, ThmmyPage.getTopicId(topicPageUrl), true);

        //Initializes graphics
        toolbar = findViewById(R.id.toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        toolbarTitle.setSingleLine(true);
        toolbarTitle.setEllipsize(TextUtils.TruncateAt.MARQUEE);
        toolbarTitle.setMarqueeRepeatLimit(-1);
        toolbarTitle.setText(topicTitle);
        toolbarTitle.setSelected(true);
        this.setToolbarOnLongClickListener(topicPageUrl);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();

        progressBar = findViewById(R.id.progressBar);
        emojiKeyboard = findViewById(R.id.emoji_keyboard);

        topicItems = new ArrayList<>();

        recyclerView = findViewById(R.id.topic_recycler_view);
        recyclerView.setHasFixedSize(true);
        //LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager = new CustomLinearLayoutManager(
                getApplicationContext(), topicPageUrl);

        recyclerView.setLayoutManager(layoutManager);
        topicAdapter = new TopicAdapter(this, emojiKeyboard, topicItems);
        recyclerView.setAdapter(topicAdapter);
        recyclerView.setItemViewCacheSize(17);  //Every page has maximum 15 posts + Poll + EditorView

        replyFAB = findViewById(R.id.topic_fab);
        replyFAB.hide();
        replyFAB.setTag(false);
        bottomNavBar = findViewById(R.id.bottom_navigation_bar);
        if (!sessionManager.isLoggedIn()) {
            replyFAB.hide();
            replyFAB.setTag(false);
        } else {
            replyFAB.setOnClickListener(view -> {
                if (sessionManager.isLoggedIn())
                    viewModel.prepareForReply();
            });
            replyFAB.setTag(true);
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

        Timber.i("Starting initial topic load");
        viewModel.loadUrl(topicPageUrl);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyleAccent);
                LayoutInflater inflater = this.getLayoutInflater();
                LinearLayout infoDialog = (LinearLayout) inflater.inflate(R.layout.dialog_topic_info
                        , null);
                TextView treeAndMods = infoDialog.findViewById(R.id.topic_tree_and_mods);
                treeAndMods.setText(new SpannableStringBuilder("Loading..."));
                treeAndMods.setMovementMethod(LinkMovementMethod.getInstance());
                TextView usersViewing = infoDialog.findViewById(R.id.users_viewing);
                usersViewing.setText(new SpannableStringBuilder("Loading..."));
                usersViewing.setMovementMethod(LinkMovementMethod.getInstance());
                viewModel.getTopicTreeAndMods().observe(this, topicTreeAndMods -> {
                    if (topicTreeAndMods == null) return;
                    treeAndMods.setText(HTMLUtils.getSpannableFromHtml(this, topicTreeAndMods));
                });
                viewModel.getTopicViewers().observe(this, topicViewers -> {
                    if (topicViewers == null) return;
                    usersViewing.setText(HTMLUtils.getSpannableFromHtml(this, topicViewers));
                });
                builder.setView(infoDialog);
                topicInfoDialog = builder.create();
                topicInfoDialog.show();
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
        } else if (emojiKeyboard.getVisibility() == View.VISIBLE) {
            emojiKeyboard.setVisibility(View.GONE);
            return;
        } else if (viewModel.isWritingReply()) {
            // persist reply
            SharedPreferences drafts = getSharedPreferences(getString(R.string.pref_topic_drafts_key), MODE_PRIVATE);
            Post reply = (Post) topicItems.get(topicItems.size() - 1);
            drafts.edit().putString(String.valueOf(viewModel.getTopicId()), reply.getBbContent()).apply();

            topicItems.remove(topicItems.size() - 1);
            topicAdapter.notifyItemRemoved(topicItems.size());
            topicAdapter.setBackButtonHidden();
            viewModel.setWritingReply(false);
            replyFAB.show();
            replyFAB.setTag(true);
            bottomNavBar.setVisibility(View.VISIBLE);
            return;
        } else if (viewModel.isEditingPost()) {
            ((Post) topicItems.get(viewModel.getPostBeingEditedPosition())).setPostType(Post.TYPE_POST);
            topicAdapter.notifyItemChanged(viewModel.getPostBeingEditedPosition());
            topicAdapter.setBackButtonHidden();
            viewModel.setEditingPost(false);
            replyFAB.show();
            replyFAB.setTag(true);
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
        if(topicInfoDialog!=null){
            topicInfoDialog.dismiss();
            topicInfoDialog=null;
        }
        recyclerView.setAdapter(null);
        viewModel.stopLoading();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // persist reply
        if (viewModel.isWritingReply()) {
            SharedPreferences drafts = getSharedPreferences(getString(R.string.pref_topic_drafts_key), MODE_PRIVATE);
            Post reply = (Post) topicItems.get(topicItems.size() - 1);
            drafts.edit().putString(String.valueOf(viewModel.getTopicId()), reply.getBbContent()).apply();
        }
    }

    @Override
    public void onPostFocusChange(int position) {
        recyclerView.scrollToPosition(position);
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
                viewModel.incrementPageRequestValue(step, false);
                repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), REPEAT_DELAY);
            } else if (autoDecrement) {
                viewModel.decrementPageRequestValue(step, false);
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
                viewModel.setPageIndicatorIndex(viewModel.getPageCount(), true);
            } else if (!autoIncrement) {
                viewModel.incrementPageRequestValue(step, true);
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
                    viewModel.loadPageIndicated();
                } else if (rect != null && event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (!rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        autoIncrement = false;
                        viewModel.setPageIndicatorIndex(viewModel.getCurrentPageIndex(), false);
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
                viewModel.setPageIndicatorIndex(1, true);
            } else if (!autoDecrement) {
                viewModel.decrementPageRequestValue(step, true);
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
                    viewModel.loadPageIndicated();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (rect != null &&
                            !rect.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())) {
                        autoIncrement = false;
                        viewModel.setPageIndicatorIndex(viewModel.getCurrentPageIndex(), false);
                        paginationEnabled(true);
                    }
                }
                return false;
            }
        });
    }

    //------------------------------------BOTTOM NAV BAR METHODS END------------------------------------

    /**
     * Binds the UI to its data
     */
    private void subscribeUI() {
        // Implement async task callbacks
        viewModel.setTopicTaskObserver(new TopicTask.TopicTaskObserver() {
            @Override
            public void onTopicTaskStarted() {
                progressBar.setVisibility(ProgressBar.VISIBLE);
                if (snackbar != null) snackbar.dismiss();
            }

            @Override
            public void onTopicTaskCancelled() {
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });
        viewModel.setDeleteTaskStartedListener(() -> progressBar.setVisibility(ProgressBar.VISIBLE));
        viewModel.setDeleteTaskFinishedListener((resultCode, data) -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (resultCode == NetworkResultCodes.SUCCESSFUL) {
                Timber.i("Post deleted successfully");
                viewModel.reloadPage();
            } else {
                Timber.w("Failed to delete post");
                Toast.makeText(getBaseContext(), "Delete failed!", Toast.LENGTH_SHORT).show();
            }
        });
        viewModel.setReplyFinishListener(new ReplyTask.ReplyTaskCallbacks() {
            @Override
            public void onReplyTaskStarted() {
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }

            @Override
            public void onReplyTaskFinished(Posting.REPLY_STATUS replyStatus) {
                View view = getCurrentFocus();
                if (view != null) {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }

                progressBar.setVisibility(ProgressBar.GONE);

                switch (replyStatus) {
                    case SUCCESSFUL:
                        BaseApplication.getInstance().logFirebaseAnalyticsEvent("post_creation", null);
                        Timber.i("Post reply successful");
                        replyFAB.show();
                        replyFAB.setTag(true);
                        bottomNavBar.setVisibility(View.VISIBLE);
                        viewModel.setWritingReply(false);

                        SharedPreferences drafts = getSharedPreferences(getString(R.string.pref_topic_drafts_key),
                                Context.MODE_PRIVATE);
                        drafts.edit().remove(String.valueOf(viewModel.getTopicId())).apply();

                        if ((((Post) topicItems.get(topicItems.size() - 1)).getPostNumber() + 1) % 15 == 0) {
                            Timber.i("Reply was posted in new page. Switching to last page.");
                            viewModel.loadUrl(ParseHelpers.getBaseURL(viewModel.getTopicUrl()) + "." + 2147483647);
                        } else {
                            viewModel.reloadPage();
                        }
                        break;
                    case NEW_REPLY_WHILE_POSTING:
                        Timber.i("New reply while writing a reply");

                        //cache reply
                        if (viewModel.isWritingReply()) {
                            SharedPreferences drafts2 = getSharedPreferences(getString(R.string.pref_topic_drafts_key), MODE_PRIVATE);
                            Post reply = (Post) topicItems.get(topicItems.size() - 1);
                            drafts2.edit().putString(String.valueOf(viewModel.getTopicId()), reply.getBbContent()).apply();
                            viewModel.setWritingReply(false);
                        }

                        Runnable addReply = () -> {
                            AlertDialog.Builder builder = new AlertDialog.Builder(TopicActivity.this,
                                    R.style.AppTheme_Dark_Dialog);
                            builder.setMessage("A new reply was posted before you completed your new post." +
                                    " Please review it and send your reply again")
                                    .setNeutralButton(getString(R.string.ok), (dialog, which) -> dialog.dismiss())
                                    .show();
                            viewModel.prepareForReply();
                        };
                        viewModel.resetPageThen(addReply);
                        break;
                    default:
                        Timber.w("Post reply unsuccessful");
                        Toast.makeText(getBaseContext(), "Post failed!", Toast.LENGTH_SHORT).show();
                        recyclerView.getChildAt(recyclerView.getChildCount() - 1).setAlpha(1f);
                        recyclerView.getChildAt(recyclerView.getChildCount() - 1).setEnabled(true);
                }
            }
        });
        viewModel.setPrepareForEditCallbacks(new PrepareForEditTask.PrepareForEditCallbacks() {
            @Override
            public void onPrepareEditStarted() {
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }

            @Override
            public void onPrepareEditCancelled() {
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });
        viewModel.setEditTaskCallbacks(new EditTask.EditTaskCallbacks() {
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

                progressBar.setVisibility(ProgressBar.GONE);

                if (result) {
                    Timber.i("Post edit successful");
                    ((Post) topicItems.get(position)).setPostType(Post.TYPE_POST);
                    topicAdapter.notifyItemChanged(position);
                    replyFAB.show();
                    replyFAB.setTag(true);
                    bottomNavBar.setVisibility(View.VISIBLE);
                    viewModel.setEditingPost(false);
                    viewModel.reloadPage();
                } else {
                    Timber.i("Post edit unsuccessful");
                    Toast.makeText(getBaseContext(), "Edit failed!", Toast.LENGTH_SHORT).show();
                    recyclerView.getChildAt(viewModel.getPostBeingEditedPosition()).setAlpha(1);
                    recyclerView.getChildAt(viewModel.getPostBeingEditedPosition()).setEnabled(true);
                }
            }
        });
        viewModel.setPrepareForReplyCallbacks(new PrepareForReplyTask.PrepareForReplyCallbacks() {
            @Override
            public void onPrepareForReplyStarted() {
                progressBar.setVisibility(ProgressBar.VISIBLE);
            }

            @Override
            public void onPrepareForReplyCancelled() {
                progressBar.setVisibility(ProgressBar.GONE);
            }
        });
        viewModel.setVoteTaskStartedListener(() -> progressBar.setVisibility(ProgressBar.VISIBLE));
        viewModel.setVoteTaskFinishedListener((resultCode, data) -> {
            progressBar.setVisibility(View.GONE);
            if (resultCode == NetworkResultCodes.SUCCESSFUL) {
                Timber.i("Vote sent");
                viewModel.resetPage();
            }
            else {
                Timber.w("Failed to send vote");
                Toast.makeText(this, "Failed to send vote", Toast.LENGTH_LONG).show();
            }
        });
        viewModel.setRemoveVoteTaskStartedListener(() -> progressBar.setVisibility(ProgressBar.VISIBLE));
        viewModel.setRemoveVoteTaskFinishedListener((resultCode, data) -> {
            progressBar.setVisibility(View.GONE);
            if (resultCode == NetworkResultCodes.SUCCESSFUL) {
                Timber.i("Vote removed");
                viewModel.resetPage();
            }
            else {
                Timber.w("Failed to remove vote");
                Toast.makeText(this, "Failed to remove vote", Toast.LENGTH_LONG).show();
            }
        });
        // observe the changes in data
        viewModel.getPageIndicatorIndex().observe(this, pageIndicatorIndex -> {
            if (pageIndicatorIndex == null) return;
            pageIndicator.setText(pageIndicatorIndex + "/" +
                    viewModel.getPageCount());
        });
        viewModel.getTopicTitle().observe(this, newTopicTitle -> {
            if (newTopicTitle == null) return;
            if (!TextUtils.equals(toolbarTitle.getText(), newTopicTitle)) {
                thisPageBookmark = new Bookmark(newTopicTitle, thisPageBookmark.getId(), thisPageBookmark.isNotificationsEnabled());
                toolbarTitle.setText(newTopicTitle);
            }
        });
        viewModel.getPageTopicId().observe(this, pageTopicId -> {
            if (pageTopicId == null) return;
            if (viewModel.getCurrentPageIndex() == viewModel.getPageCount()) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null)
                    notificationManager.cancel(NEW_POST_TAG, pageTopicId);
            }
        });
        viewModel.getReplyPageUrl().observe(this, replyPageUrl -> {
            if (replyPageUrl == null) {
                replyFAB.hide();
                replyFAB.setTag(false);
            } else {
                replyFAB.show();
                replyFAB.setTag(true);
            }
        });
        viewModel.getTopicItems().observe(this, postList -> {
            if (postList == null)
                progressBar.setVisibility(ProgressBar.VISIBLE);
            recyclerView.getRecycledViewPool().clear(); //Avoid inconsistency detected bug
            topicItems.clear();

            /* A workaround to avoid automatic scrolling when a new page
            page is loaded (it happens sometimes only)*/
            recyclerView.setAdapter(topicAdapter);

            if (postList != null) {
                topicItems.addAll(postList);
                topicAdapter.notifyDataSetChanged();
            }
        });
        /*viewModel.getFocusedPostIndex().observe(this, focusedPostIndex -> {
            if (focusedPostIndex == null) return;
            recyclerView.scrollToPosition(focusedPostIndex);
        });*/
        viewModel.getTopicTaskResultCode().observe(this, resultCode -> {
            if (resultCode == null) return;
            progressBar.setVisibility(ProgressBar.GONE);
            switch (resultCode) {
                case SUCCESS:
                    Timber.i("Successfully loaded a topic");
                    paginationEnabled(true);
                    break;
                case NETWORK_ERROR:
                    Timber.w("Network error on loaded page");
                    if (viewModel.getTopicItems().getValue() == null) {
                        // no page has been loaded yet. Give user the ability to refresh
                        recyclerView.setVisibility(View.GONE);
                        TextView errorTextview = findViewById(R.id.error_textview);

                        Spannable errorText = new SpannableString(getString(R.string.network_error_retry_prompt));
                        errorText.setSpan(
                                new ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.accent, null)),
                                errorText.toString().indexOf("Tap to retry"),
                                errorText.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        errorTextview.setText(errorText);
                        errorTextview.setVisibility(View.VISIBLE);
                        errorTextview.setOnClickListener(view -> {
                            viewModel.reloadPage();
                            errorTextview.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        });
                    } else {
                        // a page has already been loaded
                        viewModel.setPageIndicatorIndex(viewModel.getCurrentPageIndex(), false);
                        snackbar = Snackbar.make(findViewById(R.id.main_content),
                                R.string.generic_network_error, Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, view -> viewModel.reloadPage());
                        snackbar.show();
                    }
                    break;
                case UNAUTHORIZED:
                    Timber.w("Requested topic was unauthorized");
                    recyclerView.setVisibility(View.GONE);
                    TextView errorTextview = findViewById(R.id.error_textview);

                    Spannable errorText = new SpannableString(getString(R.string.unauthorized_topic_error));
                    errorText.setSpan(
                            //TODO: maybe change the color to a red in order to indicate the error nature of the message
                            new ForegroundColorSpan(ResourcesCompat.getColor(getResources(), R.color.accent, null)),
                            0,
                            errorText.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    errorTextview.setText(getString(R.string.unauthorized_topic_error));
                    errorTextview.setVisibility(View.VISIBLE);
                    break;
                default:
                    //Parse failed - should never happen
                    Timber.wtf("Parse failed!");  //TODO report ParseException!!!
                    Toast.makeText(getBaseContext(), "Fatal Error", Toast.LENGTH_SHORT).show();
                    finish();
                    break;
            }
        });
        viewModel.getPrepareForReplyResult().observe(this, prepareForReplyResult -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (prepareForReplyResult != null && prepareForReplyResult.isSuccessful()) {
                Timber.i("Prepare for reply successful");
                //prepare for a reply
                viewModel.setWritingReply(true);
                topicItems.add(Post.newQuickReply());
                topicAdapter.notifyItemInserted(topicItems.size());
                recyclerView.scrollToPosition(topicItems.size() - 1);
                replyFAB.hide();
                replyFAB.setTag(false);
                bottomNavBar.setVisibility(View.GONE);
            } else {
                Timber.i("Prepare for reply unsuccessful");
                Snackbar.make(findViewById(R.id.main_content), getString(R.string.generic_network_error), Snackbar.LENGTH_SHORT).show();
            }
        });
        viewModel.getPrepareForEditResult().observe(this, result -> {
            progressBar.setVisibility(ProgressBar.GONE);
            if (result != null && result.isSuccessful()) {
                Timber.i("Prepare for edit successful");
                viewModel.setEditingPost(true);
                ((Post) topicItems.get(result.getPosition())).setPostType(Post.TYPE_EDIT);
                topicAdapter.notifyItemChanged(result.getPosition());
                recyclerView.scrollToPosition(result.getPosition());
                replyFAB.hide();
                replyFAB.setTag(false);
                bottomNavBar.setVisibility(View.GONE);
            } else {
                Timber.i("Prepare for edit unsuccessful");
                Snackbar.make(findViewById(R.id.main_content), getString(R.string.generic_network_error), Snackbar.LENGTH_SHORT).show();
            }
        });
    }

    /**This method sets a long click listener on the title of the topic. Once the
     * listener gets triggered, it copies the link url of the topic in the clipboard.
     * This method is getting called on the onCreate() of the TopicActivity*/
    void setToolbarOnLongClickListener(String url) {
        toolbar.setOnLongClickListener(view -> {
            //Try to set the clipboard text
            try {
                //Create a ClipboardManager
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

                clipboard.setPrimaryClip(ClipData.newPlainText(BUNDLE_TOPIC_URL, url));

                //Make a toast to inform the user that the url was copied
                Toast.makeText(
                        TopicActivity.this,
                        TopicActivity.this.getString(R.string.link_copied_msg),
                        Toast.LENGTH_SHORT).show();
            }
            //Something happened. Probably the device does not support this (report to Firebase)
            catch (NullPointerException e) {
                Timber.e(e, "Error while trying to copy topic's url.");
            }

            return true;
        });
    }
}