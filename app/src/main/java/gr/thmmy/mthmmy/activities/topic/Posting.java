package gr.thmmy.mthmmy.activities.topic;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import okhttp3.Response;
import timber.log.Timber;

/**
 * This is a utility class containing a collection of static methods to help with topic replying.
 */
class Posting {
    /**
     * {@link REPLY_STATUS} enum defines the different possible outcomes of a topic reply request.
     */
    enum REPLY_STATUS {
        /**
         * The request was successful
         */
        SUCCESSFUL,
        /**
         * Request was lacking a subject
         */
        NO_SUBJECT,
        /**
         * Request had empty body
         */
        EMPTY_BODY,
        /**
         * There were new topic replies while making the request
         */
        NEW_REPLY_WHILE_POSTING,
        /**
         * Error 404, page was not found
         */
        NOT_FOUND,
        /**
         * User session ended while posting the reply
         */
        SESSION_ENDED,
        /**
         * Other undefined of unidentified error
         */
        OTHER_ERROR
    }

    /**
     * This method can be used to check whether a topic post request was successful or not and if
     * not maybe get the reason why.
     *
     * @param response {@link okhttp3.Response} of the request
     * @return a {@link REPLY_STATUS} that describes the response status
     * @throws IOException method relies to {@link org.jsoup.Jsoup#parse(String)}
     */
    static REPLY_STATUS replyStatus(Response response) throws IOException {
        if (response.code() == 404) return REPLY_STATUS.NOT_FOUND;
        if (response.code() < 200 || response.code() >= 400) return REPLY_STATUS.OTHER_ERROR;
        String finalUrl = response.request().url().toString();
        if (finalUrl.contains("action=post")) {
            Document postErrorPage = Jsoup.parse(response.body().string());
            String[] errors = postErrorPage.select("tr[id=errors] div[id=error_list]").first()
                    .toString().split("<br>");
            for (int i = 0; i < errors.length; ++i) { //TODO test
                Timber.d(String.valueOf(i));
                Timber.d(errors[i]);
            }
            for (String error : errors) {
                if (error.contains("Your session timed out while posting") ||
                        error.contains("Υπερβήκατε τον μέγιστο χρόνο σύνδεσης κατά την αποστολή"))
                    return REPLY_STATUS.SESSION_ENDED;
                if (error.contains("No subject was filled in")
                        || error.contains("Δεν δόθηκε τίτλος"))
                    return REPLY_STATUS.NO_SUBJECT;
                if (error.contains("The message body was left empty")
                        || error.contains("Δεν δόθηκε κείμενο για το μήνυμα"))
                    return REPLY_STATUS.EMPTY_BODY;
            }
            return REPLY_STATUS.NEW_REPLY_WHILE_POSTING;
        }
        return REPLY_STATUS.SUCCESSFUL;
    }

