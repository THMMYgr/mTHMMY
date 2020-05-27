package gr.thmmy.mthmmy.views.editorview;

import android.graphics.drawable.AnimationDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.recyclerview.widget.RecyclerView;

import gr.thmmy.mthmmy.R;

public class EmojiKeyboardAdapter extends RecyclerView.Adapter<EmojiKeyboardAdapter.EmojiViewHolder> {
    private EmojiKeyboard.Emoji[] emojiIds;
    private OnEmojiClickListener listener;

    public EmojiKeyboardAdapter(EmojiKeyboard.Emoji[] emojiIds) {
        this.emojiIds = emojiIds;
    }

    public void setOnEmojiClickListener(OnEmojiClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppCompatImageButton emojiButton = (AppCompatImageButton) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.emoji_keyboard_grid_cell, parent, false);
        return new EmojiViewHolder(emojiButton);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        holder.emojiButton.setOnClickListener(view -> listener.onEmojiClick(view, position));
    }

    @Override
    public void onViewAttachedToWindow(@NonNull EmojiViewHolder holder) {
        holder.emojiButton.setImageResource(emojiIds[holder.getAdapterPosition()].getSrc());
        if (holder.emojiButton.getDrawable() instanceof AnimationDrawable) {
            AnimationDrawable emojiAnimation = (AnimationDrawable) holder.emojiButton.getDrawable();
            emojiAnimation.start();
        }
    }

    @Override
    public int getItemCount() {
        return emojiIds.length;
    }

    static class EmojiViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageButton emojiButton;

        EmojiViewHolder(AppCompatImageButton emojiButton) {
            super(emojiButton);
            this.emojiButton = emojiButton;
        }
    }

    interface OnEmojiClickListener {
        void onEmojiClick(View view, int position);
    }
}
