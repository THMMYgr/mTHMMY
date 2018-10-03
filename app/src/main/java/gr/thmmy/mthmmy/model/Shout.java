package gr.thmmy.mthmmy.model;

public class Shout {
    private final String shouter, shouterProfileURL, date, shout;

    public Shout(String shouter, String shouterProfileURL, String date, String shout) {
        this.shouter = shouter;
        this.shouterProfileURL = shouterProfileURL;
        this.date = date;
        this.shout = shout;
    }

    public String getShouter() {
        return shouter;
    }

    public String getShouterProfileURL() {
        return shouterProfileURL;
    }

    public String getDate() {
        return date;
    }

    public String getShout() {
        return shout;
    }
}
