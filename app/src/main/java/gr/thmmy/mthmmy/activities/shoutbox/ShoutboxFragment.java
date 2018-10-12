package gr.thmmy.mthmmy.activities.shoutbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.editorview.EditorView;
import gr.thmmy.mthmmy.editorview.EmojiKeyboard;
import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.model.Shoutbox;
import gr.thmmy.mthmmy.session.SessionManager;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import gr.thmmy.mthmmy.viewmodel.ShoutboxViewModel;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class ShoutboxFragment extends Fragment implements EmojiKeyboard.EmojiKeyboardOwner {

    private MaterialProgressBar progressBar;
    private ShoutboxTask shoutboxTask;
    private ShoutAdapter shoutAdapter;
    private EmojiKeyboard emojiKeyboard;
    private EditorView editorView;

    private ShoutboxViewModel shoutboxViewModel;

    public static ShoutboxFragment newInstance() {
        return new ShoutboxFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_shoutbox, container, false);
        setHasOptionsMenu(true);

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

        emojiKeyboard = rootView.findViewById(R.id.emoji_keyboard);
        editorView = rootView.findViewById(R.id.edior_view);
        editorView.setEmojiKeyboardOwner(this);
        InputConnection ic = editorView.getInputConnection();
        setEmojiKeyboardInputConnection(ic);
        editorView.setOnSubmitListener(view -> {
            if (shoutboxViewModel.getShoutboxMutableLiveData().getValue() == null) return;
            if (editorView.getText().toString().isEmpty()) {
                editorView.setError("Required");
                return;
            }
            shoutboxViewModel.sendShout(editorView.getText().toString());
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.shoutbox_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            shoutboxViewModel.loadShoutbox();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        shoutboxViewModel = ViewModelProviders.of(this).get(ShoutboxViewModel.class);
        shoutboxViewModel.getShoutboxMutableLiveData().observe(this, shoutbox -> {
            if (shoutbox != null) {
                Timber.i("Shoutbox loaded successfully");
                shoutAdapter.setShouts(shoutbox.getShouts());
                shoutAdapter.notifyDataSetChanged();
            }
        });
        shoutboxViewModel.setOnShoutboxTaskStarted(this::onShoutboxTaskSarted);
        shoutboxViewModel.setOnShoutboxTaskFinished(this::onShoutboxTaskFinished);
        shoutboxViewModel.setOnSendShoutTaskStarted(this::onSendShoutTaskStarted);
        shoutboxViewModel.setOnSendShoutTaskFinished(this::onSendShoutTaskFinished);

        shoutboxViewModel.loadShoutbox();
    }

    private void onShoutboxTaskSarted() {
        Timber.i("Starting shoutbox task...");
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSendShoutTaskStarted() {
        Timber.i("Start sending a shout...");
        editorView.setAlpha(0.5f);
        editorView.setEnabled(false);
        setEmojiKeyboardVisible(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSendShoutTaskFinished(int resultCode, Void ignored) {
        editorView.setAlpha(1f);
        editorView.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
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
            shoutboxViewModel.setShoutbox(shoutbox);
            if (shoutbox.getShoutSend() != null)
                editorView.setVisibility(View.VISIBLE);
        } else if (resultCode == NetworkResultCodes.NETWORK_ERROR) {
            Timber.w("Failed to retreive shoutbox due to network error");
            Toast.makeText(getContext(), "NetworkError", Toast.LENGTH_SHORT).show();
        } else {
            Timber.wtf("Failed to retreive shoutbox due to unknown error");
            Toast.makeText(getContext(), "Failed to retrieve shoutbox, please contact mthmmy developer team", Toast.LENGTH_LONG).show();
        }
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
