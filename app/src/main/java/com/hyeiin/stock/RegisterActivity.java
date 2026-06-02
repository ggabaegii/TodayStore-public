package com.hyeiin.stock;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.TypedValue;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends BaseActivity {

    private static final String PRIVACY_POLICY_TEXT =
            "시행일: 2026년 4월 20일\n\n"
                    + "1. 수집하는 개인정보 항목\n"
                    + "- 필수: 이메일 주소, 이름, 역할(사장/직원)\n"
                    + "- 서비스 이용 과정에서 생성되는 정보: 매장명, 매장 참여 정보, 전달사항 작성자명, 체크리스트 완료자명, 재고 수정자명\n"
                    + "- 문의 시 수집 항목: 문의자가 Google Form에 직접 입력한 정보\n\n"
                    + "2. 수집 목적\n"
                    + "- 회원 식별 및 로그인 인증\n"
                    + "- 매장 생성, 직원 초대 및 매장 내 업무 관리 서비스 제공\n"
                    + "- 전달사항, 재고, 체크리스트 등 매장 관리 기능 제공\n"
                    + "- 고객 문의 확인 및 답변\n\n"
                    + "3. 보관 기간\n"
                    + "- 회원 정보: 회원 탈퇴 시 즉시 삭제\n"
                    + "- 매장 데이터: 매장 삭제 시 관련 데이터 삭제\n"
                    + "- 문의 내용: 처리 완료 후 30일 보관\n"
                    + "- 관계 법령에 따라 보관이 필요한 경우 해당 기간 동안 보관할 수 있습니다.\n\n"
                    + "4. 개인정보 처리 위탁 및 제3자 제공\n"
                    + "본 앱은 Google Firebase를 통해 데이터를 저장합니다.\n"
                    + "Firebase를 통해 인증, 데이터 저장, 서버 기능, 앱 보안 확인 등이 처리될 수 있습니다.\n\n"
                    + "본 앱은 사용자의 개인정보를 법령에 따른 경우를 제외하고 제3자에게 판매하거나 제공하지 않습니다.\n\n"
                    + "Google 개인정보처리방침: https://policies.google.com/privacy\n\n"
                    + "5. 사용자 권리\n"
                    + "- 사용자는 언제든지 개인정보 열람, 정정, 삭제를 요청할 수 있습니다.\n"
                    + "- 앱 내 탈퇴 기능 또는 문의 창구를 통해 회원 탈퇴 및 데이터 삭제를 요청할 수 있습니다.\n"
                    + "- 단, 매장 내 공동 데이터는 다른 구성원의 업무 기록과 관련될 수 있어 법령, 분쟁 대응, 서비스 운영상 필요한 범위에서 제한될 수 있습니다.\n"
                    + "- 문의: https://forms.gle/TRy4mdfB1UhECANQ7\n\n"
                    + "6. 만 14세 미만 아동\n"
                    + "본 앱은 만 14세 미만 아동을 대상으로 하지 않으며, 만 14세 미만 아동의 개인정보를 고의로 수집하지 않습니다.\n\n"
                    + "7. 보안 조치\n"
                    + "본 앱은 Firebase Authentication, Firebase 보안 규칙, App Check 등을 통해 비인가 접근을 방지하기 위해 노력합니다.\n"
                    + "다만 사용자는 본인의 계정 정보가 타인에게 노출되지 않도록 주의해야 합니다.\n\n"
                    + "8. 개인정보처리방침 변경\n"
                    + "본 개인정보처리방침은 서비스 변경 또는 관련 법령에 따라 수정될 수 있으며, 변경 시 앱 내 또는 별도 공지 수단을 통해 안내합니다.";

    private static final String TERMS_TEXT =
            "1. 서비스 목적\n"
                    + "본 서비스는 매장 업무 관리, 전달사항, 체크리스트, 재고, 루틴 관리를 돕기 위해 제공됩니다.\n\n"
                    + "2. 회원가입 및 계정 관리\n"
                    + "사용자는 정확한 정보를 입력해야 하며, 본인 계정 정보를 안전하게 관리해야 합니다.\n"
                    + "계정 정보 관리 소홀로 발생한 문제의 책임은 사용자에게 있습니다.\n\n"
                    + "3. 매장 및 데이터 관리\n"
                    + "사용자가 등록한 매장 정보, 전달사항, 체크리스트, 재고, 루틴 등의 내용에 대한 책임은 작성자에게 있습니다.\n"
                    + "매장 내 공동 데이터는 매장 구성원이 함께 열람하거나 사용할 수 있습니다.\n\n"
                    + "4. 금지 행위\n"
                    + "- 타인의 계정 또는 초대 코드를 무단으로 사용하는 행위\n"
                    + "- 허위 정보를 입력하거나 서비스 운영을 방해하는 행위\n"
                    + "- 불법적이거나 부적절한 내용을 등록하는 행위\n\n"
                    + "5. 서비스 변경 및 중단\n"
                    + "서비스 안정성, 보안, 기능 개선을 위해 일부 기능이 변경되거나 일시 중단될 수 있습니다.\n\n"
                    + "6. 이용 제한\n"
                    + "사용자가 약관을 위반하거나 서비스 운영을 방해하는 경우 이용이 제한될 수 있습니다.\n\n"
                    + "7. 책임 제한\n"
                    + "사용자의 부주의로 발생한 계정 노출, 데이터 오입력, 구성원 간 분쟁에 대해 앱 제공자는 책임을 지지 않습니다.\n"
                    + "단, 관련 법령에 따라 앱 제공자의 책임이 인정되는 경우는 예외로 합니다.\n\n"
                    + "8. 약관 변경\n"
                    + "본 약관은 서비스 변경 또는 관련 법령에 따라 수정될 수 있으며, 변경 시 앱 내 또는 별도 공지 수단을 통해 안내합니다.\n\n"
                    + "9. 문의\n"
                    + "서비스 관련 문의는 앱 내 문의 창구를 통해 접수할 수 있습니다.";

    private MaterialButtonToggleGroup toggleRole;
    private TextInputLayout tilName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPassword;
    private TextInputLayout tilPasswordConfirm;
    private TextInputLayout tilStoreName;
    private TextInputEditText etName;
    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private TextInputEditText etPasswordConfirm;
    private TextInputEditText etStoreName;
    private MaterialCheckBox checkPrivacyConsent;
    private MaterialCheckBox checkTermsConsent;
    private TextView tvViewPrivacyPolicy;
    private TextView tvViewTerms;
    private MaterialButton btnRegister;
    private MaterialButton btnRegisterLoading;

    private boolean isOwner = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        applyInsets();
        setupToolbar(R.id.toolbar, true);

        initViews();
        setupRoleToggle();
        setupConsentSection();

        btnRegister.setOnClickListener(v -> attemptRegister());
        findViewById(R.id.tvGoLogin).setOnClickListener(v -> finish());

        toggleRole.check(R.id.btnRoleOwner);
        updateRegisterEnabled();
    }

    private void initViews() {
        toggleRole = findViewById(R.id.toggleRole);
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilPasswordConfirm = findViewById(R.id.tilPasswordConfirm);
        tilStoreName = findViewById(R.id.tilStoreName);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        etStoreName = findViewById(R.id.etStoreName);
        checkPrivacyConsent = findViewById(R.id.checkPrivacyConsent);
        checkTermsConsent = findViewById(R.id.checkTermsConsent);
        tvViewPrivacyPolicy = findViewById(R.id.tvViewPrivacyPolicy);
        tvViewTerms = findViewById(R.id.tvViewTerms);
        btnRegister = findViewById(R.id.btnRegister);
        btnRegisterLoading = findViewById(R.id.btnRegisterLoading);
    }

    private void setupRoleToggle() {
        toggleRole.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) {
                return;
            }
            isOwner = checkedId == R.id.btnRoleOwner;
            tilStoreName.setVisibility(isOwner ? View.VISIBLE : View.GONE);
            clearAllErrors();
        });
    }

    private void setupConsentSection() {
        checkPrivacyConsent.setOnCheckedChangeListener((buttonView, isChecked) -> updateRegisterEnabled());
        checkTermsConsent.setOnCheckedChangeListener((buttonView, isChecked) -> updateRegisterEnabled());

        tvViewPrivacyPolicy.setOnClickListener(v ->
                showPolicyDialog("개인정보처리방침", PRIVACY_POLICY_TEXT));

        tvViewTerms.setOnClickListener(v ->
                showPolicyDialog("서비스 이용약관", TERMS_TEXT));
    }

    private void updateRegisterEnabled() {
        boolean enabled = checkPrivacyConsent.isChecked() && checkTermsConsent.isChecked();
        btnRegister.setEnabled(enabled);
        btnRegister.setAlpha(enabled ? 1f : 0.5f);
    }

    private void showPolicyDialog(String title, String content) {
        ScrollView scrollView = new ScrollView(this);
        int padding = dp(20);
        scrollView.setPadding(padding, dp(8), padding, 0);

        TextView textView = new TextView(this);
        textView.setText(content);
        Linkify.addLinks(textView, Linkify.WEB_URLS);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setLinksClickable(true);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        textView.setLineSpacing(0f, 1.5f);
        textView.setTextColor(getColor(R.color.colorTextPrimary));
        textView.setPadding(0, 0, 0, dp(8));
        scrollView.addView(textView);

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setView(scrollView)
                .setPositiveButton("확인", null)
                .show();
    }

    private void attemptRegister() {
        clearAllErrors();

        if (!checkPrivacyConsent.isChecked() || !checkTermsConsent.isChecked()) {
            toast("필수 약관에 모두 동의해 주세요.");
            return;
        }

        String name = t(etName);
        String email = t(etEmail);
        String password = t(etPassword);
        String passwordConfirm = t(etPasswordConfirm);
        String storeName = t(etStoreName);

        boolean hasError = false;
        if (name.isEmpty()) {
            tilName.setError("이름을 입력해 주세요.");
            hasError = true;
        }
        if (email.isEmpty()) {
            tilEmail.setError("이메일을 입력해 주세요.");
            hasError = true;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("올바른 이메일 형식이 아닙니다.");
            hasError = true;
        }
        if (password.isEmpty()) {
            tilPassword.setError("비밀번호를 입력해 주세요.");
            hasError = true;
        } else if (password.length() < 6) {
            tilPassword.setError("비밀번호는 6자 이상이어야 합니다.");
            hasError = true;
        }
        if (passwordConfirm.isEmpty()) {
            tilPasswordConfirm.setError("비밀번호 확인을 입력해 주세요.");
            hasError = true;
        } else if (!password.equals(passwordConfirm)) {
            tilPasswordConfirm.setError("비밀번호가 일치하지 않습니다.");
            hasError = true;
        }
        if (isOwner && storeName.isEmpty()) {
            tilStoreName.setError("매장 이름을 입력해 주세요.");
            hasError = true;
        }
        if (hasError) {
            return;
        }

        setLoading(true);
        if (isOwner) {
            registerOwnerViaFunction(name, email, password, storeName);
        } else {
            registerStaff(name, email, password);
        }
    }

    private void registerOwnerViaFunction(String name, String email, String password, String storeName) {
        FirebaseManager.auth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("name", name);
                    data.put("email", email);
                    data.put("storeName", storeName);

                    FirebaseManager.functions()
                            .getHttpsCallable("createOwnerProfile")
                            .call(data)
                            .addOnSuccessListener(functionResult -> sendVerificationEmailAndFinish("owner"))
                            .addOnFailureListener(e -> {
                                setLoading(false);
                                FirebaseManager.auth().signOut();
                                toast("매장 생성 실패: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    handleAuthError(e);
                });
    }

    private void registerStaff(String name, String email, String password) {
        FirebaseManager.auth().createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result ->
                        saveStaffUser(result.getUser().getUid(), name, email))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    handleAuthError(e);
                });
    }

    private void saveStaffUser(String uid, String name, String email) {
        Map<String, Object> userDoc = new HashMap<>();
        userDoc.put("name", name);
        userDoc.put("email", email);
        userDoc.put("role", "staff");
        userDoc.put("storeId", "");
        userDoc.put("storeName", "");
        userDoc.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
        userDoc.put("storeIds", new ArrayList<String>());

        FirebaseManager.db().collection("users").document(uid).set(userDoc)
                .addOnSuccessListener(v -> sendVerificationEmailAndFinish("staff"))
                .addOnFailureListener(e -> {
                    setLoading(false);
                    toast("사용자 저장 실패: " + e.getMessage());
                });
    }

    private void sendVerificationEmailAndFinish(String role) {
        com.google.firebase.auth.FirebaseUser user = FirebaseManager.currentUser();
        if (user == null) {
            setLoading(false);
            goLogin();
            return;
        }

        user.sendEmailVerification()
                .addOnSuccessListener(v -> {
                    setLoading(false);
                    String email = user.getEmail() != null ? user.getEmail() : "";
                    FirebaseManager.auth().signOut();

                    String title = "가입 완료";
                    String message;
                    if ("owner".equals(role)) {
                        message = "'" + email + "' 로 인증 메일을 발송했습니다.\n"
                                + "이메일의 링크를 누른 뒤 로그인해 주세요.\n"
                                + "직원 초대 코드는 로그인 후 마이페이지에서 발급할 수 있습니다.";
                    } else {
                        message = "'" + email + "' 로 인증 메일을 발송했습니다.\n"
                                + "이메일의 링크를 누른 뒤 로그인해 주세요.\n"
                                + "매장 참여는 로그인 후 매장 추가 버튼에서 초대 코드를 입력하면 됩니다.";
                    }

                    new AlertDialog.Builder(this)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton("로그인하러 가기", (dialog, which) -> goLogin())
                            .setCancelable(false)
                            .show();
                })
                .addOnFailureListener(e -> {
                    setLoading(false);
                    FirebaseManager.auth().signOut();
                    toast("가입은 완료되었지만 인증 메일 발송에 실패했습니다. 로그인 화면에서 다시 시도해 주세요.");
                    goLogin();
                });
    }

    private void goLogin() {
        startActivity(new Intent(this, LoginActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
    }

    private void handleAuthError(Exception e) {
        String message = e.getMessage() != null ? e.getMessage() : "";
        if (message.contains("already in use")) {
            tilEmail.setError("이미 사용 중인 이메일입니다.");
        } else if (message.contains("badly formatted")) {
            tilEmail.setError("올바른 이메일 형식이 아닙니다.");
        } else {
            toast("회원가입 실패: " + message);
        }
    }

    private void setLoading(boolean loading) {
        btnRegister.setVisibility(loading ? View.GONE : View.VISIBLE);
        btnRegisterLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    private void clearAllErrors() {
        tilName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilPasswordConfirm.setError(null);
        tilStoreName.setError(null);
    }

    private String t(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void toast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
