package gr.thmmy.mthmmy.utils;

import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        holder.emojiButton.setImageResource(emojiIds[position].getSrc());
        if (holder.emojiButton.getDrawable() instanceof AnimationDrawable) {
            AnimationDrawable emojiAnimation = (AnimationDrawable) holder.emojiButton.getDrawable();
            if (!emojiAnimation.isRunning())
                emojiAnimation.start();
        }
        holder.emojiButton.setOnClickListener(view -> listener.onEmojiClick(view, position));
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
