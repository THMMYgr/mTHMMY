package gr.thmmy.mthmmy.viewmodel;

import android.app.NotificationManager;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.Objects;

import gr.thmmy.mthmmy.activities.topic.TopicActivity;
import gr.thmmy.mthmmy.activities.topic.TopicTask;
import gr.thmmy.mthmmy.activities.topic.TopicTaskResult;
import gr.thmmy.mthmmy.model.Bookmark;
import timber.log.Timber;

import static gr.thmmy.mthmmy.services.NotificationService.NEW_POST_TAG;

public class TopicViewModel extends BaseViewModel implements TopicTask.OnTopicTaskCompleted {
    private MutableLiveData<TopicTaskResult> topicTaskResultMutableLiveData;

    public MutableLiveData<TopicTaskResult> getTopicTaskResultMutableLiveData() {
        if (topicTaskResultMutableLiveData == null) {
            topicTaskResultMutableLiveData = new MutableLiveData<>();
            //load topic data
        }
        return topicTaskResultMutableLiveData;
    }

    public void initialLoad(String pageUrl) {
        new TopicTask().execute(pageUrl);
        //load posts
    }

    @Override
    public void onTopicTaskCompleted(TopicTaskResult result) {
        topicTaskResultMutableLiveData.setValue(result);
    }
}
