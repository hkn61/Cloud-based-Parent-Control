//package com.example.test;
//
//import android.app.Application;
//import android.util.Log;
//import com.amplifyframework.datastore.generated.model.AmplifyModelProvider;
//
//public class Amplify extends Application {
//
//    public void onCreate() {
//        super.onCreate();
//
//        try {
//            Amplify.configure(getApplicationContext());
//
//            Log.i("MyAmplifyApp", "Initialized Amplify");
//        } catch (AmplifyException e) {
//            Log.e("MyAmplifyApp", "Could not initialize Amplify", e);
//        }
//    }
//}
