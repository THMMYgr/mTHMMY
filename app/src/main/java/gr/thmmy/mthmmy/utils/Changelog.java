package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.PreferenceManager;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import gr.thmmy.mthmmy.BuildConfig;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.main.MainActivity;
import timber.log.Timber;

public class Changelog {
    public enum LAUNCH_TYPE {
        FIRST_LAUNCH_EVER, FIRST_LAUNCH_AFTER_UPDATE, NORMAL_LAUNCH
    }

    private static final String PREF_VERSION_CODE_KEY = "VERSION_CODE";

    @Nullable
    public static LAUNCH_TYPE getLaunchType(Context context) {
        final int notThere = -1;

        //Gets current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;
        //Gets saved version code
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, notThere);

        //Checks for first run or upgrade
        if (currentVersionCode == savedVersionCode) {
            //This is just a normal run
            return LAUNCH_TYPE.NORMAL_LAUNCH;
        } else if (savedVersionCode == notThere) {
            //Updates the shared preferences with the current version code
            prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
            return LAUNCH_TYPE.FIRST_LAUNCH_EVER;
        } else if (currentVersionCode > savedVersionCode) {
            //Updates the shared preferences with the current version code
            prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
            return LAUNCH_TYPE.FIRST_LAUNCH_AFTER_UPDATE;
        }
        //Manually changed the shared prefs?? Omg
        return null;
    }

    public static AlertDialog getChangelogDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = ((MainActivity) context).getLayoutInflater();
        LinearLayout changelogView = (LinearLayout) inflater.inflate(R.layout.dialog_changelog, null);

        builder.setNegativeButton(R.string.dialog_not_interested_button, (dialog, which) -> dialog.dismiss());

        try {
            JSONObject jsonObject = new JSONObject(loadJSONFromAssets(context));
            JSONArray jsonArray = jsonObject.getJSONArray("changelogs");

            boolean currentVersionFound = false;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObjectInner = jsonArray.getJSONObject(i);
                int versionCode = jsonObjectInner.getInt("version_code");

                if (versionCode == BuildConfig.VERSION_CODE) {
                    TextView version = changelogView.findViewById(R.id.version);
                    version.setText(context.getResources().getString(R.string.changelog_version_text,
                            jsonObjectInner.getString("version")));

                    TextView releaseDate = changelogView.findViewById(R.id.release_date);
                    releaseDate.setText(jsonObjectInner.getString("release_date"));

                    LinearLayout changeListView = changelogView.findViewById(R.id.changelog_content);

                    JSONArray changeList = jsonObjectInner.getJSONArray("change_list");
                    for (int changeIndex = 0; changeIndex < changeList.length(); ++changeIndex) {
                        TextView changeText = new TextView(changeListView.getContext());
                        changeText.setTextColor(context.getResources().getColor(R.color.primary_text));
                        LinearLayout.LayoutParams params = new LinearLayout.
                                LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT);
                        params.setMargins(0, 5, 0, 5);
                        changeText.setLayoutParams(params);

                        JSONObject change = changeList.getJSONObject(changeIndex);
                        String changeType = change.getString("change"),
                                changeTitle = change.getString("title"),
                                changeDescription = change.getString("description");
                        int changeColor;

                        switch (changeType) {
                            case "feature":
                                changeColor = R.color.changelog_feature_dot;
                                break;
                            case "bug":
                                changeColor = R.color.changelog_bug_dot;
                                break;
                            case "improvement":
                                changeColor = R.color.changelog_improvement_dot;
                                break;
                            default:
                                changeColor = R.color.changelog_default_dot;
                                break;
                        }

                        SpannableStringBuilder spannable = new SpannableStringBuilder("• " +
                                changeTitle + " " + changeDescription);
                        spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD),
                                0, changeTitle.length() + 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannable.setSpan(new ForegroundColorSpan(context.getResources().getColor(changeColor)),
                                0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);


                        changeText.setText(spannable);
                        changeListView.addView(changeText);
                    }
                    currentVersionFound = true;
                    break;
                }
            }
            if(!currentVersionFound)
                return null;
        } catch (JSONException exception) {
            Timber.e(exception, "Couldn't read changelog json from assets");
        }

        builder.setView(changelogView);
        return builder.create();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static String loadJSONFromAssets(Context context) {
        String json;
        try {
            InputStream is = context.getAssets().open("changelog.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }
}