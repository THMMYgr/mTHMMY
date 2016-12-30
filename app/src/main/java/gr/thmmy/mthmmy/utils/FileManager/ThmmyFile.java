package gr.thmmy.mthmmy.utils.FileManager;

import android.os.Environment;
import android.support.annotation.Nullable;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Objects;

import mthmmy.utils.Report;
import okhttp3.Request;
import okhttp3.Response;

import static gr.thmmy.mthmmy.activities.base.BaseActivity.getClient;

/**
 * Used for downloading and storing a file from the forum using {@link okhttp3}.
 * <p>Class has one constructor: <ul><li>{@link #ThmmyFile(URL, String, String)}</li></ul>
 * and the methods:<ul><li>getters</li>
 * <li>{@link #download()}</li>
 * <li>{@link #download(String)}</li></ul></p>
 */
public class ThmmyFile {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    private static final String TAG = "ThmmyFile";
    /**
     * Folder name used when downloading files without a package name.
     */
    private static final String NO_PACKAGE_FOLDER_NAME = "Other";
    private final URL fileUrl;
    private final String filename, fileInfo;
    private String extension, filePath;
    private File file;

    /**
     * This constructor only creates a ThmmyFile object and <b>does not download</b> the file. To download
     * the file use {@link #download(String)} or {@link #download()}!
     *
     * @param fileUrl  {@link URL} object with file's url
     * @param filename {@link String} with desired file name
     * @param fileInfo {@link String} with any extra information (like number of downloads)
     */
    public ThmmyFile(URL fileUrl, String filename, String fileInfo) {
        this.fileUrl = fileUrl;
        this.filename = filename;
        this.fileInfo = fileInfo;
        this.extension = null;
        this.filePath = null;
        this.file = null;
    }

    public URL getFileUrl() {
        return fileUrl;
    }

    public String getFilename() {
        return filename;
    }

    public String getFileInfo() {
        return fileInfo;
    }

    /**
     * This is null until {@link #download(String)} or {@link #download()} is called and has succeeded.
     *
     * @return String with file's extension or null
     */
    @Nullable
    public String getExtension() {
        return extension;
    }

    /**
     * This is null until {@link #download(String)} or {@link #download()} is called and has succeeded.
     *
     * @return String with file's path or null
     */
    @Nullable
    public String getFilePath() {
        return filePath;
    }

    /**
     * This is null until {@link #download(String)} or {@link #download()} is called and has succeeded.
     *
     * @return {@link File} or null
     */
    @Nullable
    public File getFile() {
        return file;
    }

    private void setExtension(String extension) {
        this.extension = extension;
    }

    private void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Used to download the file. If download is successful file's extension and path will be assigned
     * to object's fields and can be accessed using getter methods.
     * <p>File is stored in sdcard1/Android/data/Downloads/packageName</p>
     *
     * @return the {@link File} if successful, null otherwise
     * @throws IOException       if the request could not be executed due to cancellation, a connectivity
     *                           problem or timeout. Because networks can fail during an exchange, it is possible that the
     *                           remote server accepted the request before the failure.
     * @throws SecurityException if the requested file is not hosted by the forum.
     */
    @Nullable
    public File download() throws IOException, SecurityException {
        if (!Objects.equals(fileUrl.getHost(), "www.thmmy.gr"))
            throw new SecurityException("Downloading files from other sources is not supported");

        Request request = new Request.Builder().url(fileUrl).build();

        Response response = getClient().newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Failed to download file: " + response);
        }
        file = getOutputMediaFile(filename);
        if (file == null) {
            Report.d(TAG, "Error creating media file, check storage permissions!");
        } else {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(response.body().bytes());
            fos.close();

            filePath = file.getAbsolutePath();
            extension = MimeTypeMap.getFileExtensionFromUrl(
                    filePath.substring(filePath.lastIndexOf("/")));
        }
        return file;
    }

    @Nullable
    private static File getOutputMediaFile(String fileName) {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/gr.thmmy.mthmmy/"
                + "Downloads/");

        // This location works best if you want the created files to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Report.d(TAG, "problem!");
                return null;
            }
        }
        // Create a media file name
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + fileName);
        return mediaFile;
    }
}
