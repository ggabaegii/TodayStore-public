package com.hyeiin.stock;

import android.util.Log;

import com.google.firebase.appcheck.AppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public final class AppCheckProviderFactoryProvider {
    private AppCheckProviderFactoryProvider() {}

    public static AppCheckProviderFactory getFactory() {
        Log.d("StockAppCheck", "Using PlayIntegrityAppCheckProviderFactory");
        return PlayIntegrityAppCheckProviderFactory.getInstance();
    }
}
