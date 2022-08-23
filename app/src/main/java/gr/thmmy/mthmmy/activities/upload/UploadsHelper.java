package gr.thmmy.mthmmy.activities.upload;

import android.content.Context;
import android.net.Uri;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Nullable
    static Uri createTempFile(Context context, Storage storage, Uri fileUri, String newFilename) {
        Timber.i("Creating new temporary file (%s)", newFilename);
        String oldFilename = FileUtils.filenameFromUri(context, fileUri);
        String fileExtension = oldFilename.substring(oldFilename.indexOf('.'));

        File tempFilesDirectory = createTempFilesDir(context);
        if (tempFilesDirectory==null)
            return null;

        String destinationFilename = tempFilesDirectory.getAbsolutePath() + File.separatorChar + newFilename + fileExtension;

        InputStream inputStream;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;

        try {
            inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Timber.e("Input stream was null, %s", UploadActivity.class.getSimpleName());
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
    static File createZipFile(Context context, @NonNull String zipFilename) {
        // Create a zip file in temp directory
        Timber.i("Creating temporary zip file %s", zipFilename);

        File tempFilesDirectory = createTempFilesDir(context);
        if (tempFilesDirectory==null)
            return null;

        return new File(tempFilesDirectory, zipFilename);
    }

    static void zip(Context context, Uri[] files, Uri zipFile) {
        Timber.i("Adding files to %s...", zipFile.getPath());
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

            Timber.i("Files added successfully to %s.", zipFile.getPath());

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static File createTempFilesDir(Context context){
        File tempFilesDirectory = context.getExternalCacheDir();

        if (tempFilesDirectory == null){
            Timber.e("Temporary files directory error (%s)!", UploadActivity.class.getSimpleName());
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "Temporary files directory error (%s)!", Toast.LENGTH_SHORT).show();
            return null;
        }

        if (!tempFilesDirectory.exists() && !tempFilesDirectory.mkdirs()) {
            Timber.e("Temporary directory %s creation returned false (%s)", tempFilesDirectory.getAbsolutePath(), UploadActivity.class.getSimpleName());
            Toast.makeText(BaseApplication.getInstance().getApplicationContext(), "Couldn't create temporary directory for file renaming!", Toast.LENGTH_SHORT).show();
            return null;
        }

        return tempFilesDirectory;
    }

    public static void deleteTempFiles(Context context, Storage storage) {
        File tempFilesDirectory = context.getExternalCacheDir();

        if (tempFilesDirectory != null){
            if (storage.isDirectoryExists(tempFilesDirectory.getAbsolutePath())) {
                for (File tempFile : storage.getFiles(tempFilesDirectory.getAbsolutePath())) {
                    storage.deleteFile(tempFile.getAbsolutePath());
                }
                storage.deleteDirectory(tempFilesDirectory.getAbsolutePath());
                Timber.i("Deleted temp files from cache.");
            }
        }
        else
            Timber.e("Couldn't delete temp files!");
    }
}
