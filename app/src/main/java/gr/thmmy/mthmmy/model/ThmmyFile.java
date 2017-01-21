package gr.thmmy.mthmmy.model;

import java.net.URL;

public class ThmmyFile {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    private static final String TAG = "ThmmyFile";
    private final URL fileUrl;
    private final String filename, fileInfo;

    /**
     * This constructor only creates a ThmmyFile object and <b>does not download</b> the file.
     *
     * @param fileUrl  {@link URL} object with file's url
     * @param filename {@link String} with desired file name
     * @param fileInfo {@link String} with any extra information (like number of downloads)
     */
    public ThmmyFile(URL fileUrl, String filename, String fileInfo) {
        this.fileUrl = fileUrl;
        this.filename = filename;
        this.fileInfo = fileInfo;
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
}
