package gr.thmmy.mthmmy.views.editorview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import gr.thmmy.mthmmy.R;

public class FormatButtonsAdapter extends RecyclerView.Adapter<FormatButtonsAdapter.FormatButtonViewHolder> {
    private OnFormatButtonClickListener listener;

    public static final int[] FORMAT_BUTTON_IDS = {R.drawable.ic_format_bold, R.drawable.ic_format_italic,
    R.drawable.ic_format_underlined, R.drawable.ic_strikethrough_s, R.drawable.ic_format_color_text,
    R.drawable.ic_format_size, R.drawable.ic_text_format, R.drawable.ic_format_list_bulleted,
    R.drawable.ic_format_align_left, R.drawable.ic_format_align_center, R.drawable.ic_format_align_right,
    R.drawable.ic_insert_link, R.drawable.ic_format_quote, R.drawable.ic_code, R.drawable.ic_functions};

    public FormatButtonsAdapter(OnFormatButtonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public FormatButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppCompatImageButton formatButton = (AppCompatImageButton) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.format_button_grid_cell, parent, false);
        return new FormatButtonViewHolder(formatButton);
    }

    @Override
    public void onBindViewHolder(@NonNull FormatButtonViewHolder holder, int position) {
        holder.formatButton.setImageResource(FORMAT_BUTTON_IDS[position]);
        holder.formatButton.setOnClickListener(v ->
                listener.onFormatButtonClick(v, FORMAT_BUTTON_IDS[holder.getAdapterPosition()]));
    }

    @Override
    public int getItemCount() {
        return FORMAT_BUTTON_IDS.length;
    }

    static class FormatButtonViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageButton formatButton;
        FormatButtonViewHolder(AppCompatImageButton formatButton) {
            super(formatButton);
            this.formatButton = formatButton;
        }
    }

    public interface OnFormatButtonClickListener {
        void onFormatButtonClick(View view, int drawableId);
    }
}
