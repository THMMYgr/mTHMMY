package gr.thmmy.mthmmy.model;

import java.text.DecimalFormat;

public class Poll {
    private final String question;
    private Entry[] entries;
    private int availableVoteCount;
    private String pollFormUrl, sc, removeVoteUrl;

    public Poll(String question, Entry[] entries, int availableVoteCount, String pollFormUrl, String sc,
                String removeVoteUrl) {
        this.question = question;
        this.entries = entries;
        this.availableVoteCount = availableVoteCount;
        this.pollFormUrl = pollFormUrl;
        this.sc = sc;
        this.removeVoteUrl = removeVoteUrl;
    }

    public String getQuestion() {
        return question;
    }

    public Entry[] getEntries() {
        return entries;
    }

    public int getAvailableVoteCount() {
        return availableVoteCount;
    }

    public String getPollFormUrl() {
        return pollFormUrl;
    }

    public String getSc() {
        return sc;
    }

    public String getRemoveVoteUrl() {
        return removeVoteUrl;
    }

    public int totalVotes() {
        int sum = 0;
        for (Entry entry : entries) {
            sum += entry.votes;
        }
        return sum;
    }

    public String getVotePercentage(int index) {
        DecimalFormat format = new DecimalFormat(".#");
        double percentage = 100 * ((double) entries[index].votes / (double) totalVotes());
        return format.format(percentage);
    }

    static class Entry {
        private final String entryName;
        private int votes;

        public Entry(String entryName, int votes) {
            this.entryName = entryName;
            this.votes = votes;
        }

        /**
         * Constructor for entry with unknown number of votes
         *
         * @param entryName
         * The name of the entry
         */
        public Entry(String entryName) {
            this.entryName = entryName;
            votes = -1;
        }

        public String getEntryName() {
            return entryName;
        }

        public int getVotes() {
            return votes;
        }

        public void setVotes(int votes) {
            this.votes = votes;
        }
    }
}
