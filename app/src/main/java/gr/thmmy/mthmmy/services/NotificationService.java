package gr.thmmy.mthmmy.services;

import static androidx.core.app.NotificationCompat.PRIORITY_MAX;
import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.NOTIFICATION_LED_KEY;
import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.NOTIFICATION_VIBRATION_KEY;
import static gr.thmmy.mthmmy.activities.settings.SettingsFragment.SELECTED_RINGTONE;
import static gr.thmmy.mthmmy.activities.settings.SettingsFragment.SETTINGS_SHARED_PREFS;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;
import static gr.thmmy.mthmmy.base.BaseActivity.BOOKMARKED_BOARDS_KEY;
import static gr.thmmy.mthmmy.base.BaseActivity.BOOKMARKED_TOPICS_KEY;
import static gr.thmmy.mthmmy.base.BaseActivity.BOOKMARKS_SHARED_PREFS;
import static gr.thmmy.mthmmy.model.Bookmark.matchExistsById;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;

import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.main.MainActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.Bookmark;
import gr.thmmy.mthmmy.model.PostNotification;
import timber.log.Timber;

public class NotificationService extends FirebaseMessagingService {
    private static final int buildVersion = Build.VERSION.SDK_INT;
    private static final int disabledNotifiationsLedColor = Color.argb(0, 0, 0, 0);

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            Timber.i("FCM data message received.");
            JSONObject json = new JSONObject(remoteMessage.getData());
            try {
                int userId = BaseApplication.getInstance().getSessionManager().getUserId();
                //Don't notify me if the sender is me!
                if (Integer.parseInt(json.getString("posterId")) != userId) {
                    int boardId = -1;
                    String boardTitle = null;
                    int topicId = Integer.parseInt(json.getString("topicId"));
                    if (remoteMessage.getFrom().contains("b")) {
                        Timber.i("FCM BOARD type message detected.");

                        SharedPreferences bookmarksFile = getSharedPreferences(BOOKMARKS_SHARED_PREFS, Context.MODE_PRIVATE);
                        String bookmarkedTopicsString = bookmarksFile.getString(BOOKMARKED_TOPICS_KEY, null);
                        if (bookmarkedTopicsString != null && matchExistsById(Bookmark.stringToArrayList(bookmarkedTopicsString), topicId)) {
                            Timber.i("Board notification suppressed (already subscribed to topic).");
                            return;
                        }

                        boardId = Integer.parseInt(json.getString("boardId"));

                        String bookmarkedBoardsString = bookmarksFile.getString(BOOKMARKED_BOARDS_KEY, null);
                        if (bookmarkedBoardsString != null) {
                            ArrayList<Bookmark> boardBookmarks = Bookmark.stringToArrayList(bookmarkedBoardsString);
                            ArrayList<Integer> subBoardIds = getSubBoardIds(json.getString("boardIds"), boardId);
                            //TODO: Also suppress if user has chosen to be notified only for direct children of boardId && !subBoardIds.isEmpty()
                            for (int subId : subBoardIds) {
                                if (matchExistsById(boardBookmarks, subId)) {
                                    Timber.i("Board notification suppressed (already subscribed to a subBoard).");
                                    return;
                                }
                            }
                        }

                        boardTitle = json.getString("boardTitle");
                    }
                    else
                        Timber.i("FCM TOPIC type message detected.");

                    int postId = Integer.parseInt(json.getString("postId"));
                    String topicTitle = json.getString("topicTitle");
                    String poster = json.getString("poster");

                    sendNotification(new PostNotification(postId, topicId, topicTitle, poster, boardId, boardTitle));
                }
                else
                    Timber.i("Notification suppressed (own userID).");
            } catch (JSONException e) {
                Timber.e(e, "JSON Exception");
            }
        }
    }

    private static ArrayList<Integer> getSubBoardIds(String boardIdsString, int boardId) {
        ArrayList<Integer> subBoardIds = new ArrayList<>();
        Pattern p = Pattern.compile("(\\d+)");
        Matcher m = p.matcher(boardIdsString);
        boolean boardIdfound = false;
        while (m.find()) {
            int subBoardId = Integer.parseInt(m.group());
            if (boardIdfound)
                subBoardIds.add(subBoardId);
            else if (boardId == subBoardId)
                boardIdfound = true;
        }
        return subBoardIds;
    }

    private static final String CHANNEL_ID = "Posts";
    private static final String CHANNEL_NAME = "New Posts";
    private static final String GROUP_KEY = "PostsGroup";
    private static int requestCode = 0;

    private static final String NEW_POSTS_COUNT = "newPostsCount";
    public static final String NEW_POST_TAG = "NEW_POST";
    private static final String SUMMARY_TAG = "SUMMARY";

    /**
     * Create and show a new post notification.
     */
    private void sendNotification(PostNotification postNotification) {
        Timber.i("Creating a notification...");

        boolean isTopicNotification = postNotification.getBoardId() == -1;

        //Reads notifications preferences
        SharedPreferences settingsFile = getSharedPreferences(SETTINGS_SHARED_PREFS, Context.MODE_PRIVATE);
        String notificationsSound = settingsFile.getString(SELECTED_RINGTONE, null);
        Uri notificationSoundUri = notificationsSound != null ? Uri.parse(notificationsSound) : null;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean notificationsLedEnabled = sharedPrefs.getBoolean(NOTIFICATION_LED_KEY, true);
        boolean notificationsVibrateEnabled = sharedPrefs.getBoolean(NOTIFICATION_VIBRATION_KEY, true);
        int notificationDefaultValues = -1;

        if (notificationsLedEnabled) {
            notificationDefaultValues = Notification.DEFAULT_LIGHTS;
        }
        if (notificationsVibrateEnabled) {
            if (notificationDefaultValues != -1) {
                notificationDefaultValues |= Notification.DEFAULT_VIBRATE;
            }
            else {
                notificationDefaultValues = Notification.DEFAULT_VIBRATE;
            }

        }
        if (notificationSoundUri == null) {
            if (notificationDefaultValues != -1) {
                notificationDefaultValues = Notification.DEFAULT_SOUND;
            }
            else {
                notificationDefaultValues |= Notification.DEFAULT_SOUND;
            }
        }

        //Builds notification
        String topicUrl = BaseApplication.getForumUrl() + "index.php?topic=" + postNotification.getTopicId() + "." + postNotification.getPostId();
        Intent intent = new Intent(this, MainActivity.class);
        Bundle extras = new Bundle();
        extras.putString(BUNDLE_TOPIC_URL, topicUrl);
        extras.putString(BUNDLE_TOPIC_TITLE, postNotification.getTopicTitle());
        intent.putExtras(extras);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        int pendingIntentFlags;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S)
            pendingIntentFlags = PendingIntent.FLAG_IMMUTABLE |PendingIntent.FLAG_ONE_SHOT;
        else
            pendingIntentFlags = PendingIntent.FLAG_ONE_SHOT;

        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode++, intent,
                pendingIntentFlags);

        int notificationId;
        String contentText;
        if (isTopicNotification) {
            notificationId = postNotification.getTopicId();
            contentText = "New post by " + postNotification.getPoster();
        }
        else {
            // Using Cantor pairing function (plus the minus sign) for id uniqueness
            int k1 = postNotification.getTopicId();
            int k2 = postNotification.getBoardId();
            notificationId = -(((k1 + k2) * (k1 + k2 + 1)) / 2 + k2);
            contentText = "New post in \"" + postNotification.getTopicTitle() + "\"";
        }

        int newPostsCount = 1;

        Notification existingNotification = getActiveNotification(notificationId);
        if (existingNotification != null) {
            newPostsCount = existingNotification.extras.getInt(NEW_POSTS_COUNT) + 1;
            if (isTopicNotification)
                contentText = newPostsCount + " new posts";
            else
                contentText = newPostsCount + " new posts in " + postNotification.getTopicTitle();
        }

        Bundle notificationExtras = new Bundle();
        notificationExtras.putInt(NEW_POSTS_COUNT, newPostsCount);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentText(contentText)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setGroup(GROUP_KEY)
                        .addExtras(notificationExtras);

        if (isTopicNotification)
            notificationBuilder.setContentTitle(postNotification.getTopicTitle());

        else
            notificationBuilder.setContentTitle(postNotification.getBoardTitle());

        //Applies user's notifications preferences
        if (notificationDefaultValues != -1) {
            notificationBuilder.setDefaults(notificationDefaultValues);
        }
        if (notificationSoundUri != null) {
            notificationBuilder.setSound(notificationSoundUri);
        }
        if (!notificationsVibrateEnabled) {
            notificationBuilder.setVibrate(new long[]{0L});
        }
        if (!notificationsLedEnabled) {
            notificationBuilder.setLights(disabledNotifiationsLedColor, 0, 1000);
        }

        if (buildVersion < Build.VERSION_CODES.O)
            notificationBuilder.setPriority(PRIORITY_MAX);

        boolean createSummaryNotification = otherNotificationsExist(notificationId);

        NotificationCompat.Builder summaryNotificationBuilder = null;
        if (createSummaryNotification) {
            summaryNotificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_notification)
                            .setGroupSummary(true)
                            .setGroup(GROUP_KEY)
                            .setAutoCancel(true)
                            .setStyle(new NotificationCompat.InboxStyle()
                                    .setSummaryText("New Posts"))
                            .setDefaults(Notification.DEFAULT_ALL);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since Android Oreo notification channel is needed.
        if (buildVersion >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(CHANNEL_ID) == null)
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));


        notificationManager.notify(NEW_POST_TAG, notificationId, notificationBuilder.build());

        if (createSummaryNotification)
            notificationManager.notify(SUMMARY_TAG, 0, summaryNotificationBuilder.build());
    }

    private Notification getActiveNotification(int notificationId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            StatusBarNotification[] barNotifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : barNotifications) {
                if (notification.getId() == notificationId)
                    return notification.getNotification();
            }

        }
        return null;
    }

    private boolean otherNotificationsExist(int notificationId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            StatusBarNotification[] barNotifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : barNotifications) {
                String tag = notification.getTag();
                if (tag != null && tag.equals(NEW_POST_TAG) && notification.getId() != notificationId)
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
        Timber.w("onDeletedMessages");
    }


    /**
     * Called if InstanceID token is updated. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Timber.i("InstanceID token updated (onNewToken)");
    }
}
