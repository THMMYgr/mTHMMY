package gr.thmmy.mthmmy.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Objects;

public class Bookmark implements java.io.Serializable {
    private final String title, id;
    private boolean isNotificationsEnabled;

    public Bookmark(String title, String id, boolean isNotificationsEnabled) {
        this.title = title;
        this.id = id;
        this.isNotificationsEnabled = isNotificationsEnabled;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public boolean isNotificationsEnabled() {
        return isNotificationsEnabled;
    }

    public void toggleNotificationsEnabled(){
        this.isNotificationsEnabled = !this.isNotificationsEnabled;
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

    public static boolean matchExistsById(ArrayList<Bookmark> array, int id) {
        if (array != null && !array.isEmpty()) {
            for (Bookmark bookmark : array) {
                if (bookmark != null) {
                    if (Objects.equals(Integer.parseInt(bookmark.getId()), id))
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
    public static String arrayListToString(@NonNull ArrayList<Bookmark> arrayList) {
        StringBuilder returnString = new StringBuilder();
        for (Bookmark bookmark : arrayList) {
            if (bookmark != null) {
                returnString.append(bookmark.getId()).append("\t");
                returnString.append(bookmark.getTitle()).append("\t");
                returnString.append(bookmark.isNotificationsEnabled()).append("\n");
            }
        }
        if (!Objects.equals(returnString.toString(), "")) return returnString.toString();
        else return null;
    }

    public static ArrayList<Bookmark> stringToArrayList(@NonNull String string) {
        ArrayList<Bookmark> returnArray = new ArrayList<>();
        String[] lines = string.split("\n");
        for (String line : lines) {
            if (line == null || line.isEmpty() || Objects.equals(line, "")) break;
            String[] parameters = line.split("\t");
            if (parameters.length != 3) break;
            returnArray.add(new Bookmark(parameters[1], parameters[0], Boolean.parseBoolean(parameters[2])));
        }
        return returnArray;
    }
}
