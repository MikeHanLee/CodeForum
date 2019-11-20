package com.example.codeforum.ui.friend;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.codeforum.MainActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class FriendViewModel extends ViewModel {

    private MutableLiveData<String> mText;
    public FriendViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is friend fragment");

    }

    public LiveData<String> getText() {
        return mText;
    }
}