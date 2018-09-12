package gr.thmmy.mthmmy.services;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.snatik.storage.Storage;

import net.gotev.uploadservice.MultipartUploadRequest;
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
    public static final String UPLOAD_REQUEST_KEY = "UPLOAD_REQUEST_KEY";

    public static final String ACTION_COMBINED_UPLOAD = "ACTION_COMBINED_UPLOAD";
    public static final String ACTION_CANCEL_UPLOAD = "ACTION_CANCEL_UPLOAD";
    public static final String ACTION_RETRY_UPLOAD = "ACTION_RETRY_UPLOAD";

    private Storage storage;
    private static AlertDialog uploadProgressDialog;
    private static String dialogUploadID;
    private static Intent multipartUploadRetryIntent;

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
                MultipartUploadRequest multipartUploadRequest = (MultipartUploadRequest) intentBundle.get(UPLOAD_REQUEST_KEY);
                if (multipartUploadRequest != null) {
                    multipartUploadRequest.startUpload();
                } else {
                    Toast.makeText(context.getApplicationContext(), "Couldn't retry upload.", Toast.LENGTH_SHORT).show();
                }
                break;
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
            alertDialogNeutral.setText("Resume on background");
            alertDialogNeutral.setOnClickListener(v -> uploadProgressDialog.dismiss());

            Button alertDialogNegative = uploadProgressDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            alertDialogNegative.setText("Cancel");
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
            Button alertDialogNeutral = uploadProgressDialog.getButton(AlertDialog.BUTTON_NEUTRAL);
            alertDialogNeutral.setText("Retry");
            alertDialogNeutral.setOnClickListener(v -> {
                if (multipartUploadRetryIntent != null) {
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(context.getApplicationContext());
                    localBroadcastManager.sendBroadcast(multipartUploadRetryIntent);
                }
                uploadProgressDialog.dismiss();
            });

            Button alertDialogNegative = uploadProgressDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            alertDialogNegative.setText("Cancel");
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
                    dialogProgressText.setText("Upload failed.");
                }

                if (uploadInfo.getUploadedBytes() == uploadInfo.getTotalBytes()) {
                    uploadProgressDialog.dismiss();
                }
            }
        }

        Toast.makeText(context.getApplicationContext(), "Upload failed", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(context.getApplicationContext(), "Upload completed successfully", Toast.LENGTH_SHORT).show();
        if (storage == null) {
            storage = new Storage(context.getApplicationContext());
        }

        BaseApplication.getInstance().logFirebaseAnalyticsEvent("file_upload", null);
        UploadsHelper.deleteTempFiles(storage);
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            uploadProgressDialog = null;
            dialogUploadID = null;
        }

        Toast.makeText(context.getApplicationContext(), "Upload canceled", Toast.LENGTH_SHORT).show();
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
        UploadsReceiver.multipartUploadRetryIntent = multipartUploadRetryIntent;
    }
}