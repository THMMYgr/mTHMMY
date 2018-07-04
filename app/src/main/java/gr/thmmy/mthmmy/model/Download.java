package gr.thmmy.mthmmy.model;

public class Download {
    public enum DownloadItemType {DOWNLOADS_CATEGORY, DOWNLOADS_FILE}

    private final String url, title, subTitle, statNumbers, extraInfo;
    private final boolean hasSubCategory;
    private final DownloadItemType type;
    private String fileName;

    public Download() {
        type = null;
        url = null;
        title = null;
        subTitle = null;
        statNumbers = null;
        hasSubCategory = false;
        extraInfo = null;
    }

    public Download(DownloadItemType type, String url, String title, String subTitle,
                    String statNumbers, boolean hasSubCategory, String extraInfo) {
        this.type = type;
        this.url = url;
        this.title = title;
        this.subTitle = subTitle;
        this.statNumbers = statNumbers;
        this.hasSubCategory = hasSubCategory;
        this.extraInfo = extraInfo;
    }

    public DownloadItemType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public String getStatNumbers() {
        return statNumbers;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public boolean hasSubCategory() {
        return hasSubCategory;
    }

    public String getFileName(){
        return fileName;
    }

    public void setFileName(String fileName){
        this.fileName = fileName;
    }
}
