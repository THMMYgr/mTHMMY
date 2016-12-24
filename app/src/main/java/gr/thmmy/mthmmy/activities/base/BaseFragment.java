package gr.thmmy.mthmmy.activities.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import mthmmy.utils.Report;
import okhttp3.OkHttpClient;

public abstract class BaseFragment extends Fragment {
    protected static final String ARG_SECTION_NUMBER = "SectionNumber";
    protected static final String ARG_TAG = "FragmentTAG";

    protected FragmentInteractionListener fragmentInteractionListener;

    private String TAG;
    protected int sectionNumber;
    protected OkHttpClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TAG = getArguments().getString(ARG_TAG);
        sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        client = BaseActivity.getClient();
        Report.d(TAG, "onCreate");
    }

    @Override
    public void onStart() {
        super.onStart();
        Report.d(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Report.d(TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Report.d(TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Report.d(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelTask();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FragmentInteractionListener) {
            fragmentInteractionListener = (FragmentInteractionListener) context;

        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        fragmentInteractionListener = null;
    }

    protected abstract void cancelTask();

    /**
     * This interface MUST be extended by the fragment subclass AND implemented by
     * the activity that contains it, to allow communication upon interaction,
     * between the fragment and the activity/ other fragments
     */
    public interface FragmentInteractionListener {}
}
