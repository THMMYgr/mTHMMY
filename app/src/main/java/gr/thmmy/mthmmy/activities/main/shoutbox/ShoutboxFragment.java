package gr.thmmy.mthmmy.activities.main.shoutbox;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.editorview.EditorView;
import gr.thmmy.mthmmy.editorview.EmojiKeyboard;
import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.model.Shoutbox;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class ShoutboxFragment extends BaseFragment implements EmojiKeyboard.EmojiKeyboardOwner {

    private static final String TAG = "ShoutboxFragment";

    private MaterialProgressBar progressBar;
    private TextView refreshLabel;
    private ShoutboxTask shoutboxTask;
    private ShoutAdapter shoutAdapter;
    private EmojiKeyboard emojiKeyboard;
    private EditorView editorView;
    private Shoutbox shoutbox;
    private ValueAnimator animator;

    public static ShoutboxFragment newInstance(int sectionNumber) {
        ShoutboxFragment fragment = new ShoutboxFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, TAG);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private void onShoutboxTaskSarted() {
        Timber.i("Starting shoutbox task...");
        hideRefreshLabel();
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSendShoutTaskStarted() {
        Timber.i("Start sending a shout...");
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSendShoutTaskFinished(int resultCode, Void ignored) {
        editorView.setAlpha(1f);
        editorView.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        showRefreshLabel();
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            Timber.i("Shout was sent successfully");
            editorView.getEditText().getText().clear();
            shoutboxTask = new ShoutboxTask(ShoutboxFragment.this::onShoutboxTaskSarted, ShoutboxFragment.this::onShoutboxTaskFinished);
            shoutboxTask.execute(SessionManager.shoutboxUrl.toString());
        } else if (resultCode == NetworkResultCodes.NETWORK_ERROR) {
            Timber.w("Failed to send shout");
            Toast.makeText(getContext(), "NetworkError", Toast.LENGTH_SHORT).show();
        }
    }

    private void onShoutboxTaskFinished(int resultCode, Shoutbox shoutbox) {
        progressBar.setVisibility(View.INVISIBLE);
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            Timber.i("Shoutbox loaded successfully");
            shoutAdapter.setShouts(shoutbox.getShouts());
            shoutAdapter.notifyDataSetChanged();
            this.shoutbox = shoutbox;
        } else if (resultCode == NetworkResultCodes.NETWORK_ERROR) {
            Timber.w("Failed to retreive shoutbox due to network error");
            Toast.makeText(getContext(), "NetworkError", Toast.LENGTH_SHORT).show();
        } else {
            Timber.wtf("Failed to retreive shoutbox due to unknown error");
            Toast.makeText(getContext(), "Failed to retrieve shoutbox, please contact mthmmy developer team", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_shoutbox, container, false);

        refreshLabel = rootView.findViewById(R.id.refresh_label);
        refreshLabel.setOnClickListener(v -> {
            shoutboxTask = new ShoutboxTask(this::onShoutboxTaskSarted, this::onShoutboxTaskFinished);
            shoutboxTask.execute(SessionManager.shoutboxUrl.toString());
        });

        progressBar = rootView.findViewById(R.id.progressBar);
        CustomRecyclerView recyclerView = rootView.findViewById(R.id.shoutbox_recyclerview);
        shoutAdapter = new ShoutAdapter(getContext(), new Shout[0]);
        recyclerView.setAdapter(shoutAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setOnTouchListener((view, motionEvent) -> {
            editorView.hideMarkdown();
            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(editorView.getWindowToken(), 0);
            return false;
        });
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    showRefreshLabel();
                } else {
                    hideRefreshLabel();
                }
            }
        });

        shoutboxTask = new ShoutboxTask(this::onShoutboxTaskSarted, this::onShoutboxTaskFinished);
        shoutboxTask.execute(SessionManager.shoutboxUrl.toString());

        emojiKeyboard = rootView.findViewById(R.id.emoji_keyboard);
        editorView = rootView.findViewById(R.id.edior_view);
        editorView.setEmojiKeyboardOwner(this);
        InputConnection ic = editorView.getInputConnection();
        setEmojiKeyboardInputConnection(ic);
        editorView.setOnSubmitListener(view -> {
            if (shoutbox == null) return;
            if (editorView.getText().toString().isEmpty()) {
                editorView.setError("Required");
                return;
            }
            editorView.setAlpha(0.5f);
            editorView.setEnabled(false);
            setEmojiKeyboardVisible(false);
            new SendShoutTask(this::onSendShoutTaskStarted, this::onSendShoutTaskFinished)
                    .execute(shoutbox.getSendShoutUrl(), editorView.getText().toString(), shoutbox.getSc(),
                            shoutbox.getShoutName(), shoutbox.getShoutSend(), shoutbox.getShoutUrl());
        });
        editorView.hideMarkdown();
        editorView.setOnTouchListener((view, motionEvent) -> {
            editorView.showMarkdown();
            return false;
        });
        editorView.setMarkdownVisible(false);
        editorView.showMarkdownOnfocus();

        return rootView;
    }

    private void hideRefreshLabel() {
        if (refreshLabel.getVisibility() == View.GONE) return;
        if (animator != null) animator.cancel();
        animator = getRefreshLabelAnimation();
        animator.start();
    }

    private void showRefreshLabel() {
        if (refreshLabel.getVisibility() == View.VISIBLE) return;
        if (animator != null) animator.cancel();
        animator = getRefreshLabelAnimation();
        animator.reverse();
    }

    private ValueAnimator getRefreshLabelAnimation() {
        ValueAnimator animator = ValueAnimator.ofFloat(-200, 0);
        animator.addUpdateListener(valueAnimator -> {
            if (((Float) valueAnimator.getAnimatedValue()).intValue() == 1) refreshLabel.setVisibility(View.VISIBLE);
            if (((Float) valueAnimator.getAnimatedValue()).intValue() == -199) refreshLabel.setVisibility(View.GONE);
            refreshLabel.setTranslationY((float) valueAnimator.getAnimatedValue());
        });
        animator.setInterpolator(new FastOutSlowInInterpolator());
        animator.setDuration(200);
        return animator;
    }

    @Override
    public void setEmojiKeyboardVisible(boolean visible) {
        emojiKeyboard.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean isEmojiKeyboardVisible() {
        return emojiKeyboard.getVisibility() == View.VISIBLE;
    }

    @Override
    public void setEmojiKeyboardInputConnection(InputConnection ic) {
        emojiKeyboard.setInputConnection(ic);
    }
}
