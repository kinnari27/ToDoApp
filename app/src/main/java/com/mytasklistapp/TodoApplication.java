package com.mytasklistapp;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class TodoApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        // Enable offline persistence for Firebase Realtime Database
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
