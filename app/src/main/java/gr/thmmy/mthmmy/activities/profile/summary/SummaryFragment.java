package gr.thmmy.mthmmy.activities.profile.summary;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import gr.thmmy.mthmmy.views.ReactiveWebView;
import timber.log.Timber;

/**
 * Use the {@link SummaryFragment#newInstance} factory method to create an instance of this fragment.
 */
public class SummaryFragment extends Fragment {
    /**
     * The key to use when putting profile's source code String to {@link SummaryFragment}'s Bundle.
     */
    private static final String PROFILE_DOCUMENT = "PROFILE_DOCUMENT";
    /**
     * {@link ArrayList} of Strings used to hold profile's information. Data are added in
     * {@link SummaryTask}.
     */
    private ArrayList<String> parsedProfileSummaryData;
    /**
     * A {@link Document} holding this profile's source code.
     */
    private Document profileSummaryDocument;
    private SummaryTask summaryTask;
    private LinearLayout mainContent;

    public SummaryFragment() {
        // Required empty public constructor
    }

    /**
     * Use ONLY this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @param profileSummaryDocument a {@link Document} containing this profile's parsed page
     * @return A new instance of fragment Summary.
     */
    public static SummaryFragment newInstance(Document profileSummaryDocument) {
        SummaryFragment fragment = new SummaryFragment();
        Bundle args = new Bundle();
        args.putString(PROFILE_DOCUMENT, profileSummaryDocument.toString());
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileSummaryDocument = Jsoup.parse(getArguments().getString(PROFILE_DOCUMENT));
        parsedProfileSummaryData = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_profile_summary, container, false);
        mainContent = rootView.findViewById(R.id.profile_activity_content);
        if (!parsedProfileSummaryData.isEmpty() && isAdded())
            populateLayout();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (parsedProfileSummaryData.isEmpty()) {
            summaryTask = new SummaryTask();
            summaryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, profileSummaryDocument);
        }
        Timber.d("onActivityCreated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (summaryTask != null && summaryTask.getStatus() != AsyncTask.Status.RUNNING)
            summaryTask.cancel(true);
    }

    /**
     * An {@link AsyncTask} that handles asynchronous parsing of a profile page's data.
     * {@link AsyncTask#onPostExecute(Object) OnPostExecute} method calls {@link #populateLayout()}
     * to build graphics.
     * <p>
     * <p>Calling SummaryTask's {@link AsyncTask#execute execute} method needs to have profile's url
     * as String parameter!</p>
     */
    private class SummaryTask extends AsyncTask<Document, Void, Void> {
        protected Void doInBackground(Document... profileSummaryPage) {
            parsedProfileSummaryData = parseProfileSummary(profileSummaryPage[0]);
            return null;
        }

        protected void onPostExecute(Void result) {
            if (isAdded()) populateLayout();
        }

        /**
         * This method is used to parse all available information in a user profile.
         *
         * @param profile {@link Document} object containing this profile's source code
         * @return ArrayList containing this profile's parsed information
         * @see org.jsoup.Jsoup Jsoup
         */
        ArrayList<String> parseProfileSummary(Document profile) {
            //Method's variables
            ArrayList<String> parsedInformation = new ArrayList<>();

            //Contains all summary's rows
            Elements summaryRows = profile.select(".bordercolor > tbody:nth-child(1) > tr:nth-child(2) tr");

            for (Element summaryRow : summaryRows) {
                String rowText = summaryRow.text(), pHtml = "";

                if (summaryRow.select("td").size() == 1) //Horizontal rule rows
                    pHtml = "";
                else if (summaryRow.text().contains("Current Status")
                        || summaryRow.text().contains("Κατάσταση")) continue;
                else if (rowText.contains("Signature") || rowText.contains("Υπογραφή")) {
                    //This needs special handling since it may have css
                    pHtml = ParseHelpers.emojiTagToHtml(ParseHelpers.youtubeEmbeddedFix(summaryRow));
                    //Add stuff to make it work in WebView
                    //style.css
                    pHtml = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />\n" +
                            "<div class=\"customSignature\">\n" + pHtml + "\n</div>");
                } else if (!rowText.contains("Name") && !rowText.contains("Όνομα")) { //Doesn't add username twice
                    if (Objects.equals(summaryRow.select("td").get(1).text(), ""))
                        continue;
                    //Style parsed information with html
                    pHtml = "<b>" + summaryRow.select("td").first().text() + "</b> "
                            + summaryRow.select("td").get(1).text();
                }
                parsedInformation.add(pHtml);
            }
            return parsedInformation;
        }
    }

    /**
     * Simple method that builds the UI of a {@link SummaryFragment}.
     * <p>Use this method <b>only after</b> parsing profile's data with
     * {@link gr.thmmy.mthmmy.activities.profile.ProfileActivity.ProfileTask} as it reads from
     * {@link #parsedProfileSummaryData}</p>
     */
    private void populateLayout() {
        for (String profileSummaryRow : parsedProfileSummaryData) {
            if (profileSummaryRow.contains("Signature")
                    || profileSummaryRow.contains("Υπογραφή")) { //This may contain css
                ReactiveWebView signatureEntry = new ReactiveWebView(this.getContext());
                signatureEntry.setBackgroundColor(Color.argb(1, 255, 255, 255));
                signatureEntry.loadDataWithBaseURL("file:///android_asset/", profileSummaryRow,
                        "text/html", "UTF-8", null);
                mainContent.addView(signatureEntry);
                continue;
            }
            TextView entry = new TextView(this.getContext());

            if (profileSummaryRow.contains("@") &&
                    (profileSummaryRow.contains("Email") || profileSummaryRow.contains("E-mail"))) {
                String email = profileSummaryRow.substring(profileSummaryRow.indexOf(":</b> ") + 6);
                profileSummaryRow = profileSummaryRow.replace(email,
                        "<a href=\"mailto:" + email + "\">" + email + "</a>");
                entry.setMovementMethod(LinkMovementMethod.getInstance());
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                entry.setTextColor(getResources().getColor(R.color.primary_text, null));
            else
                //noinspection deprecation
                entry.setTextColor(getResources().getColor(R.color.primary_text));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                entry.setText(Html.fromHtml(profileSummaryRow, Html.FROM_HTML_MODE_LEGACY));
            } else {
                //noinspection deprecation
                entry.setText(Html.fromHtml(profileSummaryRow));
            }

            mainContent.addView(entry);
        }
    }
}
