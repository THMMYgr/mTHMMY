package gr.thmmy.mthmmy.activities.inbox;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.create_pm.CreatePMActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.model.PM;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.CircleTransform;
import gr.thmmy.mthmmy.utils.MessageAnimations;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import gr.thmmy.mthmmy.viewmodel.InboxViewModel;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;
import static gr.thmmy.mthmmy.utils.parsing.ParseHelpers.USER_COLOR_WHITE;
import static gr.thmmy.mthmmy.utils.parsing.ParseHelpers.USER_COLOR_YELLOW;

public class InboxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private InboxViewModel inboxViewModel;

    public InboxAdapter(InboxActivity context) {
        this.context = context;
        inboxViewModel = ViewModelProviders.of(context).get(InboxViewModel.class);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_inbox_pm_row, parent, false);
        return new InboxAdapter.PMViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
        InboxAdapter.PMViewHolder holder = (PMViewHolder) viewHolder;
        PM currentPM = pms().get(position);

        //Post's WebView parameters
        holder.pm.setClickable(true);
        holder.pm.setWebViewClient(new LinkLauncher());

        Picasso.with(context)
                .load(currentPM.getThumbnailUrl())
                .fit()
                .centerCrop()
                .error(Objects.requireNonNull(ResourcesCompat.getDrawable(context.getResources()
                        , R.drawable.ic_default_user_avatar_darker, null)))
                .placeholder(Objects.requireNonNull(ResourcesCompat.getDrawable(context.getResources()
                        , R.drawable.ic_default_user_avatar_darker, null)))
                .transform(new CircleTransform())
                .into(holder.thumbnail);

        //Sets username,submit date, index number, subject, post's and attached files texts
        holder.username.setText(currentPM.getAuthor());
        holder.pmDate.setText(currentPM.getPmDate());
        holder.subject.setText(currentPM.getSubject());
        holder.pm.loadDataWithBaseURL("file:///android_asset/", currentPM.getContent(),
                "text/html", "UTF-8", null);

        // author info
        if (currentPM.getAuthorSpecialRank() != null && !currentPM.getAuthorSpecialRank().equals("")) {
            holder.specialRank.setText(currentPM.getAuthorSpecialRank());
            holder.specialRank.setVisibility(View.VISIBLE);
        } else holder.specialRank.setVisibility(View.GONE);
        if (currentPM.getAuthorRank() != null && !currentPM.getAuthorRank().equals("")) {
            holder.rank.setText(currentPM.getAuthorRank());
            holder.rank.setVisibility(View.VISIBLE);
        } else holder.rank.setVisibility(View.GONE);
        if (currentPM.getAuthorGender() != null && !currentPM.getAuthorGender().equals("")) {
            holder.gender.setText(currentPM.getAuthorGender());
            holder.gender.setVisibility(View.VISIBLE);
        } else holder.gender.setVisibility(View.GONE);
        if (currentPM.getAuthorNumberOfPosts() != null && !currentPM.getAuthorNumberOfPosts().equals("")) {
            holder.numberOfPosts.setText(currentPM.getAuthorNumberOfPosts());
            holder.numberOfPosts.setVisibility(View.VISIBLE);
        } else holder.numberOfPosts.setVisibility(View.GONE);
        if (currentPM.getAuthorPersonalText() != null && !currentPM.getAuthorPersonalText().equals("")) {
            holder.personalText.setText(currentPM.getAuthorPersonalText());
            holder.personalText.setVisibility(View.VISIBLE);
        } else holder.personalText.setVisibility(View.GONE);
        if (currentPM.getAuthorColor() != USER_COLOR_YELLOW)
            holder.username.setTextColor(currentPM.getAuthorColor());
        else holder.username.setTextColor(USER_COLOR_WHITE);

        if (currentPM.getAuthorNumberOfStars() > 0) {
            holder.stars.setTypeface(Typeface.createFromAsset(context.getAssets()
                    , "fonts/fontawesome-webfont.ttf"));

            String aStar = context.getResources().getString(R.string.fa_icon_star);
            StringBuilder usersStars = new StringBuilder();
            for (int i = 0; i < currentPM.getAuthorNumberOfStars(); ++i) {
                usersStars.append(aStar);
            }
            holder.stars.setText(usersStars.toString());
            holder.stars.setTextColor(currentPM.getAuthorColor());
            holder.stars.setVisibility(View.VISIBLE);
        } else holder.stars.setVisibility(View.GONE);

        // in the context of inbox there is no point in highlighting quoted PMs
        /*if (currentPM.isUserMentioned()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.cardChildLinear.setBackground(context.getResources().
                        getDrawable(R.drawable.mention_card, null));
            } else
                holder.cardChildLinear.setBackground(context.getResources().
                        getDrawable(R.drawable.mention_card));
        } else */
        if (currentPM.getAuthorColor() == ParseHelpers.USER_COLOR_PINK) {
            //Special card for special member of the month!
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                holder.cardChildLinear.setBackground(context.getResources().
                        getDrawable(R.drawable.member_of_the_month_card, null));
            } else
                holder.cardChildLinear.setBackground(context.getResources().
                        getDrawable(R.drawable.member_of_the_month_card));
        } else holder.cardChildLinear.setBackground(null);

        //Avoid's view's visibility recycling
        if (inboxViewModel.isUserExtraInfoVisible(holder.getAdapterPosition())) {
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

        //Sets graphics behavior
        holder.thumbnail.setOnClickListener(view -> {
            //Clicking the thumbnail opens user's profile
            Intent intent = new Intent(context, ProfileActivity.class);
            Bundle extras = new Bundle();
            extras.putString(BUNDLE_PROFILE_URL, currentPM.getAuthorProfileUrl());
            if (currentPM.getThumbnailUrl() == null)
                extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, "");
            else
                extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, currentPM.getAuthorProfileUrl());
            extras.putString(BUNDLE_PROFILE_USERNAME, currentPM.getAuthor());
            intent.putExtras(extras);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        holder.header.setOnClickListener(v -> {
            //Clicking the header makes it expand/collapse
            inboxViewModel.toggleUserInfo(holder.getAdapterPosition());
            MessageAnimations.animateUserExtraInfoVisibility(holder.username,
                    holder.subject, Color.parseColor("#FFFFFF"),
                    Color.parseColor("#757575"), holder.userExtraInfo);
        });
        //Clicking the expanded part of a header (the extra info) makes it collapse
        holder.userExtraInfo.setOnClickListener(v -> {
            inboxViewModel.hideUserInfo(holder.getAdapterPosition());
            MessageAnimations.animateUserExtraInfoVisibility(holder.username,
                    holder.subject, Color.parseColor("#FFFFFF"),
                    Color.parseColor("#757575"), (LinearLayout) v);
        });

        holder.overflowButton.setOnClickListener(view -> {
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            View popupContent = layoutInflater.inflate(R.layout.activity_inbox_overflow_menu, null);

            //Creates the PopupWindow
            final PopupWindow popUp = new PopupWindow(holder.overflowButton.getContext());
            popUp.setContentView(popupContent);
            popUp.setWidth(LinearLayout.LayoutParams.WRAP_CONTENT);
            popUp.setHeight(LinearLayout.LayoutParams.WRAP_CONTENT);
            popUp.setFocusable(true);

            TextView quoteButton = popupContent.findViewById(R.id.pm_quote_button);
            quoteButton.setVisibility(View.GONE); // TODO
            Drawable quoteDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_format_quote);
            quoteButton.setCompoundDrawablesRelativeWithIntrinsicBounds(quoteDrawable, null, null, null);
            quoteButton.setOnClickListener(v -> {
                Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show();
                // TODO: Create delete PM task
            });

            final TextView replyButton = popupContent.findViewById(R.id.pm_reply_button);
            Drawable replyDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_reply);
            replyButton.setCompoundDrawablesRelativeWithIntrinsicBounds(replyDrawable, null, null, null);

            replyButton.setOnClickListener(v -> {
                Intent sendPMIntent = new Intent(context, CreatePMActivity.class);
                sendPMIntent.putExtra(CreatePMActivity.BUNDLE_SEND_PM_URL, currentPM.getReplyUrl());
                context.startActivity(sendPMIntent);
                popUp.dismiss();
            });

            TextView deletePostButton = popupContent.findViewById(R.id.delete_post);

            Drawable deleteStartDrawable = AppCompatResources.getDrawable(context, R.drawable.ic_delete_white_24dp);
            deletePostButton.setVisibility(View.GONE); //TODO
            deletePostButton.setCompoundDrawablesRelativeWithIntrinsicBounds(deleteStartDrawable, null, null, null);
            popupContent.findViewById(R.id.delete_post).setOnClickListener(v -> {
                new AlertDialog.Builder(holder.overflowButton.getContext())
                        .setTitle("Delete personal message")
                        .setMessage("Do you really want to delete this personal message?")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton(android.R.string.yes, (dialog, whichButton) -> {
                            Toast.makeText(context, "TODO", Toast.LENGTH_SHORT).show();
                            // TODO: Create delete PM task
                        })
                        .setNegativeButton(android.R.string.no, null).show();
                popUp.dismiss();
            });

            //Displays the popup
            popUp.showAsDropDown(holder.overflowButton);
        });
    }

    @Override
    public int getItemCount() {
        return inboxViewModel.getInbox() == null ? 0 : pms().size();
    }

    private ArrayList<PM> pms() {
        return inboxViewModel.getInbox().getPms();
    }

    static class PMViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout cardChildLinear;
        final TextView pmDate, username, subject;
        final ImageView thumbnail;
        final public WebView pm;
        final ImageButton overflowButton;
        final RelativeLayout header;
        final LinearLayout userExtraInfo;

        final TextView specialRank, rank, gender, numberOfPosts, personalText, stars;

        PMViewHolder(@NonNull View view) {
            super(view);
            cardChildLinear = view.findViewById(R.id.card_child_linear);
            pmDate = view.findViewById(R.id.pm_date);
            thumbnail = view.findViewById(R.id.thumbnail);
            username = view.findViewById(R.id.username);
            subject = view.findViewById(R.id.subject);
            pm = view.findViewById(R.id.pm);
            pm.setBackgroundColor(Color.argb(1, 255, 255, 255));
            overflowButton = view.findViewById(R.id.pm_overflow_menu);

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

    /**
     * This class is used to handle link clicks in WebViews. When link url is one that the app can
     * handle internally, it does. Otherwise user is prompt to open the link in a browser.
     */
    private class LinkLauncher extends WebViewClient {
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
}
