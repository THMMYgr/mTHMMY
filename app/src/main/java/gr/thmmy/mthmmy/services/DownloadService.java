package gr.thmmy.mthmmy.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.receiver.Receiver;
import mthmmy.utils.Report;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 */
public class DownloadService extends IntentService {
    private static final String TAG = "DownloadService";
    private static int sDownloadId =0;

    private Receiver receiver;

    public static final String ACTION_DOWNLOAD = "gr.thmmy.mthmmy.services.action.DOWNLOAD";
    public static final String EXTRA_DOWNLOAD_URL = "gr.thmmy.mthmmy.services.extra.DOWNLOAD_URL";

    public static final String EXTRA_DOWNLOAD_ID = "gr.thmmy.mthmmy.services.extra.DOWNLOAD_ID";
    public static final String EXTRA_DOWNLOAD_STATE = "gr.thmmy.mthmmy.services.extra.DOWNLOAD_STATE";
    public static final String EXTRA_NOTIFICATION_TITLE = "gr.thmmy.mthmmy.services.extra.NOTIFICATION_TITLE";
    public static final String EXTRA_NOTIFICATION_TEXT = "gr.thmmy.mthmmy.services.extra.NOTIFICATION_TEXT";
    public static final String EXTRA_NOTIFICATION_TICKER = "gr.thmmy.mthmmy.services.extra.NOTIFICATION_TICKER";

    public static final String STARTED = "Started";
    public static final String COMPLETED = "Completed";
    public static final String FAILED = "Failed";



    public DownloadService() {
        super("DownloadService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final IntentFilter filter = new IntentFilter(DownloadService.ACTION_DOWNLOAD);
        receiver = new Receiver();
        registerReceiver(receiver, filter);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }

    /**
     * Starts this service to perform action Download with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    public static void startActionDownload(Context context, String downloadUrl) {
        Intent intent = new Intent(context, DownloadService.class);
        intent.setAction(ACTION_DOWNLOAD);
        intent.putExtra(EXTRA_DOWNLOAD_URL, downloadUrl);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD.equals(action)) {
                final String downloadLink = intent.getStringExtra(EXTRA_DOWNLOAD_URL);
                handleActionDownload(downloadLink);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void handleActionDownload(String downloadLink) {
        OkHttpClient client = BaseApplication.getInstance().getClient();
        BufferedSink sink = null;
        String trueName = null;
        int downloadId = sDownloadId;
        sDownloadId++;

        try {
            Request request = new Request.Builder().url(downloadLink).build();
            Response response = client.newCall(request).execute();

            String contentType = response.headers("Content-Type").toString();   //check if link provides a binary file
            if(contentType.equals("[application/octet-stream]"))
            {
                String fileName = response.headers("Content-Disposition").toString().split("\"")[1];

                File dirPath = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "mthmmy");
                if(!dirPath.isDirectory())
                {
                    if(dirPath.mkdirs())
                        Report.i(TAG, "mTHMMY's directory created successfully!");
                    else
                        Report.e(TAG, "Couldn't create mTHMMY's directory...");
                }


                String nameFormat;
                String[] tokens = fileName.split("\\.(?=[^\\.]+$)");

                if(tokens.length!=2)
                {
                    Report.w(TAG, "Error getting file extension");
                    nameFormat = fileName + "(%d)";
                }
                else
                    nameFormat = tokens[0] + "(%d)." + tokens[1];


                File file = new File(dirPath, fileName);

                for (int i = 1;;i++) {
                    if (!file.exists()) {
                        break;
                    }

                    file = new File(dirPath, String.format(nameFormat, i));
                }

                trueName = file.getName();

                Report.v(TAG, "Started saving file " + trueName);
                sendNotification(downloadId, STARTED, trueName);

                sink = Okio.buffer(Okio.sink(file));
                sink.writeAll(response.body().source());
                sink.flush();
                Report.i(TAG, "Download OK!");
                sendNotification(downloadId, COMPLETED, trueName);
            }
            else
                Report.e(TAG, "Response not a binary!");
        }
        catch (FileNotFoundException e){
            Report.e(TAG, "FileNotFound", e);
            Report.i(TAG, "Download failed...");
            if(trueName!=null)
                sendNotification(downloadId, FAILED, trueName);
            else
                sendNotification(downloadId, FAILED, "file");
        }
        catch (IOException e){
            Report.e(TAG, "IOException", e);
            Report.i(TAG, "Download failed...");
            if(trueName!=null)
                sendNotification(downloadId, FAILED, trueName);
            else
                sendNotification(downloadId, FAILED, "file");
        } finally {
            if (sink!= null) {
                try {
                    sink.close();
                } catch (IOException e) {
                    // Ignore - Significant errors should already have been reported
                }
            }
        }
    }

    private void sendNotification(int downloadId, String type, @NonNull String fileName)
    {
        switch (type) {
            case STARTED: {
                Intent intent = new Intent(ACTION_DOWNLOAD);
                intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
                intent.putExtra(EXTRA_DOWNLOAD_STATE, STARTED);
                intent.putExtra(EXTRA_NOTIFICATION_TITLE, "Download Started");
                intent.putExtra(EXTRA_NOTIFICATION_TEXT, "\"" + fileName + "\" downloading...");
                intent.putExtra(EXTRA_NOTIFICATION_TICKER, "Downloading...");
                sendBroadcast(intent);
                break;
            }
            case COMPLETED: {
                Intent intent = new Intent(ACTION_DOWNLOAD);
                intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
                intent.putExtra(EXTRA_DOWNLOAD_STATE, COMPLETED);
                intent.putExtra(EXTRA_NOTIFICATION_TITLE, "Download Completed");
                intent.putExtra(EXTRA_NOTIFICATION_TEXT, "\"" + fileName + "\" finished downloading.");
                intent.putExtra(EXTRA_NOTIFICATION_TICKER, "Download Completed");
                sendBroadcast(intent);
                break;
            }
            case FAILED: {
                Intent intent = new Intent(ACTION_DOWNLOAD);
                intent.putExtra(EXTRA_DOWNLOAD_ID, downloadId);
                intent.putExtra(EXTRA_DOWNLOAD_STATE, FAILED);
                intent.putExtra(EXTRA_NOTIFICATION_TITLE, "Download Failed");
                intent.putExtra(EXTRA_NOTIFICATION_TEXT, "\"" + fileName + "\" failed.");
                intent.putExtra(EXTRA_NOTIFICATION_TICKER, "Download Failed");
                sendBroadcast(intent);
                break;
            }
        }

    }

}
