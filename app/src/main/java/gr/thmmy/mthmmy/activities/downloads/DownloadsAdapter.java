package gr.thmmy.mthmmy.activities.downloads;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.model.Download;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_TITLE;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_URL;

class DownloadsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    /**
     * Debug Tag for logging debug output to LogCat
     */
    @SuppressWarnings("unused")
    private static final String TAG = "DownloadsAdapter";
    private final int VIEW_TYPE_DOWNLOAD = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private final Context context;
    private ArrayList<Download> parsedDownloads = new ArrayList<>();
    private final ArrayList<Boolean> downloadExpandableVisibility = new ArrayList<>();

    DownloadsAdapter(Context context, ArrayList<Download> parsedDownloads) {
        this.context = context;
        this.parsedDownloads = parsedDownloads;
    }

    interface OnLoadMoreListener {
        void onLoadMore();
    }

    @Override
    public int getItemViewType(int position) {
        return (parsedDownloads.get(position) == null) ? VIEW_TYPE_LOADING : VIEW_TYPE_DOWNLOAD;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DOWNLOAD) {
            View download = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_downloads_row, parent, false);
            return new DownloadViewHolder(download);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View loading = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recycler_loading_item, parent, false);
            return new LoadingViewHolder(loading);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof DownloadViewHolder) {
            final Download download = parsedDownloads.get(position);
            final DownloadViewHolder downloadViewHolder = (DownloadViewHolder) holder;

            if (downloadExpandableVisibility.size() != parsedDownloads.size()) {
                for (int i = downloadExpandableVisibility.size(); i < parsedDownloads.size(); ++i)
                    downloadExpandableVisibility.add(false);
            }

            if (download.getType() == Download.DownloadItemType.DOWNLOADS_CATEGORY) {
                downloadViewHolder.downloadRow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(context, DownloadsActivity.class);
                        Bundle extras = new Bundle();
                        extras.putString(BUNDLE_DOWNLOADS_URL, download.getUrl());
                        extras.putString(BUNDLE_DOWNLOADS_TITLE, download.getTitle());
                        intent.putExtras(extras);
                        intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                });

                if (downloadExpandableVisibility.get(downloadViewHolder.getAdapterPosition())) {
                    downloadViewHolder.informationExpandable.setVisibility(View.VISIBLE);
                    downloadViewHolder.informationExpandableBtn.setImageResource(R.drawable.ic_arrow_drop_up);
                } else {
                    downloadViewHolder.informationExpandable.setVisibility(View.GONE);
                    downloadViewHolder.informationExpandableBtn.setImageResource(R.drawable.ic_arrow_drop_down);
                }
                downloadViewHolder.informationExpandableBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final boolean visible = downloadExpandableVisibility.get(downloadViewHolder.
                                getAdapterPosition());
                        if (visible) {
                            downloadViewHolder.informationExpandable.setVisibility(View.GONE);
                            downloadViewHolder.informationExpandableBtn.setImageResource(R.drawable.ic_arrow_drop_down);
                        } else {
                            downloadViewHolder.informationExpandable.setVisibility(View.VISIBLE);
                            downloadViewHolder.informationExpandableBtn.setImageResource(R.drawable.ic_arrow_drop_up);
                        }
                        downloadExpandableVisibility.set(downloadViewHolder.getAdapterPosition(), !visible);
                    }
                });
                downloadViewHolder.title.setTypeface(Typeface.createFromAsset(context.getAssets()
                        , "fonts/fontawesome-webfont.ttf"));
                if (download.hasSubCategory()) {
                    String tmp = context.getResources().getString(R.string.fa_folder) + " "
                            + download.getTitle();
                    downloadViewHolder.title.setText(tmp);
                } else {
                    String tmp = context.getResources().getString(R.string.fa_file) + " "
                            + download.getTitle();
                    downloadViewHolder.title.setText(tmp);
                }
            } else {
                //TODO implement download on click

                downloadViewHolder.upperLinear.setBackgroundColor(context.getResources().getColor(R.color.background));
                downloadViewHolder.informationExpandable.setVisibility(View.VISIBLE);
                downloadViewHolder.informationExpandableBtn.setVisibility(View.GONE);
                downloadViewHolder.informationExpandableBtn.setEnabled(false);
                downloadViewHolder.title.setText(download.getTitle());
            }

            downloadViewHolder.subTitle.setText(download.getSubTitle());
            String tmp = download.getExtraInfo();
            if (tmp != null && !Objects.equals(tmp, ""))
                downloadViewHolder.extraInfo.setText(tmp);
            else downloadViewHolder.extraInfo.setVisibility(View.GONE);
            tmp = download.getStatNumbers();
            if (tmp != null && !Objects.equals(tmp, ""))
                downloadViewHolder.uploaderDate.setText(tmp);
            else downloadViewHolder.uploaderDate.setVisibility(View.GONE);
        } else if (holder instanceof LoadingViewHolder) {
            LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
            loadingViewHolder.progressBar.setIndeterminate(true);
        }
    }

    @Override
    public int getItemCount() {
        return parsedDownloads.size();
    }

    private static class DownloadViewHolder extends RecyclerView.ViewHolder {
        final LinearLayout upperLinear, downloadRow, informationExpandable;
        final TextView title, subTitle, extraInfo, uploaderDate;
        final ImageButton informationExpandableBtn;

        DownloadViewHolder(View download) {
            super(download);
            upperLinear = (LinearLayout) download.findViewById(R.id.upper_linear);
            downloadRow = (LinearLayout) download.findViewById(R.id.download_row);
            informationExpandable = (LinearLayout) download.findViewById(R.id.child_board_expandable);
            title = (TextView) download.findViewById(R.id.download_title);
            subTitle = (TextView) download.findViewById(R.id.download_sub_title);
            extraInfo = (TextView) download.findViewById(R.id.download_extra_info);
            uploaderDate = (TextView) download.findViewById(R.id.download_uploader_date);
            informationExpandableBtn = (ImageButton) download.findViewById(R.id.download_information_button);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        final MaterialProgressBar progressBar;

        LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = (MaterialProgressBar) itemView.findViewById(R.id.recycler_progress_bar);
        }
    }
}
