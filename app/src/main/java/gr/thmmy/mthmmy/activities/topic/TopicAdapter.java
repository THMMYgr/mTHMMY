package gr.thmmy.mthmmy.activities.topic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.model.LinkTarget;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.utils.CircleTransform;
import gr.thmmy.mthmmy.utils.FileManager.ThmmyFile;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import mthmmy.utils.Report;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_USERNAME;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.base_url;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.toQuoteList;

/**
 * Custom {@link android.support.v7.widget.RecyclerView.Adapter} used for topics.
 */
class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.MyViewHolder> {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    private static final String TAG = "TopicAdapter";
    /**
     * Int that holds thumbnail's size defined in R.dimen
     */
    private static int THUMBNAIL_SIZE;
    private final Context context;
    private final List<Post> postsList;
    /**
     * Used to hold the state of visibility and other attributes for views that are animated or
     * otherwise changed. Used in combination with {@link #isPostDateAndNumberVisibile},
     * {@link #isUserExtraInfoVisibile} and {@link #isQuoteButtonChecked}.
     */
    private final ArrayList<boolean[]> viewProperties = new ArrayList<>();
    /**
     * Index of state indicator in the boolean array. If true post is expanded and post's date and
     * number are visible.
     */
    private static final int isPostDateAndNumberVisibile = 0;
    /**
     * Index of state indicator in the boolean array. If true user's extra info are expanded and
     * visible.
     */
    private static final int isUserExtraInfoVisibile = 1;
    /**
     * Index of state indicator in the boolean array. If true quote button for this post is checked.
     */
    private static final int isQuoteButtonChecked = 2;
    //private final MaterialProgressBar progressBar;
    private DownloadTask downloadTask;
    private TopicActivity.TopicTask topicTask;

    /**
     * Custom {@link RecyclerView.ViewHolder} implementation
     */
    class MyViewHolder extends RecyclerView.ViewHolder {
        final CardView cardView;
        final LinearLayout cardChildLinear;
        final FrameLayout postDateAndNumberExp;
        final TextView postDate, postNum, username, subject;
        final ImageView thumbnail;
        final public WebView post;
        final ImageButton quoteToggle;
        final RelativeLayout header;
        final LinearLayout userExtraInfo;
        final View bodyFooterDivider;
        final LinearLayout postFooter;

        final TextView specialRank, rank, gender, numberOfPosts, personalText, stars;

        MyViewHolder(View view) {
            super(view);
            //Initializes layout's graphic elements
            //Standard stuff
            cardView = (CardView) view.findViewById(R.id.card_view);
            cardChildLinear = (LinearLayout) view.findViewById(R.id.card_child_linear);
            postDateAndNumberExp = (FrameLayout) view.findViewById(R.id.post_date_and_number_exp);
            postDate = (TextView) view.findViewById(R.id.post_date);
            postNum = (TextView) view.findViewById(R.id.post_number);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);
            username = (TextView) view.findViewById(R.id.username);
            subject = (TextView) view.findViewById(R.id.subject);
            post = (WebView) view.findViewById(R.id.post);
            post.setBackgroundColor(Color.argb(1, 255, 255, 255));
            quoteToggle = (ImageButton) view.findViewById(R.id.toggle_quote_button);
            bodyFooterDivider = view.findViewById(R.id.body_footer_divider);
            postFooter = (LinearLayout) view.findViewById(R.id.post_footer);

            //User's extra info
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
         * Cancels all pending Picasso requests
         */
        void cleanup() {
            Picasso.with(context).cancelRequest(thumbnail);
            thumbnail.setImageDrawable(null);
        }
    }

    /**
     * @param context   the context of the {@link RecyclerView}
     * @param postsList List of {@link Post} objects to use
     */
    TopicAdapter(Context context, MaterialProgressBar progressBar, List<Post> postsList,
                 TopicActivity.TopicTask topicTask) {
        this.context = context;
        this.postsList = postsList;

        THUMBNAIL_SIZE = (int) context.getResources().getDimension(R.dimen.thumbnail_size);
        for (int i = 0; i < postsList.size(); ++i) {
            //Initializes properties, array's values will be false by default
            viewProperties.add(new boolean[3]);
        }
        //this.progressBar = progressBar;
        downloadTask = new DownloadTask();
        this.topicTask = topicTask;
    }

