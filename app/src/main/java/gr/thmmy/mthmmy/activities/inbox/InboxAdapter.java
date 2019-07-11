package gr.thmmy.mthmmy.activities.inbox;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.res.ResourcesCompat;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.model.PM;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.utils.CircleTransform;
import gr.thmmy.mthmmy.viewmodel.InboxViewModel;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

public class InboxAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private InboxViewModel inboxViewModel;

    public InboxAdapter(InboxActivity context) {
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
    }

    @Override
    public int getItemCount() {
        return pms().size();
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
