package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import java.io.File;

import gr.thmmy.mthmmy.R;

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
    public static String getFileExtension(@NonNull String filename) {
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

    public static long sizeFromUri(Context context, @NonNull Uri uri) {
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        }
        return -1;
    }

    /**
     * Returns a String with a single FontAwesome typeface character corresponding to this file's
     * extension.
     *
     * @param filename String with filename <b>containing file's extension</b>
     * @return FontAwesome character according to file's type
     * @see <a href="http://fontawesome.io/">FontAwesome</a>
     */
    @NonNull
    public static String faIconFromFilename(Context context, String filename) {
        filename = filename.toLowerCase();

        if (filename.contains("jpg") || filename.contains("gif") || filename.contains("jpeg")
                || filename.contains("png"))
            return context.getResources().getString(R.string.fa_file_image_o);
        else if (filename.contains("pdf"))
            return context.getResources().getString(R.string.fa_file_pdf_o);
        else if (filename.contains("zip") || filename.contains("rar") || filename.contains("tar.gz"))
            return context.getResources().getString(R.string.fa_file_zip_o);
        else if (filename.contains("txt"))
            return context.getResources().getString(R.string.fa_file_text_o);
        else if (filename.contains("doc") || filename.contains("docx"))
            return context.getResources().getString(R.string.fa_file_word_o);
        else if (filename.contains("xls") || filename.contains("xlsx"))
            return context.getResources().getString(R.string.fa_file_excel_o);
        else if (filename.contains("pps"))
            return context.getResources().getString(R.string.fa_file_powerpoint_o);
        else if (filename.contains("mpg"))
            return context.getResources().getString(R.string.fa_file_video_o);

        return context.getResources().getString(R.string.fa_file);
    }
}
