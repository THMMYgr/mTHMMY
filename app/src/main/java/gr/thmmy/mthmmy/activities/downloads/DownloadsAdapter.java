package gr.thmmy.mthmmy.activities.downloads;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_TITLE;
import static gr.thmmy.mthmmy.activities.downloads.DownloadsActivity.BUNDLE_DOWNLOADS_URL;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.model.Download;
import gr.thmmy.mthmmy.model.ThmmyFile;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

class DownloadsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final int VIEW_TYPE_DOWNLOAD = 0;
    private final int VIEW_TYPE_LOADING = 1;

    private final Context context;
    private ArrayList<Download> parsedDownloads;
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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DOWNLOAD) {
            View download = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.activity_downloads_row, parent, false);
            return new DownloadViewHolder(download);
        }
        // viewType == VIEW_TYPE_LOADING
        else {
            View loading = LayoutInflater.from(parent.getContext()).
                    inflate(R.layout.recycler_loading_item, parent, false);
            return new LoadingViewHolder(loading);
        }
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
                downloadViewHolder.downloadRow.setOnClickListener(view -> {
                    Intent intent = new Intent(context, DownloadsActivity.class);
                    Bundle extras = new Bundle();
                    extras.putString(BUNDLE_DOWNLOADS_URL, download.getUrl());
                    extras.putString(BUNDLE_DOWNLOADS_TITLE, download.getTitle());
                    intent.putExtras(extras);
                    intent.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                });

                final int pos = downloadViewHolder.getBindingAdapterPosition();

                if (pos >=0 && pos < downloadExpandableVisibility.size() && downloadExpandableVisibility.get(pos)) {
                    downloadViewHolder.informationExpandable.setVisibility(View.VISIBLE);
                    downloadViewHolder.informationExpandableBtn.setImageResource(R.drawable.ic_arrow_drop_up_accent_24dp);
                }
                else {
                    downloadViewHolder.informationExpandable.setVisibility(View.GONE);
                    downloadViewHolder.informationExpandableBtn.setImageResource(R.drawable.ic_arrow_drop_down_accent_24dp);
                }

                downloadViewHolder.informationExpandableBtn.setOnClickListener(view -> {
                    final int pos2 = downloadViewHolder.getBindingAdapterPosition();
                    if (pos2 >=0 && pos2 < downloadExpandableVisibility.size()){
                        final boolean visible = downloadExpandableVisibility.get(pos2);
                        if (visible) {
                            downloadViewHolder.informationExpandable.setVisibility(View.GONE);
                            downloadViewHolder.informationExpandableBtn.setImageResource(R.drawable.ic_arrow_drop_down_accent_24dp);
                        }
                        else {
                            downloadViewHolder.informationExpandable.setVisibility(View.VISIBLE);
                            downloadViewHolder.informationExpandableBtn.setImageResource(R.drawable.ic_arrow_drop_up_accent_24dp);
                        }
                        downloadExpandableVisibility.set(pos2, !visible);
                    }
                });
                downloadViewHolder.title.setTypeface(Typeface.createFromAsset(context.getAssets()
                        , "fonts/fontawesome-webfont.ttf"));
                if (download.hasSubCategory()) {
                    String tmp = context.getResources().getString(R.string.fa_folder) + " "
                            + download.getTitle();
                    downloadViewHolder.title.setText(tmp);
                }
                else {
                    String tmp = context.getResources().getString(R.string.fa_file) + " "
                            + download.getTitle();
                    downloadViewHolder.title.setText(tmp);
                }
            }
            else {
                downloadViewHolder.downloadRow.setOnClickListener(view -> {
                    try {
                        ((BaseActivity) context).downloadFile(new ThmmyFile(
                                new URL(download.getUrl()), download.getFileName(), null));
                    } catch (MalformedURLException e) {
                        Timber.e(e, "MalformedURLException");
                    }
                });

                downloadViewHolder.upperLinear.setBackgroundColor(context.getResources().getColor(R.color.background, null));
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
        }
        else if (holder instanceof LoadingViewHolder) {
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
            upperLinear = download.findViewById(R.id.upper_linear);
            downloadRow = download.findViewById(R.id.download_row);
            informationExpandable = download.findViewById(R.id.child_board_expandable);
            title = download.findViewById(R.id.download_title);
            subTitle = download.findViewById(R.id.download_sub_title);
            uploaderDate = download.findViewById(R.id.download_extra_info);
            extraInfo = download.findViewById(R.id.download_uploader_date);
            informationExpandableBtn = download.findViewById(R.id.download_information_button);
        }
    }

    private static class LoadingViewHolder extends RecyclerView.ViewHolder {
        final MaterialProgressBar progressBar;

        LoadingViewHolder(View itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.recycler_progress_bar);
        }
    }
}
