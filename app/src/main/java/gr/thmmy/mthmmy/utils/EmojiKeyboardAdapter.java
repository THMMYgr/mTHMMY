package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Arrays;

import gr.thmmy.mthmmy.R;

public class EmojiKeyboardAdapter extends RecyclerView.Adapter<EmojiKeyboardAdapter.EmojiViewHolder> {
    private int[] emojiIds;

    public EmojiKeyboardAdapter(int[] emojiIds) {
        this.emojiIds = emojiIds;
    }

    @NonNull
    @Override
    public EmojiViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        AppCompatImageView imageView = (AppCompatImageView) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.emoji_keyboard_grid_cell, parent, false);
        return new EmojiViewHolder(imageView);
    }

    @Override
    public void onBindViewHolder(@NonNull EmojiViewHolder holder, int position) {
        holder.imageView.setImageResource(emojiIds[position]);
    }

    @Override
    public int getItemCount() {
        return emojiIds.length;
    }

    public static class EmojiViewHolder extends RecyclerView.ViewHolder {
        AppCompatImageView imageView;
        public EmojiViewHolder(AppCompatImageView imageView) {
            super(imageView);
            this.imageView = imageView;
        }
    }
}
