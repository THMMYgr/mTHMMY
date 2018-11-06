package gr.thmmy.mthmmy.utils.parsing;

import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;

import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.model.BBTag;
import gr.thmmy.mthmmy.model.HtmlTag;

public class BBParser {
    private static final String[] ALL_TAGS = {"b", "i", "u", "s", "glow", "shadow", "move", "pre", "lefter",
            "center", "right", "hr", "size", "font", "color", "youtube", "flash", "img", "url"
            , "email", "ftp", "table", "tr", "td", "sup", "sub", "tt", "code", "quote", "tex", "list", "li"};
    private static final String[] SUPPORTED_TAGS = {"b", "i", "u", "s"};

    public static SpannableStringBuilder bb2span(String bb) {
        SpannableStringBuilder builder = new SpannableStringBuilder(bb);
        // store the original indices of the string
        LinkedList<Integer> stringIndices = new LinkedList<>();
        for (int i = 0; i < builder.length(); i++) {
            stringIndices.add(i);
        }

        BBTag[] tags = getTags(bb);
        for (BBTag tag : tags) {
            int start = stringIndices.indexOf(tag.getStart());
            int end = stringIndices.indexOf(tag.getEnd());
            int startTagLength = tag.getName().length() + 2;
            int endTagLength = tag.getName().length() + 3;
            switch (tag.getName()) {
                case "b":
                    builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "i":
                    builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "u":
                    builder.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                case "s":
                    builder.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    break;
                default:
                    throw new UnsupportedCharsetException("Tag not supported");
            }
            //remove starting and ending tag and and do the same changes in the list
            builder.delete(start, start + startTagLength);
            for (int i = start; i < start + startTagLength; i++) {
                stringIndices.remove(start);
            }
            builder.delete(end - startTagLength, end - startTagLength + endTagLength);
            for (int i = end - startTagLength; i < end - startTagLength + endTagLength; i++) {
                stringIndices.remove(end - startTagLength);
            }
        }
        return builder;
    }

    public static SpannableStringBuilder html2span(String html) {
        SpannableStringBuilder builder = new SpannableStringBuilder(html);
        // store the original indices of the string
        LinkedList<Integer> stringIndices = new LinkedList<>();
        for (int i = 0; i < builder.length(); i++) {
            stringIndices.add(i);
        }

        HtmlTag[] tags = getHtmlTags(html);
        for (HtmlTag tag : tags) {
            int start = stringIndices.indexOf(tag.getStart());
            int end = stringIndices.indexOf(tag.getEnd());
            int startTagLength = tag.getName().length() + 2;
            int endTagLength = tag.getName().length() + 3;

            switch (tag.getName()) {

            }

            //remove starting and ending tag and and do the same changes in the list
            builder.delete(start, start + startTagLength);
            for (int i = start; i < start + startTagLength; i++) {
                stringIndices.remove(start);
            }
            builder.delete(end - startTagLength, end - startTagLength + endTagLength);
            for (int i = end - startTagLength; i < end - startTagLength + endTagLength; i++) {
                stringIndices.remove(end - startTagLength);
            }
        }
        return builder;
    }

    public static BBTag[] getTags(String bb) {
        Pattern bbtagPattern = Pattern.compile("\\[(.+?)\\]");

        LinkedList<BBTag> tags = new LinkedList<>();
        Matcher bbMatcher = bbtagPattern.matcher(bb);
        while (bbMatcher.find()) {
            String startTag = bbMatcher.group(1);
            int separatorIndex = startTag.indexOf('=');
            String name, attribute = null;
            if (separatorIndex > 0) {
                attribute = startTag.substring(separatorIndex);
                name = startTag.substring(0, separatorIndex);
            } else
                name = startTag;

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
                tags.add(new BBTag(bbMatcher.start(), name, attribute));
        }
        // remove parsed tags with no end tag
        for (BBTag bbTag : tags)
            if (bbTag.getEnd() == 0)
                tags.remove(bbTag);
        return tags.toArray(new BBTag[0]);
    }

    public static HtmlTag[] getHtmlTags(String html) {
        Pattern htmlPattern = Pattern.compile("<(.+?)>");

        LinkedList<HtmlTag> tags = new LinkedList<>();
        Matcher htmlMatcher = htmlPattern.matcher(html);
        while (htmlMatcher.find()) {
            String startTag = htmlMatcher.group(1);
            int separatorIndex = startTag.indexOf(' ');
            String name, attribute = null, attributeValue = null;
            if (separatorIndex > 0) {
                String fullAttribute = startTag.substring(separatorIndex);
                int equalsIndex = fullAttribute.indexOf('=');
                attribute = fullAttribute.substring(0, equalsIndex);
                attributeValue = fullAttribute.substring(equalsIndex);
                name = startTag.substring(0, separatorIndex);
            } else
                name = startTag;

            if (name.startsWith("/")) {
                //closing tag
                name = name.substring(1);
                for (int i = tags.size() - 1; i >= 0; i--) {
                    if (tags.get(i).getName().equals(name)) {
                        tags.get(i).setEnd(htmlMatcher.start());
                        break;
                    }
                }
                continue;
            }
            if (isHtmlTagSupported(name, attribute, attributeValue))
                tags.add(new HtmlTag(htmlMatcher.start(), name, attribute, attributeValue));
        }
        // remove parsed tags with no end tag
        for (HtmlTag htmlTag : tags)
            if (htmlTag.getEnd() == 0)
                tags.remove(htmlTag);
        return tags.toArray(new HtmlTag[0]);
    }

    private static boolean isHtmlTagSupported(String name, String attribute, String attributeValue) {
        return false;
    }

    public static boolean isSupported(String tagName) {
        for (String tag : SUPPORTED_TAGS)
            if (TextUtils.equals(tag, tagName)) return true;
        return false;
    }
}
