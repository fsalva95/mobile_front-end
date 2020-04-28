package com.example.mobileapplication.ui.selectcars;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SelectCarsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public SelectCarsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is selectcars fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}