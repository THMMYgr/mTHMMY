package gr.thmmy.mthmmy.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.PostNotification;
import timber.log.Timber;

import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_TITLE;
import static gr.thmmy.mthmmy.activities.topic.TopicActivity.BUNDLE_TOPIC_URL;

public class NotificationService extends FirebaseMessagingService {
    private static final int buildVersion = Build.VERSION.SDK_INT;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage.getData().size() > 0) {
            JSONObject json = new JSONObject(remoteMessage.getData());
            try {
                int userId = BaseApplication.getInstance().getSessionManager().getUserId();
                //Don't notify me if the sender is me!
                if(Integer.parseInt(json.getString("posterId"))!= userId)
                {
                    int topicId = Integer.parseInt(json.getString("topicId"));
                    int postId = Integer.parseInt(json.getString("postId"));
                    String topicTitle = json.getString("topicTitle");
                    String poster = json.getString("poster");
                    sendNotification(new PostNotification(postId, topicId, topicTitle, poster));
                }
            } catch (JSONException e) {
                Timber.e(e, "JSON Exception");
            }
        }
    }

    private static final String CHANNEL_ID = "Posts";
    private static final String GROUP_KEY = "PostsGroup";
    private static int requestCode = 0;

    private static final String NEW_POSTS_COUNT = "newPostsCount";
    private static final String NEW_POST_TAG = "NEW_POST"; //notification tag
    private static final String SUMMARY_TAG = "SUMMARY";

    /**
     * Create and show a new post notification.
     */
    private void sendNotification(PostNotification postNotification) {
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

        if (buildVersion >= Build.VERSION_CODES.M){
            Notification existingNotification = getActiveNotification(topicId);
            if(existingNotification!=null)
            {
                newPostsCount = existingNotification.extras.getInt(NEW_POSTS_COUNT) + 1;
                contentText = newPostsCount + " new posts";
            }
        }

        Bundle notificationExtras = new Bundle();
        notificationExtras.putInt(NEW_POSTS_COUNT, newPostsCount);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(postNotification.getTopicTitle())
                        .setContentText(contentText)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent)
                        .addExtras(notificationExtras);

        if (buildVersion < Build.VERSION_CODES.O)
            notificationBuilder.setPriority(PRIORITY_HIGH);

        boolean createSummaryNotification = false;
        if(buildVersion >= Build.VERSION_CODES.LOLLIPOP)
        {
            createSummaryNotification = true;
            if(buildVersion >= Build.VERSION_CODES.M)
                createSummaryNotification = otherNotificationsExist(topicId);
        }

        notificationBuilder.setVibrate(new long[0]);
        notificationBuilder.setGroup(GROUP_KEY);

        NotificationCompat.Builder summaryNotificationBuilder = null;
        if(createSummaryNotification)
        {
            summaryNotificationBuilder =
                    new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setGroupSummary(true)
                            .setGroup(GROUP_KEY)
                            .setAutoCancel(true)
                            .setStyle(new NotificationCompat.InboxStyle()
                                    .setSummaryText("New Posts"))
                            .setSound(defaultSoundUri);
        }




        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since Android Oreo notification channel is needed.
        if (buildVersion >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Topic Updates", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }


        notificationManager.notify(NEW_POST_TAG, topicId, notificationBuilder.build());


        if(createSummaryNotification)
            notificationManager.notify(SUMMARY_TAG,0, summaryNotificationBuilder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private Notification getActiveNotification(int notificationId) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager!=null)
        {
            StatusBarNotification[] barNotifications = notificationManager.getActiveNotifications();
            for(StatusBarNotification notification: barNotifications) {
                if (notification.getId() == notificationId)
                    return notification.getNotification();
            }

        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean otherNotificationsExist(int notificationId){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(notificationManager!=null) {
            StatusBarNotification[] barNotifications = notificationManager.getActiveNotifications();
            for (StatusBarNotification notification : barNotifications) {
                if (notification.getTag().equals(NEW_POST_TAG) && notification.getId() != notificationId)
                    return true;
            }
        }
        return false;
    }


}
