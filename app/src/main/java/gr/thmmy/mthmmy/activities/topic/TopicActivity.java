package gr.thmmy.mthmmy.activities.topic;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.ImageLoader;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.BaseActivity;
import gr.thmmy.mthmmy.activities.LoginActivity;
import gr.thmmy.mthmmy.data.Post;
import gr.thmmy.mthmmy.utils.CircularNetworkImageView;
import gr.thmmy.mthmmy.utils.ImageController;
import okhttp3.Request;
import okhttp3.Response;

import static gr.thmmy.mthmmy.session.SessionManager.LOGGED_IN;
import static gr.thmmy.mthmmy.session.SessionManager.LOGIN_STATUS;

@SuppressWarnings("unchecked")
public class TopicActivity extends BaseActivity {

//-----------------------------------------CLASS VARIABLES------------------------------------------

    /* --Posts-- */
    private List<Post> postsList;
    private LinearLayout postsLinearLayout;
    private static final int NO_POST_FOCUS = -1;
    private int postFocus = NO_POST_FOCUS;
    //Quote
    private final ArrayList<Integer> toQuoteList = new ArrayList<>();
    /* --Topic's pages-- */
    private int thisPage = 1;
    private String base_url = "";
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

    /* --Thumbnail-- */
    private static final int THUMBNAIL_SIZE = 80;
    private ImageLoader imageLoader = ImageController.getInstance().getImageLoader();

    //Other variables
    private ProgressBar progressBar;
    private static final String TAG = "TopicActivity";
    private String topicTitle;
    private String parsedTitle;
    private FloatingActionButton replyFAB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topic);

        Bundle extras = getIntent().getExtras();
        topicTitle = getIntent().getExtras().getString("TOPIC_TITLE");

        //Initialize toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(topicTitle);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        createDrawer();

        //Variables initialization
        postsLinearLayout = (LinearLayout) findViewById(R.id.posts_list);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if (imageLoader == null)
            imageLoader = ImageController.getInstance().getImageLoader();

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

        replyFAB = (FloatingActionButton) findViewById(R.id.fab);
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

        new TopicTask().execute(extras.getString("TOPIC_URL")); //Attempt data parsing
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
        super.onResume();
        drawer.setSelection(-1);
    }

    @Override
    protected void onDestroy() { //When finished cancel whatever request can still be canceled
        super.onDestroy();
        ImageController.getInstance().cancelPendingRequests();
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
            //Restart activity with new page
            Pair<View, String> p1 = Pair.create((View)replyFAB, "fab");
            Pair<View, String> p2 = Pair.create((View)toolbar, "toolbar");
            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, p1, p2);

            Intent intent = getIntent();
            intent.putExtra("TOPIC_URL", pagesUrls.get(pageRequested));
            intent.putExtra("TOPIC_TITLE", topicTitle);
            startActivity(intent, options.toBundle());
            finish();
        }
    }
//------------------------------------BOTTOM NAV BAR METHODS END------------------------------------

