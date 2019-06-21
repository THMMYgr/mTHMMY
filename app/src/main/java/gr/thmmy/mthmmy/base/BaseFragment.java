package gr.thmmy.mthmmy.base;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import okhttp3.OkHttpClient;

public abstract class BaseFragment extends Fragment {
    protected static final String ARG_SECTION_NUMBER = "SectionNumber";
    protected static final String ARG_TAG = "FragmentTAG";

    protected FragmentInteractionListener fragmentInteractionListener;

    protected static OkHttpClient client;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //int sectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
        if (client == null)
            client = BaseApplication.getInstance().getClient(); //must check every time - e.g.
        // becomes null when app restarts after crash
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

    /**
     * This interface MUST be extended by the fragment subclass AND implemented by
     * the activity that contains it, to allow communication upon interaction,
     * between the fragment and the activity/ other fragments
     */
    public interface FragmentInteractionListener {}
}
