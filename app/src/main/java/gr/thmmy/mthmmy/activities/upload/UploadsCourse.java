package gr.thmmy.mthmmy.activities.upload;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class UploadsCourse {
    private final int id;
    private final String name, minifiedName, greeklishName;

    private UploadsCourse(int id, String name, String minifiedName, String greeklishName) {
        this.id = id;
        this.name = name;
        this.minifiedName = minifiedName;
        this.greeklishName = greeklishName;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getMinifiedName() {
        return minifiedName;
    }

    public String getGreeklishName() {
        return greeklishName;
    }

    public static HashMap<Integer, UploadsCourse> generateCoursesFromJSON(JSONObject json) throws JSONException {
        HashMap<Integer, UploadsCourse> coursesHashMap = new HashMap<>();
        if(json.has("courses")){
            JSONArray coursesArray = json.getJSONArray("courses");
            for(int i=0, size = coursesArray.length(); i<size; i++) {
                JSONObject course = coursesArray.getJSONObject(i);
                int id = course.getInt("id");
                String name = course.getString("name");
                String minifiedName = course.getString("minified");
                String greeklisName = course.getString("greeklish");
                coursesHashMap.put(course.getInt("id"), new UploadsCourse(id, name, minifiedName, greeklisName));
            }
        }

        if(json.has("categories")){
            JSONArray categoriesArray = json.getJSONArray("categories");
            for(int i=0, size = categoriesArray.length(); i<size; i++) {
                JSONObject category = categoriesArray.getJSONObject(i);
                coursesHashMap.putAll(generateCoursesFromJSON(category));
            }
        }

        return coursesHashMap;
    }
}