//---------------------------------------TOPIC ASYNC TASK-------------------------------------------
    public class TopicTask extends AsyncTask<String, Void, Boolean> {
        //Class variables
        private static final String TAG = "TopicTask"; //Separate tag for AsyncTask

        //Show a progress bar until done
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        protected Boolean doInBackground(String... strings) {
            Document document;
            base_url = strings[0].substring(0, strings[0].lastIndexOf(".")); //This topic's base url
            String pageUrl = strings[0]; //This page's url

            //Find message focus if present
            {
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
                return true;
            } catch (SSLHandshakeException e) {
                Log.w(TAG, "Certificate problem (please switch to unsafe connection).");
            } catch (Exception e) {
                Log.e("TAG", "ERROR", e);
            }
            return false;
        }

        protected void onPostExecute(Boolean result) {
            if (!result) { //Parse failed!
                //Should never happen
                Toast.makeText(getBaseContext()
                        , "Fatal error!\n Aborting...", Toast.LENGTH_LONG).show();
                finish();
            }
            //Parse was successful
            progressBar.setVisibility(ProgressBar.INVISIBLE); //Hide progress bar
            populateLayout(); //Show parsed data
            //Set current page
            pageIndicator.setText(String.valueOf(thisPage) + "/" + String.valueOf(numberOfPages));
            pageRequestValue = thisPage;
            if (numberOfPages >= 1000)
                pageIndicator.setTextSize(16);
        }

        /* Parse method */
        private void parse(Document document) {
            //Find topic title if missing
            if (topicTitle == null || Objects.equals(topicTitle, "")) {
                parsedTitle = document.select("td[id=top_subject]").first().text();
                parsedTitle = parsedTitle.substring(parsedTitle.indexOf("Topic:") + 7
                        , parsedTitle.indexOf("(Read") - 8);
            }

            { //Find current page's index
                thisPage = TopicParser.parseCurrentPageIndex(document);
            }
            { //Find number of pages
                numberOfPages = TopicParser.parseTopicNumberOfPages(document, thisPage);

                for (int i = 0; i < numberOfPages; i++) {
                    //Generate each page's url from topic's base url +".15*numberOfPage"
                    pagesUrls.put(i, base_url + "." + String.valueOf(i * 15));
                }
            }

            postsList = TopicParser.parseTopic(document);
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
        //Enable reply button
        replyFAB.setEnabled(true);

        //Set topic title if not already present
        if (topicTitle == null || Objects.equals(topicTitle, "")) {
            topicTitle = parsedTitle;
            if (toolbar != null) {
                toolbar.setTitle(topicTitle);
            }
        }

        //Now that parsing is complete and we have the url for every page enable page nav buttons
        firstPage.setEnabled(true);
        previousPage.setEnabled(true);
        nextPage.setEnabled(true);
        lastPage.setEnabled(true);

        //Initialize an inflater
        LayoutInflater inflater = (LayoutInflater) getApplicationContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Create a card for each post
        for (final Post currentPost : postsList) {
            //Inflate a topic post row layout
            View convertView = inflater.inflate(R.layout.activity_topic_post_row
                    , postsLinearLayout, false);

            //Get an ImageLoader instance
            if (imageLoader == null)
                imageLoader = ImageController.getInstance().getImageLoader();

            //Initialize layout's graphic elements
            //Basic stuff
            final CardView cardView = (CardView) convertView.findViewById(R.id.card_view);
            final FrameLayout postDateAndNumberExp = (FrameLayout) convertView.findViewById(R.id.post_date_and_number_exp);
            TextView postDate = (TextView) convertView.findViewById(R.id.post_date);
            TextView postNum = (TextView) convertView.findViewById(R.id.post_number);
            CircularNetworkImageView thumbnail = (CircularNetworkImageView) convertView.findViewById(R.id.thumbnail);
            final TextView username = (TextView) convertView.findViewById(R.id.username);
            final TextView subject = (TextView) convertView.findViewById(R.id.subject);
            final WebView post = (WebView) convertView.findViewById(R.id.post);
            final ImageButton quoteToggle = (ImageButton) convertView.findViewById(R.id.toggle_quote_button);

            //User's extra
            RelativeLayout header = (RelativeLayout) convertView.findViewById(R.id.header);
            final LinearLayout userExtraInfo = (LinearLayout) convertView.findViewById(R.id.user_extra_info);

            //Post's WebView parameters set
            post.setClickable(true);
            post.setWebViewClient(new LinkLauncher());
            post.getSettings().setJavaScriptEnabled(true);
            //TODO
            post.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);



            //Avoiding errors about layout having 0 width/height
            thumbnail.setMinimumWidth(1);
            thumbnail.setMinimumHeight(1);
            //Set thumbnail size
            thumbnail.setMaxWidth(THUMBNAIL_SIZE);
            thumbnail.setMaxHeight(THUMBNAIL_SIZE);

            //Thumbnail image set
            if (currentPost.getThumbnailUrl() != null
                    && !Objects.equals(currentPost.getThumbnailUrl(), "")) {
                thumbnail.setImageUrl(currentPost.getThumbnailUrl(), imageLoader);
            }

            //Username set
            username.setText(currentPost.getAuthor());

            //Post's submit date set
            postDate.setText(currentPost.getPostDate());

            //Post's index number set
            if (currentPost.getPostNumber() != 0)
                postNum.setText(getString(R.string.user_number_of_posts, currentPost.getPostNumber()));
                //postNum.setText("#" + currentPost.getPostNumber());
            else
                postNum.setText("");

            //Post's subject set
            subject.setText(currentPost.getSubject());

            //Post's text set
            post.loadDataWithBaseURL("file:///android_asset/", currentPost.getContent(), "text/html", "UTF-8", null);

            quoteToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (view.isSelected()) {
                        if (toQuoteList.contains(currentPost.getPostNumber())) {
                            toQuoteList.remove(toQuoteList.indexOf(currentPost.getPostNumber()));
                            view.setSelected(false);
                        } else
                            Log.i(TAG, "An error occurred while trying to exclude post from" +
                                    "toQuoteList, post wasn't there!");
                    } else {
                        toQuoteList.add(currentPost.getPostNumber());
                        view.setSelected(true);
                    }
                }
            });

            //If user is not deleted then we have more to do
            if (!currentPost.isDeleted()) { //Set extra info
                //Variables for Graphics
                TextView g_specialRank, g_rank, g_gender, g_numberOfPosts, g_personalText;
                LinearLayout g_stars_holder = (LinearLayout) convertView.findViewById(R.id.stars);

                //Variables for content
                String c_specialRank = currentPost.getSpecialRank(), c_rank = currentPost.getRank(), c_gender = currentPost.getGender(), c_numberOfPosts = currentPost.getNumberOfPosts(), c_personalText = currentPost.getPersonalText(), c_urlOfStars = currentPost.getUrlOfStars();
                int c_numberOfStars = currentPost.getNumberOfStars();

                if (!Objects.equals(c_specialRank, "") && c_specialRank != null) {
                    g_specialRank = (TextView) convertView.findViewById(R.id.special_rank);
                    g_specialRank.setText(c_specialRank);
                    g_specialRank.setVisibility(View.VISIBLE);
                }
                if (!Objects.equals(c_rank, "") && c_rank != null) {
                    g_rank = (TextView) convertView.findViewById(R.id.rank);
                    g_rank.setText(c_rank);
                    g_rank.setVisibility(View.VISIBLE);
                }
                if (!Objects.equals(c_gender, "") && c_gender != null) {
                    g_gender = (TextView) convertView.findViewById(R.id.gender);
                    g_gender.setText(c_gender);
                    g_gender.setVisibility(View.VISIBLE);
                }
                if (!Objects.equals(c_numberOfPosts, "") && c_numberOfPosts != null) {
                    g_numberOfPosts = (TextView) convertView.findViewById(R.id.number_of_posts);
                    g_numberOfPosts.setText(c_numberOfPosts);
                    g_numberOfPosts.setVisibility(View.VISIBLE);
                }
                if (!Objects.equals(c_personalText, "") && c_personalText != null) {
                    g_personalText = (TextView) convertView.findViewById(R.id.personal_text);
                    g_personalText.setText("\"" + c_personalText + "\"");
                    g_personalText.setVisibility(View.VISIBLE);
                }
                for (int i = 0; i < c_numberOfStars; ++i) {
                    CircularNetworkImageView star = new CircularNetworkImageView(this);
                    star.setImageUrl(c_urlOfStars, imageLoader);

                    //Remove spacing between stars...
                    //Don't know why this is happening in the first place
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    params.setMargins((int) getResources().getDimension(R.dimen.stars_margin)
                            , 0
                            , (int) getResources().getDimension(R.dimen.stars_margin)
                            , 0);
                    star.setLayoutParams(params);

                    g_stars_holder.addView(star, 0);
                    g_stars_holder.setVisibility(View.VISIBLE);
                }

                /* --Header expand/collapse functionality-- */

                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TopicAnimations.animateUserExtraInfoVisibility(userExtraInfo);
                    }
                });

                //Clicking the expanded part of a header should collapse the extra info
                userExtraInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TopicAnimations.animateUserExtraInfoVisibility(v);
                    }
                });
                /* --Header expand/collapse functionality end-- */
            }

            /* --Card expand/collapse functionality-- */

            //Should expand/collapse when card is touched
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TopicAnimations.animatePostExtraInfoVisibility(postDateAndNumberExp
                            , username, subject
                            ,ContextCompat.getColor(getApplicationContext(), R.color.black)
                            ,ContextCompat.getColor(getApplicationContext(), R.color.secondary_text));
                }
            });

            //Also when post is clicked
            post.setOnTouchListener(new CustomTouchListener(post,cardView, quoteToggle));

            /* --Card expand/collapse-like functionality end-- */

            //Add view to the linear layout that holds all posts
            postsLinearLayout.addView(convertView);

            //Set post focus
            if (postFocus != NO_POST_FOCUS) {
                if (currentPost.getPostIndex() == postFocus) {
                    //TODO
                }
            }
        }
    }
