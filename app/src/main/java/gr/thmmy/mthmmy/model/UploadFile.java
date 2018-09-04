package gr.thmmy.mthmmy.model;

import android.net.Uri;
import android.support.annotation.Nullable;

import java.io.File;

public class UploadFile {
    private final boolean isCameraPhoto;
    private Uri fileUri;
    private File photoFile;

    private UploadFile() {
        isCameraPhoto = false;
        fileUri = null;
        photoFile = null;
    }

    public UploadFile(boolean isCameraPhoto, Uri fileUri, @Nullable File photoFile) {
        this.isCameraPhoto = isCameraPhoto;
        this.fileUri = fileUri;
        this.photoFile = photoFile;
    }

    public boolean isCameraPhoto() {
        return isCameraPhoto;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public File getPhotoFile() {
        return photoFile;
    }

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public void setPhotoFile(File photoFile) {
        this.photoFile = photoFile;
    }
}
