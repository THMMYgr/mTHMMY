package gr.thmmy.mthmmy.model;

import androidx.annotation.NonNull;

public class HtmlTag {
    private int start, end;
    private String name, attributeKey, attributeValue;

    public HtmlTag(int start, String name) {
        this.start = start;
        this.name = name;
    }

    public HtmlTag(int start, String name, String attributeKey, String attributeValue) {
        this.start = start;
        this.name = name;
        this.attributeKey = attributeKey;
        this.attributeValue = attributeValue;
    }

    @NonNull
    @Override
    public String toString() {
        return "start:" + start + ",end:" + end + ",name:" + name;
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

    public String getAttributeKey() {
        return attributeKey;
    }

    public String getAttributeValue() {
        return attributeValue;
    }
}