    @Override
    public void onViewRecycled(final MyViewHolder holder) {
        holder.cleanup();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_topic_post_row, parent, false);
        return new MyViewHolder(itemView);
    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        final Post currentPost = postsList.get(position);

        //Post's WebView parameters
        holder.post.setClickable(true);
        holder.post.setWebViewClient(new LinkLauncher());

        //Avoids errors about layout having 0 width/height
        holder.thumbnail.setMinimumWidth(1);
        holder.thumbnail.setMinimumHeight(1);
        //Sets thumbnail size
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

        //Sets username,submit date, index number, subject, post's and attached files texts
        holder.username.setText(currentPost.getAuthor());
        holder.postDate.setText(currentPost.getPostDate());
        if (currentPost.getPostNumber() != 0)
            holder.postNum.setText(context.getString(
                    R.string.user_number_of_posts, currentPost.getPostNumber()));
        else
            holder.postNum.setText("");
        holder.subject.setText(currentPost.getSubject());
        holder.post.loadDataWithBaseURL("file:///android_asset/", currentPost.getContent(), "text/html", "UTF-8", null);
        if (currentPost.getAttachedFiles() != null && currentPost.getAttachedFiles().size() != 0) {
            holder.bodyFooterDivider.setVisibility(View.VISIBLE);
            int filesTextColor;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                filesTextColor = context.getResources().getColor(R.color.accent, null);
            } else //noinspection deprecation
                filesTextColor = context.getResources().getColor(R.color.accent);

