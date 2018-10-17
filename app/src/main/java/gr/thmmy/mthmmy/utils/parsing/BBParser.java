package gr.thmmy.mthmmy.utils.parsing;

import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.model.BBTag;
import timber.log.Timber;

public class BBParser {
    public static final String[] supportedTags = {"b"};

    public SpannedString bb2span(String bb) {
        SpannableStringBuilder builder = new SpannableStringBuilder(bb);
        BBTag[] tags = getTags(bb);
    }

    public BBTag[] getTags(String bb) {
        Pattern bbtagPattern = Pattern.compile("[*+]");

        LinkedList<BBTag> tags = new LinkedList<>();
        String searcingString = bb;
        Matcher bbMatcher = bbtagPattern.matcher(searcingString);
        while (bbMatcher.find()) {
            String name = bbMatcher.group(0);
            if (!isSupported(name)) continue;
            tags.add(new BBTag(bbMatcher.start(), name));
            searcingString = searcingString.substring(bbMatcher.start() + name.length());
            bbMatcher = bbtagPattern.matcher(searcingString);
        }
        return tags.toArray(new BBTag[0]);
    }

    public boolean isSupported(String tagName) {
        for (String tag : supportedTags)
            if (TextUtils.equals(tag, tagName)) return true;
        return false;
    }
}
