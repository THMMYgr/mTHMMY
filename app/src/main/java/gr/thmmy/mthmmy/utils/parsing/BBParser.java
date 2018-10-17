package gr.thmmy.mthmmy.utils.parsing;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.StyleSpan;

import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.model.BBTag;
import timber.log.Timber;

public class BBParser {
    private static final String[] supportedTags = {"b"};

    public static SpannedString bb2span(String bb) {
        SpannableStringBuilder builder = new SpannableStringBuilder(bb);
        BBTag[] tags = getTags(bb);
        for (BBTag tag : tags) {
            switch (tag.getName()) {
                case "b":
                    builder.setSpan(new StyleSpan(Typeface.BOLD), tag.getStart(), tag.getEnd(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                default:
                    throw new UnsupportedCharsetException("Tag not supported");
            }
        }
    }

    public static BBTag[] getTags(String bb) {
        Pattern bbtagPattern = Pattern.compile("\\[(.+?)\\]");

        LinkedList<BBTag> tags = new LinkedList<>();
        Matcher bbMatcher = bbtagPattern.matcher(bb);
        while (bbMatcher.find()) {
            String name = bbMatcher.group(0);
            if (name.startsWith("/")) {
                //closing tag
                name = name.substring(1);
                for (int i = tags.size() - 1; i >= 0; i--) {
                    if (tags.get(i).getName().equals(name)) {
                        tags.get(i).setEnd(bbMatcher.start());
                        break;
                    }
                }
                continue;
            }
            if (isSupported(name))
                tags.add(new BBTag(bbMatcher.start(), name));
        }
        return tags.toArray(new BBTag[0]);
    }

    public static boolean isSupported(String tagName) {
        for (String tag : supportedTags)
            if (TextUtils.equals(tag, tagName)) return true;
        return false;
    }
}
