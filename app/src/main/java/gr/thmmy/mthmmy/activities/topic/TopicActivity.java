package gr.thmmy.mthmmy.activities.topic;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.LinkTarget;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.utils.ParseHelpers;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

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
    private static final int PERMISSIONS_REQUEST_CODE = 69;
    private static TopicTask topicTask;
    //About posts
    private TopicAdapter topicAdapter;
    private ArrayList<Post> postsList;
    private static final int NO_POST_FOCUS = -1;
    private static int postFocus = NO_POST_FOCUS;
    private static int postFocusPosition = 0;
    //Quotes
    public static final ArrayList<Integer> toQuoteList = new ArrayList<>();
    //Topic's pages
    private int thisPage = 1;
    public static String base_url = "";
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
    private ImageButton firstPage;
    private ImageButton previousPage;
    private TextView pageIndicator;
    private ImageButton nextPage;
    private ImageButton lastPage;
    //Other variables
    private MaterialProgressBar progressBar;
    private String topicTitle;
    private FloatingActionButton replyFAB;
    private String parsedTitle;
    private RecyclerView recyclerView;
    private String loadedPageUrl = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);
        requestPerms();

        Bundle extras = getIntent().getExtras();
        topicTitle = extras.getString(BUNDLE_TOPIC_TITLE);
        LinkTarget.Target target = LinkTarget.resolveLinkTarget(
                Uri.parse(extras.getString(BUNDLE_TOPIC_URL)));
        if (!target.is(LinkTarget.Target.TOPIC)) {
            Report.e(TAG, "Bundle came with a non topic url!\nUrl:\n" + extras.getString(BUNDLE_TOPIC_URL));
            Toast.makeText(this, "An error has occurred\n Aborting.", Toast.LENGTH_SHORT).show();
            finish();
        }

        //Initializes graphics
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(topicTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();

        progressBar = (MaterialProgressBar) findViewById(R.id.progressBar);

        postsList = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.topic_recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        topicAdapter = new TopicAdapter(getApplicationContext(), progressBar, postsList,
                topicTask);
        recyclerView.setAdapter(topicAdapter);

        replyFAB = (FloatingActionButton) findViewById(R.id.topic_fab);
        replyFAB.setEnabled(false);
        if (!sessionManager.isLoggedIn()) replyFAB.hide();
        else {
            replyFAB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (sessionManager.isLoggedIn()) {
                        //TODO Reply
                    } else {
                        new AlertDialog.Builder(TopicActivity.this)
                                .setMessage("You need to be logged in to reply!")
                                .setPositiveButton("Login", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(TopicActivity.this, LoginActivity.class);
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

    /*@Override
    public void onRequestPermissionsResult(int permsRequestCode, @NonNull String[] permissions
            , @NonNull int[] grantResults) {
        switch (permsRequestCode) {
            case PERMISSIONS_REQUEST_CODE:
                readWriteAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                break;
        }
    }*/

    boolean requestPerms() { //Runtime permissions request for devices with API >= 23
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
            String[] PERMISSIONS_STORAGE = {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE};

            if (checkSelfPermission(PERMISSIONS_STORAGE[0]) == PackageManager.PERMISSION_DENIED ||
                    checkSelfPermission(PERMISSIONS_STORAGE[1]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(PERMISSIONS_STORAGE, PERMISSIONS_REQUEST_CODE);
                return false;
            } else return true;
        } else return true;
    }

    //--------------------------------------BOTTOM NAV BAR METHODS----------------------------------
    private void paginationEnabled(boolean enabled) {
        firstPage.setEnabled(enabled);
        previousPage.setEnabled(enabled);
        nextPage.setEnabled(enabled);
        lastPage.setEnabled(enabled);
    }

    private void initIncrementButton(ImageButton increment, final int step) {
        // Increment once for a click
        increment.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!autoIncrement && step == LARGE_STEP) { //If just clicked go to last page
                    changePage(numberOfPages - 1);
                    return;
                }
                //Clicked and holden
                autoIncrement = false; //Stop incrementing
                incrementPageRequestValue(step);
                changePage(pageRequestValue - 1);
            }
        });

        // Auto increment for a long click
        increment.setOnLongClickListener(
                new View.OnLongClickListener() {
                    public boolean onLongClick(View arg0) {
                        autoIncrement = true;
                        repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), INITIAL_DELAY);
                        return false;
                    }
                }
        );

        // When the button is released
        increment.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && autoIncrement) {
                    changePage(pageRequestValue - 1);
                }
                return false;
            }
        });
    }

    private void initDecrementButton(ImageButton decrement, final int step) {
        // Decrement once for a click
        decrement.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!autoDecrement && step == LARGE_STEP) { //If just clicked go to first page
                    changePage(0);
                    return;
                }
                //Clicked and hold
                autoDecrement = false; //Stop decrementing
                decrementPageRequestValue(step);
                changePage(pageRequestValue - 1);
            }
        });


        // Auto decrement for a long click
        decrement.setOnLongClickListener(
                new View.OnLongClickListener() {
                    public boolean onLongClick(View arg0) {
                        autoDecrement = true;
                        repeatUpdateHandler.postDelayed(new RepetitiveUpdater(step), INITIAL_DELAY);
                        return false;
                    }
                }
        );

        // When the button is released
        decrement.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP && autoDecrement) {
                    changePage(pageRequestValue - 1);
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
            base_url = strings[0].substring(0, strings[0].lastIndexOf(".")); //This topic's base url
            String newPageUrl = strings[0];

            //Finds the index of message focus if present
            {
                postFocus = NO_POST_FOCUS;
                if (newPageUrl.contains("msg")) {
                    String tmp = newPageUrl.substring(newPageUrl.indexOf("msg") + 3);
                    if (tmp.contains(";"))
                        postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf(";")));
                    else
                        postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf("#")));
                }
            }

            //Checks if the page to be loaded is the one already shown
            if (!Objects.equals(loadedPageUrl, "") && !loadedPageUrl.contains(base_url)) {
                if (newPageUrl.contains("topicseen#new"))
                    if (Integer.parseInt(loadedPageUrl.substring(base_url.length())) == numberOfPages)
                        return SAME_PAGE;
                if (Objects.equals(loadedPageUrl.substring(base_url.length())
                        , newPageUrl.substring(base_url.length())))
                    return SAME_PAGE;
            }

            loadedPageUrl = newPageUrl;
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
                    progressBar.setVisibility(ProgressBar.INVISIBLE);
                    topicAdapter.customNotifyDataSetChanged(new TopicTask());
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
            //postsList = TopicParser.parseTopic(topic, language);
        }
    }

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
}