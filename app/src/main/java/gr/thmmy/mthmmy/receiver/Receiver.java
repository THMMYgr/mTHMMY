package gr.thmmy.mthmmy.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.webkit.MimeTypeMap;

import java.io.File;

import timber.log.Timber;

import static gr.thmmy.mthmmy.services.DownloadService.ACTION_DOWNLOAD;
import static gr.thmmy.mthmmy.services.DownloadService.COMPLETED;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_DOWNLOAD_ID;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_DOWNLOAD_STATE;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_FILE_NAME;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_NOTIFICATION_TEXT;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_NOTIFICATION_TICKER;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_NOTIFICATION_TITLE;
import static gr.thmmy.mthmmy.services.DownloadService.SAVE_DIR;
import static gr.thmmy.mthmmy.services.DownloadService.STARTED;

public class Receiver extends BroadcastReceiver {
    private static final String DOWNLOADS_CHANNEL_ID = "Downloads";
    private static final String DOWNLOADS_CHANNEL_NAME = "Downloads";

    public Receiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, DOWNLOADS_CHANNEL_ID);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (intent.getAction().equals(ACTION_DOWNLOAD)) {
            Bundle extras = intent.getExtras();
            int id = extras.getInt(EXTRA_DOWNLOAD_ID);
            String state = extras.getString(EXTRA_DOWNLOAD_STATE, "NONE");
            String title = extras.getString(EXTRA_NOTIFICATION_TITLE);
            String text = extras.getString(EXTRA_NOTIFICATION_TEXT);
            String ticker = extras.getString(EXTRA_NOTIFICATION_TICKER);

            notificationBuilder.setContentTitle(title)
                    .setContentText(text)
                    .setTicker(ticker)
                    .setAutoCancel(true);

            if (state.equals(STARTED))
                notificationBuilder.setOngoing(true)
                        .setSmallIcon(android.R.drawable.stat_sys_download);
            else if (state.equals(COMPLETED)) {
                String fileName = extras.getString(EXTRA_FILE_NAME, "NONE");

                File file = new File(SAVE_DIR, fileName);
                if (file.exists()) {
                    String type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                            MimeTypeMap.getFileExtensionFromUrl(file.getAbsolutePath()));


                    Intent chooserIntent = new Intent(Intent.ACTION_VIEW);
                    chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    chooserIntent.setDataAndType(Uri.fromFile(file), type);
                    Intent chooser = Intent.createChooser(chooserIntent, "Open With...");

                    PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, chooser, PendingIntent.FLAG_CANCEL_CURRENT);
                    notificationBuilder.setContentIntent(pendingIntent)
                            .setSmallIcon(android.R.drawable.stat_sys_download_done);

                } else
                    Timber.w("File doesn't exist.");
            }

            // Since Android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                notificationManager.createNotificationChannel(new NotificationChannel(DOWNLOADS_CHANNEL_ID, DOWNLOADS_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH));

            notificationManager.notify(id, notificationBuilder.build());
        }
    }

}
