package com.hyeiin.stock;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

public class AnnouncementDetailActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private View viewAccentBar;
    private MaterialCardView cardTypeBadge;
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvDateTime;
    private TextView tvContent;
    private MaterialButton btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_detail);
        applyInsets();

        initViews();
        setupToolbar();
        bindData();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        viewAccentBar = findViewById(R.id.viewDetailAccentBar);
        cardTypeBadge = findViewById(R.id.cardDetailTypeBadge);
        tvTitle = findViewById(R.id.tvDetailTitle);
        tvAuthor = findViewById(R.id.tvDetailAuthor);
        tvDateTime = findViewById(R.id.tvDetailDateTime);
        tvContent = findViewById(R.id.tvDetailContent);
        btnDelete = findViewById(R.id.btnDelete);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindData() {
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String author = getIntent().getStringExtra("author");
        String dateTime = getIntent().getStringExtra("dateTime");
        boolean isSpecial = getIntent().getBooleanExtra("isSpecial", false);
        boolean isOwner = getIntent().getBooleanExtra("isOwner", false);

        toolbar.setTitle(isSpecial ? "특이사항" : "전달사항");

        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvDateTime.setText(dateTime);
        tvContent.setText(content);

        if (isSpecial) {
            viewAccentBar.setVisibility(View.VISIBLE);
            cardTypeBadge.setVisibility(View.VISIBLE);
        }

        String currentUid = UserSession.get().getUid();
        String authorId = getIntent().getStringExtra("authorId");
        boolean canDelete = isOwner || (authorId != null && authorId.equals(currentUid));

        if (canDelete) {
            btnDelete.setVisibility(View.VISIBLE);
            btnDelete.setOnClickListener(v -> showDeleteDialog());
        }
    }

    private void showDeleteDialog() {
        new AlertDialog.Builder(this)
                .setTitle("삭제 확인")
                .setMessage("이 내용을 삭제하시겠습니까?")
                .setPositiveButton("삭제", (d, w) -> {
                    String storeId = UserSession.get().getStoreId();
                    String itemId = getIntent().getStringExtra("itemId");
                    if (!storeId.isEmpty() && itemId != null) {
                        FirebaseManager.db()
                                .collection("stores").document(storeId)
                                .collection("announcements").document(itemId)
                                .delete();
                    }
                    finish();
                })
                .setNegativeButton("취소", null)
                .show();
    }
}
