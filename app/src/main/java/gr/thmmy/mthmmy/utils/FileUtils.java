package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import java.io.File;

import static gr.thmmy.mthmmy.services.DownloadHelper.SAVE_DIR;

public class FileUtils {
    @NonNull
    public static String getMimeType(@NonNull String fileName) {
        String type = null;
        final String extension = MimeTypeMap.getFileExtensionFromUrl(fileName);
        if (extension != null)
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.toLowerCase());
        if (type == null)
            type = "*/*";

        return type;
    }

    public static boolean fileNameExists(String fileName) {
        return fileName != null && (new File(SAVE_DIR.getAbsolutePath(), fileName)).isFile();
    }

    @Nullable
    public static String getFileExtension(String filename) {
        String fileExtension;

        if (!filename.contains(".")) {
            return null;
        }
        if (filename.toLowerCase().endsWith(".tar.gz")) {
            fileExtension = filename.substring(filename.length() - 7);
        } else {
            fileExtension = filename.substring(filename.lastIndexOf("."));
        }

        return fileExtension;
    }

    public static String getFilenameWithoutExtension(String filename) {
        String fileExtension = getFileExtension(filename);

        return fileExtension == null
                ? filename
                : filename.substring(0, filename.indexOf(fileExtension));
    }

    @NonNull
    public static String filenameFromUri(Context context, Uri uri) {
        String filename = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    filename = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        if (filename == null) {
            filename = uri.getPath();
            int cut = filename.lastIndexOf('/');
            if (cut != -1) {
                filename = filename.substring(cut + 1);
            }
        }

        return filename;
    }

    public static long sizeFromUri(Context context, @NonNull Uri uri){
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        }
        return -1;
    }
}
