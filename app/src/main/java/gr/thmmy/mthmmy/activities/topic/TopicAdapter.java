package gr.thmmy.mthmmy.activities.topic;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Poll;
import gr.thmmy.mthmmy.model.Post;
import gr.thmmy.mthmmy.model.ThmmyFile;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.model.TopicItem;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import gr.thmmy.mthmmy.utils.parsing.ThmmyParser;
import gr.thmmy.mthmmy.viewmodel.TopicViewModel;
import gr.thmmy.mthmmy.views.ReactiveWebView;
import gr.thmmy.mthmmy.views.editorview.EditorView;
import gr.thmmy.mthmmy.views.editorview.IEmojiKeyboard;
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
import static gr.thmmy.mthmmy.utils.FileUtils.faIconFromFilename;
import static gr.thmmy.mthmmy.utils.ui.GlideUtils.isValidContextForGlide;

/**
 * Custom {@link RecyclerView.Adapter} used for topics.
 */
class TopicAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * Int that holds thumbnail's size defined in R.dimen
     */
    private final Context context;
    private final OnPostFocusChangeListener postFocusListener;
    private final IEmojiKeyboard emojiKeyboard;
    private final List<TopicItem> topicItems;
    private TopicViewModel viewModel;

    /**
     * @param context    the context of the {@link RecyclerView}
     * @param topicItems List of {@link Post} objects to use
     */
    TopicAdapter(TopicActivity context, IEmojiKeyboard emojiKeyboard, List<TopicItem> topicItems) {
        this.context = context;
        this.topicItems = topicItems;
        this.postFocusListener = context;
        this.emojiKeyboard = emojiKeyboard;

        viewModel = ViewModelProviders.of(context).get(TopicViewModel.class);
    }

    @Override
    public int getItemViewType(int position) {
        if (topicItems.get(position) instanceof Poll) return Poll.TYPE_POLL;
        return ((Post) topicItems.get(position)).getPostType();
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

            final EditText quickReplyText = ((EditorView) view.findViewById(R.id.reply_editorview)).getEditText();
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

            final EditText editPostEdittext = ((EditorView) view.findViewById(R.id.edit_editorview)).getEditText();
            editPostEdittext.setFocusableInTouchMode(true);
            editPostEdittext.setOnFocusChangeListener((v, hasFocus) -> editPostEdittext.post(() -> {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(editPostEdittext, InputMethodManager.SHOW_IMPLICIT);
            }));
            editPostEdittext.requestFocus();

            return new EditMessageViewHolder(view);
        } else if (viewType == Poll.TYPE_POLL) {
            View view = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_topic_poll, parent, false);
            return new PollViewHolder(view);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @SuppressLint({"SetJavaScriptEnabled", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder currentHolder,
                                 final int position) {
        if (currentHolder.getItemViewType() == Poll.TYPE_POLL) {
            Poll poll = (Poll) topicItems.get(position);
            Poll.Entry[] entries = poll.getEntries();
            PollViewHolder holder = (PollViewHolder) currentHolder;

            boolean pollSupported = true;
            for (Poll.Entry entry : entries) {
                if (ThmmyParser.containsHtml(entry.getEntryName())) {
                    pollSupported = false;
                    break;
                }
            }
            if (ThmmyParser.containsHtml(poll.getQuestion()))
                pollSupported = false;
            if (entries.length > 30)
                pollSupported = false;
            if (!pollSupported) {
                holder.optionsLayout.setVisibility(View.GONE);
                holder.voteChart.setVisibility(View.GONE);
                holder.selectedEntry.setVisibility(View.GONE);
                holder.removeVotesButton.setVisibility(View.GONE);
                holder.showPollResultsButton.setVisibility(View.GONE);
                holder.hidePollResultsButton.setVisibility(View.GONE);
                // use the submit vote button to open poll on browser
                holder.submitButton.setText("Open in browser");
                holder.submitButton.setOnClickListener(v -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(viewModel.getTopicUrl()));
                    context.startActivity(browserIntent);
                });
                holder.submitButton.setVisibility(View.VISIBLE);
                // put a warning instead of a question
                holder.question.setText("This topic contains a poll that is not supported in mTHMMY");
                return;
            }

            holder.question.setText(poll.getQuestion());
            holder.optionsLayout.removeAllViews();
            holder.errorTextview.setVisibility(View.GONE);

            final int primaryTextColor = context.getResources().getColor(R.color.primary_text);
            final int accentColor = context.getResources().getColor(R.color.accent);

            if (poll.getAvailableVoteCount() > 1) {
                // vote multiple options
                for (Poll.Entry entry : entries) {
                    CheckBox checkBox = new CheckBox(context);
                    checkBox.setMovementMethod(LinkMovementMethod.getInstance());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        checkBox.setText(Html.fromHtml(entry.getEntryName(), Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        //noinspection deprecation
                        checkBox.setText(Html.fromHtml(entry.getEntryName()));
                    }
                    checkBox.setTextColor(primaryTextColor);
                    holder.optionsLayout.addView(checkBox);
                }
                holder.voteChart.setVisibility(View.GONE);
                holder.selectedEntry.setVisibility(View.GONE);
                holder.optionsLayout.setVisibility(View.VISIBLE);
            } else if (poll.getAvailableVoteCount() == 1) {
                // vote single option
                RadioGroup radioGroup = new RadioGroup(context);
                for (int i = 0; i < entries.length; i++) {
                    RadioButton radioButton = new RadioButton(context);
                    radioButton.setId(i);
                    radioButton.setMovementMethod(LinkMovementMethod.getInstance());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        radioButton.setText(Html.fromHtml(entries[i].getEntryName(), Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        //noinspection deprecation
                        radioButton.setText(Html.fromHtml(entries[i].getEntryName()));
                    }
                    radioButton.setText(ThmmyParser.html2span(context, entries[i].getEntryName()));
                    radioButton.setTextColor(primaryTextColor);
                    radioGroup.addView(radioButton);
                }
                holder.optionsLayout.addView(radioGroup);
                holder.voteChart.setVisibility(View.GONE);
                holder.selectedEntry.setVisibility(View.GONE);
                holder.optionsLayout.setVisibility(View.VISIBLE);
            } else if (poll.isPollResultsHidden()) {
                // vote already submitted but results are hidden
                Poll.Entry[] entries1 = poll.getEntries();
                for (int i = 0; i < entries1.length; i++) {
                    Poll.Entry entry = entries1[i];
                    TextView textView = new TextView(context);
                    textView.setMovementMethod(LinkMovementMethod.getInstance());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        textView.setText(Html.fromHtml(entry.getEntryName(), Html.FROM_HTML_MODE_LEGACY));
                    } else {
                        //noinspection deprecation
                        textView.setText(Html.fromHtml(entry.getEntryName()));
                    }
                    textView.setTextColor(primaryTextColor);
                    if (poll.getSelectedEntryIndex() == i) {
                        // apply bold to the selected entry
                        SpannableString spanString = new SpannableString(textView.getText() + " ✓");
                        spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, spanString.length(), 0);
                        textView.setText(spanString);
                        textView.setTextColor(accentColor);
                    }
                    holder.optionsLayout.addView(textView);
                }
                holder.voteChart.setVisibility(View.GONE);
                holder.selectedEntry.setVisibility(View.GONE);
                holder.optionsLayout.setVisibility(View.VISIBLE);
            } else {
                // Showing results
                holder.optionsLayout.setVisibility(View.GONE);

                if (poll.getSelectedEntryIndex() != -1) {
                    holder.selectedEntry.setText("You voted \"" +
                            poll.getEntries()[poll.getSelectedEntryIndex()].getEntryName() + "\"");
                    holder.selectedEntry.setVisibility(View.VISIBLE);
                }

                Arrays.sort(entries, (p1, p2) -> p1.getVotes() - p2.getVotes());
                List<BarEntry> valuesToCompare = new ArrayList<>();
                int totalVotes = 0;
                for (int i = 0; i < entries.length; i++) {
                    valuesToCompare.add(new BarEntry(i, entries[i].getVotes()));
                    totalVotes += entries[i].getVotes();
                }
                BarDataSet dataSet = new BarDataSet(valuesToCompare, "Vote Results");
                dataSet.setColor(accentColor);
                dataSet.setValueTextColor(accentColor);

                YAxis yAxisLeft = holder.voteChart.getAxisLeft();
                yAxisLeft.setGranularity(1);
                yAxisLeft.setTextColor(primaryTextColor);
                yAxisLeft.setAxisMinimum(0);
                yAxisLeft.setSpaceTop(40f);
                YAxis yAxisRight = holder.voteChart.getAxisRight();
                yAxisRight.setEnabled(false);

                XAxis xAxis = holder.voteChart.getXAxis();
                xAxis.setValueFormatter((value, axis) -> Html.fromHtml(entries[(int) value].getEntryName()).toString());
                xAxis.setTextColor(primaryTextColor);
                xAxis.setGranularity(1f);
                xAxis.setLabelCount(entries.length);
                xAxis.setDrawGridLines(false);
                xAxis.setDrawAxisLine(false);
                xAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);

                BarData barData = new BarData(dataSet);
                int finalSum = totalVotes;
                barData.setValueFormatter((value, entry, dataSetIndex, viewPortHandler) -> {
                    DecimalFormat format = new DecimalFormat("###.#%");
                    double percentage = 0;
                    if (finalSum != 0)
                        percentage = ((double) value / (double) finalSum);
                    return "" + (int) value + " (" + format.format(percentage) + ")";
                });
                holder.voteChart.setData(barData);
                holder.voteChart.getLegend().setEnabled(false);
                holder.voteChart.getDescription().setEnabled(false);
                int chartHeightDp = 10 + 30 * entries.length;
                DisplayMetrics metrics = context.getResources().getDisplayMetrics();
                holder.voteChart.setMinimumHeight((int) (chartHeightDp * (metrics.densityDpi / 160f)));
                holder.voteChart.invalidate();
                holder.voteChart.setVisibility(View.VISIBLE);
            }
            if (poll.getRemoveVoteUrl() != null) {
                holder.removeVotesButton.setOnClickListener(v -> viewModel.removeVote());
                holder.removeVotesButton.setVisibility(View.VISIBLE);
            } else holder.removeVotesButton.setVisibility(View.GONE);
            if (poll.getShowVoteResultsUrl() != null) {
                holder.showPollResultsButton.setOnClickListener(v -> viewModel.loadUrl(poll.getShowVoteResultsUrl()));
                holder.showPollResultsButton.setVisibility(View.VISIBLE);
            } else holder.showPollResultsButton.setVisibility(View.GONE);

            if (poll.getShowOptionsUrl() != null) {
                holder.hidePollResultsButton.setOnClickListener(v -> viewModel.loadUrl(poll.getShowOptionsUrl()));
                holder.hidePollResultsButton.setVisibility(View.VISIBLE);
            } else holder.hidePollResultsButton.setVisibility(View.GONE);
            if (poll.getPollFormUrl() != null) {
                holder.submitButton.setOnClickListener(v -> {
                    if (!viewModel.submitVote(holder.optionsLayout)) {
                        holder.errorTextview.setText(context.getResources()
                                .getQuantityString(R.plurals.error_too_many_checked, poll.getAvailableVoteCount(),
                                        poll.getAvailableVoteCount()));
                        holder.errorTextview.setVisibility(View.VISIBLE);
                    }
                });
                holder.submitButton.setVisibility(View.VISIBLE);
            } else holder.submitButton.setVisibility(View.GONE);
        } else {
            Post currentPost = (Post) topicItems.get(position);
            if (currentHolder instanceof PostViewHolder) {
                final PostViewHolder holder = (PostViewHolder) currentHolder;

                //Post's WebView parameters
                holder.post.setClickable(true);
                holder.post.setWebViewClient(new LinkLauncher());

                //noinspection ConstantConditions
                loadAvatar(currentPost.getThumbnailURL(), holder.thumbnail, holder.itemView.getContext());

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
                            attached.setText(faIconFromFilename(context, attachedFile.getFilename()) + " "
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
                if (mUserColor != USER_COLOR_YELLOW)
                    holder.username.setTextColor(mUserColor);
                else
                    holder.username.setTextColor(USER_COLOR_WHITE);

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

                if (currentPost.isUserMentionedInPost()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.cardChildLinear.setBackground(context.getResources().
                                getDrawable(R.drawable.mention_card, null));
                    } else //noinspection deprecation
                        holder.cardChildLinear.setBackground(context.getResources().
                                getDrawable(R.drawable.mention_card));
                } else if (mUserColor == TopicParser.USER_COLOR_PINK) {
                    //Special card for special member of the month!
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
                    if (layoutInflater == null)
                        return;

                    View popUpContent = layoutInflater.inflate(R.layout.activity_topic_overflow_menu, null);

                    //Creates the PopupWindow
                    final PopupWindow popUp = new PopupWindow(holder.overflowButton.getContext());
                    popUp.setContentView(popUpContent);
                    popUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
                    popUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
                    popUp.setFocusable(true);

                    TextView shareButton = popUpContent.findViewById(R.id.post_share_button);
                    Drawable shareStartDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_share_white_24dp);
                    shareButton.setCompoundDrawablesRelativeWithIntrinsicBounds(shareStartDrawable, null, null, null);
                    shareButton.setOnClickListener(v -> {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_TEXT, currentPost.getPostURL());
                        context.startActivity(Intent.createChooser(sendIntent, "Share via"));
                        popUp.dismiss();
                    });

                    final TextView editPostButton = popUpContent.findViewById(R.id.edit_post);
                    Drawable editStartDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_edit_white_24dp);
                    editPostButton.setCompoundDrawablesRelativeWithIntrinsicBounds(editStartDrawable, null, null, null);

                    if (viewModel.isEditingPost() || currentPost.getPostEditURL() == null || currentPost.getPostEditURL().equals(""))
                        editPostButton.setVisibility(View.GONE);
                    else {
                        editPostButton.setOnClickListener(v -> {
                            viewModel.prepareForEdit(position, currentPost.getPostEditURL());
                            popUp.dismiss();
                        });
                    }

                    TextView deletePostButton = popUpContent.findViewById(R.id.delete_post);

                    if (currentPost.getPostDeleteURL() == null || currentPost.getPostDeleteURL().equals(""))
                        deletePostButton.setVisibility(View.GONE);
                    else {
                        Drawable deleteStartDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_delete_white_24dp);
                        deletePostButton.setCompoundDrawablesRelativeWithIntrinsicBounds(deleteStartDrawable, null, null, null);
                        popUpContent.findViewById(R.id.delete_post).setOnClickListener(v -> {
                            new AlertDialog.Builder(holder.overflowButton.getContext())
                                    .setTitle("Delete post")
                                    .setMessage("Do you really want to delete this post?")
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> viewModel.deletePost(currentPost.getPostDeleteURL()))
                                    .setNegativeButton(android.R.string.no, null).show();
                            popUp.dismiss();
                        });
                    }

                    //Displays the popup
                    popUp.showAsDropDown(holder.overflowButton);
                });

                //noinspection PointlessBooleanExpression,ConstantConditions
                if (!BaseActivity.getSessionManager().isLoggedIn() || !viewModel.canReply())
                    holder.quoteToggle.setVisibility(View.GONE);
                else {
                    if (viewModel.getToQuoteList().contains(currentPost.getPostIndex()))
                        holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_checked_accent_24dp);
                    else
                        holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_unchecked_24dp);
                    //Sets graphics behavior
                    holder.quoteToggle.setOnClickListener(view -> {
                        viewModel.postIndexToggle(currentPost.getPostIndex());
                        if (viewModel.getToQuoteList().contains(currentPost.getPostIndex()))
                            holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_checked_accent_24dp);
                        else
                            holder.quoteToggle.setImageResource(R.drawable.ic_format_quote_unchecked_24dp);
                    });
                }
            } else if (currentHolder instanceof QuickReplyViewHolder) {
                final QuickReplyViewHolder holder = (QuickReplyViewHolder) currentHolder;
                Post reply = (Post) topicItems.get(position);

                //noinspection ConstantConditions
                loadAvatar(getSessionManager().getAvatarLink(), holder.thumbnail, holder.itemView.getContext());

                holder.username.setText(getSessionManager().getUsername());
                holder.itemView.setAlpha(1f);
                holder.itemView.setEnabled(true);
                if (reply.getSubject() != null) {
                    holder.quickReplySubject.setText(reply.getSubject());
                } else {
                    holder.quickReplySubject.setText("Re: " + viewModel.getTopicTitle().getValue());
                }
                holder.quickReplySubject.setRawInputType(InputType.TYPE_CLASS_TEXT);
                holder.quickReplySubject.setImeOptions(EditorInfo.IME_ACTION_DONE);

                holder.replyEditor.setEmojiKeyboard(emojiKeyboard);
                holder.replyEditor.requestEditTextFocus();
                emojiKeyboard.registerEmojiInputField(holder.replyEditor);

                holder.replyEditor.setOnSubmitListener(view -> {
                    if (holder.quickReplySubject.getText().toString().isEmpty()) return;
                    if (holder.replyEditor.getText().toString().isEmpty()) {
                        holder.replyEditor.setError("Required");
                        return;
                    }
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    holder.itemView.setAlpha(0.5f);
                    holder.itemView.setEnabled(false);
                    emojiKeyboard.hide();

                    SharedPreferences drafts = context.getSharedPreferences(context.getString(R.string.pref_topic_drafts_key),
                            Context.MODE_PRIVATE);
                    drafts.edit().remove(String.valueOf(viewModel.getTopicId())).apply();

                    viewModel.postReply(context, holder.quickReplySubject.getText().toString(),
                            holder.replyEditor.getText().toString());
                });
                holder.replyEditor.setOnClickListener(view -> holder.replyEditor.setError(null));

                String replyText = "";

                if (reply.getBbContent() != null)
                    replyText += reply.getBbContent();
                else {
                    SharedPreferences drafts = context.getSharedPreferences(context.getString(R.string.pref_topic_drafts_key),
                            Context.MODE_PRIVATE);
                    replyText += drafts.getString(String.valueOf(viewModel.getTopicId()), "");
                    if (viewModel.getBuildedQuotes() != null && !viewModel.getBuildedQuotes().isEmpty())
                        replyText += viewModel.getBuildedQuotes();
                }
                holder.replyEditor.setText(replyText);
                holder.replyEditor.getEditText().setSelection(holder.replyEditor.getText().length());
                holder.replyEditor.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        ((Post) topicItems.get(holder.getAdapterPosition())).setBbContent(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });

                if (backPressHidden) {
                    holder.replyEditor.requestEditTextFocus();
                    backPressHidden = false;
                }
                holder.quickReplySubject.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        ((Post) topicItems.get(holder.getAdapterPosition())).setSubject(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
            } else if (currentHolder instanceof EditMessageViewHolder) {
                final EditMessageViewHolder holder = (EditMessageViewHolder) currentHolder;

                //noinspection ConstantConditions
                loadAvatar(getSessionManager().getAvatarLink(), holder.thumbnail,  holder.itemView.getContext());

                holder.username.setText(getSessionManager().getUsername());
                holder.editSubject.setText(currentPost.getSubject());
                holder.editSubject.setRawInputType(InputType.TYPE_CLASS_TEXT);
                holder.editSubject.setImeOptions(EditorInfo.IME_ACTION_DONE);

                holder.editEditor.setEmojiKeyboard(emojiKeyboard);
                holder.editEditor.requestEditTextFocus();
                emojiKeyboard.registerEmojiInputField(holder.editEditor);
                if (currentPost.getBbContent() == null)
                    holder.editEditor.setText(viewModel.getPostBeingEditedText());
                else
                    holder.editEditor.setText(currentPost.getBbContent());
                holder.editEditor.getEditText().setSelection(holder.editEditor.getText().length());
                holder.editEditor.setOnSubmitListener(view -> {
                    if (holder.editSubject.getText().toString().isEmpty()) return;
                    if (holder.editEditor.getText().toString().isEmpty()) {
                        holder.editEditor.setError("Required");
                        return;
                    }
                    InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    holder.itemView.setAlpha(0.5f);
                    holder.itemView.setEnabled(false);
                    emojiKeyboard.hide();

                    viewModel.editPost(position, holder.editSubject.getText().toString(), holder.editEditor.getText().toString());
                });

                holder.editSubject.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        ((Post) topicItems.get(holder.getAdapterPosition())).setSubject(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                holder.editEditor.getEditText().addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                        ((Post) topicItems.get(holder.getAdapterPosition())).setBbContent(charSequence.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }
                });
                if (backPressHidden) {
                    holder.editEditor.requestEditTextFocus();
                    backPressHidden = false;
                }
            }
        }
    }

    private void loadAvatar(String imageUrl, ImageView imageView, Context context) {
        if(imageUrl!=null)
            imageUrl = imageUrl.trim();

        if(isValidContextForGlide(context)) {
            Glide.with(context)
                    .load(imageUrl)
                    .circleCrop()
                    .error(R.drawable.ic_default_user_avatar_darker)
                    .placeholder(R.drawable.ic_default_user_avatar_darker)
                    .into(imageView);
        }
    }

    @Override
    public int getItemCount() {
        return topicItems.size();
    }

    /**
     * Custom {@link RecyclerView.ViewHolder} implementation
     */
    private class PostViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout cardChildLinear;
        final TextView postDate, postNum, username, subject;
        final ImageView thumbnail;
        final public ReactiveWebView post;
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
    static class QuickReplyViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView username;
        final EditText quickReplySubject;
        final EditorView replyEditor;

        QuickReplyViewHolder(View quickReply) {
            super(quickReply);
            thumbnail = quickReply.findViewById(R.id.thumbnail);
            username = quickReply.findViewById(R.id.username);
            quickReplySubject = quickReply.findViewById(R.id.quick_reply_subject);
            replyEditor = quickReply.findViewById(R.id.reply_editorview);
        }
    }

    static class EditMessageViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView username;
        final EditText editSubject;
        final EditorView editEditor;

        EditMessageViewHolder(View editView) {
            super(editView);

            thumbnail = editView.findViewById(R.id.thumbnail);
            username = editView.findViewById(R.id.username);
            editSubject = editView.findViewById(R.id.edit_message_subject);
            editEditor = editView.findViewById(R.id.edit_editorview);
        }
    }

    static class PollViewHolder extends RecyclerView.ViewHolder {
        final TextView question, errorTextview, selectedEntry;
        final LinearLayout optionsLayout;
        final AppCompatButton submitButton;
        final AppCompatButton removeVotesButton, showPollResultsButton, hidePollResultsButton;
        final HorizontalBarChart voteChart;

        PollViewHolder(View itemView) {
            super(itemView);

            question = itemView.findViewById(R.id.question_textview);
            optionsLayout = itemView.findViewById(R.id.options_layout);
            submitButton = itemView.findViewById(R.id.submit_button);
            removeVotesButton = itemView.findViewById(R.id.remove_vote_button);
            showPollResultsButton = itemView.findViewById(R.id.show_poll_results_button);
            hidePollResultsButton = itemView.findViewById(R.id.show_poll_options_button);
            errorTextview = itemView.findViewById(R.id.error_too_many_checked);
            voteChart = itemView.findViewById(R.id.vote_chart);
            selectedEntry = itemView.findViewById(R.id.selected_entry_textview);
            voteChart.setScaleYEnabled(false);
            voteChart.setDoubleTapToZoomEnabled(false);
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
                if (uriString.contains(ParseHelpers.getBaseURL(viewModel.getTopicUrl()))) {
                    if (uriString.contains("topicseen#new") || uriString.contains("#new")) {
                        if (viewModel.getCurrentPageIndex() == viewModel.getPageCount()) {
                            //same page
                            postFocusListener.onPostFocusChange(getItemCount() - 1);
                            Timber.d("new");
                            return true;
                        }
                    }
                    if (uriString.contains("msg")) {
                        String tmpUrlSbstr = uriString.substring(uriString.indexOf("msg") + 3);
                        if (tmpUrlSbstr.contains("msg"))
                            tmpUrlSbstr = tmpUrlSbstr.substring(0, tmpUrlSbstr.indexOf("msg") - 1);
                        int testAgainst = Integer.parseInt(tmpUrlSbstr);
                        for (int i = 0; i < topicItems.size(); i++) {
                            if (topicItems.get(i) instanceof Post && ((Post) topicItems.get(i)).getPostIndex() == testAgainst) {
                                //same page
                                postFocusListener.onPostFocusChange(i);
                                return true;
                            }
                        }
                    } else if ((Objects.equals(uriString, ParseHelpers.getBaseURL(viewModel.getTopicUrl())) &&
                            viewModel.getCurrentPageIndex() == 1) ||
                            Integer.parseInt(uriString.substring(ParseHelpers.getBaseURL(viewModel.getTopicUrl()).length() + 1)) / 15 + 1 ==
                                    viewModel.getCurrentPageIndex()) {
                        //same page
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
}