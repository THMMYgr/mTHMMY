package gr.thmmy.mthmmy.model;

public class BBTag {
    private int start, end;
    private String name;

    public BBTag(int start, String name) {
        this.start = start;
        this.name = name;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
