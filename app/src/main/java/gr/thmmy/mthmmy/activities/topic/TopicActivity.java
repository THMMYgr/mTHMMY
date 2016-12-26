package gr.thmmy.mthmmy.activities.topic;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
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
import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.base.BaseActivity;
import gr.thmmy.mthmmy.data.Post;
import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

import static gr.thmmy.mthmmy.session.SessionManager.LOGGED_IN;
import static gr.thmmy.mthmmy.session.SessionManager.LOGIN_STATUS;

/**
 * Activity for topics. When creating an Intent of this activity you need to bundle a <b>String</b>
 * containing this topics's url using the key {@link #EXTRAS_TOPIC_URL} and a <b>String</b> containing
 * this topic's title using the key {@link #EXTRAS_TOPIC_TITLE}.
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
    public static final String EXTRAS_TOPIC_URL = "TOPIC_URL";
    /**
     * The key to use when putting topic's title String to {@link TopicActivity}'s Bundle.
     */
    public static final String EXTRAS_TOPIC_TITLE = "TOPIC_TITLE";
    static String PACKAGE_NAME;
    private TopicTask topicTask;
    //About posts
    private List<Post> postsList;
    static final int NO_POST_FOCUS = -1;
    static int postFocus = NO_POST_FOCUS;
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
    private ProgressBar progressBar;
    private String topicTitle;
    private FloatingActionButton replyFAB;
    private String parsedTitle;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        PACKAGE_NAME = getApplicationContext().getPackageName();
        Bundle extras = getIntent().getExtras();
        topicTitle = extras.getString("TOPIC_TITLE");

        //Initializes graphics
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(topicTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        postsList  = new ArrayList<>();

        recyclerView = (RecyclerView) findViewById(R.id.topic_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new TopicAdapter(getApplicationContext(), postsList));

        replyFAB = (FloatingActionButton) findViewById(R.id.topic_fab);
        replyFAB.setEnabled(false);
        replyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
                int tmp_curr_status = sharedPrefs.getInt(LOGIN_STATUS, -1);
                if (tmp_curr_status == -1) {
                    Report.e(TAG, "Error while getting LOGIN_STATUS");
                    new AlertDialog.Builder(TopicActivity.this)
                            .setTitle("ERROR!")
                            .setMessage("An error occurred while trying to find your LOGIN_STATUS.")
                            .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                }
                            })
                            .show();
                } else if (tmp_curr_status != LOGGED_IN) {
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
                } else {
                    //TODO
                    //Reply
                }
            }
        });

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

        firstPage.setEnabled(false);
        previousPage.setEnabled(false);
        nextPage.setEnabled(false);
        lastPage.setEnabled(false);

        //Gets posts
        topicTask = new TopicTask();
        topicTask.execute(extras.getString(EXTRAS_TOPIC_URL)); //Attempt data parsing
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
        if (topicTask != null && topicTask.getStatus() != AsyncTask.Status.RUNNING)
            topicTask.cancel(true);
    }


    //--------------------------------------BOTTOM NAV BAR METHODS--------------------------------------
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
     * <p>Calling ProfileTask's {@link AsyncTask#execute execute} method needs to have profile's url
     * as String parameter!</p>
     */
    public class TopicTask extends AsyncTask<String, Void, Integer> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        private static final String TAG = "TopicTask"; //Separate tag for AsyncTask
        private static final int SUCCESS = 0;
        private static final int NETWORK_ERROR = 1;
        private static final int OTHER_ERROR = 2;

        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            replyFAB.setEnabled(false);
        }

        protected Integer doInBackground(String... strings) {
            Document document;
            base_url = strings[0].substring(0, strings[0].lastIndexOf(".")); //This topic's base url
            String pageUrl = strings[0];

            //Finds message focus if present
            {
                postFocus = NO_POST_FOCUS;
                if (pageUrl.contains("msg")) {
                    String tmp = pageUrl.substring(pageUrl.indexOf("msg") + 3);
                    if (tmp.contains(";"))
                        postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf(";")));
                    else
                        postFocus = Integer.parseInt(tmp.substring(0, tmp.indexOf("#")));
                }
            }

            Request request = new Request.Builder()
                    .url(pageUrl)
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
            switch (parseResult) {
                case SUCCESS:
                    progressBar.setVisibility(ProgressBar.INVISIBLE);

                    recyclerView.swapAdapter(new TopicAdapter(getApplicationContext(), postsList), false);
                    //Set post focus
                    if (postFocus != NO_POST_FOCUS) {
                        for (int i = postsList.size() - 1; i >= 0; --i) {
                            int currentPostIndex = postsList.get(i).getPostIndex();
                            if (currentPostIndex == postFocus) {
                                layoutManager.scrollToPosition(i);
                            }
                        }
                    }
                    replyFAB.setEnabled(true);

                    //Set current page
                    pageIndicator.setText(String.valueOf(thisPage) + "/" + String.valueOf(numberOfPages));
                    pageRequestValue = thisPage;

                    firstPage.setEnabled(true);
                    previousPage.setEnabled(true);
                    nextPage.setEnabled(true);
                    lastPage.setEnabled(true);

                    if (topicTitle == null || Objects.equals(topicTitle, ""))
                        toolbar.setTitle(parsedTitle);
                    break;
                case NETWORK_ERROR:
                    Toast.makeText(getBaseContext(), "Network Error", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    //Parse failed - should never happen
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
            String language = TopicParser.defineLanguage(topic);

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

            postsList = TopicParser.parseTopic(topic, language);
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