package gr.thmmy.mthmmy.utils.crashreporting;


import com.google.firebase.crashlytics.FirebaseCrashlytics;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;

public class CrashReporter {
    private static final int STRING_BATCH_LENGTH = 250;

    private CrashReporter() {}

    public static void reportForumInfo(Document document) {
        ParseHelpers.Theme theme = ParseHelpers.parseTheme(document);
        ParseHelpers.Language language = ParseHelpers.Language.getLanguage(document);
        String themeKey = "forum theme", themeValue;
        String languageKey = "forum language", languageValue;
        switch (theme) {
            case SCRIBBLES2:
                themeValue = "Scribbles2";
                break;
            case SMF_DEFAULT:
                themeValue = "SMF Default Theme";
                break;
            case SMFONE_BLUE:
                themeValue = "SMFone_Blue";
                break;
            case HELIOS_MULTI:
                themeValue = "Helios_Multi";
                break;
            default:
                themeValue = "Unknown theme";
        }

        if (language == ParseHelpers.Language.GREEK)
            languageValue = "Greek";
        else if (language == ParseHelpers.Language.ENGLISH)
            languageValue = "English";
        else
            languageValue = "Unknown";

        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCustomKey(themeKey, themeValue);
        crashlytics.setCustomKey(languageKey, languageValue);
        crashlytics.setCustomKey("isLoggedIn", BaseApplication.getInstance().getSessionManager().isLoggedIn());
    }

    public static void reportDocument(Document document, String key) {
        FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
        String documentString = document.toString();

        ParseHelpers.Language language = ParseHelpers.Language.getLanguage(document);
        Elements postRows;
        if (language == ParseHelpers.Language.GREEK)
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
            crashlytics.setCustomKey(key + "_" + i + 1, batch);
        }
    }
}
