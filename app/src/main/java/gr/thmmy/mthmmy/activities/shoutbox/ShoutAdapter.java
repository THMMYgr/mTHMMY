package gr.thmmy.mthmmy.activities.shoutbox;

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
import android.widget.TextView;

import androidx.annotation.NonNull;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.model.ThmmyPage;
import gr.thmmy.mthmmy.views.CustomRecyclerView;
import gr.thmmy.mthmmy.views.ReactiveWebView;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

public class ShoutAdapter extends CustomRecyclerView.Adapter<ShoutAdapter.ShoutViewHolder> {
    private Context context;
    private Shout[] shouts;

    ShoutAdapter(Context context, Shout[] shouts) {
        this.context = context;
        this.shouts = shouts;
    }

    void setShouts(Shout[] shouts) {
        this.shouts = shouts;
    }

    @NonNull
    @Override
    public ShoutViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_shoutbox_shout_row, parent, false);
        return new ShoutViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoutViewHolder holder, int position) {
        Shout currentShout = shouts[position];
        holder.author.setText(currentShout.getShouter());
        if (currentShout.isMemberOfTheMonth()) holder.author.setTextColor(context.getResources().getColor(R.color.member_of_the_month));
        else holder.author.setTextColor(context.getResources().getColor(R.color.accent));
        holder.author.setOnClickListener(view -> {
            Intent intent = new Intent(context, ProfileActivity.class);
            Bundle extras = new Bundle();
            extras.putString(BUNDLE_PROFILE_URL, shouts[holder.getAdapterPosition()].getShouterProfileURL());
            extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, "");
            extras.putString(BUNDLE_PROFILE_USERNAME, "");
            intent.putExtras(extras);
            intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        holder.dateTime.setText(currentShout.getDate());
        holder.shoutContent.setClickable(true);
        holder.shoutContent.setWebViewClient(new LinkLauncher());
        holder.shoutContent.loadDataWithBaseURL("file:///android_asset/", currentShout.getShout(),
                "text/html", "UTF-8", null);
    }

    @Override
    public int getItemCount() {
        return shouts.length;
    }

    static class ShoutViewHolder extends CustomRecyclerView.ViewHolder {

        TextView author, dateTime;
        ReactiveWebView shoutContent;

        ShoutViewHolder(@NonNull View itemView) {
            super(itemView);
            author = itemView.findViewById(R.id.author_textview);
            dateTime = itemView.findViewById(R.id.date_time_textview);
            shoutContent = itemView.findViewById(R.id.shout_content);
            shoutContent.setBackgroundColor(Color.argb(1, 255, 255, 255));
        }
    }

    class LinkLauncher extends WebViewClient {

        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            final Uri uri = request.getUrl();
            return handleUri(uri);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            final Uri uri = Uri.parse(url);
            return handleUri(uri);
        }

        private boolean handleUri(Uri uri) {
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
