package gr.thmmy.mthmmy.activities.topic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.data.Post;
import gr.thmmy.mthmmy.utils.CircleTransform;
import gr.thmmy.mthmmy.utils.FontManager;
import mthmmy.utils.Report;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.NO_POST_FOCUS;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.base_url;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.postFocus;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.toQuoteList;
import static gr.thmmy.mthmmy.utils.FontManager.FONTAWESOME;

class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.MyViewHolder> {
    private static final String TAG = "TopicAdapter";

    private static int THUMBNAIL_SIZE;
    private final Context context;
    private final List<Post> postsList;
    private boolean foundPostFocus = false;
    private ArrayList<boolean[]> viewProperties = new ArrayList<>();
    private static final int isPostDateAndNumberVisibile = 0;
    private static final int isUserExtraInfoVisibile = 1;
    private static final int isQuoteButtonChecked = 2;

    class MyViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final FrameLayout postDateAndNumberExp;
        final TextView postDate, postNum, username, subject;
        final ImageView thumbnail;
        final public WebView post;
        final ImageButton quoteToggle;
        final RelativeLayout header;
        final LinearLayout userExtraInfo;

        final TextView specialRank, rank, gender, numberOfPosts, personalText, stars;

        MyViewHolder(View view) {
            super(view);

            //Initialize layout's graphic elements
            //Basic stuff
            cardView = (CardView) view.findViewById(R.id.card_view);
            postDateAndNumberExp = (FrameLayout) view.findViewById(R.id.post_date_and_number_exp);
            postDate = (TextView) view.findViewById(R.id.post_date);
            postNum = (TextView) view.findViewById(R.id.post_number);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
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
            stars = (TextView) view.findViewById(R.id.stars);
        }

