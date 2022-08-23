package gr.thmmy.mthmmy.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.snatik.storage.Storage;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadService;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.upload.UploadsHelper;
import gr.thmmy.mthmmy.activities.upload.multipart.MultipartUploadException;
import gr.thmmy.mthmmy.base.BaseApplication;
import timber.log.Timber;

public class UploadsReceiver extends UploadServiceBroadcastReceiver {
    public static final String UPLOAD_ID_KEY = "UPLOAD_ID_KEY";

    public static final String ACTION_COMBINED_UPLOAD = "ACTION_COMBINED_UPLOAD";
    public static final String ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD";
    public static final String ACTION_RETRY_UPLOAD = "ACTION_RETRY_UPLOAD";

    /*public static final String UPLOAD_RETRY_FILENAME = "UPLOAD_RETRY_FILENAME";
    public static final String UPLOAD_RETRY_CATEGORY = "UPLOAD_RETRY_CATEGORY";
    public static final String UPLOAD_RETRY_TITLE = "UPLOAD_RETRY_TITLE";
    public static final String UPLOAD_RETRY_DESCRIPTION = "UPLOAD_RETRY_DESCRIPTION";
    public static final String UPLOAD_RETRY_ICON = "UPLOAD_RETRY_ICON";
    public static final String UPLOAD_RETRY_UPLOADER = "UPLOAD_RETRY_UPLOADER";
    public static final String UPLOAD_RETRY_FILE_URI = "UPLOAD_RETRY_FILE_URI";*/

    private Storage storage;
    private static AlertDialog uploadProgressDialog;
    private static String dialogUploadID;
    //private static Intent multipartUploadRetryIntent;

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
                Timber.d("Received ACTION_CANCEL_UPLOAD (id: %s)", uploadID);
                UploadService.stopUpload(uploadID);
                break;
            /*case ACTION_RETRY_UPLOAD:
                String retryFilename = intentBundle.getString(UPLOAD_RETRY_FILENAME);
                String retryCategory = intentBundle.getString(UPLOAD_RETRY_CATEGORY);
                String retryTitleText = intentBundle.getString(UPLOAD_RETRY_TITLE);
                String retryDescription = intentBundle.getString(UPLOAD_RETRY_DESCRIPTION);
                String retryIcon = intentBundle.getString(UPLOAD_RETRY_ICON);
                String retryUploaderProfile = intentBundle.getString(UPLOAD_RETRY_UPLOADER);
                Uri retryFileUri = (Uri) intentBundle.get(UPLOAD_RETRY_FILE_URI);
                String retryUploadID = UUID.randomUUID().toString();

                UploadActivity.uploadFile(context, retryUploadID,
                        UploadActivity.getConfigForUpload(context, retryUploadID, retryFilename, retryCategory,
                                retryTitleText, retryDescription, retryIcon, retryUploaderProfile, retryFileUri),
                        retryCategory, retryTitleText, retryDescription, retryIcon,
                        retryUploaderProfile, retryFileUri);

                break;*/
            default:
                super.onReceive(context, intent);
                break;
        }
    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        Timber.i("Upload in progress (id: %s)", uploadInfo.getUploadId());
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                        Exception exception) {
        Timber.i("Error while uploading (id: %s)", uploadInfo.getUploadId());

        cancelNotification(context, uploadInfo.getNotificationID());
        Intent combinedActionsIntent = new Intent(UploadsReceiver.ACTION_COMBINED_UPLOAD);
        combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_ID_KEY, uploadInfo.getUploadId());
        context.sendBroadcast(combinedActionsIntent);

        Toast.makeText(context.getApplicationContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        String response = serverResponse.getBodyAsString();
        if (response.contains("Η προσθήκη του αρχείου ήταν επιτυχημένη.") || response.contains("The upload was successful.")) {
            Timber.i("Upload completed successfully (id: %s)", uploadInfo.getUploadId());
            Toast.makeText(context.getApplicationContext(), "Upload completed successfully!", Toast.LENGTH_SHORT).show();
            BaseApplication.getInstance().logFirebaseAnalyticsEvent("file_upload", null);
        }
        else {
            MultipartUploadException multipartUploadException = new MultipartUploadException(response);
            Timber.e(multipartUploadException);
            onError(context, uploadInfo, serverResponse, multipartUploadException);
        }

        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }

        UploadsHelper.deleteTempFiles(context, storage);
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        Timber.i("Upload cancelled (id: %s)", uploadInfo.getUploadId());

        Toast.makeText(context.getApplicationContext(), R.string.upload_cancelled, Toast.LENGTH_SHORT).show();
        if (storage == null)
            storage = new Storage(context.getApplicationContext());

        //cancelNotification(context, uploadInfo.getNotificationID());
        UploadsHelper.deleteTempFiles(context, storage);
    }

    public static void setDialogDisplay(AlertDialog uploadProgressDialog, String dialogUploadID,
                                        Intent multipartUploadRetryIntent) {
        UploadsReceiver.uploadProgressDialog = uploadProgressDialog;
        UploadsReceiver.dialogUploadID = dialogUploadID;
        //UploadsReceiver.multipartUploadRetryIntent = multipartUploadRetryIntent;
    }

    private void cancelNotification(Context context, int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.cancel(notificationId);
    }
}