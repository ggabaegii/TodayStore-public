package com.hyeiin.stock;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;

public class StockApplication extends Application {
    private static final String TAG = "StockAppCheck";

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck.getInstance()
                .installAppCheckProviderFactory(AppCheckProviderFactoryProvider.getFactory());
        requestAppCheckTokenForDebugLog();
    }

    private void requestAppCheckTokenForDebugLog() {
        if (!BuildConfig.DEBUG) return;

        FirebaseAppCheck.getInstance()
                .getAppCheckToken(false)
                .addOnSuccessListener(token ->
                        Log.d(TAG, "Debug App Check token request completed. Check Logcat tag DebugAppCheckProvider."))
                .addOnFailureListener(error ->
                        Log.e(TAG, "Debug App Check token request failed.", error));
    }
}
