package gr.thmmy.mthmmy.activities.topic;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.activities.base.BaseActivity;
import gr.thmmy.mthmmy.data.Post;
import mthmmy.utils.Report;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.session.SessionManager.LOGGED_IN;
import static gr.thmmy.mthmmy.session.SessionManager.LOGIN_STATUS;

@SuppressWarnings("unchecked")
public class TopicActivity extends BaseActivity {

    //-----------------------------------------CLASS VARIABLES------------------------------------------
    private TopicTask topicTask;

    /* --Posts-- */
    private List<Post> postsList;
    static final int NO_POST_FOCUS = -1;
    static int postFocus = NO_POST_FOCUS;
    //Quote
    public static final ArrayList<Integer> toQuoteList = new ArrayList<>();
    /* --Topic's pages-- */
    private int thisPage = 1;
    public static String base_url = "";
    private int numberOfPages = 1;
    private final SparseArray<String> pagesUrls = new SparseArray<>();
    //Page select
    private TextView pageIndicator;
    private final Handler repeatUpdateHandler = new Handler();
    private final long INITIAL_DELAY = 500;
    private boolean autoIncrement = false;
    private boolean autoDecrement = false;
    private static final int SMALL_STEP = 1;
    private static final int LARGE_STEP = 10;
    private Integer pageRequestValue;

    private ImageButton firstPage;
    private ImageButton previousPage;
    private ImageButton nextPage;
    private ImageButton lastPage;

    //Other variables
    private ProgressBar progressBar;
    @SuppressWarnings("unused")
    private static final String TAG = "TopicActivity";
    private String topicTitle;
    private FloatingActionButton replyFAB;
    private String parsedTitle;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    static String PACKAGE_NAME;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        PACKAGE_NAME = getApplicationContext().getPackageName();

        Bundle extras = getIntent().getExtras();
        topicTitle = getIntent().getExtras().getString("TOPIC_TITLE");

        //Initialize toolbar, drawer and ProgressBar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(topicTitle);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        createDrawer();

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        postsList = new ArrayList<>();

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

        replyFAB = (FloatingActionButton) findViewById(R.id.topic_fab);
        replyFAB.setEnabled(false);

        replyFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
                int tmp_curr_status = sharedPrefs.getInt(LOGIN_STATUS, -1);
                if (tmp_curr_status == -1) {
                    new AlertDialog.Builder(TopicActivity.this)
                            .setTitle("ERROR!")
                            .setMessage("An error occurred while trying to find your LOGIN_STATUS.\n" +
                                    "Please sent below info to developers:\n"
                                    + getLocalClassName() + "." + "l"
                                    + Thread.currentThread().getStackTrace()[1].getLineNumber())
                            .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    //Todo
                                    //Maybe sent info back to developers?
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

        recyclerView = (RecyclerView) findViewById(R.id.topic_recycler_view);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(new TopicAdapter(getApplicationContext(), postsList));

        topicTask = new TopicTask();
        topicTask.execute(extras.getString("TOPIC_URL")); //Attempt data parsing
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
                //Clicked and holden
                autoDecrement = false; //Stop incrementing
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
        if (pageRequestValue >= 1000)
            pageIndicator.setTextSize(16);
        else
            pageIndicator.setTextSize(20);
    }

    private void decrementPageRequestValue(int step) {
        if (pageRequestValue > step)
            pageRequestValue = pageRequestValue - step;
        else
            pageRequestValue = 1;
        pageIndicator.setText(pageRequestValue + "/" + String.valueOf(numberOfPages));
        if (numberOfPages >= 1000)
            pageIndicator.setTextSize(16);
        else
            pageIndicator.setTextSize(20);
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

    //---------------------------------------TOPIC ASYNC TASK-------------------------------------------
    public class TopicTask extends AsyncTask<String, Void, Integer> {
        //Class variables
        private static final String TAG = "TopicTask"; //Separate tag for AsyncTask
        private static final int SUCCESS = 0;
        private static final int NETWORK_ERROR = 1;
        private static final int OTHER_ERROR = 2;

        //Show a progress bar until done
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
            replyFAB.setEnabled(false);
        }

        protected Integer doInBackground(String... strings) {
            Document document;
            base_url = strings[0].substring(0, strings[0].lastIndexOf(".")); //This topic's base url
            String pageUrl = strings[0]; //This page's url

            //Find message focus if present
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
                parse(document); //Parse data
                return SUCCESS;
            } catch (IOException e) {
                Report.i(TAG, "IO Exception", e);
                return NETWORK_ERROR;
            } catch (Exception e) {
                Report.e(TAG, "Exception", e);
                return OTHER_ERROR;
            }
        }

