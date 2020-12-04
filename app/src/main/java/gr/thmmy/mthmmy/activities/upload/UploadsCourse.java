package gr.thmmy.mthmmy.activities.upload;

import android.os.Bundle;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.base.BaseApplication;
import timber.log.Timber;

class UploadsCourse {
    private String name;
    private String minifiedName;
    private String greeklishName;

    private UploadsCourse(String fullName, String minifiedName, String greeklishName) {
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

    static Map<String, UploadsCourse> generateUploadsCourses(String[] uploadsCoursesRes) {
        Map<String, UploadsCourse> uploadsCourses = new HashMap<>();
        for (String uploadsCourseStr : uploadsCoursesRes) {
            String[] split = uploadsCourseStr.split(":");
            UploadsCourse uploadsCourse = new UploadsCourse(split[0], split[1], split[2]);
            uploadsCourses.put(uploadsCourse.getName(), uploadsCourse);
        }
        return uploadsCourses;
    }

    static UploadsCourse findCourse(String retrievedCourse,
                                    Map<String, UploadsCourse> uploadsCourses) {
        retrievedCourse = normalizeGreekNumbers(retrievedCourse);
        UploadsCourse uploadsCourse = uploadsCourses.get(retrievedCourse);
        if (uploadsCourse != null) return uploadsCourse;

        String foundKey = null;
        for (Map.Entry<String, UploadsCourse> entry : uploadsCourses.entrySet()) {
            String key = entry.getKey();
            if ((key.contains(retrievedCourse) || retrievedCourse.contains(key))
                    && (foundKey == null || key.length() > foundKey.length()))
                foundKey = key;
        }

        if (foundKey == null) {
            Timber.w("Couldn't find course that matches %s", retrievedCourse);
            Bundle bundle = new Bundle();
            bundle.putString("course_name", retrievedCourse);
            BaseApplication.getInstance().logFirebaseAnalyticsEvent("unsupported_uploads_course", bundle);
            return null;
        }

        return uploadsCourses.get(foundKey);
    }

    private static String normalizeGreekNumbers(String stringWithGreekNumbers) {
        StringBuilder normalizedStrBuilder = new StringBuilder(stringWithGreekNumbers);
        Pattern pattern = Pattern.compile("(Ι+)(?:\\s|\\(|\\)|$)");
        Matcher matcher = pattern.matcher(stringWithGreekNumbers);
        while (matcher.find())
            normalizedStrBuilder.replace(matcher.start(1), matcher.end(1), matcher.group(1).replaceAll("Ι", "I"));
        return normalizedStrBuilder.toString();
    }
}
