package gr.thmmy.mthmmy.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.View;

import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.main.MainActivity;
import gr.thmmy.mthmmy.activities.profile.ProfileActivity;
import gr.thmmy.mthmmy.model.ThmmyPage;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_THUMBNAIL_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_URL;
import static gr.thmmy.mthmmy.activities.profile.ProfileActivity.BUNDLE_PROFILE_USERNAME;

public class HTMLUtils {
    private HTMLUtils() {}

    public static SpannableStringBuilder getSpannableFromHtml(Activity activity, String html) {
        CharSequence sequence;
        if (Build.VERSION.SDK_INT >= 24) {
            sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            sequence = Html.fromHtml(html);
        }
        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
        for (URLSpan span : urls) {
            makeLinkClickable(activity, strBuilder, span);
        }
        return strBuilder;
    }

    public static void makeLinkClickable(Context context, SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                ThmmyPage.PageCategory target = ThmmyPage.resolvePageCategory(Uri.parse(span.getURL()));
                if (target.is(ThmmyPage.PageCategory.BOARD)) {
                    Intent intent = new Intent(context, BoardActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString(BUNDLE_BOARD_URL, span.getURL());
                    extras.putString(BUNDLE_BOARD_TITLE, "");
                    intent.putExtras(extras);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else if (target.is(ThmmyPage.PageCategory.PROFILE)) {
                    Intent intent = new Intent(context, ProfileActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString(BUNDLE_PROFILE_URL, span.getURL());
                    extras.putString(BUNDLE_PROFILE_THUMBNAIL_URL, "");
                    extras.putString(BUNDLE_PROFILE_USERNAME, "");
                    intent.putExtras(extras);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } else if (target.is(ThmmyPage.PageCategory.INDEX)) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }
}
