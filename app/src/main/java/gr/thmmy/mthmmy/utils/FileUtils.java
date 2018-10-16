package gr.thmmy.mthmmy.utils;

import androidx.annotation.NonNull;
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

    public static boolean fileNameExists (String fileName) {
        return fileName != null && (new File(SAVE_DIR.getAbsolutePath(), fileName)).isFile();
    }
}
