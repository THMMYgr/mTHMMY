package gr.thmmy.mthmmy.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

public class TakePhoto {
    private static final int DEFAULT_MIN_WIDTH_QUALITY = 400;
    private static final String IMAGE_CONTENT_DESCRIPTION = "mThmmy uploads image";

    @Nullable
    public static Intent getIntent(Context context, @NonNull File photoFile) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //Ensures that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(context.getPackageManager()) != null) {
            Uri photoURI = FileProvider.getUriForFile(context, context.getPackageName() +
                    ".provider", photoFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

            //Grants necessary permissions for Gallery to use the Uri
            List<ResolveInfo> resInfoList = context.getPackageManager().
                    queryIntentActivities(takePictureIntent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, photoURI,
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                                Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            return takePictureIntent;
        }
        return null;
    }

    public static Uri processResult(Context context, File photoFile) {
        Bitmap bitmap;
        Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", photoFile);

        bitmap = getImageResized(context, fileUri);
        int rotation = getRotation(context, fileUri);
        bitmap = rotate(bitmap, rotation);

        try {
            FileOutputStream out = new FileOutputStream(photoFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileUri;
    }

    @Nullable
    public static File createImageFile(Context context) {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.FRANCE).format(new Date());
        String imageFileName = "mThmmy_" + timeStamp + ".jpg";

        File imageFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) +
                File.separator + "mThmmy");

        if (!imageFolder.exists()) {
            if (!imageFolder.mkdirs()) {
                Timber.w("Photos folder build returned false in %s", TakePhoto.class.getSimpleName());
                Toast.makeText(context, "Couldn't create photos directory", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        return new File(imageFolder, imageFileName);
    }

    public static void galleryAddPic(Context context, File photoFile) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, photoFile.getName());
        values.put(MediaStore.Images.Media.DESCRIPTION, IMAGE_CONTENT_DESCRIPTION);
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.BUCKET_ID, photoFile.toString().toLowerCase(Locale.US).hashCode());
        values.put(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, photoFile.getName().toLowerCase(Locale.US));
        values.put("_data", photoFile.getAbsolutePath());

        ContentResolver cr = context.getContentResolver();
        cr.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    private static Bitmap getImageResized(Context context, Uri selectedImage) {
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

    private static int getRotation(Context context, Uri imageUri) {
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

    private static Bitmap rotate(Bitmap bm, int rotation) {
        if (rotation != 0) {
            Matrix matrix = new Matrix();
            matrix.postRotate(rotation);
            return Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        }
        return bm;
    }
}
