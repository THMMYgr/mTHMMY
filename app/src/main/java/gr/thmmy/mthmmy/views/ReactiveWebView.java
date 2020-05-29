package gr.thmmy.mthmmy.views;

import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import gr.thmmy.mthmmy.base.BaseApplication;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ReactiveWebView extends WebView {
    private final static int screenWidth = BaseApplication.getInstance().getWidthInPixels();
    private final static int screenHeight = BaseApplication.getInstance().getHeightInPixels();

    private final static long MAX_TOUCH_DURATION = 100;
    private final Context context;
    private long downTime;


    public ReactiveWebView(Context context) {
        super(context);
        this.context = context;
        setOnLongClickListener();
    }

    public ReactiveWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        setOnLongClickListener();
    }

    public ReactiveWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        setOnLongClickListener();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downTime = event.getEventTime();
                break;
            case MotionEvent.ACTION_UP:
                if(event.getEventTime() - downTime <= MAX_TOUCH_DURATION)
                    performClick();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        WebView.HitTestResult result = this.getHitTestResult();
        if(result.getType() == WebView.HitTestResult.IMAGE_TYPE){
            String imageURL = result.getExtra();
            showImage(imageURL);
        }
        return super.performClick();
    }

    private void showImage(String url) {
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

    private void setOnLongClickListener(){
        this.setOnLongClickListener(v -> {
            HitTestResult result = ReactiveWebView.this.getHitTestResult();
            if(result.getType() == HitTestResult.SRC_ANCHOR_TYPE){
                ClipboardManager clipboard = (ClipboardManager) BaseApplication.getInstance().getSystemService(CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ReactiveWebViewCopiedText", result.getExtra());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(BaseApplication.getInstance().getApplicationContext(),"Link copied",Toast.LENGTH_SHORT).show();
            }
            return false;
        });
    }
}
