package gr.thmmy.mthmmy.activities.main.shoutbox;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import gr.thmmy.mthmmy.R;
import gr.thmmy.mthmmy.activities.main.forum.ForumFragment;
import gr.thmmy.mthmmy.base.BaseFragment;
import gr.thmmy.mthmmy.utils.CustomRecyclerView;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class ShoutboxFragment extends BaseFragment {

    private static final String TAG = "ShoutboxFragment";

    private MaterialProgressBar progressBar;

    public static ForumFragment newInstance(int sectionNumber) {
        ForumFragment fragment = new ForumFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TAG, TAG);
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_shoutbox, container, false);

        progressBar = rootView.findViewById(R.id.progressBar);
        CustomRecyclerView recyclerView = rootView.findViewById(R.id.shoutbox_recyclerview);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
