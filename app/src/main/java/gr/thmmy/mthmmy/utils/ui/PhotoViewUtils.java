package gr.thmmy.mthmmy.utils.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.ViewGroup;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import gr.thmmy.mthmmy.base.BaseApplication;

public class PhotoViewUtils {
    private final static int screenWidth = BaseApplication.getInstance().getWidthInPixels();
    private final static int screenHeight = BaseApplication.getInstance().getHeightInPixels();

    public static void displayPhotoViewImage(Context context, String url) {
        Dialog builder = new Dialog(context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        PhotoView photoView = new PhotoView(context);
        photoView.setLayoutParams(new ViewGroup.LayoutParams(screenWidth, screenHeight));

        Glide.with(context).load(url).fitCenter().into(photoView);

        builder.addContentView(photoView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }
}
