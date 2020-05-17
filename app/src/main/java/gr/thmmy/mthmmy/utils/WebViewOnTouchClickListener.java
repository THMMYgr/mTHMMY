package gr.thmmy.mthmmy.utils;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import gr.thmmy.mthmmy.base.BaseApplication;

/**
 * Workaround for WebView's inability to send onClick events (sends only onLongClickEvents)
 * If an image is find when clicking it will be inflated as a zoomed/zoomable/pinchable PhotoView
 */
public class WebViewOnTouchClickListener implements View.OnTouchListener {
    private final static int screenWidth = BaseApplication.getInstance().getWidthInPixels();
    private final static int screenHeight = BaseApplication.getInstance().getHeightInPixels();

    private final static long MAX_TOUCH_DURATION = 100;
    private final Context context;
    private long downTime;

    public WebViewOnTouchClickListener(Context context){
        this.context = context;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTime = event.getEventTime();
                break;
            case MotionEvent.ACTION_UP:
                if(event.getEventTime() - downTime <= MAX_TOUCH_DURATION)
                    onClick((WebView) v);
                break;
            default:
                break;
        }
        return false;
    }

    private void onClick(WebView webView){
        WebView.HitTestResult result = webView.getHitTestResult();
        if(result.getType() == WebView.HitTestResult.IMAGE_TYPE){
            String imageURL = result.getExtra();
            showImage(imageURL);
        }
        webView.performClick();
    }

    private void showImage(String url) {
        Dialog builder = new Dialog(context);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));

        PhotoView photoView = new PhotoView(context);
        photoView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
        photoView.getLayoutParams().width = screenWidth;
        photoView.getLayoutParams().height = screenHeight;

        Glide.with(context).load(url).fitCenter().into(photoView);
        builder.addContentView(photoView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        builder.show();
    }
}
