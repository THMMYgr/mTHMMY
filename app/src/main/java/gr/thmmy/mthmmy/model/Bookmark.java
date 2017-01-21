package gr.thmmy.mthmmy.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class Bookmark implements java.io.Serializable {
    private final String title, id;

    public Bookmark(String title, String id) {
        this.title = title;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public boolean matchExists(ArrayList<Bookmark> array) {
        if (array != null && !array.isEmpty()) {
            for (Bookmark bookmark : array) {
                if (bookmark != null) {
                    if (Objects.equals(bookmark.getId(), this.id)
                            && Objects.equals(bookmark.getTitle(), this.title))
                        return true;
                }
            }
        }
        return false;
    }

    public int findIndex(ArrayList<Bookmark> array) {
        if (array != null && !array.isEmpty()) {
            for (int i = 0; i < array.size(); ++i) {
                if (array.get(i) != null && Objects.equals(array.get(i).getId(), this.id)
                        && Objects.equals(array.get(i).getTitle(), this.title))
                    return i;
            }
        }
        return -1;
    }

    @Nullable
    public static String arrayToString(@NonNull ArrayList<Bookmark> arrayList) {
        String returnString = "";
        for (Bookmark bookmark : arrayList) {
            if (bookmark != null) {
                returnString += (bookmark.getId() + "\t");
                returnString += (bookmark.getTitle() + "\n");
            }
        }
        if (!Objects.equals(returnString, "")) return returnString;
        else return null;
    }

    public static ArrayList<Bookmark> arrayFromString(@NonNull String string) {
        ArrayList<Bookmark> returnArray = new ArrayList<>();
        String[] lines = string.split("\n");
        for (String line : lines) {
            if (line == null || line.isEmpty() || Objects.equals(line, "")) break;
            String[] parameters = line.split("\t");
            if (parameters.length != 2) break;
            returnArray.add(new Bookmark(parameters[1], parameters[0]));
        }
        return returnArray;
    }
}
