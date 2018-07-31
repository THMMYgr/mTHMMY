package gr.thmmy.mthmmy.activities.topic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
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
import android.text.TextUtils;
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
import gr.thmmy.mthmmy.viewmodel.TopicViewModel;
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
    private final OnPostFocusChangeListener postFocusListener;
    private final List<Post> postsList;
    private TopicViewModel viewModel;

    /**
     * @param context   the context of the {@link RecyclerView}
     * @param postsList List of {@link Post} objects to use
     */
    TopicAdapter(TopicActivity context, List<Post> postsList) {
        this.context = context;
        this.postsList = postsList;
        this.postFocusListener = context;

        viewModel = ViewModelProviders.of(context).get(TopicViewModel.class);

        THUMBNAIL_SIZE = (int) context.getResources().getDimension(R.dimen.thumbnail_size);
    }

    @Override
    public int getItemViewType(int position) {
        return postsList.get(position).getPostType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
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
            quickReplyText.setOnFocusChangeListener((v, hasFocus) -> quickReplyText.post(() -> {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(quickReplyText, InputMethodManager.SHOW_IMPLICIT);
            }));
            quickReplyText.requestFocus();

            return new QuickReplyViewHolder(view);
        } else if (viewType == Post.TYPE_EDIT) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_topic_edit_row, parent, false);
            view.findViewById(R.id.edit_message_submit).setEnabled(true);

            final EditText editPostEdittext = view.findViewById(R.id.edit_message_text);
            editPostEdittext.setFocusableInTouchMode(true);
            editPostEdittext.setOnFocusChangeListener((v, hasFocus) -> editPostEdittext.post(() -> {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editPostEdittext, InputMethodManager.SHOW_IMPLICIT);
            }));
            editPostEdittext.requestFocus();

            return new EditMessageViewHolder(view);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder currentHolder,
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
                            , R.drawable.ic_default_user_thumbnail_white_24dp, null))
                    .placeholder(ResourcesCompat.getDrawable(context.getResources()
                            , R.drawable.ic_default_user_thumbnail_white_24dp, null))
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

                        attached.setOnClickListener(view -> ((BaseActivity) context).downloadFile(attachedFile));

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
                StringBuilder usersStars = new StringBuilder();
                for (int i = 0; i < mNumberOfStars; ++i) {
                    usersStars.append(aStar);
                }
                holder.stars.setText(usersStars.toString());
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
            if (!currentPost.isDeleted() && viewModel.isUserExtraInfoVisible(holder.getAdapterPosition())) {
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
                holder.thumbnail.setOnClickListener(view -> {
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
                });
                holder.header.setOnClickListener(v -> {
                    //Clicking the header makes it expand/collapse
                    viewModel.toggleUserInfo(holder.getAdapterPosition());
                    TopicAnimations.animateUserExtraInfoVisibility(holder.username,
                            holder.subject, Color.parseColor("#FFFFFF"),
                            Color.parseColor("#757575"), holder.userExtraInfo);
                });
                //Clicking the expanded part of a header (the extra info) makes it collapse
                holder.userExtraInfo.setOnClickListener(v -> {
                    viewModel.hideUserInfo(holder.getAdapterPosition());
                    TopicAnimations.animateUserExtraInfoVisibility(holder.username,
                            holder.subject, Color.parseColor("#FFFFFF"),
                            Color.parseColor("#757575"), (LinearLayout) v);
                });
            } else {
                holder.header.setOnClickListener(null);
                holder.userExtraInfo.setOnClickListener(null);
            }

            holder.overflowButton.setOnClickListener(view -> {
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

                popUpContent.findViewById(R.id.post_share_button).setOnClickListener(v -> {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_TEXT, currentPost.getPostURL());
                    context.startActivity(Intent.createChooser(sendIntent, "Share via"));
                    popUp.dismiss();
                });

                TextView deletePostButton = popUpContent.findViewById(R.id.delete_post);

                if (currentPost.getPostDeleteURL() == null || currentPost.getPostDeleteURL().equals("")) {
                    deletePostButton.setVisibility(View.GONE);
                } else {
                    popUpContent.findViewById(R.id.delete_post).setOnClickListener(v -> {
                        new AlertDialog.Builder(holder.overflowButton.getContext())
                                .setTitle("Delete post")
                                .setMessage("Do you really want to delete this post?")
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(android.R.string.yes, (dialog, whichButton) ->
                                        viewModel.deletePost(currentPost.getPostDeleteURL()))
                                .setNegativeButton(android.R.string.no, null).show();
                        popUp.dismiss();
                    });
                }

                final TextView editPostButton = popUpContent.findViewById(R.id.edit_post);

                if (viewModel.isEditingPost() || currentPost.getPostEditURL() == null || currentPost.getPostEditURL().equals("")) {
                    editPostButton.setVisibility(View.GONE);
                } else {
                    editPostButton.setOnClickListener(v -> {
                        viewModel.prepareForEdit(position, postsList.get(position).getPostEditURL());
                        popUp.dismiss();
                    });
                }

                //Displays the popup
                popUp.showAsDropDown(holder.overflowButton);
            });

            //noinspection PointlessBooleanExpression,ConstantConditions
            if (!BaseActivity.getSessionManager().isLoggedIn() || !viewModel.canReply()) {
                holder.quoteToggle.setVisibility(View.GONE);
            } else {
                if (viewModel.getToQuoteList().contains(currentPost.getPostIndex()))
                    holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_checked_accent_24dp);
                else
                    holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_unchecked_grey_24dp);
                //Sets graphics behavior
                holder.quoteToggle.setOnClickListener(view -> {
                    viewModel.postIndexToggle(currentPost.getPostIndex());
                    if (viewModel.getToQuoteList().contains(currentPost.getPostIndex()))
                        holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_checked_accent_24dp);
                    else
                        holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_unchecked_grey_24dp);
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
                            , R.drawable.ic_default_user_thumbnail_white_24dp, null))
                    .placeholder(ResourcesCompat.getDrawable(context.getResources()
                            , R.drawable.ic_default_user_thumbnail_white_24dp, null))
                    .transform(new CircleTransform())
                    .into(holder.thumbnail);
            holder.username.setText(getSessionManager().getUsername());
            holder.quickReplySubject.setText("Re: " + viewModel.getTopicTitle());

            holder.quickReply.setText(viewModel.getBuildedQuotes());


            holder.submitButton.setOnClickListener(view -> {
                if (holder.quickReplySubject.getText().toString().isEmpty()) return;
                if (holder.quickReply.getText().toString().isEmpty()) return;
                holder.submitButton.setEnabled(false);

                viewModel.postReply(context, holder.quickReplySubject.getText().toString(),
                        holder.quickReply.getText().toString());

                holder.quickReplySubject.getText().clear();
                holder.quickReplySubject.setText("Re: " + viewModel.getTopicTitle());
                holder.quickReply.getText().clear();
                holder.submitButton.setEnabled(true);
            });


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
            holder.editMessage.setText(viewModel.getPostBeingEditedText());

            holder.submitButton.setOnClickListener(view -> {
                if (holder.editSubject.getText().toString().isEmpty()) return;
                if (holder.editMessage.getText().toString().isEmpty()) return;
                holder.submitButton.setEnabled(false);

                viewModel.editPost(position, holder.editSubject.getText().toString(), holder.editMessage.getText().toString());

                holder.editSubject.getText().clear();
                holder.editSubject.setText(postsList.get(position).getSubject());
                holder.submitButton.setEnabled(true);
            });

            if (backPressHidden) {
                holder.editMessage.requestFocus();
                backPressHidden = false;
            }
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

        QuickReplyViewHolder(View quickReply) {
            super(quickReply);
            thumbnail = quickReply.findViewById(R.id.thumbnail);
            username = quickReply.findViewById(R.id.username);
            this.quickReply = quickReply.findViewById(R.id.quick_reply_text);
            quickReplySubject = quickReply.findViewById(R.id.quick_reply_subject);
            submitButton = quickReply.findViewById(R.id.quick_reply_submit);
        }
    }

    private static class EditMessageViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView username;
        final EditText editMessage, editSubject;
        final AppCompatImageButton submitButton;

        EditMessageViewHolder(View editView) {
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
            viewModel.stopLoading();
            if (target.is(ThmmyPage.PageCategory.TOPIC)) {
                //This url points to a topic
                //Checks if the page to be loaded is the one already shown
                if (uriString.contains(viewModel.getBaseUrl())) {
                    Timber.e("reached here!");
                    if (uriString.contains("topicseen#new") || uriString.contains("#new")) {
                        if (viewModel.getCurrentPageIndex() == viewModel.getPageCount()) {
                            //same page
                            postFocusListener.onPostFocusChange(getItemCount() - 1);
                            Timber.e("new");
                            return true;
                        }
                    }
                    if (uriString.contains("msg")) {
                        String tmpUrlSbstr = uriString.substring(uriString.indexOf("msg") + 3);
                        if (tmpUrlSbstr.contains("msg"))
                            tmpUrlSbstr = tmpUrlSbstr.substring(0, tmpUrlSbstr.indexOf("msg") - 1);
                        int testAgainst = Integer.parseInt(tmpUrlSbstr);
                        Timber.e("reached tthere! %s", testAgainst);
                        for (int i = 0; i < postsList.size(); i++) {
                            if (postsList.get(i).getPostIndex() == testAgainst) {
                                //same page
                                Timber.e(Integer.toString(i));
                                postFocusListener.onPostFocusChange(i);
                                return true;
                            }
                        }
                    } else if ((Objects.equals(uriString, viewModel.getBaseUrl()) && viewModel.getCurrentPageIndex() == 1) ||
                            Integer.parseInt(uriString.substring(viewModel.getBaseUrl().length() + 1)) / 15 + 1 ==
                                    viewModel.getCurrentPageIndex()) {
                        //same page
                        Timber.e("ha");
                        return true;
                    }
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

    //we need to set a callback to topic activity to scroll the recyclerview when post focus is requested
    public interface OnPostFocusChangeListener {
        void onPostFocusChange(int position);
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