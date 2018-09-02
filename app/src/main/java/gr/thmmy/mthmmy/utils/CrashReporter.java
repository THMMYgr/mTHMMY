package gr.thmmy.mthmmy.utils;

import com.crashlytics.android.Crashlytics;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;

public class CrashReporter {
    private static final int STRING_BATCH_LENGTH = 250;

    private CrashReporter() {}

    public static void reportDocument(Document document, String key) {
        String documentString = document.toString();

        ParseHelpers.Language language = ParseHelpers.Language.getLanguage(document);
        Elements postRows;
        if (language.is(ParseHelpers.Language.GREEK))
            postRows = document.select("form[id=quickModForm]>table>tbody>tr:matches(στις)");
        else
            postRows = document.select("form[id=quickModForm]>table>tbody>tr:matches(on)");
        for (Element thisRow : postRows) {
            String subject = thisRow.select("div[id^=subject_]").first().select("a").first().text();
            documentString = documentString.replace(subject, "subject");
            String post = thisRow.select("div").select(".post").first().text();
            documentString = documentString.replace(post, "post");
        }

        int batchCount = documentString.length() / STRING_BATCH_LENGTH;
        for (int i = 0; i < batchCount; i++) {
            String batch;
            if (i != batchCount - 1)
                batch = documentString.substring(i * STRING_BATCH_LENGTH, (i + 1) * STRING_BATCH_LENGTH);
            else
                batch = documentString.substring(i * STRING_BATCH_LENGTH);
            Crashlytics.setString(key + "_" + i + 1, batch);
        }
    }
}
