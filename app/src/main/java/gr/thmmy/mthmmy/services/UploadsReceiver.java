package gr.thmmy.mthmmy.services;

import android.content.Context;

import com.snatik.storage.Storage;

import net.gotev.uploadservice.ServerResponse;
import net.gotev.uploadservice.UploadInfo;
import net.gotev.uploadservice.UploadServiceBroadcastReceiver;

import gr.thmmy.mthmmy.activities.upload.UploadsHelper;
import gr.thmmy.mthmmy.base.BaseApplication;

public class UploadsReceiver extends UploadServiceBroadcastReceiver {
    public interface Delegate {
        void onProgress(long uploadedBytes, long totalBytes, int progress, double uploadRate);

        void onError(Exception exception);

        void onCompleted(int serverResponseCode, byte[] serverResponseBody);

        void onCancelled();
    }

    private Delegate delegate;
    private Storage storage;

    public void setDelegate(Delegate delegate) {
        this.delegate = delegate;
    }

    public void provideStorage(Storage storage) {
        this.storage = storage;
    }

    @Override
    public void onProgress(Context context, UploadInfo uploadInfo) {
        if (delegate != null) {
            delegate.onProgress(uploadInfo.getUploadedBytes(), uploadInfo.getTotalBytes(),
                    uploadInfo.getProgressPercent(), uploadInfo.getUploadRate());
        }
    }

    @Override
    public void onError(Context context, UploadInfo uploadInfo, ServerResponse serverResponse,
                        Exception exception) {
        if (delegate != null) {
            delegate.onError(exception);
        }
        if (storage != null){
            UploadsHelper.deleteTempFiles(storage);
        }
    }

    @Override
    public void onCompleted(Context context, UploadInfo uploadInfo, ServerResponse serverResponse) {
        if (delegate != null) {
            delegate.onCompleted(serverResponse.getHttpCode(), serverResponse.getBody());
        }
        if (storage != null){
            UploadsHelper.deleteTempFiles(storage);
        }
        BaseApplication.getInstance().logFirebaseAnalyticsEvent("file_upload", null);
    }

    @Override
    public void onCancelled(Context context, UploadInfo uploadInfo) {
        if (delegate != null) {
            delegate.onCancelled();
        }
        if (storage != null){
            UploadsHelper.deleteTempFiles(storage);
        }
    }
}