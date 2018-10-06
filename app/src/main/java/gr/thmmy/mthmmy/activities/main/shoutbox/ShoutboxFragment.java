package gr.thmmy.mthmmy.activities.main.shoutbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.editorview.EditorView;
import gr.thmmy.mthmmy.editorview.EmojiKeyboard;
import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.model.Shoutbox;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ShoutboxFragment extends BaseFragment implements EmojiKeyboard.EmojiKeyboardOwner {

    private static final String TAG = "ShoutboxFragment";

    private MaterialProgressBar progressBar;
    private ShoutboxTask shoutboxTask;
    private ShoutAdapter shoutAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private EmojiKeyboard emojiKeyboard;
    private EditorView editorView;
    private Shoutbox shoutbox;

    public static ShoutboxFragment newInstance(int sectionNumber) {
        ShoutboxFragment fragment = new ShoutboxFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, TAG);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    private void onShoutboxTaskSarted() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSendShoutTaskStarted() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSendShoutTaskFinished(int resultCode, Void ignored) {
        editorView.setAlpha(1f);
        editorView.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            editorView.getEditText().getText().clear();
            shoutboxTask = new ShoutboxTask(ShoutboxFragment.this::onShoutboxTaskSarted, ShoutboxFragment.this::onShoutboxTaskFinished);
            shoutboxTask.execute("https://www.thmmy.gr/smf/index.php?action=forum");
        } else if (resultCode == NetworkResultCodes.NETWORK_ERROR) {
            Toast.makeText(getContext(), "NetworkError", Toast.LENGTH_SHORT).show();
        }
    }

    private void onShoutboxTaskFinished(int resultCode, Shoutbox shoutbox) {
        progressBar.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            shoutAdapter.setShouts(shoutbox.getShouts());
            shoutAdapter.notifyDataSetChanged();
            this.shoutbox = shoutbox;
        } else if (resultCode == NetworkResultCodes.NETWORK_ERROR) {
            Toast.makeText(getContext(), "NetworkError", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "Failed to retrieve shoutbox, please contact mthmmy developer team", Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_shoutbox, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        CustomRecyclerView recyclerView = rootView.findViewById(R.id.shoutbox_recyclerview);
        shoutAdapter = new ShoutAdapter(getContext(), new Shout[0]);
        recyclerView.setAdapter(shoutAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        shoutboxTask = new ShoutboxTask(this::onShoutboxTaskSarted, this::onShoutboxTaskFinished);
        shoutboxTask.execute("https://www.thmmy.gr/smf/index.php?action=forum");

        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
        swipeRefreshLayout.setColorSchemeResources(R.color.accent);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            shoutboxTask = new ShoutboxTask(ShoutboxFragment.this::onShoutboxTaskSarted, ShoutboxFragment.this::onShoutboxTaskFinished);
            shoutboxTask.execute("https://www.thmmy.gr/smf/index.php?action=forum");
        });

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

        return rootView;
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
