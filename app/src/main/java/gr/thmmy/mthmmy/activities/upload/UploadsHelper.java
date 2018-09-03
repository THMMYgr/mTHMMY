package gr.thmmy.mthmmy.activities.upload;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.snatik.storage.Storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

class UploadsHelper {
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    static Uri createTempFile(Context context, Storage storage, Uri fileUri, String newFilename) {
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

        return FileProvider.getUriForFile(context, context.getPackageName() +
                ".provider", storage.getFile(destinationFilename));
    }

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

    static long sizeFromUri(Context context, @NonNull Uri uri){
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
            }
        }
        return -1;
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
