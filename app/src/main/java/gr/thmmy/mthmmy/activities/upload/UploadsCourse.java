package gr.thmmy.mthmmy.activities.upload;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class UploadsCourse {
    private String name;
    private String minifiedName;
    private String greeklishName;

    public UploadsCourse(String fullName, String minifiedName, String greeklishName) {
        this.name = fullName;
        this.minifiedName = minifiedName;
        this.greeklishName = greeklishName;
    }

    String getName() {
        return name;
    }

    String getMinifiedName() {
        return minifiedName;
    }

    String getGreeklishName() {
        return greeklishName;
    }

    static Map<String, UploadsCourse> generateUploadsCourses(String[] uploadsCoursesRes){
        Map<String, UploadsCourse> uploadsCourses = new HashMap<>();
        for(String uploadsCourseStr:uploadsCoursesRes) {
            String[] split = uploadsCourseStr.split(",");
            UploadsCourse uploadsCourse = new UploadsCourse(split[0], split[1], split[2]);
            uploadsCourses.put(uploadsCourse.getName(),uploadsCourse);
        }
        return uploadsCourses;
    }

    static UploadsCourse findCourse(String retrievedCourse,
                                    Map<String, UploadsCourse> uploadsCourses){
        retrievedCourse = normalizeGreekNumbers(retrievedCourse);
        Timber.w("AAAAAAAA %s",retrievedCourse);
        UploadsCourse uploadsCourse = uploadsCourses.get(retrievedCourse);
        if(uploadsCourse != null) return uploadsCourse;

        String foundKey = null;
        for (Map.Entry<String, UploadsCourse> entry : uploadsCourses.entrySet()) {
            String key = entry.getKey();
            if (key.contains(retrievedCourse)&& (foundKey==null || key.length()>foundKey.length()))
                    foundKey = key;
        }

        if(foundKey==null){
            Timber.w("Couldn't find course that matches %s", retrievedCourse);
            //TODO: report to Firebase for a new Course
        }

        return uploadsCourses.get(foundKey);
    }

    private static String normalizeGreekNumbers(String stringWithGreekNumbers) {
        return stringWithGreekNumbers.replaceAll("Ι", "I");
    }
}
