package gr.thmmy.mthmmy.model;

import java.text.DecimalFormat;

public class Poll extends TopicItem {
    public static final int TYPE_POLL = 3;

    private final String question;
    private Entry[] entries;
    private int availableVoteCount, selectedEntryIndex = -1;
    private String pollFormUrl, sc, removeVoteUrl, showVoteResultsUrl, showOptionsUrl;

    public Poll(String question, Entry[] entries, int availableVoteCount, String pollFormUrl, String sc,
                String removeVoteUrl, String showVoteResultsUrl, String showOptionsUrl, int selectedEntryIndex) {
        this.question = question;
        this.entries = entries;
        this.availableVoteCount = availableVoteCount;
        this.pollFormUrl = pollFormUrl;
        this.sc = sc;
        this.removeVoteUrl = removeVoteUrl;
        this.showVoteResultsUrl = showVoteResultsUrl;
        this.showOptionsUrl = showOptionsUrl;
        this.selectedEntryIndex = selectedEntryIndex;
    }

    private int totalVotes() {
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

    public String getShowVoteResultsUrl() {
        return showVoteResultsUrl;
    }

    public String getShowOptionsUrl() {
        return showOptionsUrl;
    }

    public int getSelectedEntryIndex() {
        return selectedEntryIndex;
    }

    public static class Entry {
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

        @Override
        public String toString() {
            return "Vote label:" + entryName + ", num votes:" + votes;
        }
    }
}
