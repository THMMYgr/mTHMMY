package gr.thmmy.mthmmy.activities.upload;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.OpenableColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

class UploadsHelper {
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;
    private static final String CACHE_IMAGE_NAME = "tempUploadFile.jpg";

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

    static File getCacheFile(Context context) {
        File imageFile = new File(context.getExternalCacheDir(), CACHE_IMAGE_NAME);
        //noinspection ResultOfMethodCallIgnored
        imageFile.getParentFile().mkdirs();
        return imageFile;
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

    @SuppressWarnings("ResultOfMethodCallIgnored")
    static void deleteCacheFiles(Context context) {
        File cacheFilesDirectory = context.getExternalCacheDir();
        assert cacheFilesDirectory != null;
        String[] tempFilesArray = cacheFilesDirectory.list();
        for (String tempFile : tempFilesArray) {
            new File(cacheFilesDirectory, tempFile).delete();
        }
    }

    /**
     * Resize to avoid using too much memory loading big images (e.g.: 2560*1920)
     **/
    static Bitmap getImageResized(Context context, Uri selectedImage) {
        Bitmap bm;
        int[] sampleSizes = new int[]{5, 3, 2, 1};
        int i = 0;
        do {
            bm = decodeBitmap(context, selectedImage, sampleSizes[i]);
            i++;
        } while (bm.getWidth() < DEFAULT_MIN_WIDTH_QUALITY && i < sampleSizes.length);
        return bm;
    }

    private static Bitmap decodeBitmap(Context context, Uri theUri, int sampleSize) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;

        AssetFileDescriptor fileDescriptor = null;
        try {
            fileDescriptor = context.getContentResolver().openAssetFileDescriptor(theUri, "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        assert fileDescriptor != null;
        return BitmapFactory.decodeFileDescriptor(
                fileDescriptor.getFileDescriptor(), null, options);
    }

    static int getRotation(Context context, Uri imageUri) {
        int rotation = 0;
        try {

            context.getContentResolver().notifyChange(imageUri, null);
            ExifInterface exif = new ExifInterface(imageUri.getPath());
            int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return rotation;
    }

    static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        return bm;
    }
}
