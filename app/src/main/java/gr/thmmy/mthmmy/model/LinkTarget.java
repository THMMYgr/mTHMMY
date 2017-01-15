package gr.thmmy.mthmmy.model;

import android.net.Uri;

import java.util.Objects;

import mthmmy.utils.Report;

public class LinkTarget {
    private static final String TAG = "LinkTarget";

    public enum Target {
        NOT_THMMY, UNKNOWN_THMMY, TOPIC, BOARD, UNREAD_POSTS, PROFILE_SUMMARY,
        PROFILE_LATEST_POSTS, PROFILE_STATS, PROFILE
    }

    public static boolean isThmmy(Uri uri) {
        return resolveLinkTarget(uri) != Target.NOT_THMMY;
    }

    public static Target resolveLinkTarget(Uri uri) {
        final String host = uri.getHost();
        final String uriString = uri.toString();

        //Checks if app can handle this url
        if (Objects.equals(host, "www.thmmy.gr")) {
            if (uriString.contains("topic=")) return Target.TOPIC;
            else if (uriString.contains("board=")) return Target.BOARD;
            else if (uriString.contains("action=profile")) {
                if (uriString.contains(";sa=showPosts"))
                    return Target.PROFILE_LATEST_POSTS;
                else if (uriString.contains(";sa=statPanel"))
                    return Target.PROFILE_STATS;
                else return Target.PROFILE_SUMMARY;
            } else if (uriString.contains("action=unread"))
                return Target.UNREAD_POSTS;
            Report.v(TAG, "Unknown thmmy link found, link: " + uriString);
            return Target.UNKNOWN_THMMY;
        }
        return Target.NOT_THMMY;
    }

    public static boolean targetEqual(Target first, Target second) {
        return first == Target.PROFILE &&
                (second == Target.PROFILE_LATEST_POSTS ||
                        second == Target.PROFILE_STATS ||
                        second == Target.PROFILE_SUMMARY)
                || first == second;
    }
}
