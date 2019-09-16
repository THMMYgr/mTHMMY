package gr.thmmy.mthmmy.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
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
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
                uploadInfo.getUploadId().equals(dialogUploadID) &&
                uploadProgressDialog != null) {
            Button alertDialogNeutral = uploadProgressDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            alertDialogNeutral.setText(R.string.upload_resume_in_background);
            alertDialogNeutral.setOnClickListener(v -> uploadProgressDialog.dismiss());

            Button alertDialogNegative = uploadProgressDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            alertDialogNegative.setText(R.string.upload_cancel);
            alertDialogNegative.setOnClickListener(v -> {
                UploadService.stopUpload(dialogUploadID);
                uploadProgressDialog.dismiss();
            });

            if (uploadProgressDialog.isShowing()) {
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
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                        Exception exception) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP &&
                uploadInfo.getUploadId().equals(dialogUploadID) &&
                uploadProgressDialog != null) {
            /*Button alertDialogNeutral = uploadProgressDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            alertDialogNeutral.setText("Retry");
            alertDialogNeutral.setOnClickListener(v -> {
                if (multipartUploadRetryIntent != null) {
                    context.sendBroadcast(multipartUploadRetryIntent);
                }
                uploadProgressDialog.dismiss();
            });*/

            Button alertDialogNegative = uploadProgressDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            alertDialogNegative.setText(R.string.upload_cancel);
            alertDialogNegative.setOnClickListener(v -> {
                NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().
                        getSystemService(Context.NOTIFICATION_SERVICE);
                if (notificationManager != null) {
                    notificationManager.cancel(uploadInfo.getNotificationID());
                }
                UploadsHelper.deleteTempFiles(storage);
                uploadProgressDialog.dismiss();
            });

            if (uploadProgressDialog.isShowing()) {
                Window progressWindow = uploadProgressDialog.getWindow();
                if (progressWindow != null) {
                    MaterialProgressBar dialogProgressBar = progressWindow.findViewById(R.id.dialogProgressBar);
                    TextView dialogProgressText = progressWindow.findViewById(R.id.dialog_upload_progress_text);

                    dialogProgressBar.setVisibility(View.GONE);
                    dialogProgressText.setText(R.string.upload_failed);
                }

                if (uploadInfo.getUploadedBytes() == uploadInfo.getTotalBytes()) {
                    uploadProgressDialog.dismiss();
                }
            }
        } else {
            NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().
                    getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.cancel(uploadInfo.getNotificationID());
            }

            Intent combinedActionsIntent = new Intent(UploadsReceiver.ACTION_COMBINED_UPLOAD);
            combinedActionsIntent.putExtra(UploadsReceiver.UPLOAD_ID_KEY, uploadInfo.getUploadId());
            context.sendBroadcast(combinedActionsIntent);
        }

        Toast.makeText(context.getApplicationContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            uploadProgressDialog = null;
            dialogUploadID = null;
        }

        String response = serverResponse.getBodyAsString();
        if(response.contains("Η προσθήκη του αρχείου ήταν επιτυχημένη.")||response.contains("The upload was successful.")){
            Toast.makeText(context.getApplicationContext(), "Upload completed successfully", Toast.LENGTH_SHORT).show();
            BaseApplication.getInstance().logFirebaseAnalyticsEvent("file_upload", null);
        }
        else {
            MultipartUploadException multipartUploadException = new MultipartUploadException(response);
            Timber.e(multipartUploadException);
            onError(context,uploadInfo,serverResponse,multipartUploadException);
        }
        
        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }

        UploadsHelper.deleteTempFiles(storage);
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            uploadProgressDialog = null;
            dialogUploadID = null;
        }

        Toast.makeText(context.getApplicationContext(), R.string.upload_canceled, Toast.LENGTH_SHORT).show();
        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }

        /*NotificationManager notificationManager = (NotificationManager) context.getApplicationContext().
                getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(uploadInfo.getNotificationID());
        }*/
        UploadsHelper.deleteTempFiles(storage);
    }

    public static void setDialogDisplay(AlertDialog uploadProgressDialog, String dialogUploadID,
                                        Intent multipartUploadRetryIntent) {
        UploadsReceiver.uploadProgressDialog = uploadProgressDialog;
        UploadsReceiver.dialogUploadID = dialogUploadID;
        //UploadsReceiver.multipartUploadRetryIntent = multipartUploadRetryIntent;
    }
}