//--------------------------------------POPULATE UI METHOD END--------------------------------------

//--------------------------------------CUSTOM WEBVIEW CLIENT---------------------------------------

    /**
     * This class is used to handle link clicks in WebViews.
     * When link url is one that the app can handle internally, it does.
     * Otherwise user is prompt to open the link in a browser.
     */
    @SuppressWarnings("unchecked")
    private class LinkLauncher extends WebViewClient { //Used to handle link clicks
        //Older versions
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Uri uri = Uri.parse(url);
            return handleUri(uri);
        }

        //Newest versions
        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            return handleUri(uri);
        }

        //Handle url clicks
        private boolean handleUri(final Uri uri) {
            //Method always returns true as we don't want any url to be loaded in WebViews

            Log.i(TAG, "Uri = " + uri);
            final String host = uri.getHost(); //Get requested url's host
            final String uriString = uri.toString();

            //Determine if you are going to pass the url to a
            //host's application activity or load it in a browser.
            if (Objects.equals(host, "www.thmmy.gr")) {
                //This is my web site, so figure out what Activity should launch
                if (uriString.contains("topic=")) { //This url points to a topic
                    //Is the link pointing to current topic?
                    if (Objects.equals(
                            uriString.substring(0, uriString.lastIndexOf(".")), base_url)) {

                        //Get uri's targeted message's index number
                        String msgIndexReq = uriString.substring(uriString.indexOf("msg") + 3);
                        if (msgIndexReq.contains("#"))
                            msgIndexReq = msgIndexReq.substring(0, msgIndexReq.indexOf("#"));
                        else
                            msgIndexReq = msgIndexReq.substring(0, msgIndexReq.indexOf(";"));

                        //Is this post already shown now? (is it in current page?)
                        for (Post post : postsList) {
                            if (post.getPostIndex() == Integer.parseInt(msgIndexReq)) {
                                //Don't restart Activity
                                //Just change post focus
                                //TODO
                                return true;
                            }
                        }
                    }
                    //Restart activity with new data
                    Pair<View, String> p1 = Pair.create((View) replyFAB, "fab");
                    Pair<View, String> p2 = Pair.create((View) toolbar, "toolbar");
                    ActivityOptionsCompat options = ActivityOptionsCompat.
                            makeSceneTransitionAnimation(TopicActivity.this, p1, p2);

                    Intent intent = getIntent();
                    intent.putExtra("TOPIC_URL", uri.toString());
                    intent.putExtra("TOPIC_TITLE", "");
                    startActivity(intent, options.toBundle());
                    finish();

                }
                return true;
            }
            //Otherwise, the link is not for a page on my site, so launch
            //another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
            return true;
        }
    }
