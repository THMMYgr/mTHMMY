package gr.thmmy.mthmmy.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.PostNotification;
import timber.log.Timber;

import static android.support.v4.app.NotificationCompat.PRIORITY_MAX;
import static gr.thmmy.mthmmy.activities.settings.SettingsActivity.NOTIFICATION_VIBRATION_KEY;
import static gr.thmmy.mthmmy.activities.settings.SettingsFragment.SELECTED_RINGTONE;
import static gr.thmmy.mthmmy.activities.settings.SettingsFragment.SETTINGS_SHARED_PREFS;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

public class NotificationService extends FirebaseMessagingService {
    private static final int buildVersion = Build.VERSION.SDK_INT;

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
                    int topicId = Integer.parseInt(json.getString("topicId"));
                    int postId = Integer.parseInt(json.getString("postId"));
                    String topicTitle = json.getString("topicTitle");
                    String poster = json.getString("poster");
                    sendNotification(new PostNotification(postId, topicId, topicTitle, poster));
                } else
                    Timber.v("Notification suppressed (own userID).");
            } catch (JSONException e) {
                Timber.e(e, "JSON Exception");
            }
        }
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

        SharedPreferences settingsFile = getSharedPreferences(SETTINGS_SHARED_PREFS, Context.MODE_PRIVATE);
        String notificationsSound = settingsFile.getString(SELECTED_RINGTONE, null);
        Uri notificationSoundUri = notificationsSound != null ? Uri.parse(notificationsSound) : null;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean notificationsVibrateEnabled = sharedPrefs.getBoolean(NOTIFICATION_VIBRATION_KEY, true);

        int notificationDefaultValues = Notification.DEFAULT_LIGHTS;
        if (notificationsVibrateEnabled) {
            notificationDefaultValues |= Notification.DEFAULT_VIBRATE;
        }
        if (notificationSoundUri == null) {
            notificationDefaultValues |= Notification.DEFAULT_SOUND;
        }

        String topicUrl = "https://www.thmmy.gr/smf/index.php?topic=" + postNotification.getTopicId() + "." + postNotification.getPostId();
        Intent intent = new Intent(this, TopicActivity.class);
        Bundle extras = new Bundle();
        extras.putString(BUNDLE_TOPIC_URL, topicUrl);
        extras.putString(BUNDLE_TOPIC_TITLE, postNotification.getTopicTitle());
        intent.putExtras(extras);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, requestCode++, intent,
                PendingIntent.FLAG_ONE_SHOT);

        final int topicId = postNotification.getTopicId();
        String contentText = "New post by " + postNotification.getPoster();
        int newPostsCount = 1;

        if (buildVersion >= Build.VERSION_CODES.M) {
            Notification existingNotification = getActiveNotification(topicId);
            if (existingNotification != null) {
                newPostsCount = existingNotification.extras.getInt(NEW_POSTS_COUNT) + 1;
                contentText = newPostsCount + " new posts";
            }
        }

        Bundle notificationExtras = new Bundle();
        notificationExtras.putInt(NEW_POSTS_COUNT, newPostsCount);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(postNotification.getTopicTitle())
                        .setContentText(contentText)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent)
                        .setDefaults(notificationDefaultValues)
                        .setGroup(GROUP_KEY)
                        .addExtras(notificationExtras);
        //Checks for values other than defaults and applies them
        if (notificationSoundUri != null) {
            notificationBuilder.setSound(notificationSoundUri);
        }
        if (!notificationsVibrateEnabled) {
            notificationBuilder.setVibrate(new long[]{0L});
        }

        if (buildVersion < Build.VERSION_CODES.O)
            notificationBuilder.setPriority(PRIORITY_MAX);

        boolean createSummaryNotification = false;
        if (buildVersion >= Build.VERSION_CODES.M)
            createSummaryNotification = otherNotificationsExist(topicId);


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
        if (buildVersion >= Build.VERSION_CODES.O)
            notificationManager.createNotificationChannel(new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));

        notificationManager.notify(NEW_POST_TAG, topicId, notificationBuilder.build());

        if (createSummaryNotification)
            notificationManager.notify(SUMMARY_TAG, 0, summaryNotificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
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

    @RequiresApi(api = Build.VERSION_CODES.M)
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


}
