package com.hyeiin.stock;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.google.android.material.appbar.MaterialToolbar;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupEdgeToEdge();
    }

    // Edge-to-Edge 기본 설정
    private void setupEdgeToEdge() {
        Window window = getWindow();

        // 시스템 바 영역까지 앱을 확장한다.
        WindowCompat.setDecorFitsSystemWindows(window, false);

        // 상태바를 투명하게 처리한다.
        window.setStatusBarColor(Color.TRANSPARENT);

        // 어두운 배경 기준으로 상태바 아이콘 색상을 설정한다.
        WindowInsetsControllerCompat controller =
                new WindowInsetsControllerCompat(window, window.getDecorView());

        controller.setAppearanceLightStatusBars(false);
    }

    // setContentView 이후 Insets를 적용한다.
    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        applyInsets();
    }

    // content, app bar, FAB에 시스템 Insets를 반영한다.
    protected void applyInsets() {
        View content = findViewById(R.id.contentView);
        View appBar = findViewById(R.id.appBarLayout);
        View fab = findViewById(R.id.fabAdd);

        if (content == null) return;

        ViewCompat.setOnApplyWindowInsetsListener(content, (v, insets) -> {
            int top = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            int navBottom = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            int imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;
            int bottom = Math.max(navBottom, imeBottom);

            if (appBar != null) {
                appBar.setPadding(0, top, 0, 0);
            }

            if (content != null) {
                content.setPadding(0, appBar == null ? top : 0, 0, bottom);
            }

            if (fab != null) {
                fab.setTranslationY(-bottom);
            }

            return insets;
        });

        ViewCompat.requestApplyInsets(content);
    }

    // Toolbar 기본 설정
    protected void setupToolbar(int toolbarId, boolean showBackButton) {
        MaterialToolbar toolbar = findViewById(toolbarId);
        if (toolbar == null) return;

        setSupportActionBar(toolbar);

        if (showBackButton) {
            toolbar.setNavigationOnClickListener(v -> finish());
        }
    }

    // 상태바 색상 설정
    protected void setStatusBarColor(@ColorRes int colorRes) {
        Window window = getWindow();
        int color = ContextCompat.getColor(this, colorRes);

        window.setStatusBarColor(color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isLight = isColorLight(color);
            window.getDecorView().setSystemUiVisibility(
                    isLight ? View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : 0
            );
        }
    }

    private boolean isColorLight(int color) {
        double darkness =
                1 - (0.299 * Color.red(color)
                        + 0.587 * Color.green(color)
                        + 0.114 * Color.blue(color)) / 255;
        return darkness < 0.5;
    }
}
