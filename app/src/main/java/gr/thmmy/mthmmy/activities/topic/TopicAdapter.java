package gr.thmmy.mthmmy.activities.topic;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.toolbox.ImageLoader;

import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.data.Post;
import gr.thmmy.mthmmy.utils.CircularNetworkImageView;
import gr.thmmy.mthmmy.utils.ImageController;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.base_url;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.toQuoteList;

class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.MyViewHolder> {
    private static final String TAG = "TopicAdapter";

    private static final int THUMBNAIL_SIZE = 80;
    private ImageLoader imageLoader = ImageController.getInstance().getImageLoader();
    private Context context;
    private List<Post> postsList;

    class MyViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        FrameLayout postDateAndNumberExp;
        TextView postDate, postNum, username, subject;
        CircularNetworkImageView thumbnail;
        public WebView post;
        ImageButton quoteToggle;
        RelativeLayout header;
        LinearLayout userExtraInfo;

        TextView specialRank, rank, gender, numberOfPosts, personalText;
        LinearLayout stars_holder;

        MyViewHolder(View view) {
            super(view);

            //Initialize layout's graphic elements
            //Basic stuff
            cardView = (CardView) view.findViewById(R.id.card_view);
            postDateAndNumberExp = (FrameLayout) view.findViewById(R.id.post_date_and_number_exp);
            postDate = (TextView) view.findViewById(R.id.post_date);
            postNum = (TextView) view.findViewById(R.id.post_number);
            thumbnail = (CircularNetworkImageView) view.findViewById(R.id.thumbnail);
            username = (TextView) view.findViewById(R.id.username);
            subject = (TextView) view.findViewById(R.id.subject);
            post = (WebView) view.findViewById(R.id.post);
            quoteToggle = (ImageButton) view.findViewById(R.id.toggle_quote_button);

            //User's extra
            header = (RelativeLayout) view.findViewById(R.id.header);
            userExtraInfo = (LinearLayout) view.findViewById(R.id.user_extra_info);
            specialRank = (TextView) view.findViewById(R.id.special_rank);
            rank = (TextView) view.findViewById(R.id.rank);
            gender = (TextView) view.findViewById(R.id.gender);
            numberOfPosts = (TextView) view.findViewById(R.id.number_of_posts);
            personalText = (TextView) view.findViewById(R.id.personal_text);
            stars_holder = (LinearLayout) view.findViewById(R.id.stars);

        }
    }


    TopicAdapter(Context context, List<Post> postsList) {
        this.context = context;
        this.postsList = postsList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_topic_post_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Post currentPost = postsList.get(position);

        //Post's WebView parameters set
        holder.post.setClickable(true);
        holder.post.setWebViewClient(new LinkLauncher());
        holder.post.getSettings().setJavaScriptEnabled(true);
        holder.post.getSettings().setPluginState(WebSettings.PluginState.ON_DEMAND);

        //Avoiding errors about layout having 0 width/height
        holder.thumbnail.setMinimumWidth(1);
        holder.thumbnail.setMinimumHeight(1);
        //Set thumbnail size
        holder.thumbnail.setMaxWidth(THUMBNAIL_SIZE);
        holder.thumbnail.setMaxHeight(THUMBNAIL_SIZE);

        //Thumbnail image set
        if (currentPost.getThumbnailUrl() != null
                && !Objects.equals(currentPost.getThumbnailUrl(), "")) {
            holder.thumbnail.setImageUrl(currentPost.getThumbnailUrl(), imageLoader);
        }

        //Username set
        holder.username.setText(currentPost.getAuthor());

        //Post's submit date set
        holder.postDate.setText(currentPost.getPostDate());

        //Post's index number set
        if (currentPost.getPostNumber() != 0)
            holder.postNum.setText(context.getString(
                    R.string.user_number_of_posts, currentPost.getPostNumber()));
        else
            holder.postNum.setText("");

        //Post's subject set
        holder.subject.setText(currentPost.getSubject());

        //Post's text set
        holder.post.loadDataWithBaseURL("file:///android_asset/", currentPost.getContent(), "text/html", "UTF-8", null);

        holder.quoteToggle.setOnClickListener(new View.OnClickListener() {
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
            //Variables for content
            String c_specialRank = currentPost.getSpecialRank()
                    , c_rank = currentPost.getRank()
                    , c_gender = currentPost.getGender()
                    , c_numberOfPosts = currentPost.getNumberOfPosts()
                    , c_personalText = currentPost.getPersonalText()
                    , c_urlOfStars = currentPost.getUrlOfStars();
            int c_numberOfStars = currentPost.getNumberOfStars();

            if (!Objects.equals(c_specialRank, "") && c_specialRank != null) {
                holder.specialRank.setText(c_specialRank);
                holder.specialRank.setVisibility(View.VISIBLE);
            }
            if (!Objects.equals(c_rank, "") && c_rank != null) {
                holder.rank.setText(c_rank);
                holder.rank.setVisibility(View.VISIBLE);
            }
            if (!Objects.equals(c_gender, "") && c_gender != null) {
                holder.gender.setText(c_gender);
                holder.gender.setVisibility(View.VISIBLE);
            }
            if (!Objects.equals(c_numberOfPosts, "") && c_numberOfPosts != null) {
                holder.numberOfPosts.setText(c_numberOfPosts);
                holder.numberOfPosts.setVisibility(View.VISIBLE);
            }
            if (!Objects.equals(c_personalText, "") && c_personalText != null) {
                holder.personalText.setText("\"" + c_personalText + "\"");
                holder.personalText.setVisibility(View.VISIBLE);
            }

            for (int i = 0; i < c_numberOfStars; ++i) {
                CircularNetworkImageView star = new CircularNetworkImageView(context);
                star.setImageUrl(c_urlOfStars, imageLoader);

                //Remove spacing between stars...
                //Don't know why this is happening in the first place
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins((int) context.getResources().getDimension(R.dimen.stars_margin)
                        , 0
                        , (int) context.getResources().getDimension(R.dimen.stars_margin)
                        , 0);
                star.setLayoutParams(params);

                holder.stars_holder.addView(star, 0);
                holder.stars_holder.setVisibility(View.VISIBLE);
            }

                /* --Header expand/collapse functionality-- */

            holder.header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TopicAnimations.animateUserExtraInfoVisibility(holder.userExtraInfo);
                }
            });

            //Clicking the expanded part of a header should collapse the extra info
            holder.userExtraInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TopicAnimations.animateUserExtraInfoVisibility(v);
                }
            });
                /* --Header expand/collapse functionality end-- */
        }

            /* --Card expand/collapse functionality-- */

        //Should expand/collapse when card is touched
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TopicAnimations.animatePostExtraInfoVisibility(holder.postDateAndNumberExp
                        , holder.username, holder.subject
                        , Color.parseColor("#000000")
                        , Color.parseColor("#757575"));
            }
        });

        //Also when post is clicked
        holder.post.setOnTouchListener(new CustomTouchListener(holder.post, holder.cardView, holder.quoteToggle));
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

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

        CustomTouchListener(WebView pPost, CardView pCard, ImageButton pQuoteToggle) {
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
                    //TODO
                }
                return true;
            }
            //Otherwise, the link is not for a page on my site, so launch
            //another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return true;
        }
    }
//------------------------------------CUSTOM WEBVIEW CLIENT END-------------------------------------

}