package com.example.mobileapplication.ui.profiles;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ProfilesViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ProfilesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is profiles fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}