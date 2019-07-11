package gr.thmmy.mthmmy.model;

import java.util.ArrayList;

public class Inbox {
    private ArrayList<PM> pms;
    private int currentPageIndex, numberOfPages;

    public int getCurrentPageIndex() {
        return currentPageIndex;
    }

    public void setCurrentPageIndex(int currentPageIndex) {
        this.currentPageIndex = currentPageIndex;
    }

    public int getNumberOfPages() {
        return numberOfPages;
    }

    public void setNumberOfPages(int numberOfPages) {
        this.numberOfPages = numberOfPages;
    }

    public ArrayList<PM> getPms() {
        return pms;
    }

    public void setPms(ArrayList<PM> pms) {
        this.pms = pms;
    }
}
