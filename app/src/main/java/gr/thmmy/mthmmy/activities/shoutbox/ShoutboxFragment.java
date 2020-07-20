package gr.thmmy.mthmmy.activities.shoutbox;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;

import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseApplication;
import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.model.Shoutbox;
import gr.thmmy.mthmmy.utils.networking.NetworkResultCodes;
import gr.thmmy.mthmmy.viewmodel.ShoutboxViewModel;
import gr.thmmy.mthmmy.views.CustomRecyclerView;
import gr.thmmy.mthmmy.views.editorview.EditorView;
import gr.thmmy.mthmmy.views.editorview.EmojiKeyboard;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;
import timber.log.Timber;

public class ShoutboxFragment extends Fragment {

    private MaterialProgressBar progressBar;
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
        editorView.setEmojiKeyboard(emojiKeyboard);
        emojiKeyboard.registerEmojiInputField(editorView);
        editorView.setOnSubmitListener(view -> {
            if (shoutboxViewModel.getShoutboxMutableLiveData().getValue() == null) return;
            if (editorView.getText().toString().isEmpty()) {
                editorView.setError("Required");
                return;
            }
            shoutboxViewModel.sendShout(editorView.getText().toString());
        });
        editorView.hideMarkdown();
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
            shoutboxViewModel.loadShoutbox(true);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        shoutboxViewModel = ViewModelProviders.of(getActivity()).get(ShoutboxViewModel.class);
        shoutboxViewModel.getShoutboxMutableLiveData().observe(this, shoutbox -> {
            if (shoutbox != null) {
                Timber.i("Shoutbox loaded successfully");
                shoutAdapter.setShouts(shoutbox.getShouts());
                shoutAdapter.notifyDataSetChanged();
                editorView.setVisibility(shoutbox.getShoutSend() == null ? View.GONE : View.VISIBLE);
            }
        });
        shoutboxViewModel.setOnShoutboxTaskStarted(this::onShoutboxTaskStarted);
        shoutboxViewModel.setOnShoutboxTaskFinished(this::onShoutboxTaskFinished);
        shoutboxViewModel.setOnSendShoutTaskStarted(this::onSendShoutTaskStarted);
        shoutboxViewModel.setOnSendShoutTaskFinished(this::onSendShoutTaskFinished);

        shoutboxViewModel.loadShoutbox(false);
    }

    private void onShoutboxTaskStarted() {
        Timber.i("Starting shoutbox task...");
        progressBar.setVisibility(View.VISIBLE);
        editorView.setVisibility(View.GONE);
    }

    private void onSendShoutTaskStarted() {
        Timber.i("Start sending a shout...");
        editorView.setAlpha(0.5f);
        editorView.setEnabled(false);
        if (emojiKeyboard.isVisible())
            emojiKeyboard.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void onSendShoutTaskFinished(int resultCode, Void ignored) {
        editorView.setAlpha(1f);
        editorView.setEnabled(true);
        progressBar.setVisibility(View.INVISIBLE);
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            Timber.i("Shout was sent successfully");
            BaseApplication.getInstance().logFirebaseAnalyticsEvent("shout", null);
            editorView.getEditText().getText().clear();
            shoutboxViewModel.loadShoutbox(true);
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

    /**
     * @return whether or not {@link ShoutboxFragment#onBackPressed()} consumed the event or not
     */
    public boolean onBackPressed() {
        if (emojiKeyboard.isVisible()) {
            emojiKeyboard.setVisibility(View.GONE);
            return true;
        }
        return false;
    }
}
