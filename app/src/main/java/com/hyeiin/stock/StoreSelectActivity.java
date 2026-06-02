package com.hyeiin.stock;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreSelectActivity extends BaseActivity {
    private static final String INQUIRY_FORM_URL = "https://forms.gle/TRy4mdfB1UhECANQ7";

    private MaterialToolbar toolbar;
    private ImageButton btnProfile;
    private TextView tvGreeting;
    private TextView tvStoreCount;
    private RecyclerView recyclerViewStores;
    private View layoutEmpty;
    private FloatingActionButton fabAddStore;

    private StoreAdapter storeAdapter;
    private final List<StoreItem> storeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_select);
        setupToolbar(R.id.toolbar, false);

        initViews();
        setupListeners();
        loadStoresFromFirestore();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        btnProfile = findViewById(R.id.btnProfile);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvStoreCount = findViewById(R.id.tvStoreCount);
        recyclerViewStores = findViewById(R.id.recyclerViewStores);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        fabAddStore = findViewById(R.id.fabAdd);

        String name = UserSession.get().getName();
        tvGreeting.setText("안녕하세요, " + name + "님");

        storeAdapter = new StoreAdapter(this::enterStore);
        recyclerViewStores.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewStores.setAdapter(storeAdapter);
        recyclerViewStores.setNestedScrollingEnabled(false);
        storeAdapter.setOnLongClickListener(this::showDeleteStoreDialog);
    }

    private void setupListeners() {
        toolbar.setNavigationOnClickListener(v -> navigateToWelcome());
        btnProfile.setOnClickListener(v -> showMyPageDialog());

        fabAddStore.setOnClickListener(v -> {
            if (UserSession.get().isOwner()) {
                showAddStoreDialog();
            } else {
                showJoinByCodeDialog();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToWelcome();
            }
        });
    }

    private void loadStoresFromFirestore() {
        String uid = UserSession.get().getUid();

        FirebaseManager.db().collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        updateUI();
                        return;
                    }

                    List<String> storeIds = (List<String>) doc.get("storeIds");
                    if (storeIds == null || storeIds.isEmpty()) {
                        storeList.clear();
                        updateUI();
                        return;
                    }

                    storeList.clear();
                    final int[] remaining = {storeIds.size()};

                    for (String sid : storeIds) {
                        FirebaseManager.db().collection("stores").document(sid).get()
                                .addOnSuccessListener(storeDoc -> {
                                    if (storeDoc.exists()) {
                                        storeList.add(new StoreItem(
                                                sid,
                                                storeDoc.getString("name"),
                                                ""
                                        ));
                                    }
                                    if (--remaining[0] == 0) {
                                        updateUI();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (--remaining[0] == 0) {
                                        updateUI();
                                    }
                                });
                    }
                })
                .addOnFailureListener(e -> updateUI());
    }

    private void updateUI() {
        boolean empty = storeList.isEmpty();
        recyclerViewStores.setVisibility(empty ? View.GONE : View.VISIBLE);
        tvStoreCount.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        tvStoreCount.setText("총 " + storeList.size() + "개 매장");
        storeAdapter.setStoreList(new ArrayList<>(storeList));
    }

    private void enterStore(StoreItem store) {
        UserSession.get()
                .setStoreId(store.getId())
                .setStoreName(store.getName());

        startActivity(new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void showAddStoreDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_store, null);
        TextInputEditText et = view.findViewById(R.id.etNewStoreName);
        TextInputLayout til = view.findViewById(R.id.tilNewStoreName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("새 매장 추가")
                .setView(view)
                .setPositiveButton("추가", null)
                .setNegativeButton("취소", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positive.setOnClickListener(btn -> {
                String name = et.getText() != null ? et.getText().toString().trim() : "";
                if (name.isEmpty()) {
                    til.setError("매장 이름을 입력해 주세요.");
                    return;
                }
                til.setError(null);
                setDialogLoading(et, positive, negative, true, "추가 중...");
                createNewStoreViaFunction(name, dialog, et, positive, negative);
            });
        });

        dialog.show();
    }

    private void showJoinByCodeDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_store, null);
        TextInputEditText et = view.findViewById(R.id.etNewStoreName);
        TextInputLayout til = view.findViewById(R.id.tilNewStoreName);

        til.setHint("초대 코드");

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("초대 코드로 매장 참가")
                .setView(view)
                .setPositiveButton("참가", null)
                .setNegativeButton("취소", null)
                .create();

        dialog.setOnShowListener(d -> {
            Button positive = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            Button negative = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            positive.setOnClickListener(btn -> {
                String code = et.getText() != null
                        ? et.getText().toString().trim().toUpperCase()
                        : "";
                if (code.isEmpty()) {
                    til.setError("초대 코드를 입력해 주세요.");
                    return;
                }
                til.setError(null);
                setDialogLoading(et, positive, negative, true, "참가 중...");
                joinStoreByCodeViaFunction(code, dialog, et, positive, negative);
            });
        });

        dialog.show();
    }

    private void setDialogLoading(TextInputEditText et,
                                  Button positive,
                                  Button negative,
                                  boolean loading,
                                  String loadingText) {
        et.setEnabled(!loading);
        positive.setEnabled(!loading);
        negative.setEnabled(!loading);
        positive.setText(loading ? loadingText : "확인");
    }

    private void createNewStoreViaFunction(String name,
                                           AlertDialog dialog,
                                           TextInputEditText et,
                                           Button positive,
                                           Button negative) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        FirebaseManager.functions()
                .getHttpsCallable("createStore")
                .call(data)
                .addOnSuccessListener(result -> {
                    Map<String, Object> res = (Map<String, Object>) result.getData();
                    String storeId = res.get("storeId") != null ? res.get("storeId").toString() : "";
                    String storeName = res.get("storeName") != null ? res.get("storeName").toString() : name;
                    storeList.add(new StoreItem(storeId, storeName, ""));
                    updateUI();
                    dialog.dismiss();
                    toast("매장이 추가되었습니다.");
                })
                .addOnFailureListener(e -> {
                    setDialogLoading(et, positive, negative, false, "");
                    positive.setText("추가");
                    toast("매장 추가 실패: " + e.getMessage());
                });
    }

    private void joinStoreByCodeViaFunction(String code,
                                            AlertDialog dialog,
                                            TextInputEditText et,
                                            Button positive,
                                            Button negative) {
        Map<String, Object> data = new HashMap<>();
        data.put("code", code);

        FirebaseManager.functions()
                .getHttpsCallable("acceptStoreInvite")
                .call(data)
                .addOnSuccessListener(result -> {
                    Map<String, Object> res = (Map<String, Object>) result.getData();
                    String storeId = res.get("storeId") != null ? res.get("storeId").toString() : "";
                    String storeName = res.get("storeName") != null ? res.get("storeName").toString() : "";
                    storeList.add(new StoreItem(storeId, storeName, ""));
                    updateUI();
                    dialog.dismiss();
                    toast("매장에 참여했습니다.");
                })
                .addOnFailureListener(e -> {
                    setDialogLoading(et, positive, negative, false, "");
                    positive.setText("참가");
                    toast("초대 코드 확인 실패: " + e.getMessage());
                });
    }

    private void showDeleteStoreDialog(StoreItem store) {
        new AlertDialog.Builder(this)
                .setTitle("매장 제거")
                .setMessage(store.getName())
                .setPositiveButton("삭제", (d, w) -> {
                    storeList.remove(store);
                    updateUI();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void showMyPageDialog() {
        UserSession session = UserSession.get();
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_mypage, null);

        TextView tvName = dialogView.findViewById(R.id.tvMyName);
        TextView tvEmail = dialogView.findViewById(R.id.tvMyEmail);
        TextView tvRole = dialogView.findViewById(R.id.tvMyRole);
        TextView tvStore = dialogView.findViewById(R.id.tvMyStore);
        View rowInviteCode = dialogView.findViewById(R.id.rowInviteCode);
        View btnCopyCode = dialogView.findViewById(R.id.btnCopyCode);
        Button btnDeleteStore = dialogView.findViewById(R.id.btnDeleteStore);
        Button btnInquiry = dialogView.findViewById(R.id.btnMyInquiry);
        Button btnLogout = dialogView.findViewById(R.id.btnMyLogout);

        tvName.setText(session.getName());
        tvEmail.setText(session.getEmail());
        tvRole.setText(session.isOwner() ? "사장" : "직원");
        tvStore.setText(storeList.isEmpty()
                ? "연결된 매장 없음"
                : "연결된 매장 " + storeList.size() + "개");
        rowInviteCode.setVisibility(View.GONE);
        btnCopyCode.setVisibility(View.GONE);
        btnDeleteStore.setVisibility(View.GONE);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        btnInquiry.setOnClickListener(v -> {
            dialog.dismiss();
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INQUIRY_FORM_URL)));
        });

        btnLogout.setOnClickListener(v -> {
            dialog.dismiss();
            new AlertDialog.Builder(this)
                    .setTitle("로그아웃")
                    .setMessage("로그아웃 하시겠습니까?")
                    .setPositiveButton("로그아웃", (d, w) -> {
                        FirebaseManager.auth().signOut();
                        UserSession.clear();
                        Intent intent = new Intent(this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                    })
                    .setNegativeButton("취소", null)
                    .show();
        });

        dialog.show();
    }

    private void navigateToWelcome() {
        Intent intent = new Intent(this, WelcomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    private void toast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