            for (final ThmmyFile attachedFile : currentPost.getAttachedFiles()) {
                final TextView attached = new TextView(context);
                attached.setTextSize(10f);
                attached.setClickable(true);
                attached.setTypeface(Typeface.createFromAsset(context.getAssets()
                        , "fonts/fontawesome-webfont.ttf"));
                attached.setText(faIconFromFilename(attachedFile.getFilename()) + " "
                        + attachedFile.getFilename() + attachedFile.getFileInfo());
                attached.setTextColor(filesTextColor);
                attached.setPadding(0, 3, 0, 3);

                attached.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (((TopicActivity) context).requestPerms()) {
                            downloadTask = new DownloadTask();
                            downloadTask.execute(attachedFile);
                        } else
                            Toast.makeText(context, "Persmissions missing!", Toast.LENGTH_SHORT)
                                    .show();
                    }
                });

                holder.postFooter.addView(attached);
            }
        } else {
            holder.bodyFooterDivider.setVisibility(View.GONE);
            holder.postFooter.removeAllViews();
        }

        if (!currentPost.isDeleted()) { //Sets user's extra info
            String mSpecialRank = currentPost.getSpecialRank(), mRank = currentPost.getRank(), mGender = currentPost.getGender(), mNumberOfPosts = currentPost.getNumberOfPosts(), mPersonalText = currentPost.getPersonalText();
            int mNumberOfStars = currentPost.getNumberOfStars(), mUserColor = currentPost.getUserColor();

            if (!Objects.equals(mSpecialRank, "") && mSpecialRank != null) {
                holder.specialRank.setText(mSpecialRank);
                holder.specialRank.setVisibility(View.VISIBLE);
            } else
                holder.specialRank.setVisibility(View.GONE);
            if (!Objects.equals(mRank, "") && mRank != null) {
                holder.rank.setText(mRank);
                holder.rank.setVisibility(View.VISIBLE);
            } else
                holder.rank.setVisibility(View.GONE);
            if (!Objects.equals(mGender, "") && mGender != null) {
                holder.gender.setText(mGender);
                holder.gender.setVisibility(View.VISIBLE);
            } else
                holder.gender.setVisibility(View.GONE);
            if (!Objects.equals(mNumberOfPosts, "") && mNumberOfPosts != null) {
                holder.numberOfPosts.setText(mNumberOfPosts);
                holder.numberOfPosts.setVisibility(View.VISIBLE);
            } else
                holder.numberOfPosts.setVisibility(View.GONE);
            if (!Objects.equals(mPersonalText, "") && mPersonalText != null) {
                holder.personalText.setText("\"" + mPersonalText + "\"");
                holder.personalText.setVisibility(View.VISIBLE);
            } else
                holder.personalText.setVisibility(View.GONE);
            if (mNumberOfStars > 0) {
                holder.stars.setTypeface(Typeface.createFromAsset(context.getAssets()
                        , "fonts/fontawesome-webfont.ttf"));

                String aStar = context.getResources().getString(R.string.fa_icon_star);
                String usersStars = "";
                for (int i = 0; i < mNumberOfStars; ++i) {
                    usersStars += aStar;
                }
                holder.stars.setText(usersStars);
                holder.stars.setTextColor(mUserColor);
                holder.stars.setVisibility(View.VISIBLE);
            } else
                holder.stars.setVisibility(View.GONE);
            //Special card for special member of the month!
            if (mUserColor == TopicParser.USER_COLOR_PINK) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    holder.cardChildLinear.setBackground(context.getResources().
                            getDrawable(R.drawable.member_of_the_month_card, null));
                } else //noinspection deprecation
                    holder.cardChildLinear.setBackground(context.getResources().
                            getDrawable(R.drawable.member_of_the_month_card));
            } else holder.cardChildLinear.setBackground(null);

            //Avoid's view's visibility recycling
            if (viewProperties.get(position)[isUserExtraInfoVisibile]) {
                holder.userExtraInfo.setVisibility(View.VISIBLE);
                holder.userExtraInfo.setAlpha(1.0f);
            } else {
                holder.userExtraInfo.setVisibility(View.GONE);
                holder.userExtraInfo.setAlpha(0.0f);
            }
            //Sets graphics behavior
            holder.header.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Clicking an expanded header starts profile activity
                    if (viewProperties.get(holder.getAdapterPosition())[isUserExtraInfoVisibile]) {
                        Intent intent = new Intent(context, ProfileActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(BUNDLE_PROFILE_URL, currentPost.getProfileURL());
                        if (currentPost.getThumbnailUrl() == null)
                            extras.putString(BUNDLE_THUMBNAIL_URL, "");
                        else
                            extras.putString(BUNDLE_THUMBNAIL_URL, currentPost.getThumbnailUrl());
                        extras.putString(BUNDLE_USERNAME, currentPost.getAuthor());
                        intent.putExtras(extras);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }

                    boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                    tmp[isUserExtraInfoVisibile] = !tmp[isUserExtraInfoVisibile];
                    viewProperties.set(holder.getAdapterPosition(), tmp);
                    TopicAnimations.animateUserExtraInfoVisibility(holder.userExtraInfo);
                }
            });
            //Clicking the expanded part of a header (the extra info) makes it collapse
            holder.userExtraInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                    tmp[1] = false;
                    viewProperties.set(holder.getAdapterPosition(), tmp);

                    TopicAnimations.animateUserExtraInfoVisibility(v);
                }
            });
        }//End of deleted profiles
        //Avoid's view's visibility recycling
        if (viewProperties.get(position)[isPostDateAndNumberVisibile]) { //Expanded
            holder.postDateAndNumberExp.setVisibility(View.VISIBLE);
            holder.postDateAndNumberExp.setAlpha(1.0f);
            holder.postDateAndNumberExp.setTranslationY(0);

            holder.username.setMaxLines(Integer.MAX_VALUE);
            holder.username.setEllipsize(null);

            holder.subject.setTextColor(Color.parseColor("#FFFFFF"));
            holder.subject.setMaxLines(Integer.MAX_VALUE);
            holder.subject.setEllipsize(null);
        } else { //Collapsed
            holder.postDateAndNumberExp.setVisibility(View.GONE);
            holder.postDateAndNumberExp.setAlpha(0.0f);
            holder.postDateAndNumberExp.setTranslationY(holder.postDateAndNumberExp.getHeight());

            holder.username.setMaxLines(1);
            holder.username.setEllipsize(TextUtils.TruncateAt.END);

            holder.subject.setTextColor(Color.parseColor("#757575"));
            holder.subject.setMaxLines(1);
            holder.subject.setEllipsize(TextUtils.TruncateAt.END);
        }
        if (viewProperties.get(position)[isQuoteButtonChecked])
            holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_checked);
        else
            holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_unchecked);
        //Sets graphics behavior
        holder.quoteToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                if (tmp[isQuoteButtonChecked]) {
                    if (toQuoteList.contains(currentPost.getPostNumber())) {
                        toQuoteList.remove(toQuoteList.indexOf(currentPost.getPostNumber()));
                    } else
                        Report.i(TAG, "An error occurred while trying to exclude post from" +
                                "toQuoteList, post wasn't there!");
                    holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_unchecked);
                } else {
                    toQuoteList.add(currentPost.getPostNumber());
                    holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_checked);
                }
                tmp[isQuoteButtonChecked] = !tmp[isQuoteButtonChecked];
                viewProperties.set(holder.getAdapterPosition(), tmp);
            }
        });
        //Card expand/collapse when card is touched
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change post's viewProperties accordingly
                boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                tmp[isPostDateAndNumberVisibile] = !tmp[isPostDateAndNumberVisibile];
                viewProperties.set(holder.getAdapterPosition(), tmp);

                TopicAnimations.animatePostExtraInfoVisibility(holder.postDateAndNumberExp
                        , holder.username, holder.subject
                        , Color.parseColor("#FFFFFF")
                        , Color.parseColor("#757575"));
            }
        });
        //Also when post is clicked
        holder.post.setOnTouchListener(new CustomTouchListener(holder.post, holder.cardView));
    }

    void customNotifyDataSetChanged(TopicActivity.TopicTask topicTask) {
        this.topicTask = topicTask;
        viewProperties.clear();
        for (int i = 0; i < postsList.size(); ++i) {
            //Initializes properties, array's values will be false by default
            viewProperties.add(new boolean[3]);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    /**
     * This class is a gesture detector for WebViews. It handles post's clicks, long clicks and
     * touch and drag.
     */
    private class CustomTouchListener implements View.OnTouchListener {
        //Long press handling
        private float downCoordinateX;
        private float downCoordinateY;
        private final float SCROLL_THRESHOLD = 7;
        final private WebView post;
        final private CardView cardView;

        //Other variables
        final static int FINGER_RELEASED = 0;
        final static int FINGER_TOUCHED = 1;
        final static int FINGER_DRAGGING = 2;
        final static int FINGER_UNDEFINED = 3;

        private int fingerState = FINGER_RELEASED;

        CustomTouchListener(WebView pPost, CardView pCard) {
            post = pPost;
            cardView = pCard;
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    //Logs XY
                    downCoordinateX = motionEvent.getX();
                    downCoordinateY = motionEvent.getY();

                    if (fingerState == FINGER_RELEASED)
                        fingerState = FINGER_TOUCHED;
                    else
                        fingerState = FINGER_UNDEFINED;
                    break;
                case MotionEvent.ACTION_UP:
                    if (fingerState != FINGER_DRAGGING) {
                        //Doesn't expand the card if this was a link
                        WebView.HitTestResult htResult = post.getHitTestResult();
                        if (htResult.getExtra() != null
                                && htResult.getExtra() != null) {
                            fingerState = FINGER_RELEASED;
                            return false;
                        }
                        cardView.performClick();
                    }
                    fingerState = FINGER_RELEASED;
                    break;
                case MotionEvent.ACTION_MOVE:
                    //Cancels long click if finger moved too much
                    if (((Math.abs(downCoordinateX - motionEvent.getX()) > SCROLL_THRESHOLD ||
                            Math.abs(downCoordinateY - motionEvent.getY()) > SCROLL_THRESHOLD))) {
                        fingerState = FINGER_DRAGGING;
                    } else fingerState = FINGER_UNDEFINED;
                    break;
                default:
                    fingerState = FINGER_UNDEFINED;
            }
            return false;
        }
    }

    /**
     * This class is used to handle link clicks in WebViews. When link url is one that the app can
     * handle internally, it does. Otherwise user is prompt to open the link in a browser.
     */
    @SuppressWarnings("unchecked")
    private class LinkLauncher extends WebViewClient {
        @SuppressWarnings("deprecation")
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Uri uri = Uri.parse(url);
            return handleUri(uri);
        }

        @TargetApi(Build.VERSION_CODES.N)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            return handleUri(uri);
        }

        @SuppressWarnings("SameReturnValue")
        private boolean handleUri(final Uri uri) {
            final String uriString = uri.toString();

            LinkTarget.Target target = LinkTarget.resolveLinkTarget(uri);
            if (target.is(LinkTarget.Target.TOPIC)) {
                //This url points to a topic
                //Checks if this is the current topic
                if (Objects.equals(uriString.substring(0, uriString.lastIndexOf(".")), base_url)) {
                    //Gets uri's targeted message's index number
                    String msgIndexReq = uriString.substring(uriString.indexOf("msg") + 3);
                    if (msgIndexReq.contains("#"))
                        msgIndexReq = msgIndexReq.substring(0, msgIndexReq.indexOf("#"));
                    else
                        msgIndexReq = msgIndexReq.substring(0, msgIndexReq.indexOf(";"));

                    //Checks if this post is in the current topic's page
                    for (Post post : postsList) {
                        if (post.getPostIndex() == Integer.parseInt(msgIndexReq)) {
                            // TODO Don't restart Activity, Just change post focus
                            return true;
                        }
                    }
                }
                topicTask.execute(uri.toString());
                return true;
            } else if (target.is(LinkTarget.Target.BOARD)) {
                Intent intent = new Intent(context, BoardActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_BOARD_URL, uriString);
                extras.putString(BUNDLE_BOARD_TITLE, "");
                intent.putExtras(extras);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } else if (target.is(LinkTarget.Target.PROFILE)) {
                Intent intent = new Intent(context, ProfileActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_PROFILE_URL, uriString);
                extras.putString(BUNDLE_THUMBNAIL_URL, "");
                extras.putString(BUNDLE_USERNAME, "");
                intent.putExtras(extras);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

            //Method always returns true as no url should be loaded in the WebViews
            return true;
        }
    }

    /**
     * Returns a String with a single FontAwesome typeface character corresponding to this file's
     * extension.
     *
     * @param filename String with filename <b>containing file's extension</b>
     * @return FontAwesome character according to file's type
     * @see <a href="http://fontawesome.io/">FontAwesome</a>
     */
    @NonNull
    private String faIconFromFilename(String filename) {
        filename = filename.toLowerCase();

        if (filename.contains("jpg") || filename.contains("gif") || filename.contains("jpeg")
                || filename.contains("png"))
            return context.getResources().getString(R.string.fa_file_image_o);
        else if (filename.contains("pdf"))
            return context.getResources().getString(R.string.fa_file_pdf_o);
        else if (filename.contains("zip") || filename.contains("rar") || filename.contains("tar.gz"))
            return context.getResources().getString(R.string.fa_file_zip_o);
        else if (filename.contains("txt"))
            return context.getResources().getString(R.string.fa_file_text_o);
        else if (filename.contains("doc") || filename.contains("docx"))
            return context.getResources().getString(R.string.fa_file_word_o);
        else if (filename.contains("xls") || filename.contains("xlsx"))
            return context.getResources().getString(R.string.fa_file_excel_o);
        else if (filename.contains("pps"))
            return context.getResources().getString(R.string.fa_file_powerpoint_o);
        else if (filename.contains("mpg"))
            return context.getResources().getString(R.string.fa_file_video_o);

        return context.getResources().getString(R.string.fa_file);
    }

    private class DownloadTask extends AsyncTask<ThmmyFile, Void, String> {
        //Class variables
        /**
         * Debug Tag for logging debug output to LogCat
         */
        private static final String TAG = "DownloadTask"; //Separate tag for AsyncTask

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(context, "Downloading", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(ThmmyFile... files) {
            try {
                File tempFile = files[0].download(context);
                if (tempFile != null) {
                    String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            files[0].getExtension());

                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(tempFile), mime);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            } catch (IOException e) {
                Report.e(TAG, "Error while trying to download a file", e);
                return e.toString();
            } catch (OutOfMemoryError e) {
                Report.e(TAG, e.toString(), e);
                return e.toString();
            } catch (IllegalStateException e) {
                Report.e(TAG, e.toString(), e);
                return e.toString();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast.makeText(context, "Download failed!", Toast.LENGTH_SHORT).show();
                Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            }
        }
    }
}