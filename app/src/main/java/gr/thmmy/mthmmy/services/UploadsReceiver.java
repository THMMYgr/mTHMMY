package gr.thmmy.mthmmy.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.snatik.storage.Storage;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import gr.thmmy.mthmmy.activities.upload.UploadsHelper;
import gr.thmmy.mthmmy.base.BaseApplication;

public class UploadsReceiver extends UploadServiceBroadcastReceiver {
    public static final String UPLOAD_NOTIFICATION_ID_KEY = "UPLOAD_NOTIFICATION_ID_KEY";

    public static final String ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD";
    public static final String ACTION_RETRY_UPLOAD = "ACTION_RETRY_UPLOAD";

    private Storage storage;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Bundle intentBundle = intent.getExtras();
        if (intentAction == null || intentBundle == null) {
            return;
        }

        if (intentAction.equals(ACTION_CANCEL_UPLOAD)) {
            String notificationID = intentBundle.getString(UPLOAD_NOTIFICATION_ID_KEY);
            UploadService.stopUpload(notificationID);
        } else if (intentAction.equals(ACTION_RETRY_UPLOAD)) {
            //TODO
        }
    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                        Exception exception) {
        Toast.makeText(context.getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }
        UploadsHelper.deleteTempFiles(storage);
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        Toast.makeText(context.getApplicationContext(), "Upload completed successfully", Toast.LENGTH_SHORT).show();
        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }
        UploadsHelper.deleteTempFiles(storage);

        BaseApplication.getInstance().logFirebaseAnalyticsEvent("file_upload", null);
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        Toast.makeText(context.getApplicationContext(), "Upload canceled", Toast.LENGTH_SHORT).show();
        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }
        UploadsHelper.deleteTempFiles(storage);

        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(uploadInfo.getNotificationID());
        }
    }
}