package gr.thmmy.mthmmy.model;

import java.util.ArrayList;

public class UploadCategory {
    private String value, categoryTitle;
    private ArrayList<UploadCategory> subCategories = new ArrayList<>();

    private UploadCategory() {
        //Disables default constructor
    }

    public UploadCategory(String value, String categoryTitle) {
        this.value = value;
        this.categoryTitle = categoryTitle;
    }

    public String getValue() {
        return value;
    }

    public String getCategoryTitle() {
        return categoryTitle;
    }

    public void addSubCategory(String value, String categoryTitle) {
        subCategories.add(new UploadCategory(value, categoryTitle));
    }

    public ArrayList<UploadCategory> getSubCategories() {
        return subCategories;
    }

    public boolean hasSubCategories() {
        return !subCategories.isEmpty();
    }
}