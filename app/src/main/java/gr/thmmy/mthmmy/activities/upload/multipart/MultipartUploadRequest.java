package gr.thmmy.mthmmy.activities.upload.multipart;

import android.content.Context;
import android.content.Intent;

import net.gotev.uploadservice.HttpUploadRequest;
import net.gotev.uploadservice.Logger;
import net.gotev.uploadservice.UploadFile;
import net.gotev.uploadservice.UploadTask;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;

/**
 * From MultipartUploadRequest gotev/android-upload-service in order to use the local custom
 * MultipartUploadTask.
 */
public class MultipartUploadRequest extends HttpUploadRequest<MultipartUploadRequest> {

    private static final String LOG_TAG = MultipartUploadRequest.class.getSimpleName();
    private boolean isUtf8Charset = false;


    public MultipartUploadRequest(final Context context, final String uploadId, final String serverUrl)
            throws IllegalArgumentException, MalformedURLException {
        super(context, uploadId, serverUrl);
    }

    public MultipartUploadRequest(final Context context, final String serverUrl)
            throws MalformedURLException, IllegalArgumentException {
        this(context, null, serverUrl);
    }

    @Override
    protected void initializeIntent(Intent intent) {
        super.initializeIntent(intent);
        intent.putExtra(MultipartUploadTask.PARAM_UTF8_CHARSET, isUtf8Charset);
    }

    @Override
    protected Class<? extends UploadTask> getTaskClass() {
        return MultipartUploadTask.class;
    }

    public MultipartUploadRequest addFileToUpload(String filePath,
                                                  String parameterName,
                                                  String fileName, String contentType)
            throws FileNotFoundException, IllegalArgumentException {

        UploadFile file = new UploadFile(filePath);
        filePath = file.getPath();

        if (parameterName == null || "".equals(parameterName)) {
            throw new IllegalArgumentException("Please specify parameterName value for file: "
                    + filePath);
        }

        file.setProperty(MultipartUploadTask.PROPERTY_PARAM_NAME, parameterName);

        if (contentType == null || contentType.isEmpty()) {
            contentType = file.getContentType(context);
            Logger.debug(LOG_TAG, "Auto-detected MIME type for " + filePath
                    + " is: " + contentType);
        }
        else {
            Logger.debug(LOG_TAG, "Content Type set for " + filePath
                    + " is: " + contentType);
        }

        file.setProperty(MultipartUploadTask.PROPERTY_CONTENT_TYPE, contentType);

        if (fileName == null || "".equals(fileName)) {
            fileName = file.getName(context);
            Logger.debug(LOG_TAG, "Using original file name: " + fileName);
        }
        else {
            Logger.debug(LOG_TAG, "Using custom file name: " + fileName);
        }

        file.setProperty(MultipartUploadTask.PROPERTY_REMOTE_FILE_NAME, fileName);

        params.files.add(file);
        return this;
    }

    public MultipartUploadRequest addFileToUpload(final String path, final String parameterName,
                                                  final String fileName)
            throws FileNotFoundException, IllegalArgumentException {
        return addFileToUpload(path, parameterName, fileName, null);
    }

    public MultipartUploadRequest addFileToUpload(final String path, final String parameterName)
            throws FileNotFoundException, IllegalArgumentException {
        return addFileToUpload(path, parameterName, null, null);
    }

    public MultipartUploadRequest setUtf8Charset() {
        isUtf8Charset = true;
        return this;
    }
}