package gr.thmmy.mthmmy.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import gr.thmmy.mthmmy.R;

import static gr.thmmy.mthmmy.services.DownloadService.ACTION_DOWNLOAD;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_DOWNLOAD_ID;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_DOWNLOAD_STATE;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_NOTIFICATION_TEXT;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_NOTIFICATION_TICKER;
import static gr.thmmy.mthmmy.services.DownloadService.EXTRA_NOTIFICATION_TITLE;
import static gr.thmmy.mthmmy.services.DownloadService.STARTED;

public class Receiver extends BroadcastReceiver {
    public Receiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if(intent.getAction().equals(ACTION_DOWNLOAD))
        {
            Bundle extras = intent.getExtras();
            int id = extras.getInt(EXTRA_DOWNLOAD_ID);
            String state = extras.getString(EXTRA_DOWNLOAD_STATE, "NONE");
            String title = extras.getString(EXTRA_NOTIFICATION_TITLE);
            String text =extras.getString(EXTRA_NOTIFICATION_TEXT);
            String ticker =extras.getString(EXTRA_NOTIFICATION_TICKER);

            builder.setContentTitle(title)
                    .setContentText(text)
                    .setTicker(ticker)
                    .setAutoCancel(true)    //???
                    .setSmallIcon(R.mipmap.ic_launcher);

            if(state.equals(STARTED))
                builder.setOngoing(true);

            Notification notification = builder.build();
            notificationManager.notify(id, notification);
        }

    }

}
