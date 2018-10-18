package gr.thmmy.mthmmy.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
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
