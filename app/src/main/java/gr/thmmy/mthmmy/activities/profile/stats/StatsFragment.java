package gr.thmmy.mthmmy.activities.profile.stats;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import okhttp3.Request;
import okhttp3.Response;
import timber.log.Timber;

public class StatsFragment extends Fragment {
    /**
     * The key to use when putting profile's url String to {@link StatsFragment}'s Bundle.
     */
    private static final String PROFILE_URL = "PROFILE_DOCUMENT";
    private String profileUrl;
    private ProfileStatsTask profileStatsTask;
    private LinearLayout mainContent;
    private MaterialProgressBar progressBar;

    private boolean userHasPosts = true;
    private String generalStatisticsTitle = "", generalStatistics = "", postingActivityByTimeTitle = "", mostPopularBoardsByPostsTitle = "", mostPopularBoardsByActivityTitle = "";
    private final List<Entry> postingActivityByTime = new ArrayList<>();
    private final List<BarEntry> mostPopularBoardsByPosts = new ArrayList<>(), mostPopularBoardsByActivity = new ArrayList<>();
    private final ArrayList<String> mostPopularBoardsByPostsLabels = new ArrayList<>(), mostPopularBoardsByActivityLabels = new ArrayList<>();

    public StatsFragment() {
        // Required empty public constructor
    }

