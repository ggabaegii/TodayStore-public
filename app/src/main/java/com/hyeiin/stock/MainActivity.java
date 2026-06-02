package com.hyeiin.stock;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends BaseActivity implements HomeFragment.OnMenuClickListener {
    private static final String INQUIRY_FORM_URL = "https://forms.gle/TRy4mdfB1UhECANQ7";

    private MaterialToolbar toolbar;
    private TextView tvToolbarStoreName;
    private ImageButton btnProfile;
    private BottomNavigationView bottomNav;

    private String userName = "사용자";
    private String storeName = "매장";
    private int currentNavId = -1;
    private CountDownTimer inviteTimer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra("userName")) {
            userName = getIntent().getStringExtra("userName");
        }
        if (getIntent().hasExtra("storeName")) {
            storeName = getIntent().getStringExtra("storeName");
        }

        setContentView(R.layout.activity_main);
        setStatusBarColor(R.color.colorPrimary);

        initViews();
        setupToolbar();
        setupBottomNav();
        setupBackPress();

        if (savedInstanceState == null) {
            navigateTo(R.id.nav_home);
            bottomNav.setSelectedItemId(R.id.nav_home);
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvToolbarStoreName = findViewById(R.id.tvToolbarStoreName);
        btnProfile = findViewById(R.id.btnProfile);
        bottomNav = findViewById(R.id.bottomNavigation);

        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            if (bottomNav.getSelectedItemId() != R.id.nav_home) {
                bottomNav.setSelectedItemId(R.id.nav_home);
            } else {
                startActivity(new Intent(this, StoreSelectActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        tvToolbarStoreName.setText(UserSession.get().getStoreName());
        btnProfile.setOnClickListener(this::showProfilePopup);
    }

    private void setupBottomNav() {
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == currentNavId) return true;
            navigateTo(id);
            return true;
        });
    }

    private void navigateTo(int navId) {
        currentNavId = navId;

        Fragment fragment;
        String tag;
        String title;

        if (navId == R.id.nav_home) {
            HomeFragment home = new HomeFragment();
            home.setOnMenuClickListener(this);
            fragment = home;
            tag = "HOME";
            title = storeName;
        } else if (navId == R.id.nav_inventory) {
            fragment = new InventoryFragment();
            tag = "INVENTORY";
            title = "재고 관리";
        } else if (navId == R.id.nav_announcement) {
            fragment = new AnnouncementFragment();
            tag = "ANNOUNCEMENT";
            title = "전달사항";
        } else if (navId == R.id.nav_checklist) {
            fragment = new ChecklistFragment();
            tag = "CHECKLIST";
            title = "체크리스트";
        } else {
            return;
        }

        TextView tvSub = toolbar.findViewById(R.id.tvToolbarSubtitle);
        if (tvSub != null) {
            tvSub.setText(navId == R.id.nav_home ? "매장 관리" : title);
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment existing = fm.findFragmentByTag(tag);
        FragmentTransaction ft = fm.beginTransaction();
        ft.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);

        if (existing != null) {
            ft.replace(R.id.fragmentContainer, existing, tag);
        } else {
            ft.replace(R.id.fragmentContainer, fragment, tag);
        }
        ft.commit();
    }

    private void setupBackPress() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (bottomNav.getSelectedItemId() != R.id.nav_home) {
                    bottomNav.setSelectedItemId(R.id.nav_home);
                    return;
                }

                startActivity(new Intent(MainActivity.this, StoreSelectActivity.class)
                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
        });
    }

    @Override
    public void onInventoryClick() {
        bottomNav.setSelectedItemId(R.id.nav_inventory);
    }

    @Override
    public void onAnnouncementClick() {
        bottomNav.setSelectedItemId(R.id.nav_announcement);
    }

    @Override
    public void onSpecialClick() {
        bottomNav.setSelectedItemId(R.id.nav_announcement);
        getSupportFragmentManager().executePendingTransactions();
        fragmentContainer().post(() -> {
            Fragment f = getSupportFragmentManager().findFragmentByTag("ANNOUNCEMENT");
            if (f instanceof AnnouncementFragment) {
                ((AnnouncementFragment) f).selectSpecialTab();
            }
        });
    }

    private View fragmentContainer() {
        return findViewById(R.id.fragmentContainer);
    }

    @Override
    public void onChecklistClick() {
        bottomNav.setSelectedItemId(R.id.nav_checklist);
    }

    private void setupInviteControls(UserSession session,
                                     TextView tvCode,
                                     TextView tvTimer,
                                     android.widget.Button btnIssue,
                                     View btnCopy) {
        if (btnIssue == null || tvCode == null) return;

        boolean owner = session.isOwner();
        btnIssue.setVisibility(owner ? View.VISIBLE : View.GONE);
        if (btnCopy != null) btnCopy.setVisibility(View.GONE);
        if (tvTimer != null) tvTimer.setVisibility(View.GONE);
        if (!owner) return;

        btnIssue.setOnClickListener(v -> issueStoreInvite(tvCode, tvTimer, btnIssue, btnCopy));
        if (btnCopy != null) {
            btnCopy.setOnClickListener(v -> {
                String code = tvCode.getText() != null ? tvCode.getText().toString() : "";
                if (code.isEmpty() || "발급 전".equals(code) || "만료됨".equals(code)) return;
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData.newPlainText("inviteCode", code);
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Toast.makeText(this, "초대 코드가 복사되었습니다.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    private void issueStoreInvite(TextView tvCode,
                                  TextView tvTimer,
                                  android.widget.Button btnIssue,
                                  View btnCopy) {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty()) return;

        FirebaseUser user = FirebaseManager.currentUser();
        if (user == null) {
            Toast.makeText(this, "로그인이 만료되었습니다. 다시 로그인해 주세요.", Toast.LENGTH_SHORT).show();
            FirebaseManager.auth().signOut();
            UserSession.clear();
            startActivity(new Intent(this, LoginActivity.class)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            return;
        }

        btnIssue.setEnabled(false);
        btnIssue.setText("발급 중...");

        Map<String, Object> data = new HashMap<>();
        data.put("storeId", storeId);

        user.getIdToken(false)
                .addOnSuccessListener(tokenResult -> FirebaseManager.functions()
                        .getHttpsCallable("issueStoreInvite")
                        .call(data)
                        .addOnSuccessListener(result -> {
                            Map<String, Object> res = (Map<String, Object>) result.getData();
                            String code = res.get("code") != null ? res.get("code").toString() : "";
                            long expiresAtMillis = 0L;
                            Object expires = res.get("expiresAtMillis");
                            if (expires instanceof Number) {
                                expiresAtMillis = ((Number) expires).longValue();
                            }
                            tvCode.setText(code);
                            if (btnCopy != null) btnCopy.setVisibility(View.VISIBLE);
                            startInviteTimer(expiresAtMillis, tvCode, tvTimer, btnIssue, btnCopy);
                        })
                        .addOnFailureListener(e -> {
                            btnIssue.setEnabled(true);
                            btnIssue.setText("초대 코드 발급");
                            Toast.makeText(this, inviteErrorMessage(e), Toast.LENGTH_LONG).show();
                        }))
                .addOnFailureListener(e -> {
                    btnIssue.setEnabled(true);
                    btnIssue.setText("초대 코드 발급");
                    Toast.makeText(this, "로그인 인증 확인 실패: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String inviteErrorMessage(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "";
        String lower = message.toLowerCase(Locale.ROOT);
        if (lower.contains("unauthenticated")) {
            return "초대 코드 발급 실패: 로그인 또는 App Check 인증이 필요합니다. 다시 로그인한 뒤 시도해 주세요.";
        }
        return "초대 코드 발급 실패: " + message;
    }

    private void startInviteTimer(long expiresAtMillis,
                                  TextView tvCode,
                                  TextView tvTimer,
                                  android.widget.Button btnIssue,
                                  View btnCopy) {
        long remaining = Math.max(0L, expiresAtMillis - System.currentTimeMillis());
        if (tvTimer != null) tvTimer.setVisibility(View.VISIBLE);
        btnIssue.setText("재발급");

        if (inviteTimer != null) inviteTimer.cancel();
        inviteTimer = new CountDownTimer(remaining, 1000L) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000L;
                long minutes = seconds / 60L;
                long restSeconds = seconds % 60L;
                if (tvTimer != null) {
                    tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, restSeconds));
                }
                btnIssue.setEnabled(true);
            }

            @Override
            public void onFinish() {
                tvCode.setText("만료됨");
                if (tvTimer != null) tvTimer.setVisibility(View.GONE);
                if (btnCopy != null) btnCopy.setVisibility(View.GONE);
                btnIssue.setEnabled(true);
                btnIssue.setText("재발급");
            }
        };
        inviteTimer.start();
    }

    private void deleteCurrentStoreViaFunction() {
        String storeId = UserSession.get().getStoreId();
        if (storeId.isEmpty()) return;

        Map<String, Object> data = new HashMap<>();
        data.put("storeId", storeId);

        FirebaseManager.functions()
                .getHttpsCallable("deleteStoreRecursive")
                .call(data)
                .addOnSuccessListener(v -> {
                    Toast.makeText(this, "매장이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, StoreSelectActivity.class)
                            .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void showProfilePopup(View anchor) {
        UserSession s = UserSession.get();
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_mypage, null);

        TextView tvName = dialogView.findViewById(R.id.tvMyName);
        TextView tvEmail = dialogView.findViewById(R.id.tvMyEmail);
        TextView tvRole = dialogView.findViewById(R.id.tvMyRole);
        TextView tvStore = dialogView.findViewById(R.id.tvMyStore);
        TextView tvInvCode = dialogView.findViewById(R.id.tvMyInviteCode);
        TextView tvInviteTimer = dialogView.findViewById(R.id.tvInviteTimer);
        android.widget.Button btnIssueInvite = dialogView.findViewById(R.id.btnIssueInvite);
        View btnCopyCode = dialogView.findViewById(R.id.btnCopyCode);
        View rowInvite = dialogView.findViewById(R.id.rowInviteCode);

        if (tvName != null) tvName.setText(s.getName());
        if (tvEmail != null) tvEmail.setText(s.getEmail());
        if (tvRole != null) tvRole.setText(s.isOwner() ? "사장" : "직원");
        if (tvStore != null) tvStore.setText(s.getStoreName());

        if (rowInvite != null) {
            rowInvite.setVisibility(s.isOwner() ? View.VISIBLE : View.GONE);
        }
        if (tvInvCode != null) {
            tvInvCode.setText("발급 전");
        }
        setupInviteControls(s, tvInvCode, tvInviteTimer, btnIssueInvite, btnCopyCode);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        android.widget.Button btnDeleteStore = dialogView.findViewById(R.id.btnDeleteStore);
        if (btnDeleteStore != null) {
            if (s.isOwner()) {
                btnDeleteStore.setVisibility(View.VISIBLE);
                btnDeleteStore.setOnClickListener(v -> {
                    dialog.dismiss();
                    new AlertDialog.Builder(this)
                            .setTitle("매장 삭제")
                            .setMessage("'" + s.getStoreName() + "' 매장을 삭제하시겠습니까?\n삭제 후에는 되돌릴 수 없습니다.")
                            .setPositiveButton("삭제", (d2, w) -> deleteCurrentStoreViaFunction())
                            .setNegativeButton("취소", null)
                            .show();
                });
            } else {
                btnDeleteStore.setVisibility(View.GONE);
            }
        }

        android.widget.Button btnInquiry = dialogView.findViewById(R.id.btnMyInquiry);
        if (btnInquiry != null) {
            btnInquiry.setOnClickListener(v -> {
                dialog.dismiss();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(INQUIRY_FORM_URL)));
            });
        }

        android.widget.Button btnLogout = dialogView.findViewById(R.id.btnMyLogout);
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> {
                dialog.dismiss();
                new AlertDialog.Builder(this)
                        .setTitle("로그아웃")
                        .setMessage("로그아웃 하시겠습니까?")
                        .setPositiveButton("로그아웃", (d2, w) -> {
                            FirebaseManager.auth().signOut();
                            UserSession.clear();
                            Intent intent = new Intent(this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        })
                        .setNegativeButton("취소", null)
                        .show();
            });
        }

        dialog.show();
    }
}
