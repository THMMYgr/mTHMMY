package gr.thmmy.mthmmy.activities.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import java.util.ArrayList;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.board.BoardActivity;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.model.Bookmark;

import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_TITLE;
import static gr.thmmy.mthmmy.activities.board.BoardActivity.BUNDLE_BOARD_URL;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;
import static gr.thmmy.mthmmy.session.SessionManager.boardUrl;
import static gr.thmmy.mthmmy.session.SessionManager.topicUrl;

public class WidgetProvider extends AppWidgetProvider {
    public static final String BOOKMARK_WIDGET_SHARED_PREFS = "bookmarkWidgetSharedPrefs";
    public static final String BOOKMARK_WIDGETS_KEY = "bookmarkWidgetsKey";

    enum Type {TOPIC, BOARD}

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId, 0); // Todo: check if there are already notifications available
        }
    }

    public void onDeleted(Context context, int[] appWidgetIds) {
        SharedPreferences widgetSharedPrefs = context.getSharedPreferences(BOOKMARK_WIDGET_SHARED_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor widgetSharedPrefsEditor = widgetSharedPrefs.edit();

        for (int appWidgetId : appWidgetIds) {
            widgetSharedPrefsEditor.remove(BOOKMARK_WIDGETS_KEY + "_t_" + appWidgetId).remove(BOOKMARK_WIDGETS_KEY + "_b_" + appWidgetId);
        }
        widgetSharedPrefsEditor.apply();
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, int notifications) {
        SharedPreferences widgetSharedPrefs = context.getSharedPreferences(BOOKMARK_WIDGET_SHARED_PREFS, Context.MODE_PRIVATE);
        ArrayList<Bookmark> tmpArrayList;

        // Gets the bookmark saved in shared prefs
        String tmpBookmarkString = widgetSharedPrefs.getString(BOOKMARK_WIDGETS_KEY + "_t_" + appWidgetId, null);
        Type type;

        if (tmpBookmarkString != null) {
            // It's a topic bookmark
            tmpArrayList = Bookmark.stringToArrayList(tmpBookmarkString);
            type = Type.TOPIC;
        } else {
            tmpBookmarkString = widgetSharedPrefs.getString(BOOKMARK_WIDGETS_KEY + "_b_" + appWidgetId, null);

            if (tmpBookmarkString != null) {
                // It's a board bookmark
                tmpArrayList = Bookmark.stringToArrayList(tmpBookmarkString);
                type = Type.BOARD;
            } else {
                // Error? TODO: Log on Timber
                return;
            }
        }

        // Creates an Intent to launch TopicActivity
        Intent intent;
        Bundle extras = new Bundle();
        if (type == Type.TOPIC) {
            intent = new Intent(context, TopicActivity.class);
            extras.putString(BUNDLE_TOPIC_URL, topicUrl + tmpArrayList.get(0).getId() + "." + 2147483647);
            extras.putString(BUNDLE_TOPIC_TITLE, tmpArrayList.get(0).getTitle());
        } else {
            intent = new Intent(context, BoardActivity.class);
            extras.putString(BUNDLE_BOARD_URL, boardUrl + tmpArrayList.get(0).getId() + ".0");
            extras.putString(BUNDLE_BOARD_TITLE, tmpArrayList.get(0).getTitle());
        }
        intent.putExtras(extras);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        // Gets the layout for the Topic Widget and attach an on-click listener to the button
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
        views.setOnClickPendingIntent(R.id.widget_button, pendingIntent);
        if (notifications > 0) {
            views.setViewVisibility(R.id.widget_notifications_number, View.VISIBLE);
            views.setTextViewText(R.id.widget_notifications_number, "" + notifications);
        } else {
            views.setViewVisibility(R.id.widget_notifications_number, View.GONE);
        }

        // Tells the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