    /**
     * Use ONLY this factory method to create a new instance of this fragment using the provided
     * parameters.
     *
     * @param profileUrl String containing this profile's url
     * @return A new instance of fragment Stats.
     */
    public static StatsFragment newInstance(String profileUrl) {
        StatsFragment fragment = new StatsFragment();
        Bundle args = new Bundle();
        args.putString(PROFILE_URL, profileUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileUrl = getArguments().getString(PROFILE_URL);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_profile_stats, container, false);
        mainContent = rootView.findViewById(R.id.main_content);
        progressBar = rootView.findViewById(R.id.progressBar);
        if (profileStatsTask!=null && profileStatsTask.getStatus() == AsyncTask.Status.FINISHED)
            populateLayout();
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (profileStatsTask==null) {
            profileStatsTask = new ProfileStatsTask();
            profileStatsTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, profileUrl + ";sa=statPanel");
        }
        Timber.d("onActivityCreated");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (profileStatsTask != null && profileStatsTask.getStatus() != AsyncTask.Status.RUNNING)
            profileStatsTask.cancel(true);
    }

    /**
     * An {@link AsyncTask} that handles asynchronous parsing of a profile page's data.
     * {@link AsyncTask#onPostExecute(Object) OnPostExecute} method calls {@link #()}
     * to build graphics.
     * <p>
     * <p>Calling SummaryTask's {@link AsyncTask#execute execute} method needs to have profile's url
     * as String parameter!</p>
     */
    private class ProfileStatsTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(String... profileUrl) {
            Request request = new Request.Builder()
                    .url(profileUrl[0])
                    .build();
            try {
                Response response = BaseActivity.getClient().newCall(request).execute();
                return parseStats(Jsoup.parse(response.body().string()));
            } catch (SSLHandshakeException e) {
                Timber.w("Certificate problem (please switch to unsafe connection).");
            } catch (Exception e) {
                Timber.e(e, "Exception");
            }
            return false;
        }

        //TODO: better parse error handling (ParseException etc.)
        @Override
        protected void onPostExecute(Boolean result) {
            if (!result) { //Parse failed!
                Timber.d("Parse failed!");
                Toast.makeText(getContext()
                        , "Fatal error!\n Aborting...", Toast.LENGTH_LONG).show();
                getActivity().finish();
            }
            //Parse was successful
            populateLayout();
        }

        private boolean parseStats(Document statsPage) {
            //Doesn't go through all the parsing if this user has no posts
            if (!statsPage.select("td:contains(No posts to speak of!)").isEmpty()) {
                userHasPosts = false;
            }
            if (!statsPage.select("td:contains(Δεν υπάρχει καμία αποστολή μηνύματος!)").isEmpty()) {
                userHasPosts = false;
            }
            if (statsPage.select("table.bordercolor[align]>tbody>tr").size() != 6)
                return false;
            {
                Elements titleRows = statsPage.select("table.bordercolor[align]>tbody>tr.titlebg");
                generalStatisticsTitle = titleRows.first().text();
                Pattern pattern = Pattern.compile("(.+)\\s-");
                Matcher matcher = pattern.matcher(generalStatisticsTitle);
                if (matcher.find())
                    generalStatisticsTitle = matcher.group(1);

                if (userHasPosts) {
                    postingActivityByTimeTitle = titleRows.get(1).text();
                    mostPopularBoardsByPostsTitle = titleRows.last().select("td").first().text();
                    mostPopularBoardsByActivityTitle = titleRows.last().select("td").last().text();
                }
            }
            {
                Elements statsRows = statsPage.select("table.bordercolor[align]>tbody>tr:not(.titlebg)");
                {
                    Elements generalStatisticsRows = statsRows.first().select("tbody>tr");
                    for (Element generalStatisticsRow : generalStatisticsRows)
                        generalStatistics += generalStatisticsRow.text() + "\n";
                    generalStatistics = generalStatistics.trim();
                }
                if (userHasPosts) {
                    {
                        Elements postingActivityByTimeCols = statsRows.get(1).select(">td").last()
                                .select("tr").first().select("td[width=4%]");
                        int i = -1;
                        for (Element postingActivityByTimeColumn : postingActivityByTimeCols) {
                            postingActivityByTime.add(new Entry(++i, Float.parseFloat(postingActivityByTimeColumn
                                    .select("img").first().attr("height"))));
                        }
                    }
                    {
                        Elements mostPopularBoardsByPostsRows = statsRows.last().select(">td").get(1)
                                .select(">table>tbody>tr");
                        int i = mostPopularBoardsByPostsRows.size();
                        for (Element mostPopularBoardsByPostsRow : mostPopularBoardsByPostsRows) {
                            Elements dataCols = mostPopularBoardsByPostsRow.select("td");
                            mostPopularBoardsByPosts.add(new BarEntry(--i,
                                    Integer.parseInt(dataCols.last().text())));
                            mostPopularBoardsByPostsLabels.add(dataCols.first().text());
                        }
                        Collections.reverse(mostPopularBoardsByPostsLabels);
                    }
                    {
                        Elements mostPopularBoardsByActivityRows = statsRows.last().select(">td").last()
                                .select(">table>tbody>tr");
                        int i = mostPopularBoardsByActivityRows.size();
                        for (Element mostPopularBoardsByActivityRow : mostPopularBoardsByActivityRows) {
                            Elements dataCols = mostPopularBoardsByActivityRow.select("td");
                            String tmp = dataCols.last().text();
                            mostPopularBoardsByActivity.add(new BarEntry(--i,
                                    Float.parseFloat(tmp.substring(0, tmp.indexOf("%")))));
                            mostPopularBoardsByActivityLabels.add(dataCols.first().text());
                        }
                        Collections.reverse(mostPopularBoardsByActivityLabels);
                    }
                }
            }
            return true;
        }
    }

    private void populateLayout() {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        ((TextView) mainContent.findViewById(R.id.general_statistics_title))
                .setText(generalStatisticsTitle);
        ((TextView) mainContent.findViewById(R.id.general_statistics))
                .setText(generalStatistics);

        if (!userHasPosts) {
            mainContent.removeViews(2, mainContent.getChildCount() - 2);
            //mainContent.removeViews(2, 6);
            return;
        }

        ((TextView) mainContent.findViewById(R.id.posting_activity_by_time_title))
                .setText(postingActivityByTimeTitle);

        LineChart postingActivityByTimeChart = mainContent
                .findViewById(R.id.posting_activity_by_time_chart);
        postingActivityByTimeChart.setDescription(null);
        postingActivityByTimeChart.getLegend().setEnabled(false);
        postingActivityByTimeChart.setScaleYEnabled(false);
        postingActivityByTimeChart.setDrawBorders(true);
        postingActivityByTimeChart.getAxisLeft().setEnabled(false);
        postingActivityByTimeChart.getAxisRight().setEnabled(false);
        XAxis postingActivityByTimeChartXAxis = postingActivityByTimeChart.getXAxis();
        postingActivityByTimeChartXAxis.setTextColor(Color.WHITE);
        postingActivityByTimeChartXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        postingActivityByTimeChartXAxis.setDrawLabels(true);
        postingActivityByTimeChartXAxis.setLabelCount(24, false);
        postingActivityByTimeChartXAxis.setGranularity(1f);

        LineDataSet postingActivityByTimeDataSet = new LineDataSet(postingActivityByTime, null);
        if (isAdded()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                postingActivityByTimeDataSet.setFillDrawable(getResources().getDrawable(R.drawable.line_chart_gradient, null));
            } else
                //noinspection deprecation
                postingActivityByTimeDataSet.setFillDrawable(getResources().getDrawable(R.drawable.line_chart_gradient));
        }
        postingActivityByTimeDataSet.setDrawFilled(true);
        postingActivityByTimeDataSet.setDrawCircles(false);
        postingActivityByTimeDataSet.setDrawValues(false);
        LineData postingActivityByTimeData = new LineData(postingActivityByTimeDataSet);
        postingActivityByTimeChart.setData(postingActivityByTimeData);
        postingActivityByTimeChart.invalidate();

        ((TextView) mainContent.findViewById(R.id.most_popular_boards_by_posts_title))
                .setText(mostPopularBoardsByPostsTitle);

        HorizontalBarChart mostPopularBoardsByPostsChart = mainContent.
                findViewById(R.id.most_popular_boards_by_posts_chart);
        mostPopularBoardsByPostsChart.setDescription(null);
        mostPopularBoardsByPostsChart.getLegend().setEnabled(false);
        mostPopularBoardsByPostsChart.setScaleEnabled(false);
        mostPopularBoardsByPostsChart.setDrawBorders(true);
        mostPopularBoardsByPostsChart.getAxisLeft().setEnabled(false);

        XAxis mostPopularBoardsByPostsChartXAxis = mostPopularBoardsByPostsChart.getXAxis();
        mostPopularBoardsByPostsChartXAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        mostPopularBoardsByPostsChartXAxis.setTextColor(Color.WHITE);
        mostPopularBoardsByPostsChartXAxis.setLabelCount(mostPopularBoardsByPostsLabels.size());
        mostPopularBoardsByPostsChartXAxis.setValueFormatter(new MyXAxisValueFormatter(mostPopularBoardsByPostsLabels));

        YAxis mostPopularBoardsByPostsChartYAxis = mostPopularBoardsByPostsChart.getAxisRight();
        mostPopularBoardsByPostsChartYAxis.setTextColor(Color.WHITE);
        mostPopularBoardsByPostsChartYAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        mostPopularBoardsByPostsChartYAxis.setDrawLabels(true);
        mostPopularBoardsByPostsChartYAxis.setLabelCount(10, false);
        mostPopularBoardsByPostsChartYAxis.setGranularity(1f);

        BarDataSet mostPopularBoardsByPostsDataSet = new BarDataSet(mostPopularBoardsByPosts, null);
        if (isAdded()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mostPopularBoardsByPostsDataSet.setColors(getResources().getColor(R.color.accent, null));
            } else
                //noinspection deprecation
                mostPopularBoardsByPostsDataSet.setColors(getResources().getColor(R.color.accent));
        }
        mostPopularBoardsByPostsDataSet.setDrawValues(false);
        mostPopularBoardsByPostsDataSet.setValueTextColor(Color.WHITE);

        BarData mostPopularBoardsByPostsData = new BarData(mostPopularBoardsByPostsDataSet);
        mostPopularBoardsByPostsData.setDrawValues(false);
        mostPopularBoardsByPostsData.setValueTextColor(Color.WHITE);
        mostPopularBoardsByPostsChart.setData(mostPopularBoardsByPostsData);
        mostPopularBoardsByPostsChart.invalidate();

        ((TextView) mainContent.findViewById(R.id.most_popular_boards_by_activity_title))
                .setText(mostPopularBoardsByActivityTitle);

        HorizontalBarChart mostPopularBoardsByActivityChart = mainContent.
                findViewById(R.id.most_popular_boards_by_activity_chart);
        mostPopularBoardsByActivityChart.setDescription(null);
        mostPopularBoardsByActivityChart.getLegend().setEnabled(false);
        mostPopularBoardsByActivityChart.setScaleEnabled(false);
        mostPopularBoardsByActivityChart.setDrawBorders(true);
        mostPopularBoardsByActivityChart.getAxisLeft().setEnabled(false);

        XAxis mostPopularBoardsByActivityChartXAxis = mostPopularBoardsByActivityChart.getXAxis();
        mostPopularBoardsByActivityChartXAxis.setPosition(XAxis.XAxisPosition.TOP_INSIDE);
        mostPopularBoardsByActivityChartXAxis.setTextColor(Color.WHITE);
        mostPopularBoardsByActivityChartXAxis.setLabelCount(mostPopularBoardsByActivity.size());
        mostPopularBoardsByActivityChartXAxis.setValueFormatter(new MyXAxisValueFormatter(mostPopularBoardsByActivityLabels));

        YAxis mostPopularBoardsByActivityChartYAxis = mostPopularBoardsByActivityChart.getAxisRight();
        mostPopularBoardsByActivityChartYAxis.setValueFormatter(new PercentFormatter());
        mostPopularBoardsByActivityChartYAxis.setTextColor(Color.WHITE);
        mostPopularBoardsByActivityChartYAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        mostPopularBoardsByActivityChartYAxis.setDrawLabels(true);
        mostPopularBoardsByActivityChartYAxis.setLabelCount(10, false);

        BarDataSet mostPopularBoardsByActivityDataSet = new BarDataSet(mostPopularBoardsByActivity, null);
        if (isAdded()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mostPopularBoardsByActivityDataSet.setColors(getResources().getColor(R.color.accent, null));
            } else
                //noinspection deprecation
                mostPopularBoardsByActivityDataSet.setColors(getResources().getColor(R.color.accent));
        }
        mostPopularBoardsByActivityDataSet.setDrawValues(false);
        mostPopularBoardsByActivityDataSet.setValueTextColor(Color.WHITE);

        BarData mostPopularBoardsByActivityData = new BarData(mostPopularBoardsByActivityDataSet);
        mostPopularBoardsByActivityData.setDrawValues(false);
        mostPopularBoardsByActivityData.setValueTextColor(Color.WHITE);
        mostPopularBoardsByActivityChart.setData(mostPopularBoardsByActivityData);
        mostPopularBoardsByActivityChart.invalidate();
        progressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    private class MyXAxisValueFormatter implements IAxisValueFormatter {
        private final ArrayList<String> mValues;

        MyXAxisValueFormatter(ArrayList<String> values) {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis) {
            return mValues.get((int) value);
        }
    }
}
