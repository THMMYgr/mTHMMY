package gr.thmmy.mthmmy.utils.parsing;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.URLSpan;
import android.text.style.UnderlineSpan;

import java.nio.charset.UnsupportedCharsetException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import gr.thmmy.mthmmy.model.BBTag;
import gr.thmmy.mthmmy.model.HtmlTag;
import gr.thmmy.mthmmy.utils.HTMLUtils;

public class ThmmyParser {
    private static final String[] ALL_BB_TAGS = {"b", "i", "u", "s", "glow", "shadow", "move", "pre", "lefter",
            "center", "right", "hr", "size", "font", "color", "youtube", "flash", "img", "url"
            , "email", "ftp", "table", "tr", "td", "sup", "sub", "tt", "code", "quote", "tex", "list", "li"};
    private static final String[] ALL_HTML_TAGS = {"b", "br", "span", "i", "div", "del", "marquee", "pre",
            "hr", "embed", "noembed", "a", "img", "table", "tr", "td", "sup", "sub", "tt", "pre", "ul", "li"};

    public static SpannableStringBuilder bb2span(String bb) {
        SpannableStringBuilder builder = new SpannableStringBuilder(bb);
        // store the original indices of the string
        LinkedList<Integer> stringIndices = new LinkedList<>();
        for (int i = 0; i < builder.length(); i++) {
            stringIndices.add(i);
        }

        BBTag[] tags = getBBTags(bb);
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

    public static SpannableStringBuilder html2span(Context context, String html) {
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
            if (tag.getAttributeKey() != null) {
                startTagLength += tag.getAttributeKey().length() + tag.getAttributeValue().length() + 4;
            }
            int endTagLength = tag.getName().length() + 3;

            if (isHtmlTagSupported(tag.getName(), tag.getAttributeKey(), tag.getAttributeValue())) {
                switch (tag.getName()) {
                    case "b":
                        builder.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    case "i":
                        builder.setSpan(new StyleSpan(Typeface.ITALIC), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    case "span":
                        if (tag.getAttributeKey().equals("style") && tag.getAttributeValue().equals("text-decoration: underline;")) {
                            builder.setSpan(new UnderlineSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        break;
                    case "del":
                        builder.setSpan(new StrikethroughSpan(), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        break;
                    case "a":
                        URLSpan urlSpan = new URLSpan(tag.getAttributeValue());
                        builder.setSpan(urlSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        HTMLUtils.makeLinkClickable(context, builder, urlSpan);
                        break;
                    default:
                        throw new UnsupportedCharsetException("Tag not supported");
                }
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

    public static BBTag[] getBBTags(String bb) {
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
            if (isBBTagSupported(name))
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
                attribute = fullAttribute.substring(1, equalsIndex);
                attributeValue = fullAttribute.substring(equalsIndex + 2, fullAttribute.length() - 1);
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
            if (isHtmlTag(name))
                tags.add(new HtmlTag(htmlMatcher.start(), name, attribute, attributeValue));
        }
        // remove parsed tags with no end tag
        for (HtmlTag htmlTag : tags)
            if (htmlTag.getEnd() == 0)
                tags.remove(htmlTag);
        return tags.toArray(new HtmlTag[0]);
    }

    private static boolean isHtmlTagSupported(String name, String attribute, String attributeValue) {
        return name.equals("b") || name.equals("i") || name.equals("span") || name.equals("del") || name.equals("a");
    }

    public static boolean isBBTagSupported(String name) {
        return name.equals("b") || name.equals("i") || name.equals("u") || name.equals("s");
    }

    public static boolean isHtmlTag(String tagName) {
        for (String tag : ALL_HTML_TAGS)
            if (TextUtils.equals(tag, tagName)) return true;
        return false;
    }

    public static boolean containsHtml(String s) {
        return getHtmlTags(s).length > 0;
    }
}
