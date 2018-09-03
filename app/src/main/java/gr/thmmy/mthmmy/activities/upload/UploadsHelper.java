package gr.thmmy.mthmmy.activities.upload;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

import static android.support.v4.content.FileProvider.getUriForFile;

class UploadsHelper {
    @NonNull
    static String filenameFromUri(Context context, Uri uri) {
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    static String createTempFile(Context context, Uri fileUri, String newFilename) {
        String oldFilename = filenameFromUri(context, fileUri);
        String fileExtension = oldFilename.substring(oldFilename.indexOf("."));
        String destinationFilename = Environment.getExternalStorageDirectory().getPath() +
                File.separatorChar + "~tmp_mThmmy_uploads" + File.separatorChar + newFilename + fileExtension;

        File tempDirectory = new File(android.os.Environment.getExternalStorageDirectory().getPath() +
                File.separatorChar + "~tmp_mThmmy_uploads");

        if (!tempDirectory.exists()) {
            if (!tempDirectory.mkdirs()) {
                Timber.w("Temporary directory build returned false in %s", UploadActivity.class.getSimpleName());
                Toast.makeText(context, "Couldn't create temporary directory", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        InputStream inputStream;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Timber.w("Input stream was null, %s", UploadActivity.class.getSimpleName());
                return null;
            }

            bufferedInputStream = new BufferedInputStream(inputStream);
            bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
            byte[] buf = new byte[1024];
            bufferedInputStream.read(buf);
            do {
                bufferedOutputStream.write(buf);
            } while (bufferedInputStream.read(buf) != -1);
        } catch (IOException exception) {
            exception.printStackTrace();
        } finally {
            try {
                if (bufferedInputStream != null) bufferedInputStream.close();
                if (bufferedOutputStream != null) bufferedOutputStream.close();
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }

        return destinationFilename;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void deleteTempFiles() {
        File tempFilesDirectory = new File(Environment.getExternalStorageDirectory().getPath() +
                File.separatorChar + "~tmp_mThmmy_uploads");

        if (tempFilesDirectory.isDirectory()) {
            String[] tempFilesArray = tempFilesDirectory.list();
            for (String tempFile : tempFilesArray) {
                new File(tempFilesDirectory, tempFile).delete();
            }
            tempFilesDirectory.delete();
        }
    }
}
