package gr.thmmy.mthmmy.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import gr.thmmy.mthmmy.model.Bookmark;

public class BaseViewModel extends ViewModel {
    protected MutableLiveData<Bookmark> currentPageBookmark;

    public LiveData<Bookmark> getCurrentPageBookmark() {
        if (currentPageBookmark == null) {
            currentPageBookmark = new MutableLiveData<>();
        }
        return currentPageBookmark;
    }
}
