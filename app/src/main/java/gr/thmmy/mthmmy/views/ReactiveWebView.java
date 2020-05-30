package gr.thmmy.mthmmy.views;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.webkit.WebView;
import android.widget.Toast;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.utils.ui.ImageDownloadDialogBuilder;

import static android.content.Context.CLIPBOARD_SERVICE;
import static gr.thmmy.mthmmy.utils.ui.PhotoViewUtils.displayPhotoViewImage;

public class ReactiveWebView extends WebView {
    private final static long MAX_TOUCH_DURATION = 100;
    private final Context context;
    private long downTime;


    public ReactiveWebView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public ReactiveWebView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public ReactiveWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    private void init(){
        setOnLongClickListener();
        this.setVerticalScrollBarEnabled(false);
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
            displayPhotoViewImage(context, imageURL);
        }
        return super.performClick();
    }

    private void setOnLongClickListener(){
        this.setOnLongClickListener(v -> {
            HitTestResult result = ReactiveWebView.this.getHitTestResult();
            if(result.getType() == HitTestResult.SRC_ANCHOR_TYPE)
                copyUrlToClipboard(result.getExtra());
            else if(result.getType() == WebView.HitTestResult.IMAGE_TYPE) {
                String imageURL = result.getExtra();
                ImageDownloadDialogBuilder builder = new ImageDownloadDialogBuilder(context,imageURL);
                builder.show();
            }
            return false;
        });
    }

    private void copyUrlToClipboard(String urlToCopy){
        ClipboardManager clipboard = (ClipboardManager) BaseApplication.getInstance().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ReactiveWebViewCopiedText", urlToCopy);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(BaseApplication.getInstance().getApplicationContext(),context.getString(R.string.link_copied_msg),Toast.LENGTH_SHORT).show();
    }
}
