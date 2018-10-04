package gr.thmmy.mthmmy.activities.main.shoutbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.editorview.EditorView;
import gr.thmmy.mthmmy.editorview.EmojiKeyboard;
import gr.thmmy.mthmmy.model.Shout;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import gr.thmmy.mthmmy.utils.NetworkResultCodes;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ShoutboxFragment extends BaseFragment implements EmojiKeyboard.EmojiKeyboardOwner {

    private static final String TAG = "ShoutboxFragment";

    private MaterialProgressBar progressBar;
    private ShoutboxTask shoutboxTask;
    private ShoutAdapter shoutAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ArrayList<Shout> shouts;
    private EmojiKeyboard emojiKeyboard;
    private EditorView editorView;

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

    private void onShoutboxTaskFinished(int resultCode, ArrayList<Shout> shouts) {
        progressBar.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(false);
        if (resultCode == NetworkResultCodes.SUCCESSFUL) {
            this.shouts.clear();
            this.shouts.addAll(shouts);
            shoutAdapter.notifyDataSetChanged();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_shoutbox, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        CustomRecyclerView recyclerView = rootView.findViewById(R.id.shoutbox_recyclerview);
        shouts = new ArrayList<>();
        shoutAdapter = new ShoutAdapter(getContext(), shouts);
        recyclerView.setAdapter(shoutAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        shoutboxTask = new ShoutboxTask(this::onShoutboxTaskSarted, this::onShoutboxTaskFinished);
        shoutboxTask.execute("https://www.thmmy.gr/smf/index.php?");

        swipeRefreshLayout = rootView.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            shoutboxTask = new ShoutboxTask(ShoutboxFragment.this::onShoutboxTaskSarted, ShoutboxFragment.this::onShoutboxTaskFinished);
            shoutboxTask.execute("https://www.thmmy.gr/smf/index.php?");
        });

        emojiKeyboard = rootView.findViewById(R.id.emoji_keyboard);
        editorView = rootView.findViewById(R.id.edior_view);
        editorView.setEmojiKeyboardOwner(this);
        InputConnection ic = editorView.onCreateInputConnection(new EditorInfo());
        setEmojiKeyboardInputConnection(ic);

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