    /**
     * This is a fucked up method.. Just don't waste your time here unless you have suicidal
     * tendencies.
     *
     * @param html the html string to be transformed to BBcode
     * @return the BBcode string
     */
    static String htmlToBBcode(String html) {
        Log.d("Cancer", html);
        Map<String, String> bbMap = new HashMap<>();
        Map<String, String> smileysMap1 = new HashMap<>();
        Map<String, String> smileysMap2 = new HashMap<>();

        smileysMap1.put("Smiley", ":)");
        smileysMap1.put("Wink", ";)");
        smileysMap1.put("Cheesy", ":D");
        smileysMap1.put("Grin", ";D");
        smileysMap1.put("Angry", ">:(");
        smileysMap1.put("Sad", ":(");
        smileysMap1.put("Shocked", ":o");
        smileysMap1.put("Cool", "8))");
        smileysMap1.put("Huh", ":???:");
        smileysMap1.put("Roll Eyes", "::)");
        smileysMap1.put("Tongue", ":P");
        smileysMap1.put("Embarrassed", ":-[");
        smileysMap1.put("Lips Sealed", ":-X");
        smileysMap1.put("Kiss", ":-*");
        smileysMap1.put("Cry", ":'(");
        smileysMap1.put("heart", "<3");
        smileysMap1.put("kleidaria", "^locked^");
        smileysMap1.put("roll_over", "^rollover^");
        smileysMap1.put("redface", "^redface^");
        smileysMap1.put("confused", "^confused^");
        smileysMap1.put("innocent", "^innocent^");
        smileysMap1.put("sleep", "^sleep^");
        smileysMap1.put("lips_sealed", "^sealed^");
        smileysMap1.put("cool", "^cool^");
        smileysMap1.put("crazy", "^crazy^");
        smileysMap1.put("mad", "^mad^");
        smileysMap1.put("wav", "^wav^");
        smileysMap1.put("BinkyBaby", "^binkybaby^");
        smileysMap1.put("DontKnow", "^dontknow^");
        smileysMap1.put("angry4", ":angry4:");
        smileysMap1.put("angryAndHot", "^angryhot^");
        smileysMap1.put("angry", "^angry^");
        smileysMap1.put("bang_head", "^banghead^");
        smileysMap1.put("CryBaby", "^crybaby^");
        smileysMap1.put("Hello", "^hello^");
        smileysMap1.put("jerk", "^jerk^");
        smileysMap1.put("NoNo", "^nono^");
        smileysMap1.put("NotWorthy", "^notworthy^");
        smileysMap1.put("Off-topic", "^off-topic^");
        smileysMap1.put("Puke", "^puke^");
        smileysMap1.put("Shout", "^shout^");
        smileysMap1.put("Slurp", "^slurp^");
        smileysMap1.put("SuperConfused", "^superconfused^");
        smileysMap1.put("SuperInnocent", "^superinnocent^");
        smileysMap1.put("CellPhone", "^cellPhone^");
        smileysMap1.put("Idiot", "^idiot^");
        smileysMap1.put("Knuppel", "^knuppel^");
        smileysMap1.put("TickedOff", "^tickedOff^");
        smileysMap1.put("Peace", "^peace^");
        smileysMap1.put("Suspicious", "^suspicious^");
        smileysMap1.put("Caffine", "^caffine^");
        smileysMap1.put("argue", "^argue^");
        smileysMap1.put("banned2", "^banned2^");
        smileysMap1.put("banned", "^banned^");
        smileysMap1.put("bath", "^bath^");
        smileysMap1.put("beg", "^beg^");
        smileysMap1.put("bluescreen", "^bluescreen^");
        smileysMap1.put("boil", "^boil^");
        smileysMap1.put("bye", "^bye^");
        smileysMap1.put("callmerip", "^callmerip^");
        smileysMap1.put("carnaval", "^carnaval^");
        smileysMap1.put("clap", "^clap^");
        smileysMap1.put("coffepot", "^coffepot^");
        smileysMap1.put("crap", "^crap^");
        smileysMap1.put("curses", "^curses^");
        smileysMap1.put("funny", "^funny^");
        smileysMap1.put("guitar", "^guitar^");
        smileysMap1.put("kissy", "^kissy^");
        smileysMap1.put("band", "^band^");
        smileysMap1.put("ivres", "^ivres^");
        smileysMap1.put("kaloe", "^kaloe^");
        smileysMap1.put("kremala", "^kremala^");
        smileysMap1.put("moon", "^moon^");
        smileysMap1.put("mopping", "^mopping^");
        smileysMap1.put("mountza", "^mountza^");
        smileysMap1.put("pcsleep", "^pcsleep^");
        smileysMap1.put("pinokio", "^pinokio^");
        smileysMap1.put("poke", "^poke^");
        smileysMap1.put("seestars", "^seestars^");
        smileysMap1.put("sfyri", "^sfyri^");
        smileysMap1.put("spam", "^spam^");
        smileysMap1.put("super", "^super^");
        smileysMap1.put("tafos", "^tafos^");
        smileysMap1.put("tomato", "^tomato^");
        smileysMap1.put("ytold", "^ytold^");
        smileysMap1.put("beer", "^beer^");
        smileysMap1.put("ο fritz!!!", "^fritz^");
        smileysMap1.put("o Wade!!!", "^wade^");
        smileysMap1.put("bonjour", "^hat^");
        smileysMap1.put("bonjour2", "^miss^");
        smileysMap1.put("question", "^que^");
        smileysMap1.put("shifty", "^shifty^");
        smileysMap1.put("shy", "^shy^");
        smileysMap1.put("music_listenning", "^music_listen^");
        smileysMap1.put("bag_face", "^bagface^");
        smileysMap1.put("rotation", "^rotate^");
        smileysMap1.put("love", "^love^");
        smileysMap1.put("speech", "^speech^");
        smileysMap1.put("shocked", "^shocked^");
        smileysMap1.put("extremely_shocked", "^ex_shocked^");
        smileysMap1.put("smurf", "^smurf^");
        smileysMap1.put("monster", "^monster^");
        smileysMap1.put("pig", "^pig^");
        smileysMap1.put("lol", "^lol^");

        smileysMap2.put("Police", "^Police^");
        smileysMap2.put("foyska", "^fouska^");
        smileysMap2.put("nista", "^nysta^");
        smileysMap2.put("10_7_3", "^sfinaki^");
        smileysMap2.put("yu", "^yue^");
        smileysMap2.put("a-eatpaper", "^eatpaper^");
        smileysMap2.put("lypi", "^lypi^");
        smileysMap2.put("megashok1wq", "^aytoxeir^");
        smileysMap2.put("victory", "^victory^");
        smileysMap2.put("filarakia", "^filarakia^");
        smileysMap2.put("rofl", "^rolfmao^");
        smileysMap2.put("locked", "^lock^");
        smileysMap2.put("facepalm", "^facepalm^");

        //html stuff on the beginning
        bbMap.put("<link rel=.+\">\n ", "");
        //quotes and code headers
        bbMap.put("\\s*?<div class=\"quoteheader\">(.*?(\\n))*?.*?<\\/div>", "");
        bbMap.put("\\s*?<div class=\"codeheader\">(.*?(\\n))+?.*?<\\/div>", "");
        bbMap.put("\\s*?<div class=\"quote\">(.*?(\\n))+?.*?<\\/div>", "");
        bbMap.put("<br>", "\\\n");
        //Non-breaking space
        bbMap.put("&nbsp;", " ");
        //bold
        bbMap.put("\\s*?<b>([\\S\\s]+?)<\\/b>", "\\[b\\]$1\\[/b\\]");
        //italics
        bbMap.put("\\s*?<i>([\\S\\s]+?)<\\/i>", "\\[i\\]$1\\[/i\\]");
        //underline
        bbMap.put("\\s*?<span style=\"text-decoration: underline;\">([\\S\\s]+?)<\\/span>", "\\[u\\]$1\\[/u\\]");
        //deleted
        bbMap.put("\\s*?<del>([\\S\\s]+?)<\\/del>", "\\[s\\]$1\\[/s\\]");
        //text color
        bbMap.put("\\s*?<span style=\"color: (.+?);\">([\\S\\s]+?)<\\/span>", "\\[color=$1\\]$2\\[/color\\]");
        //glow
        bbMap.put("\\s*?<span style=\"background-color: (.+?);\">([\\S\\s]+?)<\\/span>", "\\[glow=$1,2,300\\]$2\\[/glow\\]");
        //shadow
        bbMap.put("\\s*?<span style=\"text-shadow: (.+?) (.+?)\">([\\S\\s]+?)<\\/span>", "\\[shadow=$1,$2\\]$3\\[/shadow\\]");
        //running text
        bbMap.put("\\s*?<marquee>\n  ([\\S\\s]+?)\n <\\/marquee>", "\\[move\\]$1\\[/move\\]");
        //alignment
        bbMap.put("\\s*?<div align=\"center\">\n ([\\S\\s]+?)\n <\\/div>", "\\[center\\]$1\\[/center\\]");
        bbMap.put("\\s*?<div style=\"text-align: (.+?);\">\n  ([\\S\\s]+?)\n <\\/div>", "\\[$1\\]$2\\[/$1\\]");
        //preformated
        bbMap.put("\\s*?<pre>([\\S\\s]+?)<\\/pre>", "\\[pre\\]$1\\[/pre\\]");
        //horizontal rule
        bbMap.put("\\s*?<hr>", "\\[hr\\]");
        //resize
        bbMap.put("\\s*?<span style=\"font-size: (.+?);(.+?)\">([\\S\\s]+?)<\\/span>", "\\[size=$1\\]$3\\[/size\\]");
        //font
        bbMap.put("\\s*?<span style=\"font-family: (.+?);\">([\\S\\s]+?)<\\/span>", "\\[font=$1\\]$2\\[/font\\]");
        //lists
        bbMap.put("\\s+<li>(.+?)<\\/li>", "\\[li\\]$1\\[/li\\]");
        bbMap.put("\n\\s+<ul style=\"margin-top: 0; margin-bottom: 0;\">([\\S\\s]+?)\n\\s+<\\/ul>",
                "\\[list\\]\n$1\n\\[/list\\]");
        //latex code
        bbMap.put("\\s*?<img src=\".+?eq=([\\S\\s]+?)\" .+?\">", "\\[tex\\]$1\\[/tex\\]");
        //code
        bbMap.put("\\s*?<div class=\"code\">((.*?(\\n))+?.*?)<\\/div>", "\\[code\\]$1\\[/code\\]");
        //teletype
        bbMap.put("\\s*?<tt>([\\S\\s]+?)<\\/tt>", "\\[tt\\]$1\\[/tt\\]");
        //superscript/subscript
        bbMap.put("\\s*?<sub>([\\S\\s]+?)<\\/sub>", "\\[sub\\]$1\\[/sub\\]");
        bbMap.put("\\s*?<sup>([\\S\\s]+?)<\\/sup>", "\\[sup\\]$1\\[/sup\\]");
        //tables
        bbMap.put("\\s*?<td.+?>([\\S\\s]+?)<\\/td>", "\\[td\\]$1\\[/td\\]");
        bbMap.put("<tr>([\\S\\s]+?)\n   <\\/tr>", "\\[tr\\]$1\\[/tr\\]");
        bbMap.put("\\s*?<table style=\"(.+?)\">\n  <tbody>\n   ([\\S\\s]+?)\n  <\\/tbody>\n <\\/table>"
                , "\\[table\\]$2\\[/table\\]");
        //videos
        bbMap.put("\\s*?<div class=\"yt\">.+?watch\\?v=(.+?)\"((.|\\n)*?)/div>\n",
                "[youtube]https://www.youtube.com/watch?v=$1[/youtube]");
        //ftp
        bbMap.put("<a href=\"ftp:(.+?)\" .+?>([\\S\\s]+?)<\\/a>", "\\[fpt=ftp:$1\\]$2\\[/ftp\\]");
        //mailto
        bbMap.put("\\s*?<a href=\"mailto:(.+?)\">([\\S\\s]+?)<\\/a>", "\\[email\\]$2\\[/email\\]");
        //links
        bbMap.put("\\s*?<a href=\"(.+?)\" .+?>([\\S\\s]+?)</a>", "\\[url=$1\\]$2\\[/url\\]");
        //smileys
        for (Map.Entry entry : smileysMap1.entrySet()) {
            bbMap.put("\n <img src=\"(.+?)//www.thmmy.gr/smf/Smileys/default_dither/(.+?) alt=\""
                    + entry.getKey().toString() + "\" .+?\">", entry.getValue().toString());
        }
        for (Map.Entry entry : smileysMap2.entrySet()) { //Those that have empty alt tag
            bbMap.put("\n <img src=\"(.+?)//www.thmmy.gr/smf/Smileys/default_dither/"
                    + entry.getKey().toString() + ".gif\" .+?\">", entry.getValue().toString());
        }

        bbMap.put("\n <img src=\"(.+?)//www.thmmy.gr/smf/Smileys/default_dither/undecided.gif\" alt=\"Undecided\" border=\"0\">"
                , Matcher.quoteReplacement(":-\\"));

        //html stuff on the end
        bbMap.put("\n</div>", "");

        for (Map.Entry entry : bbMap.entrySet()) {
            html = html.replaceAll(entry.getKey().toString(), entry.getValue().toString());
        }

        //img need to be done last or it messes up everything else
        html = html.replaceAll("\\s+<img src=\"(.+?)\" .+? width=\"(.+?)\" .+? height=\"(.+?)\" .+?>",
                "\\[img width=$2 height=$3\\]$1\\[/img\\]");
        html = html.replaceAll("\\s+<img src=\"(.+?)\" .+? height=\"(.+?)\" .+? width=\"(.+?)\" .+?>",
                "\\[img height=$2 width=$3\\]$1\\[/img\\]");
        html = html.replaceAll("\\s+<img src=\"(.+?)\" .+? width=\"(.+?)\" .+?>", "\\[img width=$2\\]$1\\[/img\\]");
        html = html.replaceAll("\\s+<img src=\"(.+?)\" .+? height=\"(.+?)\" .+?>", "\\[img height=$2\\]$1\\[/img\\]");
        html = html.replaceAll("\\s+<img src=\"(.+?)\".+?>", "\\[img\\]$1\\[/img\\]");

        Log.d("Cancer", html);
        return html;
    }
}
