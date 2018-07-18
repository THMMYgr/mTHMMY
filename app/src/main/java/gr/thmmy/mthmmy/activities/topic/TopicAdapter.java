package gr.thmmy.mthmmy.activities.topic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyFile;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.CircleTransform;
import timber.log.Timber;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicParser.USER_COLOR_WHITE;
import static gr.thmmy.mthmmy.activities.topic.TopicParser.USER_COLOR_YELLOW;
import static gr.thmmy.mthmmy.base.BaseActivity.getSessionManager;

/**
 * Custom {@link android.support.v7.widget.RecyclerView.Adapter} used for topics.
 */
class TopicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * Int that holds thumbnail's size defined in R.dimen
     */
    private static int THUMBNAIL_SIZE;
    private final Context context;
    private String topicTitle;
    private String baseUrl;
    private final ArrayList<Integer> toQuoteList = new ArrayList<>();
    private final List<Post> postsList;
    /**
     * Used to hold the state of visibility and other attributes for views that are animated or
     * otherwise changed. Used in combination with {@link #isUserExtraInfoVisibile} and
     * {@link #isQuoteButtonChecked}.
     */
    private final ArrayList<boolean[]> viewProperties = new ArrayList<>();
    /**
     * Index of state indicator in the boolean array. If true user's extra info are expanded and
     * visible.
     */
    private static final int isUserExtraInfoVisibile = 0;
    /**
     * Index of state indicator in the boolean array. If true quote button for this post is checked.
     */
    private static final int isQuoteButtonChecked = 1;
    private TopicActivity.TopicTask topicTask;
    private TopicActivity.ReplyTask replyTask;
    private TopicActivity.DeleteTask deleteTask;
    private TopicActivity.PrepareForEdit prepareForEditTask;
    private TopicActivity.EditTask editTask;

    private final String[] replyDataHolder = new String[2];
    private final int replySubject = 0, replyText = 1;
    private String commitEditURL, numReplies, seqnum, sc, topic, buildedQuotes, postText;
    private boolean canReply = false;

    /**
     * @param context   the context of the {@link RecyclerView}
     * @param postsList List of {@link Post} objects to use
     */
    TopicAdapter(Context context, List<Post> postsList, String baseUrl,
                 TopicActivity.TopicTask topicTask) {
        this.context = context;
        this.postsList = postsList;
        this.baseUrl = baseUrl;

        THUMBNAIL_SIZE = (int) context.getResources().getDimension(R.dimen.thumbnail_size);
        for (int i = 0; i < postsList.size(); ++i) {
            //Initializes properties, array's values will be false by default
            viewProperties.add(new boolean[3]);
        }
        this.topicTask = topicTask;
    }

    ArrayList<Integer> getToQuoteList() {
        return toQuoteList;
    }

    void prepareForReply(TopicActivity.ReplyTask replyTask, String topicTitle, String numReplies,
                         String seqnum, String sc, String topic, String buildedQuotes) {
        this.replyTask = replyTask;
        this.topicTitle = topicTitle;
        this.numReplies = numReplies;
        this.seqnum = seqnum;
        this.sc = sc;
        this.topic = topic;
        this.buildedQuotes = buildedQuotes;
    }

    void prepareForDelete(TopicActivity.DeleteTask deleteTask) {
        this.deleteTask = deleteTask;
    }

    void prepareForPrepareForEdit(TopicActivity.PrepareForEdit prepareForEditTask) {
        this.prepareForEditTask = prepareForEditTask;
    }

    void prepareForEdit(TopicActivity.EditTask editTask, String commitEditURL, String numReplies, String seqnum, String sc,
                        String topic, String postText) {
        this.commitEditURL = commitEditURL;
        this.editTask = editTask;
        this.numReplies = numReplies;
        this.seqnum = seqnum;
        this.sc = sc;
        this.topic = topic;
        this.postText = postText;
    }

    @Override
    public int getItemViewType(int position) {
        return postsList.get(position).getPostType();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == Post.TYPE_POST) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_topic_post_row, parent, false);
            return new PostViewHolder(itemView);
        } else if (viewType == Post.TYPE_QUICK_REPLY) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_topic_quick_reply_row, parent, false);
            view.findViewById(R.id.quick_reply_submit).setEnabled(true);

            final EditText quickReplyText = view.findViewById(R.id.quick_reply_text);
            quickReplyText.setFocusableInTouchMode(true);
            quickReplyText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    quickReplyText.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.showSoftInput(quickReplyText, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            quickReplyText.requestFocus();

            //Default post subject
            replyDataHolder[replySubject] = "Re: " + topicTitle;
            //Build quotes
            if (!Objects.equals(buildedQuotes, ""))
                replyDataHolder[replyText] = buildedQuotes;
            return new QuickReplyViewHolder(view, new CustomEditTextListener(replySubject),
                    new CustomEditTextListener(replyText));
        } else if (viewType == Post.TYPE_EDIT) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_topic_edit_row, parent, false);
            view.findViewById(R.id.edit_message_submit).setEnabled(true);

            return new EditMessageViewHolder(view);
        }
        return null;
    }

    @SuppressLint({"SetJavaScriptEnabled", "SetTextI18n"})
    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder currentHolder,
                                 final int position) {
        if (currentHolder instanceof PostViewHolder) {
            final Post currentPost = postsList.get(position);
            final PostViewHolder holder = (PostViewHolder) currentHolder;

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
                    .load(currentPost.getThumbnailURL())
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
            if ((currentPost.getAttachedFiles() != null && currentPost.getAttachedFiles().size() != 0)
                    || (currentPost.getLastEdit() != null)) {
                holder.bodyFooterDivider.setVisibility(View.VISIBLE);
                holder.postFooter.removeAllViews();

                if (currentPost.getAttachedFiles() != null && currentPost.getAttachedFiles().size() != 0) {
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
                                ((BaseActivity) context).downloadFile(attachedFile);
                            }
                        });

                        holder.postFooter.addView(attached);
                    }
                }
                if (currentPost.getLastEdit() != null && currentPost.getLastEdit().length() > 0) {
                    int lastEditTextColor;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        lastEditTextColor = context.getResources().getColor(R.color.white, null);
                    } else //noinspection deprecation
                        lastEditTextColor = context.getResources().getColor(R.color.white);

                    final TextView lastEdit = new TextView(context);
                    lastEdit.setTextSize(12f);
                    lastEdit.setText(currentPost.getLastEdit());
                    lastEdit.setTextColor(lastEditTextColor);
                    lastEdit.setPadding(0, 3, 0, 3);
                    holder.postFooter.addView(lastEdit);
                }
            } else {
                holder.bodyFooterDivider.setVisibility(View.GONE);
                holder.postFooter.removeAllViews();
            }

            String mSpecialRank, mRank, mGender, mNumberOfPosts, mPersonalText;
            int mNumberOfStars, mUserColor;

            if (!currentPost.isDeleted()) { //Sets user's extra info
                mSpecialRank = currentPost.getSpecialRank();
                mRank = currentPost.getRank();
                mGender = currentPost.getGender();
                mNumberOfPosts = currentPost.getNumberOfPosts();
                mPersonalText = currentPost.getPersonalText();
                mNumberOfStars = currentPost.getNumberOfStars();
            } else {
                mSpecialRank = null;
                mRank = null;
                mGender = null;
                mNumberOfPosts = null;
                mPersonalText = null;
                mNumberOfStars = 0;
            }
            mUserColor = currentPost.getUserColor();

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
            if (mUserColor != USER_COLOR_YELLOW) {
                holder.username.setTextColor(mUserColor);
            } else {
                holder.username.setTextColor(USER_COLOR_WHITE);
            }
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
            if (!currentPost.isDeleted() && viewProperties.get(position)[isUserExtraInfoVisibile]) {
                holder.userExtraInfo.setVisibility(View.VISIBLE);
                holder.userExtraInfo.setAlpha(1.0f);

                holder.username.setMaxLines(Integer.MAX_VALUE);
                holder.username.setEllipsize(null);

                holder.subject.setTextColor(Color.parseColor("#FFFFFF"));
                holder.subject.setMaxLines(Integer.MAX_VALUE);
                holder.subject.setEllipsize(null);
            } else {
                holder.userExtraInfo.setVisibility(View.GONE);
                holder.userExtraInfo.setAlpha(0.0f);

                holder.username.setMaxLines(1);
                holder.username.setEllipsize(TextUtils.TruncateAt.END);

                holder.subject.setTextColor(Color.parseColor("#757575"));
                holder.subject.setMaxLines(1);
                holder.subject.setEllipsize(TextUtils.TruncateAt.END);
            }
            if (!currentPost.isDeleted()) {
                //Sets graphics behavior
                holder.thumbnail.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Clicking the thumbnail opens user's profile
                        Intent intent = new Intent(context, ProfileActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(BUNDLE_PROFILE_URL, currentPost.getProfileURL());
                        if (currentPost.getThumbnailURL() == null)
                            extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, "");
                        else
                            extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, currentPost.getThumbnailURL());
                        extras.putString(BUNDLE_PROFILE_USERNAME, currentPost.getAuthor());
                        intent.putExtras(extras);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });
                holder.header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Clicking the header makes it expand/collapse
                        boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                        tmp[isUserExtraInfoVisibile] = !tmp[isUserExtraInfoVisibile];
                        viewProperties.set(holder.getAdapterPosition(), tmp);
                        TopicAnimations.animateUserExtraInfoVisibility(holder.username,
                                holder.subject, Color.parseColor("#FFFFFF"),
                                Color.parseColor("#757575"), holder.userExtraInfo);
                    }
                });
                //Clicking the expanded part of a header (the extra info) makes it collapse
                holder.userExtraInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean[] tmp = viewProperties.get(holder.getAdapterPosition());
                        tmp[isUserExtraInfoVisibile] = false;
                        viewProperties.set(holder.getAdapterPosition(), tmp);

                        TopicAnimations.animateUserExtraInfoVisibility(holder.username,
                                holder.subject, Color.parseColor("#FFFFFF"),
                                Color.parseColor("#757575"), (LinearLayout) v);
                    }
                });
            } else {
                holder.header.setOnClickListener(null);
                holder.userExtraInfo.setOnClickListener(null);
            }

            holder.overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Inflates the popup menu content
                    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    if (layoutInflater == null) {
                        return;
                    }
                    View popUpContent = layoutInflater.inflate(R.layout.activity_topic_overflow_menu, null);

                    //Creates the PopupWindow
                    final PopupWindow popUp = new PopupWindow(holder.overflowButton.getContext());
                    popUp.setContentView(popUpContent);
                    popUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                    popUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                    popUp.setFocusable(true);

                    popUpContent.findViewById(R.id.post_share_button).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent sendIntent = new Intent(android.content.Intent.ACTION_SEND);
                            sendIntent.setType("text/plain");
                            sendIntent.putExtra(android.content.Intent.EXTRA_TEXT, currentPost.getPostURL());
                            context.startActivity(Intent.createChooser(sendIntent, "Share via"));
                            popUp.dismiss();
                        }
                    });

                    TextView deletePostButton = popUpContent.findViewById(R.id.delete_post);

                    if (currentPost.getPostDeleteURL() == null || currentPost.getPostDeleteURL().equals("")) {
                        deletePostButton.setVisibility(View.GONE);
                    } else {
                        popUpContent.findViewById(R.id.delete_post).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(holder.overflowButton.getContext())
                                        .setTitle("Delete post")
                                        .setMessage("Do you really want to delete this post?")
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int whichButton) {
                                                deleteTask.execute(currentPost.getPostDeleteURL());
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, null).show();
                                popUp.dismiss();
                            }
                        });
                    }

                    final TextView editPostButton = popUpContent.findViewById(R.id.edit_post);

                    if (currentPost.getPostEditURL() == null || currentPost.getPostEditURL().equals("")) {
                        editPostButton.setVisibility(View.GONE);
                    } else {
                        editPostButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                prepareForEditTask.execute(position);
                            }
                        });
                    }

                    //Displays the popup
                    popUp.showAsDropDown(holder.overflowButton);
                }
            });

            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!BaseActivity.getSessionManager().isLoggedIn() || !canReply) {
                holder.quoteToggle.setVisibility(View.GONE);
            } else {
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
                            if (toQuoteList.contains(postsList.indexOf(currentPost))) {
                                toQuoteList.remove(toQuoteList.indexOf(postsList.indexOf(currentPost)));
                            } else
                                Timber.i("An error occurred while trying to exclude post fromtoQuoteList, post wasn't there!");
                            holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_unchecked);
                        } else {
                            toQuoteList.add(postsList.indexOf(currentPost));
                            holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_checked);
                        }
                        tmp[isQuoteButtonChecked] = !tmp[isQuoteButtonChecked];
                        viewProperties.set(holder.getAdapterPosition(), tmp);
                    }
                });
            }
        } else if (currentHolder instanceof QuickReplyViewHolder) {
            final QuickReplyViewHolder holder = (QuickReplyViewHolder) currentHolder;

            //noinspection ConstantConditions
            Picasso.with(context)
                    .load(getSessionManager().getAvatarLink())
                    .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .centerCrop()
                    .error(ResourcesCompat.getDrawable(context.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .placeholder(ResourcesCompat.getDrawable(context.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .transform(new CircleTransform())
                    .into(holder.thumbnail);
            holder.username.setText(getSessionManager().getUsername());


            if (replyDataHolder[replyText] != null && !Objects.equals(replyDataHolder[replyText], "")) {
                holder.quickReply.setText(replyDataHolder[replyText]);
                holder.quickReplySubject.setText(replyDataHolder[replySubject]);

                holder.submitButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.quickReplySubject.getText().toString().isEmpty()) return;
                        if (holder.quickReply.getText().toString().isEmpty()) return;
                        holder.submitButton.setEnabled(false);
                        replyTask.execute(holder.quickReplySubject.getText().toString(),
                                holder.quickReply.getText().toString(), numReplies, seqnum, sc, topic);

                        holder.quickReplySubject.getText().clear();
                        holder.quickReplySubject.setText("Re: " + topicTitle);
                        holder.quickReply.getText().clear();
                        holder.submitButton.setEnabled(true);
                    }
                });
            }

            if (backPressHidden) {
                holder.quickReply.requestFocus();
                backPressHidden = false;
            }
        } else if (currentHolder instanceof EditMessageViewHolder) {
            final EditMessageViewHolder holder = (EditMessageViewHolder) currentHolder;

            //noinspection ConstantConditions
            Picasso.with(context)
                    .load(getSessionManager().getAvatarLink())
                    .resize(THUMBNAIL_SIZE, THUMBNAIL_SIZE)
                    .centerCrop()
                    .error(ResourcesCompat.getDrawable(context.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .placeholder(ResourcesCompat.getDrawable(context.getResources()
                            , R.drawable.ic_default_user_thumbnail, null))
                    .transform(new CircleTransform())
                    .into(holder.thumbnail);
            holder.username.setText(getSessionManager().getUsername());

            holder.editSubject.setText(postsList.get(position).getSubject());
            holder.editMessage.setText(postText);

            holder.submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (holder.editSubject.getText().toString().isEmpty()) return;
                    if (holder.editMessage.getText().toString().isEmpty()) return;
                    holder.submitButton.setEnabled(false);
                    editTask.execute(new EditTaskDTO(position, commitEditURL, holder.editSubject.getText().toString(),
                            holder.editMessage.getText().toString(), numReplies, seqnum, sc, topic));

                    holder.editSubject.getText().clear();
                    holder.editSubject.setText(postsList.get(position).getSubject());
                    holder.submitButton.setEnabled(true);
                }
            });

            if (backPressHidden) {
                holder.editMessage.requestFocus();
                backPressHidden = false;
            }
        }
    }

    void resetTopic(String baseUrl, TopicActivity.TopicTask topicTask, boolean canReply) {
        this.baseUrl = baseUrl;
        this.topicTask = topicTask;
        this.canReply = canReply;
        viewProperties.clear();
        for (int i = 0; i < postsList.size(); ++i) {
            //Initializes properties, array's values will be false by default
            viewProperties.add(new boolean[3]);
        }
    }

    @Override
    public int getItemCount() {
        return postsList.size();
    }

    /**
     * Custom {@link RecyclerView.ViewHolder} implementation
     */
    private class PostViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout cardChildLinear;
        final TextView postDate, postNum, username, subject;
        final ImageView thumbnail;
        final public WebView post;
        final ImageButton quoteToggle, overflowButton;
        final RelativeLayout header;
        final LinearLayout userExtraInfo;
        final View bodyFooterDivider;
        final LinearLayout postFooter;

        final TextView specialRank, rank, gender, numberOfPosts, personalText, stars;

        PostViewHolder(View view) {
            super(view);
            //Initializes layout's graphic elements
            //Standard stuff
            cardChildLinear = view.findViewById(R.id.card_child_linear);
            postDate = view.findViewById(R.id.post_date);
            postNum = view.findViewById(R.id.post_number);
            thumbnail = view.findViewById(R.id.thumbnail);
            username = view.findViewById(R.id.username);
            subject = view.findViewById(R.id.subject);
            post = view.findViewById(R.id.post);
            post.setBackgroundColor(Color.argb(1, 255, 255, 255));
            quoteToggle = view.findViewById(R.id.toggle_quote_button);
            overflowButton = view.findViewById(R.id.post_overflow_menu);
            bodyFooterDivider = view.findViewById(R.id.body_footer_divider);
            postFooter = view.findViewById(R.id.post_footer);

            //User's extra info
            header = view.findViewById(R.id.header);
            userExtraInfo = view.findViewById(R.id.user_extra_info);
            specialRank = view.findViewById(R.id.special_rank);
            rank = view.findViewById(R.id.rank);
            gender = view.findViewById(R.id.gender);
            numberOfPosts = view.findViewById(R.id.number_of_posts);
            personalText = view.findViewById(R.id.personal_text);
            stars = view.findViewById(R.id.stars);
        }
    }

    private boolean backPressHidden = false;

    void setBackButtonHidden() {
        this.backPressHidden = true;
    }


    /**
     * Custom {@link RecyclerView.ViewHolder} implementation
     */
    private static class QuickReplyViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView username;
        final EditText quickReply, quickReplySubject;
        final AppCompatImageButton submitButton;

        QuickReplyViewHolder(View quickReply, CustomEditTextListener replySubject
                , CustomEditTextListener replyText) {
            super(quickReply);
            thumbnail = quickReply.findViewById(R.id.thumbnail);
            username = quickReply.findViewById(R.id.username);
            this.quickReply = quickReply.findViewById(R.id.quick_reply_text);
            this.quickReply.addTextChangedListener(replyText);
            quickReplySubject = quickReply.findViewById(R.id.quick_reply_subject);
            quickReplySubject.addTextChangedListener(replySubject);
            submitButton = quickReply.findViewById(R.id.quick_reply_submit);
        }
    }

    private static class EditMessageViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView username;
        final EditText editMessage, editSubject;
        final AppCompatImageButton submitButton;

        public EditMessageViewHolder(View editView) {
            super(editView);

            thumbnail = editView.findViewById(R.id.thumbnail);
            username = editView.findViewById(R.id.username);
            editMessage = editView.findViewById(R.id.edit_message_text);
            editSubject = editView.findViewById(R.id.edit_message_subject);
            submitButton = editView.findViewById(R.id.edit_message_submit);
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

            ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(uri);
            if (target.is(ThmmyPage.PageCategory.TOPIC)) {
                //This url points to a topic
                //Checks if this is the current topic
                if (Objects.equals(uriString.substring(0, uriString.lastIndexOf(".")), baseUrl)) {
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

                    topicTask.execute(uri.toString());
                }

                Intent intent = new Intent(context, TopicActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_TOPIC_URL, uriString);
                intent.putExtras(extras);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } else if (target.is(ThmmyPage.PageCategory.BOARD)) {
                Intent intent = new Intent(context, BoardActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_BOARD_URL, uriString);
                extras.putString(BUNDLE_BOARD_TITLE, "");
                intent.putExtras(extras);
                intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return true;
            } else if (target.is(ThmmyPage.PageCategory.PROFILE)) {
                Intent intent = new Intent(context, ProfileActivity.class);
                Bundle extras = new Bundle();
                extras.putString(BUNDLE_PROFILE_URL, uriString);
                extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, "");
                extras.putString(BUNDLE_PROFILE_USERNAME, "");
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

    private class CustomEditTextListener implements TextWatcher {
        private final int positionInDataHolder;

        CustomEditTextListener(int positionInDataHolder) {
            this.positionInDataHolder = positionInDataHolder;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            replyDataHolder[positionInDataHolder] = charSequence.toString();
        }

        @Override
        public void afterTextChanged(Editable editable) {
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
}