//------------------------------------CUSTOM WEBVIEW CLIENT END-------------------------------------

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

//--------------------------------------CUSTOM TOUCH LISTENER---------------------------------------
    /**
     * This class is a gesture detector for WebViews.
     * It handles post's clicks, long clicks and touch and drag.
     */

    private class CustomTouchListener implements View.OnTouchListener {
        //Long press handling
        private final int LONG_PRESS_DURATION = 650;
        private final Handler webViewLongClickHandler = new Handler();
        private boolean wasLongClick = false;
        private float downCoordinateX;
        private float downCoordinateY;
        private final float SCROLL_THRESHOLD = 7;
        final private WebView post;
        final private CardView cardView;
        final private ImageButton quoteToggle;

        //Other variables
        final static int FINGER_RELEASED = 0;
        final static int FINGER_TOUCHED = 1;
        final static int FINGER_DRAGGING = 2;
        final static int FINGER_UNDEFINED = 3;

        private int fingerState = FINGER_RELEASED;

        CustomTouchListener(WebView pPost, CardView pCard, ImageButton pQuoteToggle){
            post = pPost;
            cardView = pCard;
            quoteToggle = pQuoteToggle;
        }

        final Runnable WebViewLongClick = new Runnable() {
            public void run() {
                wasLongClick = true;
                quoteToggle.performClick();
            }
        };

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {

            switch (motionEvent.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    downCoordinateX = motionEvent.getX();
                    downCoordinateY = motionEvent.getY();
                    if (fingerState == FINGER_RELEASED)
                        fingerState = FINGER_TOUCHED;
                    else
                        fingerState = FINGER_UNDEFINED;
                    //Start long click runnable
                    webViewLongClickHandler.postDelayed(WebViewLongClick
                            , LONG_PRESS_DURATION);
                    break;

                case MotionEvent.ACTION_UP:
                    webViewLongClickHandler.removeCallbacks(WebViewLongClick);

                    if (!wasLongClick && fingerState != FINGER_DRAGGING) {
                        //If this was a link don't expand the card
                        WebView.HitTestResult htResult = post.getHitTestResult();
                        if (htResult.getExtra() != null
                                && htResult.getExtra() != null)
                            return false;
                        //Else expand/collapse card
                        cardView.performClick();
                    } else
                        wasLongClick = false;
                    fingerState = FINGER_RELEASED;
                    break;

                case MotionEvent.ACTION_MOVE:
                    //If finger moved too much, cancel long click
                    if (((Math.abs(downCoordinateX - motionEvent.getX()) > SCROLL_THRESHOLD ||
                            Math.abs(downCoordinateY - motionEvent.getY()) > SCROLL_THRESHOLD))) {
                        webViewLongClickHandler.removeCallbacks(WebViewLongClick);
                        fingerState = FINGER_DRAGGING;
                    } else fingerState = FINGER_UNDEFINED;
                    break;

                default:
                    fingerState = FINGER_UNDEFINED;

            }
            return false;
        }
    }
//------------------------------------CUSTOM TOUCH LISTENER END-------------------------------------
}