        protected void onPostExecute(Integer result) {
            switch (result) {
                case SUCCESS:
                    //Parse was successful
                    progressBar.setVisibility(ProgressBar.INVISIBLE); //Hide progress bar
                    populateLayout(); //Show parsed data

                    //Set current page
                    pageIndicator.setText(String.valueOf(thisPage) + "/" + String.valueOf(numberOfPages));
                    pageRequestValue = thisPage;
                    if (numberOfPages >= 1000)
                        pageIndicator.setTextSize(16);

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

        /* Parse method */
        private void parse(Document document) {
            String language = TopicParser.defineLanguage(document);

            //Find topic title if missing
            if (topicTitle == null || Objects.equals(topicTitle, "")) {
                parsedTitle = document.select("td[id=top_subject]").first().text();
                if (parsedTitle.contains("Topic:")) {
                    parsedTitle = parsedTitle.substring(parsedTitle.indexOf("Topic:") + 7
                            , parsedTitle.indexOf("(Read") - 2);
                } else {
                    parsedTitle = parsedTitle.substring(parsedTitle.indexOf("Θέμα:") + 6
                            , parsedTitle.indexOf("(Αναγνώστηκε") - 2);
                    Report.d(TAG, parsedTitle);
                }
            }

            { //Find current page's index
                thisPage = TopicParser.parseCurrentPageIndex(document, language);
            }
            { //Find number of pages
                numberOfPages = TopicParser.parseTopicNumberOfPages(document, thisPage, language);

                for (int i = 0; i < numberOfPages; i++) {
                    //Generate each page's url from topic's base url +".15*numberOfPage"
                    pagesUrls.put(i, base_url + "." + String.valueOf(i * 15));
                }
            }

            postsList = TopicParser.parseTopic(document, language);
        }
        /* Parse method end */
    }
//-------------------------------------TOPIC ASYNC TASK END-----------------------------------------

//----------------------------------------POPULATE UI METHOD----------------------------------------

    /**
     * This method runs on the main thread. It reads from the postsList and dynamically
     * adds a card for each post to the ScrollView.
     */
    private void populateLayout() {
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
    }

//--------------------------------------POPULATE UI METHOD END--------------------------------------

//----------------------------------------REPETITIVE UPDATER----------------------------------------

    /**
     * This class is used to implement the repetitive incrementPageRequestValue/decrementPageRequestValue of page value
     * when long pressing one of the page navigation buttons.
     */
    class RepetitiveUpdater implements Runnable {
        private final int step;

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
//--------------------------------------REPETITIVE UPDATER END--------------------------------------

//------------------------------METHODS FOR DOWNLOADING ATTACHED FILES------------------------------

    /**
     * Create a File
     */
    static void downloadFileAsync(final String downloadUrl, final String fileName, final Context context) {
        Request request = new Request.Builder().url(downloadUrl).build();

        getClient().newCall(request).enqueue(new Callback() {
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    throw new IOException("Failed to download file: " + response);
                }
                File tmpFile = getOutputMediaFile(PACKAGE_NAME, fileName);
                if (tmpFile == null) {
                    Report.d(TAG
                            , "Error creating media file, check storage permissions!");
                } else {
                    FileOutputStream fos = new FileOutputStream(tmpFile);
                    fos.write(response.body().bytes());
                    fos.close();

                    String filePath = tmpFile.getAbsolutePath();
                    String extension = MimeTypeMap.getFileExtensionFromUrl(
                            filePath.substring(filePath.lastIndexOf("/")));
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);

                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(tmpFile), mime);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        });
    }

    /**
     * Create a File
     */
    @Nullable
    private static File getOutputMediaFile(String packageName, String fileName) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + packageName
                + "/Downloads");

        // This location works best if you want the created files to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "problem!");
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return mediaFile;
    }

//----------------------------METHODS FOR DOWNLOADING ATTACHED FILES END----------------------------
}