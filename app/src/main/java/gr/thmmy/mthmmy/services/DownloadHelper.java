package gr.thmmy.mthmmy.services;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.io.File;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.ThmmyFile;
import okhttp3.Cookie;
import timber.log.Timber;

import static gr.thmmy.mthmmy.utils.FileUtils.getMimeType;

/**
 * Not an actual service, but simply a helper class that adds a download to the queue of Android's
 * DownloadManager system service.
 */
public class DownloadHelper {
    public static final File SAVE_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

    public static void enqueueDownload(ThmmyFile thmmyFile){
        Context applicationContext = BaseApplication.getInstance().getApplicationContext();
        Toast.makeText(applicationContext, "Download started!", Toast.LENGTH_SHORT).show();

        try {
            String fileName = renameFileIfExists(thmmyFile.getFilename());
            Uri downloadURI = Uri.parse(thmmyFile.getFileUrl().toString());

            DownloadManager downloadManager = (DownloadManager)applicationContext.getSystemService(Context.DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(downloadURI);

            Cookie thmmyCookie = BaseApplication.getInstance().getSessionManager().getThmmyCookie();
            if(thmmyCookie!=null)
                request.addRequestHeader("Cookie", thmmyCookie.name() + "=" + thmmyCookie.value());
            request.setTitle(fileName);
            request.setMimeType(getMimeType(fileName));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(SAVE_DIR.getName(), fileName);
            request.allowScanningByMediaScanner();

            BaseApplication.getInstance().logFirebaseAnalyticsEvent("file_download", null);
            downloadManager.enqueue(request);
        } catch (Exception e) {
            Toast.makeText(applicationContext, "Download failed...", Toast.LENGTH_SHORT).show();
            Timber.e(e, "Exception while enqueuing download.");
        }
    }

    @NonNull
    private static String renameFileIfExists(String originalFileName) {
        final String dirPath = SAVE_DIR.getAbsolutePath();
        File file = new File(dirPath, originalFileName);

        String nameFormat;
        String[] tokens = originalFileName.split("\\.(?=[^.]+$)");

        if (tokens.length != 2) {
            Timber.w("Couldn't get file extension...");
            nameFormat = originalFileName + "(%d)";
        } else
            nameFormat = tokens[0] + "-%d." + tokens[1];

        for (int i = 1; ; i++) {
            if (!file.isFile())
                break;

            file = new File(dirPath, String.format(nameFormat, i));
        }
        return file.getName();
    }
}
