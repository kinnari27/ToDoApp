package com.mytasklistapp;

import android.app.Application;
import com.google.firebase.FirebaseApp;

public class TodoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}
