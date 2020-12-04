package gr.thmmy.mthmmy.activities.upload;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.snatik.storage.Storage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.FileUtils;
import timber.log.Timber;

public class UploadsHelper {
    private static final int BUFFER = 4096;
    private static final String TEMP_FILES_DIRECTORY = "~tmp_mTHMMY_uploads";

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    static Uri createTempFile(Context context, Storage storage, Uri fileUri, String newFilename) {
        String oldFilename = FileUtils.filenameFromUri(context, fileUri);
        String fileExtension = oldFilename.substring(oldFilename.indexOf('.'));
        String destinationFilename = Environment.getExternalStorageDirectory().getPath() +
                File.separatorChar + TEMP_FILES_DIRECTORY + File.separatorChar + newFilename + fileExtension;

        File tempDirectory = new File(android.os.Environment.getExternalStorageDirectory().getPath() +
                File.separatorChar + TEMP_FILES_DIRECTORY);

        if (!tempDirectory.exists() && !tempDirectory.mkdirs()) {
            Timber.w("Temporary directory build returned false in %s", UploadActivity.class.getSimpleName());
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "Couldn't create temporary directory", Toast.LENGTH_SHORT).show();
            return null;
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

    @Nullable
    static File createZipFile(@NonNull String zipFilename) {
        // Create a zip file name
        File zipFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) +
                File.separator + "mTHMMY");

        if (!zipFolder.exists() && !zipFolder.mkdirs()) {
            Timber.w("Zip folder build returned false in %s", UploadsHelper.class.getSimpleName());
            return null;
        }

        return new File(zipFolder, zipFilename);
    }

    static void zip(Context context, Uri[] files, Uri zipFile) {
        try {
            BufferedInputStream origin;
            OutputStream dest = context.getContentResolver().openOutputStream(zipFile);
            assert dest != null;
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
            byte[] data = new byte[BUFFER];

            for (Uri file : files) {
                InputStream inputStream = context.getContentResolver().openInputStream(file);
                assert inputStream != null;
                origin = new BufferedInputStream(inputStream, BUFFER);

                ZipEntry entry = new ZipEntry(FileUtils.filenameFromUri(context, file));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteTempFiles(Storage storage) {
        File tempFilesDirectory = new File(Environment.getExternalStorageDirectory().getPath() +
                File.separatorChar + TEMP_FILES_DIRECTORY);

        if (storage.isDirectoryExists(tempFilesDirectory.getAbsolutePath())) {
            for (File tempFile : storage.getFiles(tempFilesDirectory.getAbsolutePath())) {
                storage.deleteFile(tempFile.getAbsolutePath());
            }
            storage.deleteDirectory(tempFilesDirectory.getAbsolutePath());
        }
    }
}
