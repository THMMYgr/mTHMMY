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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.utils.parsing.ParseHelpers;
import gr.thmmy.mthmmy.views.ReactiveWebView;

/**
 * Use the {@link SummaryFragment#newInstance} factory method to create an instance of this fragment.
 */
public class SummaryFragment extends Fragment {
    /**
     * The key to use when putting profile's source code String to {@link SummaryFragment}'s Bundle.
     */
    private static final String PROFILE_DOCUMENT = "PROFILE_DOCUMENT";
    /**
     * {@link HashMap} used to hold profile's information. Data are added in
     * {@link SummaryTask}.
     */
    private LinkedHashMap<String, String> parsedProfileSummaryData;
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
        profileSummaryDocument = Jsoup.parse(Objects.requireNonNull(requireArguments().getString(PROFILE_DOCUMENT)));
        parsedProfileSummaryData = new LinkedHashMap<>();
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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (parsedProfileSummaryData.isEmpty()) {
            summaryTask = new SummaryTask();
            summaryTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, profileSummaryDocument);
        }
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
        LinkedHashMap<String, String> parseProfileSummary(Document profile) {
            LinkedHashMap<String, String> parsedInformation = new LinkedHashMap<>();

            //Contains all summary's rows
            Elements summaryRows = profile.select("td.windowbg > table > tbody > tr");

            for (Element summaryRow : summaryRows) {
                String key, value;

                int tdSize = summaryRow.select("td").size();

                if (tdSize > 1) {
                    key = summaryRow.select("td").first().text().trim();

                    if (key.startsWith("Name") || key.startsWith("Όνομα"))
                        continue;
                    else if (key.startsWith("Signature") || key.startsWith("Υπογραφή")) {
                        key = summaryRow.selectFirst("td > table > tbody > tr > td").text().trim();
                        summaryRow.selectFirst("td > table > tbody > tr").remove(); //key not needed, outer html needed for CSS
                        value = ParseHelpers.emojiTagToHtml(ParseHelpers.youtubeEmbeddedFix(summaryRow));   // Is emojiTagToHtml() really needed here?
                        if (summaryRow.text().trim().isEmpty())
                            continue;
                        value = ("<link rel=\"stylesheet\" type=\"text/css\" href=\"style.css\" />\n" +
                                "<link rel=\"stylesheet\" type=\"text/css\" href=\"style_light.css\" />\n" +
                                "<div class=\"customSignature\">\n" + value + "\n</div>");
                    }
                    else {
                        if (summaryRow.select("td").get(1).text().isEmpty())
                            continue;
                        if (key.startsWith("Date Registered") || key.startsWith("Ημερομηνία εγγραφής") || key.startsWith("Last Active") || key.startsWith("Τελευταία σύνδεση"))
                            value = summaryRow.select("td").get(1).text().trim();
                        else
                            value = summaryRow.select("td").get(1).html().trim();
                    }
                    parsedInformation.put(key, value);
                }
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
        for (LinkedHashMap.Entry<String, String> entry : parsedProfileSummaryData.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            if (key.startsWith("Current Status") || key.startsWith("Κατάσταση")) {
                addEmptyView();
                continue;
            }

            TextView textView = new TextView(this.getContext());

            if (((key.startsWith("Email") || key.startsWith("E-mail"))
                    && value.contains("@")) || key.startsWith("Website") || key.startsWith("Ιστοτόπος"))
                textView.setMovementMethod(LinkMovementMethod.getInstance());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                textView.setTextColor(getResources().getColor(R.color.primary_text, null));
            else
                textView.setTextColor(getResources().getColor(R.color.primary_text));

            String textViewContent = "<b>" + key + "</b> " + value;

            if (key.startsWith("Signature") || key.startsWith("Υπογραφή")) {
                addEmptyView();
                textViewContent = "<b>" + key + "</b>";
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                textView.setText(Html.fromHtml(textViewContent, Html.FROM_HTML_MODE_LEGACY));
            else
                textView.setText(Html.fromHtml(textViewContent));

            mainContent.addView(textView);

            if (key.startsWith("Last Active") || key.startsWith("Τελευταία σύνδεση"))
                addEmptyView();

            if (key.startsWith("Signature") || key.startsWith("Υπογραφή")) {
                ReactiveWebView signatureEntry = new ReactiveWebView(this.getContext());
                signatureEntry.setBackgroundColor(Color.argb(1, 255, 255, 255));
                signatureEntry.loadDataWithBaseURL("file:///android_asset/", value,
                        "text/html", "UTF-8", null);
                mainContent.addView(signatureEntry);
            }
        }
    }

    private void addEmptyView() {
        mainContent.addView(new TextView(this.getContext()));
    }
}
