package gr.thmmy.mthmmy.model;

import android.webkit.URLUtil;

import java.net.URL;

public class ThmmyFile {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    private static final String TAG = "ThmmyFile";
    private final URL fileUrl;
    private final String fileName, fileInfo;

    /**
     * This constructor only creates a ThmmyFile object and <b>does not download</b> the file.
     *
     * @param fileUrl  {@link URL} object with file's url
     * @param fileName {@link String} with desired file name
     * @param fileInfo {@link String} with any extra information (like number of downloads)
     */
    public ThmmyFile(URL fileUrl, String fileName, String fileInfo) {
        this.fileUrl = fileUrl;
        if(fileName!=null)
            this.fileName = fileName;
        else
            this.fileName = URLUtil.guessFileName(fileUrl.toString(), null, null);
        this.fileInfo = fileInfo;
    }

    public ThmmyFile(URL fileUrl) {
        this.fileUrl = fileUrl;
        this.fileName = URLUtil.guessFileName(fileUrl.toString(), null, null);
        this.fileInfo = null;
    }

    public URL getFileUrl() {
        return fileUrl;
    }

    public String getFilename() {
        return fileName;
    }

    public String getFileInfo() {
        return fileInfo;
    }
}
