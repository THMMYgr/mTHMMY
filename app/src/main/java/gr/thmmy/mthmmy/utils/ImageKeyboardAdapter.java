package gr.thmmy.mthmmy.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.Arrays;

public class ImageKeyboardAdapter extends BaseAdapter {

    private Context context;
    private Integer[] emojiIds;

    public ImageKeyboardAdapter(Context context, Integer[] emojiIds) {
        this.context = context;
        this.emojiIds = emojiIds;
        // sort images by width
        Arrays.sort(this.emojiIds, (img1, img2) ->
                context.getResources().getDrawable(img1).getIntrinsicWidth() -
                        context.getResources().getDrawable(img2).getIntrinsicWidth());
    }

    @Override
    public int getCount() {
        return emojiIds.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView emoji;
        if (convertView == null) {
            emoji = new ImageView(context);
            emoji.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            emoji.setScaleType(ImageView.ScaleType.CENTER_CROP);

            emoji.setPadding(8, 8, 8, 8);
        } else {
            emoji = (ImageView) convertView;
        }
        emoji.setImageResource(emojiIds[position]);
        return emoji;
    }
}
