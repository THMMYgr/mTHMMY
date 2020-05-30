package gr.thmmy.mthmmy.utils.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import java.net.MalformedURLException;
import java.net.URL;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseActivity;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.ThmmyFile;
import timber.log.Timber;

import static android.content.Context.CLIPBOARD_SERVICE;

public class ImageDownloadDialogBuilder extends AlertDialog.Builder{
    private static final String[] colors = {"Copy image location", "Save Image"};

    private Context context;
    private String imageURL;

    public ImageDownloadDialogBuilder(@NonNull Context context, String imageURL) {
        super(context);
        this.context = context;
        this.imageURL = imageURL;

        setItems(colors, (dialog, which) -> {
            if(which == 0)
                copyUrlToClipboard();
            else {
                try {
                    getBaseActivity().downloadFile(new ThmmyFile(new URL(imageURL)));
                } catch (MalformedURLException e) {
                    Timber.e(e, "Exception downloading image (MalformedURLException)");
                } catch (NullPointerException e) {
                    Timber.e(e, "Exception downloading image (NullPointerException)");
                }
            }
        });
    }

    private void copyUrlToClipboard(){
        ClipboardManager clipboard = (ClipboardManager) BaseApplication.getInstance().getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ReactiveWebViewCopiedText", imageURL);
        clipboard.setPrimaryClip(clip);
        Toast.makeText(BaseApplication.getInstance().getApplicationContext(),context.getString(R.string.link_copied_msg),Toast.LENGTH_SHORT).show();
    }

    private BaseActivity getBaseActivity() {
        Context baseActivityContext = context;
        while (baseActivityContext instanceof ContextWrapper) {
            if (context instanceof BaseActivity)
                return (BaseActivity) context;
            baseActivityContext = ((ContextWrapper)context).getBaseContext();
        }
        return null;
    }
}
