package gr.thmmy.mthmmy.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.snatik.storage.Storage;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.upload.UploadsHelper;
import gr.thmmy.mthmmy.base.BaseApplication;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class UploadsReceiver extends UploadServiceBroadcastReceiver {
    public static final String UPLOAD_ID_KEY = "UPLOAD_ID_KEY";

    public static final String ACTION_COMBINED_UPLOAD = "ACTION_COMBINED_UPLOAD";
    public static final String ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD";
    public static final String ACTION_RETRY_UPLOAD = "ACTION_RETRY_UPLOAD";

    private Storage storage;
    private static AlertDialog uploadProgressDialog;
    private static String dialogUploadID;

    @Override
    public void onReceive(Context context, Intent intent) {
        String intentAction = intent.getAction();
        Bundle intentBundle = intent.getExtras();
        if (intentAction == null || intentBundle == null) {
            super.onReceive(context, intent);
            return;
        }

        switch (intentAction) {
            case ACTION_CANCEL_UPLOAD:
                String uploadID = intentBundle.getString(UPLOAD_ID_KEY);
                UploadService.stopUpload(uploadID);
                break;
            case ACTION_RETRY_UPLOAD:
                //TODO
                break;
            default:
                super.onReceive(context, intent);
                break;
        }
    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        if (uploadProgressDialog != null && uploadProgressDialog.isShowing() &&
                uploadInfo.getUploadId().equals(dialogUploadID)) {
            Window progressWindow = uploadProgressDialog.getWindow();
            if (progressWindow != null) {
                MaterialProgressBar dialogProgressBar = progressWindow.findViewById(R.id.dialogProgressBar);
                TextView dialogProgressText = progressWindow.findViewById(R.id.dialog_upload_progress_text);

                dialogProgressBar.setProgress(uploadInfo.getProgressPercent());
                dialogProgressText.setText(context.getResources().getString(
                        R.string.upload_progress_dialog_bytes_uploaded,
                        (float) uploadInfo.getUploadRate(),
                        (int) uploadInfo.getUploadedBytes() / 1000,
                        (int) uploadInfo.getTotalBytes() / 1000));
            }

            if (uploadInfo.getUploadedBytes() == uploadInfo.getTotalBytes()) {
                uploadProgressDialog.dismiss();
            }
        }
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

    public static void setDialogDisplay(AlertDialog uploadProgressDialog,
                                        String dialogUploadID) {
        UploadsReceiver.uploadProgressDialog = uploadProgressDialog;
        UploadsReceiver.dialogUploadID = dialogUploadID;
    }
}