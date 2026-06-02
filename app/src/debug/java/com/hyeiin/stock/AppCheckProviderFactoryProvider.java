package com.hyeiin.stock;

import android.util.Log;

import com.google.firebase.appcheck.AppCheckProviderFactory;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;

public final class AppCheckProviderFactoryProvider {
    private AppCheckProviderFactoryProvider() {}

    public static AppCheckProviderFactory getFactory() {
        Log.d("StockAppCheck", "Using DebugAppCheckProviderFactory");
        return DebugAppCheckProviderFactory.getInstance();
    }
}
