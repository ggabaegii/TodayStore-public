package com.hyeiin.stock;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.HashMap;
import java.util.Map;

public class AnnouncementWriteActivity extends BaseActivity {

    private MaterialToolbar toolbar;
    private MaterialButtonToggleGroup toggleType;
    private MaterialButton btnTypeAnnouncement;
    private MaterialButton btnTypeSpecial;
    private LinearLayout layoutSpecialNotice;
    private TextInputLayout tilTitle;
    private TextInputLayout tilContent;
    private TextInputEditText etTitle;
    private TextInputEditText etContent;
    private MaterialButton btnSubmit;

    private boolean isOwner = false;
    private int defaultType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_announcement_write);

        isOwner = UserSession.get().isOwner();
        defaultType = getIntent().getIntExtra("defaultType", 0);

        applyInsets();

        initViews();
        setupToolbar();
        setupToggle();
        setupSubmit();

        toggleType.check(defaultType == 1 && isOwner
                ? R.id.btnTypeSpecial : R.id.btnTypeAnnouncement);

        if (!isOwner) {
            btnTypeSpecial.setEnabled(false);
            btnTypeSpecial.setAlpha(0.4f);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        toggleType = findViewById(R.id.toggleGroupType);
        btnTypeAnnouncement = findViewById(R.id.btnTypeAnnouncement);
        btnTypeSpecial = findViewById(R.id.btnTypeSpecial);
        layoutSpecialNotice = findViewById(R.id.layoutSpecialNotice);
        tilTitle = findViewById(R.id.tilTitle);
        etTitle = findViewById(R.id.etTitle);
        tilContent = findViewById(R.id.tilContent);
        etContent = findViewById(R.id.etContent);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupToggle() {
        toggleType.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            layoutSpecialNotice.setVisibility(
                    checkedId == R.id.btnTypeSpecial && !isOwner ? View.VISIBLE : View.GONE
            );
        });
    }

    private void setupSubmit() {
        btnSubmit.setOnClickListener(v -> {
            String title = t(etTitle);
            String content = t(etContent);
            boolean err = false;
            if (title.isEmpty()) {
                tilTitle.setError("제목을 입력해 주세요.");
                err = true;
            } else {
                tilTitle.setError(null);
            }
            if (content.isEmpty()) {
                tilContent.setError("내용을 입력해 주세요.");
                err = true;
            } else {
                tilContent.setError(null);
            }
            if (err) return;

            int selectedType = toggleType.getCheckedButtonId() == R.id.btnTypeSpecial ? 1 : 0;
            setLoading(true);
            saveToFirestore(title, content, selectedType);
        });
    }

    private void saveToFirestore(String title, String content, int type) {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty()) {
            Toast.makeText(this, "매장 정보를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            setLoading(false);
            return;
        }

        Map<String, Object> data = new HashMap<>();
        data.put("title", title);
        data.put("content", content);
        data.put("type", type == 1 ? "SPECIAL" : "ANNOUNCEMENT");
        data.put("author", UserSession.get().getName());
        data.put("authorUid", UserSession.get().getUid());
        data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        data.put("updatedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        FirebaseManager.db()
                .collection("stores").document(storeId)
                .collection("announcements")
                .add(data)
                .addOnSuccessListener(ref -> {
                    setLoading(false);
                    Toast.makeText(this, "등록이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    Toast.makeText(this, "등록 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void setLoading(boolean on) {
        btnSubmit.setEnabled(!on);
        btnSubmit.setText(on ? "등록 중..." : "등록하기");
    }

    private String t(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
