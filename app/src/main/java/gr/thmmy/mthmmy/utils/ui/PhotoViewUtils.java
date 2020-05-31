package gr.thmmy.mthmmy.utils.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.view.Window;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;

public class PhotoViewUtils {
    private final static int screenWidth = BaseApplication.getInstance().getWidthInPixels();
    private final static int screenHeight = BaseApplication.getInstance().getHeightInPixels();

    public static void displayPhotoViewImage(Context context, String imageURL) {
        Dialog builder = new Dialog(context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        PhotoView photoView = new PhotoView(context);
        photoView.setLayoutParams(new ViewGroup.LayoutParams(screenWidth, screenHeight));
        Glide.with(context)
                .load(imageURL)
                .fitCenter()
                .error(R.drawable.ic_file_not_found)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        photoView.setZoomable(false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        photoView.setOnLongClickListener(v -> {
                            ImageDownloadDialogBuilder imageDownloadDialogBuilder = new ImageDownloadDialogBuilder(context, imageURL);
                            imageDownloadDialogBuilder.show();
                            return false;
                        });
                        return false;
                    }
                })
                .into(photoView);

        builder.addContentView(photoView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }
}