        /**
         * Possible cleanup needed (like so:)
         * https://stackoverflow.com/questions/24897441/picasso-how-to-cancel-all-image-requests-made-in-an-adapter
         * TODO
         */
    }


    TopicAdapter(Context context, List<Post> postsList) {
        this.context = context;
        this.postsList = postsList;

        THUMBNAIL_SIZE = (int) context.getResources().getDimension(R.dimen.thumbnail_size);
        for (int i = 0; i < postsList.size(); ++i) {
            //Initialize properties, array's values will be false by default
            viewProperties.add(new boolean[3]);
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_topic_post_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        final Post currentPost = postsList.get(position);

        //Post's WebView parameters set
        holder.post.setClickable(true);
        holder.post.setWebViewClient(new LinkLauncher());
        holder.post.getSettings().setJavaScriptEnabled(true);

        //Avoiding errors about layout having 0 width/height
        holder.thumbnail.setMinimumWidth(1);
        holder.thumbnail.setMinimumHeight(1);
        //Set thumbnail size
        holder.thumbnail.setMaxWidth(THUMBNAIL_SIZE);
        holder.thumbnail.setMaxHeight(THUMBNAIL_SIZE);

        //noinspection ConstantConditions
        Picasso.with(context)
                .load(currentPost.getThumbnailUrl())
                .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                .centerCrop()
                .error(ResourcesCompat.getDrawable(context.getResources()
                        , R.drawable.ic_default_user_thumbnail, null))
                .placeholder(ResourcesCompat.getDrawable(context.getResources()
                        , R.drawable.ic_default_user_thumbnail, null))
                .transform(new CircleTransform())
                .into(holder.thumbnail);

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
                        Report.i(TAG, "An error occurred while trying to exclude post from" +
                                "toQuoteList, post wasn't there!");
                } else {
                    toQuoteList.add(currentPost.getPostNumber());
                    view.setSelected(true);
                }
            }
        });

        //If user is not deleted then we have more to do
        if (!currentPost.isDeleted()) { //Set extra info
            //Variables with content
            String c_specialRank = currentPost.getSpecialRank(), c_rank = currentPost.getRank(), c_gender = currentPost.getGender(), c_numberOfPosts = currentPost.getNumberOfPosts(), c_personalText = currentPost.getPersonalText();
            int c_numberOfStars = currentPost.getNumberOfStars(), c_userColor = currentPost.getUserColor();

            if (!Objects.equals(c_specialRank, "") && c_specialRank != null) {
                holder.specialRank.setText(c_specialRank);
                holder.specialRank.setVisibility(View.VISIBLE);
            } else
                holder.specialRank.setVisibility(View.GONE);
            if (!Objects.equals(c_rank, "") && c_rank != null) {
                holder.rank.setText(c_rank);
                holder.rank.setVisibility(View.VISIBLE);
            } else
                holder.rank.setVisibility(View.GONE);
            if (!Objects.equals(c_gender, "") && c_gender != null) {
                holder.gender.setText(c_gender);
                holder.gender.setVisibility(View.VISIBLE);
            } else
                holder.gender.setVisibility(View.GONE);
            if (!Objects.equals(c_numberOfPosts, "") && c_numberOfPosts != null) {
                holder.numberOfPosts.setText(c_numberOfPosts);
                holder.numberOfPosts.setVisibility(View.VISIBLE);
            } else
                holder.numberOfPosts.setVisibility(View.GONE);
            if (!Objects.equals(c_personalText, "") && c_personalText != null) {
                holder.personalText.setText("\"" + c_personalText + "\"");
                holder.personalText.setVisibility(View.VISIBLE);
            } else
                holder.personalText.setVisibility(View.GONE);

            if (c_numberOfStars != 0) {
                holder.stars.setTypeface(FontManager.getTypeface(context, FONTAWESOME));

                String aStar = context.getResources().getString(R.string.fa_icon_star);
                String usersStars = "";
                for (int i = 0; i < c_numberOfStars; ++i) {
                    usersStars += aStar;
                }
                holder.stars.setText(usersStars);
                holder.stars.setTextColor(c_userColor);
                holder.stars.setVisibility(View.VISIBLE);
            } else
                holder.stars.setVisibility(View.GONE);

            /* --Header expand/collapse functionality-- */

            //Check if current post's header is expanded
            if (viewProperties.get(position)[isUserExtraInfoVisibile]) //Expanded
                holder.userExtraInfo.setVisibility(View.VISIBLE);
            else //Collapsed
                holder.userExtraInfo.setVisibility(View.GONE);

            holder.header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Change post's viewProperties accordingly
                    boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                    tmp[isUserExtraInfoVisibile] = !tmp[isUserExtraInfoVisibile];
                    viewProperties.set(holder.getAdapterPosition(), tmp);

                    TopicAnimations.animateUserExtraInfoVisibility(holder.userExtraInfo);
                }
            });

            //Clicking the expanded part of a header should collapse the extra info
            holder.userExtraInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Change post's viewProperties accordingly
                    boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                    tmp[1] = false;
                    viewProperties.set(holder.getAdapterPosition(), tmp);

                    TopicAnimations.animateUserExtraInfoVisibility(v);
                }
            });
            /* --Header expand/collapse functionality end-- */
        }

            /* --Card expand/collapse functionality-- */

        //Check if current post is expanded
        if (viewProperties.get(position)[isPostDateAndNumberVisibile]) { //Expanded
            holder.postDateAndNumberExp.setVisibility(View.VISIBLE);

            holder.username.setMaxLines(Integer.MAX_VALUE);
            holder.username.setEllipsize(null);

            holder.subject.setTextColor(Color.parseColor("#000000"));
            holder.subject.setMaxLines(Integer.MAX_VALUE);
            holder.subject.setEllipsize(null);
        } else { //Collapsed
            holder.postDateAndNumberExp.setVisibility(View.GONE);

            holder.username.setMaxLines(1);
            holder.username.setEllipsize(TextUtils.TruncateAt.END);

            holder.subject.setTextColor(Color.parseColor("#757575"));
            holder.subject.setMaxLines(1);
            holder.subject.setEllipsize(TextUtils.TruncateAt.END);
        }

        //Should expand/collapse when card is touched
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change post's viewProperties accordingly
                boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                tmp[isPostDateAndNumberVisibile] = !tmp[isPostDateAndNumberVisibile];
                viewProperties.set(holder.getAdapterPosition(), tmp);

                TopicAnimations.animatePostExtraInfoVisibility(holder.postDateAndNumberExp
                        , holder.username, holder.subject
                        , Color.parseColor("#000000")
                        , Color.parseColor("#757575"));
            }
        });

        //Also when post is clicked
        holder.post.setOnTouchListener(new CustomTouchListener(holder.post, holder.cardView, holder.quoteToggle));

        if (postFocus != NO_POST_FOCUS && !foundPostFocus) {
            if (currentPost.getPostIndex() == postFocus) {
                holder.cardView.requestFocus();
                foundPostFocus = true;
            }
        }
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

            Report.i(TAG, "Uri clicked = " + uri);
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